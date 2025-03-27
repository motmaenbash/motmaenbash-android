package nu.milad.motmaenbash.utils


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.consts.AppConstants.APP_PREFERENCES
import nu.milad.motmaenbash.consts.AppConstants.PREF_KEY_LAST_UPDATE_TIME
import nu.milad.motmaenbash.consts.AppConstants.UPDATE_DATA_URL
import nu.milad.motmaenbash.consts.AppConstants.UPDATE_TIPS_URL
import nu.milad.motmaenbash.viewmodels.MainViewModel
import nu.milad.motmaenbash.viewmodels.SettingsViewModel
import nu.milad.motmaenbash.workers.DatabaseUpdateWorker
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateManager(
    private val context: Context
) {
    private val TAG = "UpdateManager"

    private val sharedPreferences = context.getSharedPreferences(
        APP_PREFERENCES, Context.MODE_PRIVATE
    )

    private val workManager = WorkManager.getInstance(context)
    private val dbHelper = DatabaseHelper(context)

    sealed class UpdateState {
        data class Idle(val lastUpdateTime: String) : UpdateState()
        object Updating : UpdateState()
        data class Success(val lastUpdateTime: String) : UpdateState()
        data class Skipped(val message: String) : UpdateState()
        data class Error(val message: String) : UpdateState()

    }

    sealed class UpdateResult {
        object Success : UpdateResult()
        object Skipped : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    /**
     * Initiates a database update if conditions are met
     * @return True if update was performed, false otherwise
     */
    suspend fun updateDatabase(): UpdateResult {
        if (!shouldPerformUpdate()) {
            return UpdateResult.Skipped
        }

        return if (NetworkUtils.isInternetAvailable(context)) {
            try {
                executeDataUpdate(showToast = true)
                UpdateResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "Error during database update: ${e.message}", e)
                UpdateResult.Error(e.message ?: "Unknown error")
            }
        } else {
            // Internet connection is not available
            showNetworkErrorMessage()
            UpdateResult.Skipped
        }

    }

    /**
     * Executes the actual database update process
     * @param displayFeedback Whether to show toast messages
     * @return True if update was successful, false otherwise
     */
    suspend fun executeDataUpdate(
        showToast: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Fetch data from server
            val dataResponse = URL(UPDATE_DATA_URL).readText()
            val tipsResponse = URL(UPDATE_TIPS_URL).readText()

            // Combine data
            val dataJsonObject = JSONObject(dataResponse).apply {
                put("tips", JSONArray(tipsResponse))
            }

            // Update local database
            dbHelper.clearDatabase()
            dbHelper.populateDatabaseWithFetchedData(dataJsonObject)

            // Update timestamp and fetch additional data
            setLastUpdateTime(DateUtils.getCurrentTimeInMillis())
            fetchSponsorData()

            if (showToast) {

                // Show success message if needed
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, "به‌روزرسانی پایگاه داده با موفقیت انجام شد", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during database update: ${e.message}", e)

            if (showToast) {

                // Show error message if needed
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "به‌روزرسانی پایگاه داده ناموفق بود. لطفا مدتی بعد مجددا تلاش کنید.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            false
        }
    }

    /**
     * Checks for app updates from server
     * @return UpdateDialogState if update is available, null otherwise
     */
    suspend fun checkAppUpdate(): MainViewModel.UpdateDialogState? = withContext(Dispatchers.IO) {
        try {
            val response = URL(AppConstants.UPDATE_APP_URL).readText()

            val jsonObject = JSONObject(response)

            val currentVersionCode = BuildConfig.VERSION_CODE
            val latestVersionCode = jsonObject.getInt("latest_version_code")

            if (currentVersionCode < latestVersionCode) {
                val latestVersionName = jsonObject.getString("latest_version_name")
                val forceUpdateVersionCode = jsonObject.getInt("force_update_version_code")
                val updateLinks = jsonObject.getJSONArray("update_links")

                val links = (0 until updateLinks.length()).map { i ->
                    updateLinks.getJSONObject(i).let {
                        it.getString("title") to it.getString("link")
                    }
                }

                return@withContext MainViewModel.UpdateDialogState(
                    latestVersionName = latestVersionName,
                    forceUpdate = currentVersionCode < forceUpdateVersionCode,
                    links = links
                )
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for app updates: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Saves sponsor data to shared preferences
     */
    private fun saveSponsorData(sponsorsJsonObject: JSONObject) {
        sharedPreferences.edit().apply {
            putString("sponsors", sponsorsJsonObject.toString())
            apply()
        }
    }

    /**
     * Fetches sponsor data from server
     */
    private suspend fun fetchSponsorData() {
        withContext(Dispatchers.IO) {
            try {
                val sponsorResponse = URL(AppConstants.UPDATE_SPONSOR_URL).readText()
                val sponsorsJSONObject = JSONObject(sponsorResponse)
                saveSponsorData(sponsorsJSONObject)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching sponsor data: ${e.message}", e)
            }
        }
    }

    /**
     * Saves the last update timestamp
     */
    private fun setLastUpdateTime(lastUpdateTime: Long) {
        sharedPreferences.edit().apply {
            putLong(PREF_KEY_LAST_UPDATE_TIME, lastUpdateTime)
            apply()
        }
    }

    /**
     * Gets the last update timestamp
     * @return Timestamp in milliseconds, 0 if never updated
     */
    fun getLastUpdateTime(): Long {
        return sharedPreferences.getLong("last_database_update_time", 0)
    }

    /**
     * Gets a human-readable string indicating when the last update occurred
     * @return String like "2 hours ago" or "Unknown" if never updated
     */
    fun getLastUpdateTimeAgo(): String =
        getLastUpdateTime().takeIf { it > 0 }?.let(DateUtils::timeAgo) ?: "نامشخص"

    /**
     * Schedules periodic database updates
     */
    suspend fun scheduleDatabaseUpdate() {
        val frequency = context.dataStore.data
            .firstOrNull()
            ?.get(SettingsViewModel.DATABASE_UPDATE_FREQ)
            ?: context.resources.getStringArray(R.array.database_update_frequency_values).first()


        Log.d("AppInitialization", "Scheduling database update every $frequency hours")


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Create a periodic work request
        val databaseUpdateRequest =
            PeriodicWorkRequestBuilder<DatabaseUpdateWorker>(
                frequency.toLong(),
                TimeUnit.HOURS
            ).setConstraints(
                constraints
            ).setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES
            ).build()


        // Enqueue the periodic work
        workManager.enqueueUniquePeriodicWork(
            "database_update_work",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            databaseUpdateRequest
        )
    }


    /**
     * Determines if an update should be performed based on last update time
     * @return True if update should be performed, false otherwise
     */
    private fun shouldPerformUpdate(): Boolean {
        val lastUpdateTime = getLastUpdateTime()

        // Always update in debug mode or if never updated
        if (BuildConfig.DEBUG || lastUpdateTime == 0L) return true

        val hoursSinceUpdate = TimeUnit.MILLISECONDS.toHours(
            DateUtils.getCurrentTimeInMillis() - lastUpdateTime
        )

        // Only update if more than 1 hours have passed
        return if (hoursSinceUpdate < 1) {
            Toast.makeText(
                context, "پایگاه داده کمتر از ۱ ساعت پیش به‌روزرسانی شده است", Toast.LENGTH_SHORT
            ).show()
            false
        } else true
    }

    /**
     * Shows a network error message
     */
    private fun showNetworkErrorMessage() {
        Toast.makeText(
            context,
            "عدم دسترسی به اینترنت. لطفا اتصال خود را بررسی و مجددا تلاش کنید.",
            Toast.LENGTH_SHORT
        ).show()
    }
}