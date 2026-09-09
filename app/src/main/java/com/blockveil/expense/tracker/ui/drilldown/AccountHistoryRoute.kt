package com.blockveil.expense.tracker.ui.drilldown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp

@Composable
fun AccountHistoryRoute(
    accountId: Long,
    onBack: () -> Unit,
    onTxnClick: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: AccountHistoryViewModel = viewModel(
        key = "account_history_$accountId",
        factory = AccountHistoryViewModel.factory(app.container, accountId),
    )
    val uiState by viewModel.uiState.collectAsState()

    AccountHistoryScreen(
        accountName = uiState.accountName,
        subtitle = uiState.subtitle,
        currency = uiState.currency,
        groups = uiState.groups,
        onBack = onBack,
        onTxnClick = onTxnClick,
    )
}
