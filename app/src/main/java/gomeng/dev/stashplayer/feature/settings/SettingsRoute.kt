package gomeng.dev.stashplayer.feature.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.network.AppUpdateChecker
import gomeng.dev.stashplayer.core.network.AppUpdateInstaller
import gomeng.dev.stashplayer.core.network.AppUpdateNotice
import gomeng.dev.stashplayer.core.network.STASH_ANDROID_LATEST_RELEASE_API_URL
import gomeng.dev.stashplayer.core.network.STASH_ANDROID_RELEASES_URL
import gomeng.dev.stashplayer.core.debug.StashDebugLogBuffer
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashLoginClient
import gomeng.dev.stashplayer.core.network.StashPluginRecommendationStatusClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.StashStreamPreference
import gomeng.dev.stashplayer.core.network.canAttemptStashCredentialTransport
import gomeng.dev.stashplayer.core.network.StashServerAuthMode
import gomeng.dev.stashplayer.core.network.toSettingsStatusCopy
import gomeng.dev.stashplayer.core.player.FastPlaybackHoldSpeedPreference
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.PlaybackEndAction
import gomeng.dev.stashplayer.core.player.SUBTITLE_FONT_SCALE_DEFAULT
import gomeng.dev.stashplayer.core.player.SUBTITLE_FONT_SCALE_MAX
import gomeng.dev.stashplayer.core.player.SUBTITLE_FONT_SCALE_MIN
import gomeng.dev.stashplayer.core.player.SubtitleLanguagePreference
import gomeng.dev.stashplayer.core.player.SubtitlePosition
import gomeng.dev.stashplayer.core.player.SubtitleTextAlignment
import gomeng.dev.stashplayer.core.player.coerceSubtitleFontScale
import gomeng.dev.stashplayer.core.security.DeviceAuthenticationAvailability
import gomeng.dev.stashplayer.core.ui.i18n.StashAppLanguage
import gomeng.dev.stashplayer.core.ui.theme.StashAccentColor
import gomeng.dev.stashplayer.core.ui.theme.StashThemeMode
import gomeng.dev.stashplayer.core.ui.theme.StashUiScale
import gomeng.dev.stashplayer.core.ui.theme.stashThemeColorSnapshot
import gomeng.dev.stashplayer.feature.security.deviceAuthenticationAvailabilityDescriptionRes
import gomeng.dev.stashplayer.feature.security.rememberDeviceAuthenticationAvailability
import gomeng.dev.stashplayer.feature.security.rememberDeviceAuthenticationLauncher
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

object SettingsDestinations {
    const val Root = "settings"
    const val Server = "settings/server"
    const val Security = "settings/security"
    const val Playback = "settings/playback"
    const val Appearance = "settings/appearance"
    const val Interface = "settings/interface"
    const val Development = "settings/development"
    const val Support = "settings/support"

    fun isSettingsRoute(route: String?): Boolean = route == Root || route?.startsWith("$Root/") == true
}

enum class SettingsSection(
    val route: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
) {
    Server(
        SettingsDestinations.Server,
        R.string.settings_category_server_title,
        R.string.settings_category_server_description,
    ),
    Security(
        SettingsDestinations.Security,
        R.string.settings_category_security_title,
        R.string.settings_category_security_description,
    ),
    Playback(
        SettingsDestinations.Playback,
        R.string.settings_category_playback_title,
        R.string.settings_category_playback_description,
    ),
    Appearance(
        SettingsDestinations.Appearance,
        R.string.settings_category_appearance_title,
        R.string.settings_category_appearance_description,
    ),
    Interface(
        SettingsDestinations.Interface,
        R.string.settings_category_interface_title,
        R.string.settings_category_interface_description,
    ),
    Support(
        SettingsDestinations.Support,
        R.string.settings_category_support_title,
        R.string.settings_category_support_description,
    ),
    Development(
        SettingsDestinations.Development,
        R.string.settings_category_development_title,
        R.string.settings_category_development_description,
    );
}

object SettingsListCopy {
    val sections: List<SettingsSection> = SettingsSection.entries
}

object PlayerDebugOverlaySettingCopy {
    @StringRes val sectionTitle = R.string.settings_category_development_title
    @StringRes val title = R.string.settings_player_debug_overlay_title
    @StringRes val description = R.string.settings_player_debug_overlay_description
}

object BiometricAppLockSettingCopy {
    @StringRes val sectionTitle = R.string.settings_category_security_title
    @StringRes val title = R.string.settings_biometric_app_lock_title
    @StringRes val description = R.string.settings_biometric_app_lock_description
}

object RecentAppsPrivacySettingCopy {
    @StringRes val sectionTitle = R.string.settings_category_security_title
    @StringRes val title = R.string.settings_recent_apps_privacy_title
    @StringRes val description = R.string.settings_recent_apps_privacy_description
}

object SupportSettingCopy {
    const val releasesUrl = STASH_ANDROID_RELEASES_URL
    const val latestReleaseApiUrl = STASH_ANDROID_LATEST_RELEASE_API_URL
    const val issuesUrl = "https://github.com/gomeng-dev/stash-player-android/issues"

    @StringRes val versionTitle = R.string.settings_support_version_title
    @StringRes val updateCheckTitle = R.string.settings_support_update_check_title
    @StringRes val issueTitle = R.string.settings_support_issue_title
}

object DebugLogSettingCopy {
    @StringRes val title = R.string.settings_debug_log_title
    @StringRes val description = R.string.settings_debug_log_description
    @StringRes val empty = R.string.settings_debug_log_empty
    @StringRes val copy = R.string.settings_debug_log_copy_button
    @StringRes val clear = R.string.settings_debug_log_clear_button
}

object PlaybackEndActionSettingCopy {
    @StringRes val title = R.string.settings_playback_end_action_title
    @StringRes val description = R.string.settings_playback_end_action_description

    val options: List<PlaybackEndActionOption> = listOf(
        PlaybackEndActionOption(PlaybackEndAction.Stop, labelFor(PlaybackEndAction.Stop), descriptionFor(PlaybackEndAction.Stop)),
        PlaybackEndActionOption(PlaybackEndAction.Repeat, labelFor(PlaybackEndAction.Repeat), descriptionFor(PlaybackEndAction.Repeat)),
        PlaybackEndActionOption(PlaybackEndAction.PlayNext, labelFor(PlaybackEndAction.PlayNext), descriptionFor(PlaybackEndAction.PlayNext)),
    )

    @StringRes
    fun labelFor(action: PlaybackEndAction): Int = when (action) {
        PlaybackEndAction.Stop -> R.string.settings_playback_end_action_stop_label
        PlaybackEndAction.Repeat -> R.string.settings_playback_end_action_repeat_label
        PlaybackEndAction.PlayNext -> R.string.settings_playback_end_action_play_next_label
    }

    @StringRes
    fun descriptionFor(action: PlaybackEndAction): Int = when (action) {
        PlaybackEndAction.Stop -> R.string.settings_playback_end_action_stop_description
        PlaybackEndAction.Repeat -> R.string.settings_playback_end_action_repeat_description
        PlaybackEndAction.PlayNext -> R.string.settings_playback_end_action_play_next_description
    }
}

