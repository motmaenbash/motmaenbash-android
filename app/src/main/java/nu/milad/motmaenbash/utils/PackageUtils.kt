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
     * @param installSource The installer package name
     * @return true if installed from a known trusted market
     */
    fun isFromTrustedSource(installSource: String?): Boolean {
        if (installSource.isNullOrBlank()) {
            Log.d(TAG, "Install source is null or blank")
            return false
        }
        val installer = installSource.trim()
        val isTrusted = TRUSTED_MARKETS.contains(installer)

        Log.d(TAG, "Install source: $installer, trusted: $isTrusted")
        return isTrusted
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

    /**
     * Checks if the app has any risky permission combinations
     */
    fun hasRiskyPermissionCombination(permissions: List<String>): Boolean {
        val permissionSet = permissions.toSet()

        return HIGH_RISK_PERMISSION_COMBINATIONS.any { riskyCombo ->
            riskyCombo.all { permission -> permissionSet.contains(permission) }
        }
    }

    /**
     * Gets a list of detected risky permission combinations
     */
    fun getDetectedRiskyPermissionCombinations(permissions: List<String>): List<Set<String>> {
        val permissionSet = permissions.toSet()

        return HIGH_RISK_PERMISSION_COMBINATIONS.filter { riskyCombo ->
            riskyCombo.all { permission -> permissionSet.contains(permission) }
        }
    }

    /**
     * Gets Persian description for risky permission combinations
     */
    fun getRiskyPermissionCombinationDescription(combo: Set<String>): String {
        val persianNames = combo.mapNotNull { permission ->
            PERMISSION_TITLES[permission]
        }

        return if (persianNames.isNotEmpty()) {
            persianNames.joinToString(" + ")
        } else {
            combo.joinToString(" + ")
        }
    }

    private val TRUSTED_MARKETS = setOf(
        // Google Play Store
        "com.android.vending",
        // Samsung Galaxy Store
        "com.sec.android.app.samsungapps",
        // Huawei AppGallery
        "com.huawei.appmarket",
        // Xiaomi GetApps
        "com.xiaomi.market",
        "com.xiaomi.mipicks",
        // Oppo App Market
        "com.oppo.market",
        "com.heytap.market",
        // Vivo App Store
        "com.vivo.appstore",
        // Honor AppGallery
        "com.hihonor.appgallery",
        // Amazon Appstore
        "com.amazon.venezia",
        // F-Droid (Open Source)
        "org.fdroid.fdroid",
        // APKMirror Installer
        "com.apkmirror.helper.prod",
        // APKPure
        "com.apkpure.aegon",
        // Uptodown
        "com.uptodown",
        "com.uptodown.installer",
        // TapTap
        "com.taptap",
        "com.taptap.global",        // TapTap Global
        "com.taptap.global.lite",   // TapTap Lite
        // QooApp
        "com.qooapp.qoohelper",
        // Iranian Markets
        "com.farsitel.bazaar",         // Bazaar
        "ir.mservices.market",         // Myket
        // Yandex Store
        "ru.yandex.store",             // Yandex Store (Russia)
        "com.yandex.store",
        // Aptoide
        "cm.aptoide.pt",
        "cm.aptoide.lite",    // Aptoide Lite
        // Tencent MyApp
        "com.tencent.android.qqdownloader",
        // Developer Tools - These are NOT actual app stores but Android system components
        "com.android.development",     // Android Development Tools (System)
        "com.android.shell",           // Android Shell (System)
    )

    private val HIGH_RISK_PERMISSION_COMBINATIONS = listOf(
        // SMS & Contact combinations (Banking trojans)
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS),
        setOf(Manifest.permission.INTERNET, Manifest.permission.READ_SMS),
        setOf(Manifest.permission.INTERNET, Manifest.permission.RECEIVE_SMS),
        // Accessibility service abuse
        setOf(Manifest.permission.INTERNET, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
        setOf(Manifest.permission.SEND_SMS, Manifest.permission.BIND_ACCESSIBILITY_SERVICE),
    )

    private val PERMISSION_TITLES = mapOf(
        Manifest.permission.INTERNET to "دسترسی به اینترنت",
        Manifest.permission.SEND_SMS to "ارسال پیامک",
        Manifest.permission.READ_SMS to "خواندن پیامک‌ها",
        Manifest.permission.RECEIVE_SMS to "دریافت پیامک",
        Manifest.permission.READ_CONTACTS to "دسترسی به مخاطب‌ها",
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE to "دسترسی کامل به صفحه"
    )


}