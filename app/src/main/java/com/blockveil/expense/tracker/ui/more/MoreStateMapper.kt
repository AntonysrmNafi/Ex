package com.blockveil.expense.tracker.ui.more

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.monthlyEquivalent

data class MoreUiState(
    val isLoading: Boolean = true,
    val netWorth: Double = 0.0,
    val accounts: List<AccountEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val subTotal: Double = 0.0,
    val flagged: List<FlaggedSubscription> = emptyList(),
    val currency: CurrencyDisplay = CurrencyDisplay(symbol = "৳", position = CurrencyPosition.PREFIX, format = CurrencyFormat.GROUPED),
)

private const val FLAG_RATIO_THRESHOLD = 2.0

/**
 * Net worth = every savings account's balance, minus every loan's remaining balance.
 * subTotal = sum of each active subscription's monthly equivalent. flagged = active
 * subscriptions costing at least [FLAG_RATIO_THRESHOLD]x the average active subscription.
 * Matches the netWorth/subTotal/flagged useMemo blocks in the source design exactly.
 */
fun buildMoreUiState(
    accounts: List<AccountEntity>,
    subscriptions: List<SubscriptionEntity>,
    currency: CurrencyDisplay,
): MoreUiState {
    val netWorth = accounts.sumOf { account ->
        if (account.category == AccountCategory.LOAN) {
            -((account.principal ?: 0.0) - (account.repaid ?: 0.0))
        } else {
            account.balance ?: 0.0
        }
    }

    val activeMonthlyAmounts = subscriptions
        .filter { it.active }
        .map { it to monthlyEquivalent(it.cycle, it.amount) }

    val subTotal = activeMonthlyAmounts.sumOf { it.second }
    val average = if (activeMonthlyAmounts.isNotEmpty()) subTotal / activeMonthlyAmounts.size else 0.0

    val flagged = if (average > 0) {
        activeMonthlyAmounts
            .filter { it.second >= average * FLAG_RATIO_THRESHOLD }
            .map { (subscription, monthly) -> FlaggedSubscription(subscription.name, monthly / average) }
    } else {
        emptyList()
    }

    return MoreUiState(
        isLoading = false,
        netWorth = netWorth,
        accounts = accounts,
        subscriptions = subscriptions,
        subTotal = subTotal,
        flagged = flagged,
        currency = currency,
    )
}
