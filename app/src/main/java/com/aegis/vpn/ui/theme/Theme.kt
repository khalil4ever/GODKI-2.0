package com.aegis.vpn.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VelvetRoseLightColorScheme = lightColorScheme(
    primary = VelvetBurgundyPrimary,
    onPrimary = Color.White,
    primaryContainer = RoseSoftTint,
    onPrimaryContainer = VelvetBurgundyDeep,
    secondary = VelvetBerryBright,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCEBF1),
    onSecondaryContainer = VelvetBurgundyPrimary,
    tertiary = AmberAlert,
    onTertiary = Color.White,
    error = CrimsonDanger,
    onError = Color.White,
    background = RosePearlBg,
    onBackground = TextPrimaryBurgundy,
    surface = RoseBlushSurface,
    onSurface = TextPrimaryBurgundy,
    surfaceVariant = Color.White,
    onSurfaceVariant = TextSecondaryMauve,
    outline = RoseOutline
)

private val VelvetWineDarkColorScheme = darkColorScheme(
    primary = VelvetRoseGlow,
    onPrimary = WineDarkBg,
    primaryContainer = VelvetBurgundyPrimary,
    onPrimaryContainer = Color(0xFFFCEBF1),
    secondary = VelvetBerryBright,
    onSecondary = Color.White,
    secondaryContainer = WineDarkCard,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AmberAlert,
    onTertiary = Color.White,
    error = CrimsonDanger,
    onError = Color.White,
    background = WineDarkBg,
    onBackground = TextPrimaryDark,
    surface = WineDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = WineDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = WineDarkOutline
)

val AegisShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun AegisVpnTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VelvetWineDarkColorScheme else VelvetRoseLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AegisTypography,
        shapes = AegisShapes,
        content = content
    )
}
