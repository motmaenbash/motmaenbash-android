package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import nu.milad.motmaenbash.consts.AppConstants.APP_PREFERENCES
import nu.milad.motmaenbash.model.App

class ScanUtils(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)


    private val packageManager: PackageManager = context.packageManager

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)


    fun getNonSystemInstalledPackages(): MutableList<PackageInfo> {
        // Get all installed packages
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        // Filter out system apps
        return installedPackages.filterTo(mutableListOf()) { packageInfo ->
            !isSystemApp(packageInfo) && packageInfo.packageName != context.packageName
        }

    }


    fun isSystemApp(packageInfo: PackageInfo): Boolean {
        return packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }


    fun isAppSuspicious(app: App): Boolean {
        return dbHelper.isAppFlagged(app.packageName, app.sha1, app.apkSha1)
    }


    fun setLastScanTime(lastUpdateTime: Long) {
        with(sharedPreferences.edit()) {
            putLong("last_scan_time", lastUpdateTime)
            apply()
        }
    }

    fun getLastScanTime(): Long {
        val lastScanTime = sharedPreferences.getLong("last_scan_time", 0)
        return lastScanTime
    }


    fun getLastScanTimeAgo(): String {

        val lastUpdateTime = getLastScanTime()
        return if (lastUpdateTime == 0L) {
            "هنوز اسکنی انجام نشده"
        } else {
            DateUtils.timeAgo(lastUpdateTime)
        }


    }
}
