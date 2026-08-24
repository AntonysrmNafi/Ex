package com.blockveil.expensetracker.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blockveil.expensetracker.data.model.CurrencyFormat
import com.blockveil.expensetracker.data.model.CurrencyPosition
import com.blockveil.expensetracker.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Snapshot of every app-wide setting (matches the SETTINGS section of the CSV backup). */
data class AppSettings(
    val budget: Double = 0.0,
    val rollingEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCountry: String = "Bangladesh",
    val currencyPosition: CurrencyPosition = CurrencyPosition.PREFIX,
    val currencyFormat: CurrencyFormat = CurrencyFormat.GROUPED,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BUDGET = doublePreferencesKey("budget")
        val ROLLING_ENABLED = booleanPreferencesKey("rolling_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_COUNTRY = stringPreferencesKey("currency_country")
        val CURRENCY_POSITION = stringPreferencesKey("currency_position")
        val CURRENCY_FORMAT = stringPreferencesKey("currency_format")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            budget = prefs[Keys.BUDGET] ?: 0.0,
            rollingEnabled = prefs[Keys.ROLLING_ENABLED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]?.toEnumOrNull<ThemeMode>() ?: ThemeMode.SYSTEM,
            currencyCountry = prefs[Keys.CURRENCY_COUNTRY] ?: "Bangladesh",
            currencyPosition = prefs[Keys.CURRENCY_POSITION]?.toEnumOrNull<CurrencyPosition>()
                ?: CurrencyPosition.PREFIX,
            currencyFormat = prefs[Keys.CURRENCY_FORMAT]?.toEnumOrNull<CurrencyFormat>()
                ?: CurrencyFormat.GROUPED,
        )
    }

    suspend fun setBudget(value: Double) {
        context.settingsDataStore.edit { it[Keys.BUDGET] = value }
    }

    suspend fun setRollingEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ROLLING_ENABLED] = value }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setCurrencyCountry(value: String) {
        context.settingsDataStore.edit { it[Keys.CURRENCY_COUNTRY] = value }
    }

    suspend fun setCurrencyPosition(value: CurrencyPosition) {
        context.settingsDataStore.edit { it[Keys.CURRENCY_POSITION] = value.name }
    }

    suspend fun setCurrencyFormat(value: CurrencyFormat) {
        context.settingsDataStore.edit { it[Keys.CURRENCY_FORMAT] = value.name }
    }
}

// Guards against a corrupted/outdated stored name crashing the app on read.
private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    runCatching { enumValueOf<T>(this) }.getOrNull()
