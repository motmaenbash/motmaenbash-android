package nu.milad.motmaenbash

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.consts.AppConstants.PREF_KEY_FIRST_LAUNCH
import java.util.Locale


class App : Application() {


    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.APP_PREFERENCES)

    override fun onCreate() {
        super.onCreate()

        // Set default locale
        Locale.setDefault(Locale("fa"))

        // Launch initialization in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            initializeApp()
        }

    }

    private suspend fun initializeApp() {


        try {

            val isFirstLaunch = dataStore.data.firstOrNull()?.get(
                booleanPreferencesKey(PREF_KEY_FIRST_LAUNCH)
            ) ?: false




            if (!isFirstLaunch) {
                dataStore.edit { preferences ->
                    preferences[booleanPreferencesKey(PREF_KEY_FIRST_LAUNCH)] = true
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()

        }

    }
}

