package nu.milad.motmaenbash.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.GppGood
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.models.Link
import nu.milad.motmaenbash.models.Stats
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AccessibilityPermissionDialog
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.ChangelogDialog
import nu.milad.motmaenbash.ui.components.CriticalPermissionsInfoDialog
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.components.GuardsInfoDialog
import nu.milad.motmaenbash.ui.components.NotificationPermissionDialog
import nu.milad.motmaenbash.ui.components.SmsPermissionDialog
import nu.milad.motmaenbash.ui.components.UpdateDialog
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PermissionManager
import nu.milad.motmaenbash.utils.ServiceUtils
import nu.milad.motmaenbash.utils.UpdateManager.UpdateState
import nu.milad.motmaenbash.utils.WebUtils
import nu.milad.motmaenbash.viewmodels.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val permissionManager = remember { PermissionManager(context) }


    val scrollState = rememberScrollState()
    val tipOfTheDay by viewModel.tipOfTheDay.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    //Stats
    val stats by viewModel.stats.collectAsState()

    val updateState by viewModel.updateState.collectAsState()
    val updateDialogState by viewModel.updateDialogState.collectAsState()

    // Observe link data
    val linkData by viewModel.linkData.collectAsState()


    var showSmsPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Permission States
    val smsPermissionState = rememberPermissionState(
        Manifest.permission.RECEIVE_SMS
    ) { isGranted ->
        viewModel.updatePermissionStatus(
            PermissionType.SMS, isGranted
        )
    }


    // Notification Permission
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { isGranted ->
            viewModel.updatePermissionStatus(
                PermissionType.NOTIFICATIONS, isGranted
            )
        }
    } else {
        null
    }


    // Observe permission statuses
    val smsPermissionStatus by viewModel.smsPermissionStatus.collectAsState()
    val notificationPermissionStatus by viewModel.notificationPermissionStatus.collectAsState()


    val accessibilitySettingStatus by viewModel.accessibilitySettingStatus.collectAsState()
    val overlayPermissionStatus by viewModel.overlayPermissionStatus.collectAsState()

    var showAccessibilityGuide by remember { mutableStateOf(false) }


    var showCriticalPermissionsInfoDialog by remember { mutableStateOf(false) }


    val isCriticalPermissionsMissing =
        !notificationPermissionStatus || !overlayPermissionStatus


    val coroutineScope = rememberCoroutineScope()


    // ActivityResultLaunchers for permissions
    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Add delay to allow system to update
        viewModel.viewModelScope.launch {
            delay(300)
            viewModel.checkPermissionStatus(PermissionType.ACCESSIBILITY)
        }
    }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // When user returns from settings, check if permission is now granted
        viewModel.checkPermissionStatus(PermissionType.OVERLAY)
    }


    // SMS Settings Launcher
    val smsSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissionStatus(PermissionType.SMS)
    }

    // Notification Settings Launcher
    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            viewModel.checkPermissionStatus(PermissionType.NOTIFICATIONS)
        }
    }

    //Changhelog
    val showChangelogDialog by viewModel.showChangelogDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkInitialPermissions()
    }


    LaunchedEffect(overlayPermissionStatus) {
        if (overlayPermissionStatus) {
            // Permission was granted, update notification
            permissionManager.updateNotificationAfterPermissionChange(context)
        }
    }

    LaunchedEffect(accessibilitySettingStatus) {
        if (accessibilitySettingStatus) {
            // Permission was granted, update notification
            permissionManager.updateNotificationAfterPermissionChange(context)
        }
    }

    LaunchedEffect(smsPermissionState.status.isGranted) {

        when {
            smsPermissionState.status.isGranted -> {
                showSmsPermissionDeniedDialog = false

                // Update notification to reflect the new permission
                permissionManager.updateNotificationAfterPermissionChange(context)
            }

            !smsPermissionState.status.isGranted &&
                    !smsPermissionState.status.shouldShowRationale &&
                    viewModel.hasRequestedSmsPermission.value -> {
                showSmsPermissionDeniedDialog = true
            }
        }

    }


    LaunchedEffect(notificationPermissionState?.status?.isGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            notificationPermissionState != null
        ) {

            when {
                notificationPermissionState.status.isGranted -> {
                    showNotificationPermissionDeniedDialog = false
                    // Start the monitoring service when notification permission is granted
                    ServiceUtils().startMonitoringService(context)

                }

                !notificationPermissionState.status.isGranted &&
                        !notificationPermissionState.status.shouldShowRationale &&
                        viewModel.hasRequestedNotificationPermission.value -> {
                    showNotificationPermissionDeniedDialog = true
                }
            }
        }
    }


    if (showChangelogDialog) {
        ChangelogDialog(
            onDismiss = {
                viewModel._showChangelogDialog.value = false
                coroutineScope.launch {
                    viewModel.setLastShownVersion()
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)

    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Settings icon
            IconButton(
                onClick = { navController.navigate(NavRoutes.SETTINGS_SCREEN) },
                modifier = Modifier
                    .padding(bottom = 0.dp, end = 4.dp)
                    .size(32.dp)
                    .align(Alignment.End)
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "تنظیمات",
                    tint = GreyMiddle,
                    modifier = Modifier.size(28.dp)
                )
            }

            AppName(navController)


            Spacer(Modifier.size(16.dp))

            TipOfDaySection(tip = tipOfTheDay, isRefreshing = isRefreshing, onRefreshClick = {
                viewModel.refreshTipOfTheDay()
            })


            if (isCriticalPermissionsMissing) {
                CriticalPermissionsSection(
                    notificationPermissionStatus = notificationPermissionStatus,
                    overlayPermissionStatus = overlayPermissionStatus,
                    onNotificationPermissionClick = {
                        if (notificationPermissionState?.status?.isGranted == false && notificationPermissionState.status.shouldShowRationale) {
                            showNotificationPermissionDeniedDialog = true
                        } else {
                            viewModel.setHasRequestedNotificationPermission(true)
                            notificationPermissionState?.launchPermissionRequest()
                        }

                    },
                    onOverlayPermissionClick = {
                        permissionManager.requestOverlayPermission(context, overlaySettingsLauncher)
                        coroutineScope.launch {
                            permissionManager.showOverlayPermissionTutorial()
                        }
                    },
                    onMoreInfoClick = {
                        showCriticalPermissionsInfoDialog = true
                    }
                )
            }


            ProtectionStatus(
                smsPermissionStatus = smsPermissionStatus,
                onSmsPermissionClick = {
                    if (!smsPermissionState.status.isGranted && smsPermissionState.status.shouldShowRationale) {
                        showSmsPermissionDeniedDialog = true
                    } else {
                        viewModel.setHasRequestedSmsPermission(true)
                        smsPermissionState.launchPermissionRequest()
                    }
                },
//                notificationPermissionStatus = notificationPermissionStatus,
                accessibilitySettingStatus = accessibilitySettingStatus,
                onAccessibilitySettingClick = {
                    if (!accessibilitySettingStatus) {
                        showAccessibilityGuide = true
                    }
                },
//                overlayPermissionStatus = overlayPermissionStatus,
//                onOverlayPermissionClick = {
//                    permissionManager.requestOverlayPermission(context, overlaySettingsLauncher)
//                    viewModel.checkOverlayPermission()
//                    coroutineScope.launch {
//                        permissionManager.showOverlayPermissionTutorial()
//                    }
//                }
                isCriticalPermissionsMissing = isCriticalPermissionsMissing
            )


            Stats(stats = stats)

            Tools(
                onAppScanClick = { navController.navigate(NavRoutes.APP_SCAN_SCREEN) },
                onUrlScanClick = { navController.navigate(NavRoutes.URL_SCAN_SCREEN) },
                onReportByUserClick = { navController.navigate(NavRoutes.USER_REPORT_SCREEN) })


            Spacer(Modifier.size(16.dp))

            UpdateSection(updateState = updateState, onUpdateDatabaseClick = {
                viewModel.updateDatabase()
            })

            Spacer(Modifier.size(16.dp))

            AboutAndFaqs(navController)

            linkData?.let {
                LinkCard(linkData = it)
            }

            // TODO: Remove beta notice after final release
            BetaNotice()

            Divider()

            AppVersion(navController)



            if (showAccessibilityGuide) {
                AccessibilityPermissionDialog(
                    onConfirm = {
                        showAccessibilityGuide = false
                        permissionManager.launchAccessibilitySettings(accessibilitySettingsLauncher)
                        Toast.makeText(
                            context,
                            "لطفا سرویس دسترسی‌پذیری (Accessibility) برنامه مطمئن باش را فعال کنید.",
                            Toast.LENGTH_LONG
                        ).show()


                    },
                    onDismiss = { showAccessibilityGuide = false }
                )
            }

            // SMS Permission Denied Dialog
            if (showSmsPermissionDeniedDialog) {
                SmsPermissionDialog(
                    onConfirm = {
                        // Open app settings directly
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        smsSettingsLauncher.launch(intent)
                        showSmsPermissionDeniedDialog = false
                    },
                    onDismiss = { showSmsPermissionDeniedDialog = false },
                )
            }

            // Notification Permission Denied Dialog (For API 33+)
            if (showNotificationPermissionDeniedDialog) {
                NotificationPermissionDialog(
                    onConfirm = {
                        // Open app notification settings
                        permissionManager.requestNotificationPermission(
                            context,
                            notificationSettingsLauncher
                        )
                        showNotificationPermissionDeniedDialog = false
                    },
                    onDismiss = { showNotificationPermissionDeniedDialog = false }

                )
            }

            if (showCriticalPermissionsInfoDialog) {
                CriticalPermissionsInfoDialog(
                    onDismiss = { showCriticalPermissionsInfoDialog = false }
                )
            }


            updateDialogState?.let { updateDialogState ->

                UpdateDialog(
                    updateDialogState = updateDialogState,
                    onDismiss = { viewModel.dismissUpdateDialog() }
                )

            }

        }
    }
}


