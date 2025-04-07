package nu.milad.motmaenbash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import nu.milad.motmaenbash.R
import nu.milad.motmaenbash.ui.activities.LocalNavController
import nu.milad.motmaenbash.ui.components.AppBar
import nu.milad.motmaenbash.ui.components.FontInfoDialog
import nu.milad.motmaenbash.ui.theme.ColorPrimary
import nu.milad.motmaenbash.ui.theme.MotmaenBashTheme
import nu.milad.motmaenbash.viewmodels.SettingsViewModel

// Data model for preference categories
data class PreferenceCategoryModel(
    val title: String,
    val icon: ImageVector,
    val preferences: List<PreferenceItem>
)

// Data model for preference items
sealed class PreferenceItem

data class ListPreferenceItem(
    val title: String,
    val entries: List<String>,
    val values: List<String>,
    val currentValue: String,
    val onValueSelected: (String) -> Unit,
    val showInfoIcon: Boolean = false,
    val onInfoClick: (() -> Unit)? = null
) : PreferenceItem()

data class SpacerItem(val height: Int) : PreferenceItem()

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showFontInfoDialog by remember { mutableStateOf(false) }

    // Collect the preferences state from ViewModel
    val prefs by viewModel.preferences.collectAsState()

    // Create preference categories with their items
    val preferenceCategories = listOf(
        PreferenceCategoryModel(
            title = "هشدارها",
            icon = Icons.Outlined.Notifications,
            preferences = listOf(
                ListPreferenceItem(
                    title = stringResource(id = R.string.setting_alert_silent_mode),
                    entries = context.resources.getStringArray(R.array.play_sound_in_silent_mode)
                        .toList(),
                    values = context.resources.getStringArray(R.array.play_sound_in_silent_mode_values)
                        .toList(),
                    currentValue = ((prefs[SettingsViewModel.PLAY_SOUND_IN_SILENT_MODE]
                        ?: viewModel.getDefaultBooleanValue(
                            context,
                            SettingsViewModel.PLAY_SOUND_IN_SILENT_MODE
                        )).toString()),
                    onValueSelected = { newValue ->
                        viewModel.saveBooleanPreference(
                            SettingsViewModel.PLAY_SOUND_IN_SILENT_MODE,
                            newValue.toBoolean()
                        )
                    }
                ),
                ListPreferenceItem(
                    title = stringResource(id = R.string.setting_alert_sound),
                    entries = context.resources.getStringArray(R.array.alert_sound).toList(),
                    values = context.resources.getStringArray(R.array.alert_sound_values).toList(),
                    currentValue = prefs[SettingsViewModel.ALERT_SOUND]
                        ?: viewModel.getDefaultValue(context, SettingsViewModel.ALERT_SOUND),
                    onValueSelected = { newValue ->
                        viewModel.saveStringPreference(SettingsViewModel.ALERT_SOUND, newValue)
                    }
                )
            )
        ),
        PreferenceCategoryModel(
            title = "بروزرسانی پایگاه داده",
            icon = Icons.Outlined.Refresh,
            preferences = listOf(
                ListPreferenceItem(
                    title = stringResource(id = R.string.setting_database_update_frequency),
                    entries = context.resources.getStringArray(R.array.database_update_frequency)
                        .toList(),
                    values = context.resources.getStringArray(R.array.database_update_frequency_values)
                        .toList(),
                    currentValue = prefs[SettingsViewModel.DATABASE_UPDATE_FREQ]
                        ?: viewModel.getDefaultValue(
                            context,
                            SettingsViewModel.DATABASE_UPDATE_FREQ
                        ),
                    onValueSelected = { newValue ->
                        viewModel.saveStringPreference(
                            SettingsViewModel.DATABASE_UPDATE_FREQ,
                            newValue
                        )
                    }
                )
            )
        ),
        PreferenceCategoryModel(
            title = "ظاهر",
            icon = Icons.Outlined.Menu,
            preferences = listOf(
                ListPreferenceItem(
                    title = stringResource(id = R.string.setting_theme),
                    entries = context.resources.getStringArray(R.array.theme).toList(),
                    values = context.resources.getStringArray(R.array.theme_values).toList(),
                    currentValue = prefs[SettingsViewModel.THEME]
                        ?: viewModel.getDefaultValue(context, SettingsViewModel.THEME),
                    onValueSelected = { newValue ->
                        viewModel.saveStringPreference(SettingsViewModel.THEME, newValue)
                    }
                ),
                ListPreferenceItem(
                    title = stringResource(id = R.string.setting_font),
                    entries = context.resources.getStringArray(R.array.font).toList(),
                    values = context.resources.getStringArray(R.array.font_values).toList(),
                    currentValue = prefs[SettingsViewModel.FONT]
                        ?: viewModel.getDefaultValue(context, SettingsViewModel.FONT),
                    onValueSelected = { newValue ->
                        viewModel.saveStringPreference(SettingsViewModel.FONT, newValue)
                    },
                    showInfoIcon = true,
                    onInfoClick = { showFontInfoDialog = true }
                )
            )
        )
    )

    AppBar(
        title = stringResource(id = R.string.settings_activity_title),
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            preferenceCategories.forEachIndexed { index, category ->
                item {
                    PreferenceCategoryHeader(title = category.title, icon = category.icon)
                }

                items(category.preferences) { preference ->
                    when (preference) {
                        is ListPreferenceItem -> {
                            ListPreference(
                                title = preference.title,
                                entries = preference.entries,
                                values = preference.values,
                                currentValue = preference.currentValue,
                                onValueSelected = preference.onValueSelected,
                                showInfoIcon = preference.showInfoIcon,
                                onInfoClick = preference.onInfoClick
                            )
                        }

                        is SpacerItem -> {
                            Spacer(modifier = Modifier.height(preference.height.dp))
                        }
                    }
                }

                // Add spacer between categories (except after the last one)
                if (index < preferenceCategories.size - 1) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Font info dialog
    if (showFontInfoDialog) {
        FontInfoDialog(onDismiss = { showFontInfoDialog = false })
    }
}

/**
 * UI component for preference category header
 */
@Composable
fun PreferenceCategoryHeader(title: String, icon: ImageVector) {
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
            style = typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ColorPrimary
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
    onValueSelected: (String) -> Unit,
    showInfoIcon: Boolean = false,
    onInfoClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val index = values.indexOf(currentValue)
    val displayValue = if (index != -1) entries[index] else ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 48.dp, top = 24.dp),
            style = typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        // info icon
        if (showInfoIcon) {
            IconButton(
                onClick = { onInfoClick?.invoke() },
                modifier = Modifier
                    .padding(top = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "اطلاعات بیشتر",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(start = 60.dp, end = 24.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            readOnly = true,
            value = displayValue,
            onValueChange = {},
            textStyle = typography.titleSmall,
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
            modifier = Modifier
                .exposedDropdownSize()
                .background(colorScheme.surface),
        ) {
            entries.forEachIndexed { index, entry ->
                DropdownMenuItem(
                    text = {
                        Text(
                            entry, style = typography.titleSmall
                        )
                    },
                    onClick = {
                        onValueSelected(values[index])
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CompositionLocalProvider(LocalNavController provides rememberNavController()) {
        MotmaenBashTheme {
            SettingsScreen()
        }
    }
}