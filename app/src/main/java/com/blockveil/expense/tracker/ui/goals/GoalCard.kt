package com.blockveil.expense.tracker.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney
import kotlin.math.max
import kotlin.math.min

/**
 * One savings goal: progress bar, "save about X/month" guidance (or a reached message),
 * a contribute field, and a delete button. Matches GoalCard exactly.
 */
@Composable
fun GoalCard(
    goal: GoalEntity,
    currency: CurrencyDisplay,
    onContribute: (Double) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var amountText by remember { mutableStateOf("") }

    val progress = if (goal.targetAmount > 0) min(goal.savedAmount / goal.targetAmount, 1.0).toFloat() else 0f
    val monthlyTarget = max((goal.targetAmount - goal.savedAmount) / max(goal.targetMonths, 1), 0.0)
    val reached = goal.savedAmount >= goal.targetAmount

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = goal.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${formatMoney(goal.savedAmount, currency)} / ${formatMoney(goal.targetAmount, currency)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(50))
                        .background(BrandPrimary),
                )
            }

            Text(
                text = if (reached) {
                    "Goal reached, nice work!"
                } else {
                    "Save about ${formatMoney(monthlyTarget, currency)}/month to hit this in ${goal.targetMonths} months"
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = "Add amount",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        val value = amountText.toDoubleOrNull()
                        if (value != null && value > 0) {
                            onContribute(value)
                            amountText = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text(text = "Add", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(onClick = onDeleteRequest, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = "Delete goal", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = BrandDanger)
            }
        }
    }
}
