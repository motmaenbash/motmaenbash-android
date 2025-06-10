package nu.milad.motmaenbash.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_APP_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_LINK_DETECTED
import nu.milad.motmaenbash.consts.AppConstants.STAT_FLAGGED_SMS_DETECTED
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity
import nu.milad.motmaenbash.viewmodels.SettingsViewModel


object AlertUtils {


    // Settings
    private var playNeutralSmsSound = false
    private var showNeutralSmsDialog = true

    fun showAlert(
        context: Context,
        alertType: Alert.AlertType,
        alertLevel: Alert.AlertLevel,
        param1: String,
        param2: String
    ) {


        // Increment statistics and log the event based on the alert type
        incrementStatAndLogEvent(context, alertType, param1, param2)

        CoroutineScope(Dispatchers.Default).launch {

            // Load settings
            val preferences = context.dataStore.data.first()
            playNeutralSmsSound = preferences[SettingsViewModel.PLAY_NEUTRAL_SMS_SOUND] ?: true
            showNeutralSmsDialog = preferences[SettingsViewModel.SHOW_NEUTRAL_SMS_DIALOG] ?: true


            // Exit if alert is neutral and dialog is off
            if (alertType == Alert.AlertType.SMS_NEUTRAL && !showNeutralSmsDialog) return@launch

            if (alertType != Alert.AlertType.SMS_NEUTRAL) {

                // Play sound and vibrate
                AudioHelper(context).apply {
                    vibrateDevice(context)
                    playAlertSound()
                }
            }

        val (alertTitle, alertSummary, alertContent) = getAlertContent(alertType)

        val alert = Alert(
            type = alertType,
            level = alertLevel,
            title = alertTitle,
            summary = alertSummary,
            content = alertContent,
            param1 = param1,
            param2 = param2
        )

        val intent = Intent(context, AlertHandlerActivity::class.java).apply {
            //Pass alert to AlertHandlerActivity
            putExtra(AlertHandlerActivity.EXTRA_ALERT, alert)
            // Ensure the alert activity is launched as a new task to prevent conflicts
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        }
        // Start the AlertHandlerActivity to display the alert
        context.startActivity(intent)
    }
    }

    private fun incrementStatAndLogEvent(
        context: Context,
        alertType: Alert.AlertType,
        param1: String?,
        param2: String?
    ) {
        val dbHelper = DatabaseHelper(context.applicationContext)
        try {

            val statKeys = when (alertType) {
                Alert.AlertType.SMS_SENDER_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                Alert.AlertType.SMS_LINK_FLAGGED -> listOf(
                    STAT_FLAGGED_LINK_DETECTED,
                    STAT_FLAGGED_SMS_DETECTED
                )

                Alert.AlertType.SMS_KEYWORD_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                Alert.AlertType.SMS_PATTERN_FLAGGED -> listOf(
                    STAT_FLAGGED_SMS_DETECTED
                )

                Alert.AlertType.APP_FLAGGED -> listOf(STAT_FLAGGED_APP_DETECTED)
                else -> emptyList()
            }

            // If a valid statKey exists, increment the corresponding statistic in the database
            statKeys.forEach { key ->
                // Increment each relevant statistic
                dbHelper.incrementUserStat(key)
                if (!BuildConfig.DEBUG) {
                val firebaseAnalytics = Firebase.analytics
                firebaseAnalytics.logEvent("Motmaenbash_alert") {
                    param("alert_type", key)
                }
            }


            }
            // Log the alert history in the database
            dbHelper.logAlertHistory(alertType, param1, param2)

        } catch (e: Exception) {
            Log.e("AlertUtils", "Error incrementing stat: ${e.message}")
        } finally {
            dbHelper.close()
        }
    }

