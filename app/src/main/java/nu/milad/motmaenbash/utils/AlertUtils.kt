package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import nu.milad.motmaenbash.ui.AlertHandlerActivity

object AlertUtils {

    fun showAlert(
        context: Context,
        alertType: Int,
        alertLevel: String,
        param1: String? = null,
        param2: String? = null,
        info: String? = null,

        ) {


        Log.d("AlertUtils", "showAlert: $alertType")
        Log.d("AlertUtils", "showAlert: $alertLevel")
        Log.d("AlertUtils", "showAlert: $param1")
        Log.d("AlertUtils", "showAlert: $param2")
        Log.d("AlertUtils", "showAlert: $info")


        val intent = Intent(context, AlertHandlerActivity::class.java).apply {
            putExtra(AlertHandlerActivity.EXTRA_ALERT_TYPE, alertType)
            putExtra(AlertHandlerActivity.EXTRA_ALERT_LEVEL, alertLevel)
            if (param1 != null) {
                putExtra(AlertHandlerActivity.EXTRA_PARAM1, param1)
            }
            if (param2 != null) {
                putExtra(AlertHandlerActivity.EXTRA_PARAM2, param2)
            }
            putExtra(AlertHandlerActivity.EXTRA_ALERT_INFO, info)
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        }
        context.startActivity(intent)
    }
}