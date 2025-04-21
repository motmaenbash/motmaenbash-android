package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import nu.milad.motmaenbash.models.App
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA1FromBytes
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA1FromFile
import java.io.File

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

            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS
                )
            } else {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS or PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS
                )
            }


            // Retrieve other app information
            val appName = packageInfo.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
                ?: "Unknown App Name"


            val appIcon = pm.getApplicationIcon(packageName)

            val versionName = packageInfo.versionName ?: "Unknown"

            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            val firstInstallTime = packageInfo.firstInstallTime
            val lastUpdateTime = packageInfo.lastUpdateTime
            val permissions = packageInfo.requestedPermissions ?: arrayOf()


            // Retrieve ApplicationInfo for additional details
            val applicationInfo: ApplicationInfo =
                pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

            val developerName = applicationInfo.metaData?.getString("developer_name")
            Log.d(TAG, "Developer Name: $developerName")


        val metaData = applicationInfo.metaData

        if (metaData != null) {
            for (key in metaData.keySet()) {
                val value = metaData[key]
                Log.d(TAG, "MetaData Key: $key, Value: $value")
            }
        } else {
            Log.d(TAG, "No XML MetaData found.")
        }


            // Calculate SHA-1 hash of the app's signatures


            // Retrieve the signing information safely based on API level
            val sha1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // On API level 28 and above, use signingInfo

            packageInfo.signingInfo?.let { signingInfo ->
                (signingInfo.apkContentsSigners
                    ?: signingInfo.signingCertificateHistory).firstOrNull()
                    ?.let { calculateSHA1FromBytes(it.toByteArray()) } ?: "No SHA1 found"
            } ?: "No SHA1 found"
            } else {
                // On devices below API level 28, use the legacy signatures
            packageInfo.signatures?.firstOrNull()?.let { signature ->
                calculateSHA1FromBytes(signature.toByteArray())
            } ?: "No SHA1 found"
            }


            // Calculate SHA-1 hash of the APK file
            val sourceApk = File(applicationInfo.sourceDir)
            val apksha1 = calculateSHA1FromFile(sourceApk)


        return App(
                appName,
                packageName,
                appIcon,
                versionCode,
                sha1,
                apksha1,
                versionName,
                firstInstallTime,
                lastUpdateTime,
            permissions.toList()
            )

    }

    fun uninstallApp(packageName: String): Intent {
        return Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
    }


}