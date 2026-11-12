package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material 3 Light Color Scheme for Finance Management
 * Highlights: Emerald Green primary (prosperity), Trust Sky Blue secondary, Warm Amber tertiary.
 */
val FinanceLightColorScheme = lightColorScheme(
    primary = FinanceEmeraldPrimaryLight,
    onPrimary = FinanceEmeraldOnPrimaryLight,
    primaryContainer = FinanceEmeraldContainerLight,
    onPrimaryContainer = FinanceEmeraldOnContainerLight,
    secondary = FinanceSkySecondaryLight,
    onSecondary = FinanceSkyOnSecondaryLight,
    secondaryContainer = FinanceSkyContainerLight,
    onSecondaryContainer = FinanceSkyOnContainerLight,
    tertiary = FinanceAmberTertiaryLight,
    onTertiary = FinanceAmberOnTertiaryLight,
    tertiaryContainer = FinanceAmberContainerLight,
    onTertiaryContainer = FinanceAmberOnContainerLight,
    background = FinanceBackgroundLight,
    onBackground = FinanceOnBackgroundLight,
    surface = FinanceSurfaceLight,
    onSurface = FinanceOnSurfaceLight,
    surfaceVariant = FinanceSurfaceVariantLight,
    onSurfaceVariant = FinanceOnSurfaceVariantLight,
    outline = FinanceOutlineLight,
    outlineVariant = FinanceOutlineVariantLight,
    error = FinanceErrorLight,
    onError = FinanceOnErrorLight,
    errorContainer = FinanceErrorContainerLight,
    onErrorContainer = FinanceOnErrorContainerLight
)

/**
 * Material 3 Dark Color Scheme for Finance Management
 * Deep midnight slate surfaces with vivid neon emerald, electric sky, and warm amber accents.
 */
val FinanceDarkColorScheme = darkColorScheme(
    primary = FinanceEmeraldPrimaryDark,
    onPrimary = FinanceEmeraldOnPrimaryDark,
    primaryContainer = FinanceEmeraldContainerDark,
    onPrimaryContainer = FinanceEmeraldOnContainerDark,
    secondary = FinanceSkySecondaryDark,
    onSecondary = FinanceSkyOnSecondaryDark,
    secondaryContainer = FinanceSkyContainerDark,
    onSecondaryContainer = FinanceSkyOnContainerDark,
    tertiary = FinanceAmberTertiaryDark,
    onTertiary = FinanceAmberOnTertiaryDark,
    tertiaryContainer = FinanceAmberContainerDark,
    onTertiaryContainer = FinanceAmberOnContainerDark,
    background = FinanceBackgroundDark,
    onBackground = FinanceOnBackgroundDark,
    surface = FinanceSurfaceDark,
    onSurface = FinanceOnSurfaceDark,
    surfaceVariant = FinanceSurfaceVariantDark,
    onSurfaceVariant = FinanceOnSurfaceVariantDark,
    outline = FinanceOutlineDark,
    outlineVariant = FinanceOutlineVariantDark,
    error = FinanceErrorDark,
    onError = FinanceOnErrorDark,
    errorContainer = FinanceErrorContainerDark,
    onErrorContainer = FinanceOnErrorContainerDark
)

/**
 * Extension property for accessing financial semantic colors directly from MaterialTheme.
 * Example: MaterialTheme.financeColors.income
 */
val MaterialTheme.financeColors: FinanceCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalFinanceCustomColors.current

@Composable
fun FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FinanceDarkColorScheme else FinanceLightColorScheme
    val customColors = if (darkTheme) DarkFinanceCustomColors else LightFinanceCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFinanceCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinanceTypography,
            shapes = FinanceShapes,
            content = content
        )
    }
}

// Backward compatibility alias for the existing app entry point
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FinanceTheme(
        darkTheme = darkTheme,
        content = content
    )
}
