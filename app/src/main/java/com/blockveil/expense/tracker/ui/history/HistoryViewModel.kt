package com.blockveil.expense.tracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.data.repository.TransferRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    transferRepository: TransferRepository,
    customCategoryRepository: CustomCategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _filterType = MutableStateFlow(HistoryFilterType.ALL)

    // Split into two 3-flow combines (kept under the direct 5-flow overload), then merged,
    // rather than one large combine() that would need an untyped vararg array.
    private val ledger = combine(
        transactionRepository.observeAll(),
        transferRepository.observeAll(),
        accountRepository.observeAll(),
    ) { transactions, transfers, accounts -> Triple(transactions, transfers, accounts) }

    private val categoriesAndSettings = combine(
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
        settingsRepository.settings,
    ) { customExpense, customIncome, settings -> Triple(customExpense, customIncome, settings) }

    private val sources = combine(ledger, categoriesAndSettings) { (transactions, transfers, accounts), (customExpense, customIncome, settings) ->
        HistorySources(
            transactions = transactions,
            transfers = transfers,
            accounts = accounts,
            customExpenseCategories = customExpense,
            customIncomeCategories = customIncome,
            currency = resolveCurrencyDisplay(settings),
        )
    }

    val uiState: StateFlow<HistoryUiState> = combine(sources, _query, _filterType) { src, query, filterType ->
        buildHistoryUiState(src, query, filterType)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onFilterChange(filter: HistoryFilterType) {
        _filterType.value = filter
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    transferRepository = container.transferRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
