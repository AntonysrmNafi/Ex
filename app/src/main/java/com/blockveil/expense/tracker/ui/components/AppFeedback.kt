package com.blockveil.expense.tracker.ui.components

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.blockveil.expense.tracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ToastDurationMs = 1800L

/**
 * Owns the transient, app-wide feedback the source design keeps at its root (`toast` and
 * `moneyAnim`), plus the transaction success sound (not in the source design; requested
 * separately). Hoisted once in [MainActivity]'s AppRoot and read via [LocalAppFeedback], so
 * any route several layers deep (a delete-confirm in More, a save in the transaction form)
 * can trigger these without every intermediate composable threading callbacks through.
 */
class AppFeedbackState(private val scope: CoroutineScope, private val appContext: Context) {
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

    /**
     * Plays transection_music.mp3 once, for a successfully added transaction (income, expense,
     * transfer, or loan repayment) from the '+' button, per the explicit request. A fresh
     * MediaPlayer is created and released on completion each call rather than kept around,
     * since this fires at most a few times a session, not rapidly enough for that cost to matter.
     */
    fun playTransactionSound() {
        try {
            val player = MediaPlayer.create(appContext, R.raw.transection_music) ?: return
            player.setOnCompletionListener { it.release() }
            player.start()
        } catch (e: Exception) {
            // Playback failing (e.g. no audio output available) shouldn't block or crash the save flow.
        }
    }
}

@Composable
fun rememberAppFeedbackState(): AppFeedbackState {
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current.applicationContext
    return remember { AppFeedbackState(scope, appContext) }
}

val LocalAppFeedback = staticCompositionLocalOf<AppFeedbackState> {
    error("LocalAppFeedback not provided (wrap the composition root in CompositionLocalProvider(LocalAppFeedback provides ...))")
}
