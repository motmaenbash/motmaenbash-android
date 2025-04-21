package nu.milad.motmaenbash.utils

import android.content.Context
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.models.Guard

/**
 * Utility class for managing guards and their status
 */
class GuardUtils(context: Context) {

    private val permissionManager = PermissionManager(context)

    /**
     * Get titles of all active guards
     * @return List of active guard titles in Persian
     */
    fun getActiveGuardsTitles(context: Context): List<Guard> {
        val activeGuards = mutableListOf<Guard>()

        // Check SMS permission
        if (permissionManager.checkPermission(PermissionType.SMS)) {
            activeGuards.add(Guard(context.getString(R.string.sms), true))
        }

        // Check Accessibility service
        if (permissionManager.checkPermission(PermissionType.ACCESSIBILITY)) {
            activeGuards.add(Guard(context.getString(R.string.web), true))
        }


        activeGuards.add(Guard(context.getString(R.string.app), true))



        return activeGuards
    }

    /**
     * Get count of active guards
     * @return Number of active guards
     */
    fun getActiveGuardsCount(): Int {
        var count = 0

        if (permissionManager.checkPermission(PermissionType.SMS)) count++
        if (permissionManager.checkPermission(PermissionType.ACCESSIBILITY)) count++
        if (permissionManager.checkPermission(PermissionType.NOTIFICATIONS)) count++

        return count
    }

    /**
     * Get total number of guards
     * @return Total number of available guards
     */
    fun getTotalGuardsCount(): Int {
        return 3 // SMS, Accessibility, Notification
    }

    /**
     * Check if critical permissions are missing
     * @return true if any critical permission is missing
     */
    fun areCriticalPermissionsMissing(): Boolean {
        return !permissionManager.checkPermission(PermissionType.NOTIFICATIONS) ||
                !permissionManager.checkPermission(PermissionType.OVERLAY)
    }
}