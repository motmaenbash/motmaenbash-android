package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_APP_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_LINK_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_SMS_DETECTED
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity

object AlertUtils {

    fun showAlert(
        context: Context,
        alertType: Int,
        alertLevel: String,
        param1: String? = null,
        param2: String? = null,
        info: String? = null,
    ) {

        // Increment statistics and log the event based on the alert type
        incrementStatAndLogEvent(context, alertType)

        val intent = Intent(context, AlertHandlerActivity::class.java).apply {
            putExtra(AlertHandlerActivity.EXTRA_ALERT_TYPE, alertType)
            putExtra(AlertHandlerActivity.EXTRA_ALERT_LEVEL, alertLevel)
            if (param1 != null) {
                putExtra(AlertHandlerActivity.EXTRA_PARAM1, param1)
            }
            if (param2 != null) {
                putExtra(AlertHandlerActivity.EXTRA_PARAM2, param2)
            }
            putExtra(AlertHandlerActivity.EXTRA_ALERT_INFO, info)
            // Ensure the alert activity is launched as a new task to prevent conflicts
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        }
        // Start the AlertHandlerActivity to display the alert
        context.startActivity(intent)
    }

    private fun incrementStatAndLogEvent(context: Context, statType: Int) {
        val dbHelper = DatabaseHelper(context.applicationContext)
        try {

            val statKeys = when (statType) {
                AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                AlertHandlerActivity.ALERT_TYPE_SMS_LINK_FLAGGED -> listOf(
                    STAT_FLAGGED_LINK_DETECTED,
                    STAT_FLAGGED_SMS_DETECTED
                )

                AlertHandlerActivity.ALERT_TYPE_SMS_KEYWORD_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                AlertHandlerActivity.ALERT_TYPE_SMS_PATTERN_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED -> listOf(STAT_FLAGGED_APP_DETECTED)
                AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED -> listOf(STAT_FLAGGED_LINK_DETECTED)
                AlertHandlerActivity.ALERT_TYPE_DOMAIN_FLAGGED -> listOf(STAT_FLAGGED_LINK_DETECTED)
                else -> emptyList()
            }

            // If a valid statKey exists, increment the corresponding statistic in the database
            statKeys.forEach { key ->
                // Increment each relevant statistic
                dbHelper.incrementUserStat(key)

                // Log event in Firebase Analytics (only for non-debug builds)
                if (!BuildConfig.DEBUG) {
                    val firebaseAnalytics = Firebase.analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                        param(FirebaseAnalytics.Param.ITEM_CATEGORY, key)
                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "flagged")
                    }
                }


            }
        } catch (e: Exception) {
            Log.e("AlertUtils", "Error incrementing stat: ${e.message}")
        } finally {
            dbHelper.close()
        }
    }
}