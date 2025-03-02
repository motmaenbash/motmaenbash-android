package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.util.Log
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


    private val _resultText = MutableStateFlow("")
    val resultText: StateFlow<String> = _resultText.asStateFlow()

    private val _isSafe = MutableStateFlow<Boolean?>(null)
    val isSafe: StateFlow<Boolean?> = _isSafe.asStateFlow()

    private val _showResult = MutableStateFlow(false)
    val showResult: StateFlow<Boolean> = _showResult.asStateFlow()

    init {

        Log.d("scan", "initttt")
    }

    fun scanUrl(urlToScan: String) {
        viewModelScope.launch {
            val (resultText, isSafe) = when {
                UrlUtils.isShaparakSubdomain(urlToScan) -> Pair(
                    "این آدرس متعلق به یک درگاه پرداخت مطمئن است.", true
                )

                dbHelper.isUrlFlagged(urlToScan) -> Pair(
                    "این آدرس به عنوان یک آدرس مشکوک شناسایی شده است.", false
                )

                else -> Pair("این آدرس در پایگاه داده ما موجود نیست. لطفا احتیاط کنید.", null)
            }
            _resultText.value = resultText
            _isSafe.value = isSafe
            _showResult.value = true
        }
    }

    fun resetResult() {
        _showResult.value = false
    }
}
