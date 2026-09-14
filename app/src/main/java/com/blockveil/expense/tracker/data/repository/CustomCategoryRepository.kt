package com.blockveil.expense.tracker.data.repository

import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

class CustomCategoryRepository(db: AppDatabase) {

    private val dao = db.customCategoryDao()

    fun observeExpenseCategories(): Flow<List<CustomCategoryEntity>> = dao.observeExpenseCategories()

    fun observeIncomeCategories(): Flow<List<CustomCategoryEntity>> = dao.observeIncomeCategories()

    /** Excludes hidden entries, for the category/source picker when adding a new transaction. */
    fun observeVisibleExpenseCategories(): Flow<List<CustomCategoryEntity>> = dao.observeVisibleExpenseCategories()

    fun observeVisibleIncomeCategories(): Flow<List<CustomCategoryEntity>> = dao.observeVisibleIncomeCategories()

    suspend fun insert(name: String, color: Int, isIncome: Boolean, icon: String = "Sell"): Long =
        dao.insert(CustomCategoryEntity(name = name, color = color, isIncome = isIncome, icon = icon))

    suspend fun setHidden(category: CustomCategoryEntity, hidden: Boolean) = dao.update(category.copy(isHidden = hidden))

    suspend fun delete(category: CustomCategoryEntity) = dao.delete(category)

    suspend fun deleteAll() = dao.deleteAll()
}
