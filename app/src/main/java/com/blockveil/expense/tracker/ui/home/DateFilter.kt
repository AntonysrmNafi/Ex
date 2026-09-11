package com.blockveil.expense.tracker.ui.home

import com.blockveil.expense.tracker.util.formatDateDisplay
import com.blockveil.expense.tracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Which slice of history Home is currently showing. [Month] doesn't carry its own month,
 * the currently navigated month lives separately in [HomeViewModel] (mirrors the source
 * design's dateFilter.type and monthOffset being two independent pieces of state, so
 * switching to "This Week" and back to "This Month" restores whatever month you'd
 * navigated to, instead of resetting to today's month).
 *
 * [Today], [Week], and [Year] each carry their own offset (in days/weeks/years from the real
 * "today") for the same reason and by the same mechanism as [Month]'s separate month state:
 * the prev/next arrows page through history for every filter type, not just Month. [All] has
 * no offset since there's no previous or next "all time" to page to.
 */
sealed class DateFilter {
    data object Month : DateFilter()
    data class Today(val dayOffset: Long = 0) : DateFilter()
    data class Week(val weekOffset: Long = 0) : DateFilter()
    data class Year(val yearOffset: Long = 0) : DateFilter()
    data object All : DateFilter()
    data class Custom(val from: LocalDate, val to: LocalDate) : DateFilter()
}

/** The text shown next to the calendar icon on Home. Matches dateFilterLabel. */
fun DateFilter.label(currentMonth: YearMonth): String = when (this) {
    DateFilter.Month -> monthLabel(currentMonth)
    is DateFilter.Today -> if (dayOffset == 0L) "Today" else formatDateDisplay(LocalDate.now().plusDays(dayOffset))
    is DateFilter.Week -> {
        val start = weekStart(LocalDate.now().plusWeeks(weekOffset))
        "${formatDateDisplay(start)} - ${formatDateDisplay(start.plusDays(6))}"
    }
    is DateFilter.Year -> (LocalDate.now().year + yearOffset).toString()
    DateFilter.All -> "All time"
    is DateFilter.Custom -> "${formatDateDisplay(from)} - ${formatDateDisplay(to)}"
}

/** Sunday-start week, matches JS Date.getDay() (Sunday = 0) used in the source design. */
private fun weekStart(anchor: LocalDate): LocalDate = anchor.minusDays(anchor.dayOfWeek.value % 7L)

/**
 * The inclusive [from, to] range this filter covers, or null for [DateFilter.All] (no
 * bound, matches every transaction). Matches homeTransactions' range derivation, minus the
 * JS Date millisecond-boundary workaround, unnecessary here since [LocalDate] has no time
 * component to zero out.
 */
fun DateFilter.dateRangeOrNull(currentMonth: YearMonth, today: LocalDate): Pair<LocalDate, LocalDate>? = when (this) {
    DateFilter.Month -> currentMonth.atDay(1) to currentMonth.atEndOfMonth()
    is DateFilter.Today -> today.plusDays(dayOffset).let { it to it }
    is DateFilter.Week -> weekStart(today.plusWeeks(weekOffset)).let { it to it.plusDays(6) }
    is DateFilter.Year -> (today.year + yearOffset).let { LocalDate.of(it, 1, 1) to LocalDate.of(it, 12, 31) }
    DateFilter.All -> null
    is DateFilter.Custom -> from to to
}

/**
 * The filter shifted one step earlier/later: a day for [Today], a week for [Week], a year
 * for [Year], and [Custom]'s own span for [Custom] (a 7-day custom range pages by 7 days).
 * [Month] and [All] aren't handled here; Month keeps using [HomeViewModel]'s shared month
 * state, and the prev/next arrows are hidden entirely for All.
 */
fun DateFilter.stepped(forward: Boolean): DateFilter {
    val sign = if (forward) 1L else -1L
    return when (this) {
        is DateFilter.Today -> copy(dayOffset = dayOffset + sign)
        is DateFilter.Week -> copy(weekOffset = weekOffset + sign)
        is DateFilter.Year -> copy(yearOffset = yearOffset + sign)
        is DateFilter.Custom -> {
            val spanDays = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1
            val shift = spanDays * sign
            Custom(from = from.plusDays(shift), to = to.plusDays(shift))
        }
        DateFilter.Month, DateFilter.All -> this
    }
}
