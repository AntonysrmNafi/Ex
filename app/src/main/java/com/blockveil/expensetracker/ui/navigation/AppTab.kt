package com.blockveil.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/** The four bottom-nav destinations. Matches BottomNav's item list in the source design. */
enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    HISTORY("History", Icons.Filled.History),
    ANALYTICS("Analytics", Icons.Filled.BarChart),
    MORE("More", Icons.Filled.MoreHoriz),
}
