package com.example.data

import com.example.nativebridge.FlashcardItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class TaskPriority(val label: String, val colorHex: Long, val weight: Int) {
    URGENT("Urgent", 0xFFF43F5E, 4),
    HIGH("High", 0xFFF59E0B, 3),
    MEDIUM("Medium", 0xFF38BDF8, 2),
    LOW("Low", 0xFF94A3B8, 1)
}

enum class TaskType(val label: String) {
    ASSIGNMENT("Assignment"),
    PROJECT("Project"),
    EXAM_PREP("Exam Prep"),
    READING("Reading")
}

enum class TaskStatus(val label: String) {
    TODO("To-Do"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

enum class TaskViewMode(val label: String) {
    CLEAN_LIST("Clean List"),
    MASONRY_GRID("Masonry Grid"),
    KANBAN_BOARD("Kanban Board")
}

enum class TaskSortOrder(val label: String) {
    DUE_DATE_ASC("Due Date (Soonest)"),
    DUE_DATE_DESC("Due Date (Furthest)"),
    PRIORITY_DESC("Priority (High to Low)"),
    COURSE_AZ("Course Code (A-Z)"),
    TITLE_AZ("Title (A-Z)")
}

enum class ScheduleSortOrder(val label: String) {
    TIME_ASC("Time (Earliest)"),
    COURSE_AZ("Course (A-Z)"),
    TYPE("Class Type")
}

enum class NoteSortOrder(val label: String) {
    LAST_EDITED_DESC("Recently Edited"),
    TITLE_AZ("Title (A-Z)"),
    COURSE_AZ("Course (A-Z)"),
    WORD_COUNT_DESC("Word Count (Longest)")
}

enum class DeckSortOrder(val label: String) {
    MASTERY_ASC("Mastery (Needs Review)"),
    MASTERY_DESC("Mastery (Highest)"),
    CARD_COUNT_DESC("Card Count (Most Cards)"),
    COURSE_AZ("Course (A-Z)")
}

data class Course(
    val code: String,
    val name: String,
    val professor: String = "",
    val location: String = "",
    val colorHex: Long = 0xFF38BDF8
)

data class ScheduleItem(
    val id: String,
    val courseCode: String,
    val title: String,
    val dayOfWeek: Int, // 1: Mon .. 7: Sun
    val timeRange: String,
    val room: String,
    val type: String
)

data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val courseCode: String,
    val dueDaysAway: Int,
    val isCompleted: Boolean,
    val priority: TaskPriority,
    val type: TaskType,
    val status: TaskStatus = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO,
    val createdEpochMs: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val courseCode: String,
    val tags: List<String>,
    val lastEdited: String,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val lastEditedEpochMs: Long = System.currentTimeMillis(),
    val defaultHighlightColorHex: Long = 0xFF38BDF8
)

enum class UnifiedSortCriterion(val label: String) {
    DATE("Date"),
    PRIORITY("Priority"),
    CATEGORY("Category / Course"),
    TITLE("Title")
}

enum class UnifiedSortDirection(val label: String) {
    ASCENDING("Ascending (Soonest / A-Z)"),
    DESCENDING("Descending (Latest / Z-A)")
}

enum class SortModule(val title: String) {
    SCHEDULE("Schedule"),
    TASKS("Tasks"),
    NOTES("Notes"),
    REVIEWER("Reviewer")
}

data class UnifiedSortFilterState(
    val module: SortModule,
    val criterion: UnifiedSortCriterion = UnifiedSortCriterion.DATE,
    val direction: UnifiedSortDirection = UnifiedSortDirection.ASCENDING,
    val selectedCategory: String = "All",
    val selectedStatus: String = "All",
    val searchQuery: String = ""
)

data class ReviewerDeck(
    val id: String,
    val title: String,
    val courseCode: String,
    val description: String,
    val cards: List<FlashcardItem>,
    val keyTakeaways: List<String>,
    val masteredCount: Int,
    val lastReviewedAgo: String,
    val sourceDoc: String
)

data class CourseGradeProjection(
    val courseCode: String,
    val courseName: String,
    val currentGrade: Double,        // e.g. 88.0
    val syllabusFinalWeight: Double, // e.g. 35.0 (%)
    val simulatedFinalScore: Double, // slider value e.g. 90.0 (%)
    val creditUnits: Int = 3
) {
    val projectedFinalGrade: Double
        get() {
            val courseworkContribution = currentGrade * ((100.0 - syllabusFinalWeight) / 100.0)
            val finalContribution = simulatedFinalScore * (syllabusFinalWeight / 100.0)
            return (courseworkContribution + finalContribution).coerceIn(0.0, 100.0)
        }

    val projectedGpaPoints: Double
        get() = when {
            projectedFinalGrade >= 93.0 -> 4.0
            projectedFinalGrade >= 90.0 -> 3.7
            projectedFinalGrade >= 87.0 -> 3.3
            projectedFinalGrade >= 83.0 -> 3.0
            projectedFinalGrade >= 80.0 -> 2.7
            projectedFinalGrade >= 77.0 -> 2.3
            projectedFinalGrade >= 70.0 -> 2.0
            else -> 1.0
        }
}

data class KnowledgeNode(
    val id: String,
    val label: String,
    val courseCode: String,
    val type: String, // "Note", "Deck", "Task", "Schedule"
    val xRatio: Float,
    val yRatio: Float,
    val connectionCount: Int = 1
)

data class KnowledgeEdge(
    val fromId: String,
    val toId: String,
    val strength: Float = 0.8f
)

data class P2PPeerDevice(
    val id: String,
    val name: String,
    val distanceMetres: Float,
    val availableBundle: String,
    val isTransferring: Boolean = false,
    val transferProgress: Float = 0f
)

data class AppPreferences(
    val isDarkMode: Boolean = true,
    val isEPaperMode: Boolean = false,
    val themeMode: String = "Dark", // "Dark", "Light", "System", "E-Paper"
    val accentGlassTheme: String = "iOS Blue",
    val glassBlurIntensity: Float = 0.85f,
    val hapticsEnabled: Boolean = true,
    val smartScheduleAlerts: Boolean = true,
    val smartAttendanceEnabled: Boolean = true,
    val detectedClassroom: String = "Hall B12 • Connected to Campus Wi-Fi",
    val isBiometricVaultLocked: Boolean = false,
    val smartPriorityAutoCalc: Boolean = true,
    val markdownSplitPreview: Boolean = false,
    val noteFontSizeSp: Int = 16,
    val showWordCount: Boolean = true,
    val defaultHighlightColorHex: Long = 0xFF0A84FF,
    val dailyCardTarget: Int = 20,
    val autoFlipDelaySec: Int = 0,
    val autoCategorizeEnabled: Boolean = true,
    val spacedRepetitionAlgorithm: String = "SM-2 Adaptive", // "Leitner 5-Stage", "SM-2 Adaptive"
    val autoExtractSlideCards: Boolean = true,
    val defaultTaskDueDays: Int = 1,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5
)

data class CalendarDayInfo(
    val dayOfWeek: Int, // 1: Mon .. 7: Sun
    val dayNameShort: String,
    val dayOfMonth: Int,
    val monthNameShort: String,
    val isToday: Boolean,
    val localDate: LocalDate
)

object RealDateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

    fun getToday(): LocalDate = LocalDate.now()

    fun getFormattedToday(): String = getToday().format(dateFormatter)

    fun getCurrentDayOfWeek(): Int = getToday().dayOfWeek.value // 1 (Mon) to 7 (Sun)

    fun formatDueDate(dueDaysAway: Int): String {
        val targetDate = getToday().plusDays(dueDaysAway.toLong())
        return when (dueDaysAway) {
            0 -> "Today (${targetDate.format(monthDayFormatter)})"
            1 -> "Tomorrow (${targetDate.format(monthDayFormatter)})"
            else -> targetDate.format(monthDayFormatter)
        }
    }

    fun getDayNumberName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day $dayOfWeek"
        }
    }

    fun getDaysForWeek(): List<CalendarDayInfo> {
        val today = getToday()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            CalendarDayInfo(
                dayOfWeek = date.dayOfWeek.value,
                dayNameShort = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                dayOfMonth = date.dayOfMonth,
                monthNameShort = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                isToday = date == today,
                localDate = date
            )
        }
    }
}
