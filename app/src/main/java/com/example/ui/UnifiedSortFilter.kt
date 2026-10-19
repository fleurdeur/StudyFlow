package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

// Unified Filtering & Sorting Logic
object UnifiedSortEngine {

    fun filterAndSortSchedule(
        items: List<ScheduleItem>,
        state: UnifiedSortFilterState,
        dayOfWeek: Int
    ): List<ScheduleItem> {
        val filtered = items.filter { item ->
            val dayMatches = item.dayOfWeek == dayOfWeek
            val categoryMatches = state.selectedCategory == "All" || item.courseCode == state.selectedCategory
            val statusMatches = state.selectedStatus == "All" || item.type.equals(state.selectedStatus, ignoreCase = true)
            dayMatches && categoryMatches && statusMatches
        }

        return when (state.criterion) {
            UnifiedSortCriterion.DATE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.timeRange }
                else
                    filtered.sortedByDescending { it.timeRange }
            }
            UnifiedSortCriterion.CATEGORY -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.courseCode }
                else
                    filtered.sortedByDescending { it.courseCode }
            }
            UnifiedSortCriterion.TITLE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.title }
                else
                    filtered.sortedByDescending { it.title }
            }
            UnifiedSortCriterion.PRIORITY -> {
                // By type importance: Lecture > Lab > Seminar
                filtered.sortedBy { it.type }
            }
        }
    }

    fun filterAndSortTasks(
        items: List<TaskItem>,
        state: UnifiedSortFilterState
    ): List<TaskItem> {
        val filtered = items.filter { item ->
            val categoryMatches = state.selectedCategory == "All" || item.courseCode == state.selectedCategory
            val statusMatches = when (state.selectedStatus) {
                "All" -> true
                "Active" -> !item.isCompleted
                "Completed" -> item.isCompleted
                "Urgent" -> item.priority == TaskPriority.URGENT || item.dueDaysAway <= 0
                else -> true
            }
            categoryMatches && statusMatches
        }

        return when (state.criterion) {
            UnifiedSortCriterion.DATE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.dueDaysAway }
                else
                    filtered.sortedByDescending { it.dueDaysAway }
            }
            UnifiedSortCriterion.PRIORITY -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedByDescending { it.priority.weight } // Highest priority first
                else
                    filtered.sortedBy { it.priority.weight }
            }
            UnifiedSortCriterion.CATEGORY -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.courseCode }
                else
                    filtered.sortedByDescending { it.courseCode }
            }
            UnifiedSortCriterion.TITLE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.title.lowercase() }
                else
                    filtered.sortedByDescending { it.title.lowercase() }
            }
        }
    }

    fun filterAndSortNotes(
        items: List<NoteItem>,
        state: UnifiedSortFilterState
    ): List<NoteItem> {
        val filtered = items.filter { item ->
            val categoryMatches = state.selectedCategory == "All" || item.courseCode == state.selectedCategory
            val statusMatches = when (state.selectedStatus) {
                "All" -> true
                "Pinned" -> item.isPinned
                else -> true
            }
            categoryMatches && statusMatches
        }

        return when (state.criterion) {
            UnifiedSortCriterion.DATE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedByDescending { it.lastEditedEpochMs } // default recent first
                else
                    filtered.sortedBy { it.lastEditedEpochMs }
            }
            UnifiedSortCriterion.CATEGORY -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.courseCode }
                else
                    filtered.sortedByDescending { it.courseCode }
            }
            UnifiedSortCriterion.TITLE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.title.lowercase() }
                else
                    filtered.sortedByDescending { it.title.lowercase() }
            }
            UnifiedSortCriterion.PRIORITY -> {
                // Pinned notes first
                filtered.sortedByDescending { it.isPinned }
            }
        }
    }

    fun filterAndSortDecks(
        items: List<ReviewerDeck>,
        state: UnifiedSortFilterState
    ): List<ReviewerDeck> {
        val filtered = items.filter { item ->
            val categoryMatches = state.selectedCategory == "All" || item.courseCode == state.selectedCategory
            val statusMatches = when (state.selectedStatus) {
                "All" -> true
                "Needs Review" -> (item.masteredCount.toFloat() / (item.cards.size.coerceAtLeast(1))) < 0.75f
                "Mastered" -> (item.masteredCount.toFloat() / (item.cards.size.coerceAtLeast(1))) >= 0.75f
                else -> true
            }
            categoryMatches && statusMatches
        }

        return when (state.criterion) {
            UnifiedSortCriterion.DATE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.title }
                else
                    filtered.sortedByDescending { it.title }
            }
            UnifiedSortCriterion.PRIORITY -> {
                // Lowest mastery first (needs attention)
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.masteredCount.toFloat() / (it.cards.size.coerceAtLeast(1)) }
                else
                    filtered.sortedByDescending { it.masteredCount.toFloat() / (it.cards.size.coerceAtLeast(1)) }
            }
            UnifiedSortCriterion.CATEGORY -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.courseCode }
                else
                    filtered.sortedByDescending { it.courseCode }
            }
            UnifiedSortCriterion.TITLE -> {
                if (state.direction == UnifiedSortDirection.ASCENDING)
                    filtered.sortedBy { it.title.lowercase() }
                else
                    filtered.sortedByDescending { it.title.lowercase() }
            }
        }
    }
}

