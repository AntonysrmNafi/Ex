package com.blockveil.expense.tracker.ui.components

/**
 * One month's worth of items, already sorted newest first. Generic so both Category
 * History (transactions only) and Account History (mixed transactions+transfers) can
 * share the same grouping shape.
 */
data class MonthGroup<T>(
    val monthLabel: String,
    val items: List<T>,
)
