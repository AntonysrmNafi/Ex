package com.blockveil.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

/**
 * Prev/next arrows around the currently selected month, e.g. "< July 2026 >". Matches
 * MonthSwitcher, but takes a real [YearMonth] instead of the source design's integer offset
 * counted from a frozen base date, since the real app has no frozen "today".
 */
@Composable
fun MonthSwitcher(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = yearMonth.format(MONTH_LABEL_FORMATTER),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
