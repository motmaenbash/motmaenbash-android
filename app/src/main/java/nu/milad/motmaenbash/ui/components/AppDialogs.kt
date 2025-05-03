package nu.milad.motmaenbash.ui.components

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.models.AppUpdate
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.WebUtils

/**
 * Collection of dialogs used throughout the application for various purposes
 * including permissions guidance, app updates, and information dialogs.
 */

/**
 * Dialog for app updates notification
 *
 * @param updateDialogState Current state of the update dialog
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun UpdateDialog(
    updateDialogState: AppUpdate,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AppAlertDialog(
        title = "نسخه جدید برنامه (${updateDialogState.latestVersionName})",
        icon = Icons.Outlined.Update,
        message = if (updateDialogState.forceUpdate)
            stringResource(R.string.app_update_message_force)
        else
            stringResource(R.string.app_update_message),
        links = updateDialogState.links,
        onConfirm = { WebUtils.openUrl(context, updateDialogState.links.first().second) },
        onDismiss = onDismiss,
        confirmText = if (updateDialogState.forceUpdate) "به‌روزرسانی" else null,
        dismissText = if (updateDialogState.forceUpdate) null else "بعدا",
    )
}


@Composable
fun ChangelogDialog(
    onDismiss: () -> Unit
) {
    val versionTitle = stringArrayResource(id = R.array.changelog_versions)[0]

    AppAlertDialog(
        title = "تغییرات نسخه جدید",
        icon = Icons.Outlined.Update,
        content = {
            Text(
                versionTitle,
                style = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Divider(verticalPadding = 6.dp)
            Text(
                text = stringArrayResource(id = R.array.changelog_changes)[0],
                style = typography.bodySmall,
            )
        },
        onDismiss = onDismiss,
        dismissText = "بزن بریم",
    )

}

/**
 * Dialog showing information about app protectors
 *
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun GuardsInfoDialog(onDismiss: () -> Unit) {
    AppAlertDialog(
        title = stringResource(R.string.guards_functionality),
        icon = Icons.Outlined.VerifiedUser,
        content = {
            GuardInfoItem(
                title = stringResource(R.string.guard_sms),
                functionality = stringResource(R.string.guard_sms_description),
                requirements = stringResource(R.string.permission_receive_sms),
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuardInfoItem(
                title = stringResource(R.string.guard_web),
                functionality = stringResource(R.string.guard_web_description),
                requirements = stringResource(R.string.permission_accessibility),
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuardInfoItem(
                title = stringResource(R.string.guard_gateway),
                functionality = stringResource(R.string.guard_gateway_description),
                requirements = stringResource(R.string.permission_accessibility),
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuardInfoItem(
                title = stringResource(R.string.guard_app),
                functionality = stringResource(R.string.guard_app_description),
                requirements = stringResource(R.string.permission_query_packages),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "برای محافظت کامل، پیشنهاد می‌شود تمامی سپرها و دسترسی‌های لازم را فعال نگه دارید. ",
                style = typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )

        },
        confirmText = "متوجه شدم",
        onConfirm = onDismiss,
        dismissText = null,
        onDismiss = { /* No action needed */ }
    )
}

/**
 * Reusable component for protector info sections
 */
@Composable
private fun GuardInfoItem(
    title: String,
    requirements: String,
    functionality: String
) {
    Text(
        "$title:",
        style = typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = colorScheme.primary
    )

    Column(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Text(
            functionality,
            color = colorScheme.onSurface,
            style = typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.background)
                .padding(8.dp)
        )
    }
    Text(
        "• دسترسی: $requirements",
        style = typography.bodySmall,
    )

}

/**
 * Section: Permission Guide Dialogs
 */

