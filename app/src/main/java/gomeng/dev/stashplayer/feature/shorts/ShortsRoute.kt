package gomeng.dev.stashplayer.feature.shorts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.model.DEFAULT_STASH_SHORTS_PAGE_SIZE
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteConfirmationState
import gomeng.dev.stashplayer.core.model.ShortsActionRailButton
import gomeng.dev.stashplayer.core.model.STASH_SHORTS_PLAYBACK_FEEDBACK_VISIBLE_MS
import gomeng.dev.stashplayer.core.model.ShortsExplicitFeedback
import gomeng.dev.stashplayer.core.model.ShortsFeedState
import gomeng.dev.stashplayer.core.model.ShortsFeedStatus
import gomeng.dev.stashplayer.core.model.ShortsInteractionOutcome
import gomeng.dev.stashplayer.core.model.buildShortsRecommendationSignals
import gomeng.dev.stashplayer.core.model.applyShortsFeedbackToItems
import gomeng.dev.stashplayer.core.model.appendDistinctShortsPage
import gomeng.dev.stashplayer.core.model.buildShortsVideoFilter
import gomeng.dev.stashplayer.core.model.mergeLikedAnchorHybridScores
import gomeng.dev.stashplayer.core.model.nextStashRandomSortSeed
import gomeng.dev.stashplayer.core.model.rankShortsCandidates
import gomeng.dev.stashplayer.core.model.rerankShortsTail
import gomeng.dev.stashplayer.core.model.resolveShortsLongPressSpeed
import gomeng.dev.stashplayer.core.model.resolveShortsSeekTarget
import gomeng.dev.stashplayer.core.model.resolveShortsCenterPlaybackFeedback
import gomeng.dev.stashplayer.core.model.shortsControllerWindowSceneIds
import gomeng.dev.stashplayer.core.model.shortsActionRailButtons
import gomeng.dev.stashplayer.core.model.shortsPrewarmSceneIds
import gomeng.dev.stashplayer.core.model.shouldLoadMoreShorts
import gomeng.dev.stashplayer.core.model.shouldToggleShortsLikeOnDoubleTap
import gomeng.dev.stashplayer.core.model.shouldToggleShortsPlaybackOnTap
import gomeng.dev.stashplayer.core.model.ShortsGestureTarget
import gomeng.dev.stashplayer.core.model.toggleShortsExplicitFeedback
import gomeng.dev.stashplayer.core.model.withoutDeletedShortsScenes
import gomeng.dev.stashplayer.core.network.GraphQlStashStreamResolver
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.buildSimilarScenesRepository
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.formatPlayerPosition
import gomeng.dev.stashplayer.core.player.defaultPlayerFullscreenSeekBarVisualPolicy
import gomeng.dev.stashplayer.core.ui.components.ReusablePlayerSeekRow
import gomeng.dev.stashplayer.core.ui.components.rememberStashThumbnailModel
import gomeng.dev.stashplayer.core.ui.components.SceneBulkDeleteConfirmationDialog
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import gomeng.dev.stashplayer.feature.player.PlayerSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsRoute(
    isFoldLikeLayout: Boolean,
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val shortsMaxDurationSeconds by settingsRepository.shortsMaxDurationSeconds.collectAsState(
        initial = StashSettingsRepository.DEFAULT_SHORTS_MAX_DURATION_SECONDS,
    )
    val shortsInteractions by localRepository.shortsInteractions.collectAsState(initial = emptyList())
    val favoriteSceneIds by localRepository.favoriteSceneIds.collectAsState(initial = emptySet())
    val watchLaterSceneIds by localRepository.watchLaterSceneIds.collectAsState(initial = emptySet())
    val activeProfile = profile
    val scope = rememberCoroutineScope()
    val seed = remember { nextStashRandomSortSeed() }
    val videoFilter = remember(seed, shortsMaxDurationSeconds) {
        buildShortsVideoFilter(seed = seed, maxDurationSeconds = shortsMaxDurationSeconds)
    }
    var likedAnchorHybridScores by remember(activeProfile) { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val recommendationSignals = remember(
        shortsInteractions,
        favoriteSceneIds,
        watchLaterSceneIds,
        likedAnchorHybridScores,
    ) {
        buildShortsRecommendationSignals(
            interactions = shortsInteractions,
            favoriteSceneIds = favoriteSceneIds,
            watchLaterSceneIds = watchLaterSceneIds,
            likedAnchorHybridScores = likedAnchorHybridScores,
        )
    }
    var feedState by remember(activeProfile, seed, shortsMaxDurationSeconds) { mutableStateOf(ShortsFeedState()) }
    var requestSerial by remember(activeProfile, seed, shortsMaxDurationSeconds) { mutableLongStateOf(0L) }
    var poolRevision by remember(activeProfile) { mutableIntStateOf(0) }
    var retryToken by remember { mutableIntStateOf(0) }
    var pagerScrollRequests by remember { mutableIntStateOf(-1) }
    var deleteConfirmation by remember { mutableStateOf(SceneBulkDeleteConfirmationState.Hidden) }
    var deleteTargetSceneId by remember { mutableStateOf<String?>(null) }
    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
    val client = remember(activeProfile) { activeProfile?.let(::StashGraphQlClient) }
    val similarScenesRepository = remember(activeProfile) {
        activeProfile?.let { serverProfile ->
            buildSimilarScenesRepository(StashGraphQlClient(serverProfile), serverProfile)
        }
    }
    val pool = remember(activeProfile) {
        activeProfile?.let { serverProfile ->
            val graphQlClient = StashGraphQlClient(serverProfile)
            ShortsPlaybackControllerPool(
                context = context.applicationContext,
                resolver = GraphQlStashStreamResolver(graphQlClient, serverProfile),
                onPlaybackEnded = { sceneId ->
                    scope.launch {
                        val currentIndex = feedState.items.indexOfFirst { it.scene.id == sceneId }
                        feedState.items.getOrNull(currentIndex)?.scene?.let { scene ->
                            localRepository.recordShortsInteraction(
                                scene = scene,
                                outcome = ShortsInteractionOutcome.Completed,
                                watchMs = 0L,
                                progress = 1f,
                            )
                        }
                        val nextIndex = currentIndex + 1
                        if (currentIndex >= 0 && nextIndex < feedState.items.size) {
                            pagerScrollRequests = nextIndex
                        }
                    }
                },
                onPlaybackError = { _, _ -> poolRevision += 1 },
            )
        }
    }

    fun loadShortsPage(page: Int, reset: Boolean = false) {
        val activeClient = client ?: return
        if (feedState.status == ShortsFeedStatus.Loading && !reset) return
        val requestId = requestSerial + 1L
        requestSerial = requestId
        feedState = if (reset) ShortsFeedState().loading() else feedState.loading()
        scope.launch {
            runCatching {
                activeClient.findSceneCardsPage(
                    perPage = DEFAULT_STASH_SHORTS_PAGE_SIZE,
                    page = page,
                    sort = "updated_at",
                    videoFilter = videoFilter,
                    )
            }.onSuccess { result ->
                if (requestSerial != requestId) return@onSuccess
                feedState = if (page == 1) {
                    val ranked = rankShortsCandidates(
                        candidates = result.scenes,
                        signals = recommendationSignals,
                        seed = seed,
                    )
                    feedState.withFirstPage(
                        scenes = result.scenes,
                        totalCount = result.totalCount,
                        perPage = DEFAULT_STASH_SHORTS_PAGE_SIZE,
                    ).copy(items = ranked)
                } else {
                    val appended = appendDistinctShortsPage(feedState.items, result.scenes)
                    val appendedState = feedState.copy(
                        items = appended,
                        nextPage = page + 1,
                        hasMore = page * DEFAULT_STASH_SHORTS_PAGE_SIZE < result.totalCount,
                        status = if (appended.isEmpty()) ShortsFeedStatus.Empty else ShortsFeedStatus.Ready,
                        errorMessage = null,
                    )
                    rerankShortsTail(appendedState, recommendationSignals, seed = seed)
                }
            }.onFailure { throwable ->
                if (requestSerial != requestId) return@onFailure
                feedState = feedState.failed(
                    redactStashCredentialText(throwable.message ?: throwable::class.simpleName.orEmpty()),
                )
            }
        }
    }

    DisposableEffect(pool) {
        onDispose {
            pool?.releaseAll()
        }
    }

    LaunchedEffect(activeProfile, seed, shortsMaxDurationSeconds) {
        if (activeProfile != null) {
            loadShortsPage(page = 1, reset = true)
        }
    }

    val likedAnchorSceneIds = remember(shortsInteractions) {
        shortsInteractions
            .filter { it.explicitFeedback == ShortsExplicitFeedback.Liked }
            .sortedByDescending { it.updatedAt }
            .map { it.sceneId }
            .distinct()
            .take(3)
    }

    LaunchedEffect(similarScenesRepository, likedAnchorSceneIds) {
        val repository = similarScenesRepository
        if (repository == null || likedAnchorSceneIds.isEmpty()) {
            likedAnchorHybridScores = emptyMap()
            return@LaunchedEffect
        }
        val recommendations = likedAnchorSceneIds.mapNotNull { sceneId ->
            repository.getSimilarScenes(sceneId = sceneId, limit = 24).getOrNull()
        }
        likedAnchorHybridScores = mergeLikedAnchorHybridScores(
            likedSceneIds = likedAnchorSceneIds.toSet(),
            recommendationsByLikedScene = recommendations,
        )
    }

    LaunchedEffect(recommendationSignals, seed) {
        if (feedState.items.isNotEmpty()) {
            val interactionByScene = shortsInteractions.associateBy { it.sceneId }
            val withFeedback = feedState.items.map { item ->
                item.copy(
                    explicitFeedback = interactionByScene[item.scene.id]?.explicitFeedback
                        ?: ShortsExplicitFeedback.None,
                )
            }
            feedState = rerankShortsTail(
                feedState = feedState.copy(items = withFeedback),
                signals = recommendationSignals,
                seed = seed,
            )
        }
    }

    if (activeProfile == null) {
        ShortsMessageSurface(
            title = stashString(R.string.shorts_requires_server_title),
            message = stashString(R.string.shorts_requires_server_message),
            isFoldLikeLayout = isFoldLikeLayout,
        )
        return
    }

    when {
        feedState.items.isEmpty() && feedState.status == ShortsFeedStatus.Loading -> {
            ShortsMessageSurface(
                title = stashString(R.string.shorts_loading_title),
                message = stashString(R.string.shorts_loading_message),
                isFoldLikeLayout = isFoldLikeLayout,
                loading = true,
            )
        }
        feedState.items.isEmpty() && feedState.status == ShortsFeedStatus.Empty -> {
            ShortsMessageSurface(
                title = stashString(R.string.shorts_empty_title),
                message = stashString(R.string.shorts_empty_message),
                isFoldLikeLayout = isFoldLikeLayout,
                actionLabel = stashString(R.string.shorts_retry_action),
                onAction = { loadShortsPage(page = 1, reset = true) },
            )
        }
        feedState.items.isEmpty() && feedState.status == ShortsFeedStatus.Error -> {
            ShortsMessageSurface(
                title = stashString(R.string.shorts_error_title),
                message = feedState.errorMessage ?: stashString(R.string.shorts_error_message),
                isFoldLikeLayout = isFoldLikeLayout,
                actionLabel = stashString(R.string.shorts_retry_action),
                onAction = { loadShortsPage(page = 1, reset = true) },
            )
        }
        else -> {
            val pagerState = rememberPagerState { feedState.items.size }

            LaunchedEffect(pagerState, feedState.items.size, feedState.hasMore, feedState.nextPage) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    feedState = feedState.copy(activeIndex = page)
                    if (
                        shouldLoadMoreShorts(
                            activeIndex = page,
                            itemCount = feedState.items.size,
                            hasMore = feedState.hasMore,
                        )
                    ) {
                        loadShortsPage(page = feedState.nextPage)
                    }
                }
            }

            LaunchedEffect(pagerScrollRequests, feedState.items.size) {
                val target = pagerScrollRequests
                if (target in feedState.items.indices) {
                    pagerState.animateScrollToPage(target)
                    pagerScrollRequests = -1
                }
            }

            val activeItem = feedState.items.getOrNull(feedState.activeIndex)
            val windowSceneIds = remember(feedState.items, feedState.activeIndex) {
                shortsControllerWindowSceneIds(feedState.items, feedState.activeIndex)
            }
            LaunchedEffect(activeItem?.scene?.id, windowSceneIds, retryToken, pool) {
                val activePool = pool ?: return@LaunchedEffect
                val activeScene = activeItem?.scene ?: return@LaunchedEffect
                activePool.releaseOutside(windowSceneIds.toSet())
                activePool.ensurePrepared(scene = activeScene, active = true)
                activePool.activate(activeScene.id)
                poolRevision += 1
                val prewarmIds = shortsPrewarmSceneIds(feedState.items, feedState.activeIndex).toSet()
                val prewarmItems = feedState.items.filter { it.scene.id in prewarmIds }
                prewarmItems.forEach { item ->
                    launch {
                        activePool.ensurePrepared(scene = item.scene, active = false)
                        activePool.activate(activeScene.id)
                        poolRevision += 1
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val item = feedState.items[page]
                    val isActive = page == feedState.activeIndex
                    val activeController = if (isActive) {
                        poolRevision
                        pool?.controllerFor(item.scene.id)
                    } else {
                        null
                    }
                    val stream = if (isActive) pool?.streamFor(item.scene.id) else null
                    val error = if (isActive) pool?.errorFor(item.scene.id) else null
                    ShortsPage(
                        item = item,
                        serverProfile = activeProfile,
                        activeController = activeController,
                        streamTitle = stream?.title,
                        errorMessage = error,
                        isActive = isActive,
                        onLike = {
                            val nextFeedback = toggleShortsExplicitFeedback(
                                current = item.explicitFeedback,
                                requested = ShortsExplicitFeedback.Liked,
                            )
                            feedState = feedState.copy(
                                items = applyShortsFeedbackToItems(
                                    items = feedState.items,
                                    sceneId = item.scene.id,
                                    feedback = nextFeedback,
                                ),
                            )
                            scope.launch {
                                localRepository.setShortsExplicitFeedback(item.scene, nextFeedback)
                                if (nextFeedback == ShortsExplicitFeedback.Liked) {
                                    runCatching {
                                        client?.ensureShortsTagOnScene(item.scene)
                                    }.onFailure {
                                        // The local learning signal should still be saved even if the server tag update fails.
                                    }
                                }
                            }
                        },
                        onNotInterested = {
                            val nextFeedback = toggleShortsExplicitFeedback(
                                current = item.explicitFeedback,
                                requested = ShortsExplicitFeedback.NotInterested,
                            )
                            feedState = feedState.copy(
                                items = applyShortsFeedbackToItems(
                                    items = feedState.items,
                                    sceneId = item.scene.id,
                                    feedback = nextFeedback,
                                ),
                            )
                            scope.launch {
                                localRepository.setShortsExplicitFeedback(item.scene, nextFeedback)
                            }
                        },
                        onDelete = {
                            deleteTargetSceneId = item.scene.id
                            deleteErrorMessage = null
                            deleteConfirmation = SceneBulkDeleteConfirmationState.open(selectedCount = 1)
                        },
                        onSeekTo = { targetMs ->
                            activeController?.player?.seekTo(targetMs)
                        },
                        onRecordSkip = { watchMs, progress ->
                            scope.launch {
                                localRepository.recordShortsInteraction(
                                    scene = item.scene,
                                    outcome = ShortsInteractionOutcome.Skipped,
                                    watchMs = watchMs,
                                    progress = progress,
                                )
                            }
                        },
                        onRetry = {
                            pool?.clear(item.scene.id)
                            retryToken += 1
                            poolRevision += 1
                        },
                        onSkip = {
                            val next = page + 1
                            if (next in feedState.items.indices) {
                                scope.launch { pagerState.animateScrollToPage(next) }
                            }
                        },
                    )
                }

                if (feedState.status == ShortsFeedStatus.Loading && feedState.items.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(24.dp),
                        color = Color.White,
                    )
                }
                if (feedState.status == ShortsFeedStatus.Error && feedState.errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = feedState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        OutlinedButton(onClick = { loadShortsPage(page = feedState.nextPage) }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(stashString(R.string.shorts_retry_action))
                        }
                    }
                }
                if (deleteErrorMessage != null) {
                    Text(
                        text = deleteErrorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.62f), MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                SceneBulkDeleteConfirmationDialog(
                    state = deleteConfirmation,
                    onConfirmationChange = { deleteConfirmation = deleteConfirmation.withConfirmation(it) },
                    onDeleteFileChange = { deleteConfirmation = deleteConfirmation.withDeleteFile(it) },
                    onDeleteGeneratedChange = { deleteConfirmation = deleteConfirmation.withDeleteGenerated(it) },
                    onCancel = {
                        deleteConfirmation = deleteConfirmation.dismiss()
                        deleteTargetSceneId = null
                    },
                    onDelete = {
                        val sceneId = deleteTargetSceneId ?: return@SceneBulkDeleteConfirmationDialog
                        val activeClient = client ?: return@SceneBulkDeleteConfirmationDialog
                        val deleteOptions = deleteConfirmation.deleteOptions
                        deleteConfirmation = deleteConfirmation.deleting()
                        scope.launch {
                            val result = runCatching {
                                activeClient.deleteScenes(listOf(sceneId), deleteOptions)
                            }.getOrElse { throwable ->
                                deleteErrorMessage = redactStashCredentialText(
                                    throwable.message ?: stashString(R.string.shorts_delete_failed_message),
                                )
                                deleteConfirmation = deleteConfirmation.dismiss()
                                deleteTargetSceneId = null
                                return@launch
                            }
                            if (result.deletedSceneIds.isNotEmpty()) {
                                pool?.clear(sceneId)
                                localRepository.removeScenesFromLocalSnapshots(result.deletedSceneIds)
                                feedState = feedState.withoutDeletedShortsScenes(result)
                                poolRevision += 1
                            } else {
                                deleteErrorMessage = result.failedSceneIds[sceneId]
                                    ?: stashString(R.string.shorts_delete_failed_message)
                            }
                            deleteConfirmation = deleteConfirmation.dismiss()
                            deleteTargetSceneId = null
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ShortsPage(
    item: gomeng.dev.stashplayer.core.model.ShortsFeedItem,
    serverProfile: gomeng.dev.stashplayer.core.network.StashServerProfile,
    activeController: gomeng.dev.stashplayer.core.player.StashPlayerController?,
    streamTitle: String?,
    errorMessage: String?,
    isActive: Boolean,
    onLike: () -> Unit,
    onNotInterested: () -> Unit,
    onDelete: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onRecordSkip: (watchMs: Long, progress: Float) -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
) {
    var positionMs by remember(activeController) { mutableLongStateOf(0L) }
    var durationMs by remember(activeController) { mutableLongStateOf(0L) }
    var isPlaying by remember(activeController) { mutableStateOf(false) }
    var sliderFraction by remember(activeController) { mutableStateOf(0f) }
    var isSeeking by remember(activeController) { mutableStateOf(false) }
    var speedHudVisible by remember(activeController) { mutableStateOf(false) }
    var heldSpeedRestore by remember(activeController) { mutableStateOf<Float?>(null) }
    var playbackFeedbackRequested by remember(activeController) { mutableStateOf(false) }
    var playbackFeedbackSerial by remember(activeController) { mutableIntStateOf(0) }
    var playbackFeedbackPlaying by remember(activeController) { mutableStateOf(false) }
    val activeStartedAt = remember(activeController) { System.currentTimeMillis() }
    val latestPositionMs by rememberUpdatedState(positionMs)
    val latestDurationMs by rememberUpdatedState(durationMs)
    val latestHeldSpeedRestore by rememberUpdatedState(heldSpeedRestore)
    val thumbnailModel = rememberStashThumbnailModel(item.scene.thumbnailUrl, serverProfile)
    val centerPlaybackFeedback = resolveShortsCenterPlaybackFeedback(
        active = isActive,
        controllerReady = activeController != null,
        hasError = errorMessage != null,
        feedbackRequested = playbackFeedbackRequested,
        playing = playbackFeedbackPlaying,
    )

    fun togglePlaybackWithFeedback() {
        val player = activeController?.player ?: return
        val targetPlaying = !player.isPlaying
        if (targetPlaying) {
            player.play()
        } else {
            player.pause()
        }
        isPlaying = targetPlaying
        playbackFeedbackPlaying = targetPlaying
        playbackFeedbackRequested = true
        playbackFeedbackSerial += 1
    }

    LaunchedEffect(activeController) {
        while (activeController != null) {
            positionMs = activeController.player.currentPosition.coerceAtLeast(0L)
            durationMs = activeController.player.duration.takeIf { it > 0L } ?: 0L
            isPlaying = activeController.player.isPlaying
            if (!isSeeking) {
                sliderFraction = if (durationMs > 0L) {
                    (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            delay(250L)
        }
    }

    LaunchedEffect(activeController, playbackFeedbackSerial) {
        if (playbackFeedbackSerial > 0) {
            delay(STASH_SHORTS_PLAYBACK_FEEDBACK_VISIBLE_MS)
            playbackFeedbackRequested = false
        }
    }

    DisposableEffect(activeController) {
        onDispose {
            val controller = activeController ?: return@onDispose
            val progress = if (latestDurationMs > 0L) {
                (latestPositionMs.toFloat() / latestDurationMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
            if (progress < 0.25f) {
                onRecordSkip(System.currentTimeMillis() - activeStartedAt, progress)
            }
            latestHeldSpeedRestore?.let { controller.setPlaybackSpeed(it) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (activeController != null) {
            PlayerSurface(
                controller = activeController,
                aspectRatioMode = AspectRatioMode.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        )

        if (activeController != null && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeController, isActive) {
                        detectTapGestures(
                            onTap = {
                                if (shouldToggleShortsPlaybackOnTap(isActive, ShortsGestureTarget.Surface)) {
                                    togglePlaybackWithFeedback()
                                }
                            },
                            onDoubleTap = {
                                if (shouldToggleShortsLikeOnDoubleTap(isActive, ShortsGestureTarget.Surface)) {
                                    onLike()
                                }
                            },
                            onLongPress = {
                                if (isActive) {
                                    val previousSpeed = activeController.player.playbackParameters.speed
                                    val decision = resolveShortsLongPressSpeed(previousSpeed, pressed = true)
                                    heldSpeedRestore = decision.restoreSpeed
                                    speedHudVisible = decision.showHud
                                    activeController.setPlaybackSpeed(decision.playbackSpeed)
                                }
                            },
                            onPress = {
                                tryAwaitRelease()
                                heldSpeedRestore?.let { restore ->
                                    val decision = resolveShortsLongPressSpeed(restore, pressed = false)
                                    activeController.setPlaybackSpeed(decision.playbackSpeed)
                                    speedHudVisible = decision.showHud
                                    heldSpeedRestore = null
                                }
                            },
                        )
                    },
            )
        }

        if (activeController == null && errorMessage == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stashString(R.string.shorts_stream_error_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onRetry) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stashString(R.string.shorts_retry_action))
                    }
                    Button(onClick = onSkip) {
                        Icon(Icons.Outlined.SkipNext, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stashString(R.string.shorts_skip_action))
                    }
                }
            }
        }

        if (activeController != null && errorMessage == null) {
            AnimatedVisibility(
                visible = centerPlaybackFeedback != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                IconButton(
                    onClick = { togglePlaybackWithFeedback() },
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color.Black.copy(alpha = 0.36f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (playbackFeedbackPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (playbackFeedbackPlaying) {
                            stashString(R.string.shorts_pause_content_description)
                        } else {
                            stashString(R.string.shorts_play_content_description)
                        },
                        tint = Color.White,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }

            ShortsActionRail(
                liked = item.explicitFeedback == ShortsExplicitFeedback.Liked,
                notInterested = item.explicitFeedback == ShortsExplicitFeedback.NotInterested,
                onLike = onLike,
                onNotInterested = onNotInterested,
                onDelete = onDelete,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
            )
        }

        if (speedHudVisible) {
            Text(
                text = stashString(R.string.shorts_speed_hold_feedback),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.52f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = streamTitle ?: item.scene.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.scene.subtitle.ifBlank { item.scene.durationText },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ReusablePlayerSeekRow(
                displayedPositionMs = resolveShortsSeekTarget(sliderFraction, durationMs),
                durationMs = durationMs,
                sliderFraction = sliderFraction,
                sliderEnabled = activeController != null && durationMs > 0L,
                visualPolicy = defaultPlayerFullscreenSeekBarVisualPolicy(),
                onSliderFractionChange = { value ->
                    isSeeking = true
                    sliderFraction = value
                },
                onSliderChangeFinished = {
                    isSeeking = false
                    onSeekTo(resolveShortsSeekTarget(sliderFraction, durationMs))
                },
            )
        }
    }
}

@Composable
private fun ShortsActionRail(
    liked: Boolean,
    notInterested: Boolean,
    onLike: () -> Unit,
    onNotInterested: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        shortsActionRailButtons().forEach { button ->
            when (button) {
                ShortsActionRailButton.Like -> {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = if (liked) {
                                stashString(R.string.shorts_liked_content_description)
                            } else {
                                stashString(R.string.shorts_like_content_description)
                            },
                            tint = if (liked) Color(0xFFFF4D6D) else Color.White,
                        )
                    }
                }
                ShortsActionRailButton.NotInterested -> {
                    IconButton(onClick = onNotInterested) {
                        Icon(
                            imageVector = Icons.Outlined.ThumbDown,
                            contentDescription = if (notInterested) {
                                stashString(R.string.shorts_not_interested_selected_content_description)
                            } else {
                                stashString(R.string.shorts_not_interested_content_description)
                            },
                            tint = if (notInterested) Color(0xFFFFC857) else Color.White,
                        )
                    }
                }
                ShortsActionRailButton.Delete -> {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stashString(R.string.shorts_delete_content_description),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortsMessageSurface(
    title: String,
    message: String,
    isFoldLikeLayout: Boolean,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(if (isFoldLikeLayout) 32.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.72f),
        )
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
