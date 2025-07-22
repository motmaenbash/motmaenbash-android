package nu.milad.motmaenbash.ui.components.intro

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nu.milad.motmaenbash.ui.components.AppLogo
import nu.milad.motmaenbash.ui.components.Divider


@Composable
fun IntroStep(
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo(90.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "به «مطمئن باش» خوش آمدید",
            style = typography.headlineSmall,
            color = colorScheme.primary,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "این برنامه برای هشدار درباره پیامک‌ها، برنامه‌ها و لینک‌های مشکوک طراحی شده است.",
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)

        Text(
            buildAnnotatedString {
                append("در مراحل بعد می‌توانید برخی دسترسی‌ها را فعال کنید. ")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("دسترسی‌هایی مثل پیامک و دسترسی‌پذیری اختیاری‌اند،")
                }

                append(" اما در صورت فعال‌سازی، بررسی‌ها خودکار انجام می‌شود.")
            },
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)
        Text(
            "بدون این دسترسی‌ها هم می‌توانید از سایر امکانات مانند اسکن برنامه‌ها، بررسی دستی لینک و... استفاده کنید.",
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.width(200.dp)
        ) {
            Text("خوندم، بزن بریم")
        }
    }
}


