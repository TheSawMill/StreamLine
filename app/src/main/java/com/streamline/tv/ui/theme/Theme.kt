package com.streamline.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme

/** Core palette shared by the UI and the procedural background scenes. */
object StreamlineColors {
    val Ink = Color(0xFF05080E)
    val InkElevated = Color(0xFF0B121C)
    val InkGlass = Color(0xFF101A26)
    val Mint = Color(0xFF7BE8C6)
    val Aqua = Color(0xFF5BC8E8)
    val Amber = Color(0xFFFFC978)
    val Cloud = Color(0xFFEAF2F9)
    val Mist = Color(0xFF9DB0C2)
    val Danger = Color(0xFFFF8A8A)
}

private val StreamlineColorScheme = darkColorScheme(
    primary = StreamlineColors.Mint,
    onPrimary = StreamlineColors.Ink,
    primaryContainer = StreamlineColors.Mint.copy(alpha = 0.16f),
    onPrimaryContainer = StreamlineColors.Mint,
    secondary = StreamlineColors.Aqua,
    onSecondary = StreamlineColors.Ink,
    tertiary = StreamlineColors.Amber,
    onTertiary = StreamlineColors.Ink,
    background = StreamlineColors.Ink,
    onBackground = StreamlineColors.Cloud,
    surface = StreamlineColors.InkElevated,
    onSurface = StreamlineColors.Cloud,
    surfaceVariant = StreamlineColors.InkGlass,
    onSurfaceVariant = StreamlineColors.Mist,
    error = StreamlineColors.Danger,
    onError = StreamlineColors.Ink,
)

@Composable
fun StreamlineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreamlineColorScheme,
        typography = Typography(),
        content = content,
    )
}
