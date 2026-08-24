package com.blockveil.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.blockveil.expensetracker.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCategoryDao {

    @Query("SELECT * FROM custom_categories WHERE isIncome = 0 ORDER BY id ASC")
    fun observeExpenseCategories(): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories WHERE isIncome = 1 ORDER BY id ASC")
    fun observeIncomeCategories(): Flow<List<CustomCategoryEntity>>

    @Insert
    suspend fun insert(category: CustomCategoryEntity): Long

    @Delete
    suspend fun delete(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories")
    suspend fun deleteAll()
}
