package com.blockveil.expense.tracker.ui.drilldown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.data.repository.TransferRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountHistoryViewModel(
    transactionRepository: TransactionRepository,
    transferRepository: TransferRepository,
    accountRepository: AccountRepository,
    customCategoryRepository: CustomCategoryRepository,
    settingsRepository: SettingsRepository,
    private val accountId: Long,
) : ViewModel() {

    private data class Ledger(
        val transactions: List<TransactionEntity>,
        val transfers: List<TransferEntity>,
        val accounts: List<AccountEntity>,
    )

    private data class Categories(
        val customExpenseCategories: List<CustomCategoryEntity>,
        val customIncomeCategories: List<CustomCategoryEntity>,
    )

    private val ledger = combine(
        transactionRepository.observeAll(),
        transferRepository.observeAll(),
        accountRepository.observeAll(),
    ) { transactions, transfers, accounts -> Ledger(transactions, transfers, accounts) }

    private val categories = combine(
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
    ) { customExpense, customIncome -> Categories(customExpense, customIncome) }

    val uiState: StateFlow<AccountHistoryUiState> = combine(ledger, categories, settingsRepository.settings) { l, c, settings ->
        buildAccountHistoryUiState(
            sources = AccountHistorySources(
                transactions = l.transactions,
                transfers = l.transfers,
                accounts = l.accounts,
                customExpenseCategories = c.customExpenseCategories,
                customIncomeCategories = c.customIncomeCategories,
                currency = resolveCurrencyDisplay(settings),
            ),
            accountId = accountId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountHistoryUiState(),
    )

    companion object {
        fun factory(container: AppContainer, accountId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AccountHistoryViewModel(
                    transactionRepository = container.transactionRepository,
                    transferRepository = container.transferRepository,
                    accountRepository = container.accountRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                    accountId = accountId,
                )
            }
        }
    }
}
