package nu.milad.motmaenbash.ui

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.GreenDark
import nu.milad.motmaenbash.ui.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.ui.theme.Red
import nu.milad.motmaenbash.ui.ui.theme.Yellow
import nu.milad.motmaenbash.utils.PackageUtils
import nu.milad.motmaenbash.utils.SettingsManager
import nu.milad.motmaenbash.utils.dataStore

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

        const val UNINSTALL_REQUEST_CODE = 1001

        // Sound types
        const val SOUND_TYPE_DING1 = "ding1"
        const val SOUND_TYPE_DING2 = "ding2"
        const val SOUND_TYPE_DING3 = "ding3"
        const val SOUND_TYPE_DING4 = "ding4"
        const val SOUND_TYPE_DING5 = "ding5"
    }

    private var soundPool: SoundPool? = null
    private var dingSoundId: Int = 0

    private var vibrator: Vibrator? = null
    private lateinit var settingsManager: SettingsManager
    private var alertSound: String = SOUND_TYPE_DING1

    private var playSoundInSilentMode: Boolean = false

    private var param1: String = ""
    private var param2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize settings manager
        settingsManager = SettingsManager(dataStore)

        // Get user preferences
        runBlocking {
            val preferences = settingsManager.preferencesFlow.first()
            alertSound = preferences[SettingsManager.ALERT_SOUND] ?: SOUND_TYPE_DING1

            val playInSilentPref = preferences[SettingsManager.PLAY_SOUND_IN_SILENT_MODE] ?: "false"
            playSoundInSilentMode = playInSilentPref == "true"

        }

        // Initialize Vibrator
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Initialize SoundPool with audio attributes
        soundPool = SoundPool.Builder().setMaxStreams(1).build()

        val soundResId = when (alertSound) {
            SOUND_TYPE_DING2 -> R.raw.ding2
            SOUND_TYPE_DING3 -> R.raw.ding3
            SOUND_TYPE_DING4 -> R.raw.ding4
            SOUND_TYPE_DING5 -> R.raw.ding5
            else -> R.raw.ding1
        }

        // Load sound
        dingSoundId = soundPool?.load(this, soundResId, 1) ?: 0

        soundPool?.setOnLoadCompleteListener { _, id, status ->
            if (status == 0 && id == dingSoundId) {
                // Vibrate when alert is shown
                vibrateDevice()

                // Play sound if appropriate
                playAlertSound()
            }
        }

        val taskDescription = ActivityManager.TaskDescription(
            getString(R.string.alert_dialog_activity_title), null, Color.TRANSPARENT
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
                    onDismiss = { finishAndRemoveTask() },
                    onUninstall = {
                        val intent = PackageUtils.uninstallApp(this, param1)
                        uninstallLauncher.launch(intent)
                    }
                )

            }
        }
    }

    private fun vibrateDevice() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") vibrator?.vibrate(500)
        }
    }

    private fun playAlertSound() {
        // Check if we should play sound in silent mode
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode

        val shouldPlaySound = when {
            // In normal mode, always play
            ringerMode == AudioManager.RINGER_MODE_NORMAL -> true

            // In silent/vibrate mode, play only if playSoundInSilentMode is true
            playSoundInSilentMode -> true

            // Otherwise don't play
            else -> false
        }


        if (!shouldPlaySound) {
            return
        }


        soundPool?.play(dingSoundId, 1f, 1f, 1, 0, 1f) ?: -1

    }

    private fun getAlertContent(
        alertType: Int, param1: String, param2: String
    ): Pair<String, String> {
        return when (alertType) {
            ALERT_TYPE_SMS_SENDER_FLAGGED -> Pair(
                "شناسایی فرستنده مشکوک!",
                "این فرستنده در لیست سیاه قرار دارد." + "\n\nفرستنده: $param1"
            )

            ALERT_TYPE_SMS_LINK_FLAGGED -> Pair(
                "شناسایی لینک مشکوک!", "متن پیام: $param2\n\nفرستنده: $param1"
            )

            ALERT_TYPE_SMS_KEYWORD_FLAGGED -> Pair(
                "کلمه کلیدی مشکوک!", "متن پیام: $param2\n\nفرستنده: $param1"
            )

            ALERT_TYPE_SMS_PATTERN_FLAGGED -> Pair(
                "شناسایی پیامک مشکوک!", "متن پیام: $param2"
            )

            ALERT_TYPE_SMS_SAFE -> Pair(
                "پیامک ایمن", "محتوای مشکوکی شناسایی نشد." + "\n\n$param2\n\nفرستنده: $param1"
            )

            ALERT_TYPE_APP_FLAGGED -> Pair(
                "برنامه مشکوک!",
                "برنامه $param1 یک برنامه مخرب و بدافزار است. لطفا بدون اینکه برنامه را باز کنید، سریعا ان را حذف کنید."
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
        soundPool?.release()
        soundPool = null
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
    alertType: Int,
    alertLevel: AlertHandlerActivity.Companion.AlertLevel,
    title: String,
    subTitle: String?,
    message: String,
    param1: String,
    onDismiss: () -> Unit,
    onUninstall: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = false, dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
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


                    val badgeColor = when (alertLevel) {
                        AlertHandlerActivity.Companion.AlertLevel.NORMAL -> GreenDark
                        AlertHandlerActivity.Companion.AlertLevel.WARNING -> Yellow
                        AlertHandlerActivity.Companion.AlertLevel.ERROR -> Red
                    }

                    Box(
                        modifier = Modifier
                            .wrapContentSize()

                            .background(
                                color = badgeColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {

                        Text(
                            text = title,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,

                            )

                    }



                    IconButton(
                        onClick = onDismiss, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = GreyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))






                Text(
                    text = title,
                    color = Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                HorizontalDivider(
                    color = Red, thickness = 2.dp
                )

                if (!subTitle.isNullOrEmpty()) {
                    Text(
                        text = subTitle,
                        color = androidx.compose.ui.graphics.Color.Black,
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
                    color = androidx.compose.ui.graphics.Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .verticalScroll(scrollState)
                )


                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "متوجه شدم",
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (alertType == AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED) {
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
                            color = androidx.compose.ui.graphics.Color.White,
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

                AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED -> "محافظ برنامه"

                AlertHandlerActivity.ALERT_TYPE_URL_FLAGGED,
                AlertHandlerActivity.ALERT_TYPE_DOMAIN_FLAGGED -> "محافظ وب‌گردی"

                else -> "محافظ"
            }

            Text(

                buildAnnotatedString {
                    append("شناسایی شده توسط ")
                    withStyle(
                        style = SpanStyle(
                            color = GreyDark, fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(protectionType)
                    }
                    withStyle(
                        style = SpanStyle(
                            color = ColorPrimary, fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(" مطمئن باش")
                    }
                },
                fontSize = 14.sp,
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
        AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED,
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
            onUninstall = {}
        )
    }
}

// تابع پیش‌نمایش برای حالت برنامه مشکوک
@Preview(showBackground = true, name = "هشدار برنامه مشکوک")
@Composable
fun AlertDialogAppPreview() {
    MotmaenBashTheme {

        AlertDialog(
            alertType = AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED,
            alertLevel = AlertHandlerActivity.Companion.AlertLevel.ERROR,
            title = "برنامه مشکوک!",
            subTitle = null,
            message = "برنامه حمله فیشینگ یک برنامه مخرب و بدافزار است. لطفا بدون اینکه برنامه را باز کنید، سریعا ان را حذف کنید.",
            param1 = "حمله فیشینگ",
            onDismiss = {},
            onUninstall = {}
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
            onUninstall = {}
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

        AlertHandlerActivity.ALERT_TYPE_APP_FLAGGED -> Triple(
            "برنامه مشکوک!",
            "برنامه حمله فیشینگ یک برنامه مخرب و بدافزار است. لطفا بدون اینکه برنامه را باز کنید، سریعا ان را حذف کنید.",
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
            onUninstall = {}
        )
    }
}