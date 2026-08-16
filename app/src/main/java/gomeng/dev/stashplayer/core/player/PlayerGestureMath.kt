package gomeng.dev.stashplayer.core.player

import kotlin.math.abs
import kotlin.math.roundToLong
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val DRAG_DEAD_ZONE_PX = 24f
private val AXIS_LOCK_RATIO = 1.35f
private val MIN_SEEK_WINDOW_MS = 120_000L
private val MAX_SEEK_WINDOW_MS = 20 * 60 * 1000L
private val SEEK_WINDOW_DURATION_DIVISOR = 8L
private val PLAYER_VERTICAL_SIDE_CONTROL_FRACTION = 0.30f
private val PLAYER_VERTICAL_SIDE_CONTROL_VELOCITY_PROJECTION_SECONDS = 0.08f
private val PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_PROJECTION_SECONDS = 0.035f
private val PLAYER_VERTICAL_SIDE_CONTROL_VELOCITY_BOOST_LIMIT = 0.18f
private val PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_BOOST_LIMIT = 0.10f
private val PLAYER_VERTICAL_SIDE_CONTROL_TOTAL_BOOST_LIMIT = 0.22f
private val PLAYER_VERTICAL_SIDE_CONTROL_SPEED_GAIN_REFERENCE_PX_PER_SECOND = 2_400f
private val PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_GAIN_REFERENCE_PX_PER_SECOND_SQUARED = 900_000f
private val PLAYER_VERTICAL_SIDE_CONTROL_SPEED_GAIN_LIMIT = 0.45f
private val PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_GAIN_LIMIT = 0.25f
private val PRESENTATION_SETTLE_DISTANCE_FRACTION = 0.18f
private val PRESENTATION_SETTLE_VELOCITY_PX_PER_SECOND = 1_400f
private val PRESENTATION_MOTION_FULL_DRAG_FRACTION = 0.30f
private val PRESENTATION_WATCH_PAGE_FADE_OUT_END_PROGRESS = 0.70f
private val PRESENTATION_FULLSCREEN_CHROME_FADE_IN_START_PROGRESS = 0.25f
private val PRESENTATION_SYSTEM_BAR_HIDE_PROGRESS = 0.92f
private val PRESENTATION_WATCH_PAGE_VIDEO_SCALE = 0.94f
private val PRESENTATION_WATCH_PAGE_VIDEO_TRANSLATION_Y_PX = 18f
private val PRESENTATION_FULLSCREEN_CHROME_TRANSLATION_Y_PX = 28f
private val PRESENTATION_ENTER_VIDEO_OVERSHOOT_START_PROGRESS = 0.40f
private val PRESENTATION_ENTER_VIDEO_OVERSHOOT_PEAK_PROGRESS = 0.65f
private val PRESENTATION_ENTER_VIDEO_OVERSHOOT_SCALE = 0.035f

enum class PlayerGestureMode {
    None,
    Seek,
    Brightness,
    Volume,
}

enum class DoubleTapRegion {
    Rewind,
    Center,
    Forward,
}

enum class AspectRatioMode {
    Fit,
    Crop,
    Stretch;

    fun next(): AspectRatioMode = when (this) {
        Fit -> Stretch
        Stretch -> Crop
        Crop -> Fit
    }
}

enum class PlayerPresentationMode {
    WatchPage,
    Fullscreen,
}

enum class PlayerPresentationGestureStartArea {
    PlayerSurface,
    WatchPageContent,
}

enum class PlayerPresentationGestureMode {
    None,
    EnterFullscreen,
    ExitFullscreen,
    ReservedPlaybackGesture,
    Locked,
}

data class PlayerWatchPageContentPullTransition(
    val gestureMode: PlayerPresentationGestureMode,
    val transitionProgress: Float,
    val consumeDrag: Boolean,
)

data class PlayerPresentationDragSession(
    val currentMode: PlayerPresentationMode,
    val gestureMode: PlayerPresentationGestureMode,
    val startArea: PlayerPresentationGestureStartArea,
    val startXPx: Float,
    val widthPx: Float,
    val containerHeightPx: Float,
    val totalDx: Float,
    val totalDy: Float,
    val latestUptimeMs: Long,
    val latestTotalDy: Float,
    val velocityYPxPerSecond: Float,
    val progress: Float,
) {
    val isPresentationActive: Boolean
        get() = gestureMode == PlayerPresentationGestureMode.EnterFullscreen ||
            gestureMode == PlayerPresentationGestureMode.ExitFullscreen
}

data class PlayerPresentationDragUpdate(
    val session: PlayerPresentationDragSession,
) {
    val progress: Float get() = session.progress
    val gestureMode: PlayerPresentationGestureMode get() = session.gestureMode
    val startArea: PlayerPresentationGestureStartArea get() = session.startArea
    val totalDragYPx: Float get() = session.totalDy
    val velocityYPxPerSecond: Float get() = session.velocityYPxPerSecond
    val containerHeightPx: Float get() = session.containerHeightPx
}

