package nu.milad.motmaenbash.ui.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.models.Alert.Companion.SMS_ALERT_TYPES
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.components.ExpandablePermissionContent
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.ui.theme.YellowDark
import nu.milad.motmaenbash.utils.AlertUtils
import nu.milad.motmaenbash.utils.AlertUtils.getAlertContent
import nu.milad.motmaenbash.utils.PackageUtils

class AlertHandlerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALERT = "extra_alert"
    }


    private lateinit var alert: Alert

    private val uninstallLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "برنامه '${alert.param2}' با موفقیت حذف شد", Toast.LENGTH_SHORT)
                .show()
            finishAndRemoveTask()
        } else {
            Toast.makeText(this, "حذف برنامه '${alert.param2}' لغو شد", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Extract intent extras
        alert = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_ALERT, Alert::class.java)
                ?: throw IllegalStateException("Alert extra is required")
        } else {
            @Suppress("DEPRECATION")
            (intent.getParcelableExtra(EXTRA_ALERT) as? Alert)
                ?: throw IllegalStateException("Alert extra is required")
        }


        val taskLabel = when (alert.type) {
            Alert.AlertType.SMS_NEUTRAL -> "پیام جدید"
            else -> "هشدار!"
        }

        val taskDescription = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityManager.TaskDescription(taskLabel)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityManager.TaskDescription(taskLabel, null, getColor(R.color.red))
        } else {
            @Suppress("DEPRECATION")
            ActivityManager.TaskDescription(
                taskLabel,
                null,
                resources.getColor(R.color.red)
            )
        }


        setTaskDescription(
            taskDescription
        )

        setContent {
            MotmaenBashTheme {
                AlertDialog(
                    alert = Alert(
                        type = alert.type,
                        level = alert.level,
                        title = alert.title,
                        summary = alert.summary,
                        content = alert.content,
                        param1 = alert.param1,
                        param2 = alert.param2,
                        param3 = alert.param3
                    ),

                    onDismiss = { finishAndRemoveTask() },
                    onUninstall = {
                        PackageUtils.uninstallApp(alert.param1).let {
                            uninstallLauncher.launch(it)
                        }
                    },
                    context = this@AlertHandlerActivity
                )
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
//        audioHelper.release()
    }
}


@Composable
fun AlertDialog(
    context: Context,
    alert: Alert,
    onDismiss: () -> Unit,
    onUninstall: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val alertColor = when (alert.level) {
            Alert.AlertLevel.WARNING -> YellowDark
            Alert.AlertLevel.ALERT -> Red
            Alert.AlertLevel.NEUTRAL -> GreyDark
            Alert.AlertLevel.INFO -> GreyDark
        }

        val alertLevelLabel = when (alert.level) {
            Alert.AlertLevel.WARNING -> "اخطار"
            Alert.AlertLevel.ALERT -> "هشدار"
            Alert.AlertLevel.NEUTRAL -> null
            Alert.AlertLevel.INFO -> "اطلاعیه"
        }
        val hint = AlertUtils.getAlertHint(alert.type)
        val scrollState = rememberScrollState()

        AppCard(
            padding = 16.dp,
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with badge and close button
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
                                color = alertColor,
                                shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (alert.type == Alert.AlertType.SMS_NEUTRAL) Icons.Outlined.Textsms else Icons.Outlined.GppBad,
                            contentDescription = alertLevelLabel,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = alert.title,
                            color = colorScheme.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(top = 8.dp)
                    ) {
                        Icon(

                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = GreyMiddle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

//                // Alert title
//                Text(
//                    text = alert.title,
//                    color = alertColor,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 8.dp)
//                )

//                Divider(verticalPadding = 4.dp, color = alertColor)
                if (alert.summary != null) {
                    Text(
                        text = alert.summary,
                        color = Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    Divider(verticalPadding = 4.dp)
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                ) {
                    // Alert message if present
                    if (!alert.content.isNullOrBlank()) {
                        Text(
                            text = alert.content,
                            color = colorScheme.onSurface,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,

                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    Divider(verticalPadding = 4.dp)
                    // Content based on alert type
                    when (alert.type) {
                        in SMS_ALERT_TYPES -> {
                            SmsAlertContent(context, alert)
                        }

                        Alert.AlertType.APP_FLAGGED, Alert.AlertType.APP_RISKY_INSTALL -> {
                            AppAlertContent(context, alert.param1, alert.param2, alert.param3)
                        }

                        else -> {
                            DefaultActionButton(onDismiss)
                        }
                    }
                }

                if (!hint.isNullOrBlank()) {
                    Divider(verticalPadding = 4.dp)

                    Text(
                        text = hint,
//                        color = colorScheme.onSurface,
                        color = alertColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                    )
                }


                if (alert.type == Alert.AlertType.APP_FLAGGED || alert.type == Alert.AlertType.APP_RISKY_INSTALL) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onUninstall?.invoke() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = alertColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "حذف سریع این برنامه",
                            color = White,
//                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                if (alert.type == Alert.AlertType.SMS_NEUTRAL) {
                    Spacer(modifier = Modifier.height(18.dp))
                } else {
                    AlertFooter(alert.type)
                }
            }
        }
    }
}


@Composable
fun SmsAlertContent(
    context: Context,
    alert: Alert,
) {
    val sender: String = alert.param1
    val messageText: String? = alert.param2

    Text(
        text = buildAnnotatedString {
            append("فرستنده: ")
            withStyle(style = SpanStyle(color = if (alert.type == Alert.AlertType.SMS_NEUTRAL) colorScheme.primary else colorScheme.onSurface)) {
                append("\u200E$sender") // Ensure proper LTR rendering for sender (\u200E is the LTR mark)
            }
        },

        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .wrapContentSize()
            .then(
                if (alert.type == Alert.AlertType.SMS_NEUTRAL) {
                    Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = "sms:$sender".toUri()
                        }
                        context.startActivity(intent)

                    }
                } else {
                    Modifier
                }
            )
    )

    messageText?.let {
        Text(
            text = "متن پیامک:",
            color = colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .padding(2.dp)
                .clip(RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.Start
        ) {
            SelectionContainer {
                Text(
                    text = it.replace(Regex("\n{3,}"), "\n\n").trim(),
                    color = colorScheme.onSurface,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.background)
                        .padding(8.dp)
                )
            }
        }
    }

}

