package nu.milad.motmaenbash.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.components.AppCard
import nu.milad.motmaenbash.ui.theme.Green
import nu.milad.motmaenbash.ui.theme.GreyDark
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.ui.theme.Red
import nu.milad.motmaenbash.utils.UrlUtils
import nu.milad.motmaenbash.viewmodels.UrlScanViewModel

@Composable
fun UrlScanScreen(
    viewModel: UrlScanViewModel = viewModel(),
    initialUrl: String = "" //For preview
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var url by remember { mutableStateOf(initialUrl) }
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        if (initialUrl.isEmpty()) {
            (context as? ComponentActivity)?.intent?.let { intent ->
                val extractedUrl = viewModel.getInitialUrl(intent)
                if (extractedUrl.isNotEmpty()) {
                    url = extractedUrl
                    if (UrlUtils.validateUrl(url)) {
                        viewModel.scanUrl(url)
                    } else {
                        Toast.makeText(
                            context, "لطفا یک URL معتبر وارد کنید", Toast.LENGTH_LONG
                        ).show()
//
                    }
                }
            }
        }
    }

    AppBar(
        title = stringResource(id = R.string.url_scan_activity_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "بررسی URL",
                color = colorScheme.primary,
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "بررسی لینک و آدرس‌های اینترنتی",
                style = typography.headlineSmall,
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
                        "لطفا URL را وارد کنید", style = typography.bodySmall
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
                            context, "لطفا یک URL معتبر وارد کنید", Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "بررسی آدرس", modifier = Modifier.padding(8.dp)
                )
            }

            // Result
            when (val currentState = state) {
                is UrlScanViewModel.ScanState.Result -> {
                    AppCard(
                        cornerRadius = 12.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val resultIcon = when (currentState.isSafe) {
                                true -> Icons.Outlined.CheckCircleOutline
                                false -> Icons.Outlined.Cancel
                                else -> Icons.AutoMirrored.Outlined.HelpOutline
                            }
                            resultIcon.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .padding(bottom = 8.dp),
                                    tint = when (currentState.isSafe) {
                                        true -> Green
                                        false -> Red
                                        else -> GreyDark
                                    }
                                )
                            }
                            Text(
                                text = "${currentState.message}\n${currentState.url}",
                                style = typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (currentState.isSafe) {
                                    true -> Green
                                    false -> Red
                                    else -> GreyDark
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "مشکوک - URL نامعتبر")
@Composable
fun UrlScanScreenPreviewMalicious() {
    val viewModel = UrlScanViewModel(object : Application() {}).apply {
        val stateFlow = _state
        stateFlow.value = UrlScanViewModel.ScanState.Result(
            message = "این آدرس به عنوان یک آدرس مشکوک شناسایی شده است.",
            isSafe = false,
            url = "https://malicious-site.com"
        )
    }

    CompositionLocalProvider(
        LocalNavController provides rememberNavController()
    ) {
        MotmaenBashTheme {
            UrlScanScreen(
                viewModel = viewModel,
                initialUrl = "https://malicious-site.com"
            )
        }
    }
}

@Preview(showBackground = true, name = "امن - درگاه پرداخت")
@Composable
fun UrlScanScreenPreviewSafe() {
    val viewModel = UrlScanViewModel(object : Application() {}).apply {
        val stateFlow = _state
        stateFlow.value = UrlScanViewModel.ScanState.Result(
            message = "این آدرس متعلق به یک درگاه پرداخت مطمئن است.",
            isSafe = true,
            url = "https://shaparak.ir"
        )
    }

    CompositionLocalProvider(
        LocalNavController provides rememberNavController()
    ) {
        MotmaenBashTheme {
            UrlScanScreen(
                viewModel = viewModel,
                initialUrl = "https://shaparak.ir"
            )
        }
    }
}

@Preview(showBackground = true, name = "نامشخص - آدرس موجود نیست")
@Composable
fun UrlScanScreenPreviewUnknown() {
    val viewModel = UrlScanViewModel(object : Application() {}).apply {
        val stateFlow = _state
        stateFlow.value = UrlScanViewModel.ScanState.Result(
            message = "این آدرس در پایگاه داده ما موجود نیست. لطفا احتیاط کنید.",
            isSafe = null,
            url = "https://unknown-site.com"
        )
    }

    CompositionLocalProvider(
        LocalNavController provides rememberNavController()
    ) {
        MotmaenBashTheme {
            UrlScanScreen(
                viewModel = viewModel,
                initialUrl = "https://unknown-site.com"
            )
        }
    }
}

@Preview(showBackground = true, name = "حالت اولیه")
@Composable
fun UrlScanScreenPreviewInitial() {
    val viewModel = UrlScanViewModel(object : Application() {}).apply {
        val stateFlow = _state
        stateFlow.value = UrlScanViewModel.ScanState.Initial
    }

    CompositionLocalProvider(
        LocalNavController provides rememberNavController()
    ) {
        MotmaenBashTheme {
            UrlScanScreen(
                viewModel = viewModel,
                initialUrl = ""
            )
        }
    }
}