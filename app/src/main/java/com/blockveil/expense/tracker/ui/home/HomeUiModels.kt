package com.blockveil.expense.tracker.ui.home

/** Everything [com.blockveil.expense.tracker.ui.components.AccountStripCard] needs for one chip. */
data class AccountStripUiModel(
    val id: Long,
    val name: String,
    val isLoan: Boolean,
    val displayAmount: Double,
    val subLabel: String,
)
