package com.blockveil.expensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expensetracker.data.datastore.SettingsRepository
import com.blockveil.expensetracker.data.repository.AccountRepository
import com.blockveil.expensetracker.data.repository.CustomCategoryRepository
import com.blockveil.expensetracker.data.repository.TransactionRepository
import com.blockveil.expensetracker.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val customCategoryRepository: CustomCategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _dateFilter = MutableStateFlow<DateFilter>(DateFilter.Month)
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    /** Read-only, exposed so [DateFilterSheet] can show the currently active selection when opened. */
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val sources = combine(
        transactionRepository.observeAll(),
        accountRepository.observeAll(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
    ) { transactions, accounts, customExpense, customIncome ->
        HomeSources(transactions, accounts, customExpense, customIncome)
    }

    private val filterAndMonth = combine(_dateFilter, _currentMonth) { filter, month -> filter to month }

    val uiState: StateFlow<HomeUiState> = combine(
        sources,
        settingsRepository.settings,
        filterAndMonth,
    ) { src, settings, filterMonthPair ->
        val (filter, month) = filterMonthPair
        buildHomeUiState(sources = src, settings = settings, filter = filter, currentMonth = month, today = LocalDate.now())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    fun onPrevMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    /** Applied when the user taps "Select" in [DateFilterSheet]. */
    fun onApplyDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
