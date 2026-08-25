package com.blockveil.expensetracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expensetracker.ui.components.AccountStripCard
import com.blockveil.expensetracker.ui.components.AppCard
import com.blockveil.expensetracker.ui.components.SectionHeader
import com.blockveil.expensetracker.ui.components.TopBar
import com.blockveil.expensetracker.ui.components.TransactionRow
import com.blockveil.expensetracker.ui.components.categoryIcon
import com.blockveil.expensetracker.ui.theme.BrandDanger
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme
import com.blockveil.expensetracker.ui.theme.faded
import com.blockveil.expensetracker.util.CurrencyDisplay
import com.blockveil.expensetracker.util.formatMoney
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The Home tab: date filter row, this month's spending card, an account strip, and recent
 * activity. Matches HomeScreen. The FAB's absolute web positioning (bottom-24 right-5, to
 * clear the bottom nav) becomes a simple BottomEnd-aligned overlay here, since Compose's
 * Scaffold already reserves space for the bottom nav via [androidx.compose.foundation.layout.PaddingValues].
 */
@Composable
fun HomeScreen(
    filterLabel: String,
    isMonthFilter: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenDateFilter: () -> Unit,
    expenseTotal: Double,
    incomeTotal: Double,
    budget: Double,
    showBudget: Boolean,
    currency: CurrencyDisplay,
    accounts: List<AccountStripUiModel>,
    hasSavingsAccount: Boolean,
    recentTransactions: List<TransactionRowUiModel>,
    totalTransactionCount: Int,
    onTxnClick: (Long) -> Unit,
    onAccountClick: (Long) -> Unit,
    onSeeAllHistory: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recentLimit = 5

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = "Expenses")

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DateFilterRow(
                        filterLabel = filterLabel,
                        isMonthFilter = isMonthFilter,
                        onPrevMonth = onPrevMonth,
                        onNextMonth = onNextMonth,
                        onOpenDateFilter = onOpenDateFilter,
                    )
                }

                item {
                    BudgetSummaryCard(
                        expenseTotal = expenseTotal,
                        incomeTotal = incomeTotal,
                        budget = budget,
                        showBudget = showBudget,
                        currency = currency,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                if (accounts.isNotEmpty()) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 4.dp),
                        ) {
                            items(accounts, key = { it.id }) { account ->
                                AccountStripCard(
                                    name = account.name,
                                    displayAmount = account.displayAmount,
                                    currency = currency,
                                    isLoan = account.isLoan,
                                    subLabel = account.subLabel,
                                    onClick = { onAccountClick(account.id) },
                                )
                            }
                        }
                    }
                }

                if (!hasSavingsAccount) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Column {
                                Text(
                                    text = "Add a savings account to get started",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                                Text(
                                    text = "Expenses and income are recorded against a savings account " +
                                        "(bank, mobile wallet, cash, or investment). Add one from the More tab first.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Recent activity",
                        right = if (recentTransactions.isNotEmpty()) {
                            {
                                Text(
                                    text = "See all",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPrimary,
                                    modifier = Modifier.clickable(onClick = onSeeAllHistory),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }

                if (recentTransactions.isEmpty()) {
                    item {
                        EmptyRecentActivity(showBudget = showBudget, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    items(recentTransactions, key = { it.id }) { txn ->
                        TransactionRow(
                            category = txn.category,
                            note = txn.note,
                            metaLine = txn.metaLine,
                            amount = txn.amount,
                            currency = currency,
                            isIncome = txn.isIncome,
                            icon = txn.icon,
                            iconTint = txn.iconTint,
                            hasReceipt = txn.hasReceipt,
                            onClick = { onTxnClick(txn.id) },
                        )
                    }
                    if (totalTransactionCount > recentLimit) {
                        item {
                            Text(
                                text = "+${totalTransactionCount - recentLimit} more this month, see History",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onSeeAllHistory)
                                    .padding(vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            containerColor = if (hasSavingsAccount) BrandPrimary else BrandPrimary.copy(alpha = 0.45f),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .size(48.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add a transaction")
        }
    }
}

@Composable
private fun DateFilterRow(
    filterLabel: String,
    isMonthFilter: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenDateFilter: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenDateFilter, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Filter by date",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = filterLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (isMonthFilter) {
            Row {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        } else {
            Spacer(modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun BudgetSummaryCard(
    expenseTotal: Double,
    incomeTotal: Double,
    budget: Double,
    showBudget: Boolean,
    currency: CurrencyDisplay,
    modifier: Modifier = Modifier,
) {
    val net = incomeTotal - expenseTotal
    val overBudget = showBudget && budget > 0 && expenseTotal >= budget
    val progressFraction = if (showBudget && budget > 0) min(expenseTotal / budget, 1.0).toFloat() else 0f
    val statusColor = if (overBudget) BrandDanger else BrandPrimary

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total spent" + if (showBudget) " this month" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showBudget && budget > 0) {
                    Text(
                        text = "${(progressFraction * 100).roundToInt()}% of budget",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.faded())
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Text(
                text = formatMoney(expenseTotal, currency),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )

            if (showBudget && budget > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(RoundedCornerShape(50))
                            .background(statusColor),
                    )
                }
                Text(
                    text = if (overBudget) {
                        "Over budget (${formatMoney(budget, currency)})"
                    } else {
                        "Budget: ${formatMoney(budget, currency)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (overBudget) BrandDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BudgetStat(
                    label = "Spent",
                    value = formatMoney(expenseTotal, currency),
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                BudgetStat(
                    label = "Income",
                    value = formatMoney(incomeTotal, currency),
                    valueColor = BrandPrimary,
                    modifier = Modifier.weight(1f),
                )
                BudgetStat(
                    label = "Net",
                    value = (if (net >= 0) "+" else "-") + formatMoney(kotlin.math.abs(net), currency),
                    valueColor = if (net >= 0) BrandPrimary else BrandDanger,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BudgetStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun EmptyRecentActivity(showBudget: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
            text = "Nothing recorded" + (if (showBudget) " this month" else " for this filter") +
                " yet.\nTap + to add an expense or income.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val currency = CurrencyDisplay(
        symbol = "৳",
        position = com.blockveil.expensetracker.data.model.CurrencyPosition.PREFIX,
        format = com.blockveil.expensetracker.data.model.CurrencyFormat.GROUPED,
    )
    ExpenseTrackerTheme {
        HomeScreen(
            filterLabel = "July 2026",
            isMonthFilter = true,
            onPrevMonth = {},
            onNextMonth = {},
            onOpenDateFilter = {},
            expenseTotal = 3120.0,
            incomeTotal = 6000.0,
            budget = 6000.0,
            showBudget = true,
            currency = currency,
            accounts = listOf(
                AccountStripUiModel(1, "City Bank", isLoan = false, displayAmount = 45230.0, subLabel = "Bank"),
                AccountStripUiModel(2, "Cash", isLoan = false, displayAmount = 1200.0, subLabel = "Cash"),
                AccountStripUiModel(3, "Car Loan", isLoan = true, displayAmount = 85000.0, subLabel = "Loan remaining"),
            ),
            hasSavingsAccount = true,
            recentTransactions = listOf(
                TransactionRowUiModel(
                    id = 1, category = "Food", isIncome = false,
                    icon = categoryIcon(false, "Food"), iconTint = MaterialTheme.colorScheme.error,
                    note = "Lunch", metaLine = "14 Jul 2026 · City Bank", amount = 450.0, hasReceipt = true,
                ),
                TransactionRowUiModel(
                    id = 2, category = "Salary", isIncome = true,
                    icon = categoryIcon(true, "Salary"), iconTint = BrandPrimary,
                    note = "July salary", metaLine = "1 Jul 2026 · City Bank", amount = 6000.0, hasReceipt = false,
                ),
            ),
            totalTransactionCount = 8,
            onTxnClick = {},
            onAccountClick = {},
            onSeeAllHistory = {},
            onAdd = {},
        )
    }
}
