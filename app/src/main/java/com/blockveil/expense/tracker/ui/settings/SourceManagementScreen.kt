package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandPrimary

/**
 * Lists every income source, built-in and custom together, each with a Hide/Unhide and
 * Delete menu, same behavior as [CategoryManagementScreen] (expense categories): hiding or
 * deleting only affects the picker for new transactions, past transactions are unaffected.
 * The "Add Custom Source" button at the top always stays visible and opens [AddCustomCategoryScreen].
 */
@Composable
fun SourceManagementScreen(
    sources: List<ManagedCategoryUiModel>,
    onSetHidden: (ManagedCategoryUiModel, Boolean) -> Unit,
    onDelete: (ManagedCategoryUiModel) -> Unit,
    onAddCustom: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<ManagedCategoryUiModel?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackHeader(title = "Source Management", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Button(
                onClick = onAddCustom,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Add Custom Source", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            SectionHeader(title = "Income sources")
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
