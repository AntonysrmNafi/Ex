package com.blockveil.expense.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom-center pill toast, matching the source design's `toast` state: dark pill, checkmark,
 * short confirmation text, auto-clears after ~1.8s ([AppFeedbackState.showToast] owns the
 * timing). Positioned above the bottom nav bar so it's never covered by it.
 *
 * `liveRegion = Polite` makes TalkBack announce new messages automatically without stealing
 * focus, standing in for a native Snackbar's built-in announcement behavior.
 */
@Composable
fun ToastHost(message: String?, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()

    // Keeps showing the last message text while the exit fade plays, instead of blanking to
    // empty text the instant `message` flips to null.
    var lastMessage by remember { mutableStateOf(message) }
    LaunchedEffect(message) { if (message != null) lastMessage = message }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = message != null,
            enter = if (reduceMotion) fadeIn(tween(0)) else fadeIn(tween(200)),
            exit = if (reduceMotion) fadeOut(tween(0)) else fadeOut(tween(200)),
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFF333333))
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(14.dp),
                )
                Text(
                    text = lastMessage.orEmpty(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
