package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.AppViewModel
import com.example.StudyFlowUiState
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun TasksScreen(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val filters = listOf("All", "Active", "Completed", "Urgent")
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val palette = LocalThemePalette.current
    val accent = palette.accent
    val isSelectMode = state.isSelectionMode && state.activeSelectionModule == SortModule.TASKS
    val sortFilter = state.sortFilters[SortModule.TASKS] ?: UnifiedSortFilterState(SortModule.TASKS)

    val filteredTasks = remember(state.tasks, sortFilter) {
        UnifiedSortEngine.filterAndSortTasks(state.tasks, sortFilter)
    }

    Box(modifier = modifier.fillMaxSize().testTag("tasks_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Sort & Select mode
            item {
                IosLargeTitleHeader(
                    title = "Tasks",
                    subtitle = "${state.tasks.count { !it.isCompleted }} active assignments",
                    onSortClick = { viewModel.openSortFilterSheet(SortModule.TASKS) },
                    onSelectClick = { viewModel.toggleSelectionMode(SortModule.TASKS) },
                    isSelectActive = isSelectMode
                )
            }

            // Multi-Selection Action Bar
            if (isSelectMode) {
                item {
                    IosSelectionBar(
                        selectedCount = state.selectedItemIds.size,
                        totalCount = filteredTasks.size,
                        onSelectAllToggle = {
                            viewModel.toggleSelectAll(filteredTasks.map { it.id })
                        },
                        onDeleteSelected = { showBatchDeleteConfirm = true },
                        onExitSelectMode = { viewModel.clearSelection() }
                    )
                }
            }

            // Quick Status Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filters, key = { it }) { filter ->
                        val isSelected = filter == sortFilter.selectedStatus
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.22f) else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.applySortFilter(
                                        SortModule.TASKS,
                                        sortFilter.copy(selectedStatus = filter)
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("filter_chip_$filter")
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) accent else palette.textSecondary
                            )
                        }
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    EmptyPlaceholder(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "No Tasks",
                        message = "Tap the + button in the bottom right corner to create your first assignment."
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val isChecked = state.selectedItemIds.contains(task.id)

                    IosCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_card_${task.id}"),
                        onClick = {
                            if (isSelectMode) {
                                viewModel.toggleItemSelection(task.id)
                            } else {
                                viewModel.openTaskDetail(task)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox in Select Mode OR normal Task Completion Checkbox
                            if (isSelectMode) {
                                IosSelectionCheckbox(
                                    isSelected = isChecked,
                                    onToggle = { viewModel.toggleItemSelection(task.id) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (task.isCompleted) accent else Color.Transparent)
                                        .border(2.dp, if (task.isCompleted) accent else palette.textSecondary, CircleShape)
                                        .clickable { viewModel.toggleTaskCompletion(task.id) }
                                        .testTag("task_check_${task.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = if (palette.isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                            }

                            // Content Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CourseBadge(code = task.courseCode)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Priority Pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(task.priority.colorHex).copy(alpha = 0.18f))
                                            .border(1.dp, Color(task.priority.colorHex).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = task.priority.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Color(task.priority.colorHex),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) palette.textSecondary else palette.textPrimary,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Due: ${RealDateTimeUtils.formatDueDate(task.dueDaysAway)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (task.dueDaysAway <= 0 && !task.isCompleted) NothingRed else palette.textSecondary,
                                    fontWeight = if (task.dueDaysAway <= 0 && !task.isCompleted) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (!isSelectMode) {
                                IosDeleteButton(
                                    onClick = { taskToDelete = task },
                                    contentDescription = "Delete Task"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Repositioned Floating Action Button in the Bottom Right
        IosFloatingAddButton(
            onClick = { viewModel.openAddTaskDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
            contentDescription = "Add New Task"
        )

        // Add Task Dialog
        if (state.showAddTaskDialog) {
            AddTaskDialog(
                viewModel = viewModel,
                accent = accent,
                onDismiss = { viewModel.closeAddTaskDialog() }
            )
        }

        // Task Detail & Preview Modal
        state.activeTaskInDetail?.let { task ->
            TaskPreviewDialog(
                task = task,
                onDismiss = { viewModel.closeTaskDetail() },
                onToggleComplete = { viewModel.toggleTaskCompletion(task.id) },
                onSave = { updated -> viewModel.updateTask(updated) },
                onDelete = {
                    viewModel.deleteTask(task.id)
                    viewModel.closeTaskDetail()
                }
            )
        }

        // Confirm Single Delete
        taskToDelete?.let { task ->
            ConfirmDeleteDialog(
                title = "Delete Task",
                message = "Are you sure you want to delete '${task.title}'?",
                onConfirm = {
                    viewModel.deleteTask(task.id)
                    taskToDelete = null
                },
                onDismiss = { taskToDelete = null }
            )
        }

        // Confirm Batch Delete
        if (showBatchDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete Selected Tasks",
                message = "Are you sure you want to delete ${state.selectedItemIds.size} selected tasks? This cannot be undone.",
                onConfirm = {
                    viewModel.deleteSelectedItems()
                    showBatchDeleteConfirm = false
                },
                onDismiss = { showBatchDeleteConfirm = false }
            )
        }
    }
}

@Composable
private fun TaskPreviewDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onSave: (TaskItem) -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent

    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(task.title) }
    var desc by remember { mutableStateOf(task.description) }
    var priority by remember { mutableStateOf(task.priority) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("task_preview_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CourseBadge(code = task.courseCode)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isEditing = !isEditing },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(palette.cardBg)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(NothingRed.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NothingRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title", color = palette.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Task Description / Notes", color = palette.textSecondary) },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskPriority.values().forEach { p ->
                            val isSel = priority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(p.colorHex) else palette.cardBg)
                                    .clickable { priority = p }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancel", color = palette.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSave(task.copy(title = title, description = desc, priority = priority))
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = if (palette.isDark) Color.Black else Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Preview Mode
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.description.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.cardBg)
                                .border(1.dp, palette.cardBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                        }
                    }

                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Due: ${RealDateTimeUtils.formatDueDate(task.dueDaysAway)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(task.priority.colorHex).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = task.priority.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(task.priority.colorHex),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Toggle Complete Button
                    Button(
                        onClick = onToggleComplete,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (task.isCompleted) palette.hairline else accent,
                            contentColor = if (task.isCompleted) palette.textPrimary else (if (palette.isDark) Color.Black else Color.White)
                        )
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (task.isCompleted) "Mark as Incomplete" else "Mark as Completed",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Close", color = palette.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    viewModel: AppViewModel,
    accent: Color,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }
    var taskType by remember { mutableStateOf(TaskType.ASSIGNMENT) }
    var dueDays by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("add_task_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", color = palette.textSecondary) },
                    placeholder = { Text("e.g. Complete Lab Report", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code", color = palette.textSecondary) },
                    placeholder = { Text("e.g. CS201", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Notes / Description", color = palette.textSecondary) },
                    placeholder = { Text("Add any details or instructions...", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("task_desc_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addTask(title, desc, courseCode, priority, taskType, dueDays)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = if (palette.isDark) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
