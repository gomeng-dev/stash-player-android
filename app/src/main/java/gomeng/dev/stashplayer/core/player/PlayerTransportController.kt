package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation

sealed interface PlayerAutoAdvanceDecision {
    data object None : PlayerAutoAdvanceDecision
    data class Advance(
        val nextSceneId: String,
        val queue: PlayerPlaybackQueue,
        val autoAdvancedFromSceneId: String,
    ) : PlayerAutoAdvanceDecision
}

object PlayerTransportController {
    fun shouldAutoAdvanceOnEnded(playbackEndAction: PlaybackEndAction): Boolean =
        playbackEndAction == PlaybackEndAction.PlayNext

    fun shouldRepeatOnEnded(
        playbackStatus: PlayerPlaybackUiStatus,
        playbackEndAction: PlaybackEndAction,
        currentSceneId: String,
        repeatedFromSceneId: String?,
        autoAdvanceArmed: Boolean = true,
    ): Boolean =
        playbackStatus == PlayerPlaybackUiStatus.Ended &&
            playbackEndAction == PlaybackEndAction.Repeat &&
            repeatedFromSceneId != currentSceneId &&
            autoAdvanceArmed

    fun resolvePreviousAction(
        currentPositionMs: Long,
        previousSceneId: String?,
        restartThresholdMs: Long = PLAYER_PREVIOUS_RESTART_THRESHOLD_MS,
    ): PlayerPreviousAction = if (currentPositionMs > restartThresholdMs || previousSceneId == null) {
        PlayerPreviousAction.RestartCurrent
    } else {
        PlayerPreviousAction.OpenPrevious(previousSceneId)
    }

    fun resolveNextAction(nextSceneId: String?): PlayerNextAction = if (nextSceneId == null) {
        PlayerNextAction.Unavailable
    } else {
        PlayerNextAction.OpenNext(nextSceneId)
    }

    fun resolveEndedAutoAdvance(
        playbackStatus: PlayerPlaybackUiStatus,
        currentSceneId: String,
        autoAdvancedFromSceneId: String?,
        queue: PlayerPlaybackQueue,
        autoAdvanceArmed: Boolean = true,
    ): PlayerAutoAdvanceDecision {
        if (
            playbackStatus != PlayerPlaybackUiStatus.Ended ||
            autoAdvancedFromSceneId == currentSceneId ||
            !autoAdvanceArmed
        ) {
            return PlayerAutoAdvanceDecision.None
        }
        val playbackOrder = queue.playbackOrderSceneIds()
        if (currentSceneId !in playbackOrder) return PlayerAutoAdvanceDecision.None
        val queueWithCurrent = updateCurrentScene(queue, currentSceneId)
        val nextSceneId = queueWithCurrent.nextSceneId() ?: return PlayerAutoAdvanceDecision.None
        return PlayerAutoAdvanceDecision.Advance(
            nextSceneId = nextSceneId,
            queue = updateCurrentScene(queueWithCurrent, nextSceneId),
            autoAdvancedFromSceneId = currentSceneId,
        )
    }

    fun resolveEndedRecommendedAutoAdvance(
        playbackStatus: PlayerPlaybackUiStatus,
        currentSceneId: String,
        autoAdvancedFromSceneId: String?,
        queue: PlayerPlaybackQueue,
        recommendations: List<SimilarSceneRecommendation>,
        autoAdvanceArmed: Boolean = true,
    ): PlayerAutoAdvanceDecision {
        if (
            playbackStatus != PlayerPlaybackUiStatus.Ended ||
            autoAdvancedFromSceneId == currentSceneId ||
            !autoAdvanceArmed
        ) {
            return PlayerAutoAdvanceDecision.None
        }
        val queueWithCurrent = updateCurrentScene(queue, currentSceneId)
        if (queueWithCurrent.currentSceneId != currentSceneId) return PlayerAutoAdvanceDecision.None
        if (queueWithCurrent.nextSceneId() != null) return PlayerAutoAdvanceDecision.None
        val appendResult = appendTopRecommendedSceneToPlaybackQueue(
            queue = queueWithCurrent,
            currentSceneId = currentSceneId,
            recommendations = recommendations,
        ) ?: return PlayerAutoAdvanceDecision.None
        return PlayerAutoAdvanceDecision.Advance(
            nextSceneId = appendResult.appendedSceneId,
            queue = updateCurrentScene(appendResult.queue, appendResult.appendedSceneId),
            autoAdvancedFromSceneId = currentSceneId,
        )
    }

    fun updateCurrentScene(
        queue: PlayerPlaybackQueue,
        sceneId: String,
    ): PlayerPlaybackQueue = selectPlayerPlaylistItem(queue, sceneId)

    fun resolveBackAction(
        playlistDrawerOpen: Boolean,
        playbackOptionsOpen: Boolean = false,
        debugSurfaceOpen: Boolean = false,
        infoDrawerExpanded: Boolean,
        controlsVisible: Boolean,
    ): PlayerBackAction = when {
        playbackOptionsOpen -> PlayerBackAction.DismissPlaybackOptions
        playlistDrawerOpen -> PlayerBackAction.DismissPlaylistDrawer
        debugSurfaceOpen -> PlayerBackAction.DismissDebugSurface
        controlsVisible -> PlayerBackAction.HideControls
        else -> PlayerBackAction.ExitPlayer
    }
}
