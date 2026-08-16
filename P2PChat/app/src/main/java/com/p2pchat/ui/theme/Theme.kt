package com.p2pchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DiscordBlue,
    secondary = DiscordGreen,
    tertiary = DiscordYellow,
    background = DiscordDarkBg,
    surface = DiscordDarkHeader,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = DiscordDarkInput
)

private val LightColorScheme = lightColorScheme(
    primary = DiscordBlue,
    secondary = DiscordGreen,
    tertiary = DiscordYellow,
    background = Color(0xFFF6F6F7),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF2E3338),
    onSurface = Color(0xFF2E3338),
    surfaceVariant = Color(0xFFEDF0F3)
)

@Composable
fun P2PChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Typography = Typography()
