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

        //todo: delete after all things is ok
//        val browserConfig = supportedBrowsers.find { it.packageName == packageName } ?: run {
//            stopFloatingViewService()
//            return
//        }


//        Log.d("zxcfd", isBrowserInForeground(packageName).toString());

        // Handle browser closure
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isBrowserClosed(
                packageName
            )
        ) {
            stopOverlayVerificationBadgeService()
            return
        }


        ///////////////////////////todo: delete from
        Log.d(
            tag,
            "Accessibility event received: ${AccessibilityEvent.eventTypeToString(event.eventType)}"
        )

        Log.d(tag, "zzzzzzzzzzzzz1:" + event.eventType)
        Log.d(tag, "zzzzzzzzzzzzz2:" + isBrowserClosed(packageName))


        // Log debug information
        Log.d(tag, "Event package: $packageName")
        Log.d(tag, "Event class: ${event.className}")
        Log.d(tag, "Event text: ${event.text}")
        Log.d(tag, "Parent node info: $parentNodeInfo")


        //        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            stopService(new Intent(getBaseContext(), FloatingViewService.class));
//        }


        // Handle browser closure
        // Check if the browser is closed and hide floating view
//
//        Log.d("MZMZMZMZ", "event: " + AccessibilityEvent.eventTypeToString(event.eventType))
//
//        Log.d("MZMZMZMZ", "event.packageName: " + event.packageName)
//
//        Log.d("MZMZMZMZ", "isBrowserClosed: " + isBrowserClosed(event.packageName.toString()))
//
//
//        val browserConfig2 = supportedBrowsers.find { it.packageName == event.packageName }
//
//        Log.d("MZMZMZMZ", "browserConfig2: " + browserConfig2.toString())
//
//        if (browserConfig2 == null
//            && !isBrowserInForeground(packageName)) {
//            Log.d(tag, "Unsupported browser, stopping FloatingViewService")
//            stopService(Intent(this, FloatingViewService::class.java))
//            return
//        }
//


        //it works better
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
//            event.packageName != null &&
//            isBrowserClosed(event.packageName.toString())
//            && event.packageName == "com.android.chrome"
//        ) {
//            Log.d(tag, "Browser closed, stopping FloatingViewService")
//            stopService(Intent(this, FloatingViewService::class.java))
//        }
//
//
//
//        // Assuming you detect browser closure and want to hide floating view
////        if (isBrowserClosed(event.packageName.toString())) {
////            stopService(Intent(this, FloatingViewService::class.java))
////        }
//
//
//        // Handle URL capture for supported browsers
//        val packageName = event.packageName.toString()
//        val parentNodeInfo = event.source ?: return


//        // Handle browser coming to the foreground
//        if (
////            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
//            isBrowserInForeground(packageName)
//        ) {
//            Log.d(tag, "zzzzzzzzzzzzz Browser is in foreground, starting FloatingViewService")
//            startService(Intent(this, FloatingViewService::class.java))
//            return
//        }


        if (AccessibilityEvent.eventTypeToString(event.eventType) == "TYPE_WINDOW_CONTENT_CHANGED") {
//            analyze(event);
        }

        if (AccessibilityEvent.eventTypeToString(event.eventType).contains("WINDOW")) {
//            dfs(parentNodeInfo);
        }


        if (event.packageName != null && event.className != null) {
        }


        //todo:delete till here


        val capturedUrl = captureUrl(parentNodeInfo, browserConfig) ?: return


        // Throttle URL analysis
        val currentEventTime = event.eventTime
        val urlSignature = "$packageName:$capturedUrl"
        val lastProcessTime = urlProcessingTimestamps[urlSignature] ?: 0L
        val throttleInterval = 1500L

        Log.d(tag, "Captured URL: $capturedUrl")

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

        Log.d("zCaptured URL:", "addressBarNodeInfo.isFocused: " + addressBarNodeInfo.isFocused)
        Log.d("zCaptured URL:", "addressBarNodeInfo.isFocused: " + addressBarNodeInfo.isFocused)


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


        //todo: delete
