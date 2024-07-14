package nu.milad.motmaenbash.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import nu.milad.motmaenbash.utils.AppUtils
import nu.milad.motmaenbash.utils.DatabaseHelper


class AppInstallReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)

        if (intent.action == Intent.ACTION_PACKAGE_ADDED || intent.action == Intent.ACTION_PACKAGE_REPLACED

        ) {

            // Ignore ACTION_PACKAGE_ADDED if it is a replacement
            if (intent.action == Intent.ACTION_PACKAGE_ADDED && isReplacing) return

            val data: Uri? = intent.data
            val packageName = data?.encodedSchemeSpecificPart




            packageName?.let {
                val appInfo = AppUtils.getAppInfo(context, packageName)


                checkAppAgainstDatabase(context, it)
            }
        } else if (intent.action == Intent.ACTION_PACKAGE_FIRST_LAUNCH) {
            val packageName = intent.data?.encodedSchemeSpecificPart


            packageName?.let {
                // Check against database
                checkAppAgainstDatabase(context, it)
            }
        }
    }


    private fun checkAppAgainstDatabase(context: Context, packageName: String) {


        val dbHelper = DatabaseHelper(context)
        val appInfo = AppUtils.getAppInfo(context, packageName)



        if (dbHelper.isAppSuspicious(packageName, appInfo.sha1, appInfo.sha1)) {
            // todo:App found suspicious in the database, alert the user

        }


    }


}