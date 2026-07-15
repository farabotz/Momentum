package com.fazli.momentum.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.fazli.momentum.data.AppTheme

/**
 * MIDNIGHT OPERATOR — default theme (SPEC §5).
 */
private val MidnightBackground = Color(0xFF14171C)
private val MidnightSurface = Color(0xFF1B1F26)
private val MidnightSurfaceInset = Color(0xFF0F1216)
private val MidnightPrimary = Color(0xFFD9A441)   // amber
private val MidnightSecondary = Color(0xFF5FA9A0) // teal
private val MidnightDanger = Color(0xFFC1503F)
private val MidnightOnSurface = Color(0xFFEDE8DC)
private val MidnightTextDim = Color(0xFF9AA1AC)
private val MidnightTextFaint = Color(0xFF5B6270)

val MidnightColorScheme = darkColorScheme(
    background = MidnightBackground,
    surface = MidnightSurface,
    surfaceVariant = MidnightSurfaceInset,
    outline = MidnightTextFaint,
    primary = MidnightPrimary,
    onPrimary = MidnightBackground,
    secondary = MidnightSecondary,
    error = MidnightDanger,
    onError = MidnightOnSurface,
    onBackground = MidnightOnSurface,
    onSurface = MidnightOnSurface,
    onSurfaceVariant = MidnightTextDim,
)

/**
 * WARM PAPER theme (SPEC §5).
 */
val WarmPaperColorScheme = lightColorScheme(
    background = Color(0xFFF4ECDD),
    surface = Color(0xFFFBF6EC),
    surfaceVariant = Color(0xFFE0D4BD),
    outline = Color(0xFFE0D4BD),
    primary = Color(0xFFB5651D),
    onPrimary = Color(0xFFFBF6EC),
    secondary = Color(0xFF5A7D4F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    onBackground = Color(0xFF4A3F2C),
    onSurface = Color(0xFF4A3F2C),
    onSurfaceVariant = Color(0xFF8A7A5C),
)

/**
 * CLEAN LIGHT theme (SPEC §5).
 */
val CleanLightColorScheme = lightColorScheme(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFFE8E8E8),
    outline = Color(0xFFE8E8E8),
    primary = Color(0xFF3B6EF0),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF16A34A),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF888888),
)

/**
 * FOREST CALM theme (SPEC §5).
 */
val ForestCalmColorScheme = darkColorScheme(
    background = Color(0xFF12211C),
    surface = Color(0xFF1A2E27),
    surfaceVariant = Color(0xFF244038),
    outline = Color(0xFF244038),
    primary = Color(0xFF3FAE7A),
    onPrimary = Color(0xFF12211C),
    secondary = Color(0xFFE0B062),
    error = MidnightDanger,
    onError = Color(0xFFE8F0EA),
    onBackground = Color(0xFFE8F0EA),
    onSurface = Color(0xFFE8F0EA),
    onSurfaceVariant = Color(0xFF8FA99C),
)

fun colorSchemeFor(theme: AppTheme): ColorScheme = when (theme) {
    AppTheme.MIDNIGHT -> MidnightColorScheme
    AppTheme.WARM_PAPER -> WarmPaperColorScheme
    AppTheme.CLEAN_LIGHT -> CleanLightColorScheme
    AppTheme.FOREST_CALM -> ForestCalmColorScheme
}

fun labelFor(theme: AppTheme): String = when (theme) {
    AppTheme.MIDNIGHT -> "Midnight Operator"
    AppTheme.WARM_PAPER -> "Warm Paper"
    AppTheme.CLEAN_LIGHT -> "Clean Light"
    AppTheme.FOREST_CALM -> "Forest Calm"
}
