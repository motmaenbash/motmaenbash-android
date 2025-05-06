package nu.milad.motmaenbash.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.withTimeout
import nu.milad.motmaenbash.utils.UpdateManager


class DatabaseUpdateWorker(
    context: Context, params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DatabaseUpdateWorker"
        private const val TIMEOUT_MINUTES = 3 * 60 * 1_000L // 3-minute timeout
    }

    override suspend fun doWork(): Result {
        val updateManager = UpdateManager(applicationContext)

        return try {
            withTimeout(TIMEOUT_MINUTES) {
                val isUpdateSuccessful = updateManager.executeDataUpdate(isManualUpdate = false)
                when {
                    isUpdateSuccessful -> {
                        Log.d(TAG, "Database update completed successfully.")
                        Result.success()
                    }

                    runAttemptCount < 2 -> {
                        Log.d(TAG, "Retry attempt: $runAttemptCount")
                        Result.retry()
                    }

                    else -> {
                        Log.e(TAG, "Database update failed after multiple attempts")
                        Result.failure()
                    }

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating database", e)
            if (runAttemptCount < 2) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}