@Composable
fun AppName(navController: NavController) {
    Column(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) {
            navController.navigate(NavRoutes.ABOUT_SCREEN)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            color = colorScheme.primary,
            text = stringResource(id = R.string.app_name_fa),
            style = typography.headlineLarge,

            )
        Text(
            text = stringResource(id = R.string.app_slogan),
            color = colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SectionTitle(text: String, textColor: Color = colorScheme.primary) {
    Text(
        text = text,
        color = textColor,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, start = 12.dp, bottom = 4.dp)

    )
}


@Composable
fun TipOfDaySection(
    tip: String?, isRefreshing: Boolean, onRefreshClick: () -> Unit
) {
    // Animation for refresh button rotation
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "Refresh Animation"
    )


    AppCard {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نکته روز",
                color = colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)

            )

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = if (isRefreshing) rotation else 0f
                    },
                enabled = !isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "نمایش یک نکته دیگر",
                    modifier = Modifier.fillMaxSize(),
                    tint = if (isRefreshing)
                        colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        colorScheme.primary
                )
            }
        }
        Divider()

        Text(
            text = tip ?: "در حال دریافت...",
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            fontSize = 13.sp,

            )

    }


}

@Composable
fun CriticalPermissionsSection(
    notificationPermissionStatus: Boolean,
    overlayPermissionStatus: Boolean,
    onNotificationPermissionClick: () -> Unit,
    onOverlayPermissionClick: () -> Unit,
    onMoreInfoClick: () -> Unit
) {

    val missingPermissions = mutableListOf<String>()
    if (!overlayPermissionStatus) {
        missingPermissions.add(stringResource(id = R.string.permission_overlay))
    }
    if (!notificationPermissionStatus) {
        missingPermissions.add(stringResource(id = R.string.permission_notification))
    }

    if (missingPermissions.isNotEmpty()) {
        Column {
            SectionTitle(
                text = "دسترسی‌های مورد نیاز"
            )

            AppCard(
                padding = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = colorScheme.onError
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    PermissionWarningBanner(
                        warningTitle = "برای شروع، برنامه به این دسترسی‌ها نیاز دارد:",
                        missingPermissions = missingPermissions,
                        warningIcon = Icons.Outlined.Info
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp, end = 16.dp, bottom = 8.dp
                    )
                ) {

                    // Notification Permission Status
                    ProtectionRow(
                        title = stringResource(id = R.string.permission_notification),
                        isGranted = notificationPermissionStatus,
                        onActivateClick = onNotificationPermissionClick,
                        enabledIcon = Icons.Outlined.CheckCircle,
                        disabledIcon = Icons.Outlined.ErrorOutline
                    )
                    Divider()


                    // Overlay Permission Status
                    ProtectionRow(
                        title = stringResource(id = R.string.permission_overlay),
                        isGranted = overlayPermissionStatus,
                        onActivateClick = onOverlayPermissionClick,
                        enabledIcon = Icons.Outlined.CheckCircle,
                        disabledIcon = Icons.Outlined.ErrorOutline
                    )
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {

                        MoreInfoButton(
                            onClick = onMoreInfoClick
                        )
                    }


                }
            }
        }
    }
}


