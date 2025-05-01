package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.DatabaseHelper
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
                is UrlAnalysisResult.SafeUrl -> Pair(
                    "این آدرس متعلق به یک درگاه پرداخت امن و مطمئن است.", true
                )

                is UrlAnalysisResult.SuspiciousUrl -> Pair(
                    "هشدار: این آدرس به عنوان یک آدرس مشکوک شناسایی شده است.", false
                )

                is UrlAnalysisResult.NeutralUrl -> Pair(
                    "در پایگاه داده ما اطلاعاتی درباره این آدرس اینترنتی موجود نیست. اگر از آن مطمئن هستید، می‌توانید ادامه دهید، اما در صورت تردید، احتیاط کنید.",
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