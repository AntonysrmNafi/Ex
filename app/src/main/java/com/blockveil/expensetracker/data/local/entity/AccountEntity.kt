package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.data.model.SavingsType

/**
 * A financial account. When [category] is SAVINGS, [type] and [balance] are used and the
 * loan fields stay null. When [category] is LOAN, [principal]/[loanAmount]/[repaid]/[active]
 * are used and [type]/[balance] stay null. Two shapes in one table because the original
 * design also treats loans and savings accounts as one interchangeable "accounts" list.
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: AccountCategory,
    val type: SavingsType? = null,
    val balance: Double? = null,
    val principal: Double? = null,
    val loanAmount: Double? = null,
    val repaid: Double? = null,
    val active: Boolean = true,
)
