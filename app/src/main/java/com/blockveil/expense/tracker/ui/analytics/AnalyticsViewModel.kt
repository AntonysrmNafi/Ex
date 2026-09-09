package com.blockveil.expense.tracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.repository.CategoryBudgetRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.di.SharedMonthState
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class AnalyticsViewModel(
    transactionRepository: TransactionRepository,
    customCategoryRepository: CustomCategoryRepository,
    private val categoryBudgetRepository: CategoryBudgetRepository,
    private val settingsRepository: SettingsRepository,
    private val sharedMonthState: SharedMonthState,
) : ViewModel() {

    private data class Ledger(
        val transactions: List<TransactionEntity>,
        val customExpenseCategories: List<CustomCategoryEntity>,
        val customIncomeCategories: List<CustomCategoryEntity>,
        val categoryBudgets: List<CategoryBudgetEntity>,
    )

    /** The app-wide current month, shared with Home, not owned by this ViewModel. */
    val currentMonth: StateFlow<YearMonth> = sharedMonthState.currentMonth

    private val ledger = combine(
        transactionRepository.observeAll(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
        categoryBudgetRepository.observeAll(),
    ) { transactions, customExpense, customIncome, categoryBudgets ->
        Ledger(transactions, customExpense, customIncome, categoryBudgets)
    }

    val uiState: StateFlow<AnalyticsUiState> = combine(
        ledger,
        settingsRepository.settings,
        sharedMonthState.currentMonth,
    ) { l, settings, month ->
        buildAnalyticsUiState(
            sources = AnalyticsSources(
                transactions = l.transactions,
                customExpenseCategories = l.customExpenseCategories,
                customIncomeCategories = l.customIncomeCategories,
                categoryBudgets = l.categoryBudgets,
                rawBudget = settings.budget,
                rollingEnabled = settings.rollingEnabled,
                currency = resolveCurrencyDisplay(settings),
            ),
            currentMonth = month,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState(),
    )

    fun onPrevMonth() {
        sharedMonthState.prevMonth()
    }

    fun onNextMonth() {
        sharedMonthState.nextMonth()
    }

    fun onSetBudget(value: Double) {
        viewModelScope.launch { settingsRepository.setBudget(value) }
    }

    fun onSetRolling(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRollingEnabled(enabled) }
    }

    /** Always stores the value, even 0. Matches the source design's setCategoryBudgets(prev => ({...prev, [cat]: v}))
     * exactly, it never deletes the entry; a 0 limit is filtered out at display time instead (see buildAnalyticsUiState). */
    fun onSetCategoryLimit(category: String, limit: Double) {
        viewModelScope.launch { categoryBudgetRepository.upsert(category, limit) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AnalyticsViewModel(
                    transactionRepository = container.transactionRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    categoryBudgetRepository = container.categoryBudgetRepository,
                    settingsRepository = container.settingsRepository,
                    sharedMonthState = container.sharedMonthState,
                )
            }
        }
    }
}
