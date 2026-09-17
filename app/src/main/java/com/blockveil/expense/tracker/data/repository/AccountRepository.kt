package com.blockveil.expense.tracker.data.repository

import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.util.isDisplayableWhileHidden
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(db: AppDatabase) {

    private val dao = db.accountDao()

    fun observeAll(): Flow<List<AccountEntity>> = dao.observeAll()

    /** Strictly excludes every hidden account, regardless of balance. For pickers where a new
     *  transaction/transfer gets posted against an account -- a hidden account can never receive one. */
    fun observeVisible(): Flow<List<AccountEntity>> = dao.observeVisible()

    /** Excludes hidden accounts once their balance reaches zero, but keeps a hidden account with
     *  a real balance still on-screen. For read-only lists (Home's account strip, More's account
     *  list) -- not for new-transaction pickers, which should use [observeVisible] instead. */
    fun observeDisplayable(): Flow<List<AccountEntity>> =
        dao.observeAll().map { accounts -> accounts.filter { it.isDisplayableWhileHidden() } }

    suspend fun getById(id: Long): AccountEntity? = dao.getById(id)

    suspend fun insert(account: AccountEntity): Long = dao.insert(account)

    suspend fun update(account: AccountEntity) = dao.update(account)

    suspend fun delete(account: AccountEntity) = dao.delete(account)

    suspend fun setHidden(account: AccountEntity, hidden: Boolean) = dao.update(account.copy(isHidden = hidden))

    suspend fun deleteAll() = dao.deleteAll()
}