data class PlayerPresentationDragRelease(
    val progress: Float,
    val targetMode: PlayerPresentationMode,
    val gestureMode: PlayerPresentationGestureMode,
    val startArea: PlayerPresentationGestureStartArea,
)

data class PlayerGestureExclusionBounds(
    val leftPx: Float,
    val topPx: Float,
    val rightPx: Float,
    val bottomPx: Float,
) {
    fun contains(x: Float, y: Float): Boolean {
        if (rightPx <= leftPx || bottomPx <= topPx) return false
        return x >= leftPx && x <= rightPx && y >= topPx && y <= bottomPx
    }

    fun translatedBy(translationYPx: Float): PlayerGestureExclusionBounds = copy(
        topPx = topPx + translationYPx,
        bottomPx = bottomPx + translationYPx,
    )
}

data class PlayerPresentationMotionState(
    val transitionProgress: Float,
    val watchPageContentAlpha: Float,
    val watchPageContentHeightFraction: Float,
    val watchPageContentTranslationYPx: Float,
    val fullscreenChromeAlpha: Float,
    val fullscreenChromeTranslationYPx: Float,
    val videoScale: Float,
    val videoTranslationYPx: Float,
    val hideSystemBars: Boolean,
    val renderWatchPageContent: Boolean,
)

val PLAYER_FAST_PLAYBACK_HOLD_SPEED = FastPlaybackHoldSpeedPreference.default.playbackSpeed ?: 1.5f

fun playerFastPlaybackHoldHudText(playbackSpeed: Float): String =
    stashString(R.string.player_fast_playback_hold_hud_text, playerPlaybackSpeedLabel(playbackSpeed))

data class PlayerFastPlaybackHoldUpdate(
    val state: PlayerFastPlaybackHoldState,
    val playbackSpeed: Float?,
    val hudText: String?,
)

enum class PlayerLongPressTimeoutAction {
    StartFastPlayback,
    ConsumeOnly,
    LockedHint,
}

fun resolvePlayerLongPressTimeoutAction(
    locked: Boolean,
    fastPlaybackHoldEnabled: Boolean,
): PlayerLongPressTimeoutAction = when {
    locked -> PlayerLongPressTimeoutAction.LockedHint
    fastPlaybackHoldEnabled -> PlayerLongPressTimeoutAction.StartFastPlayback
    else -> PlayerLongPressTimeoutAction.ConsumeOnly
}

data class PlayerFastPlaybackHoldState(
    val active: Boolean,
    private val restoreSpeed: Float?,
) {
    fun start(
        currentSpeed: Float,
        locked: Boolean,
        speedPreference: FastPlaybackHoldSpeedPreference = FastPlaybackHoldSpeedPreference.default,
    ): PlayerFastPlaybackHoldUpdate {
        if (locked) {
            return PlayerFastPlaybackHoldUpdate(
                state = Idle,
                playbackSpeed = null,
                hudText = playerLockedTouchHint(),
            )
        }
        val speedMultiplier = speedPreference.playbackSpeed ?: return PlayerFastPlaybackHoldUpdate(
            state = Idle,
            playbackSpeed = null,
            hudText = null,
        )
        val speed = currentSpeed * speedMultiplier
        return PlayerFastPlaybackHoldUpdate(
            state = PlayerFastPlaybackHoldState(
                active = true,
                restoreSpeed = currentSpeed,
            ),
            playbackSpeed = speed,
            hudText = playerFastPlaybackHoldHudText(speed),
        )
    }

    fun release(): PlayerFastPlaybackHoldUpdate = finish()

    fun cancel(): PlayerFastPlaybackHoldUpdate = finish()

    private fun finish(): PlayerFastPlaybackHoldUpdate = PlayerFastPlaybackHoldUpdate(
        state = Idle,
        playbackSpeed = restoreSpeed.takeIf { active },
        hudText = null,
    )

    companion object {
        val Idle = PlayerFastPlaybackHoldState(active = false, restoreSpeed = null)
    }
}

fun remainingPlayerLongPressTimeoutMillis(
    downUptimeMs: Long,
    latestPointerUptimeMs: Long,
    longPressTimeoutMs: Long,
): Long {
    val deadlineMs = downUptimeMs + longPressTimeoutMs.coerceAtLeast(0L)
    return (deadlineMs - latestPointerUptimeMs).coerceAtLeast(0L)
}

