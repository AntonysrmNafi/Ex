package com.blockveil.expense.tracker.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.util.detectDefaultCurrencyCountry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Snapshot of every app-wide setting (matches the SETTINGS section of the CSV backup). */
data class AppSettings(
    val budget: Double = 0.0,
    val rollingEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCountry: String = detectDefaultCurrencyCountry(),
    val currencyPosition: CurrencyPosition = CurrencyPosition.PREFIX,
    val currencyFormat: CurrencyFormat = CurrencyFormat.GROUPED,
    // The 7 built-in categories/sources aren't rows in a table (unlike a custom one), so
    // hiding/deleting one is tracked here by name instead. Hidden or deleted either way drops
    // out of the picker for new transactions; only "deleted" also drops out of Category/Source
    // Management itself, since unlike hide it has no way back. Past transactions are entirely
    // unaffected either way: categoryColor()/categoryIcon() resolve a fixed name's color/icon
    // regardless of this state, exactly as they already do for a deleted custom category.
    val hiddenFixedExpenseCategories: Set<String> = emptySet(),
    val hiddenFixedIncomeCategories: Set<String> = emptySet(),
    val deletedFixedExpenseCategories: Set<String> = emptySet(),
    val deletedFixedIncomeCategories: Set<String> = emptySet(),
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BUDGET = doublePreferencesKey("budget")
        val ROLLING_ENABLED = booleanPreferencesKey("rolling_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_COUNTRY = stringPreferencesKey("currency_country")
        val CURRENCY_POSITION = stringPreferencesKey("currency_position")
        val CURRENCY_FORMAT = stringPreferencesKey("currency_format")
        val HIDDEN_FIXED_EXPENSE = stringSetPreferencesKey("hidden_fixed_expense")
        val HIDDEN_FIXED_INCOME = stringSetPreferencesKey("hidden_fixed_income")
        val DELETED_FIXED_EXPENSE = stringSetPreferencesKey("deleted_fixed_expense")
        val DELETED_FIXED_INCOME = stringSetPreferencesKey("deleted_fixed_income")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            budget = prefs[Keys.BUDGET] ?: 0.0,
            rollingEnabled = prefs[Keys.ROLLING_ENABLED] ?: false,
            themeMode = prefs[Keys.THEME_MODE]?.toEnumOrNull<ThemeMode>() ?: ThemeMode.SYSTEM,
            currencyCountry = prefs[Keys.CURRENCY_COUNTRY] ?: detectDefaultCurrencyCountry(),
            currencyPosition = prefs[Keys.CURRENCY_POSITION]?.toEnumOrNull<CurrencyPosition>()
                ?: CurrencyPosition.PREFIX,
            currencyFormat = prefs[Keys.CURRENCY_FORMAT]?.toEnumOrNull<CurrencyFormat>()
                ?: CurrencyFormat.GROUPED,
            hiddenFixedExpenseCategories = prefs[Keys.HIDDEN_FIXED_EXPENSE] ?: emptySet(),
            hiddenFixedIncomeCategories = prefs[Keys.HIDDEN_FIXED_INCOME] ?: emptySet(),
            deletedFixedExpenseCategories = prefs[Keys.DELETED_FIXED_EXPENSE] ?: emptySet(),
            deletedFixedIncomeCategories = prefs[Keys.DELETED_FIXED_INCOME] ?: emptySet(),
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

    suspend fun setFixedCategoryHidden(isIncome: Boolean, name: String, hidden: Boolean) {
        val key = if (isIncome) Keys.HIDDEN_FIXED_INCOME else Keys.HIDDEN_FIXED_EXPENSE
        context.settingsDataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (hidden) current + name else current - name
        }
    }

    suspend fun deleteFixedCategory(isIncome: Boolean, name: String) {
        val key = if (isIncome) Keys.DELETED_FIXED_INCOME else Keys.DELETED_FIXED_EXPENSE
        context.settingsDataStore.edit { prefs ->
            prefs[key] = (prefs[key] ?: emptySet()) + name
        }
    }
}

// Guards against a corrupted/outdated stored name crashing the app on read.
private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    runCatching { enumValueOf<T>(this) }.getOrNull()
