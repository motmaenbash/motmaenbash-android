import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AdsClick
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppAlertDialog
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.RowDivider
import nu.milad.motmaenbash.ui.theme.Green
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.utils.PermissionManager
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
    val suspiciousLinksDetected by viewModel.suspiciousLinksDetected.collectAsState()
    val suspiciousSmsDetected by viewModel.suspiciousSmsDetected.collectAsState()
    val suspiciousAppDetected by viewModel.suspiciousAppDetected.collectAsState()

    val updateState by viewModel.updateState.collectAsState()
    val updateDialogState by viewModel.updateDialogState.collectAsState()

    // Observe sponsor data
    val sponsorData by viewModel.sponsorData.collectAsState()

    // Permission States
    val smsPermissionState = rememberPermissionState(
        Manifest.permission.RECEIVE_SMS
    ) { isGranted ->
        viewModel.updatePermissionStatus(
            MainViewModel.PermissionType.SMS, isGranted
        )
    }

    // Notification Permission
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { isGranted ->
            viewModel.updatePermissionStatus(
                MainViewModel.PermissionType.NOTIFICATIONS, isGranted
            )
        }
    } else {
        null
    }


    // Observe permission statuses
    val smsPermissionStatus by viewModel.smsPermissionStatus.collectAsState()
    val accessibilitySettingStatus by viewModel.accessibilitySettingStatus.collectAsState()
    val overlayPermissionStatus by viewModel.overlayPermissionStatus.collectAsState()

    var showAccessibilityGuide by remember { mutableStateOf(false) }

    // Permission warning banner state
    val showPermissionWarning =
        !smsPermissionStatus || !accessibilitySettingStatus || !overlayPermissionStatus
    val coroutineScope = rememberCoroutineScope()


    // ActivityResultLaunchers for permissions
    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Add delay to allow system to update
        viewModel.viewModelScope.launch {
            delay(300)
            viewModel.checkAccessibilityPermission()
        }
    }

    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // When user returns from settings, check if permission is now granted
        viewModel.checkOverlayPermission()
    }




    LaunchedEffect(Unit) {
        viewModel.checkInitialPermissions()
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

//
//            //todo:delete
//
//
//            val radioOptions = listOf("Option 1", "Option 2", "Option 3")
//            var selectedOption by remember { mutableStateOf(radioOptions[0]) }
//            var textFieldValue by remember { mutableStateOf("") }
//            val sliderValue by remember { mutableFloatStateOf(0f) }
//
//
//            Column(
//                modifier = Modifier
//                    .wrapContentSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Sample Text
//                Text(
//                    text = "Primary Color Text",
//                    color = colorScheme.primary,
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Sample Button
//                Button(
//                    onClick = { /* Handle click */ },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = "Primary Button",
//                        color = colorScheme.onPrimary
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Sample Card
//                Card(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = "Surface Background Card",
//                            color = colorScheme.onSurface,
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "This is a sample card.",
//                            color = colorScheme.onSurface,
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Sample Tertiary Text
//                Text(
//                    text = "Tertiary Color Text",
//                    color = colorScheme.tertiary,
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Sample Icon Button
//                IconButton(
//                    onClick = { /* Handle click */ },
//                    modifier = Modifier
//                        .size(40.dp)
//                        .background(colorScheme.tertiary)
//                        .padding(8.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Settings,
//                        contentDescription = "Settings",
//                        tint = colorScheme.onTertiary
//                    )
//                }
//            }
//
//
//            // Sample RadioButton Group
//            Column {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                ) {
//                    RadioButton(
//                        selected = selectedOption == "text",
//                        onClick = { selectedOption = "text22" },
//                        colors = RadioButtonDefaults.colors(
//                            unselectedColor = colorScheme.onBackground,
//                            selectedColor = colorScheme.primary
//                        )
//                    )
//                    Text(
//                        text = "text",
//                        color = colorScheme.onBackground,
//                        modifier = Modifier.padding(start = 8.dp)
//                    )
//                }
//
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample Switch
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            ) {
//                Switch(
//                    checked = true,
//                    onCheckedChange = { },
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = colorScheme.primary,
//                        uncheckedThumbColor = colorScheme.onSurface,
//                        checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f),
//                        uncheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.5f)
//                    )
//                )
//
//                Switch(
//                    checked = false,
//                    onCheckedChange = { },
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = colorScheme.primary,
//                        uncheckedThumbColor = colorScheme.onSurface,
//                        checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f),
//                        uncheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.5f)
//                    )
//                )
//                Text(
//                    text = "Switch",
//                    color = colorScheme.onBackground,
//                    modifier = Modifier.padding(start = 8.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample TextField
//            TextField(
//                value = textFieldValue,
//                onValueChange = { textFieldValue = it },
//                label = { Text("Text Field") },
//
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample Checkbox
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            ) {
//                Checkbox(
//                    checked = true,
//                    colors = CheckboxDefaults.colors(
//                        uncheckedColor = colorScheme.onSurface,
//                        checkedColor = colorScheme.primary
//                    ),
//                    onCheckedChange = { },
//
//                    )
//                Checkbox(
//                    checked = false,
//                    colors = CheckboxDefaults.colors(
//                        uncheckedColor = colorScheme.onSurface,
//                        checkedColor = colorScheme.primary
//                    ),
//                    onCheckedChange = { },
//
//                    )
//                Text(
//                    text = "Checkbox",
//                    color = colorScheme.onBackground,
//                    modifier = Modifier.padding(start = 8.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample Slider
//            Slider(
//                value = sliderValue,
//                onValueChange = { },
//                valueRange = 0f..100f,
//                colors = SliderDefaults.colors(
//                    thumbColor = colorScheme.primary,
//                    activeTrackColor = colorScheme.primary,
//                    inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.5f)
//                ),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample Button using Secondary Color
//            Button(
//                onClick = { /* Handle click */ },
//                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    text = "Secondary Button",
//                    color = colorScheme.onSecondary
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sample Text using OnSecondary Color for text
//            Text(
//                text = "Text using OnSecondary Color",
//                color = colorScheme.onSecondary,
//            )
//            //todo: delete till here


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

            TipOfDaySection(tip = tipOfTheDay, isRefreshing = isRefreshing, onRefreshClick = {
                viewModel.refreshTipOfTheDay()
            })


//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(100.dp)
//                    .padding(8.dp)
//                    .clip(RoundedCornerShape(16.dp))
//            ) {
//                val exoPlayer = remember {
//                    ExoPlayer.Builder(context).build().apply {
//                        val mediaItem = MediaItem.fromUri(
//                            "android.resource://${context.packageName}/${R.raw.emoji}".toUri()
//                        )
//                        setMediaItem(mediaItem)
//                        repeatMode = Player.REPEAT_MODE_ALL
//                        playWhenReady = true
//                        prepare()
//                    }
//                }
//
//                DisposableEffect(Unit) {
//                    onDispose {
//                        exoPlayer.release()
//                    }
//                }
//
//                AndroidView(
//                    factory = { ctx ->
//                        PlayerView(ctx).apply {
//                            player = exoPlayer
//                            useController = false
//                        }
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//
//            Text(
//                modifier = Modifier
//                    .padding(bottom = 12.dp),
//                color = Yellow,
//                text = "عه! ان که عه؟! آغاعه اموجه عه آیا؟ بعام؟!",
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp
//
//            )

            ProtectionStatus(
                showPermissionWarning,
                smsPermissionStatus = smsPermissionStatus,
                onSmsPermissionClick = {
                    smsPermissionState.launchPermissionRequest()
                },
                accessibilitySettingStatus = accessibilitySettingStatus,
                onAccessibilitySettingClick = {

                    if (!accessibilitySettingStatus) {
                        showAccessibilityGuide = true
                    }
                },
                overlayPermissionStatus = overlayPermissionStatus,
                onOverlayPermissionClick = {

                    permissionManager.requestOverlayPermission(context, overlaySettingsLauncher)
                    viewModel.checkOverlayPermission()  // Update status after request

                    coroutineScope.launch {
                        permissionManager.showOverlayPermissionTutorial()
                    }


                })

            Stats(
                suspiciousLinksDetected = suspiciousLinksDetected,
                suspiciousSmsDetected = suspiciousSmsDetected,
                suspiciousAppDetected = suspiciousAppDetected,
            )

            Tools(
                onAppScanClick = { navController.navigate(NavRoutes.APP_SCAN_SCREEN) },
                onUrlScanClick = { navController.navigate(NavRoutes.URL_SCAN_SCREEN) },
                onReportByUserClick = { navController.navigate(NavRoutes.USER_REPORT_SCREEN) })

            DatabaseUpdateSection(updateState = updateState, onUpdateDatabaseClick = {
                viewModel.updateDatabase()
            })


            Spacer(modifier = Modifier.size(8.dp))


            AboutAndFaq(
                onAboutClick = { navController.navigate(NavRoutes.ABOUT_SCREEN) },
                onFaqClick = { navController.navigate(NavRoutes.FAQ_SCREEN) },
                onPermissionsExplanationClick = { navController.navigate(NavRoutes.PERMISSION_SCREEN) },
            )


            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            sponsorData?.let {
                SponsorCard(sponsorData = it)
            }


            // TODO: Remove beta notice after final release
            BetaNotice()


            AppVersion(navController)



            if (showAccessibilityGuide) {
                PermissionGuideDialog(
                    permissionType = MainViewModel.PermissionType.ACCESSIBILITY,
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
            indication = null, interactionSource = remember { MutableInteractionSource() },
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
            text = "اپلیکیشن تشخیص فیشینگ",
            color = colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        color = colorScheme.primary,
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

    Column {
        SectionTitle("نکته روز")

        Box(modifier = Modifier.padding(8.dp)) {
            AppCard(padding = 0.dp) {

                Text(
                    text = tip ?: "در حال بارگیری...",
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 24.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Justify
                )

            }

            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .wrapContentSize()
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .graphicsLayer {
                        rotationZ = if (isRefreshing) rotation else 0f
                    },
                enabled = !isRefreshing,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "نمایش یک نکته دیگر",
                    modifier = Modifier
                        .fillMaxSize(),
                    tint = if (isRefreshing) colorScheme.onSurface.copy(alpha = 0.6f) else colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PermissionWarningBanner(
    smsPermissionStatus: Boolean,
    accessibilitySettingStatus: Boolean,
    overlayPermissionStatus: Boolean
) {
    val missingPermissions = mutableListOf<String>()

    if (!smsPermissionStatus) missingPermissions.add("دسترسی پیامک")
    if (!accessibilitySettingStatus) missingPermissions.add("دسترسی دسترسی‌پذیری")
    if (!overlayPermissionStatus) missingPermissions.add("نمایش روی سایر برنامه‌ها")

    if (missingPermissions.isNotEmpty()) {

        AppCard(
            containerColor = Color.Red.copy(alpha = 0.05f),
            cornerRadius = 12.dp, //todo: check and dfelete if not needed
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
                        imageVector = Icons.Outlined.PrivacyTip,
                        contentDescription = "هشدار",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "برای حفاظت کامل، دسترسی‌های زیر را فعال کنید:",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                missingPermissions.forEach { permission ->
                    Text(
                        text = "• $permission",
                        color = Color.Red,
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
    showPermissionWarning: Boolean,
    smsPermissionStatus: Boolean,
    onSmsPermissionClick: () -> Unit,
    accessibilitySettingStatus: Boolean,
    onAccessibilitySettingClick: () -> Unit,
    overlayPermissionStatus: Boolean,
    onOverlayPermissionClick: () -> Unit
) {
    // state for showing the protector info dialog
    var showProtectorInfoDialog by remember { mutableStateOf(false) }

    // Define permissions as a list of triples: (label, isGranted, activateAction)
    val permissions = listOf(
        Triple(
            stringResource(id = R.string.sms_protection),
            smsPermissionStatus,
            onSmsPermissionClick
        ),
        Triple(
            stringResource(id = R.string.accessibility_protection),
            accessibilitySettingStatus,
            onAccessibilitySettingClick
        ),
        Triple(
            stringResource(id = R.string.overlay_protection),
            overlayPermissionStatus,
            onOverlayPermissionClick
        ),
        Triple("محافظ نصب برنامه", true, {}) // This one is always active with no action
    )


    Column {
        SectionTitle(stringResource(id = R.string.protection_status))

        AppCard(
            padding = 8.dp,
            modifier = Modifier.fillMaxWidth(),
            border = if (showPermissionWarning) {
                BorderStroke(
                    width = 1.dp,
                    color = Color.Red.copy(alpha = 0.5f)
                )
            } else {
                null
            }

        ) {


            if (showPermissionWarning) {
                //Permission Warning
                Column(modifier = Modifier.padding(8.dp)) {

                    PermissionWarningBanner(
                        smsPermissionStatus = smsPermissionStatus,
                        accessibilitySettingStatus = accessibilitySettingStatus,
                        overlayPermissionStatus = overlayPermissionStatus
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier.padding(
                    start = 16.dp, end = 16.dp, bottom = 8
                        .dp
                )
            ) {


                permissions.forEach { (label, isGranted, onActivateClick) ->
                    PermissionRow(
                        label = label,
                        isGranted = isGranted,
                        onActivateClick = onActivateClick,
                    )

                    RowDivider()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                showProtectorInfoDialog = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "More Information",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 4.dp),
                            tint = GreyMiddle
                        )

                        Text(
                            modifier = Modifier.padding(end = 4.dp),
                            text = "اطلاعات بیشتر",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Left,
                            color = GreyMiddle
                        )
                    }
                }
            }

        }
    }

    // Show the protector info dialog when the icon is clicked
    if (showProtectorInfoDialog) {
        ProtectorInfoDialog(
            onDismiss = { showProtectorInfoDialog = false }
        )
    }
}


@Composable
fun ProtectorInfoDialog(
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        title = "توضیحات محافظ‌ها",
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        content = {
            Column {
                Text(
                    "محافظ پیامک:",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    "• دسترسی مورد نیاز: دسترسی خواندن پیامک‌ها",
                    style = typography.bodySmall,
                )
                Text(
                    "• عملکرد: تشخیص پیامک‌های مشکوک و فیشینگ بانکی و هشدار به کاربر",
                    style = typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "محافظ دسترسی‌پذیری:",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    "• دسترسی مورد نیاز: سرویس دسترسی‌پذیری (Accessibility)",
                    style = typography.bodySmall,
                )
                Text(
                    "• عملکرد: تشخیص صفحات فیشینگ، پیشگیری از دزدی اطلاعات حساس و هشدار به کاربر",
                    style = typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "محافظ نمایشی:",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    "• دسترسی مورد نیاز: نمایش روی سایر برنامه‌ها",
                    style = typography.bodySmall,
                )
                Text(
                    "• عملکرد: نمایش هشدارهای فوری هنگام شناسایی خطر و محافظت در لحظه",
                    style = typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "محافظ نصب برنامه:",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    "• دسترسی مورد نیاز: کار با بخش نصب برنامه‌ها (برای هشدار هنگام نصب برنامه‌های مشکوک)",
                    style = typography.bodySmall,
                )
                Text(
                    "• عملکرد: بررسی برنامه‌های در حال نصب و هشدار برای برنامه‌های مشکوک",
                    style = typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "تمام دسترسی‌ها صرفا برای محافظت از شما در برابر تهدیدات امنیتی استفاده می‌شود. هیچ داده شخصی از دستگاه شما خارج نمی‌شود.",
                    style = typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        confirmText = "متوجه شدم",
        onConfirm = onDismiss,
        dismissText = null,
        onDismiss = { /* Do nothing */ }
    )
}

@Composable
fun PermissionGuideDialog(
    permissionType: MainViewModel.PermissionType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        title = "راهنمای فعالسازی",
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        content = {
            when (permissionType) {
                MainViewModel.PermissionType.ACCESSIBILITY -> {
                    Text(
                        "• چرا نیاز است؟\nاین دسترسی (Accessibility) برای شناسایی خودکار پیامک‌ها و فعالیت‌های مشکوک در پس‌زمینه ضروری است.",
                        style = typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• مراحل فعال‌سازی:",
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // More detailed steps based on device manufacturer
                    val deviceManufacturer = Build.MANUFACTURER.lowercase()
                    if (deviceManufacturer.contains("samsung")) {
                        Text("1. وارد تنظیمات دستگاه شوید")
                        Text("2. بخش «دسترسی‌پذیری» (Accessibility) را انتخاب کنید")
                        Text("3. بخش «برنامه های نصب شده» یا «Downloaded Services» را انتخاب کنید")
                        Text("4. گزینه «مطمئن باش» را پیدا کرده و آن را فعال کنید")
                    } else {
                        Text("1. وارد تنظیمات دستگاه شوید")
                        Text("2. بخش «دسترسی‌پذیری» (Accessibility) را انتخاب کنید")
                        Text("3. گزینه «مطمئن باش» را پیدا کرده و آن را فعال کنید")
                    }
                }

                else -> Text("راهنمای فعالسازی", style = typography.bodyMedium)
            }
        },
        onConfirm = onConfirm,
        confirmText = "برو به تنظیمات",
        dismissText = "لغو",
        onDismiss = onDismiss
    )
}

@Composable
fun PermissionRow(
    label: String,
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
            imageVector = if (isGranted) Icons.Outlined.VerifiedUser else Icons.Outlined.RemoveModerator,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 2.dp),
            tint = if (isGranted) Green else Red

        )
        Text(

            text = label + ": غیرفعال".takeIf { !isGranted }.orEmpty(),
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            color = if (isGranted) Green else Red

        )

        Box(
            modifier = Modifier.width(76.dp),
            contentAlignment = Alignment.Center,
        ) {

            if (!isGranted) {
                Button(
                    onClick = onActivateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)

                ) {
                    Text(text = "فعال‌سازی", fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "فعال",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Green,

                    )
            }
        }
    }


}

@Composable
fun Stats(
    suspiciousLinksDetected: Int,
    suspiciousSmsDetected: Int,
    suspiciousAppDetected: Int,
) {
    val stats = listOf(
        "هشدار لینک مشکوک" to NumberUtils.formatNumber(suspiciousLinksDetected),
        "هشدار پیامک مشکوک" to NumberUtils.formatNumber(suspiciousSmsDetected),
        "هشدار اپلیکیشن مشکوک" to NumberUtils.formatNumber(suspiciousAppDetected),
        "تعداد کل هشدارها" to NumberUtils.formatNumber(suspiciousLinksDetected + suspiciousSmsDetected + suspiciousAppDetected)
    )

    Column {
        SectionTitle("آمار")
        AppCard(padding = 8.dp) {

            Column(modifier = Modifier.padding(16.dp)) {
                stats.forEachIndexed { index, (label, value) ->
                    StatRow(
                        label = label,
                        value = value,
                    )

                    // Only show divider if it's not the last item
                    if (index != stats.size - 1) {
                        RowDivider()
                    }

                }
            }

        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
        )
        Text(
            text = NumberUtils.toPersianNumbers(value),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
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
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ToolCard(
                text = "اسکن برنامه‌ها",
                icon = Icons.Outlined.TrackChanges,
                onClick = onAppScanClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                text = "بررسی URL",
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
    text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier
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
fun DatabaseUpdateSection(
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
                    is UpdateState.Idle -> "آخرین بروزرسانی: ${updateState.lastUpdateTime}"
                    is UpdateState.Updating -> "در حال بروزرسانی..."
                    is UpdateState.Success -> "آخرین بروزرسانی: ${updateState.lastUpdateTime}"
                    is UpdateState.Skipped -> "آخرین بروزرسانی: ${updateState.message}"
                    is UpdateState.Error -> "خطا! لطفا بعدا تلاش کنید."

                }, modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = when (updateState) {
                    is UpdateState.Error -> Red
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
                        contentDescription = "در حال بروزرسانی",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = colorScheme.onSurface.copy(alpha = 0.6f)

                    )
                }

                is UpdateState.Success -> {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "بروزرسانی موفق",
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
                            contentDescription = "بروزرسانی",
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
fun AboutAndFaq(
    onAboutClick: () -> Unit,
    onFaqClick: () -> Unit,
    onPermissionsExplanationClick: () -> Unit,
) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = onAboutClick,
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
                onClick = onFaqClick,
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
                onClick = onPermissionsExplanationClick,
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
fun SponsorCard(sponsorData: MainViewModel.SponsorData?) {
    val context = LocalContext.current

    val sponsorColor = sponsorData?.color?.let {
        try {
            Color(it.toColorInt())
        } catch (e: IllegalArgumentException) {
            colorScheme.onSurface
        }
    } ?: colorScheme.onSurface


    if (sponsorData != null) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(4.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    WebUtils.openUrl(context, sponsorData.link)
                },

            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // logo
                Image(
                    painter = rememberAsyncImagePainter(sponsorData.logoUrl),
                    contentDescription = "Sponsor Logo",
                    modifier = Modifier.size(64.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = sponsorData.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = sponsorColor
                    )
                    sponsorData.description?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
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
        AppCard(
            border = BorderStroke(1.dp, Color("#FFB74D".toColorInt())),
            containerColor = Color("#FFF3E0".toColorInt()),
            cornerRadius = 12.dp

        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "این نسخه آزمایشی برنامه است و ممکن است دارای ایرادات و اشکالاتی باشد. لطفا مشکلات را از طریق گزارش اشکال در صفحه درباره برنامه اطلاع دهید.",
                    fontSize = 12.sp,
                    color = Color("#795548".toColorInt()),
                )


            }
        }
    }
}

@Composable
fun AppVersion(navController: NavController) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .padding(bottom = 8.dp)
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


@Composable
fun UpdateDialog(
    updateDialogState: MainViewModel.UpdateDialogState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AppAlertDialog(
        title = "نسخه جدید برنامه (${updateDialogState.latestVersionName})",
        icon = Icons.Outlined.Update,
        message = if (updateDialogState.forceUpdate) "برای ادامه استفاده از برنامه، لطفا به نسخه جدید به‌روزرسانی کنید."
        else stringResource(R.string.app_update_message),
        links = updateDialogState.links,
        onConfirm = { WebUtils.openUrl(context, updateDialogState.links.first().second) },
        onDismiss = onDismiss,
        confirmText = if (updateDialogState.forceUpdate) "به‌روزرسانی" else null,
        dismissText = if (updateDialogState.forceUpdate) null else "بعدا",
    )
}


@Preview(showBackground = true, name = "TipOfDay Preview")
@Composable
fun PreviewTipOfDaySection() {
    TipOfDaySection(
        tip = "با توجه به انتشار نسخه‌های جعلی همراه بانک و اینترنت بانک از سوی کلاهبردارها، اکیدا از جستجوی این خدمات در موتورهای جستجوی خودداری و از سایت اصلی بانک‌ها اقدام به دریافت برنامه‌ها یا خدمات مورد نظر نمایید.",
        isRefreshing = false,
        onRefreshClick = {}
    )
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
