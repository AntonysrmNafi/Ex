package com.blockveil.expense.tracker.ui.history

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.ui.components.toRowUiModel
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.accountName
import com.blockveil.expense.tracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth

/** Everything the mapper needs from the repositories. */
data class HistorySources(
    val transactions: List<TransactionEntity>,
    val transfers: List<TransferEntity>,
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val currency: CurrencyDisplay,
)

private enum class FilterKind { EXPENSE, INCOME, TRANSFER }

private data class DatedEntry(
    val date: LocalDate,
    val secondaryKey: Long,
    val entry: HistoryEntry,
    val kind: FilterKind,
    val searchText: String,
)

/**
 * Builds [HistoryUiState] from raw transactions and transfers. Matches HistoryScreen's
 * `combined` / `filtered` / `groups` derivations, including the search fields per item type
 * (category+note for transactions, note+from-name+to-name for transfers).
 *
 * Note: sorting ties within the same date fall back to each item's own table id. The source
 * design can safely tie-break with a single shared `nextId` counter across transactions and
 * transfers; Room gives each table its own independent id sequence, so this is a reasonable
 * best-effort ordering for same-day items rather than a byte-for-byte guarantee.
 */
fun buildHistoryUiState(sources: HistorySources, query: String, filterType: HistoryFilterType): HistoryUiState {
    val q = query.trim().lowercase()

    val transactionEntries = sources.transactions.map { it.toDatedEntry(sources) }
    val transferEntries = sources.transfers.map { it.toDatedEntry(sources) }

    val filtered = (transactionEntries + transferEntries)
        .filter { dated ->
            when (filterType) {
                HistoryFilterType.ALL -> true
                HistoryFilterType.TRANSFER -> dated.kind == FilterKind.TRANSFER
                HistoryFilterType.EXPENSE -> dated.kind == FilterKind.EXPENSE
                HistoryFilterType.INCOME -> dated.kind == FilterKind.INCOME
            }
        }
        .filter { q.isEmpty() || it.searchText.contains(q) }
        .sortedWith(compareByDescending<DatedEntry> { it.date }.thenByDescending { it.secondaryKey })

    val totalExpense = filtered
        .filter { it.kind == FilterKind.EXPENSE }
        .sumOf { (it.entry as HistoryEntry.TransactionItem).model.amount }
    val totalIncome = filtered
        .filter { it.kind == FilterKind.INCOME }
        .sumOf { (it.entry as HistoryEntry.TransactionItem).model.amount }

    val groups = filtered
        .groupBy { YearMonth.from(it.date) }
        .entries
        .sortedByDescending { it.key }
        .map { (yearMonth, items) -> HistoryGroup(monthLabel = monthLabel(yearMonth), entries = items.map { it.entry }) }

    return HistoryUiState(
        isLoading = false,
        query = query,
        filterType = filterType,
        itemCount = filtered.size,
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        currency = sources.currency,
        groups = groups,
    )
}

private fun TransactionEntity.toDatedEntry(sources: HistorySources): DatedEntry {
    val isIncome = type == TransactionType.INCOME
    val model = toRowUiModel(sources.accounts, sources.customExpenseCategories, sources.customIncomeCategories)
    return DatedEntry(
        date = date,
        secondaryKey = id,
        entry = HistoryEntry.TransactionItem(model),
        kind = if (isIncome) FilterKind.INCOME else FilterKind.EXPENSE,
        searchText = "$category $note".lowercase(),
    )
}

private fun TransferEntity.toDatedEntry(sources: HistorySources): DatedEntry {
    val model = toRowUiModel(sources.accounts)
    val fromName = accountName(sources.accounts, fromId)
    val toName = accountName(sources.accounts, toId)
    return DatedEntry(
        date = date,
        secondaryKey = id,
        entry = HistoryEntry.TransferItem(model),
        kind = FilterKind.TRANSFER,
        searchText = "$note $fromName $toName".lowercase(),
    )
}
