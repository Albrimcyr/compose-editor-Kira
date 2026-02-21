package example.feb.presentation

import example.feb.data.ChapterRepository
import example.feb.domain.model.Chapter
import formari.composeapp.generated.resources.Res
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

*/


class AppViewModel(
    private val repository: ChapterRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default // not UI
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // -------------------------------------------------------------------------------------
    // Public and Private

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------------------
    // COMMAND QUEUE to avoid desync (Unlimited for now)

    private val commands = Channel<Command>(capacity = Channel.UNLIMITED)

    private sealed interface Command {
        data object Load : Command
        data object AddChapter : Command
        data class  SelectChapter(val id: UUID) : Command
        data class  StartRenaming(val id: UUID) : Command
        data class RenameCommit(val id: UUID, val title: String) : Command
        data class  DeleteChapter(val id: UUID) : Command
        data class  ContentChange(val text: String) : Command
        data class  PersistChapter(val id: UUID) : Command
        data object Esc : Command
        data object ToggleTheme : Command
    }

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Command.Load ->              handleLoad()
                is Command.AddChapter ->        handleAddChapter()
                is Command.SelectChapter ->     handleSelect(cmd.id)
                is Command.StartRenaming ->     handleStartRenaming(cmd.id)
                is Command.RenameCommit ->      handleRenameCommit(cmd.id, cmd.title)
                is Command.DeleteChapter ->     handleDelete(cmd.id)
                is Command.ContentChange ->     handleContentChange(cmd.text)
                is Command.PersistChapter ->    handlePersist(cmd.id)
                is Command.Esc ->               handleEsc()
                is Command.ToggleTheme ->       handleToggleTheme()
            }
        }
    }

    private fun dispatch(command: Command) {
        commands.trySend(command)
    }

    // -------------------------------------------------------------------------------------
    // PUBLIC API

    fun onAddChapter() =                          dispatch(Command.AddChapter)
    fun onSelectChapter(id: UUID) =               dispatch(Command.SelectChapter(id))
    fun onStartRenaming(id: UUID) =               dispatch(Command.StartRenaming(id))
    fun onRenameCommit(id: UUID, title: String) = dispatch(Command.RenameCommit(id, title))
    fun onDeleteChapter(id: UUID) =               dispatch(Command.DeleteChapter(id))
    fun onContentChange(text: String) =           dispatch(Command.ContentChange(text))
    fun onEsc() =                                 dispatch(Command.Esc)
    fun onDel() { uiState.value.selectedId?.let { dispatch(Command.DeleteChapter(it)) } }
    fun onToggleTheme() =                         dispatch(Command.ToggleTheme)

    // -------------------------------------------------------------------------------------

    // debounce 0.5s saving (just in case)
    private var contentSaveJob: Job? = null
    private val contentSaveDelayMs = 500L

    // ADD Load command to the Loop on Init.
    init {
        scope.launch { commandLoop() }
        dispatch(Command.Load)
    }

    fun dispose() {
        contentSaveJob?.cancel()
        contentSaveJob = null
        commands.close()
        scope.cancel()
    }

    // -------------------------------------------------------------------------------------
    // INTERNALS.

    // LOAD (INTERNAL)

    private suspend fun handleLoad() {
        contentSaveJob?.cancel()
        contentSaveJob = null

        _uiState.update { it.copy(isLoading = true, errorMessage = null, editingState = EditingState.None) }

        runCatching { repository.loadAll() }
            .onSuccess { loaded ->

                val selected = _uiState.value.selectedId?.takeIf { id -> loaded.any { it.id == id } }
                    ?: loaded.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapters = loaded,
                        selectedId = selected,
                        editingState = EditingState.None,
                        errorMessage = null
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Load failed: ${e.message ?: "Something happened!"}"
                    )
                }
            }
    }

    // -------------------------------------------------------------------------------------
    // OTHERS (INTERNAL)

    private suspend fun handleAddChapter() {

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
                errorMessage = null
            )
        }

        runCatching { repository.upsert(ch) }  // just ignore. Can be logged later or smth else.

    }

    private suspend fun handleSelect(id: UUID) {
        val exists = _uiState.value.chapters.any { it.id == id }
        if (!exists) return

        _uiState.update { it.copy(selectedId = id, editingState = EditingState.None) }
    }

    private suspend fun handleStartRenaming(id: UUID) {
        if (_uiState.value.chapters.none { it.id == id }) return
        _uiState.update { it.copy(editingState = EditingState.Renaming(id)) }
    }


    private suspend fun handleRenameCommit(id: UUID, title: String) {
        val trimmed = title.trim()
        _uiState.update { it.copy(editingState = EditingState.None) }
        if (trimmed.isEmpty()) return

        val old = _uiState.value.chapters.firstOrNull { it.id == id } ?: return
        val updated = old.copy(title = trimmed)

        _uiState.update { s ->
            s.copy(chapters = s.chapters.map { if (it.id == id) updated else it })
        }

        runCatching { repository.upsert(updated) }
    }

    private suspend fun handleDelete(id: UUID) {
        val state = _uiState.value
        if (state.chapters.none { it.id == id }) return

        if (state.selectedId == id) {
            contentSaveJob?.cancel()
            contentSaveJob = null
        }

        val newList = state.chapters.filterNot { it.id == id }
        val newSelected = when {
            state.selectedId != id -> state.selectedId
            newList.isNotEmpty() -> newList.last().id
            else -> null
        }

        val newEditing = when (val es = state.editingState) {
            is EditingState.Renaming -> if (es.id == id) EditingState.None else es
            EditingState.None -> EditingState.None
        }

        _uiState.update {
            it.copy(
                chapters = newList,
                selectedId = newSelected,
                editingState = newEditing
            )
        }

        runCatching { repository.delete(id) }
    }

    private suspend fun handleContentChange(text: String) {
        val id = _uiState.value.selectedId ?: return
        val old = _uiState.value.chapters.firstOrNull { it.id == id } ?: return
        val updated = old.copy(content = text)

        _uiState.update { s ->
            s.copy(chapters = s.chapters.map { if (it.id == id) updated else it })
        }

        contentSaveJob?.cancel()
        contentSaveJob = scope.launch {
            delay(contentSaveDelayMs)
            dispatch(Command.PersistChapter(id))
        }
    }

    private suspend fun handlePersist(id: UUID) {
        val ch = _uiState.value.chapters.firstOrNull { it.id == id } ?: return
        runCatching { repository.upsert(ch) }
    }

    private suspend fun handleEsc() {
        _uiState.update { it.copy(editingState = EditingState.None) }
    }

    private suspend fun handleToggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

}