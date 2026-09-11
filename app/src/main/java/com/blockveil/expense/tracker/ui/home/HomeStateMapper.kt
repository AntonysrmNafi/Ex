package com.blockveil.expense.tracker.ui.home

import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.SavingsType
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.ui.components.toRowUiModel
import com.blockveil.expense.tracker.util.computeEffectiveBudget
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import java.time.LocalDate
import java.time.YearMonth

/** Everything the mapper needs from the repositories, bundled so ViewModel combine() stays simple. */
data class HomeSources(
    val transactions: List<TransactionEntity>,
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
)

private const val RECENT_LIMIT = 5

/**
 * Builds [HomeUiState] from raw data. Matches the source design's homeTransactions /
 * homeExpenseTotal / homeIncomeTotal / dateFilterLabel / effectiveBudget derivations.
 */
fun buildHomeUiState(
    sources: HomeSources,
    settings: AppSettings,
    filter: DateFilter,
    currentMonth: YearMonth,
    today: LocalDate = LocalDate.now(),
): HomeUiState {
    val range = filter.dateRangeOrNull(currentMonth, today)
    val filtered = sources.transactions
        .filter { range == null || (it.date >= range.first && it.date <= range.second) }
        // Repository queries already order by date desc, but re-sorting here keeps this
        // function correct on its own, independent of the caller's query order.
        .sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })

    val expenseTotal = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val incomeTotal = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val isMonthFilter = filter == DateFilter.Month
    val showPeriodArrows = filter !is DateFilter.All
    val effectiveBudget = computeEffectiveBudget(settings.budget, settings.rollingEnabled, currentMonth, sources.transactions)

    val accountUiModels = sources.accounts.map { account -> account.toStripUiModel() }

    val recent = filtered.take(RECENT_LIMIT).map { txn ->
        txn.toRowUiModel(sources.accounts, sources.customExpenseCategories, sources.customIncomeCategories)
    }

    return HomeUiState(
        isLoading = false,
        filterLabel = filter.label(currentMonth),
        showPeriodArrows = showPeriodArrows,
        expenseTotal = expenseTotal,
        incomeTotal = incomeTotal,
        budget = effectiveBudget,
        showBudget = isMonthFilter,
        currency = resolveCurrencyDisplay(settings),
        accounts = accountUiModels,
        hasSavingsAccount = sources.accounts.any { it.category == AccountCategory.SAVINGS },
        recentTransactions = recent,
        totalTransactionCount = filtered.size,
    )
}

private fun AccountEntity.toStripUiModel(): AccountStripUiModel {
    val isLoan = category == AccountCategory.LOAN
    val displayAmount: Double
    val subLabel: String
    if (isLoan) {
        displayAmount = (principal ?: 0.0) - (repaid ?: 0.0)
        subLabel = if (active) "Loan remaining" else "Loan settled"
    } else {
        displayAmount = balance ?: 0.0
        // Raw enum-name formatting, e.g. SavingsType.MOBILE_WALLET -> "MOBILE WALLET",
        // matches account.type.replace("_", " ") in the source design exactly (no title-casing).
        subLabel = (type ?: SavingsType.CASH).name.replace("_", " ")
    }
    return AccountStripUiModel(id = id, name = name, isLoan = isLoan, displayAmount = displayAmount, subLabel = subLabel)
}
