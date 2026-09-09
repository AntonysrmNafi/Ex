package com.blockveil.expense.tracker.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.theme.BrandPrimary

/**
 * The new-goal input card: name, target amount, months, and an Add goal button. Matches
 * the add-goal Card at the top of GoalsSection exactly, including its validation
 * (non-blank name, target > 0, months > 0) before clearing the fields.
 */
@Composable
fun AddGoalCard(
    onAddGoal: (name: String, target: Double, months: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Goal name",
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppTextField(
                    value = target,
                    onValueChange = { target = it },
                    placeholder = "Target amount",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = months,
                    onValueChange = { months = it },
                    placeholder = "Months",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.width(96.dp),
                )
            }
            Button(
                onClick = {
                    val targetValue = target.toDoubleOrNull()
                    val monthsValue = months.toIntOrNull()
                    if (name.isNotBlank() && targetValue != null && targetValue > 0 && monthsValue != null && monthsValue > 0) {
                        onAddGoal(name.trim(), targetValue, monthsValue)
                        name = ""
                        target = ""
                        months = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Add goal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
