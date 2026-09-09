package com.blockveil.expense.tracker.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

/**
 * The single "which month am I looking at" cursor shared across the app. Matches the
 * source design's one top-level `monthOffset` state: navigating months on Home also moves
 * Analytics to the same month, and vice versa, since they're driven by the same source of
 * truth rather than each screen keeping its own independent month.
 */
class SharedMonthState {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    fun prevMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }
}
