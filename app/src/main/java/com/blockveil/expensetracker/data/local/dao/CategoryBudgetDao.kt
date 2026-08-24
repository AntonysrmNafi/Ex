package com.blockveil.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blockveil.expensetracker.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {

    @Query("SELECT * FROM category_budgets ORDER BY category ASC")
    fun observeAll(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE category = :category")
    suspend fun delete(category: String)

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAll()
}
