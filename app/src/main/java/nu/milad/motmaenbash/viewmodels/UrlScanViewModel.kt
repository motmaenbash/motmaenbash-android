package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.GatewayCertificateChecker
import nu.milad.motmaenbash.utils.TextUtils
import nu.milad.motmaenbash.utils.UrlUtils

class UrlScanViewModel(private val context: Application) : AndroidViewModel(context) {

    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(context) }

    val _state = MutableStateFlow<ScanState>(ScanState.Initial)
    val state: StateFlow<ScanState> = _state.asStateFlow()
    private val audioHelper by lazy {
        AudioHelper(context)
    }

    fun scanUrl(urlToScan: String) {
        viewModelScope.launch {
            _state.value = ScanState.Loading

            val result = UrlUtils.analyzeUrl(url = urlToScan, databaseHelper = dbHelper)


            val (resultText, isSafe) = when (result) {
                is UrlAnalysisResult.VerifiedPaymentGatewayUrl -> {
                    val certificateStatus = withContext(Dispatchers.IO) {
                        GatewayCertificateChecker.check(UrlUtils.extractHost(urlToScan))
                    }

                    when (certificateStatus) {
                        GatewayCertificateChecker.Status.TRUSTED -> Pair(
                            "این آدرس متعلق به یک درگاه پرداخت امن و مطمئن است.", true
                        )

                        GatewayCertificateChecker.Status.USER_INSTALLED_ROOT -> Pair(
                            "هشدار: گواهی امنیتی این درگاه را مرجعی صادر کرده که روی دستگاه شما نصب شده است و ممکن است ارتباط شما شنود شود. اطلاعات کارت خود را در آن وارد نکنید.",
                            false
                        )

                        GatewayCertificateChecker.Status.UNTRUSTED -> Pair(
                            "هشدار: گواهی امنیتی این درگاه پرداخت معتبر نیست. اطلاعات کارت خود را در آن وارد نکنید.",
                            false
                        )

                        GatewayCertificateChecker.Status.UNKNOWN -> Pair(
                            "این آدرس متعلق به درگاه‌های پرداخت شاپرک است، اما گواهی امنیتی آن بررسی نشد. اتصال اینترنت خود را بررسی کنید.",
                            null
                        )
                    }
                }

                is UrlAnalysisResult.SuspiciousUrl -> Pair(
                    "هشدار: این آدرس به عنوان یک آدرس مشکوک شناسایی شده است.", false
                )

                is UrlAnalysisResult.NeutralUrl -> Pair(
                    "در پایگاه داده برنامه مطمئن باش، اطلاعاتی درباره این آدرس اینترنتی موجود نیست. اگر از آن مطمئن هستید، می‌توانید ادامه دهید، اما در صورت تردید، احتیاط کنید.",
                    null
                )
            }

            if (isSafe == false) {
                audioHelper.vibrateDevice(context)
                audioHelper.playAlertSound()
            }

            _state.value = ScanState.Result(resultText, isSafe, urlToScan)
        }
    }

    fun getInitialUrl(intent: Intent?): String {
        val text = when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            Intent.ACTION_PROCESS_TEXT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT).orEmpty()
                } else ""
            }

            else -> ""
        }.trim()

        // Extract first URL from shared text
        val links = TextUtils.extractLinks(text)
        return links.firstOrNull() ?: ""
    }

    fun resetResult() {
        _state.value = ScanState.Initial
    }

    sealed class ScanState {
        data object Initial : ScanState()
        data object Loading : ScanState()
        data class Result(
            val message: String,
            val isSafe: Boolean?,
            val url: String
        ) : ScanState()

        data class Error(val message: String) : ScanState()
    }
}