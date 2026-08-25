package com.blockveil.expensetracker.ui.transaction

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
import com.blockveil.expensetracker.data.datastore.AppSettings
import com.blockveil.expensetracker.data.datastore.SettingsRepository
import com.blockveil.expensetracker.data.local.entity.TransactionEntity
import com.blockveil.expensetracker.data.model.TransactionType
import com.blockveil.expensetracker.data.repository.AccountRepository
import com.blockveil.expensetracker.data.repository.CustomCategoryRepository
import com.blockveil.expensetracker.data.repository.TransactionRepository
import com.blockveil.expensetracker.di.AppContainer
import com.blockveil.expensetracker.ui.theme.CustomCategoryPalette
import com.blockveil.expensetracker.util.resolveCurrencyDisplay
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

        viewModelScope.launch {
            val current = existing
            if (current != null) {
                transactionRepository.updateTransaction(current, entity)
            } else {
                transactionRepository.addTransaction(entity)
            }
            isSaved = true
        }
    }

    fun onDelete() {
        val current = existing ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(current)
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
                    existingId = existingId,
                )
            }
        }
    }
}
