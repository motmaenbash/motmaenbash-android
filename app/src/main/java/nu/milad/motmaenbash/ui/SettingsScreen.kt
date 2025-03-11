package nu.milad.motmaenbash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.ui.theme.GreyDark
import nu.milad.motmaenbash.utils.SettingsManager
import nu.milad.motmaenbash.utils.dataStore

@Composable
fun SettingsScreen(settingsManager: SettingsManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = settingsManager.preferencesFlow.collectAsState(initial = emptyPreferences())


    AppBar(
        title = stringResource(id = R.string.settings_activity_title),
    ) { contentPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PreferenceCategory(title = "هشدارها", icon = Icons.Outlined.Notifications) {


                ListPreference(
                    title = stringResource(id = R.string.setting_alert_silent_mode),
                    entries = context.resources.getStringArray(R.array.play_sound_in_silent_mode)
                        .toList(),
                    values = context.resources.getStringArray(R.array.play_sound_in_silent_mode_values)
                        .toList(),
                    currentValue = prefs.value[SettingsManager.PLAY_SOUND_IN_SILENT_MODE]
                        ?: context.resources.getStringArray(R.array.play_sound_in_silent_mode_values)
                            .first(),
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(
                                SettingsManager.PLAY_SOUND_IN_SILENT_MODE, newValue
                            )
                        }
                    })

                ListPreference(
                    title = stringResource(id = R.string.setting_alert_sound),
                    entries = context.resources.getStringArray(R.array.alert_sound).toList(),
                    values = context.resources.getStringArray(R.array.alert_sound_values).toList(),
                    currentValue = prefs.value[SettingsManager.ALERT_SOUND]
                        ?: context.resources.getStringArray(R.array.alert_sound_values).first(),
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(
                                SettingsManager.ALERT_SOUND, newValue
                            )
                        }
                    })
            }

            PreferenceCategory(
                title = "بروزرسانی پایگاه داده", icon = Icons.Outlined.Refresh,
            ) {
                ListPreference(
                    title = stringResource(id = R.string.setting_database_update_frequency),
                    entries = context.resources.getStringArray(R.array.database_update_frequency)
                        .toList(),
                    values = context.resources.getStringArray(R.array.database_update_frequency_values)
                        .toList(),
                    currentValue = prefs.value[SettingsManager.DATABASE_UPDATE_FREQ]
                        ?: context.resources.getStringArray(R.array.database_update_frequency_values)
                            .first(),
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(
                                SettingsManager.DATABASE_UPDATE_FREQ, newValue
                            )
                        }
                    })
            }

            PreferenceCategory(title = "ظاهر", icon = Icons.Outlined.Menu) {
                ListPreference(
                    title = stringResource(id = R.string.setting_theme),
                    entries = context.resources.getStringArray(R.array.theme).toList(),
                    values = context.resources.getStringArray(R.array.theme_values).toList(),
                    currentValue = prefs.value[SettingsManager.THEME]
                        ?: context.resources.getStringArray(R.array.theme_values).first(),
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(SettingsManager.THEME, newValue)
                        }
                    })

                ListPreference(
                    title = stringResource(id = R.string.setting_font),
                    entries = context.resources.getStringArray(R.array.font).toList(),
                    values = context.resources.getStringArray(R.array.font_values).toList(),
                    currentValue = prefs.value[SettingsManager.FONT]
                        ?: context.resources.getStringArray(R.array.font_values).first(),
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(SettingsManager.FONT, newValue)
                        }
                    })
            }

        }
    }
}

@Composable
fun PreferenceCategory(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorPrimary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ColorPrimary
            )
        }
        content()

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp),

            thickness = .8.dp, color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPreference(
    title: String,
    entries: List<String>,
    values: List<String>,
    currentValue: String,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val index = values.indexOf(currentValue)
    val displayValue = if (index != -1) entries[index] else ""

    Text(
        text = title,
        modifier = Modifier.padding(start = 48.dp, top = 32.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = GreyDark
    )
    ExposedDropdownMenuBox(
        modifier = Modifier.padding(start = 64.dp, end = 24.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it }) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = displayValue,
            onValueChange = {},
            textStyle = MaterialTheme.typography.titleSmall,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RectangleShape,
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = {
                        Text(
                            entry, style = MaterialTheme.typography.titleSmall

                        )
                    }, onClick = {
                        onValueSelected(values[index])
                        expanded = false
                    }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }


}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val settingsManager = SettingsManager(LocalContext.current.dataStore)
    SettingsScreen(settingsManager)
}
