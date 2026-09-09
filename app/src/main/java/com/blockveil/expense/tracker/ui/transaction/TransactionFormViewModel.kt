package com.blockveil.expense.tracker.ui.transaction

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
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
import com.blockveil.expense.tracker.data.repository.TransferRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.ui.theme.CustomCategoryPalette
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionFormViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val customCategoryRepository: CustomCategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val transferRepository: TransferRepository,
    private val existingId: Long?,
) : ViewModel() {

    val sources: StateFlow<TransactionFormSources> = combine(
        accountRepository.observeAll(),
        customCategoryRepository.observeExpenseCategories(),
        customCategoryRepository.observeIncomeCategories(),
        settingsRepository.settings,
    ) { accounts, customExpense, customIncome, settings ->
        TransactionFormSources(accounts, customExpense, customIncome, resolveCurrencyDisplay(settings))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionFormSources(emptyList(), emptyList(), emptyList(), resolveCurrencyDisplay(AppSettings())),
    )

    var fields by mutableStateOf(TransactionFormFields())
        private set

    /** Null while creating a new entry, or the loaded record once editing has finished loading. */
    var existing by mutableStateOf<TransactionEntity?>(null)
        private set

    var isLoadingExisting by mutableStateOf(existingId != null)
        private set

    var isSaved by mutableStateOf(false)
        private set

    var isDeleted by mutableStateOf(false)
        private set

    /** Toast text for the save/delete that just completed, matching the source design's per-action showToast() call. */
    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    /** Whether the save that just completed should play the money-burst, matching triggerMoneyAnim()'s call sites (add, transfer, repay, not edits or deletes). */
    var triggersMoneyBurst by mutableStateOf(false)
        private set

    /** True when editing an existing entry, locks the type selector, matches `locked = !!existing`. */
    val isEditing: Boolean get() = existingId != null

    init {
        if (existingId != null) {
            viewModelScope.launch {
                val loaded = transactionRepository.getById(existingId)
                existing = loaded
                if (loaded != null) {
                    fields = fields.copy(
                        type = if (loaded.type == TransactionType.INCOME) TransactionFormType.INCOME else TransactionFormType.EXPENSE,
                        amount = trimTrailingZeros(loaded.amount),
                        category = loaded.category,
                        accountId = loaded.accountId,
                        location = loaded.location,
                        note = loaded.note,
                        date = loaded.date,
                        photoUri = loaded.receiptPhotoPath?.let(Uri::parse),
                    )
                }
                isLoadingExisting = false
            }
        }
    }

    fun onTypeChange(type: TransactionFormType) {
        if (isEditing) return // locked while editing, matches the source design
        fields = fields.copy(type = type, category = "", errorMessage = null)
    }

    fun onAmountChange(value: String) {
        fields = fields.copy(amount = value, errorMessage = null)
    }

    fun onCategoryChange(category: String) {
        fields = fields.copy(category = category, errorMessage = null)
    }

    fun onAccountChange(accountId: Long) {
        fields = fields.copy(accountId = accountId, errorMessage = null)
    }

    fun onFromChange(id: Long) {
        fields = fields.copy(fromId = id, errorMessage = null)
    }

    fun onToChange(id: Long) {
        fields = fields.copy(toId = id, errorMessage = null)
    }

    fun onLoanChange(id: Long) {
        fields = fields.copy(loanId = id, errorMessage = null)
    }

    fun onLocationChange(value: String) {
        fields = fields.copy(location = value)
    }

    fun onNoteChange(value: String) {
        fields = fields.copy(note = value)
    }

    fun onDateChange(date: LocalDate) {
        fields = fields.copy(date = date)
    }

    fun onPhotoChange(uri: Uri?) {
        fields = fields.copy(photoUri = uri)
    }

    fun onErrorMessage(message: String) {
        fields = fields.copy(errorMessage = message)
    }

    /** Adds a custom category if its name isn't already taken (case-insensitive), then selects it. Matches handleAddCustomCategory. */
    fun onAddCustomCategory(name: String) {
        val isIncome = fields.type == TransactionFormType.INCOME
        val currentSources = sources.value
        val existingList = if (isIncome) currentSources.customIncomeCategories else currentSources.customExpenseCategories
        if (existingList.none { it.name.equals(name, ignoreCase = true) }) {
            val color = CustomCategoryPalette[existingList.size % CustomCategoryPalette.size].toArgb()
            viewModelScope.launch {
                customCategoryRepository.insert(name = name, color = color, isIncome = isIncome)
            }
        }
        fields = fields.copy(category = name)
    }

    fun onSave() {
        when (fields.type) {
            TransactionFormType.INCOME, TransactionFormType.EXPENSE -> saveIncomeOrExpense()
            TransactionFormType.TRANSFER -> saveTransfer()
            TransactionFormType.REPAY -> saveRepayment()
        }
    }

    private fun saveIncomeOrExpense() {
        val currentSources = sources.value
        val isExpense = fields.type == TransactionFormType.EXPENSE
        val category = effectiveCategory(fields, currentSources)
        val accountId = effectiveAccountId(fields, currentSources)

        val error = validateIncomeExpense(fields.amount, accountId, isExpense, currentSources.accounts, existing)
        if (error != null) {
            fields = fields.copy(errorMessage = error)
            return
        }

        val amount = fields.amount.toDouble()
        val entity = TransactionEntity(
            id = existing?.id ?: 0,
            type = if (fields.type == TransactionFormType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE,
            amount = amount,
            category = category,
            note = fields.note.trim(),
            date = fields.date,
            accountId = accountId,
            location = fields.location.trim(),
            receiptPhotoPath = fields.photoUri?.toString(),
        )

        val isAdd = existing == null
        val noun = if (isExpense) "Expense" else "Income"
        viewModelScope.launch {
            val current = existing
            if (current != null) {
                transactionRepository.updateTransaction(current, entity)
            } else {
                transactionRepository.addTransaction(entity)
            }
            feedbackMessage = if (isAdd) "$noun added" else "$noun updated"
            triggersMoneyBurst = isAdd
            isSaved = true
        }
    }

    private fun saveTransfer() {
        val currentSources = sources.value
        val fromId = effectiveFromId(fields, currentSources)
        val toId = effectiveToId(fields, currentSources, fromId)

        val error = validateTransfer(fields.amount, fromId, toId, currentSources.accounts)
        if (error != null) {
            fields = fields.copy(errorMessage = error)
            return
        }

        val amount = fields.amount.toDouble()
        viewModelScope.launch {
            transferRepository.addTransfer(fromId!!, toId!!, amount, fields.note.trim(), fields.date)
            feedbackMessage = "Transfer complete"
            triggersMoneyBurst = true
            isSaved = true
        }
    }

    private fun saveRepayment() {
        val currentSources = sources.value
        val fromId = effectiveFromId(fields, currentSources)
        val loanId = effectiveLoanId(fields, currentSources)

        val error = validateRepay(fields.amount, fromId, loanId, currentSources.accounts)
        if (error != null) {
            fields = fields.copy(errorMessage = error)
            return
        }

        val amount = fields.amount.toDouble()
        val loan = currentSources.accounts.find { it.id == loanId }
        val willSettle = loan != null && transferRepository.wouldSettleLoan(loan, amount)
        viewModelScope.launch {
            transferRepository.addRepayment(fromId!!, loanId!!, amount, fields.date)
            feedbackMessage = if (willSettle) "Loan fully repaid and settled" else "Loan repayment recorded"
            triggersMoneyBurst = true
            isSaved = true
        }
    }

    fun onDelete() {
        val current = existing ?: return
        val noun = if (current.type == TransactionType.INCOME) "Income" else "Expense"
        viewModelScope.launch {
            transactionRepository.deleteTransaction(current)
            feedbackMessage = "$noun deleted"
            isDeleted = true
        }
    }

    private fun trimTrailingZeros(amount: Double): String {
        // Editing shows "450" instead of "450.0" for a whole number, but keeps real decimals
        // like "450.5", matching how a user would naturally re-type the amount.
        return if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
    }

    companion object {
        fun factory(container: AppContainer, existingId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TransactionFormViewModel(
                    transactionRepository = container.transactionRepository,
                    accountRepository = container.accountRepository,
                    customCategoryRepository = container.customCategoryRepository,
                    settingsRepository = container.settingsRepository,
                    transferRepository = container.transferRepository,
                    existingId = existingId,
                )
            }
        }
    }
}
