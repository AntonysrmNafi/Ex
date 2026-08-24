package com.blockveil.expensetracker.data.backup

import com.blockveil.expensetracker.data.datastore.AppSettings
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expensetracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expensetracker.data.local.entity.GoalEntity
import com.blockveil.expensetracker.data.local.entity.SubscriptionEntity
import com.blockveil.expensetracker.data.local.entity.TransactionEntity
import com.blockveil.expensetracker.data.local.entity.TransferEntity

/** A full, portable snapshot of every table the app persists, ready to serialize to CSV. */
data class BackupSnapshot(
    val settings: AppSettings,
    val accounts: List<AccountEntity>,
    val transactions: List<TransactionEntity>,
    val subscriptions: List<SubscriptionEntity>,
    val goals: List<GoalEntity>,
    val transfers: List<TransferEntity>,
    val categoryBudgets: List<CategoryBudgetEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
)
