package com.blockveil.expense.tracker.util

import java.util.Locale

/** One row from the world currency list: a country, its currency code, symbol, and flag. */
data class Currency(
    val country: String,
    val code: String,
    val symbol: String,
    val flag: String,
)

/** Every country/currency pair offered in Settings, same order as COUNTRIES in the source design. */
object CurrencyCatalog {

    val all: List<Currency> = listOf(
        Currency("Afghanistan", "AFN", "؋", "🇦🇫"),
        Currency("Albania", "ALL", "L", "🇦🇱"),
        Currency("Algeria", "DZD", "د.ج", "🇩🇿"),
        Currency("Argentina", "ARS", "$", "🇦🇷"),
        Currency("Australia", "AUD", "$", "🇦🇺"),
        Currency("Austria", "EUR", "€", "🇦🇹"),
        Currency("Bahrain", "BHD", ".د.ب", "🇧🇭"),
        Currency("Bangladesh", "BDT", "৳", "🇧🇩"),
        Currency("Belgium", "EUR", "€", "🇧🇪"),
        Currency("Bhutan", "BTN", "Nu.", "🇧🇹"),
        Currency("Brazil", "BRL", "R$", "🇧🇷"),
        Currency("Canada", "CAD", "$", "🇨🇦"),
        Currency("China", "CNY", "¥", "🇨🇳"),
        Currency("Denmark", "DKK", "kr", "🇩🇰"),
        Currency("Egypt", "EGP", "£", "🇪🇬"),
        Currency("Finland", "EUR", "€", "🇫🇮"),
        Currency("France", "EUR", "€", "🇫🇷"),
        Currency("Germany", "EUR", "€", "🇩🇪"),
        Currency("Greece", "EUR", "€", "🇬🇷"),
        Currency("Hong Kong", "HKD", "$", "🇭🇰"),
        Currency("India", "INR", "₹", "🇮🇳"),
        Currency("Indonesia", "IDR", "Rp", "🇮🇩"),
        Currency("Iran", "IRR", "﷼", "🇮🇷"),
        Currency("Iraq", "IQD", "ع.د", "🇮🇶"),
        Currency("Ireland", "EUR", "€", "🇮🇪"),
        Currency("Israel", "ILS", "₪", "🇮🇱"),
        Currency("Italy", "EUR", "€", "🇮🇹"),
        Currency("Japan", "JPY", "¥", "🇯🇵"),
        Currency("Jordan", "JOD", "د.ا", "🇯🇴"),
        Currency("Kenya", "KES", "KSh", "🇰🇪"),
        Currency("Kuwait", "KWD", "د.ك", "🇰🇼"),
        Currency("Lebanon", "LBP", "ل.ل", "🇱🇧"),
        Currency("Malaysia", "MYR", "RM", "🇲🇾"),
        Currency("Maldives", "MVR", "Rf", "🇲🇻"),
        Currency("Mexico", "MXN", "$", "🇲🇽"),
        Currency("Myanmar", "MMK", "K", "🇲🇲"),
        Currency("Nepal", "NPR", "₨", "🇳🇵"),
        Currency("Netherlands", "EUR", "€", "🇳🇱"),
        Currency("New Zealand", "NZD", "$", "🇳🇿"),
        Currency("Nigeria", "NGN", "₦", "🇳🇬"),
        Currency("Norway", "NOK", "kr", "🇳🇴"),
        Currency("Oman", "OMR", "ر.ع.", "🇴🇲"),
        Currency("Pakistan", "PKR", "₨", "🇵🇰"),
        Currency("Philippines", "PHP", "₱", "🇵🇭"),
        Currency("Poland", "PLN", "zł", "🇵🇱"),
        Currency("Portugal", "EUR", "€", "🇵🇹"),
        Currency("Qatar", "QAR", "ر.ق", "🇶🇦"),
        Currency("Russia", "RUB", "₽", "🇷🇺"),
        Currency("Saudi Arabia", "SAR", "ر.س", "🇸🇦"),
        Currency("Singapore", "SGD", "$", "🇸🇬"),
        Currency("South Africa", "ZAR", "R", "🇿🇦"),
        Currency("South Korea", "KRW", "₩", "🇰🇷"),
        Currency("Spain", "EUR", "€", "🇪🇸"),
        Currency("Sri Lanka", "LKR", "Rs", "🇱🇰"),
        Currency("Sweden", "SEK", "kr", "🇸🇪"),
        Currency("Switzerland", "CHF", "Fr", "🇨🇭"),
        Currency("Taiwan", "TWD", "$", "🇹🇼"),
        Currency("Thailand", "THB", "฿", "🇹🇭"),
        Currency("Turkey", "TRY", "₺", "🇹🇷"),
        Currency("Uganda", "UGX", "USh", "🇺🇬"),
        Currency("Ukraine", "UAH", "₴", "🇺🇦"),
        Currency("United Arab Emirates", "AED", "د.إ", "🇦🇪"),
        Currency("United Kingdom", "GBP", "£", "🇬🇧"),
        Currency("United States", "USD", "$", "🇺🇸"),
        Currency("Vietnam", "VND", "₫", "🇻🇳"),
        Currency("Yemen", "YER", "﷼", "🇾🇪"),
    )

    fun findByCountry(country: String): Currency? = all.firstOrNull { it.country == country }
}

/**
 * Best-effort match of the device's region setting to one of [CurrencyCatalog.all]'s country
 * names, for picking the default currency the very first time the app runs (before the user
 * has ever opened Settings > Currency). Falls back to Ireland/EUR if the device's region is
 * blank or doesn't match anything in the catalog, exactly as requested.
 */
fun detectDefaultCurrencyCountry(): String {
    val isoCountry = Locale.getDefault().country
    if (isoCountry.isBlank()) return "Ireland"
    val displayName = Locale("", isoCountry).getDisplayCountry(Locale.ENGLISH)
    return CurrencyCatalog.all.firstOrNull { it.country.equals(displayName, ignoreCase = true) }?.country ?: "Ireland"
}
