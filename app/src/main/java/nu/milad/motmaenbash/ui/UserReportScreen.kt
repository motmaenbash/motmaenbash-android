import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.consts.AppConstants.USER_REPORT_FORM_URL
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.WebUtils.openUrlInCustomTab


@Composable
fun UserReportScreen(navController: NavController) {
    val context = LocalContext.current

    AppBar(
        title = stringResource(id = R.string.user_report_activity_title),
        onNavigationIconClick = { navController.navigateUp() },
        onActionClick = { /* Handle menu action */ },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Text(
                text = stringResource(id = R.string.report_description),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                onClick = {
                    val url = USER_REPORT_FORM_URL
                    openUrlInCustomTab(context, url)
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserReportScreenPreview() {
    MotmaenBashTheme {
        UserReportScreen(rememberNavController())
    }
}
