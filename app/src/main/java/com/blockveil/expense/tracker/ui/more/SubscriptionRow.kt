package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.accountName
import com.blockveil.expense.tracker.util.formatDateDisplay
import com.blockveil.expense.tracker.util.formatMoney
import com.blockveil.expense.tracker.util.monthlyEquivalent

/**
 * One subscription row: icon + name, "category · cycle · amount (~monthly/mo)", account +
 * date range + billed count, an active toggle, and a delete button. Matches the
 * subscriptions.map(...) block in MoreScreen exactly.
 */
@Composable
fun SubscriptionRow(
    subscription: SubscriptionEntity,
    accounts: List<AccountEntity>,
    currency: CurrencyDisplay,
    onToggleActive: (Boolean) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthly = monthlyEquivalent(subscription.cycle, subscription.amount)
    val showEquivalent = subscription.cycle != SubscriptionCycle.MONTHLY && monthly > 0

    val rangeText = subscription.startDate?.let { start ->
        val startText = " · ${formatDateDisplay(start)}"
        val endText = subscription.endDate?.let { " – ${formatDateDisplay(it)}" } ?: ""
        startText + endText
    } ?: ""

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${subscription.category} · ${subscription.cycle.label} · ${formatMoney(subscription.amount, currency)}" +
                            if (showEquivalent) " (~${formatMoney(monthly, currency)}/mo)" else "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${accountName(accounts, subscription.accountId)}$rangeText · billed ${subscription.billedDates.size}x",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SubscriptionActiveToggle(active = subscription.active, onToggle = { onToggleActive(!subscription.active) })
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete ${subscription.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp).clickable(onClick = onDeleteRequest),
                )
            }
        }
    }
}

@Composable
private fun SubscriptionActiveToggle(active: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 20.dp)
            .clip(RoundedCornerShape(50))
            .background(if (active) BrandPrimary else MaterialTheme.colorScheme.outline)
            .selectable(selected = active, onClick = onToggle, role = Role.Switch)
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(if (active) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
