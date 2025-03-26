package nu.milad.motmaenbash.ui.screens

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.model.App
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.Green
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.viewmodels.AppScanViewModel
import nu.milad.motmaenbash.viewmodels.ScanState


@Composable
fun AppScanScreen(
    viewModel: AppScanViewModel = viewModel()
) {
    
    val currentScanState by viewModel.scanState.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()
    val detectedSuspiciousApps by viewModel.suspiciousApps.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()

    BackHandler(enabled = currentScanState == ScanState.IN_PROGRESS) {
        // Stop the scan when back is pressed during an active scan
        viewModel.stopScan()
    }

    AppBar(
        title = stringResource(id = R.string.app_scan_activity_title),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            item {
                Text(
                    text = stringResource(id = R.string.last_scan_time, lastScanTime),
                    style = typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Status message with color based on scan state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    when (currentScanState) {
                        ScanState.COMPLETED_SUCCESSFULLY -> Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Success",
                            tint = Green,
                            modifier = Modifier.size(24.dp)
                        )

                        ScanState.COMPLETED_WITH_ERRORS, ScanState.STOPPED -> Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "Error",
                            tint = Red,
                            modifier = Modifier.size(24.dp)
                        )

                        else -> {}
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = scanStatusMessage,
                        style = typography.headlineMedium,
                        color = when (currentScanState) {
                            ScanState.IN_PROGRESS -> ColorPrimary
                            ScanState.COMPLETED_SUCCESSFULLY -> Green
                            ScanState.COMPLETED_WITH_ERRORS, ScanState.STOPPED -> Red
                            else -> colorScheme.onSurface
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (currentScanState == ScanState.IN_PROGRESS) viewModel.stopScan() else viewModel.startScan()
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentScanState == ScanState.IN_PROGRESS) GreyDark else colorScheme.primaryContainer
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = if (currentScanState == ScanState.IN_PROGRESS) "توقف" else "اسکن برنامه‌ها")

                    if (currentScanState == ScanState.IN_PROGRESS) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show currently scanned apps
            if (currentScanState == ScanState.IN_PROGRESS) {
                item {
                    CurrentlyScannedAppsView(viewModel)
                }
            }

            // Show message if no suspicious apps
            if (currentScanState == ScanState.COMPLETED_SUCCESSFULLY && detectedSuspiciousApps.isEmpty()) {
                item {
                    Text(
                        text = "هیچ برنامه مشکوکی شناسایی نشد",
                        fontSize = 16.sp,
                        color = ColorPrimary, fontWeight = FontWeight.Bold
                    )
                }
            } else {
                items(detectedSuspiciousApps) { app ->
                    SuspiciousAppItem(app = app)
                }
            }
        }
    }
}


@Composable
fun SuspiciousAppItem(app: App) {
    val context = LocalContext.current
    // Track if this app has been uninstalled
    val (isUninstalled, setUninstalled) = remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appName = result.data?.getStringExtra("APP_NAME") ?: app.appName
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "برنامه '$appName' با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            // Mark as uninstalled when successful
            setUninstalled(true)
        } else {
            Toast.makeText(context, "حذف برنامه '$appName' لغو شد", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display app icon if available
        app.appIcon?.let { icon ->
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = app.appName,
            style = typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

        Button(
            onClick = {
                val intent = PackageUtils.uninstallApp(context, app.packageName)
                uninstallLauncher.launch(intent)
            },
            enabled = !isUninstalled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isUninstalled) Color.Gray else colorScheme.primary,
                disabledContainerColor = Color.Gray
            )
        ) {
            Text(text = if (isUninstalled) "حذف شد" else "حذف برنامه")
        }
    }
}

@Composable
fun CurrentlyScannedAppsView(viewModel: AppScanViewModel = viewModel()) {
    val currentlyScannedApps by viewModel.currentlyScannedApps.collectAsState()

    if (currentlyScannedApps.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "در حال بارگیری برنامه‌ها...")
        }
        return
    }

    // Take last 4 apps
    val displayedApps = currentlyScannedApps.takeLast(4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayedApps.forEach { app ->
            AnimatedAppIcon(app = app)
        }
    }
}

