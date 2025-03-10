import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.BuildConfig
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.Grey
import nu.milad.motmaenbash.ui.ui.theme.GreyLight
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.WebUtils.openUrlInCustomTab

@Composable
fun AboutScreen(
    navController: NavController,
) {

    val context = LocalContext.current

    AppBar(
        title = stringResource(id = R.string.about_activity_title),
        onNavigationIconClick = { navController.navigateUp() },
        onActionClick = { /* Handle menu action */ },
    ) { contentPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo()
            Spacer(modifier = Modifier.height(24.dp))
            AppInfo()
            Spacer(modifier = Modifier.height(18.dp))
            SocialMediaLinks()
            Spacer(modifier = Modifier.height(18.dp))
            ActionButtons(context)
        }
    }
}

@Composable
fun AppLogo() {
    Card(
        modifier = Modifier.size(120.dp),

        shape = RoundedCornerShape(24.dp),


        ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "MotmaenBash Logo",
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPrimary),

            contentScale = ContentScale.Crop
        )

    }
}


@Composable
fun AppInfo() {

    val context = LocalContext.current

    Text(
        text = stringResource(id = R.string.app_name_fa),
        style = MaterialTheme.typography.headlineLarge,
        color = ColorPrimary,
    )


    Text(
        text = "اپلیکیشن تشخیص فیشینگ", style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.bodySmall
    )

    Text(
        text = "https://motmaenbash.ir", style = MaterialTheme.typography.bodySmall.copy(
            color = ColorPrimary, fontWeight = FontWeight.Bold
        ), modifier = Modifier
            .padding(top = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://motmaenbash.ir"))
                context.startActivity(intent)
            })

    HorizontalDivider(
        color = GreyLight, thickness = 1.dp, modifier = Modifier.padding(32.dp, 18.dp)
    )




    Text(
        text = "برنامه‌نویس: میلاد نوری",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold

    )

}


@Composable
fun SocialMediaLinks() {

    val context = LocalContext.current


    val links = listOf(
        "کانال تلگرام" to "https://t.me/MiladNouriChannel",
        "توییتر" to "https://twitter.com/MilaDnu",
        "کانال یوتوب" to "https://youtube.com/MilaDnu",
        "اینستاگرام" to "https://instagram.com/milad_nouri",
        "وب‌سایت" to "https://milad.nu"
    )

    links.forEach { (label, url) ->

        Row(
            modifier = Modifier.padding(vertical = 6.dp) // Adjust vertical padding as needed
        ) {
            // Label
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 4.dp)
            )

            // URL
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall.copy(color = ColorPrimary),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                })
        }
    }

}

@Composable
fun ActionButtons(context: Context) {
    Button(
        onClick = { openUrlInCustomTab(context, AppConstants.DONATE_URL) },
        modifier = Modifier.wrapContentSize(),
        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_donate),
            contentDescription = "Donate",
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))



        Text(
            text = "حـمـایـت مـالـی", fontSize = 17.sp, fontWeight = FontWeight.Bold
        )
    }


    HorizontalDivider(
        color = GreyLight, thickness = 1.dp, modifier = Modifier.padding(32.dp, 18.dp)
    )


    Row(
        modifier = Modifier.wrapContentSize(),
    ) {
        Button(
            onClick = { sendBugReport(context) },
            modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Grey)
        ) {
            Icon(
//                imageVector = Icons.Sharp.Build, contentDescription = null, tint = Color.White
                painter = painterResource(id = R.drawable.ic_bug),
                contentDescription = "Donate",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "گزارش اشکال")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { shareAppInfo(context) },
            modifier = Modifier.wrapContentSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Grey)
        ) {
            Icon(
                imageVector = Icons.Sharp.Share, contentDescription = null, tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "معرفی به دوستان")

        }
    }
}


private fun shareAppInfo(context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        putExtra(
            Intent.EXTRA_TEXT, """
                اپلیکیشن مطمئن باش را به شما معرفی می‌کنم. این برنامه به شما کمک می‌کند تا از فیشینگ و کلاهبرداری‌های اینترنتی جلوگیری کنید.
                
                می‌توانید اپلیکیشن را از لینک زیر دانلود کنید:
                https://cafebazaar.ir/app/nu.milad.motmaenbash
                
                برنامه: ${context.getString(R.string.app_name)}
                نسخه: ${BuildConfig.VERSION_NAME}
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
        data = Uri.parse("mailto:")
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
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_email_app_found), Toast.LENGTH_SHORT)
            .show()
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    MotmaenBashTheme {
        AboutScreen(rememberNavController())
    }
}
