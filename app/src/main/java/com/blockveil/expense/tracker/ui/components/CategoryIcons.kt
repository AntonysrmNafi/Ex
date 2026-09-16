package com.blockveil.expense.tracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity

// Lucide -> Material icon mapping, one per fixed expense category (CATEGORY_META).
private val EXPENSE_ICONS: Map<String, ImageVector> = mapOf(
    "Food" to Icons.Filled.Restaurant,
    "Transport" to Icons.Filled.DirectionsCar,
    "Shopping" to Icons.Filled.ShoppingBag,
    "Bills" to Icons.Filled.Receipt,
    "Entertainment" to Icons.Filled.Movie,
    "Health" to Icons.Filled.MonitorHeart,
    "Other" to Icons.Filled.MoreHoriz,
)

// Lucide -> Material icon mapping, one per fixed income category (INCOME_CATEGORY_META).
private val INCOME_ICONS: Map<String, ImageVector> = mapOf(
    "Salary" to Icons.Filled.AccountBalance,
    "Freelance" to Icons.Filled.Work,
    "Business" to Icons.Filled.Business,
    "Investment" to Icons.Filled.TrendingUp,
    "Gift" to Icons.Filled.CardGiftcard,
    "Refund" to Icons.Filled.Undo,
    "Other" to Icons.Filled.MoreHoriz,
)

/**
 * Icon choices offered when creating a custom expense category (see AddCustomCategoryScreen),
 * keyed by a stable string stored on CustomCategoryEntity.icon so a future icon-set change
 * doesn't need a data migration. "Sell" (a generic price tag) is the default/fallback.
 */
val EXPENSE_CATEGORY_ICON_CHOICES: List<Pair<String, ImageVector>> = listOf(
    "Sell" to Icons.Filled.Sell,
    "Restaurant" to Icons.Filled.Restaurant,
    "LocalCafe" to Icons.Filled.LocalCafe,
    "LocalGroceryStore" to Icons.Filled.LocalGroceryStore,
    "DirectionsCar" to Icons.Filled.DirectionsCar,
    "DirectionsBus" to Icons.Filled.DirectionsBus,
    "DirectionsBike" to Icons.Filled.DirectionsBike,
    "LocalGasStation" to Icons.Filled.LocalGasStation,
    "Flight" to Icons.Filled.Flight,
    "Hotel" to Icons.Filled.Hotel,
    "ShoppingBag" to Icons.Filled.ShoppingBag,
    "Checkroom" to Icons.Filled.Checkroom,
    "Diamond" to Icons.Filled.Diamond,
    "Receipt" to Icons.Filled.Receipt,
    "Bolt" to Icons.Filled.Bolt,
    "WaterDrop" to Icons.Filled.WaterDrop,
    "Wifi" to Icons.Filled.Wifi,
    "PhoneAndroid" to Icons.Filled.PhoneAndroid,
    "Apartment" to Icons.Filled.Apartment,
    "Home" to Icons.Filled.Home,
    "Build" to Icons.Filled.Build,
    "LocalLaundryService" to Icons.Filled.LocalLaundryService,
    "Movie" to Icons.Filled.Movie,
    "MusicNote" to Icons.Filled.MusicNote,
    "SportsEsports" to Icons.Filled.SportsEsports,
    "CameraAlt" to Icons.Filled.CameraAlt,
    "Celebration" to Icons.Filled.Celebration,
    "Redeem" to Icons.Filled.Redeem,
    "MonitorHeart" to Icons.Filled.MonitorHeart,
    "LocalHospital" to Icons.Filled.LocalHospital,
    "Medication" to Icons.Filled.Medication,
    "FitnessCenter" to Icons.Filled.FitnessCenter,
    "Spa" to Icons.Filled.Spa,
    "Pets" to Icons.Filled.Pets,
    "ChildCare" to Icons.Filled.ChildCare,
    "School" to Icons.Filled.School,
    "MenuBook" to Icons.Filled.MenuBook,
    "VolunteerActivism" to Icons.Filled.VolunteerActivism,
    "Security" to Icons.Filled.Security,
    "Savings" to Icons.Filled.Savings,
    "CreditCard" to Icons.Filled.CreditCard,
    "AttachMoney" to Icons.Filled.AttachMoney,
    "Repeat" to Icons.Filled.Repeat,
)

