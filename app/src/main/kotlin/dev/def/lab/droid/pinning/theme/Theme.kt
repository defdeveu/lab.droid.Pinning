package dev.def.lab.droid.pinning.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandOrange = Color(0xFFD76F2B)
private val PrimaryOnLight = Color(0xFFB45309)
private val PrimaryOnDark = Color(0xFFF2A46B)

private val LightColors = lightColorScheme(
    primary = PrimaryOnLight,
    onPrimary = Color.White,
    primaryContainer = BrandOrange,
    onPrimaryContainer = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryOnDark,
    onPrimary = Color.Black,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PinningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