//        AlertUtils.showAlert(
//            applicationContext,
//            AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED,
//            AlertHandlerActivity.Companion.AlertLevel.ERROR.toString(),
//            url
//        )


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
//return t.p("com.android.chrome", "com.chrome.dev", "com.chrome.canary", "org.mozilla.firefox_beta", "org.mozilla.fenix", "org.mozilla.focus", "com.microsoft.emmx", "com.microsoft.bing", "com.duckduckgo.mobile.android", "com.opera.touch", "com.opera.browser.beta", "org.mozilla.firefox", "com.opera.browser", "org.mozilla.rocket", "com.opera.mini.native", "com.android.browser", "com.swarajyadev.linkprotector");


//fun getSupportedBrowserConfigs() = listOf(
//    // Chrome and Chromium-based browsers
//    SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"),
//    SupportedBrowserConfig("com.brave.browser", "com.brave.browser:id/url_bar"),
//    SupportedBrowserConfig("com.kiwibrowser.browser", "com.kiwibrowser.browser:id/url_bar"),
//    SupportedBrowserConfig("org.bromite.bromite", "org.bromite.bromite:id/url_bar"),
//    SupportedBrowserConfig("org.ungoogled.chromium.stable", "org.ungoogled.chromium.stable:id/url_bar"),
//    SupportedBrowserConfig("com.chrome.beta", "com.chrome.beta:id/url_bar"),
//    SupportedBrowserConfig("com.chrome.dev", "com.chrome.dev:id/url_bar"),
//    SupportedBrowserConfig("com.chrome.canary", "com.chrome.canary:id/url_bar"),
//    SupportedBrowserConfig("com.vivaldi.browser", "com.vivaldi.browser:id/url_bar"),
//    SupportedBrowserConfig("com.ecosia.android", "com.ecosia.android:id/url_bar"),
//
//    // Firefox and Mozilla-based browsers
//    SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"),
//    SupportedBrowserConfig("org.mozilla.fenix", "org.mozilla.fenix:id/mozac_browser_toolbar_url_view"),
//    SupportedBrowserConfig("org.mozilla.focus", "org.mozilla.focus:id/mozac_browser_toolbar_url_view"),
//    SupportedBrowserConfig("io.github.forkmaintainers.iceraven", "io.github.forkmaintainers.iceraven:id/mozac_browser_toolbar_url_view"),
//
//    // Microsoft and Edge browsers
//    SupportedBrowserConfig("com.microsoft.emmx", "com.microsoft.emmx:id/url_bar"),
//
//    // Opera browsers
//    SupportedBrowserConfig("com.opera.browser", "com.opera.browser:id/url_field"),
//    SupportedBrowserConfig("com.opera.mini.native", "com.opera.mini.native:id/url_field"),
//
//    // Other browsers
//    SupportedBrowserConfig("com.duckduckgo.mobile.android", "com.duckduckgo.mobile.android:id/omnibarTextInput"),
//    SupportedBrowserConfig("com.sec.android.app.sbrowser", "com.sec.android.app.sbrowser:id/location_bar_edit_text"),
//    SupportedBrowserConfig("com.qwant.liberty", "com.qwant.liberty:id/url_bar"),
//    SupportedBrowserConfig("mark.via", "mark.via:id/am"),
//    SupportedBrowserConfig("org.adblockplus.browser", "org.adblockplus.browser:id/url_bar"),
//    SupportedBrowserConfig("com.naver.whale", "com.naver.whale:id/url_bar"),
//    SupportedBrowserConfig("net.fast.web.browser", "net.fast.web.browser:id/url_bar")
//)