/**
 * Dialog for notification permission guidance
 *
 * @param onConfirm Callback when user confirms to go to settings
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(id = R.string.permission_notification),
        icon = Icons.Outlined.Notifications,
        content = {
            Text(
                stringResource(id = R.string.permission_notification_description),
                style = typography.bodyMedium
            )
            Divider(verticalPadding = 12.dp)
            Text(
                "شما قبلا این دسترسی را رد کرده‌اید. برای فعال‌سازی نمایش اعلان، به صفحه تنظیمات برنامه بروید و این دسترسی را فعال کنید.",
                style = typography.bodySmall
            )
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "برو به تنظیمات",
        dismissText = "لغو"
    )
}

/**
 * Dialog for SMS permission guidance
 *
 * @param onConfirm Callback when user confirms to go to settings
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun SmsPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(id = R.string.permission_receive_sms),
        icon = Icons.Outlined.Sms,
        content = {
            Text(
                stringResource(id = R.string.permission_receive_sms_description),
                style = typography.bodySmall
            )
            Divider(verticalPadding = 12.dp)
            Text(
                "شما قبلا این دسترسی را رد کرده‌اید. برای فعال‌سازی سپر پیامک، به صفحه تنظیمات برنامه بروید و این دسترسی را فعال کنید.",
                style = typography.bodySmall
            )
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "برو به تنظیمات",
        dismissText = "لغو"
    )
}

/**
 * Dialog for accessibility permission guidance
 *
 * @param onConfirm Callback when user confirms to go to settings
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun AccessibilityPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = "راهنمای فعال‌سازی",
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        content = {
            Text(
                stringResource(R.string.permission_accessibility_description),
                style = typography.bodySmall,
            )
            Divider(verticalPadding = 12.dp)
            Text(
                "مراحل فعال‌سازی:",
                style = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            // Device-specific instructions
            when {
                Build.MANUFACTURER.lowercase().contains("samsung") -> {
                    Text(
                        "1. وارد تنظیمات دستگاه شوید.",
                        style = typography.bodySmall,
                    )
                    Text(
                        "2. بخش «دسترسی‌پذیری» (Accessibility) را انتخاب کنید.",
                        style = typography.bodySmall,
                    )
                    Text(
                        "3. بخش «برنامه های نصب شده» یا «Downloaded Services» را انتخاب کنید.",
                        style = typography.bodySmall,
                    )
                    Text(
                        "4. برنامه «مطمئن باش» را پیدا کرده و آن را فعال کنید.",
                        style = typography.bodySmall,
                    )
                }

                else -> {
                    Text(
                        "1. وارد تنظیمات دستگاه شوید.", style = typography.bodySmall,
                    )
                    Text(
                        "2. بخش «دسترسی‌پذیری» (Accessibility) را انتخاب کنید.",
                        style = typography.bodySmall,
                    )
                    Text(
                        "3. گزینه «مطمئن باش» را پیدا کرده و آن را فعال کنید.",
                        style = typography.bodySmall,
                    )
                }
            }

            Divider(verticalPadding = 12.dp)
            Text(
                text = stringResource(R.string.permission_settings_may_vary),
                fontWeight = FontWeight.Bold,
                style = typography.bodySmall,
            )
        },
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "برو به تنظیمات",
        dismissText = "لغو"
    )
}

/**
 * Dialog for overlay permission guidance with animated switch
 */
@Composable
fun OverlayPermissionDialog() {
    val activity = LocalActivity.current
    var animationCounter by remember { mutableIntStateOf(0) }
    val animatedChecked by remember { derivedStateOf { animationCounter % 2 == 1 } }

    LaunchedEffect(Unit) {
        repeat(5) {
            delay(500)
            animationCounter++
        }
    }

    AppAlertDialog(
        title = "راهنمای فعال‌سازی",
        icon = Icons.Outlined.PhoneAndroid,
        content = {
            Text(
                " برنامه مطمئن باش برای نمایش هشدارهای امنیتی مثل هشدار پیامک فیشینگ، شناسایی برنامه مخرب و... " +
                        "، به دسترسی \"نمایش پنجره روی دیگر برنامه‌ها\" نیاز دارد.",
                style = typography.bodySmall,
                color = colorScheme.onBackground
            )

            Divider(verticalPadding = 12.dp)

            Text(
                "مراحل فعال‌سازی:",
                style = typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )
            Text(
                text = "۱. بعد از بستن این پنجره راهنما، در لیست برنامه‌های نمایش داده شده، برنامه مطمئن باش (Motmaen Bash) را پیدا کنید.",
                style = typography.bodySmall,
                color = colorScheme.onBackground
            )

            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.background)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Motmaen Bash",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "MotmaenBash Logo",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = "۲. گزینه «نمایش پنجره روی دیگر برنامه‌ها» یا Display over other apps را فعال کنید.",
                style = typography.bodySmall,
            )

            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.background)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = animatedChecked,
                        onCheckedChange = null,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Display over other apps",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }

            Divider(verticalPadding = 12.dp)
            Text(
                text = stringResource(R.string.permission_settings_may_vary),
                fontWeight = FontWeight.Bold,
                style = typography.bodySmall,
            )
        },
        onConfirm = { activity?.finish() },
        confirmText = "متوجه شدم",
        dismissText = null,
        onDismiss = { /* No action needed */ }
    )
}

