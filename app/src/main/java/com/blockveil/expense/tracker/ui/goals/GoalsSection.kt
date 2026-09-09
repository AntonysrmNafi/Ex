package com.blockveil.expense.tracker.ui.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.util.CurrencyDisplay

/**
 * Goals section: header, the add-goal card, then every existing goal (or an empty-state
 * message). Matches GoalsSection exactly. Designed to be dropped into the More tab once
 * it's built (Bag 15/16), alongside Accounts and Subscriptions, same as the source design.
 */
@Composable
fun GoalsSection(
    goals: List<GoalEntity>,
    currency: CurrencyDisplay,
    onAddGoal: (name: String, target: Double, months: Int) -> Unit,
    onContribute: (GoalEntity, Double) -> Unit,
    onDeleteRequest: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Goals", icon = Icons.Filled.TrackChanges, modifier = Modifier.padding(bottom = 4.dp))

        AddGoalCard(onAddGoal = onAddGoal, modifier = Modifier.padding(bottom = 10.dp))

        if (goals.isEmpty()) {
            Text(
                text = "No savings goals yet, add one above.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        } else {
            goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    currency = currency,
                    onContribute = { amount -> onContribute(goal, amount) },
                    onDeleteRequest = { onDeleteRequest(goal) },
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }
    }
}
