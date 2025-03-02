package nu.milad.motmaenbash.ui


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.ui.ui.theme.MotmaenBashTheme
import java.util.Locale

open class BaseComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale()
        setContent {
            // Apply theme and content for your activities
            MotmaenBashTheme {
                // Your activity's content will go here
                // Replace this with a placeholder or specific Composable
                // FaqScreen() or any other screen
            }
        }
//        applyTheme()
    }

    private fun setLocale() {
        val locale = Locale("fa")
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

//    private fun applyTheme() {
//        CoroutineScope(Dispatchers.Main).launch {
//            val theme = dataStore.data.map { preferences ->
//                preferences[stringPreferencesKey("theme")] ?: "light"
//            }.first()
//
//            when (theme) {
//                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            }
//        }
//    }
}