package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.math.abs

data class GalleryCardModel(
    val id: String,
    val title: String,
    val coverUrl: String? = null,
    val previewUrl: String? = null,
    val imageCount: Int? = null,
    val date: String? = null,
    val studio: String? = null,
    val rating100: Int? = null,
    val details: String? = null,
    val organized: Boolean? = null,
    val performerCount: Int? = null,
    val sceneCount: Int? = null,
    val tagChips: List<SceneCardTagChip> = emptyList(),
    val performerChips: List<SceneCardTagChip> = emptyList(),
    val sceneChips: List<SceneCardTagChip> = emptyList(),
) {
    val bestThumbnailUrl: String?
        get() = coverUrl ?: previewUrl
}

data class GalleryLinkedGalleryModel(
    val id: String,
    val title: String,
)

data class GalleryImageModel(
    val id: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val previewUrl: String? = null,
    val imageUrl: String? = null,
    val date: String? = null,
    val studio: String? = null,
    val rating100: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val sizeBytes: Long? = null,
    val tagChips: List<SceneCardTagChip> = emptyList(),
    val performerChips: List<SceneCardTagChip> = emptyList(),
    val linkedGalleries: List<GalleryLinkedGalleryModel> = emptyList(),
    val details: String? = null,
    val photographer: String? = null,
    val organized: Boolean? = null,
    val oCounter: Int? = null,
    val fileName: String? = null,
    val filePath: String? = null,
    val parentFolderId: String? = null,
    val parentFolderPath: String? = null,
    val parentFolderName: String? = null,
) {
    val bestDisplayUrl: String?
        get() = imageUrl ?: previewUrl ?: thumbnailUrl

    val bestViewerImageUrl: String?
        get() = imageUrl.nonBlankOrNull() ?: previewUrl.nonBlankOrNull() ?: thumbnailUrl.nonBlankOrNull()
}

data class GalleryImageFolderItem(
    val image: GalleryImageModel,
    val originalIndex: Int,
)

data class GalleryImageFolderViewerRequest(
    val initialIndex: Int,
    val images: List<GalleryImageModel>,
)

data class GalleryImageFolderGroup(
    val title: String,
    val path: String,
    val items: List<GalleryImageFolderItem>,
    val folderId: String? = null,
    val imageCountOverride: Int? = null,
    val coverImageOverride: GalleryImageModel? = null,
    val hasSubFolders: Boolean = false,
    val showLoadedItemCount: Boolean = true,
) {
    val id: String get() = folderId?.trim()?.takeIf { it.isNotBlank() } ?: path.ifBlank { "__unfiled__" }
    val knownImageCount: Int? get() = imageCountOverride ?: items.size.takeIf { showLoadedItemCount && it > 0 }
    val imageCount: Int get() = knownImageCount ?: 0
    val images: List<GalleryImageModel> get() = items.map { it.image }
    val coverImage: GalleryImageModel? get() = coverImageOverride ?: items.firstOrNull()?.image

    fun viewerRequestForImage(imageId: String): GalleryImageFolderViewerRequest? {
        val index = items.indexOfFirst { item -> item.image.id == imageId }
        if (index < 0) return null
        return GalleryImageFolderViewerRequest(
            initialIndex = index,
            images = images,
        )
    }
}

data class GalleryLinkedSceneModel(
    val id: String,
    val title: String,
)

data class GalleryFileInfoModel(
    val fileName: String,
    val sizeBytes: Long? = null,
)

data class GalleryChapterModel(
    val id: String,
    val title: String,
    val imageIndex: Int,
)

data class StashGalleryDetailModel(
    val gallery: GalleryCardModel,
    val code: String? = null,
    val photographer: String? = null,
    val fileCount: Int? = null,
    val linkedScenes: List<GalleryLinkedSceneModel> = emptyList(),
    val files: List<GalleryFileInfoModel> = emptyList(),
    val chapters: List<GalleryChapterModel> = emptyList(),
)

/**
 * Pure reducer state for safe gallery multi-selection.
 *
 * Long press enters selection mode. While active, normal card taps toggle gallery
 * ids instead of opening detail. Toolbar requests only choose already-loaded
 * visible galleries and always clear selection after a successful navigation.
 */
data class GallerySelectionState(
    val selectedGalleryIds: Set<String> = emptySet(),
) {
    val isActive: Boolean
        get() = selectedGalleryIds.isNotEmpty()

    val selectedCount: Int
        get() = selectedGalleryIds.size

    fun selectFromLongPress(galleryId: String): GallerySelectionState {
        val normalizedId = galleryId.normalizedGalleryIdOrNull() ?: return this
        return copy(selectedGalleryIds = selectedGalleryIds + normalizedId)
    }

    fun handleCardTap(galleryId: String): GallerySelectionTapResult {
        val normalizedId = galleryId.normalizedGalleryIdOrNull()
        if (!isActive || normalizedId == null) {
            return GallerySelectionTapResult(state = this, shouldOpenGallery = true)
        }
        val updatedSelection = if (normalizedId in selectedGalleryIds) {
            selectedGalleryIds - normalizedId
        } else {
            selectedGalleryIds + normalizedId
        }
        return GallerySelectionTapResult(
            state = copy(selectedGalleryIds = updatedSelection),
            shouldOpenGallery = false,
        )
    }

    fun clear(): GallerySelectionState = copy(selectedGalleryIds = emptySet())

    fun selectVisibleGalleries(visibleGalleryIds: Iterable<String>): GallerySelectionState = copy(
        selectedGalleryIds = visibleGalleryIds.normalizedGalleryIdSet(),
    )

    fun invertVisibleSelection(visibleGalleryIds: Iterable<String>): GallerySelectionState {
        val visibleIds = visibleGalleryIds.normalizedGalleryIdSet()
        val hiddenSelection = selectedGalleryIds - visibleIds
        val invertedVisibleSelection = visibleIds - selectedGalleryIds
        return copy(selectedGalleryIds = hiddenSelection + invertedVisibleSelection)
    }

    fun selectedOpenRequest(visibleGalleries: List<GalleryCardModel>): SelectedGalleryNavigationRequest? {
        val selectedGallery = visibleGalleries.firstOrNull { it.id in selectedGalleryIds } ?: return null
        return SelectedGalleryNavigationRequest(
            galleryId = selectedGallery.id,
            nextSelectionState = clear(),
        )
    }

    fun toolbarOpenFirstRequest(visibleGalleries: List<GalleryCardModel>): SelectedGalleryNavigationRequest? {
        if (isActive) return selectedOpenRequest(visibleGalleries)
        val firstGallery = visibleGalleries.firstOrNull() ?: return null
        return SelectedGalleryNavigationRequest(
            galleryId = firstGallery.id,
            nextSelectionState = clear(),
        )
    }

    fun randomOpenRequest(
        visibleGalleries: List<GalleryCardModel>,
        randomIndex: Int,
    ): SelectedGalleryNavigationRequest? {
        val candidates = if (isActive) {
            visibleGalleries.filter { it.id in selectedGalleryIds }
        } else {
            visibleGalleries
        }
        if (candidates.isEmpty()) return null
        val selectedGallery = candidates[Math.floorMod(randomIndex, candidates.size)]
        return SelectedGalleryNavigationRequest(
            galleryId = selectedGallery.id,
            nextSelectionState = clear(),
        )
    }

    fun clearIfResultIdentityChanged(
        previousGalleryIds: List<String>,
        currentGalleryIds: List<String>,
    ): GallerySelectionState = if (previousGalleryIds == currentGalleryIds) this else clear()
}

data class GallerySelectionTapResult(
    val state: GallerySelectionState,
    val shouldOpenGallery: Boolean,
)

data class SelectedGalleryNavigationRequest(
    val galleryId: String,
    val nextSelectionState: GallerySelectionState,
)

private fun String.normalizedGalleryIdOrNull(): String? = trim().takeIf { it.isNotBlank() }

private fun Iterable<String>.normalizedGalleryIdSet(): Set<String> = mapNotNull { it.normalizedGalleryIdOrNull() }.toSet()

data class GalleryPhotoViewerPagePolicy(
    val currentIndex: Int,
    val totalCount: Int,
    val indexLabel: String,
    val chromeVisible: Boolean,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val image: GalleryImageModel?,
    val viewerImageUrl: String?,
)

fun galleryPhotoViewerPreloadImageUrls(
    images: List<GalleryImageModel>,
    currentIndex: Int,
    preloadAhead: Int = 1,
): List<String> {
    if (images.isEmpty() || preloadAhead <= 0) return emptyList()
    val safeCurrentIndex = currentIndex.coerceIn(0, images.lastIndex)
    val lastPreloadIndex = (safeCurrentIndex + preloadAhead).coerceAtMost(images.lastIndex)
    if (safeCurrentIndex >= lastPreloadIndex) return emptyList()
    return ((safeCurrentIndex + 1)..lastPreloadIndex)
        .mapNotNull { index -> images[index].bestViewerImageUrl }
        .distinct()
}

data class GalleryPhotoZoomState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

data class GalleryPhotoZoomPolicy(
    val minScale: Float = 1f,
    val maxScale: Float = 4f,
    val doubleTapScale: Float = 2.5f,
    val zoomedEpsilon: Float = 0.005f,
)

