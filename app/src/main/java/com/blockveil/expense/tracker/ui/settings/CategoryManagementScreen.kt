package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.PageHeader
import com.blockveil.expense.tracker.ui.components.SectionHeader

/**
 * Lists every user-created category (expense and income, separately) with a delete button.
 * Deleting one is non-destructive to past transactions: [com.blockveil.expense.tracker.util.categoryColor]
 * already falls back to a neutral color for a category name that no longer has a custom
 * entry, so old entries keep their category name, they just lose the custom color.
 * Renaming/editing isn't here yet, just view + delete, for now.
 */
@Composable
fun CategoryManagementScreen(
    expenseCategories: List<CustomCategoryEntity>,
    incomeCategories: List<CustomCategoryEntity>,
    onDelete: (CustomCategoryEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<CustomCategoryEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageHeader(title = "Category Management", onClose = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Expense categories", modifier = Modifier.padding(top = 4.dp))
            CategoryList(
                categories = expenseCategories,
                emptyText = "No custom expense categories yet.",
                onDeleteRequest = { pendingDelete = it },
                modifier = Modifier.padding(bottom = 20.dp),
            )

            SectionHeader(title = "Income categories")
            CategoryList(
                categories = incomeCategories,
                emptyText = "No custom income categories yet.",
                onDeleteRequest = { pendingDelete = it },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        ConfirmDialog(
            title = "Delete category?",
            message = "\"${toDelete.name}\" will no longer appear as a category choice. Past transactions keep their category name.",
            onConfirm = {
                onDelete(toDelete)
                pendingDelete = null
            },
            onCancel = { pendingDelete = null },
        )
    }
}

@Composable
private fun CategoryList(
    categories: List<CustomCategoryEntity>,
    emptyText: String,
    onDeleteRequest: (CustomCategoryEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (categories.isEmpty()) {
            Text(text = emptyText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        categories.forEach { category ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(category.color)),
                        )
                        Text(
                            text = category.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete ${category.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp).clickable { onDeleteRequest(category) },
                    )
                }
            }
        }
    }
}
