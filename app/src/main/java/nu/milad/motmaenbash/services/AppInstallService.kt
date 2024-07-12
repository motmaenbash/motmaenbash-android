package nu.milad.motmaenbash.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.receivers.AppInstallReceiver

class AppInstallService : Service() {

    private lateinit var appInstallReceiver: AppInstallReceiver

    override fun onCreate() {
        super.onCreate()
        appInstallReceiver = AppInstallReceiver()

        // Register AppInstallReceiver
        val appInstallFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
            priority = 999
        }


        registerReceiver(appInstallReceiver, appInstallFilter)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
