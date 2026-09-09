package com.blockveil.expense.tracker.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.ui.components.ChipRow
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/** "From" account picker for a transfer/repayment: shows each account's balance. Matches the From ChipRow. */
@Composable
fun FromAccountPicker(
    label: String,
    accounts: List<AccountEntity>,
    selectedId: Long?,
    currency: CurrencyDisplay,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    PickerSection(label = label, modifier = modifier) {
        ChipRow(
            options = accounts,
            getKey = { it.id.toString() },
            getLabel = { "${it.name} · ${formatMoney(it.balance ?: 0.0, currency)}" },
            selectedKey = selectedId?.toString() ?: "",
            onSelect = { key -> key.toLongOrNull()?.let(onSelect) },
        )
    }
}

/** "To" account picker for a transfer: name only, no balance. Matches the To ChipRow exactly. */
@Composable
fun ToAccountPicker(
    accounts: List<AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    PickerSection(label = "To", modifier = modifier) {
        if (accounts.isEmpty()) {
            Text(
                text = "No other savings account to transfer to.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ChipRow(
                options = accounts,
                getKey = { it.id.toString() },
                getLabel = { it.name },
                selectedKey = selectedId?.toString() ?: "",
                onSelect = { key -> key.toLongOrNull()?.let(onSelect) },
            )
        }
    }
}

/** Loan picker for a repayment: shows how much remains on each loan. Matches the Loan ChipRow. */
@Composable
fun LoanPicker(
    loans: List<AccountEntity>,
    selectedId: Long?,
    currency: CurrencyDisplay,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    PickerSection(label = "Loan", modifier = modifier) {
        ChipRow(
            options = loans,
            getKey = { it.id.toString() },
            getLabel = { loan ->
                val remaining = (loan.principal ?: 0.0) - (loan.repaid ?: 0.0)
                "${loan.name} · ${formatMoney(remaining, currency)} left"
            },
            selectedKey = selectedId?.toString() ?: "",
            onSelect = { key -> key.toLongOrNull()?.let(onSelect) },
        )
    }
}

@Composable
private fun PickerSection(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        content()
    }
}
