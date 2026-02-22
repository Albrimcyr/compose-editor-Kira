package example.feb.presentation

import example.feb.data.ChapterRepository
import example.feb.domain.model.Chapter
import example.feb.domain.text.HtmlContentNormalizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel

import java.util.UUID


/** MVVM ViewModel

   * -- one source of truth (single [AppUiState])
   * -- deterministic ordering, commands!
   * -- async protected! With draft content in UI.

*/


class AppViewModel(
    private val repository: ChapterRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default // not UI
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ── State ────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // ── Command Queue ────────────────────────────────────────────────────────────────────────────────────────────────

    private val commands = Channel<Command>(capacity = Channel.UNLIMITED)

    private sealed interface Command {
        data object Load : Command
        data object AddChapter : Command
        data class  SelectChapter   (val id: UUID) : Command
        data class  StartRenaming   (val id: UUID) : Command
        data class  RenameCommit    (val id: UUID, val title: String) : Command
        data class  DeleteChapter   (val id: UUID) : Command
        data class  ContentChanged  (val id: UUID, val html: String) : Command
        data class  CommitDraft     (val id: UUID) : Command
        data object Esc : Command
        data object ToggleTheme : Command
    }

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Command.Load ->                  handleLoad()
                is Command.AddChapter ->            handleAddChapter()
                is Command.SelectChapter ->         handleSelect(cmd.id)
                is Command.StartRenaming ->         handleStartRenaming(cmd.id)
                is Command.RenameCommit ->          handleRenameCommit(cmd.id, cmd.title)
                is Command.DeleteChapter ->         handleDelete(cmd.id)
                is Command.ContentChanged ->        handleContentChanged(cmd.id, cmd.html)
                is Command.CommitDraft ->           handleCommitDraft(cmd.id)
                is Command.Esc ->                   handleEsc()
                is Command.ToggleTheme ->           handleToggleTheme()
            }
        }
    }

    private fun dispatch(command: Command) {
        commands.trySend(command)
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────────────────────────────────────────────

    // Discrete Commands
    fun onAddChapter() =                            dispatch(Command.AddChapter)
    fun onSelectChapter(id: UUID) =                 dispatch(Command.SelectChapter(id))
    fun onStartRenaming(id: UUID) =                 dispatch(Command.StartRenaming(id))
    fun onRenameCommit(id: UUID, title: String) =   dispatch(Command.RenameCommit(id, title))
    fun onContentChange(id: UUID, html: String) =   dispatch(Command.ContentChanged(id, html))
    fun onDeleteChapter(id: UUID) =                 dispatch(Command.DeleteChapter(id))
    fun onEsc() =                                   dispatch(Command.Esc)
    fun onDel() { uiState.value.selectedId?.let {   dispatch(Command.DeleteChapter(it)) } }
    fun onToggleTheme() =                           dispatch(Command.ToggleTheme)

    // ── INIT / DISPOSE ───────────────────────────────────────────────────────────────────────────────────────────────

    init {
        scope.launch { commandLoop() }
        dispatch(Command.Load)
    }

    fun dispose() {
        commands.close()
        scope.cancel()
    }

    // ── LOAD ─────────────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun handleLoad() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, editingState = EditingState.None) }

        runCatching { repository.loadAll() }
            .onSuccess { raw ->
                val loaded = raw.map { ch ->
                    val normalized = HtmlContentNormalizer.normalizeToHtml(ch.content)
                    if (normalized == ch.content) ch else ch.copy(content = normalized)
                }

                val selected = _uiState.value.selectedId
                    ?.takeIf { id -> loaded.any { it.id == id } }
                    ?: loaded.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapters = loaded,
                        selectedId = selected,
                        editingState = EditingState.None,
                        errorMessage = null,
                        draftChapterId = null,
                        draftHtml = "",
                        isDraftDirty = false,
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Load failed: ${e.message ?: "Unknown error"}")
                }
            }
    }


    // ── HANDLERS ─────────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun handleAddChapter() {
        flushDraft()

        val ch = Chapter(
            id = UUID.randomUUID(),
            title = "New chapter",
            content = ""
        )

        _uiState.update { state ->
            state.copy(
                chapters = state.chapters + ch,
                selectedId = ch.id,
                editingState = EditingState.None,
                errorMessage = null,
                draftChapterId = null,
                draftHtml = "",
                isDraftDirty = false,
            )
        }
        runCatching { repository.upsert(ch) } // just ignore. Can be logged later or smth else.

    }

    private suspend fun handleSelect(id: UUID) {

        val state = _uiState.value
        if (state.selectedId == id) return
        if (state.chapters.none { it.id == id }) return

        flushDraft()

        _uiState.update {
            it.copy(
                selectedId = id,
                editingState = EditingState.None,
                draftChapterId = null,
                draftHtml = "",
                isDraftDirty = false,
            )
        }
    }

    private suspend fun handleStartRenaming(id: UUID) {
        if (_uiState.value.chapters.none { it.id == id }) return
        _uiState.update { it.copy(editingState = EditingState.Renaming(id)) }
    }


    private suspend fun handleRenameCommit(id: UUID, title: String) {
        val trimmed = title.trim()
        _uiState.update { it.copy(editingState = EditingState.None) }
        if (trimmed.isEmpty()) return

        val updated = updateChapter(id) { it.copy(title = trimmed) } ?: return
        runCatching { repository.upsert(updated) }
    }

    private suspend fun handleDelete(id: UUID) {
        val state = _uiState.value
        if (state.chapters.none { it.id == id }) return

        val isDeletingSelected = state.selectedId == id
        if (isDeletingSelected) {
            cancelDraft()
        } else {
            flushDraft()
        }

        val newList = state.chapters.filterNot { it.id == id }
        val newEditing = when (val es = state.editingState) {
            is EditingState.Renaming -> if (es.id == id) EditingState.None else es
            EditingState.None -> EditingState.None
        }

        val newSelected = if (isDeletingSelected) newList.firstOrNull()?.id else state.selectedId

        _uiState.update {
            it.copy(
                chapters = newList,
                selectedId = newSelected,
                editingState = newEditing,
                draftChapterId = if (isDeletingSelected) null else it.draftChapterId,
                draftHtml = if (isDeletingSelected) "" else it.draftHtml,
                isDraftDirty = if (isDeletingSelected) false else it.isDraftDirty,
            )
        }
        runCatching { repository.delete(id) }
    }

    private suspend fun handleContentChanged(id: UUID, html: String) {
        if (_uiState.value.selectedId != id) return

        _uiState.update {
            it.copy(draftChapterId = id, draftHtml = html, isDraftDirty = true)
        }
        scheduleDebouncedCommit(id)
    }

    private suspend fun handleCommitDraft(id: UUID) {
        val state = _uiState.value
        if (state.draftChapterId != id || !state.isDraftDirty) return

        val updated = updateChapter(id) { it.copy(content = state.draftHtml) } ?: return
        _uiState.update { it.copy(isDraftDirty = false) }
        runCatching { repository.upsert(updated) }
    }


    private suspend fun handleEsc() {
        _uiState.update { it.copy(editingState = EditingState.None) }
    }

    private suspend fun handleToggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }

    }


    // ── DRAFT HELPERS ────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun flushDraft() {
        val state = _uiState.value
        val id = state.draftChapterId ?: return
        if (!state.isDraftDirty) return

        persistJob?.cancel()
        persistJob = null

        val updated = updateChapter(id) { it.copy(content = state.draftHtml) } ?: return
        _uiState.update { it.copy(isDraftDirty = false) }
        runCatching { repository.upsert(updated) }
    }

    private fun cancelDraft() {
        persistJob?.cancel()
        persistJob = null
    }

    // ── DEBOUNCE  ────────────────────────────────────────────────────────────────────────────────────────────────────
    private val persistDebounceMs = 1_000L
    private var persistJob: Job? = null

    private fun scheduleDebouncedCommit(id: UUID) {
        persistJob?.cancel()
        persistJob = scope.launch {
            delay(persistDebounceMs)
            dispatch(Command.CommitDraft(id))
        }
    }

    // ── HELPERS  ─────────────────────────────────────────────────────────────────────────────────────────────────────

    private fun updateChapter(id: UUID, transform: (Chapter) -> Chapter): Chapter? {
        val current = _uiState.value.chapters.firstOrNull { it.id == id } ?: return null
        val updated = transform(current)
        _uiState.update { state ->
            state.copy(chapters = state.chapters.map { if (it.id == id) updated else it })
        }
        return updated
    }

}
