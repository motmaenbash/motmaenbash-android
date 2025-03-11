import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.ui.LocalNavController
import nu.milad.motmaenbash.ui.ui.theme.BackgroundLightGray
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.Green
import nu.milad.motmaenbash.ui.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.ui.theme.Red
import nu.milad.motmaenbash.utils.NumberUtils
import nu.milad.motmaenbash.viewmodels.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current


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


    // Observe permission statuses
    val smsPermissionStatus by viewModel.smsPermissionStatus.collectAsState()
    val accessibilitySettingStatus by viewModel.accessibilitySettingStatus.collectAsState()
    val overlayPermissionStatus by viewModel.overlayPermissionStatus.collectAsState()

    var showAccessibilityGuide by remember { mutableStateOf(false) }
    var showOverlayGuide by remember { mutableStateOf(false) }

    val PERMISSION_REQUEST_CODE = 1001

    // Permission warning banner state
    val showPermissionWarning =
        !smsPermissionStatus || !accessibilitySettingStatus || !overlayPermissionStatus

    LaunchedEffect(Unit) {
        Log.d("MainScreen", "LaunchedEffect triggered")
        viewModel.checkInitialPermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Settings icon
        IconButton(
            onClick = { navController.navigate(NavRoutes.SETTINGS_SCREEN) },
            modifier = Modifier
                .padding(bottom = 0.dp)
                .size(32.dp)
                .align(Alignment.End)
        ) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "تنظیمات",
                tint = GreyDark,
                modifier = Modifier.size(28.dp)
            )
        }

        AppName(navController)

        TipOfDaySection(tip = tipOfTheDay, isRefreshing = isRefreshing, onRefreshClick = {
            viewModel.refreshTipOfTheDay()
        })



        ProtectionStatus(
            showPermissionWarning,
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
                if (!overlayPermissionStatus) {
                    showOverlayGuide = true
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

        AppVersion(navController)





        updateDialogState?.let { state ->
            UpdateDialog(
                updateDialogState = state,
                onDismiss = { viewModel.dismissUpdateDialog() },
                onUpdateClick = { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                })
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
            color = ColorPrimary,
            text = stringResource(id = R.string.app_name_fa),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "اپلیکیشن تشخیص فیشینگ",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
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
        Text(
            text = "نکته روز",
            color = ColorPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = tip ?: "در حال بارگیری...",
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 100.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify
                )

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = if (isRefreshing) rotation else 0f
                        },
                    enabled = !isRefreshing,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بارگیری مجدد",
                        modifier = Modifier.size(24.dp),
                        tint = if (isRefreshing) Color.Black.copy(alpha = 0.6f) else ColorPrimary
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
    overlayPermissionStatus: Boolean,
    onOverlayPermissionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.protection_status),
            color = ColorPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)

        ) {


            Column(modifier = Modifier.padding(16.dp)) {
                PermissionRow(
                    label = stringResource(id = R.string.sms_protection),
                    isGranted = smsPermissionStatus,
                    onActivateClick = onSmsPermissionClick,
                )



                PermissionRow(
                    label = stringResource(id = R.string.accessibility_protection),
                    isGranted = accessibilitySettingStatus,
                    onActivateClick = onAccessibilitySettingClick,
                    showActivateButton = !accessibilitySettingStatus

                )
                PermissionRow(
                    label = stringResource(id = R.string.overlay_protection),
                    isGranted = overlayPermissionStatus,
                    onActivateClick = onOverlayPermissionClick,
                    showActivateButton = !overlayPermissionStatus
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محافظ نصب برنامه: فعال",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Green,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    label: String,
    isGranted: Boolean,
    onActivateClick: () -> Unit,
    showActivateButton: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${if (isGranted) "فعال" else "غیرفعال"}",
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            color = if (isGranted) Green else Red

        )
        if (!isGranted && showActivateButton) {
            Button(
                onClick = onActivateClick,
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
            ) {
                Text(text = "فعال‌سازی", fontSize = 13.sp)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "آمار",
            color = ColorPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)

        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                StatRow(
                    label = "هشدار لینک مشکوک",
                    value = NumberUtils.formatNumber(suspiciousLinksDetected)
                )
                StatRow(
                    label = "هشدار پیامک مشکوک",
                    value = NumberUtils.formatNumber(suspiciousSmsDetected)
                )
                StatRow(
                    label = "هشدار اپلیکیشن مشکوک",
                    value = NumberUtils.formatNumber(suspiciousAppDetected)
                )
                StatRow(
                    label = "تعداد کل هشدارها",
                    value = NumberUtils.formatNumber(suspiciousLinksDetected + suspiciousSmsDetected + suspiciousAppDetected)
                )
            }

        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            textAlign = TextAlign.Right
        )
        Text(
            text = NumberUtils.toPersianNumbers(value),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
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
        Text(
            text = "ابزارها",
            color = ColorPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ToolCard(
                text = "اسکن برنامه‌ها",
                icon = R.drawable.ic_scan,
                onClick = onAppScanClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                text = "اسکن URL",
                icon = R.drawable.ic_search,
                onClick = onUrlScanClick,
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                text = "گزارش کاربر",
                icon = R.drawable.ic_report,
                onClick = onReportByUserClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ToolCard(
    text: String, icon: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp),
                tint = ColorPrimary
            )
            Text(

                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = GreyDark,
                text = text,
                textAlign = TextAlign.Center,

                )

        }
    }
}


