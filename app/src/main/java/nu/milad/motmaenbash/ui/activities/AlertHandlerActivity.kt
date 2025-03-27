package nu.milad.motmaenbash.ui.activities


import android.app.ActivityManager
import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.ui.graphics.Color.Companion.Black
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
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_APP_FLAGGED
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_SMS_KEYWORD_FLAGGED
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_SMS_LINK_FLAGGED
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_SMS_PATTERN_FLAGGED
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_SMS_SAFE
import nu.milad.motmaenbash.ui.activities.AlertHandlerActivity.Companion.ALERT_TYPE_SMS_SENDER_FLAGGED
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.RowDivider
import nu.milad.motmaenbash.ui.theme.GreenDark
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.GreyMiddle
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.ui.theme.Yellow
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.utils.dataStore
import nu.milad.motmaenbash.viewmodels.SettingsViewModel

class AlertHandlerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALERT_TITLE = "extra_alert_title"
        const val EXTRA_ALERT_SUB_TITLE = "extra_alert_sub_title"
        const val EXTRA_ALERT_MESSAGE = "extra_alert_message"
        const val EXTRA_ALERT_INFO = "extra_alert_info"
        const val EXTRA_ALERT_TYPE = "extra_alert_type"
        const val EXTRA_ALERT_LEVEL = "extra_alert_lever"

        const val EXTRA_PARAM1 = "extra_param1"
        const val EXTRA_PARAM2 = "extra_param2"

        // SMS
        const val ALERT_TYPE_SMS_SENDER_FLAGGED = 1
        const val ALERT_TYPE_SMS_LINK_FLAGGED = 2
        const val ALERT_TYPE_SMS_KEYWORD_FLAGGED = 3
        const val ALERT_TYPE_SMS_PATTERN_FLAGGED = 4
        const val ALERT_TYPE_SMS_SAFE = 5 // No flagged content detected

        // App
        const val ALERT_TYPE_APP_FLAGGED = 6

        // URL
        const val ALERT_TYPE_URL_FLAGGED = 7
        const val ALERT_TYPE_DOMAIN_FLAGGED = 8

        // Alert levels
        enum class AlertLevel {
            NORMAL, WARNING, ERROR
        }

        // Sound types
        const val SOUND_TYPE_DING1 = "ding1"

    }

    private var alertSound: String = SOUND_TYPE_DING1

    private var playSoundInSilentMode: Boolean = false

    private var param1: String = ""
    private var param2: String = ""

    private lateinit var audioHelper: AudioHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioHelper = AudioHelper(this)

        // Get user preferences
        lifecycleScope.launch {
            val preferences = applicationContext.dataStore.data.first()
            alertSound = preferences[SettingsViewModel.ALERT_SOUND] ?: SOUND_TYPE_DING1
            val playInSilentPref =
                preferences[SettingsViewModel.PLAY_SOUND_IN_SILENT_MODE] ?: "false"
            playSoundInSilentMode = playInSilentPref == "true"
            // Vibrate when alert is shown
            audioHelper.vibrateDevice(this@AlertHandlerActivity)

            // Play sound using SoundPlayer
            audioHelper.playSound(alertSound, playSoundInSilentMode)
        }


        val taskDescription = ActivityManager.TaskDescription(
            getString(R.string.alert_dialog_activity_title), null, TRANSPARENT
        )
        setTaskDescription(taskDescription)

        param1 = intent.getStringExtra(EXTRA_PARAM1).orEmpty()
        param2 = intent.getStringExtra(EXTRA_PARAM2).orEmpty()
        val subTitle = intent.getStringExtra(EXTRA_ALERT_SUB_TITLE)
        val alertType = intent.getIntExtra(EXTRA_ALERT_TYPE, 0)
        val alertLevelString = intent.getStringExtra(EXTRA_ALERT_LEVEL)

        val (title, message) = getAlertContent(alertType, param1, param2)

        val alertLevel = when (alertLevelString) {
            AlertLevel.NORMAL.toString() -> AlertLevel.NORMAL
            AlertLevel.WARNING.toString() -> AlertLevel.WARNING
            AlertLevel.ERROR.toString() -> AlertLevel.ERROR
            else -> AlertLevel.NORMAL
        }

        setContent {
            MotmaenBashTheme {

                AlertDialog(
                    alertType = alertType,
                    alertLevel = alertLevel,
                    title = title,
                    subTitle = subTitle,
                    message = message,
                    param1 = param1,
                    param2 = param2,
                    onDismiss = { finishAndRemoveTask() },
                    onUninstall = {
                        val intent = PackageUtils.uninstallApp(this, param1)
                        uninstallLauncher.launch(intent)
                    },
                    context = this
                )

            }
        }
    }


    private fun getAlertContent(
        alertType: Int, param1: String, param2: String
    ): Pair<String, String> {
        return when (alertType) {
            ALERT_TYPE_SMS_SENDER_FLAGGED -> Pair(
                "شناسایی فرستنده مشکوک!",
                "این فرستنده در لیست سیاه قرار دارد." + "\n\nفرستنده پیامک: $param1"
            )

            ALERT_TYPE_SMS_LINK_FLAGGED -> Pair(
                "شناسایی لینک مشکوک!", "\n\nفرستنده پیامک: $param1"
            )

            ALERT_TYPE_SMS_KEYWORD_FLAGGED -> Pair(
                "کلمه کلیدی مشکوک!", "\n\nفرستنده پیامک: $param1"
            )

            ALERT_TYPE_SMS_PATTERN_FLAGGED -> Pair(
                "شناسایی پیامک مشکوک!", "\n\nفرستنده پیامک: $param1"
            )

            ALERT_TYPE_SMS_SAFE -> Pair(
                "پیامک امن", "محتوای مشکوکی شناسایی نشد." + "\n\nفرستنده پیامک: $param1"
            )

            ALERT_TYPE_APP_FLAGGED -> Pair(
                "برنامه مشکوک!",
                "برنامه «$param2» یک برنامه مخرب و بدافزار است."
            )

            ALERT_TYPE_URL_FLAGGED -> Pair(
                "آدرس مشکوک!", "این آدرس در لیست سیاه قرار دارد."
            )

            ALERT_TYPE_DOMAIN_FLAGGED -> Pair(
                "دامنه مشکوک!", "این دامنه در لیست سیاه قرار دارد."
            )

            else -> Pair(
                "خطا", "نوع هشدار نامشخص."
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioHelper.release()
    }


    // Activity result launcher for uninstall
    private val uninstallLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "برنام '${param2}' با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
            finishAndRemoveTask()
        } else {
            Toast.makeText(this, "حذف برنامه '${param2}' لغو شد", Toast.LENGTH_SHORT).show()
        }
    }


}