data class PlaybackEndActionOption(
    val action: PlaybackEndAction,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object BackgroundPlaybackSettingCopy {
    @StringRes val title = R.string.settings_background_playback_title
    @StringRes val description = R.string.settings_background_playback_description
}

object PictureInPictureSettingCopy {
    @StringRes val title = R.string.settings_pip_title
    @StringRes val description = R.string.settings_pip_description
}

object ShortsMaxDurationSettingCopy {
    @StringRes val title = R.string.settings_shorts_max_duration_title
    @StringRes val description = R.string.settings_shorts_max_duration_description
    @StringRes val valueLabel = R.string.settings_shorts_max_duration_value_label
    val sliderValueRange = 60f..180f
    val sliderSteps = 119
}

object ShortsRecommendationResetSettingCopy {
    @StringRes val title = R.string.settings_shorts_recommendation_reset_title
    @StringRes val description = R.string.settings_shorts_recommendation_reset_description
    @StringRes val button = R.string.settings_shorts_recommendation_reset_button
    @StringRes val confirmTitle = R.string.settings_shorts_recommendation_reset_confirm_title
    @StringRes val confirmMessage = R.string.settings_shorts_recommendation_reset_confirm_message
    @StringRes val confirm = R.string.settings_shorts_recommendation_reset_confirm_button
    @StringRes val cancel = R.string.settings_shorts_recommendation_reset_cancel_button
    @StringRes val success = R.string.settings_shorts_recommendation_reset_success
}

object PlaybackSettingsCopy {
    val visibleCardTitles: List<Int> = listOf(
        DefaultStreamPreferenceSettingCopy.title,
        PlaybackEndActionSettingCopy.title,
        FastPlaybackHoldSpeedSettingCopy.title,
        BackgroundPlaybackSettingCopy.title,
        PictureInPictureSettingCopy.title,
        ShortsMaxDurationSettingCopy.title,
        ShortsRecommendationResetSettingCopy.title,
        SubtitleLanguageSettingCopy.title,
        SubtitleFontScaleSettingCopy.title,
        SubtitlePositionSettingCopy.title,
        SubtitleTextAlignmentSettingCopy.title,
    )
}

object FastPlaybackHoldSpeedSettingCopy {
    @StringRes val title = R.string.settings_fast_playback_hold_speed_title
    @StringRes val description = R.string.settings_fast_playback_hold_speed_description

    val options: List<FastPlaybackHoldSpeedOption> = listOf(
        FastPlaybackHoldSpeedOption(FastPlaybackHoldSpeedPreference.Off, labelFor(FastPlaybackHoldSpeedPreference.Off), descriptionFor(FastPlaybackHoldSpeedPreference.Off)),
        FastPlaybackHoldSpeedOption(FastPlaybackHoldSpeedPreference.OnePointTwentyFive, labelFor(FastPlaybackHoldSpeedPreference.OnePointTwentyFive), descriptionFor(FastPlaybackHoldSpeedPreference.OnePointTwentyFive)),
        FastPlaybackHoldSpeedOption(FastPlaybackHoldSpeedPreference.OnePointFive, labelFor(FastPlaybackHoldSpeedPreference.OnePointFive), descriptionFor(FastPlaybackHoldSpeedPreference.OnePointFive)),
        FastPlaybackHoldSpeedOption(FastPlaybackHoldSpeedPreference.OnePointSeventyFive, labelFor(FastPlaybackHoldSpeedPreference.OnePointSeventyFive), descriptionFor(FastPlaybackHoldSpeedPreference.OnePointSeventyFive)),
        FastPlaybackHoldSpeedOption(FastPlaybackHoldSpeedPreference.TwoPointZero, labelFor(FastPlaybackHoldSpeedPreference.TwoPointZero), descriptionFor(FastPlaybackHoldSpeedPreference.TwoPointZero)),
    )

    @StringRes
    fun labelFor(speedPreference: FastPlaybackHoldSpeedPreference): Int = when (speedPreference) {
        FastPlaybackHoldSpeedPreference.Off -> R.string.settings_fast_playback_hold_speed_off_label
        FastPlaybackHoldSpeedPreference.OnePointTwentyFive -> R.string.settings_fast_playback_hold_speed_125_label
        FastPlaybackHoldSpeedPreference.OnePointFive -> R.string.settings_fast_playback_hold_speed_150_label
        FastPlaybackHoldSpeedPreference.OnePointSeventyFive -> R.string.settings_fast_playback_hold_speed_175_label
        FastPlaybackHoldSpeedPreference.TwoPointZero -> R.string.settings_fast_playback_hold_speed_200_label
    }

    @StringRes
    fun descriptionFor(speedPreference: FastPlaybackHoldSpeedPreference): Int = when (speedPreference) {
        FastPlaybackHoldSpeedPreference.Off -> R.string.settings_fast_playback_hold_speed_off_description
        FastPlaybackHoldSpeedPreference.OnePointTwentyFive -> R.string.settings_fast_playback_hold_speed_125_description
        FastPlaybackHoldSpeedPreference.OnePointFive -> R.string.settings_fast_playback_hold_speed_150_description
        FastPlaybackHoldSpeedPreference.OnePointSeventyFive -> R.string.settings_fast_playback_hold_speed_175_description
        FastPlaybackHoldSpeedPreference.TwoPointZero -> R.string.settings_fast_playback_hold_speed_200_description
    }
}

data class FastPlaybackHoldSpeedOption(
    val speedPreference: FastPlaybackHoldSpeedPreference,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object PlaybackOrientationSettingCopy {
    @StringRes val title = R.string.settings_playback_orientation_title
    @StringRes val description = R.string.settings_playback_orientation_description

    val options: List<PlaybackOrientationModeOption> = listOf(
        PlaybackOrientationModeOption(PlaybackOrientationMode.Off, labelFor(PlaybackOrientationMode.Off), descriptionFor(PlaybackOrientationMode.Off)),
        PlaybackOrientationModeOption(PlaybackOrientationMode.Sensor, labelFor(PlaybackOrientationMode.Sensor), descriptionFor(PlaybackOrientationMode.Sensor)),
    )

    @StringRes
    fun labelFor(mode: PlaybackOrientationMode): Int = when (mode) {
        PlaybackOrientationMode.Off -> R.string.settings_playback_orientation_off_label
        PlaybackOrientationMode.Sensor -> R.string.settings_playback_orientation_sensor_label
    }

    @StringRes
    fun descriptionFor(mode: PlaybackOrientationMode): Int = when (mode) {
        PlaybackOrientationMode.Off -> R.string.settings_playback_orientation_off_description
        PlaybackOrientationMode.Sensor -> R.string.settings_playback_orientation_sensor_description
    }
}

data class PlaybackOrientationModeOption(
    val mode: PlaybackOrientationMode,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object SubtitleLanguageSettingCopy {
    @StringRes val title = R.string.settings_subtitle_language_title
    @StringRes val description = R.string.settings_subtitle_language_description

    val options: List<SubtitleLanguageOption> = listOf(
        SubtitleLanguageOption(SubtitleLanguagePreference.Auto, labelFor(SubtitleLanguagePreference.Auto), descriptionFor(SubtitleLanguagePreference.Auto)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Off, labelFor(SubtitleLanguagePreference.Off), descriptionFor(SubtitleLanguagePreference.Off)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Korean, labelFor(SubtitleLanguagePreference.Korean), descriptionFor(SubtitleLanguagePreference.Korean)),
        SubtitleLanguageOption(SubtitleLanguagePreference.English, labelFor(SubtitleLanguagePreference.English), descriptionFor(SubtitleLanguagePreference.English)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Japanese, labelFor(SubtitleLanguagePreference.Japanese), descriptionFor(SubtitleLanguagePreference.Japanese)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Chinese, labelFor(SubtitleLanguagePreference.Chinese), descriptionFor(SubtitleLanguagePreference.Chinese)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Russian, labelFor(SubtitleLanguagePreference.Russian), descriptionFor(SubtitleLanguagePreference.Russian)),
        SubtitleLanguageOption(SubtitleLanguagePreference.Spanish, labelFor(SubtitleLanguagePreference.Spanish), descriptionFor(SubtitleLanguagePreference.Spanish)),
    )

    @StringRes
    fun labelFor(language: SubtitleLanguagePreference): Int = when (language) {
        SubtitleLanguagePreference.Auto -> R.string.settings_subtitle_language_auto_label
        SubtitleLanguagePreference.Off -> R.string.settings_subtitle_language_off_label
        SubtitleLanguagePreference.Korean -> R.string.settings_subtitle_language_korean_label
        SubtitleLanguagePreference.English -> R.string.settings_subtitle_language_english_label
        SubtitleLanguagePreference.Japanese -> R.string.settings_subtitle_language_japanese_label
        SubtitleLanguagePreference.Chinese -> R.string.settings_subtitle_language_chinese_label
        SubtitleLanguagePreference.Russian -> R.string.settings_subtitle_language_russian_label
        SubtitleLanguagePreference.Spanish -> R.string.settings_subtitle_language_spanish_label
    }

    @StringRes
    fun descriptionFor(language: SubtitleLanguagePreference): Int = when (language) {
        SubtitleLanguagePreference.Auto -> R.string.settings_subtitle_language_auto_description
        SubtitleLanguagePreference.Off -> R.string.settings_subtitle_language_off_description
        SubtitleLanguagePreference.Korean -> R.string.settings_subtitle_language_korean_description
        SubtitleLanguagePreference.English -> R.string.settings_subtitle_language_english_description
        SubtitleLanguagePreference.Japanese -> R.string.settings_subtitle_language_japanese_description
        SubtitleLanguagePreference.Chinese -> R.string.settings_subtitle_language_chinese_description
        SubtitleLanguagePreference.Russian -> R.string.settings_subtitle_language_russian_description
        SubtitleLanguagePreference.Spanish -> R.string.settings_subtitle_language_spanish_description
    }
}

data class SubtitleLanguageOption(
    val language: SubtitleLanguagePreference,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object SubtitleFontScaleSettingCopy {
    @StringRes val title = R.string.settings_subtitle_font_scale_title
    @StringRes val description = R.string.settings_subtitle_font_scale_description
    @StringRes val resetButton = R.string.settings_subtitle_font_scale_reset_button
}

object SubtitlePositionSettingCopy {
    @StringRes val title = R.string.settings_subtitle_position_title
    @StringRes val description = R.string.settings_subtitle_position_description

    val options: List<SubtitlePositionOption> = listOf(
        SubtitlePositionOption(SubtitlePosition.Bottom, labelFor(SubtitlePosition.Bottom), descriptionFor(SubtitlePosition.Bottom)),
        SubtitlePositionOption(SubtitlePosition.Middle, labelFor(SubtitlePosition.Middle), descriptionFor(SubtitlePosition.Middle)),
        SubtitlePositionOption(SubtitlePosition.Top, labelFor(SubtitlePosition.Top), descriptionFor(SubtitlePosition.Top)),
    )

    @StringRes
    fun labelFor(position: SubtitlePosition): Int = when (position) {
        SubtitlePosition.Bottom -> R.string.settings_subtitle_position_bottom_label
        SubtitlePosition.Middle -> R.string.settings_subtitle_position_middle_label
        SubtitlePosition.Top -> R.string.settings_subtitle_position_top_label
    }

    @StringRes
    fun descriptionFor(position: SubtitlePosition): Int = when (position) {
        SubtitlePosition.Bottom -> R.string.settings_subtitle_position_bottom_description
        SubtitlePosition.Middle -> R.string.settings_subtitle_position_middle_description
        SubtitlePosition.Top -> R.string.settings_subtitle_position_top_description
    }
}

data class SubtitlePositionOption(
    val position: SubtitlePosition,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object SubtitleTextAlignmentSettingCopy {
    @StringRes val title = R.string.settings_subtitle_text_alignment_title
    @StringRes val description = R.string.settings_subtitle_text_alignment_description

    val options: List<SubtitleTextAlignmentOption> = listOf(
        SubtitleTextAlignmentOption(SubtitleTextAlignment.Start, labelFor(SubtitleTextAlignment.Start), descriptionFor(SubtitleTextAlignment.Start)),
        SubtitleTextAlignmentOption(SubtitleTextAlignment.Center, labelFor(SubtitleTextAlignment.Center), descriptionFor(SubtitleTextAlignment.Center)),
        SubtitleTextAlignmentOption(SubtitleTextAlignment.End, labelFor(SubtitleTextAlignment.End), descriptionFor(SubtitleTextAlignment.End)),
    )

    @StringRes
    fun labelFor(alignment: SubtitleTextAlignment): Int = when (alignment) {
        SubtitleTextAlignment.Start -> R.string.settings_subtitle_text_alignment_start_label
        SubtitleTextAlignment.Center -> R.string.settings_subtitle_text_alignment_center_label
        SubtitleTextAlignment.End -> R.string.settings_subtitle_text_alignment_end_label
    }

    @StringRes
    fun descriptionFor(alignment: SubtitleTextAlignment): Int = when (alignment) {
        SubtitleTextAlignment.Start -> R.string.settings_subtitle_text_alignment_start_description
        SubtitleTextAlignment.Center -> R.string.settings_subtitle_text_alignment_center_description
        SubtitleTextAlignment.End -> R.string.settings_subtitle_text_alignment_end_description
    }
}

data class SubtitleTextAlignmentOption(
    val alignment: SubtitleTextAlignment,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object DefaultStreamPreferenceSettingCopy {
    @StringRes val title = R.string.settings_default_stream_preference_title
    @StringRes val description = R.string.settings_default_stream_preference_description

    val options: List<DefaultStreamPreferenceOption> = listOf(
        DefaultStreamPreferenceOption(StashStreamPreference.Auto, labelFor(StashStreamPreference.Auto), descriptionFor(StashStreamPreference.Auto)),
        DefaultStreamPreferenceOption(StashStreamPreference.DirectFirst, labelFor(StashStreamPreference.DirectFirst), descriptionFor(StashStreamPreference.DirectFirst)),
        DefaultStreamPreferenceOption(StashStreamPreference.HlsFirst, labelFor(StashStreamPreference.HlsFirst), descriptionFor(StashStreamPreference.HlsFirst)),
    )

    @StringRes
    fun labelFor(preference: StashStreamPreference): Int = when (preference) {
        StashStreamPreference.Auto -> R.string.settings_stream_preference_auto_label
        StashStreamPreference.DirectFirst -> R.string.settings_stream_preference_direct_label
        StashStreamPreference.HlsFirst -> R.string.settings_stream_preference_hls_label
    }

    @StringRes
    fun descriptionFor(preference: StashStreamPreference): Int = when (preference) {
        StashStreamPreference.Auto -> R.string.settings_stream_preference_auto_description
        StashStreamPreference.DirectFirst -> R.string.settings_stream_preference_direct_description
        StashStreamPreference.HlsFirst -> R.string.settings_stream_preference_hls_description
    }
}

data class DefaultStreamPreferenceOption(
    val preference: StashStreamPreference,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object ThemeModeSettingCopy {
    @StringRes val sectionTitle = R.string.settings_category_appearance_title
    @StringRes val title = R.string.settings_theme_mode_title
    @StringRes val description = R.string.settings_theme_mode_description

    val options: List<ThemeModeOption> = listOf(
        ThemeModeOption(StashThemeMode.SYSTEM, labelFor(StashThemeMode.SYSTEM), descriptionFor(StashThemeMode.SYSTEM)),
        ThemeModeOption(StashThemeMode.DARK, labelFor(StashThemeMode.DARK), descriptionFor(StashThemeMode.DARK)),
        ThemeModeOption(StashThemeMode.LIGHT, labelFor(StashThemeMode.LIGHT), descriptionFor(StashThemeMode.LIGHT)),
    )

    @StringRes
    fun labelFor(mode: StashThemeMode): Int = when (mode) {
        StashThemeMode.SYSTEM -> R.string.settings_theme_mode_system_label
        StashThemeMode.DARK -> R.string.settings_theme_mode_dark_label
        StashThemeMode.LIGHT -> R.string.settings_theme_mode_light_label
    }

    @StringRes
    fun descriptionFor(mode: StashThemeMode): Int = when (mode) {
        StashThemeMode.SYSTEM -> R.string.settings_theme_mode_system_description
        StashThemeMode.DARK -> R.string.settings_theme_mode_dark_description
        StashThemeMode.LIGHT -> R.string.settings_theme_mode_light_description
    }
}

data class ThemeModeOption(
    val mode: StashThemeMode,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object AccentColorSettingCopy {
    @StringRes val title = R.string.settings_accent_color_title
    @StringRes val description = R.string.settings_accent_color_description

    val options: List<AccentColorOption> = listOf(
        AccentColorOption(StashAccentColor.Purple, labelFor(StashAccentColor.Purple), descriptionFor(StashAccentColor.Purple)),
        AccentColorOption(StashAccentColor.Blue, labelFor(StashAccentColor.Blue), descriptionFor(StashAccentColor.Blue)),
        AccentColorOption(StashAccentColor.Cyan, labelFor(StashAccentColor.Cyan), descriptionFor(StashAccentColor.Cyan)),
        AccentColorOption(StashAccentColor.Amber, labelFor(StashAccentColor.Amber), descriptionFor(StashAccentColor.Amber)),
        AccentColorOption(StashAccentColor.Rose, labelFor(StashAccentColor.Rose), descriptionFor(StashAccentColor.Rose)),
    )

    @StringRes
    fun labelFor(accentColor: StashAccentColor): Int = when (accentColor) {
        StashAccentColor.Purple -> R.string.settings_accent_color_purple_label
        StashAccentColor.Blue -> R.string.settings_accent_color_blue_label
        StashAccentColor.Cyan -> R.string.settings_accent_color_cyan_label
        StashAccentColor.Amber -> R.string.settings_accent_color_amber_label
        StashAccentColor.Rose -> R.string.settings_accent_color_rose_label
    }

    @StringRes
    fun descriptionFor(accentColor: StashAccentColor): Int = when (accentColor) {
        StashAccentColor.Purple -> R.string.settings_accent_color_purple_description
        StashAccentColor.Blue -> R.string.settings_accent_color_blue_description
        StashAccentColor.Cyan -> R.string.settings_accent_color_cyan_description
        StashAccentColor.Amber -> R.string.settings_accent_color_amber_description
        StashAccentColor.Rose -> R.string.settings_accent_color_rose_description
    }
}

data class AccentColorOption(
    val accentColor: StashAccentColor,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object UiScaleSettingCopy {
    @StringRes val title = R.string.settings_ui_scale_title
    @StringRes val description = R.string.settings_ui_scale_description
    @StringRes val resetButton = R.string.settings_ui_scale_reset_button

    val options: List<UiScaleOption> = listOf(
        UiScaleOption(StashUiScale.Compact, labelFor(StashUiScale.Compact), descriptionFor(StashUiScale.Compact)),
        UiScaleOption(StashUiScale.Default, labelFor(StashUiScale.Default), descriptionFor(StashUiScale.Default)),
        UiScaleOption(StashUiScale.Comfortable, labelFor(StashUiScale.Comfortable), descriptionFor(StashUiScale.Comfortable)),
        UiScaleOption(StashUiScale.Large, labelFor(StashUiScale.Large), descriptionFor(StashUiScale.Large)),
    )

    val sliderValueRange: ClosedFloatingPointRange<Float> = 0f..(options.lastIndex.toFloat())
    val sliderSteps: Int = (options.size - 2).coerceAtLeast(0)

    @StringRes
    fun labelFor(uiScale: StashUiScale): Int = when (uiScale) {
        StashUiScale.Compact -> R.string.settings_ui_scale_compact_label
        StashUiScale.Default -> R.string.settings_ui_scale_default_label
        StashUiScale.Comfortable -> R.string.settings_ui_scale_comfortable_label
        StashUiScale.Large -> R.string.settings_ui_scale_large_label
    }

    @StringRes
    fun descriptionFor(uiScale: StashUiScale): Int = when (uiScale) {
        StashUiScale.Compact -> R.string.settings_ui_scale_compact_description
        StashUiScale.Default -> R.string.settings_ui_scale_default_description
        StashUiScale.Comfortable -> R.string.settings_ui_scale_comfortable_description
        StashUiScale.Large -> R.string.settings_ui_scale_large_description
    }

    fun sliderValueFor(uiScale: StashUiScale): Float =
        options.indexOfFirst { it.uiScale == uiScale }
            .takeIf { it >= 0 }
            ?.toFloat()
            ?: sliderValueFor(StashUiScale.default)

    fun optionForSliderValue(value: Float): UiScaleOption =
        options[value.roundToInt().coerceIn(0, options.lastIndex)]
}

data class UiScaleOption(
    val uiScale: StashUiScale,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object LanguageSettingCopy {
    @StringRes val sectionTitle = R.string.settings_category_interface_title
    @StringRes val title = R.string.settings_language_title
    @StringRes val description = R.string.settings_language_description

    val options: List<LanguageOption> = listOf(
        LanguageOption(StashAppLanguage.SYSTEM, labelFor(StashAppLanguage.SYSTEM), descriptionFor(StashAppLanguage.SYSTEM)),
        LanguageOption(StashAppLanguage.KOREAN, labelFor(StashAppLanguage.KOREAN), descriptionFor(StashAppLanguage.KOREAN)),
        LanguageOption(StashAppLanguage.ENGLISH, labelFor(StashAppLanguage.ENGLISH), descriptionFor(StashAppLanguage.ENGLISH)),
        LanguageOption(
            StashAppLanguage.CHINESE_SIMPLIFIED,
            labelFor(StashAppLanguage.CHINESE_SIMPLIFIED),
            descriptionFor(StashAppLanguage.CHINESE_SIMPLIFIED),
        ),
        LanguageOption(
            StashAppLanguage.CHINESE_TRADITIONAL,
            labelFor(StashAppLanguage.CHINESE_TRADITIONAL),
            descriptionFor(StashAppLanguage.CHINESE_TRADITIONAL),
        ),
    )

    @StringRes
    fun labelFor(language: StashAppLanguage): Int = when (language) {
        StashAppLanguage.SYSTEM -> R.string.settings_language_system_label
        StashAppLanguage.KOREAN -> R.string.settings_language_korean_label
        StashAppLanguage.ENGLISH -> R.string.settings_language_english_label
        StashAppLanguage.CHINESE_SIMPLIFIED -> R.string.settings_language_chinese_simplified_label
        StashAppLanguage.CHINESE_TRADITIONAL -> R.string.settings_language_chinese_traditional_label
    }

    @StringRes
    fun descriptionFor(language: StashAppLanguage): Int = when (language) {
        StashAppLanguage.SYSTEM -> R.string.settings_language_system_description
        StashAppLanguage.KOREAN -> R.string.settings_language_korean_description
        StashAppLanguage.ENGLISH -> R.string.settings_language_english_description
        StashAppLanguage.CHINESE_SIMPLIFIED -> R.string.settings_language_chinese_simplified_description
        StashAppLanguage.CHINESE_TRADITIONAL -> R.string.settings_language_chinese_traditional_description
    }
}

data class LanguageOption(
    val language: StashAppLanguage,
    @StringRes val label: Int,
    @StringRes val description: Int,
)

object HybridRecommendationSettingCopy {
    @StringRes val sectionTitle = R.string.settings_recommendation_section_title
    @StringRes val statusDescription = R.string.settings_recommendation_status_description
    @StringRes val fallbackDescription = R.string.settings_recommendation_fallback_description
    @StringRes val testButton = R.string.settings_recommendation_test_button
}

object ServerLibrarySettingsCopy {
    @StringRes val sectionTitle = R.string.settings_server_library_section_title
    @StringRes val createGalleriesTitle = R.string.settings_server_create_galleries_from_folders_title
    @StringRes val createGalleriesDescription = R.string.settings_server_create_galleries_from_folders_description
    @StringRes val scanButton = R.string.settings_server_metadata_scan_button
    @StringRes val scanConfirmTitle = R.string.settings_server_metadata_scan_confirm_title
    @StringRes val scanConfirm = R.string.settings_server_metadata_scan_confirm_button
    @StringRes val scanCancel = R.string.settings_server_metadata_scan_cancel_button
}

@Composable
fun SettingsRoute(
    isFoldLikeLayout: Boolean,
    hasAvailableUpdate: Boolean = false,
    onOpenSection: (String) -> Unit,
) {
    SettingsScreenScaffold(isFoldLikeLayout = isFoldLikeLayout) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        SettingsListCopy.sections.forEach { section ->
            SettingsSectionCard(
                section = section,
                showBadge = hasAvailableUpdate && section == SettingsSection.Support,
                onClick = { onOpenSection(section.route) },
            )
        }
    }
}

@Composable
fun SettingsDetailRoute(
    section: SettingsSection,
    isFoldLikeLayout: Boolean,
    availableUpdateNotice: AppUpdateNotice? = null,
    onUpdateNoticeChange: (AppUpdateNotice?) -> Unit = {},
    onNavigateBack: () -> Unit,
    onOpenOnboarding: () -> Unit,
) {
    SettingsScreenScaffold(isFoldLikeLayout = isFoldLikeLayout) {
        TextButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.settings_back_button))
        }
        Text(stringResource(section.title), style = MaterialTheme.typography.headlineMedium)
        when (section) {
            SettingsSection.Server -> ServerSettingsContent(onOpenOnboarding = onOpenOnboarding)
            SettingsSection.Security -> SecuritySettingsContent()
            SettingsSection.Playback -> PlaybackSettingsContent()
            SettingsSection.Appearance -> AppearanceSettingsContent()
            SettingsSection.Interface -> InterfaceSettingsContent()
            SettingsSection.Development -> DevelopmentSettingsContent()
            SettingsSection.Support -> SupportSettingsContent(
                availableUpdateNotice = availableUpdateNotice,
                onUpdateNoticeChange = onUpdateNoticeChange,
            )
        }
    }
}

@Composable
private fun SettingsScreenScaffold(
    isFoldLikeLayout: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(if (isFoldLikeLayout) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SettingsSectionCard(
    section: SettingsSection,
    showBadge: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(section.title), style = MaterialTheme.typography.titleMedium)
            }
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
            Text(
                stringResource(R.string.settings_row_open_indicator),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServerSettingsContent(onOpenOnboarding: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val savedProfile by repository.serverProfile.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var serverName by remember { mutableStateOf("Home") }
    var serverUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(SettingsServerAuthModeOption.LinkOnly) }
    var allowInsecureLocalApiKey by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var recommendationStatusText by remember { mutableStateOf<String?>(null) }
    var recommendationStatusIsSuccess by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var createGalleriesFromFolders by remember { mutableStateOf<Boolean?>(null) }
    var isServerLibraryLoading by remember { mutableStateOf(false) }
    var isServerLibrarySaving by remember { mutableStateOf(false) }
    var isMetadataScanRunning by remember { mutableStateOf(false) }
    var serverLibraryStatusText by remember { mutableStateOf<String?>(null) }
    var serverLibraryErrorText by remember { mutableStateOf<String?>(null) }
    var showMetadataScanConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(savedProfile) {
        savedProfile?.let {
            serverName = it.name
            serverUrl = it.baseUrl
            apiKey = it.apiKey
            username = it.username
            password = it.password
            allowInsecureLocalApiKey = it.allowInsecureLocalApiKey
            authMode = when (it.authMode) {
                StashServerAuthMode.None -> SettingsServerAuthModeOption.LinkOnly
                StashServerAuthMode.ApiKey -> SettingsServerAuthModeOption.ApiKey
                StashServerAuthMode.SessionCookie -> SettingsServerAuthModeOption.Password
            }
        }
        createGalleriesFromFolders = null
        serverLibraryStatusText = null
        serverLibraryErrorText = null
        val activeProfile = savedProfile
        if (activeProfile?.isConfigured() != true) {
            isServerLibraryLoading = false
            return@LaunchedEffect
        }
        isServerLibraryLoading = true
        runCatching { StashGraphQlClient(activeProfile).findServerLibrarySettings() }
            .onSuccess { settings ->
                createGalleriesFromFolders = settings.createGalleriesFromFolders
            }
            .onFailure {
                StashDebugLogBuffer.record("Settings", "Stash server library settings load failed", it)
                serverLibraryErrorText = it.message ?: context.getString(R.string.settings_server_library_load_failed)
            }
        isServerLibraryLoading = false
    }

    if (showMetadataScanConfirm) {
        AlertDialog(
            onDismissRequest = { showMetadataScanConfirm = false },
            title = { Text(stringResource(ServerLibrarySettingsCopy.scanConfirmTitle)) },
            text = { Text(stringResource(R.string.settings_server_metadata_scan_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMetadataScanConfirm = false
                        coroutineScope.launch {
                            val activeProfile = savedProfile
                            if (activeProfile == null) {
                                serverLibraryStatusText = null
                                serverLibraryErrorText = context.getString(R.string.settings_recommendation_no_stash_server)
                                return@launch
                            }
                            isMetadataScanRunning = true
                            serverLibraryStatusText = context.getString(R.string.settings_server_metadata_scan_starting)
                            serverLibraryErrorText = null
                            runCatching { StashGraphQlClient(activeProfile).scanMetadata() }
                                .onSuccess { jobId ->
                                    serverLibraryStatusText = context.getString(R.string.settings_server_metadata_scan_started, jobId)
                                }
                                .onFailure {
                                    StashDebugLogBuffer.record("Settings", "Stash metadata scan start failed", it)
                                    serverLibraryStatusText = null
                                    serverLibraryErrorText = it.message ?: context.getString(R.string.settings_server_metadata_scan_failed)
                                }
                            isMetadataScanRunning = false
                        }
                    },
                ) {
                    Text(stringResource(ServerLibrarySettingsCopy.scanConfirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMetadataScanConfirm = false }) {
                    Text(stringResource(ServerLibrarySettingsCopy.scanCancel))
                }
            },
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.settings_stash_server_section_title), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = { Text(stringResource(R.string.settings_server_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.settings_server_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            SettingsServerAuthModeRow(
                selected = authMode == SettingsServerAuthModeOption.LinkOnly,
                label = R.string.setup_auth_mode_link_only_label,
                description = R.string.setup_auth_mode_link_only_description,
                onClick = { authMode = SettingsServerAuthModeOption.LinkOnly },
            )
            SettingsServerAuthModeRow(
                selected = authMode == SettingsServerAuthModeOption.ApiKey,
                label = R.string.setup_auth_mode_api_key_label,
                description = R.string.setup_auth_mode_api_key_description,
                onClick = { authMode = SettingsServerAuthModeOption.ApiKey },
            )
            SettingsServerAuthModeRow(
                selected = authMode == SettingsServerAuthModeOption.Password,
                label = R.string.setup_auth_mode_password_label,
                description = R.string.setup_auth_mode_password_description,
                onClick = { authMode = SettingsServerAuthModeOption.Password },
            )
            if (authMode == SettingsServerAuthModeOption.ApiKey) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.settings_api_key_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            } else if (authMode == SettingsServerAuthModeOption.Password) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.setup_username_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.setup_password_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
            if (statusText != null) Text(statusText!!, color = MaterialTheme.colorScheme.primary)
            if (errorText != null) Text(errorText!!, color = MaterialTheme.colorScheme.error)
            Button(
                onClick = {
                    coroutineScope.launch {
                        val profile = buildSettingsServerProfile(
                            serverName = serverName,
                            serverUrl = serverUrl,
                            apiKey = apiKey,
                            username = username,
                            password = password,
                            authMode = authMode,
                            allowInsecureLocalApiKey = allowInsecureLocalApiKey,
                        )
                        errorText = null
                        if (!profile.isConfigured()) {
                            statusText = null
                            errorText = context.getString(R.string.auto_kr_0532)
                            return@launch
                        }
                        if (!canAttemptStashCredentialTransport(profile.baseUrl, profile.authMode, profile.allowInsecureLocalApiKey)) {
                            statusText = null
                            errorText = context.getString(R.string.settings_insecure_auth_blocked)
                            return@launch
                        }
                        isSaving = true
                        statusText = context.getString(R.string.settings_save_in_progress)
                        val saveResult = if (authMode == SettingsServerAuthModeOption.Password) {
                            if (username.isBlank() || password.isBlank()) {
                                Result.failure(IllegalArgumentException(context.getString(R.string.setup_username_password_required)))
                            } else {
                                runCatching {
                                    val login = StashLoginClient().loginWithPassword(serverUrl, username, password)
                                    profile.copy(
                                        apiKey = "",
                                        authMode = StashServerAuthMode.SessionCookie,
                                        sessionCookie = login.sessionCookie,
                                        username = username,
                                        password = password,
                                    )
                                }
                            }
                        } else {
                            Result.success(profile)
                        }
                        saveResult
                            .onSuccess { authenticatedProfile ->
                                repository.saveServerProfile(authenticatedProfile)
                                password = ""
                                statusText = context.getString(R.string.settings_save_complete)
                            }
                            .onFailure {
                                StashDebugLogBuffer.record("Settings", "Stash server save failed", it)
                                statusText = null
                                errorText = it.message ?: context.getString(R.string.settings_connection_failed)
                            }
                        isSaving = false
                    }
                },
                enabled = !isSaving && serverUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_save_button)) }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val profile = buildSettingsServerProfile(
                            serverName = serverName,
                            serverUrl = serverUrl,
                            apiKey = apiKey,
                            username = username,
                            password = password,
                            authMode = authMode,
                            allowInsecureLocalApiKey = allowInsecureLocalApiKey,
                        )
                        errorText = null
                        if (!profile.isConfigured()) {
                            statusText = null
                            errorText = context.getString(R.string.auto_kr_0532)
                            return@launch
                        }
                        if (!canAttemptStashCredentialTransport(profile.baseUrl, profile.authMode, profile.allowInsecureLocalApiKey)) {
                            statusText = null
                            errorText = context.getString(R.string.settings_insecure_auth_blocked)
                            return@launch
                        }
                        isSaving = true
                        statusText = context.getString(R.string.settings_connection_testing)
                        val testProfileResult = if (authMode == SettingsServerAuthModeOption.Password) {
                            if (username.isBlank() || password.isBlank()) {
                                Result.failure(IllegalArgumentException(context.getString(R.string.setup_username_password_required)))
                            } else {
                                runCatching {
                                    val login = StashLoginClient().loginWithPassword(serverUrl, username, password)
                                    profile.copy(
                                        apiKey = "",
                                        authMode = StashServerAuthMode.SessionCookie,
                                        sessionCookie = login.sessionCookie,
                                        username = username,
                                        password = password,
                                    )
                                }
                            }
                        } else {
                            Result.success(profile)
                        }
                        testProfileResult
                            .mapCatching { StashGraphQlClient(it).testConnection() }
                            .onSuccess {
                                statusText = context.getString(R.string.settings_connection_success, it)
                            }
                            .onFailure {
                                StashDebugLogBuffer.record("Settings", "Stash server test failed", it)
                                statusText = null
                                errorText = it.message ?: context.getString(R.string.settings_connection_failed)
                            }
                        isSaving = false
                    }
                },
                enabled = !isSaving && serverUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_connection_test_button)) }
            Button(
                onClick = onOpenOnboarding,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_open_onboarding_button)) }
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.clearServerProfile()
                        serverUrl = ""
                        apiKey = ""
                        username = ""
                        password = ""
                        authMode = SettingsServerAuthModeOption.LinkOnly
                        allowInsecureLocalApiKey = true
                        statusText = context.getString(R.string.settings_server_clear_complete)
                        errorText = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_server_clear_button)) }
        }
    }

    ServerLibrarySettingsCard(
        serverConfigured = savedProfile?.isConfigured() == true,
        createGalleriesFromFolders = createGalleriesFromFolders,
        loading = isServerLibraryLoading,
        saving = isServerLibrarySaving,
        scanning = isMetadataScanRunning,
        statusText = serverLibraryStatusText,
        errorText = serverLibraryErrorText,
        onToggleCreateGalleries = { enabled ->
            coroutineScope.launch {
                val activeProfile = savedProfile
                if (activeProfile == null) {
                    serverLibraryStatusText = null
                    serverLibraryErrorText = context.getString(R.string.settings_recommendation_no_stash_server)
                    return@launch
                }
                isServerLibrarySaving = true
                serverLibraryStatusText = context.getString(R.string.settings_server_library_update_saving)
                serverLibraryErrorText = null
                runCatching { StashGraphQlClient(activeProfile).setCreateGalleriesFromFolders(enabled) }
                    .onSuccess { settings ->
                        createGalleriesFromFolders = settings.createGalleriesFromFolders
                        serverLibraryStatusText = context.getString(R.string.settings_server_library_update_saved)
                    }
                    .onFailure {
                        StashDebugLogBuffer.record("Settings", "Stash server library settings update failed", it)
                        serverLibraryStatusText = null
                        serverLibraryErrorText = it.message ?: context.getString(R.string.settings_server_library_update_failed)
                    }
                isServerLibrarySaving = false
            }
        },
        onRequestScan = { showMetadataScanConfirm = true },
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(HybridRecommendationSettingCopy.sectionTitle), style = MaterialTheme.typography.titleMedium)
            recommendationStatusText?.let { text ->
                Text(
                    text,
                    color = if (recommendationStatusIsSuccess) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        recommendationStatusText = context.getString(R.string.settings_recommendation_testing)
                        recommendationStatusIsSuccess = true
                        val activeProfile = savedProfile
                        if (activeProfile == null) {
                            recommendationStatusText = context.getString(R.string.settings_recommendation_no_stash_server)
                            recommendationStatusIsSuccess = false
                            return@launch
                        }
                        val copy = StashPluginRecommendationStatusClient(
                            StashGraphQlClient(activeProfile),
                        ).check().toSettingsStatusCopy()
                        recommendationStatusText = copy.message
                        recommendationStatusIsSuccess = copy.isSuccess
                        if (!copy.isSuccess) {
                            StashDebugLogBuffer.record("Settings", "Hybrid recommendation status failed: ${copy.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(HybridRecommendationSettingCopy.testButton)) }
        }
    }
}

@Composable
private fun ServerLibrarySettingsCard(
    serverConfigured: Boolean,
    createGalleriesFromFolders: Boolean?,
    loading: Boolean,
    saving: Boolean,
    scanning: Boolean,
    statusText: String?,
    errorText: String?,
    onToggleCreateGalleries: (Boolean) -> Unit,
    onRequestScan: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(ServerLibrarySettingsCopy.sectionTitle), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(ServerLibrarySettingsCopy.createGalleriesTitle), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(ServerLibrarySettingsCopy.createGalleriesDescription),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = createGalleriesFromFolders == true,
                    enabled = serverConfigured && createGalleriesFromFolders != null && !loading && !saving,
                    onCheckedChange = onToggleCreateGalleries,
                )
            }
            Button(
                onClick = onRequestScan,
                enabled = serverConfigured && !loading && !saving && !scanning,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(ServerLibrarySettingsCopy.scanButton))
            }
            Text(
                text = when {
                    loading -> stringResource(R.string.settings_server_library_loading)
                    scanning -> stringResource(R.string.settings_server_metadata_scan_starting)
                    !serverConfigured -> stringResource(R.string.settings_recommendation_no_stash_server)
                    else -> statusText ?: stringResource(R.string.settings_server_metadata_scan_description)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            errorText?.let { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private enum class SettingsServerAuthModeOption {
    LinkOnly,
    ApiKey,
    Password,
}

private fun SettingsServerAuthModeOption.toServerAuthMode(): StashServerAuthMode = when (this) {
    SettingsServerAuthModeOption.LinkOnly -> StashServerAuthMode.None
    SettingsServerAuthModeOption.ApiKey -> StashServerAuthMode.ApiKey
    SettingsServerAuthModeOption.Password -> StashServerAuthMode.SessionCookie
}

private fun buildSettingsServerProfile(
    serverName: String,
    serverUrl: String,
    apiKey: String,
    username: String,
    password: String,
    authMode: SettingsServerAuthModeOption,
    allowInsecureLocalApiKey: Boolean,
): StashServerProfile = StashServerProfile(
    name = serverName,
    baseUrl = serverUrl,
    apiKey = if (authMode == SettingsServerAuthModeOption.ApiKey) apiKey else "",
    username = if (authMode == SettingsServerAuthModeOption.Password) username else "",
    password = if (authMode == SettingsServerAuthModeOption.Password) password else "",
    authMode = authMode.toServerAuthMode(),
    allowInsecureLocalApiKey = allowInsecureLocalApiKey,
)

@Composable
private fun SettingsServerAuthModeRow(
    selected: Boolean,
    @StringRes label: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SecuritySettingsContent() {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val biometricAppLockEnabled by repository.biometricAppLockEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_BIOMETRIC_APP_LOCK_ENABLED,
    )
    val recentAppsPrivacyEnabled by repository.recentAppsPrivacyEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_RECENT_APPS_PRIVACY_ENABLED,
    )
    val availability = rememberDeviceAuthenticationAvailability()
    val coroutineScope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val authenticate = rememberDeviceAuthenticationLauncher(
        onAuthenticated = {
            coroutineScope.launch {
                repository.setBiometricAppLockEnabled(true)
                statusMessage = context.getString(R.string.settings_biometric_app_lock_enabled_status)
            }
        },
        onAuthenticationError = { message ->
            statusMessage = message.toString()
        },
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(BiometricAppLockSettingCopy.title), style = MaterialTheme.typography.titleMedium)
                Text(
                    statusMessage ?: stringResource(deviceAuthenticationAvailabilityDescriptionRes(availability)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (availability == DeviceAuthenticationAvailability.Available) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Switch(
                checked = biometricAppLockEnabled,
                enabled = biometricAppLockEnabled || availability == DeviceAuthenticationAvailability.Available,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (availability == DeviceAuthenticationAvailability.Available) {
                            authenticate?.invoke()
                        } else {
                            statusMessage = context.getString(deviceAuthenticationAvailabilityDescriptionRes(availability))
                        }
                    } else {
                        coroutineScope.launch {
                            repository.setBiometricAppLockEnabled(false)
                            statusMessage = context.getString(R.string.settings_biometric_app_lock_disabled_status)
                        }
                    }
                },
            )
        }
    }

    SettingsSwitchCard(
        title = RecentAppsPrivacySettingCopy.title,
        description = RecentAppsPrivacySettingCopy.description,
        checked = recentAppsPrivacyEnabled,
        onCheckedChange = { enabled ->
            coroutineScope.launch {
                repository.setRecentAppsPrivacyEnabled(enabled)
            }
        },
    )
}

