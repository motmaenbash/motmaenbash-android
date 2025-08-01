package nu.milad.motmaenbash.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.receivers.AppInstallReceiver
import nu.milad.motmaenbash.receivers.SmsReceiver
import nu.milad.motmaenbash.ui.activities.MainActivity
import nu.milad.motmaenbash.utils.GuardUtils
import nu.milad.motmaenbash.utils.NotificationUtils
import nu.milad.motmaenbash.utils.PermissionManager

class MonitoringService : Service() {

    private lateinit var smsReceiver: SmsReceiver
    private lateinit var appInstallReceiver: AppInstallReceiver
    private lateinit var guardUtils: GuardUtils

    // Notification objects
    private lateinit var serviceNotification: Notification
    private lateinit var notificationBuilder: NotificationCompat.Builder

    // Messages for notification rotation
    private val notificationMessages = listOf(
        " در حال محافظت توسط «مطمئن باش»",
        "سپرهای فعال: نصب برنامه"  // Default value, will be updated during rotation
    )

    private var currentMessageIndex = 0

    // Handler for updating notifications
    private val updateNotificationHandler = Handler(Looper.getMainLooper())
    private lateinit var updateNotificationRunnable: Runnable

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1

        private const val UPDATE_INTERVAL = 15 * 60 * 1_000L // 15 minutes

        // Action to force update the notification immediately
        const val ACTION_UPDATE_NOTIFICATION = "nu.milad.motmaenbash.ACTION_UPDATE_NOTIFICATION"
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if this is a request to update the notification
        if (intent?.action == ACTION_UPDATE_NOTIFICATION) {
            // Force an immediate notification update
            updateNotification()
            return START_STICKY
        }

        // Create notification and start foreground immediately
        if (::serviceNotification.isInitialized) {
            startForeground(FOREGROUND_NOTIFICATION_ID, serviceNotification)
        } else {
            initializeNotification()
            startForeground(FOREGROUND_NOTIFICATION_ID, serviceNotification)
        }

        return START_STICKY  // ensures service restart if killed
    }

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationUtils.createMonitoringServiceChannel(this)

        // Initialize GuardUtils
        guardUtils = GuardUtils(this)


        // Initialize notification and start foreground service
        initializeNotification()
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

        // Initialize and start notification update runnable
        setupNotificationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up receivers
        unregisterReceiver(smsReceiver)
        unregisterReceiver(appInstallReceiver)

        // Stop notification updates
        updateNotificationHandler.removeCallbacks(updateNotificationRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Initialize notification builder with common properties
     */
    private fun initializeNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder = NotificationCompat.Builder(
            this,
            NotificationUtils.MONITORING_SERVICE_CHANNEL_ID
        )
            .setContentTitle(getString(R.string.app_name))
            .setContentText(notificationMessages[currentMessageIndex])
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSound(null)
            .setVibrate(longArrayOf(0))
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)

        // Build initial notification
        serviceNotification = notificationBuilder.build()
    }

    /**
     * Set up periodic notification updates
     */
    private fun setupNotificationUpdates() {
        updateNotificationRunnable = Runnable {
            // Only update if service is still running
            if (::notificationBuilder.isInitialized) {
                updateNotification()
                // Schedule next update
                updateNotificationHandler.postDelayed(
                    updateNotificationRunnable,
                    UPDATE_INTERVAL
                )
            }
        }

        // Start updates
        updateNotificationHandler.post(updateNotificationRunnable)
    }

    /**
     * Update notification text with the next message
     * This method is both called periodically and can be triggered manually
     */
    private fun updateNotification() {
        // Check overlay permission and set notification messages
        val permissionManager = PermissionManager(this)

        // For forced updates, prioritize showing active guards
        val shouldShowActiveGuards = currentMessageIndex == notificationMessages.size - 1

        // Get the notification text
        val notificationText = if (!permissionManager.checkPermission(PermissionType.OVERLAY)) {
            "نیاز به فعال‌سازی دسترسی نمایش روی برنامه‌ها"
        } else if (shouldShowActiveGuards) {
            // Show current active guards
            getActiveGuardsMessage()
        } else {
            notificationMessages[currentMessageIndex]
        }

        // Increment message index for next time (for periodic updates)
        currentMessageIndex = (currentMessageIndex + 1) % notificationMessages.size

        // Update existing notification with new text
        notificationBuilder.setContentText(notificationText)

        // Update notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build())
    }


    /**
     * Generate a message showing active guards
     */
    private fun getActiveGuardsMessage(): String {
        val activeGuardsList = guardUtils.getActiveGuardsTitles(this)
        val guardsText = activeGuardsList.joinToString(" | ") { it.title }
        return "سپرهای فعال: $guardsText"
    }
}