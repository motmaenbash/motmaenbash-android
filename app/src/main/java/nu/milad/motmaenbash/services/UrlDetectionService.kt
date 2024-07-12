package nu.milad.motmaenbash.services

import android.accessibilityservice.AccessibilityService

import android.view.accessibility.AccessibilityEvent


class UrlDetectionService : AccessibilityService() {

    private val LOG_TAG = "UrlDetectionService"


    override fun onCreate() {
        super.onCreate()
    }

    override fun onServiceConnected() {


    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {

    }

    override fun onInterrupt() {

    }


}