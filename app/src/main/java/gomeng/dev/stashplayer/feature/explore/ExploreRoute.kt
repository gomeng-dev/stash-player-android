package gomeng.dev.stashplayer.feature.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryFilterAction
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryFilterSection
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryOpenSheet
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryTagOptionsState
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryManualFilter
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryRandomShuffleAction
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoverySavedFilter
import gomeng.dev.stashplayer.core.discovery.buildStashDiscoveryFavoriteToggleDecision
import gomeng.dev.stashplayer.core.discovery.buildStashDiscoveryQueueAddDecision
import gomeng.dev.stashplayer.core.discovery.buildStashDiscoveryWatchLaterToggleDecision
import gomeng.dev.stashplayer.core.discovery.clearStashDiscoveryFilterSection
import gomeng.dev.stashplayer.core.discovery.applyTagOptionsFailure
import gomeng.dev.stashplayer.core.discovery.applyTagOptionsSuccess
import gomeng.dev.stashplayer.core.discovery.dismiss
import gomeng.dev.stashplayer.core.discovery.open
import gomeng.dev.stashplayer.core.discovery.startTagOptionsRequest
import gomeng.dev.stashplayer.core.discovery.toFilterSheetVisibility
import gomeng.dev.stashplayer.core.discovery.withTagOptionsQuery
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.local.appliedFilterState
import gomeng.dev.stashplayer.core.local.applyLocalFavoriteFilter
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteConfirmationState
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteResult
import gomeng.dev.stashplayer.core.model.StashSceneDeleteOptions
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.SceneSelectionState
import gomeng.dev.stashplayer.core.model.StashScenesViewMode
import gomeng.dev.stashplayer.core.model.StashExplorePageState
import gomeng.dev.stashplayer.core.model.StashExploreSortOption
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.defaultStashExplorePageSizeOptions
import gomeng.dev.stashplayer.core.model.defaultStashExploreSortOptions
import gomeng.dev.stashplayer.core.model.initialFromPersisted
import gomeng.dev.stashplayer.core.model.normalizeStashDiscoveryQuery
import gomeng.dev.stashplayer.core.model.shouldLoadExploreResultsFromServer
import gomeng.dev.stashplayer.core.model.shouldShowSceneCardQuickActionsInMediaGrid
import gomeng.dev.stashplayer.core.model.shouldUseLocalFavoriteExploreResults
import gomeng.dev.stashplayer.core.model.stashDiscoveryResultCountLabel
import gomeng.dev.stashplayer.core.model.stashMediaGridColumnCount
import gomeng.dev.stashplayer.core.model.stashMediaGridThumbnailHeightDp
import gomeng.dev.stashplayer.core.model.toPersistedFilterState
import gomeng.dev.stashplayer.core.model.afterBulkDelete
import gomeng.dev.stashplayer.core.model.withoutBulkDeletedSceneIds
import gomeng.dev.stashplayer.core.model.withoutBulkDeletedScenes
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueueContinuation
import gomeng.dev.stashplayer.core.ui.components.SceneCard
import gomeng.dev.stashplayer.core.ui.components.SceneBulkDeleteConfirmationDialog
import gomeng.dev.stashplayer.core.ui.components.StashActiveVideoFilterChipsRow
import gomeng.dev.stashplayer.core.ui.components.rememberStashThumbnailModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyState
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashErrorState
import gomeng.dev.stashplayer.core.ui.designsystem.StashErrorStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashSecondaryButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.stashDiscoveryVisualPolicy
import gomeng.dev.stashplayer.core.ui.discovery.StashDiscoveryFilterSheets
import gomeng.dev.stashplayer.core.ui.discovery.StashScenesToolbar
import gomeng.dev.stashplayer.core.ui.discovery.StashVideoFilterGroupRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val SEARCH_DEBOUNCE_MS = 500L

