package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashSavedFilterRef(
    val id: String,
    val name: String,
)

fun StashVideoFilterState.toSavedFilterPayload(): StashVideoFilterState = copy(
    savedFilter = null,
    randomShuffleSeed = null,
)

fun StashVideoFilterState.toRecentFilterSnapshot(): StashVideoFilterState = copy(
    savedFilter = null,
    randomShuffleSeed = null,
)

fun StashVideoFilterState.shouldSaveAsRecentFilter(): Boolean = toRecentFilterSnapshot()
    .serializeForStorage()
    .isNotBlank()

fun shouldPromoteRecentVideoFilterAfterChange(
    previous: StashVideoFilterState,
    updated: StashVideoFilterState,
): Boolean {
    val previousKey = previous.toRecentFilterSnapshot().serializeForStorage()
    val updatedKey = updated.toRecentFilterSnapshot().serializeForStorage()
    return previousKey != updatedKey && updatedKey.isNotBlank()
}

fun StashVideoFilterState.recentTagSummaryLabel(maxVisibleTagNames: Int = 2): String {
    if (tags.isEmpty()) return ""
    val maxVisible = maxVisibleTagNames.coerceAtLeast(1)
    val normalizedNames = tags.map { tag ->
        normalizeStashVideoFilterText(tag.name).ifBlank { tag.id }
    }
    val visibleNames = normalizedNames.take(maxVisible)
    val hiddenCount = normalizedNames.size - visibleNames.size
    return buildString {
        append(stashString(R.string.auto_kr_0133))
        append(visibleNames.joinToString(", "))
        if (hiddenCount > 0) append(" +$hiddenCount")
    }
}

fun StashVideoFilterState.recentFilterSummaryLabel(maxVisibleChips: Int = 3): String {
    val snapshot = toRecentFilterSnapshot()
    val labels = buildList {
        snapshot.recentTagSummaryLabel().takeIf { it.isNotBlank() }?.let(::add)
        snapshot.activeFilterChips()
            .filterNot { it.category == StashVideoFilterCategory.Tag }
            .map { it.label }
            .forEach(::add)
    }
    if (labels.isEmpty()) return stashString(R.string.auto_kr_0129)
    val visibleLabels = labels.take(maxVisibleChips.coerceAtLeast(1))
    val hiddenCount = labels.size - visibleLabels.size
    return buildString {
        append(visibleLabels.joinToString(" · "))
        if (hiddenCount > 0) append(" +$hiddenCount")
    }
}

fun StashVideoFilterState.quickSavedVideoFilterName(suffix: String? = null): String {
    val baseName = recentFilterSummaryLabel(maxVisibleChips = Int.MAX_VALUE)
        .takeUnless { it == stashString(R.string.auto_kr_0129) }
        ?: stashString(R.string.auto_kr_0134)
    val normalizedSuffix = suffix?.let(::normalizeStashVideoFilterText).orEmpty()
    return if (normalizedSuffix.isBlank()) baseName else "$baseName · $normalizedSuffix"
}

fun promoteStashRecentVideoFilter(
    existing: List<StashVideoFilterState>,
    candidate: StashVideoFilterState,
    limit: Int,
): List<StashVideoFilterState> {
    val normalizedCandidate = candidate.toRecentFilterSnapshot()
    val candidateKey = normalizedCandidate.serializeForStorage()
    if (candidateKey.isBlank()) return existing.take(limit.coerceAtLeast(0))
    val maxSize = limit.coerceAtLeast(1)
    return (listOf(normalizedCandidate) + existing.filterNot { it.toRecentFilterSnapshot().serializeForStorage() == candidateKey })
        .take(maxSize)
}

fun StashVideoFilterState.withSavedFilterReference(ref: StashSavedFilterRef): StashVideoFilterState = copy(savedFilter = ref)
