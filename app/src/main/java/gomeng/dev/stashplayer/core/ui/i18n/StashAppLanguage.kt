package gomeng.dev.stashplayer.core.ui.i18n

enum class StashAppLanguage(
    val persistedValue: String,
    val localeTag: String?,
) {
    SYSTEM("system", null),
    KOREAN("ko", "ko"),
    ENGLISH("en", "en"),
    CHINESE_SIMPLIFIED("zh-hans", "zh-Hans"),
    CHINESE_TRADITIONAL("zh-hant", "zh-Hant"),
    ;

    companion object {
        val default: StashAppLanguage = SYSTEM

        fun fromPersistedValue(value: String?): StashAppLanguage =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}
