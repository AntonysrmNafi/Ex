package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Sell
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.ui.components.MonthSwitcher
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.components.TopBar
import com.blockveil.expense.tracker.util.CurrencyDisplay
import java.time.YearMonth

/**
 * The Analytics tab, complete: month switcher, forecast/anomaly callouts, income/expense
 * donut breakdowns, budget usage ring, monthly budget settings, and per-category envelope
 * budgets. Matches AnalyticsScreen in full (Bag 12 built the donuts, Bag 13 adds everything
 * else in the exact same order as the source design's JSX).
 */
@Composable
fun AnalyticsScreen(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    incomeTotals: List<CategoryTotal>,
    expenseTotals: List<CategoryTotal>,
    customExpenseCategories: List<CustomCategoryEntity>,
    customIncomeCategories: List<CustomCategoryEntity>,
    currency: CurrencyDisplay,
    forecast: Double?,
    effectiveBudget: Double,
    anomalies: List<AnomalyItem>,
    budgetCategories: List<BudgetCategoryUsage>,
    rawBudget: Double,
    rollingEnabled: Boolean,
    onSetBudget: (Double) -> Unit,
    onSetRolling: (Boolean) -> Unit,
    allExpenseCategories: List<String>,
    categoryBudgetMap: Map<String, Double>,
    categorySpendMap: Map<String, Double>,
    onSetCategoryLimit: (String, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopBar(title = "Analytics")

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonthSwitcher(yearMonth = currentMonth, onPrev = onPrevMonth, onNext = onNextMonth)
            }

            if (forecast != null) {
                item { ForecastCard(forecast = forecast, budget = effectiveBudget, currency = currency) }
            }

            if (anomalies.isNotEmpty()) {
                item { AnomalyCard(anomalies = anomalies, currency = currency) }
            }

            item {
                CategoryDonutCard(
                    title = "Income by category",
                    icon = Icons.Filled.ArrowCircleDown,
                    totals = incomeTotals,
                    isIncome = true,
                    currency = currency,
                    customExpenseCategories = customExpenseCategories,
                    customIncomeCategories = customIncomeCategories,
                    emptyText = "No income yet this month.",
                )
            }

            item {
                CategoryDonutCard(
                    title = "Expenses by category",
                    icon = Icons.Filled.ArrowCircleUp,
                    totals = expenseTotals,
                    isIncome = false,
                    currency = currency,
                    customExpenseCategories = customExpenseCategories,
                    customIncomeCategories = customIncomeCategories,
                    emptyText = "No expenses yet this month.",
                )
            }

            item { BudgetFillCard(categories = budgetCategories, currency = currency) }

            item {
                MonthlyBudgetCard(
                    budget = rawBudget,
                    rollingEnabled = rollingEnabled,
                    onSetBudget = onSetBudget,
                    onSetRolling = onSetRolling,
                )
            }

            item { SectionHeader(title = "Envelope budget (per category)", icon = Icons.Filled.Sell) }

            items(allExpenseCategories, key = { it }) { category ->
                EnvelopeRow(
                    category = category,
                    limit = categoryBudgetMap[category] ?: 0.0,
                    spent = categorySpendMap[category] ?: 0.0,
                    customExpenseCategories = customExpenseCategories,
                    currency = currency,
                    onSetLimit = { value -> onSetCategoryLimit(category, value) },
                )
            }
        }
    }
}