@Composable
fun AlertDialog(
    context: Context,
    alertType: Int,
    alertLevel: AlertHandlerActivity.Companion.AlertLevel,
    title: String,
    subTitle: String?,
    message: String,
    param1: String,
    param2: String? = "",
    onDismiss: () -> Unit,
    onUninstall: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false
        )
    ) {
        val badgeColor = when (alertLevel) {
            AlertHandlerActivity.Companion.AlertLevel.NORMAL -> GreenDark
            AlertHandlerActivity.Companion.AlertLevel.WARNING -> Yellow
            AlertHandlerActivity.Companion.AlertLevel.ERROR -> Red
        }

        AppCard(
            padding = 16.dp,
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    val (badgeIcon, contentDescription) = when (alertLevel) {
                        AlertHandlerActivity.Companion.AlertLevel.NORMAL ->
                            Icons.Outlined.Check to "عادی"

                        AlertHandlerActivity.Companion.AlertLevel.WARNING ->
                            Icons.Outlined.ErrorOutline to "هشدار"

                        AlertHandlerActivity.Companion.AlertLevel.ERROR ->
                            Icons.Outlined.ErrorOutline to "خطا"
                    }

                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = badgeColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = contentDescription,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier
                                .size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = title,
                            color = colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }



                    IconButton(
                        onClick = onDismiss, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = GreyMiddle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = title,
                    color = badgeColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )


                RowDivider(verticalPadding = 0.dp)


                if (!subTitle.isNullOrEmpty()) {
                    Text(
                        text = subTitle,
                        color = Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                val scrollState = rememberScrollState()

                Text(
                    text = message,
                    color = colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .verticalScroll(scrollState)
                )

                if (alertType in listOf(
                        ALERT_TYPE_SMS_SENDER_FLAGGED,
                        ALERT_TYPE_SMS_LINK_FLAGGED,
                        ALERT_TYPE_SMS_KEYWORD_FLAGGED,
                        ALERT_TYPE_SMS_PATTERN_FLAGGED,
                        ALERT_TYPE_SMS_SAFE
                    )
                ) {


                    if (param2 != null) {
                        Text(
                            text = "متن پیامک:",
                            color = colorScheme.onSurface,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            horizontalAlignment = Alignment.Start

                        ) {

                            Text(
                                text = param2,
                                color = colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.background)
                                    .padding(8.dp)


                            )

                        }
                    }

                    Text(
                        text = " لطفا بدون اینکه پیامک را باز کنید، سریعا آن را حذف کنید.",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )

                } else if (alertType == ALERT_TYPE_APP_FLAGGED) {
                    // Display app icon and name for app alerts
                    val appInfo = run {
                        val pm = context.packageManager
                        try {
                            pm.getApplicationInfo(param1, 0)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (appInfo != null) {


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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (param2 != null) {
                                        Text(
                                            text = param2,
                                            color = colorScheme.onSurface,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Text(
                                        text = param1,
                                        color = colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = appInfo.loadIcon(context.packageManager)
                                    ),
                                    contentDescription = "MotmaenBash Logo",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Text(
                        text = " لطفا بدون اینکه برنامه را باز کنید، سریعا آن را حذف کنید.",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onUninstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    {
                        Text(
                            text = "حذف سریع این برنامه",
                            color = White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "متوجه شدم",
                            color = White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }


            }


            val protectionType = when (alertType) {
                AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED, AlertHandlerActivity.ALERT_TYPE_SMS_LINK_FLAGGED,
                AlertHandlerActivity.ALERT_TYPE_SMS_KEYWORD_FLAGGED,
                AlertHandlerActivity.ALERT_TYPE_SMS_PATTERN_FLAGGED,
                AlertHandlerActivity.ALERT_TYPE_SMS_SAFE -> "محافظ پیامک"

                ALERT_TYPE_APP_FLAGGED -> "محافظ برنامه"

                AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED,
                AlertHandlerActivity.ALERT_TYPE_DOMAIN_FLAGGED -> "محافظ وب‌گردی"

                else -> "محافظ"
            }

            RowDivider(verticalPadding = 0.dp)

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
                        append(" مطمئن باش")
                    }
                },

                fontSize = 12.sp,
                color = GreyDark,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }
}


// Preview parameter provider for different alert types
class AlertTypePreviewParameterProvider : PreviewParameterProvider<Int> {
    override val values = sequenceOf(
        AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED,
        AlertHandlerActivity.ALERT_TYPE_SMS_LINK_FLAGGED,
        ALERT_TYPE_APP_FLAGGED,
        AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED
    )
}

// تابع پیش‌نمایش برای حالت پیامک مشکوک
@Preview(showBackground = true, name = "هشدار پیامک مشکوک")
@Composable
fun AlertDialogSmsPreview() {
    MotmaenBashTheme {
        AlertDialog(
            alertType = AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED,
            alertLevel = AlertHandlerActivity.Companion.AlertLevel.WARNING,
            title = "شناسایی فرستنده مشکوک!",
            subTitle = null,
            message = "این فرستنده در لیست سیاه قرار دارد.\n\nفرستنده: 3000123456",
            param1 = "3000123456",
            onDismiss = {},
            onUninstall = {},
            context = LocalContext.current
        )
    }
}

// تابع پیش‌نمایش برای حالت برنامه مشکوک
@Preview(showBackground = true, name = "هشدار برنامه مشکوک")
@Composable
fun AlertDialogAppPreview() {

    MotmaenBashTheme {
        AlertDialog(
            alertType = ALERT_TYPE_APP_FLAGGED,
            alertLevel = AlertHandlerActivity.Companion.AlertLevel.ERROR,
            title = "برنامه مشکوک!",
            subTitle = null,
            message = "برنامه حمله فیشینگ یک برنامه مخرب و بدافزار است.",
            param1 = "test.app.pack",
            param2 = "نام برنامه مخرب",
            onDismiss = {},
            onUninstall = {},
            context = LocalContext.current
        )
    }
}

// تابع پیش‌نمایش برای حالت وب گردی مشکوک
@Preview(showBackground = true, name = "هشدار دامنه مشکوک")
@Composable
fun AlertDialogDomainPreview() {
    MotmaenBashTheme {
        AlertDialog(
            alertType = AlertHandlerActivity.ALERT_TYPE_DOMAIN_FLAGGED,
            alertLevel = AlertHandlerActivity.Companion.AlertLevel.ERROR,
            title = "دامنه مشکوک!",
            subTitle = null,
            message = "این دامنه در لیست سیاه قرار دارد.",
            param1 = "",
            onDismiss = {},
            onUninstall = {},
            context = LocalContext.current

        )
    }
}

@Preview(showBackground = true, name = "پیش‌نمایش پارامتری هشدارها")
@Composable
fun AlertDialogParameterizedPreview(
    @PreviewParameter(AlertTypePreviewParameterProvider::class) alertType: Int
) {
    val (title, message, level) = when (alertType) {
        AlertHandlerActivity.ALERT_TYPE_SMS_SENDER_FLAGGED -> Triple(
            "شناسایی فرستنده مشکوک!",
            "این فرستنده در لیست سیاه قرار دارد.\n\nفرستنده: 3000123456",
            AlertHandlerActivity.Companion.AlertLevel.WARNING
        )

        AlertHandlerActivity.ALERT_TYPE_SMS_LINK_FLAGGED -> Triple(
            "شناسایی لینک مشکوک!",
            "متن پیام: پیام حاوی لینک\n\nفرستنده: 09123456789",
            AlertHandlerActivity.Companion.AlertLevel.ERROR
        )

        ALERT_TYPE_APP_FLAGGED -> Triple(
            "برنامه مشکوک!",
            "برنامه حمله فیشینگ یک برنامه مخرب و بدافزار است.",
            AlertHandlerActivity.Companion.AlertLevel.ERROR
        )

        AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED -> Triple(
            "آدرس مشکوک!",
            "این آدرس در لیست سیاه قرار دارد.",
            AlertHandlerActivity.Companion.AlertLevel.WARNING
        )

        else -> Triple(
            "خطا",
            "نوع هشدار نامشخص.",
            AlertHandlerActivity.Companion.AlertLevel.NORMAL
        )
    }
    MotmaenBashTheme {

        AlertDialog(
            alertType = alertType,
            alertLevel = level,
            title = title,
            subTitle = null,
            message = message,
            param1 = "",
            onDismiss = {},
            onUninstall = {},
            context = LocalContext.current
        )
    }
}