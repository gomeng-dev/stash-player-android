package gomeng.dev.stashplayer.core.player

/**
 * Pure policy seam for PlayerRoute resume-save side effects.
 *
 * Route/effect layers still own clocks, player snapshots, coroutine launches, and network calls.
 * This policy only decides whether a save should be attempted, which payload to send, and how
 * the local effect state should move forward after seeks/saves.
 */
data class PlayerResumeSaveEffectState(
    val lastAttempt: PlayerResumeSaveAttempt? = null,
    val lastSeekAtMs: Long = 0L,
)

data class PlayerResumeSaveDecision(
    val shouldSave: Boolean,
    val state: PlayerResumeSaveEffectState,
    val payload: PlayerResumeActivitySavePayload?,
)

object PlayerResumeSyncPolicy {
    fun markSeekForResumeSave(
        state: PlayerResumeSaveEffectState,
        nowMs: Long,
    ): PlayerResumeSaveEffectState = state.copy(lastSeekAtMs = nowMs)

    fun resetSeekThrottleForStreamRefresh(
        state: PlayerResumeSaveEffectState,
    ): PlayerResumeSaveEffectState = state.copy(lastSeekAtMs = 0L)

    fun resolveResumeSave(
        state: PlayerResumeSaveEffectState,
        reason: PlayerResumeSaveReason,
        playbackPrepared: Boolean,
        isPlaying: Boolean,
        positionMs: Long,
        durationMs: Long,
        accumulatedPlaySeconds: Double,
        nowMs: Long,
    ): PlayerResumeSaveDecision {
        val shouldAttempt = shouldAttemptPlayerResumeSave(
            reason = reason,
            playbackPrepared = playbackPrepared,
            isPlaying = isPlaying,
            positionMs = positionMs,
            accumulatedPlaySeconds = accumulatedPlaySeconds,
            nowMs = nowMs,
            lastAttempt = state.lastAttempt,
            lastSeekAtMs = state.lastSeekAtMs,
        )
        if (!shouldAttempt) {
            return PlayerResumeSaveDecision(
                shouldSave = false,
                state = state,
                payload = null,
            )
        }

        val nextState = state.copy(lastAttempt = markPlayerResumeSaveAttempt(positionMs, nowMs))
        return PlayerResumeSaveDecision(
            shouldSave = true,
            state = nextState,
            payload = resolvePlayerResumeActivitySavePayload(
                positionMs = positionMs,
                durationMs = durationMs,
                accumulatedPlaySeconds = accumulatedPlaySeconds,
            ),
        )
    }
}

typealias PlayerSceneAddPlaySyncState = PlayerAddPlayGuardState

object PlayerSceneAddPlaySyncPolicy {
    fun resolveAttempt(
        state: PlayerSceneAddPlaySyncState,
        sceneId: String,
        accumulatedPlaySeconds: Double,
        nowMs: Long,
    ): PlayerAddPlayAttemptDecision = requestPlayerAddPlayAttempt(
        state = state,
        sceneId = sceneId,
        accumulatedPlaySeconds = accumulatedPlaySeconds,
        nowMs = nowMs,
    )

    fun markSucceeded(
        state: PlayerSceneAddPlaySyncState,
        sceneId: String,
        nowMs: Long,
    ): PlayerSceneAddPlaySyncState = markPlayerAddPlayAttemptSucceeded(state, sceneId, nowMs)

    fun markFailed(
        state: PlayerSceneAddPlaySyncState,
        sceneId: String,
    ): PlayerSceneAddPlaySyncState = markPlayerAddPlayAttemptFailed(state, sceneId)
}
