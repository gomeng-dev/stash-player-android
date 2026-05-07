package gomeng.dev.stashplayer.core.player

private const val DEFAULT_MIN_TARGET_MS = 15_000L
private const val DEFAULT_MIN_ELAPSED_MS = 2_000L
private const val DEFAULT_RESET_POSITION_THRESHOLD_MS = 3_000L
private const val DEFAULT_SETTLED_DISTANCE_MS = 5_000L
private const val DEFAULT_MAX_PENDING_MS = 4_500L

fun shouldFallbackAfterSeekReset(
    pendingTargetMs: Long?,
    elapsedSinceSeekMs: Long,
    currentPositionMs: Long,
    hasFallbackCandidate: Boolean,
    minTargetMs: Long = DEFAULT_MIN_TARGET_MS,
    minElapsedMs: Long = DEFAULT_MIN_ELAPSED_MS,
    resetPositionThresholdMs: Long = DEFAULT_RESET_POSITION_THRESHOLD_MS,
): Boolean {
    val target = pendingTargetMs ?: return false
    return hasFallbackCandidate &&
        target >= minTargetMs &&
        elapsedSinceSeekMs >= minElapsedMs &&
        currentPositionMs in 0L..resetPositionThresholdMs
}

fun shouldClearPendingSeekWatch(
    pendingTargetMs: Long?,
    elapsedSinceSeekMs: Long,
    currentPositionMs: Long,
    settledDistanceMs: Long = DEFAULT_SETTLED_DISTANCE_MS,
    maxPendingMs: Long = DEFAULT_MAX_PENDING_MS,
): Boolean {
    val target = pendingTargetMs ?: return true
    return elapsedSinceSeekMs >= maxPendingMs ||
        kotlin.math.abs(currentPositionMs - target) <= settledDistanceMs ||
        currentPositionMs > DEFAULT_RESET_POSITION_THRESHOLD_MS
}
