package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.ui.components.PageHeader
import com.blockveil.expense.tracker.ui.components.RadioRow
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.util.CurrencyCatalog

/**
 * Currency settings: a link to the full country picker, then Position and Format radio
 * groups. Matches CurrencySettingsPage exactly.
 */
@Composable
fun CurrencySettingsScreen(
    currencyCountry: String,
    currencyPosition: CurrencyPosition,
    currencyFormat: CurrencyFormat,
    onOpenPicker: () -> Unit,
    onSetPosition: (CurrencyPosition) -> Unit,
    onSetFormat: (CurrencyFormat) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currency = CurrencyCatalog.findByCountry(currencyCountry)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        PageHeader(title = "Currency", onClose = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Country", modifier = Modifier.padding(top = 4.dp))
            SettingsLinkRow(
                label = if (currency != null) "${currency.flag} ${currency.country} · ${currency.code}" else currencyCountry,
                onClick = onOpenPicker,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            SectionHeader(title = "Position")
            RadioRow(
                label = "Before amount (${currency?.symbol ?: ""}100)",
                selected = currencyPosition == CurrencyPosition.PREFIX,
                onSelect = { onSetPosition(CurrencyPosition.PREFIX) },
            )
            RadioRow(
                label = "After amount (100${currency?.symbol ?: ""})",
                selected = currencyPosition == CurrencyPosition.SUFFIX,
                onSelect = { onSetPosition(CurrencyPosition.SUFFIX) },
                modifier = Modifier.padding(bottom = 16.dp),
            )

            SectionHeader(title = "Format")
            RadioRow(
                label = "Grouped digits (12,34,567)",
                selected = currencyFormat == CurrencyFormat.GROUPED,
                onSelect = { onSetFormat(CurrencyFormat.GROUPED) },
            )
            RadioRow(
                label = "Plain digits (1234567)",
                selected = currencyFormat == CurrencyFormat.PLAIN,
                onSelect = { onSetFormat(CurrencyFormat.PLAIN) },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}