@Composable
fun AnimatedAppIcon(app: App) {
    val infiniteTransition = rememberInfiniteTransition(label = "App Icon Animation")

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
    )

    Box(
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = floatAnim.coerceIn(0.7f, 1f)
            }
    ) {
        app.appIcon?.let { icon ->
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * Safe ViewModel for previews that doesn't create MediaPlayer or other Android
 * system services that can cause preview rendering issues
 */
class PreviewAppScanViewModel(
    scanState: ScanState, scanStatusMessage: String,
    lastScanTime: String, suspiciousApps: List<App>,
    currentlyScannedApps: List<App> = emptyList()
) : AppScanViewModel(Application()) {

    private val _scanState = MutableStateFlow(scanState)
    override val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow(scanStatusMessage)
    override val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _lastScanTime = MutableStateFlow(lastScanTime)
    override val lastScanTime: StateFlow<String> = _lastScanTime.asStateFlow()

    private val _suspiciousApps = MutableStateFlow(suspiciousApps)
    override val suspiciousApps: StateFlow<List<App>> = _suspiciousApps.asStateFlow()

    private val _currentlyScannedApps = MutableStateFlow(currentlyScannedApps)
    override val currentlyScannedApps: StateFlow<List<App>> = _currentlyScannedApps.asStateFlow()

    init {
        _lastScanTime.value = lastScanTime
        _scanStatusMessage.value = scanStatusMessage
    }

    override fun startScan() {}
    override fun stopScan() {}
}


@Preview(showBackground = true)
@Composable
fun AppScanScreenPreview() {
    MotmaenBashTheme {
        val suspiciousApps = listOf(
            App(
                appName = "برنامه مخرب ۱",
                packageName = "com.fake.app1",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                sha1 = "dummy_sha1",
                apkSha1 = "dummy_apk_sha1",
                versionName = "1.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه مشکوک ۲",
                packageName = "com.fake.app2",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 2,
                sha1 = "dummy_sha2",
                apkSha1 = "dummy_apk_sha2",
                versionName = "2.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            )
        )

        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.COMPLETED_SUCCESSFULLY,
            scanStatusMessage = "اسکن کامل شد!",
            lastScanTime = "۸ ساعت پیش",
            suspiciousApps = suspiciousApps
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanInProgressPreview() {
    MotmaenBashTheme {
        val scanningApps = listOf(
            App(
                appName = "برنامه در حال اسکن",
                packageName = "com.example.app1",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                sha1 = "dummy_sha1",
                apkSha1 = "dummy_apk_sha1",
                versionName = "1.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه دیگر",
                packageName = "com.example.app2",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 2,
                sha1 = "dummy_sha2",
                apkSha1 = "dummy_apk_sha2",
                versionName = "2.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه سوم",
                packageName = "com.example.app3",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 3,
                sha1 = "dummy_sha3",
                apkSha1 = "dummy_apk_sha3",
                versionName = "3.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه چهارم",
                packageName = "com.example.app4",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 4,
                sha1 = "dummy_sha4",
                apkSha1 = "dummy_apk_sha4",
                versionName = "4.0",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            )
        )

        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.IN_PROGRESS,
            scanStatusMessage = "اسکن ۲/۵۰",
            lastScanTime = "۸ ساعت پیش",
            suspiciousApps = emptyList(),
            currentlyScannedApps = scanningApps
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanNotStartedPreview() {
    MotmaenBashTheme {
        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.NOT_STARTED,
            scanStatusMessage = "تعداد برنامه: ۱۲۳",
            lastScanTime = "هنوز اسکنی انجام نشده",
            suspiciousApps = emptyList()
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanCompletedWithErrorsPreview() {
    MotmaenBashTheme {
        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.COMPLETED_WITH_ERRORS,
            scanStatusMessage = "خطا در حین اسکن",
            lastScanTime = "۸ ساعت پیش",
            suspiciousApps = emptyList()
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanStoppedPreview() {
    MotmaenBashTheme {
        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.STOPPED,
            scanStatusMessage = "اسکن لغو شد!",
            lastScanTime = "۸ ساعت پیش",
            suspiciousApps = emptyList()
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanCompletedNoSuspiciousAppsPreview() {
    MotmaenBashTheme {
        val mockViewModel = PreviewAppScanViewModel(
            scanState = ScanState.COMPLETED_SUCCESSFULLY,
            scanStatusMessage = "اسکن کامل شد!",
            lastScanTime = "۸ ساعت پیش",
            suspiciousApps = emptyList()
        )

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}