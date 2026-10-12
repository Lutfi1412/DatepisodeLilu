package com.lutfilia.rundown.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    primaryContainer = Blush,
    onPrimaryContainer = RoseDark,
    secondary = Gold,
    onSecondary = Wine,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = Wine,
    tertiary = Lilac,
    background = Petal,
    onBackground = Wine,
    surface = Color(0xFFFFFAFB),
    onSurface = Wine,
    surfaceVariant = Blush,
    onSurfaceVariant = WineSoft,
    outline = BlushDeep,
    error = Color(0xFFD64550)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/** Sengaja hanya tema terang: nuansa romantis lebih terjaga. */
@Composable
fun RundownTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
