package nu.milad.motmaenbash.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import nu.milad.motmaenbash.R


object NotificationUtils {

    const val MAIN_NOTIFICATION_CHANNEL_ID = "motmaenbash_main_channel"
    const val MONITORING_SERVICE_CHANNEL_ID = "motmaenbash_monitoring_service_channel"


    fun createMainNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                MAIN_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.motmaenbash_general_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.general_channel_description)
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }


    fun createMonitoringServiceChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                MONITORING_SERVICE_CHANNEL_ID,
                context.getString(R.string.motmaenbash_monitoring_service_channel),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setSound(null, null)
                setShowBadge(false)
                enableVibration(false)
                vibrationPattern = longArrayOf(0)
                description = context.getString(R.string.monitoring_service_channel_description)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }


}