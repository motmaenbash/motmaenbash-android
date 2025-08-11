package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.PackageUtils

class AppInstallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AppInstallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val packageName = intent.data?.schemeSpecificPart ?: return
        Log.d(TAG, "Received broadcast for package: $packageName, action: ${intent.action}")

        val isSystemApp = PackageUtils.isSystemApp(context, packageName)
        Log.d(TAG, "Received broadcast is for system app: $isSystemApp")
        // Skip system apps
        if (isSystemApp) {
            Log.d(TAG, "Skipping security check for system app: $packageName")
            return
        }

        val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)


        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                // Skip if this is just an update to an existing app
                if (!isReplacing) {
                    checkAppAgainstDatabase(context, packageName)
                }
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                checkAppAgainstDatabase(context, packageName)
            }

            Intent.ACTION_PACKAGE_FIRST_LAUNCH -> {
                // Check against database on first launch
                checkAppAgainstDatabase(context, packageName)
            }
        }
    }


    private fun checkAppAgainstDatabase(context: Context, packageName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting security check for app: $packageName")

            try {
                val app = PackageUtils.getAppInfo(context, packageName) ?: return@launch

                val dbHelper = DatabaseHelper(context)
                val isAppFlagged = dbHelper.isAppFlagged(
                    packageName,
                    app.apkHash,
                    app.sighHash
                )

                if (isAppFlagged) {
                    withContext(Dispatchers.Main) {
                        AlertUtils.showAlert(
                            context = context,
                            alertType = Alert.AlertType.APP_FLAGGED,
                            alertLevel = Alert.AlertLevel.ALERT,
                            param1 = packageName,
                            param2 = app.appName,
                        )
                    }
                    return@launch
                } else if (!PackageUtils.isFromTrustedSource(app.installSource) &&
                    PackageUtils.hasRiskyPermissionCombination(app.permissions)
                ) {

                    val riskyPermissionCombinations =
                        PackageUtils.getDetectedRiskyPermissionCombinations(app.permissions)

                    // Collect all descriptions
                    val descriptions = riskyPermissionCombinations.mapIndexed { index, combo ->
                        PackageUtils.getRiskyPermissionCombinationDescription(combo)
                    }
                    // Format the descriptions for param3
                    val param3 = when {
                        descriptions.isEmpty() -> ""
                        descriptions.size <= 5 -> descriptions.joinToString("\nو ")
                        else -> {
                            val firstFour = descriptions.take(4).joinToString("\nو ")
                            val remainingCount = descriptions.size - 4
                            "$firstFour\nو $remainingCount ترکیب دیگر..."
                        }
                    }

                    AlertUtils.showAlert(
                        context = context,
                        alertType = Alert.AlertType.APP_RISKY_INSTALL,
                        alertLevel = Alert.AlertLevel.WARNING,
                        param1 = packageName,
                        param2 = app.appName,
                        param3 = param3
                    )
                }


            } catch (e: Exception) {
                Log.e(TAG, "Error checking app against database: $packageName", e)
            }

        }
    }
}