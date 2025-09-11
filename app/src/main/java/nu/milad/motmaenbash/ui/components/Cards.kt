package nu.milad.motmaenbash.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.WebUtils.openUrl


@Composable
fun DonationCard() {
    val context = LocalContext.current

    AppCard(
        content = {

            AppCard(
                containerColor = colorScheme.tertiaryContainer,
                padding = 4.dp

            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.donation_message),
                        style = typography.headlineSmall,
                        color = colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { openUrl(context, AppConstants.DONATE_URL) },
                        modifier = Modifier.wrapContentSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CurrencyBitcoin,
                            contentDescription = "Donate",
                            tint = Color.White
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(R.string.donation_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }

        },
    )
}


@Composable
fun SecurityWarning() {
    AppCard(
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
        padding = 12.dp,
        content = {

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colorScheme.onError

                    )
                    Text(
                        text = "نکته مهم",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onError
                    )
                }

                Divider(verticalPadding = 2.dp, horizontalPadding = 8.dp)
                Text(
                    text = "اپلیکیشن «مطمئن باش» و به‌روزرسانی‌های آن را فقط از منابع رسمی و مارکت‌های معتبر نصب کنید.",
                    style = typography.bodySmall,
                    color = colorScheme.onSurface

                )

            }

        },
    )

}


@Preview(showBackground = true)
@Composable
fun DonationCardPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            DonationCard()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SecurityWarningPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            SecurityWarning()
        }
    }
}

