package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PackageUtils.getAppInfo
import nu.milad.motmaenbash.utils.ScanUtils
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

class AppScanViewModel(private val context: Application) : AndroidViewModel(context) {

    private val _lastScanTime = MutableStateFlow("هنوز اسکنی انجام نشده")
    val lastScanTime: StateFlow<String> = _lastScanTime.asStateFlow()

    private val _suspiciousApps = MutableStateFlow<List<App>>(emptyList())
    val suspiciousApps: StateFlow<List<App>> = _suspiciousApps.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _currentlyScannedApps = MutableStateFlow<List<App>>(emptyList())
    val currentlyScannedApps: StateFlow<List<App>> = _currentlyScannedApps.asStateFlow()

    private val scanScope = CoroutineScope(Job() + Dispatchers.Default)

    private val mediaPlayer = MediaPlayer.create(context, R.raw.ding1)

    private val _scanState = MutableStateFlow(ScanState.NOT_STARTED)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val scanUtils = ScanUtils(context)
    private var scanManuallyStoppped = false

    init {
        _lastScanTime.value = NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
    }

    fun startScan() {
        _scanState.value = ScanState.IN_PROGRESS
        _suspiciousApps.value = emptyList()
        _currentlyScannedApps.value = emptyList()
        _scanStatusMessage.value = "در حال دریافت لیست برنامه‌ها..."
        scanManuallyStoppped = false

        // Use an atomic counter for thread-safe progress tracking
        val processedAppsCounter = AtomicInteger(0)

        scanScope.launch {
            val nonSystemAppPackages = scanUtils.getNonSystemInstalledPackages()
            val totalApps = nonSystemAppPackages.size
            val detectSuspiciousAppsJobs = nonSystemAppPackages.mapIndexed { index, packageInfo ->
                async {

                    val app = getAppInfo(context, packageInfo.packageName)
                    val currentProcessed = processedAppsCounter.incrementAndGet()


                    launch(Dispatchers.Main) {

                        _scanStatusMessage.value =
                            NumberUtils.toPersianNumbers("$currentProcessed/$totalApps")

                        // Add app to currently scanned apps
                        val currentScannedApps = _currentlyScannedApps.value.toMutableList()
                        currentScannedApps.add(app)
                        _currentlyScannedApps.value = currentScannedApps
                    }

                    if (scanUtils.isAppSuspicious(app)) {
                        launch(Dispatchers.Main) {
                            val suspiciousList = _suspiciousApps.value.toMutableList()
                            suspiciousList.add(app)
                            _suspiciousApps.value = suspiciousList
                            mediaPlayer.start()
                        }
                        app
                    } else null
                }
            }

            try {
                val suspiciousApps = detectSuspiciousAppsJobs.awaitAll().filterNotNull()

                if (!scanManuallyStoppped) {

                    if (suspiciousApps.isNotEmpty()) {
                        _scanState.value = ScanState.COMPLETED_SUCCESSFULLY
                    } else {
                        _scanState.value = ScanState.COMPLETED_SUCCESSFULLY
                    }

                    val currentTime = System.currentTimeMillis()
                    scanUtils.setLastScanTime(currentTime)
                    _lastScanTime.value =
                        NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
                }
            } catch (e: Exception) {

                if (e is CancellationException && scanManuallyStoppped) {
                    return@launch
                }

                // Only update state if scan wasn't manually stopped
                if (!scanManuallyStoppped) {
                    _scanState.value = ScanState.COMPLETED_WITH_ERRORS
                    _scanStatusMessage.value = "خطا در حین اسکن"
                }
            }
        }
    }

    fun stopScan() {
        scanManuallyStoppped = true

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