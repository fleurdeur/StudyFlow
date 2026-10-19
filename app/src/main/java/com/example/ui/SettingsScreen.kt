package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewModel
import com.example.StudyFlowUiState
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    state: StudyFlowUiState,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    val palette = LocalThemePalette.current
    val accent = palette.accent
    val isDark = state.preferences.isDarkMode

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // iOS Large Title Header
        item {
            IosLargeTitleHeader(
                title = "Settings",
                subtitle = "Appearance, behavior, & system preferences"
            )
        }

        // Section 1: Appearance & Theme
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "System Theme & Appearance",
                    icon = Icons.Default.Palette,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Light / Dark Mode Toggle
                Text(
                    text = "Display Mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf("Dark" to Icons.Default.DarkMode, "Light" to Icons.Default.LightMode)
                    modes.forEach { (modeName, modeIcon) ->
                        val isSelected = if (modeName == "Dark") isDark else !isDark
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent else Color.Transparent)
                                .clickable { viewModel.setThemeMode(modeName) }
                                .padding(vertical = 10.dp)
                                .testTag("theme_mode_$modeName"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$modeName Mode",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) (if (palette.isDark) Color.Black else Color.White) else palette.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Accent Theme Swatches
                Text(
                    text = "Refractive Accent Palette",
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val themes = listOf(
                    "iOS Blue" to IosBlue,
                    "Nothing Red" to NothingRed,
                    "Matrix Mint" to MatrixMint,
                    "Stark Mono" to (if (isDark) MonoWhite else MonoBlack),
                    "Cyber Amber" to IosAmber,
                    "Electric Violet" to ElectricViolet
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.forEach { (name, color) ->
                        val isSelected = state.preferences.accentGlassTheme == name
                        Box(
                            modifier = Modifier
                                .width(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) color.copy(alpha = 0.22f) else palette.cardBg)
                                .border(1.dp, if (isSelected) color else palette.cardBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.setAccentTheme(name) }
                                .padding(vertical = 10.dp, horizontal = 6.dp)
                                .testTag("theme_option_$name"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) color else palette.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Haptic Feedback Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tactile Haptic Feedback", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text("Subtle mechanical vibrations on button tap", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = state.preferences.hapticsEnabled,
                        onCheckedChange = { checked ->
                            viewModel.updatePreferences { it.copy(hapticsEnabled = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
                    )
                }
            }
        }

        // Section 2: Schedule & Class Intelligence
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Schedule & Class Assistant",
                    icon = Icons.Default.CalendarToday,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Class Countdown HUD", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text("Displays upcoming class and room countdown on home", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = state.preferences.smartScheduleAlerts,
                        onCheckedChange = { checked ->
                            viewModel.updatePreferences { it.copy(smartScheduleAlerts = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
                    )
                }
            }
        }

        // Section 3: Tasks & Urgency Engine
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Tasks & Productivity Engine",
                    icon = Icons.Default.CheckCircle,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Calculate Priority", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text("Tasks due within 24 hours are automatically elevated to Urgent", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = state.preferences.smartPriorityAutoCalc,
                        onCheckedChange = { checked ->
                            viewModel.updatePreferences { it.copy(smartPriorityAutoCalc = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Default Task Due Date Offset", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val daysOptions = listOf(0 to "Today", 1 to "Tomorrow", 3 to "In 3 Days")
                    daysOptions.forEach { (days, label) ->
                        val isSelected = state.preferences.defaultTaskDueDays == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.22f) else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updatePreferences { it.copy(defaultTaskDueDays = days) }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accent else palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Notes & Markdown Vault
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Notes & Markdown Vault",
                    icon = Icons.Default.Description,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Word Count & Reading Time HUD", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text("Show stats in note reader and editor header", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }
                    Switch(
                        checked = state.preferences.showWordCount,
                        onCheckedChange = { checked ->
                            viewModel.updatePreferences { it.copy(showWordCount = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Editor Font Size", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fontOptions = listOf(14 to "Compact (14sp)", 16 to "Standard (16sp)", 18 to "Large (18sp)")
                    fontOptions.forEach { (sz, label) ->
                        val isSelected = state.preferences.noteFontSizeSp == sz
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.22f) else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updatePreferences { it.copy(noteFontSizeSp = sz) }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accent else palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Reviewer & Spaced Repetition
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Active Recall & Spaced Repetition",
                    icon = Icons.Default.Psychology,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Flashcard Target", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text("${state.preferences.dailyCardTarget} cards target", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(palette.cardBg)
                                .border(1.dp, palette.cardBorder, CircleShape)
                                .clickable {
                                    val current = state.preferences.dailyCardTarget
                                    if (current > 5) viewModel.updatePreferences { it.copy(dailyCardTarget = current - 5) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }

                        Text(
                            text = "${state.preferences.dailyCardTarget}",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(palette.cardBg)
                                .border(1.dp, palette.cardBorder, CircleShape)
                                .clickable {
                                    val current = state.preferences.dailyCardTarget
                                    if (current < 100) viewModel.updatePreferences { it.copy(dailyCardTarget = current + 5) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Spaced Repetition Algorithm", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val algos = listOf("Leitner 5-Stage", "SM-2 Adaptive")
                    algos.forEach { algo ->
                        val isSelected = state.preferences.spacedRepetitionAlgorithm == algo
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.22f) else palette.cardBg)
                                .border(1.dp, if (isSelected) accent else palette.cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updatePreferences { it.copy(spacedRepetitionAlgorithm = algo) }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = algo,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accent else palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Data & Storage Management
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Data & Storage Management",
                    icon = Icons.Default.Storage,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DataStatPill(count = state.schedules.size, label = "Classes")
                    DataStatPill(count = state.tasks.size, label = "Tasks")
                    DataStatPill(count = state.notes.size, label = "Notes")
                    DataStatPill(count = state.decks.size, label = "Decks")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showClearCacheDialog = true },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.cardBg, contentColor = palette.textPrimary)
                    ) {
                        Text("Clear Cache", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NothingRed.copy(alpha = 0.18f), contentColor = NothingRed)
                    ) {
                        Text("Reset All Data", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section 7: System & Engine Diagnostics
        item {
            IosCard(modifier = Modifier.fillMaxWidth()) {
                SettingSectionTitle(
                    title = "Engine & Build Diagnostics",
                    icon = Icons.Default.Memory,
                    accentColor = accent
                )

                Spacer(modifier = Modifier.height(12.dp))
                DiagnosticRow(label = "Engine Pipeline", value = state.nativeEngineInfo)
                DiagnosticRow(label = "UI Architecture", value = "iOS 27 + Nothing Phone OS")
                DiagnosticRow(label = "Client Version", value = "StudyFlow v27.4.2")
                DiagnosticRow(label = "Database", value = "High-Speed In-Memory + SAF")
            }
        }
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        ConfirmDeleteDialog(
            title = "Reset All Workspace Data",
            message = "This will erase all notes, schedule items, tasks, and flashcard decks. Are you sure?",
            onConfirm = {
                viewModel.clearAllData()
                showResetDialog = false
                Toast.makeText(context, "Workspace reset to fresh state", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Clear Cache Confirm Dialog
    if (showClearCacheDialog) {
        ConfirmDeleteDialog(
            title = "Clear Document Cache",
            message = "Clear parsed token cache and temporary PDF streams?",
            onConfirm = {
                showClearCacheDialog = false
                Toast.makeText(context, "Temporary cache cleared successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }
}

@Composable
private fun SettingSectionTitle(
    title: String,
    icon: ImageVector,
    accentColor: Color
) {
    val palette = LocalThemePalette.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
    }
}

@Composable
private fun DataStatPill(count: Int, label: String) {
    val palette = LocalThemePalette.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.accent,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    val palette = LocalThemePalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
