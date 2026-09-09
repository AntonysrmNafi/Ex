package com.blockveil.expense.tracker.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.SavingsType
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.data.repository.AccountRepository
import com.blockveil.expense.tracker.data.repository.SubscriptionRepository
import com.blockveil.expense.tracker.data.repository.TransferRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MoreViewModel(
    private val accountRepository: AccountRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val transferRepository: TransferRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<MoreUiState> = combine(
        accountRepository.observeAll(),
        subscriptionRepository.observeAll(),
        settingsRepository.settings,
    ) { accounts, subscriptions, settings ->
        buildMoreUiState(accounts, subscriptions, resolveCurrencyDisplay(settings))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoreUiState(),
    )

    /** Matches handleAddAccount's SAVINGS branch exactly. */
    fun onAddSavingsAccount(name: String, type: SavingsType, balance: Double) {
        viewModelScope.launch {
            accountRepository.insert(
                AccountEntity(name = name, category = AccountCategory.SAVINGS, type = type, balance = balance),
            )
        }
    }

    /**
     * Matches the Add-loan button's validation exactly (name non-blank, both amounts > 0,
     * a deposit account chosen), then handleAddAccount's LOAN branch for persistence.
     */
    fun onAddLoanAccount(name: String, loanAmountText: String, settlementAmountText: String, depositAccountId: Long?): String? {
        val loanAmount = loanAmountText.toDoubleOrNull()
        val settlementAmount = settlementAmountText.toDoubleOrNull()
        if (name.isBlank()) return "Enter a name"
        if (loanAmount == null || loanAmount <= 0) return "Enter a valid loan amount"
        if (settlementAmount == null || settlementAmount <= 0) return "Enter a valid settlement amount"
        if (depositAccountId == null) return "Choose an account to deposit into"

        viewModelScope.launch {
            transferRepository.addLoanAccount(
                name = name.trim(),
                loanAmount = loanAmount,
                settlementAmount = settlementAmount,
                depositAccountId = depositAccountId,
                date = LocalDate.now(),
            )
        }
        return null
    }

    /** Matches validateAddSubscription + addSubscription exactly. */
    fun onAddSubscription(
        name: String,
        amountText: String,
        cycle: SubscriptionCycle,
        accountId: Long?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): String? {
        val accounts = uiState.value.accounts
        val error = validateAddSubscription(name, amountText, accountId, cycle, startDate, endDate, accounts)
        if (error != null) return error

        val amount = amountText.toDouble()
        viewModelScope.launch {
            subscriptionRepository.addSubscription(
                name = name.trim(),
                amount = amount,
                cycle = cycle,
                accountId = accountId!!,
                startDate = startDate,
                endDate = endDate,
            )
        }
        return null
    }

    fun onToggleSubscriptionActive(subscription: SubscriptionEntity, active: Boolean) {
        viewModelScope.launch { subscriptionRepository.setActive(subscription, active) }
    }

    fun onDeleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch { subscriptionRepository.delete(subscription) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MoreViewModel(
                    accountRepository = container.accountRepository,
                    subscriptionRepository = container.subscriptionRepository,
                    transferRepository = container.transferRepository,
                    settingsRepository = container.settingsRepository,
                )
            }
        }
    }
}
