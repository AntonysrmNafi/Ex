package com.blockveil.expensetracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Sizes mirror the web preview's text scale (9px to 19px) instead of Material's defaults,
// since the goal is to match an existing design pixel for pixel, not a fresh Material look.
val ExpenseTrackerTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 19.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 10.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 9.sp),
)
