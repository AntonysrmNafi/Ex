package com.blockveil.expensetracker.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A dashed rounded-rect border. Compose has no built-in equivalent to CSS's border-dashed,
 * this is the direct visual equivalent, used for the "+ Custom" category chip.
 */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 3.dp,
): Modifier = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f),
        ),
    )
}
