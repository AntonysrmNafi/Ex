package com.blockveil.expense.tracker.ui.history

import com.blockveil.expense.tracker.ui.components.TransactionRowUiModel
import com.blockveil.expense.tracker.ui.components.TransferRowUiModel

/** The 4 quick filters on History. Matches the "all"/"expense"/"income"/"transfer" buttons. */
enum class HistoryFilterType(val label: String) {
    ALL("All"),
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer"),
}

/** One row in the combined list: either a transaction or a transfer/repayment. */
sealed class HistoryEntry {
    data class TransactionItem(val model: TransactionRowUiModel) : HistoryEntry()
    data class TransferItem(val model: TransferRowUiModel) : HistoryEntry()
}

/** One month's worth of entries, already sorted newest first. */
data class HistoryGroup(
    val monthLabel: String,
    val entries: List<HistoryEntry>,
)
