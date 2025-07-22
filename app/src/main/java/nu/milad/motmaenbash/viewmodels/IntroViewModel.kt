package nu.milad.motmaenbash.viewmodels

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.consts.AppConstants
import nu.milad.motmaenbash.utils.dataStore

class IntroViewModel(private val context: Application) :
    BasePermissionViewModel(context) {

    init {
        checkInitialPermissions()
    }

    // Mark intro as shown (completed)
    suspend fun setHasSeenIntro() {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(AppConstants.PREF_KEY_HAS_SEEN_INTRO)] = true
        }
    }
    
    fun updateNormalSmsNotificationSetting(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[SettingsViewModel.SHOW_NEUTRAL_SMS_DIALOG] = enabled
            }
        }
    }
}