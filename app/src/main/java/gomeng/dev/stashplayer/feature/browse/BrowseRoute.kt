package gomeng.dev.stashplayer.feature.browse

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import gomeng.dev.stashplayer.core.model.StashBrowseScenePageState
import gomeng.dev.stashplayer.core.model.StashBrowseSortOption
import gomeng.dev.stashplayer.core.model.StashScenesViewMode
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.defaultStashBrowseSortOptions
import gomeng.dev.stashplayer.core.model.defaultStashDiscoveryPageSizeOptions
import gomeng.dev.stashplayer.core.model.initialFromPersisted
import gomeng.dev.stashplayer.core.model.shouldShowSceneCardQuickActionsInMediaGrid
import gomeng.dev.stashplayer.core.model.stashDiscoveryResultCountLabel
import gomeng.dev.stashplayer.core.model.stashMediaGridColumnCount
import gomeng.dev.stashplayer.core.model.stashMediaGridThumbnailHeightDp
import gomeng.dev.stashplayer.core.model.toPersistedFilterState
import gomeng.dev.stashplayer.core.model.afterBulkDelete
import gomeng.dev.stashplayer.core.model.withoutBulkDeletedSceneIds
import gomeng.dev.stashplayer.core.model.withoutBulkDeletedScenes
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueueContinuation
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun BrowseRoute(
    isFoldLikeLayout: Boolean,
    onOpenScene: (String, List<SceneCardModel>, Boolean, PlayerPlaybackQueueContinuation?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val sortOptions = remember { defaultStashBrowseSortOptions() }
    val pageSizeOptions = remember { defaultStashDiscoveryPageSizeOptions() }
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val favoriteSceneIds by localRepository.favoriteSceneIds.collectAsState(initial = emptySet())
    val watchLaterSceneIds by localRepository.watchLaterSceneIds.collectAsState(initial = emptySet())
    val queueSceneIds by localRepository.queueSceneIds.collectAsState(initial = emptySet())
    val savedVideoFilters by localRepository.savedVideoFilters.collectAsState(initial = emptyList())
    val persistedBrowseFilterState by localRepository.persistedBrowseFilterState.collectAsState(initial = null)
    val recentBrowseFilters by localRepository.recentBrowseVideoFilters.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var pageState by remember { mutableStateOf(StashBrowseScenePageState.initial(sortOptions.first())) }
    var didApplyPersistedBrowseState by remember { mutableStateOf(false) }
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
        if (!reset && pageState.isLoading) return
        val activeSort = pageState.sortOption
        val activeDirection = pageState.sortDirection
        val activePageSize = pageState.pageSize
        val activeVideoFilter = pageState.videoFilter
        val requestId = requestSerial + 1L
        requestSerial = requestId
        pageState = if (reset) {
            StashBrowseScenePageState.initial(
                sortOption = activeSort,
                videoFilter = activeVideoFilter,
                sortDirection = activeDirection,
                pageSize = activePageSize,
            ).loading()
        } else {
            pageState.loading()
        }
        scope.launch {
            runCatching {
                StashGraphQlClient(activeProfile).findSceneCardsPage(
                    perPage = activePageSize,
                    page = page,
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
                    pageState.videoFilter != activeVideoFilter
                ) {
                    return@onSuccess
                }
                pageState = if (page == 1) {
                    StashBrowseScenePageState.initial(
                        sortOption = activeSort,
                        videoFilter = activeVideoFilter,
                        sortDirection = activeDirection,
                        pageSize = activePageSize,
                    ).withFirstPage(
                        scenes = result.scenes,
                        totalCount = result.totalCount,
                        perPage = activePageSize,
                    )
                } else {
                    pageState.withNextPage(
                        scenes = result.scenes,
                        totalCount = result.totalCount,
                        perPage = activePageSize,
                    )
                }
            }.onFailure {
                if (
                    requestSerial != requestId ||
                    profile != activeProfile ||
                    pageState.sortOption != activeSort ||
                    pageState.sortDirection != activeDirection ||
                    pageState.pageSize != activePageSize ||
                    pageState.videoFilter != activeVideoFilter
                ) {
                    return@onFailure
                }
                pageState = pageState.failed(it.message ?: stashString(R.string.auto_kr_0409))
            }
        }
    }

    fun applyBrowseFilterAction(
        action: StashDiscoveryFilterAction,
        saveRecent: Boolean = action.shouldPromoteRecent,
    ) {
        if (action.shouldReload) {
            requestSerial += 1L
            pageState = pageState.withVideoFilter(action.videoFilter)
        }
        if (saveRecent) {
            scope.launch { localRepository.saveRecentBrowseVideoFilter(action.videoFilter) }
        }
    }

    LaunchedEffect(persistedBrowseFilterState) {
        val persisted = persistedBrowseFilterState ?: return@LaunchedEffect
        if (!didApplyPersistedBrowseState) {
            pageState = StashBrowseScenePageState.initialFromPersisted(
                sortOptions = sortOptions,
                persisted = persisted,
                pageSizeOptions = pageSizeOptions,
            )
            didApplyPersistedBrowseState = true
        }
    }

    LaunchedEffect(
        didApplyPersistedBrowseState,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
    ) {
        if (didApplyPersistedBrowseState) {
            localRepository.saveBrowseFilterState(pageState.toPersistedFilterState())
        }
    }

    LaunchedEffect(
        profile,
        didApplyPersistedBrowseState,
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
        reloadToken,
    ) {
        if (profile != null && didApplyPersistedBrowseState) {
            loadPage(page = 1, reset = true)
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
        recentFilters = recentBrowseFilters,
        savedFilters = savedVideoFilters,
        savedFilterName = savedFilterName,
        tagOptionsState = tagOptionsState,
        onSavedFilterNameChange = { savedFilterName = it },
        onApplyFilterAction = ::applyBrowseFilterAction,
        onOpenSheet = { target -> openSheet = openSheet.open(target) },
        onDismiss = { openSheet = openSheet.dismiss() },
        onSaveCurrentFilter = { name, filterToSave ->
            scope.launch {
                val saved = localRepository.saveVideoFilter(name, filterToSave)
                savedFilterName = ""
                applyBrowseFilterAction(
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
                applyBrowseFilterAction(
                    applyStashDiscoverySavedFilter(pageState.videoFilter, saved.appliedFilterState()),
                    saveRecent = false,
                )
            }
        },
        onDeleteSavedFilter = { saved ->
            scope.launch {
                localRepository.deleteSavedVideoFilter(saved.id)
                if (pageState.videoFilter.savedFilter?.id == saved.id) {
                    applyBrowseFilterAction(
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
            applyBrowseFilterAction(applyStashDiscoveryManualFilter(pageState.videoFilter, updated), saveRecent = false)
        },
        onClearSavedFilter = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.SavedFilter), saveRecent = false)
        },
    )

    BrowseContent(
        isFoldLikeLayout = isFoldLikeLayout,
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
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.Tags), saveRecent = false)
        },
        onClearDateRange = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.DateRange), saveRecent = false)
        },
        onClearDurationRange = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.DurationRange), saveRecent = false)
        },
        onClearPlaybackState = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.PlaybackState), saveRecent = false)
        },
        onClearRatingRange = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.RatingRange), saveRecent = false)
        },
        onClearMediaFormat = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.MediaFormat), saveRecent = false)
        },
        onClearLocalFavoriteOnly = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.LocalFavorite), saveRecent = false)
        },
        onClearSavedFilter = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.SavedFilter), saveRecent = false)
        },
        onClearRandomShuffle = {
            applyBrowseFilterAction(clearStashDiscoveryFilterSection(pageState.videoFilter, StashDiscoveryFilterSection.RandomShuffle), saveRecent = false)
        },
        onToggleRandomShuffle = {
            applyBrowseFilterAction(applyStashDiscoveryRandomShuffleAction(pageState.videoFilter))
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
                        scenes = pageState.scenes.withoutBulkDeletedScenes(result),
                        totalCount = pageState.totalCount?.let { (it - result.deletedSceneIds.size).coerceAtLeast(0) },
                    )
                }
                result
            }
        },
        onRetry = {
            if (pageState.scenes.isEmpty()) {
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
                PlayerPlaybackQueueContinuation.Browse(
                    sort = pageState.sortOption.sort,
                    direction = pageState.sortDirection,
                    videoFilter = pageState.videoFilter,
                    nextPage = pageState.nextPage,
                    pageSize = pageState.pageSize,
                    hasMore = pageState.hasMore,
                ),
            )
        },
    )
}

