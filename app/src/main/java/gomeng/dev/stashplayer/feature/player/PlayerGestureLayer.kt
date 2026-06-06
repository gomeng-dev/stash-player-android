package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.player.DoubleTapRegion
import gomeng.dev.stashplayer.core.player.PlayerGestureExclusionBounds
import gomeng.dev.stashplayer.core.player.PlayerGestureMode
import gomeng.dev.stashplayer.core.player.PlayerLongPressTimeoutAction
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragRelease
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragSession
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragUpdate
import gomeng.dev.stashplayer.core.player.PlayerPresentationGestureMode
import gomeng.dev.stashplayer.core.player.PlayerPresentationGestureStartArea
import gomeng.dev.stashplayer.core.player.PlayerPresentationMode
import gomeng.dev.stashplayer.core.player.PlayerSeekDragState
import gomeng.dev.stashplayer.core.player.PlayerSideGestureLayout
import gomeng.dev.stashplayer.core.player.calculateVerticalSideControlGestureFraction
import gomeng.dev.stashplayer.core.player.classifyDoubleTapRegion
import gomeng.dev.stashplayer.core.player.buildPlayerPresentationDragUpdate
import gomeng.dev.stashplayer.core.player.classifyPlayerDrag
import gomeng.dev.stashplayer.core.player.dragBy
import gomeng.dev.stashplayer.core.player.formatPlayerPosition
import gomeng.dev.stashplayer.core.player.playerLockedTouchHint
import gomeng.dev.stashplayer.core.player.remainingPlayerLongPressTimeoutMillis
import gomeng.dev.stashplayer.core.player.release
import gomeng.dev.stashplayer.core.player.resolvePlayerLongPressTimeoutAction
import gomeng.dev.stashplayer.core.player.shouldPlayerGestureLayerHandlePointerStart
import gomeng.dev.stashplayer.core.player.startPlayerPresentationDragSession
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.hypot
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerGestureLayer(
    locked: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onToggleOverlay: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    brightnessFraction: () -> Float,
    volumeFraction: () -> Float,
    onBrightnessFraction: (Float) -> String,
    onVolumeFraction: (Float) -> String,
    onHudText: (String?) -> Unit,
    onSeekPreview: (PlayerSeekPreview?) -> Unit,
    sideGestureLayout: PlayerSideGestureLayout = PlayerSideGestureLayout.default,
    fastPlaybackHoldEnabled: Boolean = true,
    onFastPlaybackHoldStart: () -> Unit,
    onFastPlaybackHoldEnd: () -> Unit,
    previewFrameFor: (Long) -> StashSpriteFrame?,
    modifier: Modifier = Modifier,
    gestureExclusionBounds: PlayerGestureExclusionBounds? = null,
    gesturesSuspendedByModalSurface: Boolean = false,
    presentationMode: PlayerPresentationMode? = null,
    onPresentationDragUpdate: (PlayerPresentationDragUpdate?) -> Unit = {},
    onPresentationDragRelease: (PlayerPresentationDragRelease) -> Unit = {},
) {
    val latestLocked by rememberUpdatedState(locked)
    val latestCurrentPositionMs by rememberUpdatedState(currentPositionMs)
    val latestDurationMs by rememberUpdatedState(durationMs)
    val latestOnToggleOverlay by rememberUpdatedState(onToggleOverlay)
    val latestOnPlayPause by rememberUpdatedState(onPlayPause)
    val latestOnSeekBy by rememberUpdatedState(onSeekBy)
    val latestOnSeekTo by rememberUpdatedState(onSeekTo)
    val latestBrightnessFraction by rememberUpdatedState(brightnessFraction)
    val latestVolumeFraction by rememberUpdatedState(volumeFraction)
    val latestOnBrightnessFraction by rememberUpdatedState(onBrightnessFraction)
    val latestOnVolumeFraction by rememberUpdatedState(onVolumeFraction)
    val latestOnHudText by rememberUpdatedState(onHudText)
    val latestOnSeekPreview by rememberUpdatedState(onSeekPreview)
    val latestSideGestureLayout by rememberUpdatedState(sideGestureLayout)
    val latestFastPlaybackHoldEnabled by rememberUpdatedState(fastPlaybackHoldEnabled)
    val latestOnFastPlaybackHoldStart by rememberUpdatedState(onFastPlaybackHoldStart)
    val latestOnFastPlaybackHoldEnd by rememberUpdatedState(onFastPlaybackHoldEnd)
    val latestPreviewFrameFor by rememberUpdatedState(previewFrameFor)
    val latestGestureExclusionBounds by rememberUpdatedState(gestureExclusionBounds)
    val latestGesturesSuspendedByModalSurface by rememberUpdatedState(gesturesSuspendedByModalSurface)
    val latestPresentationMode by rememberUpdatedState(presentationMode)
    val latestOnPresentationDragUpdate by rememberUpdatedState(onPresentationDragUpdate)
    val latestOnPresentationDragRelease by rememberUpdatedState(onPresentationDragRelease)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    if (!shouldPlayerGestureLayerHandlePointerStart(
                            x = firstDown.position.x,
                            y = firstDown.position.y,
                            gestureExclusionBounds = latestGestureExclusionBounds,
                            gesturesSuspendedByModalSurface = latestGesturesSuspendedByModalSurface,
                        )
                    ) {
                        awaitPointerUpWithoutPlayerHandling(firstDown)
                        return@awaitEachGesture
                    }
                    when (
                        val firstGesture = awaitTapOrHandleDrag(
                            down = firstDown,
                            isLocked = { latestLocked },
                            currentPositionMs = { latestCurrentPositionMs },
                            durationMs = { latestDurationMs },
                            onSeekTo = latestOnSeekTo,
                            brightnessFraction = latestBrightnessFraction,
                            volumeFraction = latestVolumeFraction,
                            onBrightnessFraction = latestOnBrightnessFraction,
                            onVolumeFraction = latestOnVolumeFraction,
                            onHudText = latestOnHudText,
                            onSeekPreview = latestOnSeekPreview,
                            sideGestureLayout = { latestSideGestureLayout },
                            fastPlaybackHoldEnabled = latestFastPlaybackHoldEnabled,
                            onFastPlaybackHoldStart = latestOnFastPlaybackHoldStart,
                            onFastPlaybackHoldEnd = latestOnFastPlaybackHoldEnd,
                            previewFrameFor = latestPreviewFrameFor,
                            presentationMode = { latestPresentationMode },
                            onPresentationDragUpdate = latestOnPresentationDragUpdate,
                            onPresentationDragRelease = latestOnPresentationDragRelease,
                        )
                    ) {
                        is PlayerPointerGestureResult.Tap -> {
                            val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis.toLong()) {
                                awaitFirstDown(requireUnconsumed = false)
                            }
                            if (secondDown == null) {
                                handlePlayerTap(
                                    locked = latestLocked,
                                    onToggleOverlay = latestOnToggleOverlay,
                                    onHudText = latestOnHudText,
                                )
                            } else if (!shouldPlayerGestureLayerHandlePointerStart(
                                    x = secondDown.position.x,
                                    y = secondDown.position.y,
                                    gestureExclusionBounds = latestGestureExclusionBounds,
                                    gesturesSuspendedByModalSurface = latestGesturesSuspendedByModalSurface,
                                )
                            ) {
                                awaitPointerUpWithoutPlayerHandling(secondDown)
                            } else {
                                when (
                                    val secondGesture = awaitTapOrHandleDrag(
                                        down = secondDown,
                                        isLocked = { latestLocked },
                                        currentPositionMs = { latestCurrentPositionMs },
                                        durationMs = { latestDurationMs },
                                        onSeekTo = latestOnSeekTo,
                                        brightnessFraction = latestBrightnessFraction,
                                        volumeFraction = latestVolumeFraction,
                                        onBrightnessFraction = latestOnBrightnessFraction,
                                        onVolumeFraction = latestOnVolumeFraction,
                                        onHudText = latestOnHudText,
                                        onSeekPreview = latestOnSeekPreview,
                                        sideGestureLayout = { latestSideGestureLayout },
                                        fastPlaybackHoldEnabled = latestFastPlaybackHoldEnabled,
                                        onFastPlaybackHoldStart = latestOnFastPlaybackHoldStart,
                                        onFastPlaybackHoldEnd = latestOnFastPlaybackHoldEnd,
                                        previewFrameFor = latestPreviewFrameFor,
                                        presentationMode = { latestPresentationMode },
                                        onPresentationDragUpdate = latestOnPresentationDragUpdate,
                                        onPresentationDragRelease = latestOnPresentationDragRelease,
                                    )
                                ) {
                                    is PlayerPointerGestureResult.Tap -> {
                                        handlePlayerDoubleTap(
                                            offset = secondGesture.position,
                                            width = size.width.toFloat(),
                                            locked = latestLocked,
                                            onPlayPause = latestOnPlayPause,
                                            onSeekBy = latestOnSeekBy,
                                            onHudText = latestOnHudText,
                                        )
                                    }
                                    PlayerPointerGestureResult.Handled -> Unit
                                }
                            }
                        }
                        PlayerPointerGestureResult.Handled -> Unit
                    }
                }
            },
    )
}

