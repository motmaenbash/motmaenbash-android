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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import nu.milad.motmaenbash.ui.components.NotificationPermissionDialog
import nu.milad.motmaenbash.ui.components.SmsPermissionDialog
import nu.milad.motmaenbash.ui.components.SmsSettingsDialog
import nu.milad.motmaenbash.ui.components.intro.DeveloperCredit
import nu.milad.motmaenbash.ui.components.intro.FinalStep
import nu.milad.motmaenbash.ui.components.intro.IntroStep
import nu.milad.motmaenbash.ui.components.intro.PermissionStep
import nu.milad.motmaenbash.ui.components.intro.TrustStep
import nu.milad.motmaenbash.utils.PermissionManager
import nu.milad.motmaenbash.utils.ServiceUtils
import nu.milad.motmaenbash.viewmodels.IntroViewModel

// Steps
private enum class IntroStep {
    INTRO, TRUST, OVERLAY, NOTIFICATIONS, SMS, ACCESSIBILITY, FINAL
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


    // Dialog states
    var showSmsPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showAccessibilityGuide by remember { mutableStateOf(false) }
    var showSmsSettingsDialog by remember { mutableStateOf(false) }

    // Permission states
    val smsPermissionState = rememberPermissionState(
        Manifest.permission.RECEIVE_SMS
    ) { isGranted ->
        viewModel.updatePermissionStatus(PermissionType.SMS, isGranted)
    }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { isGranted ->
            viewModel.updatePermissionStatus(PermissionType.NOTIFICATIONS, isGranted)
        }
    } else null

    // Store required permissions and get remaining ones
    val allPermissionSteps = remember {
        listOf(
            IntroStep.INTRO to true, // Always include intro as the first step
            IntroStep.TRUST to true, // Always include trust step
            IntroStep.OVERLAY to overlayPermissionStatus,
            IntroStep.NOTIFICATIONS to notificationPermissionStatus,
            IntroStep.SMS to smsPermissionStatus,
            IntroStep.ACCESSIBILITY to accessibilitySettingStatus,
            IntroStep.FINAL to true, // Always include final step
        )
    }

    // Step management
    // Filter out already granted permissions (except intro and trust which should always be shown)
    val requiredPermissions = remember(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        listOf(IntroStep.INTRO, IntroStep.TRUST) + allPermissionSteps.filter {
            it.first != IntroStep.INTRO && it.first != IntroStep.TRUST && it.first != IntroStep.FINAL && !it.second
        }.map { it.first } + IntroStep.FINAL
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    // Get current permission step
    val currentStep = remember(requiredPermissions, currentStepIndex) {
        requiredPermissions.getOrNull(currentStepIndex)
    }
    val isLastStep = currentStepIndex == requiredPermissions.size - 1

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
            currentStep == IntroStep.ACCESSIBILITY && accessibilitySettingStatus // Consume back press when we've navigated to main screen
        }
        onDispose {
            // Clean up if needed
        }
    }

    // Function to navigate to main screen

    // Navigation function
    val navigateToMainScreen = remember {
        {
            coroutineScope.launch {
                // Mark as not first launch inside coroutine
                viewModel.setHasSeenIntro()

                // Navigate to main screen after preferences are updated
                navController.navigate(NavRoutes.MAIN_SCREEN) {
                    // Clear back stack so user can't go back to permission screen
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
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


    LaunchedEffect(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        when (currentStep) {
            IntroStep.INTRO, IntroStep.TRUST -> {
                // Nothing to do for intro and trust
            }

            IntroStep.OVERLAY -> if (overlayPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            IntroStep.NOTIFICATIONS -> if (notificationPermissionStatus) {
                // Start the monitoring service when notification permission is granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ServiceUtils().startMonitoringService(context)
                }
                delay(500)
                currentStepIndex++
            }

            IntroStep.SMS -> if (smsPermissionStatus) {
                delay(500)
                showSmsSettingsDialog = true

            }

            IntroStep.ACCESSIBILITY -> if (accessibilitySettingStatus) {
                delay(500)
                currentStepIndex++

            }

            IntroStep.FINAL -> {
                // final step - no automatic progression
            }

            else -> {
                // This should not happen, but handle gracefully
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
                IntroStep.INTRO -> IntroStep(onNext = { currentStepIndex++ })
                IntroStep.TRUST -> TrustStep(onNext = { currentStepIndex++ })
                IntroStep.OVERLAY -> PermissionStep(
                    title = stringResource(id = R.string.permission_overlay),
                    permissionName = "SYSTEM_ALERT_WINDOW",
                    description = stringResource(id = R.string.permission_overlay_description),
                    icon = Icons.Outlined.PhoneAndroid,
                    isGranted = overlayPermissionStatus,
                    isLastStep = isLastStep,
                    onGrant = {
                        permissionManager.requestOverlayPermission(context, overlayLauncher)
                        coroutineScope.launch {
                            permissionManager.showOverlayPermissionTutorial()
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    },
                )

                IntroStep.NOTIFICATIONS -> PermissionStep(
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
                )

                IntroStep.SMS -> PermissionStep(
                    title = stringResource(id = R.string.permission_receive_sms),
                    permissionName = "RECEIVE_SMS",
                    description = stringResource(id = R.string.permission_receive_sms_description),
                    icon = Icons.Outlined.Sms,
                    isGranted = smsPermissionStatus,
                    isLastStep = isLastStep,
                    optionalDescription = "این دسترسی اختیاری است و بعدا هم قابل فعال‌سازی است.",
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
                )

                IntroStep.ACCESSIBILITY -> {
                    PermissionStep(
                        title = "سرویس دسترسی‌پذیری",
                        permissionName = "ACCESSIBILITY_SERVICE",
                        description = stringResource(id = R.string.permission_accessibility_short_description),
                        icon = Icons.Outlined.AccessibilityNew,
                        isGranted = accessibilitySettingStatus,
                        isLastStep = isLastStep,
                        optionalDescription = "این دسترسی اختیاری است و بعدا هم قابل فعال‌سازی است.",
                        onGrant = { showAccessibilityGuide = true },
                        onNext = {
                            currentStepIndex++
                        },
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

                IntroStep.FINAL -> FinalStep(onNext = { navigateToMainScreen() })
                else -> {
                    // This should not happen,
                }

            }

            if (showSmsSettingsDialog) {
                SmsSettingsDialog(

                    onEnableNormalSms = {
                        // Update the settings using the existing SettingsViewModel method
                        viewModel.updateNormalSmsNotificationSetting(true)
                        showSmsSettingsDialog = false
                        coroutineScope.launch {
                            delay(500)
                            currentStepIndex++
                        }
                    },


                    onDisableNormalSms = {
                        // Use the method you'll add to IntroViewModel
                        viewModel.updateNormalSmsNotificationSetting(false)
                        showSmsSettingsDialog = false
                        coroutineScope.launch {
                            delay(500)
                            currentStepIndex++
                        }
                    },
                    onDismiss = {
                        showSmsSettingsDialog = false
                        coroutineScope.launch {
                            delay(500)
                            currentStepIndex++
                        }
                    }
                )
            }
        }

        if (currentStep == IntroStep.INTRO || currentStep == IntroStep.FINAL) {
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