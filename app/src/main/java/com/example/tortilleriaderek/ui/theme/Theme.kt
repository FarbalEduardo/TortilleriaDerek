package com.example.tortilleriaderek.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MaizPrimary,
    onPrimary = Color.White,
    primaryContainer = MaizPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = TerracotaSecondary,
    onSecondary = Color.White,
    tertiary = VerdeAgave,
    onTertiary = Color.White,
    background = Color(0xFF181615),
    surface = Color(0xFF252321),
    onBackground = Color(0xFFF4F2F0),
    onSurface = Color(0xFFF4F2F0),
    surfaceVariant = Color(0xFF383532),
    onSurfaceVariant = Color(0xFFD5D1CD),
    outline = Color(0xFF736E69),
    outlineVariant = Color(0xFF524E4A)
)

private val LightColorScheme = lightColorScheme(
    primary = MaizPrimary,
    onPrimary = Color.White,
    primaryContainer = MaizPrimaryLight,
    onPrimaryContainer = MaizPrimaryDark,
    secondary = TerracotaSecondary,
    onSecondary = Color.White,
    secondaryContainer = TerracotaLight,
    onSecondaryContainer = TerracotaSecondary,
    tertiary = VerdeAgave,
    onTertiary = Color.White,
    tertiaryContainer = VerdeAgaveLight,
    onTertiaryContainer = VerdeAgave,
    background = SurfaceWarm,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderLight,
    error = ErrorRed,
    errorContainer = ErrorContainer
)

@Composable
fun TortilleriaDerekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Íconos blancos (hora, batería, señal) sobre la barra de estado oscura (#181615)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                window.statusBarColor = android.graphics.Color.parseColor("#181615")
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}