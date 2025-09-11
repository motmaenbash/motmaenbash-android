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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.RemoveModerator
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val focusManager = LocalFocusManager.current

    var url by remember { mutableStateOf(initialUrl) }
    val state by viewModel.state.collectAsState()

    fun scanUrl() {
        if (UrlUtils.validateUrl(url)) {
            viewModel.scanUrl(url)
            focusManager.clearFocus() // Clear focus to hide keyboard
        } else {
            Toast.makeText(
                context, "لطفا یک URL معتبر وارد کنید", Toast.LENGTH_LONG
            ).show()
        }
    }

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
                            context, "لطفا یک آدرس اینترنتی معتبر وارد کنید", Toast.LENGTH_LONG
                        ).show()
//
                    }
                }
            }
        }
    }

    AppBar(
        title = stringResource(id = R.string.url_scan_screen_title),
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
                color = colorScheme.onBackground,
                text = "بررسی لینک و آدرس‌های اینترنتی",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            OutlinedTextField(
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surface
                ),
                value = url,
                onValueChange = {
                    url = it.trim()
                    viewModel.resetResult()
                },
                label = {
                    Text(
                        "آدرس اینترنتی مورد نظر را وارد کنید", style = typography.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textStyle = TextStyle(
                    textDirection = TextDirection.Ltr, fontSize = 18.sp
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        scanUrl()
                    }
                )
            )
            Button(
                onClick = {
                    scanUrl()
                },
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "بررسی",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            // Result
            when (val currentState = state) {
                is UrlScanViewModel.ScanState.Result -> {
                    AppCard(
                        cornerRadius = 12.dp,
                        content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val resultIcon = when (currentState.isSafe) {
                                    true -> Icons.Outlined.VerifiedUser
                                    false -> Icons.Outlined.RemoveModerator
                                    else -> Icons.Outlined.GppMaybe
                                }
                                Icon(
                                    imageVector = resultIcon,
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

                                val textColor = when (currentState.isSafe) {
                                    true -> Green
                                    false -> Red
                                    else -> GreyDark
                                }

                                Text(
                                    text = currentState.message,
                                    style = typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp),
                                    textAlign = TextAlign.Center,

                                    )

                                Text(
                                    text = currentState.url,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                    style = typography.bodyMedium.copy(
                                        textDirection = TextDirection.Ltr
                                    )
                                )
                            }
                        }
                    )
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
            url = "https://malicious-site.com/"
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
                initialUrl = "https://shaparak.ir/"
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
            "در پایگاه داده برنامه مطمئن باش، اطلاعاتی درباره این آدرس اینترنتی موجود نیست. اگر از آن مطمئن هستید، می‌توانید ادامه دهید، اما در صورت تردید، احتیاط کنید.",
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
                initialUrl = "https://unknown-site.com/"
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