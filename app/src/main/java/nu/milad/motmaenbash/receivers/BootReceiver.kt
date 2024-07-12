import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import nu.milad.motmaenbash.receivers.SmsReceiver
import nu.milad.motmaenbash.receivers.AppInstallReceiver

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start AppInstallReceiver & SmsReceiver when device boots up
            startAppInstallReceiver(context)
            startSmsReceiver(context)

    }
    }

    private fun startAppInstallReceiver(context: Context) {
        // Register AppInstallReceiver dynamically
        val appInstallFilter = IntentFilter()
        appInstallFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        appInstallFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        appInstallFilter.addDataScheme("package")

        appInstallFilter.priority = 999

        context.registerReceiver(AppInstallReceiver(), appInstallFilter)
    }

    private fun startSmsReceiver(context: Context) {

        // Register SmsReceiver dynamically
    }
}
