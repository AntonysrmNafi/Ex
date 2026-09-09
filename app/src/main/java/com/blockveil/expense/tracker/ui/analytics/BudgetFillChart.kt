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
 * A ring where each category gets a slice sized by its share of the total limit across all
 * budgeted categories; within each slice, a solid sub-arc shows how much of that category's
 * own limit has been used so far (capped at 100%, an over-budget category doesn't overflow
 * into the next slice). Matches BudgetFillChart, using Canvas's native drawArc instead of
 * the source design's SVG strokeDasharray/strokeDashoffset technique.
 */
@Composable
fun BudgetFillChart(
    categories: List<BudgetCategoryUsage>,
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

        val totalLimit = categories.sumOf { it.limit }
        if (totalLimit > 0) {
            var cumulativeDegrees = -90f
            categories.forEach { category ->
                val segmentSweep = (category.limit / totalLimit * 360.0).toFloat()

                drawArc(
                    color = category.color.copy(alpha = 0.2f),
                    startAngle = cumulativeDegrees,
                    sweepAngle = segmentSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                    topLeft = topLeft,
                    size = arcSize,
                )

                val usedFraction = if (category.limit > 0) (category.spent / category.limit).coerceIn(0.0, 1.0) else 0.0
                val usedSweep = (usedFraction * segmentSweep).toFloat()
                if (usedSweep > 0f) {
                    drawArc(
                        color = category.color,
                        startAngle = cumulativeDegrees,
                        sweepAngle = usedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                }

                cumulativeDegrees += segmentSweep
            }
        }
    }
}
