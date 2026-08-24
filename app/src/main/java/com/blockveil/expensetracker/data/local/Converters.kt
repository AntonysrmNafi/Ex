package com.blockveil.expensetracker.data.local

import androidx.room.TypeConverter
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.data.model.SavingsType
import com.blockveil.expensetracker.data.model.SubscriptionCycle
import com.blockveil.expensetracker.data.model.TransactionType
import com.blockveil.expensetracker.data.model.TransferKind
import java.time.LocalDate

/**
 * Room can only persist primitives, so every LocalDate, date list, and enum used in an
 * entity needs an explicit converter here. minSdk 26 means java.time works natively,
 * no desugaring library required.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    // Stored as semicolon-joined epoch days, same separator convention as the CSV backup format.
    @TypeConverter
    fun fromDateList(dates: List<LocalDate>): String =
        dates.joinToString(";") { it.toEpochDay().toString() }

    @TypeConverter
    fun toDateList(raw: String?): List<LocalDate> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(";").mapNotNull { it.toLongOrNull()?.let(LocalDate::ofEpochDay) }
    }

    @TypeConverter
    fun fromAccountCategory(value: AccountCategory): String = value.name

    @TypeConverter
    fun toAccountCategory(value: String): AccountCategory = AccountCategory.valueOf(value)

    @TypeConverter
    fun fromSavingsType(value: SavingsType?): String? = value?.name

    @TypeConverter
    fun toSavingsType(value: String?): SavingsType? = value?.let(SavingsType::valueOf)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromSubscriptionCycle(value: SubscriptionCycle): String = value.name

    @TypeConverter
    fun toSubscriptionCycle(value: String): SubscriptionCycle = SubscriptionCycle.valueOf(value)

    @TypeConverter
    fun fromTransferKind(value: TransferKind): String = value.name

    @TypeConverter
    fun toTransferKind(value: String): TransferKind = TransferKind.valueOf(value)
}
