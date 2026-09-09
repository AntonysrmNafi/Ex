package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.CalendarDialog
import com.blockveil.expense.tracker.ui.components.ChipRow
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.formatDateDisplay
import java.time.LocalDate

/**
 * The add-subscription card: name, cycle chips, amount, a start/end date range for cycles
 * that need one, an account picker, an error line, and an Add button. Matches
 * AddSubscriptionCard exactly, including only asking for a date range on 3 Day/Weekly/
 * Monthly/Yearly cycles (Day and One time skip it).
 */
@Composable
fun AddSubscriptionCard(
    savingsAccounts: List<AccountEntity>,
    onAddSubscription: (name: String, amount: String, cycle: SubscriptionCycle, accountId: Long?, startDate: LocalDate?, endDate: LocalDate?) -> String?,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf(SubscriptionCycle.MONTHLY) }
    var accountId by remember { mutableStateOf(savingsAccounts.firstOrNull()?.id) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var datePickerFor by remember { mutableStateOf<String?>(null) } // "start" | "end" | null

    val effectiveAccountId = accountId?.takeIf { id -> savingsAccounts.any { it.id == id } } ?: savingsAccounts.firstOrNull()?.id

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            AppTextField(
                value = name,
                onValueChange = { name = it; error = null },
                placeholder = "Subscription name",
                modifier = Modifier.padding(bottom = 8.dp),
            )

            ChipRow(
                options = SubscriptionCycle.entries,
                getKey = { it.name },
                getLabel = { it.label },
                selectedKey = cycle.name,
                onSelect = { key ->
                    cycle = SubscriptionCycle.valueOf(key)
                    if (!cycle.needsDateRange) {
                        startDate = null
                        endDate = null
                    }
                    error = null
                },
                modifier = Modifier.padding(bottom = 8.dp),
            )

            AppTextField(
                value = amount,
                onValueChange = { amount = it; error = null },
                placeholder = "Amount",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            if (cycle.needsDateRange) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DateField(
                        label = "Start date",
                        date = startDate,
                        onClick = { datePickerFor = "start" },
                        modifier = Modifier.weight(1f),
                    )
                    DateField(
                        label = "End date",
                        date = endDate,
                        onClick = { datePickerFor = "end" },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (savingsAccounts.isEmpty()) {
                Text(
                    text = "Add a savings account first to deduct charges from.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            } else {
                ChipRow(
                    options = savingsAccounts,
                    getKey = { it.id.toString() },
                    getLabel = { it.name },
                    selectedKey = effectiveAccountId?.toString() ?: "",
                    onSelect = { key -> accountId = key.toLongOrNull(); error = null },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            error?.let {
                Text(text = it, fontSize = 10.sp, color = BrandDanger, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    val result = onAddSubscription(name, amount, cycle, effectiveAccountId, startDate, endDate)
                    if (result == null) {
                        name = ""
                        amount = ""
                        cycle = SubscriptionCycle.MONTHLY
                        startDate = null
                        endDate = null
                        error = null
                    } else {
                        error = result
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Add subscription", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    val pickerTarget = datePickerFor
    if (pickerTarget != null) {
        CalendarDialog(
            initialDate = (if (pickerTarget == "start") startDate else endDate) ?: LocalDate.now(),
            onSelect = { picked ->
                if (pickerTarget == "start") startDate = picked else endDate = picked
                datePickerFor = null
            },
            onCancel = { datePickerFor = null },
        )
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            Text(
                text = date?.let { formatDateDisplay(it) } ?: "Select",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (date != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