@Composable
private fun PlaybackSettingsContent() {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val defaultStreamPreference by repository.defaultStreamPreference.collectAsState(
        initial = StashSettingsRepository.DEFAULT_STREAM_PREFERENCE,
    )
    val playbackEndAction by repository.playbackEndAction.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYBACK_END_ACTION,
    )
    val fastPlaybackHoldSpeed by repository.fastPlaybackHoldSpeed.collectAsState(
        initial = StashSettingsRepository.DEFAULT_FAST_PLAYBACK_HOLD_SPEED,
    )
    val backgroundPlaybackEnabled by repository.backgroundPlaybackEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_BACKGROUND_PLAYBACK_ENABLED,
    )
    val pictureInPictureEnabled by repository.pictureInPictureEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PICTURE_IN_PICTURE_ENABLED,
    )
    val shortsMaxDurationSeconds by repository.shortsMaxDurationSeconds.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SHORTS_MAX_DURATION_SECONDS,
    )
    val subtitleLanguage by repository.subtitleLanguage.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_LANGUAGE,
    )
    val subtitleFontScale by repository.subtitleFontScale.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_FONT_SCALE,
    )
    val subtitlePosition by repository.subtitlePosition.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_POSITION,
    )
    val subtitleTextAlignment by repository.subtitleTextAlignment.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_TEXT_ALIGNMENT,
    )
    val coroutineScope = rememberCoroutineScope()
    var showShortsResetDialog by remember { mutableStateOf(false) }
    var shortsResetFeedback by remember { mutableStateOf<Int?>(null) }

    SettingsRadioGroupCard(
        title = DefaultStreamPreferenceSettingCopy.title,
        description = DefaultStreamPreferenceSettingCopy.description,
    ) {
        DefaultStreamPreferenceSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = defaultStreamPreference == option.preference,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setDefaultStreamPreference(option.preference)
                    }
                },
            )
        }
    }

    SettingsRadioGroupCard(
        title = PlaybackEndActionSettingCopy.title,
        description = PlaybackEndActionSettingCopy.description,
    ) {
        PlaybackEndActionSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = playbackEndAction == option.action,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setPlaybackEndAction(option.action)
                    }
                },
            )
        }
    }

    SettingsRadioGroupCard(
        title = FastPlaybackHoldSpeedSettingCopy.title,
        description = FastPlaybackHoldSpeedSettingCopy.description,
    ) {
        FastPlaybackHoldSpeedSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = fastPlaybackHoldSpeed == option.speedPreference,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setFastPlaybackHoldSpeed(option.speedPreference)
                    }
                },
            )
        }
    }

    SettingsSwitchCard(
        title = BackgroundPlaybackSettingCopy.title,
        description = BackgroundPlaybackSettingCopy.description,
        checked = backgroundPlaybackEnabled,
        onCheckedChange = { enabled ->
            coroutineScope.launch { repository.setBackgroundPlaybackEnabled(enabled) }
        },
    )

    SettingsSwitchCard(
        title = PictureInPictureSettingCopy.title,
        description = PictureInPictureSettingCopy.description,
        checked = pictureInPictureEnabled,
        onCheckedChange = { enabled ->
            coroutineScope.launch { repository.setPictureInPictureEnabled(enabled) }
        },
    )

    ShortsMaxDurationSliderCard(
        maxDurationSeconds = shortsMaxDurationSeconds,
        onMaxDurationChange = { seconds ->
            coroutineScope.launch {
                repository.setShortsMaxDurationSeconds(seconds)
            }
        },
    )

    ShortsRecommendationResetCard(
        feedback = shortsResetFeedback,
        onRequestReset = { showShortsResetDialog = true },
    )

    if (showShortsResetDialog) {
        AlertDialog(
            onDismissRequest = { showShortsResetDialog = false },
            title = { Text(stringResource(ShortsRecommendationResetSettingCopy.confirmTitle)) },
            text = { Text(stringResource(ShortsRecommendationResetSettingCopy.confirmMessage)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showShortsResetDialog = false
                        coroutineScope.launch {
                            localRepository.clearShortsRecommendationHistory()
                            shortsResetFeedback = ShortsRecommendationResetSettingCopy.success
                        }
                    },
                ) {
                    Text(stringResource(ShortsRecommendationResetSettingCopy.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showShortsResetDialog = false }) {
                    Text(stringResource(ShortsRecommendationResetSettingCopy.cancel))
                }
            },
        )
    }

    SettingsRadioGroupCard(
        title = SubtitleLanguageSettingCopy.title,
        description = SubtitleLanguageSettingCopy.description,
    ) {
        SubtitleLanguageSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = subtitleLanguage == option.language,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setSubtitleLanguage(option.language)
                    }
                },
            )
        }
    }

    SettingsSubtitleFontScaleSliderCard(
        fontScale = subtitleFontScale,
        onFontScaleChange = { scale ->
            coroutineScope.launch {
                repository.setSubtitleFontScale(scale)
            }
        },
    )

    SettingsRadioGroupCard(
        title = SubtitlePositionSettingCopy.title,
        description = SubtitlePositionSettingCopy.description,
    ) {
        SubtitlePositionSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = subtitlePosition == option.position,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setSubtitlePosition(option.position)
                    }
                },
            )
        }
    }

    SettingsRadioGroupCard(
        title = SubtitleTextAlignmentSettingCopy.title,
        description = SubtitleTextAlignmentSettingCopy.description,
    ) {
        SubtitleTextAlignmentSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = subtitleTextAlignment == option.alignment,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setSubtitleTextAlignment(option.alignment)
                    }
                },
            )
        }
    }
}

