package com.blockveil.expense.tracker.ui.home

import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.ui.components.TransactionRowUiModel
import com.blockveil.expense.tracker.util.CurrencyDisplay

data class HomeUiState(
    val isLoading: Boolean = true,
    val filterLabel: String = "",
    val showPeriodArrows: Boolean = true,
    val expenseTotal: Double = 0.0,
    val incomeTotal: Double = 0.0,
    val budget: Double = 0.0,
    val showBudget: Boolean = false,
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
    val accounts: List<AccountStripUiModel> = emptyList(),
    val hasSavingsAccount: Boolean = false,
    val recentTransactions: List<TransactionRowUiModel> = emptyList(),
    val totalTransactionCount: Int = 0,
)
