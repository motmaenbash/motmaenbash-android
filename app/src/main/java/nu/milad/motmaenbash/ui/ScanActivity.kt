package nu.milad.motmaenbash.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityScanBinding
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.ScanUtils

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var scanUtils: ScanUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        dbHelper = DatabaseHelper(this)
        scanUtils = ScanUtils(this)

        setupUI()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        binding.lastScanTime.text = getString(
            R.string.last_scan_time, NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
        )

        binding.scanButton.setOnClickListener {
//            startScan()
        }

        binding.stopButton.setOnClickListener {
//            stopScan()
        }
    }


}
