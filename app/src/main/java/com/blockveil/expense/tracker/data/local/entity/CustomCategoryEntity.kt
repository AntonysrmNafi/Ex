package com.blockveil.expense.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A user-created category. [isIncome] separates it into the expense list or the income
 * list, matching CUSTOM_EXPENSE_CATEGORIES / CUSTOM_INCOME_CATEGORIES being two separate
 * arrays in the source design. [color] is a packed ARGB int, ready for Compose's Color(Int).
 */
@Entity(
    tableName = "custom_categories",
    indices = [Index(value = ["name", "isIncome"], unique = true)],
)
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val isIncome: Boolean,
    /** Hidden categories drop out of the picker for new transactions but keep showing on
     *  past transactions that already used them, same as a fixed category being hidden. */
    val isHidden: Boolean = false,
    /** Key into CUSTOM_CATEGORY_ICON_CHOICES (see CategoryIcons.kt); "Sell" if unset. */
    val icon: String = "Sell",
)
