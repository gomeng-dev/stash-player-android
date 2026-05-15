package gomeng.dev.stashplayer.core.ui.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashGalleryDisplayMode
import gomeng.dev.stashplayer.core.model.StashGallerySortOption
import gomeng.dev.stashplayer.core.model.StashScenesViewMode
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.stashScenesToolbarDropdownContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarFilterContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarImmediateActionContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarRandomActionContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSavedFiltersContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSelectionCountContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSelectionCountLabel
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSectionFilterContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSurfaceDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarRandomActionLabel
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSectionFilterLabel
import gomeng.dev.stashplayer.core.model.stashScenesToolbarSavedFiltersLabel
import gomeng.dev.stashplayer.core.model.stashToolbarAllFiltersBadgeCount
import gomeng.dev.stashplayer.core.model.stashToolbarDateDurationPlaybackBadgeCount
import gomeng.dev.stashplayer.core.model.isRandomSort
import gomeng.dev.stashplayer.core.model.label
import gomeng.dev.stashplayer.core.model.stashGalleryDisplayModes
import gomeng.dev.stashplayer.core.model.stashScenesToolbarDropdownLabel
import gomeng.dev.stashplayer.core.model.stashScenesToolbarFilterLabel
import gomeng.dev.stashplayer.core.model.stashToolbarLocalLibraryBadgeCount
import gomeng.dev.stashplayer.core.model.stashToolbarRatingMediaBadgeCount
import gomeng.dev.stashplayer.core.model.stashToolbarTagFilterBadgeCount
import gomeng.dev.stashplayer.core.model.stashScenesToolbarViewModeContentDescription
import gomeng.dev.stashplayer.core.model.stashScenesToolbarViewModeLabel
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPill
import gomeng.dev.stashplayer.core.ui.designsystem.StashGlassSurface
import gomeng.dev.stashplayer.core.ui.designsystem.StashRadii
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun <T> StashScenesToolbar(
    horizontalPadding: Dp,
    isConfigured: Boolean,
    sortValue: String,
    sortOptions: List<T>,
    sortOptionLabel: (T) -> String,
    onSelectSort: (T) -> Unit,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onOpenSavedFilters: () -> Unit,
    onOpenFilter: () -> Unit,
    onToggleRandomShuffle: () -> Unit,
    showFilterShortcuts: Boolean = true,
    onOpenTagFilter: (() -> Unit)? = null,
    onOpenDateDurationPlaybackFilter: (() -> Unit)? = null,
    onOpenRatingMediaFormatFilter: (() -> Unit)? = null,
    onOpenLocalLibraryFilter: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    searchValue: String? = null,
    onSearchChange: ((String) -> Unit)? = null,
    onClearSearch: (() -> Unit)? = null,
    sortDirectionLabel: String? = null,
    onToggleSortDirection: (() -> Unit)? = null,
    pageSizeValue: String? = null,
    pageSizeOptions: List<Int> = emptyList(),
    onSelectPageSize: ((Int) -> Unit)? = null,
    viewMode: StashScenesViewMode = StashScenesViewMode.Grid,
    onToggleViewMode: (() -> Unit)? = null,
    selectionCount: Int = 0,
    visibleCount: Int = 0,
    onClearSelection: (() -> Unit)? = null,
    onSelectAll: (() -> Unit)? = null,
    onInvertSelection: (() -> Unit)? = null,
    onPlaySelection: (() -> Unit)? = null,
    onDeleteSelection: (() -> Unit)? = null,
) {
    val isSelectionMode = selectionCount > 0
    StashGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .semantics { contentDescription = stashScenesToolbarSurfaceDescription(isSelectionMode) },
        cornerRadius = StashRadii.Card,
        contentPadding = PaddingValues(StashSpacing.ChipGap),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        ) {
            if (!isSelectionMode && searchValue != null && onSearchChange != null) {
                StashDiscoverySearchInput(
                    value = searchValue,
                    enabled = isConfigured,
                    onValueChange = onSearchChange,
                    onClear = onClearSearch,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
            ) {
                if (isSelectionMode) {
                    SelectionToolbarContents(
                        selectionCount = selectionCount,
                        onClearSelection = onClearSelection,
                        onSelectAll = onSelectAll,
                        onInvertSelection = onInvertSelection,
                        onPlaySelection = onPlaySelection,
                        onDeleteSelection = onDeleteSelection,
                    )
                } else {
                    if (showFilterShortcuts) {
                        VideoFilterShortcutButtons(
                            isConfigured = isConfigured,
                            videoFilter = videoFilter,
                            savedFilterCount = savedFilterCount,
                            onOpenSavedFilters = onOpenSavedFilters,
                            onOpenTagFilter = onOpenTagFilter,
                            onOpenDateDurationPlaybackFilter = onOpenDateDurationPlaybackFilter,
                            onOpenRatingMediaFormatFilter = onOpenRatingMediaFormatFilter,
                            onOpenLocalLibraryFilter = onOpenLocalLibraryFilter,
                            onOpenUnifiedFilter = onOpenFilter,
                        )
                    }
                    ToolbarDropdown(
                        icon = Icons.AutoMirrored.Outlined.Sort,
                        label = stashString(R.string.auto_kr_0071),
                        value = sortValue,
                        enabled = isConfigured,
                        options = sortOptions,
                        optionLabel = sortOptionLabel,
                        onSelect = onSelectSort,
                    )
                    if (sortDirectionLabel != null && onToggleSortDirection != null) {
                        ToolbarPillButton(
                            icon = Icons.Outlined.SwapVert,
                            label = stashScenesToolbarDropdownLabel(stashString(R.string.auto_kr_0072), sortDirectionLabel),
                            contentDescription = stashScenesToolbarImmediateActionContentDescription(stashString(R.string.auto_kr_0401), sortDirectionLabel),
                            enabled = isConfigured,
                            onClick = onToggleSortDirection,
                        )
                    }
                    ToolbarPillButton(
                        icon = Icons.Outlined.Shuffle,
                        label = stashScenesToolbarRandomActionLabel(videoFilter.randomShuffle),
                        contentDescription = stashScenesToolbarRandomActionContentDescription(videoFilter.randomShuffle),
                        enabled = isConfigured,
                        selected = videoFilter.randomShuffle,
                        onClick = onToggleRandomShuffle,
                    )
                    if (pageSizeValue != null && onSelectPageSize != null && pageSizeOptions.isNotEmpty()) {
                        ToolbarDropdown(
                            icon = Icons.AutoMirrored.Outlined.ViewList,
                            label = stashString(R.string.auto_kr_0074),
                            value = pageSizeValue,
                            enabled = isConfigured,
                            options = pageSizeOptions,
                            optionLabel = { stashString(R.string.auto_kr_0398, it) },
                            onSelect = onSelectPageSize,
                        )
                    }
                    OperationsOverflowMenu(
                        enabled = isConfigured,
                        visibleCount = visibleCount,
                        videoFilter = videoFilter,
                        onPlayFirstOrSelected = onPlaySelection,
                        onSelectAll = onSelectAll,
                        onInvertSelection = onInvertSelection,
                        onOpenSavedFilters = onOpenSavedFilters,
                        onOpenFilter = onOpenFilter,
                        onToggleRandomShuffle = onToggleRandomShuffle,
                    )
                    ToolbarPillButton(
                        icon = if (viewMode == StashScenesViewMode.Grid) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                        label = viewMode.stashScenesToolbarViewModeLabel(),
                        contentDescription = viewMode.stashScenesToolbarViewModeContentDescription(),
                        enabled = isConfigured && onToggleViewMode != null,
                        onClick = { onToggleViewMode?.invoke() },
                    )
                }
            }
        }
    }
}

