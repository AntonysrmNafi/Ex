package com.blockveil.expense.tracker.data.backup

import androidx.room.withTransaction
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.AppDatabase
import kotlinx.coroutines.flow.first

class BackupManager(
    private val db: AppDatabase,
    private val settingsRepository: SettingsRepository,
) {

    /** Builds the full backup CSV text from every table's current contents. Matches handleBackup. */
    suspend fun exportCsv(): String {
        val snapshot = BackupSnapshot(
            settings = settingsRepository.settings.first(),
            accounts = db.accountDao().observeAll().first(),
            transactions = db.transactionDao().observeAll().first(),
            subscriptions = db.subscriptionDao().observeAll().first(),
            goals = db.goalDao().observeAll().first(),
            transfers = db.transferDao().observeAll().first(),
            categoryBudgets = db.categoryBudgetDao().observeAll().first(),
            customExpenseCategories = db.customCategoryDao().observeExpenseCategories().first(),
            customIncomeCategories = db.customCategoryDao().observeIncomeCategories().first(),
        )
        return CsvBackup.build(snapshot)
    }

    /**
     * Replaces every table's contents with what's in [text]. Matches handleRestoreFile
     * exactly: a wholesale replace (not a merge), every table cleared first, then repopulated
     * with the backup's rows (original ids preserved so foreign keys still line up).
     * Returns an error message on failure, or null on success.
     */
    suspend fun restoreCsv(text: String): String? {
        val snapshot = runCatching { parseBackupSections(CsvBackup.parse(text)) }.getOrNull()
            ?: return "That file doesn't look like a valid backup."

        return try {
            db.withTransaction {
                db.transactionDao().deleteAll()
                db.transferDao().deleteAll()
                db.subscriptionDao().deleteAll()
                db.goalDao().deleteAll()
                db.categoryBudgetDao().deleteAll()
                db.customCategoryDao().deleteAll()
                db.accountDao().deleteAll()

                snapshot.accounts.forEach { db.accountDao().insert(it) }
                snapshot.transactions.forEach { db.transactionDao().insert(it) }
                snapshot.subscriptions.forEach { db.subscriptionDao().insert(it) }
                snapshot.goals.forEach { db.goalDao().insert(it) }
                snapshot.transfers.forEach { db.transferDao().insert(it) }
                snapshot.categoryBudgets.forEach { db.categoryBudgetDao().upsert(it) }
                snapshot.customExpenseCategories.forEach { db.customCategoryDao().insert(it) }
                snapshot.customIncomeCategories.forEach { db.customCategoryDao().insert(it) }
            }

            val settings = snapshot.settings
            settingsRepository.setBudget(settings.budget)
            settingsRepository.setRollingEnabled(settings.rollingEnabled)
            settingsRepository.setThemeMode(settings.themeMode)
            settingsRepository.setCurrencyCountry(settings.currencyCountry)
            settingsRepository.setCurrencyPosition(settings.currencyPosition)
            settingsRepository.setCurrencyFormat(settings.currencyFormat)
            null
        } catch (e: Exception) {
            "Couldn't read that backup file."
        }
    }

    /**
     * Deletes every transaction, account, subscription, goal, transfer, and category
     * budget/custom category, and resets the budget settings, but leaves theme and currency
     * preferences untouched. Matches handleClearAllData exactly.
     */
    suspend fun clearAllData() {
        db.withTransaction {
            db.transactionDao().deleteAll()
            db.transferDao().deleteAll()
            db.subscriptionDao().deleteAll()
            db.goalDao().deleteAll()
            db.categoryBudgetDao().deleteAll()
            db.customCategoryDao().deleteAll()
            db.accountDao().deleteAll()
        }
        settingsRepository.setBudget(0.0)
        settingsRepository.setRollingEnabled(false)
    }
}