fun classifyPlayerDrag(
    startX: Float,
    width: Float,
    totalDx: Float,
    totalDy: Float,
    gestureExcluded: Boolean = false,
    sideGestureLayout: PlayerSideGestureLayout = PlayerSideGestureLayout.default,
): PlayerGestureMode {
    if (gestureExcluded) return PlayerGestureMode.None

    val absDx = abs(totalDx)
    val absDy = abs(totalDy)
    if (maxOf(absDx, absDy) < DRAG_DEAD_ZONE_PX) return PlayerGestureMode.None

    return when {
        absDx >= absDy * AXIS_LOCK_RATIO -> PlayerGestureMode.Seek
        absDy >= absDx * AXIS_LOCK_RATIO -> resolvePlayerSideGestureMode(startX, width, sideGestureLayout)
        else -> PlayerGestureMode.None
    }
}

fun calculateVerticalSideControlTargetFraction(
    startFraction: Float,
    totalDy: Float,
    heightPx: Float,
    velocityYPxPerSecond: Float = 0f,
    accelerationYPxPerSecondSquared: Float = 0f,
): Float {
    val height = heightPx.takeIf { it > 0f } ?: 1f
    val distanceFraction = -(totalDy / height)
    val dragDirection = when {
        distanceFraction > 0f -> 1f
        distanceFraction < 0f -> -1f
        else -> 0f
    }
    val velocityBoost = (-(velocityYPxPerSecond * PLAYER_VERTICAL_SIDE_CONTROL_VELOCITY_PROJECTION_SECONDS) / height)
        .coerceIn(-PLAYER_VERTICAL_SIDE_CONTROL_VELOCITY_BOOST_LIMIT, PLAYER_VERTICAL_SIDE_CONTROL_VELOCITY_BOOST_LIMIT)
    val accelerationBoost = (
        -(
            0.5f *
                accelerationYPxPerSecondSquared *
                PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_PROJECTION_SECONDS *
                PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_PROJECTION_SECONDS
            ) / height
        ).coerceIn(
        -PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_BOOST_LIMIT,
        PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_BOOST_LIMIT,
    )
    val sameDirectionMomentumBoost = (velocityBoost + accelerationBoost)
        .coerceIn(
            -PLAYER_VERTICAL_SIDE_CONTROL_TOTAL_BOOST_LIMIT,
            PLAYER_VERTICAL_SIDE_CONTROL_TOTAL_BOOST_LIMIT,
        )
        .let { boost ->
            when {
                dragDirection > 0f -> boost.coerceAtLeast(0f)
                dragDirection < 0f -> boost.coerceAtMost(0f)
                else -> 0f
            }
        }
    return (startFraction + distanceFraction + sameDirectionMomentumBoost).coerceIn(0f, 1f)
}

fun resolveVerticalSideControlGestureFraction(
    rawFraction: Float,
    previousFraction: Float,
    deltaY: Float,
): Float = when {
    deltaY < 0f -> rawFraction.coerceAtLeast(previousFraction)
    deltaY > 0f -> rawFraction.coerceAtMost(previousFraction)
    else -> rawFraction
}.coerceIn(0f, 1f)

fun calculateVerticalSideControlGestureFraction(
    previousFraction: Float,
    deltaY: Float,
    heightPx: Float,
    velocityYPxPerSecond: Float = 0f,
    accelerationYPxPerSecondSquared: Float = 0f,
): Float {
    val height = heightPx.takeIf { it > 0f } ?: 1f
    val baseDelta = -(deltaY / height)
    if (baseDelta == 0f) return previousFraction.coerceIn(0f, 1f)
    val sameDirectionSpeed = if (baseDelta.isSameDirectionAs(-velocityYPxPerSecond)) {
        abs(velocityYPxPerSecond)
    } else {
        0f
    }
    val sameDirectionAcceleration = if (baseDelta.isSameDirectionAs(-accelerationYPxPerSecondSquared)) {
        abs(accelerationYPxPerSecondSquared)
    } else {
        0f
    }
    val speedGain = (sameDirectionSpeed / PLAYER_VERTICAL_SIDE_CONTROL_SPEED_GAIN_REFERENCE_PX_PER_SECOND)
        .coerceIn(0f, PLAYER_VERTICAL_SIDE_CONTROL_SPEED_GAIN_LIMIT)
    val accelerationGain =
        (sameDirectionAcceleration / PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_GAIN_REFERENCE_PX_PER_SECOND_SQUARED)
            .coerceIn(0f, PLAYER_VERTICAL_SIDE_CONTROL_ACCELERATION_GAIN_LIMIT)
    return (previousFraction + (baseDelta * (1f + speedGain + accelerationGain))).coerceIn(0f, 1f)
}

fun shouldAllowWatchPagePullToFullscreen(
    contentScrolledToTop: Boolean,
    locked: Boolean,
): Boolean = contentScrolledToTop && !locked

