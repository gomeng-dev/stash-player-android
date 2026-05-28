package gomeng.dev.stashplayer.feature.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.text.CueGroup
import androidx.media3.session.MediaSession
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import gomeng.dev.stashplayer.core.debug.StashDebugLogBuffer
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteConfirmationState
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteResult
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.network.GraphQlStashStreamResolver
import gomeng.dev.stashplayer.core.network.ResolvedStashStreamCandidate
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashStreamPreference
import gomeng.dev.stashplayer.core.network.StashStreamSourceCategory
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.StashStream
import gomeng.dev.stashplayer.core.network.buildSimilarScenesRepository
import gomeng.dev.stashplayer.core.network.findStashSpriteAtTime
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.network.spritePreviewHeadersFor
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.BrightnessController
import gomeng.dev.stashplayer.core.player.FastPlaybackHoldSpeedPreference
import gomeng.dev.stashplayer.core.player.PLAYER_CONTROLS_AUTO_HIDE_MS
import gomeng.dev.stashplayer.core.player.PlaybackEndAction
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.PlayerAutoAdvanceDecision
import gomeng.dev.stashplayer.core.player.PlayerBackAction
import gomeng.dev.stashplayer.core.player.PlayerNextAction
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueue
import gomeng.dev.stashplayer.core.player.PlayerPlaybackUiStatus
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragRelease
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragUpdate
import gomeng.dev.stashplayer.core.player.PlayerPresentationMode
import gomeng.dev.stashplayer.core.player.PlayerFastPlaybackHoldState
import gomeng.dev.stashplayer.core.player.PlayerGestureExclusionBounds
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerState
import gomeng.dev.stashplayer.core.player.PlayerPlaylistDrawerHostScope
import gomeng.dev.stashplayer.core.player.PlayerPresentationRouteState
import gomeng.dev.stashplayer.core.player.PlayerPictureInPictureAspectRatio
import gomeng.dev.stashplayer.core.player.PlayerPreviousAction
import gomeng.dev.stashplayer.core.player.PlayerRatingState
import gomeng.dev.stashplayer.core.player.PlayerSimilarRecommendationsRequestKey
import gomeng.dev.stashplayer.core.player.PlayerSimilarRecommendationsRequestState
import gomeng.dev.stashplayer.core.player.PlayerResumeSaveEffectState
import gomeng.dev.stashplayer.core.player.PlayerResumeSaveReason
import gomeng.dev.stashplayer.core.player.PlayerResumeSyncPolicy
import gomeng.dev.stashplayer.core.player.PlayerSceneAddPlaySyncPolicy
import gomeng.dev.stashplayer.core.player.PlayerSceneAddPlaySyncState
import gomeng.dev.stashplayer.core.player.PlayerStreamFallbackDecision
import gomeng.dev.stashplayer.core.player.PlayerStreamSelectionController
import gomeng.dev.stashplayer.core.player.PlayerStreamSourceCandidateLabel
import gomeng.dev.stashplayer.core.player.StashPictureInPictureActionHandler
import gomeng.dev.stashplayer.core.player.StashPictureInPictureController
import gomeng.dev.stashplayer.core.player.StashPictureInPictureRequest
import gomeng.dev.stashplayer.core.player.StashPlayerController
import gomeng.dev.stashplayer.core.player.StashPlaybackSessionRegistry
import gomeng.dev.stashplayer.core.player.StashPlaybackSessionService
import gomeng.dev.stashplayer.core.player.SubtitleLanguagePreference
import gomeng.dev.stashplayer.core.player.SubtitlePosition
import gomeng.dev.stashplayer.core.player.SubtitleTextAlignment
import gomeng.dev.stashplayer.core.player.VolumeController
import gomeng.dev.stashplayer.core.player.applyPlayerPlaylistDeleteResult
import gomeng.dev.stashplayer.core.player.buildPlayerPlaylistUiItems
import gomeng.dev.stashplayer.core.player.buildPlayerOverlayQuickActionStates
import gomeng.dev.stashplayer.core.player.buildPlayerPresentationMotionState
import gomeng.dev.stashplayer.core.player.buildPlayerOverlayTitle
import gomeng.dev.stashplayer.core.player.appendSimilarSceneToPlaybackQueue
import gomeng.dev.stashplayer.core.player.buildPlayerDebugInfoUiState
import gomeng.dev.stashplayer.core.player.buildPlayerInfoDrawerContentState
import gomeng.dev.stashplayer.core.player.buildResumePlaybackPromptState
import gomeng.dev.stashplayer.core.player.buildPlayerStreamPreferenceOptions
import gomeng.dev.stashplayer.core.player.buildPlayerStreamSourceOptions
import gomeng.dev.stashplayer.core.player.buildSingleScenePlaybackQueue
import gomeng.dev.stashplayer.core.player.canShowPlayerPlaylistAction
import gomeng.dev.stashplayer.core.player.formatPlayerPosition
import gomeng.dev.stashplayer.core.player.playerDebugOverlayText
import gomeng.dev.stashplayer.core.player.playerAspectRatioHudText
import gomeng.dev.stashplayer.core.player.playerPlaybackInfoLoadStartLogMessage
import gomeng.dev.stashplayer.core.player.playerPlaybackInfoLoadSuccessLogMessage
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedHudText
import gomeng.dev.stashplayer.core.player.resolvePlayerPictureInPictureAspectRatio
import gomeng.dev.stashplayer.core.player.playerSimilarRecommendationClickLogMessage
import gomeng.dev.stashplayer.core.player.playerSimilarRecommendationQueueLogMessage
import gomeng.dev.stashplayer.core.player.playerSimilarRecommendationsLoadedLogMessage
import gomeng.dev.stashplayer.core.player.playerSimilarRecommendationsRequestStartLogMessage
import gomeng.dev.stashplayer.core.player.removePlayerPlaylistItem
import gomeng.dev.stashplayer.core.player.reorderPlayerPlaylistItem
import gomeng.dev.stashplayer.core.player.selectSimilarSceneForPlayback
import gomeng.dev.stashplayer.core.player.PlayerTransportController
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistDrawerPresentationPolicy
import gomeng.dev.stashplayer.core.player.resolvePlayerPresentationOverlayAlpha
import gomeng.dev.stashplayer.core.player.resolvePlayerResumeStartPositionMs
import gomeng.dev.stashplayer.core.player.sanitizePlaybackErrorText
import gomeng.dev.stashplayer.core.player.shouldAutoHidePlayerControls
import gomeng.dev.stashplayer.core.player.shouldAutoFallbackPlaybackSource
import gomeng.dev.stashplayer.core.player.shouldHidePlayerSystemBars
import gomeng.dev.stashplayer.core.player.shouldPausePlayerForLifecycleStop
import gomeng.dev.stashplayer.core.player.shouldExposePictureInPictureButton
import gomeng.dev.stashplayer.core.player.shouldRequestSimilarRecommendationsForWatchPage
import gomeng.dev.stashplayer.core.player.shouldPromptForPlayerResumePosition
import gomeng.dev.stashplayer.core.player.markSimilarRecommendationsRequestCancelled
import gomeng.dev.stashplayer.core.player.markSimilarRecommendationsRequestCompleted
import gomeng.dev.stashplayer.core.player.markSimilarRecommendationsRequestStarted
import gomeng.dev.stashplayer.core.player.nextPlayerMediaSessionId
import gomeng.dev.stashplayer.core.player.nextPlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.playerPlaybackOrientationHudText
import gomeng.dev.stashplayer.core.player.togglePlayerLockState
import gomeng.dev.stashplayer.core.player.subtitleTrackLanguageCode
import gomeng.dev.stashplayer.core.player.toPlayerSceneCardModel
import gomeng.dev.stashplayer.core.ui.components.SceneBulkDeleteConfirmationDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val RESUME_PROMPT_TIMEOUT_MS = 4_000L
private val PLAYER_PLAYLIST_TRAILING_ITEM_COUNT = 30
private val PLAYER_PRESENTATION_MOTION_DURATION_MS = 240
private val PLAYER_SIDE_CONTROL_OVERLAY_AUTO_HIDE_MS = 2_000L
private val PLAYER_SIDE_CONTROL_OVERLAY_FADE_MS = 220

private object PlayerSceneAddPlaySessionGuard {
    private var state = PlayerSceneAddPlaySyncState()

    @Synchronized
    fun reserve(sceneId: String, accumulatedPlaySeconds: Double, nowMs: Long): Boolean {
        val decision = PlayerSceneAddPlaySyncPolicy.resolveAttempt(
            state = state,
            sceneId = sceneId,
            accumulatedPlaySeconds = accumulatedPlaySeconds,
            nowMs = nowMs,
        )
        state = decision.state
        return decision.shouldCallAddPlay
    }

    @Synchronized
    fun markSucceeded(sceneId: String, nowMs: Long) {
        state = PlayerSceneAddPlaySyncPolicy.markSucceeded(state, sceneId, nowMs)
    }

    @Synchronized
    fun markFailed(sceneId: String) {
        state = PlayerSceneAddPlaySyncPolicy.markFailed(state, sceneId)
    }
}

