package com.blockveil.expense.tracker.ui.transaction

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.categoryIcon
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.formatDateDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * Read-only preview of a transaction: tapping a row anywhere in the app (Home, History,
 * category/account drill-down) opens this first instead of jumping straight into editing.
 * The Edit button here is what actually opens TransactionFormScreen.
 */
@Composable
fun TransactionDetailScreen(
    state: TransactionDetailUiState,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BackHeader(title = "Transaction", onBack = onBack)

        val txn = state.transaction
        if (txn == null) {
            // Still loading, or the transaction was deleted elsewhere while this screen was
            // open; either way there's nothing to preview, so just leave the header showing.
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            val isIncome = txn.type == TransactionType.INCOME
            val amountColor = if (isIncome) BrandPrimary else BrandDanger

            Column(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)) {
                Text(
                    text = (if (isIncome) "+" else "-") + formatMoney(txn.amount, state.currency),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor,
                )
                Text(
                    text = if (isIncome) "Income" else "Expense",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    DetailRow(label = "Category") {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(state.categoryColor),
                            )
                            Icon(
                                imageVector = categoryIcon(isIncome, txn.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(text = txn.category, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    DetailRow(label = "Account") {
                        Text(text = state.accountName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    DetailRow(label = "Date") {
                        Text(text = formatDateDisplay(txn.date), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (txn.note.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                        DetailRow(label = "Note") {
                            Text(text = txn.note, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (txn.location.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                        DetailRow(label = "Location") {
                            Text(text = txn.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            if (txn.receiptPhotoPath != null) {
                Text(
                    text = if (isIncome) "Attachment" else "Receipt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                AsyncImage(
                    model = Uri.parse(txn.receiptPhotoPath),
                    contentDescription = "Attached photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}
