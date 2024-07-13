package nu.milad.motmaenbash.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)


        // Set up donate button
        binding.donateButton.setOnClickListener() {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://milad.nu/page/donate"))
            startActivity(intent)
        }


    }
}