package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.receivers.AppInstallReceiver.Companion.TAG
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object AppUtils {

    /**
     * Calculates the SHA-1 hash of the provided byte array.
     *
     * @param signature The byte array to hash.
     * @return The SHA-1 hash as a Base64 encoded string.
     */
    private fun calculateSHA1(signature: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(signature)
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }

    /**
     * Calculates the SHA-1 hash of the provided file.
     *
     * @param file The APK file to hash.
     * @return The SHA-1 hash as a Base64 encoded string.
     */
    private fun calculateSHA1FromFile(file: File): String {
        val md = MessageDigest.getInstance("SHA-1")
        val fis = FileInputStream(file)
        val buffer = ByteArray(8192)
        var len = fis.read(buffer)
        while (len != -1) {
            md.update(buffer, 0, len)
            len = fis.read(buffer)
        }
        fis.close()
        val digest = md.digest()
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }

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
            val packageInfo: PackageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_PERMISSIONS
                    )
            } else {
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS
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

            // Retrieve ApplicationInfo for additional details
            val applicationInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)

            // Calculate SHA-1 hash of the app's signatures
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                packageInfo.signatures
            }
            val sha1 = calculateSHA1(signatures[0].toByteArray())

            // Calculate SHA-1 hash of the APK file
            val sourceApk = File(applicationInfo.sourceDir)
            val apksha1 = calculateSHA1FromFile(sourceApk)



            App(
                appName,
                packageName,
                versionCode,
                sha1,
                apksha1,
            )
        } catch (e: Exception) {
            App(
                "Unknown",
                packageName,
                -1,
                "",
                "",
            )
        }
    }
}
