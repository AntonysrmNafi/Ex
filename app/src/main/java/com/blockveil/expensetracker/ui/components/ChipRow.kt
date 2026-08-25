package com.blockveil.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.blockveil.expensetracker.ui.theme.BrandPrimary

private val ChipShape = RoundedCornerShape(50)

/**
 * A wrapping row of pill-shaped filter chips, exactly one of which is selected at a time.
 * Matches ChipRow in the source design.
 */
@Composable
fun <T> ChipRow(
    options: List<T>,
    getKey: (T) -> String,
    getLabel: (T) -> String,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val key = getKey(option)
            val active = key == selectedKey
            val borderColor = if (active) BrandPrimary else MaterialTheme.colorScheme.outline
            val textColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            val backgroundColor = if (active) BrandPrimary else Color.Transparent

            Text(
                text = getLabel(option),
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(ChipShape)
                    .background(backgroundColor)
                    .border(width = 1.dp, color = borderColor, shape = ChipShape)
                    .selectable(selected = active, onClick = { onSelect(key) }, role = Role.Button)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