enum class GalleryPhotoDisplayMode {
    FitToScreen,
    OriginalSize,
}

enum class ContentScalePolicy {
    Fit,
    None,
}

data class GalleryPhotoDisplayModeUpdate(
    val displayMode: GalleryPhotoDisplayMode,
    val zoomState: GalleryPhotoZoomState,
    val contentScalePolicy: ContentScalePolicy,
)

fun toggleGalleryPhotoDisplayMode(
    currentMode: GalleryPhotoDisplayMode,
    currentZoomState: GalleryPhotoZoomState,
): GalleryPhotoDisplayModeUpdate {
    val nextMode = when (currentMode) {
        GalleryPhotoDisplayMode.FitToScreen -> GalleryPhotoDisplayMode.OriginalSize
        GalleryPhotoDisplayMode.OriginalSize -> GalleryPhotoDisplayMode.FitToScreen
    }
    return GalleryPhotoDisplayModeUpdate(
        displayMode = nextMode,
        zoomState = GalleryPhotoZoomState(),
        contentScalePolicy = nextMode.toContentScalePolicy(),
    )
}

fun GalleryPhotoDisplayMode.toContentScalePolicy(): ContentScalePolicy = when (this) {
    GalleryPhotoDisplayMode.FitToScreen -> ContentScalePolicy.Fit
    GalleryPhotoDisplayMode.OriginalSize -> ContentScalePolicy.None
}

enum class GalleryPhotoSlideshowTrigger {
    Timer,
    ManualNavigation,
    CloseOrDispose,
    Error,
}

sealed class GalleryPhotoSlideshowAction {
    data class AdvanceTo(val index: Int) : GalleryPhotoSlideshowAction()
    data object PauseAtBoundary : GalleryPhotoSlideshowAction()
    data object PauseForManualNavigation : GalleryPhotoSlideshowAction()
    data object PauseForCloseOrDispose : GalleryPhotoSlideshowAction()
    data object PauseForError : GalleryPhotoSlideshowAction()
    data object AlreadyPaused : GalleryPhotoSlideshowAction()
}

fun resolveGalleryPhotoSlideshowAction(
    playing: Boolean,
    currentIndex: Int,
    totalCount: Int,
    trigger: GalleryPhotoSlideshowTrigger,
): GalleryPhotoSlideshowAction {
    if (!playing) return GalleryPhotoSlideshowAction.AlreadyPaused
    return when (trigger) {
        GalleryPhotoSlideshowTrigger.ManualNavigation -> GalleryPhotoSlideshowAction.PauseForManualNavigation
        GalleryPhotoSlideshowTrigger.CloseOrDispose -> GalleryPhotoSlideshowAction.PauseForCloseOrDispose
        GalleryPhotoSlideshowTrigger.Error -> GalleryPhotoSlideshowAction.PauseForError
        GalleryPhotoSlideshowTrigger.Timer -> {
            val safeLastIndex = (totalCount - 1).coerceAtLeast(0)
            val safeCurrentIndex = currentIndex.coerceIn(0, safeLastIndex)
            if (totalCount <= 1 || safeCurrentIndex >= safeLastIndex) {
                GalleryPhotoSlideshowAction.PauseAtBoundary
            } else {
                GalleryPhotoSlideshowAction.AdvanceTo(safeCurrentIndex + 1)
            }
        }
    }
}

enum class GalleryImageOCounterAction {
    Increment,
    Decrement,
    Reset,
}

fun shouldStartGalleryImageMutation(inFlight: Boolean, imageId: String?): Boolean =
    !inFlight && !imageId.isNullOrBlank()

fun optimisticGalleryImageOCounter(currentCount: Int?, action: GalleryImageOCounterAction): Int {
    val safeCount = (currentCount ?: 0).coerceAtLeast(0)
    return when (action) {
        GalleryImageOCounterAction.Increment -> safeCount + 1
        GalleryImageOCounterAction.Decrement -> (safeCount - 1).coerceAtLeast(0)
        GalleryImageOCounterAction.Reset -> 0
    }
}

fun galleryPhotoViewerOCounterToolbarActions(): List<GalleryImageOCounterAction> = listOf(
    GalleryImageOCounterAction.Increment,
)

data class GalleryPhotoLinkedGalleryNavigation(
    val galleryId: String,
    val label: String,
)

fun resolveGalleryPhotoLinkedGalleryNavigation(
    image: GalleryImageModel?,
    linkedGalleryIndex: Int,
): GalleryPhotoLinkedGalleryNavigation? {
    val linked = image?.linkedGalleries
        ?.filter { gallery -> gallery.id.isNotBlank() }
        ?.getOrNull(linkedGalleryIndex)
        ?: return null
    return GalleryPhotoLinkedGalleryNavigation(
        galleryId = linked.id.trim(),
        label = linked.title.trim().ifBlank { linked.id.trim() },
    )
}

data class GalleryAppreciationModeState(
    val enabled: Boolean = false,
    val tapNavigationEnabled: Boolean = false,
    val chromeVisible: Boolean = true,
    val modalOpen: Boolean = false,
)

enum class GalleryPhotoTapAction {
    ToggleChrome,
    PreviousImage,
    NextImage,
    PreviousBoundaryNoOp,
    NextBoundaryNoOp,
    Ignore,
}

fun resolveGalleryPhotoTapAction(
    state: GalleryAppreciationModeState,
    tapX: Float,
    viewportWidth: Float,
    zoomState: GalleryPhotoZoomState,
    hasPrevious: Boolean,
    hasNext: Boolean,
    controlsOwnTap: Boolean = false,
    doubleTapCandidate: Boolean = false,
    centerDeadZoneFraction: Float = 0.34f,
    zoomPolicy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): GalleryPhotoTapAction {
    if (controlsOwnTap || doubleTapCandidate || viewportWidth <= 0f || tapX.isNaN()) {
        return GalleryPhotoTapAction.Ignore
    }
    if (!state.enabled || !state.tapNavigationEnabled) {
        return GalleryPhotoTapAction.ToggleChrome
    }
    if (!galleryPhotoViewerPagerSwipeEnabled(zoomState, zoomPolicy)) {
        return GalleryPhotoTapAction.ToggleChrome
    }

    val centerFraction = centerDeadZoneFraction.coerceIn(0f, 0.9f)
    val sideFraction = (1f - centerFraction) / 2f
    val leftBoundary = viewportWidth * sideFraction
    val rightBoundary = viewportWidth * (1f - sideFraction)
    return when {
        tapX < leftBoundary -> if (hasPrevious) {
            GalleryPhotoTapAction.PreviousImage
        } else {
            GalleryPhotoTapAction.PreviousBoundaryNoOp
        }
        tapX > rightBoundary -> if (hasNext) {
            GalleryPhotoTapAction.NextImage
        } else {
            GalleryPhotoTapAction.NextBoundaryNoOp
        }
        else -> GalleryPhotoTapAction.ToggleChrome
    }
}

data class GalleryPhotoViewerChromePolicy(
    val showEditingControls: Boolean,
    val showLinkedGalleryShortcut: Boolean,
    val showPersistentMetadataChrome: Boolean,
    val showAppreciationModeToggleInTopChrome: Boolean,
    val bottomActionRowScrollable: Boolean,
    val maxBottomRows: Int,
    val collapsedBottomRows: Int,
    val expandedBottomRows: Int,
    val defaultExpanded: Boolean,
)

fun galleryPhotoViewerChromePolicy(
    state: GalleryAppreciationModeState,
): GalleryPhotoViewerChromePolicy = if (state.enabled) {
    GalleryPhotoViewerChromePolicy(
        showEditingControls = false,
        showLinkedGalleryShortcut = false,
        showPersistentMetadataChrome = false,
        showAppreciationModeToggleInTopChrome = true,
        bottomActionRowScrollable = true,
        maxBottomRows = 1,
        collapsedBottomRows = 1,
        expandedBottomRows = 1,
        defaultExpanded = false,
    )
} else {
    GalleryPhotoViewerChromePolicy(
        showEditingControls = true,
        showLinkedGalleryShortcut = true,
        showPersistentMetadataChrome = true,
        showAppreciationModeToggleInTopChrome = true,
        bottomActionRowScrollable = true,
        maxBottomRows = 1,
        collapsedBottomRows = 1,
        expandedBottomRows = 3,
        defaultExpanded = false,
    )
}

fun shouldRevealGalleryPhotoChromeAfterTap(
    state: GalleryAppreciationModeState,
    action: GalleryPhotoTapAction,
): Boolean = when (action) {
    GalleryPhotoTapAction.ToggleChrome -> true
    GalleryPhotoTapAction.Ignore -> false
    GalleryPhotoTapAction.PreviousImage,
    GalleryPhotoTapAction.NextImage,
    GalleryPhotoTapAction.PreviousBoundaryNoOp,
    GalleryPhotoTapAction.NextBoundaryNoOp -> !state.enabled || !state.tapNavigationEnabled
}

fun shouldShowGalleryPhotoPageChangeHud(
    state: GalleryAppreciationModeState,
    suppressForDiscreteNavigation: Boolean,
): Boolean = !suppressForDiscreteNavigation || !state.enabled || !state.tapNavigationEnabled