/**
 * Icon choices offered when creating a custom income source, kept entirely separate from
 * [EXPENSE_CATEGORY_ICON_CHOICES] so the picker only ever shows income-appropriate icons
 * (salary, business, investment, etc.) here, never a food/transport/shopping icon.
 */
val INCOME_SOURCE_ICON_CHOICES: List<Pair<String, ImageVector>> = listOf(
    "AccountBalance" to Icons.Filled.AccountBalance,
    "Work" to Icons.Filled.Work,
    "Business" to Icons.Filled.Business,
    "Storefront" to Icons.Filled.Storefront,
    "Laptop" to Icons.Filled.Laptop,
    "Handshake" to Icons.Filled.Handshake,
    "TrendingUp" to Icons.Filled.TrendingUp,
    "ShowChart" to Icons.Filled.ShowChart,
    "BarChart" to Icons.Filled.BarChart,
    "PieChart" to Icons.Filled.PieChart,
    "CurrencyExchange" to Icons.Filled.CurrencyExchange,
    "Percent" to Icons.Filled.Percent,
    "Savings" to Icons.Filled.Savings,
    "AccountBalanceWallet" to Icons.Filled.AccountBalanceWallet,
    "CreditCard" to Icons.Filled.CreditCard,
    "AttachMoney" to Icons.Filled.AttachMoney,
    "LocalAtm" to Icons.Filled.LocalAtm,
    "MonetizationOn" to Icons.Filled.MonetizationOn,
    "Paid" to Icons.Filled.Paid,
    "Payments" to Icons.Filled.Payments,
    "RequestQuote" to Icons.Filled.RequestQuote,
    "ReceiptLong" to Icons.Filled.ReceiptLong,
    "CardGiftcard" to Icons.Filled.CardGiftcard,
    "Redeem" to Icons.Filled.Redeem,
    "EmojiEvents" to Icons.Filled.EmojiEvents,
    "VolunteerActivism" to Icons.Filled.VolunteerActivism,
    "Apartment" to Icons.Filled.Apartment,
    "HomeWork" to Icons.Filled.HomeWork,
    "Diamond" to Icons.Filled.Diamond,
    "Undo" to Icons.Filled.Undo,
)

private fun iconForKey(isIncome: Boolean, key: String): ImageVector {
    val choices = if (isIncome) INCOME_SOURCE_ICON_CHOICES else EXPENSE_CATEGORY_ICON_CHOICES
    return choices.firstOrNull { it.first == key }?.second ?: Icons.Filled.Sell
}

/**
 * Icon for a category name, fixed or custom. Fixed categories use their own dedicated icon;
 * a custom one uses whichever icon was chosen when it was created (from
 * [EXPENSE_CATEGORY_ICON_CHOICES] or [INCOME_SOURCE_ICON_CHOICES] depending on [isIncome]),
 * falling back to a generic tag icon for anything that matches neither (e.g. a category
 * whose custom entry was later deleted).
 */
fun categoryIcon(
    isIncome: Boolean,
    category: String,
    customExpenseCategories: List<CustomCategoryEntity> = emptyList(),
    customIncomeCategories: List<CustomCategoryEntity> = emptyList(),
): ImageVector {
    val fixed = if (isIncome) INCOME_ICONS[category] else EXPENSE_ICONS[category]
    if (fixed != null) return fixed
    val custom = (if (isIncome) customIncomeCategories else customExpenseCategories).firstOrNull { it.name == category }
    return custom?.let { iconForKey(isIncome, it.icon) } ?: Icons.Filled.Sell
}
