package gomeng.dev.stashplayer.core.player

data class PlayerPresentationRouteState(
    val settledMode: PlayerPresentationMode,
    val targetMode: PlayerPresentationMode,
    val dragUpdate: PlayerPresentationDragUpdate? = null,
    val releaseProgress: Float? = null,
    val gestureMode: PlayerPresentationGestureMode = PlayerPresentationGestureMode.None,
    val landscapeAutoFullscreen: Boolean = false,
) {
    val fullscreenPlayerActive: Boolean
        get() = targetMode == PlayerPresentationMode.Fullscreen

    val presentationTransitionActive: Boolean
        get() = dragUpdate != null || settledMode != targetMode

    val fullscreenChromeActive: Boolean
        get() = fullscreenPlayerActive || presentationTransitionActive

    val playerSurfacePresentationMode: PlayerPresentationMode?
        get() = if (presentationTransitionActive && dragUpdate == null) {
            null
        } else {
            PlayerWatchPageController.playerSurfacePresentationGestureMode(fullscreenPlayerActive)
        }

    fun withTargetMode(targetMode: PlayerPresentationMode): PlayerPresentationRouteStateUpdate =
        PlayerPresentationRouteStateUpdate(
            state = copy(targetMode = targetMode),
            refreshControls = true,
        )

    fun exitFullscreenToWatchPage(): PlayerPresentationRouteStateUpdate =
        withTargetMode(PlayerPresentationMode.WatchPage)

    fun withDragUpdate(update: PlayerPresentationDragUpdate?): PlayerPresentationRouteState =
        if (update == null) {
            this
        } else {
            copy(
                dragUpdate = update,
                gestureMode = update.gestureMode,
            )
        }

    fun withDragRelease(release: PlayerPresentationDragRelease): PlayerPresentationRouteStateUpdate =
        PlayerPresentationRouteStateUpdate(
            state = copy(
                targetMode = release.targetMode,
                dragUpdate = null,
                releaseProgress = release.progress,
                gestureMode = release.gestureMode,
            ),
            refreshControls = true,
        )

    fun settleAnimation(): PlayerPresentationRouteState = copy(
        settledMode = targetMode,
        releaseProgress = null,
        gestureMode = PlayerPresentationGestureMode.None,
    )

    fun forDeviceLandscape(isLandscape: Boolean): PlayerPresentationRouteState = when {
        isLandscape && targetMode == PlayerPresentationMode.WatchPage -> copy(
            targetMode = PlayerPresentationMode.Fullscreen,
            dragUpdate = null,
            releaseProgress = null,
            gestureMode = PlayerPresentationGestureMode.None,
            landscapeAutoFullscreen = true,
        )
        !isLandscape && landscapeAutoFullscreen -> copy(
            targetMode = PlayerPresentationMode.WatchPage,
            dragUpdate = null,
            releaseProgress = null,
            gestureMode = PlayerPresentationGestureMode.None,
            landscapeAutoFullscreen = false,
        )
        else -> this
    }

    companion object {
        fun initial(mode: PlayerPresentationMode): PlayerPresentationRouteState =
            PlayerPresentationRouteState(
                settledMode = mode,
                targetMode = mode,
            )
    }
}

data class PlayerPresentationRouteStateUpdate(
    val state: PlayerPresentationRouteState,
    val refreshControls: Boolean,
)

fun shouldExitPlayerFromFullscreenBack(
    fullscreenPlayerActive: Boolean,
    isLandscape: Boolean,
): Boolean = fullscreenPlayerActive && isLandscape

fun allowsPlayerPresentationModeChanges(isLandscape: Boolean): Boolean = !isLandscape
