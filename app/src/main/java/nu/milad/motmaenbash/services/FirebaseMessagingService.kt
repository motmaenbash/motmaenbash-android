package nu.milad.motmaenbash.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.MainActivity
import nu.milad.motmaenbash.utils.NotificationUtils
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val tag = "MyFirebaseMsgService"
        private val notificationId = AtomicInteger(0)
        private const val KEY_IMAGE_URL = "image_url"
        private const val KEY_TITLE = "title"
        private const val KEY_BODY = "body"
    }


//todo: delete

// cQckhoRGTBCt-34tTuJp3z:APA91bFO6o9jxzX5sf2eQYXf0VFJs0LsXw7lFSu2xcZWK8G6pU9bMbIg-tANwFiQihQ7SKy0-l7Hh6Vqurq-Es7bAril18L0xLFM4Md8WkitGhcy8qSVqZw

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createMainNotificationChannel(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(tag, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(tag, "Message data payload: ${remoteMessage.data}")

            val title = remoteMessage.data[KEY_TITLE] ?: getString(R.string.app_name)
            val body = remoteMessage.data[KEY_BODY] ?: ""
            val imageUrl = remoteMessage.data[KEY_IMAGE_URL]
            val channelId = getString(R.string.motmaenbash_general_channel)

            if (shouldProcessInBackground(remoteMessage.data)) {
                scheduleJob(remoteMessage.data)
            } else {
                handleNow(title, body, imageUrl, channelId)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(tag, "Message Notification Body: ${it.body}")

            val title = it.title ?: getString(R.string.app_name)
            val body = it.body ?: ""
            val imageUrl = it.imageUrl?.toString()
            val channelId = getString(R.string.motmaenbash_general_channel)

            handleNow(title, body, imageUrl, channelId)
        }
    }

    private fun shouldProcessInBackground(data: Map<String, String>): Boolean {
        val hasLargePayload = data.size > 10
        val requiresNetworkOperation = data[KEY_IMAGE_URL]?.isNotEmpty() ?: false

        return hasLargePayload || requiresNetworkOperation
    }

    override fun onNewToken(token: String) {
        Log.d(tag, "Refreshed token: $token")
    }

    private fun scheduleJob(data: Map<String, String>) {
        val inputData = Data.Builder().apply {
            for ((key, value) in data) {
                putString(key, value)
            }
        }.build()

        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()

        Log.d(tag, "Work scheduled for background processing")
    }

    private fun handleNow(title: String, body: String, imageUrl: String?, channelId: String) {
        Log.d(tag, "Processing notification immediately")

        if (imageUrl != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = downloadImage(imageUrl)
                    sendNotification(title, body, bitmap)
                } catch (e: Exception) {
                    Log.e(tag, "Error downloading image: ${e.message}")
                    sendNotification(title, body, null)
                }
            }
        } else {
            sendNotification(title, body, null)
        }
    }

    private fun downloadImage(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            Log.e(tag, "Error downloading image: ${e.message}")
            null
        }
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        image: Bitmap? = null
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_title", title)
            putExtra("notification_body", messageBody)
        }

        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, NotificationUtils.MAIN_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        image?.let {
            notificationBuilder.setLargeIcon(it)
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .bigLargeIcon(null as Bitmap?)
            )
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createMainNotificationChannel(this)
        }

        val id = notificationId.incrementAndGet()
        notificationManager.notify(id, notificationBuilder.build())
    }

    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            val data = inputData
            val title = data.getString(KEY_TITLE) ?: applicationContext.getString(R.string.app_name)
            val body = data.getString(KEY_BODY) ?: ""
            val imageUrl = data.getString(KEY_IMAGE_URL)
            val channelId = applicationContext.getString(R.string.motmaenbash_general_channel)

            try {
                val bitmap = imageUrl?.let { downloadImageFromUrl(it) }
                showNotification(title, body, bitmap, channelId)
                return Result.success()
            } catch (e: Exception) {
                Log.e(tag, "Error in background job: ${e.message}")
                return Result.failure()
            }
        }

        private fun downloadImageFromUrl(imageUrl: String): Bitmap? {
            return try {
                val url = URL(imageUrl)
                BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: IOException) {
                Log.e(tag, "Error downloading image: ${e.message}")
                null
            }
        }

        private fun showNotification(
            title: String,
            body: String,
            image: Bitmap?,
            channelId: String
        ) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("notification_title", title)
                putExtra("notification_body", body)
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            image?.let {
                notificationBuilder.setLargeIcon(it)
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?)
                )
            }

            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationUtils.createMainNotificationChannel(applicationContext)
            }

            val notificationId = AtomicInteger(0).incrementAndGet()
            notificationManager.notify(notificationId, notificationBuilder.build())
        }

        companion object {
            private const val tag = "NotificationWorker"
            private const val KEY_TITLE = "title"
            private const val KEY_BODY = "body"
            private const val KEY_IMAGE_URL = "image_url"
        }
    }
}