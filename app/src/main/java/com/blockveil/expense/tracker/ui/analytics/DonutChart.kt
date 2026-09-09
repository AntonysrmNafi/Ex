package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * A ring chart: a faint full-circle track, then one colored arc per segment, each sized
 * proportionally to its share of the total. Segments start at 12 o'clock and go clockwise.
 *
 * Matches DonutChart in spirit, but uses Compose's native drawArc (startAngle/sweepAngle)
 * instead of the source design's SVG strokeDasharray/strokeDashoffset trick, since Canvas
 * arcs are the direct, simpler equivalent on Android.
 */
@Composable
fun DonutChart(
    segments: List<Pair<Color, Double>>,
    modifier: Modifier = Modifier,
    diameter: Dp = 148.dp,
    thickness: Dp = 20.dp,
) {
    Canvas(modifier = modifier.size(diameter)) {
        val strokeWidthPx = thickness.toPx()
        val arcDiameter = min(size.width, size.height) - strokeWidthPx
        val topLeft = Offset((size.width - arcDiameter) / 2f, (size.height - arcDiameter) / 2f)
        val arcSize = Size(arcDiameter, arcDiameter)

        drawArc(
            color = Color.Gray.copy(alpha = 0.15f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx),
            topLeft = topLeft,
            size = arcSize,
        )

        val total = segments.sumOf { it.second }
        if (total > 0) {
            var cumulativeDegrees = -90f // start at 12 o'clock, matches the source design's -rotate-90
            segments.forEach { (color, value) ->
                val sweep = (value / total * 360.0).toFloat()
                if (sweep > 0f) {
                    drawArc(
                        color = color,
                        startAngle = cumulativeDegrees,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }
                cumulativeDegrees += sweep
            }
        }
    }
}
