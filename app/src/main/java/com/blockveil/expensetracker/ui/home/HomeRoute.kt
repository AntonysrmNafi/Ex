package com.blockveil.expensetracker.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expensetracker.ExpenseTrackerApp

/**
 * Connects [HomeViewModel] to [HomeScreen] and owns the DateFilterSheet's visibility.
 * This is the composable [com.blockveil.expensetracker.MainActivity] calls for the Home
 * tab; HomeScreen itself stays free of any ViewModel/database awareness.
 */
@Composable
fun HomeRoute(
    onTxnClick: (Long) -> Unit,
    onAccountClick: (Long) -> Unit,
    onSeeAllHistory: () -> Unit,
    onAdd: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(app.container))

    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.dateFilter.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    var showDateFilterSheet by remember { mutableStateOf(false) }

    HomeScreen(
        filterLabel = uiState.filterLabel,
        isMonthFilter = uiState.isMonthFilter,
        onPrevMonth = viewModel::onPrevMonth,
        onNextMonth = viewModel::onNextMonth,
        onOpenDateFilter = { showDateFilterSheet = true },
        expenseTotal = uiState.expenseTotal,
        incomeTotal = uiState.incomeTotal,
        budget = uiState.budget,
        showBudget = uiState.showBudget,
        currency = uiState.currency,
        accounts = uiState.accounts,
        hasSavingsAccount = uiState.hasSavingsAccount,
        recentTransactions = uiState.recentTransactions,
        totalTransactionCount = uiState.totalTransactionCount,
        onTxnClick = onTxnClick,
        onAccountClick = onAccountClick,
        onSeeAllHistory = onSeeAllHistory,
        onAdd = onAdd,
    )

    if (showDateFilterSheet) {
        DateFilterSheet(
            current = currentFilter,
            currentMonth = currentMonth,
            onApply = viewModel::onApplyDateFilter,
            onDismiss = { showDateFilterSheet = false },
        )
    }
}
