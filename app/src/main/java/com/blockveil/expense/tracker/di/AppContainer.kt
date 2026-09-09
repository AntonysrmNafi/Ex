package com.blockveil.expense.tracker.di

import android.content.Context
import com.blockveil.expense.tracker.data.backup.BackupManager
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CategoryBudgetRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.GoalRepository
import com.blockveil.expense.tracker.data.repository.SubscriptionRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.data.repository.TransferRepository

/** Single place every screen's ViewModel pulls its repositories from, via [ExpenseTrackerApp]. */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val accountRepository = AccountRepository(database)
    val transactionRepository = TransactionRepository(database)
    val subscriptionRepository = SubscriptionRepository(database)
    val goalRepository = GoalRepository(database)
    val transferRepository = TransferRepository(database)
    val categoryBudgetRepository = CategoryBudgetRepository(database)
    val customCategoryRepository = CustomCategoryRepository(database)
    val settingsRepository = SettingsRepository(context)
    val sharedMonthState = SharedMonthState()
    val backupManager = BackupManager(database, settingsRepository)
}
