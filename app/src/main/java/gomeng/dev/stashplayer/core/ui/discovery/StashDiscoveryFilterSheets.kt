package gomeng.dev.stashplayer.core.ui.discovery

import androidx.compose.runtime.Composable
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryFilterAction
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryFilterSection
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryOpenSheet
import gomeng.dev.stashplayer.core.discovery.StashDiscoveryTagOptionsState
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryManualFilter
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryRandomShuffleAction
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryRecentFilter
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoverySavedFilter
import gomeng.dev.stashplayer.core.discovery.applyStashDiscoveryTags
import gomeng.dev.stashplayer.core.discovery.clearStashDiscoveryFilterSection
import gomeng.dev.stashplayer.core.discovery.toFilterSheetVisibility
import gomeng.dev.stashplayer.core.local.LocalSavedVideoFilter
import gomeng.dev.stashplayer.core.local.appliedFilterState
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.ui.components.StashDateDurationPlaybackFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashLocalLibraryFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashRatingMediaFormatFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashSavedFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashTagFilterSheet
import gomeng.dev.stashplayer.core.ui.components.StashUnifiedVideoFilterSheet

fun buildStashDiscoveryTagSheetApplyAction(
    videoFilter: StashVideoFilterState,
    tags: List<StashSelectedTag>,
): StashDiscoveryFilterAction = applyStashDiscoveryTags(videoFilter, tags)

