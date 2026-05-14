package gomeng.dev.stashplayer.core.model

import androidx.annotation.StringRes
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
enum class StashScenesToolbarControl(val label: String) {
    SearchInput(stashString(R.string.auto_kr_0003)),
    SavedFilters(stashString(R.string.auto_kr_0065)),
    TagFilters(stashString(R.string.auto_kr_0066)),
    DateDurationPlaybackFilters(stashString(R.string.auto_kr_0067)),
    RatingMediaFilters(stashString(R.string.auto_kr_0068)),
    LocalLibraryFilters(stashString(R.string.auto_kr_0069)),
    Filters(stashString(R.string.auto_kr_0070)),
    SortField(stashString(R.string.auto_kr_0071)),
    SortDirection(stashString(R.string.auto_kr_0072)),
    RandomShuffle(stashString(R.string.auto_kr_0073)),
    PageSize(stashString(R.string.auto_kr_0074)),
    OperationsOverflow(stashString(R.string.auto_kr_0075)),
    ViewMode(stashString(R.string.auto_kr_0076)),
}

enum class StashScenesSelectionToolbarControl(val label: String) {
    ClearSelection(stashString(R.string.auto_kr_0077)),
    SelectionCount(stashString(R.string.auto_kr_0078)),
    SelectAll(stashString(R.string.auto_kr_0079)),
    PlaySelection(stashString(R.string.auto_kr_0080)),
    DeleteSelection(stashString(R.string.auto_kr_0081)),
    More(stashString(R.string.auto_kr_0082)),
}

enum class StashFilterSurfaceRole {
    SearchInput,
    SavedFilterModal,
    UnifiedFilterHub,
    QuickPopover,
    QuickAction,
    ModalEditTarget,
    Operations,
}

data class StashFilterSurfaceDescriptor(
    val label: String,
    val role: StashFilterSurfaceRole,
    val editTarget: StashVideoFilterEditTarget? = null,
)

enum class StashMediaToolbarSurface {
    Scenes,
    Galleries,
}

enum class StashMediaToolbarControl {
    SearchInput,
    SavedFilters,
    TagFilters,
    DateDurationPlaybackFilters,
    RatingMediaFilters,
    LocalLibraryFilters,
    GalleryEntityFilters,
    Filters,
    SortField,
    SortDirection,
    RandomShuffle,
    PageSize,
    OperationsOverflow,
    ViewMode,
}

data class StashMediaToolbarItem(
    val control: StashMediaToolbarControl,
    val label: String,
    val role: StashFilterSurfaceRole,
    val activeCount: Int = 0,
    val sourceKey: String = control.name,
)

data class StashMediaToolbarState(
    val surface: StashMediaToolbarSurface,
    val items: List<StashMediaToolbarItem>,
    val isSelectionMode: Boolean = false,
) {
    fun item(control: StashMediaToolbarControl): StashMediaToolbarItem? = items.firstOrNull { it.control == control }
}

enum class StashGalleryDisplayMode {
    Grid,
    List,
    Wall,
}

enum class StashScenesViewMode(@StringRes val labelRes: Int) {
    Grid(R.string.auto_kr_0083),
    List(R.string.auto_kr_0084),
}

fun stashScenesToolbarNormalControls(
    hasSearchInput: Boolean,
    supportsPageSize: Boolean,
    supportsSortDirection: Boolean = true,
): List<StashScenesToolbarControl> = buildList {
    if (hasSearchInput) add(StashScenesToolbarControl.SearchInput)
    add(StashScenesToolbarControl.SavedFilters)
    add(StashScenesToolbarControl.TagFilters)
    add(StashScenesToolbarControl.DateDurationPlaybackFilters)
    add(StashScenesToolbarControl.RatingMediaFilters)
    add(StashScenesToolbarControl.LocalLibraryFilters)
    add(StashScenesToolbarControl.Filters)
    add(StashScenesToolbarControl.SortField)
    if (supportsSortDirection) add(StashScenesToolbarControl.SortDirection)
    add(StashScenesToolbarControl.RandomShuffle)
    if (supportsPageSize) add(StashScenesToolbarControl.PageSize)
    add(StashScenesToolbarControl.OperationsOverflow)
    add(StashScenesToolbarControl.ViewMode)
}

fun stashScenesToolbarNormalControlLabels(
    hasSearchInput: Boolean,
    supportsPageSize: Boolean,
    supportsSortDirection: Boolean = true,
): List<String> = stashScenesToolbarNormalControls(
    hasSearchInput = hasSearchInput,
    supportsPageSize = supportsPageSize,
    supportsSortDirection = supportsSortDirection,
).map { it.label }

