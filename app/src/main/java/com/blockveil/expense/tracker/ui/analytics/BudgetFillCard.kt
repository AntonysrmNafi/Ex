package com.blockveil.expense.tracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney
import kotlin.math.roundToInt

/**
 * Budget usage across every category that has a limit set: a ring chart plus a per-category
 * breakdown (color dot, name, percent used, spent/limit). Matches BudgetFillCard exactly.
 */
@Composable
fun BudgetFillCard(
    categories: List<BudgetCategoryUsage>,
    currency: CurrencyDisplay,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = "Budget usage by category", icon = Icons.Filled.Wallet)

            if (categories.isEmpty()) {
                Text(
                    text = "Set category limits below to see usage here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val totalLimit = categories.sumOf { it.limit }
                val totalSpent = categories.sumOf { it.spent }
                val pct = if (totalLimit > 0) ((totalSpent / totalLimit) * 100).roundToInt() else 0

                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BudgetFillChart(categories = categories)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Used", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$pct%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pct > 100) BrandDanger else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                categories.forEach { category ->
                    val catPct = if (category.limit > 0) ((category.spent / category.limit) * 100).roundToInt() else 0
                    val over = category.spent > category.limit

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(category.color))
                        Text(
                            text = category.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$catPct%",
                            fontSize = 10.sp,
                            color = if (over) BrandDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${formatMoney(category.spent, currency)}/${formatMoney(category.limit, currency)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(96.dp),
                        )
                    }
                }
            }
        }
    }
}
