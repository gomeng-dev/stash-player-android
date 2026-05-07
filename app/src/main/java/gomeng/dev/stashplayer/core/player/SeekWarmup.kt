package gomeng.dev.stashplayer.core.player

import kotlin.math.abs

const val SEEK_PREVIEW_WARM_THROTTLE_MS: Long = 500L
const val SEEK_PREVIEW_FINAL_SKIP_TOLERANCE_MS: Long = 250L
const val SEEK_PREVIEW_FINAL_SKIP_MAX_AGE_MS: Long = 1_000L

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

fun shouldSkipFinalSeekAfterWarm(
    lastWarmTargetMs: Long?,
    finalTargetMs: Long,
    lastWarmAtMs: Long,
    nowMs: Long,
    toleranceMs: Long = SEEK_PREVIEW_FINAL_SKIP_TOLERANCE_MS,
    maxWarmAgeMs: Long = SEEK_PREVIEW_FINAL_SKIP_MAX_AGE_MS,
): Boolean {
    val previousTarget = lastWarmTargetMs ?: return false
    val warmAgeMs = nowMs - lastWarmAtMs
    return warmAgeMs in 0L..maxWarmAgeMs && abs(finalTargetMs - previousTarget) <= toleranceMs
}
