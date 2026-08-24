package com.blockveil.expensetracker.data.repository

import androidx.room.withTransaction
import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.local.entity.TransactionEntity
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val db: AppDatabase) {

    private val transactionDao = db.transactionDao()
    private val accountDao = db.accountDao()

    fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    /** Inserts the transaction and applies its effect to the linked account's balance. */
    suspend fun addTransaction(transaction: TransactionEntity): Long = db.withTransaction {
        val id = transactionDao.insert(transaction)
        applyBalanceDelta(transaction.accountId, transaction.type, transaction.amount, reverse = false)
        id
    }

    /**
     * Updates a transaction. Reverses the old amount's effect on its old account first, then
     * applies the new amount to its (possibly different) account, so editing never double
     * counts, matches handleSaveTransaction's edit branch in the source design.
     */
    suspend fun updateTransaction(old: TransactionEntity, updated: TransactionEntity) = db.withTransaction {
        applyBalanceDelta(old.accountId, old.type, old.amount, reverse = true)
        transactionDao.update(updated)
        applyBalanceDelta(updated.accountId, updated.type, updated.amount, reverse = false)
    }

    /** Deletes the transaction and reverses its effect on the linked account's balance. */
    suspend fun deleteTransaction(transaction: TransactionEntity) = db.withTransaction {
        transactionDao.delete(transaction)
        applyBalanceDelta(transaction.accountId, transaction.type, transaction.amount, reverse = true)
    }

    private suspend fun applyBalanceDelta(
        accountId: Long?,
        type: TransactionType,
        amount: Double,
        reverse: Boolean,
    ) {
        val id = accountId ?: return
        val account = accountDao.getById(id) ?: return
        // Regular income/expense transactions only ever touch a savings-type account's
        // balance. Loans have no `balance` field, they're moved via the transfer repository.
        if (account.category != AccountCategory.SAVINGS) return
        val sign = if (type == TransactionType.INCOME) 1.0 else -1.0
        val effectiveSign = if (reverse) -sign else sign
        updateBalance(account, effectiveSign * amount)
    }

    private suspend fun updateBalance(account: AccountEntity, delta: Double) {
        accountDao.update(account.copy(balance = (account.balance ?: 0.0) + delta))
    }

    suspend fun deleteAll() = transactionDao.deleteAll()
}
