package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.consts.AppConstants.PREF_KEY_LAST_CHANGELOG_SHOW_VERSION
import nu.milad.motmaenbash.models.AppUpdate
import nu.milad.motmaenbash.models.Link
import nu.milad.motmaenbash.models.Stats
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.UpdateManager
import nu.milad.motmaenbash.utils.UpdateManager.UpdateState
import nu.milad.motmaenbash.utils.dataStore
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random


class MainViewModel(private val context: Application) : BasePermissionViewModel(context) {

    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(context) }

    // Tip of the Day
    private val _tipOfTheDay = MutableStateFlow<String?>(null)
    val tipOfTheDay: StateFlow<String?> = _tipOfTheDay.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    // Stats
    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    // Link
    private val _linkData = MutableStateFlow<Link?>(null)
    val linkData: StateFlow<Link?> = _linkData.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(AppConstants.APP_PREFERENCES, Context.MODE_PRIVATE)

    private val updateManager = UpdateManager(context)

    // Database Update
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle("بارگذاری..."))
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updateDialogState = MutableStateFlow<AppUpdate?>(null)
    val updateDialogState: StateFlow<AppUpdate?> = _updateDialogState.asStateFlow()

    //Changelog
    val _showChangelogDialog = MutableStateFlow(false)
    val showChangelogDialog: StateFlow<Boolean> = _showChangelogDialog

    init {
        refreshTipOfTheDay()
        loadDatabaseUpdateTime()
        loadStatsFromDatabase()
        loadRandomLink()
        checkVersionForChangelog()

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
                _stats.value = dbHelper.getUserStats()

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

    private fun loadRandomLink() {
        // Only load if linkData is null
        if (_linkData.value != null) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val link = getRandomLink()
                link?.let {
                    _linkData.value = Link(
                        type = it.optInt("type", 1),
                        title = it.optString("title", ""),
                        description = it.optString("description", ""),
                        image = it.optString("image", ""),
                        link = it.getString("link"),
                        color = it.optString("color", "")
                    )
                }
            } catch (e: Exception) {
                Log.e("LinkBug", "Error loading link", e)
            }
        }
    }

    private fun getRandomLink(): JSONObject? {
        return try {
            val linksJsonString = sharedPreferences.getString("links", null) ?: return null
            val linksJsonArray = JSONArray(linksJsonString)
            if (linksJsonArray.length() > 0) {
                val randomIndex = Random().nextInt(linksJsonArray.length())
                linksJsonArray.getJSONObject(randomIndex)
            } else null
        } catch (e: Exception) {
            Log.e("LinkBug", "Error getting random link", e)
            null
        }
    }


    // Check changelog version
    private fun checkVersionForChangelog() {
        viewModelScope.launch {
            val currentVersion = BuildConfig.VERSION_CODE
            val lastShownVersion: Int = context.dataStore.data.firstOrNull()?.get(
                intPreferencesKey(PREF_KEY_LAST_CHANGELOG_SHOW_VERSION)
            ) ?: 0

            if (currentVersion > lastShownVersion) {
                _showChangelogDialog.value = true
            }

        }
    }


    suspend fun setLastShownVersion() {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey(PREF_KEY_LAST_CHANGELOG_SHOW_VERSION)] =
                BuildConfig.VERSION_CODE
        }
    }

}