/*
   val aVar: C1562a = C1562a("alook.browser", "search_fragment_input_view")

    val aVar2: C1562a = C1562a("alook.browser.google", "search_fragment_input_view")
    val aVar3: C1562a = C1562a("com.amazon.cloud9", "url")
    val aVar4: C1562a = C1562a("com.android.browser", "url")
    val aVar5: C1562a = C1562a("com.android.chrome", "url_bar")
    val aVar6: C1562a = C1562a("com.avast.android.secure.browser", "editor")
    val aVar7: C1562a = C1562a("com.avg.android.secure.browser", "editor")
    val aVar8: C1562a = C1562a("com.brave.browser", "url_bar")
    val aVar9: C1562a = C1562a("com.brave.browser_beta", "url_bar")
    val aVar10: C1562a = C1562a("com.brave.browser_default", "url_bar")
    val aVar11: C1562a = C1562a("com.brave.browser_dev", "url_bar")
    val aVar12: C1562a = C1562a("com.brave.browser_nightly", "url_bar")
    val aVar13: C1562a = C1562a("com.chrome.beta", "url_bar")
    val aVar14: C1562a = C1562a("com.chrome.canary", "url_bar")
    val aVar15: C1562a = C1562a("com.chrome.dev", "url_bar")
    val aVar16: C1562a = C1562a("com.cookiegames.smartcookie", "search")
    val aVar17: C1562a = aVar11
    val aVar18: C1562a =
        C1562a("com.cookiejarapps.android.smartcookieweb", "mozac_browser_toolbar_url_view")
    val aVar19: C1562a = aVar10
    val aVar20: C1562a = C1562a("com.duckduckgo.mobile.android", "omnibarTextInput")
    val aVar21: C1562a = C1562a("com.ecosia.android", "url_bar")
    val aVar22: C1562a = C1562a("com.google.android.apps.chrome", "url_bar")
    val aVar23: C1562a = C1562a("com.google.android.apps.chrome_dev", "url_bar")
    val aVar24: C1562a = C1562a("com.jamal2367.styx", "search")
    val aVar25: C1562a = C1562a("com.kiwibrowser.browser", "url_bar")
    val aVar26: C1562a = C1562a("com.kiwibrowser.browser.dev", "url_bar")
    val aVar27: C1562a = C1562a("com.microsoft.emmx", "url_bar")
    val aVar28: C1562a = C1562a("com.microsoft.emmx.beta", "url_bar")
    val aVar29: C1562a = C1562a("com.microsoft.emmx.canary", "url_bar")
    val aVar30: C1562a = C1562a("com.microsoft.emmx.dev", "url_bar")
    val aVar31: C1562a = aVar20
    val aVar32: C1562a = C1562a("com.mmbox.browser", "search_box")
    val aVar33: C1562a = C1562a("com.mmbox.xbrowser", "search_box")
    val aVar34: C1562a = C1562a("com.mycompany.app.soulbrowser", "edit_text")
    val aVar35: C1562a = C1562a("com.naver.whale", "url_bar")
    val aVar36: C1562a = aVar34
    val aVar37: C1562a = C1562a("com.opera.browser", "url_field")
    val aVar38: C1562a = C1562a("com.opera.browser.beta", "url_field")
    val aVar39: C1562a = C1562a("com.opera.mini.native", "url_field")
    val aVar40: C1562a = C1562a("com.opera.mini.native.beta", "url_field")
    val aVar41: C1562a = C1562a("com.opera.touch", "addressbarEdit")
    val aVar42: C1562a = C1562a("com.qflair.browserq", "url")
    val aVar43: C1562a = C1562a("com.qwant.liberty", "mozac_browser_toolbar_url_view,url_bar_title")
    val aVar44: C1562a = aVar41
    val aVar45: C1562a = C1562a("com.sec.android.app.sbrowser", "location_bar_edit_text")
    val aVar46: C1562a = C1562a("com.sec.android.app.sbrowser.beta", "location_bar_edit_text")
    val aVar47: C1562a = C1562a("com.stoutner.privacybrowser.free", "url_edittext")
    val aVar48: C1562a = C1562a("com.stoutner.privacybrowser.standard", "url_edittext")
    val aVar49: C1562a = C1562a("com.vivaldi.browser", "url_bar")
    val aVar50: C1562a = C1562a("com.vivaldi.browser.snapshot", "url_bar")
    val aVar51: C1562a = C1562a("com.vivaldi.browser.sopranos", "url_bar")
    val aVar52: C1562a = aVar48
    val aVar53: C1562a = C1562a("com.z28j.feel", "g2")
    val aVar54: C1562a = C1562a("idm.internet.download.manager", "search")
    val aVar55: C1562a = C1562a("idm.internet.download.manager.adm.lite", "search")
    val aVar56: C1562a = C1562a("idm.internet.download.manager.plus", "search")
    val aVar57: C1562a =
        C1562a("io.github.forkmaintainers.iceraven", "mozac_browser_toolbar_url_view")
    val aVar58: C1562a = aVar53
    val aVar59: C1562a = C1562a("mark.via", "am,an")
    val aVar60: C1562a = C1562a("mark.via.gp", "as")
    val aVar61: C1562a = C1562a("net.slions.fulguris.full.download", "search")
    val aVar62: C1562a = C1562a("net.slions.fulguris.full.download.debug", "search")
    val aVar63: C1562a = C1562a("net.slions.fulguris.full.playstore", "search")
    val aVar64: C1562a = C1562a("net.slions.fulguris.full.playstore.debug", "search")
    val aVar65: C1562a = aVar60
    val aVar66: C1562a = C1562a("org.adblockplus.browser", "url_bar,url_bar_title")
    val aVar67: C1562a = C1562a("org.adblockplus.browser.beta", "url_bar,url_bar_title")
    val aVar68: C1562a = C1562a("org.bromite.bromite", "url_bar")
    val aVar69: C1562a = C1562a("org.bromite.chromium", "url_bar")
    val aVar70: C1562a = C1562a("org.chromium.chrome", "url_bar")
    val aVar71: C1562a = C1562a("org.codeaurora.swe.browser", "url_bar")
    val aVar72: C1562a = aVar67
    val aVar73: C1562a = C1562a("org.gnu.icecat", "url_bar_title,mozac_browser_toolbar_url_view")
    val aVar74: C1562a = C1562a("org.mozilla.fenix", "mozac_browser_toolbar_url_view")
    val aVar75: C1562a = C1562a("org.mozilla.fenix.nightly", "mozac_browser_toolbar_url_view")
    val aVar76: C1562a =
        C1562a("org.mozilla.fennec_aurora", "mozac_browser_toolbar_url_view,url_bar_title")
    val aVar77: C1562a =
        C1562a("org.mozilla.fennec_fdroid", "mozac_browser_toolbar_url_view,url_bar_title")
    val aVar78: C1562a =
        C1562a("org.mozilla.firefox", "mozac_browser_toolbar_url_view,url_bar_title")
    val aVar79: C1562a =
        C1562a("org.mozilla.firefox_beta", "mozac_browser_toolbar_url_view,url_bar_title")
    val aVar80: C1562a = aVar73
    val aVar81: C1562a = C1562a("org.mozilla.focus", "mozac_browser_toolbar_url_view,display_url")
        C1562a("org.mozilla.focus.beta", "mozac_browser_toolbar_url_view,display_url")
        C1562a("org.mozilla.focus.nightly", "mozac_browser_toolbar_url_view,display_url")
    val aVar84: C1562a = C1562a("org.mozilla.klar", "mozac_browser_toolbar_url_view,display_url")
    val aVar85: C1562a = C1562a("org.mozilla.reference.browser", "mozac_browser_toolbar_url_view")
    val aVar86: C1562a = C1562a("org.mozilla.rocket", "display_url")
    val aVar87: C1562a = C1562a("org.ungoogled.chromium.extensions.stable", "url_bar")
    val aVar88: C1562a = C1562a("org.ungoogled.chromium.stable", "url_bar")
        C1562a("us.spotco.fennec_dos", "mozac_browser_toolbar_url_view,url_bar_title")

        C1562a("acr.browser.barebones", "search"),
        C1562a("acr.browser.lightning", "search"),
        C1562a("com.feedback.browser.wjbrowser", "addressbar_url"),
        C1562a("com.ghostery.android.ghostery", "search_field"),
        C1562a("com.htc.sense.browser", "title"),
        C1562a("com.jerky.browser2", "enterUrl"),
        C1562a("com.ksmobile.cb", "address_bar_edit_text"),
        C1562a("com.linkbubble.playstore", "url_text"),
        C1562a("com.mx.browser", "address_editor_with_progress"),
        C1562a("com.mx.browser.tablet", "address_editor_with_progress"),
        C1562a("com.nubelacorp.javelin", "enterUrl"),
        C1562a("jp.co.fenrir.android.sleipnir", "url_text"),
        C1562a("jp.co.fenrir.android.sleipnir_black", "url_text"),
        C1562a("jp.co.fenrir.android.sleipnir_test", "url_text"),
        C1562a("mobi.mgeek.TunnyBrowser", "title"),
        C1562a("org.iron.srware", "url_bar"),
        C1562a("net.fast.web.browser", "url_bar"),
        C1562a("com.swarajyadev.linkprotector", "et_lpsf_browser_address")
    */