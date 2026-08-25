package com.blockveil.expensetracker.ui.transaction

import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.local.entity.TransactionEntity

private const val MAX_REASONABLE_AMOUNT = 10_000_000.0

/**
 * Validates an income/expense save. Matches handleSave in the source design exactly,
 * including the insufficient-balance check for expenses (which accounts for the amount
 * being "reclaimed" back when editing a transaction already charged to the same account,
 * so editing an existing expense's note doesn't falsely trip the balance check).
 */
fun validateIncomeExpense(
    amountText: String,
    accountId: Long?,
    isExpense: Boolean,
    accounts: List<AccountEntity>,
    existing: TransactionEntity?,
): String? {
    val amount = amountText.toDoubleOrNull()
    if (amount == null) return "Enter a valid number"
    if (amount <= 0) return "Amount must be greater than 0"
    if (amount > MAX_REASONABLE_AMOUNT) return "That amount looks too large, double check it"
    if (accountId == null) return "Choose an account"

    if (isExpense) {
        val selected = accounts.firstOrNull { it.id == accountId }
        if (selected != null) {
            val reclaimed = if (existing != null && existing.accountId == accountId) existing.amount else 0.0
            val available = (selected.balance ?: 0.0) + reclaimed
            if (amount > available) return "Insufficient balance in ${selected.name}"
        }
    }
    return null
}
