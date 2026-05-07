package gomeng.dev.stashplayer.feature.player

import gomeng.dev.stashplayer.core.player.coerceSeekRequestPosition
import gomeng.dev.stashplayer.core.player.shouldClearPendingSeekWatch
import gomeng.dev.stashplayer.core.player.shouldFallbackAfterSeekReset
import gomeng.dev.stashplayer.core.player.shouldHoldPlaybackForSeekPreview
import gomeng.dev.stashplayer.core.player.shouldResumePlaybackAfterSeekRelease
import gomeng.dev.stashplayer.core.player.shouldResumePlaybackForSeekPreview
import gomeng.dev.stashplayer.core.player.shouldSkipFinalSeekAfterWarm
import gomeng.dev.stashplayer.core.player.shouldWarmSeekPreview
import gomeng.dev.stashplayer.core.player.updatedSeekPreviewResumeIntent

/**
 * Pure state holder for seek preview/warm-seek bookkeeping that used to live directly in PlayerRoute.
 */
data class PlayerSeekPreviewControllerState(
    val preview: PlayerSeekPreview? = null,
    val resumeAfterPreview: Boolean = false,
    val lastWarmSeekTargetMs: Long? = null,
    val lastWarmSeekAtMs: Long = 0L,
    val pendingSeekTargetMs: Long? = null,
    val pendingSeekStartedAtMs: Long = 0L,
)

data class PlayerSeekRequest(
    val targetPositionMs: Long,
    val resumePlayback: Boolean,
)

data class PlayerSeekPreviewUpdate(
    val state: PlayerSeekPreviewControllerState,
    val seekRequest: PlayerSeekRequest? = null,
    val holdPlayback: Boolean = false,
    val markResumeSaveAtMs: Long? = null,
    val displayPositionMs: Long? = null,
)

data class PlayerSeekReleaseUpdate(
    val state: PlayerSeekPreviewControllerState,
    val seekRequest: PlayerSeekRequest? = null,
    val resumeWithoutSeek: Boolean = false,
    val markResumeSaveAtMs: Long,
    val displayPositionMs: Long,
)

sealed interface PlayerPendingSeekAction {
    data object Keep : PlayerPendingSeekAction
    data object Clear : PlayerPendingSeekAction
    data class Fallback(val targetPositionMs: Long) : PlayerPendingSeekAction
}

data class PlayerPendingSeekWatchUpdate(
    val state: PlayerSeekPreviewControllerState,
    val action: PlayerPendingSeekAction,
)

object PlayerSeekPreviewController {
    fun updatePreview(
        state: PlayerSeekPreviewControllerState,
        preview: PlayerSeekPreview?,
        durationMs: Long,
        wasPlaying: Boolean,
        playWhenReady: Boolean,
        isBuffering: Boolean,
        nowMs: Long,
    ): PlayerSeekPreviewUpdate {
        if (preview == null) {
            return PlayerSeekPreviewUpdate(
                state = state.copy(
                    preview = null,
                    resumeAfterPreview = false,
                ),
            )
        }

        val isStartingPreview = state.preview == null
        val resumeAfterPreview = updatedSeekPreviewResumeIntent(
            currentResumeIntent = state.resumeAfterPreview,
            isStartingPreview = isStartingPreview,
            wasPlaying = wasPlaying,
            playWhenReady = playWhenReady,
            isBuffering = isBuffering,
        )
        val warmedTargetMs = coerceSeekRequestPosition(preview.targetPositionMs, durationMs)
        val warmAllowed = shouldWarmSeekPreview(
            lastWarmTargetMs = state.lastWarmSeekTargetMs,
            lastWarmAtMs = state.lastWarmSeekAtMs,
            targetPositionMs = warmedTargetMs,
            nowMs = nowMs,
        )
        val baseState = state.copy(
            preview = preview,
            resumeAfterPreview = resumeAfterPreview,
        )
        if (!warmAllowed) {
            return PlayerSeekPreviewUpdate(state = baseState)
        }

        val shouldHoldPlayback = shouldHoldPlaybackForSeekPreview(
            wasPlaying = wasPlaying,
            playWhenReady = playWhenReady,
            isBuffering = isBuffering,
        )
        val shouldResume = shouldResumePlaybackForSeekPreview(
            wasPlaying = wasPlaying,
            playWhenReady = playWhenReady,
            isBuffering = isBuffering,
        )
        return PlayerSeekPreviewUpdate(
            state = baseState.copy(
                lastWarmSeekTargetMs = warmedTargetMs,
                lastWarmSeekAtMs = nowMs,
            ),
            seekRequest = PlayerSeekRequest(
                targetPositionMs = warmedTargetMs,
                resumePlayback = shouldResume,
            ),
            holdPlayback = shouldHoldPlayback,
            markResumeSaveAtMs = nowMs,
            displayPositionMs = warmedTargetMs,
        )
    }