@Composable
private fun AppearanceSettingsContent() {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val themeMode by repository.themeMode.collectAsState(
        initial = StashSettingsRepository.DEFAULT_THEME_MODE,
    )
    val accentColor by repository.accentColor.collectAsState(
        initial = StashSettingsRepository.DEFAULT_ACCENT_COLOR,
    )
    val uiScale by repository.uiScale.collectAsState(
        initial = StashSettingsRepository.DEFAULT_UI_SCALE,
    )
    val coroutineScope = rememberCoroutineScope()

    SettingsRadioGroupCard(
        title = ThemeModeSettingCopy.title,
        description = ThemeModeSettingCopy.description,
    ) {
        ThemeModeSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = themeMode == option.mode,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setThemeMode(option.mode)
                    }
                },
            )
        }
    }

    SettingsRadioGroupCard(
        title = AccentColorSettingCopy.title,
        description = AccentColorSettingCopy.description,
    ) {
        AccentColorSettingCopy.options.forEach { option ->
            SettingsAccentColorRow(
                selected = accentColor == option.accentColor,
                label = option.label,
                description = option.description,
                swatchColor = Color(stashThemeColorSnapshot(darkTheme = false, accentColor = option.accentColor).primaryArgb),
                onClick = {
                    coroutineScope.launch {
                        repository.setAccentColor(option.accentColor)
                    }
                },
            )
        }
    }

    SettingsUiScaleSliderCard(
        uiScale = uiScale,
        onUiScaleChange = { scale ->
            coroutineScope.launch {
                repository.setUiScale(scale)
            }
        },
    )
}

