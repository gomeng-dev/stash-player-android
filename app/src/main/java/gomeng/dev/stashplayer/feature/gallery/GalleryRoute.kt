package gomeng.dev.stashplayer.feature.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.local.LocalSavedGalleryFilter
import gomeng.dev.stashplayer.core.local.LocalSavedImageFilter
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.local.appliedFilterState
import gomeng.dev.stashplayer.core.model.ContentScalePolicy
import gomeng.dev.stashplayer.core.model.GalleryAppreciationModeState
import gomeng.dev.stashplayer.core.model.GalleryCardModel
import gomeng.dev.stashplayer.core.model.GalleryFileInfoModel
import gomeng.dev.stashplayer.core.model.GalleryImageFolderGroup
import gomeng.dev.stashplayer.core.model.GalleryImageModel
import gomeng.dev.stashplayer.core.model.GalleryImageOCounterAction
import gomeng.dev.stashplayer.core.model.GalleryPhotoDetailLabels
import gomeng.dev.stashplayer.core.model.GalleryPhotoDisplayMode
import gomeng.dev.stashplayer.core.model.GalleryPhotoSlideshowAction
import gomeng.dev.stashplayer.core.model.GalleryPhotoSlideshowTrigger
import gomeng.dev.stashplayer.core.model.GalleryPhotoTapAction
import gomeng.dev.stashplayer.core.model.GalleryPhotoZoomState
import gomeng.dev.stashplayer.core.model.GallerySelectionState
import gomeng.dev.stashplayer.core.model.StashGalleryBrowseMode
import gomeng.dev.stashplayer.core.model.StashGalleryDetailModel
import gomeng.dev.stashplayer.core.model.StashGalleryDisplayMode
import gomeng.dev.stashplayer.core.model.StashGalleryFilterCategory
import gomeng.dev.stashplayer.core.model.StashGalleryGlobalImageGridPageState
import gomeng.dev.stashplayer.core.model.StashGalleryImageGridPageState
import gomeng.dev.stashplayer.core.model.StashGalleryFilterState
import gomeng.dev.stashplayer.core.model.StashGalleryGridPageState
import gomeng.dev.stashplayer.core.model.StashImageFilterCategory
import gomeng.dev.stashplayer.core.model.StashImageFilterState
import gomeng.dev.stashplayer.core.model.StashGalleryMediaControl
import gomeng.dev.stashplayer.core.model.StashGallerySortOption
import gomeng.dev.stashplayer.core.model.StashSelectedEntity
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.defaultStashDiscoveryPageSizeOptions
import gomeng.dev.stashplayer.core.model.galleryFileInfoLabels
import gomeng.dev.stashplayer.core.model.galleryImageLinkedGalleryLabels
import gomeng.dev.stashplayer.core.model.galleryImageMetadataLabels
import gomeng.dev.stashplayer.core.model.galleryImagePerformerLabels
import gomeng.dev.stashplayer.core.model.galleryPhotoDetailRows
import gomeng.dev.stashplayer.core.model.galleryPhotoViewerChromePolicy
import gomeng.dev.stashplayer.core.model.galleryPhotoViewerOCounterToolbarActions
import gomeng.dev.stashplayer.core.model.groupGalleryImagesByParentFolder
import gomeng.dev.stashplayer.core.model.galleryMetadataLabels
import gomeng.dev.stashplayer.core.model.galleryPhotoViewerPagePolicy
import gomeng.dev.stashplayer.core.model.galleryPhotoViewerPagerSwipeEnabled
import gomeng.dev.stashplayer.core.model.galleryPhotoViewerPreloadImageUrls
import gomeng.dev.stashplayer.core.model.galleryPhotoZoomOnDoubleTap
import gomeng.dev.stashplayer.core.model.galleryPhotoZoomShouldHandleTransform
import gomeng.dev.stashplayer.core.model.galleryPhotoZoomTransform
import gomeng.dev.stashplayer.core.model.galleryPreviewSnippet
import gomeng.dev.stashplayer.core.model.galleryRelationshipChipLabels
import gomeng.dev.stashplayer.core.model.optimisticGalleryImageOCounter
import gomeng.dev.stashplayer.core.model.resolveGalleryPhotoLinkedGalleryNavigation
import gomeng.dev.stashplayer.core.model.resolveGalleryPhotoSlideshowAction
import gomeng.dev.stashplayer.core.model.resolveGalleryPhotoTapAction
import gomeng.dev.stashplayer.core.model.shouldAutoHideGalleryPhotoChrome
import gomeng.dev.stashplayer.core.model.shouldRevealGalleryPhotoChromeAfterTap
import gomeng.dev.stashplayer.core.model.shouldStartGalleryImageMutation
import gomeng.dev.stashplayer.core.model.toContentScalePolicy
import gomeng.dev.stashplayer.core.model.toggleGalleryPhotoDisplayMode
import gomeng.dev.stashplayer.core.model.shouldShowGalleryPhotoPageChangeHud
import gomeng.dev.stashplayer.core.model.targetImageIndex
import gomeng.dev.stashplayer.core.model.quickSavedGalleryFilterName
import gomeng.dev.stashplayer.core.model.quickSavedImageFilterName
import gomeng.dev.stashplayer.core.model.shouldPromoteRecentGalleryFilterAfterChange
import gomeng.dev.stashplayer.core.model.shouldPromoteRecentImageFilterAfterChange
import gomeng.dev.stashplayer.core.model.clearStashGalleryFilterCategory
import gomeng.dev.stashplayer.core.model.clearStashImageFilterCategory
import gomeng.dev.stashplayer.core.model.normalizeStashDiscoveryQuery
import gomeng.dev.stashplayer.core.model.normalizedGalleryEntities
import gomeng.dev.stashplayer.core.model.stashGalleryGridLayoutPolicy
import gomeng.dev.stashplayer.core.model.stashGalleryMediaToolbarPolicy
import gomeng.dev.stashplayer.core.model.stashGallerySortOptions
import gomeng.dev.stashplayer.core.model.stashGalleryTopLevelHeaderPolicy
import gomeng.dev.stashplayer.core.model.stashGalleryDisplayModes
import gomeng.dev.stashplayer.core.model.stashGalleryImageDisplayModes
import gomeng.dev.stashplayer.core.model.stashGalleryImageGridColumnCount
import gomeng.dev.stashplayer.core.model.stashGalleryImageGridLayoutPolicy
import gomeng.dev.stashplayer.core.model.stashGalleryImageGridThumbnailHeightDp
import gomeng.dev.stashplayer.core.model.stashImageSortOptions
import gomeng.dev.stashplayer.core.model.switchStashGalleryBrowseMode
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.buildStashThumbnailRequestSpec
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.player.ImageViewerTransportVisualPolicy
import gomeng.dev.stashplayer.core.player.PlayerOverlayTransportUiState
import gomeng.dev.stashplayer.core.player.PlayerRatingState
import gomeng.dev.stashplayer.core.player.buildImageViewerTransportButtonVisualStyles
import gomeng.dev.stashplayer.core.ui.components.StashEntityFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashGallerySavedFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashImageSavedFilterSheet
import gomeng.dev.stashplayer.core.ui.components.rememberStashThumbnailModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyState
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashErrorState
import gomeng.dev.stashplayer.core.ui.designsystem.StashErrorStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashMediaCard
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadge
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadgeModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashScreenHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSecondaryButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.stashThumbnailClip
import gomeng.dev.stashplayer.core.ui.discovery.StashDiscoverySearchInput
import gomeng.dev.stashplayer.core.ui.discovery.StashGalleryToolbar
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import gomeng.dev.stashplayer.feature.player.PlayerOverlayTransportControls
import gomeng.dev.stashplayer.feature.player.PlayerRatingControls
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val GALLERY_GRID_DEFAULT_PAGE_SIZE = 24
private const val GALLERY_IMAGE_GRID_DEFAULT_PAGE_SIZE = 60
private const val GALLERY_SEARCH_DEBOUNCE_MS = 500L
private const val GALLERY_PHOTO_CHROME_AUTO_HIDE_MS = 3_000L
private const val GALLERY_PHOTO_BOUNDARY_FEEDBACK_MS = 1_500L
private const val GALLERY_PHOTO_TRANSIENT_HUD_MS = 1_300L
private const val GALLERY_ENTITY_SELECTOR_PAGE_SIZE = 40

internal enum class GalleryEntityFilterKind {
    Tags,
    Studios,
    Performers,
    Scenes,
    ParentFolders,
}

internal enum class ImageEntityFilterKind {
    Tags,
    PerformerTags,
    Studios,
    Performers,
    Galleries,
}

private fun ImageEntityFilterKind.selectedFrom(filter: StashImageFilterState): List<StashSelectedEntity> = when (this) {
    ImageEntityFilterKind.Tags -> filter.tags
    ImageEntityFilterKind.PerformerTags -> filter.performerTags
    ImageEntityFilterKind.Studios -> filter.studios
    ImageEntityFilterKind.Performers -> filter.performers
    ImageEntityFilterKind.Galleries -> filter.galleries
}

private fun ImageEntityFilterKind.withSelected(
    filter: StashImageFilterState,
    selected: List<StashSelectedEntity>,
): StashImageFilterState {
    val normalized = selected.normalizedGalleryEntities()
    return when (this) {
        ImageEntityFilterKind.Tags -> filter.copy(tags = normalized)
        ImageEntityFilterKind.PerformerTags -> filter.copy(performerTags = normalized)
        ImageEntityFilterKind.Studios -> filter.copy(studios = normalized)
        ImageEntityFilterKind.Performers -> filter.copy(performers = normalized)
        ImageEntityFilterKind.Galleries -> filter.copy(galleries = normalized)
    }
}

private fun GalleryEntityFilterKind.selectedFrom(filter: StashGalleryFilterState): List<StashSelectedEntity> = when (this) {
    GalleryEntityFilterKind.Tags -> filter.tags
    GalleryEntityFilterKind.Studios -> filter.studios
    GalleryEntityFilterKind.Performers -> filter.performers
    GalleryEntityFilterKind.Scenes -> filter.scenes
    GalleryEntityFilterKind.ParentFolders -> filter.parentFolders
}

private fun GalleryEntityFilterKind.withSelected(
    filter: StashGalleryFilterState,
    selected: List<StashSelectedEntity>,
): StashGalleryFilterState {
    val normalized = selected.normalizedGalleryEntities()
    return when (this) {
        GalleryEntityFilterKind.Tags -> filter.copy(tags = normalized)
        GalleryEntityFilterKind.Studios -> filter.copy(studios = normalized)
        GalleryEntityFilterKind.Performers -> filter.copy(performers = normalized)
        GalleryEntityFilterKind.Scenes -> filter.copy(scenes = normalized)
        GalleryEntityFilterKind.ParentFolders -> filter.copy(parentFolders = normalized)
    }
}

private fun List<StashSelectedEntity>.toggleGalleryEntity(entity: StashSelectedEntity): List<StashSelectedEntity> {
    val normalizedEntity = listOf(entity).normalizedGalleryEntities().firstOrNull() ?: return this.normalizedGalleryEntities()
    return if (any { it.id == normalizedEntity.id }) {
        filterNot { it.id == normalizedEntity.id }
    } else {
        (this + normalizedEntity).normalizedGalleryEntities()
    }
}

@Composable
private fun ImageEntityFilterKind.title(): String = when (this) {
    ImageEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_sheet_tags_title)
    ImageEntityFilterKind.PerformerTags -> stringResource(R.string.gallery_filter_sheet_performer_tags_title)
    ImageEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_sheet_studios_title)
    ImageEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_sheet_performers_title)
    ImageEntityFilterKind.Galleries -> stringResource(R.string.gallery_filter_sheet_galleries_title)
}

@Composable
private fun ImageEntityFilterKind.searchLabel(): String = when (this) {
    ImageEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_sheet_tags_search)
    ImageEntityFilterKind.PerformerTags -> stringResource(R.string.gallery_filter_sheet_performer_tags_search)
    ImageEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_sheet_studios_search)
    ImageEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_sheet_performers_search)
    ImageEntityFilterKind.Galleries -> stringResource(R.string.gallery_filter_sheet_galleries_search)
}

@Composable
private fun ImageEntityFilterKind.buttonLabel(count: Int): String = when (this) {
    ImageEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_button_tags, count)
    ImageEntityFilterKind.PerformerTags -> stringResource(R.string.gallery_filter_button_performer_tags, count)
    ImageEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_button_studios, count)
    ImageEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_button_performers, count)
    ImageEntityFilterKind.Galleries -> stringResource(R.string.gallery_filter_button_galleries, count)
}

@Composable
private fun GalleryEntityFilterKind.title(): String = when (this) {
    GalleryEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_sheet_tags_title)
    GalleryEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_sheet_studios_title)
    GalleryEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_sheet_performers_title)
    GalleryEntityFilterKind.Scenes -> stringResource(R.string.gallery_filter_sheet_scenes_title)
    GalleryEntityFilterKind.ParentFolders -> stringResource(R.string.gallery_filter_sheet_parent_folders_title)
}

@Composable
private fun GalleryEntityFilterKind.searchLabel(): String = when (this) {
    GalleryEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_sheet_tags_search)
    GalleryEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_sheet_studios_search)
    GalleryEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_sheet_performers_search)
    GalleryEntityFilterKind.Scenes -> stringResource(R.string.gallery_filter_sheet_scenes_search)
    GalleryEntityFilterKind.ParentFolders -> stringResource(R.string.gallery_filter_sheet_parent_folders_search)
}

