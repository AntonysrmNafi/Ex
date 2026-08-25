package com.blockveil.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.blockveil.expensetracker.ui.navigation.AppScaffold
import com.blockveil.expensetracker.ui.navigation.AppTab
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                AppRoot()
            }
        }
    }
}

/**
 * Wires up the shared scaffold and bottom nav from Bag 4. Each tab currently shows a
 * placeholder, real screens replace these one at a time starting with Home in Bag 5.
 */
@Composable
private fun AppRoot() {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    AppScaffold(selectedTab = selectedTab, onTabSelect = { selectedTab = it }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${selectedTab.label} screen, coming soon",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
