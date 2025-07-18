package nu.milad.motmaenbash.viewmodels

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.utils.AudioHelper
import nu.milad.motmaenbash.utils.UpdateManager
import nu.milad.motmaenbash.utils.dataStore

open class SettingsViewModel(
    context: Application
) : AndroidViewModel(context) {

    // Preference Keys
    companion object {
        val DATABASE_UPDATE_FREQ = stringPreferencesKey("database_update_frequency")
        val THEME = stringPreferencesKey("theme")
        val FONT = stringPreferencesKey("font")
        val PLAY_SOUND_IN_SILENT_MODE = booleanPreferencesKey("play_alert_sound_in_silent_mode")
        val ALERT_SOUND = stringPreferencesKey("alert_sound")
        val SHOW_NEUTRAL_SMS_DIALOG = booleanPreferencesKey("show_neutral_sms_dialog")
        val PLAY_NEUTRAL_SMS_SOUND = booleanPreferencesKey("play_neutral_sms_sound")
        val NEUTRAL_SMS_SOUND = stringPreferencesKey("neutral_sms_sound")
    }


    // SoundPlayer instance for managing sound effects
    private val soundPlayer = AudioHelper(context)

    private val dataStore = context.dataStore

    open val preferences: StateFlow<Preferences> = dataStore.data
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyPreferences()
        )

    /**
     * Save a string preference and handle any related side effects
     */
    fun saveStringPreference(key: Preferences.Key<String>, value: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }

            when (key) {

                // Reschedule periodic updates
                DATABASE_UPDATE_FREQ -> UpdateManager(getApplication()).scheduleDatabaseUpdate()

                // Play sound feedback
                ALERT_SOUND, NEUTRAL_SMS_SOUND -> soundPlayer.playSound(value, true)
            }

        }
    }

    /**
     * Save a boolean preference
     */
    fun saveBooleanPreference(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }


    // get default values from resources
    fun getDefaultValue(context: Context, key: Preferences.Key<String>): String {
        return when (key) {

            ALERT_SOUND -> context.resources.getStringArray(R.array.alert_sound_values).first()

            // Select the second item from the array
            DATABASE_UPDATE_FREQ -> context.resources.getStringArray(R.array.database_update_frequency_values)[1]

            THEME -> context.resources.getStringArray(R.array.theme_values).first()

            FONT -> context.resources.getStringArray(R.array.font_values).first()


            else -> ""
        }
    }

    // Get default boolean value
    fun getDefaultBooleanValue(key: Preferences.Key<Boolean>): Boolean {
        return when (key) {
            PLAY_SOUND_IN_SILENT_MODE -> true
            SHOW_NEUTRAL_SMS_DIALOG -> false
            PLAY_NEUTRAL_SMS_SOUND -> false
            else -> false
        }
    }


}