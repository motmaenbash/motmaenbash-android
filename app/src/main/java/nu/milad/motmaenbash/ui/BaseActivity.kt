package nu.milad.motmaenbash.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale("fa")
    }


    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            createConfigurationContext(config)

        } else {
            config.locale = locale
            config.setLayoutDirection(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}