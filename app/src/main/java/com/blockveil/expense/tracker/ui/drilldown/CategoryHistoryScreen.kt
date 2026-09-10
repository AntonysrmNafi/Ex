package com.blockveil.expense.tracker.ui.drilldown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.MonthGroup
import com.blockveil.expense.tracker.ui.components.TransactionRow
import com.blockveil.expense.tracker.ui.components.TransactionRowUiModel
import com.blockveil.expense.tracker.ui.components.categoryIcon
import com.blockveil.expense.tracker.ui.theme.faded
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * Every transaction in one category, newest first, grouped by month. Matches
 * CategoryHistoryScreen exactly.
 */
@Composable
fun CategoryHistoryScreen(
    category: String,
    isIncome: Boolean,
    itemCount: Int,
    total: Double,
    currency: CurrencyDisplay,
    badgeColor: Color,
    groups: List<MonthGroup<TransactionRowUiModel>>,
    onBack: () -> Unit,
    onTxnClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BackHeader(
            title = category,
            onBack = onBack,
            leadingBadge = {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(badgeColor.faded()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = categoryIcon(isIncome, category),
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp),
                    )
                }
            },
        )

        Text(
            text = "$itemCount transaction${if (itemCount != 1) "s" else ""} · " +
                "${if (isIncome) "+" else "-"}${formatMoney(total, currency)} total",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .fillMaxWidth(),
        )

        if (groups.isEmpty()) {
            Text(
                text = "No transactions in this category.",
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
                    items(group.items, key = { it.id }) { model ->
                        TransactionRow(
                            category = model.category,
                            note = model.note,
                            metaLine = model.metaLine,
                            amount = model.amount,
                            currency = currency,
                            isIncome = model.isIncome,
                            icon = model.icon,
                            iconTint = model.iconTint,
                            hasReceipt = model.hasReceipt,
                            onClick = { onTxnClick(model.id) },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
