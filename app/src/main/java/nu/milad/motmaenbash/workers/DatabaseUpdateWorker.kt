package nu.milad.motmaenbash.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import nu.milad.motmaenbash.utils.UpdateManager


class DatabaseUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("DatabaseUpdateWorker", "Worker: Database update started.")
        val updateManager = UpdateManager(applicationContext)
        return try {
            val isUpdateSuccessful = updateManager.executeDataUpdate(isManualUpdate = false)

            if (isUpdateSuccessful) {
                Log.d("DatabaseUpdateWorker", "Worker: Database update completed successfully.")
                Result.success()
            } else {
                Log.d("DatabaseUpdateWorker", "Worker: Database update failed.")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e("DatabaseUpdateWorker", "Worker: Error updating database", e)
            Result.retry()
        }
    }
}