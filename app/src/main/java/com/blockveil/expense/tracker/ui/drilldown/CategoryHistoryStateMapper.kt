package com.blockveil.expense.tracker.ui.drilldown

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.ui.components.MonthGroup
import com.blockveil.expense.tracker.ui.components.TransactionRowUiModel
import com.blockveil.expense.tracker.ui.components.toRowUiModel
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.monthLabel
import java.time.YearMonth

data class CategoryHistorySources(
    val transactions: List<TransactionEntity>,
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val currency: CurrencyDisplay,
)

data class CategoryHistoryUiState(
    val isLoading: Boolean = true,
    val category: String = "",
    val isIncome: Boolean = false,
    val itemCount: Int = 0,
    val total: Double = 0.0,
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
    val groups: List<MonthGroup<TransactionRowUiModel>> = emptyList(),
)

/** Matches CategoryHistoryScreen's `items`/`groups` derivation exactly, grouped by real month. */
fun buildCategoryHistoryUiState(
    sources: CategoryHistorySources,
    category: String,
    isIncome: Boolean,
): CategoryHistoryUiState {
    val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
    val items = sources.transactions
        .filter { it.type == type && it.category == category }
        .sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })

    val total = items.sumOf { it.amount }

    val groups = items
        .groupBy { YearMonth.from(it.date) }
        .entries
        .sortedByDescending { it.key }
        .map { (yearMonth, list) ->
            MonthGroup(
                monthLabel = monthLabel(yearMonth),
                items = list.map { it.toRowUiModel(sources.accounts, sources.customExpenseCategories, sources.customIncomeCategories) },
            )
        }

    return CategoryHistoryUiState(
        isLoading = false,
        category = category,
        isIncome = isIncome,
        itemCount = items.size,
        total = total,
        currency = sources.currency,
        groups = groups,
    )
}
