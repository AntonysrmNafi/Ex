package com.blockveil.expensetracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared shell for the 4 main tabs: a background surface, the tab content, and the bottom
 * nav bar. Screens that push on top of a tab (transaction details, settings, category
 * drill-downs, etc) render outside this scaffold entirely, matching how the source design
 * layers its slideInRight overlays above everything including the bottom nav.
 */
@Composable
fun AppScaffold(
    selectedTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(selected = selectedTab, onSelect = onTabSelect) },
    ) { innerPadding ->
        content(innerPadding)
    }
}
