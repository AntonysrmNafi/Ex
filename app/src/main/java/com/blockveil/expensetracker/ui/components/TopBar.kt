package com.blockveil.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The small "BLOCKVEIL" eyebrow label above the current screen's big title. Matches TopBar.
 */
@Composable
fun TopBar(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)) {
        Text(
            text = "BLOCKVEIL",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
