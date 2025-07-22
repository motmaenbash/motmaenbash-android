package nu.milad.motmaenbash.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants.USER_REPORT_FORM_URL
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.WebUtils.openUrl


@Composable
fun UserReportScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AppBar(
        title = stringResource(id = R.string.user_report_screen_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(
                text = stringResource(id = R.string.report_description),
                style = typography.bodySmall,
            )
            Button(
                onClick = {
                    val url = USER_REPORT_FORM_URL
                    openUrl(context, url)
                }, modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.send_report)
                )
            }
            Text(
                text = stringResource(id = R.string.report_time_message),
                style = typography.bodySmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserReportScreenPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            UserReportScreen()
        }
    }
}