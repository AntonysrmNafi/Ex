package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(db: AppDatabase) {

    private val dao = db.goalDao()

    fun observeAll(): Flow<List<GoalEntity>> = dao.observeAll()

    suspend fun insert(goal: GoalEntity): Long = dao.insert(goal)

    suspend fun update(goal: GoalEntity) = dao.update(goal)

    suspend fun delete(goal: GoalEntity) = dao.delete(goal)

    suspend fun deleteAll() = dao.deleteAll()
}