@Composable
private fun InterfaceSettingsContent() {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val appLanguage by repository.appLanguage.collectAsState(
        initial = StashSettingsRepository.DEFAULT_APP_LANGUAGE,
    )
    val coroutineScope = rememberCoroutineScope()

    SettingsRadioGroupCard(
        title = LanguageSettingCopy.title,
        description = LanguageSettingCopy.description,
    ) {
        LanguageSettingCopy.options.forEach { option ->
            SettingsRadioRow(
                selected = appLanguage == option.language,
                label = option.label,
                description = option.description,
                onClick = {
                    coroutineScope.launch {
                        repository.setAppLanguage(option.language)
                    }
                },
            )
        }
    }
}

@Composable
private fun DevelopmentSettingsContent() {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val playerDebugOverlayEnabled by repository.playerDebugOverlayEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYER_DEBUG_OVERLAY_ENABLED,
    )
    val debugEntries by StashDebugLogBuffer.entries.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var logStatus by remember { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(PlayerDebugOverlaySettingCopy.title), style = MaterialTheme.typography.bodyLarge)
            }
            Switch(
                checked = playerDebugOverlayEnabled,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        repository.setPlayerDebugOverlayEnabled(enabled)
                    }
                },
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(DebugLogSettingCopy.title), style = MaterialTheme.typography.titleMedium)
            if (debugEntries.isEmpty()) {
                Text(
                    stringResource(DebugLogSettingCopy.empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                debugEntries.take(30).forEach { entry ->
                    Text(
                        entry.formatForCopy(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            logStatus?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                context.getString(R.string.settings_debug_log_clipboard_label),
                                StashDebugLogBuffer.copyText(),
                            ),
                        )
                        logStatus = context.getString(R.string.settings_debug_log_copy_complete)
                    },
                    enabled = debugEntries.isNotEmpty(),
                ) {
                    Text(stringResource(DebugLogSettingCopy.copy))
                }
                TextButton(
                    onClick = {
                        StashDebugLogBuffer.clear()
                        logStatus = context.getString(R.string.settings_debug_log_clear_complete)
                    },
                    enabled = debugEntries.isNotEmpty(),
                ) {
                    Text(stringResource(DebugLogSettingCopy.clear))
                }
            }
        }
    }
}

