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
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.SectionHeader

/**
 * Lists every income source, built-in and custom together, each with a Hide/Unhide and
 * Delete menu, same behavior as [CategoryManagementScreen] (expense categories): hiding or
 * deleting only affects the picker for new transactions, past transactions are unaffected.
 */
@Composable
fun SourceManagementScreen(
    sources: List<ManagedCategoryUiModel>,
    onSetHidden: (ManagedCategoryUiModel, Boolean) -> Unit,
    onDelete: (ManagedCategoryUiModel) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<ManagedCategoryUiModel?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackHeader(title = "Source Management", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Income sources", modifier = Modifier.padding(top = 4.dp))
            ManagedCategoryList(
                entries = sources,
                emptyText = "No income sources.",
                onSetHidden = onSetHidden,
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
