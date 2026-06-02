package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashUnifiedFilterPanelSection(
    val title: String,
    val summary: String,
    val isActive: Boolean,
    val editTarget: StashVideoFilterEditTarget,
)

fun StashVideoFilterState.unifiedFilterPanelButtonLabel(savedFilterCount: Int): String = buildString {
    append(stashString(R.string.auto_kr_0092))
    activeFilterCount.takeIf { it > 0 }?.let { append(" $it") }
    savedFilterCount.takeIf { it > 0 }?.let { append(stashString(R.string.auto_kr_0148, it)) }
}

fun StashVideoFilterState.unifiedFilterPanelSections(savedFilterCount: Int): List<StashUnifiedFilterPanelSection> {
    fun labelsFor(vararg categories: StashVideoFilterCategory): List<String> {
        val targets = categories.toSet()
        return activeFilterChips().filter { it.category in targets }.map { it.label }
    }

    val tagLabels = labelsFor(StashVideoFilterCategory.Tag)
    val detailLabels = labelsFor(
        StashVideoFilterCategory.DateRange,
        StashVideoFilterCategory.DurationRange,
        StashVideoFilterCategory.OCounter,
        StashVideoFilterCategory.PlaybackState,
    )
    val ratingMediaLabels = labelsFor(
        StashVideoFilterCategory.Rating,
        StashVideoFilterCategory.MediaFormat,
    )
    val localLabels = buildList {
        savedFilterCount.takeIf { it > 0 }?.let { add(stashString(R.string.auto_kr_0149, it)) }
        labelsFor(StashVideoFilterCategory.LocalFavorite).forEach(::add)
        labelsFor(StashVideoFilterCategory.SavedFilter).forEach(::add)
    }
    val randomLabels = labelsFor(StashVideoFilterCategory.RandomShuffle)

    return listOf(
        StashUnifiedFilterPanelSection(
            title = stashString(R.string.auto_kr_0066),
            summary = tagLabels.joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0150) },
            isActive = tagLabels.isNotEmpty(),
            editTarget = StashVideoFilterEditTarget.Tags,
        ),
        StashUnifiedFilterPanelSection(
            title = stashString(R.string.auto_kr_0085),
            summary = detailLabels.joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0151) },
            isActive = detailLabels.isNotEmpty(),
            editTarget = StashVideoFilterEditTarget.DateDurationPlayback,
        ),
        StashUnifiedFilterPanelSection(
            title = stashString(R.string.auto_kr_0068),
            summary = ratingMediaLabels.joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0152) },
            isActive = ratingMediaLabels.isNotEmpty(),
            editTarget = StashVideoFilterEditTarget.RatingMedia,
        ),
        StashUnifiedFilterPanelSection(
            title = stashString(R.string.auto_kr_0086),
            summary = localLabels.joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0153) },
            isActive = localFavoriteOnly || savedFilter != null || savedFilterCount > 0,
            editTarget = StashVideoFilterEditTarget.LocalLibrary,
        ),
        StashUnifiedFilterPanelSection(
            title = stashString(R.string.auto_kr_0087),
            summary = randomLabels.joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0154) },
            isActive = randomShuffle,
            editTarget = StashVideoFilterEditTarget.RandomShuffle,
        ),
    )
}

fun StashVideoFilterState.hasResettableUnifiedFilterPanelSection(
    target: StashVideoFilterEditTarget,
): Boolean = when (target) {
    StashVideoFilterEditTarget.Tags -> tags.isNotEmpty()
    StashVideoFilterEditTarget.DateDurationPlayback ->
        dateRange?.isEmpty == false || durationRange?.isEmpty == false || oCounterFilter?.isNoOp == false || playbackState != null
    StashVideoFilterEditTarget.RatingMedia -> ratingRange?.isEmpty == false || !mediaFormat.isEmpty
    StashVideoFilterEditTarget.LocalLibrary -> localFavoriteOnly || savedFilter != null
    StashVideoFilterEditTarget.RandomShuffle -> randomShuffle
}

fun StashVideoFilterState.resetUnifiedFilterPanelSection(
    target: StashVideoFilterEditTarget,
): StashVideoFilterState = when (target) {
    StashVideoFilterEditTarget.Tags -> copy(tags = emptyList())
    StashVideoFilterEditTarget.DateDurationPlayback -> copy(
        dateRange = null,
        durationRange = null,
        oCounterFilter = null,
        playbackState = null,
    )
    StashVideoFilterEditTarget.RatingMedia -> copy(
        ratingRange = null,
        mediaFormat = StashMediaFormatFilter(),
    )
    StashVideoFilterEditTarget.LocalLibrary -> copy(
        localFavoriteOnly = false,
        savedFilter = null,
    )
    StashVideoFilterEditTarget.RandomShuffle -> withoutStashRandomShuffle()
}
