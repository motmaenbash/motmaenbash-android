package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import nu.milad.motmaenbash.utils.ServiceUtils

class MonitoringStarterReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Only start monitoring service on Android 8 (Oreo) and above
        // On older Android versions, we use manifest-declared receivers instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON",
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    ServiceUtils().startMonitoringService(context)
                }
            }
        }
    }

}