@Composable
fun ExploreRoute(
    isFoldLikeLayout: Boolean,
    onOpenScene: (String, List<SceneCardModel>, Boolean, PlayerPlaybackQueueContinuation?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val sortOptions = remember { defaultStashExploreSortOptions() }
    val pageSizeOptions = remember { defaultStashExplorePageSizeOptions() }
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val favoriteSceneIds by localRepository.favoriteSceneIds.collectAsState(initial = emptySet())
    val favoriteScenes by localRepository.favoriteScenes.collectAsState(initial = emptyList())
    val watchLaterSceneIds by localRepository.watchLaterSceneIds.collectAsState(initial = emptySet())
    val queueSceneIds by localRepository.queueSceneIds.collectAsState(initial = emptySet())
    val savedVideoFilters by localRepository.savedVideoFilters.collectAsState(initial = emptyList())
    val persistedExploreFilterState by localRepository.persistedExploreFilterState.collectAsState(initial = null)
    val recentExploreFilters by localRepository.recentExploreVideoFilters.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var pageState by remember { mutableStateOf(StashExplorePageState.initial(sortOptions.first())) }
    var didApplyPersistedExploreState by remember { mutableStateOf(false) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var requestSerial by remember { mutableLongStateOf(0L) }
    var openSheet by remember { mutableStateOf<StashDiscoveryOpenSheet>(StashDiscoveryOpenSheet.None) }
    val filterSheetVisibility = openSheet.toFilterSheetVisibility()
    val isTagFilterOpen = filterSheetVisibility.isTagFilterOpen
    var savedFilterName by remember { mutableStateOf("") }
    var tagOptionsState by remember { mutableStateOf(StashDiscoveryTagOptionsState()) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadTags(query: String) {
        val activeProfile = profile ?: return
        val started = tagOptionsState.startTagOptionsRequest(
            query = query,
            profileRevision = activeProfile.hashCode(),
        )
        tagOptionsState = started.state
        scope.launch {
            runCatching {
                StashGraphQlClient(activeProfile).findTags(query = query)
            }.onSuccess { tags ->
                val currentProfile = profile ?: return@onSuccess
                tagOptionsState = tagOptionsState.applyTagOptionsSuccess(
                    request = started.request,
                    currentProfileRevision = currentProfile.hashCode(),
                    options = tags,
                )
            }.onFailure {
                val currentProfile = profile ?: return@onFailure
                tagOptionsState = tagOptionsState.applyTagOptionsFailure(
                    request = started.request,
                    currentProfileRevision = currentProfile.hashCode(),
                    errorMessage = it.message,
                )
            }
        }
    }

    fun loadPage(page: Int, reset: Boolean = false) {
        val activeProfile = profile ?: return
        val activeQuery = pageState.query
        if (!pageState.hasExploreIntent) return
        if (!shouldLoadExploreResultsFromServer(activeQuery, pageState.videoFilter)) return
        if (!reset && pageState.isLoading) return
        val activeSort = pageState.sortOption
        val activeDirection = pageState.sortDirection
        val activePageSize = pageState.pageSize
        val activeVideoFilter = pageState.videoFilter
        val requestId = requestSerial + 1L
        requestSerial = requestId
        pageState = if (reset) {
            StashExplorePageState.initial(activeSort)
                .copy(
                    query = activeQuery,
                    sortDirection = activeDirection,
                    pageSize = activePageSize,
                    videoFilter = activeVideoFilter,
                )
                .loading()
        } else {
            pageState.loading()
        }
        scope.launch {
            runCatching {
                StashGraphQlClient(activeProfile).findSceneCardsPage(
                    perPage = activePageSize,
                    page = page,
                    query = activeQuery.ifBlank { null },
                    sort = activeSort.sort,
                    direction = activeDirection,
                    videoFilter = activeVideoFilter,
                )
            }.onSuccess { result ->
                if (
                    requestSerial != requestId ||
                    profile != activeProfile ||
                    pageState.sortOption != activeSort ||
                    pageState.sortDirection != activeDirection ||
                    pageState.pageSize != activePageSize ||
                    pageState.videoFilter != activeVideoFilter ||
                    pageState.query != activeQuery
                ) {
                    return@onSuccess
                }
                pageState = if (page == 1) {
                    StashExplorePageState.initial(activeSort)
                        .copy(
                            query = activeQuery,
                            sortDirection = activeDirection,
                            pageSize = activePageSize,
                            videoFilter = activeVideoFilter,
                        )
                        .withFirstPage(result.scenes, result.totalCount, activePageSize)
                } else {
                    pageState.withNextPage(result.scenes, result.totalCount, activePageSize)
                }
            }.onFailure {
                if (
                    requestSerial != requestId ||
                    profile != activeProfile ||
                    pageState.sortOption != activeSort ||
                    pageState.sortDirection != activeDirection ||
                    pageState.pageSize != activePageSize ||
                    pageState.videoFilter != activeVideoFilter ||
                    pageState.query != activeQuery
                ) {
                    return@onFailure
                }
                pageState = pageState.failed(it.message ?: stashString(R.string.auto_kr_0518))
            }
        }
    }

    fun applyExploreFilterAction(
        action: StashDiscoveryFilterAction,
        saveRecent: Boolean = action.shouldPromoteRecent,
    ) {
        if (action.shouldReload) {
            requestSerial += 1L
            pageState = pageState.withVideoFilter(action.videoFilter)
        }
        if (saveRecent) {
            scope.launch { localRepository.saveRecentExploreVideoFilter(action.videoFilter) }
        }
    }

    LaunchedEffect(inputText) {
        delay(SEARCH_DEBOUNCE_MS)
        val normalizedQuery = normalizeStashDiscoveryQuery(inputText)
        if (normalizedQuery != pageState.query) {
            requestSerial += 1L
            pageState = pageState.withQuery(normalizedQuery)
        }
    }

    LaunchedEffect(persistedExploreFilterState) {
        val persisted = persistedExploreFilterState ?: return@LaunchedEffect
        if (!didApplyPersistedExploreState) {
            val restored = StashExplorePageState.initialFromPersisted(sortOptions, pageSizeOptions, persisted)
            pageState = restored
            inputText = restored.query
            didApplyPersistedExploreState = true
        }
    }

    LaunchedEffect(
        didApplyPersistedExploreState,
        pageState.query,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
    ) {
        if (didApplyPersistedExploreState) {
            localRepository.saveExploreFilterState(pageState.toPersistedFilterState())
        }
    }

    LaunchedEffect(
        profile,
        didApplyPersistedExploreState,
        pageState.query,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
        reloadToken,
    ) {
        if (
            profile != null &&
            didApplyPersistedExploreState &&
            pageState.hasExploreIntent &&
            shouldLoadExploreResultsFromServer(pageState.query, pageState.videoFilter)
        ) {
            loadPage(page = 1, reset = true)
        }
    }

    LaunchedEffect(
        favoriteScenes,
        didApplyPersistedExploreState,
        pageState.query,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
    ) {
        if (didApplyPersistedExploreState && shouldUseLocalFavoriteExploreResults(pageState.query, pageState.videoFilter)) {
            requestSerial += 1L
            pageState = StashExplorePageState.initial(pageState.sortOption)
                .copy(
                    query = pageState.query,
                    sortDirection = pageState.sortDirection,
                    pageSize = pageState.pageSize,
                    videoFilter = pageState.videoFilter,
                )
                .withFirstPage(favoriteScenes, favoriteScenes.size, favoriteScenes.size.coerceAtLeast(1))
        }
    }

    LaunchedEffect(isTagFilterOpen, tagOptionsState.query, profile) {
        if (isTagFilterOpen && profile != null) {
            delay(250L)
            loadTags(tagOptionsState.query)
        }
    }

    StashDiscoveryFilterSheets(
        openSheet = openSheet,
        videoFilter = pageState.videoFilter,
        recentFilters = recentExploreFilters,
        savedFilters = savedVideoFilters,
        savedFilterName = savedFilterName,
        tagOptionsState = tagOptionsState,
        onSavedFilterNameChange = { savedFilterName = it },
        onApplyFilterAction = ::applyExploreFilterAction,
        onOpenSheet = { target -> openSheet = openSheet.open(target) },
        onDismiss = { openSheet = openSheet.dismiss() },
        onSaveCurrentFilter = { name, filterToSave ->
            scope.launch {
                val saved = localRepository.saveVideoFilter(name, filterToSave)
                savedFilterName = ""
                applyExploreFilterAction(
                    applyStashDiscoverySavedFilter(pageState.videoFilter, saved.appliedFilterState()),
                    saveRecent = false,
                )
            }
        },
        onQuickSaveCurrentFilter = { name, filterToSave ->
            scope.launch {
                val saved = localRepository.saveVideoFilter(
                    name = name,
                    filterState = filterToSave,
                    overwriteExisting = false,
                )
                applyExploreFilterAction(
                    applyStashDiscoverySavedFilter(pageState.videoFilter, saved.appliedFilterState()),
                    saveRecent = false,
                )
            }
        },
        onDeleteSavedFilter = { saved ->
            scope.launch {
                localRepository.deleteSavedVideoFilter(saved.id)
                if (pageState.videoFilter.savedFilter?.id == saved.id) {
                    applyExploreFilterAction(
                        clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.SavedFilter),
                        saveRecent = false,
                    )
                }
            }
        },
        onTagQueryChange = { tagOptionsState = tagOptionsState.withTagOptionsQuery(it) },
        onRetryTags = { loadTags(tagOptionsState.query) },
        onToggleLocalFavoriteOnly = {
            val updated = pageState.videoFilter.copy(localFavoriteOnly = !pageState.videoFilter.localFavoriteOnly)
            applyExploreFilterAction(applyStashDiscoveryManualFilter(pageState.videoFilter, updated), saveRecent = false)
        },
        onClearSavedFilter = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.SavedFilter), saveRecent = false)
        },
    )

    ExploreContent(
        isFoldLikeLayout = isFoldLikeLayout,
        inputText = inputText,
        onInputTextChange = { inputText = it },
        onClearInput = {
            inputText = ""
            requestSerial += 1L
            pageState = pageState.withQuery("")
        },
        sortOptions = sortOptions,
        pageSizeOptions = pageSizeOptions,
        pageState = pageState,
        onSelectSort = { option ->
            if (option != pageState.sortOption) {
                requestSerial += 1L
                pageState = pageState.forSort(option)
            }
        },
        onToggleSortDirection = {
            val nextDirection = if (pageState.sortDirection == StashSortDirection.Desc) {
                StashSortDirection.Asc
            } else {
                StashSortDirection.Desc
            }
            requestSerial += 1L
            pageState = pageState.withSortDirection(nextDirection)
        },
        onSelectPageSize = { pageSize ->
            if (pageSize != pageState.pageSize) {
                requestSerial += 1L
                pageState = pageState.withPageSize(pageSize)
            }
        },
        isConfigured = profile != null,
        serverProfile = profile,
        favoriteSceneIds = favoriteSceneIds,
        watchLaterSceneIds = watchLaterSceneIds,
        queueSceneIds = queueSceneIds,
        savedFilterCount = savedVideoFilters.size,
        onOpenSavedFilters = { openSheet = openSheet.open(StashDiscoveryOpenSheet.SavedFilters) },
        onOpenUnifiedFilter = { openSheet = openSheet.open(StashDiscoveryOpenSheet.UnifiedFilterPanel) },
        onOpenTagFilter = { openSheet = openSheet.open(StashDiscoveryOpenSheet.Tags) },
        onOpenDateDurationPlaybackFilter = { openSheet = openSheet.open(StashDiscoveryOpenSheet.DateDurationPlayback) },
        onOpenRatingMediaFormatFilter = { openSheet = openSheet.open(StashDiscoveryOpenSheet.RatingMedia) },
        onOpenLocalLibraryFilter = { openSheet = openSheet.open(StashDiscoveryOpenSheet.LocalLibrary) },
        onClearTags = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.Tags), saveRecent = false)
        },
        onClearDateRange = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.DateRange), saveRecent = false)
        },
        onClearDurationRange = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.DurationRange), saveRecent = false)
        },
        onClearPlaybackState = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.PlaybackState), saveRecent = false)
        },
        onClearRatingRange = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.RatingRange), saveRecent = false)
        },
        onClearMediaFormat = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.MediaFormat), saveRecent = false)
        },
        onClearLocalFavoriteOnly = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.LocalFavorite), saveRecent = false)
        },
        onClearSavedFilter = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.SavedFilter), saveRecent = false)
        },
        onClearRandomShuffle = {
            applyExploreFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.RandomShuffle), saveRecent = false)
        },
        onToggleRandomShuffle = {
            applyExploreFilterAction(applyStashDiscoveryRandomShuffleAction(pageState.videoFilter))
        },
        onToggleFavorite = { scene ->
            val decision = buildStashDiscoveryFavoriteToggleDecision(scene.id, favoriteSceneIds)
            scope.launch {
                localRepository.setFavorite(scene, decision.shouldEnable)
                decision.feedbackText?.let { snackbarHostState.showSnackbar(it) }
            }
        },
        onToggleWatchLater = { scene ->
            val decision = buildStashDiscoveryWatchLaterToggleDecision(scene.id, watchLaterSceneIds)
            scope.launch { localRepository.setWatchLater(scene, decision.shouldEnable) }
        },
        onAddToQueue = { scene ->
            val decision = buildStashDiscoveryQueueAddDecision(scene.id, queueSceneIds)
            scope.launch {
                if (decision.shouldAdd) {
                    localRepository.addToQueue(scene)
                    snackbarHostState.showSnackbar(decision.feedbackText)
                } else {
                    localRepository.removeFromQueue(scene.id)
                    snackbarHostState.showSnackbar(stashString(R.string.scene_card_queue_removed_feedback))
                }
            }
        },
        onDeleteSelectedScenes = { sceneIds, deleteOptions ->
            val activeProfile = profile
            if (activeProfile == null) {
                SceneBulkDeleteResult(
                    requestedSceneIds = sceneIds,
                    deletedSceneIds = emptySet(),
                    failedSceneIds = sceneIds.associateWith { stashString(R.string.auto_kr_0410) },
                )
            } else {
                val result = StashGraphQlClient(activeProfile).deleteScenes(sceneIds, deleteOptions)
                if (result.deletedSceneIds.isNotEmpty()) {
                    localRepository.removeScenesFromLocalSnapshots(result.deletedSceneIds)
                    pageState = pageState.copy(
                        results = pageState.results.withoutBulkDeletedScenes(result),
                        totalCount = pageState.totalCount?.let { (it - result.deletedSceneIds.size).coerceAtLeast(0) },
                    )
                }
                result
            }
        },
        onRetry = {
            if (pageState.results.isEmpty()) {
                reloadToken++
            } else if (!pageState.isLoading) {
                loadPage(pageState.nextPage)
            }
        },
        onLoadMore = {
            if (!pageState.isLoading && pageState.hasMore) {
                loadPage(pageState.nextPage)
            }
        },
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarHostState,
        onOpenScene = { sceneId, scenes, randomShuffle ->
            onOpenScene(
                sceneId,
                scenes,
                randomShuffle,
                if (pageState.query.isBlank()) {
                    PlayerPlaybackQueueContinuation.Browse(
                        sort = pageState.sortOption.sort,
                        direction = pageState.sortDirection,
                        videoFilter = pageState.videoFilter,
                        nextPage = pageState.nextPage,
                        pageSize = pageState.pageSize,
                        hasMore = pageState.hasMore,
                    )
                } else {
                    PlayerPlaybackQueueContinuation.Explore(
                        query = pageState.query,
                        sort = pageState.sortOption.sort,
                        direction = pageState.sortDirection,
                        videoFilter = pageState.videoFilter,
                        nextPage = pageState.nextPage,
                        pageSize = pageState.pageSize,
                        hasMore = pageState.hasMore,
                    )
                },
            )
        },
    )
}

