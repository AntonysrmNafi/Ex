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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.categoryColor
import com.blockveil.expense.tracker.util.formatMoney
import kotlin.math.roundToInt

/** One category's total for a donut chart / breakdown list. */
data class CategoryTotal(val category: String, val total: Double)

/**
 * A donut chart plus a per-category breakdown list (color dot, name, percent, amount).
 * Matches CategoryDonutCard exactly, including the center-overlay grand total.
 */
@Composable
fun CategoryDonutCard(
    title: String,
    icon: ImageVector,
    totals: List<CategoryTotal>,
    isIncome: Boolean,
    currency: CurrencyDisplay,
    customExpenseCategories: List<CustomCategoryEntity>,
    customIncomeCategories: List<CustomCategoryEntity>,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = title, icon = icon)

            if (totals.isEmpty()) {
                Text(text = emptyText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val grandTotal = totals.sumOf { it.total }
                val segments = totals.map { t ->
                    categoryColor(isIncome, t.category, customExpenseCategories, customIncomeCategories) to t.total
                }

                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DonutChart(segments = segments)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatMoney(grandTotal, currency),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isIncome) BrandPrimary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                totals.forEach { t ->
                    val color = categoryColor(isIncome, t.category, customExpenseCategories, customIncomeCategories)
                    val pct = if (grandTotal > 0) ((t.total / grandTotal) * 100).roundToInt() else 0

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Text(
                            text = t.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(text = "$pct%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatMoney(t.total, currency),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(64.dp),
                        )
                    }
                }
            }
        }
    }
}
