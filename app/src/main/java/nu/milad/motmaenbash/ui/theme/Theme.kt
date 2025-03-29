package nu.milad.motmaenbash.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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


private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    secondary = Grey,
    tertiary = ColorPrimaryDark,
    primaryContainer = ColorPrimary,
    // Background and surface
    background = BackgroundLightGray,
    surface = White,
    // Content colors
    onPrimary = White,
    onPrimaryContainer = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun MotmaenBashTheme(
    content: @Composable () -> Unit
) {

    val context = LocalContext.current
    val dataStore = context.dataStore

    val themePreference = dataStore.data
        .map { preferences -> preferences[SettingsViewModel.THEME] ?: "system" }
        .collectAsState(initial = "system").value

    val fontPreference = dataStore.data
        .map { preferences -> preferences[SettingsViewModel.FONT] ?: "vazir" }
        .collectAsState(initial = "vazir").value


    val colorScheme = when (themePreference) {
        "light" -> LightColorScheme
        "dark" -> LightColorScheme
        else -> LightColorScheme
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
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = selectedTypography,
            content = content
        )
    }
}