package nu.milad.motmaenbash.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import nu.milad.motmaenbash.consts.AppConstants

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.APP_PREFERENCES)