fun buildPlayerPresentationMotionState(
    transitionProgress: Float,
    maxWatchPageContentTranslationYPx: Float = 48f,
    presentationGestureMode: PlayerPresentationGestureMode = PlayerPresentationGestureMode.None,
    dragUpdate: PlayerPresentationDragUpdate? = null,
): PlayerPresentationMotionState {
    val progress = resolvePlayerPresentationDisplayProgress(
        animationProgress = transitionProgress,
        dragProgress = dragUpdate?.progress,
    )
    val directMotionActive = dragUpdate != null ||
        presentationGestureMode == PlayerPresentationGestureMode.ExitFullscreen
    val watchPageContentAlpha = resolveWatchPageContentAlpha(
        progress = progress,
        interactiveDragActive = directMotionActive,
    )
    val fullscreenChromeAlpha = resolveFullscreenChromeAlpha(
        progress = progress,
        interactiveDragActive = directMotionActive,
    )
    val baseVideoScale = PRESENTATION_WATCH_PAGE_VIDEO_SCALE + ((1f - PRESENTATION_WATCH_PAGE_VIDEO_SCALE) * progress)
    val enterVideoOvershootScale = if (shouldApplyEnterFullscreenVideoOvershoot(presentationGestureMode, dragUpdate)) {
        calculateEnterFullscreenVideoOvershootScale(
            progress = progress,
            presentationGestureMode = presentationGestureMode,
        )
    } else {
        0f
    }
    return PlayerPresentationMotionState(
        transitionProgress = progress,
        watchPageContentAlpha = watchPageContentAlpha,
        watchPageContentHeightFraction = resolveWatchPagePanelHeightFraction(progress),
        watchPageContentTranslationYPx = resolveWatchPagePanelTranslationYPx(
            progress = progress,
            dragUpdate = dragUpdate,
            maxTranslationYPx = maxWatchPageContentTranslationYPx,
        ),
        fullscreenChromeAlpha = fullscreenChromeAlpha,
        fullscreenChromeTranslationYPx = PRESENTATION_FULLSCREEN_CHROME_TRANSLATION_Y_PX * (1f - fullscreenChromeAlpha),
        videoScale = baseVideoScale + enterVideoOvershootScale,
        videoTranslationYPx = PRESENTATION_WATCH_PAGE_VIDEO_TRANSLATION_Y_PX * (1f - progress),
        hideSystemBars = dragUpdate == null && progress >= PRESENTATION_SYSTEM_BAR_HIDE_PROGRESS,
        renderWatchPageContent = watchPageContentAlpha > 0.01f,
    )
}

private fun shouldApplyEnterFullscreenVideoOvershoot(
    presentationGestureMode: PlayerPresentationGestureMode,
    dragUpdate: PlayerPresentationDragUpdate?,
): Boolean {
    if (presentationGestureMode != PlayerPresentationGestureMode.EnterFullscreen) return false
    return dragUpdate == null ||
        (
            dragUpdate.gestureMode == PlayerPresentationGestureMode.EnterFullscreen &&
                dragUpdate.startArea == PlayerPresentationGestureStartArea.PlayerSurface
            )
}

private fun calculateEnterFullscreenVideoOvershootScale(
    progress: Float,
    presentationGestureMode: PlayerPresentationGestureMode,
): Float {
    if (presentationGestureMode != PlayerPresentationGestureMode.EnterFullscreen) return 0f
    if (progress <= PRESENTATION_ENTER_VIDEO_OVERSHOOT_START_PROGRESS || progress >= 1f) return 0f
    val overshootFraction = if (progress <= PRESENTATION_ENTER_VIDEO_OVERSHOOT_PEAK_PROGRESS) {
        ((progress - PRESENTATION_ENTER_VIDEO_OVERSHOOT_START_PROGRESS) /
            (PRESENTATION_ENTER_VIDEO_OVERSHOOT_PEAK_PROGRESS - PRESENTATION_ENTER_VIDEO_OVERSHOOT_START_PROGRESS))
    } else {
        ((1f - progress) / (1f - PRESENTATION_ENTER_VIDEO_OVERSHOOT_PEAK_PROGRESS))
    }.coerceIn(0f, 1f)
    return PRESENTATION_ENTER_VIDEO_OVERSHOOT_SCALE * overshootFraction
}

fun resolvePlayerPresentationOverlayAlpha(
    motionState: PlayerPresentationMotionState,
    fullscreenPlayerActive: Boolean,
    presentationDragActive: Boolean,
): Float = if (fullscreenPlayerActive || presentationDragActive) {
    motionState.fullscreenChromeAlpha
} else {
    1f
}

fun resolvePlayerPresentationDisplayProgress(
    animationProgress: Float,
    dragProgress: Float?,
): Float = (dragProgress ?: animationProgress).coerceIn(0f, 1f)

fun resolveWatchPageContentAlpha(
    progress: Float,
    interactiveDragActive: Boolean,
): Float {
    val boundedProgress = progress.coerceIn(0f, 1f)
    return if (interactiveDragActive) {
        1f - boundedProgress
    } else {
        ((PRESENTATION_WATCH_PAGE_FADE_OUT_END_PROGRESS - boundedProgress) /
            PRESENTATION_WATCH_PAGE_FADE_OUT_END_PROGRESS).coerceIn(0f, 1f)
    }
}

