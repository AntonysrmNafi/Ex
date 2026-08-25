package com.blockveil.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

// Lucide -> Material icon mapping, one per fixed expense category (CATEGORY_META).
private val EXPENSE_ICONS: Map<String, ImageVector> = mapOf(
    "Food" to Icons.Filled.Restaurant,
    "Transport" to Icons.Filled.DirectionsCar,
    "Shopping" to Icons.Filled.ShoppingBag,
    "Bills" to Icons.Filled.Receipt,
    "Entertainment" to Icons.Filled.Movie,
    "Health" to Icons.Filled.MonitorHeart,
    "Other" to Icons.Filled.MoreHoriz,
)

// Lucide -> Material icon mapping, one per fixed income category (INCOME_CATEGORY_META).
private val INCOME_ICONS: Map<String, ImageVector> = mapOf(
    "Salary" to Icons.Filled.AccountBalance,
    "Freelance" to Icons.Filled.Work,
    "Business" to Icons.Filled.Business,
    "Investment" to Icons.Filled.TrendingUp,
    "Gift" to Icons.Filled.CardGiftcard,
    "Refund" to Icons.Filled.Undo,
    "Other" to Icons.Filled.MoreHoriz,
)

/**
 * Icon for a category name, fixed or custom. Custom categories fall back to a tag/sell icon,
 * matching getCategoryMeta's `Icon: Tag` fallback for anything outside the fixed lists.
 */
fun categoryIcon(isIncome: Boolean, category: String): ImageVector =
    (if (isIncome) INCOME_ICONS[category] else EXPENSE_ICONS[category]) ?: Icons.Filled.Sell