@Composable
fun PermissionWarningBanner(
    missingPermissions: List<String>,
    warningTitle: String,
    warningIcon: ImageVector = Icons.Outlined.PrivacyTip
) {

    if (missingPermissions.isNotEmpty()) {

        AppCard(
            containerColor = colorScheme.errorContainer,
            cornerRadius = 12.dp,
            padding = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = warningIcon,
                        contentDescription = "هشدار",
                        tint = colorScheme.onError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = warningTitle,
                        color = colorScheme.onError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                missingPermissions.forEach { permission ->
                    Text(
                        text = "• $permission",
                        color = colorScheme.onError,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    }
}

@Composable
fun ProtectionStatus(
    smsPermissionStatus: Boolean,
    onSmsPermissionClick: () -> Unit,
    accessibilitySettingStatus: Boolean,
    onAccessibilitySettingClick: () -> Unit,
    isCriticalPermissionsMissing: Boolean = false // Add this parameter
) {
    // state for showing the protector info dialog
    var showProtectorInfoDialog by remember { mutableStateOf(false) }

    val missingPermissions = mutableListOf<String>()
    if (!smsPermissionStatus) missingPermissions.add(stringResource(id = R.string.permission_receive_sms))
    if (!accessibilitySettingStatus) missingPermissions.add(stringResource(id = R.string.permission_accessibility))

    // Define permissions as a list of triples: (label, isGranted, activateAction)
    val permissions = listOf(
        Triple(
            stringResource(id = R.string.guard_sms),
            smsPermissionStatus && !isCriticalPermissionsMissing,
            onSmsPermissionClick
        ),
        Triple(
            stringResource(id = R.string.guard_web),
            accessibilitySettingStatus && !isCriticalPermissionsMissing,
            onAccessibilitySettingClick
        ),
        Triple(
            stringResource(id = R.string.guard_gateway),
            accessibilitySettingStatus && !isCriticalPermissionsMissing,
            onAccessibilitySettingClick
        ),
        Triple(
            stringResource(id = R.string.guard_app),
            !isCriticalPermissionsMissing
        ) {} // This one is always active with no action
    )


    Column {
        SectionTitle(stringResource(id = R.string.guards_status))

        Box(modifier = Modifier.fillMaxWidth()) {
            // The original card content
            AppCard(
                padding = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                border = if (missingPermissions.isNotEmpty() && !isCriticalPermissionsMissing) {
                    BorderStroke(
                        width = 1.dp,
                        color = colorScheme.errorContainer
                    )
                } else {
                    null
                }
            ) {
                if (missingPermissions.isNotEmpty() && !isCriticalPermissionsMissing) {
                    // Permission Warning
                    Column(modifier = Modifier.padding(8.dp)) {
                        PermissionWarningBanner(
                            warningTitle = "برای حفاظت کامل، دسترسی‌های زیر را فعال کنید:",
                            missingPermissions = missingPermissions,
                            warningIcon = Icons.Outlined.PrivacyTip
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp, end = 16.dp, bottom = 8.dp
                    )
                ) {
                    permissions.forEach { (label, isGranted, onActivateClick) ->
                        ProtectionRow(
                            title = label,
                            isGranted = isGranted,
                            onActivateClick = onActivateClick,
                        )

                        Divider()
                    }




                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        MoreInfoButton(
                            onClick = { showProtectorInfoDialog = true },
                        )
                    }


                }
            }

            // Overlay that appears when critical permissions are missing
            if (isCriticalPermissionsMissing) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(12.dp)
                        .background(colorScheme.surface.copy(alpha = 0.9f))
                        .clickable(enabled = false) { /* Disabled click */ },
                    contentAlignment = Alignment.Center,


                    ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.RemoveModerator,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            text = "برای شروع، ابتدا دسترسی‌های مورد نیاز بالا را فعال کنید.",
                            color = colorScheme.primary,
                            fontSize = 16.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Show the protector info dialog when the icon is clicked
    if (showProtectorInfoDialog) {
        GuardsInfoDialog(
            onDismiss = { showProtectorInfoDialog = false }
        )
    }
}

@Composable
fun ProtectionRow(
    title: String,
    enabledIcon: ImageVector = Icons.Outlined.VerifiedUser,
    disabledIcon: ImageVector = Icons.Outlined.RemoveModerator,
    isGranted: Boolean,
    onActivateClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = if (isGranted) enabledIcon else disabledIcon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 2.dp),
            tint = if (isGranted) colorScheme.primary else colorScheme.onError

        )
        Text(

            text = title, // + ": غیرفعال".takeIf { !isGranted }.orEmpty(),
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) colorScheme.primary else colorScheme.onError

        )


        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .widthIn(min = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {


            if (!isGranted) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    Button(
                        onClick = onActivateClick,
                        modifier = Modifier
                            .heightIn(min = 36.dp),

                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp)

                    ) {
                        Text(
                            text = "فعال‌سازی",
                            fontSize = 12.sp,
                        )
                    }
                }


            } else {
                Text(
                    text = "فعال",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = colorScheme.primary,

                    )
            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreInfoButton(
    title: String = "اطلاعات بیشتر",
    onClick: () -> Unit = {},
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .heightIn(min = 32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),

            ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = GreyMiddle
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = title,
                color = GreyMiddle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }


}


@Composable
fun Stats(stats: Stats) {

    val userStats = listOf(
        "هشدار لینک مشکوک" to NumberUtils.formatNumber(stats.suspiciousLinksDetected),
        "هشدار پیامک مشکوک" to NumberUtils.formatNumber(stats.suspiciousSmsDetected),
        "هشدار برنامه مشکوک" to NumberUtils.formatNumber(stats.suspiciousAppDetected),
        "تعداد همه هشدارها" to NumberUtils.formatNumber(stats.suspiciousLinksDetected + stats.suspiciousSmsDetected + stats.suspiciousAppDetected),
        "تشخیص اصالت درگاه" to NumberUtils.formatNumber(stats.verifiedGatewayDetected),
    )

    Column {
        SectionTitle("آمار شما")
        AppCard(padding = 8.dp) {

            Column(modifier = Modifier.padding(16.dp)) {
                userStats.forEachIndexed { index, (label, value) ->
                    StatRow(
                        label = label,
                        value = value,
                        isLabelBold = index == userStats.size - 1 || index == userStats.size - 2,
                        icon = if (index == userStats.size - 1) Icons.Outlined.Security else if (index == userStats.size - 2) Icons.Outlined.GppGood else Icons.Outlined.Shield
                    )

                    // Only show divider if it's not the last item
                    if (index != userStats.size - 1) {
                        Divider()
                    }

                }
            }

        }
    }
}

@Composable
fun StatRow(
    label: String,
    icon: ImageVector?,
    value: String,
    isLabelBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp),
                tint = GreyMiddle

            )

        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = if (isLabelBold) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
        )
        Text(
            text = NumberUtils.toPersianNumbers(value),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 4.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Left
        )
    }


}

