package com.blockveil.expensetracker.data.repository

import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(db: AppDatabase) {

    private val dao = db.subscriptionDao()

    fun observeAll(): Flow<List<SubscriptionEntity>> = dao.observeAll()

    suspend fun getById(id: Long): SubscriptionEntity? = dao.getById(id)

    suspend fun insert(subscription: SubscriptionEntity): Long = dao.insert(subscription)

    suspend fun update(subscription: SubscriptionEntity) = dao.update(subscription)

    suspend fun delete(subscription: SubscriptionEntity) = dao.delete(subscription)

    suspend fun deleteAll() = dao.deleteAll()
}
