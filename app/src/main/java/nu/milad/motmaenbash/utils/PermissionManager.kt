package nu.milad.motmaenbash.utils

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.services.UrlGuardService
import nu.milad.motmaenbash.ui.activities.PermissionTutorialActivity

/**
 * Centralized permission handler for managing all app permissions
 */
class PermissionManager(private val context: Context) {

    /**
     * Check SMS permission
     * @return true if permission is granted, false otherwise
     */
    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check accessibility permission
     * @return true if accessibility service is enabled, false otherwise
     */
    private fun checkAccessibilityPermission(): Boolean {
        val componentName = ComponentName(context, UrlGuardService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            if (componentNameString.equals(componentName.flattenToString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Check overlay permission
     * @return true if overlay permission is granted, false otherwise
     */
    private fun checkOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    /**
     * Check notification permission
     * @return true if notification permission is granted, false otherwise
     */
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required pre-API 33

        }
    }

    /**
     * Check permission status by permission type
     * @param type The permission type to check
     * @return true if permission is granted, false otherwise
     */
    fun checkPermission(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.SMS -> checkSmsPermission()
            PermissionType.ACCESSIBILITY -> checkAccessibilityPermission()
            PermissionType.OVERLAY -> checkOverlayPermission()
            PermissionType.NOTIFICATIONS -> checkNotificationPermission()
        }
    }

    /**
     * Check all application permissions at once
     * @return Map of permission types to their granted status
     */
    fun checkAllPermissions(): Map<PermissionType, Boolean> {
        return mapOf(
            PermissionType.SMS to checkSmsPermission(),
            PermissionType.ACCESSIBILITY to checkAccessibilityPermission(),
            PermissionType.OVERLAY to checkOverlayPermission(),
            PermissionType.NOTIFICATIONS to checkNotificationPermission()
        )
    }

    /**
     * Launch accessibility settings
     */
    fun launchAccessibilitySettings(launcher: ActivityResultLauncher<Intent>) {
        val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        launcher.launch(accessibilityIntent)
    }

    /**
     * Request overlay permission
     */
    fun requestOverlayPermission(context: Context, launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
            launcher.launch(intent)
        }
    }

    /**
     * Request notification permission via settings
     * This is used for devices below Android 13 (API 33) or when permission is permanently denied
     */
    fun requestNotificationPermission(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }

                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
            }
        }
        launcher.launch(intent)
    }

    /**
     * Show overlay permission tutorial
     */
    suspend fun showOverlayPermissionTutorial() {
        delay(500)
        val intent = Intent(context, PermissionTutorialActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

}