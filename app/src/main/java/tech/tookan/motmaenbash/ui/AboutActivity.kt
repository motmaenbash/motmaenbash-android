package tech.tookan.motmaenbash.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.tookan.motmaenbash.BuildConfig

import tech.tookan.motmaenbash.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appVersion.text = "نسخه: ${BuildConfig.VERSION_NAME}"


    }
}