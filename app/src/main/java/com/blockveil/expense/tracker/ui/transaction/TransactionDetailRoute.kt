package com.blockveil.expense.tracker.ui.transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp

@Composable
fun TransactionDetailRoute(
    transactionId: Long,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: TransactionDetailViewModel = viewModel(
        key = "transaction_detail_$transactionId",
        factory = TransactionDetailViewModel.factory(app.container, transactionId),
    )
    val state by viewModel.uiState.collectAsState()

    TransactionDetailScreen(
        state = state,
        onEdit = { onEdit(transactionId) },
        onBack = onBack,
    )
}
