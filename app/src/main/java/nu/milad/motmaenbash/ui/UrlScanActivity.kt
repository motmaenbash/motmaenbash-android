package nu.milad.motmaenbash.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nu.milad.motmaenbash.databinding.ActivityUrlScanBinding

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
        }


        // Set the click listener for the scan button
        binding.scanButton.setOnClickListener {
            var url = urlInput.text.toString()

        }


    }


}