@Composable
fun StashDiscoveryFilterSheets(
    openSheet: StashDiscoveryOpenSheet,
    videoFilter: StashVideoFilterState,
    recentFilters: List<StashVideoFilterState>,
    savedFilters: List<LocalSavedVideoFilter>,
    savedFilterName: String,
    tagOptionsState: StashDiscoveryTagOptionsState,
    onSavedFilterNameChange: (String) -> Unit,
    onApplyFilterAction: (StashDiscoveryFilterAction, Boolean) -> Unit,
    onOpenSheet: (StashDiscoveryOpenSheet) -> Unit,
    onDismiss: () -> Unit,
    onSaveCurrentFilter: (String, StashVideoFilterState) -> Unit,
    onQuickSaveCurrentFilter: (String, StashVideoFilterState) -> Unit,
    onDeleteSavedFilter: (LocalSavedVideoFilter) -> Unit,
    onTagQueryChange: (String) -> Unit,
    onApplyTags: (List<StashSelectedTag>) -> Unit = { tags ->
        val action = buildStashDiscoveryTagSheetApplyAction(videoFilter, tags)
        onApplyFilterAction(action, action.shouldPromoteRecent)
    },
    onRetryTags: () -> Unit,
    onToggleLocalFavoriteOnly: () -> Unit,
    onClearSavedFilter: () -> Unit,
) {
    val visibility = openSheet.toFilterSheetVisibility()

    if (visibility.isUnifiedFilterPanelOpen) {
        StashUnifiedVideoFilterSheet(
            videoFilter = videoFilter,
            recentFilters = recentFilters,
            savedFilterCount = savedFilters.size,
            onApplyRecentFilter = { recent ->
                val action = applyStashDiscoveryRecentFilter(videoFilter, recent)
                onApplyFilterAction(action, action.shouldPromoteRecent)
            },
            onApplyVideoFilter = { updated ->
                val action = applyStashDiscoveryManualFilter(videoFilter, updated)
                onApplyFilterAction(action, action.shouldPromoteRecent)
            },
            onOpenTagFilter = { onOpenSheet(StashDiscoveryOpenSheet.Tags) },
            onOpenDateDurationPlaybackFilter = { onOpenSheet(StashDiscoveryOpenSheet.DateDurationPlayback) },
            onOpenRatingMediaFormatFilter = { onOpenSheet(StashDiscoveryOpenSheet.RatingMedia) },
            onOpenLocalLibraryFilter = { onOpenSheet(StashDiscoveryOpenSheet.LocalLibrary) },
            onToggleRandomShuffle = {
                val action = applyStashDiscoveryRandomShuffleAction(videoFilter)
                onApplyFilterAction(action, action.shouldPromoteRecent)
            },
            onDismiss = onDismiss,
        )
    }

    if (visibility.isSavedFilterSheetOpen) {
        StashSavedFilterSheet(
            savedFilters = savedFilters,
            recentFilters = recentFilters,
            currentFilter = videoFilter,
            savedFilterName = savedFilterName,
            onSavedFilterNameChange = onSavedFilterNameChange,
            onApplyRecentFilter = { recent ->
                val action = applyStashDiscoveryRecentFilter(videoFilter, recent)
                onApplyFilterAction(action, action.shouldPromoteRecent)
            },
            onApplySavedFilter = { saved ->
                val action = applyStashDiscoverySavedFilter(videoFilter, saved.appliedFilterState())
                onApplyFilterAction(action, action.shouldPromoteRecent)
            },
            onSaveCurrentFilter = onSaveCurrentFilter,
            onQuickSaveCurrentFilter = onQuickSaveCurrentFilter,
            onDeleteSavedFilter = onDeleteSavedFilter,
            onDismiss = onDismiss,
        )
    }

    if (visibility.isTagFilterOpen) {
        StashTagFilterSheet(
            videoFilter = videoFilter,
            tagQuery = tagOptionsState.query,
            tagOptions = tagOptionsState.options,
            isLoading = tagOptionsState.isLoading,
            error = tagOptionsState.error,
            onTagQueryChange = onTagQueryChange,
            onApplyTags = onApplyTags,
            onRetry = onRetryTags,
            onDismiss = onDismiss,
        )
    }

    if (visibility.isDateDurationPlaybackFilterOpen) {
        StashDateDurationPlaybackFilterSheet(
            videoFilter = videoFilter,
            onApply = { updated ->
                onApplyFilterAction(applyStashDiscoveryManualFilter(videoFilter, updated), false)
            },
            onClearDateRange = {
                onApplyFilterAction(clearStashDiscoveryFilterSection(videoFilter, StashDiscoveryFilterSection.DateRange), false)
            },
            onClearDurationRange = {
                onApplyFilterAction(clearStashDiscoveryFilterSection(videoFilter, StashDiscoveryFilterSection.DurationRange), false)
            },
            onClearPlaybackState = {
                onApplyFilterAction(clearStashDiscoveryFilterSection(videoFilter, StashDiscoveryFilterSection.PlaybackState), false)
            },
            onDismiss = onDismiss,
        )
    }

    if (visibility.isRatingMediaFormatFilterOpen) {
        StashRatingMediaFormatFilterSheet(
            videoFilter = videoFilter,
            onApply = { updated ->
                onApplyFilterAction(applyStashDiscoveryManualFilter(videoFilter, updated), false)
            },
            onClearRatingRange = {
                onApplyFilterAction(clearStashDiscoveryFilterSection(videoFilter, StashDiscoveryFilterSection.RatingRange), false)
            },
            onClearMediaFormat = {
                onApplyFilterAction(clearStashDiscoveryFilterSection(videoFilter, StashDiscoveryFilterSection.MediaFormat), false)
            },
            onDismiss = onDismiss,
        )
    }

    if (visibility.isLocalLibraryFilterOpen) {
        StashLocalLibraryFilterSheet(
            videoFilter = videoFilter,
            savedFilters = savedFilters,
            savedFilterName = savedFilterName,
            onSavedFilterNameChange = onSavedFilterNameChange,
            onToggleLocalFavoriteOnly = onToggleLocalFavoriteOnly,
            onSaveCurrentFilter = { onSaveCurrentFilter(savedFilterName, videoFilter) },
            onApplySavedFilter = { saved ->
                onApplyFilterAction(applyStashDiscoverySavedFilter(videoFilter, saved.appliedFilterState()), false)
            },
            onDeleteSavedFilter = onDeleteSavedFilter,
            onClearSavedFilter = onClearSavedFilter,
            onDismiss = onDismiss,
        )
    }
}