fun resolveFullscreenChromeAlpha(
    progress: Float,
    interactiveDragActive: Boolean,
): Float {
    val boundedProgress = progress.coerceIn(0f, 1f)
    return if (interactiveDragActive) {
        boundedProgress
    } else {
        ((boundedProgress - PRESENTATION_FULLSCREEN_CHROME_FADE_IN_START_PROGRESS) /
            (1f - PRESENTATION_FULLSCREEN_CHROME_FADE_IN_START_PROGRESS)).coerceIn(0f, 1f)
    }
}

fun resolveWatchPagePanelHeightFraction(progress: Float): Float = (1f - progress).coerceIn(0f, 1f)

fun resolveWatchPagePanelTranslationYPx(
    progress: Float,
    dragUpdate: PlayerPresentationDragUpdate?,
    maxTranslationYPx: Float,
): Float {
    val maxTranslation = maxTranslationYPx.coerceAtLeast(0f)
    return when {
        dragUpdate?.startArea == PlayerPresentationGestureStartArea.WatchPageContent ->
            dragUpdate.totalDragYPx.coerceIn(0f, maxTranslation)
        dragUpdate?.startArea == PlayerPresentationGestureStartArea.PlayerSurface ->
            maxTranslation * progress.coerceIn(0f, 1f)
        else -> maxTranslation * progress.coerceIn(0f, 1f)
    }
}

fun buildPlayerPresentationDragUpdate(
    session: PlayerPresentationDragSession,
): PlayerPresentationDragUpdate? = session
    .takeIf { it.isPresentationActive }
    ?.let(::PlayerPresentationDragUpdate)

fun startPlayerPresentationDragSession(
    currentMode: PlayerPresentationMode,
    startArea: PlayerPresentationGestureStartArea,
    startXPx: Float,
    widthPx: Float,
    containerHeightPx: Float,
    uptimeMs: Long,
): PlayerPresentationDragSession =
    PlayerPresentationDragSession(
        currentMode = currentMode,
        gestureMode = PlayerPresentationGestureMode.None,
        startArea = startArea,
        startXPx = startXPx,
        widthPx = widthPx,
        containerHeightPx = containerHeightPx,
        totalDx = 0f,
        totalDy = 0f,
        latestUptimeMs = uptimeMs,
        latestTotalDy = 0f,
        velocityYPxPerSecond = 0f,
        progress = when (currentMode) {
            PlayerPresentationMode.WatchPage -> 0f
            PlayerPresentationMode.Fullscreen -> 1f
        },
    )

fun PlayerPresentationDragSession.dragBy(
    deltaX: Float,
    deltaY: Float,
    uptimeMs: Long,
    locked: Boolean = false,
    contentScrolledToTop: Boolean = true,
): PlayerPresentationDragSession {
    val rawTotalDx = totalDx + deltaX
    val rawTotalDy = totalDy + deltaY
    if (
        gestureMode == PlayerPresentationGestureMode.None &&
        startArea == PlayerPresentationGestureStartArea.WatchPageContent &&
        !contentScrolledToTop &&
        rawTotalDy > 0f
    ) {
        return copy(
            totalDx = 0f,
            totalDy = 0f,
            latestUptimeMs = uptimeMs,
            latestTotalDy = 0f,
            velocityYPxPerSecond = 0f,
            progress = 0f,
        )
    }
    val elapsedMs = (uptimeMs - latestUptimeMs).coerceAtLeast(1L)
    val nextGestureMode = if (gestureMode == PlayerPresentationGestureMode.None) {
        classifyPlayerPresentationDrag(
            presentationMode = currentMode,
            startArea = startArea,
            startX = startXPx,
            width = widthPx,
            totalDx = rawTotalDx,
            totalDy = rawTotalDy,
            locked = locked,
            contentScrolledToTop = contentScrolledToTop,
        )
    } else {
        gestureMode
    }
    return copy(
        gestureMode = nextGestureMode,
        totalDx = rawTotalDx,
        totalDy = rawTotalDy,
        latestUptimeMs = uptimeMs,
        latestTotalDy = rawTotalDy,
        velocityYPxPerSecond = ((rawTotalDy - latestTotalDy) / elapsedMs.toFloat()) * 1000f,
        progress = calculatePlayerPresentationTransitionProgress(
            currentMode = currentMode,
            gestureMode = nextGestureMode,
            totalDy = rawTotalDy,
            containerHeightPx = containerHeightPx,
        ),
    )
}

fun PlayerPresentationDragSession.release(): PlayerPresentationDragRelease =
    PlayerPresentationDragRelease(
        progress = progress,
        targetMode = settlePlayerPresentationDrag(
            currentMode = currentMode,
            gestureMode = gestureMode,
            totalDy = totalDy,
            containerHeightPx = containerHeightPx,
            velocityYPxPerSecond = velocityYPxPerSecond,
        ),
        gestureMode = gestureMode,
        startArea = startArea,
    )

