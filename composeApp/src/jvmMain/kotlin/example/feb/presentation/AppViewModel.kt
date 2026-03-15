package example.feb.presentation

import example.feb.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel

import java.util.UUID


/** MVVM + UDF-like ViewModel (hybrid)

   * -- one source of truth (single [AppUiState])
   * -- sequential command processing via Channel
   * -- frequent content updates stay in-memory until debounce
   * -- testability: isolated UseCases
   * -- something else... is planned?

**/


class AppViewModel(
    private val loadChapters:       LoadChaptersUseCase,
    private val addChapter:         AddChapterUseCase,
    private val renameChapter:      RenameChapterUseCase,
    private val deleteChapter:      DeleteChapterUseCase,
    private val saveChapterContent: SaveChapterContentUseCase,
    private val setChapterZoom:     SetChapterZoomUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // ── State ────────────────────────────────────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // ── Command Queue ────────────────────────────────────────────────────────────────────────────────────────────────

    private val commands = Channel<Command>(capacity = Channel.BUFFERED)

    private enum class ZoomDirection { In, Out }

    private sealed interface Command {

        // Serious commands
        data object Load                                                        : Command
        data object AddChapter                                                  : Command
        data class  DeleteChapter   (val id: UUID)                              : Command
        data class  SelectChapter   (val id: UUID)                              : Command
        data object CloseChapter                                                : Command
        data class  RenameCommit    (val id: UUID, val title: String)           : Command
        data class  SaveDraft       (val id: UUID, val revision: Long)          : Command
        data class  ZoomStep        (val direction: ZoomDirection)              : Command

    }

    private suspend fun commandLoop() {
        for (cmd in commands) {
            when (cmd) {
                is Command.Load                 -> handleLoad()
                is Command.AddChapter           -> handleAddChapter()
                is Command.DeleteChapter        -> handleDelete(cmd.id)
                is Command.SelectChapter        -> handleSelect(cmd.id)
                is Command.RenameCommit         -> handleRenameCommit(cmd.id, cmd.title)
                is Command.SaveDraft            -> handleSaveDraft(cmd.id, cmd.revision)
                is Command.CloseChapter         -> handleCloseChapter()
                is Command.ZoomStep             -> handleZoomStep(cmd.direction)
            }
        }
    }

    private fun dispatch(command: Command) {
        commands.trySend(command)
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────────────────────────────────────────────

    // Serious queued workflow actions. Command Loop.
    fun onAddChapter()                              = dispatch(Command.AddChapter)
    fun onSelectChapter(id: UUID)                   = dispatch(Command.SelectChapter(id))
    fun onRenameCommit(id: UUID, title: String)     = dispatch(Command.RenameCommit(id, title))
    fun onDeleteChapter(id: UUID)                   = dispatch(Command.DeleteChapter(id))
    fun onDel() { uiState.value.selectedId?.let {     dispatch(Command.DeleteChapter(it)) } }
    fun onCloseChapter()                            = dispatch(Command.CloseChapter)
    fun onIncreaseZoom()                            = dispatch(Command.ZoomStep(ZoomDirection.In))
    fun onDecreaseZoom()                            = dispatch(Command.ZoomStep(ZoomDirection.Out))

    // Simple UI actions (synchronous reducers), no IO
    fun onStartRenaming(id: UUID)                   = reduceStartRenaming(id)
    fun onEsc()                                     = reduceCancelEditing()
    fun onToggleTheme()                             = reduceToggleTheme()
    fun onSearchQueryChanged(query: String)         = reduceSearchQueryChanged(query)
    fun onClearSearch()                             = reduceClearSearch()
    fun onToggleToolbar()                           = reduceToggleToolbar()
    fun onContentChange(id: UUID, markdown: String) = reduceEditorInput(id, markdown)
    fun onPlainTextChanged(text: String)            = scheduleDebouncedStatsUpdate(text)

    // ── INIT / DISPOSE ───────────────────────────────────────────────────────────────────────────────────────────────

    init {
        scope.launch { commandLoop() }
        dispatch(Command.Load)
    }

    fun dispose() {
        cancelPendingSave()
        cancelPendingStats()

        scope.launch { persistDraftIfNeeded() }.invokeOnCompletion {
            commands.close()
            scope.cancel()
        }
    }

    // ── Direct Reducers ──────────────────────────────────────────────────────────────────────────────────────────────

    private val zoomSteps = listOf(100, 125, 150, 175, 200, 225, 250, 275, 300)
    private var draftRevision: Long = 0L // helper, local // might be moved

    private fun reduceStartRenaming(id: UUID) {
        if (_uiState.value.chapters.none { it.id == id }) return
        _uiState.update { it.copy(editingState = EditingState.Renaming(id)) }
    }

    private fun reduceCancelEditing() =
        _uiState.update { it.copy(editingState = EditingState.None) }

    private fun reduceToggleTheme() =
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }

    private fun reduceSearchQueryChanged(query: String) =
        _uiState.update { it.copy(searchQuery = query) }

    private fun reduceClearSearch() =
        _uiState.update { it.copy(searchQuery = "") }

    private fun reduceToggleToolbar() =
        _uiState.update { it.copy(isToolbarVisible = !it.isToolbarVisible) }

    private fun reduceEditorInput(id: UUID, markdown: String) {
        val state = _uiState.value
        if (state.selectedId != id) return

        draftRevision += 1
        val revision = draftRevision

        _uiState.update { it.copy(draft = DraftState.Dirty(chapterId = id, markdown = markdown)) }

        scheduleDebouncedSave(id, revision)
    }

    // ── LOAD ─────────────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun handleLoad() {

        cancelPendingSave()
        cancelPendingStats()

        _uiState.update { it.copy(isLoading = true, errorMessage = null, editingState = EditingState.None) }

        loadChapters()

                .onSuccess { loaded ->

                    val selectedChapter = _uiState.value.selectedId
                        ?.let { id -> loaded.firstOrNull { it.id == id } }
                        ?: loaded.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoading      = false,
                            chapters       = loaded,
                            selectedId     = selectedChapter?.id,
                            editingState   = EditingState.None,
                            errorMessage   = null,
                            draft          = DraftState.Clean,
                            contentStats   = computeContentStats(selectedChapter?.content.orEmpty())
                        )
                    }
                }

            .onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Load failed: ${e.message}")
                }
            }
    }


    // ── COMMAND HANDLERS ─────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun handleAddChapter() {

        persistDraftIfNeeded()
        cancelPendingStats()

        addChapter()
            .onSuccess { chapter ->
                _uiState.update { state ->
                    state.copy(
                        chapters       = state.chapters + chapter,
                        selectedId     = chapter.id,
                        editingState   = EditingState.None,
                        errorMessage   = null,
                        draft          = DraftState.Clean,
                        contentStats   = ContentStats.Empty,
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

        val chapter = state.chapters.firstOrNull { it.id == id } ?: return

        persistDraftIfNeeded()
        cancelPendingStats()

        _uiState.update {
            it.copy(
                selectedId     = id,
                editingState   = EditingState.None,
                draft          = DraftState.Clean,
                errorMessage   = null,
                contentStats   = computeContentStats(chapter.content),
            )
        }
    }


    private suspend fun handleRenameCommit(id: UUID, rawTitle: String) {
        _uiState.update { it.copy(editingState = EditingState.None) }

        when (val result = renameChapter(id, rawTitle)) {

            is RenameResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        chapters     = state.chapters.map { if (it.id == id) result.updated else it },
                        errorMessage = null,
                    )
                }
            }

            is RenameResult.BlankTitle      -> Unit
            is RenameResult.ChapterNotFound -> Unit
            is RenameResult.Error -> { _uiState.update {
                it.copy(errorMessage = "Rename failed..?: ${result.cause.message}") }
            }

        }
    }

    private suspend fun handleDelete(id: UUID) {
        val state = _uiState.value

        // safe
        val deletedIndex = state.chapters.indexOfFirst { it.id == id }
        if  (deletedIndex == -1) return

        val isDeletingSelected = state.selectedId == id

        if (isDeletingSelected) {
            cancelPendingSave()
            cancelPendingStats()
        } else {
            persistDraftIfNeeded()
        }

        val newChapters = state.chapters.filterNot { it.id == id }

        // choosing Selected ID (future implementation)
        val newSelectedId = when {
            !isDeletingSelected                 -> state.selectedId
            newChapters.isEmpty()               -> null
            deletedIndex >= newChapters.size    -> newChapters.last().id
            else                                -> newChapters[deletedIndex].id
        }

        // to calculate stats (and maybe something in future)
        val newSelectedChapter = newSelectedId?.let { selectedId ->
            newChapters.firstOrNull { it.id == selectedId }
        }

        val newEditingState = when (val editingState = state.editingState) {
            is EditingState.Renaming    -> if (editingState.id == id) EditingState.None else editingState
            is EditingState.None        -> EditingState.None
        }

        // delete logic with an ability to delete without opening in the future
        _uiState.update {
            it.copy(
                chapters       = newChapters,
                selectedId     = newSelectedId,
                editingState   = newEditingState,
                draft          = if (isDeletingSelected) DraftState.Clean else it.draft,
                contentStats   = if (isDeletingSelected) {
                    computeContentStats(newSelectedChapter?.content.orEmpty())
                } else { it.contentStats },
            )
        }

        deleteChapter(id)
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Delete failed..?: ${e.message}") }
                dispatch(Command.Load)
            }
    }

    private suspend fun handleSaveDraft(id: UUID, revision: Long) {
        val state = _uiState.value

        val draft = state.draft as? DraftState.Dirty ?: return  // first check
        if (draft.chapterId != id) return                       // second check
        if (draftRevision != revision) return                   // third check

        persistDraftSnapshot(
            id = id,
            markdownSnapshot = draft.markdown,
            expectedRevision = revision,
        )
    }


    private suspend fun handleCloseChapter() {
        persistDraftIfNeeded()
        cancelPendingStats()

        _uiState.update {
            it.copy(
                selectedId      = null,
                editingState    = EditingState.None,
                draft           = DraftState.Clean,
                errorMessage    = null,
                contentStats    = ContentStats.Empty,
            )
        }
    }

    private suspend fun handleZoomStep(direction: ZoomDirection) {
        val state = _uiState.value
        val id    = state.selectedId ?: return
        val next  = when (direction) {
            ZoomDirection.In  -> zoomSteps.firstOrNull { it > state.selectedZoomPercent }
            ZoomDirection.Out -> zoomSteps.lastOrNull  { it < state.selectedZoomPercent }
        } ?: return
        handleSetZoom(id, next)
    }

    private suspend fun handleSetZoom(id: UUID, zoomPercent: Int) {
        persistDraftIfNeeded()

        setChapterZoom(id, zoomPercent)
            .onSuccess { updated ->
                _uiState.update { state ->
                    state.copy(chapters = state.chapters.map { if (it.id == id) updated else it })
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Failed to save zoom: ${e.message}") }
            }
    }


    // ── DRAFT HELPERS ────────────────────────────────────────────────────────────────────────────────────────────────

    private suspend fun persistDraftIfNeeded() {

        val draft = _uiState.value.draft as? DraftState.Dirty ?: return

        cancelPendingSave()

        persistDraftSnapshot(
            id = draft.chapterId,
            markdownSnapshot = draft.markdown,
            expectedRevision = null,
        )

    }

    private suspend fun persistDraftSnapshot(
        id: UUID,
        markdownSnapshot: String,
        expectedRevision: Long?,
    ) {
        saveChapterContent(id, markdownSnapshot)
            .onSuccess { updated ->
                _uiState.update { current ->

                    val updatedChapters = current.chapters.map { chapter ->
                        if (chapter.id == id) updated else chapter
                    }

                    val currentDraft = current.draft
                    val sameSnapshotStillCurrent =
                        currentDraft is DraftState.Dirty &&
                                currentDraft.chapterId  == id &&
                                currentDraft.markdown   == markdownSnapshot &&
                                (expectedRevision == null || draftRevision == expectedRevision)

                    if (sameSnapshotStillCurrent) {
                        current.copy(
                            chapters        = updatedChapters,
                            draft           = DraftState.Clean,
                            errorMessage    = null,
                        )
                    } else {
                        current.copy(
                            chapters        = updatedChapters,
                            errorMessage    = null,
                        )
                    }
                }
            }

            .onFailure { error -> _uiState.update { it.copy(errorMessage = "Save failed: ${error.message}") } }
    }

    // ── DEBOUNCERS  ──────────────────────────────────────────────────────────────────────────────────────────────────

    private val persistDebounceMs = 1_000L
    private var saveJob: Job? = null

    private fun scheduleDebouncedSave(id: UUID, revision: Long) {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(persistDebounceMs)
            dispatch(Command.SaveDraft(id, revision))
        }
    }

    private val statsDebounceMs = 300L
    private var statsJob: Job? = null

    private fun scheduleDebouncedStatsUpdate(plainText: String) {
        val selectedId = _uiState.value.selectedId ?: return

        statsJob?.cancel()
        statsJob = scope.launch {
            delay(statsDebounceMs)
            val stats = computeContentStatsFromPlainText(markdownToPlainText(plainText))

            _uiState.update { state ->
                if (state.selectedId != selectedId) {
                    state
                } else {
                    state.copy(contentStats = stats)
                }
            }
        }
    }

    private fun cancelPendingSave() {
        saveJob?.cancel()
        saveJob = null
    }

    private fun cancelPendingStats() {
        statsJob?.cancel()
        statsJob = null
    }

}
