package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.ui.theme.faded
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney
import kotlin.math.min

/**
 * One account row. Savings: name, type, balance. Loan: name + "Loan" with a repayment
 * progress bar, a "Settled" badge once fully repaid, and Received/Settlement/Repaid detail
 * lines. Matches the accounts.map(...) block in MoreScreen exactly, including dimming a
 * settled loan to 60% opacity, and the savings type subtitle's raw enum-name casing
 * (e.g. "MOBILE WALLET", not "Mobile Wallet") to match `a.type.replace("_", " ")` exactly.
 */
@Composable
fun AccountRow(account: AccountEntity, currency: CurrencyDisplay, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isLoan = account.category == AccountCategory.LOAN
    val settled = isLoan && !account.active

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (settled) 0.6f else 1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        if (isLoan) {
            val principal = account.principal ?: 0.0
            val repaid = account.repaid ?: 0.0
            val progress = if (principal > 0) min(repaid / principal, 1.0).toFloat() else 0f
            val remaining = principal - repaid

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${account.name} · Loan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (settled) {
                        Text(
                            text = "Settled",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BrandPrimary.faded())
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
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
                    text = "Received ${formatMoney(account.loanAmount ?: principal, currency)} · " +
                        "Settlement ${formatMoney(principal, currency)}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Repaid ${formatMoney(repaid, currency)} of ${formatMoney(principal, currency)} · " +
                        if (settled) "fully settled" else "${formatMoney(remaining, currency)} left",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = account.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = account.type?.name?.replace("_", " ") ?: "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatMoney(account.balance ?: 0.0, currency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