@Composable
private fun ExploreContent(
    isFoldLikeLayout: Boolean,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    sortOptions: List<StashExploreSortOption>,
    pageSizeOptions: List<Int>,
    pageState: StashExplorePageState,
    onSelectSort: (StashExploreSortOption) -> Unit,
    onToggleSortDirection: () -> Unit,
    onSelectPageSize: (Int) -> Unit,
    isConfigured: Boolean,
    serverProfile: StashServerProfile?,
    favoriteSceneIds: Set<String>,
    watchLaterSceneIds: Set<String>,
    queueSceneIds: Set<String>,
    savedFilterCount: Int,
    onOpenSavedFilters: () -> Unit,
    onOpenUnifiedFilter: () -> Unit,
    onOpenTagFilter: () -> Unit,
    onOpenDateDurationPlaybackFilter: () -> Unit,
    onOpenRatingMediaFormatFilter: () -> Unit,
    onOpenLocalLibraryFilter: () -> Unit,
    onClearTags: () -> Unit,
    onClearDateRange: () -> Unit,
    onClearDurationRange: () -> Unit,
    onClearPlaybackState: () -> Unit,
    onClearRatingRange: () -> Unit,
    onClearMediaFormat: () -> Unit,
    onClearLocalFavoriteOnly: () -> Unit,
    onClearSavedFilter: () -> Unit,
    onClearRandomShuffle: () -> Unit,
    onToggleRandomShuffle: () -> Unit,
    onToggleFavorite: (SceneCardModel) -> Unit,
    onToggleWatchLater: (SceneCardModel) -> Unit,
    onAddToQueue: (SceneCardModel) -> Unit,
    onDeleteSelectedScenes: suspend (List<String>, StashSceneDeleteOptions) -> SceneBulkDeleteResult,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onOpenScene: (String, List<SceneCardModel>, Boolean) -> Unit,
) {
    val visualPolicy = stashDiscoveryVisualPolicy()
    val horizontalPadding = if (isFoldLikeLayout) {
        visualPolicy.expandedHorizontalPaddingDp.dp
    } else {
        visualPolicy.compactHorizontalPaddingDp.dp
    }
    val gridGap = visualPolicy.gridItemGapDp.dp
    val bottomContentPadding = visualPolicy.gridBottomPaddingDp.dp
    val columns = stashMediaGridColumnCount(isFoldLikeLayout)
    val thumbnailHeight = stashMediaGridThumbnailHeightDp(isFoldLikeLayout).dp
    val results = pageState.results.applyLocalFavoriteFilter(pageState.videoFilter.localFavoriteOnly, favoriteSceneIds)
    val totalCount = pageState.totalCount
    val visibleResultIds = results.map { it.id }
    var selectionState by remember { mutableStateOf(SceneSelectionState()) }
    var viewMode by remember { mutableStateOf(StashScenesViewMode.Grid) }
    var deleteConfirmation by remember { mutableStateOf(SceneBulkDeleteConfirmationState.Hidden) }
    var previousVisibleResultIds by remember { mutableStateOf(visibleResultIds) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(
        pageState.query,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
    ) {
        selectionState = selectionState.clear()
    }

    LaunchedEffect(visibleResultIds) {
        selectionState = selectionState.clearIfResultIdentityChanged(previousVisibleResultIds, visibleResultIds)
        previousVisibleResultIds = visibleResultIds
    }

    SceneBulkDeleteConfirmationDialog(
        state = deleteConfirmation,
        onConfirmationChange = { deleteConfirmation = deleteConfirmation.withConfirmation(it) },
        onDeleteFileChange = { deleteConfirmation = deleteConfirmation.withDeleteFile(it) },
        onDeleteGeneratedChange = { deleteConfirmation = deleteConfirmation.withDeleteGenerated(it) },
        onCancel = { deleteConfirmation = deleteConfirmation.dismiss() },
        onDelete = {
            val selectedIds = selectionState.selectedSceneIds.toList()
            val deleteOptions = deleteConfirmation.deleteOptions
            deleteConfirmation = deleteConfirmation.deleting()
            scope.launch {
                val result = runCatching { onDeleteSelectedScenes(selectedIds, deleteOptions) }.getOrElse { throwable ->
                    SceneBulkDeleteResult(
                        requestedSceneIds = selectedIds,
                        deletedSceneIds = emptySet(),
                        failedSceneIds = selectedIds.associateWith {
                            redactStashCredentialText(throwable.message ?: stashString(R.string.auto_kr_0411))
                        },
                    )
                }
                selectionState = selectionState.afterBulkDelete(result)
                previousVisibleResultIds = previousVisibleResultIds.withoutBulkDeletedSceneIds(result)
                deleteConfirmation = deleteConfirmation.dismiss()
                snackbarHostState.showSnackbar(result.koreanSummary)
            }
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = StashSpacing.SectionGap),
            verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
        ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stashString(R.string.navigation_explore_label), style = MaterialTheme.typography.headlineLarge)
        }

        StashScenesToolbar(
            horizontalPadding = horizontalPadding,
            isConfigured = isConfigured,
            searchValue = inputText,
            onSearchChange = onInputTextChange,
            onClearSearch = onClearInput,
            sortValue = pageState.sortOption.label,
            sortOptions = sortOptions,
            sortOptionLabel = { it.label },
            onSelectSort = onSelectSort,
            sortDirectionLabel = directionLabel(pageState.sortDirection),
            onToggleSortDirection = onToggleSortDirection,
            pageSizeValue = stashString(R.string.auto_kr_0414, pageState.pageSize),
            pageSizeOptions = pageSizeOptions,
            onSelectPageSize = onSelectPageSize,
            videoFilter = pageState.videoFilter,
            savedFilterCount = savedFilterCount,
            onOpenSavedFilters = onOpenSavedFilters,
            onOpenFilter = onOpenUnifiedFilter,
            onOpenTagFilter = onOpenTagFilter,
            onOpenDateDurationPlaybackFilter = onOpenDateDurationPlaybackFilter,
            onOpenRatingMediaFormatFilter = onOpenRatingMediaFormatFilter,
            onOpenLocalLibraryFilter = onOpenLocalLibraryFilter,
            onToggleRandomShuffle = onToggleRandomShuffle,
            showFilterShortcuts = false,
            viewMode = viewMode,
            onToggleViewMode = {
                viewMode = if (viewMode == StashScenesViewMode.Grid) StashScenesViewMode.List else StashScenesViewMode.Grid
            },
            selectionCount = selectionState.selectedCount,
            visibleCount = results.size,
            onClearSelection = { selectionState = selectionState.clear() },
            onSelectAll = { selectionState = selectionState.selectVisibleScenes(results.map { it.id }) },
            onInvertSelection = { selectionState = selectionState.invertVisibleSelection(results.map { it.id }) },
            onPlaySelection = {
                selectionState.toolbarPlaybackRequest(results)?.let { request ->
                    selectionState = request.nextSelectionState
                    onOpenScene(request.startSceneId, request.scenes, pageState.videoFilter.randomShuffle)
                }
            },
            onDeleteSelection = { deleteConfirmation = SceneBulkDeleteConfirmationState.open(selectionState.selectedCount) },
        )

        if (selectionState.selectedCount == 0) {
            StashVideoFilterGroupRow(
                horizontalPadding = horizontalPadding,
                isConfigured = isConfigured,
                videoFilter = pageState.videoFilter,
                savedFilterCount = savedFilterCount,
                onOpenSavedFilters = onOpenSavedFilters,
                onOpenFilter = onOpenUnifiedFilter,
                onOpenTagFilter = onOpenTagFilter,
                onOpenDateDurationPlaybackFilter = onOpenDateDurationPlaybackFilter,
                onOpenRatingMediaFormatFilter = onOpenRatingMediaFormatFilter,
                onOpenLocalLibraryFilter = onOpenLocalLibraryFilter,
            )
        }

        StashActiveVideoFilterChipsRow(
            videoFilter = pageState.videoFilter,
            horizontalPadding = horizontalPadding,
            onTagClick = onOpenTagFilter,
            onClearTags = onClearTags,
            onDateDurationPlaybackClick = onOpenDateDurationPlaybackFilter,
            onClearDateRange = onClearDateRange,
            onClearDurationRange = onClearDurationRange,
            onClearPlaybackState = onClearPlaybackState,
            onRatingMediaFormatClick = onOpenRatingMediaFormatFilter,
            onClearRatingRange = onClearRatingRange,
            onClearMediaFormat = onClearMediaFormat,
            onLocalLibraryClick = onOpenLocalLibraryFilter,
            onClearLocalFavoriteOnly = onClearLocalFavoriteOnly,
            onClearSavedFilter = onClearSavedFilter,
            onRandomShuffleClick = onToggleRandomShuffle,
            onClearRandomShuffle = onClearRandomShuffle,
        )

        if (pageState.hasExploreIntent) {
            Column(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stashString(
                        R.string.auto_kr_0521,
                        pageState.query.ifBlank { stashString(R.string.auto_kr_0520) },
                        pageState.activeFilterCount,
                        pageState.sortOption.label,
                        directionSymbol(pageState.sortDirection),
                        pageState.pageSize,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (results.isNotEmpty() || totalCount != null) {
                    Text(
                        text = stashDiscoveryResultCountLabel(totalCount, results.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        when {
            !isConfigured -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stashString(R.string.auto_kr_0415),
                    message = stashString(R.string.explore_requires_server_message),
                    primaryActionLabel = stashString(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onPrimaryAction = onOpenSettings,
            )
            !pageState.hasExploreIntent -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stashString(R.string.explore_empty_prompt_title),
                    message = stashString(R.string.explore_empty_prompt_message),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
            pageState.isLoading && results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            pageState.error != null && results.isEmpty() -> StashErrorState(
                state = StashErrorStateModel(
                    title = stashString(R.string.explore_load_failed_title),
                    message = pageState.error,
                    secondaryActionLabel = stashString(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onRetry = onRetry,
                onSecondaryAction = onOpenSettings,
            )
            else -> {
                if (viewMode == StashScenesViewMode.Grid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, bottom = bottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
                        horizontalArrangement = Arrangement.spacedBy(gridGap),
                    ) {
                        items(results, key = { it.id }) { scene ->
                            SceneCard(
                                scene = scene,
                                thumbnailHeight = thumbnailHeight,
                                thumbnailModel = rememberStashThumbnailModel(scene.thumbnailUrl, serverProfile),
                                isLocalFavorite = scene.id in favoriteSceneIds,
                                isInWatchLater = scene.id in watchLaterSceneIds,
                                isInQueue = scene.id in queueSceneIds,
                                isSelected = scene.id in selectionState.selectedSceneIds,
                                onToggleFavorite = { onToggleFavorite(scene) },
                                onToggleWatchLater = { onToggleWatchLater(scene) },
                                onAddToQueue = { onAddToQueue(scene) },
                                showQuickActions = shouldShowSceneCardQuickActionsInMediaGrid(),
                                onLongClick = { selectionState = selectionState.selectFromLongPress(scene.id) },
                                onClick = {
                                    val tapResult = selectionState.handleCardTap(scene.id)
                                    selectionState = tapResult.state
                                    if (tapResult.shouldOpenScene) {
                                        onOpenScene(scene.id, results, pageState.videoFilter.randomShuffle)
                                    }
                                },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ExploreFooter(
                                pageState = pageState,
                                onRetry = onRetry,
                                onLoadMore = onLoadMore,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, bottom = bottomContentPadding),
                        verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
                    ) {
                        lazyItems(results, key = { it.id }) { scene ->
                            SceneCard(
                                scene = scene,
                                thumbnailHeight = thumbnailHeight,
                                thumbnailModel = rememberStashThumbnailModel(scene.thumbnailUrl, serverProfile),
                                isLocalFavorite = scene.id in favoriteSceneIds,
                                isInWatchLater = scene.id in watchLaterSceneIds,
                                isInQueue = scene.id in queueSceneIds,
                                isSelected = scene.id in selectionState.selectedSceneIds,
                                onToggleFavorite = { onToggleFavorite(scene) },
                                onToggleWatchLater = { onToggleWatchLater(scene) },
                                onAddToQueue = { onAddToQueue(scene) },
                                showQuickActions = shouldShowSceneCardQuickActionsInMediaGrid(),
                                onLongClick = { selectionState = selectionState.selectFromLongPress(scene.id) },
                                onClick = {
                                    val tapResult = selectionState.handleCardTap(scene.id)
                                    selectionState = tapResult.state
                                    if (tapResult.shouldOpenScene) {
                                        onOpenScene(scene.id, results, pageState.videoFilter.randomShuffle)
                                    }
                                },
                            )
                        }
                        item {
                            ExploreFooter(
                                pageState = pageState,
                                onRetry = onRetry,
                                onLoadMore = onLoadMore,
                            )
                        }
                    }
                }
            }
        }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ExploreFooter(
    pageState: StashExplorePageState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            pageState.isLoading -> CircularProgressIndicator()
            pageState.error != null -> StashSecondaryButton(text = stashString(R.string.auto_kr_0031), onClick = onRetry)
            pageState.hasMore -> StashSecondaryButton(text = stashString(R.string.auto_kr_0418), onClick = onLoadMore)
            pageState.results.isEmpty() -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stashString(R.string.explore_no_results_title),
                    message = stashString(R.string.explore_no_results_message),
                ),
            )
            else -> Text(stashString(R.string.auto_kr_0528), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun directionLabel(direction: StashSortDirection): String = when (direction) {
    StashSortDirection.Desc -> stashString(R.string.auto_kr_0422)
    StashSortDirection.Asc -> stashString(R.string.auto_kr_0423)
}

private fun directionSymbol(direction: StashSortDirection): String = when (direction) {
    StashSortDirection.Desc -> "↓"
    StashSortDirection.Asc -> "↑"
}