    fun getAlertContent(
        alertType: Alert.AlertType,
        threatType: Alert.ThreatType? = null,
        isSpecificUrl: Boolean = false
    ): Triple<String, String?, String?> =
        when (alertType) {


            Alert.AlertType.SMS_LINK_FLAGGED -> Triple(
                "لینک مشکوک در پیامک",
                "این پیامک حاوی لینک مشکوک است.",
                "این پیامک شامل لینک به وب‌سایت‌های مشکوک یا کلاهبرداری است. از باز کردن لینک خودداری کنید."
            )

            Alert.AlertType.SMS_KEYWORD_FLAGGED -> Triple(
                "کلمه کلیدی مشکوک در پیامک",
                "این پیامک حاوی واژه‌های مشکوک است.",
                "برخی واژه‌های به‌کاررفته در این پیامک معمولا در پیامک‌های کلاهبرداری دیده می‌شوند. توصیه می‌کنیم این پیامک را نادیده بگیرید و از هرگونه تعامل با آن خودداری کنید."
            )

            Alert.AlertType.SMS_PATTERN_FLAGGED -> Triple(
                "الگوی مشکوک در پیامک",
                "این پیامک به الگوهای کلاهبرداری شباهت دارد.",
                "محتوای این پیامک با الگوهای رایج در پیامک‌های فیشینگ و کلاهبرداری شباهت دارد. توصیه می‌کنیم از باز کردن لینک‌ها، پاسخ دادن یا ارائه اطلاعات شخصی خودداری کنید."
            )

            Alert.AlertType.SMS_SENDER_FLAGGED -> Triple(
                "پیامک از فرستنده مشکوک",
                "فرستنده، سابقه ارسال پیامک مشکوک دارد.",
                "براساس گزارش کاربرها، فرستنده این پیامک سابقه  این پیامک سابقه ارسال پارسال پیامک مشکوک داشته است. در باز کردن لینک‌ها و پاسخ به پیامک‌های این فرستنده احتیاط کنید."
            )

            Alert.AlertType.SMS_NEUTRAL -> Triple(
                "پیامک جدید",
                null,
                null
            )

            Alert.AlertType.APP_FLAGGED -> Triple(
                "نصب برنامه مشکوک",
                "برنامه نصب شده مشکوک است.",
                "براساس گزارش کاربرها، این برنامه مشکوک به بدافزار یا کلاهبرداری است و می‌تواند امنیت دستگاه و اطلاعات شما را تهدید کند. توصیه می‌شود بدون اجرای برنامه یا اعطای دسترسی، سریع آن را حذف کنید."
            )


            Alert.AlertType.URL_FLAGGED -> {
                val domainOrUrlText =
                    if (isSpecificUrl) "آدرس اینترنتی" else "دامنه اینترنتی"

                val subTitle =
                    domainOrUrlText + " مشکوک"

                when (threatType) {
                    Alert.ThreatType.PHISHING -> Triple(
                        subTitle,
                        "این $domainOrUrlText برای فیشینگ گزارش شده است.",
                        "براساس گزارش کاربرها، این $domainOrUrlText برای فیشینگ استفاده می‌شود و هدف آن سرقت اطلاعات شخصی و مالی شماست. توصیه می‌شود از ورود به این سایت خودداری کنید."
                    )

                    Alert.ThreatType.PONZI -> Triple(
                        subTitle,
                        "این $domainOrUrlText مربوط به طرح پانزی است.",
                        "براساس گزارش کاربرها، این $domainOrUrlText مرتبط با طرح‌های پانزی یا هرمی است که با وعده‌های غیرواقعی سود، سرمایه‌ی شما را به خطر می‌اندازند. توصیه می‌شود از ورود به این سایت خودداری کنید."
                    )

                    Alert.ThreatType.SCAM -> Triple(
                        subTitle,
                        "این $domainOrUrlText برای کلاهبرداری گزارش شده است.",
                        "براساس گزارش کاربرها، این $domainOrUrlText برای کلاهبرداری استفاده می‌شود و می‌تواند به ضرر مالی شما منجر شود. توصیه می‌شود از ورود به این سایت خودداری کنید."
                    )

                    else -> Triple(
                        subTitle,
                        "این $domainOrUrlText سابقه فعالیت مشکوک دارد.",
                        "براساس گزارش کاربرها، این $domainOrUrlText سابقه فعالیت مرتبط با پانزی، فیشینگ، اسکم یا کلاهبرداری دارد و ممکن است شما را به دام بیاندازد یا اطلاعات شما رابه سرقت ببرد. توصیه می‌شود از ورود به این سایت خودداری کرده و آن را به عنوان یک خطر امنیتی در نظر بگیرید."
                    )
                }
            }


        }

    fun getAlertHint(
        alertType: Alert.AlertType,
        threatType: Alert.ThreatType? = null,
    ): String? {
        return when (alertType) {
            Alert.AlertType.SMS_LINK_FLAGGED -> "از باز کردن لینک این پیامک خودداری کنید."
            Alert.AlertType.SMS_KEYWORD_FLAGGED,
            Alert.AlertType.SMS_PATTERN_FLAGGED -> "از باز کردن و تعامل با این پیامک خودداری کنید."

            Alert.AlertType.SMS_SENDER_FLAGGED -> "پیامک‌های این شماره را با دقت بررسی کنید."
            Alert.AlertType.APP_FLAGGED -> "بدون اجرای برنامه، سریع آن را حذف کنید."

            Alert.AlertType.URL_FLAGGED -> when (threatType) {
                Alert.ThreatType.PHISHING -> "از باز کردن این لینک خودداری کنید."
                Alert.ThreatType.SCAM -> "از ورود به این سایت خودداری کنید."
                Alert.ThreatType.PONZI -> "از ورود و پرداخت در این سایت خودداری کنید."
                else -> "از ورود به این سایت خودداری کنید."
            }

            else -> null


        }
    }

}