    fun releaseSeek(
        state: PlayerSeekPreviewControllerState,
        targetPositionMs: Long,
        durationMs: Long,
        wasPlaying: Boolean,
        playWhenReady: Boolean,
        isBuffering: Boolean,
        nowMs: Long,
    ): PlayerSeekReleaseUpdate {
        val isSeekPreviewRelease = state.preview != null
        val shouldResume = shouldResumePlaybackAfterSeekRelease(
            wasPlaying = wasPlaying,
            playWhenReady = playWhenReady,
            isBuffering = isBuffering,
            resumeAfterSeekPreview = state.resumeAfterPreview,
            isSeekPreviewRelease = isSeekPreviewRelease,
        )
        val coercedPositionMs = coerceSeekRequestPosition(targetPositionMs, durationMs)
        val shouldSkipSeek = shouldSkipFinalSeekAfterWarm(
            lastWarmTargetMs = state.lastWarmSeekTargetMs,
            finalTargetMs = coercedPositionMs,
            lastWarmAtMs = state.lastWarmSeekAtMs,
            nowMs = nowMs,
        )
        val nextState = state.copy(
            resumeAfterPreview = false,
            pendingSeekTargetMs = coercedPositionMs,
            pendingSeekStartedAtMs = nowMs,
            lastWarmSeekTargetMs = if (shouldSkipSeek) state.lastWarmSeekTargetMs else coercedPositionMs,
            lastWarmSeekAtMs = if (shouldSkipSeek) state.lastWarmSeekAtMs else nowMs,
        )
        return PlayerSeekReleaseUpdate(
            state = nextState,
            seekRequest = if (shouldSkipSeek) {
                null
            } else {
                PlayerSeekRequest(coercedPositionMs, shouldResume)
            },
            resumeWithoutSeek = shouldSkipSeek && shouldResume,
            markResumeSaveAtMs = nowMs,
            displayPositionMs = coercedPositionMs,
        )
    }

    fun resolvePendingSeekWatch(
        state: PlayerSeekPreviewControllerState,
        currentPositionMs: Long,
        hasFallbackCandidate: Boolean,
        nowMs: Long,
    ): PlayerPendingSeekWatchUpdate {
        val pendingTarget = state.pendingSeekTargetMs
            ?: return PlayerPendingSeekWatchUpdate(state, PlayerPendingSeekAction.Keep)
        val elapsedSinceSeekMs = nowMs - state.pendingSeekStartedAtMs
        return when {
            shouldFallbackAfterSeekReset(
                pendingTargetMs = pendingTarget,
                elapsedSinceSeekMs = elapsedSinceSeekMs,
                currentPositionMs = currentPositionMs,
                hasFallbackCandidate = hasFallbackCandidate,
            ) -> PlayerPendingSeekWatchUpdate(
                state = state.clearPendingSeekWatch(),
                action = PlayerPendingSeekAction.Fallback(pendingTarget),
            )
            shouldClearPendingSeekWatch(
                pendingTargetMs = pendingTarget,
                elapsedSinceSeekMs = elapsedSinceSeekMs,
                currentPositionMs = currentPositionMs,
            ) -> PlayerPendingSeekWatchUpdate(
                state = state.clearPendingSeekWatch(),
                action = PlayerPendingSeekAction.Clear,
            )
            else -> PlayerPendingSeekWatchUpdate(state, PlayerPendingSeekAction.Keep)
        }
    }

    fun cleanupTransientSeekState(state: PlayerSeekPreviewControllerState): PlayerSeekPreviewControllerState =
        state.copy(
            preview = null,
            resumeAfterPreview = false,
            pendingSeekTargetMs = null,
            pendingSeekStartedAtMs = 0L,
        )

    private fun PlayerSeekPreviewControllerState.clearPendingSeekWatch(): PlayerSeekPreviewControllerState =
        copy(
            pendingSeekTargetMs = null,
            pendingSeekStartedAtMs = 0L,
        )
}
