package nu.milad.motmaenbash.ui

import android.media.MediaPlayer
import android.os.Bundle
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityAlertDialogBinding
import nu.milad.motmaenbash.utils.AppUtils

class AlertDialogActivity : BaseActivity() {

    companion object {
        const val EXTRA_ALERT_TITLE = "extra_alert_title"
        const val EXTRA_ALERT_MESSAGE = "extra_alert_message"
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
        const val EXTRA_ALERT_LEVEL = "extra_alert_lever"

        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_INFO = "extra_app_info"
    }


    private lateinit var binding: ActivityAlertDialogBinding
    private lateinit var packageName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playDingSound()

        packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).toString()
        val appInfo = intent.getStringExtra(EXTRA_APP_INFO)
        val title = intent.getStringExtra(EXTRA_ALERT_TITLE)
        val message = intent.getStringExtra(EXTRA_ALERT_MESSAGE)

        if (title == null) {
            binding.titleTextView.text = title
        }

        binding.messageTextView.text = message




        binding.uninstallButton.setOnClickListener {
            AppUtils.uninstallApp(this, packageName, packageName) //todo: pass app name
        }


        binding.okButton.setOnClickListener {
            finish()
        }

        // Set dialog to be not cancelable
        setFinishOnTouchOutside(false)
    }


    private fun playDingSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.dingding)
        mediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.release()
        }
        mediaPlayer.start()
    }


}
