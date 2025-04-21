package nu.milad.motmaenbash.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.map
import nu.milad.motmaenbash.utils.dataStore
import nu.milad.motmaenbash.viewmodels.SettingsViewModel

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary_DM,
    secondary = Grey,
    tertiary = ColorPrimaryDark,
    // Background and surface
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    // Container colors
    primaryContainer = ColorPrimaryDark,
    // primaryContainer = Color(0xFF1E1E1E),
    secondaryContainer = Color(0xFF2C2C2C),
    // Content colors
    onPrimary = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFE0E0E0),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    errorContainer = Color(0x0DFF5252),
    onError = Color(0xFFFF5252)
)

private val DimColorScheme = darkColorScheme(
    primary = ColorPrimary,
    secondary = Grey,
    tertiary = ColorPrimaryDark,
    primaryContainer = Color(0xFF0E5B9E),
    // Background and surface
    background = Color(0xFF15202B),
    surface = Color(0xFF192734),
    // Content colors
    onPrimary = Color(0xFFE1E8ED),
    onPrimaryContainer = Color(0xFFE1E8ED),
    onSecondary = Color(0xFFE1E8ED),
    onTertiary = Color(0xFFE1E8ED),
    onBackground = Color(0xFFE1E8ED),
    onSurface = Color(0xFFE1E8ED),
    errorContainer = Color(0x0DFF6B6B),
    onError = Color(0xFFFF6B6B)
)

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    secondary = GreyDark,
    tertiary = ColorPrimaryDark,
    primaryContainer = ColorPrimary,
    // Background and surface
    background = BackgroundLightGray,
    surface = White,
    tertiaryContainer = Color(0xFFFFF3E0),
    // Content colors
    onPrimary = White,
    onPrimaryContainer = White,
    onSecondary = White,
    onTertiaryContainer = Color(0xFFC74900),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    errorContainer = Color(0x0DFF0000),
    onError = Red,
)

@Composable
fun MotmaenBashTheme(
    content: @Composable () -> Unit
) {

    val context = LocalContext.current
    val dataStore = context.dataStore

    val themePreference = dataStore.data
        .map { preferences -> preferences[SettingsViewModel.THEME] ?: "light" }
        .collectAsState(initial = "system").value

    val fontPreference = dataStore.data
        .map { preferences -> preferences[SettingsViewModel.FONT] ?: "vazir" }
        .collectAsState(initial = "vazir").value


    val colorScheme = when (themePreference) {
        "light" -> LightColorScheme
        "dark" -> DarkColorScheme
        "dim" -> DimColorScheme
        else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    val isLightTheme = colorScheme == LightColorScheme

    // Define the font family based on preference
    val fontFamily = when (fontPreference) {
        "vazir" -> VazirFontFamily
        "sahel" -> SahelFontFamily
        else -> VazirFontFamily
    }

    val selectedTypography = getTypography(fontFamily)

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as ComponentActivity).window

            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = isLightTheme
                isAppearanceLightNavigationBars = isLightTheme
            }

        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,


    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = selectedTypography,
            content = content
        )
    }
}
