package gomeng.dev.stashplayer.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.PlayerBackAction
import gomeng.dev.stashplayer.core.player.PlayerDebugInfoUiState
import gomeng.dev.stashplayer.core.player.PlayerGestureExclusionBounds
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerContentState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerLayout
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerState
import gomeng.dev.stashplayer.core.player.PlayerPlaybackUiStatus
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionState
import gomeng.dev.stashplayer.core.player.PlayerPlaylistUiItem
import gomeng.dev.stashplayer.core.player.PlayerSeparatedPlaybackOptionSheet
import gomeng.dev.stashplayer.core.player.PlayerStreamPreferenceOption
import gomeng.dev.stashplayer.core.player.PlayerStreamSourceOption
import gomeng.dev.stashplayer.core.player.resolvePlayerBackAction
import gomeng.dev.stashplayer.core.player.resolvePlayerOverlayTransportUiState
import gomeng.dev.stashplayer.core.player.resolvePlayerOverlayVisibilityPolicy
import gomeng.dev.stashplayer.core.player.shouldShowPlaybackStatusOverlay
import gomeng.dev.stashplayer.core.ui.designsystem.StashPlayerYoutubeVisualTokens
import kotlin.math.roundToLong

@Composable
fun PlayerOverlay(
    title: String,
    controlsVisible: Boolean,
    locked: Boolean,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    aspectRatioMode: AspectRatioMode,
    hudText: String?,
    seekPreview: PlayerSeekPreview?,
    playbackStatus: PlayerPlaybackUiStatus,
    playbackErrorText: String?,
    canTryAlternateSource: Boolean,
    canOpenSettings: Boolean,
    canOpenNextScene: Boolean,
    canEnterPictureInPicture: Boolean,
    canShuffleQueue: Boolean,
    shuffleEnabled: Boolean,
    ratingStep: Int,
    ratingMessage: String?,
    ratingUpdating: Boolean,
    currentStreamInfoText: String?,
    quickActions: List<PlayerOverlayQuickActionState>,
    fullscreenPlayerActive: Boolean,
    sceneId: String,
    infoDrawerContentState: PlayerInfoDrawerContentState,
    debugInfoUiState: PlayerDebugInfoUiState,
    similarRecommendations: List<SimilarSceneRecommendation>,
    similarRecommendationsLoading: Boolean,
    similarRecommendationsError: String?,
    similarRecommendationsSource: SimilarVideosRecommendationSource,
    serverProfile: StashServerProfile?,
    streamPreferenceOptions: List<PlayerStreamPreferenceOption>,
    streamSourceOptions: List<PlayerStreamSourceOption>,
    playlistItems: List<PlayerPlaylistUiItem>,
    infoDrawerState: PlayerInfoDrawerState,
    infoDrawerLayout: PlayerInfoDrawerLayout,
    previewRequestHeadersFor: (StashSpriteFrame) -> Map<String, String>,
    previewFrameFor: (Long) -> StashSpriteFrame?,
    onSeekPreview: (PlayerSeekPreview?) -> Unit,
    onExitPlayer: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onPreviousTransport: () -> Unit,
    onNextTransport: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleFullscreenPlayer: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onCycleSpeed: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onSelectPlaybackSpeed: (Float) -> Unit,
    onSelectAspectRatioMode: (AspectRatioMode) -> Unit,
    onSelectShuffleEnabled: (Boolean) -> Unit,
    onSelectRatingStep: (Int) -> Unit,
    onAddCurrentSceneToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onPlaySimilarScene: (String) -> Unit,
    onAddSimilarSceneToQueue: (String) -> Unit,
    onRetrySimilarRecommendations: () -> Unit,
    onSelectStreamPreference: (String) -> Unit,
    onSelectStreamSource: (Int) -> Unit,
    onToggleInfoDrawer: () -> Unit,
    onInfoDrawerDrag: (Float) -> Unit,
    onInfoDrawerDragEnd: () -> Unit,
    onOpenPlaylistDrawer: () -> Unit,
    onRetryPlayback: () -> Unit,
    onTryAlternateSource: () -> Unit,
    onOpenSettings: () -> Unit,
    onBottomControlsGestureBoundsChanged: (PlayerGestureExclusionBounds?) -> Unit = {},
    onBottomControlsHeightChanged: (Float) -> Unit = {},
    onPlayerGestureSuspendedByModalSurfaceChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val playbackFraction = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    var sliderDragging by remember { mutableStateOf(false) }
    var sliderPreviewFraction by remember { mutableFloatStateOf(0f) }
    var activePlaybackOptionSheet by remember { mutableStateOf<PlayerSeparatedPlaybackOptionSheet?>(null) }
    val sliderFraction = if (sliderDragging) sliderPreviewFraction else playbackFraction
    val displayedPositionMs = if (sliderDragging && durationMs > 0L) {
        (sliderFraction * durationMs).roundToLong()
    } else {
        positionMs
    }
    val backAction = resolvePlayerBackAction(
        playlistDrawerOpen = false,
        playbackOptionsOpen = activePlaybackOptionSheet != null,
    )
    val visibilityPolicy = resolvePlayerOverlayVisibilityPolicy(
        controlsVisible = controlsVisible,
        locked = locked,
        infoDrawerExpanded = infoDrawerState == PlayerInfoDrawerState.Expanded,
    )
    var bottomControlsBaseGestureBounds by remember { mutableStateOf<PlayerGestureExclusionBounds?>(null) }

    LaunchedEffect(
        visibilityPolicy.showBottomControls,
        bottomControlsBaseGestureBounds,
        infoDrawerLayout.drawerOffsetPx,
    ) {
        val baseBounds = bottomControlsBaseGestureBounds
        onBottomControlsGestureBoundsChanged(
            if (visibilityPolicy.showBottomControls && baseBounds != null) {
                baseBounds.translatedBy(infoDrawerLayout.drawerOffsetPx)
            } else {
                null
            },
        )
    }

    LaunchedEffect(activePlaybackOptionSheet) {
        onPlayerGestureSuspendedByModalSurfaceChanged(activePlaybackOptionSheet != null)
    }

    val transportState = resolvePlayerOverlayTransportUiState(
        controlsVisible = controlsVisible,
        locked = locked,
        isPlaying = isPlaying,
        canOpenNextScene = canOpenNextScene,
        seekPreviewActive = seekPreview != null,
        playbackStatusVisible = hudText != null || shouldShowPlaybackStatusOverlay(
            status = playbackStatus,
            hasSeekPreview = seekPreview != null,
            hasHudText = false,
        ),
    )

    BackHandler(
        enabled = backAction == PlayerBackAction.DismissPlaybackOptions,
    ) {
        when (backAction) {
            PlayerBackAction.DismissPlaybackOptions -> activePlaybackOptionSheet = null
            PlayerBackAction.DismissPlaylistDrawer -> Unit
            PlayerBackAction.DismissDebugSurface,
            PlayerBackAction.HideControls,
            PlayerBackAction.ExitPlayer -> Unit
        }
    }

    Box(modifier = modifier) {
        if (visibilityPolicy.showTopControls || visibilityPolicy.showUnlockOnly) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(StashPlayerYoutubeVisualTokens.TopScrimHeightDp.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = StashPlayerYoutubeVisualTokens.TopScrimStartAlpha),
                                Color.Black.copy(alpha = StashPlayerYoutubeVisualTokens.TopScrimEndAlpha),
                            ),
                        ),
                    ),
            )
        }

        if (visibilityPolicy.showTopControls) {
            PlayerTopControls(
                playlistItemCount = playlistItems.size,
                playbackSpeed = playbackSpeed,
                aspectRatioMode = aspectRatioMode,
                shuffleEnabled = shuffleEnabled,
                canShuffleQueue = canShuffleQueue,
                quickActions = if (visibilityPolicy.showQuickActions) quickActions else emptyList(),
                fullscreenPlayerActive = fullscreenPlayerActive,
                canEnterPictureInPicture = canEnterPictureInPicture,
                onToggleFullscreenPlayer = onToggleFullscreenPlayer,
                onEnterPictureInPicture = onEnterPictureInPicture,
                onOpenStreamOptions = { activePlaybackOptionSheet = PlayerSeparatedPlaybackOptionSheet.Stream },
                onOpenSpeedOptions = { activePlaybackOptionSheet = PlayerSeparatedPlaybackOptionSheet.Speed },
                onCycleAspectRatio = onCycleAspectRatio,
                onTogglePlaybackMode = onSelectShuffleEnabled,
                onOpenPlaylistDrawer = onOpenPlaylistDrawer,
                onExitPlayer = onExitPlayer,
                onToggleLock = onToggleLock,
                onAddCurrentSceneToQueue = onAddCurrentSceneToQueue,
                onToggleFavorite = onToggleFavorite,
                onToggleWatchLater = onToggleWatchLater,
            )
        } else if (visibilityPolicy.showUnlockOnly) {
            PlayerLockedTopControls(onToggleLock = onToggleLock)
        }

        if (!visibilityPolicy.showUnlockOnly) {
            PlayerStatusOverlay(
                hudText = hudText,
                seekPreview = seekPreview,
                playbackStatus = playbackStatus,
                playbackErrorText = playbackErrorText,
                canTryAlternateSource = canTryAlternateSource,
                canOpenSettings = canOpenSettings,
                canOpenNextScene = canOpenNextScene,
                previewRequestHeadersFor = previewRequestHeadersFor,
                onRetryPlayback = onRetryPlayback,
                onTryAlternateSource = onTryAlternateSource,
                onOpenNextScene = onNextTransport,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (transportState.visible) {
            PlayerOverlayTransportControls(
                state = transportState,
                isPlaying = isPlaying,
                onPreviousTransport = onPreviousTransport,
                onPlayPause = onPlayPause,
                onNextTransport = onNextTransport,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        if (visibilityPolicy.showBottomControls) {
            PlayerBottomControls(
                title = title,
                displayedPositionMs = displayedPositionMs,
                durationMs = durationMs,
                sliderFraction = sliderFraction,
                ratingStep = ratingStep,
                ratingMessage = ratingMessage,
                ratingUpdating = ratingUpdating,
                quickActions = quickActions,
                sceneId = sceneId,
                infoDrawerContentState = infoDrawerContentState,
                debugInfoUiState = debugInfoUiState,
                similarRecommendations = similarRecommendations,
                similarRecommendationsLoading = similarRecommendationsLoading,
                similarRecommendationsError = similarRecommendationsError,
                similarRecommendationsSource = similarRecommendationsSource,
                serverProfile = serverProfile,
                onSliderFractionChange = { fraction ->
                    sliderDragging = true
                    sliderPreviewFraction = fraction.coerceIn(0f, 1f)
                    val targetPositionMs = (sliderPreviewFraction * durationMs).roundToLong()
                    onSeekPreview(
                        PlayerSeekPreview(
                            deltaMs = targetPositionMs - positionMs,
                            targetPositionMs = targetPositionMs,
                            durationMs = durationMs,
                            frame = previewFrameFor(targetPositionMs),
                        ),
                    )
                },
                onSliderChangeFinished = {
                    if (durationMs > 0L && sliderDragging) {
                        onSeekTo((sliderPreviewFraction * durationMs).roundToLong())
                    }
                    sliderDragging = false
                    onSeekPreview(null)
                },
                onSelectRatingStep = onSelectRatingStep,
                onAddCurrentSceneToQueue = onAddCurrentSceneToQueue,
                onToggleFavorite = onToggleFavorite,
                onToggleWatchLater = onToggleWatchLater,
                onPlaySimilarScene = onPlaySimilarScene,
                onAddSimilarSceneToQueue = onAddSimilarSceneToQueue,
                onRetrySimilarRecommendations = onRetrySimilarRecommendations,
                infoDrawerState = infoDrawerState,
                infoDrawerLayout = infoDrawerLayout,
                onToggleInfoDrawer = onToggleInfoDrawer,
                onInfoDrawerDrag = onInfoDrawerDrag,
                onInfoDrawerDragEnd = onInfoDrawerDragEnd,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInParent()
                        onBottomControlsHeightChanged(coordinates.size.height.toFloat())
                        bottomControlsBaseGestureBounds = PlayerGestureExclusionBounds(
                            leftPx = bounds.left,
                            topPx = bounds.top,
                            rightPx = bounds.right,
                            bottomPx = bounds.bottom,
                        )
                    },
            )
        }
    }

    activePlaybackOptionSheet?.let { sheet ->
        PlayerPlaybackOptionsSheet(
            sheet = sheet,
            currentStreamInfoText = currentStreamInfoText,
            streamPreferenceOptions = streamPreferenceOptions,
            streamSourceOptions = streamSourceOptions,
            playbackSpeed = playbackSpeed,
            aspectRatioMode = aspectRatioMode,
            canShuffleQueue = canShuffleQueue,
            shuffleEnabled = shuffleEnabled,
            onDismiss = { activePlaybackOptionSheet = null },
            onSelectStreamSource = { index ->
                activePlaybackOptionSheet = null
                onSelectStreamSource(index)
            },
            onSelectStreamPreference = { id ->
                activePlaybackOptionSheet = null
                onSelectStreamPreference(id)
            },
            onSelectPlaybackSpeed = { speed ->
                activePlaybackOptionSheet = null
                onSelectPlaybackSpeed(speed)
            },
            onSelectAspectRatioMode = { mode ->
                activePlaybackOptionSheet = null
                onSelectAspectRatioMode(mode)
            },
            onSelectShuffleEnabled = { enabled ->
                activePlaybackOptionSheet = null
                if (enabled || enabled != shuffleEnabled) {
                    onSelectShuffleEnabled(enabled)
                }
            },
        )
    }

}