@Composable
fun PlayerRoute(
    sceneId: String,
    isFoldLikeLayout: Boolean,
    playbackQueue: PlayerPlaybackQueue = buildSingleScenePlaybackQueue(sceneId),
    initialPresentationMode: PlayerPresentationMode = PlayerPresentationMode.WatchPage,
    onPlaybackQueueChange: (PlayerPlaybackQueue) -> Unit = {},
    onPresentationModeChange: (PlayerPresentationMode) -> Unit = {},
    onOpenScene: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onExitPlayer: () -> Unit = {},
    onPlaylistDrawerOpen: suspend (String, Int) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val playerDebugOverlayEnabled by settingsRepository.playerDebugOverlayEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYER_DEBUG_OVERLAY_ENABLED,
    )
    val defaultStreamPreference by settingsRepository.defaultStreamPreference.collectAsState(
        initial = StashSettingsRepository.DEFAULT_STREAM_PREFERENCE,
    )
    val playbackEndAction by settingsRepository.playbackEndAction.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYBACK_END_ACTION,
    )
    val fastPlaybackHoldSpeed by settingsRepository.fastPlaybackHoldSpeed.collectAsState(
        initial = StashSettingsRepository.DEFAULT_FAST_PLAYBACK_HOLD_SPEED,
    )
    val backgroundPlaybackEnabled by settingsRepository.backgroundPlaybackEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_BACKGROUND_PLAYBACK_ENABLED,
    )
    val pictureInPictureEnabled by settingsRepository.pictureInPictureEnabled.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PICTURE_IN_PICTURE_ENABLED,
    )
    val playbackOrientationMode by settingsRepository.playbackOrientationMode.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYBACK_ORIENTATION_MODE,
    )
    val subtitleLanguage by settingsRepository.subtitleLanguage.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_LANGUAGE,
    )
    val subtitleFontScale by settingsRepository.subtitleFontScale.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_FONT_SCALE,
    )
    val subtitlePosition by settingsRepository.subtitlePosition.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_POSITION,
    )
    val subtitleTextAlignment by settingsRepository.subtitleTextAlignment.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SUBTITLE_TEXT_ALIGNMENT,
    )

    val activeProfile = profile
    if (activeProfile == null) {
        PlayerActionMessage(
            message = stashString(R.string.auto_kr_0470),
            primaryActionLabel = stashString(R.string.auto_kr_0270),
            onPrimaryAction = onOpenSettings,
        )
        return
    }

    val client = remember(activeProfile) { StashGraphQlClient(activeProfile) }
    var stream by remember(sceneId, activeProfile) { mutableStateOf<StashStream?>(null) }
    var loadError by remember(sceneId, activeProfile) { mutableStateOf<String?>(null) }
    var streamLoadRetryKey by remember(sceneId, activeProfile) { mutableLongStateOf(0L) }

    LaunchedEffect(sceneId, activeProfile, streamLoadRetryKey) {
        stream = null
        loadError = null
        StashDebugLogBuffer.record("Player", playerPlaybackInfoLoadStartLogMessage(sceneId))
        runCatching {
            GraphQlStashStreamResolver(client, activeProfile).resolve(sceneId)
        }.onSuccess { resolvedStream ->
            StashDebugLogBuffer.record(
                "Player",
                playerPlaybackInfoLoadSuccessLogMessage(
                    sceneId = sceneId,
                    streamCandidateCount = resolvedStream.resolvedCandidates.size,
                    thumbnailAvailable = !resolvedStream.thumbnailUrl.isNullOrBlank() || !resolvedStream.spriteImageUrl.isNullOrBlank(),
                ),
            )
            stream = resolvedStream
        }.onFailure {
            StashDebugLogBuffer.record("Player", "Playback info load failed", it)
            loadError = sanitizePlaybackErrorText(it.message ?: it::class.simpleName ?: stashString(R.string.auto_kr_0447)) ?: stashString(R.string.auto_kr_0447)
        }
    }

    when {
        loadError != null -> PlayerActionMessage(
            message = stashString(R.string.auto_kr_0471, loadError),
            primaryActionLabel = stashString(R.string.auto_kr_0031),
            onPrimaryAction = { streamLoadRetryKey += 1L },
            secondaryActionLabel = stashString(R.string.auto_kr_0270),
            onSecondaryAction = onOpenSettings,
        )
        stream == null -> PlayerLoadingMessage(stashString(R.string.auto_kr_0472))
        else -> RealPlayerRoute(
            sceneId = sceneId,
            stream = stream!!,
            profile = activeProfile,
            playerDebugOverlayEnabled = playerDebugOverlayEnabled,
            defaultStreamPreference = defaultStreamPreference,
            playbackEndAction = playbackEndAction,
            fastPlaybackHoldSpeed = fastPlaybackHoldSpeed,
            backgroundPlaybackEnabled = backgroundPlaybackEnabled,
            pictureInPictureEnabled = pictureInPictureEnabled,
            playbackOrientationMode = playbackOrientationMode,
            subtitleLanguage = subtitleLanguage,
            subtitleFontScale = subtitleFontScale,
            subtitlePosition = subtitlePosition,
            subtitleTextAlignment = subtitleTextAlignment,
            client = client,
            isFoldLikeLayout = isFoldLikeLayout,
            playbackQueue = playbackQueue.withCurrent(sceneId),
            initialPresentationMode = initialPresentationMode,
            onPlaybackQueueChange = onPlaybackQueueChange,
            onPresentationModeChange = onPresentationModeChange,
            onOpenScene = onOpenScene,
            onOpenSettings = onOpenSettings,
            onExitPlayer = onExitPlayer,
            onPlaylistDrawerOpen = onPlaylistDrawerOpen,
        )
    }
}

