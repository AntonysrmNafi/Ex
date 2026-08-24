package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blockveil.expensetracker.data.model.TransactionType
import java.time.LocalDate

/**
 * A single income or expense entry. Note: the original web preview also stores a
 * "monthOffset" relative to a frozen demo date, purely so the mock data could pretend to
 * span different months. The real app has no frozen clock, so [date] is a real calendar
 * date and any month grouping is computed from it directly where needed (Bag 6 onward).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("accountId"), Index("date")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val note: String = "",
    val date: LocalDate,
    val accountId: Long?,
    val location: String = "",
    val receiptPhotoPath: String? = null,
)
