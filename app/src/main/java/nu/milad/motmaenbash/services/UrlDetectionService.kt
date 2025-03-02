package nu.milad.motmaenbash.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import nu.milad.motmaenbash.utils.DatabaseHelper


class UrlDetectionService : AccessibilityService() {

    private lateinit var databaseHelper: DatabaseHelper
    private val LOG_TAG = "UrlDetectionService"


    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onServiceConnected() {

        // Configure the accessibility service
        val info = serviceInfo
        info.eventTypes =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED


    }

    override fun onInterrupt() {
        Log.d(LOG_TAG, "Service interrupted")
    }

    private class SupportedBrowserConfig(val packageName: String, val addressBarId: String)


}


}