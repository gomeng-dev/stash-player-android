package gomeng.dev.stashplayer.core.player

const val SEEK_PREVIEW_WARM_THROTTLE_MS: Long = 500L

@Suppress("UNUSED_PARAMETER")
fun shouldWarmSeekPreview(
    lastWarmTargetMs: Long?,
    lastWarmAtMs: Long,
    targetPositionMs: Long,
    nowMs: Long,
    throttleMs: Long = SEEK_PREVIEW_WARM_THROTTLE_MS,
): Boolean {
    lastWarmTargetMs ?: return true
    return nowMs - lastWarmAtMs >= throttleMs
}
