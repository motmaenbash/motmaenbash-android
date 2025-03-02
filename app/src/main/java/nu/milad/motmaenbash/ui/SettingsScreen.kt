package nu.milad.motmaenbash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.emptyPreferences
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.ui.theme.ColorPrimary
import nu.milad.motmaenbash.utils.SettingsManager
import nu.milad.motmaenbash.utils.dataStore

@Composable
fun SettingsScreen(navController: NavController, settingsManager: SettingsManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = settingsManager.preferencesFlow.collectAsState(initial = emptyPreferences())

    AppBar(
        title = stringResource(id = R.string.settings_activity_title),
        onNavigationIconClick = { navController.navigateUp() },
        onActionClick = { /* Handle menu action */ },
    ) { contentPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PreferenceCategory(title = "بروزرسانی پایگاه داده", icon = Icons.Default.Refresh) {
                ListPreference(
                    title = "فرکانس بروزرسانی",
                    entries = context.resources.getStringArray(R.array.database_update_frequency)
                        .toList(),
                    values = context.resources.getStringArray(R.array.database_update_frequency_values)
                        .toList(),
                    currentValue = prefs.value[SettingsManager.DATABASE_UPDATE_FREQ] ?: "daily",
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(
                                SettingsManager.DATABASE_UPDATE_FREQ,
                                newValue
                            )
                        }
                    }
                )
            }

            PreferenceCategory(title = "ظاهر", icon = Icons.Default.Menu) {
                ListPreference(title = "انتخاب تم",
                    entries = context.resources.getStringArray(R.array.theme).toList(),
                    values = context.resources.getStringArray(R.array.theme_values).toList(),
                    currentValue = prefs.value[SettingsManager.THEME] ?: "light",
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(SettingsManager.THEME, newValue)
                        }
                    })

                ListPreference(title = "انتخاب فونت",
                    entries = context.resources.getStringArray(R.array.font).toList(),
                    values = context.resources.getStringArray(R.array.font_values).toList(),
                    currentValue = prefs.value[SettingsManager.FONT] ?: "vazir",
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(SettingsManager.FONT, newValue)
                        }
                    })
            }

            PreferenceCategory(title = "هشدارها", icon = Icons.Default.Notifications) {
                ListPreference(title = "صدای هشدار",
                    entries = context.resources.getStringArray(R.array.alert_sound).toList(),
                    values = context.resources.getStringArray(R.array.alert_sound_values).toList(),
                    currentValue = prefs.value[SettingsManager.ALERT_SOUND] ?: "",
                    onValueSelected = { newValue ->
                        scope.launch {
                            settingsManager.saveStringPreference(
                                SettingsManager.ALERT_SOUND,
                                newValue
                            )
                        }
                    })
            }
        }
    }
}

@Composable
fun PreferenceCategory(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = ColorPrimary)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary
                )
            }
            content()
        }
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .padding(vertical = 4.dp),
            readOnly = true,
            value = displayValue,
            onValueChange = {},
            textStyle = MaterialTheme.typography.bodyMedium,
            label = { Text(title, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = { Text(entry) },
                    onClick = {
                        onValueSelected(values[index])
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val settingsManager = SettingsManager(LocalContext.current.dataStore)
    SettingsScreen(rememberNavController(), settingsManager)
}
