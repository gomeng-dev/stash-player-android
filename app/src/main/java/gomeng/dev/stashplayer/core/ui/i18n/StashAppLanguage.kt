package gomeng.dev.stashplayer.core.ui.i18n

enum class StashAppLanguage(
    val persistedValue: String,
    val localeTag: String?,
) {
    SYSTEM("system", null),
    KOREAN("ko", "ko"),
    ENGLISH("en", "en"),
    ;

    companion object {
        val default: StashAppLanguage = SYSTEM

        fun fromPersistedValue(value: String?): StashAppLanguage =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}
