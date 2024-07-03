package tech.tookan.motmaenbash.ui

import android.os.Bundle
import android.widget.EditText

import androidx.appcompat.app.AppCompatActivity
import tech.tookan.motmaenbash.databinding.ActivityUrlScanBinding

class UrlScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrlScanBinding
    private lateinit var urlInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = ActivityUrlScanBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        urlInput = binding.urlInput


    }
}