fun calculatePlayerPresentationTransitionProgress(
    currentMode: PlayerPresentationMode,
    gestureMode: PlayerPresentationGestureMode,
    totalDy: Float,
    containerHeightPx: Float,
): Float {
    if (containerHeightPx <= 0f) {
        return when (currentMode) {
            PlayerPresentationMode.WatchPage -> 0f
            PlayerPresentationMode.Fullscreen -> 1f
        }
    }
    val fullDragDistancePx = (containerHeightPx * PRESENTATION_MOTION_FULL_DRAG_FRACTION).coerceAtLeast(1f)
    return when (gestureMode) {
        PlayerPresentationGestureMode.EnterFullscreen -> (abs(totalDy) / fullDragDistancePx).coerceIn(0f, 1f)
        PlayerPresentationGestureMode.ExitFullscreen -> (1f - (abs(totalDy) / fullDragDistancePx)).coerceIn(0f, 1f)
        PlayerPresentationGestureMode.None,
        PlayerPresentationGestureMode.ReservedPlaybackGesture,
        PlayerPresentationGestureMode.Locked -> when (currentMode) {
            PlayerPresentationMode.WatchPage -> 0f
            PlayerPresentationMode.Fullscreen -> 1f
        }
    }
}

fun classifyPlayerPresentationDrag(
    presentationMode: PlayerPresentationMode,
    startArea: PlayerPresentationGestureStartArea,
    startX: Float,
    width: Float,
    totalDx: Float,
    totalDy: Float,
    locked: Boolean = false,
    contentScrolledToTop: Boolean = true,
): PlayerPresentationGestureMode {
    if (locked) return PlayerPresentationGestureMode.Locked

    val absDx = abs(totalDx)
    val absDy = abs(totalDy)
    if (maxOf(absDx, absDy) < DRAG_DEAD_ZONE_PX) return PlayerPresentationGestureMode.None
    if (absDx >= absDy * AXIS_LOCK_RATIO) return PlayerPresentationGestureMode.ReservedPlaybackGesture
    if (absDy < absDx * AXIS_LOCK_RATIO) return PlayerPresentationGestureMode.None

    return when (presentationMode) {
        PlayerPresentationMode.WatchPage -> when (startArea) {
            PlayerPresentationGestureStartArea.PlayerSurface -> if (totalDy != 0f) {
                PlayerPresentationGestureMode.EnterFullscreen
            } else {
                PlayerPresentationGestureMode.None
            }

            PlayerPresentationGestureStartArea.WatchPageContent -> if (
                totalDy > 0f && shouldAllowWatchPagePullToFullscreen(
                    contentScrolledToTop = contentScrolledToTop,
                    locked = locked,
                )
            ) {
                PlayerPresentationGestureMode.EnterFullscreen
            } else {
                PlayerPresentationGestureMode.None
            }
        }

        PlayerPresentationMode.Fullscreen -> if (isPlayerSideControlGestureStart(startX, width)) {
            PlayerPresentationGestureMode.ReservedPlaybackGesture
        } else if (totalDy != 0f) {
            PlayerPresentationGestureMode.ExitFullscreen
        } else {
            PlayerPresentationGestureMode.None
        }
    }
}

fun resolveWatchPageContentPullTransition(
    currentGestureMode: PlayerPresentationGestureMode,
    startX: Float,
    width: Float,
    totalDx: Float,
    totalDy: Float,
    containerHeightPx: Float,
    locked: Boolean,
    contentScrolledToTop: Boolean,
): PlayerWatchPageContentPullTransition {
    val gestureMode = if (currentGestureMode == PlayerPresentationGestureMode.None) {
        classifyPlayerPresentationDrag(
            presentationMode = PlayerPresentationMode.WatchPage,
            startArea = PlayerPresentationGestureStartArea.WatchPageContent,
            startX = startX,
            width = width,
            totalDx = totalDx,
            totalDy = totalDy,
            locked = locked,
            contentScrolledToTop = contentScrolledToTop,
        )
    } else {
        currentGestureMode
    }
    val transitionProgress = if (gestureMode == PlayerPresentationGestureMode.EnterFullscreen) {
        calculatePlayerPresentationTransitionProgress(
            currentMode = PlayerPresentationMode.WatchPage,
            gestureMode = gestureMode,
            totalDy = totalDy,
            containerHeightPx = containerHeightPx,
        )
    } else {
        0f
    }
    return PlayerWatchPageContentPullTransition(
        gestureMode = gestureMode,
        transitionProgress = transitionProgress,
        consumeDrag = gestureMode == PlayerPresentationGestureMode.EnterFullscreen ||
            gestureMode == PlayerPresentationGestureMode.Locked,
    )
}

