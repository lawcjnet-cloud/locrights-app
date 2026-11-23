package com.locrights.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "loc_rights_prefs")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val readerFontScale: Float = 1.0f,
    val notificationsEnabled: Boolean = false
)

class ThemePreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SCALE = floatPreferencesKey("reader_font_scale")
        val NOTIFICATIONS_OPT_IN = booleanPreferencesKey("notifications_opt_in")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
        UserSettings(
            themeMode = theme,
            readerFontScale = (prefs[Keys.FONT_SCALE] ?: 1.0f).coerceIn(0.85f, 1.4f),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_OPT_IN] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.4f) }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_OPT_IN] = enabled }
    }
}