@Composable
fun Tools(
    onAppScanClick: () -> Unit,
    onUrlScanClick: () -> Unit,
    onReportByUserClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        SectionTitle("ابزارها")
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ToolCard(
                text = "اسکن برنامه‌ها",
                icon = Icons.Outlined.TrackChanges,
                onClick = onAppScanClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                text = "بررسی لینک",
                icon = Icons.Outlined.AdsClick,
                onClick = onUrlScanClick,
                modifier = Modifier.weight(1f)

            )
            ToolCard(
                text = "گزارش کاربر",
                icon = Icons.Outlined.Face,
                onClick = onReportByUserClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ToolCard(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {


    AppCard(
        modifier = modifier,
        padding = 4.dp,
    )

    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
                tint = colorScheme.primary
            )
            Text(

                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = colorScheme.onSurface,
                text = text,
                textAlign = TextAlign.Center,

                )
        }
    }
}


@Composable
fun UpdateSection(
    updateState: UpdateState, onUpdateDatabaseClick: () -> Unit
) {
    // Infinite rotation animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "Infinite Rotation"
    )

    AppCard(padding = 8.dp) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display appropriate text based on update state
            Text(
                text = when (updateState) {
                    is UpdateState.Idle -> stringResource(
                        R.string.last_update_time,
                        updateState.lastUpdateTime
                    )

                    is UpdateState.Updating -> "در حال به‌روزرسانی..."
                    is UpdateState.Success -> stringResource(
                        R.string.last_update_time,
                        updateState.lastUpdateTime
                    )

                    is UpdateState.Skipped -> stringResource(
                        R.string.last_update_time,
                        updateState.message
                    )

                    is UpdateState.Error -> "خطا! لطفا بعدا تلاش کنید."

                }, modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = when (updateState) {
                    is UpdateState.Error -> colorScheme.onError
                    else -> colorScheme.onSurface
                }
            )

            // Show progress indicator or update button
            when (updateState) {
                is UpdateState.Updating -> {
                    LaunchedEffect(updateState) {
                        // Animation auto-managed by infiniteTransition
                    }
                    Icon(
                        imageVector = Icons.Outlined.Update,
                        contentDescription = "در حال به‌روزرسانی",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = colorScheme.onSurface.copy(alpha = 0.6f)

                    )
                }

                is UpdateState.Success -> {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "به‌روزرسانی موفق",
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.primary
                    )
                }

                else -> {
                    IconButton(
                        onClick = onUpdateDatabaseClick, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Update,
                            contentDescription = "به‌روزرسانی",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.primary

                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AboutAndFaqs(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Button(
                onClick = { navController.navigate(NavRoutes.ABOUT_SCREEN) },
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp),
                contentPadding = PaddingValues(1.dp)
            ) {
                Text(
                    text = "درباره برنامه",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { navController.navigate(NavRoutes.FAQ_SCREEN) },
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp),
                contentPadding = PaddingValues(1.dp)
            ) {
                Text(
                    text = "سوالات متداول",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { navController.navigate(NavRoutes.PERMISSION_SCREEN) },
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp),
                contentPadding = PaddingValues(1.dp)
            ) {
                Text(
                    text = "دسترسی‌ها",
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LinkCard(linkData: Link?) {
    val context = LocalContext.current

    if (linkData != null) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    WebUtils.openUrl(context, linkData.link)
                },

            ) {

            if (linkData.type == 1) {
                val linkColor = linkData.color?.let {
                    try {
                        Color(it.toColorInt())
                    } catch (_: IllegalArgumentException) {
                        colorScheme.onSurface
                    }
                } ?: colorScheme.onSurface

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!linkData.image.isNullOrEmpty()) {
                        // link image
                        Image(
                            painter = rememberAsyncImagePainter(linkData.image),
                            contentDescription = "link image",
                            modifier = Modifier
                                .size(64.dp)
                                .padding(end = 12.dp)
                        )
                    }
                    Column {
                        Text(
                            text = linkData.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = linkColor
                        )
                        linkData.description?.let {
                            if (it.isNotBlank()) {
                                Text(
                                    text = it,
                                    fontSize = 13.sp,
                                    color = GreyMiddle
                                )
                            }
                        }
                    }
                }
            } else if (linkData.type == 2) {
                Image(
                    painter = rememberAsyncImagePainter(linkData.image),
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

        }
    }
}

// TODO: Remove this component after final release
@Composable
fun BetaNotice() {
    val appVersion = BuildConfig.VERSION_NAME.lowercase()
    val isPreRelease = appVersion.contains("alpha") || appVersion.contains("beta")

    if (isPreRelease) {
        AppCard {

            AppCard(
                containerColor = colorScheme.tertiaryContainer,
                padding = 4.dp

            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "این نسخه آزمایشی برنامه است و ممکن است دارای ایرادات و اشکالاتی باشد. لطفا مشکلات را از طریق گزارش اشکال در صفحه درباره برنامه اطلاع دهید.",
                        fontSize = 12.sp,
                        color = colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center
                    )


                }
            }
        }
    }
}

