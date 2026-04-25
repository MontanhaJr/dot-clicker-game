package com.montanhajr.pointgame.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PopBlue,
    onPrimary = PopWhite,
    secondary = PopCyan,
    onSecondary = PopDarkBlue,
    tertiary = PopYellow,
    onTertiary = PopDarkBlue,
    background = PopDarkBlue,
    onBackground = PopWhite,
    surface = PopDeepBlue,
    onSurface = PopWhite,
    error = PopRed,
    onError = PopWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PopBlue,
    onPrimary = PopWhite,
    secondary = PopCyan,
    onSecondary = PopDarkBlue,
    tertiary = PopYellow,
    onTertiary = PopDarkBlue,
    background = PopWhite,
    onBackground = PopDarkBlue,
    surface = Color(0xFFF0F5FF),
    onSurface = PopDarkBlue,
    error = PopRed,
    onError = PopWhite
)

@Composable
fun DotConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to force the Pop! theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
