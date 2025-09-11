package nu.milad.motmaenbash.viewmodels

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import nu.milad.motmaenbash.models.AppThreatType
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.Green
import nu.milad.motmaenbash.ui.theme.Grey
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.ui.theme.YellowDark
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.utils.PackageUtils.getAppInfo
import nu.milad.motmaenbash.utils.PermissionAnalyzer
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

    // Use lazy initialization for the ScanUtils and DatabaseHelper
    private val scanUtils by lazy { ScanUtils(context) }
    private val databaseHelper by lazy { DatabaseHelper(context) }
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
            } catch (_: Exception) {
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
                            if (app != null) {
                                val currentProcessed = processedAppsCounter.incrementAndGet()

                                launch(Dispatchers.Main) {
                                    _scanStatusMessage.value =
                                        NumberUtils.toPersianNumbers("$currentProcessed/$totalApps")

                                    // Add app to currently scanned apps
                                    val currentScannedApps =
                                        _currentlyScannedApps.value.toMutableList()
                                    currentScannedApps.add(app)
                                    _currentlyScannedApps.value = currentScannedApps
                                }

                                val threatType = when {
                                    databaseHelper.isAppFlagged(
                                        app.packageName,
                                        app.apkHash,
                                        app.sighHash
                                    ) ->
                                        AppThreatType.MALWARE

                                    !PackageUtils.isFromTrustedSource(context, app.installSource) &&
                                            !databaseHelper.isTrustedSideloadApp(
                                                app.packageName,
                                                app.sighHash
                                            ) &&
                                            PermissionAnalyzer.hasRiskyPermissionCombination(app.permissions) ->
                                        AppThreatType.RISKY_PERMISSIONS

                                    else -> null
                                }

                                val appWithThreat = app.copy(threatType = threatType)

                                if (threatType != null) {
                                    launch(Dispatchers.Main) {
                                        val suspiciousList = _suspiciousApps.value.toMutableList()
                                        suspiciousList.add(appWithThreat)
                                        _suspiciousApps.value = suspiciousList

                                        audioHelper.vibrateDevice(context)
                                        audioHelper.playAlertSound()
                                    }
                                    appWithThreat
                                } else null
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
                        _scanStatusMessage.value =
                            "${NumberUtils.toPersianNumbers(totalApps.toString())} برنامه بررسی شد."
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
            } catch (_: Exception) {
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

@Composable
fun createAppSections(suspiciousApps: List<App>): List<SectionConfig> {
    val malwareApps = suspiciousApps.filter { it.threatType == AppThreatType.MALWARE }
    val riskyPermissionApps =
        suspiciousApps.filter { it.threatType == AppThreatType.RISKY_PERMISSIONS }

    return listOf(
        SectionConfig(
            title = "بدافزارهای شناسایی شده",
            subtitle = "این برنامه‌ها مشکوک هستند و باید سریع حذف شوند.".takeIf { malwareApps.isNotEmpty() },
            icon = Icons.Outlined.GppBad,
            color = if (malwareApps.isNotEmpty()) Red else GreyMiddle.copy(alpha = 0.5f),
            apps = malwareApps,
            appItemType = AppThreatType.MALWARE
        ),
        SectionConfig(
            title = "برنامه‌های نیاز به بررسی بیشتر",
            subtitle = ("این برنامه‌ها به دلایل زیر نیاز به بررسی بیشتر و دقیق‌تر دارند:\n" +
                    "• <b>نصب از منابعی غیر از مارکت‌های معتبر اندروید</b>\n" +
                    "• <b>ترکیب دسترسی‌های حساس</b>").takeIf { riskyPermissionApps.isNotEmpty() },
            icon = Icons.Outlined.AdminPanelSettings,
            color = if (riskyPermissionApps.isNotEmpty()) YellowDark else GreyMiddle.copy(alpha = 0.5f),
            apps = riskyPermissionApps,
            appItemType = AppThreatType.RISKY_PERMISSIONS
        )
    )
}

@Composable
fun getScanStatusConfig(scanState: ScanState): ScanStatusConfig {
    return when (scanState) {
        ScanState.COMPLETED_SUCCESSFULLY -> ScanStatusConfig(
            Icons.Outlined.CheckCircle,
            Green,
            ""
        )

        ScanState.COMPLETED_WITH_ERRORS, ScanState.STOPPED -> ScanStatusConfig(
            Icons.Outlined.ErrorOutline,
            Red,
            ""
        )

        ScanState.IN_PROGRESS -> ScanStatusConfig(
            null,
            ColorPrimary,
            ""
        )

        else -> ScanStatusConfig(
            null,
            colorScheme.onSurface,
            ""
        )
    }
}

// Data classes for UI state
data class ScanStatusConfig(
    val icon: ImageVector?,
    val color: Color,
    val message: String
)

data class SectionConfig(
    val title: String,
    val subtitle: String?,
    val icon: ImageVector,
    val color: Color,
    val apps: List<App>,
    val appItemType: AppThreatType
)

enum class ScanState {
    NOT_STARTED, IN_PROGRESS, COMPLETED_SUCCESSFULLY, COMPLETED_WITH_ERRORS, STOPPED
}

object EmptyStateHelper {
    fun getEmptyStateMessage(scanState: ScanState, appItemType: AppThreatType): String {
        return when {
            scanState == ScanState.COMPLETED_SUCCESSFULLY -> {
                when (appItemType) {
                    AppThreatType.MALWARE -> "بدافزاری شناسایی نشد"
                    AppThreatType.RISKY_PERMISSIONS -> "برنامه‌ای با دسترسی‌های مشکوک یافت نشد"
                }
            }

            scanState == ScanState.STOPPED -> "اسکن لغو شد"
            scanState == ScanState.COMPLETED_WITH_ERRORS -> "خطا در حین اسکن"
            else -> "در حال بررسی برنامه‌ها"
        }
    }

    fun getEmptyStateIcon(scanState: ScanState): ImageVector {
        return when (scanState) {
            ScanState.COMPLETED_SUCCESSFULLY -> Icons.Outlined.Security
            ScanState.STOPPED, ScanState.COMPLETED_WITH_ERRORS -> Icons.Outlined.ErrorOutline
            else -> Icons.Outlined.TrackChanges
        }
    }

    fun getEmptyStateBackgroundColor(scanState: ScanState): Color {
        return when (scanState) {
            ScanState.COMPLETED_WITH_ERRORS -> Red.copy(alpha = 0.1f)
            else -> Grey.copy(alpha = 0.1f)
        }
    }

    fun getEmptyStateTextColor(scanState: ScanState): Color {
        return when (scanState) {
            ScanState.COMPLETED_WITH_ERRORS -> Red
            else -> Grey
        }
    }
}