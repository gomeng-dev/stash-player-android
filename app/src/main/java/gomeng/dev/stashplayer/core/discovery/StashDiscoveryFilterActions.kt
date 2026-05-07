package gomeng.dev.stashplayer.core.discovery

import gomeng.dev.stashplayer.core.model.StashMediaFormatFilter
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.nextStashRandomSortSeed
import gomeng.dev.stashplayer.core.model.shouldPromoteRecentVideoFilterAfterChange
import gomeng.dev.stashplayer.core.model.withToolbarRandomActionSeed
import gomeng.dev.stashplayer.core.model.withoutStashRandomShuffle

data class StashDiscoveryFilterAction(
    val videoFilter: StashVideoFilterState,
    val shouldReload: Boolean,
    val shouldPromoteRecent: Boolean,
)

enum class StashDiscoveryFilterSection {
    Tags,
    DateRange,
    DurationRange,
    PlaybackState,
    RatingRange,
    MediaFormat,
    LocalFavorite,
    SavedFilter,
    RandomShuffle,
}

fun applyStashDiscoveryManualFilter(
    current: StashVideoFilterState,
    updated: StashVideoFilterState,
): StashDiscoveryFilterAction = StashDiscoveryFilterAction(
    videoFilter = updated,
    shouldReload = updated != current,
    shouldPromoteRecent = updated != current,
)

fun applyStashDiscoveryRecentFilter(
    current: StashVideoFilterState,
    recent: StashVideoFilterState,
    nextRandomSeed: () -> Int = ::nextStashRandomSortSeed,
): StashDiscoveryFilterAction {
    val normalized = recent.withGeneratedStashRandomShuffleSeedIfNeeded(nextRandomSeed)
    return StashDiscoveryFilterAction(
        videoFilter = normalized,
        shouldReload = normalized != current,
        shouldPromoteRecent = true,
    )
}

fun applyStashDiscoverySavedFilter(
    current: StashVideoFilterState,
    savedApplied: StashVideoFilterState,
): StashDiscoveryFilterAction = StashDiscoveryFilterAction(
    videoFilter = savedApplied,
    shouldReload = savedApplied != current,
    shouldPromoteRecent = savedApplied != current,
)

fun applyStashDiscoveryTags(
    current: StashVideoFilterState,
    tags: List<StashSelectedTag>,
): StashDiscoveryFilterAction {
    val updated = current.copy(tags = tags)
    val changed = updated != current
    return StashDiscoveryFilterAction(
        videoFilter = updated,
        shouldReload = changed,
        shouldPromoteRecent = changed && shouldPromoteRecentVideoFilterAfterChange(current, updated),
    )
}

fun clearStashDiscoveryFilterSection(
    current: StashVideoFilterState,
    section: StashDiscoveryFilterSection,
): StashDiscoveryFilterAction {
    val updated = when (section) {
        StashDiscoveryFilterSection.Tags -> current.copy(tags = emptyList())
        StashDiscoveryFilterSection.DateRange -> current.copy(dateRange = null)
        StashDiscoveryFilterSection.DurationRange -> current.copy(durationRange = null)
        StashDiscoveryFilterSection.PlaybackState -> current.copy(playbackState = null)
        StashDiscoveryFilterSection.RatingRange -> current.copy(ratingRange = null)
        StashDiscoveryFilterSection.MediaFormat -> current.copy(mediaFormat = StashMediaFormatFilter())
        StashDiscoveryFilterSection.LocalFavorite -> current.copy(localFavoriteOnly = false)
        StashDiscoveryFilterSection.SavedFilter -> current.copy(savedFilter = null)
        StashDiscoveryFilterSection.RandomShuffle -> current.withoutStashRandomShuffle()
    }
    return StashDiscoveryFilterAction(
        videoFilter = updated,
        shouldReload = updated != current,
        shouldPromoteRecent = false,
    )
}

fun applyStashDiscoveryRandomShuffleAction(
    current: StashVideoFilterState,
    nextRandomSeed: () -> Int = ::nextStashRandomSortSeed,
): StashDiscoveryFilterAction {
    val updated = current.withToolbarRandomActionSeed(nextRandomSeed())
    return StashDiscoveryFilterAction(
        videoFilter = updated,
        shouldReload = updated != current,
        shouldPromoteRecent = true,
    )
}

private fun StashVideoFilterState.withGeneratedStashRandomShuffleSeedIfNeeded(
    nextRandomSeed: () -> Int,
): StashVideoFilterState = when {
    !randomShuffle && randomShuffleSeed != null -> withoutStashRandomShuffle()
    randomShuffle && randomShuffleSeed == null -> withToolbarRandomActionSeed(nextRandomSeed())
    else -> this
}
