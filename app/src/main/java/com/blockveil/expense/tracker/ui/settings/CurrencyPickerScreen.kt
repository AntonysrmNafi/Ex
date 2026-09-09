package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.ui.theme.faded
import com.blockveil.expense.tracker.util.Currency
import com.blockveil.expense.tracker.util.CurrencyCatalog

/**
 * Search + select any of the currency catalog entries. Matches CurrencyPickerPage exactly,
 * including filtering on both country name and currency code.
 */
@Composable
fun CurrencyPickerScreen(
    selectedCountry: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val filtered = CurrencyCatalog.all.filter { c ->
        query.isBlank() || c.country.contains(query, ignoreCase = true) || c.code.contains(query, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            ) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Select Currency", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        AppTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Enter Country Name",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        if (filtered.isEmpty()) {
            Text(
                text = "No countries match.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.country }) { currency ->
                    CurrencyRow(
                        currency = currency,
                        selected = currency.country == selectedCountry,
                        onClick = { onSelect(currency.country) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(currency: Currency, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) BrandPrimary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (selected) BrandPrimary.faded() else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = currency.flag, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.country,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${currency.code} · ${currency.symbol}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