@Composable
fun AppVersion(navController: NavController) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .padding(vertical = 8.dp)
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() },
            ) { navController.navigate(NavRoutes.ABOUT_SCREEN) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "مطمئن باش، نسخه " + BuildConfig.VERSION_NAME,
            fontSize = 13.sp,
            color = colorScheme.onSurface
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AppNamePreview() {
    MotmaenBashTheme {
        AppName(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Required Permissions - All Granted")
@Composable
fun RequiredPermissionsSectionPreview_AllGranted() {
    MotmaenBashTheme {
        CriticalPermissionsSection(
            notificationPermissionStatus = true,
            overlayPermissionStatus = true,
            onNotificationPermissionClick = {},
            onOverlayPermissionClick = {},
            onMoreInfoClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Required Permissions - Missing Permissions")
@Composable
fun RequiredPermissionsSectionPreview_MissingPermissions() {
    MotmaenBashTheme {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = 1f
            )
        ) {
            CriticalPermissionsSection(
                notificationPermissionStatus = false,
                overlayPermissionStatus = false,
                onNotificationPermissionClick = {},
                onOverlayPermissionClick = {},
                onMoreInfoClick = {}
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
fun ProtectionStatusPreview() {
    MotmaenBashTheme {
        ProtectionStatus(
            smsPermissionStatus = false,
            onSmsPermissionClick = {},
            accessibilitySettingStatus = false,
            onAccessibilitySettingClick = {},
            isCriticalPermissionsMissing = false

        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProtectionStatusLargeFontPreview() {
    MotmaenBashTheme {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = 1.6f
            )
        ) {
            ProtectionStatus(
                smsPermissionStatus = false,
                onSmsPermissionClick = {},
                accessibilitySettingStatus = false,
                onAccessibilitySettingClick = {},
                isCriticalPermissionsMissing = false

            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProtectionStatusLockedPreview() {
    MotmaenBashTheme {
        ProtectionStatus(
            smsPermissionStatus = false,
            onSmsPermissionClick = {},
            accessibilitySettingStatus = false,
            onAccessibilitySettingClick = {},
            isCriticalPermissionsMissing = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatsPreview() {
    MotmaenBashTheme {
        Stats(
            suspiciousLinksDetected = 42,
            suspiciousSmsDetected = 15,
            suspiciousAppDetected = 7,
            verifiedGatewayDetected = 89
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ToolsPreview() {
    MotmaenBashTheme {
        Tools(
            onAppScanClick = {},
            onUrlScanClick = {},
            onReportByUserClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateSectionPreview() {
    MotmaenBashTheme {
        UpdateSection(
            updateState = UpdateState.Success(lastUpdateTime = "۱۴۰۳/۰۵/۲۰"),
            onUpdateDatabaseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutAndFaqPreview() {
    MotmaenBashTheme {
        AboutAndFaqs(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppVersionPreview() {
    MotmaenBashTheme {
        AppVersion(navController = rememberNavController())
    }
}


@Preview(showBackground = true, name = "TipOfDay Preview")
@Composable
fun PreviewTipOfDaySection() {
    MotmaenBashTheme {
        TipOfDaySection(
            tip = "با توجه به انتشار نسخه‌های جعلی همراه بانک و اینترنت بانک از سوی کلاهبردارها، اکیدا از جستجوی این خدمات در موتورهای جستجوی خودداری و از سایت اصلی بانک‌ها اقدام به دریافت برنامه‌ها یا خدمات مورد نظر نمایید.",
            isRefreshing = false,
            onRefreshClick = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BetaNoticePreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            BetaNotice()
        }
    }
}
