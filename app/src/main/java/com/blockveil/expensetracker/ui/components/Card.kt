package com.blockveil.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CardShape = RoundedCornerShape(16.dp)
private val CardPadding = PaddingValues(14.dp)

/**
 * The app's standard surface card: 16dp rounded corners, 14dp padding, a faint drop shadow.
 * Matches the Card component in the source design (rounded-2xl, p-3.5, shadow-[0_1px_3px...]).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = CardPadding,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = CardShape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f),
            )
            .clip(CardShape)
            .background(backgroundColor)
            .padding(contentPadding),
    ) {
        content()
    }
}
