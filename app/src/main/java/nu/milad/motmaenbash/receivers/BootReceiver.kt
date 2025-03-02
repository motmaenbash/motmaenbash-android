package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import nu.milad.motmaenbash.services.MonitoringService

class BootReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            startMonitoringService(context)
        }
    }

    private fun startMonitoringService(context: Context) {

        val serviceIntent = Intent(context, MonitoringService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

    }
}
