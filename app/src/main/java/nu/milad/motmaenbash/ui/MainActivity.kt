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

//        val dbHelper = DatabaseHelper(this)

        // Start the monitoring service
        val serviceIntent = Intent(this, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            MotmaenBashTheme {
                val navController = rememberNavController()
                val targetScreen = handleIntent(intent.data)
//                AppNavigation(navController, targetScreen, dbHelper)
                AppNavigation(navController, targetScreen)

            }
        }
    }


}


@Composable
fun AppNavigation(
//    navController: NavHostController, startDestination: String, dbHelper: DatabaseHelper
    navController: NavHostController, startDestination: String
) {

    val context = LocalContext.current

    // Handle back press
    BackHandler(enabled = true) {
        if (navController.currentBackStackEntry?.destination?.route == NavRoutes.MAIN_SCREEN) {
            // If on the main screen, close the app
            (context as? ComponentActivity)?.finish()
        } else {
            // Otherwise, navigate back
            navController.navigateUp()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(NavRoutes.MAIN_SCREEN) { MainScreen(navController) }
        composable(NavRoutes.ABOUT_SCREEN) { AboutScreen(navController) }
        composable(NavRoutes.USER_REPORT_SCREEN) { UserReportScreen(navController) }
//        composable(NavRoutes.URL_SCAN_SCREEN) { UrlScanScreen(dbHelper = dbHelper) }
        composable(NavRoutes.URL_SCAN_SCREEN) { UrlScanScreen(navController) }
        composable(NavRoutes.APP_SCAN_SCREEN) { AppScanScreen(navController) }


        composable(NavRoutes.FAQ_SCREEN) { InfoListScreen(navController, Pages.FAQ) }
        composable(NavRoutes.PERMISSION_SCREEN) { InfoListScreen(navController, Pages.PERMISSION) }

        composable(NavRoutes.SETTINGS_SCREEN) {
            SettingsScreen(navController, settingsManager = SettingsManager(context.dataStore))
//            SettingsScreen()
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
//        AppNavigation(navController, NavRoutes.MAIN_SCREEN, DatabaseHelper(LocalContext.current))
        AppNavigation(rememberNavController(), NavRoutes.MAIN_SCREEN)
    }
}