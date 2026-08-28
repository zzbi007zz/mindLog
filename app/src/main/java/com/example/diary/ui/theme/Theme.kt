package com.example.diary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Diary & Journal palette: warm paper + muted ink + a single warm accent.
// Light-only for MVP simplicity and tested contrast (dark scheme is a
// documented follow-up — not a plan goal).
private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = PaperLight,
    primaryContainer = AccentContainer,
    onPrimaryContainer = InkLight,
    secondary = Taupe,
    onSecondary = PaperLight,
    secondaryContainer = PanelLight,
    onSecondaryContainer = InkLight,
    background = PaperLight,
    onBackground = InkLight,
    surface = PaperLight,
    onSurface = InkLight,
    surfaceVariant = PanelLight,
    onSurfaceVariant = MutedLight,
    surfaceContainer = PanelLight,
    surfaceContainerHigh = PanelLight,
    surfaceContainerHighest = PanelLight,
    surfaceTint = Accent,
    outline = Hairline,
    outlineVariant = Hairline,
)

@Composable
fun DiaryTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DiaryTypography,
        content = content,
    )
}