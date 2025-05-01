package nu.milad.motmaenbash.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.models.Alert.ThreatType
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult.SuspiciousUrl
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.AlertUtils.getAlertContent
import nu.milad.motmaenbash.utils.AudioHelper
import java.util.concurrent.atomic.AtomicLong

class AlertOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private val overlayViews = mutableMapOf<Long, View>()
    private lateinit var audioHelper: AudioHelper

    companion object {
        private val alertIdGenerator = AtomicLong()
        private const val tag = "AlertOverlayService"

        fun showAlert(context: Context, suspiciousUrl: SuspiciousUrl) {
            val intent = Intent(context, AlertOverlayService::class.java).apply {
                putExtra("url", suspiciousUrl.url)
                putExtra("threatType", suspiciousUrl.threatType?.name) // as String
                putExtra("isSpecificUrl", suspiciousUrl.isSpecificUrl)
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val threatTypeName = intent.getStringExtra("threatType")
        val threatType = threatTypeName?.let { ThreatType.valueOf(it) }
        val isSpecificUrl = intent.getBooleanExtra("isSpecificUrl", false)

        val alertId = alertIdGenerator.incrementAndGet()

        try {
            if (!::audioHelper.isInitialized) {
                audioHelper = AudioHelper(this)
            }

            audioHelper.vibrateDevice(this)
            CoroutineScope(Dispatchers.Main).launch {
                audioHelper.playAlertSound()
            }

            if (windowManager == null) {
                windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            }

            // Inflate a new view for this alert
            val newOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_alert, null)


            // Set url and message
            val (title, summary, message) = getAlertContent(
                alertType = Alert.AlertType.URL_FLAGGED,
                threatType = threatType,
                isSpecificUrl = isSpecificUrl
            )
            val hint = AlertUtils.getAlertHint(Alert.AlertType.URL_FLAGGED, threatType)
            newOverlayView.findViewById<TextView>(R.id.url)?.text = url
            newOverlayView.findViewById<TextView>(R.id.alertBadge)?.text = title
            newOverlayView.findViewById<TextView>(R.id.alertTitle)?.text = summary
            newOverlayView.findViewById<TextView>(R.id.alertMessage)?.text = message
            newOverlayView.findViewById<TextView>(R.id.alertHint)?.text = hint


            // Set up independent close handlers for this specific view
            newOverlayView.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener {
                removeOverlay(alertId)
            }


            // Create layout params for the overlay
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )


            // Add the view to the window manager and store it
            windowManager?.addView(newOverlayView, params)
            overlayViews[alertId] = newOverlayView

        } catch (e: Exception) {
            Log.e(tag, "Error creating overlay", e)
        }

        return START_NOT_STICKY
    }


    private fun removeOverlay(alertId: Long) {
        try {
            val viewToRemove = overlayViews[alertId]
            if (viewToRemove != null && windowManager != null) {
                windowManager?.removeView(viewToRemove)
                overlayViews.remove(alertId)

                // Only stop service if all overlays are gone
                if (overlayViews.isEmpty()) {
                    stopSelf()
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(tag, "Error removing specific overlay view", e)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            // Clean up all views when service is destroyed
            overlayViews.forEach { (_, view) ->
                try {
                    windowManager?.removeView(view)
                } catch (e: Exception) {
                    Log.e(tag, "Error removing view on destroy", e)
                }
            }
            overlayViews.clear()
        } catch (e: Exception) {
            Log.e(tag, "Error in onDestroy", e)
        }
        super.onDestroy()
    }
}