@Composable
private fun RealPlayerRoute(
    sceneId: String,
    stream: StashStream,
    profile: StashServerProfile,
    playerDebugOverlayEnabled: Boolean,
    defaultStreamPreference: StashStreamPreference,
    playbackEndAction: PlaybackEndAction,
    fastPlaybackHoldSpeed: FastPlaybackHoldSpeedPreference,
    backgroundPlaybackEnabled: Boolean,
    pictureInPictureEnabled: Boolean,
    playbackOrientationMode: PlaybackOrientationMode,
    subtitleLanguage: SubtitleLanguagePreference,
    subtitleFontScale: Float,
    subtitlePosition: SubtitlePosition,
    subtitleTextAlignment: SubtitleTextAlignment,
    client: StashGraphQlClient,
    isFoldLikeLayout: Boolean,
    playbackQueue: PlayerPlaybackQueue,
    initialPresentationMode: PlayerPresentationMode,
    onPlaybackQueueChange: (PlayerPlaybackQueue) -> Unit,
    onPresentationModeChange: (PlayerPresentationMode) -> Unit,
    onOpenScene: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onExitPlayer: () -> Unit,
    onPlaylistDrawerOpen: suspend (String, Int) -> Unit,
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localView = LocalView.current
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val activity = remember(context, localView) { context.findActivity() ?: localView.context.findActivity() }
    val baseResolvedCandidates = stream.resolvedCandidates.ifEmpty {
        listOf(
            ResolvedStashStreamCandidate(
                uri = stream.uri,
                sourceCategory = stream.sourceCategory,
                sourceType = stream.sourceType,
                sourceLabel = stream.sourceLabel,
                mimeType = stream.sourceMimeType,
                urlExtensionHint = stream.sourceUrlExtensionHint,
                isHlsManifest = stream.sourceIsHlsManifest,
            ),
        )
    }
    var activeCandidateIndex by remember(stream) { mutableStateOf(0) }
    var streamPreference by remember(stream, defaultStreamPreference) { mutableStateOf(defaultStreamPreference) }
    var prepareRequestKey by remember(stream) { mutableLongStateOf(0L) }
    val resolvedCandidates = PlayerStreamSelectionController.orderResolvedCandidatesForPreference(
        resolvedCandidates = baseResolvedCandidates,
        streamCandidates = stream.streamCandidates,
        preference = streamPreference,
    )
    var reprepareStartPositionMs by remember(stream) { mutableStateOf<Long?>(null) }
    var seekPreviewState by remember(stream) { mutableStateOf(PlayerSeekPreviewControllerState()) }
    val activeCandidate = resolvedCandidates[
        PlayerStreamSelectionController.coerceCandidateIndex(activeCandidateIndex, resolvedCandidates.size),
    ]
    val activeCandidateKey = PlayerStreamSelectionController.candidateKey(activeCandidate)
    val controller = remember(stream.sceneId, stream.requestHeaders) {
        StashPlayerController(
            context = context,
            requestHeaders = stream.requestHeaders,
        )
    }
    val mediaSession = remember(controller) {
        MediaSession.Builder(context, controller.player)
            .setId(nextPlayerMediaSessionId())
            .build()
    }
    val similarScenesRepository = remember(client, profile) {
        buildSimilarScenesRepository(
            graphQlClient = client,
            stashServerProfile = profile,
        )
    }
    var similarRecommendationsSource by remember(sceneId, similarScenesRepository) {
        mutableStateOf(SimilarVideosRecommendationSource.HybridBackend)
    }
    var similarRecommendations by remember(sceneId, similarScenesRepository) {
        mutableStateOf<List<SimilarSceneRecommendation>>(emptyList())
    }
    var similarRecommendationsLoading by remember(sceneId, similarScenesRepository) { mutableStateOf(false) }
    var similarRecommendationsError by remember(sceneId, similarScenesRepository) { mutableStateOf<String?>(null) }
    var similarRecommendationsRetryKey by remember(sceneId, similarScenesRepository) { mutableLongStateOf(0L) }
    var similarRecommendationsRequestState by remember(similarScenesRepository) {
        mutableStateOf(PlayerSimilarRecommendationsRequestState())
    }
    val brightnessController = remember(activity) { activity?.let(::BrightnessController) }
    val volumeController = remember(context) { VolumeController(context) }

    var controlsVisible by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var presentationRouteState by remember(sceneId) {
        mutableStateOf(PlayerPresentationRouteState.initial(initialPresentationMode))
    }
    val fullscreenPlayerActive = presentationRouteState.fullscreenPlayerActive
    val presentationTransitionActive = presentationRouteState.presentationTransitionActive
    val fullscreenChromeActive = presentationRouteState.fullscreenChromeActive
    val playerSurfacePresentationMode = presentationRouteState.playerSurfacePresentationMode
    val presentationDragUpdate = presentationRouteState.dragUpdate
    val presentationProgressAnimation = remember(sceneId) {
        Animatable(if (presentationRouteState.targetMode == PlayerPresentationMode.Fullscreen) 1f else 0f)
    }
    LaunchedEffect(presentationRouteState.targetMode) {
        onPresentationModeChange(presentationRouteState.targetMode)
    }
    LaunchedEffect(sceneId, presentationRouteState.targetMode, presentationDragUpdate) {
        val dragUpdate = presentationDragUpdate
        if (dragUpdate != null) {
            presentationProgressAnimation.snapTo(dragUpdate.progress.coerceIn(0f, 1f))
        } else {
            presentationRouteState.releaseProgress?.let { releaseProgress ->
                presentationProgressAnimation.snapTo(releaseProgress.coerceIn(0f, 1f))
                presentationRouteState = presentationRouteState.copy(releaseProgress = null)
            }
            val targetMode = presentationRouteState.targetMode
            val targetProgress = if (targetMode == PlayerPresentationMode.Fullscreen) 1f else 0f
            if (presentationRouteState.settledMode != targetMode || presentationProgressAnimation.value != targetProgress) {
                presentationProgressAnimation.animateTo(
                    targetValue = targetProgress,
                    animationSpec = tween(
                        durationMillis = PLAYER_PRESENTATION_MOTION_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            presentationRouteState = presentationRouteState.settleAnimation()
        }
    }
    val panelMaxTranslationYPx = with(LocalDensity.current) { 360.dp.toPx() }
    val presentationMotionState = buildPlayerPresentationMotionState(
        transitionProgress = presentationProgressAnimation.value,
        maxWatchPageContentTranslationYPx = panelMaxTranslationYPx,
        presentationGestureMode = presentationRouteState.gestureMode,
        dragUpdate = presentationDragUpdate,
    )
    val fullscreenInfoPolicy = PlayerWatchPageController.resolveFullscreenOverlayInfoPolicy(
        legacyInfoDrawerState = PlayerInfoDrawerState.Collapsed,
        legacyDragDeltaPx = 0f,
        watchPageDetailsVisible = true,
    )
    val infoDrawerState = fullscreenInfoPolicy.overlayInfoState
    val infoDrawerLayout = fullscreenInfoPolicy.overlayLayout
    var playerGestureExclusionBounds by remember { mutableStateOf<PlayerGestureExclusionBounds?>(null) }
    var playerGesturesSuspendedByModalSurface by remember { mutableStateOf(false) }
    var lastControlInteractionAtMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var positionMs by remember { mutableLongStateOf(stream.startPositionMs) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackStatus by remember(stream) { mutableStateOf(PlayerPlaybackUiStatus.Loading) }
    var playbackErrorText by remember(stream) { mutableStateOf<String?>(null) }
    var hudText by remember { mutableStateOf<String?>(null) }
    var sideControlOverlayState by remember { mutableStateOf<PlayerSideControlOverlayState?>(null) }
    var sideControlOverlayVisible by remember { mutableStateOf(false) }
    var sideControlOverlayDragging by remember { mutableStateOf(false) }
    var subtitleCueText by remember(stream) { mutableStateOf<String?>(null) }
    val seekPreview = seekPreviewState.preview
    val pictureInPictureActive by StashPictureInPictureController.active.collectAsState(
        initial = activity?.isInPictureInPictureMode == true,
    )

    fun markPlayerInteraction() {
        lastControlInteractionAtMs = System.currentTimeMillis()
    }

    fun exitFullscreenToWatchPage() {
        val update = presentationRouteState.exitFullscreenToWatchPage()
        presentationRouteState = update.state
        if (update.refreshControls) {
            controlsVisible = true
            lastControlInteractionAtMs = System.currentTimeMillis()
        }
    }

    fun updatePresentationDrag(update: PlayerPresentationDragUpdate?) {
        presentationRouteState = presentationRouteState.withDragUpdate(update)
    }

    fun releasePresentationDrag(release: PlayerPresentationDragRelease) {
        val update = presentationRouteState.withDragRelease(release)
        presentationRouteState = update.state
        if (update.refreshControls) {
            controlsVisible = true
            lastControlInteractionAtMs = System.currentTimeMillis()
        }
    }

    fun showSideControlOverlay(kind: PlayerSideControlKind, fraction: Float) {
        sideControlOverlayVisible = true
        sideControlOverlayState = PlayerSideControlOverlayState(
            kind = kind,
            fraction = fraction.coerceIn(0f, 1f),
            updatedAtMs = System.currentTimeMillis(),
        )
    }

    fun updateBrightnessFraction(fraction: Float): String {
        val controller = brightnessController ?: return stashString(R.string.auto_kr_0480)
        val value = controller.setFraction(fraction)
        showSideControlOverlay(PlayerSideControlKind.Brightness, value)
        return controller.label(value)
    }

    fun updateVolumeFraction(fraction: Float): String {
        val value = volumeController.setFraction(fraction)
        showSideControlOverlay(PlayerSideControlKind.Volume, value)
        return volumeController.label(value)
    }

    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var fastPlaybackHoldState by remember { mutableStateOf(PlayerFastPlaybackHoldState.Idle) }
    var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.Fit) }
    var ratingState by remember(stream.sceneId, stream.rating100) { mutableStateOf(PlayerRatingState(stream.rating100)) }
    var ratingSaveRequestId by remember(stream.sceneId) { mutableLongStateOf(0L) }
    var oCounter by remember(stream.sceneId, stream.oCounter) {
        mutableIntStateOf((stream.oCounter ?: 0).coerceAtLeast(0))
    }
    var oCounterUpdating by remember(stream.sceneId) { mutableStateOf(false) }
    val favoriteSceneIds by localRepository.favoriteSceneIds.collectAsState(initial = emptySet())
    val watchLaterSceneIds by localRepository.watchLaterSceneIds.collectAsState(initial = emptySet())
    val queueSceneIds by localRepository.queueSceneIds.collectAsState(initial = emptySet())
    var playlistDrawerOpen by remember { mutableStateOf(false) }
    var playlistDeleteConfirmation by remember { mutableStateOf(SceneBulkDeleteConfirmationState.Hidden) }
    var pendingPlaylistDeleteSceneIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val currentSceneCard = remember(stream, positionMs, durationMs, watchLaterSceneIds) {
        stream.toPlayerSceneCardModel(
            currentPositionMs = positionMs,
            durationMs = durationMs,
            isInWatchLater = sceneId in watchLaterSceneIds,
        )
    }
    LaunchedEffect(stream.sceneId) {
        localRepository.recordPlaybackHistory(currentSceneCard)
    }
    val quickActions = buildPlayerOverlayQuickActionStates(
        isQueued = sceneId in queueSceneIds,
        isFavorite = sceneId in favoriteSceneIds,
        isInWatchLater = sceneId in watchLaterSceneIds,
    )
    val addCurrentSceneToQueue: () -> Unit = {
        markPlayerInteraction()
        controlsVisible = true
        if (sceneId in queueSceneIds) {
            hudText = stashString(R.string.auto_kr_0484)
        } else {
            scope.launch {
                localRepository.addToQueue(currentSceneCard)
                hudText = stashString(R.string.auto_kr_0227)
            }
        }
    }
    val toggleFavorite: () -> Unit = {
        markPlayerInteraction()
        controlsVisible = true
        val shouldEnable = sceneId !in favoriteSceneIds
        scope.launch {
            localRepository.setFavorite(currentSceneCard, shouldEnable)
            hudText = if (shouldEnable) stashString(R.string.auto_kr_0485) else stashString(R.string.auto_kr_0121)
        }
    }
    val toggleWatchLater: () -> Unit = {
        markPlayerInteraction()
        controlsVisible = true
        val shouldEnable = sceneId !in watchLaterSceneIds
        scope.launch {
            localRepository.setWatchLater(currentSceneCard, shouldEnable)
            hudText = if (shouldEnable) stashString(R.string.auto_kr_0486) else stashString(R.string.auto_kr_0487)
        }
    }
    val retrySimilarRecommendations: () -> Unit = {
        markPlayerInteraction()
        controlsVisible = true
        similarRecommendationsRetryKey += 1L
    }
    var playCountSynced by remember(sceneId) { mutableStateOf(false) }
    var accumulatedPlaySeconds by remember(sceneId) { mutableFloatStateOf(0f) }
    var resumeSaveState by remember(sceneId) { mutableStateOf(PlayerResumeSaveEffectState()) }
    var autoAdvancedFromSceneId by remember(sceneId) { mutableStateOf<String?>(null) }
    var repeatedFromSceneId by remember(sceneId) { mutableStateOf<String?>(null) }
    var autoAdvanceArmed by remember(stream) { mutableStateOf(false) }
    var playbackPrepared by remember(stream) { mutableStateOf(false) }
    val resolvedResumeStartPositionMs = remember(stream) {
        resolvePlayerResumeStartPositionMs(
            startPositionMs = stream.startPositionMs,
            durationSeconds = stream.durationSeconds,
        )
    }
    val shouldShowResumePrompt = remember(stream) {
        shouldPromptForPlayerResumePosition(
            startPositionMs = stream.startPositionMs,
            durationSeconds = stream.durationSeconds,
        )
    }
    var resumeStartPositionMs by remember(stream) {
        mutableStateOf<Long?>(
            if (shouldShowResumePrompt) {
                null
            } else {
                resolvedResumeStartPositionMs
            },
        )
    }
    var resumePromptVisible by remember(stream) { mutableStateOf(shouldShowResumePrompt) }

    BackHandler {
        when (
            PlayerTransportController.resolveBackAction(
                playlistDrawerOpen = false,
                infoDrawerExpanded = infoDrawerState == PlayerInfoDrawerState.Expanded,
                controlsVisible = controlsVisible && !locked,
            )
        ) {
            PlayerBackAction.DismissPlaybackOptions -> Unit
            PlayerBackAction.DismissPlaylistDrawer -> Unit
            PlayerBackAction.DismissDebugSurface -> Unit
            PlayerBackAction.HideControls -> controlsVisible = false
            PlayerBackAction.ExitPlayer -> {
                if (fullscreenPlayerActive) {
                    exitFullscreenToWatchPage()
                } else {
                    onExitPlayer()
                }
            }
        }
    }
    PlayerSystemBarsEffect(
        activity = activity,
        hideSystemBars = presentationDragUpdate == null &&
            (presentationMotionState.hideSystemBars ||
                (fullscreenPlayerActive && shouldHidePlayerSystemBars(controlsVisible))),
    )
    LaunchedEffect(controller, subtitleLanguage) {
        controller.applySubtitleLanguagePreference(
            preference = subtitleLanguage,
            languageCode = subtitleTrackLanguageCode(subtitleLanguage),
        )
    }

    LaunchedEffect(backgroundPlaybackEnabled) {
        if (backgroundPlaybackEnabled) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, StashPlaybackSessionService::class.java),
            )
        }
    }

    DisposableEffect(mediaSession) {
        StashPlaybackSessionRegistry.register(mediaSession)
        onDispose {
            StashPlaybackSessionRegistry.unregister(mediaSession)
            mediaSession.release()
            context.stopService(Intent(context, StashPlaybackSessionService::class.java))
        }
    }

    LaunchedEffect(stream) {
        resumeSaveState = PlayerResumeSyncPolicy.resetSeekThrottleForStreamRefresh(resumeSaveState)
    }

    val similarRecommendationsLoadEligible = fullscreenInfoPolicy.watchPageDetailsEnabled

    LaunchedEffect(
        sceneId,
        similarScenesRepository,
        similarRecommendationsRetryKey,
        similarRecommendationsLoadEligible,
    ) {
        if (
            !shouldRequestSimilarRecommendationsForWatchPage(
                sceneId = sceneId,
                watchPageVisible = similarRecommendationsLoadEligible,
                requestState = similarRecommendationsRequestState,
                retryKey = similarRecommendationsRetryKey,
            )
        ) {
            return@LaunchedEffect
        }
        val requestKey = PlayerSimilarRecommendationsRequestKey(
            sceneId = sceneId.trim(),
            retryKey = similarRecommendationsRetryKey,
        )
        StashDebugLogBuffer.record(
            "Player",
            playerSimilarRecommendationsRequestStartLogMessage(
                sceneId = sceneId,
                retryKey = similarRecommendationsRetryKey,
            ),
        )
        similarRecommendationsRequestState = similarRecommendationsRequestState
            .markSimilarRecommendationsRequestStarted(requestKey)
        similarRecommendationsLoading = true
        similarRecommendationsError = null
        similarRecommendations = emptyList()
        try {
            similarScenesRepository.getSimilarScenesWithSource(sceneId, limit = 10)
                .onSuccess { result ->
                    similarRecommendationsRequestState = similarRecommendationsRequestState
                        .markSimilarRecommendationsRequestCompleted(requestKey)
                    similarRecommendations = result.recommendations
                    similarRecommendationsSource = result.source
                    StashDebugLogBuffer.record(
                        "Player",
                        playerSimilarRecommendationsLoadedLogMessage(
                            sceneId = sceneId,
                            source = result.source,
                            count = result.recommendations.size,
                        ),
                    )
                    similarRecommendationsLoading = false
                }
                .onFailure { throwable ->
                    if (throwable is CancellationException) {
                        similarRecommendationsRequestState = similarRecommendationsRequestState
                            .markSimilarRecommendationsRequestCancelled(requestKey)
                        similarRecommendationsLoading = false
                        throw throwable
                    }
                    similarRecommendationsRequestState = similarRecommendationsRequestState
                        .markSimilarRecommendationsRequestCompleted(requestKey)
                    similarRecommendationsError = sanitizePlaybackErrorText(
                        throwable.message ?: throwable::class.simpleName ?: stashString(R.string.auto_kr_0473),
                    ) ?: stashString(R.string.auto_kr_0473)
                    StashDebugLogBuffer.record("Player", "Similar recommendations request failed", throwable)
                    similarRecommendationsLoading = false
                }
        } catch (cancellation: CancellationException) {
            similarRecommendationsRequestState = similarRecommendationsRequestState
                .markSimilarRecommendationsRequestCancelled(requestKey)
            similarRecommendationsLoading = false
            throw cancellation
        }
    }

    fun retryPlaybackAt(startPositionMs: Long) {
        controller.clearLastError()
        playbackStatus = PlayerPlaybackUiStatus.Loading
        playbackErrorText = null
        seekPreviewState = seekPreviewState.copy(pendingSeekTargetMs = null, pendingSeekStartedAtMs = 0L)
        controller.prepare(
            uri = activeCandidate.uri,
            title = stream.title,
            startPositionMs = startPositionMs.coerceAtLeast(0L),
            playWhenReady = true,
            requestHeaders = activeCandidate.requestHeaders,
        )
        controller.resumePlaybackIfDesired(true)
    }

    fun tryNextPlaybackSource(startPositionMs: Long) {
        when (
            val decision = PlayerStreamSelectionController.resolveFallback(
                activeCandidateIndex = activeCandidateIndex,
                candidateCount = resolvedCandidates.size,
                startPositionMs = startPositionMs,
            )
        ) {
            is PlayerStreamFallbackDecision.TryNext -> {
                controller.clearLastError()
                activeCandidateIndex = decision.nextIndex
                reprepareStartPositionMs = decision.startPositionMs
                prepareRequestKey += 1
                seekPreviewState = seekPreviewState.copy(pendingSeekTargetMs = null, pendingSeekStartedAtMs = 0L)
                playbackStatus = PlayerPlaybackUiStatus.Loading
                playbackErrorText = null
                hudText = decision.hudText
            }
            is PlayerStreamFallbackDecision.RetryCurrent -> retryPlaybackAt(decision.startPositionMs)
        }
    }

    fun requestResumeActivitySave(reason: PlayerResumeSaveReason) {
        val nowMs = System.currentTimeMillis()
        val currentPositionMs = controller.player.currentPosition.coerceAtLeast(0L)
        val currentDurationMs = controller.player.duration.takeIf { it > 0L } ?: durationMs
        val decision = PlayerResumeSyncPolicy.resolveResumeSave(
            state = resumeSaveState,
            reason = reason,
            playbackPrepared = playbackPrepared,
            isPlaying = controller.player.isPlaying,
            positionMs = currentPositionMs,
            durationMs = currentDurationMs,
            accumulatedPlaySeconds = accumulatedPlaySeconds.toDouble(),
            nowMs = nowMs,
        )
        if (!decision.shouldSave) return

        resumeSaveState = decision.state
        val savePayload = decision.payload ?: return
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { client.saveActivity(sceneId, savePayload.resumeTimeSeconds, savePayload.playDurationSeconds) }
        }
    }

    DisposableEffect(controller, sceneId) {
        val autoAdvanceArmListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    autoAdvanceArmed = true
                }
            }

            override fun onCues(cueGroup: CueGroup) {
                subtitleCueText = cueGroup.cues
                    .mapNotNull { cue -> cue.text?.toString()?.trim()?.takeIf { it.isNotBlank() } }
                    .joinToString("\n")
                    .takeIf { it.isNotBlank() }
            }
        }
        controller.player.addListener(autoAdvanceArmListener)
        onDispose {
            controller.player.removeListener(autoAdvanceArmListener)
            requestResumeActivitySave(PlayerResumeSaveReason.Final)
            controller.release()
        }
    }

    DisposableEffect(lifecycleOwner, controller, sceneId, backgroundPlaybackEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                requestResumeActivitySave(PlayerResumeSaveReason.Final)
            }
            if (
                event == Lifecycle.Event.ON_STOP &&
                shouldPausePlayerForLifecycleStop(backgroundPlaybackEnabled) &&
                activity?.isInPictureInPictureMode != true
            ) {
                controller.player.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(stream, resumePromptVisible) {
        if (resumePromptVisible && resumeStartPositionMs == null) {
            delay(RESUME_PROMPT_TIMEOUT_MS)
            resumeStartPositionMs = resolvedResumeStartPositionMs
            resumePromptVisible = false
        }
    }

    LaunchedEffect(activeCandidateKey, prepareRequestKey, resumeStartPositionMs) {
        val startPositionMs = PlayerStreamSelectionController.resolvePrepareStartPosition(
            reprepareStartPositionMs = reprepareStartPositionMs,
            playbackPrepared = playbackPrepared,
            resumeStartPositionMs = resumeStartPositionMs,
            currentPositionMs = controller.player.currentPosition,
        ) ?: return@LaunchedEffect
        playbackStatus = PlayerPlaybackUiStatus.Loading
        playbackErrorText = null
        controller.clearLastError()
        controller.prepare(
            uri = activeCandidate.uri,
            title = stream.title,
            startPositionMs = startPositionMs,
            playWhenReady = true,
            requestHeaders = activeCandidate.requestHeaders,
            captionTracks = stream.captionTracks,
        )
        playbackPrepared = true
        reprepareStartPositionMs = null
        hudText = PlayerStreamSelectionController.activeSourceHudText(
            sourceLabel = activeCandidate.sourceLabel,
            sourceTypeLabel = activeCandidate.sourceType.displayName,
        )
        markPlayerInteraction()
    }

    LaunchedEffect(controller) {
        while (true) {
            positionMs = controller.player.currentPosition.coerceAtLeast(0L)
            durationMs = controller.player.duration.takeIf { it > 0L } ?: 0L
            isPlaying = controller.player.isPlaying
            val lastError = controller.lastError
            playbackErrorText = sanitizePlaybackErrorText(lastError?.message ?: lastError?.errorCodeName)
            if (
                shouldAutoFallbackPlaybackSource(
                    hasPlaybackError = lastError != null,
                    hasFallbackCandidate = activeCandidateIndex < resolvedCandidates.lastIndex,
                )
            ) {
                tryNextPlaybackSource(positionMs)
                delay(500L)
                continue
            }
            playbackStatus = when {
                lastError != null -> PlayerPlaybackUiStatus.Error
                controller.player.playbackState == Player.STATE_BUFFERING -> PlayerPlaybackUiStatus.Buffering
                controller.player.playbackState == Player.STATE_ENDED -> PlayerPlaybackUiStatus.Ended
                controller.player.playbackState == Player.STATE_READY -> PlayerPlaybackUiStatus.Ready
                else -> PlayerPlaybackUiStatus.Loading
            }
            if (isPlaying) {
                autoAdvanceArmed = true
                accumulatedPlaySeconds += 0.5f
            }
            val pendingSeekUpdate = PlayerSeekPreviewController.resolvePendingSeekWatch(
                state = seekPreviewState,
                currentPositionMs = positionMs,
                hasFallbackCandidate = activeCandidateIndex < resolvedCandidates.lastIndex,
                nowMs = System.currentTimeMillis(),
            )
            seekPreviewState = pendingSeekUpdate.state
            when (val action = pendingSeekUpdate.action) {
                is PlayerPendingSeekAction.Fallback -> tryNextPlaybackSource(action.targetPositionMs)
                PlayerPendingSeekAction.Clear,
                PlayerPendingSeekAction.Keep -> Unit
            }
            delay(500L)
        }
    }

    LaunchedEffect(sceneId, controller) {
        while (true) {
            delay(15_000L)
            requestResumeActivitySave(PlayerResumeSaveReason.Periodic)
            if (!playCountSynced) {
                val nowMs = System.currentTimeMillis()
                val shouldCallAddPlay = PlayerSceneAddPlaySessionGuard.reserve(
                    sceneId = sceneId,
                    accumulatedPlaySeconds = accumulatedPlaySeconds.toDouble(),
                    nowMs = nowMs,
                )
                if (shouldCallAddPlay) {
                    runCatching { client.addPlay(sceneId) }
                        .onSuccess {
                            PlayerSceneAddPlaySessionGuard.markSucceeded(sceneId, System.currentTimeMillis())
                            playCountSynced = true
                        }
                        .onFailure {
                            PlayerSceneAddPlaySessionGuard.markFailed(sceneId)
                        }
                }
            }
        }
    }

    LaunchedEffect(hudText) {
        if (hudText != null) {
            delay(1_200L)
            hudText = null
        }
    }

    LaunchedEffect(sideControlOverlayState?.updatedAtMs, sideControlOverlayDragging) {
        val state = sideControlOverlayState ?: return@LaunchedEffect
        if (sideControlOverlayDragging) return@LaunchedEffect
        delay(PLAYER_SIDE_CONTROL_OVERLAY_AUTO_HIDE_MS)
        if (sideControlOverlayState?.updatedAtMs == state.updatedAtMs && !sideControlOverlayDragging) {
            sideControlOverlayVisible = false
            delay(PLAYER_SIDE_CONTROL_OVERLAY_FADE_MS.toLong())
            if (sideControlOverlayState?.updatedAtMs == state.updatedAtMs && !sideControlOverlayDragging) {
                sideControlOverlayState = null
            }
        }
    }

    LaunchedEffect(
        controlsVisible,
        locked,
        isPlaying,
        seekPreview,
        hudText,
        lastControlInteractionAtMs,
    ) {
        delay(PLAYER_CONTROLS_AUTO_HIDE_MS)
        val elapsedSinceInteractionMs = System.currentTimeMillis() - lastControlInteractionAtMs
        if (
            shouldAutoHidePlayerControls(
                controlsVisible = controlsVisible,
                locked = locked,
                isPlaying = isPlaying,
                hasTransientOverlay = seekPreview != null || hudText != null,
                elapsedSinceInteractionMs = elapsedSinceInteractionMs,
            )
        ) {
            controlsVisible = false
        }
    }

    suspend fun loadRecommendationsForEndedAutoAdvance(): List<SimilarSceneRecommendation> {
        if (similarRecommendations.isNotEmpty()) return similarRecommendations
        val requestKey = PlayerSimilarRecommendationsRequestKey(
            sceneId = sceneId.trim(),
            retryKey = similarRecommendationsRetryKey,
        )
        if (
            !shouldRequestSimilarRecommendationsForWatchPage(
                sceneId = sceneId,
                watchPageVisible = true,
                requestState = similarRecommendationsRequestState,
                retryKey = similarRecommendationsRetryKey,
            )
        ) {
            return emptyList()
        }
        similarRecommendationsRequestState = similarRecommendationsRequestState
            .markSimilarRecommendationsRequestStarted(requestKey)
        similarRecommendationsLoading = true
        similarRecommendationsError = null
        return similarScenesRepository.getSimilarScenesWithSource(sceneId, limit = 10)
            .onSuccess { result ->
                similarRecommendationsRequestState = similarRecommendationsRequestState
                    .markSimilarRecommendationsRequestCompleted(requestKey)
                similarRecommendations = result.recommendations
                similarRecommendationsSource = result.source
                similarRecommendationsError = null
                similarRecommendationsLoading = false
            }
            .onFailure { throwable ->
                if (throwable is CancellationException) {
                    similarRecommendationsRequestState = similarRecommendationsRequestState
                        .markSimilarRecommendationsRequestCancelled(requestKey)
                    similarRecommendationsLoading = false
                    throw throwable
                }
                similarRecommendationsRequestState = similarRecommendationsRequestState
                    .markSimilarRecommendationsRequestCompleted(requestKey)
                similarRecommendationsError = sanitizePlaybackErrorText(
                    throwable.message ?: throwable::class.simpleName ?: stashString(R.string.auto_kr_0473),
                ) ?: stashString(R.string.auto_kr_0473)
                similarRecommendationsLoading = false
            }
            .getOrNull()
            ?.recommendations
            .orEmpty()
    }

    LaunchedEffect(playbackStatus, playbackEndAction, sceneId, autoAdvanceArmed) {
        if (playbackStatus != PlayerPlaybackUiStatus.Ended) {
            repeatedFromSceneId = null
            return@LaunchedEffect
        }
        if (
            PlayerTransportController.shouldRepeatOnEnded(
                playbackStatus = playbackStatus,
                playbackEndAction = playbackEndAction,
                currentSceneId = sceneId,
                repeatedFromSceneId = repeatedFromSceneId,
                autoAdvanceArmed = autoAdvanceArmed,
            )
        ) {
            repeatedFromSceneId = sceneId
            controller.seekTo(0L, resumePlayback = true)
            playbackStatus = PlayerPlaybackUiStatus.Loading
        }
    }

    LaunchedEffect(playbackStatus, playbackQueue, sceneId, similarRecommendations, playbackEndAction) {
        if (!PlayerTransportController.shouldAutoAdvanceOnEnded(playbackEndAction)) {
            return@LaunchedEffect
        }
        when (
            val decision = PlayerTransportController.resolveEndedAutoAdvance(
                playbackStatus = playbackStatus,
                currentSceneId = sceneId,
                autoAdvancedFromSceneId = autoAdvancedFromSceneId,
                queue = playbackQueue,
                autoAdvanceArmed = autoAdvanceArmed,
            )
        ) {
            is PlayerAutoAdvanceDecision.Advance -> {
                autoAdvancedFromSceneId = decision.autoAdvancedFromSceneId
                onPlaybackQueueChange(decision.queue)
                onOpenScene(decision.nextSceneId)
                return@LaunchedEffect
            }
            PlayerAutoAdvanceDecision.None -> Unit
        }

        if (playbackStatus != PlayerPlaybackUiStatus.Ended || autoAdvancedFromSceneId == sceneId || !autoAdvanceArmed) {
            return@LaunchedEffect
        }
        val queueAtTail = PlayerTransportController.updateCurrentScene(playbackQueue, sceneId)
        if (queueAtTail.currentSceneId != sceneId || queueAtTail.nextSceneId() != null) {
            return@LaunchedEffect
        }

        val recommendationCandidates = loadRecommendationsForEndedAutoAdvance()
        when (
            val decision = PlayerTransportController.resolveEndedRecommendedAutoAdvance(
                playbackStatus = playbackStatus,
                currentSceneId = sceneId,
                autoAdvancedFromSceneId = autoAdvancedFromSceneId,
                queue = playbackQueue,
                recommendations = recommendationCandidates,
                autoAdvanceArmed = autoAdvanceArmed,
            )
        ) {
            is PlayerAutoAdvanceDecision.Advance -> {
                autoAdvancedFromSceneId = decision.autoAdvancedFromSceneId
                onPlaybackQueueChange(decision.queue)
                onOpenScene(decision.nextSceneId)
            }
            PlayerAutoAdvanceDecision.None -> Unit
        }
    }

    val previewFrameFor: (Long) -> gomeng.dev.stashplayer.core.network.StashSpriteFrame? = remember(stream.spriteFrames) {
        { targetPositionMs -> findStashSpriteAtTime(stream.spriteFrames, targetPositionMs / 1000.0) }
    }

    val seekTo: (Long) -> Unit = { targetPositionMs ->
        markPlayerInteraction()
        val update = PlayerSeekPreviewController.releaseSeek(
            state = seekPreviewState,
            targetPositionMs = targetPositionMs,
            durationMs = durationMs,
            wasPlaying = controller.player.isPlaying,
            playWhenReady = controller.player.playWhenReady,
            isBuffering = controller.player.playbackState == Player.STATE_BUFFERING,
            nowMs = System.currentTimeMillis(),
        )
        seekPreviewState = update.state
        resumeSaveState = PlayerResumeSyncPolicy.markSeekForResumeSave(resumeSaveState, update.markResumeSaveAtMs)
        update.seekRequest?.let { request ->
            controller.seekTo(request.targetPositionMs, resumePlayback = request.resumePlayback)
        } ?: controller.resumePlaybackIfDesired(update.resumeWithoutSeek)
        positionMs = update.displayPositionMs
    }
    val seekBy: (Long) -> Unit = { deltaMs ->
        val basePositionMs = controller.player.currentPosition.coerceAtLeast(0L)
        seekTo(basePositionMs + deltaMs)
    }
    val updateSeekPreview: (PlayerSeekPreview?) -> Unit = { preview ->
        if (preview != null) {
            markPlayerInteraction()
        }
        val update = PlayerSeekPreviewController.updatePreview(
            state = seekPreviewState,
            preview = preview,
            durationMs = durationMs,
            wasPlaying = controller.player.isPlaying,
            playWhenReady = controller.player.playWhenReady,
            isBuffering = controller.player.playbackState == Player.STATE_BUFFERING,
            nowMs = System.currentTimeMillis(),
        )
        seekPreviewState = update.state
        update.seekRequest?.let { request ->
            if (update.holdPlayback) {
                controller.holdPlaybackForSeekPreview()
            }
            controller.seekTo(request.targetPositionMs, resumePlayback = request.resumePlayback)
        }
        update.markResumeSaveAtMs?.let { seekAtMs ->
            resumeSaveState = PlayerResumeSyncPolicy.markSeekForResumeSave(resumeSaveState, seekAtMs)
        }
        update.displayPositionMs?.let { targetPositionMs ->
            positionMs = targetPositionMs
        }
    }
    val selectRatingStep: (Int) -> Unit = selectRatingStep@{ ratingStep ->
        markPlayerInteraction()
        controlsVisible = true
        if (ratingStep == ratingState.ratingStep && !ratingState.isUpdating) {
            return@selectRatingStep
        }
        val requestId = ratingSaveRequestId + 1L
        ratingSaveRequestId = requestId
        val optimisticState = ratingState.optimisticallySelectRatingStep(ratingStep)
        ratingState = optimisticState
        scope.launch {
            runCatching { client.updateSceneRating(sceneId, optimisticState.rating100) }
                .onSuccess { saved ->
                    if (ratingSaveRequestId == requestId) {
                        ratingState = if (saved) {
                            optimisticState.completeUpdate()
                        } else {
                            optimisticState.failUpdate(stashString(R.string.auto_kr_0474))
                        }
                    }
                }
                .onFailure { throwable ->
                    if (ratingSaveRequestId == requestId) {
                        ratingState = optimisticState.failUpdate(throwable.message ?: throwable::class.simpleName)
                    }
                }
        }
    }
    val incrementOCounter: () -> Unit = {
        if (!oCounterUpdating) {
            markPlayerInteraction()
            controlsVisible = true
            val previousCount = oCounter
            val optimisticCount = previousCount + 1
            oCounter = optimisticCount
            oCounterUpdating = true
            scope.launch {
                runCatching { client.addO(sceneId) }
                    .onSuccess { savedCount ->
                        oCounter = savedCount
                        hudText = stashString(R.string.player_o_counter_saved, savedCount)
                    }
                    .onFailure { throwable ->
                        oCounter = previousCount
                        hudText = stashString(
                            R.string.player_o_counter_save_failed,
                            throwable.message ?: throwable::class.simpleName.orEmpty(),
                        )
                    }
                oCounterUpdating = false
            }
        }
    }
    val startFastPlaybackHold: () -> Unit = {
        markPlayerInteraction()
        val update = fastPlaybackHoldState.start(
            currentSpeed = playbackSpeed,
            locked = locked,
            speedPreference = fastPlaybackHoldSpeed,
        )
        fastPlaybackHoldState = update.state
        update.playbackSpeed?.let { speed ->
            playbackSpeed = speed
            controller.setPlaybackSpeed(speed)
        }
        hudText = update.hudText
    }
    val endFastPlaybackHold: () -> Unit = {
        markPlayerInteraction()
        val update = fastPlaybackHoldState.release()
        fastPlaybackHoldState = update.state
        update.playbackSpeed?.let { speed ->
            playbackSpeed = speed
            controller.setPlaybackSpeed(speed)
        }
        hudText = update.hudText
    }
    val streamSourceOptions = buildPlayerStreamSourceOptions(
        candidates = resolvedCandidates.map { candidate ->
            PlayerStreamSourceCandidateLabel(
                sourceLabel = candidate.sourceLabel,
                sourceTypeLabel = candidate.sourceType.displayName,
                sourceCategoryLabel = candidate.sourceCategory.displayName,
                mimeType = candidate.mimeType,
                urlExtensionHint = candidate.urlExtensionHint,
                isHlsManifest = candidate.isHlsManifest,
            )
        },
        selectedIndex = activeCandidateIndex,
    )
    val streamPreferenceOptions = buildPlayerStreamPreferenceOptions(
        selectedPreferenceId = streamPreference.id,
        canChooseDirect = resolvedCandidates.any { it.sourceCategory == StashStreamSourceCategory.Direct },
        canChooseHls = resolvedCandidates.any {
            it.sourceCategory == StashStreamSourceCategory.Hls || it.sourceCategory == StashStreamSourceCategory.Transcode
        },
    )
    val currentStreamInfoText = playerDebugOverlayText(
        enabled = playerDebugOverlayEnabled,
        option = streamSourceOptions.getOrNull(
            PlayerStreamSelectionController.coerceCandidateIndex(activeCandidateIndex, streamSourceOptions.size),
        ),
    )
    val debugInfoUiState = buildPlayerDebugInfoUiState(
        pathOrUrl = stream.path ?: activeCandidate.uri.toString(),
        streamSourceLabel = activeCandidate.sourceLabel,
        streamSourceTypeLabel = activeCandidate.sourceType.displayName,
        streamSourceCategoryLabel = activeCandidate.sourceCategory.displayName,
        thumbnailUrl = stream.thumbnailUrl,
        spriteVttUrl = stream.spriteVttUrl,
        spriteImageUrl = stream.spriteImageUrl,
        spriteFrameCount = stream.spriteFrames.size,
        activeCandidateIndex = activeCandidateIndex,
        resolvedCandidateCount = resolvedCandidates.size,
        rawCandidateCount = stream.streamCandidates.size,
        recommendationSourceLabel = when (similarRecommendationsSource) {
            SimilarVideosRecommendationSource.HybridBackend -> stashString(R.string.auto_kr_0034)
            SimilarVideosRecommendationSource.GraphQlFallback -> stashString(R.string.auto_kr_0033)
        },
    )
    val playlistItems = if (canShowPlayerPlaylistAction(playbackQueue)) {
        buildPlayerPlaylistUiItems(playbackQueue.withCurrent(sceneId))
    } else {
        emptyList()
    }
    val playlistDrawerPresentationPolicy = resolvePlayerPlaylistDrawerPresentationPolicy(playlistDrawerOpen)
    val playerGesturesSuspended = playerGesturesSuspendedByModalSurface ||
        playlistDrawerPresentationPolicy.suspendsPlayerGestures
    BackHandler(enabled = playlistDrawerOpen) {
        playlistDrawerOpen = false
    }
    val selectPlaylistScene: (String) -> Unit = { selectedSceneId ->
        val updatedQueue = PlayerTransportController.updateCurrentScene(playbackQueue.withCurrent(sceneId), selectedSceneId)
        markPlayerInteraction()
        controlsVisible = true
        onPlaybackQueueChange(updatedQueue)
        onOpenScene(updatedQueue.currentSceneId ?: selectedSceneId)
    }
    val reorderPlaylistScene: (String, Int) -> Unit = { reorderedSceneId, toIndex ->
        val currentQueue = playbackQueue.withCurrent(sceneId)
        val updatedQueue = reorderPlayerPlaylistItem(currentQueue, sceneId = reorderedSceneId, toIndex = toIndex)
        if (updatedQueue != currentQueue) {
            markPlayerInteraction()
            controlsVisible = true
            onPlaybackQueueChange(updatedQueue)
            hudText = stashString(R.string.auto_kr_0476)
        }
    }
    val removePlaylistScene: (String) -> Unit = { removedSceneId ->
        val currentQueue = playbackQueue.withCurrent(sceneId)
        val updatedQueue = removePlayerPlaylistItem(currentQueue, removedSceneId)
        markPlayerInteraction()
        controlsVisible = true
        if (updatedQueue != currentQueue) {
            onPlaybackQueueChange(updatedQueue)
            hudText = stashString(R.string.auto_kr_0477)
        } else {
            hudText = stashString(R.string.auto_kr_0478)
        }
    }
    val selectStreamSource: (Int) -> Unit = { selectedIndex ->
        val decision = PlayerStreamSelectionController.selectSource(
            selectedIndex = selectedIndex,
            activeCandidateIndex = activeCandidateIndex,
            candidateCount = resolvedCandidates.size,
            currentPositionMs = controller.player.currentPosition,
            title = streamSourceOptions.getOrNull(
                PlayerStreamSelectionController.coerceCandidateIndex(selectedIndex, streamSourceOptions.size),
            )?.title ?: resolvedCandidates[
                PlayerStreamSelectionController.coerceCandidateIndex(selectedIndex, resolvedCandidates.size),
            ].sourceType.displayName,
        )
        markPlayerInteraction()
        controlsVisible = true
        if (decision.shouldReprepare) {
            activeCandidateIndex = decision.selectedIndex
            reprepareStartPositionMs = decision.reprepareStartPositionMs
            prepareRequestKey += 1
            if (decision.shouldClearPendingSeek) {
                seekPreviewState = seekPreviewState.copy(pendingSeekTargetMs = null, pendingSeekStartedAtMs = 0L)
            }
            playbackStatus = PlayerPlaybackUiStatus.Loading
            playbackErrorText = null
        }
        hudText = decision.hudText
    }
    val selectStreamPreference: (String) -> Unit = { preferenceId ->
        val nextPreference = StashStreamPreference.entries.firstOrNull { it.id == preferenceId }
            ?: StashStreamPreference.Auto
        val nextResolvedCandidates = PlayerStreamSelectionController.orderResolvedCandidatesForPreference(
            resolvedCandidates = baseResolvedCandidates,
            streamCandidates = stream.streamCandidates,
            preference = nextPreference,
        )
        val decision = PlayerStreamSelectionController.selectPreferenceFromOrderedCandidates(
            preferenceId = preferenceId,
            activeCandidateKey = activeCandidateKey,
            preferredCandidateKey = PlayerStreamSelectionController.candidateKey(nextResolvedCandidates.firstOrNull()),
            currentPositionMs = controller.player.currentPosition,
        )
        markPlayerInteraction()
        controlsVisible = true
        streamPreference = decision.preference
        activeCandidateIndex = decision.selectedIndex
        if (decision.shouldReprepare) {
            reprepareStartPositionMs = decision.reprepareStartPositionMs
            prepareRequestKey += 1
            if (decision.shouldClearPendingSeek) {
                seekPreviewState = seekPreviewState.copy(pendingSeekTargetMs = null, pendingSeekStartedAtMs = 0L)
            }
            playbackStatus = PlayerPlaybackUiStatus.Loading
            playbackErrorText = null
        }
        hudText = decision.hudText
    }
    val transportQueue = playbackQueue.withCurrent(sceneId)
    val infoDrawerDurationMs = durationMs.takeIf { it > 0L }
        ?: ((stream.durationSeconds ?: 0.0) * 1000.0).toLong().coerceAtLeast(0L)
    val infoDrawerContentState = buildPlayerInfoDrawerContentState(
        title = stream.title,
        fileName = stream.fileName,
        path = stream.path,
        tagChips = stream.tags,
        studioName = stream.studioName,
        playCount = stream.playCount,
        width = stream.width,
        height = stream.height,
        durationMs = infoDrawerDurationMs,
        rating100 = ratingState.rating100,
        hasSimilarVideosSurface = true,
    )
    fun similarScenePayload(selectedSceneId: String): SimilarSceneRecommendation? {
        return similarRecommendations.firstOrNull { recommendation ->
            recommendation.sceneId == selectedSceneId || recommendation.scene.id == selectedSceneId
        }
    }
    val addSimilarSceneToQueue: (String) -> Unit = { selectedSceneId ->
        val recommendation = similarScenePayload(selectedSceneId)
        val sceneTitle = recommendation?.scene?.title?.takeIf { it.isNotBlank() }
            ?: recommendation?.scene?.fileName
            ?: selectedSceneId
        val thumbnailUrl = recommendation?.scene?.thumbnailUrl ?: recommendation?.scene?.spriteImageUrl
        val queueSizeBefore = playbackQueue.items.size
        val updatedQueue = appendSimilarSceneToPlaybackQueue(
            queue = playbackQueue,
            currentSceneId = sceneId,
            sceneId = selectedSceneId,
            title = sceneTitle,
            thumbnailUrl = thumbnailUrl,
        )
        StashDebugLogBuffer.record(
            "Player",
            playerSimilarRecommendationQueueLogMessage(
                currentSceneId = sceneId,
                selectedSceneId = selectedSceneId,
                source = similarRecommendationsSource,
                recommendationFound = recommendation != null,
                thumbnailAvailable = !thumbnailUrl.isNullOrBlank(),
                queueSizeBefore = queueSizeBefore,
                queueSizeAfter = updatedQueue.items.size,
            ),
        )
        markPlayerInteraction()
        controlsVisible = true
        onPlaybackQueueChange(updatedQueue)
        hudText = if (updatedQueue.items.any { it.sceneId == selectedSceneId }) stashString(R.string.auto_kr_0227) else stashString(R.string.auto_kr_0479)
    }
    val playSimilarScene: (String) -> Unit = { selectedSceneId ->
        val recommendation = similarScenePayload(selectedSceneId)
        val sceneTitle = recommendation?.scene?.title?.takeIf { it.isNotBlank() }
            ?: recommendation?.scene?.fileName
            ?: selectedSceneId
        val thumbnailUrl = recommendation?.scene?.thumbnailUrl ?: recommendation?.scene?.spriteImageUrl
        val queueSizeBefore = playbackQueue.items.size
        val updatedQueue = selectSimilarSceneForPlayback(
            queue = playbackQueue,
            currentSceneId = sceneId,
            sceneId = selectedSceneId,
            title = sceneTitle,
            thumbnailUrl = thumbnailUrl,
        )
        val targetSceneId = updatedQueue.currentSceneId ?: selectedSceneId
        StashDebugLogBuffer.record(
            "Player",
            playerSimilarRecommendationClickLogMessage(
                currentSceneId = sceneId,
                selectedSceneId = selectedSceneId,
                source = similarRecommendationsSource,
                recommendationFound = recommendation != null,
                thumbnailAvailable = !thumbnailUrl.isNullOrBlank(),
                queueSizeBefore = queueSizeBefore,
                queueSizeAfter = updatedQueue.items.size,
                targetSceneId = targetSceneId,
            ),
        )
        markPlayerInteraction()
        controlsVisible = true
        onPlaybackQueueChange(updatedQueue)
        onOpenScene(targetSceneId)
    }
    val previousTransportAction = PlayerTransportController.resolvePreviousAction(
        currentPositionMs = positionMs,
        previousSceneId = transportQueue.previousSceneId(),
    )
    val nextTransportAction = PlayerTransportController.resolveNextAction(transportQueue.nextSceneId())
    val previousPictureInPictureSceneId = transportQueue.previousSceneId()
    fun openTransportScene(targetSceneId: String) {
        val updatedQueue = PlayerTransportController.updateCurrentScene(transportQueue, targetSceneId)
        markPlayerInteraction()
        controlsVisible = true
        onPlaybackQueueChange(updatedQueue)
        onOpenScene(updatedQueue.currentSceneId ?: targetSceneId)
    }
    val handlePreviousTransport: () -> Unit = {
        when (val action = previousTransportAction) {
            is PlayerPreviousAction.OpenPrevious -> openTransportScene(action.sceneId)
            PlayerPreviousAction.RestartCurrent -> {
                markPlayerInteraction()
                controlsVisible = true
                seekTo(0L)
                hudText = stashString(R.string.auto_kr_0481)
            }
        }
    }
    val handleNextTransport: () -> Unit = {
        when (val action = nextTransportAction) {
            is PlayerNextAction.OpenNext -> openTransportScene(action.sceneId)
            PlayerNextAction.Unavailable -> {
                markPlayerInteraction()
                controlsVisible = true
                hudText = stashString(R.string.auto_kr_0226)
            }
        }
    }
    val handlePreviousPictureInPicture: () -> Unit = {
        previousPictureInPictureSceneId?.let(::openTransportScene)
    }

    val watchPageContentState = PlayerWatchPageController.buildSceneWatchPageContentState(
        title = stream.title,
        tagChips = stream.tags,
        studioName = stream.studioName,
        playCount = stream.playCount,
        width = stream.width,
        height = stream.height,
        durationMs = infoDrawerDurationMs,
        rating100 = ratingState.rating100,
        hasSimilarScenes = PlayerWatchPageController.shouldShowSceneWatchPageSimilarSection(
            recommendationCount = similarRecommendations.size,
            isLoading = similarRecommendationsLoading,
            errorMessage = similarRecommendationsError,
        ),
    )
    val watchPageActionItems = PlayerWatchPageController.buildSceneWatchPageActionRowItems(
        ratingStep = ratingState.ratingStep,
        ratingUpdating = ratingState.isUpdating,
        isQueued = sceneId in queueSceneIds,
        isFavorite = sceneId in favoriteSceneIds,
        isInWatchLater = sceneId in watchLaterSceneIds,
        oCounter = oCounter,
        oCounterUpdating = oCounterUpdating,
        ratingMessage = ratingState.message,
    )
    val watchPageDebugEntry = PlayerWatchPageController.buildSceneWatchPageDebugEntry(enabled = true)
    val pictureInPictureSupported = StashPictureInPictureController.isSupported(activity)
    val canEnterPictureInPicture = shouldExposePictureInPictureButton(
        pictureInPictureEnabled = pictureInPictureEnabled,
        pictureInPictureSupported = pictureInPictureSupported,
    )
    val pictureInPictureRequest = remember(
        pictureInPictureEnabled,
        playbackStatus,
        isPlaying,
        locked,
        playerGesturesSuspended,
        previousPictureInPictureSceneId,
        nextTransportAction,
        stream.width,
        stream.height,
    ) {
        StashPictureInPictureRequest(
            enabled = pictureInPictureEnabled,
            playbackReady = playbackStatus == PlayerPlaybackUiStatus.Ready || playbackStatus == PlayerPlaybackUiStatus.Buffering,
            isPlaying = isPlaying,
            locked = locked,
            modalSurfaceOpen = playerGesturesSuspended,
            canPlayPrevious = previousPictureInPictureSceneId != null,
            canPlayNext = nextTransportAction is PlayerNextAction.OpenNext,
            aspectRatio = resolvePlayerPictureInPictureAspectRatio(stream.width, stream.height).toAndroidRational(),
        )
    }

    DisposableEffect(pictureInPictureRequest, activity) {
        StashPictureInPictureController.register(pictureInPictureRequest, activity)
        onDispose {
            StashPictureInPictureController.unregister(pictureInPictureRequest)
        }
    }

    DisposableEffect(controller, previousPictureInPictureSceneId, nextTransportAction) {
        val handler = object : StashPictureInPictureActionHandler {
            override fun onPlayPause() {
                controller.playPause()
            }

            override fun onPrevious() {
                handlePreviousPictureInPicture()
            }

            override fun onNext() {
                handleNextTransport()
            }
        }
        StashPictureInPictureController.registerActionHandler(handler)
        onDispose {
            StashPictureInPictureController.unregisterActionHandler(handler)
        }
    }

    val overlayState = PlayerOverlayState(
        title = buildPlayerOverlayTitle(
            streamTitle = stream.title,
            sourceLabel = streamSourceOptions.getOrNull(
                PlayerStreamSelectionController.coerceCandidateIndex(activeCandidateIndex, streamSourceOptions.size),
            )?.title ?: PlayerStreamSelectionController.activeSourceHudText(
                sourceLabel = activeCandidate.sourceLabel,
                sourceTypeLabel = activeCandidate.sourceType.displayName,
            ),
            isFoldLikeLayout = isFoldLikeLayout,
        ),
        controlsVisible = controlsVisible,
        locked = locked,
        isPlaying = isPlaying,
        positionMs = positionMs,
        durationMs = durationMs,
        playbackSpeed = playbackSpeed,
        playbackOrientationMode = playbackOrientationMode,
        aspectRatioMode = aspectRatioMode,
        hudText = hudText,
        seekPreview = seekPreview,
        playbackStatus = playbackStatus,
        playbackErrorText = playbackErrorText,
        canTryAlternateSource = activeCandidateIndex < resolvedCandidates.lastIndex,
        canOpenSettings = true,
        canOpenNextScene = nextTransportAction is PlayerNextAction.OpenNext,
        canEnterPictureInPicture = canEnterPictureInPicture,
        canShuffleQueue = playbackQueue.hasQueue,
        shuffleEnabled = playbackQueue.shuffleEnabled,
        ratingStep = ratingState.ratingStep,
        ratingMessage = ratingState.message,
        ratingUpdating = ratingState.isUpdating,
        currentStreamInfoText = currentStreamInfoText,
        quickActions = quickActions,
        fullscreenPlayerActive = fullscreenChromeActive,
        sceneId = sceneId,
        infoDrawerContentState = infoDrawerContentState,
        debugInfoUiState = debugInfoUiState,
        similarRecommendations = similarRecommendations,
        similarRecommendationsLoading = similarRecommendationsLoading,
        similarRecommendationsError = similarRecommendationsError,
        similarRecommendationsSource = similarRecommendationsSource,
        serverProfile = profile,
        streamPreferenceOptions = streamPreferenceOptions,
        streamSourceOptions = streamSourceOptions,
        playlistItems = playlistItems,
        infoDrawerState = infoDrawerState,
        infoDrawerLayout = infoDrawerLayout,
        previewFrameFor = previewFrameFor,
    )
    val overlayCallbacks = PlayerOverlayCallbacks(
        onSeekPreview = updateSeekPreview,
        onExitPlayer = {
            if (fullscreenPlayerActive) {
                exitFullscreenToWatchPage()
            } else {
                onExitPlayer()
            }
        },
        onPlayPause = {
            markPlayerInteraction()
            controlsVisible = true
            controller.playPause()
        },
        onSeekTo = seekTo,
        onPreviousTransport = handlePreviousTransport,
        onNextTransport = handleNextTransport,
        onToggleLock = {
            markPlayerInteraction()
            val toggleResult = togglePlayerLockState(locked)
            locked = toggleResult.locked
            hudText = toggleResult.hudText
            controlsVisible = toggleResult.controlsVisible
        },
        onToggleFullscreenPlayer = {
            markPlayerInteraction()
            controlsVisible = true
            val nextMode = if (fullscreenPlayerActive) {
                PlayerPresentationMode.WatchPage
            } else {
                PlayerPresentationMode.Fullscreen
            }
            presentationRouteState = presentationRouteState.withTargetMode(nextMode).state
        },
        onEnterPictureInPicture = {
            markPlayerInteraction()
            controlsVisible = false
            StashPictureInPictureController.enterIfEligible(activity)
        },
        onCycleSpeed = {
            markPlayerInteraction()
            controlsVisible = true
            playbackSpeed = when (playbackSpeed) {
                0.5f -> 1f
                1f -> 1.25f
                1.25f -> 1.5f
                1.5f -> 2f
                else -> 0.5f
            }
            controller.setPlaybackSpeed(playbackSpeed)
            hudText = playerPlaybackSpeedHudText(playbackSpeed)
        },
        onTogglePlaybackOrientationMode = {
            markPlayerInteraction()
            controlsVisible = true
            val nextMode = nextPlaybackOrientationMode(playbackOrientationMode)
            scope.launch {
                settingsRepository.setPlaybackOrientationMode(nextMode)
            }
            hudText = playerPlaybackOrientationHudText(nextMode)
        },
        onCycleAspectRatio = {
            markPlayerInteraction()
            controlsVisible = true
            aspectRatioMode = aspectRatioMode.next()
            hudText = playerAspectRatioHudText(aspectRatioMode)
        },
        onSelectPlaybackSpeed = { speed ->
            markPlayerInteraction()
            controlsVisible = true
            playbackSpeed = speed
            controller.setPlaybackSpeed(speed)
            hudText = playerPlaybackSpeedHudText(speed)
        },
        onSelectAspectRatioMode = { mode ->
            markPlayerInteraction()
            controlsVisible = true
            aspectRatioMode = mode
            hudText = playerAspectRatioHudText(mode)
        },
        onSelectShuffleEnabled = { enabled ->
            markPlayerInteraction()
            controlsVisible = true
            val updatedQueue = playbackQueue.withCurrent(sceneId).withShuffleEnabled(enabled)
            onPlaybackQueueChange(updatedQueue)
            hudText = if (enabled) stashString(R.string.auto_kr_0482) else stashString(R.string.auto_kr_0483)
        },
        onSelectRatingStep = selectRatingStep,
        onAddCurrentSceneToQueue = addCurrentSceneToQueue,
        onToggleFavorite = toggleFavorite,
        onToggleWatchLater = toggleWatchLater,
        onPlaySimilarScene = playSimilarScene,
        onAddSimilarSceneToQueue = addSimilarSceneToQueue,
        onRetrySimilarRecommendations = retrySimilarRecommendations,
        onSelectStreamPreference = selectStreamPreference,
        onSelectStreamSource = selectStreamSource,
        onToggleInfoDrawer = {
            markPlayerInteraction()
            controlsVisible = true
        },
        onInfoDrawerDrag = { _ ->
            markPlayerInteraction()
            controlsVisible = true
        },
        onInfoDrawerDragEnd = {
            markPlayerInteraction()
            controlsVisible = true
        },
        onOpenPlaylistDrawer = {
            playlistDrawerOpen = true
            scope.launch {
                onPlaylistDrawerOpen(sceneId, PLAYER_PLAYLIST_TRAILING_ITEM_COUNT)
            }
        },
        onRetryPlayback = {
            markPlayerInteraction()
            controlsVisible = true
            retryPlaybackAt(
                if (playbackStatus == PlayerPlaybackUiStatus.Ended) 0L else positionMs,
            )
        },
        onTryAlternateSource = {
            markPlayerInteraction()
            controlsVisible = true
            tryNextPlaybackSource(positionMs)
        },
        onOpenSettings = {
            markPlayerInteraction()
            controlsVisible = true
            onOpenSettings()
        },
        onBottomControlsGestureBoundsChanged = { bounds ->
            playerGestureExclusionBounds = bounds
        },
        onBottomControlsHeightChanged = { _ ->
            // Fullscreen chrome no longer measures or reveals an in-player drawer.
        },
        onPlayerGestureSuspendedByModalSurfaceChanged = { suspended ->
            playerGesturesSuspendedByModalSurface = suspended
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
        ) {
        PlayerSurface(
            controller = controller,
            aspectRatioMode = aspectRatioMode,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = infoDrawerLayout.videoScale * presentationMotionState.videoScale
                    scaleY = infoDrawerLayout.videoScale * presentationMotionState.videoScale
                    translationY = infoDrawerLayout.videoTranslateYPx + presentationMotionState.videoTranslationYPx
                    transformOrigin = TransformOrigin(0.5f, 0f)
                },
        )

        PlayerGestureLayer(
            locked = locked,
            currentPositionMs = positionMs,
            durationMs = durationMs,
            onToggleOverlay = {
                markPlayerInteraction()
                val nextControlsVisible = !controlsVisible
                controlsVisible = nextControlsVisible
            },
            onPlayPause = {
                markPlayerInteraction()
                controlsVisible = true
                controller.playPause()
            },
            onSeekBy = seekBy,
            onSeekTo = seekTo,
            brightnessFraction = { brightnessController?.currentFraction() ?: 0.5f },
            volumeFraction = { volumeController.currentFraction() },
            onBrightnessFraction = ::updateBrightnessFraction,
            onVolumeFraction = ::updateVolumeFraction,
            onHudText = {
                markPlayerInteraction()
                hudText = it
            },
            onSeekPreview = updateSeekPreview,
            fastPlaybackHoldEnabled = fastPlaybackHoldSpeed.enabled,
            onFastPlaybackHoldStart = startFastPlaybackHold,
            onFastPlaybackHoldEnd = endFastPlaybackHold,
            previewFrameFor = previewFrameFor,
            modifier = Modifier.fillMaxSize(),
            gestureExclusionBounds = playerGestureExclusionBounds,
            gesturesSuspendedByModalSurface = playerGesturesSuspended,
            presentationMode = playerSurfacePresentationMode,
            onPresentationDragUpdate = { update ->
                markPlayerInteraction()
                controlsVisible = true
                updatePresentationDrag(update)
            },
            onPresentationDragRelease = ::releasePresentationDrag,
        )

        if (!pictureInPictureActive) {
            PlayerSubtitleOverlay(
                cueText = subtitleCueText,
                fontScale = subtitleFontScale,
                position = subtitlePosition,
                alignment = subtitleTextAlignment,
            )
        }

        if (!pictureInPictureActive) {
            PlayerOverlay(
                state = overlayState,
                callbacks = overlayCallbacks,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = resolvePlayerPresentationOverlayAlpha(
                            motionState = presentationMotionState,
                            fullscreenPlayerActive = fullscreenPlayerActive,
                            presentationDragActive = presentationTransitionActive,
                        )
                        translationY = presentationMotionState.fullscreenChromeTranslationYPx
                    },
            )
        }

        if (!pictureInPictureActive) {
            sideControlOverlayState?.let { state ->
                PlayerSideControlSliderOverlay(
                    state = state,
                    visible = sideControlOverlayVisible,
                    onFractionChange = { fraction ->
                        markPlayerInteraction()
                        sideControlOverlayVisible = true
                        sideControlOverlayDragging = true
                        hudText = when (state.kind) {
                            PlayerSideControlKind.Brightness -> updateBrightnessFraction(fraction)
                            PlayerSideControlKind.Volume -> updateVolumeFraction(fraction)
                        }
                    },
                    onChangeFinished = {
                        sideControlOverlayDragging = false
                        sideControlOverlayState = sideControlOverlayState?.copy(
                            updatedAtMs = System.currentTimeMillis(),
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        SceneBulkDeleteConfirmationDialog(
            state = playlistDeleteConfirmation,
            onConfirmationChange = { playlistDeleteConfirmation = playlistDeleteConfirmation.withConfirmation(it) },
            onDeleteFileChange = { playlistDeleteConfirmation = playlistDeleteConfirmation.withDeleteFile(it) },
            onDeleteGeneratedChange = { playlistDeleteConfirmation = playlistDeleteConfirmation.withDeleteGenerated(it) },
            onCancel = {
                pendingPlaylistDeleteSceneIds = emptyList()
                playlistDeleteConfirmation = playlistDeleteConfirmation.dismiss()
            },
            onDelete = {
                val selectedIds = pendingPlaylistDeleteSceneIds
                if (selectedIds.isEmpty()) {
                    playlistDeleteConfirmation = playlistDeleteConfirmation.dismiss()
                    return@SceneBulkDeleteConfirmationDialog
                }
                val deleteOptions = playlistDeleteConfirmation.deleteOptions
                playlistDeleteConfirmation = playlistDeleteConfirmation.deleting()
                scope.launch {
                    val result = runCatching { client.deleteScenes(selectedIds, deleteOptions) }.getOrElse { throwable ->
                        SceneBulkDeleteResult(
                            requestedSceneIds = selectedIds,
                            deletedSceneIds = emptySet(),
                            failedSceneIds = selectedIds.associateWith {
                                redactStashCredentialText(throwable.message ?: stashString(R.string.auto_kr_0411))
                            },
                        )
                    }
                    if (result.deletedSceneIds.isNotEmpty()) {
                        localRepository.removeScenesFromLocalSnapshots(result.deletedSceneIds)
                    }
                    val cleanup = applyPlayerPlaylistDeleteResult(
                        queue = playbackQueue,
                        currentSceneId = sceneId,
                        result = result,
                    )
                    onPlaybackQueueChange(cleanup.queue)
                    pendingPlaylistDeleteSceneIds = emptyList()
                    playlistDeleteConfirmation = playlistDeleteConfirmation.dismiss()
                    hudText = result.koreanSummary
                    markPlayerInteraction()
                    controlsVisible = true
                    when {
                        cleanup.shouldExitPlayer -> onExitPlayer()
                        cleanup.sceneToOpen != null -> onOpenScene(cleanup.sceneToOpen)
                    }
                }
            },
        )

        if (resumePromptVisible) {
            ResumePlaybackPrompt(
                resumePositionMs = stream.startPositionMs,
                onResume = {
                    markPlayerInteraction()
                    resumeStartPositionMs = stream.startPositionMs
                    resumePromptVisible = false
                },
                onRestart = {
                    markPlayerInteraction()
                    resumeStartPositionMs = 0L
                    positionMs = 0L
                    resumePromptVisible = false
                },
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
        }
        if (presentationMotionState.renderWatchPageContent) {
        if (!pictureInPictureActive) {
            PlayerWatchPageContent(
            state = watchPageContentState,
            actionItems = watchPageActionItems,
            debugEntry = watchPageDebugEntry,
            debugInfoUiState = debugInfoUiState,
            ratingStep = ratingState.ratingStep,
            ratingMessage = ratingState.message,
            ratingUpdating = ratingState.isUpdating,
            sceneId = sceneId,
            similarRecommendations = similarRecommendations,
            similarRecommendationsLoading = similarRecommendationsLoading,
            similarRecommendationsError = similarRecommendationsError,
            similarRecommendationsSource = similarRecommendationsSource,
            queuedSceneIds = queueSceneIds,
            serverProfile = profile,
            onSelectRatingStep = selectRatingStep,
            onAddCurrentSceneToQueue = addCurrentSceneToQueue,
            onIncrementOCounter = incrementOCounter,
            onToggleFavorite = toggleFavorite,
            onToggleWatchLater = toggleWatchLater,
            onPlaySimilarScene = playSimilarScene,
            onAddSimilarSceneToQueue = addSimilarSceneToQueue,
            onRetrySimilarRecommendations = retrySimilarRecommendations,
            onPresentationDragUpdate = { update ->
                markPlayerInteraction()
                controlsVisible = true
                updatePresentationDrag(update)
            },
            onPresentationDragRelease = ::releasePresentationDrag,
            onGestureHudText = {
                markPlayerInteraction()
                hudText = it
            },
            presentationGestureLocked = locked,
            modifier = Modifier
                .heightIn(max = 360.dp * presentationMotionState.watchPageContentHeightFraction)
                .graphicsLayer {
                    alpha = presentationMotionState.watchPageContentAlpha
                    translationY = presentationMotionState.watchPageContentTranslationYPx
                },
            )
        }
        }
        }
        if (
            !pictureInPictureActive &&
            playlistDrawerOpen &&
            playlistDrawerPresentationPolicy.hostScope == PlayerPlaylistDrawerHostScope.FullPlayerPage
        ) {
            PlayerPlaylistDrawer(
                items = playlistItems,
                shuffleEnabled = playbackQueue.shuffleEnabled,
                serverProfile = profile,
                onDismiss = { playlistDrawerOpen = false },
                onSelectScene = { selectedSceneId ->
                    playlistDrawerOpen = false
                    selectPlaylistScene(selectedSceneId)
                },
                onReorderScene = reorderPlaylistScene,
                onRemoveScene = { item -> removePlaylistScene(item.sceneId) },
                onRequestDeleteScene = { item ->
                    markPlayerInteraction()
                    controlsVisible = true
                    pendingPlaylistDeleteSceneIds = listOf(item.sceneId)
                    playlistDeleteConfirmation = SceneBulkDeleteConfirmationState.open(1)
                },
            )
        }
    }
}

@Composable
private fun ResumePlaybackPrompt(
    resumePositionMs: Long,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = buildResumePlaybackPromptState(
        resumePositionMs = resumePositionMs,
        restartLabel = stashString(R.string.auto_kr_0489),
    )
    Column(
        modifier = modifier
            .padding(24.dp)
            .widthIn(max = 360.dp)
            .background(Color.Black.copy(alpha = 0.78f), MaterialTheme.shapes.medium)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stashString(R.string.auto_kr_0488),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = formatPlayerPosition(resumePositionMs),
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onRestart) {
                Text(state.restartLabel)
            }
            if (state.showResumeButton) {
                Button(onClick = onResume) {
                    Text(stashString(R.string.auto_kr_0059))
                }
            }
        }
    }
}

@Composable
private fun PlayerLoadingMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(message, color = Color.White)
        }
    }
}

@Composable
private fun PlayerActionMessage(
    message: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onPrimaryAction) {
                    Text(primaryActionLabel)
                }
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    OutlinedButton(onClick = onSecondaryAction) {
                        Text(secondaryActionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSystemBarsEffect(
    activity: Activity?,
    hideSystemBars: Boolean,
) {
    DisposableEffect(activity, hideSystemBars) {
        val window = activity?.window
        val decorView = window?.decorView
        val controller = if (window != null && decorView != null) {
            WindowInsetsControllerCompat(window, decorView)
        } else {
            null
        }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (hideSystemBars) {
            controller?.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun PlayerPictureInPictureAspectRatio.toAndroidRational(): Rational =
    Rational(width, height)
