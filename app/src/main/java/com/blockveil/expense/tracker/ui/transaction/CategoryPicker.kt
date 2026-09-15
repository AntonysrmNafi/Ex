package com.blockveil.expense.tracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.dashedBorder
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.ui.theme.ExpenseTrackerTheme

/**
 * Category (or "Source" for income) chip picker. Tapping "+ Custom" navigates away to
 * Settings > Category/Source Management instead of adding one inline here (see
 * TransactionFormScreen's onNavigateToCustom), since that's where color and icon are chosen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    label: String,
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onNavigateToCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                CategoryChip(label = category, active = category == selected, onClick = { onSelect(category) })
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .dashedBorder(color = MaterialTheme.colorScheme.onSurfaceVariant, cornerRadius = 20.dp)
                    .selectable(selected = false, onClick = onNavigateToCustom, role = Role.Button)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(11.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Custom", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, active: Boolean, onClick: () -> Unit) {
    val borderColor = if (active) BrandPrimary else MaterialTheme.colorScheme.outline
    Text(
        text = label,
        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) BrandPrimary else Color.Transparent)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(50))
            .selectable(selected = active, onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryPickerPreview() {
    ExpenseTrackerTheme {
        Column(modifier = Modifier.width(340.dp).padding(16.dp)) {
            CategoryPicker(
                label = "Category",
                categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other"),
                selected = "Food",
                onSelect = {},
                onNavigateToCustom = {},
            )
        }
    }
}
