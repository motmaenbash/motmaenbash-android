package nu.milad.motmaenbash.ui.screens

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.models.App
import nu.milad.motmaenbash.models.AppThreatType
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.ui.theme.YellowDark
import nu.milad.motmaenbash.utils.AlertUtils.getAlertContent
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.utils.PermissionAnalyzer
import nu.milad.motmaenbash.utils.parseBoldTags
import nu.milad.motmaenbash.utils.toSafeBitmap
import nu.milad.motmaenbash.viewmodels.AppScanViewModel
import nu.milad.motmaenbash.viewmodels.EmptyStateHelper.getEmptyStateBackgroundColor
import nu.milad.motmaenbash.viewmodels.EmptyStateHelper.getEmptyStateIcon
import nu.milad.motmaenbash.viewmodels.EmptyStateHelper.getEmptyStateMessage
import nu.milad.motmaenbash.viewmodels.EmptyStateHelper.getEmptyStateTextColor
import nu.milad.motmaenbash.viewmodels.ScanState
import nu.milad.motmaenbash.viewmodels.SectionConfig
import nu.milad.motmaenbash.viewmodels.createAppSections
import nu.milad.motmaenbash.viewmodels.getScanStatusConfig


@Composable
fun AppScanScreen(
    viewModel: AppScanViewModel = viewModel()
) {

    val currentScanState by viewModel.scanState.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()
    val detectedSuspiciousApps by viewModel.suspiciousApps.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()

    // Separate apps by threat type
    val sections = createAppSections(detectedSuspiciousApps)
    BackHandler(enabled = currentScanState == ScanState.IN_PROGRESS) {
        // Stop the scan when back is pressed during an active scan
        viewModel.stopScan()
    }

    AppBar(
        title = stringResource(id = R.string.app_scan_screen_title),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header section
            item {
                ScanHeader(
                    lastScanTime = lastScanTime,
                    currentScanState = currentScanState,
                    scanStatusMessage = scanStatusMessage,
                    onScanAction = {
                        if (currentScanState == ScanState.IN_PROGRESS) viewModel.stopScan()
                        else viewModel.startScan()
                    }
                )
            }

            // Currently scanned apps during scan
            if (currentScanState == ScanState.IN_PROGRESS) {
                item { CurrentlyScannedAppsView(viewModel) }
            }

            // App sections
            if (currentScanState != ScanState.NOT_STARTED) {
                sections.forEach { section ->
                    item {
                        AppSectionView(
                            section = section,
                            scanState = currentScanState,
                            viewModel = viewModel
                        )
                    }
                }
            }


        }
    }
}

@Composable
private fun ScanHeader(
    lastScanTime: String,
    currentScanState: ScanState,
    scanStatusMessage: String,
    onScanAction: () -> Unit
) {
    val statusConfig = getScanStatusConfig(currentScanState)
    val isScanning: Boolean = currentScanState == ScanState.IN_PROGRESS

    Text(
        text = stringResource(id = R.string.last_scan_time, lastScanTime),
        style = typography.headlineSmall,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        statusConfig.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusConfig.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = scanStatusMessage,
            style = typography.headlineMedium,
            color = statusConfig.color
        )
    }

    Spacer(modifier = Modifier.height(8.dp))



    Button(
        onClick = onScanAction,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) GreyDark else colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = if (isScanning) "توقف" else "اسکن برنامه‌ها")

        if (isScanning) {
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

@Composable
private fun AppSectionView(
    section: SectionConfig,
    scanState: ScanState,
    viewModel: AppScanViewModel
) {
    AppCard {
        Column(modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp)) {
            SectionHeader(
                title = section.title,
                subtitle = section.subtitle,
                icon = section.icon,
                color = section.color,
                count = section.apps.size
            )

            if (section.apps.isNotEmpty()) {
                section.apps.forEachIndexed { index, app ->
                    when (section.appItemType) {
                        AppThreatType.MALWARE -> MalwareAppItem(
                            app = app,
                            viewModel = viewModel
                        )

                        AppThreatType.RISKY_PERMISSIONS -> RiskyPermissionAppItem(
                            app = app,
                            viewModel = viewModel
                        )
                    }
                    if (index < section.apps.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 0.dp),
                            color = colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            } else {
                EmptyStateCard(
                    message = getEmptyStateMessage(scanState, section.appItemType),
                    icon = getEmptyStateIcon(scanState),
                    backgroundColor = getEmptyStateBackgroundColor(scanState),
                    textColor = getEmptyStateTextColor(scanState)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    color: Color,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        bottomEnd = 12.dp,
                        bottomStart = 12.dp
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = title,
                color = colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(24.dp)
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = NumberUtils.toPersianNumbers(count.toString()),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }

    if (subtitle != null) {
        Text(
            parseBoldTags(subtitle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            fontSize = 12.sp,
            color = colorScheme.onSurface
        )

        Divider(
            modifier = Modifier.padding(vertical = 0.dp),
            color = colorScheme.outline.copy(alpha = 0.2f)

        )
    }


}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = message,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = message,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }


}

@Composable
fun MalwareAppItem(
    app: App,
    viewModel: AppScanViewModel
) {
    val context = LocalContext.current
    // Track if this app has been uninstalled
    val uninstalledApps by viewModel.uninstalledApps.collectAsState()
    val isUninstalled = app.packageName in uninstalledApps

    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appName = result.data?.getStringExtra("APP_NAME") ?: app.appName
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "برنامه '$appName' با موفقیت حذف شد", Toast.LENGTH_SHORT)
                .show()
            // Mark as uninstalled when successful
            viewModel.markAppAsUninstalled(app.packageName)
        } else {
            Toast.makeText(context, "حذف برنامه '$appName' انجام نشد", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display app icon if available
        app.appIcon?.let { icon ->
            Image(
                painter = rememberAsyncImagePainter(model = icon),
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = app.appName,
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = app.packageName,
                color = GreyMiddle,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )

        }

        Spacer(modifier = Modifier.width(4.dp))

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            Button(
                onClick = {
                    val intent = PackageUtils.uninstallApp(app.packageName)
                    uninstallLauncher.launch(intent)
                },
                modifier = Modifier
                    .heightIn(min = 36.dp),


                enabled = !isUninstalled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUninstalled) Color.Gray else Red,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White

                ),

                contentPadding = PaddingValues(horizontal = 12.dp)

            ) {
                Text(
                    text = if (isUninstalled) "حذف شد" else "حذف برنامه",
                    fontSize = 12.sp,
                )
            }
        }

    }
}

