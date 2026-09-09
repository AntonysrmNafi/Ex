package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandPrimary

/**
 * Monthly budget amount plus the rolling-budget toggle. Matches MonthlyBudgetCard,
 * including its pattern of keeping the text field as local, editable state that only
 * resyncs from [budget] when it changes elsewhere (so typing isn't interrupted by every
 * recomposition), and only actually saving when "Save" is pressed.
 */
@Composable
fun MonthlyBudgetCard(
    budget: Double,
    rollingEnabled: Boolean,
    onSetBudget: (Double) -> Unit,
    onSetRolling: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var budgetText by remember(budget) { mutableStateOf(formatPlainNumber(budget)) }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = "Monthly budget", icon = Icons.Filled.Wallet)
            Text(
                text = "Set to 0 to disable the budget alert.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            AppTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(text = "Rolling budget", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Carry unused (or overspent) budget forward",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                RollingToggle(checked = rollingEnabled, onToggle = { onSetRolling(!rollingEnabled) })
            }

            Button(
                onClick = {
                    val value = budgetText.toDoubleOrNull()
                    if (value != null && value >= 0) onSetBudget(value)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Save", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RollingToggle(checked: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) BrandPrimary else MaterialTheme.colorScheme.outline)
            .selectable(selected = checked, onClick = onToggle, role = Role.Switch)
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/** "450" for a whole number, "450.5" for a real decimal, close enough to String(budget) for editing. */
private fun formatPlainNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
