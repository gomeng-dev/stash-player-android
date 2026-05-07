package gomeng.dev.stashplayer.feature.player

import gomeng.dev.stashplayer.core.network.StashSpriteFrame

data class PlayerSeekPreview(
    val deltaMs: Long,
    val targetPositionMs: Long,
    val durationMs: Long,
    val frame: StashSpriteFrame? = null,
)