fun shouldResetWatchPageContentPullBaseline(
    gestureMode: PlayerPresentationGestureMode,
    contentScrolledToTop: Boolean,
    totalDy: Float,
): Boolean = gestureMode == PlayerPresentationGestureMode.None &&
    !contentScrolledToTop &&
    totalDy > 0f

sealed class PlayerSurfaceDragDecision {
    object None : PlayerSurfaceDragDecision()
    data class Playback(val mode: PlayerGestureMode) : PlayerSurfaceDragDecision()
    data class Presentation(val mode: PlayerPresentationGestureMode) : PlayerSurfaceDragDecision()
}

fun resolvePlayerSurfaceDragDecision(
    presentationMode: PlayerPresentationMode,
    startX: Float,
    width: Float,
    totalDx: Float,
    totalDy: Float,
    locked: Boolean = false,
    sideGestureLayout: PlayerSideGestureLayout = PlayerSideGestureLayout.default,
): PlayerSurfaceDragDecision {
    val presentationGestureMode = classifyPlayerPresentationDrag(
        presentationMode = presentationMode,
        startArea = PlayerPresentationGestureStartArea.PlayerSurface,
        startX = startX,
        width = width,
        totalDx = totalDx,
        totalDy = totalDy,
        locked = locked,
    )
    return when (presentationGestureMode) {
        PlayerPresentationGestureMode.EnterFullscreen,
        PlayerPresentationGestureMode.ExitFullscreen,
        PlayerPresentationGestureMode.Locked -> PlayerSurfaceDragDecision.Presentation(presentationGestureMode)
        PlayerPresentationGestureMode.ReservedPlaybackGesture -> {
            val playbackMode = classifyPlayerDrag(
                startX = startX,
                width = width,
                totalDx = totalDx,
                totalDy = totalDy,
                sideGestureLayout = sideGestureLayout,
            )
            if (playbackMode == PlayerGestureMode.None) {
                PlayerSurfaceDragDecision.None
            } else {
                PlayerSurfaceDragDecision.Playback(playbackMode)
            }
        }
        PlayerPresentationGestureMode.None -> {
            val playbackMode = classifyPlayerDrag(
                startX = startX,
                width = width,
                totalDx = totalDx,
                totalDy = totalDy,
                sideGestureLayout = sideGestureLayout,
            )
            when {
                playbackMode == PlayerGestureMode.None -> PlayerSurfaceDragDecision.None
                playbackMode == PlayerGestureMode.Seek -> PlayerSurfaceDragDecision.Playback(playbackMode)
                presentationMode == PlayerPresentationMode.Fullscreen -> PlayerSurfaceDragDecision.Playback(playbackMode)
                else -> PlayerSurfaceDragDecision.None
            }
        }
    }
}

fun settlePlayerPresentationDrag(
    currentMode: PlayerPresentationMode,
    gestureMode: PlayerPresentationGestureMode,
    totalDy: Float,
    containerHeightPx: Float,
    velocityYPxPerSecond: Float,
): PlayerPresentationMode {
    if (containerHeightPx <= 0f) return currentMode
    val distanceThresholdPx = containerHeightPx * PRESENTATION_SETTLE_DISTANCE_FRACTION
    val enterFullscreen = abs(totalDy) >= distanceThresholdPx ||
        abs(velocityYPxPerSecond) >= PRESENTATION_SETTLE_VELOCITY_PX_PER_SECOND
    val exitFullscreen = abs(totalDy) >= distanceThresholdPx ||
        abs(velocityYPxPerSecond) >= PRESENTATION_SETTLE_VELOCITY_PX_PER_SECOND
    return when {
        gestureMode == PlayerPresentationGestureMode.EnterFullscreen && enterFullscreen -> PlayerPresentationMode.Fullscreen
        gestureMode == PlayerPresentationGestureMode.ExitFullscreen && exitFullscreen -> PlayerPresentationMode.WatchPage
        else -> currentMode
    }
}

private fun isPlayerBrightnessGestureStart(startX: Float, width: Float): Boolean {
    if (width <= 0f) return false
    return startX <= width * PLAYER_VERTICAL_SIDE_CONTROL_FRACTION
}

private fun isPlayerVolumeGestureStart(startX: Float, width: Float): Boolean {
    if (width <= 0f) return false
    return startX >= width * (1f - PLAYER_VERTICAL_SIDE_CONTROL_FRACTION)
}

private fun resolvePlayerSideGestureMode(
    startX: Float,
    width: Float,
    sideGestureLayout: PlayerSideGestureLayout,
): PlayerGestureMode = when {
    isPlayerBrightnessGestureStart(startX, width) -> when (sideGestureLayout) {
        PlayerSideGestureLayout.Default -> PlayerGestureMode.Brightness
        PlayerSideGestureLayout.Reversed -> PlayerGestureMode.Volume
    }
    isPlayerVolumeGestureStart(startX, width) -> when (sideGestureLayout) {
        PlayerSideGestureLayout.Default -> PlayerGestureMode.Volume
        PlayerSideGestureLayout.Reversed -> PlayerGestureMode.Brightness
    }
    else -> PlayerGestureMode.None
}

