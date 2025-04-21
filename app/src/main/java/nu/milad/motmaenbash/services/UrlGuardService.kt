package nu.milad.motmaenbash.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.LruCache
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_LINK_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_VERIFIED_GATEWAY
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult.SuspiciousUrl
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.UrlUtils
import org.json.JSONObject


class UrlGuardService : AccessibilityService() {

    private val urlProcessingTimestamps = HashMap<String, Long>()
    private lateinit var databaseHelper: DatabaseHelper
    private val tag = "UrlDetectionService"
    private val domainCache = LruCache<String, String>(100)

    sealed class UrlAnalysisResult {
        object NeutralUrl : UrlAnalysisResult()
        data class SafeUrl(val url: String) : UrlAnalysisResult()
        data class SuspiciousUrl(
            val url: String,
            val threatType: Alert.ThreatType? = null,
            val isSpecificUrl: Boolean = false
        ) : UrlAnalysisResult()
    }

    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
    }

    override fun onServiceConnected() {
        configureAccessibilityService()
        if (BuildConfig.DEBUG) {
            Toast.makeText(
                applicationContext,
                "Accessibility Service Connected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun configureAccessibilityService() {
        // Configure the accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d(tag, "Accessibility service configured")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        // Early return for irrelevant events
        val packageName = event.packageName?.toString() ?: return
        val parentNodeInfo = event.source ?: return

        // Check if this is a window state change event
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Check if this event represents switching away from a browser
            val isSwitchingFromBrowser =
                supportedBrowsers.any { it.packageName == event.packageName.toString() } &&
                        event.className?.toString()?.contains("RecentAppsActivity") == true

            if (isSwitchingFromBrowser || isBrowserClosed(event.packageName.toString())) {
                Log.d(tag, "Browser closed or switched away from, stopping services")
                stopOverlayVerificationBadgeService()
                return
            }
        }


        // Find browser configuration
        val browserConfig = supportedBrowsers.find { it.packageName == packageName } ?: return


        // Handle browser closure
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isBrowserClosed(
                packageName
            )
        ) {
            stopOverlayVerificationBadgeService()
            return
        }


        val capturedUrl = captureUrl(parentNodeInfo, browserConfig) ?: return


        // Throttle URL analysis
        val currentEventTime = event.eventTime
        val urlSignature = "$packageName:$capturedUrl"
        val lastProcessTime = urlProcessingTimestamps[urlSignature] ?: 0L
        val throttleInterval = 1500L


        if (currentEventTime - lastProcessTime > throttleInterval) {
            urlProcessingTimestamps[urlSignature] = currentEventTime

            val result = UrlUtils.analyzeUrl(
                url = capturedUrl,
                domainCache = domainCache,
                databaseHelper = databaseHelper
            )

            when (result) {
                is UrlAnalysisResult.SafeUrl -> {
                    startOverlayVerificationBadgeService(result.url)

                    databaseHelper.incrementUserStat(STAT_VERIFIED_GATEWAY)

                    logAnalyticsAsync(
                        "Motmaenbash_alert",
                        mapOf("alert_type" to STAT_VERIFIED_GATEWAY)
                    )

                }

                is SuspiciousUrl -> {
                    showSuspiciousUrlAlert(result)
                }

                is UrlAnalysisResult.NeutralUrl -> {
                    stopAllServices()
                }
            }
        }


        // Safely recycle node info
        parentNodeInfo.safeRecycleNodeInfo()


    }


    // This method uses the address bar ID to find the URL
    private fun captureUrl(info: AccessibilityNodeInfo, config: BrowserConfig): String? {

        // Find nodes by address bar ID
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        // Return null if no nodes found
        val addressBarNodeInfo = nodes?.firstOrNull() ?: return null


        try {
            // Skip if address bar is focused
            if (addressBarNodeInfo.isFocused) {
                stopAllServices()
                return null
            }
            // Extract and validate URL
            val url = addressBarNodeInfo.text?.toString()?.lowercase() ?: return null
            return if (UrlUtils.validateUrl(url)) url else null
        } finally {
            addressBarNodeInfo.safeRecycleNodeInfo()
        }


    }


    private fun startOverlayVerificationBadgeService(url: String) {
        val intent = Intent(this, OverlayVerificationBadgeService::class.java).apply {
            putExtra("URL", UrlUtils.extractAndCacheDomain(domainCache, url))
        }
        startService(intent)
    }

    private fun stopOverlayVerificationBadgeService() {
        val overlayVerificationBadgeService =
            isServiceRunning(OverlayVerificationBadgeService::class.java)
        if (overlayVerificationBadgeService) {
            val serviceIntent = Intent(this, OverlayVerificationBadgeService::class.java)
            stopService(serviceIntent)
            Log.d(tag, "FloatingViewService stopped")
        }
    }

    private fun stopAllServices() {
        // Check and stop AlertOverlayService and FloatingViewService
        listOf(
            OverlayVerificationBadgeService::class.java,
            OverlayAlertService::class.java
        ).forEach { serviceClass ->
            if (isServiceRunning(serviceClass)) {
                val serviceIntent = Intent(this, serviceClass)
                stopService(serviceIntent)
                Log.d(tag, "${serviceClass.simpleName} stopped")
            }
        }
    }

    // Helper method to check if a service is running
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { service -> serviceClass.name == service.service.className }
    }

    private fun showSuspiciousUrlAlert(suspiciousUrl: SuspiciousUrl) {
        OverlayAlertService.showAlert(
            applicationContext,
            suspiciousUrl
        )


        // Increment statistic
        databaseHelper.incrementUserStat(STAT_FLAGGED_LINK_DETECTED)
        logAnalyticsAsync("Motmaenbash_alert", mapOf("alert_type" to STAT_FLAGGED_LINK_DETECTED))


    }


    private fun AccessibilityNodeInfo.safeRecycleNodeInfo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.recycle()
        }
    }

    private fun isBrowserClosed(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true

        val am = getSystemService(ACTIVITY_SERVICE) as? ActivityManager ?: return true
        return try {

            val runningTasks = am.getRunningTasks(1)
            runningTasks.firstOrNull()?.topActivity?.packageName != packageName

        } catch (e: Exception) {
            Log.e(tag, "Error checking browser status", e)
            true
        }
    }

    private fun isBrowserInForeground(packageName: String): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as? ActivityManager ?: return false
        return try {
            val runningTasks = am.getRunningTasks(1)
            runningTasks.firstOrNull()?.topActivity?.packageName == packageName
        } catch (e: Exception) {
            Log.e(tag, "Error checking browser foreground status", e)
            false
        }
    }

    override fun onInterrupt() {
        Log.d(tag, "Service interrupted")
    }

    private class BrowserConfig(val packageName: String, val addressBarId: String)

    companion object {

        private val supportedBrowsers: List<BrowserConfig>
            /**
             * @return a list of supported browser configs
             * This list could be instead obtained from remote server to support future browser updates without updating an app
             */
            get() = listOf(
                BrowserConfig(
                    "com.android.chrome", "com.android.chrome:id/url_bar"
                ),
                BrowserConfig(
                    "org.mozilla.firefox", "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
                ),
                BrowserConfig(
                    "com.microsoft.emmx", "com.microsoft.emmx:id/url_bar"
                ), // Microsoft Edge
                BrowserConfig(
                    "com.opera.browser", "com.opera.browser:id/url_field"
                ),
                BrowserConfig("com.brave.browser", "com.brave.browser:id/url_bar"),
                BrowserConfig(
                    "com.duckduckgo.mobile.android",
                    "com.duckduckgo.mobile.android:id/omnibarTextInput"
                ),
                BrowserConfig(
                    "com.sec.android.app.sbrowser", "com.sec.android.app.sbrowser:id/url"
                ),
                BrowserConfig("com.ucmobile.intl", "com.ucmobile.intl:id/url"),
                BrowserConfig("com.qwant.liberty", "com.qwant.liberty:id/url_bar")
            )
    }


    private fun logAnalyticsAsync(eventName: String, params: Map<String, Any>) {

        if (BuildConfig.DEBUG) return

        val workRequest = OneTimeWorkRequestBuilder<AnalyticsWorker>()

            .setInputData(
                workDataOf(
                    "event_name" to eventName,
                    "params_json" to JSONObject(params).toString()
                )
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}


class AnalyticsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val eventName = inputData.getString("event_name") ?: return Result.failure()
        val paramsJson = inputData.getString("params_json") ?: return Result.failure()

        try {
            val firebaseAnalytics = Firebase.analytics
            val params = JSONObject(paramsJson)

            firebaseAnalytics.logEvent(eventName) {
                params.keys().forEach { key ->
                    param(key, params.get(key).toString())
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}
