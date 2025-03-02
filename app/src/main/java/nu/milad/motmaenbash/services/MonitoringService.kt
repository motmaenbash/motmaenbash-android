package nu.milad.motmaenbash.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.receivers.AppInstallReceiver
import nu.milad.motmaenbash.receivers.SmsReceiver
import nu.milad.motmaenbash.ui.MainActivity

class MonitoringService : Service() {

    private lateinit var smsReceiver: SmsReceiver
    private lateinit var appInstallReceiver: AppInstallReceiver

    private lateinit var serviceNotification: Notification


    private val SMS_CHANNEL_ID = "sms_channel"
    private val APP_INSTALL_CHANNEL_ID = "app_install_channel"
    private val FOREGROUND_NOTIFICATION_ID = 1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create notification and start foreground immediately
        if (::serviceNotification.isInitialized) {
            startForeground(FOREGROUND_NOTIFICATION_ID, serviceNotification)
        } else {
            serviceNotification = createNotification()
            startForeground(FOREGROUND_NOTIFICATION_ID, serviceNotification)
        }

        return START_STICKY  // ensures service restart if killed
    }

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        createNotificationChannel()

        serviceNotification = createNotification()
        startForeground(FOREGROUND_NOTIFICATION_ID, serviceNotification)

        // Initialize receivers
        smsReceiver = SmsReceiver()
        appInstallReceiver = AppInstallReceiver()

        // Register SMS receiver
        val smsFilter = IntentFilter().apply {
            addAction("android.provider.Telephony.SMS_RECEIVED")
        }
        registerReceiver(smsReceiver, smsFilter)

        // Register App Install receiver
        val appInstallFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
            priority = 999
        }
        registerReceiver(appInstallReceiver, appInstallFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister receivers
        unregisterReceiver(smsReceiver)
        unregisterReceiver(appInstallReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, SMS_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(" در حال محافظت توسط مطمئن باش :app")
            .setSmallIcon(R.drawable.ic_bug)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSound(null)
            .setVibrate(longArrayOf(0))
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            // Add an action to open app settings (optional)
            // .addAction(actionToOpenAppSettings())
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val smsChannel = NotificationChannel(
                SMS_CHANNEL_ID,
                "SMS Service Channel",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setSound(null, null)
                setShowBadge(false)
                enableVibration(false)
                vibrationPattern = longArrayOf(0)
                description = "کانال اعلان سرویس پایش پیامک‌ها"
            }

            val appInstallChannel = NotificationChannel(
                APP_INSTALL_CHANNEL_ID,
                "App Install Service Channel",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setSound(null, null)
                setShowBadge(false)
                enableVibration(false)
                vibrationPattern = longArrayOf(0)
                description = "کانال اعلان سرویس پایش نصب برنامه‌ها"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(smsChannel)
            manager.createNotificationChannel(appInstallChannel)
        }
    }
}