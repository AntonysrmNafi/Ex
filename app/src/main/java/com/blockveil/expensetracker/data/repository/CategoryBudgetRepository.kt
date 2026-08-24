package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

class CategoryBudgetRepository(db: AppDatabase) {

    private val dao = db.categoryBudgetDao()

    fun observeAll(): Flow<List<CategoryBudgetEntity>> = dao.observeAll()

    suspend fun upsert(category: String, limitAmount: Double) =
        dao.upsert(CategoryBudgetEntity(category = category, limitAmount = limitAmount))

    suspend fun delete(category: String) = dao.delete(category)

    suspend fun deleteAll() = dao.deleteAll()
}
