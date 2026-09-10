package com.blockveil.expense.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
 * Uniform bump applied to every dp and sp in the app (text and layout sizes together, so
 * icons/containers keep their proportions relative to the text next to them) since the
 * source design's sizes (mostly 10-14sp) read as quite small/dense on a real device.
 */
private const val UiScaleFactor = 1.12f

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
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity) {
        Density(density = baseDensity.density * UiScaleFactor, fontScale = baseDensity.fontScale * UiScaleFactor)
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ExpenseTrackerTypography,
            content = content,
        )
    }
}