// Consistent Unified Sort & Filter Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSortFilterBottomSheet(
    module: SortModule,
    currentState: UnifiedSortFilterState,
    availableCategories: List<String>,
    onApply: (UnifiedSortFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent

    var tempCriterion by remember { mutableStateOf(currentState.criterion) }
    var tempDirection by remember { mutableStateOf(currentState.direction) }
    var tempCategory by remember { mutableStateOf(currentState.selectedCategory) }
    var tempStatus by remember { mutableStateOf(currentState.selectedStatus) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(palette.hairline)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("sort_filter_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sort & Filter ${module.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                TextButton(
                    onClick = {
                        tempCriterion = UnifiedSortCriterion.DATE
                        tempDirection = UnifiedSortDirection.ASCENDING
                        tempCategory = "All"
                        tempStatus = "All"
                    }
                ) {
                    Text("Reset", color = palette.textSecondary, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Section 1: Sort Criterion
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SORT BY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UnifiedSortCriterion.values().forEach { crit ->
                        val isSelected = tempCriterion == crit
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent else Color.Transparent)
                                .clickable { tempCriterion = crit }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = crit.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Section 2: Sort Direction
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DIRECTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (tempDirection == UnifiedSortDirection.ASCENDING) accent.copy(alpha = 0.2f) else palette.cardBg)
                            .border(
                                1.dp,
                                if (tempDirection == UnifiedSortDirection.ASCENDING) accent else palette.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { tempDirection = UnifiedSortDirection.ASCENDING }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tempDirection == UnifiedSortDirection.ASCENDING) accent else palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ascending",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (tempDirection == UnifiedSortDirection.ASCENDING) FontWeight.Bold else FontWeight.Normal,
                                color = if (tempDirection == UnifiedSortDirection.ASCENDING) accent else palette.textPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (tempDirection == UnifiedSortDirection.DESCENDING) accent.copy(alpha = 0.2f) else palette.cardBg)
                            .border(
                                1.dp,
                                if (tempDirection == UnifiedSortDirection.DESCENDING) accent else palette.cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { tempDirection = UnifiedSortDirection.DESCENDING }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (tempDirection == UnifiedSortDirection.DESCENDING) accent else palette.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Descending",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (tempDirection == UnifiedSortDirection.DESCENDING) FontWeight.Bold else FontWeight.Normal,
                                color = if (tempDirection == UnifiedSortDirection.DESCENDING) accent else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Section 3: Filter by Category / Course
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CATEGORY / COURSE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    letterSpacing = 1.sp
                )

                val categories = listOf("All") + availableCategories.distinct().filter { it.isNotEmpty() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = tempCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
                                .clickable { tempCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Section 4: Filter by Status / Type
            val statusOptions = when (module) {
                SortModule.TASKS -> listOf("All", "Active", "Completed", "Urgent")
                SortModule.SCHEDULE -> listOf("All", "Lecture", "Lab", "Seminar")
                SortModule.NOTES -> listOf("All", "Pinned")
                SortModule.REVIEWER -> listOf("All", "Needs Review", "Mastered")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STATUS / TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusOptions.forEach { status ->
                        val isSelected = tempStatus == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
                                .clickable { tempStatus = status }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons: Apply
            Button(
                onClick = {
                    onApply(
                        currentState.copy(
                            criterion = tempCriterion,
                            direction = tempDirection,
                            selectedCategory = tempCategory,
                            selectedStatus = tempStatus
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_sort_filter_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = if (palette.isDark) Color.Black else Color.White
                )
            ) {
                Text("Apply Sort & Filters", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

// Multi-Select Action Bar (Shows in Selectable Mode)
@Composable
fun IosSelectionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAllToggle: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExitSelectMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val allSelected = selectedCount > 0 && selectedCount == totalCount

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.cardBg)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("selection_mode_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$selectedCount Selected",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (allSelected) "Deselect All" else "Select All",
                style = MaterialTheme.typography.labelSmall,
                color = palette.accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSelectAllToggle() }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedCount > 0) {
                IconButton(
                    onClick = onDeleteSelected,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NothingRed.copy(alpha = 0.2f))
                        .testTag("delete_selected_batch_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        tint = NothingRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            TextButton(
                onClick = onExitSelectMode,
                modifier = Modifier.heightIn(min = 36.dp)
            ) {
                Text("Done", color = palette.accent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Circular Checkbox for Selection Mode
@Composable
fun IosSelectionCheckbox(
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) accent else Color.Transparent)
                .border(2.dp, if (isSelected) accent else palette.textSecondary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (palette.isDark) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
