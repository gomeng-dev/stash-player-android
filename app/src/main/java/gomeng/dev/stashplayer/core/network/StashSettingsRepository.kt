package gomeng.dev.stashplayer.core.network

import android.content.Context
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.PlaybackEndAction
import gomeng.dev.stashplayer.core.player.SUBTITLE_FONT_SCALE_DEFAULT
import gomeng.dev.stashplayer.core.player.SubtitleLanguagePreference
import gomeng.dev.stashplayer.core.player.SubtitlePosition
import gomeng.dev.stashplayer.core.player.SubtitleTextAlignment
import gomeng.dev.stashplayer.core.player.coerceSubtitleFontScale
import gomeng.dev.stashplayer.core.ui.i18n.StashAppLanguage
import gomeng.dev.stashplayer.core.ui.theme.StashAccentColor
import gomeng.dev.stashplayer.core.ui.theme.StashThemeMode
import gomeng.dev.stashplayer.core.ui.theme.StashUiScale
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stashSettingsDataStore by preferencesDataStore(name = "stash_settings")

class StashSettingsRepository(private val context: Context) {
    val serverProfile: Flow<StashServerProfile?> = context.stashSettingsDataStore.data.map { prefs ->
        val baseUrl = prefs[Keys.BaseUrl].orEmpty()
        if (baseUrl.isBlank()) {
            null
        } else {
            StashServerProfile(
                name = prefs[Keys.Name].orEmpty().ifBlank { "Home" },
                baseUrl = baseUrl,
                apiKey = prefs[Keys.ApiKey].orEmpty(),
                authMode = stashServerAuthModeFromPersistedValue(prefs[Keys.AuthMode]),
                sessionCookie = prefs[Keys.SessionCookie].orEmpty(),
                allowInsecureLocalApiKey = prefs[Keys.AllowInsecureLocalApiKey] ?: false,
            )
        }
    }

    val playerDebugOverlayEnabled: Flow<Boolean> = context.stashSettingsDataStore.data.map { prefs ->
        prefs[Keys.PlayerDebugOverlayEnabled] ?: DEFAULT_PLAYER_DEBUG_OVERLAY_ENABLED
    }

    val themeMode: Flow<StashThemeMode> = context.stashSettingsDataStore.data.map { prefs ->
        themeModeFromPersistedValue(prefs[Keys.ThemeMode])
    }

    val appLanguage: Flow<StashAppLanguage> = context.stashSettingsDataStore.data.map { prefs ->
        appLanguageFromPersistedValue(prefs[Keys.AppLanguage])
    }

    val accentColor: Flow<StashAccentColor> = context.stashSettingsDataStore.data.map { prefs ->
        accentColorFromPersistedValue(prefs[Keys.AccentColor])
    }

    val uiScale: Flow<StashUiScale> = context.stashSettingsDataStore.data.map { prefs ->
        uiScaleFromPersistedValue(prefs[Keys.UiScale])
    }

    val defaultStreamPreference: Flow<StashStreamPreference> = context.stashSettingsDataStore.data.map { prefs ->
        defaultStreamPreferenceFromPersistedValue(prefs[Keys.DefaultStreamPreference])
    }

    val playbackEndAction: Flow<PlaybackEndAction> = context.stashSettingsDataStore.data.map { prefs ->
        playbackEndActionFromPersistedValue(prefs[Keys.PlaybackEndAction])
    }

    val backgroundPlaybackEnabled: Flow<Boolean> = context.stashSettingsDataStore.data.map { prefs ->
        prefs[Keys.BackgroundPlaybackEnabled] ?: DEFAULT_BACKGROUND_PLAYBACK_ENABLED
    }

    val pictureInPictureEnabled: Flow<Boolean> = context.stashSettingsDataStore.data.map { prefs ->
        prefs[Keys.PictureInPictureEnabled] ?: DEFAULT_PICTURE_IN_PICTURE_ENABLED
    }

    val playbackOrientationMode: Flow<PlaybackOrientationMode> = context.stashSettingsDataStore.data.map { prefs ->
        playbackOrientationModeFromPersistedValue(prefs[Keys.PlaybackOrientationMode])
    }

    val subtitleLanguage: Flow<SubtitleLanguagePreference> = context.stashSettingsDataStore.data.map { prefs ->
        subtitleLanguageFromPersistedValue(prefs[Keys.SubtitleLanguage])
    }

    val subtitleFontScale: Flow<Float> = context.stashSettingsDataStore.data.map { prefs ->
        subtitleFontScaleFromPersistedValue(prefs[Keys.SubtitleFontScale])
    }

