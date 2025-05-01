package nu.milad.motmaenbash.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyBitcoin
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
import nu.milad.motmaenbash.utils.WebUtils.openUrlInCustomTab


@Composable
fun DonationCard() {
    val context = LocalContext.current

    AppCard {

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
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { openUrlInCustomTab(context, AppConstants.DONATE_URL) },
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }

    }
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
