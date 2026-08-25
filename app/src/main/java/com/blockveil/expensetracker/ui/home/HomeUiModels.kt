package com.blockveil.expensetracker.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Everything [com.blockveil.expensetracker.ui.components.TransactionRow] needs for one row. */
data class TransactionRowUiModel(
    val id: Long,
    val category: String,
    val isIncome: Boolean,
    val icon: ImageVector,
    val iconTint: Color,
    val note: String,
    val metaLine: String,
    val amount: Double,
    val hasReceipt: Boolean,
)

/** Everything [com.blockveil.expensetracker.ui.components.AccountStripCard] needs for one chip. */
data class AccountStripUiModel(
    val id: Long,
    val name: String,
    val isLoan: Boolean,
    val displayAmount: Double,
    val subLabel: String,
)