@Composable
private fun BrowseContent(
    isFoldLikeLayout: Boolean,
    sortOptions: List<StashBrowseSortOption>,
    pageSizeOptions: List<Int>,
    pageState: StashBrowseScenePageState,
    onSelectSort: (StashBrowseSortOption) -> Unit,
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
    val scenes = pageState.scenes.applyLocalFavoriteFilter(pageState.videoFilter.localFavoriteOnly, favoriteSceneIds)
    val totalCount = pageState.totalCount
    val visibleSceneIds = scenes.map { it.id }
    var selectionState by remember { mutableStateOf(SceneSelectionState()) }
    var viewMode by remember { mutableStateOf(StashScenesViewMode.Grid) }
    var deleteConfirmation by remember { mutableStateOf(SceneBulkDeleteConfirmationState.Hidden) }
    var previousVisibleSceneIds by remember { mutableStateOf(visibleSceneIds) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(
        pageState.sortOption,
        pageState.sortDirection,
        pageState.pageSize,
        pageState.videoFilter,
    ) {
        selectionState = selectionState.clear()
    }

    LaunchedEffect(visibleSceneIds) {
        selectionState = selectionState.clearIfResultIdentityChanged(previousVisibleSceneIds, visibleSceneIds)
        previousVisibleSceneIds = visibleSceneIds
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
                previousVisibleSceneIds = previousVisibleSceneIds.withoutBulkDeletedSceneIds(result)
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
            Text(stashString(R.string.auto_kr_0412), style = MaterialTheme.typography.headlineLarge)
            Text(
                text = stashString(R.string.auto_kr_0413),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (scenes.isNotEmpty() || totalCount != null) {
                Text(
                    text = stashDiscoveryResultCountLabel(totalCount, scenes.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        StashScenesToolbar(
            horizontalPadding = horizontalPadding,
            isConfigured = isConfigured,
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
            viewMode = viewMode,
            onToggleViewMode = {
                viewMode = if (viewMode == StashScenesViewMode.Grid) StashScenesViewMode.List else StashScenesViewMode.Grid
            },
            selectionCount = selectionState.selectedCount,
            visibleCount = scenes.size,
            onClearSelection = { selectionState = selectionState.clear() },
            onSelectAll = { selectionState = selectionState.selectVisibleScenes(scenes.map { it.id }) },
            onInvertSelection = { selectionState = selectionState.invertVisibleSelection(scenes.map { it.id }) },
            onPlaySelection = {
                selectionState.toolbarPlaybackRequest(scenes)?.let { request ->
                    selectionState = request.nextSelectionState
                    onOpenScene(request.startSceneId, request.scenes, pageState.videoFilter.randomShuffle)
                }
            },
            onDeleteSelection = { deleteConfirmation = SceneBulkDeleteConfirmationState.open(selectionState.selectedCount) },
        )

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

        when {
            !isConfigured -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stashString(R.string.auto_kr_0415),
                    message = stashString(R.string.auto_kr_0416),
                    primaryActionLabel = stashString(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onPrimaryAction = onOpenSettings,
            )
            pageState.isLoading && scenes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            pageState.error != null && scenes.isEmpty() -> StashErrorState(
                state = StashErrorStateModel(
                    title = stashString(R.string.auto_kr_0417),
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
                        items(scenes, key = { it.id }) { scene ->
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
                                        onOpenScene(scene.id, scenes, pageState.videoFilter.randomShuffle)
                                    }
                                },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            BrowseFooter(
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
                        lazyItems(scenes, key = { it.id }) { scene ->
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
                                        onOpenScene(scene.id, scenes, pageState.videoFilter.randomShuffle)
                                    }
                                },
                            )
                        }
                        item {
                            BrowseFooter(
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
private fun BrowseFooter(
    pageState: StashBrowseScenePageState,
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
            pageState.scenes.isEmpty() -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stashString(R.string.auto_kr_0419),
                    message = stashString(R.string.auto_kr_0420),
                ),
            )
            else -> Text(stashString(R.string.auto_kr_0421), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun directionLabel(direction: StashSortDirection): String = when (direction) {
    StashSortDirection.Desc -> stashString(R.string.auto_kr_0422)
    StashSortDirection.Asc -> stashString(R.string.auto_kr_0423)
}