@Composable
private fun GalleryEntityFilterKind.buttonLabel(count: Int): String = when (this) {
    GalleryEntityFilterKind.Tags -> stringResource(R.string.gallery_filter_button_tags, count)
    GalleryEntityFilterKind.Studios -> stringResource(R.string.gallery_filter_button_studios, count)
    GalleryEntityFilterKind.Performers -> stringResource(R.string.gallery_filter_button_performers, count)
    GalleryEntityFilterKind.Scenes -> stringResource(R.string.gallery_filter_button_scenes, count)
    GalleryEntityFilterKind.ParentFolders -> stringResource(R.string.gallery_filter_button_parent_folders, count)
}

internal data class GalleryRouteResetLoadKey(
    val profileRevision: Int,
    val browseMode: StashGalleryBrowseMode,
    val query: String,
    val sortOption: Any,
    val sortDirection: Any,
    val displayMode: StashGalleryDisplayMode?,
    val filterIdentity: Any,
    val randomSeed: Int?,
    val pageSize: Int,
    val reloadToken: Long,
)

internal data class GalleryDetailRouteResetLoadKey(
    val profileRevision: Int,
    val galleryId: String,
    val pageSize: Int,
    val reloadToken: Long,
)

internal class GalleryRouteStateViewModel : ViewModel() {
    var galleryInputText by mutableStateOf("")
    var imageInputText by mutableStateOf("")
    var browseMode by mutableStateOf(StashGalleryBrowseMode.Galleries)
    var reloadToken by mutableLongStateOf(0L)
    var imageReloadToken by mutableLongStateOf(0L)
    var requestSerial by mutableLongStateOf(0L)
    var imageRequestSerial by mutableLongStateOf(0L)
    var pageState by mutableStateOf(StashGalleryGridPageState.initial().copy(pageSize = GALLERY_GRID_DEFAULT_PAGE_SIZE))
    var imagePageState by mutableStateOf(StashGalleryGlobalImageGridPageState.initial().copy(pageSize = GALLERY_IMAGE_GRID_DEFAULT_PAGE_SIZE))
    var galleryToolbarPreferencesRestored by mutableStateOf(false)
    var imageToolbarPreferencesRestored by mutableStateOf(false)
    var galleryBrowseModeRestored by mutableStateOf(false)
    var activeEntityFilter by mutableStateOf<GalleryEntityFilterKind?>(null)
    var activeImageEntityFilter by mutableStateOf<ImageEntityFilterKind?>(null)
    var entitySearchQuery by mutableStateOf("")
    var entityOptions by mutableStateOf(emptyList<StashSelectedEntity>())
    var entityDraft by mutableStateOf(emptyList<StashSelectedEntity>())
    var entityOptionsLoading by mutableStateOf(false)
    var showSavedFilterSheet by mutableStateOf(false)
    var showSavedImageFilterSheet by mutableStateOf(false)
    var savedGalleryFilterName by mutableStateOf("")
    var savedImageFilterName by mutableStateOf("")
    var selectedImageFolderPath by mutableStateOf<String?>(null)
    var gallerySelectionState by mutableStateOf(GallerySelectionState())
    var previousVisibleGalleryIds by mutableStateOf(emptyList<String>())
    var lastGalleryResetLoadKey by mutableStateOf<GalleryRouteResetLoadKey?>(null)
    var lastImageResetLoadKey by mutableStateOf<GalleryRouteResetLoadKey?>(null)

    fun clearTransientLoading() {
        if (pageState.isLoading) pageState = pageState.copy(isLoading = false)
        if (imagePageState.isLoading) imagePageState = imagePageState.copy(isLoading = false)
    }
}

internal class GalleryDetailRouteStateViewModel : ViewModel() {
    private var activeGalleryId: String? = null
    var reloadToken by mutableLongStateOf(0L)
    var requestSerial by mutableLongStateOf(0L)
    var pageState by mutableStateOf(
        StashGalleryImageGridPageState.initial("").copy(pageSize = GALLERY_IMAGE_GRID_DEFAULT_PAGE_SIZE),
    )
    var lastDetailResetLoadKey by mutableStateOf<GalleryDetailRouteResetLoadKey?>(null)

    fun clearTransientLoading() {
        if (pageState.isLoading) pageState = pageState.copy(isLoading = false)
    }

    fun ensureGallery(galleryId: String) {
        if (activeGalleryId == galleryId) return
        activeGalleryId = galleryId
        reloadToken = 0L
        requestSerial += 1L
        lastDetailResetLoadKey = null
        pageState = StashGalleryImageGridPageState.initial(galleryId)
            .copy(pageSize = GALLERY_IMAGE_GRID_DEFAULT_PAGE_SIZE)
    }
}

