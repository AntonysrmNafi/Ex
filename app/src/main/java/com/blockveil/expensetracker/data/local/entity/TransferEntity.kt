package com.blockveil.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.blockveil.expensetracker.data.model.TransferKind
import java.time.LocalDate

/**
 * Money moved between two accounts: a plain transfer, a loan repayment, or a loan
 * disbursement (matches the "kind" values used in the source design's TRANSFERS table).
 * Both ends are nullable with SET_NULL so history survives a deleted account.
 */
@Entity(
    tableName = "transfers",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("fromId"), Index("toId")],
)
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: TransferKind,
    val fromId: Long?,
    val toId: Long?,
    val amount: Double,
    val note: String = "",
    val date: LocalDate,
)
