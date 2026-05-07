package gomeng.dev.stashplayer.core.ui.theme

enum class StashThemeMode(val persistedValue: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light"),
    ;

    companion object {
        val default: StashThemeMode = DARK

        fun fromPersistedValue(value: String?): StashThemeMode =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

fun resolveStashDarkTheme(
    mode: StashThemeMode,
    systemInDarkTheme: Boolean,
): Boolean = when (mode) {
    StashThemeMode.SYSTEM -> systemInDarkTheme
    StashThemeMode.DARK -> true
    StashThemeMode.LIGHT -> false
}
