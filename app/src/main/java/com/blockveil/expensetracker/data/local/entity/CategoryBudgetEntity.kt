package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A monthly spending limit for one expense category, e.g. "Food, limit 5000". */
@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey val category: String,
    val limitAmount: Double,
)
