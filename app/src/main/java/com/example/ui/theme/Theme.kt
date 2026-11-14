package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TruDarkColorScheme = darkColorScheme(
    primary = TruLime,
    onPrimary = TruBg,
    primaryContainer = TruLime.copy(alpha = 0.16f),
    onPrimaryContainer = TruLime,
    secondary = TruViolet,
    onSecondary = TruTextPrimary,
    secondaryContainer = TruViolet.copy(alpha = 0.2f),
    onSecondaryContainer = TruTextPrimary,
    background = TruBg,
    onBackground = TruTextPrimary,
    surface = TruSurface,
    onSurface = TruTextPrimary,
    surfaceVariant = TruSurfaceElevated,
    onSurfaceVariant = TruTextMuted,
    outline = TruBorder,
    outlineVariant = TruBorder,
    error = TruDangerCoral,
    onError = TruBg
)

val TruShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun TruTicketTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TruDarkColorScheme,
        typography = TruTypography,
        shapes = TruShapes,
        content = content
    )
}
