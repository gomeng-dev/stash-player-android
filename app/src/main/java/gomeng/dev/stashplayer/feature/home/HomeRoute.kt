package gomeng.dev.stashplayer.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.StashMainTabSection
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.nextStashRandomSortSeed
import gomeng.dev.stashplayer.core.model.withStashRandomShuffleSeed
import gomeng.dev.stashplayer.core.network.buildSimilarScenesRepository
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.ui.components.SceneCard
import gomeng.dev.stashplayer.core.ui.components.rememberStashThumbnailModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPill
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyState
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashGhostButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashHeroMediaCard
import gomeng.dev.stashplayer.core.ui.designsystem.StashScreenHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSectionHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSectionHeaderModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashServerStatusCard as StashDesignServerStatusCard
import gomeng.dev.stashplayer.core.ui.designsystem.StashStatCard
import gomeng.dev.stashplayer.core.ui.designsystem.stashServerStatusCardModel
import gomeng.dev.stashplayer.feature.queue.buildQueuePlaybackRequest
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun HomeRoute(
    isFoldLikeLayout: Boolean,
    onOpenScene: (String, List<SceneCardModel>, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenShorts: () -> Unit,
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val queueScenes by localRepository.queueScenes.collectAsState(initial = emptyList())
    val watchLaterScenes by localRepository.watchLaterScenes.collectAsState(initial = emptyList())
    val favoriteScenes by localRepository.favoriteScenes.collectAsState(initial = emptyList())
    val playbackHistoryScenes by localRepository.playbackHistoryScenes.collectAsState(initial = emptyList())
    val shortsInteractions by localRepository.shortsInteractions.collectAsState(initial = emptyList())
    var sections by remember(profile) { mutableStateOf<List<StashMainTabSection>>(emptyList()) }
    var recommendedScenes by remember(profile) { mutableStateOf<List<SceneCardModel>>(emptyList()) }
    val recommendationSeed = remember(profile) { nextStashRandomSortSeed() }
    var isLoading by remember(profile) { mutableStateOf(false) }
    var error by remember(profile) { mutableStateOf<String?>(null) }
    var reloadToken by remember { mutableStateOf(0) }

    LaunchedEffect(profile, reloadToken) {
        val activeProfile = profile
        if (activeProfile == null) {
            sections = emptyList()
            error = null
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        error = null
        runCatching { StashGraphQlClient(activeProfile).findMainTabSections() }
            .onSuccess { sections = it }
            .onFailure { error = homeHubErrorText(it.message ?: it::class.simpleName) ?: stashString(R.string.auto_kr_0447) }
        isLoading = false
    }

    LaunchedEffect(profile, favoriteScenes, shortsInteractions, sections, recommendationSeed) {
        val activeProfile = profile
        if (activeProfile == null) {
            recommendedScenes = emptyList()
            return@LaunchedEffect
        }
        val anchors = buildHomeRecommendationAnchors(
            favoriteScenes = favoriteScenes,
            shortsInteractions = shortsInteractions,
        )
        if (anchors.isEmpty()) {
            recommendedScenes = emptyList()
            return@LaunchedEffect
        }
        val client = StashGraphQlClient(activeProfile)
        val repository = buildSimilarScenesRepository(
            graphQlClient = client,
            stashServerProfile = activeProfile,
        )
        val hybridRecommendations = anchors
            .take(3)
            .flatMap { anchor ->
                repository.getSimilarScenes(sceneId = anchor.sceneId, limit = 12).getOrDefault(emptyList())
            }
        val libraryCandidates = runCatching {
            client.findSceneCardsPage(
                perPage = HOME_RECOMMENDATION_LIBRARY_CANDIDATE_PAGE_SIZE,
                sort = "random",
                videoFilter = StashVideoFilterState().withStashRandomShuffleSeed(recommendationSeed),
            ).scenes
        }.getOrDefault(emptyList())
        recommendedScenes = rankHomeRecommendedScenes(
            candidates = homeRecommendationCandidatePool(
                libraryCandidates = libraryCandidates,
                sectionCandidates = sections.flatMap { it.scenes },
            ),
            anchors = anchors,
            hybridRecommendations = hybridRecommendations,
        )
    }

    val hubState = buildHomeHubState(
        hasProfile = profile != null,
        serverLabel = profile?.normalizedBaseUrl().orEmpty(),
        queueCount = queueScenes.size,
        watchLaterCount = watchLaterScenes.size,
        favoriteCount = favoriteScenes.size,
        hasServerError = error != null,
    )

    fun playQueue(shuffle: Boolean) {
        val request = buildQueuePlaybackRequest(queueScenes, shuffle) ?: return
        onOpenScene(request.selectedSceneId, request.scenes, request.randomShuffle)
    }

    HomeHubContent(
        isFoldLikeLayout = isFoldLikeLayout,
        hubState = hubState,
        sections = sections,
        recommendedScenes = recommendedScenes,
        serverProfile = profile,
        queueScenes = queueScenes,
        watchLaterScenes = watchLaterScenes,
        favoriteScenes = favoriteScenes,
        playbackHistoryScenes = playbackHistoryScenes,
        isRefreshing = isLoading,
        error = error,
        onRefresh = { reloadToken++ },
        onOpenScene = onOpenScene,
        onOpenSettings = onOpenSettings,
        onOpenQueue = onOpenQueue,
        onOpenFavorites = onOpenFavorites,
        onOpenExplore = onOpenExplore,
        onOpenShorts = onOpenShorts,
        onPlayQueue = { playQueue(shuffle = false) },
        onShuffleQueue = { playQueue(shuffle = true) },
    )
}

private const val HOME_RECOMMENDATION_LIBRARY_CANDIDATE_PAGE_SIZE = 100

@Composable
private fun HomeHubContent(
    isFoldLikeLayout: Boolean,
    hubState: HomeHubState,
    sections: List<StashMainTabSection>,
    recommendedScenes: List<SceneCardModel>,
    serverProfile: StashServerProfile?,
    queueScenes: List<SceneCardModel>,
    watchLaterScenes: List<SceneCardModel>,
    favoriteScenes: List<SceneCardModel>,
    playbackHistoryScenes: List<SceneCardModel>,
    isRefreshing: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onOpenScene: (String, List<SceneCardModel>, Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenShorts: () -> Unit,
    onPlayQueue: () -> Unit,
    onShuffleQueue: () -> Unit,
) {
    val horizontalPadding = if (isFoldLikeLayout) 24.dp else 14.dp
    val cardWidth = if (isFoldLikeLayout) 168.dp else 142.dp
    val thumbnailHeight = if (isFoldLikeLayout) 212.dp else 190.dp
    val hasProfile = !hubState.requiresSetup
    val serverScenes = sections.flatMap { it.scenes }
    val heroSelection = selectHomeHeroScene(
        playbackHistoryScenes = playbackHistoryScenes,
        queueScenes = queueScenes,
        watchLaterScenes = watchLaterScenes,
        serverScenes = serverScenes,
        favoriteScenes = favoriteScenes,
    )
    val heroScene = heroSelection?.scene
    val heroScenes = heroSelection?.playbackScenes.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 92.dp, top = 14.dp),
    ) {
        item {
            StashScreenHeader(
                title = stashString(R.string.auto_kr_0001),
                subtitle = hubState.server.statusLabel,
                modifier = Modifier.padding(horizontal = horizontalPadding),
                trailing = if (hasProfile) {
                    {
                        StashGhostButton(
                            text = if (isRefreshing) stashString(R.string.auto_kr_0448) else stashString(R.string.auto_kr_0449),
                            onClick = onRefresh,
                            enabled = !isRefreshing,
                        )
                    }
                } else {
                    null
                },
            )
        }

        if (!hasProfile || error != null) {
            item {
                HomeServerStatusCard(
                    state = hubState,
                    error = error,
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    onRefresh = onRefresh,
                    onOpenSettings = onOpenSettings,
                )
            }
        }

        if (hasProfile) {
            if (heroScene != null) {
                item {
                    HomeHeroCard(
                        scene = heroScene,
                        serverProfile = serverProfile,
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        onPlay = { onOpenScene(heroScene.id, heroScenes.ifEmpty { listOf(heroScene) }, false) },
                    )
                }
            }

            item {
                HomeMiniStatsRow(
                    queueCount = queueScenes.size,
                    watchLaterCount = watchLaterScenes.size,
                    favoriteCount = favoriteScenes.size,
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }

            item {
                HomeQuickActionRow(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    canPlayQueue = queueScenes.isNotEmpty(),
                    canShuffleQueue = queueScenes.size > 1,
                    onPlayQueue = onPlayQueue,
                    onShuffleQueue = onShuffleQueue,
                    onOpenQueue = onOpenQueue,
                    onOpenFavorites = onOpenFavorites,
                    onOpenExplore = onOpenExplore,
                    onOpenShorts = onOpenShorts,
                    onOpenSettings = onOpenSettings,
                )
            }

            if (queueScenes.isNotEmpty()) {
                item {
                    HomeScenePreviewRow(
                        title = homePreviewSectionTitle(HomeHubSectionId.Queue),
                        scenes = queueScenes.take(8),
                        allScenes = queueScenes,
                        cardWidth = cardWidth,
                        thumbnailHeight = thumbnailHeight,
                        horizontalPadding = horizontalPadding,
                        serverProfile = serverProfile,
                        onOpenScene = onOpenScene,
                    )
                }
            }

            if (watchLaterScenes.isNotEmpty()) {
                item {
                    HomeScenePreviewRow(
                        title = homePreviewSectionTitle(HomeHubSectionId.WatchLater),
                        scenes = watchLaterScenes.take(8),
                        allScenes = watchLaterScenes,
                        cardWidth = cardWidth,
                        thumbnailHeight = thumbnailHeight,
                        horizontalPadding = horizontalPadding,
                        serverProfile = serverProfile,
                        onOpenScene = onOpenScene,
                    )
                }
            }

            if (favoriteScenes.isNotEmpty()) {
                item {
                    HomeScenePreviewRow(
                        title = homePreviewSectionTitle(HomeHubSectionId.Favorites),
                        scenes = favoriteScenes.take(8),
                        allScenes = favoriteScenes,
                        cardWidth = cardWidth,
                        thumbnailHeight = thumbnailHeight,
                        horizontalPadding = horizontalPadding,
                        serverProfile = serverProfile,
                        onOpenScene = onOpenScene,
                    )
                }
            }

            if (recommendedScenes.isNotEmpty()) {
                item {
                    HomeScenePreviewRow(
                        title = homePreviewSectionTitle(HomeHubSectionId.Recommended),
                        scenes = recommendedScenes.take(8),
                        allScenes = recommendedScenes,
                        cardWidth = cardWidth,
                        thumbnailHeight = thumbnailHeight,
                        horizontalPadding = horizontalPadding,
                        serverProfile = serverProfile,
                        onOpenScene = onOpenScene,
                    )
                }
            }

        }

        if (isRefreshing && sections.isEmpty() && hasProfile) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (sections.isNotEmpty()) {
            item {
                StashSectionHeader(
                    state = StashSectionHeaderModel(
                        title = stashString(R.string.auto_kr_0450),
                        subtitle = stashString(R.string.auto_kr_0451),
                    ),
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }
        }

        items(sections, key = { it.spec.id }) { section ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StashSectionHeader(
                    state = StashSectionHeaderModel(
                        title = section.spec.title,
                        itemCount = section.scenes.size,
                    ),
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                ) {
                    items(section.scenes, key = { it.id }) { scene ->
                        SceneCard(
                            scene = scene,
                            onClick = { onOpenScene(scene.id, section.scenes, false) },
                            modifier = Modifier.width(cardWidth),
                            thumbnailHeight = thumbnailHeight,
                            thumbnailModel = rememberStashThumbnailModel(scene.thumbnailUrl, serverProfile),
                        )
                    }
                }
            }
        }

        if (hasProfile && !isRefreshing && sections.isEmpty() && error == null) {
            item {
                StashEmptyState(
                    state = StashEmptyStateModel(
                        title = stashString(R.string.auto_kr_0452),
                        message = stashString(R.string.auto_kr_0453),
                        primaryActionLabel = stashString(R.string.auto_kr_0002),
                    ),
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    onPrimaryAction = onOpenExplore,
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    scene: SceneCardModel,
    serverProfile: StashServerProfile?,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
) {
    val thumbnailModel = rememberStashThumbnailModel(scene.thumbnailUrl, serverProfile)
    StashHeroMediaCard(
        title = scene.title,
        subtitle = scene.subtitle.ifBlank { stashString(R.string.auto_kr_0383) },
        thumbnailModel = thumbnailModel,
        thumbnailContentDescription = scene.title,
        metadataLabels = scene.metadataBadges.map { it.label },
        showResumeBadge = scene.progress > 0f,
        primaryActionLabel = stashString(R.string.auto_kr_0039),
        onPrimaryAction = onPlay,
        modifier = modifier.height(360.dp),
    )
}

@Composable
private fun HomeMiniStatsRow(
    queueCount: Int,
    watchLaterCount: Int,
    favoriteCount: Int,
    modifier: Modifier = Modifier,
) {
    val stats = buildHomeDashboardStats(
        queueCount = queueCount,
        watchLaterCount = watchLaterCount,
        favoriteCount = favoriteCount,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        stats.forEach { stat ->
            StashStatCard(
                label = stat.label,
                value = stat.value,
                subtitle = stat.subtitle,
                contentDescription = stashString(R.string.auto_kr_0454, stat.label, stat.value),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeQuickActionRow(
    modifier: Modifier = Modifier,
    canPlayQueue: Boolean,
    canShuffleQueue: Boolean,
    onPlayQueue: () -> Unit,
    onShuffleQueue: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenExplore: () -> Unit,
    onOpenShorts: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val actions = buildHomeQuickActions(
        canPlayQueue = canPlayQueue,
        canShuffleQueue = canShuffleQueue,
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(actions, key = { it.label }) { action ->
            StashActionPill(
                label = action.label,
                enabled = action.enabled,
                onClick = {
                    when (action.action) {
                        HomeHubAction.PlayQueue -> onPlayQueue()
                        HomeHubAction.ShuffleQueue -> onShuffleQueue()
                        HomeHubAction.OpenQueue -> onOpenQueue()
                        HomeHubAction.OpenFavorites -> onOpenFavorites()
                        HomeHubAction.OpenExplore -> onOpenExplore()
                        HomeHubAction.OpenShorts -> onOpenShorts()
                        HomeHubAction.OpenBrowse -> onOpenExplore()
                        HomeHubAction.OpenSettings -> onOpenSettings()
                        HomeHubAction.RetryServerSections -> Unit
                    }
                },
                icon = {
                    Icon(
                        imageVector = homeQuickActionIcon(action),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

private fun homeQuickActionIcon(action: HomeQuickActionModel): ImageVector = when (action.label) {
    stashString(R.string.auto_kr_0428) -> Icons.Outlined.PlayArrow
    stashString(R.string.auto_kr_0429) -> Icons.Outlined.Shuffle
    stashString(R.string.auto_kr_0004) -> Icons.AutoMirrored.Outlined.PlaylistPlay
    stashString(R.string.auto_kr_0016) -> Icons.Outlined.Bookmarks
    stashString(R.string.auto_kr_0238) -> Icons.Outlined.Star
    stashString(R.string.navigation_explore_label) -> Icons.Outlined.Explore
    stashString(R.string.navigation_shorts_label) -> Icons.Outlined.PlayArrow
    else -> Icons.Outlined.Settings
}

@Composable
private fun HomeServerStatusCard(
    state: HomeHubState,
    error: String?,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val model = stashServerStatusCardModel(
        statusLabel = state.server.statusLabel,
        detail = state.server.detail,
        requiresSetup = state.requiresSetup,
        hasError = error != null,
        errorMessage = error,
    )
    StashDesignServerStatusCard(
        model = model,
        onPrimaryAction = when {
            state.requiresSetup -> onOpenSettings
            error != null -> onRefresh
            else -> null
        },
        onSecondaryAction = if (error != null) onOpenSettings else null,
        modifier = modifier,
    )
}

@Composable
private fun HomeScenePreviewRow(
    title: String,
    scenes: List<SceneCardModel>,
    allScenes: List<SceneCardModel>,
    cardWidth: androidx.compose.ui.unit.Dp,
    thumbnailHeight: androidx.compose.ui.unit.Dp,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    serverProfile: StashServerProfile?,
    onOpenScene: (String, List<SceneCardModel>, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StashSectionHeader(
            state = StashSectionHeaderModel(
                title = title,
                itemCount = allScenes.size,
            ),
            modifier = Modifier.padding(horizontal = horizontalPadding),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) {
            items(scenes, key = { it.id }) { scene ->
                val thumbnailModel = rememberStashThumbnailModel(scene.thumbnailUrl, serverProfile)
                SceneCard(
                    scene = scene,
                    onClick = { onOpenScene(scene.id, allScenes, false) },
                    modifier = Modifier.width(cardWidth),
                    thumbnailHeight = thumbnailHeight,
                    thumbnailModel = thumbnailModel,
                )
            }
        }
    }
}
