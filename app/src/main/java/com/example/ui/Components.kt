package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

fun Modifier.iosNothingGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.Unspecified,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp
): Modifier = this.then(
    Modifier
        .clip(shape)
        .background(if (backgroundColor != Color.Unspecified) backgroundColor else IosNothingCard)
        .border(borderWidth, borderBrush ?: IosNothingBorderBrush, shape)
)

// Compatibility alias for liquidGlass
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.Unspecified,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp
): Modifier = this.iosNothingGlass(shape, backgroundColor, borderBrush, borderWidth)

@Composable
fun IosCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = LocalThemePalette.current.cardBg,
    borderBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalThemePalette.current
    val tilt = LocalTiltVector.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(120),
        label = "ios_card_press"
    )

    // Keyed dynamic specular reflection driven by accelerometer tilt
    val dynamicBorder = borderBrush ?: remember(tilt, palette.accent, palette.isDark, palette.isEPaper) {
        createDynamicSpecularBorderBrush(tilt, palette.accent, palette.isDark, palette.isEPaper)
    }

    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, dynamicBorder, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = palette.accent.copy(alpha = 0.15f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(18.dp),
        content = content
    )
}

// Repositioned Floating Action Button in the Bottom Right
@Composable
fun IosFloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Create New"
) {
    val palette = LocalThemePalette.current
    val accent = palette.accent
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(100),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(accent)
            .border(
                1.5.dp,
                if (palette.isDark) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .testTag("fab_add_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription,
            tint = if (palette.isDark) Color.Black else Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

// Single Authoritative "+" Action Button (kept for header actions or secondary buttons)
@Composable
fun IosAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalThemePalette.current.accent,
    contentDescription: String = "Create New"
) {
    val palette = LocalThemePalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "plus_btn_scale"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(accentColor)
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(13.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            )
            .testTag("action_add_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription,
            tint = if (palette.isDark) Color.Black else Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

// iOS Red Trash Action Button
@Composable
fun IosDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Delete"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(NothingRed.copy(alpha = 0.14f))
            .border(1.dp, NothingRed.copy(alpha = 0.35f), CircleShape)
            .testTag("delete_action_btn")
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = contentDescription,
            tint = NothingRed,
            modifier = Modifier.size(17.dp)
        )
    }
}

// iOS Large Title Header with Nothing Dot Indicator, Sort & Select Mode Controls
@Composable
fun IosLargeTitleHeader(
    title: String,
    subtitle: String? = null,
    onSortClick: (() -> Unit)? = null,
    onSelectClick: (() -> Unit)? = null,
    isSelectActive: Boolean = false,
    onAddClick: (() -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Nothing OS glyph dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                )
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort Button
            if (onSortClick != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { onSortClick() }
                        .testTag("header_sort_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort and Filter",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Select Mode Toggle Button
            if (onSelectClick != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelectActive) palette.accent.copy(alpha = 0.25f) else palette.cardBg)
                        .border(
                            1.dp,
                            if (isSelectActive) palette.accent else palette.cardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectClick() }
                        .testTag("header_select_mode_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Select Mode",
                        tint = if (isSelectActive) palette.accent else palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (trailingAction != null) {
                trailingAction()
            }
            if (onAddClick != null) {
                IosAddButton(onClick = onAddClick)
            }
        }
    }
}

@Composable
fun IosSortButton(
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) palette.accent.copy(alpha = 0.25f) else palette.cardBg)
            .border(1.dp, if (isActive) palette.accent else palette.cardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("ios_sort_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = "Sort and Filter",
            tint = if (isActive) palette.accent else palette.textPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun IosSelectModeButton(
    onClick: () -> Unit,
    isSelectMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelectMode) palette.accent.copy(alpha = 0.25f) else palette.cardBg)
            .border(1.dp, if (isSelectMode) palette.accent else palette.cardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("ios_select_mode_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Checklist,
            contentDescription = "Select Mode",
            tint = if (isSelectMode) palette.accent else palette.textPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun IosActionButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = LocalThemePalette.current.accent,
    enabled: Boolean = true
) {
    val palette = LocalThemePalette.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) accentColor else palette.hairline)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) (if (palette.isDark) Color.Black else Color.White) else palette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) (if (palette.isDark) Color.Black else Color.White) else palette.textMuted
            )
        }
    }
}

// Compatibility Aliases
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = LocalThemePalette.current.accent,
    enabled: Boolean = true
) = IosActionButton(onClick, text, modifier, icon, accentColor, enabled)

@Composable
fun LiquidGlassDeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Delete"
) = IosDeleteButton(onClick, modifier, contentDescription)

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = LocalThemePalette.current.cardBg,
    borderBrush: Brush = LocalThemePalette.current.borderBrush,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = IosCard(modifier, shape, backgroundColor, borderBrush, onClick, content)

@Composable
fun CourseBadge(
    code: String,
    modifier: Modifier = Modifier,
    colorHex: Long = 0xFF0A84FF
) {
    val color = Color(colorHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("course_badge_$code")
    ) {
        Text(
            text = code,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EmptyPlaceholder(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.cardBg)
            .border(1.dp, palette.cardBorder, RoundedCornerShape(22.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(palette.hairline)
                .border(1.dp, palette.cardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(palette.surface)
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            NothingRed.copy(alpha = 0.5f),
                            palette.cardBorder
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .padding(22.dp)
                .testTag("confirm_delete_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("Cancel", color = palette.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = NothingRed, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_delete_btn")
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