fun shouldAutoHideGalleryPhotoChrome(
    state: GalleryAppreciationModeState,
    zoomState: GalleryPhotoZoomState,
    idleElapsedMs: Long,
    autoHideDelayMs: Long = 3_000L,
    loadingOrErrorVisible: Boolean = false,
    boundaryFeedbackVisible: Boolean = false,
    zoomPolicy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): Boolean = state.enabled &&
    state.chromeVisible &&
    !state.modalOpen &&
    !loadingOrErrorVisible &&
    !boundaryFeedbackVisible &&
    galleryPhotoViewerPagerSwipeEnabled(zoomState, zoomPolicy) &&
    idleElapsedMs >= autoHideDelayMs

data class StashGalleryPage(
    val galleries: List<GalleryCardModel>,
    val totalCount: Int,
)

data class StashGalleryTextFilterState(
    val title: String = "",
    val details: String = "",
    val code: String = "",
    val photographer: String = "",
    val path: String = "",
    val url: String = "",
    val checksum: String = "",
) {
    val isEmpty: Boolean get() = listOf(title, details, code, photographer, path, url, checksum).all { it.isBlank() }
}

data class StashGalleryNumberRange(
    val min: Int? = null,
    val max: Int? = null,
) {
    val isEmpty: Boolean get() = min == null && max == null
}

enum class StashGalleryFilterCategory(val id: String) {
    Title("title"),
    Details("details"),
    Code("code"),
    Photographer("photographer"),
    Path("path"),
    Url("url"),
    Checksum("checksum"),
    DateRange("date_range"),
    CreatedAtRange("created_at_range"),
    UpdatedAtRange("updated_at_range"),
    Rating("rating"),
    BooleanFlags("boolean_flags"),
    Counts("counts"),
    AverageResolution("average_resolution"),
    Tags("tags"),
    Studios("studios"),
    Performers("performers"),
    Scenes("scenes"),
    ParentFolders("parent_folders"),
    SavedFilter("saved_filter"),
}

data class StashActiveGalleryFilterChip(
    val category: StashGalleryFilterCategory,
    val label: String,
)

data class StashGalleryFilterState(
    val text: StashGalleryTextFilterState = StashGalleryTextFilterState(),
    val dateRange: StashDateRange? = null,
    val createdAtRange: StashDateRange? = null,
    val updatedAtRange: StashDateRange? = null,
    val ratingRange: StashRatingRange? = null,
    val organized: Boolean? = null,
    val isZip: Boolean? = null,
    val hasChapters: Boolean? = null,
    val imageCountRange: StashGalleryNumberRange? = null,
    val fileCountRange: StashGalleryNumberRange? = null,
    val tagCountRange: StashGalleryNumberRange? = null,
    val averageResolution: StashVideoResolution? = null,
    val tags: List<StashSelectedEntity> = emptyList(),
    val studios: List<StashSelectedEntity> = emptyList(),
    val performers: List<StashSelectedEntity> = emptyList(),
    val scenes: List<StashSelectedEntity> = emptyList(),
    val parentFolders: List<StashSelectedEntity> = emptyList(),
    val savedFilter: StashSavedFilterRef? = null,
) {
    val isEmpty: Boolean get() = activeFilterChips().isEmpty()
    val activeFilterCount: Int get() = activeFilterChips().size

    fun activeFilterChips(): List<StashActiveGalleryFilterChip> = buildList {
        text.title.normalizedGalleryFilterTextOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Title, stashString(R.string.gallery_filter_chip_title, value)))
        }
        text.details.normalizedGalleryFilterTextOrNull()?.let {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Details, stashString(R.string.gallery_filter_chip_details)))
        }
        text.code.normalizedGalleryFilterTextOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Code, stashString(R.string.gallery_filter_chip_code, value)))
        }
        text.photographer.normalizedGalleryFilterTextOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Photographer, stashString(R.string.gallery_filter_chip_photographer, value)))
        }
        text.path.normalizedGalleryFilterTextOrNull()?.let {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Path, stashString(R.string.gallery_filter_chip_path)))
        }
        text.url.normalizedGalleryFilterTextOrNull()?.let {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Url, stashString(R.string.gallery_filter_chip_url)))
        }
        text.checksum.normalizedGalleryFilterTextOrNull()?.let {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Checksum, stashString(R.string.gallery_filter_chip_checksum)))
        }
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.DateRange, stashString(R.string.auto_kr_0156, range.dateLabel())))
        }
        createdAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.CreatedAtRange, stashString(R.string.gallery_filter_chip_created_at, range.dateLabel())))
        }
        updatedAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.UpdatedAtRange, stashString(R.string.gallery_filter_chip_updated_at, range.dateLabel())))
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Rating, stashString(R.string.auto_kr_0158, range.displayLabel())))
        }
        if (organized != null || isZip != null || hasChapters != null) {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.BooleanFlags, stashString(R.string.gallery_filter_chip_boolean_flags)))
        }
        if (
            imageCountRange?.takeUnless { it.isEmpty } != null ||
            fileCountRange?.takeUnless { it.isEmpty } != null ||
            tagCountRange?.takeUnless { it.isEmpty } != null
        ) {
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Counts, stashString(R.string.gallery_filter_chip_counts)))
        }
        averageResolution?.let { resolution ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.AverageResolution, stashString(R.string.gallery_filter_chip_average_resolution, resolution.label)))
        }
        tags.firstEntityNameOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Tags, stashString(R.string.gallery_filter_chip_tags, value)))
        }
        studios.firstEntityNameOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Studios, stashString(R.string.gallery_filter_chip_studios, value)))
        }
        performers.firstEntityNameOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Performers, stashString(R.string.gallery_filter_chip_performers, value)))
        }
        scenes.firstEntityNameOrNull()?.let { value ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.Scenes, stashString(R.string.gallery_filter_chip_scenes, value)))
        }
        parentFolders.normalizedGalleryEntities().takeIf { it.isNotEmpty() }?.let { folders ->
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.ParentFolders, stashString(R.string.gallery_filter_chip_parent_folders, folders.size)))
        }
        savedFilter?.let { filter ->
            val name = normalizeStashVideoFilterText(filter.name).ifBlank { filter.id }
            add(StashActiveGalleryFilterChip(StashGalleryFilterCategory.SavedFilter, stashString(R.string.auto_kr_0160, name)))
        }
    }

    fun serializeForStorage(): String = buildList {
        addGalleryFilterStorageField("title", text.title)
        addGalleryFilterStorageField("details", text.details)
        addGalleryFilterStorageField("code", text.code)
        addGalleryFilterStorageField("photographer", text.photographer)
        addGalleryFilterStorageField("path", text.path)
        addGalleryFilterStorageField("url", text.url)
        addGalleryFilterStorageField("checksum", text.checksum)
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add("dateStart=${encodeGalleryFilterField(range.start.orEmpty())}")
            add("dateEnd=${encodeGalleryFilterField(range.end.orEmpty())}")
        }
        createdAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add("createdStart=${encodeGalleryFilterField(range.start.orEmpty())}")
            add("createdEnd=${encodeGalleryFilterField(range.end.orEmpty())}")
        }
        updatedAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add("updatedStart=${encodeGalleryFilterField(range.start.orEmpty())}")
            add("updatedEnd=${encodeGalleryFilterField(range.end.orEmpty())}")
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add("ratingMin=${range.min ?: ""}")
            add("ratingMax=${range.max ?: ""}")
        }
        organized?.let { add("organized=$it") }
        isZip?.let { add("isZip=$it") }
        hasChapters?.let { add("hasChapters=$it") }
        imageCountRange?.takeUnless { it.isEmpty }?.let { addGalleryNumberRangeStorageFields("imageCount") }
        fileCountRange?.takeUnless { it.isEmpty }?.let { addGalleryNumberRangeStorageFields("fileCount") }
        tagCountRange?.takeUnless { it.isEmpty }?.let { addGalleryNumberRangeStorageFields("tagCount") }
        averageResolution?.let { add("averageResolution=${it.id}") }
        addGalleryEntityStorageField("tags", tags)
        addGalleryEntityStorageField("studios", studios)
        addGalleryEntityStorageField("performers", performers)
        addGalleryEntityStorageField("scenes", scenes)
        addGalleryEntityStorageField("parentFolders", parentFolders)
        savedFilter?.let { filter ->
            add("saved=${encodeGalleryFilterField(filter.id)}:${encodeGalleryFilterField(filter.name)}")
        }
    }.joinToString(";")

    private fun MutableList<String>.addGalleryFilterStorageField(name: String, value: String) {
        val normalized = value.normalizedGalleryFilterTextOrNull() ?: return
        add("$name=${encodeGalleryFilterField(normalized)}")
    }

    private fun MutableList<String>.addGalleryNumberRangeStorageFields(prefix: String) {
        val range = when (prefix) {
            "imageCount" -> imageCountRange
            "fileCount" -> fileCountRange
            "tagCount" -> tagCountRange
            else -> null
        } ?: return
        add("${prefix}Min=${range.min ?: ""}")
        add("${prefix}Max=${range.max ?: ""}")
    }

    private fun MutableList<String>.addGalleryEntityStorageField(name: String, entities: List<StashSelectedEntity>) {
        val serializedEntities = entities.normalizedGalleryEntities()
            .joinToString(",") { entity ->
                encodeGalleryFilterField("${entity.id}|${entity.name}")
            }
        if (serializedEntities.isNotBlank()) add("$name=$serializedEntities")
    }
}

