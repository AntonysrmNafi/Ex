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
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CategoryHistoryViewModel(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    customCategoryRepository: CustomCategoryRepository,
    settingsRepository: SettingsRepository,
    private val category: String,
    private val isIncome: Boolean,
) : ViewModel() {

    private data class Sources(
        val transactions: List<TransactionEntity>,
        val accounts: List<AccountEntity>,
        val customExpenseCategories: List<CustomCategoryEntity>,
        val customIncomeCategories: List<CustomCategoryEntity>,
    )

    private val sources = combine(
        transactionRepository.observeAll(),
        accountRepository.observeAll(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
    ) { transactions, accounts, customExpense, customIncome ->
        Sources(transactions, accounts, customExpense, customIncome)
    }

    val uiState: StateFlow<CategoryHistoryUiState> = combine(sources, settingsRepository.settings) { src, settings ->
        buildCategoryHistoryUiState(
            sources = CategoryHistorySources(
                transactions = src.transactions,
                accounts = src.accounts,
                customExpenseCategories = src.customExpenseCategories,
                customIncomeCategories = src.customIncomeCategories,
                currency = resolveCurrencyDisplay(settings),
            ),
            category = category,
            isIncome = isIncome,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryHistoryUiState(category = category, isIncome = isIncome),
    )

    companion object {
        fun factory(container: AppContainer, category: String, isIncome: Boolean): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CategoryHistoryViewModel(
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                    category = category,
                    isIncome = isIncome,
                )
            }
        }
    }
}