@Composable
fun DatabaseUpdateSection(
    updateState: MainViewModel.UpdateState, onUpdateDatabaseClick: () -> Unit
) {
    // Infinite rotation animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "Infinite Rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display appropriate text based on update state
            Text(
                text = when (updateState) {
                    is MainViewModel.UpdateState.Idle -> "آخرین بروزرسانی: ${updateState.lastUpdateTime}"
                    is MainViewModel.UpdateState.Updating -> "در حال بروزرسانی..."
                    is MainViewModel.UpdateState.Success -> "آخرین بروزرسانی: ${updateState.lastUpdateTime}"
                    is MainViewModel.UpdateState.Error -> "خطا! لطفا بعدا تلاش کنید."
                }, modifier = Modifier.weight(1f), fontSize = 14.sp, color = when (updateState) {
                    is MainViewModel.UpdateState.Error -> Red
                    else -> Color.Black
                }
            )

            // Show progress indicator or update button
            when {
                updateState is MainViewModel.UpdateState.Updating -> {
                    LaunchedEffect(updateState) {
                        // Animation auto-managed by infiniteTransition
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_update),
                        contentDescription = "در حال بروزرسانی",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = Color.Black.copy(alpha = 0.6f)

                    )
                }

                updateState is MainViewModel.UpdateState.Success -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "بروزرسانی موفق",
                        modifier = Modifier.size(24.dp),
                        tint = ColorPrimary
                    )
                }

                else -> {
                    IconButton(
                        onClick = onUpdateDatabaseClick, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_update),
                            contentDescription = "بروزرسانی",
                            modifier = Modifier.size(24.dp),
                            tint = ColorPrimary

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
    val context = LocalContext.current

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
                    text = "درباره برنامه", textAlign = TextAlign.Center
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
                    text = "سوالات متداول", textAlign = TextAlign.Center,
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
                    text = "دسترسی‌ها", textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SponsorCard(sponsorData: MainViewModel.SponsorData?) {
    val context = LocalContext.current // Get the context

    val sponsorColor = sponsorData?.color?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (e: IllegalArgumentException) {
            Color.Black
        }
    } ?: Color.Black


    if (sponsorData != null) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .padding(4.dp)
                .clickable(
                    indication = null, interactionSource = remember { MutableInteractionSource() },
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sponsorData.link))
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundLightGray)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)
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
                        fontSize = 16.sp,
                        color = sponsorColor
                    )
                    sponsorData.description?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it, fontSize = 14.sp, color = Color.Gray
                            )
                        }
                    }
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
            .padding(bottom = 8.dp)
            .clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() },
            ) { navController.navigate(NavRoutes.ABOUT_SCREEN) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "مطمئن باش، نسخه " + BuildConfig.VERSION_NAME, fontSize = 13.sp
        )
    }
}

@Composable
fun UpdateDialog(
    updateDialogState: MainViewModel.UpdateDialogState,
    onDismiss: () -> Unit,
    onUpdateClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!updateDialogState.forceUpdate) onDismiss() }, title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_update),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp),
                    tint = ColorPrimary
                )
                Text(
                    text = "نسخه جدید برنامه (${updateDialogState.latestVersionName})",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }, text = {
            Column {
                Text(
                    text = if (updateDialogState.forceUpdate) "برای ادامه استفاده از برنامه، لطفا به نسخه جدید به‌روزرسانی کنید."
                    else stringResource(R.string.app_update_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 0.dp),
                    fontSize = 15.sp
                )

                // Available update sources
                updateDialogState.links.forEach { (title, link) ->
                    Button(
                        onClick = { onUpdateClick(link) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Text(text = title)
                    }
                }
            }
        }, confirmButton = {}, dismissButton = if (!updateDialogState.forceUpdate) {
            {
                TextButton(
                    onClick = onDismiss, modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("بعدا")
                }
            }
        } else null)
}


//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    MotmaenBashTheme {
//        val navController = rememberNavController()
//        MainScreen(navController)
//    }
//}