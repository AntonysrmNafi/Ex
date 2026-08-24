package com.blockveil.expensetracker.util

import androidx.compose.ui.graphics.Color
import com.blockveil.expensetracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expensetracker.ui.theme.CategoryColors
import com.blockveil.expensetracker.ui.theme.IncomeCategoryColors

/** Fixed expense categories, same order as CATEGORY_META in the source design. */
val EXPENSE_CATEGORIES: List<String> = listOf(
    "Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other",
)

/** Fixed income categories, same order as INCOME_CATEGORY_META in the source design. */
val INCOME_CATEGORIES: List<String> = listOf(
    "Salary", "Freelance", "Business", "Investment", "Gift", "Refund", "Other",
)

private val FIXED_EXPENSE_COLORS: Map<String, Color> = mapOf(
    "Food" to CategoryColors.Food,
    "Transport" to CategoryColors.Transport,
    "Shopping" to CategoryColors.Shopping,
    "Bills" to CategoryColors.Bills,
    "Entertainment" to CategoryColors.Entertainment,
    "Health" to CategoryColors.Health,
    "Other" to CategoryColors.Other,
)

private val FIXED_INCOME_COLORS: Map<String, Color> = mapOf(
    "Salary" to IncomeCategoryColors.Salary,
    "Freelance" to IncomeCategoryColors.Freelance,
    "Business" to IncomeCategoryColors.Business,
    "Investment" to IncomeCategoryColors.Investment,
    "Gift" to IncomeCategoryColors.Gift,
    "Refund" to IncomeCategoryColors.Refund,
    "Other" to IncomeCategoryColors.Other,
)

private val DEFAULT_CATEGORY_COLOR = Color(0xFF616161)

/**
 * Resolves the display color for any category name, fixed or user-created. Matches
 * getCategoryMeta's color lookup: check the fixed list first, then the matching custom
 * list, then fall back to a neutral gray if nothing matches (e.g. a category whose custom
 * entry was later deleted).
 */
fun categoryColor(
    isIncome: Boolean,
    category: String,
    customExpenseCategories: List<CustomCategoryEntity> = emptyList(),
    customIncomeCategories: List<CustomCategoryEntity> = emptyList(),
): Color {
    val fixed = if (isIncome) FIXED_INCOME_COLORS[category] else FIXED_EXPENSE_COLORS[category]
    if (fixed != null) return fixed
    val custom = (if (isIncome) customIncomeCategories else customExpenseCategories)
        .firstOrNull { it.name == category }
    return custom?.let { Color(it.color) } ?: DEFAULT_CATEGORY_COLOR
}
