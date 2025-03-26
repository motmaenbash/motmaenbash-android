import android.Manifest
import android.os.Build
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
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants.PREF_KEY_INTRO_SHOWN
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppLogo
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.RedVariant
import nu.milad.motmaenbash.utils.PermissionManager
import nu.milad.motmaenbash.utils.dataStore
import nu.milad.motmaenbash.viewmodels.MainViewModel

@Composable
fun PermissionsIntroScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val permissionManager = remember { PermissionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    // Permission states
    val smsPermissionStatus by viewModel.smsPermissionStatus.collectAsState()
    val overlayPermissionStatus by viewModel.overlayPermissionStatus.collectAsState()
    val accessibilitySettingStatus by viewModel.accessibilitySettingStatus.collectAsState()
    val notificationPermissionStatus by viewModel.notificationPermissionStatus.collectAsState()

    // Store required permissions and get remaining ones
    val allPermissionSteps = remember {
        listOf(
            "intro" to true, // Always include intro as the first step
            "overlay" to overlayPermissionStatus,
            "notifications" to notificationPermissionStatus,
            "sms" to smsPermissionStatus,
            "accessibility" to accessibilitySettingStatus,
        )
    }

    // Filter out already granted permissions (except intro which should always be shown)
    val requiredPermissions = remember(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        listOf("intro") + allPermissionSteps.filter { it.first != "intro" && !it.second }
            .map { it.first }
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Get current permission step
    val currentStep = remember(requiredPermissions, currentStepIndex) {
        requiredPermissions.getOrNull(currentStepIndex) ?: "completed"
    }

    // Prevent returning to permission screen after navigating to main screen
    DisposableEffect(key1 = Unit) {
        val onBackPressedCallback = {
            if (currentStep == "accessibility" && accessibilitySettingStatus) {
                true // Consume back press when we've navigated to main screen
            } else {
                false
            }
        }
        onDispose {
            // Clean up if needed
        }
    }

    // Launchers
    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkOverlayPermission()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updatePermissionStatus(MainViewModel.PermissionType.NOTIFICATIONS, isGranted)
    }

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updatePermissionStatus(MainViewModel.PermissionType.SMS, isGranted)
    }

    // Accessibility guide dialog
    var showAccessibilityGuide by remember { mutableStateOf(false) }
    val accessibilitySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkAccessibilityPermission()
    }

    // Check initial permissions
    LaunchedEffect(Unit) {
        viewModel.checkOverlayPermission()
        viewModel.checkNotificationPermission()
        viewModel.checkAccessibilityPermission()
        viewModel.checkSmsPermission()
    }

    LaunchedEffect(
        overlayPermissionStatus,
        notificationPermissionStatus,
        smsPermissionStatus,
        accessibilitySettingStatus,
    ) {
        when (currentStep) {
            "intro" -> {
            }

            "overlay" -> if (overlayPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            "notifications" -> if (notificationPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            "sms" -> if (smsPermissionStatus) {
                delay(500)
                currentStepIndex++
            }

            "accessibility" -> if (accessibilitySettingStatus) {
                delay(500)
                navController.navigate(NavRoutes.MAIN_SCREEN) {
                    // Clear back stack so user can't go back to permission screen
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }
    }

    // Handle back button only when we're not on the first step
    BackHandler(enabled = currentStepIndex > 0 && currentStep != "accessibility") {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    // Get the appropriate icon for the current step
    remember(currentStep) {
        when (currentStep) {
            "overlay" -> Icons.Outlined.PhoneAndroid
            "notifications" -> Icons.Outlined.Notifications
            "sms" -> Icons.Outlined.Sms
            "accessibility" -> Icons.Outlined.AccessibilityNew
            else -> null
        }
    }

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
                "intro" -> {
                    // Intro
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogo(100.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "به «مطمئن باش» خوش آمدید",
                            style = typography.headlineSmall,
                            color = colorScheme.primary,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "برای شناسایی و هشدار درباره پیامک‌ها، برنامه‌ها و لینک‌های فیشینگ و کلاهبرداری، در مرحله‌های بعد دسترسی‌های مورد نیاز برنامه را فعال کنید.",
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
                            Text("بزن بریم")
                        }
                    }
                }

                "overlay" -> PermissionStep(
                    title = "نمایش پنجره روی سایر برنامه‌ها",
                    permissionName = "SYSTEM_ALERT_WINDOW",
                    description = "برای نمایش هشدارهای فوری هنگام شناسایی تهدید، نیاز به این دسترسی داریم",
                    icon = Icons.Outlined.PhoneAndroid,
                    isGranted = overlayPermissionStatus,
                    onGrant = {
                        permissionManager.requestOverlayPermission(context, overlayLauncher)
                        viewModel.checkOverlayPermission()
                        viewModel.viewModelScope.launch {
                            permissionManager.showOverlayPermissionTutorial()
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    }
                )

                "notifications" -> PermissionStep(
                    title = "دسترسی نمایش اعلان",
                    permissionName = "POST_NOTIFICATIONS",
                    description = "برای ارسال هشدارهای سریع در مورد تهدیدات امنیتی و فیشینگ، نیاز به این دسترسی داریم",
                    icon = Icons.Outlined.Notifications,
                    isGranted = notificationPermissionStatus,
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.updatePermissionStatus(
                                MainViewModel.PermissionType.NOTIFICATIONS,
                                true
                            )
                        }
                    },
                    onNext = {
                        currentStepIndex++
                    },
                )

                "sms" -> PermissionStep(
                    title = "دسترسی دریافت پیامک",
                    permissionName = "RECEIVE_SMS",
                    description = "برای شناسایی و مسدود کردن پیامک‌های فیشینگ و کلاهبرداری، نیاز به دسترسی خواندن پیامک داریم",
                    icon = Icons.Outlined.Sms,
                    isGranted = smsPermissionStatus,
                    onGrant = { smsLauncher.launch(Manifest.permission.RECEIVE_SMS) },
                    onNext = {
                        currentStepIndex++
                    }
                )

                "accessibility" -> {
                    PermissionStep(
                        title = "سرویس دسترسی‌پذیری",
                        permissionName = "ACCESSIBILITY_SERVICE",
                        description = "برای محافظت از شما در برابر برنامه‌های مخرب و شناسایی سایت‌های فیشینگ، به این دسترسی نیاز داریم",
                        icon = Icons.Outlined.AccessibilityNew,
                        isGranted = accessibilitySettingStatus,
                        onGrant = { showAccessibilityGuide = true },
                        content = {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Launch a coroutine to handle the suspend function
                                    coroutineScope.launch {
                                        val dataStore = context.dataStore

                                        // Mark as not first launch inside coroutine
                                        dataStore.edit { preferences ->
                                            preferences[booleanPreferencesKey(PREF_KEY_INTRO_SHOWN)] =
                                                true
                                        }

                                        // Navigate to main screen after preferences are updated
                                        navController.navigate(NavRoutes.MAIN_SCREEN) {
                                            // Clear back stack so user can't go back to permission screen
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreyDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("ورود به برنامه")
                            }
                        }
                    )

                    if (showAccessibilityGuide) {
                        PermissionGuideDialog(
                            permissionType = MainViewModel.PermissionType.ACCESSIBILITY,
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
        if (currentStep ==
            "intro"
        )
            DeveloperCredit()
    }
}


@Composable
fun AnimatedPermissionIcon(icon: ImageVector) {
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
            .size(64.dp)
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
            "توسعه توسط میلاد نوری",
            style = typography.bodySmall,
            color = GreyMiddle,

            )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Heart",
                modifier = Modifier
                    .size(18.dp)
                    .scale(scale),
                tint = RedVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "برای مردم ایران",
                fontWeight = FontWeight.Bold, color = GreyMiddle,

                style = typography.bodySmall,
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
    icon: ImageVector? = null,
    onGrant: () -> Unit,
    onNext: (() -> Unit)? = null,
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
                fontSize = 12.sp
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

        if (onNext != null) {
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(
                onClick = onNext,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorScheme.primary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("گام بعدی")
                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Outlined.KeyboardDoubleArrowLeft,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )


                }
            }
        }

        // Custom content section
        content?.invoke()

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
                        title = "نمایش پنجره روی سایر برنامه‌ها",
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