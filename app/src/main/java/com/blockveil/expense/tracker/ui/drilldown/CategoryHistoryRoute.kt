package com.blockveil.expense.tracker.ui.drilldown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.util.categoryColor

@Composable
fun CategoryHistoryRoute(
    category: String,
    isIncome: Boolean,
    onBack: () -> Unit,
    onTxnClick: (Long) -> Unit,
) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: CategoryHistoryViewModel = viewModel(
        key = "category_history_${category}_$isIncome",
        factory = CategoryHistoryViewModel.factory(app.container, category, isIncome),
    )
    val uiState by viewModel.uiState.collectAsState()

    CategoryHistoryScreen(
        category = uiState.category,
        isIncome = uiState.isIncome,
        itemCount = uiState.itemCount,
        total = uiState.total,
        currency = uiState.currency,
        badgeColor = categoryColor(isIncome, category),
        groups = uiState.groups,
        onBack = onBack,
        onTxnClick = onTxnClick,
    )
}
