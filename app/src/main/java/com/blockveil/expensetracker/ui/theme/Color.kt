package com.blockveil.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Brand accent colors, shared between light and dark mode (from the original design's COLORS).
val BrandPrimary = Color(0xFF2E7D32)
val BrandAccent = Color(0xFFFFA000)
val BrandDanger = Color(0xFFD32F2F)
val BrandRepay = Color(0xFF5C6BC0)

// Light theme surface colors (from the original design's LIGHT).
val LightBackground = Color(0xFFF7F7F5)
val LightSurface = Color(0xFFFFFFFF)
val LightText = Color(0xFF1A1A1A)
val LightSubtext = Color(0xFF6B6B6B)
val LightBorder = Color(0x14000000) // rgba(0,0,0,0.08)

// Dark theme surface colors (from the original design's DARK).
val DarkBackground = Color(0xFF14161A)
val DarkSurface = Color(0xFF1E2126)
val DarkText = Color(0xFFF0F0F0)
val DarkSubtext = Color(0xFF9AA0A6)
val DarkBorder = Color(0x14FFFFFF) // rgba(255,255,255,0.08)

/** Fixed expense category colors, matched with the original design's CATEGORY_META. */
object CategoryColors {
    val Food = Color(0xFFEF6C00)
    val Transport = Color(0xFF1976D2)
    val Shopping = Color(0xFF8E24AA)
    val Bills = Color(0xFFC62828)
    val Entertainment = Color(0xFF00897B)
    val Health = Color(0xFF3949AB)
    val Other = Color(0xFF616161)
}

/** Fixed income category colors, matched with the original design's INCOME_CATEGORY_META. */
object IncomeCategoryColors {
    val Salary = Color(0xFF2E7D32)
    val Freelance = Color(0xFF00897B)
    val Business = Color(0xFF1976D2)
    val Investment = Color(0xFF6D4C41)
    val Gift = Color(0xFF8E24AA)
    val Refund = Color(0xFFEF6C00)
    val Other = Color(0xFF616161)
}

/** Palette offered when the user picks a color for a custom category, from CUSTOM_COLOR_PALETTE. */
val CustomCategoryPalette = listOf(
    Color(0xFF5C6BC0),
    Color(0xFF26A69A),
    Color(0xFFEC407A),
    Color(0xFF7E57C2),
    Color(0xFF66BB6A),
    Color(0xFFFFA726),
    Color(0xFF29B6F6),
    Color(0xFF8D6E63),
)
