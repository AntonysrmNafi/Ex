package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

class TransferRepository(db: AppDatabase) {

    private val dao = db.transferDao()

    fun observeAll(): Flow<List<TransferEntity>> = dao.observeAll()

    suspend fun insert(transfer: TransferEntity): Long = dao.insert(transfer)

    suspend fun delete(transfer: TransferEntity) = dao.delete(transfer)

    suspend fun deleteAll() = dao.deleteAll()
}
