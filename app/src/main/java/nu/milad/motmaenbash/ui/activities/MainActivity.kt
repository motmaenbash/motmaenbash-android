package nu.milad.motmaenbash.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants.PREF_KEY_HAS_SEEN_INTRO
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.consts.Pages
import nu.milad.motmaenbash.ui.screens.AboutScreen
import nu.milad.motmaenbash.ui.screens.AppScanScreen
import nu.milad.motmaenbash.ui.screens.InfoListScreen
import nu.milad.motmaenbash.ui.screens.IntroScreen
import nu.milad.motmaenbash.ui.screens.MainScreen
import nu.milad.motmaenbash.ui.screens.SettingsScreen
import nu.milad.motmaenbash.ui.screens.UrlScanScreen
import nu.milad.motmaenbash.ui.screens.UserReportScreen
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.ServiceUtils
import nu.milad.motmaenbash.utils.dataStore

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        lifecycleScope.launch {

            val startDestination = handleIntent(intent, this@MainActivity)

            setContent {
                MotmaenBashTheme {
                    val navController = rememberNavController()
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CompositionLocalProvider(LocalNavController provides navController) {
                            AppNavigation(startDestination)
                        }
                    }
                }

            }
        }


        // Only start monitoring service on Android 8 (Oreo) and above
        // On older Android versions, we use manifest-declared receivers instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ServiceUtils().startMonitoringService(this)
        }
    }


}

val LocalNavController = compositionLocalOf<NavHostController> {
    error("No NavController provided")
}

@Composable
fun AppNavigation(
    startDestination: String
) {
    val context = LocalContext.current
    val navController = LocalNavController.current

    // Handle back press
    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.destination?.route == NavRoutes.MAIN_SCREEN ||
            navController.currentBackStackEntry?.destination?.route == NavRoutes.PERMISSION_INTRO_SCREEN
        ) {
            // Close the app when on Main or Intro screen
            (context as? ComponentActivity)?.finish()

            // Navigate back or return to main screen
        } else if (!navController.navigateUp()) {
            navController.navigate(NavRoutes.MAIN_SCREEN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.MAIN_SCREEN) { MainScreen() }
        composable(NavRoutes.PERMISSION_INTRO_SCREEN) { IntroScreen() }
        composable(NavRoutes.ABOUT_SCREEN) { AboutScreen() }
        composable(NavRoutes.USER_REPORT_SCREEN) { UserReportScreen() }
        composable(NavRoutes.URL_SCAN_SCREEN) { UrlScanScreen() }
        composable(NavRoutes.APP_SCAN_SCREEN) { AppScanScreen() }
        composable(NavRoutes.FAQ_SCREEN) { InfoListScreen(Pages.FAQ) }
        composable(NavRoutes.PERMISSION_SCREEN) { InfoListScreen(Pages.PERMISSION) }
        composable(NavRoutes.CHANGELOG_SCREEN) { InfoListScreen(Pages.CHANGELOG) }
        composable(NavRoutes.SETTINGS_SCREEN) { SettingsScreen() }
    }
}

private suspend fun handleIntent(intent: Intent?, context: Context): String {
    val dataStore = context.dataStore

    val isIntroShown = dataStore.data.firstOrNull()?.get(
        booleanPreferencesKey(PREF_KEY_HAS_SEEN_INTRO)

    ) ?: false

    if (!isIntroShown) {
        return NavRoutes.PERMISSION_INTRO_SCREEN
    }

    val data = intent?.data

    return when {
        // App shortcut scheme handling
        data?.scheme == "app" -> when (data.schemeSpecificPart) {
            "//scan" -> NavRoutes.APP_SCAN_SCREEN
            "//url_scan" -> NavRoutes.URL_SCAN_SCREEN
            "//report" -> NavRoutes.USER_REPORT_SCREEN
            else -> NavRoutes.MAIN_SCREEN
        }

        // process browser intent (ACTION_SEND, PROCESS_TEXT)
        intent?.action in listOf(
            Intent.ACTION_SEND,
            Intent.ACTION_PROCESS_TEXT
        ) -> NavRoutes.URL_SCAN_SCREEN

        else -> NavRoutes.MAIN_SCREEN
    }
}