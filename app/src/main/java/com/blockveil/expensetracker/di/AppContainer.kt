package com.blockveil.expensetracker.di

import android.content.Context
import com.blockveil.expensetracker.data.datastore.SettingsRepository
import com.blockveil.expensetracker.data.local.AppDatabase
import com.blockveil.expensetracker.data.repository.AccountRepository
import com.blockveil.expensetracker.data.repository.CategoryBudgetRepository
import com.blockveil.expensetracker.data.repository.CustomCategoryRepository
import com.blockveil.expensetracker.data.repository.GoalRepository
import com.blockveil.expensetracker.data.repository.SubscriptionRepository
import com.blockveil.expensetracker.data.repository.TransactionRepository
import com.blockveil.expensetracker.data.repository.TransferRepository

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
}
