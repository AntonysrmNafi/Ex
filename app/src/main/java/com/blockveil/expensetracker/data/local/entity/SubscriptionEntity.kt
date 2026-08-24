package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blockveil.expensetracker.data.model.SubscriptionCycle
import java.time.LocalDate

/**
 * A recurring subscription/bill. [billedDates] is every date it has already charged, used
 * by the due-date calculation (Bag 3) to know which occurrences are still owed.
 */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("accountId")],
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val cycle: SubscriptionCycle,
    val accountId: Long?,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val active: Boolean = true,
    val billedDates: List<LocalDate> = emptyList(),
)
