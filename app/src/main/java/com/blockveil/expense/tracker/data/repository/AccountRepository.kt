package com.blockveil.expense.tracker.data.repository

import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(db: AppDatabase) {

    private val dao = db.accountDao()

    fun observeAll(): Flow<List<AccountEntity>> = dao.observeAll()

    /** Excludes hidden accounts, for anywhere an account can be picked for a new transaction/transfer or listed on Home. */
    fun observeVisible(): Flow<List<AccountEntity>> = dao.observeVisible()

    suspend fun getById(id: Long): AccountEntity? = dao.getById(id)

    suspend fun insert(account: AccountEntity): Long = dao.insert(account)

    suspend fun update(account: AccountEntity) = dao.update(account)

    suspend fun delete(account: AccountEntity) = dao.delete(account)

    suspend fun setHidden(account: AccountEntity, hidden: Boolean) = dao.update(account.copy(isHidden = hidden))

    suspend fun deleteAll() = dao.deleteAll()
}
