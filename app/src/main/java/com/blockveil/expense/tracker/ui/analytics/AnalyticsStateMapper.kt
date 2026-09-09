package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.ui.graphics.Color
import com.blockveil.expense.tracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.EXPENSE_CATEGORIES
import com.blockveil.expense.tracker.util.categoryColor
import com.blockveil.expense.tracker.util.computeEffectiveBudget
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

data class AnalyticsSources(
    val transactions: List<TransactionEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val categoryBudgets: List<CategoryBudgetEntity>,
    val rawBudget: Double,
    val rollingEnabled: Boolean,
    val currency: CurrencyDisplay,
)

/** One category spending noticeably more than last month. Matches the `anomalies` shape exactly. */
data class AnomalyItem(val category: String, val percent: Int, val thisMonth: Double, val lastMonth: Double)

/** One category's budget usage for [BudgetFillChart]/[BudgetFillCard]. Matches `budgetCategories`. */
data class BudgetCategoryUsage(val name: String, val limit: Double, val spent: Double, val color: Color)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val incomeTotals: List<CategoryTotal> = emptyList(),
    val expenseTotals: List<CategoryTotal> = emptyList(),
    val customExpenseCategories: List<CustomCategoryEntity> = emptyList(),
    val customIncomeCategories: List<CustomCategoryEntity> = emptyList(),
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
    val forecast: Double? = null,
    val effectiveBudget: Double = 0.0,
    val anomalies: List<AnomalyItem> = emptyList(),
    val budgetCategories: List<BudgetCategoryUsage> = emptyList(),
    val categoryBudgetMap: Map<String, Double> = emptyMap(),
    val categorySpendMap: Map<String, Double> = emptyMap(),
    val rawBudget: Double = 0.0,
    val rollingEnabled: Boolean = false,
    val allExpenseCategories: List<String> = EXPENSE_CATEGORIES,
)

private const val ANOMALY_MIN_LAST_MONTH = 100.0
private const val ANOMALY_MIN_PERCENT = 30

/**
 * Matches AnalyticsScreen's expenseTotals/incomeTotals/budgetCategories/forecast/anomalies
 * derivations exactly, one difference noted below.
 *
 * The forecast pace projection uses the real "day of month so far" and this month's actual
 * length, instead of the source design's hardcoded BASE_DAY=15 and a fixed 31-day month
 * (both were only ever correct for the frozen preview date); the projection math itself
 * (spent-so-far / days-so-far * days-in-month) is identical.
 */
fun buildAnalyticsUiState(
    sources: AnalyticsSources,
    currentMonth: YearMonth,
    today: LocalDate = LocalDate.now(),
): AnalyticsUiState {
    val monthTransactions = sources.transactions.filter { YearMonth.from(it.date) == currentMonth }
    val monthExpenseItems = monthTransactions.filter { it.type == TransactionType.EXPENSE }

    val prevMonth = currentMonth.minusMonths(1)
    val prevMonthExpenseItems = sources.transactions.filter {
        it.type == TransactionType.EXPENSE && YearMonth.from(it.date) == prevMonth
    }

    fun totalsFor(type: TransactionType): List<CategoryTotal> = monthTransactions
        .filter { it.type == type }
        .groupBy { it.category }
        .map { (category, items) -> CategoryTotal(category, items.sumOf { it.amount }) }
        .sortedByDescending { it.total }

    val categorySpendMap = monthExpenseItems.groupBy { it.category }.mapValues { (_, items) -> items.sumOf { it.amount } }
    val prevCategorySpendMap = prevMonthExpenseItems.groupBy { it.category }.mapValues { (_, items) -> items.sumOf { it.amount } }

    val anomalies = categorySpendMap.entries
        .mapNotNull { (category, thisMonth) ->
            val lastMonth = prevCategorySpendMap[category]
            if (lastMonth == null || lastMonth < ANOMALY_MIN_LAST_MONTH) return@mapNotNull null
            val percent = (((thisMonth - lastMonth) / lastMonth) * 100).roundToInt()
            if (percent >= ANOMALY_MIN_PERCENT) AnomalyItem(category, percent, thisMonth, lastMonth) else null
        }
        .sortedByDescending { it.percent }

    val effectiveBudget = computeEffectiveBudget(sources.rawBudget, sources.rollingEnabled, currentMonth, sources.transactions)

    val forecast: Double? = if (currentMonth == YearMonth.now()) {
        val dayOfMonth = today.dayOfMonth
        if (dayOfMonth > 0) {
            val monthExpenseTotal = monthExpenseItems.sumOf { it.amount }
            (monthExpenseTotal / dayOfMonth) * currentMonth.lengthOfMonth()
        } else {
            null
        }
    } else {
        null
    }

    val categoryBudgetMap = sources.categoryBudgets.associate { it.category to it.limitAmount }
    val budgetCategories = categoryBudgetMap
        .filterValues { it > 0 }
        .map { (name, limit) ->
            BudgetCategoryUsage(
                name = name,
                limit = limit,
                spent = categorySpendMap[name] ?: 0.0,
                color = categoryColor(isIncome = false, category = name, customExpenseCategories = sources.customExpenseCategories),
            )
        }
        .sortedByDescending { it.limit }

    val allExpenseCategories = EXPENSE_CATEGORIES + sources.customExpenseCategories.map { it.name }

    return AnalyticsUiState(
        isLoading = false,
        incomeTotals = totalsFor(TransactionType.INCOME),
        expenseTotals = totalsFor(TransactionType.EXPENSE),
        customExpenseCategories = sources.customExpenseCategories,
        customIncomeCategories = sources.customIncomeCategories,
        currency = sources.currency,
        forecast = forecast,
        effectiveBudget = effectiveBudget,
        anomalies = anomalies,
        budgetCategories = budgetCategories,
        categoryBudgetMap = categoryBudgetMap,
        categorySpendMap = categorySpendMap,
        rawBudget = sources.rawBudget,
        rollingEnabled = sources.rollingEnabled,
        allExpenseCategories = allExpenseCategories,
    )
}
