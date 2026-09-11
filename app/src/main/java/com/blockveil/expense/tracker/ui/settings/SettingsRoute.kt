package com.blockveil.expense.tracker.ui.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private sealed class SettingsPage {
    data object Main : SettingsPage()
    data object Currency : SettingsPage()
    data object CurrencyPicker : SettingsPage()
    data object CategoryManagement : SettingsPage()
    data object AccountManagement : SettingsPage()
    data class Info(val key: InfoPageKey) : SettingsPage()
}

/**
 * Wires [SettingsViewModel] to the Settings/Currency/CurrencyPicker/Info pages, and owns
 * the backup export, restore import, and clear-data confirm flows. Matches the source
 * design's internal `view` state machine (settings/currency/currency-picker/info) as a
 * sealed class swap, same pattern used for the top-level pushed screens in MainActivity.
 *
 * Feedback (backup/restore/clear results) goes through the app-wide [LocalAppFeedback] toast
 * rather than a local Scaffold Snackbar, since "All data cleared" and friends need to survive
 * this screen closing immediately after, since a local SnackbarHost would vanish with it.
 */
@Composable
fun SettingsRoute(onClose: () -> Unit) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.container))
    val settings by viewModel.settings.collectAsState()
    val feedback = LocalAppFeedback.current

    var page by remember { mutableStateOf<SettingsPage>(SettingsPage.Main) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Mirrors the BackHandler in MainActivity.AppRoot one level down: back inside Settings
    // steps back through its own page stack first (CurrencyPicker -> Currency -> Main, or any
    // other sub-page -> Main) before ever reaching the outer handler that closes Settings itself.
    BackHandler(enabled = page != SettingsPage.Main) {
        page = if (page == SettingsPage.CurrencyPicker) SettingsPage.Currency else SettingsPage.Main
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val csv = viewModel.exportBackup()
            val wrote = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
            }.isSuccess
            feedback.showToast(if (wrote) "Backup saved" else "Couldn't save the backup")
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                }
            }.getOrNull()
            if (text == null) {
                feedback.showToast("Couldn't read that file")
            } else {
                val error = viewModel.restoreBackup(text)
                feedback.showToast(error ?: "Backup restored")
                if (error == null) onClose()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val current = page) {
            SettingsPage.Main -> SettingsScreen(
                themeMode = settings.themeMode,
                onSetThemeMode = viewModel::onSetThemeMode,
                currencyCountry = settings.currencyCountry,
                currencyPosition = settings.currencyPosition,
                onOpenCurrency = { page = SettingsPage.Currency },
                onOpenCategoryManagement = { page = SettingsPage.CategoryManagement },
                onOpenAccountManagement = { page = SettingsPage.AccountManagement },
                onBackup = {
                    val fileName = "blockveil-backup-${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.csv"
                    backupLauncher.launch(fileName)
                },
                onRestore = { restoreLauncher.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "*/*")) },
                onClearAllData = { showClearConfirm = true },
                onOpenInfoPage = { key -> page = SettingsPage.Info(key) },
                onClose = onClose,
            )
            SettingsPage.Currency -> CurrencySettingsScreen(
                currencyCountry = settings.currencyCountry,
                currencyPosition = settings.currencyPosition,
                currencyFormat = settings.currencyFormat,
                onOpenPicker = { page = SettingsPage.CurrencyPicker },
                onSetPosition = viewModel::onSetCurrencyPosition,
                onSetFormat = viewModel::onSetCurrencyFormat,
                onBack = { page = SettingsPage.Main },
            )
            SettingsPage.CurrencyPicker -> CurrencyPickerScreen(
                selectedCountry = settings.currencyCountry,
                onSelect = { country ->
                    viewModel.onSetCurrencyCountry(country)
                    page = SettingsPage.Currency
                },
                onBack = { page = SettingsPage.Currency },
            )
            is SettingsPage.Info -> INFO_PAGES[current.key]?.let { content ->
                InfoScreen(content = content, onBack = { page = SettingsPage.Main })
            }
            SettingsPage.CategoryManagement -> CategoryManagementScreen(
                expenseCategories = expenseCategories,
                incomeCategories = incomeCategories,
                onDelete = viewModel::onDeleteCategory,
                onBack = { page = SettingsPage.Main },
            )
            SettingsPage.AccountManagement -> AccountManagementScreen(
                accounts = accounts,
                currency = resolveCurrencyDisplay(settings),
                onDelete = viewModel::onDeleteAccount,
                onSetHidden = viewModel::onSetAccountHidden,
                onDeleteBlocked = { message -> feedback.showToast(message) },
                onBack = { page = SettingsPage.Main },
            )
        }
    }

    if (showClearConfirm) {
        ConfirmDialog(
            title = "Clear all data?",
            message = "This permanently deletes all transactions, accounts, subscriptions, goals, and transfers. This can't be undone.",
            confirmLabel = "Clear everything",
            onConfirm = {
                showClearConfirm = false
                scope.launch {
                    viewModel.clearAllData()
                    feedback.showToast("All data cleared")
                    onClose()
                }
            },
            onCancel = { showClearConfirm = false },
        )
    }
}
