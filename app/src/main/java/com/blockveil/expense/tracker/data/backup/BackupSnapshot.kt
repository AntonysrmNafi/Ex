package com.blockveil.expense.tracker.data.backup

import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity

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
