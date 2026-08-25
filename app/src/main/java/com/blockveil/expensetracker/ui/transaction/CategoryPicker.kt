package com.blockveil.expensetracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expensetracker.ui.components.AppTextField
import com.blockveil.expensetracker.ui.components.dashedBorder
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme

/**
 * Category (or "Source" for income) chip picker, with an inline "+ Custom" add flow.
 * Matches the source design's category section, including handleAddCustomCategory's
 * case-insensitive duplicate check: typing an existing category's name just selects it
 * rather than creating a second copy.
 */
@Composable
fun CategoryPicker(
    label: String,
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onAddCustomCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addingCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

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
                    .selectable(selected = addingCustom, onClick = { addingCustom = !addingCustom }, role = Role.Button)
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

        if (addingCustom) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                AppTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = "New category name",
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val name = customText.trim()
                        if (name.isNotEmpty()) {
                            if (categories.none { it.equals(name, ignoreCase = true) }) {
                                onAddCustomCategory(name)
                            }
                            onSelect(name)
                            customText = ""
                            addingCustom = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text(text = "Add", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
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
                onAddCustomCategory = {},
            )
        }
    }
}