fun stashSceneMediaToolbarState(
    hasSearchInput: Boolean,
    supportsPageSize: Boolean,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    supportsSortDirection: Boolean = true,
    selectionCount: Int = 0,
): StashMediaToolbarState = StashMediaToolbarState(
    surface = StashMediaToolbarSurface.Scenes,
    isSelectionMode = selectionCount > 0,
    items = stashScenesToolbarNormalControls(
        hasSearchInput = hasSearchInput,
        supportsPageSize = supportsPageSize,
        supportsSortDirection = supportsSortDirection,
    ).map { control ->
        control.toMediaToolbarItem(
            videoFilter = videoFilter,
            savedFilterCount = savedFilterCount,
        )
    },
)

fun stashGalleryMediaToolbarFoundationState(
    hasSearchInput: Boolean,
    supportsSortDirection: Boolean,
    supportsPageSize: Boolean,
): StashMediaToolbarState = StashMediaToolbarState(
    surface = StashMediaToolbarSurface.Galleries,
    items = buildList {
        if (hasSearchInput) add(StashScenesToolbarControl.SearchInput.toMediaToolbarItem())
        add(StashScenesToolbarControl.SortField.toMediaToolbarItem())
        if (supportsSortDirection) add(StashScenesToolbarControl.SortDirection.toMediaToolbarItem())
        add(StashScenesToolbarControl.RandomShuffle.toMediaToolbarItem())
        if (supportsPageSize) add(StashScenesToolbarControl.PageSize.toMediaToolbarItem())
        add(StashScenesToolbarControl.OperationsOverflow.toMediaToolbarItem())
        add(StashScenesToolbarControl.ViewMode.toMediaToolbarItem())
    },
)

fun stashGalleryDisplayModes(): List<StashGalleryDisplayMode> = StashGalleryDisplayMode.entries.toList()

fun StashGalleryDisplayMode.next(): StashGalleryDisplayMode {
    val modes = stashGalleryDisplayModes()
    return modes[(modes.indexOf(this) + 1).floorMod(modes.size)]
}

fun StashScenesToolbarControl.toMediaToolbarItem(
    videoFilter: StashVideoFilterState = StashVideoFilterState(),
    savedFilterCount: Int = 0,
): StashMediaToolbarItem {
    val descriptor = filterSurfaceDescriptor()
    return StashMediaToolbarItem(
        control = toMediaToolbarControl(),
        label = descriptor.label,
        role = descriptor.role,
        activeCount = when (this) {
            StashScenesToolbarControl.SavedFilters -> savedFilterCount
            StashScenesToolbarControl.TagFilters -> videoFilter.stashToolbarTagFilterBadgeCount()
            StashScenesToolbarControl.DateDurationPlaybackFilters -> videoFilter.stashToolbarDateDurationPlaybackBadgeCount()
            StashScenesToolbarControl.RatingMediaFilters -> videoFilter.stashToolbarRatingMediaBadgeCount()
            StashScenesToolbarControl.LocalLibraryFilters -> videoFilter.stashToolbarLocalLibraryBadgeCount()
            StashScenesToolbarControl.Filters -> videoFilter.stashToolbarAllFiltersBadgeCount()
            else -> 0
        },
        sourceKey = name,
    )
}

private fun StashScenesToolbarControl.toMediaToolbarControl(): StashMediaToolbarControl = when (this) {
    StashScenesToolbarControl.SearchInput -> StashMediaToolbarControl.SearchInput
    StashScenesToolbarControl.SavedFilters -> StashMediaToolbarControl.SavedFilters
    StashScenesToolbarControl.TagFilters -> StashMediaToolbarControl.TagFilters
    StashScenesToolbarControl.DateDurationPlaybackFilters -> StashMediaToolbarControl.DateDurationPlaybackFilters
    StashScenesToolbarControl.RatingMediaFilters -> StashMediaToolbarControl.RatingMediaFilters
    StashScenesToolbarControl.LocalLibraryFilters -> StashMediaToolbarControl.LocalLibraryFilters
    StashScenesToolbarControl.Filters -> StashMediaToolbarControl.Filters
    StashScenesToolbarControl.SortField -> StashMediaToolbarControl.SortField
    StashScenesToolbarControl.SortDirection -> StashMediaToolbarControl.SortDirection
    StashScenesToolbarControl.RandomShuffle -> StashMediaToolbarControl.RandomShuffle
    StashScenesToolbarControl.PageSize -> StashMediaToolbarControl.PageSize
    StashScenesToolbarControl.OperationsOverflow -> StashMediaToolbarControl.OperationsOverflow
    StashScenesToolbarControl.ViewMode -> StashMediaToolbarControl.ViewMode
}

private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