@Composable
private fun SupportSettingsContent(
    availableUpdateNotice: AppUpdateNotice?,
    onUpdateNoticeChange: (AppUpdateNotice?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateChecker = remember { AppUpdateChecker() }
    val updateInstaller = remember { AppUpdateInstaller() }
    val currentVersion = currentAppVersionName(context)
    var statusText by remember { mutableStateOf<String?>(null) }
    var updateStatusText by remember { mutableStateOf<String?>(null) }
    var isCheckingForUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }

    if (availableUpdateNotice != null) {
        SettingsActionCard(
            title = SupportSettingCopy.versionTitle,
            descriptionText = stringResource(
                R.string.settings_support_update_available,
                availableUpdateNotice.latestVersionName,
                availableUpdateNotice.currentVersionName,
            ),
            buttonText = when {
                isDownloadingUpdate -> stringResource(R.string.settings_support_update_downloading)
                availableUpdateNotice.apkAsset != null -> stringResource(R.string.settings_support_update_download_button)
                else -> stringResource(R.string.settings_support_update_open_action)
            },
            enabled = !isDownloadingUpdate,
            onClick = {
                val apkAsset = availableUpdateNotice.apkAsset
                if (apkAsset == null) {
                    statusText = openSupportUrl(context, availableUpdateNotice.releaseUrl)
                } else if (!updateInstaller.canRequestPackageInstalls(context)) {
                    updateInstaller.openUnknownAppSourcesSettings(context)
                    updateStatusText = context.getString(R.string.settings_support_update_install_permission_required)
                } else {
                    scope.launch {
                        isDownloadingUpdate = true
                        updateStatusText = context.getString(R.string.settings_support_update_downloading)
                        runCatching {
                            val apkFile = updateInstaller.downloadApk(context, apkAsset)
                            updateInstaller.launchInstaller(context, apkFile)
                        }.onSuccess {
                            updateStatusText = context.getString(R.string.settings_support_update_install_started)
                        }.onFailure {
                            updateStatusText = context.getString(R.string.settings_support_update_download_failed)
                        }
                        isDownloadingUpdate = false
                    }
                }
            },
        )
    } else {
        SettingsLinkCard(
            title = SupportSettingCopy.versionTitle,
            descriptionText = stringResource(R.string.settings_support_version_description, currentVersion),
            onClick = {
                statusText = openSupportUrl(context, SupportSettingCopy.releasesUrl)
            },
        )
    }
    SettingsActionCard(
        title = SupportSettingCopy.updateCheckTitle,
        descriptionText = updateStatusText,
        buttonText = if (isCheckingForUpdate) {
            stringResource(R.string.settings_support_update_checking)
        } else {
            stringResource(R.string.settings_support_update_check_button)
        },
        enabled = !isCheckingForUpdate,
        onClick = {
            scope.launch {
                isCheckingForUpdate = true
                updateStatusText = context.getString(R.string.settings_support_update_checking)
                runCatching { updateChecker.checkForUpdate(currentVersion) }
                    .onSuccess { notice ->
                        onUpdateNoticeChange(notice)
                        updateStatusText = if (notice != null) {
                            context.getString(
                                R.string.settings_support_update_available,
                                notice.latestVersionName,
                                notice.currentVersionName,
                            )
                        } else {
                            context.getString(R.string.settings_support_update_current, currentVersion)
                        }
                    }
                    .onFailure {
                        updateStatusText = context.getString(R.string.settings_support_update_failed)
                    }
                isCheckingForUpdate = false
            }
        },
    )
    SettingsLinkCard(
        title = SupportSettingCopy.issueTitle,
        descriptionText = null,
        onClick = {
            statusText = openSupportUrl(context, SupportSettingCopy.issuesUrl)
        },
    )
    statusText?.let {
        Text(it, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun SettingsActionCard(
    @StringRes title: Int,
    descriptionText: String?,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            descriptionText?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onClick, enabled = enabled) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun SettingsLinkCard(
    @StringRes title: Int,
    descriptionText: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            descriptionText?.takeIf { it.isNotBlank() }?.let { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun openSupportUrl(context: Context, url: String): String? {
    return runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        null
    }.getOrElse {
        context.getString(R.string.settings_support_link_open_failed)
    }
}

private fun currentAppVersionName(context: Context): String {
    val fallback = context.getString(R.string.settings_support_unknown_version)
    return runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: fallback
    }.getOrDefault(fallback)
}

@Composable
private fun SettingsUiScaleSliderCard(
    uiScale: StashUiScale,
    onUiScaleChange: (StashUiScale) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(UiScaleSettingCopy.sliderValueFor(uiScale)) }

    LaunchedEffect(uiScale) {
        sliderValue = UiScaleSettingCopy.sliderValueFor(uiScale)
    }

    val previewOption = UiScaleSettingCopy.optionForSliderValue(sliderValue)
    val previewPercent = (previewOption.uiScale.multiplier * 100).roundToInt()
    val previewLabel = stringResource(previewOption.label)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(UiScaleSettingCopy.title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (uiScale != StashUiScale.default) {
                    TextButton(
                        onClick = {
                            sliderValue = UiScaleSettingCopy.sliderValueFor(StashUiScale.default)
                            onUiScaleChange(StashUiScale.default)
                        },
                    ) {
                        Text(stringResource(UiScaleSettingCopy.resetButton))
                    }
                }
            }
            Text(
                stringResource(R.string.settings_ui_scale_value_format, previewLabel, previewPercent),
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    val selectedScale = UiScaleSettingCopy.optionForSliderValue(sliderValue).uiScale
                    sliderValue = UiScaleSettingCopy.sliderValueFor(selectedScale)
                    if (selectedScale != uiScale) {
                        onUiScaleChange(selectedScale)
                    }
                },
                valueRange = UiScaleSettingCopy.sliderValueRange,
                steps = UiScaleSettingCopy.sliderSteps,
            )
        }
    }
}

