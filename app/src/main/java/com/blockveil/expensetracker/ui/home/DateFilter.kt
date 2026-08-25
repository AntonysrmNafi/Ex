package com.blockveil.expensetracker.ui.home

import com.blockveil.expensetracker.util.formatDateDisplay
import com.blockveil.expensetracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Which slice of history Home is currently showing. [Month] doesn't carry its own month,
 * the currently navigated month lives separately in [HomeViewModel] (mirrors the source
 * design's dateFilter.type and monthOffset being two independent pieces of state, so
 * switching to "This Week" and back to "This Month" restores whatever month you'd
 * navigated to, instead of resetting to today's month).
 */
sealed class DateFilter {
    data object Month : DateFilter()
    data object Today : DateFilter()
    data object Week : DateFilter()
    data object Year : DateFilter()
    data object All : DateFilter()
    data class Custom(val from: LocalDate, val to: LocalDate) : DateFilter()
}

/** The text shown next to the calendar icon on Home. Matches dateFilterLabel. */
fun DateFilter.label(currentMonth: YearMonth): String = when (this) {
    DateFilter.Month -> monthLabel(currentMonth)
    DateFilter.Today -> "Today"
    DateFilter.Week -> "This Week"
    DateFilter.Year -> "This Year"
    DateFilter.All -> "All time"
    is DateFilter.Custom -> "${formatDateDisplay(from)} - ${formatDateDisplay(to)}"
}

/**
 * The inclusive [from, to] range this filter covers, or null for [DateFilter.All] (no
 * bound, matches every transaction). Matches homeTransactions' range derivation, minus the
 * JS Date millisecond-boundary workaround, unnecessary here since [LocalDate] has no time
 * component to zero out.
 */
fun DateFilter.dateRangeOrNull(currentMonth: YearMonth, today: LocalDate): Pair<LocalDate, LocalDate>? = when (this) {
    DateFilter.Month -> currentMonth.atDay(1) to currentMonth.atEndOfMonth()
    DateFilter.Today -> today to today
    DateFilter.Week -> {
        // Sunday-start week, matches JS Date.getDay() (Sunday = 0) used in the source design.
        val start = today.minusDays(today.dayOfWeek.value % 7L)
        start to start.plusDays(6)
    }
    DateFilter.Year -> LocalDate.of(today.year, 1, 1) to LocalDate.of(today.year, 12, 31)
    DateFilter.All -> null
    is DateFilter.Custom -> from to to
}
