package com.blockveil.expense.tracker.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android has no direct equivalent of the web's `prefers-reduced-motion`, so this reads the
 * same signal the OS itself uses to skip animations: the Settings.Global animator duration
 * scale (Settings > Accessibility > Remove animations sets this to 0). Matches the source
 * design's `usePrefersReducedMotion()`, gating the same three effects it does: ConfirmDialog's
 * pop-in, the toast's fade-in, and the money-burst overlay (skipped entirely).
 *
 * Re-read on every recomposition of the call site rather than observed live, since this is a
 * rarely-changed system setting and none of its three call sites re-render often enough for
 * that gap to matter in practice.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale == 0f
}