private suspend fun AwaitPointerEventScope.awaitPointerUpWithoutPlayerHandling(down: PointerInputChange) {
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == down.id }
            ?: event.changes.firstOrNull { it.pressed }
            ?: return
        if (!change.pressed) return
    }
}

private sealed class PlayerPointerGestureResult {
    data class Tap(val position: Offset) : PlayerPointerGestureResult()
    object Handled : PlayerPointerGestureResult()
}

private suspend fun AwaitPointerEventScope.awaitTapOrHandleDrag(
    down: PointerInputChange,
    isLocked: () -> Boolean,
    currentPositionMs: () -> Long,
    durationMs: () -> Long,
    onSeekTo: (Long) -> Unit,
    brightnessFraction: () -> Float,
    volumeFraction: () -> Float,
    onBrightnessFraction: (Float) -> String,
    onVolumeFraction: (Float) -> String,
    onHudText: (String?) -> Unit,
    onSeekPreview: (PlayerSeekPreview?) -> Unit,
    sideGestureLayout: () -> PlayerSideGestureLayout,
    fastPlaybackHoldEnabled: Boolean = true,
    onFastPlaybackHoldStart: () -> Unit,
    onFastPlaybackHoldEnd: () -> Unit,
    previewFrameFor: (Long) -> StashSpriteFrame?,
    presentationMode: () -> PlayerPresentationMode?,
    onPresentationDragUpdate: (PlayerPresentationDragUpdate?) -> Unit,
    onPresentationDragRelease: (PlayerPresentationDragRelease) -> Unit,
): PlayerPointerGestureResult {
    val dragStart = down.position
    var totalDrag = Offset.Zero
    var presentationSession: PlayerPresentationDragSession? = presentationMode()?.let { mode ->
        startPlayerPresentationDragSession(
            currentMode = mode,
            startArea = PlayerPresentationGestureStartArea.PlayerSurface,
            startXPx = dragStart.x,
            widthPx = size.width.toFloat(),
            containerHeightPx = size.height.toFloat(),
            uptimeMs = down.uptimeMillis,
        )
    }
    var dragMode = PlayerGestureMode.None
    var seekDragState = PlayerSeekDragState.Idle
    var sideControlLastTargetFraction = 0f
    var sideControlVelocityYPxPerSecond = 0f
    var sideControlAccelerationYPxPerSecondSquared = 0f
    var movedPastTapSlop = false
    var handledDrag = false
    var fastPlaybackHoldActive = false
    var longPressArmed = true
    var latestPointerUptimeMs = down.uptimeMillis

    fun finishFastPlaybackHoldIfNeeded() {
        if (fastPlaybackHoldActive) {
            fastPlaybackHoldActive = false
            onFastPlaybackHoldEnd()
        }
    }

    try {
        while (true) {
            val remainingLongPressTimeoutMs = remainingPlayerLongPressTimeoutMillis(
                downUptimeMs = down.uptimeMillis,
                latestPointerUptimeMs = latestPointerUptimeMs,
                longPressTimeoutMs = viewConfiguration.longPressTimeoutMillis,
            )
            val event = if (longPressArmed && !fastPlaybackHoldActive && !movedPastTapSlop) {
                if (remainingLongPressTimeoutMs > 0L) {
                    withTimeoutOrNull(remainingLongPressTimeoutMs) {
                        awaitPointerEvent()
                    }
                } else {
                    null
                }
            } else {
                awaitPointerEvent()
            }

            if (event == null) {
                longPressArmed = false
                when (resolvePlayerLongPressTimeoutAction(isLocked(), fastPlaybackHoldEnabled)) {
                    PlayerLongPressTimeoutAction.LockedHint -> {
                        onHudText(playerLockedTouchHint())
                        return PlayerPointerGestureResult.Handled
                    }
                    PlayerLongPressTimeoutAction.ConsumeOnly -> return PlayerPointerGestureResult.Handled
                    PlayerLongPressTimeoutAction.StartFastPlayback -> {
                        fastPlaybackHoldActive = true
                        onFastPlaybackHoldStart()
                        continue
                    }
                }
            }

            val change = event.changes.firstOrNull { it.id == down.id }
                ?: event.changes.firstOrNull { it.pressed }
                ?: run {
                    finishFastPlaybackHoldIfNeeded()
                    return PlayerPointerGestureResult.Handled
                }
            val previousPointerUptimeMs = latestPointerUptimeMs
            latestPointerUptimeMs = change.uptimeMillis

            if (!change.pressed) {
                val activePresentationSession = presentationSession?.takeIf { it.isPresentationActive }
                if (activePresentationSession != null) {
                    onPresentationDragRelease(activePresentationSession.release())
                    presentationSession = null
                    onSeekPreview(null)
                    return PlayerPointerGestureResult.Handled
                }
                if (fastPlaybackHoldActive) {
                    finishFastPlaybackHoldIfNeeded()
                    onSeekPreview(null)
                    return PlayerPointerGestureResult.Handled
                }
                if (handledDrag || movedPastTapSlop) {
                    if (!isLocked() && dragMode == PlayerGestureMode.Seek && seekDragState.active) {
                        onSeekTo(seekDragState.targetPositionMs)
                    }
                    onSeekPreview(null)
                    return PlayerPointerGestureResult.Handled
                }
                onSeekPreview(null)
                return PlayerPointerGestureResult.Tap(change.position)
            }

            val dragAmount = change.positionChange()
            if (dragAmount == Offset.Zero) continue
            val elapsedMs = (change.uptimeMillis - previousPointerUptimeMs).coerceAtLeast(1L)
            val pointerVelocityYPxPerSecond = (dragAmount.y / elapsedMs.toFloat()) * 1000f

            if (fastPlaybackHoldActive) {
                change.consume()
                continue
            }

            totalDrag += dragAmount
            presentationSession = presentationSession?.dragBy(
                deltaX = dragAmount.x,
                deltaY = dragAmount.y,
                uptimeMs = change.uptimeMillis,
                locked = isLocked(),
            )
            movedPastTapSlop = movedPastTapSlop ||
                hypot(totalDrag.x.toDouble(), totalDrag.y.toDouble()) > viewConfiguration.touchSlop.toDouble()
            if (movedPastTapSlop) {
                longPressArmed = false
            }

            if (isLocked()) {
                handledDrag = true
                onHudText(playerLockedTouchHint())
                change.consume()
                continue
            }

            val activePresentationSession = presentationSession?.takeIf { it.isPresentationActive }
            if (activePresentationSession != null) {
                handledDrag = true
                buildPlayerPresentationDragUpdate(activePresentationSession)?.let(onPresentationDragUpdate)
                onSeekPreview(null)
                change.consume()
                continue
            }
            if (presentationSession?.gestureMode == PlayerPresentationGestureMode.Locked) {
                handledDrag = true
                onHudText(playerLockedTouchHint())
                change.consume()
                continue
            }

            if (dragMode == PlayerGestureMode.None) {
                val playbackMode = classifyPlayerDrag(
                    startX = dragStart.x,
                    width = size.width.toFloat(),
                    totalDx = totalDrag.x,
                    totalDy = totalDrag.y,
                    sideGestureLayout = sideGestureLayout(),
                )
                val currentPresentationMode = presentationMode()
                dragMode = when {
                    playbackMode == PlayerGestureMode.None -> PlayerGestureMode.None
                    playbackMode == PlayerGestureMode.Seek -> playbackMode
                    currentPresentationMode == PlayerPresentationMode.Fullscreen -> playbackMode
                    else -> PlayerGestureMode.None
                }
                if (dragMode == PlayerGestureMode.Seek) {
                    seekDragState = PlayerSeekDragState.Idle.begin(
                        currentPositionMs = currentPositionMs(),
                        durationMs = durationMs(),
                        widthPx = size.width.toFloat(),
                    )
                } else if (dragMode == PlayerGestureMode.Brightness) {
                    sideControlLastTargetFraction = brightnessFraction()
                    sideControlVelocityYPxPerSecond = pointerVelocityYPxPerSecond
                    sideControlAccelerationYPxPerSecondSquared = 0f
                } else if (dragMode == PlayerGestureMode.Volume) {
                    sideControlLastTargetFraction = volumeFraction()
                    sideControlVelocityYPxPerSecond = pointerVelocityYPxPerSecond
                    sideControlAccelerationYPxPerSecondSquared = 0f
                }
            } else if (dragMode == PlayerGestureMode.Brightness || dragMode == PlayerGestureMode.Volume) {
                val previousVelocityYPxPerSecond = sideControlVelocityYPxPerSecond
                sideControlVelocityYPxPerSecond = pointerVelocityYPxPerSecond
                sideControlAccelerationYPxPerSecondSquared =
                    ((pointerVelocityYPxPerSecond - previousVelocityYPxPerSecond) / elapsedMs.toFloat()) * 1000f
            }

            when (dragMode) {
                PlayerGestureMode.Seek -> {
                    seekDragState = seekDragState.dragTo(totalDrag.x)
                    onSeekPreview(
                        PlayerSeekPreview(
                            deltaMs = seekDragState.deltaMs,
                            targetPositionMs = seekDragState.targetPositionMs,
                            durationMs = seekDragState.durationMs,
                            frame = previewFrameFor(seekDragState.targetPositionMs),
                        ),
                    )
                    onHudText(null)
                    handledDrag = true
                    change.consume()
                }
                PlayerGestureMode.Brightness -> {
                    val targetFraction = calculateVerticalSideControlGestureFraction(
                        previousFraction = sideControlLastTargetFraction,
                        deltaY = dragAmount.y,
                        heightPx = size.height.toFloat(),
                        velocityYPxPerSecond = sideControlVelocityYPxPerSecond,
                        accelerationYPxPerSecondSquared = sideControlAccelerationYPxPerSecondSquared,
                    )
                    sideControlLastTargetFraction = targetFraction
                    onHudText(
                        onBrightnessFraction(targetFraction),
                    )
                    handledDrag = true
                    change.consume()
                }
                PlayerGestureMode.Volume -> {
                    val targetFraction = calculateVerticalSideControlGestureFraction(
                        previousFraction = sideControlLastTargetFraction,
                        deltaY = dragAmount.y,
                        heightPx = size.height.toFloat(),
                        velocityYPxPerSecond = sideControlVelocityYPxPerSecond,
                        accelerationYPxPerSecondSquared = sideControlAccelerationYPxPerSecondSquared,
                    )
                    sideControlLastTargetFraction = targetFraction
                    onHudText(
                        onVolumeFraction(targetFraction),
                    )
                    handledDrag = true
                    change.consume()
                }
                PlayerGestureMode.None -> Unit
            }
        }
    } finally {
        presentationSession?.takeIf { it.isPresentationActive }?.let { session ->
            onPresentationDragRelease(session.release())
        }
        finishFastPlaybackHoldIfNeeded()
    }
}

private fun handlePlayerTap(
    locked: Boolean,
    onToggleOverlay: () -> Unit,
    onHudText: (String?) -> Unit,
) {
    if (locked) {
        onHudText(playerLockedTouchHint())
    } else {
        onToggleOverlay()
    }
}

private fun handlePlayerDoubleTap(
    offset: Offset,
    width: Float,
    locked: Boolean,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onHudText: (String?) -> Unit,
) {
    if (locked) {
        onHudText(playerLockedTouchHint())
        return
    }
    when (classifyDoubleTapRegion(offset.x, width)) {
        DoubleTapRegion.Rewind -> {
            onSeekBy(-10_000L)
            onHudText(stashString(R.string.auto_kr_0456))
        }
        DoubleTapRegion.Center -> {
            onPlayPause()
            onHudText(stashString(R.string.auto_kr_0457))
        }
        DoubleTapRegion.Forward -> {
            onSeekBy(10_000L)
            onHudText(stashString(R.string.auto_kr_0458))
        }
    }
}