@Composable
fun GalleryRoute(
    isFoldLikeLayout: Boolean,
    onOpenGallery: (String) -> Unit,
    onOpenPhoto: (Int, List<GalleryImageModel>) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val routeState: GalleryRouteStateViewModel = viewModel()
    DisposableEffect(Unit) {
        onDispose { routeState.clearTransientLoading() }
    }
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localLibraryRepository = remember(context) { StashLocalLibraryRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val galleryToolbarPreferences by settingsRepository.galleryToolbarPreferences.collectAsState(initial = null)
    val imageToolbarPreferences by settingsRepository.imageToolbarPreferences.collectAsState(initial = null)
    val persistedGalleryBrowseMode by settingsRepository.galleryBrowseMode.collectAsState(initial = null)
    val savedGalleryFilters by localLibraryRepository.savedGalleryFilters.collectAsState(initial = emptyList())
    val recentGalleryFilters by localLibraryRepository.recentGalleryFilters.collectAsState(initial = emptyList())
    val savedImageFilters by localLibraryRepository.savedImageFilters.collectAsState(initial = emptyList())
    val recentImageFilters by localLibraryRepository.recentImageFilters.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var galleryInputText by routeState::galleryInputText
    var imageInputText by routeState::imageInputText
    var browseMode by routeState::browseMode
    var reloadToken by routeState::reloadToken
    var imageReloadToken by routeState::imageReloadToken
    var requestSerial by routeState::requestSerial
    var imageRequestSerial by routeState::imageRequestSerial
    var pageState by routeState::pageState
    var imagePageState by routeState::imagePageState
    var galleryToolbarPreferencesRestored by routeState::galleryToolbarPreferencesRestored
    var imageToolbarPreferencesRestored by routeState::imageToolbarPreferencesRestored
    var galleryBrowseModeRestored by routeState::galleryBrowseModeRestored
    var activeEntityFilter by routeState::activeEntityFilter
    var activeImageEntityFilter by routeState::activeImageEntityFilter
    var entitySearchQuery by routeState::entitySearchQuery
    var entityOptions by routeState::entityOptions
    var entityDraft by routeState::entityDraft
    var entityOptionsLoading by routeState::entityOptionsLoading
    var showSavedFilterSheet by routeState::showSavedFilterSheet
    var showSavedImageFilterSheet by routeState::showSavedImageFilterSheet
    var savedGalleryFilterName by routeState::savedGalleryFilterName
    var savedImageFilterName by routeState::savedImageFilterName
    var selectedImageFolderPath by routeState::selectedImageFolderPath
    var gallerySelectionState by routeState::gallerySelectionState
    var previousVisibleGalleryIds by routeState::previousVisibleGalleryIds
    val visibleGalleryIds = pageState.galleries.map { it.id }

    fun openGallerySelectionRequest(galleryId: String, nextSelectionState: GallerySelectionState) {
        gallerySelectionState = nextSelectionState
        onOpenGallery(galleryId)
    }

    fun selectBrowseMode(targetMode: StashGalleryBrowseMode, persistSelection: Boolean = true) {
        val result = switchStashGalleryBrowseMode(
            currentMode = browseMode,
            targetMode = targetMode,
            galleryState = pageState,
            imageState = imagePageState,
            gallerySelectionState = gallerySelectionState,
        )
        browseMode = result.mode
        if (result.mode != StashGalleryBrowseMode.Images) {
            selectedImageFolderPath = null
        }
        pageState = result.galleryState
        imagePageState = result.imageState
        gallerySelectionState = result.gallerySelectionState
        if (persistSelection) {
            scope.launch { settingsRepository.setGalleryBrowseMode(result.mode) }
        }
    }

    BackHandler(enabled = selectedImageFolderPath != null && browseMode == StashGalleryBrowseMode.Images) {
        selectedImageFolderPath = null
    }

    BackHandler(enabled = gallerySelectionState.isActive) {
        gallerySelectionState = gallerySelectionState.clear()
    }

    fun applyGalleryFilter(updatedFilter: StashGalleryFilterState) {
        val previousFilter = pageState.galleryFilter
        if (shouldPromoteRecentGalleryFilterAfterChange(previousFilter, updatedFilter)) {
            scope.launch { localLibraryRepository.saveRecentGalleryFilter(updatedFilter) }
        }
        gallerySelectionState = gallerySelectionState.clear()
        requestSerial += 1L
        pageState = pageState.withGalleryFilter(updatedFilter)
    }

    fun applySavedGalleryFilter(savedFilter: LocalSavedGalleryFilter) {
        applyGalleryFilter(savedFilter.appliedFilterState())
        savedGalleryFilterName = savedFilter.name
    }

    fun applyImageFilter(updatedFilter: StashImageFilterState) {
        val previousFilter = imagePageState.imageFilter
        if (shouldPromoteRecentImageFilterAfterChange(previousFilter, updatedFilter)) {
            scope.launch { localLibraryRepository.saveRecentImageFilter(updatedFilter) }
        }
        selectedImageFolderPath = null
        imageRequestSerial += 1L
        imagePageState = imagePageState.withImageFilter(updatedFilter)
    }

    fun applySavedImageFilter(savedFilter: LocalSavedImageFilter) {
        applyImageFilter(savedFilter.appliedFilterState())
        savedImageFilterName = savedFilter.name
    }

    fun openImageEntityFilter(kind: ImageEntityFilterKind) {
        activeEntityFilter = null
        entityDraft = kind.selectedFrom(imagePageState.imageFilter).normalizedGalleryEntities()
        entityOptions = entityDraft
        entitySearchQuery = ""
        activeImageEntityFilter = kind
    }

    fun applyImageEntityFilter(kind: ImageEntityFilterKind, selected: List<StashSelectedEntity>) {
        applyImageFilter(kind.withSelected(imagePageState.imageFilter, selected))
        activeImageEntityFilter = null
    }

    fun persistGalleryToolbarPreferences(updatedPageState: StashGalleryGridPageState) {
        scope.launch { settingsRepository.setGalleryToolbarPreferences(updatedPageState.toolbarPreferences) }
    }

    fun applyGalleryToolbarPageState(updatedPageState: StashGalleryGridPageState) {
        pageState = updatedPageState
        persistGalleryToolbarPreferences(updatedPageState)
    }

    fun applyImageToolbarPageState(updatedPageState: StashGalleryGlobalImageGridPageState) {
        if (updatedPageState.displayMode != StashGalleryDisplayMode.Folders || updatedPageState.sortOption != imagePageState.sortOption) {
            selectedImageFolderPath = null
        }
        imagePageState = updatedPageState
        scope.launch { settingsRepository.setImageToolbarPreferences(updatedPageState.toolbarPreferences) }
    }

    fun saveCurrentGalleryFilter(name: String, filterState: StashGalleryFilterState, overwriteExisting: Boolean = true) {
        scope.launch {
            val saved = localLibraryRepository.saveGalleryFilter(
                name = name,
                filterState = filterState,
                overwriteExisting = overwriteExisting,
            )
            savedGalleryFilterName = saved.name
            applyGalleryFilter(saved.appliedFilterState())
        }
    }

    fun saveCurrentImageFilter(name: String, filterState: StashImageFilterState, overwriteExisting: Boolean = true) {
        scope.launch {
            val saved = localLibraryRepository.saveImageFilter(
                name = name,
                filterState = filterState,
                overwriteExisting = overwriteExisting,
            )
            savedImageFilterName = saved.name
            applyImageFilter(saved.appliedFilterState())
        }
    }

    fun loadPage(page: Int, reset: Boolean = false) {
        val activeProfile = profile ?: return
        if (!reset && pageState.isLoading) return
        val requestId = requestSerial + 1L
        requestSerial = requestId
        val activeQuery = pageState.query
        val activePageSize = pageState.pageSize
        val activeSortOption = pageState.sortOption
        val activeSort = pageState.serverSort
        val activeDirection = pageState.sortDirection
        val activeRandomSeed = pageState.randomSeed
        val activeGalleryFilter = pageState.galleryFilter
        pageState = if (reset) {
            pageState.copy(
                galleries = emptyList(),
                nextPage = 1,
                hasMore = true,
                isLoading = false,
                error = null,
                totalCount = null,
            ).loading()
        } else {
            pageState.loading()
        }
        scope.launch {
            runCatching {
                StashGraphQlClient(activeProfile).findGalleryCardsPage(
                    perPage = activePageSize,
                    page = page,
                    query = activeQuery.ifBlank { null },
                    sort = activeSort,
                    direction = activeDirection,
                    galleryFilter = activeGalleryFilter,
                )
            }.onSuccess { result ->
                if (
                    requestSerial != requestId ||
                    profile != activeProfile ||
                    pageState.query != activeQuery ||
                    pageState.pageSize != activePageSize ||
                    pageState.sortOption != activeSortOption ||
                    pageState.sortDirection != activeDirection ||
                    pageState.randomSeed != activeRandomSeed ||
                    pageState.galleryFilter != activeGalleryFilter
                ) {
                    return@onSuccess
                }
                pageState = if (page == 1) {
                    pageState.withFirstPage(result.galleries, result.totalCount, activePageSize)
                } else {
                    pageState.withNextPage(result.galleries, result.totalCount, activePageSize)
                }
            }.onFailure { throwable ->
                if (
                    requestSerial != requestId ||
                    profile != activeProfile ||
                    pageState.query != activeQuery ||
                    pageState.pageSize != activePageSize ||
                    pageState.sortOption != activeSortOption ||
                    pageState.sortDirection != activeDirection ||
                    pageState.randomSeed != activeRandomSeed ||
                    pageState.galleryFilter != activeGalleryFilter
                ) {
                    return@onFailure
                }
                pageState = pageState.failed(
                    redactStashCredentialText(throwable.message ?: stashString(R.string.gallery_load_failed_message)),
                )
            }
        }
    }

    fun loadGlobalImagesPage(page: Int, reset: Boolean = false) {
        val activeProfile = profile ?: return
        if (!reset && imagePageState.isLoading) return
        val requestId = imageRequestSerial + 1L
        imageRequestSerial = requestId
        val activeQuery = imagePageState.query
        val activePageSize = imagePageState.pageSize
        val activeSortOption = imagePageState.sortOption
        val activeSort = imagePageState.serverSort
        val activeDirection = imagePageState.serverDirection
        val activeImageFilter = imagePageState.imageFilter
        val activeImageFilterIdentity = activeImageFilter.identityKey
        val activeRandomSeed = imagePageState.randomSeed
        imagePageState = if (reset) {
            imagePageState.copy(
                images = emptyList(),
                nextPage = 1,
                hasMore = true,
                isLoading = false,
                error = null,
                totalCount = null,
            ).loading()
        } else {
            imagePageState.loading()
        }
        scope.launch {
            runCatching {
                StashGraphQlClient(activeProfile).findImagesPage(
                    perPage = activePageSize,
                    page = page,
                    query = activeQuery.ifBlank { null },
                    sort = activeSort,
                    direction = activeDirection,
                    imageFilter = activeImageFilter,
                )
            }.onSuccess { result ->
                if (
                    imageRequestSerial != requestId ||
                    profile != activeProfile ||
                    imagePageState.query != activeQuery ||
                    imagePageState.pageSize != activePageSize ||
                    imagePageState.sortOption != activeSortOption ||
                    imagePageState.serverSort != activeSort ||
                    imagePageState.serverDirection != activeDirection ||
                    imagePageState.imageFilter.identityKey != activeImageFilterIdentity ||
                    imagePageState.randomSeed != activeRandomSeed
                ) {
                    return@onSuccess
                }
                imagePageState = if (page == 1) {
                    imagePageState.withFirstPage(result.images, result.totalCount, activePageSize)
                } else {
                    imagePageState.withNextPage(result.images, result.totalCount, activePageSize)
                }
            }.onFailure { throwable ->
                if (
                    imageRequestSerial != requestId ||
                    profile != activeProfile ||
                    imagePageState.query != activeQuery ||
                    imagePageState.pageSize != activePageSize ||
                    imagePageState.sortOption != activeSortOption ||
                    imagePageState.serverSort != activeSort ||
                    imagePageState.serverDirection != activeDirection ||
                    imagePageState.imageFilter.identityKey != activeImageFilterIdentity ||
                    imagePageState.randomSeed != activeRandomSeed
                ) {
                    return@onFailure
                }
                imagePageState = imagePageState.failed(
                    redactStashCredentialText(throwable.message ?: stashString(R.string.gallery_global_images_load_failed_message)),
                )
            }
        }
    }

    LaunchedEffect(galleryInputText) {
        delay(GALLERY_SEARCH_DEBOUNCE_MS)
        val normalizedQuery = normalizeStashDiscoveryQuery(galleryInputText)
        if (normalizedQuery != pageState.query) {
            requestSerial += 1L
            gallerySelectionState = gallerySelectionState.clear()
            pageState = pageState.withQuery(normalizedQuery)
        }
    }

    LaunchedEffect(imageInputText) {
        delay(GALLERY_SEARCH_DEBOUNCE_MS)
        val normalizedQuery = normalizeStashDiscoveryQuery(imageInputText)
        if (normalizedQuery != imagePageState.query) {
            selectedImageFolderPath = null
            imageRequestSerial += 1L
            imagePageState = imagePageState.withQuery(normalizedQuery)
        }
    }

    LaunchedEffect(visibleGalleryIds) {
        gallerySelectionState = gallerySelectionState.clearIfResultIdentityChanged(
            previousGalleryIds = previousVisibleGalleryIds,
            currentGalleryIds = visibleGalleryIds,
        )
        previousVisibleGalleryIds = visibleGalleryIds
    }

    LaunchedEffect(galleryToolbarPreferences) {
        val preferences = galleryToolbarPreferences ?: return@LaunchedEffect
        if (!galleryToolbarPreferencesRestored) {
            pageState = pageState.withToolbarPreferences(preferences)
            galleryToolbarPreferencesRestored = true
        }
    }

    LaunchedEffect(imageToolbarPreferences) {
        val preferences = imageToolbarPreferences ?: return@LaunchedEffect
        if (!imageToolbarPreferencesRestored) {
            imagePageState = imagePageState.withToolbarPreferences(preferences)
            imageToolbarPreferencesRestored = true
        }
    }

    LaunchedEffect(persistedGalleryBrowseMode) {
        val restoredMode = persistedGalleryBrowseMode ?: return@LaunchedEffect
        if (!galleryBrowseModeRestored) {
            selectBrowseMode(restoredMode, persistSelection = false)
            galleryBrowseModeRestored = true
        }
    }

    LaunchedEffect(profile, browseMode, galleryBrowseModeRestored, pageState.query, pageState.sortOption, pageState.sortDirection, pageState.randomSeed, pageState.galleryFilter, pageState.pageSize, reloadToken, galleryToolbarPreferencesRestored) {
        if (profile == null) {
            pageState = pageState.copy(
                galleries = emptyList(),
                nextPage = 1,
                hasMore = true,
                isLoading = false,
                error = null,
                totalCount = null,
            )
        } else if (!galleryToolbarPreferencesRestored || !galleryBrowseModeRestored) {
            return@LaunchedEffect
        } else if (browseMode != StashGalleryBrowseMode.Galleries) {
            return@LaunchedEffect
        } else {
            val resetLoadKey = GalleryRouteResetLoadKey(
                profileRevision = profile.hashCode(),
                browseMode = browseMode,
                query = pageState.query,
                sortOption = pageState.sortOption,
                sortDirection = pageState.sortDirection,
                displayMode = null,
                filterIdentity = pageState.galleryFilter,
                randomSeed = pageState.randomSeed,
                pageSize = pageState.pageSize,
                reloadToken = reloadToken,
            )
            if (routeState.lastGalleryResetLoadKey != resetLoadKey || pageState.totalCount == null) {
                routeState.lastGalleryResetLoadKey = resetLoadKey
                loadPage(page = 1, reset = true)
            }
        }
    }

    LaunchedEffect(profile, browseMode, galleryBrowseModeRestored, imageToolbarPreferencesRestored, imagePageState.query, imagePageState.sortOption, imagePageState.sortDirection, imagePageState.displayMode, imagePageState.imageFilter.identityKey, imagePageState.randomSeed, imagePageState.pageSize, imageReloadToken) {
        if (profile == null) {
            imagePageState = imagePageState.copy(
                images = emptyList(),
                nextPage = 1,
                hasMore = true,
                isLoading = false,
                error = null,
                totalCount = null,
            )
        } else if (!galleryBrowseModeRestored || !imageToolbarPreferencesRestored) {
            return@LaunchedEffect
        } else if (browseMode == StashGalleryBrowseMode.Images) {
            val resetLoadKey = GalleryRouteResetLoadKey(
                profileRevision = profile.hashCode(),
                browseMode = browseMode,
                query = imagePageState.query,
                sortOption = imagePageState.sortOption,
                sortDirection = imagePageState.sortDirection,
                displayMode = imagePageState.displayMode,
                filterIdentity = imagePageState.imageFilter.identityKey,
                randomSeed = imagePageState.randomSeed,
                pageSize = imagePageState.pageSize,
                reloadToken = imageReloadToken,
            )
            if (routeState.lastImageResetLoadKey != resetLoadKey || imagePageState.totalCount == null) {
                routeState.lastImageResetLoadKey = resetLoadKey
                loadGlobalImagesPage(page = 1, reset = true)
            }
        }
    }

    LaunchedEffect(profile, activeEntityFilter, entitySearchQuery) {
        val kind = activeEntityFilter ?: return@LaunchedEffect
        val activeProfile = profile ?: return@LaunchedEffect
        val activeQuery = entitySearchQuery
        if (kind == GalleryEntityFilterKind.ParentFolders) {
            entityOptionsLoading = false
            entityOptions = emptyList()
            return@LaunchedEffect
        }
        entityOptionsLoading = true
        runCatching {
            val client = StashGraphQlClient(activeProfile)
            when (kind) {
                GalleryEntityFilterKind.Tags -> client.findTags(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                ).map { tag -> StashSelectedEntity(id = tag.id, name = tag.name) }
                GalleryEntityFilterKind.Studios -> client.findStudios(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                )
                GalleryEntityFilterKind.Performers -> client.findPerformers(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                )
                GalleryEntityFilterKind.Scenes -> client.findSceneEntities(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                )
                GalleryEntityFilterKind.ParentFolders -> emptyList()
            }
        }.onSuccess { options ->
            if (profile == activeProfile && activeEntityFilter == kind && entitySearchQuery == activeQuery) {
                entityOptions = (entityDraft + options).normalizedGalleryEntities()
            }
        }.onFailure {
            if (profile == activeProfile && activeEntityFilter == kind && entitySearchQuery == activeQuery) {
                entityOptions = entityDraft.normalizedGalleryEntities()
            }
        }
        if (profile == activeProfile && activeEntityFilter == kind && entitySearchQuery == activeQuery) {
            entityOptionsLoading = false
        }
    }

    LaunchedEffect(profile, activeImageEntityFilter, entitySearchQuery) {
        val kind = activeImageEntityFilter ?: return@LaunchedEffect
        val activeProfile = profile ?: return@LaunchedEffect
        val activeQuery = entitySearchQuery
        entityOptionsLoading = true
        runCatching {
            val client = StashGraphQlClient(activeProfile)
            when (kind) {
                ImageEntityFilterKind.Tags -> client.findTags(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                ).map { tag -> StashSelectedEntity(id = tag.id, name = tag.name) }
                ImageEntityFilterKind.PerformerTags -> client.findTags(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                ).map { tag -> StashSelectedEntity(id = tag.id, name = tag.name) }
                ImageEntityFilterKind.Studios -> client.findStudios(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                )
                ImageEntityFilterKind.Performers -> client.findPerformers(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                )
                ImageEntityFilterKind.Galleries -> client.findGalleryCardsPage(
                    perPage = GALLERY_ENTITY_SELECTOR_PAGE_SIZE,
                    query = activeQuery,
                    sort = "title",
                    direction = StashSortDirection.Asc,
                ).galleries.map { gallery -> StashSelectedEntity(id = gallery.id, name = gallery.title) }
            }
        }.onSuccess { options ->
            if (profile == activeProfile && activeImageEntityFilter == kind && entitySearchQuery == activeQuery) {
                entityOptions = (entityDraft + options).normalizedGalleryEntities()
            }
        }.onFailure {
            if (profile == activeProfile && activeImageEntityFilter == kind && entitySearchQuery == activeQuery) {
                entityOptions = entityDraft.normalizedGalleryEntities()
            }
        }
        if (profile == activeProfile && activeImageEntityFilter == kind && entitySearchQuery == activeQuery) {
            entityOptionsLoading = false
        }
    }

    fun openEntityFilter(kind: GalleryEntityFilterKind) {
        activeImageEntityFilter = null
        entityDraft = kind.selectedFrom(pageState.galleryFilter).normalizedGalleryEntities()
        entityOptions = entityDraft
        entitySearchQuery = ""
        activeEntityFilter = kind
    }

    fun applyEntityFilter(kind: GalleryEntityFilterKind, selected: List<StashSelectedEntity>) {
        applyGalleryFilter(kind.withSelected(pageState.galleryFilter, selected))
        activeEntityFilter = null
    }

    GalleryContent(
        isFoldLikeLayout = isFoldLikeLayout,
        browseMode = browseMode,
        onSelectBrowseMode = { mode -> selectBrowseMode(mode) },
        inputText = if (browseMode == StashGalleryBrowseMode.Images) imageInputText else galleryInputText,
        onInputTextChange = { text ->
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageInputText = text
            } else {
                galleryInputText = text
            }
        },
        onClearInput = {
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageInputText = ""
                selectedImageFolderPath = null
                imageRequestSerial += 1L
                imagePageState = imagePageState.withQuery("")
            } else {
                galleryInputText = ""
                requestSerial += 1L
                gallerySelectionState = gallerySelectionState.clear()
                pageState = pageState.withQuery("")
            }
        },
        pageState = pageState,
        imagePageState = imagePageState,
        selectedImageFolderPath = selectedImageFolderPath,
        gallerySelectionState = gallerySelectionState,
        serverProfile = profile,
        modifier = modifier,
        onRetry = {
            if (pageState.galleries.isEmpty()) {
                reloadToken++
            } else if (!pageState.isLoading) {
                loadPage(pageState.nextPage)
            }
        },
        onRetryImages = {
            if (imagePageState.images.isEmpty()) {
                imageReloadToken++
            } else if (!imagePageState.isLoading) {
                loadGlobalImagesPage(imagePageState.nextPage)
            }
        },
        onLoadMore = {
            if (!pageState.isLoading && pageState.hasMore) {
                loadPage(pageState.nextPage)
            }
        },
        onLoadMoreImages = {
            if (!imagePageState.isLoading && imagePageState.hasMore) {
                loadGlobalImagesPage(imagePageState.nextPage)
            }
        },
        onOpenGallery = { gallery ->
            val tapResult = gallerySelectionState.handleCardTap(gallery.id)
            gallerySelectionState = tapResult.state
            if (tapResult.shouldOpenGallery) {
                onOpenGallery(gallery.id)
            }
        },
        onOpenImage = { index, images -> onOpenPhoto(index, images) },
        onSelectImageFolder = { folder -> selectedImageFolderPath = folder.path },
        onBackToImageFolders = { selectedImageFolderPath = null },
        onLongPressGallery = { gallery ->
            gallerySelectionState = gallerySelectionState.selectFromLongPress(gallery.id)
        },
        onOpenSettings = onOpenSettings,
        onSelectSort = { sortOption ->
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageRequestSerial += 1L
                applyImageToolbarPageState(imagePageState.forSort(sortOption))
            } else {
                requestSerial += 1L
                gallerySelectionState = gallerySelectionState.clear()
                applyGalleryToolbarPageState(pageState.forSort(sortOption))
            }
        },
        onToggleSortDirection = {
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageRequestSerial += 1L
                applyImageToolbarPageState(
                    imagePageState.withSortDirection(
                        if (imagePageState.sortDirection == StashSortDirection.Desc) {
                            StashSortDirection.Asc
                        } else {
                            StashSortDirection.Desc
                        },
                    ),
                )
            } else {
                requestSerial += 1L
                gallerySelectionState = gallerySelectionState.clear()
                applyGalleryToolbarPageState(
                    pageState.withSortDirection(
                        if (pageState.sortDirection == StashSortDirection.Desc) {
                            StashSortDirection.Asc
                        } else {
                            StashSortDirection.Desc
                        },
                    ),
                )
            }
        },
        onRandomAction = {
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageRequestSerial += 1L
                applyImageToolbarPageState(imagePageState.withRandomSeed())
            } else {
                requestSerial += 1L
                gallerySelectionState = gallerySelectionState.clear()
                applyGalleryToolbarPageState(pageState.withRandomSeed())
            }
        },
        onSelectPageSize = { pageSize ->
            if (browseMode == StashGalleryBrowseMode.Images) {
                imageRequestSerial += 1L
                applyImageToolbarPageState(imagePageState.withPageSize(pageSize))
            } else {
                requestSerial += 1L
                gallerySelectionState = gallerySelectionState.clear()
                applyGalleryToolbarPageState(pageState.withPageSize(pageSize))
            }
        },
        onSelectDisplayMode = { displayMode ->
            if (browseMode == StashGalleryBrowseMode.Images) {
                applyImageToolbarPageState(imagePageState.withDisplayMode(displayMode))
            } else {
                applyGalleryToolbarPageState(pageState.withDisplayMode(displayMode))
            }
        },
        onClearSelection = { gallerySelectionState = gallerySelectionState.clear() },
        onSelectVisibleGalleries = {
            gallerySelectionState = gallerySelectionState.selectVisibleGalleries(visibleGalleryIds)
        },
        onInvertVisibleSelection = {
            gallerySelectionState = gallerySelectionState.invertVisibleSelection(visibleGalleryIds)
        },
        onOpenSelectedGallery = {
            gallerySelectionState.selectedOpenRequest(pageState.galleries)?.let { request ->
                openGallerySelectionRequest(request.galleryId, request.nextSelectionState)
            }
        },
        onOpenFirstVisibleGallery = {
            gallerySelectionState.toolbarOpenFirstRequest(pageState.galleries)?.let { request ->
                openGallerySelectionRequest(request.galleryId, request.nextSelectionState)
            }
        },
        onOpenRandomVisibleGallery = {
            gallerySelectionState.randomOpenRequest(pageState.galleries, Random.nextInt())?.let { request ->
                openGallerySelectionRequest(request.galleryId, request.nextSelectionState)
            }
        },
        onOpenEntityFilter = ::openEntityFilter,
        onOpenImageEntityFilter = ::openImageEntityFilter,
        onOpenSavedFilters = {
            savedGalleryFilterName = pageState.galleryFilter.quickSavedGalleryFilterName()
            showSavedFilterSheet = true
        },
        onOpenSavedImageFilters = {
            savedImageFilterName = imagePageState.imageFilter.quickSavedImageFilterName()
            showSavedImageFilterSheet = true
        },
        onClearFilterCategory = { category ->
            applyGalleryFilter(clearStashGalleryFilterCategory(pageState.galleryFilter, category))
        },
        onClearImageFilterCategory = { category ->
            applyImageFilter(clearStashImageFilterCategory(imagePageState.imageFilter, category))
        },
        onOpenLinkedGallery = onOpenGallery,
    )

    activeEntityFilter?.let { kind ->
        StashEntityFilterSheet(
            title = kind.title(),
            searchLabel = kind.searchLabel(),
            selectedEntities = entityDraft,
            availableEntities = entityOptions,
            searchQuery = entitySearchQuery,
            isLoading = entityOptionsLoading,
            onSearchQueryChange = { entitySearchQuery = it },
            onToggleEntity = { entity -> entityDraft = entityDraft.toggleGalleryEntity(entity) },
            onReset = { entityDraft = emptyList() },
            onApply = { applyEntityFilter(kind, entityDraft) },
            onDismiss = { activeEntityFilter = null },
            allowTypedOption = kind == GalleryEntityFilterKind.ParentFolders,
        )
    }

    activeImageEntityFilter?.let { kind ->
        StashEntityFilterSheet(
            title = kind.title(),
            searchLabel = kind.searchLabel(),
            selectedEntities = entityDraft,
            availableEntities = entityOptions,
            searchQuery = entitySearchQuery,
            isLoading = entityOptionsLoading,
            onSearchQueryChange = { entitySearchQuery = it },
            onToggleEntity = { entity -> entityDraft = entityDraft.toggleGalleryEntity(entity) },
            onReset = { entityDraft = emptyList() },
            onApply = { applyImageEntityFilter(kind, entityDraft) },
            onDismiss = { activeImageEntityFilter = null },
            allowTypedOption = false,
        )
    }

    if (showSavedFilterSheet) {
        StashGallerySavedFilterSheet(
            savedFilters = savedGalleryFilters,
            recentFilters = recentGalleryFilters,
            currentFilter = pageState.galleryFilter,
            savedFilterName = savedGalleryFilterName,
            onSavedFilterNameChange = { savedGalleryFilterName = it },
            onApplyRecentFilter = { filter -> applyGalleryFilter(filter) },
            onApplySavedFilter = ::applySavedGalleryFilter,
            onSaveCurrentFilter = { name, filter -> saveCurrentGalleryFilter(name, filter, overwriteExisting = true) },
            onQuickSaveCurrentFilter = { name, filter -> saveCurrentGalleryFilter(name, filter, overwriteExisting = false) },
            onDeleteSavedFilter = { savedFilter -> scope.launch { localLibraryRepository.deleteSavedGalleryFilter(savedFilter.id) } },
            onDismiss = { showSavedFilterSheet = false },
        )
    }

    if (showSavedImageFilterSheet) {
        StashImageSavedFilterSheet(
            savedFilters = savedImageFilters,
            recentFilters = recentImageFilters,
            currentFilter = imagePageState.imageFilter,
            savedFilterName = savedImageFilterName,
            onSavedFilterNameChange = { savedImageFilterName = it },
            onApplyRecentFilter = { filter -> applyImageFilter(filter) },
            onApplySavedFilter = ::applySavedImageFilter,
            onSaveCurrentFilter = { name, filter -> saveCurrentImageFilter(name, filter, overwriteExisting = true) },
            onQuickSaveCurrentFilter = { name, filter -> saveCurrentImageFilter(name, filter, overwriteExisting = false) },
            onDeleteSavedFilter = { savedFilter -> scope.launch { localLibraryRepository.deleteSavedImageFilter(savedFilter.id) } },
            onDismiss = { showSavedImageFilterSheet = false },
        )
    }
}

