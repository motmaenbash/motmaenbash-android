package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AppInstallReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AppInstallReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action}")

        if (intent.action == Intent.ACTION_PACKAGE_ADDED || intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            val data: Uri? = intent.data
            val packageName = data?.encodedSchemeSpecificPart

            Log.d(TAG, "Package name: $packageName")

        }
    }


}