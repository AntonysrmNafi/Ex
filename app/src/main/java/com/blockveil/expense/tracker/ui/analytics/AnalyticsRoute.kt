package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback

@Composable
fun AnalyticsRoute() {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.factory(app.container))
    val uiState by viewModel.uiState.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val feedback = LocalAppFeedback.current

    AnalyticsScreen(
        currentMonth = currentMonth,
        onPrevMonth = viewModel::onPrevMonth,
        onNextMonth = viewModel::onNextMonth,
        incomeTotals = uiState.incomeTotals,
        expenseTotals = uiState.expenseTotals,
        customExpenseCategories = uiState.customExpenseCategories,
        customIncomeCategories = uiState.customIncomeCategories,
        currency = uiState.currency,
        forecast = uiState.forecast,
        effectiveBudget = uiState.effectiveBudget,
        anomalies = uiState.anomalies,
        budgetCategories = uiState.budgetCategories,
        rawBudget = uiState.rawBudget,
        rollingEnabled = uiState.rollingEnabled,
        onSetBudget = { amount ->
            viewModel.onSetBudget(amount)
            feedback.showToast("Budget updated")
        },
        onSetRolling = viewModel::onSetRolling,
        allExpenseCategories = uiState.allExpenseCategories,
        categoryBudgetMap = uiState.categoryBudgetMap,
        categorySpendMap = uiState.categorySpendMap,
        onSetCategoryLimit = viewModel::onSetCategoryLimit,
    )
}