@Composable
fun StashVideoFilterGroupRow(
    horizontalPadding: Dp,
    isConfigured: Boolean,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onOpenSavedFilters: () -> Unit,
    onOpenFilter: () -> Unit,
    onOpenTagFilter: (() -> Unit)? = null,
    onOpenDateDurationPlaybackFilter: (() -> Unit)? = null,
    onOpenRatingMediaFormatFilter: (() -> Unit)? = null,
    onOpenLocalLibraryFilter: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    StashGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        cornerRadius = StashRadii.Card,
        contentPadding = PaddingValues(StashSpacing.ChipGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        ) {
            VideoFilterShortcutButtons(
                isConfigured = isConfigured,
                videoFilter = videoFilter,
                savedFilterCount = savedFilterCount,
                onOpenSavedFilters = onOpenSavedFilters,
                onOpenTagFilter = onOpenTagFilter,
                onOpenDateDurationPlaybackFilter = onOpenDateDurationPlaybackFilter,
                onOpenRatingMediaFormatFilter = onOpenRatingMediaFormatFilter,
                onOpenLocalLibraryFilter = onOpenLocalLibraryFilter,
                onOpenUnifiedFilter = onOpenFilter,
            )
        }
    }
}

@Composable
private fun VideoFilterShortcutButtons(
    isConfigured: Boolean,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onOpenSavedFilters: () -> Unit,
    onOpenTagFilter: (() -> Unit)?,
    onOpenDateDurationPlaybackFilter: (() -> Unit)?,
    onOpenRatingMediaFormatFilter: (() -> Unit)?,
    onOpenLocalLibraryFilter: (() -> Unit)?,
    onOpenUnifiedFilter: () -> Unit,
) {
    ToolbarPillButton(
        icon = Icons.Outlined.Bookmarks,
        label = stashScenesToolbarSavedFiltersLabel(savedFilterCount),
        contentDescription = stashScenesToolbarSavedFiltersContentDescription(savedFilterCount),
        enabled = isConfigured,
        selected = savedFilterCount > 0,
        onClick = onOpenSavedFilters,
    )
    if (onOpenTagFilter != null) {
        ToolbarPillButton(
            icon = Icons.Outlined.LocalOffer,
            label = stashScenesToolbarSectionFilterLabel(stashString(R.string.auto_kr_0066), videoFilter.stashToolbarTagFilterBadgeCount()),
            contentDescription = stashScenesToolbarSectionFilterContentDescription(stashString(R.string.auto_kr_0066), videoFilter.stashToolbarTagFilterBadgeCount()),
            enabled = isConfigured,
            selected = videoFilter.stashToolbarTagFilterBadgeCount() > 0,
            onClick = onOpenTagFilter,
        )
    }
    if (onOpenDateDurationPlaybackFilter != null) {
        ToolbarPillButton(
            icon = Icons.Outlined.Event,
            label = stashScenesToolbarSectionFilterLabel(stashString(R.string.auto_kr_0067), videoFilter.stashToolbarDateDurationPlaybackBadgeCount()),
            contentDescription = stashScenesToolbarSectionFilterContentDescription(stashString(R.string.auto_kr_0399), videoFilter.stashToolbarDateDurationPlaybackBadgeCount()),
            enabled = isConfigured,
            selected = videoFilter.stashToolbarDateDurationPlaybackBadgeCount() > 0,
            onClick = onOpenDateDurationPlaybackFilter,
        )
    }
    if (onOpenRatingMediaFormatFilter != null) {
        ToolbarPillButton(
            icon = Icons.Outlined.Star,
            label = stashScenesToolbarSectionFilterLabel(stashString(R.string.auto_kr_0068), videoFilter.stashToolbarRatingMediaBadgeCount()),
            contentDescription = stashScenesToolbarSectionFilterContentDescription(stashString(R.string.auto_kr_0068), videoFilter.stashToolbarRatingMediaBadgeCount()),
            enabled = isConfigured,
            selected = videoFilter.stashToolbarRatingMediaBadgeCount() > 0,
            onClick = onOpenRatingMediaFormatFilter,
        )
    }
    if (onOpenLocalLibraryFilter != null) {
        ToolbarPillButton(
            icon = Icons.Outlined.Favorite,
            label = stashScenesToolbarSectionFilterLabel(stashString(R.string.auto_kr_0069), videoFilter.stashToolbarLocalLibraryBadgeCount()),
            contentDescription = stashScenesToolbarSectionFilterContentDescription(stashString(R.string.auto_kr_0400), videoFilter.stashToolbarLocalLibraryBadgeCount()),
            enabled = isConfigured,
            selected = videoFilter.stashToolbarLocalLibraryBadgeCount() > 0,
            onClick = onOpenLocalLibraryFilter,
        )
    }
    ToolbarPillButton(
        icon = Icons.Outlined.Tune,
        label = stashScenesToolbarFilterLabel(videoFilter.activeFilterCount),
        contentDescription = stashScenesToolbarFilterContentDescription(videoFilter.activeFilterCount),
        enabled = isConfigured,
        selected = videoFilter.stashToolbarAllFiltersBadgeCount() > 0,
        onClick = onOpenUnifiedFilter,
    )
}

