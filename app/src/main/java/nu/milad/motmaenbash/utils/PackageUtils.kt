package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import nu.milad.motmaenbash.models.App
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA256FromFile
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA256HexFromBytes
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA256HexFromFile
import java.io.File

/**
 * Utility class for retrieving and processing package information.
 */
object PackageUtils {
    private const val TAG = "PackageUtils"

    /**
     * Retrieves information about the specified app package.
     *
     * @param context The application context.
     * @param packageName The package name of the app to retrieve info for.
     * @return An AppData object containing information about the app.
     */
    fun getAppInfo(context: Context, packageName: String): App {

        val pm: PackageManager = context.packageManager

        // Get appropriate flags based on API level
        val packageFlags = getPackageInfoFlags()

        try {

            val packageInfo: PackageInfo = pm.getPackageInfo(packageName, packageFlags)


            // Get basic app information
            val appName = packageInfo.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
                ?: "Unknown App Name"
            val appIcon = pm.getApplicationIcon(packageName)

            val versionName = packageInfo.versionName ?: "Unknown"

            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)

            val firstInstallTime = packageInfo.firstInstallTime
            val lastUpdateTime = packageInfo.lastUpdateTime
            val permissions = packageInfo.requestedPermissions ?: arrayOf()

            // Get signing information
            val signSha256 = getSignatureSha256(packageInfo)

            val applicationInfo: ApplicationInfo =
                packageInfo.applicationInfo ?: pm.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )

            // Calculate APK file hashes
            val sourceApk = File(applicationInfo.sourceDir)
            val apksha256 = calculateSHA256HexFromFile(sourceApk)

            // Get installation source
            val installSource = getInstallationSource(context, packageName)


            return App(
                appName,
                packageName,
                appIcon,
                versionCode,
                apksha256,
                signSha256,
                versionName,
                firstInstallTime,
                lastUpdateTime,
                permissions.toList()
            )


        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package not found: $packageName", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving app info for $packageName", e)
            throw e
        }


    }

    /**
     * Returns the appropriate flags for PackageManager.getPackageInfo based on API level
     */
    private fun getPackageInfoFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_PROVIDERS
        }
    }

    /**
     * Gets SHA256 signature hash based on API level
     */
    private fun getSignatureSha256(packageInfo: PackageInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // On API level 28 and above, use signingInfo
            packageInfo.signingInfo?.let { signingInfo ->
                (signingInfo.apkContentsSigners
                    ?: signingInfo.signingCertificateHistory).firstOrNull()
                    ?.let { calculateSHA256HexFromBytes(it.toByteArray()) } ?: "No SHA256 found"
            } ?: "No SHA256 found"
        } else {
            // On devices below API level 28, use the legacy signatures
            @Suppress("DEPRECATION")
            packageInfo.signatures?.firstOrNull()?.let { signature ->
                calculateSHA256HexFromBytes(signature.toByteArray())
            } ?: "No SHA256 found"

        }
    }

    /**
     * Gets the installation source of the app
     */
    private fun getInstallationSource(context: Context, packageName: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val installSourceInfo = context.packageManager.getInstallSourceInfo(packageName)
                val installingPackageName = installSourceInfo.installingPackageName ?: "Unknown"
                val initiatingPackageName = installSourceInfo.initiatingPackageName ?: "Unknown"
                val originatingPackageName = installSourceInfo.originatingPackageName ?: "Unknown"

                "Installing: $installingPackageName, Initiating: $initiatingPackageName, Originating: $originatingPackageName"
            } else {
                @Suppress("DEPRECATION")
                val installer =
                    context.packageManager.getInstallerPackageName(packageName) ?: "Unknown"

                "Installer: $installer"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installation source for $packageName", e)
            "Error determining source: ${e.message}"
        }
    }

    /**
     * Creates an intent to uninstall the specified app
     *
     * @param packageName The package name of the app to uninstall
     * @return Intent configured for uninstallation
     */
    fun uninstallApp(packageName: String): Intent {
        return Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
    }
}