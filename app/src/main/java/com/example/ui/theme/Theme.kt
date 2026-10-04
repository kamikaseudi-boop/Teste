package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color(0xFF003822),
    primaryContainer = PrimaryGreenContainerDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = SecondaryBlueLight,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = AccentCoralLight,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenContainerLight,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueContainer,
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AccentCoral,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF9A3412),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun NutriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to maintain brand consistency
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
