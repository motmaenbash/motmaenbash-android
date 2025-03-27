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
import nu.milad.motmaenbash.services.UrlDetectionService
import nu.milad.motmaenbash.ui.activities.PermissionTutorialActivity

/**
 * Centralized permission handler for managing all app permissions
 */
class PermissionManager(private val context: Context) {

    /**
     * Check SMS permission
     * @return true if permission is granted, false otherwise
     */
    fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check accessibility permission
     * @return true if accessibility service is enabled, false otherwise
     */
    fun checkAccessibilityPermission(): Boolean {
        val componentName = ComponentName(context, UrlDetectionService::class.java)
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
    fun checkOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    /**
     * Check notification permission
     * @return true if notification permission is granted, false otherwise
     */
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required pre-API 33
        }
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
     * Show overlay permission tutorial
     */
    suspend fun showOverlayPermissionTutorial() {
        delay(500)
        val intent = Intent(context, PermissionTutorialActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun hasCriticalPermissions(): Boolean {
        return checkSmsPermission() && checkOverlayPermission()
    }
}