@Composable
private fun AppAlertContent(
    context: Context,
    packageName: String,
    appName: String?,
    permissionCombinationDescription: String?
) {
    val appInfo = runCatching {
        context.packageManager.getApplicationInfo(packageName, 0)
    }.getOrNull()


    if (permissionCombinationDescription != null) {

        val descriptions = permissionCombinationDescription.split("\n").filter { it.isNotBlank() }
        if (descriptions.isNotEmpty()) {
            ExpandablePermissionContent(
                descriptions = descriptions,
                modifier = Modifier.padding(4.dp)
            )
        }
    }

    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        appInfo?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    appName?.let {
                        Text(
                            text = it,
                            color = colorScheme.onSurface,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "نام بسته: $packageName",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = it.loadIcon(context.packageManager)
                    ),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

            }
        }
    }

}

@Composable
private fun DefaultActionButton(onDismiss: () -> Unit) {
    Button(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(48.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "متوجه شدم",
            color = White,
            fontSize = 13.sp,
//            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun AlertFooter(alertType: Alert.AlertType) {
    val protectionType = when (alertType) {
        in SMS_ALERT_TYPES -> "سپر پیامک"
        Alert.AlertType.APP_FLAGGED, Alert.AlertType.APP_RISKY_INSTALL -> "سپر برنامه"
        else -> "سپر امنیتی"
    }

    Divider(verticalPadding = 4.dp)

    Text(
        buildAnnotatedString {
            append("شناسایی شده توسط ")
            withStyle(
                style = SpanStyle(
                    color = GreyMiddle, fontWeight = FontWeight.Bold
                )
            ) {
                append(protectionType)
            }
            withStyle(
                style = SpanStyle(
                    color = colorScheme.primary, fontWeight = FontWeight.Bold
                )
            ) {
                append(" «مطمئن باش»")
            }
        },
        fontSize = 12.sp,
        color = GreyDark,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
    )
}

class AllAlertTypesPreviewParameterProvider : PreviewParameterProvider<Alert.AlertType> {
    override val values = sequenceOf(
        Alert.AlertType.SMS_SENDER_FLAGGED,
        Alert.AlertType.SMS_LINK_FLAGGED,
        Alert.AlertType.SMS_KEYWORD_FLAGGED,
        Alert.AlertType.SMS_PATTERN_FLAGGED,
        Alert.AlertType.APP_FLAGGED,
        Alert.AlertType.URL_FLAGGED,
        Alert.AlertType.APP_RISKY_INSTALL,

        )
}

@Composable
fun createAlertFromType(
    alertType: Alert.AlertType
): Alert {
    // Get alert content from AlertUtils
    val (alertTitle, alertSummary, alertContent) = getAlertContent(alertType)

    // Define appropriate parameters based on alert type
    val (param1, param2) = when (alertType) {
        Alert.AlertType.SMS_SENDER_FLAGGED -> "3000123456" to "این پیامک از یک فرستنده مشکوک است."
        Alert.AlertType.SMS_LINK_FLAGGED -> "09123456789" to "این پیامک حاوی لینک مشکوک است: https://malicious-link.com"
        Alert.AlertType.SMS_KEYWORD_FLAGGED -> "09123456789" to "این پیامک حاوی کلمات کلیدی مشکوک است."
        Alert.AlertType.SMS_PATTERN_FLAGGED -> "09123456789" to "این پیامک دارای الگوی مشکوک است."
        Alert.AlertType.SMS_NEUTRAL -> "09123456789" to "این یک پیامک عادی است."
        Alert.AlertType.APP_FLAGGED -> "com.malicious.app" to "نام برنامه مخرب"
        Alert.AlertType.URL_FLAGGED -> "https://malicious-site.com" to "این آدرس مشکوک است."
        Alert.AlertType.APP_RISKY_INSTALL -> "com.malicious.app" to "نام برنامه مخرب"

    }

    // Define alert level based on type
    val level = when (alertType) {
        Alert.AlertType.SMS_SENDER_FLAGGED -> Alert.AlertLevel.WARNING
        Alert.AlertType.SMS_LINK_FLAGGED -> Alert.AlertLevel.ALERT
        Alert.AlertType.SMS_KEYWORD_FLAGGED -> Alert.AlertLevel.WARNING
        Alert.AlertType.SMS_PATTERN_FLAGGED -> Alert.AlertLevel.WARNING
        Alert.AlertType.SMS_NEUTRAL -> Alert.AlertLevel.NEUTRAL
        Alert.AlertType.APP_FLAGGED -> Alert.AlertLevel.ALERT
        Alert.AlertType.URL_FLAGGED -> Alert.AlertLevel.ALERT
        Alert.AlertType.APP_RISKY_INSTALL -> Alert.AlertLevel.WARNING

    }

    return Alert(
        type = alertType,
        level = level,
        title = alertTitle,
        summary = alertSummary,
        content = alertContent,
        param1 = param1,
        param2 = param2
    )
}

@Preview(showBackground = true, name = "همه انواع هشدارها")
@Composable
fun AllAlertTypesPreview(
    @PreviewParameter(AllAlertTypesPreviewParameterProvider::class) alertType: Alert.AlertType
) {
    val context = LocalContext.current
    val alert = createAlertFromType(alertType)

    MotmaenBashTheme {
        AlertDialog(
            alert = alert,
            onDismiss = {},
            onUninstall = if (alertType == Alert.AlertType.APP_FLAGGED) {
                {}
            } else null,
            context = context
        )
    }
}

@Preview(showBackground = true, name = "هشدار پیامک فرستنده مشکوک")
@Composable
fun SmsSenderFlaggedPreview() {
    val context = LocalContext.current
    val alertType = Alert.AlertType.SMS_SENDER_FLAGGED
    val (title, summary, message) = getAlertContent(alertType)

    MotmaenBashTheme {
        AlertDialog(
            alert = Alert(
                type = alertType,
                level = Alert.AlertLevel.WARNING,
                title = title,
                summary = summary,
                content = message,
                param1 = "3000123456",
                param2 = "سلام. این متن یک پیامک آزمایشی است.",
            ),
            onDismiss = {},
            context = context
        )
    }
}

@Preview(showBackground = true, name = "هشدار پیامک لینک مشکوک")
@Composable
fun SmsLinkFlaggedPreview() {
    val context = LocalContext.current
    val alertType = Alert.AlertType.SMS_LINK_FLAGGED
    val (title, summary, message) = getAlertContent(alertType)

    MotmaenBashTheme {
        AlertDialog(
            alert = Alert(
                type = alertType,
                level = Alert.AlertLevel.ALERT,
                title = title,
                summary = summary,
                content = message,
                param1 = "09123456789",
                param2 = "این پیامک حاوی لینک مشکوک است: https://malicious-link.com",
            ),
            onDismiss = {},
            context = context
        )
    }
}

@Preview(showBackground = true, name = "هشدار برنامه مشکوک")
@Composable
fun AppFlaggedPreview() {
    val context = LocalContext.current
    val alertType = Alert.AlertType.APP_FLAGGED
    val (title, summary, message) = getAlertContent(alertType)

    MotmaenBashTheme {
        AlertDialog(
            alert = Alert(
                type = alertType,
                level = Alert.AlertLevel.ALERT,
                title = title,
                summary = summary,
                content = message,
                param1 = "com.malicious.app.web.messenger",
                param2 = "نام برنامه مخرب",
            ),
            onDismiss = {},
            onUninstall = {},
            context = context
        )
    }
}

@Preview(showBackground = true, name = "هشدار آدرس مشکوک")
@Composable
fun UrlFlaggedPreview() {
    val context = LocalContext.current
    val alertType = Alert.AlertType.URL_FLAGGED
    val (title, summary, message) = getAlertContent(alertType)

    MotmaenBashTheme {
        AlertDialog(
            alert = Alert(
                type = alertType,
                level = Alert.AlertLevel.ALERT,
                title = title,
                summary = summary,
                content = message,
                param1 = "https://malicious-site.com",
                param2 = "این آدرس مشکوک است.",
            ),
            onDismiss = {},
            context = context
        )
    }
}