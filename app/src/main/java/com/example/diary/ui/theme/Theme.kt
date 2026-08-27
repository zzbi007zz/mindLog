package com.example.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = PaperLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = PanelLight,
    onSurface = InkLight,
    surfaceVariant = PanelLight,
    onSurfaceVariant = MutedLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = PaperDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = PanelDark,
    onSurface = InkDark,
    surfaceVariant = PanelDark,
    onSurfaceVariant = MutedDark,
)

@Composable
fun DiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = DiaryTypography,
        content = content,
    )
}