package com.blockveil.expensetracker.util

import com.blockveil.expensetracker.data.model.SubscriptionCycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Note on "today": the source web preview freezes the clock at a fixed demo date
 * (TODAY_FIXED = 15 Jul 2026) so its seed data always looks current. A real app has no such
 * luxury, every function below that needs "today" defaults to [LocalDate.now], the device's
 * actual current date, and month-grouping is computed from each record's real [LocalDate]
 * rather than a stored "monthOffset" like the preview used.
 */

private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

/** e.g. "14 Jul 2026". Matches formatDateDisplay in the source design. */
fun formatDateDisplay(date: LocalDate): String = date.format(DISPLAY_DATE_FORMATTER)

/** Advances a date by one billing cycle. Null for ONE_TIME, which has no natural "next" occurrence. */
fun addCycle(date: LocalDate, cycle: SubscriptionCycle): LocalDate? = when (cycle) {
    SubscriptionCycle.DAY -> date.plusDays(1)
    SubscriptionCycle.THREE_DAY -> date.plusDays(3)
    SubscriptionCycle.WEEKLY -> date.plusDays(7)
    SubscriptionCycle.MONTHLY -> date.plusMonths(1)
    SubscriptionCycle.YEARLY -> date.plusYears(1)
    SubscriptionCycle.ONE_TIME -> null
}

/**
 * Every occurrence date for a subscription that's due (on/before [today], and on/before its
 * own end date if it has one) but hasn't been billed yet. Used both to preview how many
 * charges a new subscription would immediately create, and to actually create them.
 * Matches computeDueDates exactly, including its 500-iteration safety guard.
 */
fun computeDueDates(
    cycle: SubscriptionCycle,
    startDate: LocalDate?,
    endDate: LocalDate?,
    billedDates: List<LocalDate>,
    today: LocalDate = LocalDate.now(),
): List<LocalDate> {
    val already = billedDates.toHashSet()

    if (cycle == SubscriptionCycle.ONE_TIME || cycle == SubscriptionCycle.DAY) {
        return if (today in already) emptyList() else listOf(today)
    }
    if (startDate == null) return emptyList()

    val cutoff = if (endDate != null && endDate.isBefore(today)) endDate else today
    val due = mutableListOf<LocalDate>()
    var cursor = startDate
    var guard = 0
    while (!cursor.isAfter(cutoff) && guard < 500) {
        if (cursor !in already) due += cursor
        cursor = addCycle(cursor, cycle) ?: break
        guard++
    }
    return due
}

/** What one occurrence of this subscription is "worth" per month on average. */
fun monthlyEquivalent(cycle: SubscriptionCycle, amount: Double): Double = when (cycle) {
    SubscriptionCycle.DAY -> amount * 30
    SubscriptionCycle.THREE_DAY -> amount * 10
    SubscriptionCycle.WEEKLY -> amount * 4.33
    SubscriptionCycle.MONTHLY -> amount
    SubscriptionCycle.YEARLY -> amount / 12
    SubscriptionCycle.ONE_TIME -> 0.0
}
