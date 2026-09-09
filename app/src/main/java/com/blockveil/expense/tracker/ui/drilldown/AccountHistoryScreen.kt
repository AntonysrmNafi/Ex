package com.blockveil.expense.tracker.ui.drilldown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.MonthGroup
import com.blockveil.expense.tracker.ui.components.TransactionRow
import com.blockveil.expense.tracker.ui.components.TransferRow
import com.blockveil.expense.tracker.ui.history.HistoryEntry
import com.blockveil.expense.tracker.util.CurrencyDisplay

/**
 * Every transaction and transfer/repayment/disbursement tied to one account, newest first,
 * grouped by month, a full ledger for that single account. Matches AccountHistoryScreen.
 */
@Composable
fun AccountHistoryScreen(
    accountName: String,
    subtitle: String,
    currency: CurrencyDisplay,
    groups: List<MonthGroup<HistoryEntry>>,
    onBack: () -> Unit,
    onTxnClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackHeader(title = accountName, onBack = onBack)

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .fillMaxWidth(),
        )

        if (groups.isEmpty()) {
            Text(
                text = "No activity on this account yet.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
            ) {
                groups.forEach { group ->
                    item {
                        Text(
                            text = group.monthLabel.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                        )
                    }
                    items(
                        group.items,
                        key = { entry ->
                            when (entry) {
                                is HistoryEntry.TransactionItem -> "t${entry.model.id}"
                                is HistoryEntry.TransferItem -> "x${entry.model.id}"
                            }
                        },
                    ) { entry ->
                        Box(modifier = Modifier.padding(bottom = 8.dp)) {
                            when (entry) {
                                is HistoryEntry.TransactionItem -> TransactionRow(
                                    category = entry.model.category,
                                    note = entry.model.note,
                                    metaLine = entry.model.metaLine,
                                    amount = entry.model.amount,
                                    currency = currency,
                                    isIncome = entry.model.isIncome,
                                    icon = entry.model.icon,
                                    iconTint = entry.model.iconTint,
                                    hasReceipt = entry.model.hasReceipt,
                                    onClick = { onTxnClick(entry.model.id) },
                                )
                                is HistoryEntry.TransferItem -> TransferRow(model = entry.model, currency = currency)
                            }
                        }
                    }
                }
            }
        }
    }
}
