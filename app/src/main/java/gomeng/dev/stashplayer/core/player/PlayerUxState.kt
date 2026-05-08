package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
val PLAYER_CONTROLS_AUTO_HIDE_MS: Long = 3_000L
val PLAYER_PREVIOUS_RESTART_THRESHOLD_MS: Long = 3_000L
val PLAYER_RESUME_SAVE_INTERVAL_MS: Long = 15_000L
val PLAYER_RESUME_SAVE_SEEK_DEBOUNCE_MS: Long = 1_500L
val PLAYER_RESUME_SAVE_MIN_POSITION_DELTA_MS: Long = 1_000L
val PLAYER_WATCHED_THRESHOLD_FRACTION: Double = 0.96
val PLAYER_WATCHED_MIN_DURATION_MS: Long = 30_000L
val PLAYER_ADD_PLAY_MIN_PLAY_SECONDS: Double = 30.0
val PLAYER_ADD_PLAY_GUARD_WINDOW_MS: Long = 10 * 60 * 1000L

sealed interface PlayerPreviousAction {
    data object RestartCurrent : PlayerPreviousAction
    data class OpenPrevious(val sceneId: String) : PlayerPreviousAction
}

sealed interface PlayerNextAction {
    data object Unavailable : PlayerNextAction
    data class OpenNext(val sceneId: String) : PlayerNextAction
}

sealed interface PlayerBackAction {
    data object DismissPlaybackOptions : PlayerBackAction
    data object DismissPlaylistDrawer : PlayerBackAction
    data object DismissDebugSurface : PlayerBackAction
    data object HideControls : PlayerBackAction
    data object ExitPlayer : PlayerBackAction
}

enum class PlayerPlaylistDrawerHostScope {
    PlayerOverlay,
    FullPlayerPage,
}

data class PlayerPlaylistDrawerPresentationPolicy(
    val hostScope: PlayerPlaylistDrawerHostScope,
    val coversWatchPageContent: Boolean,
    val suspendsPlayerGestures: Boolean,
)

fun resolvePlayerPlaylistDrawerPresentationPolicy(
    playlistDrawerOpen: Boolean,
): PlayerPlaylistDrawerPresentationPolicy = PlayerPlaylistDrawerPresentationPolicy(
    hostScope = PlayerPlaylistDrawerHostScope.FullPlayerPage,
    coversWatchPageContent = playlistDrawerOpen,
    suspendsPlayerGestures = playlistDrawerOpen,
)

data class PlayerLockToggleResult(
    val locked: Boolean,
    val hudText: String,
    val controlsVisible: Boolean,
)

data class PlayerPlaybackSpeedQuickOption(
    val speed: Float,
    val label: String,
    val selected: Boolean,
)

data class PlayerAspectRatioQuickOption(
    val mode: AspectRatioMode,
    val label: String,
    val selected: Boolean,
)

data class PlayerResumeActivitySavePayload(
    val resumeTimeSeconds: Double,
    val playDurationSeconds: Double?,
    val treatAsWatched: Boolean,
)

data class PlayerResumeSaveAttempt(
    val positionMs: Long,
    val attemptedAtMs: Long,
)

data class PlayerAddPlayGuardState(
    val lastSuccessfulAddPlayAtMsBySceneId: Map<String, Long> = emptyMap(),
    val inFlightSceneIds: Set<String> = emptySet(),
)

data class PlayerAddPlayAttemptDecision(
    val shouldCallAddPlay: Boolean,
    val state: PlayerAddPlayGuardState,
)

enum class PlayerResumeSaveReason {
    Periodic,
    Final,
}

enum class PlayerPlaybackUiStatus {
    Loading,
    Buffering,
    Ready,
    Ended,
    Error,
}

enum class PlayerErrorRecoveryAction {
    RetryPlayback,
    TryAlternateSource,
    OpenSettings,
    OpenNextScene,
}

