package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

class CustomCategoryRepository(db: AppDatabase) {

    private val dao = db.customCategoryDao()

    fun observeExpenseCategories(): Flow<List<CustomCategoryEntity>> = dao.observeExpenseCategories()

    fun observeIncomeCategories(): Flow<List<CustomCategoryEntity>> = dao.observeIncomeCategories()

    suspend fun insert(name: String, color: Int, isIncome: Boolean): Long =
        dao.insert(CustomCategoryEntity(name = name, color = color, isIncome = isIncome))

    suspend fun delete(category: CustomCategoryEntity) = dao.delete(category)

    suspend fun deleteAll() = dao.deleteAll()
}
