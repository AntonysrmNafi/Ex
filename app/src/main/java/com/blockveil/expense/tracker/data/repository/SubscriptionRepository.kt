package com.blockveil.expense.tracker.data.repository

import androidx.room.withTransaction
import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.util.computeDueDates
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class SubscriptionRepository(private val db: AppDatabase) {

    private val subscriptionDao = db.subscriptionDao()
    private val transactionDao = db.transactionDao()
    private val accountDao = db.accountDao()

    fun observeAll(): Flow<List<SubscriptionEntity>> = subscriptionDao.observeAll()

    suspend fun getById(id: Long): SubscriptionEntity? = subscriptionDao.getById(id)

    /**
     * Adds a subscription and immediately records any charges already due in its date range
     * (e.g. a Monthly bill backdated to a start date earlier this month), deducting them from
     * the chosen account. Matches handleAddSubscription exactly: due dates come from
     * [computeDueDates] with no prior billedDates, one EXPENSE transaction per due date,
     * category "Other", note "{name} subscription".
     */
    suspend fun addSubscription(
        name: String,
        amount: Double,
        cycle: SubscriptionCycle,
        accountId: Long,
        startDate: LocalDate?,
        endDate: LocalDate?,
        today: LocalDate = LocalDate.now(),
    ): Long = db.withTransaction {
        val due = computeDueDates(cycle = cycle, startDate = startDate, endDate = endDate, billedDates = emptyList(), today = today)

        if (due.isNotEmpty()) {
            due.forEach { date ->
                transactionDao.insert(
                    TransactionEntity(
                        type = TransactionType.EXPENSE,
                        amount = amount,
                        category = "Other",
                        note = "$name subscription",
                        date = date,
                        accountId = accountId,
                        location = "",
                    ),
                )
            }
            val account = accountDao.getById(accountId)
            if (account != null) {
                accountDao.update(account.copy(balance = (account.balance ?: 0.0) - (amount * due.size)))
            }
        }

        subscriptionDao.insert(
            SubscriptionEntity(
                name = name,
                category = "Other",
                amount = amount,
                cycle = cycle,
                accountId = accountId,
                startDate = startDate,
                endDate = endDate,
                active = true,
                billedDates = due,
            ),
        )
    }

    /** Flips active on/off with no other side effects, matches the inline onToggleSubscription handler exactly. */
    suspend fun setActive(subscription: SubscriptionEntity, active: Boolean) {
        subscriptionDao.update(subscription.copy(active = active))
    }

    suspend fun delete(subscription: SubscriptionEntity) = subscriptionDao.delete(subscription)

    suspend fun deleteAll() = subscriptionDao.deleteAll()
}
