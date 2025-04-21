package nu.milad.motmaenbash.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.utils.PermissionManager

abstract class BasePermissionViewModel(context: Application) : AndroidViewModel(context) {

    private val permissionManager: PermissionManager by lazy { PermissionManager(context) }

    // Permission states
    private val _smsPermissionStatus = MutableStateFlow(false)
    val smsPermissionStatus: StateFlow<Boolean> = _smsPermissionStatus.asStateFlow()

    private val _notificationPermissionStatus = MutableStateFlow(false)
    val notificationPermissionStatus: StateFlow<Boolean> =
        _notificationPermissionStatus.asStateFlow()

    private val _accessibilitySettingStatus = MutableStateFlow(false)
    val accessibilitySettingStatus: StateFlow<Boolean> = _accessibilitySettingStatus.asStateFlow()

    private val _overlayPermissionStatus = MutableStateFlow(false)
    val overlayPermissionStatus: StateFlow<Boolean> = _overlayPermissionStatus.asStateFlow()

    private val _hasRequestedSmsPermission = MutableStateFlow(false)
    val hasRequestedSmsPermission = _hasRequestedSmsPermission.asStateFlow()

    fun setHasRequestedSmsPermission(requested: Boolean) {
        _hasRequestedSmsPermission.value = requested
    }

    private val _hasRequestedNotificationPermission = MutableStateFlow(false)
    val hasRequestedNotificationPermission = _hasRequestedNotificationPermission.asStateFlow()

    fun setHasRequestedNotificationPermission(requested: Boolean) {
        _hasRequestedNotificationPermission.value = requested
    }

    fun checkInitialPermissions() {
        val permissions = permissionManager.checkAllPermissions()

        _smsPermissionStatus.value = permissions[PermissionType.SMS] ?: false
        _accessibilitySettingStatus.value = permissions[PermissionType.ACCESSIBILITY] ?: false
        _overlayPermissionStatus.value = permissions[PermissionType.OVERLAY] ?: false
        _notificationPermissionStatus.value = permissions[PermissionType.NOTIFICATIONS] ?: false
    }

    fun checkPermissionStatus(type: PermissionType) {
        val isGranted = permissionManager.checkPermission(type)
        updatePermissionStatus(type, isGranted)
    }

    fun updatePermissionStatus(type: PermissionType, isGranted: Boolean) {
        when (type) {
            PermissionType.SMS -> _smsPermissionStatus.value = isGranted
            PermissionType.ACCESSIBILITY -> _accessibilitySettingStatus.value = isGranted
            PermissionType.OVERLAY -> _overlayPermissionStatus.value = isGranted
            PermissionType.NOTIFICATIONS -> _notificationPermissionStatus.value = isGranted
        }
    }
}