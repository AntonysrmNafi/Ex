package com.blockveil.expensetracker.util

import com.blockveil.expensetracker.data.datastore.AppSettings
import com.blockveil.expensetracker.data.model.CurrencyFormat
import com.blockveil.expensetracker.data.model.CurrencyPosition
import java.util.Locale
import kotlin.math.abs

/** Everything formatMoney needs: which symbol, which side it goes on, and whether to group digits. */
data class CurrencyDisplay(
    val symbol: String,
    val position: CurrencyPosition,
    val format: CurrencyFormat,
)

/**
 * Builds a [CurrencyDisplay] from the user's saved settings. Falls back to Taka if the
 * saved country name doesn't match the catalog (e.g. right after a fresh install or a
 * data wipe, before any country has been explicitly chosen).
 */
fun resolveCurrencyDisplay(settings: AppSettings): CurrencyDisplay {
    val symbol = CurrencyCatalog.findByCountry(settings.currencyCountry)?.symbol ?: "৳"
    return CurrencyDisplay(symbol = symbol, position = settings.currencyPosition, format = settings.currencyFormat)
}

// Matches JS's /\B(?=(\d{2})+(?!\d))/g exactly: Java's regex engine supports the same
// \B word-boundary and negative lookahead syntax, so this is a direct, faithful port.
private val SOUTH_ASIAN_GROUPING = Regex("\\B(?=(\\d{2})+(?!\\d))")

/**
 * Groups digits South Asian (lakh/crore) style: the last 3 digits stay together, everything
 * before that groups in pairs. E.g. "1234567" -> "12,34,567". Matches groupDigits exactly.
 */
fun groupDigits(numStr: String): String {
    if (numStr.length <= 3) return numStr
    val lastThree = numStr.substring(numStr.length - 3)
    val rest = numStr.substring(0, numStr.length - 3)
    return rest.replace(SOUTH_ASIAN_GROUPING, ",") + "," + lastThree
}

/**
 * Formats an amount as e.g. "৳12,34,567.00" or "1,234.00 kr" depending on [currency].
 * Matches formatMoney exactly. NaN and negative signs are never shown here, callers that
 * need a +/- prefix (income vs expense) add it themselves, same as in the source design.
 */
fun formatMoney(value: Double, currency: CurrencyDisplay): String {
    val safeValue = if (value.isNaN()) 0.0 else value
    val absValue = abs(safeValue)
    // Locale.US pins the decimal separator to '.', regardless of the device's own locale
    // (some locales default to ',' as the decimal separator, which would silently corrupt this).
    val fixed = String.format(Locale.US, "%.2f", absValue)
    val dotIndex = fixed.indexOf('.')
    val intPart = fixed.substring(0, dotIndex)
    val decPart = fixed.substring(dotIndex + 1)
    val digits = if (currency.format == CurrencyFormat.GROUPED) "${groupDigits(intPart)}.$decPart" else fixed
    return if (currency.position == CurrencyPosition.SUFFIX) "$digits${currency.symbol}" else "${currency.symbol}$digits"
}
