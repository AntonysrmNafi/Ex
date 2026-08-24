package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(db: AppDatabase) {

    private val dao = db.accountDao()

    fun observeAll(): Flow<List<AccountEntity>> = dao.observeAll()

    suspend fun getById(id: Long): AccountEntity? = dao.getById(id)

    suspend fun insert(account: AccountEntity): Long = dao.insert(account)

    suspend fun update(account: AccountEntity) = dao.update(account)

    suspend fun delete(account: AccountEntity) = dao.delete(account)

    suspend fun deleteAll() = dao.deleteAll()
}
