package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo

class ScanUtils(private val context: Context) {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)


    fun isSystemApp(packageInfo: PackageInfo): Boolean {
        return packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
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
