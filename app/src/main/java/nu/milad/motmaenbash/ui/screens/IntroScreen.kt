package nu.milad.motmaenbash.ui.screens


import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.consts.PermissionType
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AccessibilityPermissionDialog
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.AppLogo
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.components.NotificationPermissionDialog
import nu.milad.motmaenbash.ui.components.SmsPermissionDialog
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.RedVariant
import nu.milad.motmaenbash.utils.PermissionManager
import nu.milad.motmaenbash.utils.ServiceUtils
import nu.milad.motmaenbash.viewmodels.IntroViewModel


private object IntroSteps {
    const val INTRO = "intro"
    const val TRUST = "trust"
    const val OVERLAY = "overlay"
    const val NOTIFICATIONS = "notifications"
    const val SMS = "sms"
    const val ACCESSIBILITY = "accessibility"
    const val COMPLETED = "completed"
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroScreen(viewModel: IntroViewModel = viewModel()) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val permissionManager = remember { PermissionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    // Observe permission statuses
    val smsPermissionStatus by viewModel.smsPermissionStatus.collectAsState()
    val overlayPermissionStatus by viewModel.overlayPermissionStatus.collectAsState()
    val accessibilitySettingStatus by viewModel.accessibilitySettingStatus.collectAsState()
    val notificationPermissionStatus by viewModel.notificationPermissionStatus.collectAsState()

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

    // Store required permissions and get remaining ones
    val allPermissionSteps = remember {
        listOf(
            IntroSteps.INTRO to true, // Always include intro as the first step
            IntroSteps.TRUST to true, // Always include trust step
            IntroSteps.OVERLAY to overlayPermissionStatus,
            IntroSteps.NOTIFICATIONS to notificationPermissionStatus,
            IntroSteps.SMS to smsPermissionStatus,
            IntroSteps.ACCESSIBILITY to accessibilitySettingStatus,
        )
    }

    // Filter out already granted permissions (except intro and trust which should always be shown)
    val requiredPermissions = remember(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        listOf(IntroSteps.INTRO, IntroSteps.TRUST) + allPermissionSteps.filter {
            it.first != IntroSteps.INTRO && it.first != IntroSteps.TRUST && !it.second
        }.map { it.first }
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Get current permission step
    val currentStep = remember(requiredPermissions, currentStepIndex) {
        requiredPermissions.getOrNull(currentStepIndex) ?: IntroSteps.COMPLETED
    }

    val isLastStep = currentStepIndex == requiredPermissions.size - 1

    var showAccessibilityGuide by remember { mutableStateOf(false) }

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

    // Prevent returning to permission screen after navigating to main screen
    DisposableEffect(key1 = Unit) {
        val onBackPressedCallback = {
            currentStep == IntroSteps.ACCESSIBILITY && accessibilitySettingStatus // Consume back press when we've navigated to main screen
        }
        onDispose {
            // Clean up if needed
        }
    }

    // Function to navigate to main screen
    val navigateToMainScreen = {
        coroutineScope.launch {
            // Mark as not first launch inside coroutine
            viewModel.setHasSeenIntro()

            // Navigate to main screen after preferences are updated
            navController.navigate(NavRoutes.MAIN_SCREEN) {
                // Clear back stack so user can't go back to permission screen
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    // Launchers
    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissionStatus(PermissionType.OVERLAY)
    }

    // Accessibility settings launcher
    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissionStatus(PermissionType.ACCESSIBILITY)
    }

    // Check initial permissions
    LaunchedEffect(Unit) {
        viewModel.checkInitialPermissions()
    }

    // Check if all permissions are granted but intro not marked as shown
    LaunchedEffect(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus
    ) {
        // If we have all permissions and we're at the intro/trust step, we should move to main screen
        if (currentStepIndex <= 1 && (currentStep == IntroSteps.INTRO || currentStep == IntroSteps.TRUST) &&
            overlayPermissionStatus &&
            notificationPermissionStatus &&
            smsPermissionStatus &&
            accessibilitySettingStatus
        ) {
            // All permissions granted but intro/trust not completed, go directly to main
            navigateToMainScreen()
        }
    }

    LaunchedEffect(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        when (currentStep) {
            IntroSteps.INTRO, IntroSteps.TRUST -> {
                // Nothing to do for intro and trust
            }

            IntroSteps.OVERLAY -> if (overlayPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            IntroSteps.NOTIFICATIONS -> if (notificationPermissionStatus) {
                // Start the monitoring service when notification permission is granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ServiceUtils().startMonitoringService(context)
                }
                delay(500)
                currentStepIndex++
            }

            IntroSteps.SMS -> if (smsPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            IntroSteps.ACCESSIBILITY -> if (accessibilitySettingStatus) {
                delay(5000)
                navigateToMainScreen()
            }
        }
    }

    // Handle back button only when we're not on the first step
    BackHandler(enabled = currentStepIndex > 0) {
        currentStepIndex--
    }

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {
                IntroSteps.INTRO -> {
                    // Intro
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogo(90.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "به «مطمئن باش» خوش آمدید",
                            style = typography.headlineSmall,
                            color = colorScheme.primary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "این برنامه برای هشدار درباره پیامک‌ها، برنامه‌ها و لینک‌های مشکوک طراحی شده است.",
                            style = typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)

                        Text(
                            buildAnnotatedString {
                                append("در مراحل بعد می‌توانید برخی دسترسی‌ها را فعال کنید. ")

                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("دسترسی‌هایی مثل پیامک و دسترسی‌پذیری اختیاری‌اند،")
                                }

                                append(" اما در صورت فعال‌سازی، بررسی‌ها خودکار انجام می‌شود.")
                            },
                            style = typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)
                        Text(
                            "بدون این دسترسی‌ها هم می‌توانید از سایر امکانات مانند اسکن برنامه‌ها، بررسی دستی لینک و... استفاده کنید.",
                            style = typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { currentStepIndex++ },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("خوندم، بزن بریم")
                        }
                    }
                }

                IntroSteps.TRUST -> {
                    // Trust Step
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Trust Icon
                        AnimatedPermissionIcon(Icons.Outlined.VerifiedUser, size = 48.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "آیا خود «مطمئن باش» امن است؟",
                            style = typography.headlineSmall,
                            color = colorScheme.primary,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Trust reasons
                        TrustReasonCard(
                            icon = Icons.Outlined.Code,
                            title = "متن‌باز (Open Source)",
                            description = "«مطمئن باش» متن‌باز است و کدهای آن توسط کارشناس‌ها و عموم کاربرها قابل بررسی و ارزیابی است."
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TrustReasonCard(
                            icon = Icons.Outlined.CloudOff,
                            title = "بدون سرور و حفظ کامل حریم خصوصی",
                            description = "برنامه سرور ندارد و بررسی‌ها به صورت آفلاین روی خود گوشی انجام می‌شود و اطلاعات برای بررسی به جایی ارسال نمی‌شود."
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TrustReasonCard(
                            icon = Icons.Outlined.AdminPanelSettings,
                            title = "دسترسی‌های اختیاری",
                            description = "دسترسی‌های حساس برنامه اختیاری‌ست و بدون فعال‌سازی آن‌ها می‌توانید از سایر امکانات برنامه استفاده کنید."
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TrustReasonCard(
                            icon = Icons.Outlined.Verified,
                            title = "انتشار بر روی مارکت‌های معتبر",
                            description = "برنامه توسط مارکت‌های معتبر بررسی و منتشر شده است."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { currentStepIndex++ },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("کامل خوندم، بزن بریم")
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                    }
                }

                IntroSteps.OVERLAY -> PermissionStep(
                    title = stringResource(id = R.string.permission_overlay),
                    permissionName = "SYSTEM_ALERT_WINDOW",
                    description = stringResource(id = R.string.permission_overlay_description),
                    icon = Icons.Outlined.PhoneAndroid,
                    isGranted = overlayPermissionStatus,
                    isLastStep = isLastStep,
                    isOptional = true,
                    onGrant = {
                        permissionManager.requestOverlayPermission(context, overlayLauncher)
                        coroutineScope.launch {
                            permissionManager.showOverlayPermissionTutorial()
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    },
                    onComplete = { navigateToMainScreen() }
                )

                IntroSteps.NOTIFICATIONS -> PermissionStep(
                    title = stringResource(id = R.string.permission_notification),
                    permissionName = "POST_NOTIFICATIONS",
                    description = stringResource(id = R.string.permission_notification_description),
                    icon = Icons.Outlined.Notifications,
                    isGranted = notificationPermissionStatus,
                    isLastStep = isLastStep,
                    onGrant = {
                        if (notificationPermissionState?.status?.isGranted == false && notificationPermissionState.status.shouldShowRationale) {
                            showNotificationPermissionDeniedDialog = true
                        } else {
                            viewModel.setHasRequestedNotificationPermission(true)
                            notificationPermissionState?.launchPermissionRequest()
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    },
                    onComplete = { navigateToMainScreen() }
                )

                IntroSteps.SMS -> PermissionStep(
                    title = stringResource(id = R.string.permission_receive_sms),
                    permissionName = "RECEIVE_SMS",
                    description = stringResource(id = R.string.permission_receive_sms_description),
                    icon = Icons.Outlined.Sms,
                    isGranted = smsPermissionStatus,
                    isLastStep = isLastStep,
                    onGrant = {
                        if (!smsPermissionState.status.isGranted && smsPermissionState.status.shouldShowRationale) {
                            showSmsPermissionDeniedDialog = true
                        } else {
                            viewModel.setHasRequestedSmsPermission(true)
                            smsPermissionState.launchPermissionRequest()
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    },
                    onComplete = { navigateToMainScreen() }
                )

                IntroSteps.ACCESSIBILITY -> {
                    PermissionStep(
                        title = "سرویس دسترسی‌پذیری",
                        permissionName = "ACCESSIBILITY_SERVICE",
                        description = stringResource(id = R.string.permission_accessibility_short_description),
                        icon = Icons.Outlined.AccessibilityNew,
                        isGranted = accessibilitySettingStatus,
                        isLastStep = true, // Always the last step
                        onGrant = { showAccessibilityGuide = true },
                        onComplete = { navigateToMainScreen() }
                    )

                    if (showAccessibilityGuide) {
                        AccessibilityPermissionDialog(
                            onConfirm = {
                                showAccessibilityGuide = false
                                permissionManager.launchAccessibilitySettings(
                                    accessibilitySettingsLauncher
                                )
                            },
                            onDismiss = { showAccessibilityGuide = false }
                        )
                    }
                }
            }
        }

        if (currentStep == IntroSteps.INTRO) {
            DeveloperCredit()
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
                onDismiss = { showSmsPermissionDeniedDialog = false }
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
    }
}

@Composable
fun TrustReasonCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    AppCard(
        padding = 2.dp,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
    ) {

        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ColorPrimary

                )
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            Divider(verticalPadding = 2.dp, horizontalPadding = 8.dp)
            Text(
                description,
                style = typography.bodySmall,
                color = colorScheme.onSurface

            )

        }

    }


}


@Composable
fun PermissionStep(
    title: String,
    permissionName: String = "",
    description: String,
    isGranted: Boolean,
    isLastStep: Boolean = false,
    isOptional: Boolean = false,
    icon: ImageVector? = null,
    onGrant: () -> Unit,
    onNext: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated icon for the permission
        icon?.let {
            AnimatedPermissionIcon(it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title and description
        Text(
            title,
            style = typography.headlineSmall,
            color = colorScheme.primary,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (permissionName.isNotEmpty()) {
            Text(
                permissionName,
                style = typography.bodySmall,
                color = GreyMiddle,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            description,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp),
            color = colorScheme.onBackground
        )



        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGrant,
            enabled = !isGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color.Gray else ColorPrimary,
                contentColor = Color.White
            )
        ) {
            Text(if (isGranted) "تکمیل شده" else "فعال‌سازی دسترسی")
        }

        // Show "Next Step" button if not the last step
        if (!isLastStep && onNext != null) {
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(
                onClick = onNext,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardDoubleArrowRight,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("گام بعدی")
                }
            }
        }

        // Show "Enter the app" button if this is the last step
        if (isLastStep) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onComplete?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
            ) {
                Text("ورود به برنامه")
            }
        }


        if (isOptional) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "این دسترسی اختیاری است و بعدا هم می‌توان آن را فعال کرد",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Custom content section
        content?.invoke()
    }
}

@Composable
fun AnimatedPermissionIcon(
    icon: ImageVector,
    size: Dp = 64.dp,
) {
    // Track whether the composable has been composed
    var isFirstComposition by remember { mutableStateOf(true) }

    // Set up the animation with proper initial and target values
    val scale by animateFloatAsState(
        targetValue = if (isFirstComposition) 1f else 1.3f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        finishedListener = { isFirstComposition = false }
    )

    // Start the animation when the composable is first rendered
    LaunchedEffect(Unit) {
        isFirstComposition = false
    }

    // Apply animation to the icon
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .scale(scale),
        tint = colorScheme.primary
    )
}

@Composable
fun DeveloperCredit() {
    // pulsing animation for heart
    val infiniteTransition = rememberInfiniteTransition(label = "heartAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartBeatAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .systemBarsPadding()
    ) {
        Text(
            "طراحی و توسعه توسط میلاد نوری",
            style = typography.bodySmall,
            color = GreyMiddle,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Heart",
                modifier = Modifier
                    .size(18.dp)
                    .scale(scale),
                tint = RedVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "برای مردم ایران",
                fontWeight = FontWeight.Bold,
                color = GreyMiddle,
                style = typography.bodySmall,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PermissionStepPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PermissionStep(
                        title = stringResource(id = R.string.permission_overlay),
                        permissionName = "SYSTEM_ALERT_WINDOW",
                        description = "برای نمایش هشدارهای فوری هنگام شناسایی تهدید، نیاز به این دسترسی داریم",
                        icon = Icons.Outlined.PhoneAndroid,
                        isGranted = false,
                        onGrant = { },
                        onNext = { }
                    )
                }

                DeveloperCredit()
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PermissionLastStepPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PermissionStep(
                        title = stringResource(id = R.string.permission_overlay),
                        permissionName = "SYSTEM_ALERT_WINDOW",
                        description = "برای نمایش هشدارهای فوری هنگام شناسایی تهدید، نیاز به این دسترسی داریم",
                        icon = Icons.Outlined.PhoneAndroid,
                        isGranted = false,
                        isLastStep = true,
                        onGrant = { },
                        onNext = { }
                    )
                }

                DeveloperCredit()
            }
        }
    }
}