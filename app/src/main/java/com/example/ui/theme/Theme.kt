package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class IosThemePalette(
    val isDark: Boolean,
    val isEPaper: Boolean = false,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val hairline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val dockBg: Color,
    val dockBorder: Color,
    val borderBrush: Brush,
    val specularHighlight: Color = Color.White.copy(alpha = 0.5f)
)

val LocalAccentColor = compositionLocalOf { IosBlue }
val LocalThemePalette = compositionLocalOf {
    IosThemePalette(
        isDark = true,
        isEPaper = false,
        background = OledUltraBlackBg,
        surface = OledUltraBlackSurface,
        cardBg = OledGlassCard,
        cardBorder = OledGlassCardBorder,
        hairline = OledHairline,
        textPrimary = IosTextPrimary,
        textSecondary = IosTextSecondary,
        textMuted = IosTextMuted,
        accent = IosBlue,
        dockBg = Color(0xDD07070A),
        dockBorder = Color(0x35FFFFFF),
        borderBrush = IosNothingBorderBrush
    )
}

@Composable
fun StudyFlowTheme(
    isDarkMode: Boolean = true,
    accentColor: Color = IosBlue,
    isEPaperMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when {
        isEPaperMode -> {
            IosThemePalette(
                isDark = isDarkMode,
                isEPaper = true,
                background = if (isDarkMode) Color(0xFF000000) else Color(0xFFFFFFFF),
                surface = if (isDarkMode) Color(0xFF111111) else Color(0xFFF9F9F9),
                cardBg = if (isDarkMode) Color(0xFF181818) else Color(0xFFFFFFFF),
                cardBorder = if (isDarkMode) Color(0xFF444444) else Color(0xFF000000),
                hairline = if (isDarkMode) Color(0xFF333333) else Color(0xFFCCCCCC),
                textPrimary = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000),
                textSecondary = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF333333),
                textMuted = if (isDarkMode) Color(0xFF777777) else Color(0xFF666666),
                accent = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF000000),
                dockBg = if (isDarkMode) Color(0xFF111111) else Color(0xFFEEEEEE),
                dockBorder = if (isDarkMode) Color(0xFF444444) else Color(0xFF000000),
                borderBrush = Brush.linearGradient(
                    listOf(
                        if (isDarkMode) Color(0xFF555555) else Color(0xFF000000),
                        if (isDarkMode) Color(0xFF333333) else Color(0xFF222222)
                    )
                ),
                specularHighlight = Color.Transparent
            )
        }
        isDarkMode -> {
            IosThemePalette(
                isDark = true,
                isEPaper = false,
                background = OledUltraBlackBg,
                surface = OledUltraBlackSurface,
                cardBg = OledGlassCard,
                cardBorder = OledGlassCardBorder,
                hairline = OledHairline,
                textPrimary = IosTextPrimary,
                textSecondary = IosTextSecondary,
                textMuted = IosTextMuted,
                accent = accentColor,
                dockBg = Color(0xDD07070A),
                dockBorder = Color(0x35FFFFFF),
                borderBrush = IosNothingBorderBrush,
                specularHighlight = accentColor.copy(alpha = 0.5f)
            )
        }
        else -> {
            IosThemePalette(
                isDark = false,
                isEPaper = false,
                background = WarmPaperLightBg,
                surface = WarmPaperSurface,
                cardBg = WarmPaperGlassCard,
                cardBorder = WarmPaperGlassCardBorder,
                hairline = WarmPaperHairline,
                textPrimary = IosLightTextPrimary,
                textSecondary = IosLightTextSecondary,
                textMuted = IosLightTextMuted,
                accent = accentColor,
                dockBg = Color(0xEEFFFFFF),
                dockBorder = Color(0x28000000),
                borderBrush = IosLightBorderBrush,
                specularHighlight = Color.White.copy(alpha = 0.85f)
            )
        }
    }

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = OledUltraBlackBg,
            primaryContainer = accentColor.copy(alpha = 0.2f),
            onPrimaryContainer = accentColor,
            secondary = NothingRed,
            onSecondary = OledUltraBlackBg,
            tertiary = MatrixMint,
            background = if (isEPaperMode) Color.Black else OledUltraBlackBg,
            onBackground = IosTextPrimary,
            surface = if (isEPaperMode) Color(0xFF111111) else OledUltraBlackSurface,
            onSurface = IosTextPrimary,
            surfaceVariant = Color(0xFF14141A),
            onSurfaceVariant = IosTextSecondary,
            outline = OledGlassCardBorder,
            outlineVariant = OledHairline
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.15f),
            onPrimaryContainer = accentColor,
            secondary = NothingRed,
            onSecondary = Color.White,
            tertiary = MatrixMint,
            background = if (isEPaperMode) Color.White else WarmPaperLightBg,
            onBackground = IosLightTextPrimary,
            surface = if (isEPaperMode) Color(0xFFF9F9F9) else WarmPaperSurface,
            onSurface = IosLightTextPrimary,
            surfaceVariant = Color(0xFFE4DFD5),
            onSurfaceVariant = IosLightTextSecondary,
            outline = WarmPaperGlassCardBorder,
            outlineVariant = WarmPaperHairline
        )
    }

    val tilt = rememberTiltVector().value

    CompositionLocalProvider(
        LocalAccentColor provides accentColor,
        LocalThemePalette provides palette,
        LocalTiltVector provides tilt
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