@Composable
fun RiskyPermissionAppItem(
    app: App,
    viewModel: AppScanViewModel
) {
    val context = LocalContext.current
    val uninstalledApps by viewModel.uninstalledApps.collectAsState()
    val isUninstalled = app.packageName in uninstalledApps


    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val appName = result.data?.getStringExtra("APP_NAME") ?: app.appName
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "برنامه '$appName' با موفقیت حذف شد", Toast.LENGTH_SHORT)
                .show()
            viewModel.markAppAsUninstalled(app.packageName)
        } else {
            Toast.makeText(context, "حذف برنامه '$appName' لغو شد", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to show details in AlertHandlerActivity
    fun showAppDetails() {
        val riskyComboDescriptions =
            PermissionAnalyzer.getDetectedRiskyPermissionCombinations(app.permissions)
                .map { PermissionAnalyzer.getRiskyPermissionCombinationDescription(it) }

        val param3 = riskyComboDescriptions.joinToString("\n")

        val (alertTitle, alertSummary, alertContent) = getAlertContent(Alert.AlertType.APP_RISKY_INSTALL)

        val intent = Intent(context, AlertHandlerActivity::class.java).apply {
            putExtra(
                AlertHandlerActivity.EXTRA_ALERT, Alert(
                    type = Alert.AlertType.APP_RISKY_INSTALL,
                    level = Alert.AlertLevel.WARNING,
                    title = alertTitle,
                    summary = alertSummary,
                    content = alertContent,
                    param1 = app.packageName,
                    param2 = app.appName,
                    param3 = param3
                )
            )

            putExtra(AlertHandlerActivity.EXTRA_IS_INFO_ONLY, true)

        }
        context.startActivity(intent)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display app icon if available
        app.appIcon?.let { icon ->
            Image(
                painter = rememberAsyncImagePainter(model = icon),
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.appName,
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = app.packageName,
                color = GreyMiddle,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(horizontalAlignment = Alignment.End) {

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                Button(
                    onClick = {
                        val intent = PackageUtils.uninstallApp(app.packageName)
                        uninstallLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .heightIn(min = 36.dp),

                    enabled = !isUninstalled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUninstalled) Color.Gray else YellowDark,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    ),

                    contentPadding = PaddingValues(horizontal = 12.dp)

                ) {
                    Text(
                        text = if (isUninstalled) "حذف شد" else "حذف برنامه",
                        fontSize = 12.sp,
                    )
                }
            }

            if (!isUninstalled) {
                Spacer(modifier = Modifier.height(8.dp))

                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    Button(
                        onClick = { showAppDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyMiddle.copy(alpha = 0.1f),
                            contentColor = GreyMiddle
                        ),
                        modifier = Modifier
                            .heightIn(min = 28.dp),


                        contentPadding = PaddingValues(horizontal = 12.dp)

                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "اطلاعات بیشتر",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "جزییات",
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThreatBadge(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
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
            Text(text = "در حال دریافت لیست برنامه‌ها...", fontSize = 15.sp)
        }
        return
    }

    // Take last 4 apps
    val displayedApps = currentlyScannedApps.takeLast(4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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
            animation = tween(1_000, easing = FastOutSlowInEasing),
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
                bitmap = icon.toSafeBitmap(size = 96).asImageBitmap(),
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
    currentlyScannedApps: List<App> = emptyList(),

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
    override val currentlyScannedApps: StateFlow<List<App>> =
        _currentlyScannedApps.asStateFlow()

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
                packageName = "com.fake.malware1",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList(),
                threatType = AppThreatType.MALWARE
            ),
            App(
                appName = "برنامه بانکی مشکوک",
                packageName = "com.fake.bank",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 2,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "2.0",
                installSource = "market",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = listOf(
                    "android.permission.SEND_SMS",
                    "android.permission.READ_CONTACTS"
                ),
                threatType = AppThreatType.RISKY_PERMISSIONS
            ),
            App(
                appName = "ویروس خطرناک",
                packageName = "com.malicious.virus",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 3,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.5",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList(),
                threatType = AppThreatType.MALWARE
            ),
            App(
                appName = "اپلیکیشن دولتی",
                packageName = "ir.gov.app",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 4,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "3.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = listOf(
                    "android.permission.SEND_SMS",
                    "android.permission.READ_CONTACTS"
                ),
                threatType = AppThreatType.RISKY_PERMISSIONS
            )
        )

        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.COMPLETED_SUCCESSFULLY,
                scanStatusMessage = "اسکن کامل شد!",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = suspiciousApps,
            )
        }
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
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.0",
                installSource = "market",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه دیگر",
                packageName = "com.example.app2",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 2,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "2.0",
                installSource = "market",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه سوم",
                packageName = "com.example.app3",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 3,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "3.0",
                installSource = "market",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            ),
            App(
                appName = "برنامه چهارم",
                packageName = "com.example.app4",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 4,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "4.0",
                installSource = "market",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList()
            )
        )

        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.IN_PROGRESS,
                scanStatusMessage = "اسکن ۲/۵۰",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = emptyList(),
                currentlyScannedApps = scanningApps,
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanNotStartedPreview() {
    MotmaenBashTheme {
        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.NOT_STARTED,
                scanStatusMessage = "تعداد برنامه: ۱۲۳",
                lastScanTime = "هنوز اسکنی انجام نشده",
                suspiciousApps = emptyList(),
            )
        }
        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanCompletedWithErrorsPreview() {
    MotmaenBashTheme {
        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.COMPLETED_WITH_ERRORS,
                scanStatusMessage = "خطا در حین اسکن",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = emptyList(),
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanStoppedPreview() {
    MotmaenBashTheme {
        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.STOPPED,
                scanStatusMessage = "اسکن لغو شد!",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = emptyList(),
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanCompletedNoSuspiciousAppsPreview() {
    MotmaenBashTheme {
        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.COMPLETED_SUCCESSFULLY,
                scanStatusMessage = "اسکن کامل شد!",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = emptyList(),
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanOnlyMalwarePreview() {
    MotmaenBashTheme {
        val malwareApps = listOf(
            App(
                appName = "بدافزار شماره ۱",
                packageName = "com.fake.malware1",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList(),
                threatType = AppThreatType.MALWARE
            ),
            App(
                appName = "ویروس خطرناک",
                packageName = "com.malicious.virus",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 2,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "2.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = emptyList(),
                threatType = AppThreatType.MALWARE
            )
        )

        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.COMPLETED_SUCCESSFULLY,
                scanStatusMessage = "اسکن کامل شد!",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = malwareApps,
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScanOnlyRiskyPermissionsPreview() {
    MotmaenBashTheme {
        val riskyApps = listOf(
            App(
                appName = "اپلیکیشن دولتی",
                packageName = "ir.gov.app",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = listOf(
                    "android.permission.SEND_SMS",
                    "android.permission.READ_CONTACTS"
                ),
                threatType = AppThreatType.RISKY_PERMISSIONS
            ),
            App(
                appName = "اپلیکیشن دولتی",
                packageName = "ir.gov.app",
                appIcon = LocalContext.current.packageManager.defaultActivityIcon,
                versionCode = 1,
                apkHash = "dummy_hash",
                sighHash = "dummy_hash",
                versionName = "1.0",
                installSource = "unknown",
                firstInstallTime = 0L,
                lastUpdateTime = 0L,
                permissions = listOf(
                    "android.permission.SEND_SMS",
                    "android.permission.READ_CONTACTS"
                ),
                threatType = AppThreatType.RISKY_PERMISSIONS
            )
        )

        val mockViewModel = remember {
            PreviewAppScanViewModel(
                scanState = ScanState.COMPLETED_SUCCESSFULLY,
                scanStatusMessage = "اسکن کامل شد!",
                lastScanTime = "۸ ساعت پیش",
                suspiciousApps = riskyApps,
            )
        }

        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            AppScanScreen(viewModel = mockViewModel)
        }
    }
}