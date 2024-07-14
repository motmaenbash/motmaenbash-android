package nu.milad.motmaenbash.utils


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit


class UpdateManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)


    private val TAG = "PerformDatabaseUpdate"

    suspend fun updateDatabase(): Boolean {

        val lastUpdateTime = getLastUpdateTime()
        if (lastUpdateTime != 0L) {

            val diffInMillis = DateUtils.getCurrentTimeInMillis() - lastUpdateTime
            val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)

            // If the last update was less than an hour ago, show a toast and return false
            if (diffInMinutes < 1) {
                Toast.makeText(
                    context,
                    "پایگاه داده کمتر از یک ساعت پیش به‌روزرسانی شده است",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        val success = performDatabaseUpdate()
        if (success) {
            setLastUpdateTime(DateUtils.getCurrentTimeInMillis())
            Toast.makeText(
                context, "به‌روزرسانی پایگاه داده با موفقیت انجام شد", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "به‌روزرسانی پایگاه داده ناموفق بود. لطفا مدتی بعد مجددا تلاش کنید.",
                Toast.LENGTH_SHORT
            ).show()
        }
        return success
    }


    private suspend fun performDatabaseUpdate(): Boolean {
        return withContext(Dispatchers.IO) {
            var success = false

            val url = "https://..." //todo: fix url


            try {
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)

                val dbHelper = DatabaseHelper(context)
                dbHelper.clearDatabase()
                dbHelper.populateDatabaseWithFetchedData(jsonObject)

                success = true



            } catch (e: Exception) {
                Log.e(TAG, "Error during database update: ${e.message}", e)
            }

            success
        }
    }

    private fun setLastUpdateTime(lastUpdateTime: Long) {
        with(sharedPreferences.edit()) {
            putLong("last_database_update_time", lastUpdateTime)
            apply()
        }
    }

    fun getLastUpdateTime(): Long {
        val lastUpdateTime = sharedPreferences.getLong("last_database_update_time", 0)
        return lastUpdateTime
    }


    fun getLastUpdateTimeAgo(): String {

        val lastUpdateTime = getLastUpdateTime()
        return if (lastUpdateTime == 0L) {
            "نامشخص"
        } else {
            DateUtils.timeAgo(lastUpdateTime)
        }


    }
}