data class PlayerErrorActionUiModel(
    val action: PlayerErrorRecoveryAction,
    val label: String,
    val primary: Boolean,
    val contentDescription: String = label,
    val minimumTouchTargetDp: Float = 48f,
)

enum class PlayerStatusOverlayContent {
    PlaybackStatus,
    Hud,
    Hidden,
}

fun buildPlayerErrorActionUiModels(
    status: PlayerPlaybackUiStatus,
    canTryAlternateSource: Boolean,
    canOpenSettings: Boolean,
    canOpenNextScene: Boolean = false,
): List<PlayerErrorActionUiModel> = when (status) {
    PlayerPlaybackUiStatus.Error -> buildList {
        add(
            PlayerErrorActionUiModel(
                action = PlayerErrorRecoveryAction.RetryPlayback,
                label = stashString(R.string.auto_kr_0031),
                primary = true,
                contentDescription = stashString(R.string.auto_kr_0267),
            ),
        )
        if (canTryAlternateSource) {
            add(
                PlayerErrorActionUiModel(
                    action = PlayerErrorRecoveryAction.TryAlternateSource,
                    label = stashString(R.string.auto_kr_0268),
                    primary = false,
                    contentDescription = stashString(R.string.auto_kr_0269),
                ),
            )
        }
        if (canOpenSettings) {
            add(
                PlayerErrorActionUiModel(
                    action = PlayerErrorRecoveryAction.OpenSettings,
                    label = stashString(R.string.auto_kr_0270),
                    primary = false,
                    contentDescription = stashString(R.string.auto_kr_0271),
                ),
            )
        }
    }
    PlayerPlaybackUiStatus.Ended -> buildList {
        if (canOpenNextScene) {
            add(
                PlayerErrorActionUiModel(
                    action = PlayerErrorRecoveryAction.OpenNextScene,
                    label = stashString(R.string.auto_kr_0225),
                    primary = true,
                    contentDescription = stashString(R.string.auto_kr_0272),
                ),
            )
            add(
                PlayerErrorActionUiModel(
                    action = PlayerErrorRecoveryAction.RetryPlayback,
                    label = stashString(R.string.auto_kr_0273),
                    primary = false,
                    contentDescription = stashString(R.string.auto_kr_0274),
                ),
            )
        } else {
            add(
                PlayerErrorActionUiModel(
                    action = PlayerErrorRecoveryAction.RetryPlayback,
                    label = stashString(R.string.auto_kr_0273),
                    primary = true,
                    contentDescription = stashString(R.string.auto_kr_0274),
                ),
            )
        }
    }
    PlayerPlaybackUiStatus.Loading,
    PlayerPlaybackUiStatus.Buffering,
    PlayerPlaybackUiStatus.Ready -> emptyList()
}

fun togglePlayerLockState(currentlyLocked: Boolean): PlayerLockToggleResult =
    if (currentlyLocked) {
        PlayerLockToggleResult(
            locked = false,
            hudText = stashString(R.string.auto_kr_0205),
            controlsVisible = true,
        )
    } else {
        PlayerLockToggleResult(
            locked = true,
            hudText = playerLockedTouchHint(),
            controlsVisible = false,
        )
    }

fun playerLockedTouchHint(): String = stashString(R.string.auto_kr_0275)

fun buildPlayerPlaybackSpeedQuickOptions(currentSpeed: Float): List<PlayerPlaybackSpeedQuickOption> =
    listOf(0.5f, 1f, 1.25f, 1.5f, 2f).map { speed ->
        PlayerPlaybackSpeedQuickOption(
            speed = speed,
            label = playerPlaybackSpeedLabel(speed),
            selected = speed == currentSpeed,
        )
    }

fun playerPlaybackSpeedLabel(speed: Float): String = if (speed == 1f) {
    stashString(R.string.auto_kr_0276)
} else {
    "${formatPlayerPlaybackSpeed(speed)}x"
}