@Composable
private fun SettingsSubtitleFontScaleSliderCard(
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(coerceSubtitleFontScale(fontScale)) }

    LaunchedEffect(fontScale) {
        sliderValue = coerceSubtitleFontScale(fontScale)
    }

    val previewPercent = (coerceSubtitleFontScale(sliderValue) * 100).roundToInt()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(SubtitleFontScaleSettingCopy.title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (coerceSubtitleFontScale(fontScale) != SUBTITLE_FONT_SCALE_DEFAULT) {
                    TextButton(
                        onClick = {
                            sliderValue = SUBTITLE_FONT_SCALE_DEFAULT
                            onFontScaleChange(SUBTITLE_FONT_SCALE_DEFAULT)
                        },
                    ) {
                        Text(stringResource(SubtitleFontScaleSettingCopy.resetButton))
                    }
                }
            }
            Text(
                stringResource(R.string.settings_subtitle_font_scale_value_format, previewPercent),
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = coerceSubtitleFontScale(it) },
                onValueChangeFinished = {
                    val selectedScale = coerceSubtitleFontScale(sliderValue)
                    sliderValue = selectedScale
                    if (selectedScale != coerceSubtitleFontScale(fontScale)) {
                        onFontScaleChange(selectedScale)
                    }
                },
                valueRange = SUBTITLE_FONT_SCALE_MIN..SUBTITLE_FONT_SCALE_MAX,
                steps = 4,
            )
        }
    }
}

