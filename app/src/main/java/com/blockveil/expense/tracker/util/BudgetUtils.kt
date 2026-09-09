package com.blockveil.expense.tracker.util

import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.TransactionType
import java.time.YearMonth

/**
 * The budget actually in effect for [currentMonth], after rolling carry-over from last
 * month if enabled. Matches `effectiveBudget` in the source design exactly. Shared by Home
 * (which displays it) and Analytics (whose forecast card compares against it), so both
 * screens always agree on the same number.
 */
fun computeEffectiveBudget(
    rawBudget: Double,
    rollingEnabled: Boolean,
    currentMonth: YearMonth,
    allTransactions: List<TransactionEntity>,
): Double {
    if (rawBudget <= 0) return 0.0
    val prevMonth = currentMonth.minusMonths(1)
    val prevMonthExpense = allTransactions
        .filter { it.type == TransactionType.EXPENSE && YearMonth.from(it.date) == prevMonth }
        .sumOf { it.amount }
    val carry = if (rollingEnabled) rawBudget - prevMonthExpense else 0.0
    return maxOf(rawBudget + carry, 0.0)
}
