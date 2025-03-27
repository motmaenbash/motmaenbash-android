package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.services.UrlDetectionService
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PermissionManager
import nu.milad.motmaenbash.utils.UpdateManager
import nu.milad.motmaenbash.utils.UpdateManager.UpdateState
import org.json.JSONObject
import java.util.Random


class MainViewModel(private val context: Application) : AndroidViewModel(context) {

    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(context) }
    private val permissionManager: PermissionManager by lazy { PermissionManager(context) }

    // Tip of the Day
    private val _tipOfTheDay = MutableStateFlow<String?>(null)
    val tipOfTheDay: StateFlow<String?> = _tipOfTheDay.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Permissions
    private val _smsPermissionStatus = MutableStateFlow(false)
    val smsPermissionStatus: StateFlow<Boolean> = _smsPermissionStatus.asStateFlow()

    private val _accessibilitySettingStatus = MutableStateFlow(false)
    val accessibilitySettingStatus: StateFlow<Boolean> = _accessibilitySettingStatus.asStateFlow()

    private val _overlayPermissionStatus = MutableStateFlow(false)
    val overlayPermissionStatus: StateFlow<Boolean> = _overlayPermissionStatus.asStateFlow()

    private val _notificationPermissionStatus = MutableStateFlow(false)
    val notificationPermissionStatus: StateFlow<Boolean> =
        _notificationPermissionStatus.asStateFlow()

    // Stats
    private val _suspiciousLinksDetected = MutableStateFlow(0)
    val suspiciousLinksDetected: StateFlow<Int> = _suspiciousLinksDetected.asStateFlow()

    private val _suspiciousSmsDetected = MutableStateFlow(0)
    val suspiciousSmsDetected: StateFlow<Int> = _suspiciousSmsDetected.asStateFlow()

    private val _suspiciousAppDetected = MutableStateFlow(0)
    val suspiciousAppDetected: StateFlow<Int> = _suspiciousAppDetected.asStateFlow()

    // Sponsor
    private val _sponsorData = MutableStateFlow<SponsorData?>(null)
    val sponsorData: StateFlow<SponsorData?> = _sponsorData.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(AppConstants.APP_PREFERENCES, Context.MODE_PRIVATE)

    private val updateManager = UpdateManager(context)

    // Database Update
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle("بارگذاری..."))
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updateDialogState = MutableStateFlow<UpdateDialogState?>(null)
    val updateDialogState: StateFlow<UpdateDialogState?> = _updateDialogState.asStateFlow()

    init {
        refreshTipOfTheDay()
        loadDatabaseUpdateTime()
        loadStatsFromDatabase()
        loadRandomSponsor()
        checkInitialPermissions()
        startUrlInterceptorService()
    }

    fun refreshTipOfTheDay() {
        if (_isRefreshing.value) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isRefreshing.emit(true) // Set loading state
                delay(400) // Small delay to show loading indicator
                _tipOfTheDay.emit(dbHelper.getRandomTip()) // Fetch and set tip
            } catch (e: Exception) {
                Log.e("TipBug", "Error fetching tip", e)
            } finally {
                _isRefreshing.emit(false) // Reset loading state
            }
        }
    }

    // Load Database Update Time
    private fun loadDatabaseUpdateTime() {
        viewModelScope.launch {
            val lastUpdateTime = updateManager.getLastUpdateTimeAgo()
            _updateState.value = UpdateState.Idle(NumberUtils.toPersianNumbers(lastUpdateTime))
        }
    }

    // Load Stats from Database
    private fun loadStatsFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val statsMap = dbHelper.getUserStats()
                _suspiciousLinksDetected.value =
                    statsMap[AppConstants.STAT_FLAGGED_LINK_DETECTED] ?: 0
                _suspiciousSmsDetected.value = statsMap[AppConstants.STAT_FLAGGED_SMS_DETECTED] ?: 0
                _suspiciousAppDetected.value = statsMap[AppConstants.STAT_FLAGGED_APP_DETECTED] ?: 0
            } catch (e: Exception) {
                Log.e("StatsBug", "Error loading stats", e)
            }
        }
    }

    // Function to trigger a database update
    fun updateDatabase() {
        if (_updateState.value is UpdateState.Updating) return // Prevent multiple update operations

        viewModelScope.launch {
            try {
                _updateState.value = UpdateState.Updating


                when (val result = updateManager.updateDatabase()) {
                    is UpdateManager.UpdateResult.Success -> {
                        // Reload the database update time after a successful update
                        loadDatabaseUpdateTime()
                        checkForAppUpdates()
                        _updateState.value =
                            UpdateState.Success(updateManager.getLastUpdateTimeAgo())
                    }

                    is UpdateManager.UpdateResult.Skipped -> {
                        _updateState.value = UpdateState.Idle(updateManager.getLastUpdateTimeAgo())
                    }

                    is UpdateManager.UpdateResult.Error -> {
                        _updateState.value = UpdateState.Error(result.message)
                    }
                }


            } catch (e: Exception) {
                Log.e("UpdateBug", "Error updating database", e)
                _updateState.value = UpdateState.Error(e.message ?: "خطای نامشخص")
            }
        }
    }

    private fun checkForAppUpdates() {
        viewModelScope.launch {
            try {
                val updateState = updateManager.checkAppUpdate()
                _updateDialogState.value = updateState
            } catch (e: Exception) {
                Log.e("UpdateBug", "Error checking for app updates", e)
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateDialogState.value = null
    }

    private fun loadRandomSponsor() {
        // Only load if sponsorData is null
        if (_sponsorData.value != null) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sponsor = getRandomSponsor()
                sponsor?.let {
                    _sponsorData.value = SponsorData(
                        title = it.getString("title"),
                        description = it.optString("description", ""),
                        logoUrl = it.optString("logo", ""),
                        link = it.getString("link"),
                        color = it.optString("color", "")
                    )
                }
            } catch (e: Exception) {
                Log.e("SponsorBug", "Error loading sponsor", e)
            }
        }
    }

    private fun getRandomSponsor(): JSONObject? {
        return try {
            val sponsorsJsonString = sharedPreferences.getString("sponsors", null) ?: return null
            val sponsorsJsonObject = JSONObject(sponsorsJsonString)
            val sponsorsJsonArray = sponsorsJsonObject.getJSONArray("sponsors")

            if (sponsorsJsonArray.length() > 0) {
                val randomIndex = Random().nextInt(sponsorsJsonArray.length())
                sponsorsJsonArray.getJSONObject(randomIndex)
            } else null
        } catch (e: Exception) {
            Log.e("SponsorBug", "Error getting random sponsor", e)
            null
        }
    }

    private fun startUrlInterceptorService() {
        context.startService(Intent(context, UrlDetectionService::class.java))
    }

    fun checkInitialPermissions() {

        // SMS
        checkSmsPermission()

        // Accessibility
        checkAccessibilityPermission()

        // Overlay
        checkOverlayPermission()

        // Notifications
        checkNotificationPermission()
    }


    // Check individual permissions
    fun checkSmsPermission() {
        _smsPermissionStatus.value = permissionManager.checkSmsPermission()
    }

    fun checkAccessibilityPermission() {
        _accessibilitySettingStatus.value = permissionManager.checkAccessibilityPermission()
    }

    fun checkOverlayPermission() {
        _overlayPermissionStatus.value = permissionManager.checkOverlayPermission()
    }

    fun checkNotificationPermission() {
        _notificationPermissionStatus.value = permissionManager.checkNotificationPermission()
    }

    // Update permission status
    fun updatePermissionStatus(type: PermissionType, isGranted: Boolean) {
        when (type) {
            PermissionType.SMS -> _smsPermissionStatus.value = isGranted
            PermissionType.ACCESSIBILITY -> _accessibilitySettingStatus.value = isGranted
            PermissionType.OVERLAY -> _overlayPermissionStatus.value = isGranted
            PermissionType.NOTIFICATIONS -> _notificationPermissionStatus.value = isGranted
        }
    }


    enum class PermissionType {
        SMS, ACCESSIBILITY, OVERLAY, NOTIFICATIONS
    }

    data class SponsorData(
        val title: String,
        val description: String?,
        val logoUrl: String?,
        val link: String,
        val color: String?
    )

    data class UpdateDialogState(
        val latestVersionName: String,
        val forceUpdate: Boolean,
        val links: List<Pair<String, String>>
    )


}