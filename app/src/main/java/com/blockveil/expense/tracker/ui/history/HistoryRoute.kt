package com.blockveil.expense.tracker.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp

@Composable
fun HistoryRoute(onTxnClick: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(app.container))
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreen(
        query = uiState.query,
        onQueryChange = viewModel::onQueryChange,
        filterType = uiState.filterType,
        onFilterChange = viewModel::onFilterChange,
        itemCount = uiState.itemCount,
        totalIncome = uiState.totalIncome,
        totalExpense = uiState.totalExpense,
        currency = uiState.currency,
        groups = uiState.groups,
        onTxnClick = onTxnClick,
    )
}
