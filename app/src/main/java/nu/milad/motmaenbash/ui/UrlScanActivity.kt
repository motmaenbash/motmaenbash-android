package nu.milad.motmaenbash.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nu.milad.motmaenbash.databinding.ActivityUrlScanBinding
import nu.milad.motmaenbash.utils.UrlUtils

class UrlScanActivity : BaseActivity() {

    private lateinit var binding: ActivityUrlScanBinding
    private lateinit var urlInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = ActivityUrlScanBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        urlInput = binding.urlInput


        // Check if there's a URL passed from the Intent
        intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.let { url ->
            val urlString = url.toString()
            if (UrlUtils.validateUrl(urlString)) {
                urlInput.setText(UrlUtils.removeUrlPrefixes(urlString))
                scanUrl(urlString)
            } else {
                showToast("URL نامعتبر است")
                finish()
            }
        }


        // Set the click listener for the scan button
        binding.scanButton.setOnClickListener {

            val url = UrlUtils.removeUrlPrefixes(urlInput.text.toString())

            if (UrlUtils.validateUrl(url)) {
                scanUrl(url)
            } else {
                showToast("لطفاً یک URL وارد کنید")
            }
        }


    }


    private fun scanUrl(url: String) {

        val resultText = when {
            UrlUtils.isShaparakSubdomain(url) -> "این URL درگاه مطمین است"
            isDomainInDatabase(url) -> "این URL مشکوک به فیشینگ است."
            else -> "این URL امن به نظر می‌رسد."
        }
        binding.scanResult.text = resultText
        binding.scanResultCard.visibility = View.VISIBLE
    }

    private fun isDomainInDatabase(url: String): Boolean {

       return true;
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}