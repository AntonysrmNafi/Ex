package com.blockveil.expensetracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expensetracker.ExpenseTrackerApp
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.ui.components.AppCard
import com.blockveil.expensetracker.ui.components.AppTextField
import com.blockveil.expensetracker.ui.components.CalendarDialog
import com.blockveil.expensetracker.ui.components.PageHeader
import com.blockveil.expensetracker.ui.theme.BrandDanger
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.util.formatDateDisplay
import java.time.LocalDate

/**
 * Add/edit an income or expense entry. Matches TransactionPage: type selector, amount,
 * category + custom-add, account, location (expense only), note, date, receipt photo,
 * save/delete. Transfer and Repay Loan are selectable (matching the design's 4-button
 * grid) but their full flow, including saving, is Bag 9 — for now they show the same
 * "not enough accounts" guard messages the source design has, or a "coming soon" note.
 *
 * Hosts [TransactionFormViewModel] directly rather than being split into a stateless
 * Screen + a Route wrapper: with 9 interdependent fields and cross-field defaults, forcing
 * everything through callback params would be more boilerplate than clarity. Home follows
 * the Screen/Route split because its state is simpler and more reusable (e.g. previewable).
 */
@Composable
fun TransactionFormScreen(
    existingId: Long?,
    onClose: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: TransactionFormViewModel = viewModel(
        key = "transaction_form_$existingId",
        factory = TransactionFormViewModel.factory(app.container, existingId),
    )

    val sources by viewModel.sources.collectAsState()
    val fields = viewModel.fields
    val category = effectiveCategory(fields, sources)
    val accountId = effectiveAccountId(fields, sources)
    val savingsAccounts = sources.accounts.filter { it.category == AccountCategory.SAVINGS }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.isSaved, viewModel.isDeleted) {
        if (viewModel.isSaved || viewModel.isDeleted) onClose()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(title = "Transaction", onClose = onClose)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            TransactionTypeSelector(
                selected = fields.type,
                locked = viewModel.isEditing,
                onSelect = viewModel::onTypeChange,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            AppTextField(
                value = fields.amount,
                onValueChange = viewModel::onAmountChange,
                label = "Amount",
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
                errorText = fields.errorMessage,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            when (fields.type) {
                TransactionFormType.INCOME, TransactionFormType.EXPENSE -> {
                    val isIncome = fields.type == TransactionFormType.INCOME
                    if (savingsAccounts.isEmpty() && !viewModel.isEditing) {
                        NoSavingsAccountGuard(isIncome = isIncome)
                    } else {
                        CategoryPicker(
                            label = if (isIncome) "Source" else "Category",
                            categories = categoryListFor(fields.type, sources),
                            selected = category,
                            onSelect = viewModel::onCategoryChange,
                            onAddCustomCategory = viewModel::onAddCustomCategory,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        AccountPicker(
                            accounts = savingsAccounts,
                            selectedId = accountId,
                            onSelect = viewModel::onAccountChange,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        if (!isIncome) {
                            AppTextField(
                                value = fields.location,
                                onValueChange = viewModel::onLocationChange,
                                label = "Location (optional)",
                                placeholder = "Location",
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                        }

                        AppTextField(
                            value = fields.note,
                            onValueChange = viewModel::onNoteChange,
                            label = "Note (optional)",
                            placeholder = "Note",
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        DateButton(
                            date = fields.date,
                            onClick = { showDatePicker = true },
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        ReceiptPhotoField(
                            label = if (isIncome) "Attachment (optional)" else "Receipt",
                            photoUri = fields.photoUri,
                            onPhotoChange = { viewModel.onPhotoChange(it); photoError = null },
                            onError = { photoError = it },
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        photoError?.let {
                            Text(text = it, fontSize = 10.sp, color = BrandDanger, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                TransactionFormType.TRANSFER -> TransferOrRepayPlaceholder(
                    guardMessage = if (savingsAccounts.size < 2) {
                        "Add at least 2 savings accounts to transfer money between them."
                    } else {
                        null
                    },
                    comingSoonMessage = "Transfers between accounts are coming in a future update.",
                )

                TransactionFormType.REPAY -> TransferOrRepayPlaceholder(
                    guardMessage = if (savingsAccounts.isEmpty()) {
                        "You need an active loan and a savings account to make a repayment."
                    } else {
                        null
                    },
                    comingSoonMessage = "Loan repayment is coming in a future update.",
                )
            }
        }

        BottomBar(
            saveLabel = saveLabelFor(fields.type),
            saveEnabled = fields.type == TransactionFormType.INCOME || fields.type == TransactionFormType.EXPENSE,
            onSave = viewModel::onSave,
            showDelete = viewModel.isEditing,
            onDeleteRequest = { showDeleteConfirm = true },
        )
    }

    if (showDatePicker) {
        CalendarDialog(
            initialDate = fields.date,
            onSelect = { viewModel.onDateChange(it); showDatePicker = false },
            onCancel = { showDatePicker = false },
        )
    }

    if (showDeleteConfirm) {
        // Interim Material AlertDialog; replaced by the source design's exact ConfirmDialog
        // styling in Bag 18 once every shared dialog gets its final pass together.
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Delete this entry?") },
            text = { Text(text = "This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.onDelete() }) {
                    Text(text = "Delete", color = BrandDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(text = "Cancel") }
            },
        )
    }
}

@Composable
private fun NoSavingsAccountGuard(isIncome: Boolean) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column {
            Text(
                text = "Add a savings account first",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = "You need at least one savings account before recording " +
                    (if (isIncome) "income" else "an expense") + ". Add one from the More tab.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransferOrRepayPlaceholder(guardMessage: String?, comingSoonMessage: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = guardMessage ?: comingSoonMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DateButton(date: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = "Date: ${formatDateDisplay(date)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BottomBar(
    saveLabel: String,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    showDelete: Boolean,
    onDeleteRequest: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)) {
            Button(
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = saveLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (showDelete) {
                TextButton(onClick = onDeleteRequest, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = BrandDanger, modifier = Modifier.padding(end = 6.dp))
                    Text(text = "Delete entry", color = BrandDanger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun saveLabelFor(type: TransactionFormType): String = when (type) {
    TransactionFormType.INCOME -> "Save Income"
    TransactionFormType.EXPENSE -> "Save Expense"
    TransactionFormType.TRANSFER -> "Transfer"
    TransactionFormType.REPAY -> "Repay Loan"
}
