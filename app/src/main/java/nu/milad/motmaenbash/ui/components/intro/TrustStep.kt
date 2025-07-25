package nu.milad.motmaenbash.ui.components.intro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.screens.AnimatedPermissionIcon
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme

@Composable
fun TrustStep(
    onNext: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trust Icon
        AnimatedPermissionIcon(Icons.Outlined.VerifiedUser, size = 48.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "آیا خود «مطمئن باش» امن است؟",
            style = typography.headlineSmall,
            color = colorScheme.primary,
            fontSize = 17.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Trust reasons
        TrustReasonCard(
            icon = Icons.Outlined.Code,
            title = "متن‌باز (Open Source)",
            description = "«مطمئن باش» متن‌باز است و کدهای آن توسط کارشناس‌ها و عموم کاربرها قابل بررسی و ارزیابی است."
        )

        Spacer(modifier = Modifier.height(4.dp))

        TrustReasonCard(
            icon = Icons.Outlined.CloudOff,
            title = "بدون سرور و حفظ کامل حریم خصوصی",
            description = "برنامه سرور ندارد و بررسی‌ها به صورت آفلاین روی خود گوشی انجام می‌شود و اطلاعات برای بررسی به جایی ارسال نمی‌شود."
        )

        Spacer(modifier = Modifier.height(4.dp))

        TrustReasonCard(
            icon = Icons.Outlined.AdminPanelSettings,
            title = "دسترسی‌های اختیاری",
            description = "دسترسی‌های حساس برنامه اختیاری‌ست و بدون فعال‌سازی آن‌ها می‌توانید از سایر امکانات برنامه استفاده کنید."
        )

        Spacer(modifier = Modifier.height(4.dp))

        TrustReasonCard(
            icon = Icons.Outlined.Verified,
            title = "انتشار بر روی مارکت‌های معتبر",
            description = "برنامه توسط مارکت‌های معتبر بررسی و منتشر شده است."
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.width(200.dp)
        ) {
            Text("کامل خوندم، بزن بریم")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "هنگام معرفی برنامه به دیگران، حتما این نکات هم بیان کنید.",
            modifier = Modifier
                .padding(start = 4.dp),
            fontSize = 13.sp,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

    }

}


@Composable
fun TrustReasonCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    AppCard(
        padding = 2.dp,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
    ) {

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ColorPrimary

                )
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            Divider(verticalPadding = 2.dp, horizontalPadding = 8.dp)
            Text(
                description,
                style = typography.bodySmall,
                color = colorScheme.onSurface

            )

        }

    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun TrustStepPreview() {
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
                    TrustStep(
                        onNext = {}
                    )
                }
            }
        }
    }
}