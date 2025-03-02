package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PackageUtils.getAppInfo
import nu.milad.motmaenbash.utils.ScanUtils

class AppScanViewModel(private val context: Application) : AndroidViewModel(context) {

    private val _lastScanTime = MutableStateFlow("هنوز اسکنی انجام نشده")
    val lastScanTime: StateFlow<String> = _lastScanTime.asStateFlow()

    private val _suspiciousApps = MutableStateFlow<List<App>>(emptyList())
    val suspiciousApps: StateFlow<List<App>> = _suspiciousApps.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val scanScope = CoroutineScope(Job() + Dispatchers.IO)

    private val mediaPlayer = MediaPlayer.create(context, R.raw.dingding)

    private val _scanState = MutableStateFlow(ScanState.NOT_STARTED)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val scanUtils = ScanUtils(context)

    init {
        _lastScanTime.value = NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
    }


    fun startScan() {
        _scanState.value = ScanState.IN_PROGRESS

        scanScope.launch {
            _suspiciousApps.value = emptyList()
            _scanStatusMessage.value = "در حال دریافت لیست برنامه‌ها..."

            val detectedSuspiciousApps = mutableListOf<App>()
            val nonSystemAppPackages = scanUtils.getNonSystemInstalledPackages()
            val totalApps = nonSystemAppPackages.size
            try {
                for ((index, packageInfo) in nonSystemAppPackages.withIndex()) {
                    if (!isActive) {
                        break
                    }

                    val currentProgress = "${index + 1}/$totalApps"
                    _scanStatusMessage.value = NumberUtils.toPersianNumbers(currentProgress)

                    val app = getAppInfo(context, packageInfo.packageName)

                    if (scanUtils.isAppSuspicious(app)) {
                        launch(Dispatchers.Main) {
                            detectedSuspiciousApps.add(app)
                            _suspiciousApps.value = detectedSuspiciousApps.toList()
                            mediaPlayer.start()
                        }
                    }
                }

                if (isActive) {
                    _scanState.value = ScanState.COMPLETED_SUCCESSFULLY
                    val currentTime = System.currentTimeMillis()
                    scanUtils.setLastScanTime(currentTime)
                    _lastScanTime.value =
                        NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
                }

            } catch (e: Exception) {
                _scanState.value = ScanState.COMPLETED_WITH_ERRORS
                _scanStatusMessage.value = "خطا در حین اسکن"
            }
        }
    }

    fun stopScan() {
        scanScope.coroutineContext.cancelChildren()
        _scanStatusMessage.value = "اسکن لغو شد!"
        _scanState.value = ScanState.STOPPED
    }

    override fun onCleared() {
        super.onCleared()
        scanScope.cancel()
        mediaPlayer.release()
    }
}

enum class ScanState {
    NOT_STARTED, IN_PROGRESS, COMPLETED_SUCCESSFULLY, COMPLETED_WITH_ERRORS, STOPPED
}