fun playerPlaybackSpeedHudText(speed: Float): String = stashString(R.string.auto_kr_0277, formatPlayerPlaybackSpeed(speed))

private fun formatPlayerPlaybackSpeed(speed: Float): String {
    val roundedInt = speed.toInt()
    return if (speed == roundedInt.toFloat()) roundedInt.toString() else speed.toString()
}

fun buildPlayerAspectRatioQuickOptions(selectedMode: AspectRatioMode): List<PlayerAspectRatioQuickOption> =
    listOf(AspectRatioMode.Fit, AspectRatioMode.Stretch, AspectRatioMode.Crop).map { mode ->
        PlayerAspectRatioQuickOption(
            mode = mode,
            label = playerAspectRatioLabel(mode),
            selected = mode == selectedMode,
        )
    }

fun playerAspectRatioLabel(mode: AspectRatioMode): String = when (mode) {
    AspectRatioMode.Fit -> stashString(R.string.auto_kr_0278)
    AspectRatioMode.Stretch -> stashString(R.string.auto_kr_0279)
    AspectRatioMode.Crop -> stashString(R.string.auto_kr_0280)
}

fun playerAspectRatioHudText(mode: AspectRatioMode): String = stashString(R.string.auto_kr_0281, playerAspectRatioLabel(mode))

fun resolvePlayerPreviousAction(
    currentPositionMs: Long,
    previousSceneId: String?,
    restartThresholdMs: Long = PLAYER_PREVIOUS_RESTART_THRESHOLD_MS,
): PlayerPreviousAction = PlayerTransportController.resolvePreviousAction(
    currentPositionMs = currentPositionMs,
    previousSceneId = previousSceneId,
    restartThresholdMs = restartThresholdMs,
)

fun resolvePlayerNextAction(nextSceneId: String?): PlayerNextAction =
    PlayerTransportController.resolveNextAction(nextSceneId)

fun resolvePlayerBackAction(
    playlistDrawerOpen: Boolean,
    playbackOptionsOpen: Boolean = false,
    debugSurfaceOpen: Boolean = false,
    infoDrawerExpanded: Boolean = false,
    controlsVisible: Boolean = false,
): PlayerBackAction = PlayerTransportController.resolveBackAction(
    playlistDrawerOpen = playlistDrawerOpen,
    playbackOptionsOpen = playbackOptionsOpen,
    debugSurfaceOpen = debugSurfaceOpen,
    infoDrawerExpanded = infoDrawerExpanded,
    controlsVisible = controlsVisible,
)

fun shouldAttemptPlayerResumeSave(
    reason: PlayerResumeSaveReason,
    playbackPrepared: Boolean,
    isPlaying: Boolean,
    positionMs: Long,
    accumulatedPlaySeconds: Double,
    nowMs: Long,
    lastAttempt: PlayerResumeSaveAttempt?,
    lastSeekAtMs: Long,
    cadenceMs: Long = PLAYER_RESUME_SAVE_INTERVAL_MS,
    seekDebounceMs: Long = PLAYER_RESUME_SAVE_SEEK_DEBOUNCE_MS,
    minPositionDeltaMs: Long = PLAYER_RESUME_SAVE_MIN_POSITION_DELTA_MS,
): Boolean {
    if (!playbackPrepared || positionMs <= 0L) return false

    val positionChanged = lastAttempt == null || kotlin.math.abs(positionMs - lastAttempt.positionMs) >= minPositionDeltaMs
    if (!positionChanged) return false

    return when (reason) {
        PlayerResumeSaveReason.Final -> true
        PlayerResumeSaveReason.Periodic -> {
            val cadenceElapsed = lastAttempt == null || nowMs - lastAttempt.attemptedAtMs >= cadenceMs
            val seekSettled = lastSeekAtMs <= 0L || nowMs - lastSeekAtMs >= seekDebounceMs
            isPlaying && accumulatedPlaySeconds > 0.0 && cadenceElapsed && seekSettled
        }
    }
}

