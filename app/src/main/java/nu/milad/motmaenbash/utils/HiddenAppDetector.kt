package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

object HiddenAppDetector {

    private const val TAG = "HiddenAppDetector"

    private const val SELF_LAUNCH_THRESHOLD = 30

    data class Result(
        val isHidden: Boolean,
        val reasons: List<String>
    )

    private val NOT_HIDDEN = Result(isHidden = false, reasons = emptyList())

    fun analyze(context: Context, packageName: String, selfLaunchCount: Int? = null): Result {
        val pm = context.packageManager

        try {
            val appEnabled = pm.getApplicationEnabledSetting(packageName)
            if (appEnabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                appEnabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            ) {
                return NOT_HIDDEN
            }

            if (pm.checkSignatures(packageName, "android") == PackageManager.SIGNATURE_MATCH) {
                return NOT_HIDDEN
            }

            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            @Suppress("DEPRECATION")
            val launchers = pm.queryIntentActivities(
                launcherIntent,
                PackageManager.GET_DISABLED_COMPONENTS
            )

            var hasEnabledLauncher = false
            var hasBlankedDisabledLauncher = false
            for (resolveInfo in launchers) {
                val activity = resolveInfo.activityInfo ?: continue
                val component = android.content.ComponentName(activity.packageName, activity.name)
                val enabled = when (pm.getComponentEnabledSetting(component)) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
                    else -> activity.enabled
                }
                if (enabled) {
                    hasEnabledLauncher = true
                    break
                }
                val hasOwnLabel = activity.labelRes != 0 ||
                        !activity.nonLocalizedLabel.isNullOrBlank()
                if (!hasOwnLabel) hasBlankedDisabledLauncher = true
            }

            if (hasEnabledLauncher) return NOT_HIDDEN

            val structurallyHidden = hasBlankedDisabledLauncher
            val selfLaunching = selfLaunchCount != null && selfLaunchCount >= SELF_LAUNCH_THRESHOLD

            if (!structurallyHidden && !selfLaunching) return NOT_HIDDEN

            val reasons = mutableListOf<String>()
            if (structurallyHidden) {
                reasons.add("آیکون خود را پنهان کرده است")
            }
            if (selfLaunching) {
                reasons.add(
                    "${NumberUtils.toPersianNumbers(selfLaunchCount.toString())} بار خودبه‌خود در پیش‌زمینه باز شده"
                )
            }

            val permissions = pm.getPackageInfo(
                packageName, PackageManager.GET_PERMISSIONS
            ).requestedPermissions?.toHashSet() ?: hashSetOf()
            if ("android.permission.RECEIVE_BOOT_COMPLETED" in permissions &&
                "android.permission.FOREGROUND_SERVICE" in permissions
            ) {
                reasons.add("اجرای خودکار و فعالیت در پس‌زمینه")
            }

            return Result(isHidden = true, reasons = reasons)
        } catch (e: Exception) {
            Log.w(TAG, "Could not analyze $packageName", e)
            return NOT_HIDDEN
        }
    }
}
