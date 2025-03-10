package nu.milad.motmaenbash.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.utils.UrlUtils
import nu.milad.motmaenbash.viewmodels.UrlScanViewModel


@Composable


fun UrlScanScreen(

    navController: NavController,
    initialUrl: String? = null,
    viewModel: UrlScanViewModel = viewModel()

) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(initialUrl ?: "") }
    val showResult by viewModel.showResult.collectAsState()
    val resultText by viewModel.resultText.collectAsState()
    val isSafe by viewModel.isSafe.collectAsState()


    AppBar(
        title = stringResource(id = R.string.url_scan_activity_title),
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
                text = "اسکن URL",
                color = ColorPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 48.dp)
            )
            Text(
                text = "اپلیکیشن تشخیص فیشینگ",
                color = Color.Black,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White

                ),
                value = url,
                onValueChange = {
                    url = it
                    viewModel.resetResult()
                },

                label = {
                    Text(
                        "لطفاً URL را وارد کنید", style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textStyle = TextStyle(
                    textDirection = TextDirection.Ltr, fontSize = 18.sp
                )
            )
            Button(
                onClick = {
                    if (UrlUtils.validateUrl(url)) {
                        viewModel.scanUrl(url)

                    } else {
                        Toast.makeText(
                            context, "لطفاً یک URL وارد کنید", Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetResult()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "بررسی آدرس", modifier = Modifier.padding(8.dp)

                )
            }

            // Show the result only if showResult is true
            if (showResult) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val resultIcon = when (isSafe) {
                            true -> R.drawable.ic_check
                            false -> R.drawable.ic_restrict
                            else -> null
                        }
                        resultIcon?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 8.dp),
                                tint = when (isSafe) {
                                    true -> Color.Green
                                    false -> Color.Red
                                    else -> Color.Gray
                                }
                            )

                        }
                        Text(
                            text = "$resultText\n$url",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (isSafe) {
                                true -> Color.Green
                                false -> Color.Red
                                else -> Color.Black
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }


    // Check if there's a URL passed from the Intent
    LaunchedEffect(Unit) {
        initialUrl?.let {
            if (UrlUtils.validateUrl(it)) {
                viewModel.scanUrl(it)
            } else {
                Toast.makeText(
                    context, "لطفاً یک URL وارد کنید", Toast.LENGTH_LONG
                ).show()

                val activity = context as? Activity
                activity?.finish()
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun UrlScanScreenPreview() {
    MotmaenBashTheme {
        UrlScanScreen(rememberNavController())
    }
}