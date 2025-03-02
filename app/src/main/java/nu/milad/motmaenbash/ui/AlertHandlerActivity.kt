package nu.milad.motmaenbash.ui

import android.app.ActivityManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityAlertDialogBinding
import nu.milad.motmaenbash.utils.PackageUtils

class AlertHandlerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALERT_TITLE = "extra_alert_title"
        const val EXTRA_ALERT_SUB_TITLE = "extra_alert_sub_title"
        const val EXTRA_ALERT_MESSAGE = "extra_alert_message"
        const val EXTRA_ALERT_INFO = "extra_alert_info"
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
        const val EXTRA_ALERT_LEVEL = "extra_alert_lever"

        const val EXTRA_PARAM1 = "extra_param1"
        const val EXTRA_PARAM2 = "extra_param2"


        // Alert types

        sealed class AlertType {
            data class SmsSenderFlagged(val sender: String) : AlertType()
            data class SmsLinkFlagged(val message: String) : AlertType()
            data class SmsKeywordFlagged(val message: String, val sender: String) : AlertType()
            data class SmsPatternFlagged(val message: String) : AlertType()
            data class SmsSafe(val message: String, val sender: String) : AlertType()
            data class AppFlagged(val appName: String) : AlertType()
            object UrlFlagged : AlertType()
            object DomainFlagged : AlertType()
        }

        // SMS
        const val ALERT_TYPE_SMS_SENDER_FLAGGED = 1
        const val ALERT_TYPE_SMS_LINK_FLAGGED = 2
        const val ALERT_TYPE_SMS_KEYWORD_FLAGGED = 3
        const val ALERT_TYPE_SMS_PATTERN_FLAGGED = 4
        const val ALERT_TYPE_SMS_SAFE = 5 // No flagged content detected

        // App
        const val ALERT_TYPE_APP_FLAGGED = 6

        // URL
        const val ALERT_TYPE_URL_FLAGGED = 7
        const val ALERT_TYPE_DOMAIN_FLAGGED = 8

        // Alert levels
        enum class AlertLevel {
            NORMAL, WARNING, ERROR
        }
    }


    private lateinit var binding: ActivityAlertDialogBinding


    private var soundPool: SoundPool? = null
    private var dingSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityAlertDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        window?.setFlags(
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//        )

        val taskDescription = ActivityManager.TaskDescription(
            getString(R.string.alert_dialog_activity_title), // Label (optional)
            null, Color.TRANSPARENT
        )

        setTaskDescription(taskDescription)

        // Initialize SoundPool
        soundPool =
            SoundPool.Builder().setMaxStreams(1) // Set the maximum number of simultaneous streams
                .build()

        // Load the sound
        dingSoundId = soundPool?.load(this, R.raw.dingding, 1) ?: 0
        soundPool?.setOnLoadCompleteListener { _, id, status ->
            if (status == 0 && id == dingSoundId) {
                playDingSound()
            }
        }


        val param1 = intent.getStringExtra(EXTRA_PARAM1).toString()
        val param2 = intent.getStringExtra(EXTRA_PARAM2).toString()
//        val appInfo = intent.getStringExtra(EXTRA_APP_INFO)
//        val title = intent.getStringExtra(EXTRA_ALERT_TITLE)
        val subTitle = intent.getStringExtra(EXTRA_ALERT_SUB_TITLE)
//        val message = intent.getStringExtra(EXTRA_ALERT_MESSAGE)
        val alertType = intent.getIntExtra(EXTRA_ALERT_TYPE, 0)
        val alertLevel = intent.getStringExtra(EXTRA_ALERT_LEVEL)



        if (alertType == ALERT_TYPE_APP_FLAGGED) {
            binding.uninstallButton.visibility = View.VISIBLE
        } else {
            binding.uninstallButton.visibility = View.GONE
        }

        val (title, message) = when (alertType) {
            ALERT_TYPE_SMS_SENDER_FLAGGED -> Pair(
                "شناسایی فرستنده مشکوک!",
                "این فرستنده در لیست سیاه قرار دارد." + "\n\nفرستنده: $param1",
            )


            ALERT_TYPE_SMS_LINK_FLAGGED -> Pair(
                "شناسایی لینک مشکوک!", "متن پیام: $param2\n\nفرستنده: $param1",
            )


            ALERT_TYPE_SMS_KEYWORD_FLAGGED -> Pair(
                "کلمه کلیدی مشکوک!", "متن پیام: $param2\n\nفرستنده: $param1",
            )


            ALERT_TYPE_SMS_PATTERN_FLAGGED -> Pair(
                "شناسایی پیامک مشکوک!", "متن پیام: $param2",
            )


            ALERT_TYPE_SMS_SAFE -> Pair(
                "پیامک ایمن", "محتوای مشکوکی شناسایی نشد." + "\n\n$param2\n\nفرستنده: $param1"
            )

            ALERT_TYPE_APP_FLAGGED -> Pair(
                "برنامه مشکوک!",
                "برنامه  $param1 یک برنامه مخرب و بدافزار است. لطفا بدون اینکه برنامه را باز کنید، سریعا ان را حذف کنید."
            )

            ALERT_TYPE_URL_FLAGGED -> Pair("آدرس مشکوک!", "این آدرس در لیست سیاه قرار دارد.")
            ALERT_TYPE_DOMAIN_FLAGGED -> Pair("دامنه مشکوک!", "این دامنه در لیست سیاه قرار دارد.")
            else -> Pair("خطا", "نوع هشدار نامشخص.")
        }


        binding.titleTextView.text = title
        binding.alertBadge.text = title
        binding.messageTextView.text = message

        if (!subTitle.isNullOrEmpty()) {
            binding.subTitleTextView.text = param1
        }


        // Set the badge background color based on the alert type
        binding.alertBadge.setBackgroundColor(
            ContextCompat.getColor(
                this, when (alertLevel) {
                    AlertLevel.NORMAL.toString() -> R.color.green_dark
                    AlertLevel.WARNING.toString() -> R.color.yellow
                    AlertLevel.ERROR.toString() -> R.color.red
                    else -> R.color.grey_dark
                }
            )
        )


        binding.uninstallButton.setOnClickListener {
            PackageUtils.uninstallApp(this, param1, "") //todo: pass app name
        }


        binding.closeButton.setOnClickListener {
//            finish()
            finishAndRemoveTask()
        }

        binding.okButton.setOnClickListener {
//            finish()
            finishAndRemoveTask()
        }


        // Set dialog to be not cancelable
        setFinishOnTouchOutside(false)


//        CustomAlertDialog(this).setTitle("هشدار برنامه نصب شده۱").setSubtitle("com.example.app")
//            .setMessage("این برنامه مشکوک است و ممکن است حاوی محتوای مخرب باشد.")
////            .setIcon(getDrawable(R.mipmap.ic_launcher))
//            .setAlertType(CustomAlertDialog.AlertType.ERROR).setPositiveButton("حذف") { dialog, _ ->
//                // Handle uninstall here
//            }.setNegativeButton("لغو") { dialog, _ ->
//                // No action needed for negative button
//
//            }.setCancelable(false).show()


    }


    private fun playDingSound2() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.dingding)
        mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.release()
        }
        mediaPlayer.start()
    }

    private fun playDingSound() {
        soundPool?.play(dingSoundId, 1f, 1f, 1, 0, 1f)
    }


    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }


}
