package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.receivers.AppInstallReceiver.Companion.TAG
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA1FromBytes
import nu.milad.motmaenbash.utils.HashUtils.calculateSHA1FromFile
import java.io.File

object PackageUtils {


    /**
     * Retrieves information about the specified app package.
     *
     * @param context The application context.
     * @param packageName The package name of the app to retrieve info for.
     * @return An AppData object containing information about the app.
     */
    fun getAppInfo(context: Context, packageName: String): App {
        val pm: PackageManager = context.packageManager
        return try {
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_PERMISSIONS
                )
            } else {
                pm.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS
                )
            }


            // Retrieve other app information
            val appName = pm.getApplicationLabel(packageInfo.applicationInfo).toString()
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
            val applicationInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)

            Log.d(TAG, "Packagei info: ${packageInfo.applicationInfo}")
            Log.d(TAG, "Packagei info: ${packageInfo.packageName}")
            Log.d(TAG, "Packagei info: ${packageInfo.firstInstallTime}")
            Log.d(TAG, "Packagei info: ${packageInfo.lastUpdateTime}")
            Log.d(TAG, "Packagei info: ${packageInfo.permissions}")
            Log.d(TAG, "Packagei info: ${permissions.toString()}")
            Log.d(TAG, "Packagei info: ${versionCode}")

            Log.d(TAG, "Packagei Application Name: ${pm.getApplicationLabel(applicationInfo)}")
            Log.d(TAG, "Packagei Application Package Name: ${applicationInfo.packageName}")
            Log.d(TAG, "Packagei Application Data Directory: ${applicationInfo.dataDir}")
            Log.d(TAG, "Packagei Application Source Directory: ${applicationInfo.sourceDir}")
            Log.d(
                TAG,
                "Packagei Application Public Source Directory: ${applicationInfo.publicSourceDir}"
            )
            Log.d(
                TAG, "Packagei Application Target SDK Version: ${applicationInfo.targetSdkVersion}"
            )


            // Calculate SHA-1 hash of the app's signatures
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                packageInfo.signatures
            }
            val sha1 = calculateSHA1FromBytes(signatures[0].toByteArray())

            // Calculate SHA-1 hash of the APK file
            val sourceApk = File(applicationInfo.sourceDir)
            val apksha1 = calculateSHA1FromFile(sourceApk)


            Log.d(TAG, "Package info: $packageName, SHA-1: $sha1, APK SHA-1: $apksha1")

            // Load XML MetaData
//            val xmlMetaData = applicationInfo.metaData
//            if (xmlMetaData != null) {
//                for (key in xmlMetaData.keySet()) {
//                    Log.d(TAG, "MetaData Key: $key, Value: ${xmlMetaData[key]}")
//                }
//            } else {
//                Log.d(TAG, "No XML MetaData found.")
//            }


            App(
                appName,
                packageName,
                versionCode,
                sha1,
                apksha1,
                versionName,
                firstInstallTime,
                lastUpdateTime,
                permissions?.toList() ?: emptyList()
            )
        } catch (e: Exception) {
            App(
                "Unknown", packageName, -1, "", "", "Unknown", -1, -1, emptyList()
            )
        }
    }

    /**
     * Uninstalls the specified app package.
     *
     * @param context The application context.
     * @param packageName The package name of the app to uninstall.
     */
    fun uninstallApp(context: Context, packageName: String, appName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        context.startActivity(intent)

        Toast.makeText(
            context, "برنامه ${appName} حذف شد", Toast.LENGTH_SHORT
        ).show()


    }


}