fun StashScenesToolbarControl.filterSurfaceDescriptor(): StashFilterSurfaceDescriptor = when (this) {
    StashScenesToolbarControl.SearchInput -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.SearchInput,
    )
    StashScenesToolbarControl.SavedFilters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.SavedFilterModal,
        editTarget = StashVideoFilterEditTarget.LocalLibrary,
    )
    StashScenesToolbarControl.TagFilters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = StashVideoFilterEditTarget.Tags,
    )
    StashScenesToolbarControl.DateDurationPlaybackFilters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = StashVideoFilterEditTarget.DateDurationPlayback,
    )
    StashScenesToolbarControl.RatingMediaFilters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = StashVideoFilterEditTarget.RatingMedia,
    )
    StashScenesToolbarControl.LocalLibraryFilters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = StashVideoFilterEditTarget.LocalLibrary,
    )
    StashScenesToolbarControl.Filters -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.UnifiedFilterHub,
    )
    StashScenesToolbarControl.SortField,
    StashScenesToolbarControl.PageSize -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.QuickPopover,
    )
    StashScenesToolbarControl.SortDirection,
    StashScenesToolbarControl.RandomShuffle,
    StashScenesToolbarControl.ViewMode -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.QuickAction,
    )
    StashScenesToolbarControl.OperationsOverflow -> StashFilterSurfaceDescriptor(
        label = label,
        role = StashFilterSurfaceRole.Operations,
    )
}

fun StashVideoFilterEditTarget.filterSurfaceDescriptor(): StashFilterSurfaceDescriptor = when (this) {
    StashVideoFilterEditTarget.Tags -> StashFilterSurfaceDescriptor(
        label = stashString(R.string.auto_kr_0066),
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = this,
    )
    StashVideoFilterEditTarget.DateDurationPlayback -> StashFilterSurfaceDescriptor(
        label = stashString(R.string.auto_kr_0085),
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = this,
    )
    StashVideoFilterEditTarget.RatingMedia -> StashFilterSurfaceDescriptor(
        label = stashString(R.string.auto_kr_0068),
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = this,
    )
    StashVideoFilterEditTarget.LocalLibrary -> StashFilterSurfaceDescriptor(
        label = stashString(R.string.auto_kr_0086),
        role = StashFilterSurfaceRole.ModalEditTarget,
        editTarget = this,
    )
    StashVideoFilterEditTarget.RandomShuffle -> StashFilterSurfaceDescriptor(
        label = stashString(R.string.auto_kr_0087),
        role = StashFilterSurfaceRole.QuickAction,
        editTarget = this,
    )
}

fun StashVideoFilterCategory.filterSurfaceDescriptor(): StashFilterSurfaceDescriptor = editTarget().filterSurfaceDescriptor()

fun stashScenesToolbarSelectionControls(): List<StashScenesSelectionToolbarControl> = listOf(
    StashScenesSelectionToolbarControl.ClearSelection,
    StashScenesSelectionToolbarControl.SelectionCount,
    StashScenesSelectionToolbarControl.SelectAll,
    StashScenesSelectionToolbarControl.PlaySelection,
    StashScenesSelectionToolbarControl.DeleteSelection,
    StashScenesSelectionToolbarControl.More,
)

fun stashScenesToolbarSelectionControlLabels(): List<String> = stashScenesToolbarSelectionControls().map { it.label }

fun stashScenesToolbarNormalOperationLabels(isRandomShuffleEnabled: Boolean): List<String> = listOf(
    stashString(R.string.auto_kr_0088),
    stashString(R.string.auto_kr_0079),
    stashString(R.string.auto_kr_0089),
    stashString(R.string.auto_kr_0090),
    stashString(R.string.auto_kr_0091),
    stashScenesToolbarRandomActionLabel(isRandomShuffleEnabled),
)

fun stashScenesToolbarSelectionMoreLabels(): List<String> = listOf(stashString(R.string.auto_kr_0077), stashString(R.string.auto_kr_0079), stashString(R.string.auto_kr_0089))

fun stashDiscoveryPrimaryQuickControlLabels(): List<String> = listOf(stashString(R.string.auto_kr_0071), stashString(R.string.auto_kr_0092), stashString(R.string.auto_kr_0073))

fun stashDiscoveryRandomToggleLabel(isRandomShuffleEnabled: Boolean): String = if (isRandomShuffleEnabled) {
    stashString(R.string.auto_kr_0093)
} else {
    stashString(R.string.auto_kr_0094)
}

fun stashScenesToolbarRandomActionLabel(isRandomShuffleEnabled: Boolean): String = if (isRandomShuffleEnabled) {
    stashString(R.string.auto_kr_0095)
} else {
    stashString(R.string.auto_kr_0073)
}

