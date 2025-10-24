package pl.edu.pk.student.core.ui.theme

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
    primary = pl.edu.pk.student.core.ui.theme.PrimaryDark,
    onPrimary = pl.edu.pk.student.core.ui.theme.OnPrimary,
    background = pl.edu.pk.student.core.ui.theme.BackgroundDark,
    surface = pl.edu.pk.student.core.ui.theme.SurfaceDark,

    outline = Color(0xFF888888),
    outlineVariant = Color(0xFF444444),

    error = ErrorDark,
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = pl.edu.pk.student.core.ui.theme.PrimaryLight,
    onPrimary = pl.edu.pk.student.core.ui.theme.OnPrimary,
    background = pl.edu.pk.student.core.ui.theme.BackgroundLight,
    surface = pl.edu.pk.student.core.ui.theme.SurfaceLight,

    outline = Color(0xFF888888),
    outlineVariant = Color(0xFF444444),

    error = ErrorLight,
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color.White,
)

@Composable
fun MediMeowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = pl.edu.pk.student.core.ui.theme.Typography,
        content = content
    )
}