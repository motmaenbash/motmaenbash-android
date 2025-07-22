package nu.milad.motmaenbash.ui.components.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.Divider
import nu.milad.motmaenbash.ui.screens.AnimatedPermissionIcon
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme

@Composable
fun FinalStep(
    onNext: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedPermissionIcon(Icons.Outlined.Security)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "آماده شروع...",
            style = typography.headlineSmall,
            color = colorScheme.primary,
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "«مطمئن باش» تلاش می‌کند با شناسایی موارد مشکوک، امنیت شما را افزایش دهد." +
                    " اما شناسایی‌نشدن یک پیامک، لینک یا برنامه، به معنای تایید یا بی‌خطر بودن آن نیست.",
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)

        Text(
            "گزارش‌های شما نقش مهمی در شناسایی تهدیدهای جدید و محافظت جمعی از همه کاربرها دارد.",
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Divider(verticalPadding = 8.dp, horizontalPadding = 24.dp)
        Text(
            "این برنامه رایگان و متن‌باز است و حمایت مالی (دونیت) شما می‌تواند به توسعه مداوم آن کمک کند.",
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.width(200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
        ) {
            Text("ورود به برنامه")
        }
    }


}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun FinalStepPreview() {
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
                    FinalStep(
                        onNext = {}
                    )
                }
            }
        }
    }
}