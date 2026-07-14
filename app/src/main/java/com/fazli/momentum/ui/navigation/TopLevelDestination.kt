package com.fazli.momentum.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import com.fazli.momentum.R

/**
 * Bottom-nav destinations (SPEC §3). Placeholders — content built in later phases.
 */
enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    TODAY(
        route = "today",
        labelRes = R.string.tab_today,
        icon = Icons.Filled.DateRange,
    ),
    PLAN(
        route = "plan",
        labelRes = R.string.tab_plan,
        icon = Icons.Filled.List,
    ),
    PROGRESS(
        route = "progress",
        labelRes = R.string.tab_progress,
        icon = Icons.Filled.Info,
    ),
    SETTINGS(
        route = "settings",
        labelRes = R.string.tab_settings,
        icon = Icons.Filled.Settings,
    ),
}
