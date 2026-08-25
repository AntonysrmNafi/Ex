package com.blockveil.expensetracker.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.util.formatDateDisplay
import com.blockveil.expensetracker.util.monthLabel
import java.time.LocalDate
import java.time.YearMonth

private enum class FilterOption { TODAY, WEEK, MONTH, YEAR, ALL, CUSTOM }

private fun DateFilter.toOption(): FilterOption = when (this) {
    DateFilter.Today -> FilterOption.TODAY
    DateFilter.Week -> FilterOption.WEEK
    DateFilter.Month -> FilterOption.MONTH
    DateFilter.Year -> FilterOption.YEAR
    DateFilter.All -> FilterOption.ALL
    is DateFilter.Custom -> FilterOption.CUSTOM
}

private data class FilterOptionRow(val option: FilterOption, val label: String, val sub: String?)

/**
 * Bottom sheet with 5 quick date-range picks plus a Custom from/to range. Matches
 * DateFilterSheet. Uses Material 3's ModalBottomSheet as the slide-up container instead of
 * the source design's custom CSS sheet + backdrop, since that's exactly the built-in
 * behavior ModalBottomSheet already provides on Android (drag-to-dismiss, scrim, etc).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterSheet(
    current: DateFilter,
    currentMonth: YearMonth,
    onApply: (DateFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    var selectedOption by remember { mutableStateOf(current.toOption()) }
    var customFrom by remember { mutableStateOf((current as? DateFilter.Custom)?.from ?: today) }
    var customTo by remember { mutableStateOf((current as? DateFilter.Custom)?.to ?: today) }
    var datePickerFor by remember { mutableStateOf<String?>(null) } // "from" | "to" | null

    val weekStart = today.minusDays(today.dayOfWeek.value % 7L)
    val weekEnd = weekStart.plusDays(6)

    val options = listOf(
        FilterOptionRow(FilterOption.TODAY, "Today", formatDateDisplay(today)),
        FilterOptionRow(FilterOption.WEEK, "This Week", "${formatDateDisplay(weekStart)} - ${formatDateDisplay(weekEnd)}"),
        FilterOptionRow(FilterOption.MONTH, "This Month", monthLabel(currentMonth)),
        FilterOptionRow(FilterOption.YEAR, "This Year", today.year.toString()),
        FilterOptionRow(FilterOption.ALL, "All time", null),
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(
                text = "FILTER BY DATE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            options.forEach { row ->
                val isSelected = selectedOption == row.option
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = isSelected, onClick = { selectedOption = row.option }, role = Role.RadioButton)
                        .padding(vertical = 10.dp),
                ) {
                    Text(
                        text = row.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                    row.sub?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            val customSelected = selectedOption == FilterOption.CUSTOM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (customSelected) BrandPrimary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .selectable(selected = customSelected, onClick = { selectedOption = FilterOption.CUSTOM }, role = Role.RadioButton)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Custom",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (customSelected) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomDateField(
                        label = "From Date",
                        date = customFrom,
                        onClick = { datePickerFor = "from" },
                        modifier = Modifier.weight(1f),
                    )
                    CustomDateField(
                        label = "To Date",
                        date = customTo,
                        onClick = { datePickerFor = "to" },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                ) {
                    Text(text = "Cancel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val resolved = when (selectedOption) {
                            FilterOption.TODAY -> DateFilter.Today
                            FilterOption.WEEK -> DateFilter.Week
                            FilterOption.MONTH -> DateFilter.Month
                            FilterOption.YEAR -> DateFilter.Year
                            FilterOption.ALL -> DateFilter.All
                            FilterOption.CUSTOM -> DateFilter.Custom(customFrom, customTo)
                        }
                        onApply(resolved)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text(text = "Select", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    val pickerTarget = datePickerFor
    if (pickerTarget != null) {
        CalendarDialog(
            initialDate = if (pickerTarget == "from") customFrom else customTo,
            onCancel = { datePickerFor = null },
            onSelect = { picked ->
                if (pickerTarget == "from") customFrom = picked else customTo = picked
                datePickerFor = null
            },
        )
    }
}

@Composable
private fun CustomDateField(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp),
            )
            Text(text = formatDateDisplay(date), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
