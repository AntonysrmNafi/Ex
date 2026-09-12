package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.SectionHeader

/**
 * Lists every user-created income source with a delete button. Kept separate from
 * [CategoryManagementScreen] (expense categories) since income entries are conceptually
 * "sources" (salary, freelance, gift) rather than categories, even though under the hood
 * they're the same [CustomCategoryEntity] table with isIncome = true.
 *
 * Deleting one is non-destructive to past transactions, same as deleting a custom expense
 * category: the source name stays on old transactions, they just lose the custom color.
 */
@Composable
fun SourceManagementScreen(
    incomeSources: List<CustomCategoryEntity>,
    onDelete: (CustomCategoryEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<CustomCategoryEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackHeader(title = "Source Management", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Income sources", modifier = Modifier.padding(top = 4.dp))
            CustomEntryList(
                entries = incomeSources,
                emptyText = "No custom income sources yet.",
                onDeleteRequest = { pendingDelete = it },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        ConfirmDialog(
            title = "Delete source?",
            message = "\"${toDelete.name}\" will no longer appear as a source choice. Past transactions keep their source name.",
            onConfirm = {
                onDelete(toDelete)
                pendingDelete = null
            },
            onCancel = { pendingDelete = null },
        )
    }
}
