package nu.milad.motmaenbash.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.services.MonitoringService

class ServiceUtils {

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(ActivityManager::class.java)
        } else {
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        }

        return manager?.getRunningServices(Int.MAX_VALUE)
            ?.any { it.service.className == serviceClass.name } ?: false
    }


    fun startMonitoringService(context: Context) {
        // Check notification permission for Android 13 and higher
        val permissionManager = PermissionManager(context)
        if (!permissionManager.checkPermission(PermissionType.NOTIFICATIONS)) {
            return
        }
        

        // Start the monitoring service if not already running
        if (!ServiceUtils().isServiceRunning(context, MonitoringService::class.java)) {
            val serviceIntent = Intent(context, MonitoringService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