/**
 * Dialog for critical permissions information
 *
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun CriticalPermissionsInfoDialog(onDismiss: () -> Unit) {
    AppAlertDialog(
        title = "دسترسی‌های لازم",
        icon = Icons.Outlined.Info,
        content = {
            Column {


                // Notification permission section
                Text(
                    stringResource(id = R.string.permission_notification) + ":",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Text(
                        stringResource(id = R.string.permission_notification_description),
                        style = typography.bodySmall,
                    )

                } else {
                    Text(
                        "• کاربرد: نمایش هشدارها و اطلاعیه‌های امنیتی",
                        style = typography.bodySmall,
                    )
                    Text(
                        "• ضرورت: برای دریافت هشدارهای مهم امنیتی توصیه می‌شود",
                        style = typography.bodySmall,
                    )
                }

                Divider(verticalPadding = 12.dp)

                // Overlay permission section
                Text(
                    stringResource(id = R.string.permission_overlay) + ":",
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
                Text(
                    stringResource(id = R.string.permission_overlay_description),
                    style = typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(12.dp))


//                todo: delete
//                Text(
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        "این دو دسترسی برای عملکرد کامل برنامه ضروری هستند و بدون آنها، توانایی برنامه در محافظت از دستگاه شما به طور قابل توجهی کاهش می‌یابد."
//                    } else {
//                        "دسترسی 'نمایش روی برنامه‌های دیگر' برای عملکرد کامل برنامه ضروری است. دسترسی نمایش اعالن برای نسخه اندروید شما الزامی نیست، اما برای دریافت هشدارها توصیه می‌شود."
//                    },
//                    style = typography.bodySmall,
//                    fontWeight = FontWeight.Bold,
//                )


            }
        },
        confirmText = "متوجه شدم",
        onConfirm = onDismiss,
        dismissText = null,
        onDismiss = { /* No action needed */ }
    )
}

/**
 * Dialog showing information about fonts used in the app
 *
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun FontInfoDialog(onDismiss: () -> Unit) {
    AppAlertDialog(
        title = "درباره فونت‌ها",
        icon = Icons.Outlined.DriveFileRenameOutline,
        onDismiss = onDismiss,
        dismissText = "بستن",
        message = "فونت‌های وزیر متن و ساحل توسط مرحوم صابر راستی‌کردار طراحی شده‌اند.\nروحش شاد و یادش گرامی 🖤 ",
    )
}


// PREVIEW SECTION
@Preview(showBackground = true)
@Composable
fun UpdateDialogPreview() {
    MotmaenBashTheme {
        // Regular update state
        val mockUpdateState = AppUpdate(
            latestVersionName = "2.1.0",
            forceUpdate = false,
            links = listOf(
                "دانلود از گوگل پلی" to "https://play.google.com",
                "دانلود از سایت" to "https://motmaenbash.ir"
            )
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            UpdateDialog(
                updateDialogState = mockUpdateState,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateDialogForceUpdatePreview() {
    MotmaenBashTheme {
        // Force update state
        val mockUpdateState = AppUpdate(
            latestVersionName = "2.1.0",
            forceUpdate = true,
            links = listOf("دانلود از سایت" to "https://motmaenbash.ir")
        )
        Surface(modifier = Modifier.fillMaxSize()) {
            UpdateDialog(
                updateDialogState = mockUpdateState,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangelogDialogPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ChangelogDialog(
                onDismiss = {}
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GuardsInfoDialogPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GuardsInfoDialog(onDismiss = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionGuideDialogAccessibilityPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AccessibilityPermissionDialog(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionGuideDialogOverlayPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OverlayPermissionDialog()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionGuideDialogNotificationPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationPermissionDialog(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionGuideDialogSmsPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SmsPermissionDialog(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CriticalPermissionsInfoDialogPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CriticalPermissionsInfoDialog(onDismiss = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FontInfoDialogPreview() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FontInfoDialog(onDismiss = {})
        }
    }
}

