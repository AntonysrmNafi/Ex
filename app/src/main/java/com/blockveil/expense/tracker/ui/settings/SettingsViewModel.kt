package com.blockveil.expense.tracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.backup.BackupManager
import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = AppSettings())

    fun onSetThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onSetCurrencyCountry(country: String) {
        viewModelScope.launch { settingsRepository.setCurrencyCountry(country) }
    }

    fun onSetCurrencyPosition(position: CurrencyPosition) {
        viewModelScope.launch { settingsRepository.setCurrencyPosition(position) }
    }

    fun onSetCurrencyFormat(format: CurrencyFormat) {
        viewModelScope.launch { settingsRepository.setCurrencyFormat(format) }
    }

    suspend fun exportBackup(): String = backupManager.exportCsv()

    /** Returns an error message on failure, or null on success. */
    suspend fun restoreBackup(text: String): String? = backupManager.restoreCsv(text)

    suspend fun clearAllData() = backupManager.clearAllData()

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    backupManager = container.backupManager,
                )
            }
        }
    }
}