private fun isPlayerSideControlGestureStart(startX: Float, width: Float): Boolean =
    isPlayerBrightnessGestureStart(startX, width) || isPlayerVolumeGestureStart(startX, width)

private fun Float.isSameDirectionAs(other: Float): Boolean = (this > 0f && other > 0f) || (this < 0f && other < 0f)

fun shouldPlayerGestureLayerHandlePointerStart(
    x: Float,
    y: Float,
    gestureExclusionBounds: PlayerGestureExclusionBounds?,
    gesturesSuspendedByModalSurface: Boolean,
    watchPageScrollSurfaceActive: Boolean = false,
): Boolean {
    if (gesturesSuspendedByModalSurface) return false
    if (watchPageScrollSurfaceActive) return false
    return gestureExclusionBounds?.contains(x, y) != true
}

fun calculateSeekDeltaMs(
    totalDx: Float,
    width: Float,
    durationMs: Long,
): Long {
    if (width <= 0f || durationMs <= 0L) return 0L
    val seekWindowMs = calculateSeekWindowMs(durationMs)
    return ((totalDx / width) * seekWindowMs).roundToLong()
}

fun calculateSeekWindowMs(durationMs: Long): Long {
    if (durationMs <= 0L) return 0L
    val scaledWindowMs = durationMs / SEEK_WINDOW_DURATION_DIVISOR
    val boundedWindowMs = scaledWindowMs.coerceIn(MIN_SEEK_WINDOW_MS, MAX_SEEK_WINDOW_MS)
    return minOf(durationMs, boundedWindowMs)
}

fun calculateSeekTargetMs(
    startPositionMs: Long,
    totalDx: Float,
    width: Float,
    durationMs: Long,
): Long {
    val deltaMs = calculateSeekDeltaMs(
        totalDx = totalDx,
        width = width,
        durationMs = durationMs,
    )
    return coerceSeekRequestPosition(startPositionMs + deltaMs, durationMs)
}

fun classifyDoubleTapRegion(x: Float, width: Float): DoubleTapRegion {
    if (width <= 0f) return DoubleTapRegion.Center
    return when {
        x < width / 3f -> DoubleTapRegion.Rewind
        x > width * 2f / 3f -> DoubleTapRegion.Forward
        else -> DoubleTapRegion.Center
    }
}

fun Long.coercePlayerPosition(minMs: Long, maxMs: Long): Long = coerceIn(minMs, maxOf(minMs, maxMs))

fun coerceSeekRequestPosition(positionMs: Long, durationMs: Long): Long =
    if (durationMs > 0L) {
        positionMs.coercePlayerPosition(0L, durationMs)
    } else {
        positionMs.coerceAtLeast(0L)
    }

data class PlayerSeekDragState(
    val active: Boolean,
    val startPositionMs: Long,
    val durationMs: Long,
    val widthPx: Float,
    val totalDx: Float,
    val deltaMs: Long,
    val targetPositionMs: Long,
) {
    fun begin(
        currentPositionMs: Long,
        durationMs: Long,
        widthPx: Float,
    ): PlayerSeekDragState {
        val startPositionMs = coerceSeekRequestPosition(currentPositionMs, durationMs)
        return PlayerSeekDragState(
            active = true,
            startPositionMs = startPositionMs,
            durationMs = durationMs,
            widthPx = widthPx,
            totalDx = 0f,
            deltaMs = 0L,
            targetPositionMs = startPositionMs,
        )
    }

    fun dragTo(totalDx: Float): PlayerSeekDragState {
        if (!active) return this
        val deltaMs = calculateSeekDeltaMs(
            totalDx = totalDx,
            width = widthPx,
            durationMs = durationMs,
        )
        return copy(
            totalDx = totalDx,
            deltaMs = deltaMs,
            targetPositionMs = coerceSeekRequestPosition(startPositionMs + deltaMs, durationMs),
        )
    }

    fun onPlaybackPositionChanged(currentPositionMs: Long): PlayerSeekDragState {
        if (active) return this
        val positionMs = coerceSeekRequestPosition(currentPositionMs, durationMs)
        return copy(
            startPositionMs = positionMs,
            targetPositionMs = positionMs,
        )
    }

    fun finish(): PlayerSeekDragState = copy(active = false)

    companion object {
        val Idle = PlayerSeekDragState(
            active = false,
            startPositionMs = 0L,
            durationMs = 0L,
            widthPx = 0f,
            totalDx = 0f,
            deltaMs = 0L,
            targetPositionMs = 0L,
        )
    }
}

fun formatPlayerPosition(positionMs: Long): String {
    val totalSeconds = (positionMs.coerceAtLeast(0L) / 1000L)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
