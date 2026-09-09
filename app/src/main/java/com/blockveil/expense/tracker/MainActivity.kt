package com.blockveil.expense.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.installSplashScreen
import androidx.core.view.WindowCompat
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.ui.analytics.AnalyticsRoute
import com.blockveil.expense.tracker.ui.components.LocalAppFeedback
import com.blockveil.expense.tracker.ui.components.MoneyBurstHost
import com.blockveil.expense.tracker.ui.components.ToastHost
import com.blockveil.expense.tracker.ui.components.rememberAppFeedbackState
import com.blockveil.expense.tracker.ui.drilldown.AccountHistoryRoute
import com.blockveil.expense.tracker.ui.drilldown.CategoryHistoryRoute
import com.blockveil.expense.tracker.ui.history.HistoryRoute
import com.blockveil.expense.tracker.ui.home.HomeRoute
import com.blockveil.expense.tracker.ui.more.MoreRoute
import com.blockveil.expense.tracker.ui.navigation.AppScaffold
import com.blockveil.expense.tracker.ui.navigation.AppTab
import com.blockveil.expense.tracker.ui.settings.SettingsGearButton
import com.blockveil.expense.tracker.ui.settings.SettingsRoute
import com.blockveil.expense.tracker.ui.theme.DarkBackground
import com.blockveil.expense.tracker.ui.theme.ExpenseTrackerTheme
import com.blockveil.expense.tracker.ui.theme.LightBackground
import com.blockveil.expense.tracker.ui.transaction.TransactionFormScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val app = LocalContext.current.applicationContext as ExpenseTrackerApp
            val settings by app.container.settingsRepository.settings.collectAsState(initial = null)

            // Null on the very first frame before DataStore emits; system default avoids a flash of the wrong theme.
            val darkTheme = when (settings?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM, null -> isSystemInDarkTheme()
            }

            // Keeps the system status/nav bar in step with the app's own theme choice (which can
            // differ from the system setting, e.g. Theme Mode = Dark on a light-system phone),
            // updating live the moment the user changes it in Settings. Deliberately not
            // switching to edge-to-edge here: none of the existing screens' top headers apply a
            // status-bar inset, so drawing content under the bar would clip them. This just
            // recolors the bars the system already reserves space for.
            val view = LocalView.current
            SideEffect {
                val barColor = (if (darkTheme) DarkBackground else LightBackground).toArgb()
                window.statusBarColor = barColor
                window.navigationBarColor = barColor
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            ExpenseTrackerTheme(darkTheme = darkTheme) {
                AppRoot()
            }
        }
    }
}

/**
 * A pushed screen rendered full-bleed on top of the tab scaffold, matching how the source
 * design layers TransactionPage/CategoryHistoryScreen/AccountHistoryScreen/SettingsPage etc
 * above everything including the bottom nav. Kept as a simple sealed class swap rather than
 * pulling in Navigation Compose's NavHost, since there's only ever one screen pushed at a
 * time here (no deep back stacks to manage yet).
 */
private sealed class PushedScreen {
    data object None : PushedScreen()
    data object Settings : PushedScreen()
    data class TransactionForm(val existingId: Long?) : PushedScreen()
    data class AccountHistory(val accountId: Long) : PushedScreen()
    data class CategoryHistory(val category: String, val isIncome: Boolean) : PushedScreen()
}

@Composable
private fun AppRoot() {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var pushedScreen by remember { mutableStateOf<PushedScreen>(PushedScreen.None) }
    val feedback = rememberAppFeedbackState()

    val openTransaction = { id: Long? -> pushedScreen = PushedScreen.TransactionForm(existingId = id) }

    CompositionLocalProvider(LocalAppFeedback provides feedback) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppScaffold(selectedTab = selectedTab, onTabSelect = { selectedTab = it }) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    when (selectedTab) {
                        AppTab.HOME -> HomeRoute(
                            onTxnClick = { id -> openTransaction(id) },
                            onAccountClick = { id -> pushedScreen = PushedScreen.AccountHistory(accountId = id) },
                            onSeeAllHistory = { selectedTab = AppTab.HISTORY },
                            onAdd = { openTransaction(null) },
                        )
                        AppTab.HISTORY -> HistoryRoute(
                            onTxnClick = { id -> openTransaction(id) },
                        )
                        AppTab.ANALYTICS -> AnalyticsRoute()
                        AppTab.MORE -> MoreRoute(
                            onAccountClick = { id -> pushedScreen = PushedScreen.AccountHistory(accountId = id) },
                        )
                    }
                }
            }

            // Floating above every tab, matches the source design's fixed-position gear button.
            SettingsGearButton(
                onClick = { pushedScreen = PushedScreen.Settings },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp, end = 20.dp),
            )

            when (val screen = pushedScreen) {
                is PushedScreen.TransactionForm -> TransactionFormScreen(
                    existingId = screen.existingId,
                    onClose = { pushedScreen = PushedScreen.None },
                )
                is PushedScreen.AccountHistory -> AccountHistoryRoute(
                    accountId = screen.accountId,
                    onBack = { pushedScreen = PushedScreen.None },
                    onTxnClick = { id -> openTransaction(id) },
                )
                is PushedScreen.CategoryHistory -> CategoryHistoryRoute(
                    category = screen.category,
                    isIncome = screen.isIncome,
                    onBack = { pushedScreen = PushedScreen.None },
                    onTxnClick = { id -> openTransaction(id) },
                )
                PushedScreen.Settings -> SettingsRoute(onClose = { pushedScreen = PushedScreen.None })
                PushedScreen.None -> Unit
            }

            // Rendered last so both sit above every tab and every pushed screen, matching the
            // source design's toast/moneyAnim living at the very root of the component tree.
            MoneyBurstHost(trigger = feedback.moneyBurstTrigger, modifier = Modifier.fillMaxSize())
            ToastHost(
                message = feedback.toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp),
            )
        }
    }
}
