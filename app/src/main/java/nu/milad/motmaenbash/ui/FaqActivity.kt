package nu.milad.motmaenbash.ui

import android.os.Bundle
import nu.milad.motmaenbash.databinding.ActivityFaqBinding

class FaqActivity : BaseActivity() {

    private lateinit var binding: ActivityFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
}