package com.blockveil.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    secondary = BrandAccent,
    onSecondary = Color.White,
    error = BrandDanger,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightSubtext,
    outline = LightBorder,
)

private val DarkColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    secondary = BrandAccent,
    onSecondary = Color.White,
    error = BrandDanger,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkSubtext,
    outline = DarkBorder,
)

/**
 * App-wide Material 3 theme. [darkTheme] defaults to the system setting.
 * Starting Bag 17, the Settings screen drives this explicitly from the user's saved
 * theme mode (Light / Dark / System) instead of relying only on the system default.
 */
@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpenseTrackerTypography,
        content = content,
    )
}
