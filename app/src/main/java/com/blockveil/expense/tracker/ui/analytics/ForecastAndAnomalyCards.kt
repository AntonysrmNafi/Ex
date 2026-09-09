package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
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
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.theme.BrandAccent
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * "At this pace, you'll spend about X by month end", turning red with an over-budget note
 * when the pace exceeds the effective budget. Matches the forecast Card in AnalyticsScreen.
 */
@Composable
fun ForecastCard(
    forecast: Double,
    budget: Double,
    currency: CurrencyDisplay,
    modifier: Modifier = Modifier,
) {
    val overForecast = budget > 0 && forecast > budget
    val iconTint = if (overForecast) BrandDanger else BrandAccent

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = Icons.Filled.TrendingUp, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Column {
                Text(
                    text = "Cash flow forecast",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "At this pace, you'll spend about ${formatMoney(forecast, currency)} by month end",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp),
                )
                if (overForecast) {
                    Text(
                        text = "That's over your ${formatMoney(budget, currency)} budget",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandDanger,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * Lists categories spending noticeably more than last month. Matches the anomalies Card in
 * AnalyticsScreen. Only rendered when there's at least one anomaly.
 */
@Composable
fun AnomalyCard(
    anomalies: List<AnomalyItem>,
    currency: CurrencyDisplay,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = BrandDanger, modifier = Modifier.size(16.dp))
                Text(
                    text = "Unusual spending",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            anomalies.forEach { anomaly ->
                Text(
                    text = "${anomaly.category}: ${anomaly.percent}% more than last month " +
                        "(${formatMoney(anomaly.thisMonth, currency)} vs ${formatMoney(anomaly.lastMonth, currency)})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}
