package nu.milad.motmaenbash.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.databinding.ActivityMainBinding
import nu.milad.motmaenbash.services.AppInstallService
import nu.milad.motmaenbash.services.FloatingViewService
import nu.milad.motmaenbash.services.UrlDetectionService
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.NumberUtils.formatNumber

class MainActivity : BaseActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    companion object {
        private const val PERMISSIONS_REQUEST_RECIEVE_SMS = 100
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234
        private const val ACCESSIBILITY_SETTINGS_REQUEST_CODE = 5678

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        dbHelper = DatabaseHelper(this)

        setupWindowInsets()
        displayRandomTip()
        displayStats()
        setupClickListeners()



        updatePermissionsStatus()


        binding.appVersion.text = "نسخه: ${BuildConfig.VERSION_NAME}"



    }


    override fun onResume() {
        super.onResume()
        displayStats()
        updatePermissionsStatus()

    }


    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun setupClickListeners() {


        binding.refreshButton.setOnClickListener { displayRandomTip() }

        // FAQ button
        binding.faqButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    FaqActivity::class.java
                )
            )
        }

        // About button
        binding.aboutButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AboutActivity::class.java
                )
            )
        }

        // App Scan button
        binding.appScan.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ScanActivity::class.java
                )
            )
        }

        // URL Scan button
        binding.urlScan.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    UrlScanActivity::class.java
                )
            )
        }

        // Permissions Explanation button
        binding.permissionsButton.setOnClickListener {
            startActivity(Intent(this, PermissionsExplanationActivity::class.java))
        }

        binding.reportByUser.setOnClickListener {
            startActivity(Intent(this, UserReportActivity::class.java))
        }
    // Function to display a random tip
    fun displayRandomTip() {
        val tip = dbHelper.getRandomTip()
        binding.tipOfTheDay.text = tip

    }

    private fun displayStats() {


        val statsMap = dbHelper.getUserStats()

        val suspiciousLinkDetected = statsMap["suspicious_link_detected"] ?: 0
        val suspiciousSmsDetected = statsMap["suspicious_sms_detected"] ?: 0
        val suspiciousAppDetected = statsMap["suspicious_app_detected"] ?: 0
        val totalScans = suspiciousLinkDetected + suspiciousSmsDetected + suspiciousAppDetected

        binding.suspiciousLinksDetected.text = formatNumber(suspiciousLinkDetected)
        binding.suspiciousSmsDetected.text = formatNumber(suspiciousSmsDetected)
        binding.suspiciousAppDetected.text = formatNumber(suspiciousAppDetected)
        binding.totalScans.text = formatNumber(totalScans)


    }


    private fun updatePermissionsStatus() {

        //
        
    }

    }
