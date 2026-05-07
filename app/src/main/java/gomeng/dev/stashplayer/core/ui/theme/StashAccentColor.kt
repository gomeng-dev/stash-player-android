package gomeng.dev.stashplayer.core.ui.theme

enum class StashAccentColor(val persistedValue: String) {
    Purple("purple"),
    Blue("blue"),
    Cyan("cyan"),
    Amber("amber"),
    Rose("rose");

    companion object {
        val default: StashAccentColor = Purple

        fun fromPersistedValue(value: String?): StashAccentColor =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}
