package gomeng.dev.stashplayer.core.player

import java.util.concurrent.atomic.AtomicLong

private val playerMediaSessionIdCounter = AtomicLong(0L)

internal fun nextPlayerMediaSessionId(): String =
    "stash-player-session-${playerMediaSessionIdCounter.incrementAndGet()}"
