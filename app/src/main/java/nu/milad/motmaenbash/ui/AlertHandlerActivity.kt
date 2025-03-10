package nu.milad.motmaenbash.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.common.io.Files.append
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.GreenDark
import nu.milad.motmaenbash.ui.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.ui.theme.Red
import nu.milad.motmaenbash.ui.ui.theme.Yellow
import nu.milad.motmaenbash.utils.PackageUtils

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

        // Alert types
        sealed class AlertType {
            data class SmsSenderFlagged(val sender: String) : AlertType()
            data class SmsLinkFlagged(val message: String) : AlertType()
            data class SmsKeywordFlagged(val message: String, val sender: String) : AlertType()
            data class SmsPatternFlagged(val message: String) : AlertType()
            data class SmsSafe(val message: String, val sender: String) : AlertType()
            data class AppFlagged(val appName: String) : AlertType()
            object UrlFlagged : AlertType()
            object DomainFlagged : AlertType()
        }

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

    }

    private var soundPool: SoundPool? = null
    private var dingSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize SoundPool
        soundPool = SoundPool.Builder().setMaxStreams(1).build()

        // Load the sound
        dingSoundId = soundPool?.load(this, R.raw.dingding, 1) ?: 0
        soundPool?.setOnLoadCompleteListener { _, id, status ->
            if (status == 0 && id == dingSoundId) {
                playDingSound()
            }
        }

        val taskDescription = ActivityManager.TaskDescription(
            getString(R.string.alert_dialog_activity_title), null, Color.TRANSPARENT
        )
        setTaskDescription(taskDescription)

        val param1 = intent.getStringExtra(EXTRA_PARAM1).orEmpty()
        val param2 = intent.getStringExtra(EXTRA_PARAM2).orEmpty()
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

            AlertDialog(alertType = alertType,
                alertLevel = alertLevel,
                title = title,
                subTitle = subTitle,
                message = message,
                param1 = param1,
                onDismiss = { finishAndRemoveTask() },
                onUninstall = { PackageUtils.uninstallApp(this, param1, param2) })

        }
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

    private fun playDingSound() {
        soundPool?.play(dingSoundId, 1f, 1f, 1, 0, 1f)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UNINSTALL_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "برنامه با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "حذف برنامه لغو شد", Toast.LENGTH_SHORT).show()
                }
            }
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
                    IconButton(
                        onClick = onDismiss, modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "بستن",
                            tint = GreyDark
                        )
                    }

                    val badgeColor = when (alertLevel) {
                        AlertHandlerActivity.Companion.AlertLevel.NORMAL -> GreenDark
                        AlertHandlerActivity.Companion.AlertLevel.WARNING -> Yellow
                        AlertHandlerActivity.Companion.AlertLevel.ERROR -> Red
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = badgeColor, shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = title,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
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

                Divider(
                    color = Red, thickness = 2.dp
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {


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
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    )
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary
                        )
                    ) {
                        Text(
                            text = "حذف سریع این برنامه",
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }


            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                buildAnnotatedString {
                    append("توسط برنامه ")
                    withStyle(
                        style = SpanStyle(
                            color = ColorPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("مطمین باش")
                    }
                },
                fontSize = 14.sp,
                color = GreyDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }


}
