package nu.milad.motmaenbash.ui

import android.os.Bundle

import nu.milad.motmaenbash.databinding.ActivityUserReportBinding

class UserReportActivity : BaseActivity() {

    private lateinit var binding: ActivityUserReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserReportBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        binding.submitReportButton.setOnClickListener {

        }
    }
}
