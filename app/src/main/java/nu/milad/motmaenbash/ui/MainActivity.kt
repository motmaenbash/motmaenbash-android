package nu.milad.motmaenbash.ui


import AboutScreen
import AppScanScreen
import MainScreen
import UserReportScreen
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.consts.Pages
import nu.milad.motmaenbash.services.MonitoringService
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.SettingsManager
import nu.milad.motmaenbash.utils.dataStore

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the monitoring service
        val serviceIntent = Intent(
            this,
            MonitoringService::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            MotmaenBashTheme {
                val navController = rememberNavController()
                val targetScreen = handleIntent(intent.data)
                CompositionLocalProvider(LocalNavController provides navController) {
                    AppNavigation(targetScreen)
                }


            }
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

        if (navController.currentBackStackEntry?.destination?.route == NavRoutes.MAIN_SCREEN) {
            // Close the app when on main screen
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
        composable(NavRoutes.ABOUT_SCREEN) { AboutScreen() }
        composable(NavRoutes.USER_REPORT_SCREEN) { UserReportScreen() }
        composable(NavRoutes.URL_SCAN_SCREEN) { UrlScanScreen() }
        composable(NavRoutes.APP_SCAN_SCREEN) { AppScanScreen() }
        composable(NavRoutes.FAQ_SCREEN) { InfoListScreen(Pages.FAQ) }
        composable(NavRoutes.PERMISSION_SCREEN) {
            InfoListScreen(
                Pages.PERMISSION
            )
        }
        composable(NavRoutes.SETTINGS_SCREEN) {
            SettingsScreen(settingsManager = SettingsManager(context.dataStore))
        }
    }
}


private fun handleIntent(uri: Uri?): String {
    return when (uri?.scheme) {
        "app" -> when (uri.schemeSpecificPart) {
            "//scan" -> NavRoutes.APP_SCAN_SCREEN
            "//url_scan" -> NavRoutes.URL_SCAN_SCREEN
            "//report" -> NavRoutes.USER_REPORT_SCREEN
            else -> NavRoutes.MAIN_SCREEN
        }

        else -> NavRoutes.MAIN_SCREEN
    }
}


@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MotmaenBashTheme {
        AppNavigation(NavRoutes.MAIN_SCREEN)
    }
}