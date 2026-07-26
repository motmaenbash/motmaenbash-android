package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.pm.PackageManager

object DisabledAppDetector {

    fun isDisabled(context: Context, packageName: String): Boolean {
        return try {
            when (context.packageManager.getApplicationEnabledSetting(packageName)) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> true

                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}
