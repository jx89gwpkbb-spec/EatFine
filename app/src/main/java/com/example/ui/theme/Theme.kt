package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrangeLight,
    onPrimary = DarkBg,
    primaryContainer = BrandOrangeDark,
    onPrimaryContainer = BrandOrangeContainer,
    secondary = FreshGreenLight,
    onSecondary = DarkBg,
    secondaryContainer = FreshGreen,
    onSecondaryContainer = FreshGreenContainer,
    tertiary = WarmAmber,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = PureWhite,
    primaryContainer = BrandOrangeContainer,
    onPrimaryContainer = BrandOnOrangeContainer,
    secondary = FreshGreen,
    onSecondary = PureWhite,
    secondaryContainer = FreshGreenContainer,
    onSecondaryContainer = FreshOnGreenContainer,
    tertiary = WarmAmber,
    background = CreamBackground,
    surface = SurfaceCard,
    surfaceVariant = SurfaceSubtle,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight
)

@Composable
fun EatFineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep signature EatFine gourmet branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