@Composable
fun StashGalleryToolbar(
    horizontalPadding: Dp,
    isConfigured: Boolean,
    sortOption: StashGallerySortOption,
    sortOptions: List<StashGallerySortOption>,
    sortDirection: StashSortDirection,
    pageSize: Int,
    pageSizeOptions: List<Int>,
    displayMode: StashGalleryDisplayMode,
    displayModeOptions: List<StashGalleryDisplayMode> = stashGalleryDisplayModes(),
    visibleCount: Int,
    onSelectSort: (StashGallerySortOption) -> Unit,
    onToggleSortDirection: () -> Unit,
    onRandomAction: () -> Unit,
    onSelectPageSize: (Int) -> Unit,
    onSelectDisplayMode: (StashGalleryDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
    selectionCount: Int = 0,
    onClearSelection: (() -> Unit)? = null,
    onSelectAll: (() -> Unit)? = null,
    onInvertSelection: (() -> Unit)? = null,
    onOpenSelection: (() -> Unit)? = null,
    onOpenFirst: (() -> Unit)? = null,
    onOpenRandom: (() -> Unit)? = null,
    onOpenRandomSelection: (() -> Unit)? = null,
    showGalleryOperations: Boolean = true,
) {
    val isSelectionMode = selectionCount > 0
    StashGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .semantics { contentDescription = stashString(R.string.gallery_toolbar_content_description) },
        cornerRadius = StashRadii.Card,
        contentPadding = PaddingValues(StashSpacing.ChipGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        ) {
            if (isSelectionMode) {
                GallerySelectionToolbarContents(
                    selectionCount = selectionCount,
                    onClearSelection = onClearSelection,
                    onSelectAll = onSelectAll,
                    onInvertSelection = onInvertSelection,
                    onOpenSelection = onOpenSelection,
                    onOpenRandomSelection = onOpenRandomSelection,
                )
            } else {
                ToolbarDropdown(
                    icon = Icons.AutoMirrored.Outlined.Sort,
                    label = stashString(R.string.auto_kr_0071),
                    value = sortOption.label,
                    enabled = isConfigured,
                    options = sortOptions,
                    optionLabel = { it.label },
                    onSelect = onSelectSort,
                )
                ToolbarPillButton(
                    icon = Icons.Outlined.SwapVert,
                    label = stashScenesToolbarDropdownLabel(stashString(R.string.auto_kr_0072), sortDirection.galleryDirectionLabel()),
                    contentDescription = stashScenesToolbarImmediateActionContentDescription(stashString(R.string.auto_kr_0401), sortDirection.galleryDirectionLabel()),
                    enabled = isConfigured,
                    onClick = onToggleSortDirection,
                )
                ToolbarPillButton(
                    icon = Icons.Outlined.Shuffle,
                    label = stashScenesToolbarRandomActionLabel(sortOption.isRandomSort()),
                    contentDescription = stashScenesToolbarRandomActionContentDescription(sortOption.isRandomSort()),
                    enabled = isConfigured,
                    selected = sortOption.isRandomSort(),
                    onClick = onRandomAction,
                )
                ToolbarDropdown(
                    icon = Icons.AutoMirrored.Outlined.ViewList,
                    label = stashString(R.string.auto_kr_0074),
                    value = stashString(R.string.auto_kr_0398, pageSize),
                    enabled = isConfigured,
                    options = pageSizeOptions,
                    optionLabel = { stashString(R.string.auto_kr_0398, it) },
                    onSelect = onSelectPageSize,
                )
                ToolbarDropdown(
                    icon = if (displayMode == StashGalleryDisplayMode.Grid) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
                    label = stashString(R.string.auto_kr_0108),
                    value = displayMode.label(),
                    enabled = isConfigured,
                    options = displayModeOptions,
                    optionLabel = { it.label() },
                    onSelect = onSelectDisplayMode,
                )
                if (showGalleryOperations) {
                    GalleryOperationsOverflowMenu(
                        enabled = isConfigured,
                        visibleCount = visibleCount,
                        onOpenFirst = onOpenFirst,
                        onOpenRandom = onOpenRandom,
                        onSelectAll = onSelectAll,
                        onInvertSelection = onInvertSelection,
                    )
                }
            }
        }
    }
}