@Composable
fun GalleryDetailRoute(
    galleryId: String,
    isFoldLikeLayout: Boolean,
    onNavigateBack: () -> Unit,
    onOpenPhoto: (String, Int, List<GalleryImageModel>) -> Unit,
    onOpenScene: (String) -> Unit,
    onOpenGallery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val profile by settingsRepository.serverProfile.collectAsState(initial = null)
    val routeState: GalleryDetailRouteStateViewModel = viewModel()
    DisposableEffect(Unit) {
        onDispose { routeState.clearTransientLoading() }
    }
    LaunchedEffect(galleryId) {
        routeState.ensureGallery(galleryId)
    }
    val scope = rememberCoroutineScope()
    var reloadToken by routeState::reloadToken
    var requestSerial by routeState::requestSerial
    var pageState by routeState::pageState

    fun loadPage(page: Int, reset: Boolean = false) {
        val activeProfile = profile ?: return
        if (!reset && pageState.isLoading) return
        val requestId = requestSerial + 1L
        requestSerial = requestId
        val activePageSize = pageState.pageSize
        pageState = if (reset) {
            StashGalleryImageGridPageState.initial(galleryId)
                .copy(
                    pageSize = activePageSize,
                    gallery = pageState.gallery,
                    galleryDetail = pageState.galleryDetail,
                )
                .loading()
        } else {
            pageState.loading()
        }
        scope.launch {
            runCatching {
                val client = StashGraphQlClient(activeProfile)
                val detail = if (page == 1) client.findGalleryDetail(galleryId) else pageState.galleryDetail
                val imagesPage = client.findGalleryImagesPage(
                    galleryId = galleryId,
                    perPage = activePageSize,
                    page = page,
                    sort = "title",
                    direction = StashSortDirection.Asc,
                )
                detail to imagesPage
            }.onSuccess { (detail, result) ->
                if (requestSerial != requestId || profile != activeProfile) {
                    return@onSuccess
                }
                pageState = if (page == 1) {
                    StashGalleryImageGridPageState.initial(galleryId)
                        .copy(
                            pageSize = activePageSize,
                            gallery = detail?.gallery ?: pageState.gallery,
                            galleryDetail = detail,
                        )
                        .withFirstPage(result.images, result.totalCount, activePageSize)
                } else {
                    pageState.withGalleryDetail(detail).withNextPage(result.images, result.totalCount, activePageSize)
                }
            }.onFailure { throwable ->
                if (requestSerial != requestId || profile != activeProfile) {
                    return@onFailure
                }
                pageState = pageState.failed(
                    redactStashCredentialText(throwable.message ?: stashString(R.string.gallery_detail_load_failed_message)),
                )
            }
        }
    }

    LaunchedEffect(galleryId, profile, reloadToken) {
        if (profile == null) {
            routeState.lastDetailResetLoadKey = null
            pageState = StashGalleryImageGridPageState.initial(galleryId)
                .copy(pageSize = GALLERY_IMAGE_GRID_DEFAULT_PAGE_SIZE)
        } else {
            val resetLoadKey = GalleryDetailRouteResetLoadKey(
                profileRevision = profile.hashCode(),
                galleryId = galleryId,
                pageSize = pageState.pageSize,
                reloadToken = reloadToken,
            )
            if (routeState.lastDetailResetLoadKey != resetLoadKey || pageState.totalCount == null) {
                routeState.lastDetailResetLoadKey = resetLoadKey
                loadPage(page = 1, reset = true)
            }
        }
    }

    GalleryDetailContent(
        galleryId = galleryId,
        isFoldLikeLayout = isFoldLikeLayout,
        pageState = pageState,
        serverProfile = profile,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onRetry = {
            if (pageState.images.isEmpty()) {
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
        onOpenPhoto = onOpenPhoto,
        onOpenScene = onOpenScene,
        onOpenGallery = onOpenGallery,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun GalleryPhotoViewerOverlay(
    images: List<GalleryImageModel>,
    initialIndex: Int,
    serverProfile: StashServerProfile?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLinkedGallery: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val persistedGalleryPhotoDisplayMode by settingsRepository.galleryPhotoDisplayMode.collectAsState(initial = null)
    var viewerImages by remember(images) { mutableStateOf(images) }
    val safeInitialIndex = remember(viewerImages, initialIndex) {
        galleryPhotoViewerPagePolicy(
            images = viewerImages,
            requestedIndex = initialIndex,
            chromeVisible = true,
        ).currentIndex
    }
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { viewerImages.size }
    val scope = rememberCoroutineScope()
    val client = remember(serverProfile) { serverProfile?.let(::StashGraphQlClient) }
    var chromeVisible by remember { mutableStateOf(true) }
    var detailsDialogOpen by remember { mutableStateOf(false) }
    var appreciationModeEnabled by remember { mutableStateOf(false) }
    var tapNavigationEnabled by remember { mutableStateOf(true) }
    var bottomToolsExpanded by rememberSaveable { mutableStateOf(false) }
    var slideshowPlaying by remember { mutableStateOf(false) }
    var photoDisplayMode by remember { mutableStateOf(GalleryPhotoDisplayMode.FitToScreen) }
    var photoDisplayModeRestored by remember { mutableStateOf(false) }
    var boundaryFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var transientHudVisible by remember { mutableStateOf(false) }
    var suppressNextPageChangeHud by remember { mutableStateOf(false) }
    var chromeIdleGeneration by remember { mutableLongStateOf(0L) }
    var ratingSaveRequestId by remember { mutableLongStateOf(0L) }
    var ratingStates by remember(images) { mutableStateOf<Map<String, PlayerRatingState>>(emptyMap()) }
    var oCounterUpdatingImageId by remember { mutableStateOf<String?>(null) }
    var viewerViewportSize by remember { mutableStateOf(IntSize.Zero) }
    val zoomStates = remember(viewerImages) { mutableStateMapOf<String, GalleryPhotoZoomState>() }
    val activeImageId = viewerImages.getOrNull(pagerState.currentPage)?.id
    val activeZoomState = activeImageId?.let { zoomStates[it] } ?: GalleryPhotoZoomState()
    val pagerSwipeEnabled = galleryPhotoViewerPagerSwipeEnabled(activeZoomState)
    val policy = galleryPhotoViewerPagePolicy(
        images = viewerImages,
        requestedIndex = pagerState.currentPage,
        chromeVisible = chromeVisible,
    )
    val activeImage = policy.image
    val activeRatingState = activeImage?.let { image -> ratingStates[image.id] ?: PlayerRatingState(image.rating100) }
    val activeOCounter = activeImage?.oCounter ?: 0
    val photoContentScale = when (photoDisplayMode.toContentScalePolicy()) {
        ContentScalePolicy.Fit -> ContentScale.Fit
        ContentScalePolicy.None -> ContentScale.None
    }
    val appreciationState = GalleryAppreciationModeState(
        enabled = appreciationModeEnabled,
        tapNavigationEnabled = tapNavigationEnabled,
        chromeVisible = chromeVisible,
        modalOpen = detailsDialogOpen,
    )
    val viewerChromePolicy = galleryPhotoViewerChromePolicy(appreciationState)
    val firstImageFeedback = stringResource(R.string.gallery_photo_viewer_first_image_feedback)
    val lastImageFeedback = stringResource(R.string.gallery_photo_viewer_last_image_feedback)

    LaunchedEffect(persistedGalleryPhotoDisplayMode) {
        val restoredMode = persistedGalleryPhotoDisplayMode ?: return@LaunchedEffect
        if (!photoDisplayModeRestored) {
            photoDisplayMode = restoredMode
            photoDisplayModeRestored = true
        }
    }

    fun refreshChrome(show: Boolean = true) {
        chromeVisible = show
        chromeIdleGeneration += 1L
    }

    fun showTransientHud() {
        transientHudVisible = true
        chromeIdleGeneration += 1L
    }

    fun updateViewerImage(imageId: String, transform: (GalleryImageModel) -> GalleryImageModel) {
        viewerImages = viewerImages.map { image ->
            if (image.id == imageId) transform(image) else image
        }
    }

    fun pauseSlideshowForManualNavigation() {
        val action = resolveGalleryPhotoSlideshowAction(
            playing = slideshowPlaying,
            currentIndex = policy.currentIndex,
            totalCount = policy.totalCount,
            trigger = GalleryPhotoSlideshowTrigger.ManualNavigation,
        )
        if (action is GalleryPhotoSlideshowAction.PauseForManualNavigation) {
            slideshowPlaying = false
        }
    }

    fun selectImageRatingStep(ratingStep: Int) {
        val image = activeImage ?: return
        val currentState = activeRatingState ?: PlayerRatingState(image.rating100)
        if (image.id.isBlank()) return
        if (ratingStep == currentState.ratingStep && !currentState.isUpdating) return
        val requestId = ratingSaveRequestId + 1L
        ratingSaveRequestId = requestId
        val optimisticState = currentState.optimisticallySelectRatingStep(ratingStep)
        ratingStates = ratingStates + (image.id to optimisticState)
        updateViewerImage(image.id) { it.copy(rating100 = optimisticState.rating100) }
        refreshChrome()
        scope.launch {
            val saved = client?.let { runCatching { it.updateImageRating(image.id, optimisticState.rating100) } }
            if (ratingSaveRequestId == requestId) {
                saved
                    ?.onSuccess { ok ->
                        ratingStates = ratingStates + (image.id to if (ok) optimisticState.completeUpdate() else optimisticState.failUpdate(stashString(R.string.gallery_photo_viewer_rating_save_failed)))
                    }
                    ?.onFailure { throwable ->
                        ratingStates = ratingStates + (image.id to optimisticState.failUpdate(
                            redactStashCredentialText(throwable.message ?: throwable::class.simpleName),
                        ))
                        updateViewerImage(image.id) { it.copy(rating100 = currentState.rating100) }
                    }
                    ?: run {
                        ratingStates = ratingStates + (image.id to optimisticState.failUpdate(stashString(R.string.gallery_photo_viewer_server_missing)))
                        updateViewerImage(image.id) { it.copy(rating100 = currentState.rating100) }
                    }
            }
        }
    }

    fun mutateImageOCounter(action: GalleryImageOCounterAction) {
        val image = activeImage ?: return
        if (!shouldStartGalleryImageMutation(inFlight = oCounterUpdatingImageId != null, imageId = image.id)) return
        val previousCount = image.oCounter
        val optimisticCount = optimisticGalleryImageOCounter(previousCount, action)
        oCounterUpdatingImageId = image.id
        updateViewerImage(image.id) { it.copy(oCounter = optimisticCount) }
        refreshChrome()
        scope.launch {
            val saved = client?.let {
                when (action) {
                    GalleryImageOCounterAction.Increment -> runCatching { it.incrementImageO(image.id) }
                    GalleryImageOCounterAction.Decrement -> runCatching { it.decrementImageO(image.id) }
                    GalleryImageOCounterAction.Reset -> runCatching { it.resetImageO(image.id) }
                }
            }
            saved
                ?.onSuccess { count -> updateViewerImage(image.id) { it.copy(oCounter = count) } }
                ?.onFailure { updateViewerImage(image.id) { it.copy(oCounter = previousCount) } }
                ?: updateViewerImage(image.id) { it.copy(oCounter = previousCount) }
            if (oCounterUpdatingImageId == image.id) {
                oCounterUpdatingImageId = null
            }
        }
    }

    fun navigateToPhotoIndex(targetIndex: Int) {
        if (targetIndex !in viewerImages.indices) return
        pauseSlideshowForManualNavigation()
        boundaryFeedbackMessage = null
        refreshChrome(show = true)
        showTransientHud()
        scope.launch { pagerState.animateScrollToPage(targetIndex) }
    }

    LaunchedEffect(
        appreciationModeEnabled,
        chromeVisible,
        activeZoomState,
        boundaryFeedbackMessage,
        chromeIdleGeneration,
    ) {
        val canAutoHide = shouldAutoHideGalleryPhotoChrome(
            state = appreciationState,
            zoomState = activeZoomState,
            idleElapsedMs = GALLERY_PHOTO_CHROME_AUTO_HIDE_MS,
            autoHideDelayMs = GALLERY_PHOTO_CHROME_AUTO_HIDE_MS,
            boundaryFeedbackVisible = boundaryFeedbackMessage != null,
        )
        if (canAutoHide) {
            delay(GALLERY_PHOTO_CHROME_AUTO_HIDE_MS)
            chromeVisible = false
            transientHudVisible = false
        }
    }

    LaunchedEffect(boundaryFeedbackMessage) {
        if (boundaryFeedbackMessage != null) {
            delay(GALLERY_PHOTO_BOUNDARY_FEEDBACK_MS)
            boundaryFeedbackMessage = null
            chromeIdleGeneration += 1L
        }
    }

    LaunchedEffect(transientHudVisible, policy.indexLabel) {
        if (transientHudVisible) {
            delay(GALLERY_PHOTO_TRANSIENT_HUD_MS)
            transientHudVisible = false
        }
    }

    LaunchedEffect(chromeVisible, appreciationModeEnabled, detailsDialogOpen) {
        if (!chromeVisible || appreciationModeEnabled || detailsDialogOpen) {
            bottomToolsExpanded = false
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        bottomToolsExpanded = false
        boundaryFeedbackMessage = null
        if (shouldShowGalleryPhotoPageChangeHud(appreciationState, suppressNextPageChangeHud)) {
            showTransientHud()
        } else {
            transientHudVisible = false
        }
        suppressNextPageChangeHud = false
    }

    LaunchedEffect(slideshowPlaying, policy.currentIndex, policy.totalCount) {
        if (!slideshowPlaying) return@LaunchedEffect
        delay(3_000L)
        when (val action = resolveGalleryPhotoSlideshowAction(
            playing = slideshowPlaying,
            currentIndex = policy.currentIndex,
            totalCount = policy.totalCount,
            trigger = GalleryPhotoSlideshowTrigger.Timer,
        )) {
            is GalleryPhotoSlideshowAction.AdvanceTo -> {
                suppressNextPageChangeHud = true
                pagerState.animateScrollToPage(action.index)
            }
            GalleryPhotoSlideshowAction.PauseAtBoundary -> {
                slideshowPlaying = false
                boundaryFeedbackMessage = lastImageFeedback
            }
            else -> Unit
        }
    }

    LaunchedEffect(slideshowPlaying, policy.viewerImageUrl) {
        if (slideshowPlaying && policy.viewerImageUrl == null) {
            val action = resolveGalleryPhotoSlideshowAction(
                playing = true,
                currentIndex = policy.currentIndex,
                totalCount = policy.totalCount,
                trigger = GalleryPhotoSlideshowTrigger.Error,
            )
            if (action is GalleryPhotoSlideshowAction.PauseForError) {
                slideshowPlaying = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            resolveGalleryPhotoSlideshowAction(
                playing = slideshowPlaying,
                currentIndex = policy.currentIndex,
                totalCount = policy.totalCount,
                trigger = GalleryPhotoSlideshowTrigger.CloseOrDispose,
            )
        }
    }

    val preloadImageUrls = remember(viewerImages, policy.currentIndex) {
        galleryPhotoViewerPreloadImageUrls(
            images = viewerImages,
            currentIndex = policy.currentIndex,
        )
    }

    LaunchedEffect(preloadImageUrls, serverProfile, viewerViewportSize) {
        if (preloadImageUrls.isEmpty() || viewerViewportSize.width <= 0 || viewerViewportSize.height <= 0) {
            return@LaunchedEffect
        }
        preloadImageUrls
            .mapNotNull { url -> buildStashThumbnailRequestSpec(url, serverProfile) }
            .forEach { spec ->
                val request = ImageRequest.Builder(context)
                    .data(spec.url)
                    .size(viewerViewportSize.width, viewerViewportSize.height)
                    .apply {
                        spec.requestHeaders.forEach { (name, value) ->
                            setHeader(name, value)
                        }
                    }
                    .build()
                context.imageLoader.enqueue(request)
            }
    }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewerViewportSize = it }
            .background(Color.Black),
    ) {
        if (viewerImages.isEmpty()) {
            Text(
                text = stringResource(R.string.gallery_photo_viewer_empty_message),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = pagerSwipeEnabled,
                key = { page -> viewerImages.getOrNull(page)?.id ?: page },
            ) { page ->
                val image = viewerImages[page]
                val viewerModel = rememberStashThumbnailModel(image.bestViewerImageUrl, serverProfile)
                var viewportSize by remember(image.id) { mutableStateOf(IntSize.Zero) }
                val zoomState = zoomStates[image.id] ?: GalleryPhotoZoomState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { viewportSize = it }
                        .pointerInput(image.id, viewportSize) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                do {
                                    val event = awaitPointerEvent()
                                    val pointerCount = event.changes.count { it.pressed }
                                    val currentZoomState = zoomStates[image.id] ?: GalleryPhotoZoomState()
                                    if (galleryPhotoZoomShouldHandleTransform(pointerCount, currentZoomState)) {
                                        val activeChanges = event.changes.filter { it.pressed && it.previousPressed }
                                        val currentCentroid = activeChanges
                                            .map { it.position }
                                            .averageOffset()
                                        val previousCentroid = activeChanges
                                            .map { it.previousPosition }
                                            .averageOffset()
                                        val zoomChange = if (activeChanges.size > 1) {
                                            val currentDistance = activeChanges
                                                .map { (it.position - currentCentroid).getDistance() }
                                                .average()
                                                .toFloat()
                                            val previousDistance = activeChanges
                                                .map { (it.previousPosition - previousCentroid).getDistance() }
                                                .average()
                                                .toFloat()
                                            if (previousDistance > 0f) currentDistance / previousDistance else 1f
                                        } else {
                                            1f
                                        }
                                        val pan = if (activeChanges.isNotEmpty()) {
                                            currentCentroid - previousCentroid
                                        } else {
                                            Offset.Zero
                                        }
                                        val nextZoomState = galleryPhotoZoomTransform(
                                            state = currentZoomState,
                                            zoomChange = zoomChange,
                                            panX = pan.x,
                                            panY = pan.y,
                                            viewportWidth = viewportSize.width.toFloat(),
                                            viewportHeight = viewportSize.height.toFloat(),
                                        )
                                        zoomStates[image.id] = nextZoomState
                                        event.changes.forEach { change ->
                                            if (change.pressed) {
                                                change.consume()
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                        .pointerInput(
                            image.id,
                            page,
                            viewportSize,
                            appreciationModeEnabled,
                            tapNavigationEnabled,
                            chromeVisible,
                        ) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    val appreciationTapState = GalleryAppreciationModeState(
                                        enabled = appreciationModeEnabled,
                                        tapNavigationEnabled = tapNavigationEnabled,
                                        chromeVisible = chromeVisible,
                                    )
                                    val action = resolveGalleryPhotoTapAction(
                                        state = appreciationTapState,
                                        tapX = tapOffset.x,
                                        viewportWidth = viewportSize.width.toFloat(),
                                        zoomState = zoomStates[image.id] ?: GalleryPhotoZoomState(),
                                        hasPrevious = page > 0,
                                        hasNext = page < viewerImages.lastIndex,
                                    )
                                    val revealChrome = shouldRevealGalleryPhotoChromeAfterTap(
                                        state = appreciationTapState,
                                        action = action,
                                    )
                                    when (action) {
                                        GalleryPhotoTapAction.ToggleChrome -> {
                                            boundaryFeedbackMessage = null
                                            refreshChrome(show = !chromeVisible)
                                        }
                                        GalleryPhotoTapAction.PreviousImage -> {
                                            pauseSlideshowForManualNavigation()
                                            boundaryFeedbackMessage = null
                                            if (revealChrome) {
                                                refreshChrome(show = true)
                                                showTransientHud()
                                            } else {
                                                suppressNextPageChangeHud = true
                                            }
                                            scope.launch {
                                                try {
                                                    pagerState.animateScrollToPage(page - 1)
                                                } finally {
                                                    if (pagerState.currentPage == page) {
                                                        suppressNextPageChangeHud = false
                                                    }
                                                }
                                            }
                                        }
                                        GalleryPhotoTapAction.NextImage -> {
                                            pauseSlideshowForManualNavigation()
                                            boundaryFeedbackMessage = null
                                            if (revealChrome) {
                                                refreshChrome(show = true)
                                                showTransientHud()
                                            } else {
                                                suppressNextPageChangeHud = true
                                            }
                                            scope.launch {
                                                try {
                                                    pagerState.animateScrollToPage(page + 1)
                                                } finally {
                                                    if (pagerState.currentPage == page) {
                                                        suppressNextPageChangeHud = false
                                                    }
                                                }
                                            }
                                        }
                                        GalleryPhotoTapAction.PreviousBoundaryNoOp -> {
                                            if (revealChrome) {
                                                refreshChrome(show = true)
                                                boundaryFeedbackMessage = firstImageFeedback
                                                showTransientHud()
                                            } else {
                                                boundaryFeedbackMessage = null
                                            }
                                        }
                                        GalleryPhotoTapAction.NextBoundaryNoOp -> {
                                            if (revealChrome) {
                                                refreshChrome(show = true)
                                                boundaryFeedbackMessage = lastImageFeedback
                                                showTransientHud()
                                            } else {
                                                boundaryFeedbackMessage = null
                                            }
                                        }
                                        GalleryPhotoTapAction.Ignore -> Unit
                                    }
                                },
                                onDoubleTap = {
                                    zoomStates[image.id] = galleryPhotoZoomOnDoubleTap(
                                        zoomStates[image.id] ?: GalleryPhotoZoomState(),
                                    )
                                    boundaryFeedbackMessage = null
                                    refreshChrome(show = true)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (viewerModel != null) {
                        AsyncImage(
                            model = viewerModel,
                            contentDescription = image.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomState.scale
                                    scaleY = zoomState.scale
                                    translationX = zoomState.offsetX
                                    translationY = zoomState.offsetY
                                },
                            contentScale = photoContentScale,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.gallery_photo_viewer_missing_image_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        val hudMessage = boundaryFeedbackMessage ?: policy.indexLabel.takeIf { transientHudVisible && !policy.chromeVisible }
        if (hudMessage != null) {
            Text(
                text = hudMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        if (policy.chromeVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.58f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StashSecondaryButton(
                    text = stringResource(R.string.gallery_photo_viewer_close_action),
                    onClick = onDismiss,
                    contentDescription = stringResource(R.string.gallery_photo_viewer_close_content_description),
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (viewerChromePolicy.showPersistentMetadataChrome) {
                        Text(
                            text = policy.image?.title.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = policy.indexLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
                if (viewerChromePolicy.showAppreciationModeToggleInTopChrome) {
                    StashSecondaryButton(
                        text = if (appreciationModeEnabled) {
                            stringResource(R.string.gallery_photo_viewer_appreciation_mode_on)
                        } else {
                            stringResource(R.string.gallery_photo_viewer_appreciation_mode_off)
                        },
                        onClick = {
                            appreciationModeEnabled = !appreciationModeEnabled
                            refreshChrome(show = true)
                        },
                        selected = appreciationModeEnabled,
                        contentDescription = stringResource(
                            R.string.gallery_photo_viewer_appreciation_mode_content_description,
                        ),
                    )
                }
                IconButton(
                    onClick = {
                        detailsDialogOpen = true
                        refreshChrome(show = true)
                    },
                    enabled = activeImage != null,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.gallery_photo_viewer_details_content_description),
                        tint = Color.White,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val transportState = PlayerOverlayTransportUiState(
                    visible = true,
                    previousContentDescription = stringResource(R.string.gallery_photo_viewer_previous_content_description),
                    playPauseContentDescription = if (slideshowPlaying) {
                        stringResource(R.string.gallery_photo_viewer_slideshow_pause)
                    } else {
                        stringResource(R.string.gallery_photo_viewer_slideshow_play)
                    },
                    nextContentDescription = stringResource(R.string.gallery_photo_viewer_next_content_description),
                    previousEnabled = policy.hasPrevious,
                    playPauseEnabled = policy.totalCount > 1,
                    nextEnabled = policy.hasNext,
                )
                if (bottomToolsExpanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.58f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StashSecondaryButton(
                            text = if (tapNavigationEnabled) {
                                stringResource(R.string.gallery_photo_viewer_tap_navigation_on)
                            } else {
                                stringResource(R.string.gallery_photo_viewer_tap_navigation_off)
                            },
                            onClick = {
                                tapNavigationEnabled = !tapNavigationEnabled
                                refreshChrome(show = true)
                            },
                            enabled = appreciationModeEnabled,
                            selected = appreciationModeEnabled && tapNavigationEnabled,
                            contentDescription = stringResource(
                                R.string.gallery_photo_viewer_tap_navigation_content_description,
                            ),
                        )
                        if (viewerChromePolicy.showEditingControls) {
                            Text(
                                text = stringResource(R.string.gallery_image_o_counter_label, activeOCounter),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                            galleryPhotoViewerOCounterToolbarActions().forEach { action ->
                                StashSecondaryButton(
                                    text = when (action) {
                                        GalleryImageOCounterAction.Increment -> stringResource(R.string.gallery_photo_viewer_o_increment)
                                        GalleryImageOCounterAction.Decrement -> stringResource(R.string.gallery_photo_viewer_o_decrement)
                                        GalleryImageOCounterAction.Reset -> stringResource(R.string.gallery_photo_viewer_o_reset)
                                    },
                                    onClick = { mutateImageOCounter(action) },
                                    enabled = activeImage != null && oCounterUpdatingImageId == null,
                                    contentDescription = when (action) {
                                        GalleryImageOCounterAction.Increment -> stringResource(R.string.gallery_photo_viewer_o_increment_content_description)
                                        GalleryImageOCounterAction.Decrement -> stringResource(R.string.gallery_photo_viewer_o_decrement_content_description)
                                        GalleryImageOCounterAction.Reset -> stringResource(R.string.gallery_photo_viewer_o_reset_content_description)
                                    },
                                )
                            }
                            activeRatingState?.let { ratingState ->
                                PlayerRatingControls(
                                    ratingStep = ratingState.ratingStep,
                                    ratingMessage = ratingState.message,
                                    ratingUpdating = ratingState.isUpdating,
                                    onSelectRatingStep = ::selectImageRatingStep,
                                )
                            }
                        }
                        if (viewerChromePolicy.showLinkedGalleryShortcut) {
                            val linkedGalleryNavigation = resolveGalleryPhotoLinkedGalleryNavigation(activeImage, 0)
                            linkedGalleryNavigation?.let { navigation ->
                                StashSecondaryButton(
                                    text = stringResource(R.string.gallery_photo_viewer_open_linked_gallery, navigation.label),
                                    onClick = {
                                        slideshowPlaying = false
                                        onOpenLinkedGallery(navigation.galleryId)
                                    },
                                    contentDescription = stringResource(
                                        R.string.gallery_photo_viewer_open_linked_gallery_content_description,
                                        navigation.label,
                                    ),
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.52f))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = policy.indexLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    PlayerOverlayTransportControls(
                        state = transportState,
                        isPlaying = slideshowPlaying,
                        onPreviousTransport = { navigateToPhotoIndex(policy.currentIndex - 1) },
                        onPlayPause = {
                            if (policy.totalCount > 1) {
                                slideshowPlaying = !slideshowPlaying
                                refreshChrome(show = true)
                            }
                        },
                        onNextTransport = { navigateToPhotoIndex(policy.currentIndex + 1) },
                        visualStyles = buildImageViewerTransportButtonVisualStyles(
                            previousEnabled = transportState.previousEnabled,
                            playPauseEnabled = transportState.playPauseEnabled,
                            nextEnabled = transportState.nextEnabled,
                        ),
                        containerAlpha = ImageViewerTransportVisualPolicy.ContainerAlpha,
                        buttonSpacingDp = ImageViewerTransportVisualPolicy.ButtonSpacingDp,
                    )
                    StashSecondaryButton(
                        text = when (photoDisplayMode) {
                            GalleryPhotoDisplayMode.FitToScreen -> stringResource(R.string.gallery_photo_viewer_fit_mode)
                            GalleryPhotoDisplayMode.OriginalSize -> stringResource(R.string.gallery_photo_viewer_original_mode)
                        },
                        onClick = {
                            val update = toggleGalleryPhotoDisplayMode(photoDisplayMode, activeZoomState)
                            photoDisplayMode = update.displayMode
                            scope.launch { settingsRepository.setGalleryPhotoDisplayMode(update.displayMode) }
                            activeImageId?.let { zoomStates[it] = update.zoomState }
                            refreshChrome(show = true)
                        },
                        selected = photoDisplayMode == GalleryPhotoDisplayMode.OriginalSize,
                        contentDescription = stringResource(R.string.gallery_photo_viewer_display_mode_content_description),
                    )
                    IconButton(
                        onClick = {
                            bottomToolsExpanded = !bottomToolsExpanded
                            refreshChrome(show = true)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.gallery_photo_viewer_more_tools_content_description),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
        if (detailsDialogOpen) {
            activeImage?.let { image ->
                GalleryPhotoDetailDialog(
                    image = image,
                    onDismiss = { detailsDialogOpen = false },
                )
            }
        }
    }
}

@Composable
private fun GalleryPhotoDetailDialog(
    image: GalleryImageModel,
    onDismiss: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val labels = GalleryPhotoDetailLabels(
        title = stringResource(R.string.gallery_photo_detail_title_label),
        id = stringResource(R.string.gallery_photo_detail_id_label),
        dimensions = stringResource(R.string.gallery_photo_detail_dimensions_label),
        fileSize = stringResource(R.string.gallery_photo_detail_file_size_label),
        fileName = stringResource(R.string.gallery_photo_detail_file_name_label),
        path = stringResource(R.string.gallery_photo_detail_path_label),
        date = stringResource(R.string.gallery_photo_detail_date_label),
        studio = stringResource(R.string.gallery_photo_detail_studio_label),
        rating = stringResource(R.string.gallery_photo_detail_rating_label),
        oCounter = stringResource(R.string.gallery_photo_detail_o_counter_label),
        organized = stringResource(R.string.gallery_photo_detail_organized_label),
        photographer = stringResource(R.string.gallery_photo_detail_photographer_label),
        performers = stringResource(R.string.gallery_photo_detail_performers_label),
        tags = stringResource(R.string.gallery_photo_detail_tags_label),
        linkedGalleries = stringResource(R.string.gallery_photo_detail_linked_galleries_label),
        details = stringResource(R.string.gallery_photo_detail_details_label),
    )
    val rows = image.galleryPhotoDetailRows(
        labels = labels,
        oCounterLabel = { count -> resources.getString(R.string.gallery_image_o_counter_label, count) },
        organizedLabel = stringResource(R.string.gallery_image_organized_label),
        unorganizedLabel = stringResource(R.string.gallery_image_unorganized_label),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.gallery_photo_viewer_details_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rows.forEach { row ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = row.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = row.value,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.gallery_photo_viewer_details_dismiss))
            }
        },
    )
}

@Composable
private fun GalleryContent(
    isFoldLikeLayout: Boolean,
    browseMode: StashGalleryBrowseMode,
    onSelectBrowseMode: (StashGalleryBrowseMode) -> Unit,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    pageState: StashGalleryGridPageState,
    imagePageState: StashGalleryGlobalImageGridPageState,
    selectedImageFolderPath: String?,
    gallerySelectionState: GallerySelectionState,
    serverProfile: StashServerProfile?,
    onRetry: () -> Unit,
    onRetryImages: () -> Unit,
    onLoadMore: () -> Unit,
    onLoadMoreImages: () -> Unit,
    onOpenGallery: (GalleryCardModel) -> Unit,
    onOpenImage: (Int, List<GalleryImageModel>) -> Unit,
    onSelectImageFolder: (GalleryImageFolderGroup) -> Unit,
    onBackToImageFolders: () -> Unit,
    onLongPressGallery: (GalleryCardModel) -> Unit,
    onOpenSettings: () -> Unit,
    onSelectSort: (StashGallerySortOption) -> Unit,
    onToggleSortDirection: () -> Unit,
    onRandomAction: () -> Unit,
    onSelectPageSize: (Int) -> Unit,
    onSelectDisplayMode: (StashGalleryDisplayMode) -> Unit,
    onClearSelection: () -> Unit,
    onSelectVisibleGalleries: () -> Unit,
    onInvertVisibleSelection: () -> Unit,
    onOpenSelectedGallery: () -> Unit,
    onOpenFirstVisibleGallery: () -> Unit,
    onOpenRandomVisibleGallery: () -> Unit,
    onOpenEntityFilter: (GalleryEntityFilterKind) -> Unit,
    onOpenImageEntityFilter: (ImageEntityFilterKind) -> Unit,
    onOpenSavedFilters: () -> Unit,
    onOpenSavedImageFilters: () -> Unit,
    onClearFilterCategory: (StashGalleryFilterCategory) -> Unit,
    onClearImageFilterCategory: (StashImageFilterCategory) -> Unit,
    onOpenLinkedGallery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = if (isFoldLikeLayout) 48.dp else 24.dp
    val gridGap = StashSpacing.CardGap
    val bottomContentPadding = 96.dp
    val layoutPolicy = stashGalleryGridLayoutPolicy(pageState.displayMode, isFoldLikeLayout)
    val columns = layoutPolicy.columns
    val thumbnailHeight = layoutPolicy.thumbnailHeightDp.dp
    val galleries = pageState.galleries
    val images = imagePageState.images
    val imageLayoutPolicy = stashGalleryImageGridLayoutPolicy(imagePageState.displayMode, isFoldLikeLayout)
    val imageColumns = imageLayoutPolicy.columns
    val imageThumbnailHeight = imageLayoutPolicy.thumbnailHeightDp.dp
    val headerPolicy = stashGalleryTopLevelHeaderPolicy()
    val toolbarPolicy = stashGalleryMediaToolbarPolicy(browseMode)
    val galleryGridState = rememberLazyGridState()
    val imageGridState = rememberLazyGridState()
    val imageFolderGridState = rememberLazyGridState()
    val imageFolderDetailGridStates = remember { mutableStateMapOf<String, LazyGridState>() }
    val imageFolderDetailGridState = remember(selectedImageFolderPath) {
        selectedImageFolderPath
            ?.let { folderPath -> imageFolderDetailGridStates.getOrPut(folderPath) { LazyGridState() } }
            ?: LazyGridState()
    }
    val activeImageGridState = when {
        imagePageState.displayMode == StashGalleryDisplayMode.Folders && selectedImageFolderPath != null -> imageFolderDetailGridState
        imagePageState.displayMode == StashGalleryDisplayMode.Folders -> imageFolderGridState
        else -> imageGridState
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = StashSpacing.SectionGap),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
    ) {
        StashScreenHeader(
            title = if (headerPolicy.showTitle) stringResource(R.string.gallery_screen_title) else "",
            subtitle = if (headerPolicy.showSubtitle) "" else null,
            modifier = Modifier.padding(horizontal = horizontalPadding),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        ) {
            StashSecondaryButton(
                text = stringResource(R.string.gallery_browse_mode_galleries),
                selected = browseMode == StashGalleryBrowseMode.Galleries,
                onClick = { onSelectBrowseMode(StashGalleryBrowseMode.Galleries) },
            )
            StashSecondaryButton(
                text = stringResource(R.string.gallery_browse_mode_images),
                selected = browseMode == StashGalleryBrowseMode.Images,
                onClick = { onSelectBrowseMode(StashGalleryBrowseMode.Images) },
            )
        }

        if (toolbarPolicy.isActive(StashGalleryMediaControl.Search)) {
            StashDiscoverySearchInput(
                value = inputText,
                enabled = serverProfile != null,
                onValueChange = onInputTextChange,
                onClear = onClearInput,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        }

        if (toolbarPolicy.isActive(StashGalleryMediaControl.Sort)) {
            val isImageMode = browseMode == StashGalleryBrowseMode.Images
            StashGalleryToolbar(
                horizontalPadding = horizontalPadding,
                isConfigured = serverProfile != null,
                sortOption = if (isImageMode) imagePageState.sortOption else pageState.sortOption,
                sortOptions = if (isImageMode) stashImageSortOptions() else stashGallerySortOptions(),
                sortDirection = if (isImageMode) imagePageState.sortDirection else pageState.sortDirection,
                pageSize = if (isImageMode) imagePageState.pageSize else pageState.pageSize,
                pageSizeOptions = defaultStashDiscoveryPageSizeOptions(),
                displayMode = if (isImageMode) imagePageState.displayMode else pageState.displayMode,
                displayModeOptions = if (isImageMode) stashGalleryImageDisplayModes() else stashGalleryDisplayModes(),
                visibleCount = if (isImageMode) images.size else galleries.size,
                selectionCount = if (isImageMode) 0 else gallerySelectionState.selectedCount,
                onClearSelection = if (isImageMode) null else onClearSelection,
                onSelectAll = if (isImageMode) null else onSelectVisibleGalleries,
                onInvertSelection = if (isImageMode) null else onInvertVisibleSelection,
                onOpenSelection = if (isImageMode) null else onOpenSelectedGallery,
                onOpenFirst = if (isImageMode) null else onOpenFirstVisibleGallery,
                onOpenRandom = if (isImageMode) null else onOpenRandomVisibleGallery,
                onOpenRandomSelection = if (isImageMode) null else onOpenRandomVisibleGallery,
                onSelectSort = onSelectSort,
                onToggleSortDirection = onToggleSortDirection,
                onRandomAction = onRandomAction,
                onSelectPageSize = onSelectPageSize,
                onSelectDisplayMode = onSelectDisplayMode,
                showGalleryOperations = !isImageMode,
            )

            if (isImageMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                ) {
                    StashSecondaryButton(
                        text = stashString(R.string.auto_kr_0344),
                        enabled = serverProfile != null,
                        onClick = onOpenSavedImageFilters,
                    )
                    ImageEntityFilterKind.entries.forEach { kind ->
                        val count = kind.selectedFrom(imagePageState.imageFilter).size
                        StashSecondaryButton(
                            text = kind.buttonLabel(count),
                            enabled = serverProfile != null,
                            onClick = { onOpenImageEntityFilter(kind) },
                        )
                    }
                }

                if (imagePageState.imageFilter.activeFilterChips().isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    ) {
                        imagePageState.imageFilter.activeFilterChips().forEach { chip ->
                            StashSecondaryButton(
                                text = chip.label,
                                enabled = serverProfile != null,
                                onClick = { onClearImageFilterCategory(chip.category) },
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                ) {
                    StashSecondaryButton(
                        text = stashString(R.string.auto_kr_0344),
                        enabled = serverProfile != null,
                        onClick = onOpenSavedFilters,
                    )
                    GalleryEntityFilterKind.entries.forEach { kind ->
                        val count = kind.selectedFrom(pageState.galleryFilter).size
                        StashSecondaryButton(
                            text = kind.buttonLabel(count),
                            enabled = serverProfile != null,
                            onClick = { onOpenEntityFilter(kind) },
                        )
                    }
                }

                if (pageState.galleryFilter.activeFilterChips().isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    ) {
                        pageState.galleryFilter.activeFilterChips().forEach { chip ->
                            StashSecondaryButton(
                                text = chip.label,
                                enabled = serverProfile != null,
                                onClick = { onClearFilterCategory(chip.category) },
                            )
                        }
                    }
                }
            }
        }

        val visibleCount = if (browseMode == StashGalleryBrowseMode.Images) images.size else galleries.size
        val totalCount = if (browseMode == StashGalleryBrowseMode.Images) imagePageState.totalCount else pageState.totalCount
        if (visibleCount > 0 || totalCount != null) {
            Text(
                text = stringResource(
                    if (browseMode == StashGalleryBrowseMode.Images) {
                        R.string.gallery_global_images_result_count_label
                    } else {
                        R.string.gallery_result_count_label
                    },
                    visibleCount,
                    totalCount ?: visibleCount,
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            serverProfile == null -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stringResource(R.string.gallery_requires_server_title),
                    message = stringResource(R.string.gallery_requires_server_message),
                    primaryActionLabel = stringResource(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onPrimaryAction = onOpenSettings,
            )

            browseMode == StashGalleryBrowseMode.Images && imagePageState.isLoading && images.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.gallery_global_images_loading_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            browseMode == StashGalleryBrowseMode.Images && imagePageState.error != null && images.isEmpty() -> StashErrorState(
                state = StashErrorStateModel(
                    title = stringResource(R.string.gallery_global_images_load_failed_title),
                    message = imagePageState.error,
                    secondaryActionLabel = stringResource(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onRetry = onRetryImages,
                onSecondaryAction = onOpenSettings,
            )

            browseMode == StashGalleryBrowseMode.Images && images.isEmpty() -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stringResource(R.string.gallery_global_images_no_results_title),
                    message = stringResource(R.string.gallery_global_images_no_results_message),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )

            browseMode == StashGalleryBrowseMode.Images -> LazyVerticalGrid(
                columns = GridCells.Fixed(imageColumns),
                state = activeImageGridState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomContentPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                if (imagePageState.displayMode == StashGalleryDisplayMode.Folders) {
                    val folderGroups = groupGalleryImagesByParentFolder(
                        images = images,
                        unfiledLabel = stashString(R.string.gallery_image_unfiled_folder_label),
                        sortDirection = if (imagePageState.sortOption.serverValue == "path" || imagePageState.sortOption.serverValue == "random") {
                            imagePageState.sortDirection
                        } else {
                            StashSortDirection.Asc
                        },
                        sortOption = imagePageState.sortOption,
                        randomSeed = imagePageState.randomSeed,
                    )
                    val selectedGroup = selectedImageFolderPath?.let { path ->
                        folderGroups.firstOrNull { group -> group.path == path }
                    }
                    if (selectedGroup == null) {
                        items(folderGroups, key = { group -> "folder-card-${group.id}" }) { group ->
                            GalleryImageFolderCard(
                                folder = group,
                                serverProfile = serverProfile,
                                thumbnailHeight = imageThumbnailHeight,
                                onClick = { onSelectImageFolder(group) },
                            )
                        }
                    } else {
                        item(key = "folder-detail-${selectedGroup.id}", span = { GridItemSpan(maxLineSpan) }) {
                            GalleryImageFolderHeader(
                                folder = selectedGroup,
                                onNavigateBack = onBackToImageFolders,
                            )
                        }
                        items(selectedGroup.items, key = { item -> item.image.id }) { item ->
                            GalleryPhotoCard(
                                image = item.image,
                                serverProfile = serverProfile,
                                thumbnailHeight = imageThumbnailHeight,
                                onOpenLinkedGallery = onOpenLinkedGallery,
                                onClick = {
                                    selectedGroup.viewerRequestForImage(item.image.id)?.let { request ->
                                        onOpenImage(request.initialIndex, request.images)
                                    }
                                },
                            )
                        }
                    }
                } else {
                    itemsIndexed(images, key = { _, image -> image.id }) { index, image ->
                        GalleryPhotoCard(
                            image = image,
                            serverProfile = serverProfile,
                            thumbnailHeight = imageThumbnailHeight,
                            onOpenLinkedGallery = onOpenLinkedGallery,
                            onClick = { onOpenImage(index, images) },
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GalleryGlobalImageFooter(
                        pageState = imagePageState,
                        onRetry = onRetryImages,
                        onLoadMore = onLoadMoreImages,
                    )
                }
            }

            pageState.isLoading && galleries.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            pageState.error != null && galleries.isEmpty() -> StashErrorState(
                state = StashErrorStateModel(
                    title = stringResource(R.string.gallery_load_failed_title),
                    message = pageState.error,
                    secondaryActionLabel = stringResource(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onRetry = onRetry,
                onSecondaryAction = onOpenSettings,
            )

            galleries.isEmpty() -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stringResource(R.string.gallery_no_results_title),
                    message = stringResource(R.string.gallery_no_results_message),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = galleryGridState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomContentPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                items(galleries, key = { it.id }) { gallery ->
                    GalleryCard(
                        gallery = gallery,
                        serverProfile = serverProfile,
                        thumbnailHeight = thumbnailHeight,
                        displayMode = pageState.displayMode,
                        isSelected = gallery.id in gallerySelectionState.selectedGalleryIds,
                        onClick = { onOpenGallery(gallery) },
                        onLongClick = { onLongPressGallery(gallery) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GalleryFooter(
                        pageState = pageState,
                        onRetry = onRetry,
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryDetailContent(
    galleryId: String,
    isFoldLikeLayout: Boolean,
    pageState: StashGalleryImageGridPageState,
    serverProfile: StashServerProfile?,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenPhoto: (String, Int, List<GalleryImageModel>) -> Unit,
    onOpenScene: (String) -> Unit,
    onOpenGallery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = if (isFoldLikeLayout) 48.dp else 24.dp
    val gridGap = StashSpacing.CardGap
    val bottomContentPadding = 96.dp
    val columns = stashGalleryImageGridColumnCount(isFoldLikeLayout)
    val thumbnailHeight = stashGalleryImageGridThumbnailHeightDp(isFoldLikeLayout).dp
    val images = pageState.images
    val title = pageState.gallery?.title ?: stringResource(R.string.gallery_detail_title_fallback, galleryId)
    val galleryDetailGridState = rememberLazyGridState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = StashSpacing.SectionGap),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
    ) {
        StashScreenHeader(
            title = title,
            subtitle = stringResource(R.string.gallery_detail_subtitle),
            modifier = Modifier.padding(horizontal = horizontalPadding),
            trailing = {
                StashSecondaryButton(
                    text = stringResource(R.string.settings_back_button),
                    onClick = onNavigateBack,
                )
            },
        )

        if (images.isNotEmpty() || pageState.totalCount != null) {
            Text(
                text = stringResource(
                    R.string.gallery_detail_result_count_label,
                    images.size,
                    pageState.totalCount ?: images.size,
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            serverProfile == null -> StashEmptyState(
                state = StashEmptyStateModel(
                    title = stringResource(R.string.gallery_requires_server_title),
                    message = stringResource(R.string.gallery_requires_server_message),
                    primaryActionLabel = stringResource(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onPrimaryAction = onOpenSettings,
            )

            pageState.isLoading && images.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            pageState.error != null && images.isEmpty() -> StashErrorState(
                state = StashErrorStateModel(
                    title = stringResource(R.string.gallery_detail_load_failed_title),
                    message = pageState.error,
                    secondaryActionLabel = stringResource(R.string.auto_kr_0270),
                ),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onRetry = onRetry,
                onSecondaryAction = onOpenSettings,
            )

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = galleryDetailGridState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomContentPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                pageState.galleryDetail?.let { detail ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GalleryDetailReadOnlyPanels(
                            detail = detail,
                            imageCount = images.size,
                            onOpenScene = onOpenScene,
                            onOpenChapter = { targetIndex -> onOpenPhoto(galleryId, targetIndex, images) },
                        )
                    }
                }
                if (images.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StashEmptyState(
                            state = StashEmptyStateModel(
                                title = stringResource(R.string.gallery_detail_no_results_title),
                                message = stringResource(R.string.gallery_detail_no_results_message),
                            ),
                        )
                    }
                }
                itemsIndexed(images, key = { _, image -> image.id }) { index, image ->
                    GalleryPhotoCard(
                        image = image,
                        serverProfile = serverProfile,
                        thumbnailHeight = thumbnailHeight,
                        onOpenLinkedGallery = onOpenGallery,
                        onClick = { onOpenPhoto(galleryId, index, images) },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GalleryImageFooter(
                        pageState = pageState,
                        onRetry = onRetry,
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun GalleryDetailReadOnlyPanels(
    detail: StashGalleryDetailModel,
    imageCount: Int,
    onOpenScene: (String) -> Unit,
    onOpenChapter: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
    ) {
        GalleryDetailInfoPanel(detail)
        if (detail.linkedScenes.isNotEmpty()) {
            GalleryReadOnlySectionCard(title = stringResource(R.string.gallery_detail_linked_scenes_title)) {
                detail.linkedScenes.forEach { scene ->
                    Text(
                        text = scene.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onOpenScene(scene.id) })
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (detail.files.isNotEmpty()) {
            GalleryReadOnlySectionCard(title = stringResource(R.string.gallery_detail_files_title)) {
                detail.files.forEach { file ->
                    GalleryFileInfoRow(file)
                }
            }
        }
        if (detail.chapters.isNotEmpty()) {
            GalleryReadOnlySectionCard(title = stringResource(R.string.gallery_detail_chapters_title)) {
                detail.chapters.forEach { chapter ->
                    val targetIndex = chapter.targetImageIndex(imageCount)
                    Text(
                        text = stringResource(R.string.gallery_detail_chapter_row, chapter.title, chapter.imageIndex + 1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(enabled = targetIndex != null, onClick = { targetIndex?.let(onOpenChapter) })
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (targetIndex != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryDetailInfoPanel(detail: StashGalleryDetailModel) {
    val resources = LocalContext.current.resources
    val gallery = detail.gallery
    val metadata = gallery.galleryMetadataLabels(
        imageCountLabel = { count -> resources.getString(R.string.gallery_image_count_label, count) },
        ratingLabel = { rating -> resources.getString(R.string.gallery_rating_label, rating) },
        organizedLabel = resources.getString(R.string.stash_sort_organized_label),
        tagCountLabel = { count -> resources.getString(R.string.gallery_tag_count_label, count) },
        performerCountLabel = { count -> resources.getString(R.string.gallery_performer_count_label, count) },
        sceneCountLabel = { count -> resources.getString(R.string.gallery_scene_count_label, count) },
    )
    val extraMetadata = listOfNotNull(
        detail.fileCount?.let { count -> resources.getString(R.string.gallery_file_count_label, count) },
        detail.code?.let { code -> resources.getString(R.string.gallery_code_label, code) },
        detail.photographer?.let { photographer -> resources.getString(R.string.gallery_photographer_label, photographer) },
    )
    GalleryReadOnlySectionCard(title = stringResource(R.string.gallery_detail_metadata_title)) {
        if (metadata.isNotEmpty() || extraMetadata.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (metadata + extraMetadata).forEach { label ->
                    StashMetadataBadge(StashMetadataBadgeModel(label = label))
                }
            }
        }
        gallery.details?.takeIf { it.isNotBlank() }?.let { details ->
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val chips = gallery.galleryRelationshipChipLabels(maxPerGroup = 8)
        if (chips.isNotEmpty()) {
            Text(
                text = chips.joinToString(separator = stringResource(R.string.gallery_metadata_separator)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GalleryFileInfoRow(file: GalleryFileInfoModel) {
    val resources = LocalContext.current.resources
    val labels = file.galleryFileInfoLabels(
        fileNameLabel = { name -> resources.getString(R.string.gallery_file_name_label, name) },
        fileSizeLabel = { size -> resources.getString(R.string.gallery_file_size_label, size) },
    )
    if (labels.isEmpty()) return
    Text(
        text = labels.joinToString(separator = stringResource(R.string.gallery_metadata_separator)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun GalleryReadOnlySectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    StashMediaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun GalleryImageFolderCard(
    folder: GalleryImageFolderGroup,
    serverProfile: StashServerProfile,
    thumbnailHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val countLabel = stringResource(R.string.gallery_image_count_label, folder.imageCount)
    StashMediaCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbnailHeight)
                    .stashThumbnailClip()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val thumbnailModel = rememberStashThumbnailModel(folder.coverImage?.bestDisplayUrl, serverProfile)
                if (thumbnailModel != null) {
                    AsyncImage(
                        model = thumbnailModel,
                        contentDescription = stringResource(R.string.gallery_image_folder_open_content_description, folder.title),
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                StashMetadataBadge(
                    badge = StashMetadataBadgeModel(
                        label = countLabel,
                        contentDescription = countLabel,
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = folder.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (folder.path.isNotBlank()) {
                    Text(
                        text = folder.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryImageFolderHeader(
    folder: GalleryImageFolderGroup,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StashSecondaryButton(
            text = stringResource(R.string.gallery_image_folder_back_action),
            onClick = onNavigateBack,
        )
        Text(
            text = folder.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (folder.path.isNotBlank()) {
            Text(
                text = folder.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = stringResource(R.string.gallery_image_count_label, folder.imageCount),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun GalleryPhotoCard(
    image: GalleryImageModel,
    serverProfile: StashServerProfile,
    thumbnailHeight: androidx.compose.ui.unit.Dp,
    onOpenLinkedGallery: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val metadata = image.galleryImageMetadataLabels(
        oCounterLabel = { count -> resources.getString(R.string.gallery_image_o_counter_label, count) },
        organizedLabel = stringResource(R.string.stash_sort_organized_label),
    )
    val performerLabels = image.galleryImagePerformerLabels(limit = 3)
    val linkedGalleryLabels = image.galleryImageLinkedGalleryLabels(limit = 3)
    StashMediaCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbnailHeight)
                    .stashThumbnailClip()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                val thumbnailModel = rememberStashThumbnailModel(
                    image.thumbnailUrl ?: image.previewUrl ?: image.imageUrl,
                    serverProfile,
                )
                if (thumbnailModel != null) {
                    AsyncImage(
                        model = thumbnailModel,
                        contentDescription = stringResource(R.string.gallery_photo_open_content_description, image.title),
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = image.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metadata.isNotEmpty()) {
                    Text(
                        text = metadata.joinToString(separator = stringResource(R.string.gallery_metadata_separator)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (performerLabels.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.gallery_image_card_performers_label,
                            performerLabels.joinToString(separator = stringResource(R.string.gallery_metadata_separator)),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (linkedGalleryLabels.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.gallery_image_card_linked_galleries_label,
                            linkedGalleryLabels.joinToString(separator = stringResource(R.string.gallery_metadata_separator)),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    ) {
                        image.linkedGalleries.take(3).forEach { gallery ->
                            StashSecondaryButton(
                                text = gallery.title,
                                onClick = { onOpenLinkedGallery(gallery.id) },
                                enabled = gallery.id.isNotBlank(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryImageFooter(
    pageState: StashGalleryImageGridPageState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            pageState.isLoading -> CircularProgressIndicator()
            pageState.error != null -> StashSecondaryButton(
                text = stringResource(R.string.auto_kr_0031),
                onClick = onRetry,
            )
            pageState.hasMore -> StashSecondaryButton(
                text = stringResource(R.string.gallery_detail_load_more_action),
                onClick = onLoadMore,
            )
            else -> Text(
                text = stringResource(R.string.gallery_detail_end_of_results_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GalleryGlobalImageFooter(
    pageState: StashGalleryGlobalImageGridPageState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            pageState.isLoading -> CircularProgressIndicator()
            pageState.error != null -> StashSecondaryButton(
                text = stringResource(R.string.auto_kr_0031),
                onClick = onRetry,
            )
            pageState.hasMore -> StashSecondaryButton(
                text = stringResource(R.string.gallery_global_images_load_more_action),
                onClick = onLoadMore,
            )
            else -> Text(
                text = stringResource(R.string.gallery_global_images_end_of_results_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun GalleryCard(
    gallery: GalleryCardModel,
    serverProfile: StashServerProfile,
    thumbnailHeight: androidx.compose.ui.unit.Dp,
    displayMode: StashGalleryDisplayMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val imageCountText = gallery.imageCount?.let { count -> resources.getString(R.string.gallery_image_count_label, count) }
    val metadata = gallery.galleryMetadataLabels(
        imageCountLabel = { count -> resources.getString(R.string.gallery_image_count_label, count) },
        ratingLabel = { rating -> resources.getString(R.string.gallery_rating_label, rating) },
        organizedLabel = resources.getString(R.string.stash_sort_organized_label),
        tagCountLabel = { count -> resources.getString(R.string.gallery_tag_count_label, count) },
        performerCountLabel = { count -> resources.getString(R.string.gallery_performer_count_label, count) },
        sceneCountLabel = { count -> resources.getString(R.string.gallery_scene_count_label, count) },
    )
    val metadataText = metadata.joinToString(separator = stringResource(R.string.gallery_metadata_separator))
    val snippet = gallery.galleryPreviewSnippet()
    StashMediaCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        when (displayMode) {
            StashGalleryDisplayMode.List -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GalleryThumbnailBox(
                    gallery = gallery,
                    serverProfile = serverProfile,
                    imageCountText = imageCountText,
                    isSelected = isSelected,
                    modifier = Modifier
                        .width(132.dp)
                        .height(thumbnailHeight),
                )
                GalleryCardTextColumn(
                    gallery = gallery,
                    metadataText = metadataText,
                    snippet = snippet,
                    relationshipChipLabels = gallery.galleryRelationshipChipLabels(maxPerGroup = 2),
                    titleMaxLines = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp, top = 10.dp, bottom = 10.dp),
                )
            }

            StashGalleryDisplayMode.Wall -> Box {
                GalleryThumbnailBox(
                    gallery = gallery,
                    serverProfile = serverProfile,
                    imageCountText = imageCountText,
                    isSelected = isSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbnailHeight),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.54f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = gallery.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (metadataText.isNotBlank()) {
                        Text(
                            text = metadataText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            StashGalleryDisplayMode.Grid,
            StashGalleryDisplayMode.Folders -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GalleryThumbnailBox(
                    gallery = gallery,
                    serverProfile = serverProfile,
                    imageCountText = imageCountText,
                    isSelected = isSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(thumbnailHeight),
                )
                GalleryCardTextColumn(
                    gallery = gallery,
                    metadataText = metadataText,
                    snippet = snippet,
                    relationshipChipLabels = gallery.galleryRelationshipChipLabels(maxPerGroup = 2),
                    titleMaxLines = 2,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun GalleryThumbnailBox(
    gallery: GalleryCardModel,
    serverProfile: StashServerProfile,
    imageCountText: String?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .stashThumbnailClip()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val thumbnailModel = rememberStashThumbnailModel(gallery.bestThumbnailUrl, serverProfile)
        if (thumbnailModel != null) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = stringResource(R.string.gallery_card_thumbnail_content_description, gallery.title),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        imageCountText?.let { label ->
            StashMetadataBadge(
                badge = StashMetadataBadgeModel(
                    label = label,
                    contentDescription = label,
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
            )
            StashMetadataBadge(
                badge = StashMetadataBadgeModel(
                    label = stringResource(R.string.auto_kr_0298),
                    contentDescription = stringResource(R.string.auto_kr_0298),
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun GalleryCardTextColumn(
    gallery: GalleryCardModel,
    metadataText: String,
    snippet: String?,
    relationshipChipLabels: List<String>,
    titleMaxLines: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = gallery.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
        if (metadataText.isNotBlank()) {
            Text(
                text = metadataText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        snippet?.let { details ->
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (relationshipChipLabels.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                relationshipChipLabels.forEach { label ->
                    StashMetadataBadge(
                        badge = StashMetadataBadgeModel(
                            label = label,
                            contentDescription = label,
                        ),
                    )
                }
            }
        }
    }
}

private fun List<Offset>.averageOffset(): Offset {
    if (isEmpty()) return Offset.Zero
    val x = sumOf { it.x.toDouble() }.toFloat() / size
    val y = sumOf { it.y.toDouble() }.toFloat() / size
    return Offset(x, y)
}

@Composable
private fun GalleryFooter(
    pageState: StashGalleryGridPageState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            pageState.isLoading -> CircularProgressIndicator()
            pageState.error != null -> StashSecondaryButton(
                text = stringResource(R.string.auto_kr_0031),
                onClick = onRetry,
            )
            pageState.hasMore -> StashSecondaryButton(
                text = stringResource(R.string.gallery_load_more_action),
                onClick = onLoadMore,
            )
            else -> Text(
                text = stringResource(R.string.gallery_end_of_results_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