    val subtitlePosition: Flow<SubtitlePosition> = context.stashSettingsDataStore.data.map { prefs ->
        subtitlePositionFromPersistedValue(prefs[Keys.SubtitlePosition])
    }

    val subtitleTextAlignment: Flow<SubtitleTextAlignment> = context.stashSettingsDataStore.data.map { prefs ->
        subtitleTextAlignmentFromPersistedValue(prefs[Keys.SubtitleTextAlignment])
    }

    suspend fun saveServerProfile(profile: StashServerProfile) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.Name] = profile.name.ifBlank { "Home" }
            prefs[Keys.BaseUrl] = profile.normalizedBaseUrl()
            prefs[Keys.ApiKey] = profile.apiKey.trim()
            prefs[Keys.AuthMode] = persistStashServerAuthMode(profile.authMode)
            prefs[Keys.SessionCookie] = profile.sessionCookie.trim()
            prefs[Keys.AllowInsecureLocalApiKey] = profile.allowInsecureLocalApiKey
        }
    }

    suspend fun setPlayerDebugOverlayEnabled(enabled: Boolean) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.PlayerDebugOverlayEnabled] = enabled
        }
    }

    suspend fun setThemeMode(mode: StashThemeMode) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.ThemeMode] = persistThemeModeValue(mode)
        }
    }

    suspend fun setAppLanguage(language: StashAppLanguage) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.AppLanguage] = persistAppLanguageValue(language)
        }
    }

    suspend fun setAccentColor(accentColor: StashAccentColor) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.AccentColor] = persistAccentColorValue(accentColor)
        }
    }

    suspend fun setUiScale(uiScale: StashUiScale) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.UiScale] = persistUiScaleValue(uiScale)
        }
    }

    suspend fun setDefaultStreamPreference(preference: StashStreamPreference) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.DefaultStreamPreference] = persistDefaultStreamPreferenceValue(preference)
        }
    }

    suspend fun setPlaybackEndAction(action: PlaybackEndAction) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.PlaybackEndAction] = persistPlaybackEndActionValue(action)
        }
    }

    suspend fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.BackgroundPlaybackEnabled] = enabled
        }
    }

    suspend fun setPictureInPictureEnabled(enabled: Boolean) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.PictureInPictureEnabled] = enabled
        }
    }

    suspend fun setPlaybackOrientationMode(mode: PlaybackOrientationMode) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.PlaybackOrientationMode] = persistPlaybackOrientationModeValue(mode)
        }
    }

    suspend fun setSubtitleLanguage(language: SubtitleLanguagePreference) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.SubtitleLanguage] = persistSubtitleLanguageValue(language)
        }
    }

    suspend fun setSubtitleFontScale(scale: Float) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.SubtitleFontScale] = persistSubtitleFontScaleValue(scale)
        }
    }

    suspend fun setSubtitlePosition(position: SubtitlePosition) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.SubtitlePosition] = persistSubtitlePositionValue(position)
        }
    }

    suspend fun setSubtitleTextAlignment(alignment: SubtitleTextAlignment) {
        context.stashSettingsDataStore.edit { prefs ->
            prefs[Keys.SubtitleTextAlignment] = persistSubtitleTextAlignmentValue(alignment)
        }
    }

    suspend fun clearServerProfile() {
        context.stashSettingsDataStore.edit { prefs ->
            prefs.remove(Keys.Name)
            prefs.remove(Keys.BaseUrl)
            prefs.remove(Keys.ApiKey)
            prefs.remove(Keys.AuthMode)
            prefs.remove(Keys.SessionCookie)
            prefs.remove(Keys.AllowInsecureLocalApiKey)
        }
    }

    private object Keys {
        val Name = stringPreferencesKey("server_name")
        val BaseUrl = stringPreferencesKey("server_base_url")
        val ApiKey = stringPreferencesKey("server_api_key")
        val AuthMode = stringPreferencesKey("server_auth_mode")
        val SessionCookie = stringPreferencesKey("server_session_cookie")
        val AllowInsecureLocalApiKey = booleanPreferencesKey("server_allow_insecure_local_api_key")
        val PlayerDebugOverlayEnabled = booleanPreferencesKey("player_debug_overlay_enabled")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val AppLanguage = stringPreferencesKey("app_language")
        val AccentColor = stringPreferencesKey("accent_color")
        val UiScale = stringPreferencesKey("ui_scale")
        val DefaultStreamPreference = stringPreferencesKey("default_stream_preference")
        val PlaybackEndAction = stringPreferencesKey("playback_end_action")
        val BackgroundPlaybackEnabled = booleanPreferencesKey("background_playback_enabled")
        val PictureInPictureEnabled = booleanPreferencesKey("picture_in_picture_enabled")
        val PlaybackOrientationMode = stringPreferencesKey("playback_orientation_mode")
        val SubtitleLanguage = stringPreferencesKey("subtitle_language")
        val SubtitleFontScale = floatPreferencesKey("subtitle_font_scale")
        val SubtitlePosition = stringPreferencesKey("subtitle_position")
        val SubtitleTextAlignment = stringPreferencesKey("subtitle_text_alignment")
    }

    companion object {
        const val DEFAULT_PLAYER_DEBUG_OVERLAY_ENABLED = false
        val DEFAULT_THEME_MODE: StashThemeMode = StashThemeMode.default
        val DEFAULT_APP_LANGUAGE: StashAppLanguage = StashAppLanguage.default
        val DEFAULT_ACCENT_COLOR: StashAccentColor = StashAccentColor.default
        val DEFAULT_UI_SCALE: StashUiScale = StashUiScale.default
        val DEFAULT_STREAM_PREFERENCE: StashStreamPreference = StashStreamPreference.Auto
        val DEFAULT_PLAYBACK_END_ACTION: PlaybackEndAction = PlaybackEndAction.default
        const val DEFAULT_BACKGROUND_PLAYBACK_ENABLED = false
        const val DEFAULT_PICTURE_IN_PICTURE_ENABLED = false
        val DEFAULT_PLAYBACK_ORIENTATION_MODE: PlaybackOrientationMode = PlaybackOrientationMode.default
        val DEFAULT_SUBTITLE_LANGUAGE: SubtitleLanguagePreference = SubtitleLanguagePreference.default
        const val DEFAULT_SUBTITLE_FONT_SCALE: Float = SUBTITLE_FONT_SCALE_DEFAULT
        val DEFAULT_SUBTITLE_POSITION: SubtitlePosition = SubtitlePosition.default
        val DEFAULT_SUBTITLE_TEXT_ALIGNMENT: SubtitleTextAlignment = SubtitleTextAlignment.default

        fun persistThemeModeValue(mode: StashThemeMode): String = mode.persistedValue

        fun themeModeFromPersistedValue(value: String?): StashThemeMode = StashThemeMode.fromPersistedValue(value)

        fun persistAppLanguageValue(language: StashAppLanguage): String = language.persistedValue

        fun appLanguageFromPersistedValue(value: String?): StashAppLanguage =
            StashAppLanguage.fromPersistedValue(value)

        fun persistAccentColorValue(accentColor: StashAccentColor): String = accentColor.persistedValue

        fun accentColorFromPersistedValue(value: String?): StashAccentColor =
            StashAccentColor.fromPersistedValue(value)

        fun persistUiScaleValue(uiScale: StashUiScale): String = uiScale.persistedValue

        fun uiScaleFromPersistedValue(value: String?): StashUiScale =
            StashUiScale.fromPersistedValue(value)

        fun persistDefaultStreamPreferenceValue(preference: StashStreamPreference): String = preference.id

        fun defaultStreamPreferenceFromPersistedValue(value: String?): StashStreamPreference =
            stashStreamPreferenceFromId(value)

        fun persistPlaybackEndActionValue(action: PlaybackEndAction): String = action.persistedValue

        fun playbackEndActionFromPersistedValue(value: String?): PlaybackEndAction =
            PlaybackEndAction.fromPersistedValue(value)

        fun persistPlaybackOrientationModeValue(mode: PlaybackOrientationMode): String = mode.persistedValue

        fun playbackOrientationModeFromPersistedValue(value: String?): PlaybackOrientationMode =
            PlaybackOrientationMode.fromPersistedValue(value)

        fun persistSubtitleLanguageValue(language: SubtitleLanguagePreference): String = language.persistedValue

        fun subtitleLanguageFromPersistedValue(value: String?): SubtitleLanguagePreference =
            SubtitleLanguagePreference.fromPersistedValue(value)

        fun persistSubtitleFontScaleValue(scale: Float): Float = coerceSubtitleFontScale(scale)

        fun subtitleFontScaleFromPersistedValue(value: Float?): Float = coerceSubtitleFontScale(value)

        fun persistSubtitlePositionValue(position: SubtitlePosition): String = position.persistedValue

        fun subtitlePositionFromPersistedValue(value: String?): SubtitlePosition =
            SubtitlePosition.fromPersistedValue(value)

        fun persistSubtitleTextAlignmentValue(alignment: SubtitleTextAlignment): String = alignment.persistedValue

        fun subtitleTextAlignmentFromPersistedValue(value: String?): SubtitleTextAlignment =
            SubtitleTextAlignment.fromPersistedValue(value)
    }
}
