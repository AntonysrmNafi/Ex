package com.blockveil.expense.tracker.ui.history

import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.util.CurrencyDisplay

data class HistoryUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val filterType: HistoryFilterType = HistoryFilterType.ALL,
    val itemCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
    val groups: List<HistoryGroup> = emptyList(),
)
