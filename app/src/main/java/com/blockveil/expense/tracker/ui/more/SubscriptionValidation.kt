package com.blockveil.expense.tracker.ui.more

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.util.computeDueDates
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Matches handleAddSub's validation exactly, in order, including the insufficient-balance check against charges due immediately. */
fun validateAddSubscription(
    name: String,
    amountText: String,
    accountId: Long?,
    cycle: SubscriptionCycle,
    startDate: LocalDate?,
    endDate: LocalDate?,
    accounts: List<AccountEntity>,
    today: LocalDate = LocalDate.now(),
): String? {
    if (name.isBlank()) return "Enter a name"
    val amount = amountText.toDoubleOrNull()
    if (amount == null || amount <= 0) return "Enter a valid amount"
    if (accountId == null) return "Choose an account to deduct from"

    if (cycle.needsDateRange) {
        if (startDate == null || endDate == null) return "Choose a start and end date"
        val days = ChronoUnit.DAYS.between(startDate, endDate)
        val minDays = cycle.minDurationDays
        if (days < minDays) return "End date must be at least $minDays days after the start date"
    }

    val due = computeDueDates(cycle = cycle, startDate = startDate, endDate = endDate, billedDates = emptyList(), today = today)
    val account = accounts.firstOrNull { it.id == accountId }
    val totalDue = due.size * amount
    if (account != null && totalDue > (account.balance ?: 0.0)) {
        val chargeWord = "charge" + if (due.size == 1) "" else "s"
        return "Insufficient balance in ${account.name} for ${due.size} $chargeWord"
    }
    return null
}
