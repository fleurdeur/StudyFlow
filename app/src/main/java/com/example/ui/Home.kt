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
import androidx.compose.material.icons.filled.*
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
import com.example.AppScreen
import com.example.AppViewModel
import com.example.StudyFlowUiState
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    var deckToDelete by remember { mutableStateOf<ReviewerDeck?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleItem?>(null) }

    val palette = LocalThemePalette.current
    val accent = palette.accent
    val currentDayOfWeek = remember { RealDateTimeUtils.getCurrentDayOfWeek() }
    val todayClasses = remember(state.schedules, currentDayOfWeek) {
        state.schedules.filter { it.dayOfWeek == currentDayOfWeek }.sortedBy { it.timeRange }
    }
    val nextClass = todayClasses.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Date, Quick Theme Toggle & Settings Navigation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            letterSpacing = (-0.5).sp
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
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Light / Dark Mode Toggle Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.cardBg)
                            .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleDarkMode() }
                            .testTag("quick_theme_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.preferences.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light/Dark Mode",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.cardBg)
                            .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
                            .testTag("nav_to_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Intelligent Next Class Countdown Banner
        if (nextClass != null && state.preferences.smartScheduleAlerts) {
            item {
                IosCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = accent.copy(alpha = 0.12f),
                    borderBrush = palette.borderBrush,
                    onClick = { viewModel.openScheduleDetail(nextClass) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UPCOMING TODAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                CourseBadge(code = nextClass.courseCode)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${nextClass.title} • ${nextClass.timeRange}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Location: ${nextClass.room} • ${nextClass.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Quick Metrics Row (iOS Grouped Style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricIosCard(
                    title = "Streak",
                    value = "${state.studyStreak}d",
                    caption = "Study Streak 🔥",
                    accentColor = IosAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricIosCard(
                    title = "Retention",
                    value = "${state.masteryPercentage}%",
                    caption = "Active Recall",
                    accentColor = accent,
                    modifier = Modifier.weight(1f)
                )
                MetricIosCard(
                    title = "Reviewed",
                    value = "${state.cardsReviewedToday}",
                    caption = "Cards Today",
                    accentColor = MatrixMint,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Schedule Timeline Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSectionHeader(
                    title = "Today's Schedule",
                    actionText = "Calendar",
                    onActionClick = { viewModel.navigateTo(AppScreen.SCHEDULE) }
                )

                if (todayClasses.isEmpty()) {
                    EmptyPlaceholder(
                        icon = Icons.Default.CalendarToday,
                        title = "No Classes Today",
                        message = "Enjoy your free day or prepare ahead for upcoming exams."
                    )
                } else {
                    todayClasses.forEach { scheduleItem ->
                        IosCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.openScheduleDetail(scheduleItem) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.width(80.dp)) {
                                    Text(
                                        text = scheduleItem.timeRange.substringBefore(" - "),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = scheduleItem.timeRange.substringAfter(" - "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(accent)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scheduleItem.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CourseBadge(code = scheduleItem.courseCode)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${scheduleItem.room} • ${scheduleItem.type}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                IosDeleteButton(
                                    onClick = { scheduleToDelete = scheduleItem },
                                    contentDescription = "Delete Class"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Priority Deadlines Section
        item {
            val pendingTasks = state.tasks.filter { !it.isCompleted }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSectionHeader(
                    title = "Pending Tasks",
                    actionText = "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.TASKS) }
                )

                if (pendingTasks.isEmpty()) {
                    EmptyPlaceholder(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "No Pending Deadlines",
                        message = "Zero pending assignments. You're completely caught up."
                    )
                } else {
                    pendingTasks.take(3).forEach { task ->
                        IosCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.openTaskDetail(task) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular check glyph
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (task.isCompleted) accent else Color.Transparent)
                                        .border(2.dp, if (task.isCompleted) accent else palette.textMuted, CircleShape)
                                        .clickable { viewModel.toggleTaskCompletion(task.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (palette.isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CourseBadge(code = task.courseCode)
                                        Text(
                                            text = RealDateTimeUtils.formatDueDate(task.dueDaysAway),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (task.dueDaysAway <= 1 && !task.isCompleted) NothingRed else palette.textSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) palette.textMuted else palette.textPrimary,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
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

        // Reviewer Decks Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSectionHeader(
                    title = "Reviewer Decks",
                    actionText = "Studio",
                    onActionClick = { viewModel.navigateTo(AppScreen.REVIEWER) }
                )

                if (state.decks.isEmpty()) {
                    EmptyPlaceholder(
                        icon = Icons.Default.Psychology,
                        title = "No Reviewer Decks",
                        message = "Synthesize flashcards from lecture slides in Reviewer Studio."
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.decks, key = { it.id }) { deck ->
                            IosCard(
                                modifier = Modifier.width(220.dp),
                                onClick = { viewModel.openDeckDetail(deck) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CourseBadge(code = deck.courseCode)
                                    IosDeleteButton(
                                        onClick = { deckToDelete = deck },
                                        contentDescription = "Delete Deck"
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = deck.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${deck.cards.size} flashcards",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accent)
                                        .clickable { viewModel.startStudySession(deck) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Study Deck",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (palette.isDark) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm dialogs
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

    taskToDelete?.let { task ->
        ConfirmDeleteDialog(
            title = "Delete Assignment",
            message = "Remove '${task.title}'?",
            onConfirm = {
                viewModel.deleteTask(task.id)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }

    deckToDelete?.let { deck ->
        ConfirmDeleteDialog(
            title = "Delete Reviewer Deck",
            message = "Remove deck '${deck.title}'?",
            onConfirm = {
                viewModel.deleteDeck(deck.id)
                deckToDelete = null
            },
            onDismiss = { deckToDelete = null }
        )
    }
}

@Composable
private fun MetricIosCard(
    title: String,
    value: String,
    caption: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current

    IosCard(
        modifier = modifier,
        backgroundColor = palette.cardBg
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    val palette = LocalThemePalette.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
            )
        }

        Text(
            text = actionText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = palette.accent,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}
