package com.blockveil.expense.tracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.backup.BackupManager
import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
    private val customCategoryRepository: CustomCategoryRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = AppSettings())

    val expenseCategories: StateFlow<List<CustomCategoryEntity>> = customCategoryRepository.observeExpenseCategories()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val incomeCategories: StateFlow<List<CustomCategoryEntity>> = customCategoryRepository.observeIncomeCategories()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAll()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    fun onSetThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onSetCurrencyCountry(country: String) {
        viewModelScope.launch { settingsRepository.setCurrencyCountry(country) }
    }

    fun onSetCurrencyPosition(position: CurrencyPosition) {
        viewModelScope.launch { settingsRepository.setCurrencyPosition(position) }
    }

    fun onSetCurrencyFormat(format: CurrencyFormat) {
        viewModelScope.launch { settingsRepository.setCurrencyFormat(format) }
    }

    fun onDeleteCategory(category: CustomCategoryEntity) {
        viewModelScope.launch { customCategoryRepository.delete(category) }
    }

    /**
     * Deleting an account is safe by design: every foreign key that points at an account
     * (transactions, subscriptions, transfers) is declared ON DELETE SET NULL, so this never
     * fails or cascades away unrelated history, those rows just fall back to showing
     * "Deleted account" (see accountName()) instead of the account's old name.
     *
     * Returns an error message if a loan account isn't fully repaid yet (a loan's `active`
     * flag already tracks exactly that, see TransferRepository.addRepayment), or null on success.
     */
    fun onDeleteAccount(account: AccountEntity): String? {
        if (account.category == AccountCategory.LOAN && account.active) {
            return "Repay this loan in full before deleting it."
        }
        viewModelScope.launch { accountRepository.delete(account) }
        return null
    }

    fun onSetAccountHidden(account: AccountEntity, hidden: Boolean) {
        viewModelScope.launch { accountRepository.setHidden(account, hidden) }
    }

    suspend fun exportBackup(): String = backupManager.exportCsv()

    /** Returns an error message on failure, or null on success. */
    suspend fun restoreBackup(text: String): String? = backupManager.restoreCsv(text)

    suspend fun clearAllData() = backupManager.clearAllData()

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    backupManager = container.backupManager,
                    customCategoryRepository = container.customCategoryRepository,
                    accountRepository = container.accountRepository,
                )
            }
        }
    }
}