private fun StashSortDirection.galleryDirectionLabel(): String = when (this) {
    StashSortDirection.Desc -> stashString(R.string.auto_kr_0422)
    StashSortDirection.Asc -> stashString(R.string.auto_kr_0423)
}

@Composable
fun StashDiscoverySearchInput(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onClear: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(min = 220.dp),
        enabled = enabled,
        singleLine = true,
        label = { Text(stashString(R.string.auto_kr_0003)) },
        placeholder = { Text(stashString(R.string.auto_kr_0402)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty() && onClear != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Outlined.Clear, contentDescription = stashString(R.string.auto_kr_0403))
                }
            }
        },
    )
}

@Composable
private fun ToolbarPillButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    selected: Boolean = false,
    destructive: Boolean = false,
) {
    StashActionPill(
        label = label,
        onClick = onClick,
        selected = selected,
        enabled = enabled,
        destructive = destructive,
        contentDescription = contentDescription,
        icon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

@Composable
private fun CompactToolbarButton(
    icon: ImageVector,
    label: String,
    contentDescription: String? = null,
    enabled: Boolean,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    ToolbarPillButton(
        icon = icon,
        label = label,
        contentDescription = contentDescription ?: label,
        enabled = enabled,
        destructive = destructive,
        onClick = onClick,
    )
}

@Composable
private fun <T> ToolbarDropdown(
    icon: ImageVector,
    label: String,
    value: String,
    enabled: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarPillButton(
            icon = icon,
            label = stashScenesToolbarDropdownLabel(label, value),
            contentDescription = stashScenesToolbarDropdownContentDescription(label, value),
            enabled = enabled,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 420.dp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun GalleryOperationsOverflowMenu(
    enabled: Boolean,
    visibleCount: Int,
    onOpenFirst: (() -> Unit)?,
    onOpenRandom: (() -> Unit)?,
    onSelectAll: (() -> Unit)?,
    onInvertSelection: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarPillButton(
            icon = Icons.Outlined.MoreVert,
            label = stashString(R.string.auto_kr_0075),
            contentDescription = stashString(R.string.gallery_toolbar_operations_content_description, visibleCount),
            enabled = enabled,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stashString(R.string.gallery_operations_open_first_action)) },
                onClick = {
                    expanded = false
                    onOpenFirst?.invoke()
                },
                enabled = onOpenFirst != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.gallery_operations_open_random_action)) },
                onClick = {
                    expanded = false
                    onOpenRandom?.invoke()
                },
                enabled = onOpenRandom != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0079)) },
                onClick = {
                    expanded = false
                    onSelectAll?.invoke()
                },
                enabled = onSelectAll != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0089)) },
                onClick = {
                    expanded = false
                    onInvertSelection?.invoke()
                },
                enabled = onInvertSelection != null && visibleCount > 0,
            )
        }
    }
}

