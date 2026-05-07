package gomeng.dev.stashplayer.core.player

enum class PlaybackEndAction(val persistedValue: String) {
    Stop("stop"),
    Repeat("repeat"),
    PlayNext("play_next");

    companion object {
        val default: PlaybackEndAction = PlayNext

        fun fromPersistedValue(value: String?): PlaybackEndAction =
            entries.firstOrNull { it.persistedValue == value } ?: default
    }
}
