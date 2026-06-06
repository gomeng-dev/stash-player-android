package gomeng.dev.stashplayer.core.player

enum class PlaybackOrientationMode(val persistedValue: String) {
    Off("off"),
    Sensor("sensor");

    companion object {
        val default: PlaybackOrientationMode = Off

        fun fromPersistedValue(value: String?): PlaybackOrientationMode =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

enum class PlayerSideGestureLayout(val persistedValue: String) {
    Default("default"),
    Reversed("reversed");

    companion object {
        val default: PlayerSideGestureLayout = Default

        fun fromPersistedValue(value: String?): PlayerSideGestureLayout =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

enum class FastPlaybackHoldSpeedPreference(
    val persistedValue: String,
    val playbackSpeed: Float?,
    val displayLabel: String,
) {
    Off("off", null, "끄기"),
    OnePointTwentyFive("1.25", 1.25f, "1.25x"),
    OnePointFive("1.5", 1.5f, "1.5x"),
    OnePointSeventyFive("1.75", 1.75f, "1.75x"),
    TwoPointZero("2.0", 2.0f, "2.0x");

    val enabled: Boolean get() = playbackSpeed != null

    companion object {
        val default: FastPlaybackHoldSpeedPreference = OnePointFive

        fun fromPersistedValue(value: String?): FastPlaybackHoldSpeedPreference =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

enum class SubtitleLanguagePreference(
    val persistedValue: String,
    val mediaLanguageCode: String?,
) {
    Auto("auto", null),
    Off("off", null),
    Korean("ko", "ko"),
    English("en", "en"),
    Japanese("ja", "ja"),
    Chinese("zh", "zh"),
    Russian("ru", "ru"),
    Spanish("es", "es");

    companion object {
        val default: SubtitleLanguagePreference = Auto

        fun fromPersistedValue(value: String?): SubtitleLanguagePreference =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

enum class SubtitlePosition(val persistedValue: String) {
    Bottom("bottom"),
    Middle("middle"),
    Top("top");

    companion object {
        val default: SubtitlePosition = Bottom

        fun fromPersistedValue(value: String?): SubtitlePosition =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

enum class SubtitleTextAlignment(val persistedValue: String) {
    Start("start"),
    Center("center"),
    End("end");

    companion object {
        val default: SubtitleTextAlignment = Center

        fun fromPersistedValue(value: String?): SubtitleTextAlignment =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

const val SUBTITLE_FONT_SCALE_MIN = 0.75f
const val SUBTITLE_FONT_SCALE_MAX = 2.0f
const val SUBTITLE_FONT_SCALE_DEFAULT = 1.0f

fun coerceSubtitleFontScale(value: Float?): Float =
    (value ?: SUBTITLE_FONT_SCALE_DEFAULT).coerceIn(SUBTITLE_FONT_SCALE_MIN, SUBTITLE_FONT_SCALE_MAX)