@Composable
private fun GallerySelectionToolbarContents(
    selectionCount: Int,
    onClearSelection: (() -> Unit)?,
    onSelectAll: (() -> Unit)?,
    onInvertSelection: (() -> Unit)?,
    onOpenSelection: (() -> Unit)?,
    onOpenRandomSelection: (() -> Unit)?,
) {
    CompactToolbarButton(
        icon = Icons.Outlined.Clear,
        label = stashString(R.string.auto_kr_0077),
        enabled = onClearSelection != null,
        onClick = { onClearSelection?.invoke() },
    )
    Text(
        text = stashScenesToolbarSelectionCountLabel(selectionCount),
        modifier = Modifier.semantics {
            contentDescription = stashScenesToolbarSelectionCountContentDescription(selectionCount)
        },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    CompactToolbarButton(
        icon = Icons.Outlined.SelectAll,
        label = stashString(R.string.auto_kr_0079),
        enabled = onSelectAll != null,
        onClick = { onSelectAll?.invoke() },
    )
    CompactToolbarButton(
        icon = Icons.Outlined.PlayArrow,
        label = stashString(R.string.gallery_selection_open_action),
        enabled = onOpenSelection != null,
        onClick = { onOpenSelection?.invoke() },
    )
    CompactToolbarButton(
        icon = Icons.Outlined.Shuffle,
        label = stashString(R.string.gallery_selection_random_action),
        enabled = onOpenRandomSelection != null,
        onClick = { onOpenRandomSelection?.invoke() },
    )
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarPillButton(
            icon = Icons.Outlined.MoreVert,
            label = stashString(R.string.auto_kr_0082),
            contentDescription = stashString(R.string.auto_kr_0405),
            enabled = true,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0077)) },
                onClick = {
                    expanded = false
                    onClearSelection?.invoke()
                },
                enabled = onClearSelection != null,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0079)) },
                onClick = {
                    expanded = false
                    onSelectAll?.invoke()
                },
                enabled = onSelectAll != null,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0089)) },
                onClick = {
                    expanded = false
                    onInvertSelection?.invoke()
                },
                enabled = onInvertSelection != null,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.gallery_selection_random_action)) },
                onClick = {
                    expanded = false
                    onOpenRandomSelection?.invoke()
                },
                enabled = onOpenRandomSelection != null,
            )
        }
    }
}

