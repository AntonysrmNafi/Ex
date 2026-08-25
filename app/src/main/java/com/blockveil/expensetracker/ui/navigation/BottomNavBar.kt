package com.blockveil.expensetracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * The 4-tab bottom navigation bar. Matches BottomNav in the source design: 18dp icons,
 * 9sp labels, no pill indicator, a hairline top border. Uses a custom Row rather than
 * Material 3's NavigationBar because that component's built-in sizing and selection
 * indicator don't match this design.
 */
@Composable
fun BottomNavBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
    ) {
        AppTab.entries.forEach { tab ->
            val active = tab == selected
            val tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = active, onClick = { onSelect(tab) }, role = Role.Tab)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null, // the label Text below is merged in as this tab's accessible name
                    tint = tint,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = tab.label, style = MaterialTheme.typography.labelSmall, color = tint)
            }
        }
    }
}
