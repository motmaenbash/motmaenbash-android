package nu.milad.motmaenbash.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityUrlScanBinding
import nu.milad.motmaenbash.databinding.ActivityUserReportBinding

class UserReportActivity : BaseActivity() {

    private lateinit var binding: ActivityUserReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserReportBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        binding.submitReportButton.setOnClickListener {
            val reportType = binding.reportType.text.toString()
            val reportDetails = binding.reportDetails.text.toString()

            if (reportType.isBlank() || reportDetails.isBlank()) {
                Toast.makeText(this, "لطفا تمام اطلاعات را وارد کنید", Toast.LENGTH_SHORT).show()
            } else {
                // todo: Handle the report submission
            }
        }
    }
}
