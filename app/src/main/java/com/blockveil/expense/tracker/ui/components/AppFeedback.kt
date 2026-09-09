package com.blockveil.expense.tracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ToastDurationMs = 1800L

/**
 * Owns the two pieces of transient, app-wide feedback the source design keeps at its root
 * (`toast` and `moneyAnim`): a short confirmation message and a "play the money-burst now"
 * signal. Hoisted once in [MainActivity]'s AppRoot and read via [LocalAppFeedback], so any
 * route several layers deep (a delete-confirm in More, a save in the transaction form) can
 * trigger both without every intermediate composable threading callbacks through.
 */
class AppFeedbackState(private val scope: CoroutineScope) {
    var toastMessage by mutableStateOf<String?>(null)
        private set

    /** 0L means "no burst pending"; any other value is a fresh trigger, matching `moneyAnim`'s Date.now() key. */
    var moneyBurstTrigger by mutableStateOf(0L)
        private set

    private var toastJob: Job? = null

    fun showToast(message: String) {
        toastMessage = message
        toastJob?.cancel()
        toastJob = scope.launch {
            delay(ToastDurationMs)
            toastMessage = null
        }
    }

    fun triggerMoneyBurst() {
        moneyBurstTrigger = System.currentTimeMillis()
    }
}

@Composable
fun rememberAppFeedbackState(): AppFeedbackState {
    val scope = rememberCoroutineScope()
    return remember { AppFeedbackState(scope) }
}

val LocalAppFeedback = staticCompositionLocalOf<AppFeedbackState> {
    error("LocalAppFeedback not provided (wrap the composition root in CompositionLocalProvider(LocalAppFeedback provides ...))")
}
