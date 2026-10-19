package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppViewModel
import com.example.StudyFlowUiState
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun ScheduleScreen(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val weekDays = remember { RealDateTimeUtils.getDaysForWeek() }
    var scheduleToDelete by remember { mutableStateOf<ScheduleItem?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val palette = LocalThemePalette.current
    val accent = palette.accent
    val isSelectMode = state.isSelectionMode && state.activeSelectionModule == SortModule.SCHEDULE
    val sortFilter = state.sortFilters[SortModule.SCHEDULE] ?: UnifiedSortFilterState(SortModule.SCHEDULE)

    val activeDaySchedule = remember(state.schedules, state.selectedDayOfWeek, sortFilter) {
        UnifiedSortEngine.filterAndSortSchedule(state.schedules, sortFilter, state.selectedDayOfWeek)
    }

    val nextClass = remember(activeDaySchedule) { activeDaySchedule.firstOrNull() }

    Box(modifier = modifier.fillMaxSize().testTag("schedule_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Floating Horizon Header
            item {
                IosCard(
                    modifier = Modifier.fillMaxWidth().testTag("floating_horizon_header"),
                    backgroundColor = palette.cardBg
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Timetable & Focus",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                            }
                            Text(
                                text = RealDateTimeUtils.getFormattedToday(),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // GPA Simulator Launcher Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent.copy(alpha = 0.18f))
                                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.openGpaSimulator() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("open_gpa_simulator_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GPA Calc", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
                                }
                            }

                            // Sort & Select Actions
                            IosSortButton(
                                onClick = { viewModel.openSortFilterSheet(SortModule.SCHEDULE) },
                                isActive = sortFilter.criterion != UnifiedSortCriterion.DATE || sortFilter.selectedCategory != "All"
                            )
                            IosSelectModeButton(
                                onClick = { viewModel.toggleSelectionMode(SortModule.SCHEDULE) },
                                isSelectMode = isSelectMode
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Smart Attendance & Geofence Status Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (state.preferences.smartAttendanceEnabled) MatrixMint.copy(alpha = 0.12f) else palette.surface)
                            .border(1.dp, if (state.preferences.smartAttendanceEnabled) MatrixMint.copy(alpha = 0.35f) else palette.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.simulateClassroomArrival("Hall B12 (Science)") }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (state.preferences.smartAttendanceEnabled) MatrixMint else palette.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.preferences.detectedClassroom,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.preferences.smartAttendanceEnabled) palette.textPrimary else palette.textSecondary
                            )
                        }

                        Text(
                            text = if (state.preferences.smartAttendanceEnabled) "Auto Attendance: ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (state.preferences.smartAttendanceEnabled) MatrixMint else palette.textSecondary
                        )
                    }
                }
            }

            // Live Focus Ring (Pomodoro Liquid Study Timer)
            item {
                PomodoroFocusRingCard(
                    state = state,
                    viewModel = viewModel,
                    accent = accent
                )
            }

            // Multi-Selection Action Bar
            if (isSelectMode) {
                item {
                    IosSelectionBar(
                        selectedCount = state.selectedItemIds.size,
                        totalCount = activeDaySchedule.size,
                        onSelectAllToggle = {
                            viewModel.toggleSelectAll(activeDaySchedule.map { it.id })
                        },
                        onDeleteSelected = { showBatchDeleteConfirm = true },
                        onExitSelectMode = { viewModel.clearSelection() }
                    )
                }
            }

            // Real Week Calendar Strip
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(weekDays, key = { it.dayOfWeek }) { dayInfo ->
                        val isSelected = state.selectedDayOfWeek == dayInfo.dayOfWeek
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) accent else palette.cardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) accent else palette.cardBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setSelectedDayOfWeek(dayInfo.dayOfWeek) }
                                .padding(vertical = 10.dp)
                                .testTag("calendar_day_${dayInfo.dayOfWeek}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayInfo.dayNameShort.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${dayInfo.dayOfMonth}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary
                                )
                                if (dayInfo.isToday) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) (if (palette.isDark) Color.Black else Color.White) else accent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timeline Classes List
            if (activeDaySchedule.isEmpty()) {
                item {
                    EmptyPlaceholder(
                        icon = Icons.Default.CalendarMonth,
                        title = "No Classes for ${RealDateTimeUtils.getDayNumberName(state.selectedDayOfWeek)}",
                        message = "Your schedule is clear. Tap the + button in the bottom right to add a class."
                    )
                }
            } else {
                items(activeDaySchedule, key = { it.id }) { item ->
                    val isChecked = state.selectedItemIds.contains(item.id)

                    // Liquid Time-Block Capsule
                    IosCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_card_${item.id}"),
                        backgroundColor = palette.cardBg,
                        onClick = {
                            if (isSelectMode) {
                                viewModel.toggleItemSelection(item.id)
                            } else {
                                viewModel.openScheduleDetail(item)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelectMode) {
                                IosSelectionCheckbox(
                                    isSelected = isChecked,
                                    onToggle = { viewModel.toggleItemSelection(item.id) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // Time block
                            Column(modifier = Modifier.width(82.dp)) {
                                Text(
                                    text = item.timeRange.substringBefore(" - "),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = item.timeRange.substringAfter(" - "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Dynamic accent bar
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(accent)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = item.courseCode,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.room} • ${item.type}", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (!isSelectMode) {
                                IosDeleteButton(
                                    onClick = { scheduleToDelete = item },
                                    contentDescription = "Delete Class"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Repositioned Floating Action Button in the Bottom Right
        IosFloatingAddButton(
            onClick = { viewModel.openAddScheduleDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
            contentDescription = "Add Class Schedule"
        )

        // Add Schedule Dialog
        if (state.showAddScheduleDialog) {
            AddScheduleDialog(
                selectedDay = state.selectedDayOfWeek,
                accent = accent,
                onDismiss = { viewModel.closeAddScheduleDialog() },
                onAdd = { course, title, day, time, room, type ->
                    viewModel.addScheduleItem(course, title, day, time, room, type)
                }
            )
        }

        // GPA Simulator Dialog
        if (state.isGpaSimulatorOpen) {
            GpaSimulatorDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { viewModel.closeGpaSimulator() }
            )
        }

        // Schedule Item Preview Dialog
        state.activeScheduleInDetail?.let { item ->
            SchedulePreviewDialog(
                item = item,
                onDismiss = { viewModel.closeScheduleDetail() },
                onAddTask = {
                    viewModel.addTask(
                        title = "${item.courseCode} Preparation",
                        desc = "Review slides and notes for ${item.title} in ${item.room}",
                        courseCode = item.courseCode,
                        priority = TaskPriority.HIGH,
                        type = TaskType.ASSIGNMENT,
                        dueDays = 1
                    )
                    viewModel.closeScheduleDetail()
                },
                onDelete = {
                    viewModel.deleteScheduleItem(item.id)
                    viewModel.closeScheduleDetail()
                }
            )
        }

        // Confirm Single Delete
        scheduleToDelete?.let { item ->
            ConfirmDeleteDialog(
                title = "Delete Class",
                message = "Remove '${item.title}' from your schedule?",
                onConfirm = {
                    viewModel.deleteScheduleItem(item.id)
                    scheduleToDelete = null
                },
                onDismiss = { scheduleToDelete = null }
            )
        }

        // Confirm Batch Delete
        if (showBatchDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete Selected Classes",
                message = "Are you sure you want to delete ${state.selectedItemIds.size} selected classes? This cannot be undone.",
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
private fun PomodoroFocusRingCard(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    accent: Color
) {
    val palette = LocalThemePalette.current
    val isFocus by viewModel.isPomodoroFocus.collectAsStateWithLifecycle()
    val isRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val remaining by viewModel.pomodoroRemainingSec.collectAsStateWithLifecycle()
    val totalSec = (if (isFocus) state.preferences.pomodoroFocusMinutes else state.preferences.pomodoroBreakMinutes) * 60
    val progress = (1f - (remaining.toFloat() / totalSec.toFloat())).coerceIn(0f, 1f)

    val ringColor = if (isFocus) IosBlue else IosAmber
    val minutes = remaining / 60
    val seconds = remaining % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    IosCard(modifier = Modifier.fillMaxWidth().testTag("pomodoro_focus_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular Canvas Progress Ring with Liquid Glow
                Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 7.dp.toPx()
                        // Track
                        drawCircle(
                            color = ringColor.copy(alpha = 0.18f),
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(strokeWidth)
                        )
                        // Progress Arc
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isFocus) "FOCUS" else "REST",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = ringColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isFocus) "C++ Study Focus Block" else "Brain Rest Interval",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (isRunning) "Active session running smoothly" else "Paused • Ready to study",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Streak: ${state.studyStreak} focus blocks completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = ringColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ringColor)
                        .clickable {
                            if (isRunning) viewModel.pausePomodoro() else viewModel.startPomodoro()
                        }
                        .testTag("pomodoro_play_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = if (palette.isDark) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reset
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.cardBorder, CircleShape)
                        .clickable { viewModel.resetPomodoro() }
                        .testTag("pomodoro_reset_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GpaSimulatorDialog(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent
    val projections = state.courseProjections

    val calculatedGpa = remember(projections) {
        if (projections.isEmpty()) 3.85 else {
            val totalPoints = projections.sumOf { it.projectedGpaPoints * it.creditUnits }
            val totalUnits = projections.sumOf { it.creditUnits }
            if (totalUnits > 0) totalPoints / totalUnits else 4.0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("gpa_simulator_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "GPA Simulator & Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            "Dynamic syllabus final exam score calculator",
                            style = MaterialTheme.typography.bodySmall,
                            color = accent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frosted Radial Progress Gauge
                IosCard(modifier = Modifier.fillMaxWidth(), backgroundColor = palette.cardBg) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sw = 8.dp.toPx()
                                drawCircle(color = accent.copy(alpha = 0.2f), style = Stroke(sw))
                                drawArc(
                                    color = accent,
                                    startAngle = -90f,
                                    sweepAngle = ((calculatedGpa / 4.0).toFloat().coerceIn(0f, 1f)) * 360f,
                                    useCenter = false,
                                    style = Stroke(sw, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%.2f".format(calculatedGpa),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text("GPA", style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column {
                            Text("Target Benchmark", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                            Text("3.85 Cumulative", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (calculatedGpa >= 3.85) "Exceeding Honors Goal" else "Within reach with final scores",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (calculatedGpa >= 3.85) MatrixMint else IosAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Course Syllabus Score Simulations:", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(projections, key = { it.courseCode }) { proj ->
                        IosCard(modifier = Modifier.fillMaxWidth(), backgroundColor = palette.cardBg) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(proj.courseCode, fontWeight = FontWeight.Bold, color = accent)
                                    Text(proj.courseName, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Projected: %.1f%%".format(proj.projectedFinalGrade),
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text("Final Weight: ${proj.syllabusFinalWeight.toInt()}%", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Target Final: ${proj.simulatedFinalScore.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(110.dp)
                                )
                                Slider(
                                    value = proj.simulatedFinalScore.toFloat(),
                                    onValueChange = { viewModel.updateSimulationScore(proj.courseCode, it.toDouble()) },
                                    valueRange = 50f..100f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = if (palette.isDark) Color.Black else Color.White)
                ) {
                    Text("Apply & Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SchedulePreviewDialog(
    item: ScheduleItem,
    onDismiss: () -> Unit,
    onAddTask: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("schedule_preview_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(item.courseCode, fontWeight = FontWeight.Bold, color = accent, style = MaterialTheme.typography.labelMedium)
                    }

                    Text(
                        text = item.type,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textSecondary
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                // Info Rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(item.timeRange, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary, fontFamily = FontFamily.Monospace)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(item.room, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(RealDateTimeUtils.getDayNumberName(item.dayOfWeek), style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddTask,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = if (palette.isDark) Color.Black else Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Prep Task", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NothingRed.copy(alpha = 0.15f))
                            .border(1.dp, NothingRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NothingRed)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Close", color = palette.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun AddScheduleDialog(
    selectedDay: Int,
    accent: Color,
    onDismiss: () -> Unit,
    onAdd: (course: String, title: String, day: Int, time: String, room: String, type: String) -> Unit
) {
    val palette = LocalThemePalette.current
    var course by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Lecture") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("add_schedule_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "New Class Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course Code", color = palette.textSecondary) },
                    placeholder = { Text("e.g. CS201", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("schedule_course_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject / Topic", color = palette.textSecondary) },
                    placeholder = { Text("e.g. Data Structures & Algorithms", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("schedule_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time Range", color = palette.textSecondary) },
                    placeholder = { Text("e.g. 10:00 - 11:30", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Location", color = palette.textSecondary) },
                    placeholder = { Text("e.g. Hall B12", color = palette.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                            val finalCourse = course.ifBlank { "General" }
                            val finalTime = time.ifBlank { "TBD" }
                            val finalRoom = room.ifBlank { "TBD" }
                            onAdd(finalCourse, title, selectedDay, finalTime, finalRoom, type)
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
