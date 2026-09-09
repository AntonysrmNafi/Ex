package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.SavingsType
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.ui.components.TopBar
import com.blockveil.expense.tracker.ui.goals.GoalsSection
import com.blockveil.expense.tracker.util.CurrencyDisplay
import java.time.LocalDate

/**
 * The More tab, complete: net worth, accounts, add an account (savings or loan), Goals
 * (Bag 14's [GoalsSection]), and Subscriptions. Matches MoreScreen in full.
 */
@Composable
fun MoreScreen(
    netWorth: Double,
    accounts: List<AccountEntity>,
    currency: CurrencyDisplay,
    onAccountClick: (Long) -> Unit,
    onAddSavingsAccount: (name: String, type: SavingsType, balance: Double) -> Unit,
    onAddLoanAccount: (name: String, loanAmount: String, settlementAmount: String, depositAccountId: Long?) -> String?,
    goals: List<GoalEntity>,
    onAddGoal: (name: String, target: Double, months: Int) -> Unit,
    onContributeGoal: (GoalEntity, Double) -> Unit,
    onDeleteGoalRequest: (GoalEntity) -> Unit,
    subscriptions: List<SubscriptionEntity>,
    subTotal: Double,
    flagged: List<FlaggedSubscription>,
    onAddSubscription: (name: String, amount: String, cycle: SubscriptionCycle, accountId: Long?, startDate: LocalDate?, endDate: LocalDate?) -> String?,
    onToggleSubscriptionActive: (SubscriptionEntity, Boolean) -> Unit,
    onDeleteSubscriptionRequest: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val savingsAccounts = accounts.filter { it.category == AccountCategory.SAVINGS }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(title = "More")

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { NetWorthCard(netWorth = netWorth, currency = currency) }

            if (accounts.isNotEmpty()) {
                items(accounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        currency = currency,
                        onClick = { onAccountClick(account.id) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            } else {
                item {
                    Text(
                        text = "No accounts yet, add one below.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                }
            }

            item {
                AddAccountCard(
                    savingsAccounts = savingsAccounts,
                    onAddSavingsAccount = onAddSavingsAccount,
                    onAddLoanAccount = onAddLoanAccount,
                )
            }

            item {
                GoalsSection(
                    goals = goals,
                    currency = currency,
                    onAddGoal = onAddGoal,
                    onContribute = onContributeGoal,
                    onDeleteRequest = onDeleteGoalRequest,
                )
            }

            item {
                SubscriptionsSection(
                    subscriptions = subscriptions,
                    accounts = accounts,
                    savingsAccounts = savingsAccounts,
                    subTotal = subTotal,
                    flagged = flagged,
                    currency = currency,
                    onAddSubscription = onAddSubscription,
                    onToggleActive = onToggleSubscriptionActive,
                    onDeleteRequest = onDeleteSubscriptionRequest,
                )
            }
        }
    }
}
