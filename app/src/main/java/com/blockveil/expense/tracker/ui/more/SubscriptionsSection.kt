package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney
import java.time.LocalDate
import kotlin.math.roundToInt

/** One subscription spending noticeably more than the average active subscription. Matches `flagged`. */
data class FlaggedSubscription(val name: String, val ratio: Double)

/**
 * Subscriptions section: header with the monthly total, a "worth a second look" card for
 * outlier subscriptions, the add-subscription card, then every subscription (or an
 * empty-state message). Matches the Subscriptions block in MoreScreen exactly.
 */
@Composable
fun SubscriptionsSection(
    subscriptions: List<SubscriptionEntity>,
    accounts: List<AccountEntity>,
    savingsAccounts: List<AccountEntity>,
    subTotal: Double,
    flagged: List<FlaggedSubscription>,
    currency: CurrencyDisplay,
    onAddSubscription: (name: String, amount: String, cycle: SubscriptionCycle, accountId: Long?, startDate: LocalDate?, endDate: LocalDate?) -> String?,
    onToggleActive: (SubscriptionEntity, Boolean) -> Unit,
    onDeleteRequest: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Subscriptions",
            icon = Icons.Filled.Repeat,
            right = {
                Text(
                    text = "${formatMoney(subTotal, currency)}/mo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )

        if (flagged.isNotEmpty()) {
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = BrandDanger, modifier = Modifier.size(14.dp))
                        Text(text = "Worth a second look", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    flagged.forEach {
                        Text(
                            text = "${it.name}: ${(it.ratio * 100).roundToInt()}% of your average subscription cost",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        AddSubscriptionCard(
            savingsAccounts = savingsAccounts,
            onAddSubscription = onAddSubscription,
            modifier = Modifier.padding(bottom = 10.dp),
        )

        if (subscriptions.isEmpty()) {
            Text(
                text = "No subscriptions yet, add one above.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            subscriptions.forEach { subscription ->
                SubscriptionRow(
                    subscription = subscription,
                    accounts = accounts,
                    currency = currency,
                    onToggleActive = { active -> onToggleActive(subscription, active) },
                    onDeleteRequest = { onDeleteRequest(subscription) },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
