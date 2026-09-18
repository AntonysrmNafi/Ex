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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blockveil.expense.tracker.ExpenseTrackerApp
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback
import com.blockveil.expense.tracker.util.EXPENSE_CATEGORIES
import com.blockveil.expense.tracker.util.INCOME_CATEGORIES
import com.blockveil.expense.tracker.ui.components.categoryIcon
import com.blockveil.expense.tracker.util.categoryColor
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Where SettingsRoute should land when it's pushed, e.g. jumping straight to Category
 *  Management from the transaction form's "Custom" tap instead of Settings' own front page. */
enum class SettingsEntryPoint {
    MAIN,
    CATEGORY_MANAGEMENT,
    SOURCE_MANAGEMENT,
}

private sealed class SettingsPage {
    data object Main : SettingsPage()
    data object Currency : SettingsPage()
    data object CurrencyPicker : SettingsPage()
    data object CategoryManagement : SettingsPage()
    data object SourceManagement : SettingsPage()
    data object AddCustomCategory : SettingsPage()
    data object AddCustomSource : SettingsPage()
    data object AccountManagement : SettingsPage()
    data object DataProtection : SettingsPage()
    data class Info(val key: InfoPageKey) : SettingsPage()
}

private fun SettingsEntryPoint.toInitialPage(): SettingsPage = when (this) {
    SettingsEntryPoint.MAIN -> SettingsPage.Main
    SettingsEntryPoint.CATEGORY_MANAGEMENT -> SettingsPage.CategoryManagement
    SettingsEntryPoint.SOURCE_MANAGEMENT -> SettingsPage.SourceManagement
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
fun SettingsRoute(onClose: () -> Unit, entryPoint: SettingsEntryPoint = SettingsEntryPoint.MAIN) {
    val app = LocalContext.current.applicationContext as ExpenseTrackerApp
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.container))
    val settings by viewModel.settings.collectAsState()
    val feedback = LocalAppFeedback.current

    var page by remember { mutableStateOf<SettingsPage>(entryPoint.toInitialPage()) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Mirrors the BackHandler in MainActivity.AppRoot one level down: back inside Settings
    // steps back through its own page stack first (CurrencyPicker -> Currency, either
    // AddCustom page -> its own Management page, anything else -> Main) before ever reaching
    // the outer handler that closes Settings itself.
    BackHandler(enabled = page != SettingsPage.Main) {
        page = when (page) {
            SettingsPage.CurrencyPicker -> SettingsPage.Currency
            SettingsPage.AddCustomCategory -> SettingsPage.CategoryManagement
            SettingsPage.AddCustomSource -> SettingsPage.SourceManagement
            else -> SettingsPage.Main
        }
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
                onOpenSourceManagement = { page = SettingsPage.SourceManagement },
                onOpenAccountManagement = { page = SettingsPage.AccountManagement },
                onOpenDataProtection = { page = SettingsPage.DataProtection },
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
            SettingsPage.CategoryManagement -> {
                val hiddenFixed = settings.hiddenFixedExpenseCategories
                val deletedFixed = settings.deletedFixedExpenseCategories
                val fixedModels = EXPENSE_CATEGORIES.filterNot { it in deletedFixed }.map { name ->
                    ManagedCategoryUiModel(
                        name = name,
                        color = categoryColor(false, name, expenseCategories, incomeCategories),
                        icon = categoryIcon(false, name, expenseCategories, incomeCategories),
                        isHidden = name in hiddenFixed,
                        isBuiltIn = true,
                    )
                }
                val customModels = expenseCategories.map { entity ->
                    ManagedCategoryUiModel(
                        name = entity.name,
                        color = Color(entity.color),
                        icon = categoryIcon(false, entity.name, expenseCategories, incomeCategories),
                        isHidden = entity.isHidden,
                        isBuiltIn = false,
                    )
                }
                CategoryManagementScreen(
                    categories = fixedModels + customModels,
                    onSetHidden = { model, hidden ->
                        if (model.isBuiltIn) {
                            viewModel.onSetFixedCategoryHidden(isIncome = false, name = model.name, hidden = hidden)
                        } else {
                            expenseCategories.firstOrNull { it.name == model.name }?.let { viewModel.onSetCategoryHidden(it, hidden) }
                        }
                    },
                    onDelete = { model ->
                        if (model.isBuiltIn) {
                            viewModel.onDeleteFixedCategory(isIncome = false, name = model.name)
                        } else {
                            expenseCategories.firstOrNull { it.name == model.name }?.let { viewModel.onDeleteCategory(it) }
                        }
                    },
                    onAddCustom = { page = SettingsPage.AddCustomCategory },
                    onBack = { page = SettingsPage.Main },
                )
            }
            SettingsPage.SourceManagement -> {
                val hiddenFixed = settings.hiddenFixedIncomeCategories
                val deletedFixed = settings.deletedFixedIncomeCategories
                val fixedModels = INCOME_CATEGORIES.filterNot { it in deletedFixed }.map { name ->
                    ManagedCategoryUiModel(
                        name = name,
                        color = categoryColor(true, name, expenseCategories, incomeCategories),
                        icon = categoryIcon(true, name, expenseCategories, incomeCategories),
                        isHidden = name in hiddenFixed,
                        isBuiltIn = true,
                    )
                }
                val customModels = incomeCategories.map { entity ->
                    ManagedCategoryUiModel(
                        name = entity.name,
                        color = Color(entity.color),
                        icon = categoryIcon(true, entity.name, expenseCategories, incomeCategories),
                        isHidden = entity.isHidden,
                        isBuiltIn = false,
                    )
                }
                SourceManagementScreen(
                    sources = fixedModels + customModels,
                    onSetHidden = { model, hidden ->
                        if (model.isBuiltIn) {
                            viewModel.onSetFixedCategoryHidden(isIncome = true, name = model.name, hidden = hidden)
                        } else {
                            incomeCategories.firstOrNull { it.name == model.name }?.let { viewModel.onSetCategoryHidden(it, hidden) }
                        }
                    },
                    onDelete = { model ->
                        if (model.isBuiltIn) {
                            viewModel.onDeleteFixedCategory(isIncome = true, name = model.name)
                        } else {
                            incomeCategories.firstOrNull { it.name == model.name }?.let { viewModel.onDeleteCategory(it) }
                        }
                    },
                    onAddCustom = { page = SettingsPage.AddCustomSource },
                    onBack = { page = SettingsPage.Main },
                )
            }
            SettingsPage.AddCustomCategory -> AddCustomCategoryScreen(
                isIncome = false,
                onCreate = { name, color, icon ->
                    viewModel.onCreateCategory(name, color, isIncome = false, icon = icon)
                    feedback.showToast("Category added")
                    page = SettingsPage.CategoryManagement
                },
                onBack = { page = SettingsPage.CategoryManagement },
            )
            SettingsPage.AddCustomSource -> AddCustomCategoryScreen(
                isIncome = true,
                onCreate = { name, color, icon ->
                    viewModel.onCreateCategory(name, color, isIncome = true, icon = icon)
                    feedback.showToast("Source added")
                    page = SettingsPage.SourceManagement
                },
                onBack = { page = SettingsPage.SourceManagement },
            )
            SettingsPage.AccountManagement -> AccountManagementScreen(
                accounts = accounts,
                currency = resolveCurrencyDisplay(settings),
                onDelete = viewModel::onDeleteAccount,
                onSetHidden = viewModel::onSetAccountHidden,
                onDeleteBlocked = { message -> feedback.showToast(message) },
                onBack = { page = SettingsPage.Main },
            )
            SettingsPage.DataProtection -> DataProtectionScreen(onBack = { page = SettingsPage.Main })
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