fun markPlayerResumeSaveAttempt(positionMs: Long, nowMs: Long): PlayerResumeSaveAttempt =
    PlayerResumeSaveAttempt(
        positionMs = positionMs.coerceAtLeast(0L),
        attemptedAtMs = nowMs,
    )

fun requestPlayerAddPlayAttempt(
    state: PlayerAddPlayGuardState,
    sceneId: String,
    accumulatedPlaySeconds: Double,
    nowMs: Long,
    minPlaySeconds: Double = PLAYER_ADD_PLAY_MIN_PLAY_SECONDS,
    guardWindowMs: Long = PLAYER_ADD_PLAY_GUARD_WINDOW_MS,
): PlayerAddPlayAttemptDecision {
    val trimmedSceneId = sceneId.trim()
    if (trimmedSceneId.isEmpty() || accumulatedPlaySeconds < minPlaySeconds) {
        return PlayerAddPlayAttemptDecision(shouldCallAddPlay = false, state = state)
    }
    if (trimmedSceneId in state.inFlightSceneIds) {
        return PlayerAddPlayAttemptDecision(shouldCallAddPlay = false, state = state)
    }
    val lastSuccessfulAtMs = state.lastSuccessfulAddPlayAtMsBySceneId[trimmedSceneId]
    if (lastSuccessfulAtMs != null && nowMs - lastSuccessfulAtMs <= guardWindowMs) {
        return PlayerAddPlayAttemptDecision(shouldCallAddPlay = false, state = state)
    }
    return PlayerAddPlayAttemptDecision(
        shouldCallAddPlay = true,
        state = state.copy(inFlightSceneIds = state.inFlightSceneIds + trimmedSceneId),
    )
}

fun markPlayerAddPlayAttemptSucceeded(
    state: PlayerAddPlayGuardState,
    sceneId: String,
    nowMs: Long,
): PlayerAddPlayGuardState {
    val trimmedSceneId = sceneId.trim()
    if (trimmedSceneId.isEmpty()) return state
    return state.copy(
        lastSuccessfulAddPlayAtMsBySceneId = state.lastSuccessfulAddPlayAtMsBySceneId + (trimmedSceneId to nowMs),
        inFlightSceneIds = state.inFlightSceneIds - trimmedSceneId,
    )
}

fun markPlayerAddPlayAttemptFailed(
    state: PlayerAddPlayGuardState,
    sceneId: String,
): PlayerAddPlayGuardState {
    val trimmedSceneId = sceneId.trim()
    if (trimmedSceneId.isEmpty()) return state
    return state.copy(inFlightSceneIds = state.inFlightSceneIds - trimmedSceneId)
}

fun isPlayerWatchedAtPosition(
    positionMs: Long,
    durationMs: Long,
    watchedThresholdFraction: Double = PLAYER_WATCHED_THRESHOLD_FRACTION,
    minDurationMs: Long = PLAYER_WATCHED_MIN_DURATION_MS,
): Boolean {
    if (positionMs <= 0L || durationMs < minDurationMs) return false
    if (watchedThresholdFraction <= 0.0 || watchedThresholdFraction > 1.0) return false
    return positionMs.toDouble() / durationMs.toDouble() >= watchedThresholdFraction
}

fun resolvePlayerResumeActivitySavePayload(
    positionMs: Long,
    durationMs: Long,
    accumulatedPlaySeconds: Double,
): PlayerResumeActivitySavePayload {
    val watched = isPlayerWatchedAtPosition(positionMs = positionMs, durationMs = durationMs)
    return PlayerResumeActivitySavePayload(
        resumeTimeSeconds = if (watched) 0.0 else positionMs.coerceAtLeast(0L) / 1000.0,
        playDurationSeconds = accumulatedPlaySeconds.takeIf { it > 0.0 },
        treatAsWatched = watched,
    )
}

