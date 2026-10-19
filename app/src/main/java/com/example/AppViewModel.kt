package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.nativebridge.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    SCHEDULE("Schedule"),
    TASKS("Tasks"),
    NOTES("Notes"),
    REVIEWER("Reviewer"),
    SETTINGS("Settings")
}

data class ActiveStudyState(
    val deck: ReviewerDeck,
    val cardIndex: Int = 0,
    val isFlipped: Boolean = false,
    val againCount: Int = 0,
    val goodCount: Int = 0,
    val easyCount: Int = 0,
    val isFinished: Boolean = false
)

data class ActiveQuizState(
    val deck: ReviewerDeck,
    val questionIndex: Int = 0,
    val currentCard: FlashcardItem,
    val options: List<String>,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val isFinished: Boolean = false
)

data class DocumentParseResultState(
    val fileName: String,
    val extractedDoc: ExtractedDocument,
    val parseTimeMs: Long,
    val isProcessing: Boolean = false,
    val statusMessage: String = ""
)

data class StudyFlowUiState(
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val courses: List<Course> = emptyList(),
    val schedules: List<ScheduleItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val notes: List<NoteItem> = emptyList(),
    val decks: List<ReviewerDeck> = emptyList(),
    val selectedDayOfWeek: Int = RealDateTimeUtils.getCurrentDayOfWeek(),
    val taskFilter: String = "All", // All, Due Today, In Progress, Completed
    val taskSortOrder: TaskSortOrder = TaskSortOrder.DUE_DATE_ASC,
    val taskViewMode: TaskViewMode = TaskViewMode.KANBAN_BOARD,
    val scheduleSortOrder: ScheduleSortOrder = ScheduleSortOrder.TIME_ASC,
    val noteFilterCourse: String = "All",
    val noteSortOrder: NoteSortOrder = NoteSortOrder.LAST_EDITED_DESC,
    val deckSortOrder: DeckSortOrder = DeckSortOrder.MASTERY_ASC,
    val noteSearchQuery: String = "",
    val activeSortFilterModule: SortModule? = null,
    val sortFilters: Map<SortModule, UnifiedSortFilterState> = mapOf(
        SortModule.SCHEDULE to UnifiedSortFilterState(SortModule.SCHEDULE),
        SortModule.TASKS to UnifiedSortFilterState(SortModule.TASKS),
        SortModule.NOTES to UnifiedSortFilterState(SortModule.NOTES),
        SortModule.REVIEWER to UnifiedSortFilterState(SortModule.REVIEWER)
    ),
    val isSelectionMode: Boolean = false,
    val activeSelectionModule: SortModule? = null,
    val selectedItemIds: Set<String> = emptySet(),
    val activeStudySession: ActiveStudyState? = null,
    val activeQuizSession: ActiveQuizState? = null,
    val activeDocumentModal: DocumentParseResultState? = null,
    val showAddTaskDialog: Boolean = false,
    val showAddScheduleDialog: Boolean = false,
    val activeTaskInDetail: TaskItem? = null,
    val activeScheduleInDetail: ScheduleItem? = null,
    val activeDeckInDetail: ReviewerDeck? = null,
    val activeNoteInViewer: NoteItem? = null,
    val isNotePageOpen: Boolean = false,
    val preferences: AppPreferences = AppPreferences(),
    val studyStreak: Int = 0,
    val cardsReviewedToday: Int = 0,
    val masteryPercentage: Int = 0,
    val nativeEngineInfo: String = NativeBridge.getEngineInfo(),
    val memoryDiagnostics: MemoryDiagnostics = NativeBridge.getMemoryDiagnostics(),

    // Pomodoro Timer State
    val pomodoroRemainingSec: Int = 25 * 60,
    val isPomodoroRunning: Boolean = false,
    val isPomodoroFocus: Boolean = true,

    // GPA Simulator & Analytics
    val isGpaSimulatorOpen: Boolean = false,
    val courseProjections: List<CourseGradeProjection> = emptyList(),

    // 2D Knowledge Graph State
    val isKnowledgeGraphOpen: Boolean = false,
    val knowledgeNodes: List<KnowledgeNode> = emptyList(),
    val knowledgeEdges: List<KnowledgeEdge> = emptyList(),

    // P2P Study Exchange State
    val isP2PRadarOpen: Boolean = false,
    val p2pPeers: List<P2PPeerDevice> = emptyList(),

    // OCR & Lecture Scanner State
    val isOcrScannerOpen: Boolean = false,
    val scannedTranscript: String = "",

    // Biometric Vault State
    val isVaultUnlocked: Boolean = false,

    // Semantic Vector Search State
    val semanticQuery: String = "",
    val semanticResults: List<VectorSearchResult> = emptyList(),
    val isSemanticSearchActive: Boolean = false
)

class AppViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StudyFlowUiState())
    val uiState: StateFlow<StudyFlowUiState> = _uiState.asStateFlow()

    private val _pomodoroRemainingSec = MutableStateFlow(25 * 60)
    val pomodoroRemainingSec: StateFlow<Int> = _pomodoroRemainingSec.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _isPomodoroFocus = MutableStateFlow(true)
    val isPomodoroFocus: StateFlow<Boolean> = _isPomodoroFocus.asStateFlow()

    private var pomodoroJob: Job? = null

    init {
        // App opens 100% clean with zero pre-made entries
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update {
            it.copy(
                currentScreen = screen,
                activeStudySession = null,
                activeQuizSession = null,
                isNotePageOpen = false,
                activeTaskInDetail = null,
                activeScheduleInDetail = null,
                activeDeckInDetail = null,
                isSelectionMode = false,
                selectedItemIds = emptySet()
            )
        }
    }

    fun setSelectedDayOfWeek(day: Int) {
        _uiState.update { it.copy(selectedDayOfWeek = day) }
    }

    fun setTaskFilter(filter: String) {
        _uiState.update { it.copy(taskFilter = filter) }
    }

    fun setTaskSortOrder(sortOrder: TaskSortOrder) {
        _uiState.update { it.copy(taskSortOrder = sortOrder) }
    }

    fun setTaskViewMode(mode: TaskViewMode) {
        _uiState.update { it.copy(taskViewMode = mode) }
    }

    fun setScheduleSortOrder(sortOrder: ScheduleSortOrder) {
        _uiState.update { it.copy(scheduleSortOrder = sortOrder) }
    }

    fun setNoteSortOrder(sortOrder: NoteSortOrder) {
        _uiState.update { it.copy(noteSortOrder = sortOrder) }
    }

    fun setDeckSortOrder(sortOrder: DeckSortOrder) {
        _uiState.update { it.copy(deckSortOrder = sortOrder) }
    }

    fun updatePreferences(transform: (AppPreferences) -> AppPreferences) {
        _uiState.update { it.copy(preferences = transform(it.preferences)) }
    }

    // Unified Sort & Filter Helper
    fun openSortFilterSheet(module: SortModule) {
        _uiState.update { it.copy(activeSortFilterModule = module) }
    }

    fun closeSortFilterSheet() {
        _uiState.update { it.copy(activeSortFilterModule = null) }
    }

    fun applySortFilter(module: SortModule, newState: UnifiedSortFilterState) {
        _uiState.update { state ->
            val updatedMap = state.sortFilters.toMutableMap()
            updatedMap[module] = newState
            state.copy(sortFilters = updatedMap, activeSortFilterModule = null)
        }
    }

    // Selectable Deletion Mode
    fun toggleSelectionMode(module: SortModule) {
        _uiState.update { state ->
            if (state.isSelectionMode && state.activeSelectionModule == module) {
                state.copy(isSelectionMode = false, activeSelectionModule = null, selectedItemIds = emptySet())
            } else {
                state.copy(isSelectionMode = true, activeSelectionModule = module, selectedItemIds = emptySet())
            }
        }
    }

    fun toggleItemSelection(id: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItemIds.contains(id)) {
                state.selectedItemIds - id
            } else {
                state.selectedItemIds + id
            }
            state.copy(selectedItemIds = newSelected)
        }
    }

    fun toggleSelectAll(allIds: List<String>) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItemIds.size == allIds.size && allIds.isNotEmpty()) {
                emptySet()
            } else {
                allIds.toSet()
            }
            state.copy(selectedItemIds = newSelected)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(isSelectionMode = false, activeSelectionModule = null, selectedItemIds = emptySet()) }
    }

    fun deleteSelectedItems() {
        val state = _uiState.value
        val toDelete = state.selectedItemIds
        if (toDelete.isEmpty()) return

        when (state.activeSelectionModule) {
            SortModule.SCHEDULE -> {
                _uiState.update { it.copy(schedules = it.schedules.filter { item -> item.id !in toDelete }, isSelectionMode = false, selectedItemIds = emptySet()) }
            }
            SortModule.TASKS -> {
                _uiState.update { it.copy(tasks = it.tasks.filter { item -> item.id !in toDelete }, isSelectionMode = false, selectedItemIds = emptySet()) }
            }
            SortModule.NOTES -> {
                _uiState.update { it.copy(notes = it.notes.filter { item -> item.id !in toDelete }, isSelectionMode = false, selectedItemIds = emptySet()) }
            }
            SortModule.REVIEWER -> {
                _uiState.update { it.copy(decks = it.decks.filter { item -> item.id !in toDelete }, isSelectionMode = false, selectedItemIds = emptySet()) }
            }
            null -> {}
        }
    }

    // Item Detail & Preview Modals
    fun openScheduleDetail(item: ScheduleItem) {
        _uiState.update { it.copy(activeScheduleInDetail = item) }
    }

    fun closeScheduleDetail() {
        _uiState.update { it.copy(activeScheduleInDetail = null) }
    }

    fun openDeckDetail(deck: ReviewerDeck) {
        _uiState.update { it.copy(activeDeckInDetail = deck) }
    }

    fun closeDeckDetail() {
        _uiState.update { it.copy(activeDeckInDetail = null) }
    }

    // Theme & Mode Controls
    fun toggleDarkMode() {
        _uiState.update { state ->
            val nextDark = !state.preferences.isDarkMode
            state.copy(
                preferences = state.preferences.copy(
                    isDarkMode = nextDark,
                    themeMode = if (nextDark) "Dark" else "Light"
                )
            )
        }
    }

    fun toggleEPaperMode() {
        _uiState.update { state ->
            val nextEPaper = !state.preferences.isEPaperMode
            state.copy(
                preferences = state.preferences.copy(
                    isEPaperMode = nextEPaper,
                    themeMode = if (nextEPaper) "E-Paper" else if (state.preferences.isDarkMode) "Dark" else "Light"
                )
            )
        }
    }

    fun setThemeMode(mode: String) {
        _uiState.update { state ->
            when (mode) {
                "Dark" -> state.copy(preferences = state.preferences.copy(isDarkMode = true, isEPaperMode = false, themeMode = "Dark"))
                "Light" -> state.copy(preferences = state.preferences.copy(isDarkMode = false, isEPaperMode = false, themeMode = "Light"))
                "E-Paper" -> state.copy(preferences = state.preferences.copy(isEPaperMode = true, themeMode = "E-Paper"))
                else -> state.copy(preferences = state.preferences.copy(themeMode = mode))
            }
        }
    }

    fun setAccentTheme(themeName: String) {
        _uiState.update { it.copy(preferences = it.preferences.copy(accentGlassTheme = themeName)) }
    }

    // Pomodoro Liquid Focus Timer
    fun startPomodoro() {
        if (_isPomodoroRunning.value) return
        _isPomodoroRunning.value = true
        _uiState.update { it.copy(isPomodoroRunning = true) }

        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_isPomodoroRunning.value && _pomodoroRemainingSec.value > 0) {
                delay(1000)
                val remaining = _pomodoroRemainingSec.value - 1
                if (remaining <= 0) {
                    val nextFocus = !_isPomodoroFocus.value
                    val nextSec = if (nextFocus) _uiState.value.preferences.pomodoroFocusMinutes * 60 else _uiState.value.preferences.pomodoroBreakMinutes * 60
                    _isPomodoroFocus.value = nextFocus
                    _pomodoroRemainingSec.value = nextSec
                    _uiState.update {
                        it.copy(
                            isPomodoroFocus = nextFocus,
                            pomodoroRemainingSec = nextSec,
                            isPomodoroRunning = true,
                            studyStreak = if (it.isPomodoroFocus) it.studyStreak + 1 else it.studyStreak
                        )
                    }
                } else {
                    _pomodoroRemainingSec.value = remaining
                }
            }
        }
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _isPomodoroRunning.value = false
        _uiState.update { it.copy(isPomodoroRunning = false, pomodoroRemainingSec = _pomodoroRemainingSec.value) }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _isPomodoroRunning.value = false
        _isPomodoroFocus.value = true
        val defaultSec = _uiState.value.preferences.pomodoroFocusMinutes * 60
        _pomodoroRemainingSec.value = defaultSec
        _uiState.update {
            it.copy(
                isPomodoroRunning = false,
                isPomodoroFocus = true,
                pomodoroRemainingSec = defaultSec
            )
        }
    }

    fun togglePomodoroFocusMode() {
        val nextFocus = !_isPomodoroFocus.value
        val sec = if (nextFocus) _uiState.value.preferences.pomodoroFocusMinutes * 60 else _uiState.value.preferences.pomodoroBreakMinutes * 60
        _isPomodoroFocus.value = nextFocus
        _pomodoroRemainingSec.value = sec
        _uiState.update { it.copy(isPomodoroFocus = nextFocus, pomodoroRemainingSec = sec) }
    }

    // Smart Attendance & Timetable Automation
    fun toggleSmartAttendance() {
        _uiState.update {
            val enabled = !it.preferences.smartAttendanceEnabled
            it.copy(preferences = it.preferences.copy(smartAttendanceEnabled = enabled))
        }
    }

    fun simulateClassroomArrival(roomName: String) {
        _uiState.update {
            it.copy(
                preferences = it.preferences.copy(
                    detectedClassroom = "$roomName • Geofence Detected"
                )
            )
        }
    }

    // GPA Simulator & Analytics
    fun openGpaSimulator() {
        // Build projections from current courses or schedule
        val currentProjections = if (_uiState.value.courseProjections.isNotEmpty()) {
            _uiState.value.courseProjections
        } else {
            val codes = _uiState.value.courses.ifEmpty {
                _uiState.value.schedules.map { Course(it.courseCode, it.title) }.distinctBy { it.code }
            }
            if (codes.isNotEmpty()) {
                codes.map { course ->
                    CourseGradeProjection(
                        courseCode = course.code,
                        courseName = course.name,
                        currentGrade = 88.0,
                        syllabusFinalWeight = 35.0,
                        simulatedFinalScore = 85.0
                    )
                }
            } else {
                listOf(
                    CourseGradeProjection("CS201", "Data Structures", 91.0, 35.0, 92.0),
                    CourseGradeProjection("BIO105", "Cellular Biology", 86.5, 40.0, 88.0),
                    CourseGradeProjection("MATH220", "Multivariable Calc", 84.0, 30.0, 85.0)
                )
            }
        }
        _uiState.update { it.copy(isGpaSimulatorOpen = true, courseProjections = currentProjections) }
    }

    fun closeGpaSimulator() {
        _uiState.update { it.copy(isGpaSimulatorOpen = false) }
    }

    fun updateSimulationScore(courseCode: String, newScore: Double) {
        _uiState.update { state ->
            val updated = state.courseProjections.map {
                if (it.courseCode == courseCode) it.copy(simulatedFinalScore = newScore) else it
            }
            state.copy(courseProjections = updated)
        }
    }

    // 2D Knowledge Graph
    fun openKnowledgeGraph() {
        refreshKnowledgeGraph()
        _uiState.update { it.copy(isKnowledgeGraphOpen = true) }
    }

    fun closeKnowledgeGraph() {
        _uiState.update { it.copy(isKnowledgeGraphOpen = false) }
    }

    fun refreshKnowledgeGraph() {
        val state = _uiState.value
        val nodes = mutableListOf<KnowledgeNode>()
        val edges = mutableListOf<KnowledgeEdge>()

        // Generate spatial node positions across courses
        var angle = 0.0
        val allItems = (state.notes.map { "Note" to it.title to it.courseCode to it.id } +
                state.decks.map { "Deck" to it.title to it.courseCode to it.id } +
                state.tasks.map { "Task" to it.title to it.courseCode to it.id }).take(14)

        if (allItems.isEmpty()) {
            // Keep graph clean with zero pre-made nodes
        } else {
            val step = (2 * Math.PI) / allItems.size
            allItems.forEachIndexed { i, item ->
                val (meta, id) = item
                val (typeTitle, course) = meta
                val (type, title) = typeTitle
                val x = (0.5 + 0.35 * Math.cos(angle)).toFloat()
                val y = (0.5 + 0.35 * Math.sin(angle)).toFloat()
                angle += step

                nodes.add(KnowledgeNode(id, title.take(24), course, type, x, y, (i % 3) + 1))
                if (i > 0) {
                    edges.add(KnowledgeEdge(nodes[i - 1].id, id, 0.8f))
                }
            }
        }

        _uiState.update { it.copy(knowledgeNodes = nodes, knowledgeEdges = edges) }
    }

    // P2P Study Exchange & File Mesh
    fun openP2PRadar() {
        _uiState.update { it.copy(isP2PRadarOpen = true) }
    }

    fun closeP2PRadar() {
        _uiState.update { it.copy(isP2PRadarOpen = false) }
    }

    fun startP2PTransfer(peerId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updated = state.p2pPeers.map {
                    if (it.id == peerId) it.copy(isTransferring = true, transferProgress = 0.1f) else it
                }
                state.copy(p2pPeers = updated)
            }

            for (step in 2..10) {
                delay(180)
                _uiState.update { state ->
                    val updated = state.p2pPeers.map {
                        if (it.id == peerId) it.copy(transferProgress = step * 0.1f) else it
                    }
                    state.copy(p2pPeers = updated)
                }
            }

            delay(200)
            _uiState.update { state ->
                val updated = state.p2pPeers.map {
                    if (it.id == peerId) it.copy(isTransferring = false, transferProgress = 1.0f) else it
                }
                state.copy(p2pPeers = updated)
            }
        }
    }

    // OCR & Lecture Scanner
    fun openOcrScanner() {
        _uiState.update {
            it.copy(
                isOcrScannerOpen = true,
                scannedTranscript = "Scanning physical whiteboard / lecture slide stream..."
            )
        }
    }

    fun closeOcrScanner() {
        _uiState.update { it.copy(isOcrScannerOpen = false) }
    }

    fun simulateOcrCapture() {
        viewModelScope.launch {
            _uiState.update { it.copy(scannedTranscript = "Detecting bounding contours and tokenizing handwritten notes...") }
            delay(400)
            val sampleCaptured = """
                # Captured Whiteboard Notes
                - Topic: Advanced Graph Traversals & TopoSort
                - Formula: O(V + E) with FIFO Queue adjacency list
                - Priority: High impact for upcoming Midterm
            """.trimIndent()
            _uiState.update { it.copy(scannedTranscript = sampleCaptured) }
        }
    }

    // Biometric Note Vault
    fun unlockVault() {
        _uiState.update { it.copy(isVaultUnlocked = true) }
    }

    fun lockVault() {
        _uiState.update { it.copy(isVaultUnlocked = false) }
    }

    // Zero-Latency Semantic Vector Search
    fun setSemanticQuery(query: String) {
        _uiState.update { it.copy(semanticQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(semanticResults = emptyList(), isSemanticSearchActive = false) }
        } else {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                val results = NativeBridge.semanticSearch(query, 6)
                _uiState.update { it.copy(semanticResults = results, isSemanticSearchActive = true) }
            }
        }
    }

    fun clearSemanticSearch() {
        _uiState.update { it.copy(semanticQuery = "", semanticResults = emptyList(), isSemanticSearchActive = false) }
    }

    // Note Studio Full Page Actions
    fun openNotePage(note: NoteItem?) {
        _uiState.update {
            it.copy(
                activeNoteInViewer = note,
                isNotePageOpen = true
            )
        }
    }

    fun closeNotePage() {
        _uiState.update {
            it.copy(
                isNotePageOpen = false,
                activeNoteInViewer = null
            )
        }
    }

    fun extractActionItemsFromNote(note: NoteItem) {
        val extractedTasks = mutableListOf<TaskItem>()
        val lines = note.content.lines()

        for (line in lines) {
            val trimmed = line.trim()
            val isActionItem = trimmed.startsWith("- [ ]") ||
                    trimmed.startsWith("- [x]") ||
                    trimmed.contains("TODO:", ignoreCase = true) ||
                    trimmed.contains("Assignment:", ignoreCase = true) ||
                    trimmed.contains("Deadline:", ignoreCase = true)

            if (isActionItem) {
                val cleanTitle = trimmed
                    .removePrefix("- [ ]")
                    .removePrefix("- [x]")
                    .trim()
                    .take(60)

                if (cleanTitle.length > 3) {
                    extractedTasks.add(
                        TaskItem(
                            id = UUID.randomUUID().toString(),
                            title = cleanTitle,
                            description = "Intelligently extracted from Note: ${note.title}",
                            courseCode = note.courseCode,
                            dueDaysAway = 2,
                            isCompleted = false,
                            priority = TaskPriority.HIGH,
                            type = TaskType.ASSIGNMENT
                        )
                    )
                }
            }
        }

        if (extractedTasks.isNotEmpty()) {
            _uiState.update { it.copy(tasks = extractedTasks + it.tasks) }
        }
    }

    // Task Management
    fun toggleTaskCompletion(taskId: String) {
        _uiState.update { state ->
            val updated = state.tasks.map { task ->
                if (task.id == taskId) {
                    val nextCompleted = !task.isCompleted
                    task.copy(
                        isCompleted = nextCompleted,
                        status = if (nextCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
                    )
                } else task
            }
            state.copy(
                tasks = updated,
                activeTaskInDetail = if (state.activeTaskInDetail?.id == taskId) {
                    val nextCompleted = !state.activeTaskInDetail.isCompleted
                    state.activeTaskInDetail.copy(
                        isCompleted = nextCompleted,
                        status = if (nextCompleted) TaskStatus.COMPLETED else TaskStatus.TODO
                    )
                } else state.activeTaskInDetail
            )
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        _uiState.update { state ->
            val updated = state.tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        status = newStatus,
                        isCompleted = (newStatus == TaskStatus.COMPLETED)
                    )
                } else task
            }
            state.copy(
                tasks = updated,
                activeTaskInDetail = if (state.activeTaskInDetail?.id == taskId) {
                    state.activeTaskInDetail.copy(status = newStatus, isCompleted = (newStatus == TaskStatus.COMPLETED))
                } else state.activeTaskInDetail
            )
        }
    }

    fun openTaskDetail(task: TaskItem) {
        _uiState.update { it.copy(activeTaskInDetail = task) }
    }

    fun closeTaskDetail() {
        _uiState.update { it.copy(activeTaskInDetail = null) }
    }

    fun updateTask(updatedTask: TaskItem) {
        _uiState.update { state ->
            val updated = state.tasks.map { if (it.id == updatedTask.id) updatedTask else it }
            state.copy(tasks = updated, activeTaskInDetail = updatedTask)
        }
    }

    fun deleteTask(taskId: String) {
        _uiState.update { state ->
            state.copy(
                tasks = state.tasks.filter { it.id != taskId },
                activeTaskInDetail = if (state.activeTaskInDetail?.id == taskId) null else state.activeTaskInDetail
            )
        }
    }

    fun clearCompletedTasks() {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.filter { !it.isCompleted })
        }
    }

    fun openAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }

    fun closeAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }

    fun autoCategorizeTask(title: String, desc: String): CategorizeResult {
        return NativeBridge.autoCategorize(title, desc)
    }

    fun addTask(title: String, desc: String, courseCode: String, priority: TaskPriority, type: TaskType, dueDays: Int) {
        val newTask = TaskItem(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Untitled Task" },
            description = desc,
            courseCode = courseCode,
            dueDaysAway = dueDays,
            isCompleted = false,
            priority = priority,
            type = type,
            status = TaskStatus.TODO,
            createdEpochMs = System.currentTimeMillis()
        )
        val updatedCourses = if (_uiState.value.courses.none { it.code == courseCode }) {
            _uiState.value.courses + Course(courseCode, courseCode)
        } else _uiState.value.courses

        NativeBridge.indexDocument(newTask.id, newTask.title, newTask.description, "Tasks")

        _uiState.update {
            it.copy(
                tasks = listOf(newTask) + it.tasks,
                courses = updatedCourses,
                showAddTaskDialog = false
            )
        }
    }

    // Schedule Management
    fun openAddScheduleDialog() {
        _uiState.update { it.copy(showAddScheduleDialog = true) }
    }

    fun closeAddScheduleDialog() {
        _uiState.update { it.copy(showAddScheduleDialog = false) }
    }

    fun addScheduleItem(courseCode: String, title: String, dayOfWeek: Int, timeRange: String, room: String, type: String) {
        val newItem = ScheduleItem(
            id = UUID.randomUUID().toString(),
            courseCode = courseCode.ifBlank { "GEN100" },
            title = title.ifBlank { "Lecture" },
            dayOfWeek = dayOfWeek,
            timeRange = timeRange.ifBlank { "10:00 - 11:30" },
            room = room.ifBlank { "Room TBD" },
            type = type
        )
        val updatedCourses = if (_uiState.value.courses.none { it.code == courseCode }) {
            _uiState.value.courses + Course(courseCode, courseCode)
        } else _uiState.value.courses

        NativeBridge.indexDocument(newItem.id, newItem.title, "${newItem.courseCode} ${newItem.room} ${newItem.type}", "Schedule")

        _uiState.update {
            it.copy(
                schedules = it.schedules + newItem,
                courses = updatedCourses,
                showAddScheduleDialog = false
            )
        }
    }

    fun deleteScheduleItem(id: String) {
        _uiState.update { state ->
            state.copy(schedules = state.schedules.filter { it.id != id })
        }
    }

    // Notes Management
    fun setNoteFilterCourse(course: String) {
        _uiState.update { it.copy(noteFilterCourse = course) }
    }

    fun setNoteSearchQuery(query: String) {
        _uiState.update { it.copy(noteSearchQuery = query) }
    }

    fun saveNote(title: String, content: String, courseCode: String, tags: List<String>) {
        val active = _uiState.value.activeNoteInViewer
        val now = System.currentTimeMillis()
        if (active != null) {
            val updated = _uiState.value.notes.map {
                if (it.id == active.id) {
                    it.copy(
                        title = title,
                        content = content,
                        courseCode = courseCode,
                        tags = tags,
                        lastEdited = "Just now",
                        lastEditedEpochMs = now
                    )
                } else it
            }
            val updatedActive = updated.firstOrNull { it.id == active.id }
            NativeBridge.indexDocument(active.id, title, content, "Notes")
            _uiState.update { it.copy(notes = updated, activeNoteInViewer = updatedActive) }
        } else {
            val newNote = NoteItem(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Untitled Note" },
                content = content,
                courseCode = courseCode,
                tags = tags,
                lastEdited = "Just now",
                lastEditedEpochMs = now,
                isPinned = false
            )
            val updatedCourses = if (_uiState.value.courses.none { it.code == courseCode }) {
                _uiState.value.courses + Course(courseCode, courseCode)
            } else _uiState.value.courses

            NativeBridge.indexDocument(newNote.id, newNote.title, newNote.content, "Notes")

            _uiState.update {
                it.copy(
                    notes = listOf(newNote) + it.notes,
                    courses = updatedCourses,
                    activeNoteInViewer = newNote
                )
            }
        }
    }

    fun deleteNote(noteId: String) {
        _uiState.update { state ->
            state.copy(
                notes = state.notes.filter { it.id != noteId },
                activeNoteInViewer = if (state.activeNoteInViewer?.id == noteId) null else state.activeNoteInViewer,
                isNotePageOpen = if (state.activeNoteInViewer?.id == noteId) false else state.isNotePageOpen
            )
        }
    }

    fun convertNoteToReviewer(note: NoteItem) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val extracted = NativeBridge.parseDocument(note.title, note.content)
            val newDeck = ReviewerDeck(
                id = UUID.randomUUID().toString(),
                title = "${note.title} (Reviewer)",
                courseCode = extracted.courseCode,
                description = "Synthesized from Note Vault: ${note.title}",
                cards = extracted.cards,
                keyTakeaways = extracted.keyPoints,
                masteredCount = 0,
                lastReviewedAgo = "Never",
                sourceDoc = "Note: ${note.title}"
            )
            NativeBridge.indexDocument(newDeck.id, newDeck.title, newDeck.description, "Reviewer")
            _uiState.update {
                it.copy(
                    decks = listOf(newDeck) + it.decks,
                    currentScreen = AppScreen.REVIEWER,
                    isNotePageOpen = false
                )
            }
        }
    }

    // Reviewer Decks Management
    fun deleteDeck(deckId: String) {
        _uiState.update { state ->
            state.copy(
                decks = state.decks.filter { it.id != deckId },
                activeStudySession = if (state.activeStudySession?.deck?.id == deckId) null else state.activeStudySession,
                activeQuizSession = if (state.activeQuizSession?.deck?.id == deckId) null else state.activeQuizSession
            )
        }
    }

    // Document to Reviewer Pipeline
    fun openDocumentModal() {
        _uiState.update {
            it.copy(
                activeDocumentModal = DocumentParseResultState(
                    fileName = "",
                    extractedDoc = ExtractedDocument(
                        title = "",
                        courseCode = "",
                        keyPoints = emptyList(),
                        cards = emptyList(),
                        estimatedReadMinutes = 0
                    ),
                    parseTimeMs = 0L,
                    isProcessing = false,
                    statusMessage = "Select a PDF or presentation file from device"
                )
            )
        }
    }

    fun processDocumentPipeline(fileName: String, content: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeDocumentModal = it.activeDocumentModal?.copy(
                        isProcessing = true,
                        statusMessage = "Zero-copy mapping & 16KB SIMD tokenizing slides..."
                    )
                )
            }
            val startTime = System.currentTimeMillis()
            delay(280)
            val extracted = NativeBridge.parseDocument(fileName, content)
            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(14)

            _uiState.update {
                it.copy(
                    activeDocumentModal = DocumentParseResultState(
                        fileName = fileName,
                        extractedDoc = extracted,
                        parseTimeMs = elapsed,
                        isProcessing = false,
                        statusMessage = "Parsed in ${elapsed}ms via StudyFlow 16KB native engine!"
                    )
                )
            }
        }
    }

    fun saveParsedDocumentAsDeckAndNote() {
        val currentModal = _uiState.value.activeDocumentModal ?: return
        val doc = currentModal.extractedDoc

        val newDeck = ReviewerDeck(
            id = UUID.randomUUID().toString(),
            title = doc.title.removeSuffix(".pdf").removeSuffix(".pptx").ifBlank { "Lecture Reviewer" },
            courseCode = doc.courseCode,
            description = "High-yield active recall reviewer synthesized from ${doc.title}",
            cards = doc.cards,
            keyTakeaways = doc.keyPoints,
            masteredCount = 0,
            lastReviewedAgo = "Just now",
            sourceDoc = doc.title
        )

        val newNote = NoteItem(
            id = UUID.randomUUID().toString(),
            title = "${doc.title.removeSuffix(".pdf").removeSuffix(".pptx").ifBlank { "Lecture" }} Study Guide",
            content = buildString {
                appendLine("# ${doc.title}")
                appendLine("> Auto-parsed by StudyFlow 16KB Native Engine • Estimated Read: ${doc.estimatedReadMinutes} min")
                appendLine()
                appendLine("## Core Key Takeaways")
                doc.keyPoints.forEach { appendLine("- $it") }
                appendLine()
                appendLine("## Active Recall Concepts")
                doc.cards.forEach { appendLine("- **${it.question}** : ${it.answer}") }
            },
            courseCode = doc.courseCode,
            tags = listOf("AutoReviewer", doc.courseCode, "Parsed"),
            lastEdited = "Just now",
            lastEditedEpochMs = System.currentTimeMillis(),
            isPinned = true
        )

        val updatedCourses = if (_uiState.value.courses.none { it.code == doc.courseCode }) {
            _uiState.value.courses + Course(doc.courseCode, doc.courseCode)
        } else _uiState.value.courses

        NativeBridge.indexDocument(newDeck.id, newDeck.title, newDeck.description, "Reviewer")
        NativeBridge.indexDocument(newNote.id, newNote.title, newNote.content, "Notes")

        _uiState.update {
            it.copy(
                decks = listOf(newDeck) + it.decks,
                notes = listOf(newNote) + it.notes,
                courses = updatedCourses,
                activeDocumentModal = null,
                currentScreen = AppScreen.REVIEWER
            )
        }
    }

    fun dismissDocumentModal() {
        _uiState.update { it.copy(activeDocumentModal = null) }
    }

    // Flashcard Study Session (SM-2 Spaced Repetition)
    fun startStudySession(deck: ReviewerDeck) {
        if (deck.cards.isEmpty()) return
        _uiState.update {
            it.copy(
                activeStudySession = ActiveStudyState(
                    deck = deck,
                    cardIndex = 0,
                    isFlipped = false,
                    againCount = 0,
                    goodCount = 0,
                    easyCount = 0,
                    isFinished = false
                )
            )
        }
    }

    fun flipCard() {
        _uiState.update { state ->
            state.activeStudySession?.let { session ->
                state.copy(activeStudySession = session.copy(isFlipped = !session.isFlipped))
            } ?: state
        }
    }

    fun recordCardAnswer(rating: String) {
        val session = _uiState.value.activeStudySession ?: return
        val currentDeck = session.deck
        val nextIndex = session.cardIndex + 1
        val isDone = nextIndex >= currentDeck.cards.size

        val updatedAgain = if (rating == "Again") session.againCount + 1 else session.againCount
        val updatedGood = if (rating == "Good") session.goodCount + 1 else session.goodCount
        val updatedEasy = if (rating == "Easy") session.easyCount + 1 else session.easyCount

        if (isDone) {
            val newlyMastered = updatedGood + updatedEasy
            val totalMastered = (currentDeck.masteredCount + newlyMastered).coerceAtMost(currentDeck.cards.size)
            val updatedDeck = currentDeck.copy(
                masteredCount = totalMastered,
                lastReviewedAgo = "Today"
            )
            val updatedDecks = _uiState.value.decks.map {
                if (it.id == currentDeck.id) updatedDeck else it
            }
            val newCardsReviewed = _uiState.value.cardsReviewedToday + currentDeck.cards.size
            val newMastery = ((totalMastered.toDouble() / currentDeck.cards.size.toDouble()) * 100).toInt()

            _uiState.update {
                it.copy(
                    decks = updatedDecks,
                    cardsReviewedToday = newCardsReviewed,
                    masteryPercentage = if (it.masteryPercentage == 0) newMastery else (it.masteryPercentage + newMastery) / 2,
                    studyStreak = maxOf(1, it.studyStreak),
                    activeStudySession = session.copy(
                        cardIndex = currentDeck.cards.size - 1,
                        isFlipped = false,
                        againCount = updatedAgain,
                        goodCount = updatedGood,
                        easyCount = updatedEasy,
                        isFinished = true
                    )
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    activeStudySession = session.copy(
                        cardIndex = nextIndex,
                        isFlipped = false,
                        againCount = updatedAgain,
                        goodCount = updatedGood,
                        easyCount = updatedEasy
                    )
                )
            }
        }
    }

    fun closeStudySession() {
        _uiState.update { it.copy(activeStudySession = null) }
    }

    // Quick Quiz Mode
    fun startQuizSession(deck: ReviewerDeck) {
        if (deck.cards.isEmpty()) return
        val firstCard = deck.cards[0]
        val options = generateQuizOptions(firstCard, deck.cards)
        _uiState.update {
            it.copy(
                activeQuizSession = ActiveQuizState(
                    deck = deck,
                    questionIndex = 0,
                    currentCard = firstCard,
                    options = options,
                    selectedOptionIndex = null,
                    isAnswered = false,
                    score = 0,
                    isFinished = false
                )
            )
        }
    }

    private fun generateQuizOptions(correctCard: FlashcardItem, allCards: List<FlashcardItem>): List<String> {
        val distractors = allCards.filter { it.answer != correctCard.answer }.map { it.answer }.shuffled().take(3)
        val combined = (distractors + correctCard.answer).shuffled()
        return combined
    }

    fun answerQuiz(optionIndex: Int) {
        val quiz = _uiState.value.activeQuizSession ?: return
        if (quiz.isAnswered) return
        val isCorrect = quiz.options.getOrNull(optionIndex) == quiz.currentCard.answer
        val newScore = if (isCorrect) quiz.score + 1 else quiz.score

        _uiState.update {
            it.copy(
                activeQuizSession = quiz.copy(
                    selectedOptionIndex = optionIndex,
                    isAnswered = true,
                    score = newScore
                )
            )
        }
    }

    fun nextQuizQuestion() {
        val quiz = _uiState.value.activeQuizSession ?: return
        val nextIdx = quiz.questionIndex + 1
        if (nextIdx >= quiz.deck.cards.size) {
            _uiState.update {
                it.copy(activeQuizSession = quiz.copy(isFinished = true))
            }
        } else {
            val nextCard = quiz.deck.cards[nextIdx]
            val options = generateQuizOptions(nextCard, quiz.deck.cards)
            _uiState.update {
                it.copy(
                    activeQuizSession = quiz.copy(
                        questionIndex = nextIdx,
                        currentCard = nextCard,
                        options = options,
                        selectedOptionIndex = null,
                        isAnswered = false
                    )
                )
            }
        }
    }

    fun closeQuizSession() {
        _uiState.update { it.copy(activeQuizSession = null) }
    }

    // Workspace Reset
    fun clearAllData() {
        NativeBridge.clearVectorIndex()
        _uiState.update {
            it.copy(
                courses = emptyList(),
                schedules = emptyList(),
                tasks = emptyList(),
                notes = emptyList(),
                decks = emptyList(),
                studyStreak = 0,
                cardsReviewedToday = 0,
                masteryPercentage = 0,
                activeStudySession = null,
                activeQuizSession = null,
                isNotePageOpen = false,
                activeTaskInDetail = null,
                isGpaSimulatorOpen = false,
                isKnowledgeGraphOpen = false,
                isP2PRadarOpen = false,
                isOcrScannerOpen = false,
                semanticResults = emptyList(),
                isSemanticSearchActive = false
            )
        }
    }
}
