package nu.milad.motmaenbash.ui.activities


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.components.AppAlertDialog
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme

class PermissionTutorialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MotmaenBashTheme {
                TutorialDialog()
            }
        }
    }
}

@Composable
fun TutorialDialog() {
    val activity = LocalActivity.current
    var animationCounter by remember { mutableStateOf(0) }
    val animatedChecked by remember {
        derivedStateOf { animationCounter % 2 == 1 }
    }
    LaunchedEffect(Unit) {
        repeat(5) {
            delay(500)
            animationCounter++
        }
    }


    AppAlertDialog(
        title = "راهنمای فعال‌سازی",
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        content = {
            Text(
                " برنامه مطمئن باش برای نمایش هشدارهای امنیتی مثل هشدار پیامک فیشینگ، شناسایی برنامه مخرب و... " +
                        " روی سایر برنامه‌ها، به دسترسی \"نمایش روی سایر برنامه\u200Cها\" نیاز دارد.",
                style = typography.bodySmall,
                color = colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "مراحل فعال‌سازی:",
                style = typography.bodyMedium,
                fontWeight = FontWeight.Bold
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
                        modifier = Modifier
                            .size(48.dp),
                        contentScale = ContentScale.Crop
                    )

                }
            }

            Text(
                text = "۲. گزینه «نمایش روی سایر برنامه‌ها» یا Display over other apps را فعال کنید.",
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
        },
        onConfirm = { activity?.finish() },
        confirmText = "متوجه شدم",
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewTutorialContent() {
    MotmaenBashTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TutorialDialog()
        }
    }
}
