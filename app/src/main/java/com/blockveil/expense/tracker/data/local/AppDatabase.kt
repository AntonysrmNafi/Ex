package com.blockveil.expense.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.blockveil.expense.tracker.data.local.dao.AccountDao
import com.blockveil.expense.tracker.data.local.dao.CategoryBudgetDao
import com.blockveil.expense.tracker.data.local.dao.CustomCategoryDao
import com.blockveil.expense.tracker.data.local.dao.GoalDao
import com.blockveil.expense.tracker.data.local.dao.SubscriptionDao
import com.blockveil.expense.tracker.data.local.dao.TransactionDao
import com.blockveil.expense.tracker.data.local.dao.TransferDao
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        SubscriptionEntity::class,
        GoalEntity::class,
        TransferEntity::class,
        CategoryBudgetEntity::class,
        CustomCategoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun goalDao(): GoalDao
    abstract fun transferDao(): TransferDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun customCategoryDao(): CustomCategoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker.db",
                ).build().also { instance = it }
            }
    }
}