fun stashScenesToolbarSavedFiltersLabel(savedFilterCount: Int): String = if (savedFilterCount > 0) {
    stashString(R.string.auto_kr_0096, savedFilterCount)
} else {
    stashString(R.string.auto_kr_0065)
}

fun stashScenesToolbarSavedFiltersContentDescription(savedFilterCount: Int): String = if (savedFilterCount > 0) {
    stashString(R.string.auto_kr_0097, savedFilterCount)
} else {
    stashString(R.string.auto_kr_0098)
}

fun stashScenesToolbarFilterLabel(activeFilterCount: Int): String = if (activeFilterCount > 0) {
    stashString(R.string.auto_kr_0099, activeFilterCount)
} else {
    stashString(R.string.auto_kr_0092)
}

fun stashScenesToolbarFilterContentDescription(activeFilterCount: Int): String = if (activeFilterCount > 0) {
    stashString(R.string.auto_kr_0100, activeFilterCount)
} else {
    stashString(R.string.auto_kr_0101)
}

fun stashScenesToolbarSectionFilterContentDescription(label: String, activeCount: Int): String = if (activeCount > 0) {
    stashString(R.string.auto_kr_0102, label, activeCount)
} else {
    stashString(R.string.auto_kr_0103, label)
}

fun stashScenesToolbarSectionFilterLabel(label: String, activeCount: Int): String = if (activeCount > 0) {
    "$label ${stashToolbarBadgeLabel(activeCount)}"
} else {
    label
}

fun stashToolbarBadgeLabel(count: Int): String = if (count > 99) "99+" else count.coerceAtLeast(0).toString()

fun StashVideoFilterState.stashToolbarTagFilterBadgeCount(): Int = tags.size

fun StashVideoFilterState.stashToolbarDateDurationPlaybackBadgeCount(): Int = listOf(
    dateRange?.takeUnless { it.isEmpty },
    durationRange?.takeUnless { it.isEmpty },
    playbackState,
).count { it != null }

fun StashVideoFilterState.stashToolbarRatingMediaBadgeCount(): Int = listOf(
    ratingRange?.takeUnless { it.isEmpty },
    mediaFormat.takeUnless { it.isEmpty },
).count { it != null }

fun StashVideoFilterState.stashToolbarLocalLibraryBadgeCount(): Int = listOf(
    localFavoriteOnly.takeIf { it },
    savedFilter,
).count { it != null }

fun StashVideoFilterState.stashToolbarAllFiltersBadgeCount(): Int = activeFilterCount

fun StashScenesToolbarControl.usesDiscoveryActionPill(): Boolean = this != StashScenesToolbarControl.SearchInput

fun StashScenesSelectionToolbarControl.usesDiscoveryActionPill(): Boolean = true

fun stashScenesToolbarDropdownLabel(label: String, value: String): String = "${label}: ${value}"

fun stashScenesToolbarDropdownContentDescription(label: String, value: String): String = stashString(R.string.auto_kr_0104, label, value)

fun stashScenesToolbarImmediateActionContentDescription(label: String, value: String): String = stashString(R.string.auto_kr_0105, label, value)

fun stashScenesToolbarRandomActionContentDescription(isRandomShuffleEnabled: Boolean): String = if (isRandomShuffleEnabled) {
    stashString(R.string.auto_kr_0106)
} else {
    stashString(R.string.auto_kr_0107)
}

fun StashScenesViewMode.stashScenesToolbarViewModeLabel(): String = stashString(labelRes)

fun StashScenesViewMode.stashScenesToolbarViewModeContentDescription(): String =
    stashScenesToolbarImmediateActionContentDescription(stashString(R.string.auto_kr_0108), stashScenesToolbarViewModeLabel())

fun StashVideoFilterState.withToolbarRandomActionSeed(seed: Int): StashVideoFilterState = withStashRandomShuffleSeed(seed)

fun stashDiscoveryResultCountLabel(totalCount: Int?, visibleCount: Int): String = if (totalCount != null) {
    stashString(R.string.auto_kr_0109, totalCount, visibleCount)
} else {
    stashString(R.string.auto_kr_0110, visibleCount)
}

fun stashScenesToolbarSurfaceDescription(isSelectionMode: Boolean): String = if (isSelectionMode) {
    stashString(R.string.auto_kr_0111)
} else {
    stashString(R.string.auto_kr_0112)
}

fun stashScenesToolbarSelectionCountLabel(selectionCount: Int): String = stashString(R.string.auto_kr_0113, selectionCount)

fun stashScenesToolbarSelectionCountContentDescription(selectionCount: Int): String = stashString(R.string.auto_kr_0114, selectionCount)

fun StashVideoFilterState.stashDiscoverySummaryChipLabels(): List<String> = activeFilterChips().map { it.label }
