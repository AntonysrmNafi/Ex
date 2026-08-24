package com.blockveil.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.blockveil.expensetracker.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {

    @Query("SELECT * FROM transfers ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<TransferEntity>>

    @Insert
    suspend fun insert(transfer: TransferEntity): Long

    @Delete
    suspend fun delete(transfer: TransferEntity)

    @Query("DELETE FROM transfers")
    suspend fun deleteAll()
}
