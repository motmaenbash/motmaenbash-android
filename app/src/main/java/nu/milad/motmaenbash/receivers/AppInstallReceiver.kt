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
import nu.milad.motmaenbash.models.AppThreatType
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.utils.PermissionAnalyzer

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
                    evaluateAppThreats(context, packageName)
                }
            }

            Intent.ACTION_PACKAGE_REPLACED -> {
                evaluateAppThreats(context, packageName)
            }

            Intent.ACTION_PACKAGE_FIRST_LAUNCH -> {
                // Check against database on first launch
                evaluateAppThreats(context, packageName)
            }
        }
    }


    private fun evaluateAppThreats(context: Context, packageName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting security check for app: $packageName")

            try {
                val app = PackageUtils.getAppInfo(context, packageName) ?: return@launch
                val dbHelper = DatabaseHelper(context)

                val threatType = when {
                    dbHelper.isAppFlagged(packageName, app.apkHash, app.sighHash) ->
                        AppThreatType.MALWARE

                    !PackageUtils.isFromTrustedSource(context, app.installSource) &&
                            !dbHelper.isTrustedSideloadApp(packageName, app.sighHash) -> {
                        // Calculate DEX hash
                        val dexHash = PackageUtils.calculateDexHash(context, packageName)
                        if (dexHash != null && dbHelper.isAppFlaggedByDex(dexHash)) {
                            AppThreatType.MALWARE
                        } else if (PermissionAnalyzer.hasRiskyPermissionCombination(app.permissions)) {
                            AppThreatType.RISKY_PERMISSIONS
                        } else {
                            null
                        }
                    }


                    else -> null
                }

                if (threatType != null) {
                    withContext(Dispatchers.Main) {
                        when (threatType) {
                            AppThreatType.MALWARE -> {
                                AlertUtils.showAlert(
                                    context = context,
                                    alertType = Alert.AlertType.APP_FLAGGED,
                                    alertLevel = Alert.AlertLevel.ALERT,
                                    param1 = packageName,
                                    param2 = app.appName,
                                )
                            }

                            AppThreatType.RISKY_PERMISSIONS -> {
                                val riskyPermissionCombinations =
                                    PermissionAnalyzer.getDetectedRiskyPermissionCombinations(app.permissions)

                                // Collect all descriptions
                                val descriptions =
                                    riskyPermissionCombinations.mapIndexed { index, combo ->
                                        PermissionAnalyzer.getRiskyPermissionCombinationDescription(
                                            combo
                                        )
                                    }

                                val param3 = descriptions.joinToString("\n")

                                AlertUtils.showAlert(
                                    context = context,
                                    alertType = Alert.AlertType.APP_RISKY_INSTALL,
                                    alertLevel = Alert.AlertLevel.WARNING,
                                    param1 = packageName,
                                    param2 = app.appName,
                                    param3 = param3
                                )
                            }
                        }

                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error checking app against database: $packageName", e)
            }

        }
    }
}