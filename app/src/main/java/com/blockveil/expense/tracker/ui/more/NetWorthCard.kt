package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * "Net worth" header plus the total figure (savings balances minus loan remainders).
 * Matches the Net worth section in MoreScreen exactly.
 */
@Composable
fun NetWorthCard(netWorth: Double, currency: CurrencyDisplay, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Net worth", icon = Icons.Filled.AccountBalance)
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(text = "Total net worth", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = formatMoney(netWorth, currency),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (netWorth < 0) BrandDanger else BrandPrimary,
                )
            }
        }
    }
}
