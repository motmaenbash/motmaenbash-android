package nu.milad.motmaenbash.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityAlertDialogBinding

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





        binding.okButton.setOnClickListener {
            finish()
        }

        binding.uninstallButton.setOnClickListener {
            uninstallApp()
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

    private fun uninstallApp() {


        val intent = Intent(Intent.ACTION_DELETE)
        intent.setData(Uri.parse("package:$packageName"))
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        startActivity(intent)


    }


}
