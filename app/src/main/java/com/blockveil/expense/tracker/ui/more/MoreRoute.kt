package com.blockveil.expense.tracker.ui.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback
import com.blockveil.expense.tracker.ui.goals.GoalsViewModel

/**
 * Wires [MoreViewModel] (accounts/net worth/subscriptions) and [GoalsViewModel] (goals) to
 * [MoreScreen], owns the delete-confirm dialogs for both goals and subscriptions, and reports
 * every successful write to [LocalAppFeedback] (toast text + money-burst) matching the source
 * design's showToast/triggerMoneyAnim calls in handleAddAccount, requestDeleteGoal, etc.
 */
@Composable
fun MoreRoute(onAccountClick: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val moreViewModel: MoreViewModel = viewModel(factory = MoreViewModel.factory(app.container))
    val goalsViewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.factory(app.container))
    val feedback = LocalAppFeedback.current

    val uiState by moreViewModel.uiState.collectAsState()
    val goals by goalsViewModel.goals.collectAsState()

    var pendingDeleteGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var pendingDeleteSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }

    MoreScreen(
        netWorth = uiState.netWorth,
        accounts = uiState.accounts,
        currency = uiState.currency,
        onAccountClick = onAccountClick,
        onAddSavingsAccount = { name, type, balance ->
            moreViewModel.onAddSavingsAccount(name, type, balance)
            feedback.showToast("Account added")
            feedback.triggerMoneyBurst()
        },
        onAddLoanAccount = { name, loanAmount, settlementAmount, depositAccountId ->
            val error = moreViewModel.onAddLoanAccount(name, loanAmount, settlementAmount, depositAccountId)
            if (error == null) {
                feedback.showToast("Loan added")
                feedback.triggerMoneyBurst()
            }
            error
        },
        goals = goals,
        onAddGoal = { name, target, months ->
            goalsViewModel.onAddGoal(name, target, months)
            feedback.showToast("Goal added")
        },
        onContributeGoal = { goal, amount ->
            goalsViewModel.onContribute(goal, amount)
            feedback.triggerMoneyBurst()
        },
        onDeleteGoalRequest = { goal -> pendingDeleteGoal = goal },
        subscriptions = uiState.subscriptions,
        subTotal = uiState.subTotal,
        flagged = uiState.flagged,
        onAddSubscription = { name, amount, cycle, accountId, startDate, endDate ->
            val error = moreViewModel.onAddSubscription(name, amount, cycle, accountId, startDate, endDate)
            if (error == null) feedback.showToast("Subscription added")
            error
        },
        onToggleSubscriptionActive = moreViewModel::onToggleSubscriptionActive,
        onDeleteSubscriptionRequest = { subscription -> pendingDeleteSubscription = subscription },
    )

    val goalToDelete = pendingDeleteGoal
    if (goalToDelete != null) {
        ConfirmDialog(
            title = "Delete goal?",
            message = "Progress on \"${goalToDelete.name}\" will be lost.",
            onConfirm = {
                goalsViewModel.onDelete(goalToDelete)
                pendingDeleteGoal = null
                feedback.showToast("Goal deleted")
            },
            onCancel = { pendingDeleteGoal = null },
        )
    }

    val subscriptionToDelete = pendingDeleteSubscription
    if (subscriptionToDelete != null) {
        ConfirmDialog(
            title = "Delete subscription?",
            message = "\"${subscriptionToDelete.name}\" will be removed from your subscriptions.",
            onConfirm = {
                moreViewModel.onDeleteSubscription(subscriptionToDelete)
                pendingDeleteSubscription = null
                feedback.showToast("Subscription deleted")
            },
            onCancel = { pendingDeleteSubscription = null },
        )
    }
}
