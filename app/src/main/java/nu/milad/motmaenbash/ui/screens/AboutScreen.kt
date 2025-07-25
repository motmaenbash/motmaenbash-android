package nu.milad.motmaenbash.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.NavRoutes
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.components.AppLogo
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.components.DonationCard
import nu.milad.motmaenbash.ui.components.SecurityWarning
import nu.milad.motmaenbash.ui.components.TickerText
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.RedVariant
import nu.milad.motmaenbash.utils.WebUtils

@Composable
fun AboutScreen() {

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val navController = LocalNavController.current

    AppBar(
        title = stringResource(id = R.string.about_screen_title),
    ) { contentPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo(size = 96.dp)
            Spacer(modifier = Modifier.height(4.dp))
            AppInfo(navController)
            Spacer(modifier = Modifier.height(18.dp))
            SocialMediaLinks()
            Spacer(modifier = Modifier.height(18.dp))

            DonationCard()

            SecurityWarning()

            ActionButtons(context)

            Divider(verticalPadding = 12.dp)

            Text(
                text = stringResource(R.string.updates_changelog),
                style = typography.bodySmall,
                modifier = Modifier.clickable {
                    navController.navigate(NavRoutes.CHANGELOG_SCREEN)
                },
            )


        }
    }


}

@Composable
fun AppInfo(navController: NavController) {

    val context = LocalContext.current

    TickerText(
        texts = listOf(
            stringResource(id = R.string.app_name_fa),
            stringResource(id = R.string.app_name)
        ),
        color = colorScheme.primary,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        animationDuration = 2_000,
        transitionDuration = 500
    )
    Text(
        text = stringResource(id = R.string.app_slogan),
        color = colorScheme.onBackground,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
        style = typography.bodySmall,
        modifier = Modifier.clickable {
            navController.navigate(NavRoutes.CHANGELOG_SCREEN)
        },
    )

    Text(
        text = "https://MotmaenBash.ir", style =
            typography.bodySmall.copy(
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            ), modifier = Modifier
            .padding(top = 4.dp)
            .clickable {
                WebUtils.openUrl(context, "https://motmaenbash.ir")
            })

    Divider(verticalPadding = 12.dp, horizontalPadding = 32.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = "Developer",
            modifier = Modifier.size(20.dp),
            tint = GreyDark
        )


        Text(
            text = "طراحی و توسعه توسط میلاد نوری",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Developer",
            modifier = Modifier.size(20.dp),
            tint = GreyDark
        )
    }

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
            fontWeight = FontWeight.Bold, color = GreyDark,

            style = typography.bodySmall,
        )
    }


}


@Composable
fun SocialMediaLinks() {

    val context = LocalContext.current


    val links = listOf(
        "کانال تلگرام" to "https://t.me/MiladNouriChannel",
        "توییتر" to "https://twitter.com/MilaDnu",
        "کانال یوتوب" to "https://youtube.com/MilaDnu",
        "اینستاگرام" to "https://instagram.com/milad_nouri"
    )
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        links.forEachIndexed { index, (label, url) ->

            Row(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Label
                Text(
                    text = "$label: ",
                    style = typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )

                // URL
                Text(
                    text = url,
                    style = typography.bodySmall.copy(
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable {
                        WebUtils.openUrl(context, url)
                    })
            }
            // show divider if it's not the last item
            if (index != links.size - 1) {
                Divider(verticalPadding = 4.dp, horizontalPadding = 4.dp)
            }
        }

    }
}

@Composable
fun ActionButtons(
    context: Context,
) {


    Row(
        modifier = Modifier.wrapContentSize(),
    ) {
        Button(
            onClick = { sendBugReport(context) },
            modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),

            ) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = "Bug Report",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "گزارش اشکال", fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { shareAppInfo(context) },
            modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
        ) {
            Icon(
                imageVector = Icons.Outlined.RecordVoiceOver,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "معرفی به دوستان", fontSize = 13.sp)

        }
    }
}


private fun shareAppInfo(context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        putExtra(
            Intent.EXTRA_TEXT, """
                📱 سلام. برنامه «مطمئن باش» رو ببین.
                این برنامه موبایل بهت کمک می‌کنه تا کمتر در دام فیشینگ و کلاهبرداری‌های اینترنتی بیفتی.

                🔒 شناسایی پیامک‌های فیشینگ و هشدار به شما
                🔍 هشدار درباره لینک‌های مشکوک شناسایی شده
                🛡️ شناسایی و هشدار درباره برنامه‌های مخرب
                
              📥 این برنامه کاملا رایگانه! می‌تونی از اینجا نصبش کنی:  
                🔗 https://MotmaenBash.ir/
                
                👈 مشاهده ویدئوی معرفی برنامه «مطمئن باش»:
                https://youtu.be/xN8Pjf4bSC4
            """.trimIndent()
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "معرفی به دوستان..."))
}

private fun sendBugReport(context: Context) {
    Toast.makeText(
        context, context.getString(R.string.report_bug_toast_message), Toast.LENGTH_SHORT
    ).show()

    val feedbackIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf("mtmnbsh@gmail.com"))
        putExtra(
            Intent.EXTRA_SUBJECT,
            "${context.getString(R.string.app_name)} - v" + BuildConfig.VERSION_CODE
        )
        putExtra(
            Intent.EXTRA_TEXT, """
                برنامه: ${context.getString(R.string.app_name_fa)} - نسخه ${BuildConfig.VERSION_CODE}دستگاه: ${Build.BRAND} - ${Build.MODEL} 
                اندروید: ${Build.VERSION.RELEASE}
                --------------------
                ${context.getString(R.string.bug_report_placeholder)}
            """.trimIndent()
        )
    }

    try {
        context.startActivity(
            Intent.createChooser(
                feedbackIntent, context.getString(R.string.send_email_chooser_title)
            )
        )
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_email_app_found), Toast.LENGTH_SHORT)
            .show()
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            AboutScreen()
        }
    }
}

