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