fun shouldAutoHidePlayerControls(
    controlsVisible: Boolean,
    locked: Boolean,
    isPlaying: Boolean,
    hasTransientOverlay: Boolean,
    elapsedSinceInteractionMs: Long,
    timeoutMs: Long = PLAYER_CONTROLS_AUTO_HIDE_MS,
): Boolean =
    controlsVisible &&
        !locked &&
        isPlaying &&
        !hasTransientOverlay &&
        elapsedSinceInteractionMs >= timeoutMs

fun shouldResumePlaybackAfterSeek(
    wasPlaying: Boolean,
    playWhenReady: Boolean,
    isBuffering: Boolean = false,
): Boolean = wasPlaying || playWhenReady || isBuffering

@Suppress("UNUSED_PARAMETER")
fun shouldResumePlaybackForSeekPreview(
    wasPlaying: Boolean,
    playWhenReady: Boolean,
    isBuffering: Boolean = false,
): Boolean = false

fun shouldHoldPlaybackForSeekPreview(
    wasPlaying: Boolean,
    playWhenReady: Boolean,
    isBuffering: Boolean = false,
): Boolean = shouldResumePlaybackAfterSeek(wasPlaying, playWhenReady, isBuffering)

fun shouldResumePlaybackAfterSeekRelease(
    wasPlaying: Boolean,
    playWhenReady: Boolean,
    isBuffering: Boolean = false,
    resumeAfterSeekPreview: Boolean = false,
    isSeekPreviewRelease: Boolean = false,
): Boolean = if (isSeekPreviewRelease) {
    resumeAfterSeekPreview
} else {
    shouldResumePlaybackAfterSeek(wasPlaying, playWhenReady, isBuffering)
}

fun updatedSeekPreviewResumeIntent(
    currentResumeIntent: Boolean,
    isStartingPreview: Boolean,
    wasPlaying: Boolean,
    playWhenReady: Boolean,
    isBuffering: Boolean = false,
): Boolean = if (isStartingPreview) {
    shouldResumePlaybackAfterSeek(wasPlaying, playWhenReady, isBuffering)
} else {
    currentResumeIntent
}

fun shouldShowPlaybackStatusOverlay(
    status: PlayerPlaybackUiStatus,
    hasSeekPreview: Boolean,
    hasHudText: Boolean,
): Boolean =
    resolvePlayerStatusOverlayContent(
        status = status,
        hasSeekPreview = hasSeekPreview,
        hasHudText = hasHudText,
    ) == PlayerStatusOverlayContent.PlaybackStatus

fun resolvePlayerStatusOverlayContent(
    status: PlayerPlaybackUiStatus,
    hasSeekPreview: Boolean,
    hasHudText: Boolean,
): PlayerStatusOverlayContent = when {
    hasSeekPreview -> PlayerStatusOverlayContent.Hidden
    status in setOf(
        PlayerPlaybackUiStatus.Error,
        PlayerPlaybackUiStatus.Buffering,
        PlayerPlaybackUiStatus.Loading,
        PlayerPlaybackUiStatus.Ended,
    ) -> PlayerStatusOverlayContent.PlaybackStatus
    hasHudText -> PlayerStatusOverlayContent.Hud
    else -> PlayerStatusOverlayContent.Hidden
}

fun shouldAutoFallbackPlaybackSource(
    hasPlaybackError: Boolean,
    hasFallbackCandidate: Boolean,
): Boolean = hasPlaybackError && hasFallbackCandidate

fun sanitizePlaybackErrorText(message: String?): String? = message
    ?.replace(Regex("(?i)((?:api[_-]?key|apikey)=)([^&\\s]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)((?:token|secret|password|passwd)=)([^&\\s]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(\\bApiKey\\s*:\\s*)([^\\s,;]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(\\bAuthorization\\s*:\\s*Bearer\\s+)([^\\s,;]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(https?://)([^/@\\s]+)@")) { match ->
        "${match.groupValues[1]}[REDACTED]@"
    }
