package nu.milad.motmaenbash.viewmodels

import android.app.Application
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
import kotlinx.coroutines.withContext
import nu.milad.motmaenbash.models.App
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PackageUtils.getAppInfo
import nu.milad.motmaenbash.utils.ScanUtils
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException

open class AppScanViewModel(private val context: Application) : AndroidViewModel(context) {

    private val _lastScanTime = MutableStateFlow("هنوز اسکنی انجام نشده")
    open val lastScanTime: StateFlow<String> = _lastScanTime.asStateFlow()

    private val _suspiciousApps = MutableStateFlow<List<App>>(emptyList())
    open val suspiciousApps: StateFlow<List<App>> = _suspiciousApps.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    open val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _currentlyScannedApps = MutableStateFlow<List<App>>(emptyList())
    open val currentlyScannedApps: StateFlow<List<App>> = _currentlyScannedApps.asStateFlow()

    private val scanScope = CoroutineScope(Job() + Dispatchers.Default)

    // Use lazy initialization to prevent preview issues
    private val audioHelper by lazy {
        AudioHelper(context)
    }

    private val _scanState = MutableStateFlow(ScanState.NOT_STARTED)
    open val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    // Use lazy initialization for the ScanUtils
    private val scanUtils by lazy { ScanUtils(context) }
    private var scanManuallyStopped = false

    init {
        scanScope.launch {
            try {
                val nonSystemAppsSize = withContext(Dispatchers.IO) {
                    scanUtils.getNonSystemInstalledPackages().size
                }
                _lastScanTime.value = NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
                _scanStatusMessage.value =
                    "تعداد برنامه: ${NumberUtils.toPersianNumbers(nonSystemAppsSize.toString())}"
            } catch (e: Exception) {
                _lastScanTime.value = "هنوز اسکنی انجام نشده"
            }
        }
    }

    open fun startScan() {


        _scanState.value = ScanState.IN_PROGRESS
        _suspiciousApps.value = emptyList()
        _currentlyScannedApps.value = emptyList()
        scanManuallyStopped = false
        // Use an atomic counter for thread-safe progress tracking
        val processedAppsCounter = AtomicInteger(0)

        scanScope.launch {
            try {
                val nonSystemAppPackages = scanUtils.getNonSystemInstalledPackages()
                val totalApps = nonSystemAppPackages.size
                val detectSuspiciousAppsJobs =
                    nonSystemAppPackages.mapIndexed { index, packageInfo ->
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

                                    audioHelper.vibrateDevice(context)
                                    audioHelper.playDefaultSound()
                                }
                                app
                            } else null
                        }
                    }

                try {
                    detectSuspiciousAppsJobs.awaitAll().filterNotNull()

                    if (!scanManuallyStopped) {
                        _scanState.value = ScanState.COMPLETED_SUCCESSFULLY
                        val currentTime = System.currentTimeMillis()
                        scanUtils.setLastScanTime(currentTime)
                        _lastScanTime.value =
                            NumberUtils.toPersianNumbers(scanUtils.getLastScanTimeAgo())
                    }
                } catch (e: Exception) {
                    if (e is CancellationException && scanManuallyStopped) {
                        return@launch
                    }

                    // Only update state if scan wasn't manually stopped
                    if (!scanManuallyStopped) {
                        _scanState.value = ScanState.COMPLETED_WITH_ERRORS
                        _scanStatusMessage.value = "خطا در حین اسکن"
                    }
                }
            } catch (e: Exception) {
                // Handle potential exceptions from scanUtils in preview context
                _scanState.value = ScanState.COMPLETED_WITH_ERRORS
                _scanStatusMessage.value = "خطا در حین اسکن"
            }
        }
    }

    open fun stopScan() {
        scanManuallyStopped = true

        scanScope.coroutineContext.cancelChildren()
        _scanStatusMessage.value = "اسکن لغو شد!"
        _scanState.value = ScanState.STOPPED
    }

    override fun onCleared() {
        super.onCleared()
        scanScope.cancel()
        audioHelper.release()
    }
}

enum class ScanState {
    NOT_STARTED, IN_PROGRESS, COMPLETED_SUCCESSFULLY, COMPLETED_WITH_ERRORS, STOPPED
}