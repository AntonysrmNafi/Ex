package com.blockveil.expense.tracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val Emojis = listOf("\uD83D\uDCB5", "\uD83D\uDCB8", "\uD83E\uDE99", "\uD83D\uDCB0") // 💵 💸 🪙 💰
private const val ParticleCount = 10
private const val VisibleDurationMs = 1300L

private data class BurstParticle(
    val emoji: String,
    val leftFraction: Float, // 0f..1f across the container width
    val delayMs: Long,
    val durationMs: Int,
    val rotationDeg: Float,
    val sizeSp: Float,
)

private fun randomParticles(): List<BurstParticle> = List(ParticleCount) { i ->
    BurstParticle(
        emoji = Emojis[i % Emojis.size],
        leftFraction = (Random.nextFloat() * 0.88f) + 0.04f,
        delayMs = (Random.nextFloat() * 250).toLong(),
        durationMs = 900 + (Random.nextFloat() * 500).toInt(),
        rotationDeg = (Random.nextFloat() - 0.5f) * 360f,
        sizeSp = 20f + Random.nextFloat() * 14f,
    )
}

/**
 * Call whenever [trigger] changes to a non-zero value (e.g. from [AppFeedbackState.triggerMoneyBurst])
 * to play one burst of falling money emoji, matching the source design's `moneyAnim` key +
 * 1300ms auto-clear. Renders nothing when reduce-motion is on, matching `triggerMoneyAnim`'s own
 * `if (reduceMotion) return` guard, and is marked invisible to accessibility services since it's
 * purely decorative celebration, not content a screen reader user needs announced.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoneyBurstHost(trigger: Long, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    var activeKey by remember { mutableStateOf(0L) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(trigger, reduceMotion) {
        if (trigger == 0L || reduceMotion) return@LaunchedEffect
        activeKey = trigger
        visible = true
        delay(VisibleDurationMs)
        visible = false
    }

    if (visible) {
        key(activeKey) {
            BoxWithConstraints(modifier = modifier.semantics { invisibleToUser() }) {
                val containerWidth = maxWidth
                val fallDistance = maxHeight + 80.dp
                val particles = remember(activeKey) { randomParticles() }
                particles.forEach { particle ->
                    BurstParticleView(particle = particle, containerWidth = containerWidth, fallDistance = fallDistance)
                }
            }
        }
    }
}

@Composable
private fun BurstParticleView(particle: BurstParticle, containerWidth: Dp, fallDistance: Dp) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(particle) {
        delay(particle.delayMs)
        started = true
    }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = particle.durationMs, easing = LinearEasing),
        label = "moneyBurstFall",
    )

    val startX = containerWidth * particle.leftFraction
    // progress 0 -> just above the top edge (-40dp); progress 1 -> past the bottom edge (fallDistance - 40dp).
    val offsetY = fallDistance * progress - 40.dp

    Box(
        modifier = Modifier
            .offset(x = startX, y = offsetY)
            .alpha(1f - progress)
            .rotate(particle.rotationDeg * progress),
    ) {
        BasicText(text = particle.emoji, style = TextStyle(fontSize = particle.sizeSp.sp))
    }
}
