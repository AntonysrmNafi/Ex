package com.blockveil.expense.tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.di.SharedMonthState
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
    private val sharedMonthState: SharedMonthState,
) : ViewModel() {

    private val _dateFilter = MutableStateFlow<DateFilter>(DateFilter.Month)

    /** Read-only, exposed so [DateFilterSheet] can show the currently active selection when opened. */
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    /** The app-wide current month, shared with Analytics, not owned by this ViewModel. */
    val currentMonth: StateFlow<YearMonth> = sharedMonthState.currentMonth

    private val sources = combine(
        transactionRepository.observeAll(),
        accountRepository.observeVisible(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
    ) { transactions, accounts, customExpense, customIncome ->
        HomeSources(transactions, accounts, customExpense, customIncome)
    }

    private val filterAndMonth = combine(_dateFilter, sharedMonthState.currentMonth) { filter, month -> filter to month }

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
        if (_dateFilter.value == DateFilter.Month) sharedMonthState.prevMonth() else _dateFilter.value = _dateFilter.value.stepped(forward = false)
    }

    fun onNextMonth() {
        if (_dateFilter.value == DateFilter.Month) sharedMonthState.nextMonth() else _dateFilter.value = _dateFilter.value.stepped(forward = true)
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
                    sharedMonthState = container.sharedMonthState,
                )
            }
        }
    }
}
