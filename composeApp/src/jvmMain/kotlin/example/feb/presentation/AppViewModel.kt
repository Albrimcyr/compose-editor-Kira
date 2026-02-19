package example.feb.presentation

import example.feb.data.ChapterRepository
import example.feb.domain.model.Chapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import java.util.UUID

class AppViewModel(
    private val repository: ChapterRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default // not main, because not UI
) {

    // if one down = keep working in
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // private > public StateFlow (to change and notify)
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // order and quick O(1) instead of chapters.first { it.id == id } O(n)
    private val chaptersById: MutableMap<UUID, Chapter> = mutableMapOf()
    private val order: MutableList<UUID> = mutableListOf()

    // debounce 0.5s saving (just in case)
    private var contentSaveJob: Job? = null
    private val contentSaveDelayMs = 500L

    // load data
    init {
        scope.launch { load() }
    }

    fun dispose() {
        scope.cancel()
    }

    // how to load
    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        runCatching { repository.loadAll() }
            .onSuccess { loaded ->
                chaptersById.clear() // clean previous
                order.clear()        //

                loaded.forEach { ch ->
                    chaptersById[ch.id] = ch  // update
                    order += ch.id            //
                }

                _uiState.update { old ->
                    val rows = order.mapNotNull { id ->
                        chaptersById[id]?.let { ChapterRowUi(it.id, it.title) } // convert to needed data for the sidebar
                    }
                    val selectedId = old.selectedId?.takeIf { chaptersById.containsKey(it) }
                    val selected = selectedId?.let { chaptersById[it] }

                    old.copy(
                        isLoading = false,
                        chapters = rows,
                        selectedId = selectedId,
                        hasSelection = selected != null,
                        selectedTitle = selected?.title ?: "",
                        selectedContent = selected?.content ?: "",
                        editingState = EditingState.None
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Load failed: ${e.message ?: "unknown error"}" // future-proof
                    )
                }
            }
    }

    fun onAddChapter() {
        val ch = Chapter(
            id = UUID.randomUUID(),
            title = "New chapter",
            content = ""
        )

        chaptersById[ch.id] = ch
        order += ch.id

        // quick UI update
        selectInternal(ch.id)
        _uiState.update { it.copy(editingState = EditingState.None) }
        rebuildChapterRows()

        // quick save
        scope.launch { repository.upsert(ch) }
    }

    fun onSelectChapter(id: UUID) {
        if (!chaptersById.containsKey(id)) return
        selectInternal(id)
        _uiState.update { it.copy(editingState = EditingState.None) }
    }

    fun onStartRenaming(id: UUID) {
        val ch = chaptersById[id] ?: return
        _uiState.update { it.copy(editingState = EditingState.Renaming(id, ch.title)) }
    }

    fun onRenameDraftChange(text: String) {
        _uiState.update { s ->
            val es = s.editingState
            if (es is EditingState.Renaming) s.copy(editingState = es.copy(draft = text))
            else s
        }
    }

    fun onRenameCommit() {
        val current = _uiState.value.editingState
        if (current !is EditingState.Renaming) return

        val id = current.id
        val trimmed = current.draft.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(editingState = EditingState.None) }
            return
        }

        val old = chaptersById[id] ?: return
        val updated = old.copy(title = trimmed)
        chaptersById[id] = updated

        // ui
        if (_uiState.value.selectedId == id) {
            _uiState.update {
                it.copy(
                    selectedTitle = trimmed,
                    editingState = EditingState.None
                )
            }
        } else {
            _uiState.update { it.copy(editingState = EditingState.None) }
        }
        rebuildChapterRows()

        scope.launch { repository.upsert(updated) }
    }

    fun onDeleteChapter(id: UUID) {
        if (!chaptersById.containsKey(id)) return

        chaptersById.remove(id)
        order.remove(id)

        // delete? clean selection
        _uiState.update { s ->
            val wasSelected = s.selectedId == id
            val wasRenaming = (s.editingState as? EditingState.Renaming)?.id == id

            s.copy(
                selectedId = if (wasSelected) null else s.selectedId,
                hasSelection = if (wasSelected) false else s.hasSelection,
                selectedTitle = if (wasSelected) "" else s.selectedTitle,
                selectedContent = if (wasSelected) "" else s.selectedContent,
                editingState = if (wasRenaming) EditingState.None else s.editingState
            )
        }

        rebuildChapterRows()

        scope.launch { repository.delete(id) }
    }

    fun onContentChange(text: String) {
        val id = _uiState.value.selectedId ?: return
        val ch = chaptersById[id] ?: return

        // ui update !
        chaptersById[id] = ch.copy(content = text)
        _uiState.update { it.copy(selectedContent = text) }

        // debounce
        contentSaveJob?.cancel()
        contentSaveJob = scope.launch {
            delay(contentSaveDelayMs)
            val latest = chaptersById[id] ?: return@launch
            repository.upsert(latest)
        }
    }

    fun onEsc() {
        _uiState.update { it.copy(editingState = EditingState.None) }
    }

    fun onDel() {
        val id = _uiState.value.selectedId ?: return
        onDeleteChapter(id)
    }

    // Helpers :)

    private fun selectInternal(id: UUID) {
        val ch = chaptersById[id] ?: return
        _uiState.update {
            it.copy(
                selectedId = id,
                hasSelection = true,
                selectedTitle = ch.title,
                selectedContent = ch.content
            )
        }
    }

    private fun rebuildChapterRows() {
        val rows = order.mapNotNull { id ->
            chaptersById[id]?.let { ChapterRowUi(it.id, it.title) }
        }
        _uiState.update { it.copy(chapters = rows) }
    }

}