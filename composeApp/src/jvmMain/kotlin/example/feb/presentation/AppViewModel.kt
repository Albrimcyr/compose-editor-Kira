package example.feb.presentation

import example.feb.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel

import java.util.UUID


/** MVVM + UDF ViewModel

   * -- one source of truth (single [AppUiState])
   * -- unidirectional data flow: UI emits intentions!
   * -- sequential command processing!
   * -- frequent content updates stay in-memory!
   * -- testability: isolated UseCases!
   * -- something else... is planned?

**/


class AppViewModel(
    private val loadChapters:       LoadChaptersUseCase,
    private val addChapter:         AddChapterUseCase,
    private val renameChapter:      RenameChapterUseCase,
    private val deleteChapter:      DeleteChapterUseCase,
    private val saveChapterContent: SaveChapterContentUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.Default // not UI
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ── State ────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // ── Command Queue ────────────────────────────────────────────────────────────────────────────────────────────────

    private val commands = Channel<Command>(capacity = Channel.UNLIMITED)

    private sealed interface Command {
        data object Load                                                : Command
        data object AddChapter                                          : Command
        data class  SelectChapter   (val id: UUID)                      : Command
        data class  StartRenaming   (val id: UUID)                      : Command
        data class  RenameCommit    (val id: UUID, val title: String)   : Command
        data class  DeleteChapter   (val id: UUID)                      : Command
        data class  ContentChanged  (val id: UUID, val html: String)    : Command
        data class  CommitDraft     (val id: UUID)                      : Command
        data object Esc                                                 : Command
        data object ToggleTheme                                         : Command
    }

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Command.Load                 -> handleLoad()
                is Command.AddChapter           -> handleAddChapter()
                is Command.SelectChapter        -> handleSelect(cmd.id)
                is Command.StartRenaming        -> handleStartRenaming(cmd.id)
                is Command.RenameCommit         -> handleRenameCommit(cmd.id, cmd.title)
                is Command.DeleteChapter        -> handleDelete(cmd.id)
                is Command.ContentChanged       -> handleContentChanged(cmd.id, cmd.html)
                is Command.CommitDraft          -> handleCommitDraft(cmd.id)
                is Command.Esc                  -> handleEsc()
                is Command.ToggleTheme          -> handleToggleTheme()
            }
        }
    }

    private fun dispatch(command: Command) {
        commands.trySend(command)
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────────────────────────────────────────────

    // Discrete Commands
    fun onAddChapter()                              = dispatch(Command.AddChapter)
    fun onSelectChapter(id: UUID)                   = dispatch(Command.SelectChapter(id))
    fun onStartRenaming(id: UUID)                   = dispatch(Command.StartRenaming(id))
    fun onRenameCommit(id: UUID, title: String)     = dispatch(Command.RenameCommit(id, title))
    fun onContentChange(id: UUID, html: String)     = dispatch(Command.ContentChanged(id, html))
    fun onDeleteChapter(id: UUID)                   = dispatch(Command.DeleteChapter(id))
    fun onEsc()                                     = dispatch(Command.Esc)
    fun onDel() { uiState.value.selectedId?.let {     dispatch(Command.DeleteChapter(it)) } }
    fun onToggleTheme()                             = dispatch(Command.ToggleTheme)

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

        loadChapters()

            .onSuccess { loaded ->
                val selected = _uiState.value.selectedId
                    ?.takeIf { id -> loaded.any { it.id == id } }
                    ?: loaded.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        isLoading      = false,
                        chapters       = loaded,
                        selectedId     = selected,
                        editingState   = EditingState.None,
                        errorMessage   = null,
                        draftChapterId = null,
                        draftHtml      = "",
                        isDraftDirty   = false,
                    )
                }
            }

            .onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Load failed: ${e.message}")
                }
            }
    }


    // ── HANDLERS ─────────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun handleAddChapter() {
        flushDraft()

        addChapter()
            .onSuccess { chapter ->
                _uiState.update { state ->
                    state.copy(
                        chapters       = state.chapters + chapter,
                        selectedId     = chapter.id,
                        editingState   = EditingState.None,
                        errorMessage   = null,
                        draftChapterId = null,
                        draftHtml      = "",
                        isDraftDirty   = false,
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Failed to add chapter...?: ${e.message}") } // Just in case.
            }
    }

    private suspend fun handleSelect(id: UUID) {

        val state = _uiState.value
        if (state.selectedId == id) return
        if (state.chapters.none { it.id == id }) return

        flushDraft()

        _uiState.update {
            it.copy(
                selectedId     = id,
                editingState   = EditingState.None,
                draftChapterId = null,
                draftHtml      = "",
                isDraftDirty   = false,
            )
        }
    }

    private suspend fun handleStartRenaming(id: UUID) {
        if (_uiState.value.chapters.none { it.id == id }) return
        _uiState.update { it.copy(editingState = EditingState.Renaming(id)) }
    }


    private suspend fun handleRenameCommit(id: UUID, rawTitle: String) {
        _uiState.update { it.copy(editingState = EditingState.None) }

        when (val result = renameChapter(id, rawTitle, _uiState.value.chapters)) {

            is RenameResult.Success -> {
                _uiState.update { state ->
                    state.copy(chapters = state.chapters.map {
                        if (it.id == id) result.updated else it
                    })
                }
            }

            is RenameResult.BlankTitle      -> { }
            is RenameResult.ChapterNotFound -> { }
            is RenameResult.Error -> { _uiState.update { it.copy(errorMessage = "Rename failed..?: ${result.cause.message}") }
            }

        }
    }

    private suspend fun handleDelete(id: UUID) {
        val state = _uiState.value
        if (state.chapters.none { it.id == id }) return

        val isDeletingSelected = state.selectedId == id
        if (isDeletingSelected) cancelDraft() else flushDraft()

        val newChapters = state.chapters.filterNot { it.id == id }
        val newSelected = if (isDeletingSelected) newChapters.firstOrNull()?.id else state.selectedId
        val newEditing  = when (val es = state.editingState) {
            is EditingState.Renaming    -> if (es.id == id) EditingState.None else es
            EditingState.None           -> EditingState.None
        }

        _uiState.update {
            it.copy(
                chapters       = newChapters,
                selectedId     = newSelected,
                editingState   = newEditing,
                draftChapterId = if (isDeletingSelected) null  else it.draftChapterId,
                draftHtml      = if (isDeletingSelected) ""    else it.draftHtml,
                isDraftDirty   = if (isDeletingSelected) false else it.isDraftDirty,
            )
        }

        deleteChapter(id)
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Delete failed: ${e.message}") }
            }
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

        saveChapterContent(id, state.draftHtml, state.chapters)
            .onSuccess { updated ->
                _uiState.update { s ->
                    s.copy(
                        isDraftDirty = false,
                        chapters     = s.chapters.map { if (it.id == id) updated else it },
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Save failed: ${e.message}") }
            }
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

        cancelDraft()

        saveChapterContent(id, state.draftHtml, state.chapters)
            .onSuccess { updated ->
                _uiState.update { s ->
                    s.copy(
                        isDraftDirty = false,
                        chapters     = s.chapters.map { if (it.id == id) updated else it },
                    )
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Auto-save failed...?: ${e.message}") }
            }
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

}
