package com.fazli.momentum.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * MIDNIGHT OPERATOR — default theme (SPEC §5).
 * Hardcoded for Fase 0; later phases make this swappable via DataStore.
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
    primary = MidnightPrimary,
    secondary = MidnightSecondary,
    error = MidnightDanger,
    onBackground = MidnightOnSurface,
    onSurface = MidnightOnSurface,
    onSurfaceVariant = MidnightTextDim,
)
