package com.university.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueDark,
    onPrimaryContainer = RoyalBlueLight,
    secondary = DarkPurpleLight,
    onSecondary = Color.White,
    secondaryContainer = PurpleContainer,
    onSecondaryContainer = PurpleGlow,
    background = Background,
    onBackground = Color.White,
    surface = SurfaceGlass,
    onSurface = Color.White,
    surfaceVariant = SurfaceGlassLight,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = AbsentRed,
    onError = Color.White,
    errorContainer = AbsentRedContainer,
    onErrorContainer = AbsentRed
)

@Composable
fun UniversityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
