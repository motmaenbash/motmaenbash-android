package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
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
        Log.d(TAG, "Starting security check for app: $packageName")

        try {
            val dbHelper = DatabaseHelper(context)
            val appInfo = PackageUtils.getAppInfo(context, packageName)

            val isAppFlagged = dbHelper.isAppFlagged(packageName, appInfo.apkHash, appInfo.sighHash)

            if (isAppFlagged) {
                // App found suspicious in the database, alert the user
                AlertUtils.showAlert(
                    context = context,
                    alertType = Alert.AlertType.APP_FLAGGED,
                    alertLevel = Alert.AlertLevel.ALERT,
                    param1 = packageName,
                    param2 = appInfo.appName,
                )
            } else if (!PackageUtils.isFromTrustedSource(appInfo.installSource) &&
                PackageUtils.hasRiskyPermissionCombination(appInfo.permissions)
            ) {

                val riskyPermissionCombinations =
                    PackageUtils.getDetectedRiskyPermissionCombinations(appInfo.permissions)

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
                    param2 = appInfo.appName,
                    param3 = param3
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Package was broadcast but is no longer available or accessible
            Log.w(TAG, "Package not found during security check: $packageName", e)
            // No need to alert the user as the package isn't accessible
        } catch (e: Exception) {
            Log.e(TAG, "Error checking app against database: $packageName", e)
        }
    }
}