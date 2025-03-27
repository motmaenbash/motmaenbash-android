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
import nu.milad.motmaenbash.utils.DatabaseHelper
import nu.milad.motmaenbash.utils.UrlUtils

class UrlScanViewModel(private val context: Application) : AndroidViewModel(context) {

    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(context) }

    val _state = MutableStateFlow<ScanState>(ScanState.Initial)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun scanUrl(urlToScan: String) {
        viewModelScope.launch {
            _state.value = ScanState.Loading

            val (resultText, isSafe) = when {
                UrlUtils.isShaparakSubdomain(urlToScan) -> Pair(
                    "این آدرس متعلق به یک درگاه پرداخت امن و مطمئن است.", true
                )

                dbHelper.isUrlFlagged(urlToScan) -> Pair(
                    "هشدار: این آدرس به عنوان یک آدرس مشکوک شناسایی شده است.", false
                )

                else -> Pair(
                    "در پایگاه داده ما اطلاعاتی درباره این آدرس اینترنتی موجود نیست. اگر از آن مطمئن هستید، می‌توانید ادامه دهید، اما در صورت تردید، احتیاط کنید.",
                    null
                )
            }

            _state.value = ScanState.Result(resultText, isSafe, urlToScan)
        }
    }

    fun getInitialUrl(intent: Intent?): String {
        return when (intent?.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            Intent.ACTION_PROCESS_TEXT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT).orEmpty()
                } else ""
            }

            else -> ""
        }.trim()
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