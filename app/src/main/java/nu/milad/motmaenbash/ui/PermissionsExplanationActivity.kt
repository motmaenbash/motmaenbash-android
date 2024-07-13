package nu.milad.motmaenbash.ui

import android.os.Bundle
import nu.milad.motmaenbash.databinding.ActivityPermissionsExplanationBinding

class PermissionsExplanationActivity : BaseActivity() {

    private lateinit var binding: ActivityPermissionsExplanationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPermissionsExplanationBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}