private fun List<StashSelectedEntity>.firstEntityNameOrNull(): String? = normalizedGalleryEntities()
    .firstOrNull()
    ?.name

fun List<StashSelectedEntity>.normalizedGalleryEntities(): List<StashSelectedEntity> {
    val seenIds = mutableSetOf<String>()
    return mapNotNull { entity ->
        val id = entity.id.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val name = normalizeStashVideoFilterText(entity.name).takeIf { it.isNotBlank() } ?: id
        StashSelectedEntity(id = id, name = name)
    }.filter { entity -> seenIds.add(entity.id) }
}

private fun String.normalizedGalleryFilterTextOrNull(): String? = normalizeStashVideoFilterText(this).takeIf { it.isNotBlank() }

private fun encodeGalleryFilterField(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

fun StashGalleryFilterState.toGallerySavedFilterPayload(): StashGalleryFilterState = copy(savedFilter = null)

fun StashGalleryFilterState.toRecentGalleryFilterSnapshot(): StashGalleryFilterState = copy(savedFilter = null)

fun StashGalleryFilterState.shouldSaveAsRecentGalleryFilter(): Boolean = toRecentGalleryFilterSnapshot()
    .serializeForStorage()
    .isNotBlank()

fun shouldPromoteRecentGalleryFilterAfterChange(
    previous: StashGalleryFilterState,
    updated: StashGalleryFilterState,
): Boolean {
    val previousKey = previous.toRecentGalleryFilterSnapshot().serializeForStorage()
    val updatedKey = updated.toRecentGalleryFilterSnapshot().serializeForStorage()
    return previousKey != updatedKey && updatedKey.isNotBlank()
}

fun StashGalleryFilterState.galleryFilterSummaryLabel(maxVisibleChips: Int = 3): String {
    val labels = toGallerySavedFilterPayload().activeFilterChips().map { it.label }
    if (labels.isEmpty()) return stashString(R.string.auto_kr_0130)
    val visibleLabels = labels.take(maxVisibleChips.coerceAtLeast(1))
    val hiddenCount = labels.size - visibleLabels.size
    return buildString {
        append(visibleLabels.joinToString(" · "))
        if (hiddenCount > 0) append(" +$hiddenCount")
    }
}

fun StashGalleryFilterState.quickSavedGalleryFilterName(suffix: String? = null): String {
    val baseName = galleryFilterSummaryLabel(maxVisibleChips = Int.MAX_VALUE)
        .takeUnless { it == stashString(R.string.auto_kr_0130) }
        ?: stashString(R.string.auto_kr_0134)
    val normalizedSuffix = suffix?.let(::normalizeStashVideoFilterText).orEmpty()
    return if (normalizedSuffix.isBlank()) baseName else "$baseName · $normalizedSuffix"
}

fun promoteStashRecentGalleryFilter(
    existing: List<StashGalleryFilterState>,
    candidate: StashGalleryFilterState,
    limit: Int,
): List<StashGalleryFilterState> {
    val normalizedCandidate = candidate.toRecentGalleryFilterSnapshot()
    val candidateKey = normalizedCandidate.serializeForStorage()
    if (candidateKey.isBlank()) return existing.take(limit.coerceAtLeast(0))
    val maxSize = limit.coerceAtLeast(1)
    return (listOf(normalizedCandidate) + existing.filterNot { it.toRecentGalleryFilterSnapshot().serializeForStorage() == candidateKey })
        .take(maxSize)
}

fun StashGalleryFilterState.withSavedFilterReference(ref: StashSavedFilterRef): StashGalleryFilterState = copy(savedFilter = ref)

fun clearStashGalleryFilterCategory(
    state: StashGalleryFilterState,
    category: StashGalleryFilterCategory,
): StashGalleryFilterState = when (category) {
    StashGalleryFilterCategory.Title -> state.copy(text = state.text.copy(title = ""))
    StashGalleryFilterCategory.Details -> state.copy(text = state.text.copy(details = ""))
    StashGalleryFilterCategory.Code -> state.copy(text = state.text.copy(code = ""))
    StashGalleryFilterCategory.Photographer -> state.copy(text = state.text.copy(photographer = ""))
    StashGalleryFilterCategory.Path -> state.copy(text = state.text.copy(path = ""))
    StashGalleryFilterCategory.Url -> state.copy(text = state.text.copy(url = ""))
    StashGalleryFilterCategory.Checksum -> state.copy(text = state.text.copy(checksum = ""))
    StashGalleryFilterCategory.DateRange -> state.copy(dateRange = null)
    StashGalleryFilterCategory.CreatedAtRange -> state.copy(createdAtRange = null)
    StashGalleryFilterCategory.UpdatedAtRange -> state.copy(updatedAtRange = null)
    StashGalleryFilterCategory.Rating -> state.copy(ratingRange = null)
    StashGalleryFilterCategory.BooleanFlags -> state.copy(organized = null, isZip = null, hasChapters = null)
    StashGalleryFilterCategory.Counts -> state.copy(imageCountRange = null, fileCountRange = null, tagCountRange = null)
    StashGalleryFilterCategory.AverageResolution -> state.copy(averageResolution = null)
    StashGalleryFilterCategory.Tags -> state.copy(tags = emptyList())
    StashGalleryFilterCategory.Studios -> state.copy(studios = emptyList())
    StashGalleryFilterCategory.Performers -> state.copy(performers = emptyList())
    StashGalleryFilterCategory.Scenes -> state.copy(scenes = emptyList())
    StashGalleryFilterCategory.ParentFolders -> state.copy(parentFolders = emptyList())
    StashGalleryFilterCategory.SavedFilter -> state.copy(savedFilter = null)
}

fun deserializeStashGalleryFilterState(serialized: String): StashGalleryFilterState {
    if (serialized.isBlank()) return StashGalleryFilterState()
    val fields = serialized
        .split(';')
        .mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
        }
        .toMap()

    return StashGalleryFilterState(
        text = StashGalleryTextFilterState(
            title = fields["title"].decodeGalleryFilterFieldOrNull().orEmpty(),
            details = fields["details"].decodeGalleryFilterFieldOrNull().orEmpty(),
            code = fields["code"].decodeGalleryFilterFieldOrNull().orEmpty(),
            photographer = fields["photographer"].decodeGalleryFilterFieldOrNull().orEmpty(),
            path = fields["path"].decodeGalleryFilterFieldOrNull().orEmpty(),
            url = fields["url"].decodeGalleryFilterFieldOrNull().orEmpty(),
            checksum = fields["checksum"].decodeGalleryFilterFieldOrNull().orEmpty(),
        ),
        dateRange = StashDateRange(
            start = fields["dateStart"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["dateEnd"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        createdAtRange = StashDateRange(
            start = fields["createdStart"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["createdEnd"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        updatedAtRange = StashDateRange(
            start = fields["updatedStart"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["updatedEnd"].decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        ratingRange = StashRatingRange(
            min = fields["ratingMin"]?.toIntOrNull(),
            max = fields["ratingMax"]?.toIntOrNull(),
        ).takeUnless { it.isEmpty },
        organized = fields["organized"]?.toBooleanStrictOrNull(),
        isZip = fields["isZip"]?.toBooleanStrictOrNull(),
        hasChapters = fields["hasChapters"]?.toBooleanStrictOrNull(),
        imageCountRange = galleryNumberRangeOrNull(fields, "imageCount"),
        fileCountRange = galleryNumberRangeOrNull(fields, "fileCount"),
        tagCountRange = galleryNumberRangeOrNull(fields, "tagCount"),
        averageResolution = fields["averageResolution"]?.let { id -> StashVideoResolution.entries.firstOrNull { it.id == id } },
        tags = fields["tags"].parseGalleryEntities(),
        studios = fields["studios"].parseGalleryEntities(),
        performers = fields["performers"].parseGalleryEntities(),
        scenes = fields["scenes"].parseGalleryEntities(),
        parentFolders = fields["parentFolders"].parseGalleryEntities(),
        savedFilter = fields["saved"].parseGallerySavedFilterRef(),
    )
}

private fun galleryNumberRangeOrNull(fields: Map<String, String>, prefix: String): StashGalleryNumberRange? = StashGalleryNumberRange(
    min = fields["${prefix}Min"]?.toIntOrNull(),
    max = fields["${prefix}Max"]?.toIntOrNull(),
).takeUnless { it.isEmpty }

private fun String?.parseGalleryEntities(): List<StashSelectedEntity> = this
    ?.takeIf { it.isNotBlank() }
    ?.split(',')
    ?.mapNotNull { value ->
        val decoded = value.decodeGalleryFilterFieldOrNull() ?: return@mapNotNull null
        val index = decoded.indexOf('|')
        if (index <= 0) return@mapNotNull null
        val id = decoded.substring(0, index)
        val name = decoded.substring(index + 1)
        StashSelectedEntity(id = id, name = name)
    }
    .orEmpty()
    .normalizedGalleryEntities()

private fun String?.parseGallerySavedFilterRef(): StashSavedFilterRef? {
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    val index = value.indexOf(':')
    if (index <= 0) return null
    val id = value.substring(0, index).decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() } ?: return null
    val name = value.substring(index + 1).decodeGalleryFilterFieldOrNull()?.takeIf { it.isNotBlank() } ?: return null
    return StashSavedFilterRef(id = id, name = name)
}

private fun String?.decodeGalleryFilterFieldOrNull(): String? = this?.let { value ->
    runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.name()) }.getOrNull()
}

const val DEFAULT_STASH_GALLERY_GRID_PAGE_SIZE = 24

data class StashGalleryToolbarPreferences(
    val sortOption: StashGallerySortOption = defaultStashGallerySortOption(),
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
    val pageSize: Int = DEFAULT_STASH_GALLERY_GRID_PAGE_SIZE,
    val displayMode: StashGalleryDisplayMode = StashGalleryDisplayMode.Grid,
)

data class StashImageToolbarPreferences(
    val sortOption: StashGallerySortOption = defaultStashImageSortOption(),
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
    val pageSize: Int = DEFAULT_STASH_GALLERY_GRID_PAGE_SIZE,
    val displayMode: StashGalleryDisplayMode = StashGalleryDisplayMode.Grid,
)

data class StashGalleryGridPageState(
    val query: String = "",
    val sortOption: StashGallerySortOption = defaultStashGallerySortOption(),
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
    val randomSeed: Int? = null,
    val galleryFilter: StashGalleryFilterState = StashGalleryFilterState(),
    val displayMode: StashGalleryDisplayMode = StashGalleryDisplayMode.Grid,
    val galleries: List<GalleryCardModel> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
) {
    val hasQuery: Boolean get() = query.isNotBlank()

    val toolbarPreferences: StashGalleryToolbarPreferences
        get() = StashGalleryToolbarPreferences(
            sortOption = sortOption,
            sortDirection = sortDirection,
            pageSize = pageSize,
            displayMode = displayMode,
        )

    fun withToolbarPreferences(preferences: StashGalleryToolbarPreferences): StashGalleryGridPageState =
        initial().forSort(preferences.sortOption).copy(
            sortDirection = preferences.sortDirection,
            pageSize = preferences.pageSize.coerceAtLeast(1),
            displayMode = preferences.displayMode,
        )

    fun withQuery(query: String): StashGalleryGridPageState = reset().copy(
        query = normalizeStashDiscoveryQuery(query),
    )

    fun forSort(
        sortOption: StashGallerySortOption,
        seed: Int? = randomSeed,
    ): StashGalleryGridPageState = reset().copy(
        sortOption = sortOption,
        sortDirection = sortOption.defaultDirection,
        randomSeed = if (sortOption.serverValue == "random") {
            normalizeStashRandomSortSeed(seed ?: nextStashRandomSortSeed())
        } else {
            null
        },
    )

    fun withSortDirection(direction: StashSortDirection): StashGalleryGridPageState = reset().copy(
        sortDirection = direction,
    )

    fun withPageSize(pageSize: Int): StashGalleryGridPageState = reset().copy(
        pageSize = pageSize.coerceAtLeast(1),
    )

    fun withGalleryFilter(galleryFilter: StashGalleryFilterState): StashGalleryGridPageState = reset().copy(
        galleryFilter = galleryFilter,
    )

    fun withRandomSeed(seed: Int = nextStashRandomSortSeed()): StashGalleryGridPageState = reset().copy(
        sortOption = stashGallerySortOptions().first { it.serverValue == "random" },
        sortDirection = StashSortDirection.Desc,
        randomSeed = normalizeStashRandomSortSeed(seed),
    )

    fun withDisplayMode(displayMode: StashGalleryDisplayMode): StashGalleryGridPageState = copy(
        displayMode = displayMode,
    )

    val serverSort: String get() = galleryServerSortValue(sortOption, randomSeed)

    fun loading(): StashGalleryGridPageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        galleries: List<GalleryCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGridPageState = copy(
        galleries = galleries,
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextPage(
        galleries: List<GalleryCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGridPageState = copy(
        galleries = this.galleries + galleries,
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashGalleryGridPageState = copy(isLoading = false, error = message)

    private fun reset(): StashGalleryGridPageState = initial().copy(
        query = query,
        sortOption = sortOption,
        sortDirection = sortDirection,
        randomSeed = randomSeed,
        galleryFilter = galleryFilter,
        displayMode = displayMode,
        pageSize = pageSize,
    )

    companion object {
        fun initial(): StashGalleryGridPageState = StashGalleryGridPageState()
    }
}

data class StashImagePage(
    val images: List<GalleryImageModel>,
    val totalCount: Int,
)

data class StashImageFolderPage(
    val folders: List<GalleryImageFolderGroup>,
    val totalCount: Int,
)

enum class StashGalleryBrowseMode {
    Galleries,
    Images,
}

data class StashGalleryTopLevelHeaderPolicy(
    val showTitle: Boolean,
    val showSubtitle: Boolean,
)

fun stashGalleryTopLevelHeaderPolicy(): StashGalleryTopLevelHeaderPolicy = StashGalleryTopLevelHeaderPolicy(
    showTitle = true,
    showSubtitle = false,
)

enum class StashGalleryMediaControl {
    Search,
    SavedFilters,
    FilterSections,
    Sort,
    SortDirection,
    Random,
    PageSize,
    DisplayMode,
    Operations,
    Selection,
}

data class StashGalleryMediaToolbarPolicy(
    val mode: StashGalleryBrowseMode,
    val requestedControls: List<StashGalleryMediaControl>,
    val activeControls: List<StashGalleryMediaControl>,
) {
    val deferredControls: List<StashGalleryMediaControl>
        get() = requestedControls.filterNot { it in activeControls }

    fun isActive(control: StashGalleryMediaControl): Boolean = control in activeControls
}

fun stashGalleryMediaToolbarPolicy(mode: StashGalleryBrowseMode): StashGalleryMediaToolbarPolicy {
    val sharedDiscoveryControls = listOf(
        StashGalleryMediaControl.Search,
        StashGalleryMediaControl.Sort,
        StashGalleryMediaControl.SortDirection,
        StashGalleryMediaControl.Random,
        StashGalleryMediaControl.PageSize,
        StashGalleryMediaControl.DisplayMode,
    )
    return when (mode) {
        StashGalleryBrowseMode.Galleries -> StashGalleryMediaToolbarPolicy(
            mode = mode,
            requestedControls = listOf(
                StashGalleryMediaControl.Search,
                StashGalleryMediaControl.SavedFilters,
                StashGalleryMediaControl.FilterSections,
            ) + sharedDiscoveryControls.drop(1) + listOf(
                StashGalleryMediaControl.Operations,
                StashGalleryMediaControl.Selection,
            ),
            activeControls = listOf(
                StashGalleryMediaControl.Search,
                StashGalleryMediaControl.SavedFilters,
                StashGalleryMediaControl.FilterSections,
            ) + sharedDiscoveryControls.drop(1) + listOf(
                StashGalleryMediaControl.Operations,
                StashGalleryMediaControl.Selection,
            ),
        )
        StashGalleryBrowseMode.Images -> StashGalleryMediaToolbarPolicy(
            mode = mode,
            requestedControls = sharedDiscoveryControls + listOf(
                StashGalleryMediaControl.SavedFilters,
                StashGalleryMediaControl.FilterSections,
            ),
            activeControls = sharedDiscoveryControls + listOf(
                StashGalleryMediaControl.SavedFilters,
                StashGalleryMediaControl.FilterSections,
            ),
        )
    }
}

data class StashGalleryGlobalImageGridPageState(
    val query: String = "",
    val sortOption: StashGallerySortOption = defaultStashImageSortOption(),
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
    val imageFilter: StashImageFilterState = StashImageFilterState(),
    val randomSeed: Int? = null,
    val displayMode: StashGalleryDisplayMode = StashGalleryDisplayMode.Grid,
    val images: List<GalleryImageModel> = emptyList(),
    val folderGroups: List<GalleryImageFolderGroup> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
) {
    val hasQuery: Boolean get() = query.isNotBlank()

    val toolbarPreferences: StashImageToolbarPreferences
        get() = StashImageToolbarPreferences(
            sortOption = sortOption,
            sortDirection = sortDirection,
            pageSize = pageSize,
            displayMode = displayMode,
        )

    fun withToolbarPreferences(preferences: StashImageToolbarPreferences): StashGalleryGlobalImageGridPageState =
        initial().forSort(preferences.sortOption).copy(
            sortDirection = preferences.sortDirection,
            pageSize = preferences.pageSize.coerceAtLeast(1),
            displayMode = if (preferences.displayMode in stashGalleryImageDisplayModes()) {
                preferences.displayMode
            } else {
                StashGalleryDisplayMode.Grid
            },
        )

    fun withQuery(query: String): StashGalleryGlobalImageGridPageState = reset().copy(
        query = normalizeStashDiscoveryQuery(query),
    )

    fun forSort(
        sortOption: StashGallerySortOption,
        seed: Int? = randomSeed,
    ): StashGalleryGlobalImageGridPageState = reset().copy(
        sortOption = sortOption,
        sortDirection = sortOption.defaultDirection,
        randomSeed = if (sortOption.serverValue == "random") {
            normalizeStashRandomSortSeed(seed ?: nextStashRandomSortSeed())
        } else {
            null
        },
    )

    fun withSortDirection(direction: StashSortDirection): StashGalleryGlobalImageGridPageState = reset().copy(
        sortDirection = direction,
    )

    fun withImageFilter(imageFilter: StashImageFilterState): StashGalleryGlobalImageGridPageState = reset().copy(
        imageFilter = imageFilter,
        randomSeed = if (sortOption.serverValue == "random") nextStashRandomSortSeed() else randomSeed,
    )

    fun withPageSize(pageSize: Int): StashGalleryGlobalImageGridPageState = reset().copy(
        pageSize = pageSize.coerceAtLeast(1),
    )

    fun withRandomSeed(seed: Int = nextStashRandomSortSeed()): StashGalleryGlobalImageGridPageState = reset().copy(
        sortOption = stashImageSortOptions().first { it.serverValue == "random" },
        sortDirection = StashSortDirection.Desc,
        randomSeed = normalizeStashRandomSortSeed(seed),
    )

    fun withDisplayMode(displayMode: StashGalleryDisplayMode): StashGalleryGlobalImageGridPageState = copy(
        displayMode = if (displayMode in stashGalleryImageDisplayModes()) displayMode else StashGalleryDisplayMode.Grid,
    )

    val serverSort: String
        get() = if (displayMode == StashGalleryDisplayMode.Folders && sortOption.isRandomSort()) {
            defaultStashImageSortOption().serverValue
        } else {
            imageServerSortValue(sortOption, randomSeed)
        }

    val serverDirection: StashSortDirection
        get() = if (displayMode == StashGalleryDisplayMode.Folders && sortOption.isRandomSort()) {
            defaultStashImageSortOption().defaultDirection
        } else {
            sortDirection
        }

    fun loading(): StashGalleryGlobalImageGridPageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        images: List<GalleryImageModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGlobalImageGridPageState = copy(
        images = images,
        folderGroups = emptyList(),
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextPage(
        images: List<GalleryImageModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGlobalImageGridPageState = copy(
        images = this.images + images,
        folderGroups = emptyList(),
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withFirstFolderPage(
        folders: List<GalleryImageFolderGroup>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGlobalImageGridPageState = copy(
        images = emptyList(),
        folderGroups = folders.asServerBackedImageFolderIndex(
            sortDirection = sortDirection,
            sortOption = sortOption,
            randomSeed = randomSeed,
        ),
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextFolderPage(
        folders: List<GalleryImageFolderGroup>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryGlobalImageGridPageState = copy(
        images = emptyList(),
        folderGroups = (this.folderGroups + folders).asServerBackedImageFolderIndex(
            sortDirection = sortDirection,
            sortOption = sortOption,
            randomSeed = randomSeed,
        ),
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashGalleryGlobalImageGridPageState = copy(isLoading = false, error = message)

    private fun reset(): StashGalleryGlobalImageGridPageState = initial().copy(
        query = query,
        sortOption = sortOption,
        sortDirection = sortDirection,
        imageFilter = imageFilter,
        randomSeed = randomSeed,
        displayMode = displayMode,
        pageSize = pageSize,
    )

    companion object {
        fun initial(): StashGalleryGlobalImageGridPageState = StashGalleryGlobalImageGridPageState()
    }
}

data class StashGalleryImageModeContentState(
    val visibleCount: Int,
    val totalCount: Int?,
    val statusPageState: StashGalleryGlobalImageGridPageState,
    val isSelectedFolderDetail: Boolean,
)

fun resolveStashGalleryImageModeContentState(
    imagePageState: StashGalleryGlobalImageGridPageState,
    selectedImageFolderPath: String?,
    selectedImageFolderPageState: StashGalleryGlobalImageGridPageState,
): StashGalleryImageModeContentState {
    val isFolderMode = imagePageState.displayMode == StashGalleryDisplayMode.Folders
    val isSelectedFolderDetail = isFolderMode && selectedImageFolderPath != null
    val statusPageState = if (isSelectedFolderDetail) selectedImageFolderPageState else imagePageState
    val visibleCount = when {
        isSelectedFolderDetail -> selectedImageFolderPageState.images.size
        isFolderMode -> imagePageState.folderGroups.size
        else -> imagePageState.images.size
    }

    return StashGalleryImageModeContentState(
        visibleCount = visibleCount,
        totalCount = statusPageState.totalCount,
        statusPageState = statusPageState,
        isSelectedFolderDetail = isSelectedFolderDetail,
    )
}

data class StashGalleryBrowseModeSwitchResult(
    val mode: StashGalleryBrowseMode,
    val galleryState: StashGalleryGridPageState,
    val imageState: StashGalleryGlobalImageGridPageState,
    val gallerySelectionState: GallerySelectionState,
)

fun switchStashGalleryBrowseMode(
    currentMode: StashGalleryBrowseMode,
    targetMode: StashGalleryBrowseMode,
    galleryState: StashGalleryGridPageState,
    imageState: StashGalleryGlobalImageGridPageState,
    gallerySelectionState: GallerySelectionState,
): StashGalleryBrowseModeSwitchResult = StashGalleryBrowseModeSwitchResult(
    mode = targetMode,
    galleryState = galleryState,
    imageState = imageState,
    gallerySelectionState = if (currentMode == StashGalleryBrowseMode.Galleries && targetMode == StashGalleryBrowseMode.Images) {
        gallerySelectionState.clear()
    } else {
        gallerySelectionState
    },
)

data class StashGalleryImageGridPageState(
    val galleryId: String,
    val gallery: GalleryCardModel? = null,
    val galleryDetail: StashGalleryDetailModel? = null,
    val images: List<GalleryImageModel> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
) {
    fun withGallery(gallery: GalleryCardModel?): StashGalleryImageGridPageState = copy(gallery = gallery)

    fun withGalleryDetail(detail: StashGalleryDetailModel?): StashGalleryImageGridPageState = copy(
        gallery = detail?.gallery ?: gallery,
        galleryDetail = detail,
    )

    fun loading(): StashGalleryImageGridPageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        images: List<GalleryImageModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryImageGridPageState = copy(
        images = images,
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextPage(
        images: List<GalleryImageModel>,
        totalCount: Int,
        perPage: Int,
    ): StashGalleryImageGridPageState = copy(
        images = this.images + images,
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashGalleryImageGridPageState = copy(isLoading = false, error = message)

    companion object {
        fun initial(galleryId: String): StashGalleryImageGridPageState = StashGalleryImageGridPageState(
            galleryId = galleryId.trim(),
        )
    }
}

fun GalleryCardModel.galleryMetadataLabels(
    imageCountLabel: (Int) -> String,
    ratingLabel: (String) -> String = { rating -> rating },
    organizedLabel: String? = null,
    tagCountLabel: (Int) -> String = { count -> count.toString() },
    performerCountLabel: (Int) -> String = { count -> count.toString() },
    sceneCountLabel: (Int) -> String = { count -> count.toString() },
): List<String> = buildList {
    imageCount?.let { count -> add(imageCountLabel(count)) }
    galleryRatingLabel()?.let { rating -> add(ratingLabel(rating)) }
    studio?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
    date?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
    if (organized == true && organizedLabel != null) add(organizedLabel)
    val tagCount = tagChips.size.takeIf { it > 0 }
    tagCount?.let { add(tagCountLabel(it)) }
    (performerCount?.takeIf { it > 0 } ?: performerChips.size.takeIf { it > 0 })
        ?.let { add(performerCountLabel(it)) }
    (sceneCount?.takeIf { it > 0 } ?: sceneChips.size.takeIf { it > 0 })
        ?.let { add(sceneCountLabel(it)) }
}

fun GalleryCardModel.galleryPreviewSnippet(maxLength: Int = 96): String? {
    val normalized = details?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotBlank() } ?: return null
    if (normalized.length <= maxLength) return normalized
    return normalized.take(maxLength.coerceAtLeast(1)).trimEnd() + "…"
}

fun GalleryCardModel.galleryRelationshipChipLabels(maxPerGroup: Int = 2): List<String> = buildList {
    val limit = maxPerGroup.coerceAtLeast(0)
    addAll(tagChips.take(limit).map { it.label })
    addAll(performerChips.take(limit).map { it.label })
    addAll(sceneChips.take(limit).map { it.label })
}

fun GalleryCardModel.galleryRatingLabel(): String? {
    val normalized = rating100?.coerceIn(0, 100) ?: return null
    return String.format(Locale.US, "%.1f", normalized / 20.0)
}

fun GalleryImageModel.galleryImageMetadataLabels(
    oCounterLabel: (Int) -> String,
    organizedLabel: String,
): List<String> = buildList {
    val dimensions = if (width != null && height != null && width > 0 && height > 0) {
        "${width}×${height}"
    } else {
        null
    }
    dimensions?.let(::add)
    studio?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
    date?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
    imageRatingLabel()?.let { rating -> add("${rating}★") }
    oCounter?.takeIf { it > 0 }?.let { count -> add(oCounterLabel(count)) }
    if (organized == true) add(organizedLabel)
    fileName?.trim()?.toStashFileNameOrNull()?.let(::add)
}

fun GalleryImageModel.galleryImageLinkedGalleryLabels(limit: Int = 2): List<String> = linkedGalleries
    .mapNotNull { it.title.trim().takeIf { title -> title.isNotBlank() } }
    .take(limit.coerceAtLeast(0))

fun GalleryImageModel.galleryImagePerformerLabels(limit: Int = 2): List<String> = performerChips
    .mapNotNull { it.label.trim().takeIf { label -> label.isNotBlank() } }
    .take(limit.coerceAtLeast(0))

fun List<GalleryImageFolderGroup>.asServerBackedImageFolderIndex(
    sortDirection: StashSortDirection = StashSortDirection.Asc,
    sortOption: StashGallerySortOption = defaultStashImageSortOption(),
    randomSeed: Int? = null,
): List<GalleryImageFolderGroup> {
    val distinctFolders = distinctBy { group -> group.id.ifBlank { group.path } }
    val pathComparator = compareBy<GalleryImageFolderGroup> { group ->
        if (group.path.isBlank()) 1 else 0
    }.thenBy { group -> group.path.lowercase(Locale.US) }
    val pathSorted = distinctFolders.sortedWith(pathComparator)
    return if (sortOption.isRandomSort()) {
        val seed = normalizeStashRandomSortSeed(randomSeed ?: 0)
        val randomComparator = compareBy<GalleryImageFolderGroup> { group ->
            group.path.toStableFolderRandomSortKey(seed)
        }.thenBy { group -> group.path.lowercase(Locale.US) }
        when (sortDirection) {
            StashSortDirection.Asc -> pathSorted.sortedWith(randomComparator)
            StashSortDirection.Desc -> pathSorted.sortedWith(randomComparator.reversed())
        }
    } else {
        when (sortDirection) {
            StashSortDirection.Asc -> pathSorted
            StashSortDirection.Desc -> pathSorted.let { sorted ->
                val filed = sorted.filter { it.path.isNotBlank() }.reversed()
                val unfiled = sorted.filter { it.path.isBlank() }
                filed + unfiled
            }
        }
    }
}

fun groupGalleryImagesByParentFolder(
    images: List<GalleryImageModel>,
    unfiledLabel: String,
    sortDirection: StashSortDirection = StashSortDirection.Asc,
    sortOption: StashGallerySortOption = defaultStashImageSortOption(),
    randomSeed: Int? = null,
    countOverridesByFolderId: Map<String, Int> = emptyMap(),
): List<GalleryImageFolderGroup> {
    val groups = linkedMapOf<String, GalleryImageFolderAccumulator>()
    images.forEachIndexed { index, image ->
        val folderId = image.parentFolderId.normalizedFolderValueOrNull()
        val folderPath = image.parentFolderPath.normalizedFolderPathOrNull()
            ?: image.filePath.toStashParentFolderPathOrEmpty()
        val fallbackTitle = folderPath.toStashFolderTitleOrDefault(unfiledLabel)
        val folderTitle = image.parentFolderName.normalizedFolderValueOrNull() ?: fallbackTitle
        val groupKey = folderId ?: folderPath.ifBlank { "__unfiled__" }
        groups.getOrPut(groupKey) {
            GalleryImageFolderAccumulator(
                folderId = folderId,
                path = folderPath,
                title = folderTitle,
            )
        }.items.add(GalleryImageFolderItem(image = image, originalIndex = index))
    }
    val folderComparator = compareBy<GalleryImageFolderAccumulator> { group ->
        if (group.path.isBlank()) 1 else 0
    }.thenBy { group -> group.path.lowercase(Locale.US) }
    val pathSortedFolders = groups.values.sortedWith(folderComparator)
    val sortedFolders = if (sortOption.isRandomSort()) {
        val seed = normalizeStashRandomSortSeed(randomSeed ?: 0)
        val randomComparator = compareBy<GalleryImageFolderAccumulator> { group ->
            group.path.toStableFolderRandomSortKey(seed)
        }.thenBy { group -> group.path.lowercase(Locale.US) }
        when (sortDirection) {
            StashSortDirection.Asc -> pathSortedFolders.sortedWith(randomComparator)
            StashSortDirection.Desc -> pathSortedFolders.sortedWith(randomComparator.reversed())
        }
    } else {
        when (sortDirection) {
            StashSortDirection.Asc -> pathSortedFolders
            StashSortDirection.Desc -> pathSortedFolders.let { sorted ->
                val filed = sorted.filter { it.path.isNotBlank() }.reversed()
                val unfiled = sorted.filter { it.path.isBlank() }
                filed + unfiled
            }
        }
    }
    val itemComparator = compareBy<GalleryImageFolderItem> { item ->
        item.image.filePath.toStashSortablePathOrEmpty().lowercase(Locale.US)
    }.thenBy { item -> item.originalIndex }
    val sortedItemComparator = when (sortDirection) {
        StashSortDirection.Asc -> itemComparator
        StashSortDirection.Desc -> itemComparator.reversed()
    }
    return sortedFolders.map { group ->
        val countOverride = group.folderId?.let(countOverridesByFolderId::get)
        GalleryImageFolderGroup(
            title = group.title,
            path = group.path,
            items = group.items.sortedWith(sortedItemComparator),
            folderId = group.folderId,
            imageCountOverride = countOverride,
            showLoadedItemCount = false,
        )
    }
}

fun List<GalleryImageFolderGroup>.exactCountOverridesByFolderId(): Map<String, Int> = mapNotNull { group ->
    val folderId = group.folderId.normalizedFolderValueOrNull() ?: return@mapNotNull null
    val count = group.imageCountOverride ?: return@mapNotNull null
    folderId to count
}.toMap()

fun List<GalleryImageFolderGroup>.withExactFolderCounts(countsByFolderId: Map<String, Int>): List<GalleryImageFolderGroup> =
    map { group ->
        val count = group.folderId.normalizedFolderValueOrNull()?.let(countsByFolderId::get)
        if (count == null) group else group.copy(imageCountOverride = count)
    }

private data class GalleryImageFolderAccumulator(
    val folderId: String?,
    val path: String,
    val title: String,
    val items: MutableList<GalleryImageFolderItem> = mutableListOf(),
)

private fun String.toStableFolderRandomSortKey(seed: Int): Long {
    val normalized = ifBlank { "__unfiled__" }.lowercase(Locale.US)
    var hash = seed.toLong() xor 0x9E3779B97F4A7C15UL.toLong()
    normalized.forEach { char ->
        hash = (hash xor char.code.toLong()) * 0x100000001B3L
    }
    hash = hash xor (hash ushr 33)
    hash *= 0xff51afd7ed558ccdUL.toLong()
    hash = hash xor (hash ushr 33)
    hash *= 0xc4ceb9fe1a85ec53UL.toLong()
    return hash xor (hash ushr 33)
}

data class GalleryPhotoDetailRow(
    val label: String,
    val value: String,
)

data class GalleryPhotoDetailLabels(
    val title: String,
    val id: String,
    val dimensions: String,
    val fileSize: String,
    val fileName: String,
    val path: String,
    val date: String,
    val studio: String,
    val rating: String,
    val oCounter: String,
    val organized: String,
    val photographer: String,
    val performers: String,
    val tags: String,
    val linkedGalleries: String,
    val details: String,
)

fun GalleryImageModel.galleryPhotoDetailRows(
    labels: GalleryPhotoDetailLabels,
    oCounterLabel: (Int) -> String,
    organizedLabel: String,
    unorganizedLabel: String,
): List<GalleryPhotoDetailRow> = buildList {
    fun addIfPresent(label: String, value: String?) {
        value?.trim()?.takeIf { it.isNotBlank() }?.let { trimmed ->
            add(GalleryPhotoDetailRow(label = label, value = trimmed))
        }
    }

    addIfPresent(labels.title, title)
    addIfPresent(labels.id, id)
    val dimensions = if (width != null && height != null && width > 0 && height > 0) {
        "${width}×${height}"
    } else {
        null
    }
    addIfPresent(labels.dimensions, dimensions)
    formatGalleryFileSize(sizeBytes)?.let { size -> add(GalleryPhotoDetailRow(labels.fileSize, size)) }
    addIfPresent(labels.fileName, fileName?.toStashFileNameOrNull())
    addIfPresent(labels.path, filePath?.toStashDisplayPathOrNull())
    addIfPresent(labels.date, date)
    addIfPresent(labels.studio, studio)
    imageRatingLabel()?.let { rating -> add(GalleryPhotoDetailRow(labels.rating, "${rating}★")) }
    oCounter?.takeIf { it > 0 }?.let { count -> add(GalleryPhotoDetailRow(labels.oCounter, oCounterLabel(count))) }
    organized?.let { isOrganized ->
        add(GalleryPhotoDetailRow(labels.organized, if (isOrganized) organizedLabel else unorganizedLabel))
    }
    addIfPresent(labels.photographer, photographer)
    val performers = galleryImagePerformerLabels(limit = Int.MAX_VALUE).joinToString(", ")
    addIfPresent(labels.performers, performers)
    val tags = tagChips.mapNotNull { it.label.trim().takeIf(String::isNotBlank) }.joinToString(", ")
    addIfPresent(labels.tags, tags)
    val galleries = galleryImageLinkedGalleryLabels(limit = Int.MAX_VALUE).joinToString(", ")
    addIfPresent(labels.linkedGalleries, galleries)
    addIfPresent(labels.details, details)
}

private fun GalleryImageModel.imageRatingLabel(): String? {
    val normalized = rating100?.coerceIn(0, 100) ?: return null
    return String.format(Locale.US, "%.1f", normalized / 20.0)
}

private fun String.toStashFileNameOrNull(): String? {
    val withoutFragment = substringBefore('#')
    val withoutQuery = withoutFragment.substringBefore('?')
    return withoutQuery.substringAfterLast('/').substringAfterLast('\\')
        .trim()
        .takeIf { it.isNotBlank() }
}

private fun String.toStashDisplayPathOrNull(): String? {
    val withoutFragment = substringBefore('#')
    val withoutQuery = withoutFragment.substringBefore('?')
    return withoutQuery.trim().takeIf { it.isNotBlank() }
}

fun GalleryFileInfoModel.galleryFileInfoLabels(
    fileNameLabel: (String) -> String,
    fileSizeLabel: (String) -> String,
): List<String> = buildList {
    fileName.trim().takeIf { it.isNotBlank() }?.let { name -> add(fileNameLabel(name)) }
    formatGalleryFileSize(sizeBytes)?.let { size -> add(fileSizeLabel(size)) }
}

fun GalleryChapterModel.targetImageIndex(imageCount: Int): Int? {
    if (imageCount <= 0) return null
    return imageIndex.coerceIn(0, imageCount - 1)
}

fun formatGalleryFileSize(sizeBytes: Long?): String? {
    val bytes = sizeBytes?.takeIf { it >= 0L } ?: return null
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes} ${units[unitIndex]}"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unitIndex])
    }
}

fun galleryPhotoViewerPagePolicy(
    images: List<GalleryImageModel>,
    requestedIndex: Int,
    chromeVisible: Boolean,
): GalleryPhotoViewerPagePolicy {
    val totalCount = images.size
    if (totalCount == 0) {
        return GalleryPhotoViewerPagePolicy(
            currentIndex = 0,
            totalCount = 0,
            indexLabel = "0 / 0",
            chromeVisible = chromeVisible,
            hasPrevious = false,
            hasNext = false,
            image = null,
            viewerImageUrl = null,
        )
    }
    val currentIndex = requestedIndex.coerceIn(0, totalCount - 1)
    val image = images[currentIndex]
    return GalleryPhotoViewerPagePolicy(
        currentIndex = currentIndex,
        totalCount = totalCount,
        indexLabel = "${currentIndex + 1} / $totalCount",
        chromeVisible = chromeVisible,
        hasPrevious = currentIndex > 0,
        hasNext = currentIndex < totalCount - 1,
        image = image,
        viewerImageUrl = image.bestViewerImageUrl,
    )
}

fun galleryPhotoZoomTransform(
    state: GalleryPhotoZoomState,
    zoomChange: Float,
    panX: Float,
    panY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    policy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): GalleryPhotoZoomState {
    val nextScale = (state.scale * zoomChange).coerceIn(policy.minScale, policy.maxScale)
    if (nextScale <= policy.minScale + policy.zoomedEpsilon) {
        return GalleryPhotoZoomState(scale = policy.minScale)
    }
    val maxOffsetX = galleryPhotoZoomMaxOffset(viewportWidth, nextScale, policy)
    val maxOffsetY = galleryPhotoZoomMaxOffset(viewportHeight, nextScale, policy)
    return GalleryPhotoZoomState(
        scale = nextScale,
        offsetX = (state.offsetX + panX).coerceIn(-maxOffsetX, maxOffsetX),
        offsetY = (state.offsetY + panY).coerceIn(-maxOffsetY, maxOffsetY),
    )
}

fun galleryPhotoZoomOnDoubleTap(
    state: GalleryPhotoZoomState,
    policy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): GalleryPhotoZoomState = if (galleryPhotoViewerPagerSwipeEnabled(state, policy)) {
    GalleryPhotoZoomState(scale = policy.doubleTapScale.coerceIn(policy.minScale, policy.maxScale))
} else {
    GalleryPhotoZoomState(scale = policy.minScale)
}

fun galleryPhotoViewerPagerSwipeEnabled(
    state: GalleryPhotoZoomState,
    policy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): Boolean = state.scale <= policy.minScale + policy.zoomedEpsilon &&
    abs(state.offsetX) <= policy.zoomedEpsilon &&
    abs(state.offsetY) <= policy.zoomedEpsilon

fun galleryPhotoZoomShouldHandleTransform(
    pointerCount: Int,
    state: GalleryPhotoZoomState,
    policy: GalleryPhotoZoomPolicy = GalleryPhotoZoomPolicy(),
): Boolean = pointerCount > 1 || !galleryPhotoViewerPagerSwipeEnabled(state, policy)

private fun galleryPhotoZoomMaxOffset(
    viewportSize: Float,
    scale: Float,
    policy: GalleryPhotoZoomPolicy,
): Float {
    if (viewportSize <= 0f || scale <= policy.minScale + policy.zoomedEpsilon) {
        return 0f
    }
    return viewportSize * (scale - policy.minScale) / 2f
}

data class StashGalleryGridLayoutPolicy(
    val columns: Int,
    val thumbnailHeightDp: Int,
)

fun stashGalleryGridLayoutPolicy(
    displayMode: StashGalleryDisplayMode,
    isFoldLikeLayout: Boolean,
): StashGalleryGridLayoutPolicy = when (displayMode) {
    StashGalleryDisplayMode.Grid,
    StashGalleryDisplayMode.Folders -> StashGalleryGridLayoutPolicy(
        columns = if (isFoldLikeLayout) 3 else 2,
        thumbnailHeightDp = if (isFoldLikeLayout) 180 else 156,
    )
    StashGalleryDisplayMode.List -> StashGalleryGridLayoutPolicy(
        columns = 1,
        thumbnailHeightDp = if (isFoldLikeLayout) 160 else 132,
    )
    StashGalleryDisplayMode.Wall -> StashGalleryGridLayoutPolicy(
        columns = if (isFoldLikeLayout) 4 else 3,
        thumbnailHeightDp = if (isFoldLikeLayout) 156 else 124,
    )
}

fun stashGalleryGridColumnCount(isFoldLikeLayout: Boolean): Int =
    stashGalleryGridLayoutPolicy(StashGalleryDisplayMode.Grid, isFoldLikeLayout).columns

fun stashGalleryGridThumbnailHeightDp(isFoldLikeLayout: Boolean): Int =
    stashGalleryGridLayoutPolicy(StashGalleryDisplayMode.Grid, isFoldLikeLayout).thumbnailHeightDp

fun stashGalleryImageDisplayModes(): List<StashGalleryDisplayMode> = listOf(
    StashGalleryDisplayMode.Grid,
    StashGalleryDisplayMode.Wall,
    StashGalleryDisplayMode.Folders,
)

fun stashGalleryImageGridLayoutPolicy(
    displayMode: StashGalleryDisplayMode,
    isFoldLikeLayout: Boolean,
): StashGalleryGridLayoutPolicy = when (displayMode) {
    StashGalleryDisplayMode.Wall -> StashGalleryGridLayoutPolicy(
        columns = if (isFoldLikeLayout) 5 else 4,
        thumbnailHeightDp = if (isFoldLikeLayout) 148 else 112,
    )
    StashGalleryDisplayMode.Grid,
    StashGalleryDisplayMode.Folders,
    StashGalleryDisplayMode.List -> StashGalleryGridLayoutPolicy(
        columns = if (isFoldLikeLayout) 4 else 3,
        thumbnailHeightDp = if (isFoldLikeLayout) 176 else 132,
    )
}

fun stashGalleryImageGridColumnCount(isFoldLikeLayout: Boolean): Int =
    stashGalleryImageGridLayoutPolicy(StashGalleryDisplayMode.Grid, isFoldLikeLayout).columns

fun stashGalleryImageGridThumbnailHeightDp(isFoldLikeLayout: Boolean): Int =
    stashGalleryImageGridLayoutPolicy(StashGalleryDisplayMode.Grid, isFoldLikeLayout).thumbnailHeightDp

private fun String?.nonBlankOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

private fun String?.toStashParentFolderPathOrEmpty(): String {
    val normalized = toStashSortablePathOrEmpty()
        .takeIf { it.isNotBlank() }
        ?: return ""
    val parent = normalized.substringBeforeLast('/', missingDelimiterValue = "")
        .trimEnd('/')
    return parent.takeIf { it.isNotBlank() } ?: ""
}

private fun String?.normalizedFolderValueOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

private fun String?.normalizedFolderPathOrNull(): String? = normalizedFolderValueOrNull()
    ?.replace('\\', '/')
    ?.trimEnd('/')
    ?.takeIf { it.isNotBlank() }

private fun String?.toStashSortablePathOrEmpty(): String = this?.substringBefore('#')
    ?.substringBefore('?')
    ?.trim()
    ?.replace('\\', '/')
    ?.trimEnd('/')
    ?.takeIf { it.isNotBlank() }
    ?: ""

private fun String.toStashFolderTitleOrDefault(defaultLabel: String): String =
    substringAfterLast('/', missingDelimiterValue = this)
        .trim()
        .takeIf { it.isNotBlank() }
        ?: defaultLabel
