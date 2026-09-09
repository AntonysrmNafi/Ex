package com.blockveil.expense.tracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.TopBar
import com.blockveil.expense.tracker.ui.components.TransactionRow
import com.blockveil.expense.tracker.ui.components.TransferRow
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * The History tab: search, quick filters, item count + totals, and a month-grouped list of
 * every transaction and transfer. Matches HistoryScreen.
 */
@Composable
fun HistoryScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    filterType: HistoryFilterType,
    onFilterChange: (HistoryFilterType) -> Unit,
    itemCount: Int,
    totalIncome: Double,
    totalExpense: Double,
    currency: CurrencyDisplay,
    groups: List<HistoryGroup>,
    onTxnClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopBar(title = "History")

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            AppTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search category, note, or account",
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                HistoryFilterType.entries.forEach { option ->
                    FilterChip(
                        label = option.label,
                        active = option == filterType,
                        onClick = { onFilterChange(option) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "$itemCount item${if (itemCount != 1) "s" else ""}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row {
                    if (totalIncome > 0) {
                        Text(text = "+${formatMoney(totalIncome, currency)} ", fontSize = 10.sp, color = BrandPrimary)
                    }
                    if (totalExpense > 0) {
                        Text(text = "-${formatMoney(totalExpense, currency)}", fontSize = 10.sp, color = BrandDanger)
                    }
                }
            }
        }

        if (groups.isEmpty()) {
            EmptyHistory()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
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
                        group.entries,
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

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val borderColor = if (active) BrandPrimary else MaterialTheme.colorScheme.outline
    Text(
        text = label,
        color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) BrandPrimary else Color.Transparent)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(50))
            .selectable(selected = active, onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Wallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = "No transactions match.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
