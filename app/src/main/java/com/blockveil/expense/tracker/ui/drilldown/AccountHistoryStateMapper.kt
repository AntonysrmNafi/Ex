package com.blockveil.expense.tracker.ui.drilldown

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.ui.components.MonthGroup
import com.blockveil.expense.tracker.ui.components.toRowUiModel
import com.blockveil.expense.tracker.ui.history.HistoryEntry
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney
import com.blockveil.expense.tracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth

data class AccountHistorySources(
    val transactions: List<TransactionEntity>,
    val transfers: List<TransferEntity>,
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val currency: CurrencyDisplay,
)

data class AccountHistoryUiState(
    val isLoading: Boolean = true,
    val accountName: String = "",
    val isLoan: Boolean = false,
    val subtitle: String = "",
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
    val groups: List<MonthGroup<HistoryEntry>> = emptyList(),
)

private data class DatedEntry(val date: LocalDate, val secondaryKey: Long, val entry: HistoryEntry)

/**
 * Matches AccountHistoryScreen exactly: every transaction charged to this account plus
 * every transfer/repayment/disbursement where it's either side, combined and grouped by
 * real month (see the HistoryStateMapper note on why same-day tie-breaks are best-effort
 * rather than byte-for-byte, given Room's per-table id sequences vs the source design's
 * single shared counter).
 */
fun buildAccountHistoryUiState(sources: AccountHistorySources, accountId: Long): AccountHistoryUiState {
    val account = sources.accounts.firstOrNull { it.id == accountId } ?: return AccountHistoryUiState(isLoading = false)
    val isLoan = account.category == AccountCategory.LOAN

    val txnEntries = sources.transactions
        .filter { it.accountId == accountId }
        .map { txn ->
            val model = txn.toRowUiModel(sources.accounts, sources.customExpenseCategories, sources.customIncomeCategories)
            DatedEntry(txn.date, txn.id, HistoryEntry.TransactionItem(model))
        }

    val transferEntries = sources.transfers
        .filter { it.fromId == accountId || it.toId == accountId }
        .map { transfer ->
            val model = transfer.toRowUiModel(sources.accounts)
            DatedEntry(transfer.date, transfer.id, HistoryEntry.TransferItem(model))
        }

    val combined = (txnEntries + transferEntries)
        .sortedWith(compareByDescending<DatedEntry> { it.date }.thenByDescending { it.secondaryKey })

    val groups = combined
        .groupBy { YearMonth.from(it.date) }
        .entries
        .sortedByDescending { it.key }
        .map { (yearMonth, items) -> MonthGroup(monthLabel = monthLabel(yearMonth), items = items.map { it.entry }) }

    val itemWord = "item" + if (combined.size != 1) "s" else ""
    val subtitle = if (isLoan) {
        val principal = account.principal ?: 0.0
        val remaining = principal - (account.repaid ?: 0.0)
        "Settlement ${formatMoney(principal, sources.currency)} · ${formatMoney(remaining, sources.currency)} left · ${combined.size} $itemWord"
    } else {
        "Balance ${formatMoney(account.balance ?: 0.0, sources.currency)} · ${combined.size} $itemWord"
    }

    return AccountHistoryUiState(
        isLoading = false,
        accountName = account.name,
        isLoan = isLoan,
        subtitle = subtitle,
        currency = sources.currency,
        groups = groups,
    )
}
