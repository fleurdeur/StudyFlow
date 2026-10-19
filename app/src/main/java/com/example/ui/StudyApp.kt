package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppScreen
import com.example.AppViewModel
import com.example.data.UnifiedSortFilterState
import com.example.ui.theme.*

data class NavigationTab(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun StudyApp(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalThemePalette.current
    val accentColor = palette.accent

    // Handle back button for sub-sessions and secondary tabs
    BackHandler(
        enabled = state.isNotePageOpen ||
                state.activeTaskInDetail != null ||
                state.activeScheduleInDetail != null ||
                state.activeDeckInDetail != null ||
                state.activeStudySession != null ||
                state.activeQuizSession != null ||
                state.activeDocumentModal != null ||
                state.activeSortFilterModule != null ||
                state.isSelectionMode ||
                state.currentScreen != AppScreen.DASHBOARD
    ) {
        when {
            state.activeSortFilterModule != null -> viewModel.closeSortFilterSheet()
            state.isSelectionMode -> viewModel.clearSelection()
            state.isNotePageOpen -> viewModel.closeNotePage()
            state.activeTaskInDetail != null -> viewModel.closeTaskDetail()
            state.activeScheduleInDetail != null -> viewModel.closeScheduleDetail()
            state.activeDeckInDetail != null -> viewModel.closeDeckDetail()
            state.activeStudySession != null -> viewModel.closeStudySession()
            state.activeQuizSession != null -> viewModel.closeQuizSession()
            state.activeDocumentModal != null -> viewModel.dismissDocumentModal()
            state.currentScreen != AppScreen.DASHBOARD -> viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Main Screen Scaffold
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (state.activeStudySession == null && state.activeQuizSession == null && !state.isNotePageOpen) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val tabs = listOf(
                            NavigationTab(AppScreen.DASHBOARD, "Home", Icons.Default.Dashboard, Icons.Outlined.Dashboard, "nav_home"),
                            NavigationTab(AppScreen.SCHEDULE, "Schedule", Icons.Default.CalendarToday, Icons.Outlined.CalendarToday, "nav_schedule"),
                            NavigationTab(AppScreen.TASKS, "Tasks", Icons.Default.CheckCircle, Icons.Outlined.CheckCircle, "nav_tasks"),
                            NavigationTab(AppScreen.NOTES, "Notes", Icons.Default.Description, Icons.Outlined.Description, "nav_notes"),
                            NavigationTab(AppScreen.REVIEWER, "Reviewer", Icons.Default.Psychology, Icons.Outlined.Psychology, "nav_reviewer"),
                            NavigationTab(AppScreen.SETTINGS, "Settings", Icons.Default.Settings, Icons.Outlined.Settings, "nav_settings")
                        )

                        // iOS 27 + Nothing Phone OS Floating Island Dock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(palette.dockBg)
                                .border(1.dp, palette.dockBorder, RoundedCornerShape(26.dp))
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .testTag("main_navigation_bar"),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEach { tab ->
                                val selected = state.currentScreen == tab.screen
                                val scale by animateFloatAsState(
                                    targetValue = if (selected) 1.05f else 1.0f,
                                    animationSpec = tween(120),
                                    label = "tab_scale"
                                )

                                Column(
                                    modifier = Modifier
                                        .scale(scale)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            viewModel.navigateTo(tab.screen)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag(tab.testTag),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = if (selected) accentColor else palette.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    if (selected) {
                                        // Nothing OS Dot Glyph indicator
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                        )
                                    } else {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = palette.textMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(160))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "screen_animated_content"
            ) { currentScreen ->
                when (currentScreen) {
                    AppScreen.DASHBOARD -> HomeScreen(state = state, viewModel = viewModel)
                    AppScreen.SCHEDULE -> ScheduleScreen(state = state, viewModel = viewModel)
                    AppScreen.TASKS -> TasksScreen(state = state, viewModel = viewModel)
                    AppScreen.NOTES -> NotesScreen(state = state, viewModel = viewModel)
                    AppScreen.REVIEWER -> ReviewerScreen(state = state, viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(state = state, viewModel = viewModel)
                }
            }
        }

        // Full-Page Dedicated Note Editor & Viewer
        AnimatedVisibility(
            visible = state.isNotePageOpen,
            enter = slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(260)) + fadeIn(animationSpec = tween(260)),
            exit = slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(220)) + fadeOut(animationSpec = tween(220))
        ) {
            NoteEditorViewerScreen(
                note = state.activeNoteInViewer,
                courses = state.courses,
                viewModel = viewModel,
                onClose = { viewModel.closeNotePage() }
            )
        }

        // Global Consistent Unified Sort & Filter Bottom Sheet
        state.activeSortFilterModule?.let { module ->
            val currentSortFilter = state.sortFilters[module] ?: UnifiedSortFilterState(module)
            val categories = state.courses.map { it.code }

            UnifiedSortFilterBottomSheet(
                module = module,
                currentState = currentSortFilter,
                availableCategories = categories,
                onApply = { newState ->
                    viewModel.applySortFilter(module, newState)
                },
                onDismiss = {
                    viewModel.closeSortFilterSheet()
                }
            )
        }
    }
}
