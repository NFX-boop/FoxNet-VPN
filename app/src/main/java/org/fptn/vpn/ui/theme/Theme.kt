package com.foxnet.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Secondary,
    onPrimary = Color.Black,
    background = Primary,
    surface = Primary,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
)

/**
 * App-wide Material 3 theme for the Compose UI.
 *
 * The FoxNet design is a deep night background (#0C1523) with a cyan accent (#31F4EC),
 * so the dark scheme is the default.
 */
@Composable
fun FptnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
