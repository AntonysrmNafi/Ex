package com.blockveil.expensetracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HEADER_WEEKDAY_FORMATTER = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
private val HEADER_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val WEEKDAY_LETTERS = listOf("S", "M", "T", "W", "T", "F", "S")

/**
 * A single-date picker: big header showing the selection, a month grid below it. Matches
 * CalendarDialog exactly rather than using Material's default DatePicker, since the source
 * design's layout (big date header, Sunday-start grid, today ring, custom Cancel/OK row)
 * doesn't resemble Material's stock picker at all.
 */
@Composable
fun CalendarDialog(
    initialDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onCancel: () -> Unit,
) {
    val today = LocalDate.now()
    var viewMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selected by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onCancel) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Text(
                        text = selected.year.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${selected.format(HEADER_WEEKDAY_FORMATTER)}, ${selected.format(HEADER_DATE_FORMATTER)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { viewMonth = viewMonth.minusMonths(1) },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = monthLabel(viewMonth),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable {
                                viewMonth = YearMonth.from(today)
                                selected = today
                            },
                        )
                        IconButton(
                            onClick = { viewMonth = viewMonth.plusMonths(1) },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        WEEKDAY_LETTERS.forEach { letter ->
                            Text(
                                text = letter,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    val firstDow = viewMonth.atDay(1).dayOfWeek.value % 7 // Sunday-start, matches Date.getDay()
                    val daysInMonth = viewMonth.lengthOfMonth()
                    val cells = buildList {
                        repeat(firstDow) { add(null) }
                        for (d in 1..daysInMonth) add(d)
                        while (size % 7 != 0) add(null)
                    }

                    cells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            week.forEach { day ->
                                CalendarDayCell(
                                    day = day,
                                    isSelected = day != null && YearMonth.from(selected) == viewMonth && selected.dayOfMonth == day,
                                    isToday = day != null && YearMonth.from(today) == viewMonth && today.dayOfMonth == day,
                                    onClick = { if (day != null) selected = viewMonth.atDay(day) },
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onCancel),
                    )
                    Text(
                        text = "OK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandPrimary,
                        modifier = Modifier
                            .padding(start = 24.dp)
                            .clickable { onSelect(selected) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
        if (day != null) {
            val description = if (isToday) "$day, today" else day.toString()
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .then(
                        when {
                            isSelected -> Modifier.background(BrandPrimary)
                            isToday -> Modifier.border(width = 1.5.dp, color = BrandPrimary, shape = CircleShape)
                            else -> Modifier
                        },
                    )
                    .clickable(onClickLabel = description, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        Color.White
                    } else if (isToday) {
                        BrandPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
    }
}
