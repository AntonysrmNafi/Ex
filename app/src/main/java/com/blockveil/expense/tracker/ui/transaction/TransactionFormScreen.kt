package com.blockveil.expense.tracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
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
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.CalendarDialog
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback
import com.blockveil.expense.tracker.ui.components.PageHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.formatDateDisplay
import java.time.LocalDate

/**
 * Add/edit an income or expense entry, or record a transfer/loan repayment. Matches
 * TransactionPage: type selector, amount, category + custom-add, account, location (expense
 * only), note, date, receipt photo for income/expense; From/To or Loan/Pay-from pickers for
 * transfer/repay; save/delete for all four.
 *
 * Hosts [TransactionFormViewModel] directly rather than being split into a stateless
 * Screen + a Route wrapper: with this many interdependent fields and cross-field defaults,
 * forcing everything through callback params would be more boilerplate than clarity. Home
 * follows the Screen/Route split because its state is simpler and more reusable (e.g.
 * previewable).
 */
@Composable
fun TransactionFormScreen(
    existingId: Long?,
    onClose: () -> Unit,
    instanceKey: Long = System.nanoTime(),
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: TransactionFormViewModel = viewModel(
        key = "transaction_form_${existingId}_$instanceKey",
        factory = TransactionFormViewModel.factory(app.container, existingId),
    )

    val feedback = LocalAppFeedback.current
    val sources by viewModel.sources.collectAsState()
    val fields = viewModel.fields
    val category = effectiveCategory(fields, sources)
    val accountId = effectiveAccountId(fields, sources)
    val savingsAccounts = sources.accounts.filter { it.category == AccountCategory.SAVINGS }
    val fromId = effectiveFromId(fields, sources)
    val toId = effectiveToId(fields, sources, fromId)
    val loanId = effectiveLoanId(fields, sources)
    val activeLoanAccounts = activeLoans(sources)

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.isSaved, viewModel.isDeleted) {
        if (viewModel.isSaved || viewModel.isDeleted) {
            viewModel.feedbackMessage?.let { feedback.showToast(it) }
            if (viewModel.triggersMoneyBurst) {
                feedback.triggerMoneyBurst()
                feedback.playTransactionSound()
            }
            onClose()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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

                TransactionFormType.TRANSFER -> {
                    if (savingsAccounts.size < 2) {
                        TransferOrRepayPlaceholder("Add at least 2 savings accounts to transfer money between them.")
                    } else {
                        FromAccountPicker(
                            label = "From",
                            accounts = savingsAccounts,
                            selectedId = fromId,
                            currency = sources.currency,
                            onSelect = viewModel::onFromChange,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        ToAccountPicker(
                            accounts = savingsAccounts.filter { it.id != fromId },
                            selectedId = toId,
                            onSelect = viewModel::onToChange,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        AppTextField(
                            value = fields.note,
                            onValueChange = viewModel::onNoteChange,
                            label = "Note (optional)",
                            placeholder = "Note (optional)",
                        )
                    }
                }

                TransactionFormType.REPAY -> {
                    if (activeLoanAccounts.isEmpty() || savingsAccounts.isEmpty()) {
                        TransferOrRepayPlaceholder("You need an active loan and a savings account to make a repayment.")
                    } else {
                        LoanPicker(
                            loans = activeLoanAccounts,
                            selectedId = loanId,
                            currency = sources.currency,
                            onSelect = viewModel::onLoanChange,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        FromAccountPicker(
                            label = "Pay from",
                            accounts = savingsAccounts,
                            selectedId = fromId,
                            currency = sources.currency,
                            onSelect = viewModel::onFromChange,
                        )
                    }
                }
            }
        }

        BottomBar(
            saveLabel = saveLabelFor(fields.type),
            saveEnabled = true,
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
        ConfirmDialog(
            title = "Delete this entry?",
            message = "This can't be undone.",
            onConfirm = { showDeleteConfirm = false; viewModel.onDelete() },
            onCancel = { showDeleteConfirm = false },
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
private fun TransferOrRepayPlaceholder(message: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        ) {
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
