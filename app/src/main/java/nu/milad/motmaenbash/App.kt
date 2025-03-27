package nu.milad.motmaenbash

import android.app.Application
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nu.milad.motmaenbash.utils.UpdateManager


class App : Application() {


    override fun onCreate() {
        super.onCreate()


        // Launch initialization in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            initializeApp()
        }

    }

    private suspend fun initializeApp() {


        try {


            val workManager = WorkManager.getInstance(applicationContext)
            val workInfos = withContext(Dispatchers.IO) {
                workManager.getWorkInfosForUniqueWork("database_update_work").get()
            }


            // Check if there's any work currently scheduled or running
            val isWorkScheduled = workInfos.any { workInfo ->
                val state = workInfo.state
                state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
            }


            if (!isWorkScheduled) {

                // Initialize UpdateManager and schedule database update
                val updateManager = UpdateManager(applicationContext)

                // Schedule periodic database update
                updateManager.scheduleDatabaseUpdate()

            }


        } catch (e: Exception) {

            e.printStackTrace()

        }

    }


}