package com.blockveil.expense.tracker.ui.transaction

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.CustomCategoryRepository
import com.blockveil.expense.tracker.data.repository.TransactionRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.accountName
import com.blockveil.expense.tracker.util.categoryColor
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    /** Null while still loading, or if the transaction was deleted out from under this screen (e.g. from another device via a future sync feature). */
    val transaction: TransactionEntity? = null,
    val accountName: String = "",
    val categoryColor: Color = Color.Gray,
    val currency: CurrencyDisplay = resolveCurrencyDisplay(AppSettings()),
)

/**
 * Backs the read-only preview shown when a transaction row is tapped, before the user
 * chooses to Edit it (see TransactionDetailScreen). Reactive rather than a one-shot load, so
 * editing the transaction and coming back to this screen (or another session doing so)
 * always reflects the current data.
 */
class TransactionDetailViewModel(
    private val transactionId: Long,
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    customCategoryRepository: CustomCategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionRepository.observeAll(),
        accountRepository.observeAll(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
        settingsRepository.settings,
    ) { transactions, accounts, expenseCategories, incomeCategories, settings ->
        val txn = transactions.firstOrNull { it.id == transactionId }
        TransactionDetailUiState(
            isLoading = false,
            transaction = txn,
            accountName = accountName(accounts, txn?.accountId),
            categoryColor = txn?.let {
                categoryColor(it.type == TransactionType.INCOME, it.category, expenseCategories, incomeCategories)
            } ?: Color.Gray,
            currency = resolveCurrencyDisplay(settings),
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = TransactionDetailUiState())

    companion object {
        fun factory(container: AppContainer, transactionId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TransactionDetailViewModel(
                    transactionId = transactionId,
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
