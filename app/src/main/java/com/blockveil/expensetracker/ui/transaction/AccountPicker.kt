package com.blockveil.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.ui.components.ChipRow
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme

/**
 * Savings-account chip picker for the transaction form. Reuses [ChipRow] from Bag 4 since
 * the layout is identical (a wrapping row of pill buttons, one selected). Matches the
 * source design's Account section, including its empty-state message for the edge case
 * where a transaction's original account has since been deleted.
 */
@Composable
fun AccountPicker(
    accounts: List<AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Account",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (accounts.isEmpty()) {
            Text(
                text = "No savings accounts available (this transaction's account may have been deleted).",
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

@Preview(showBackground = true)
@Composable
private fun AccountPickerPreview() {
    ExpenseTrackerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AccountPicker(
                accounts = listOf(
                    AccountEntity(id = 1, name = "City Bank", category = AccountCategory.SAVINGS, balance = 45230.0),
                    AccountEntity(id = 2, name = "Cash", category = AccountCategory.SAVINGS, balance = 1200.0),
                ),
                selectedId = 1,
                onSelect = {},
            )
        }
    }
}
