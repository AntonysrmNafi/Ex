package com.blockveil.expensetracker.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme

/**
 * 2x2 grid of transaction type buttons (Income/Expense/Transfer/Repay Loan). When [locked]
 * is true (editing an existing transaction), every non-selected button is disabled and
 * dimmed, matching the source design's `locked = !!existing` behavior: you can't change an
 * existing entry's type, only its details.
 */
@Composable
fun TransactionTypeSelector(
    selected: TransactionFormType,
    locked: Boolean,
    onSelect: (TransactionFormType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TransactionFormType.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { option ->
                    TypeButton(
                        option = option,
                        active = option == selected,
                        disabled = locked && option != selected,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeButton(
    option: TransactionFormType,
    active: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (active) option.color else Color.Transparent
    val border = if (active) option.color else MaterialTheme.colorScheme.outline
    val content = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(12.dp))
            .alpha(if (disabled) 0.4f else 1f)
            .selectable(selected = active, enabled = !disabled, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = option.icon, contentDescription = null, tint = content, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = option.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionTypeSelectorPreview() {
    ExpenseTrackerTheme {
        Column(modifier = Modifier.width(320.dp).padding(16.dp)) {
            TransactionTypeSelector(selected = TransactionFormType.EXPENSE, locked = false, onSelect = {})
        }
    }
}
