package com.blockveil.expense.tracker.ui.transaction

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.util.groupDigits

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

/** Validates a transfer between two savings accounts. Matches handleSave's isTransfer branch exactly. */
fun validateTransfer(
    amountText: String,
    fromId: Long?,
    toId: Long?,
    accounts: List<AccountEntity>,
): String? {
    val amount = amountText.toDoubleOrNull()
    if (amount == null || amount <= 0) return "Enter a valid amount"
    if (fromId == null || toId == null || fromId == toId) return "Choose two different accounts"
    val fromAccount = accounts.firstOrNull { it.id == fromId }
    if (fromAccount != null && amount > (fromAccount.balance ?: 0.0)) return "Insufficient balance in ${fromAccount.name}"
    return null
}

/** Validates a loan repayment. Matches handleSave's isRepay branch exactly. */
fun validateRepay(
    amountText: String,
    fromId: Long?,
    loanId: Long?,
    accounts: List<AccountEntity>,
): String? {
    val amount = amountText.toDoubleOrNull()
    if (amount == null || amount <= 0) return "Enter a valid amount"
    if (fromId == null || loanId == null) return "Choose a loan and a source account"
    val fromAccount = accounts.firstOrNull { it.id == fromId }
    val loan = accounts.firstOrNull { it.id == loanId }
    val remaining = if (loan != null) (loan.principal ?: 0.0) - (loan.repaid ?: 0.0) else 0.0
    if (fromAccount != null && amount > (fromAccount.balance ?: 0.0)) return "Insufficient balance in ${fromAccount.name}"
    if (amount > remaining) return "Only ${groupDigits(remaining.toLong().toString())} left on this loan"
    return null
}
