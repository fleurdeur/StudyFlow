package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Calibrated Dual Eye-Care Palettes

// 1. OLED Ultra Black Theme: True black background (#000000) paired with 65% opaque dark glass cards (#121212)
val OledUltraBlackBg = Color(0xFF000000)               // Pure True OLED Black (#000000)
val OledUltraBlackSurface = Color(0xFF0A0A0E)          // Ultra Deep Inset
val OledGlassCard = Color(0xA6121212)                  // 65% opaque dark glass cards (#121212)
val OledGlassCardBorder = Color(0x33FFFFFF)            // Refractive rim border
val OledHairline = Color(0x1AFFFFFF)

// 2. Warm Paper Light Theme: Low-blue-light warm off-white (#F4F1EA) with frosted white glass (#FFFFFF at 70%)
val WarmPaperLightBg = Color(0xFFF4F1EA)               // Low-blue-light warm off-white (#F4F1EA)
val WarmPaperSurface = Color(0xFFEBE6DC)               // Low-contrast paper surface
val WarmPaperGlassCard = Color(0xB3FFFFFF)             // Frosted white glass layers (#FFFFFF at 70% opacity)
val WarmPaperGlassCardBorder = Color(0x287A7265)       // Low-contrast warm borders
val WarmPaperHairline = Color(0x1F7A7265)

// 3. E-Paper High-Contrast Monochrome
val EPaperWhiteBg = Color(0xFFFFFFFF)
val EPaperBlackText = Color(0xFF000000)
val EPaperCardBg = Color(0xFFFFFFFF)
val EPaperCardBorder = Color(0xFF000000)

// Backward compatibility references
val IosNothingBg = OledUltraBlackBg
val IosNothingSurface = OledUltraBlackSurface
val IosNothingCard = OledGlassCard
val IosNothingCardBorder = OledGlassCardBorder
val IosHairline = OledHairline

val IosLightBg = WarmPaperLightBg
val IosLightSurface = WarmPaperSurface
val IosLightCard = WarmPaperGlassCard
val IosLightCardBorder = WarmPaperGlassCardBorder
val IosLightHairline = WarmPaperHairline

// Accent System
val NothingRed = Color(0xFFFF3B30)            // Iconic Nothing / iOS Red
val IosBlue = Color(0xFF0A84FF)               // iOS Electric Blue
val MatrixMint = Color(0xFF30D158)            // iOS / Matrix Green
val MonoWhite = Color(0xFFFFFFFF)             // Stark Glyphs White
val MonoBlack = Color(0xFF1C1C1E)             // Stark Glyphs Charcoal Black
val IosAmber = Color(0xFFFF9F0A)              // iOS Warning Amber
val ElectricViolet = Color(0xFFBF5AF2)        // iOS Electric Violet

// Text Colors
val IosTextPrimary = Color(0xFFFFFFFF)
val IosTextSecondary = Color(0xFF98989F)      // Low-strain OLED secondary
val IosTextMuted = Color(0xFF636366)          // Tertiary

val TextPrimary = IosTextPrimary
val TextSecondary = IosTextSecondary
val TextMuted = IosTextMuted
val GlassObsidianDeep = Color(0xDD07070A)
val LiquidMint = MatrixMint

val IosLightTextPrimary = Color(0xFF24211D)   // Soft warm dark text (low blue light)
val IosLightTextSecondary = Color(0xFF696359) // Warm paper secondary
val IosLightTextMuted = Color(0xFF8F887C)

// Refractive border brushes
val IosNothingBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x40FFFFFF),
        Color(0x18FFFFFF),
        Color(0x0CFFFFFF)
    )
)
val LiquidGlassBorderBrush = IosNothingBorderBrush

val IosLightBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x35000000),
        Color(0x18000000),
        Color(0x0C000000)
    )
)

fun getAccentColor(themeName: String, isDark: Boolean = true): Color {
    return when (themeName) {
        "Nothing Red" -> if (isDark) Color(0xFFFF453A) else Color(0xFFFF3B30)
        "Matrix Mint" -> if (isDark) Color(0xFF30D158) else Color(0xFF34C759)
        "Stark Mono" -> if (isDark) MonoWhite else MonoBlack
        "Cyber Amber" -> if (isDark) Color(0xFFFF9F0A) else Color(0xFFFF9500)
        "Electric Violet" -> if (isDark) Color(0xFFBF5AF2) else Color(0xFFAF52DE)
        else -> if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF) // "iOS Blue"
    }
}
