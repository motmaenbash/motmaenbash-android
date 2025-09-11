package nu.milad.motmaenbash.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.DeadObjectException
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import nu.milad.motmaenbash.models.App
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
    fun getAppInfo(context: Context, packageName: String): App? {

        return try {

            val pm: PackageManager = context.packageManager

            // Get appropriate flags based on API level
            val packageFlags = getPackageInfoFlags()


            val packageInfo: PackageInfo = pm.getPackageInfo(packageName, packageFlags)

            // Get basic app information
            val appName =
                packageInfo.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
                    ?: "Unknown"
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
            val apkSha256 = calculateSHA256HexFromFile(sourceApk)

            // Get installation source
            val installSource = getInstallationSource(context, packageName)

            return App(
                appName,
                packageName,
                appIcon,
                versionCode,
                apkSha256,
                signSha256,
                versionName,
                installSource,
                firstInstallTime,
                lastUpdateTime,
                permissions.toList()
            )

        } catch (e: PackageManager.NameNotFoundException) {
            // Package is no longer available or accessible
            Log.w(TAG, "Package not found during security check: $packageName", e)
            null
        } catch (e: DeadObjectException) {
            Log.w(TAG, "System server died while accessing package: $packageName", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving app info: $packageName", e)
            null
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
                context.packageManager.getInstallSourceInfo(packageName)
                    .installingPackageName ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName) ?: "Unknown"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installation source for $packageName", e)
            "Unknown"
        }
    }

    /**
     * Checks if the app is installed from a trusted source
     *
     * @param context The application context for database access
     * @param installSource The installer package name
     * @return true if installed from a trusted market
     */
    fun isFromTrustedSource(context: Context, installSource: String?): Boolean {
        if (installSource.isNullOrBlank()) {
            Log.d(TAG, "Install source is null or blank")
            return false
        }

        return try {
            val installer = installSource.trim().lowercase()
            val dbHelper = DatabaseHelper(context)
            val isTrusted = dbHelper.isTrustedMarketPackage(installer)

        Log.d(TAG, "Install source: $installer, trusted: $isTrusted")
            isTrusted
        } catch (e: Exception) {
            Log.e(TAG, "Error checking trusted source for $installSource", e)
            false
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

    fun isSystemApp(packageInfo: PackageInfo): Boolean {
        return packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun isSystemApp(context: Context, packageName: String): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val appInfo = packageInfo.applicationInfo
            // consistent null checking
            appInfo?.let {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            } ?: false

        } catch (_: Exception) {
            false
        }
    }


}