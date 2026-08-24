package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A savings goal, e.g. "iPhone, target 120000, saved 35000, over 6 months". */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetMonths: Int,
)
