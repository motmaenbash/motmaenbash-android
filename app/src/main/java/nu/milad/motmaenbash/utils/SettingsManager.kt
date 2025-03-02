package nu.milad.motmaenbash.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

open class SettingsManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        val DATABASE_UPDATE_FREQ = stringPreferencesKey("database_update_frequency")
        val THEME = stringPreferencesKey("theme")
        val FONT = stringPreferencesKey("font_selection")
        val ALERT_SOUND = stringPreferencesKey("alert_sound")
    }

    suspend fun saveStringPreference(key: Preferences.Key<String>, value: String) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }


    val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            emit(emptyPreferences())
        }

}