@Composable
private fun SettingsSwitchCard(
    @StringRes title: Int,
    @StringRes description: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun ShortsRecommendationResetCard(
    @StringRes feedback: Int?,
    onRequestReset: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(ShortsRecommendationResetSettingCopy.title),
                style = MaterialTheme.typography.titleMedium,
            )
            Button(onClick = onRequestReset) {
                Text(stringResource(ShortsRecommendationResetSettingCopy.button))
            }
            feedback?.let { message ->
                Text(
                    text = stringResource(message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ShortsMaxDurationSliderCard(
    maxDurationSeconds: Int,
    onMaxDurationChange: (Int) -> Unit,
) {
    var localValue by remember(maxDurationSeconds) { mutableFloatStateOf(maxDurationSeconds.toFloat()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(ShortsMaxDurationSettingCopy.title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = stringResource(ShortsMaxDurationSettingCopy.valueLabel, localValue.roundToInt()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = localValue,
                onValueChange = { localValue = it },
                onValueChangeFinished = { onMaxDurationChange(localValue.roundToInt()) },
                valueRange = ShortsMaxDurationSettingCopy.sliderValueRange,
                steps = ShortsMaxDurationSettingCopy.sliderSteps,
            )
        }
    }
}

@Composable
private fun SettingsRadioGroupCard(
    @StringRes title: Int,
    @StringRes description: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsRadioRow(
    selected: Boolean,
    @StringRes label: Int,
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SettingsAccentColorRow(
    selected: Boolean,
    @StringRes label: Int,
    @StringRes description: Int,
    swatchColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(swatchColor, CircleShape),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