@Composable
private fun OperationsOverflowMenu(
    enabled: Boolean,
    visibleCount: Int,
    videoFilter: StashVideoFilterState,
    onPlayFirstOrSelected: (() -> Unit)?,
    onSelectAll: (() -> Unit)?,
    onInvertSelection: (() -> Unit)?,
    onOpenSavedFilters: () -> Unit,
    onOpenFilter: () -> Unit,
    onToggleRandomShuffle: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarPillButton(
            icon = Icons.Outlined.MoreVert,
            label = stashString(R.string.auto_kr_0075),
            contentDescription = stashString(R.string.auto_kr_0404),
            enabled = enabled,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0088)) },
                onClick = {
                    expanded = false
                    onPlayFirstOrSelected?.invoke()
                },
                enabled = onPlayFirstOrSelected != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0079)) },
                onClick = {
                    expanded = false
                    onSelectAll?.invoke()
                },
                enabled = onSelectAll != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0089)) },
                onClick = {
                    expanded = false
                    onInvertSelection?.invoke()
                },
                enabled = onInvertSelection != null && visibleCount > 0,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0090)) },
                onClick = {
                    expanded = false
                    onOpenSavedFilters()
                },
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0091)) },
                onClick = {
                    expanded = false
                    onOpenFilter()
                },
            )
            DropdownMenuItem(
                text = { Text(stashScenesToolbarRandomActionLabel(videoFilter.randomShuffle)) },
                onClick = {
                    expanded = false
                    onToggleRandomShuffle()
                },
            )
        }
    }
}

@Composable
private fun SelectionToolbarContents(
    selectionCount: Int,
    onClearSelection: (() -> Unit)?,
    onSelectAll: (() -> Unit)?,
    onInvertSelection: (() -> Unit)?,
    onPlaySelection: (() -> Unit)?,
    onDeleteSelection: (() -> Unit)?,
) {
    CompactToolbarButton(
        icon = Icons.Outlined.Clear,
        label = stashString(R.string.auto_kr_0077),
        enabled = onClearSelection != null,
        onClick = { onClearSelection?.invoke() },
    )
    Text(
        text = stashScenesToolbarSelectionCountLabel(selectionCount),
        modifier = Modifier.semantics {
            contentDescription = stashScenesToolbarSelectionCountContentDescription(selectionCount)
        },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    CompactToolbarButton(
        icon = Icons.Outlined.SelectAll,
        label = stashString(R.string.auto_kr_0079),
        enabled = onSelectAll != null,
        onClick = { onSelectAll?.invoke() },
    )
    CompactToolbarButton(
        icon = Icons.Outlined.PlayArrow,
        label = stashString(R.string.auto_kr_0039),
        enabled = onPlaySelection != null,
        onClick = { onPlaySelection?.invoke() },
    )
    CompactToolbarButton(
        icon = Icons.Outlined.Delete,
        label = stashString(R.string.auto_kr_0081),
        enabled = onDeleteSelection != null,
        destructive = true,
        onClick = { onDeleteSelection?.invoke() },
    )
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolbarPillButton(
            icon = Icons.Outlined.MoreVert,
            label = stashString(R.string.auto_kr_0082),
            contentDescription = stashString(R.string.auto_kr_0405),
            enabled = true,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0077)) },
                onClick = {
                    expanded = false
                    onClearSelection?.invoke()
                },
                enabled = onClearSelection != null,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0079)) },
                onClick = {
                    expanded = false
                    onSelectAll?.invoke()
                },
                enabled = onSelectAll != null,
            )
            DropdownMenuItem(
                text = { Text(stashString(R.string.auto_kr_0089)) },
                onClick = {
                    expanded = false
                    onInvertSelection?.invoke()
                },
                enabled = onInvertSelection != null,
            )
        }
    }
}
