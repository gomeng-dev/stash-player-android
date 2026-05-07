package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
fun filterStashSavedFilterNames(
    names: List<String>,
    query: String,
): List<String> {
    val normalizedQuery = normalizeStashVideoFilterText(query).lowercase()
    if (normalizedQuery.isBlank()) return names
    return names.filter { name ->
        normalizeStashVideoFilterText(name).lowercase().contains(normalizedQuery)
    }
}

fun duplicateStashSavedFilterName(
    requestedName: String,
    existingNames: List<String>,
): String? {
    val normalizedRequested = savedFilterBaseName(requestedName).lowercase()
    return existingNames.firstOrNull { existing ->
        normalizeStashVideoFilterText(existing).lowercase() == normalizedRequested
    }
}

fun uniqueStashSavedFilterName(
    requestedName: String,
    existingNames: List<String>,
): String {
    val baseName = savedFilterBaseName(requestedName)
    val normalizedExistingNames = existingNames
        .map { normalizeStashVideoFilterText(it).lowercase() }
        .toSet()
    if (baseName.lowercase() !in normalizedExistingNames) return baseName

    var index = 2
    while (true) {
        val candidate = "$baseName ($index)"
        if (candidate.lowercase() !in normalizedExistingNames) return candidate
        index += 1
    }
}

fun StashVideoFilterState.stashSavedFilterSummaryLabel(): String = recentFilterSummaryLabel(maxVisibleChips = 3)
    .takeUnless { it == stashString(R.string.auto_kr_0129) }
    ?: stashString(R.string.auto_kr_0130)

data class StashSavedFilterQuickApplyCandidate(
    val id: String,
    val name: String,
    val filterState: StashVideoFilterState,
    val updatedAt: Long,
)

data class StashSavedFilterQuickApplyItem(
    val id: String,
    val label: String,
    val summary: String,
    val isActive: Boolean,
    val contentDescription: String,
)

fun savedFiltersForQuickApply(
    savedFilters: List<StashSavedFilterQuickApplyCandidate>,
    currentFilter: StashVideoFilterState,
    limit: Int = 4,
): List<StashSavedFilterQuickApplyItem> {
    if (limit <= 0) return emptyList()
    val activeSavedFilterId = currentFilter.savedFilter?.id
    return savedFilters
        .sortedWith(
            compareByDescending<StashSavedFilterQuickApplyCandidate> { it.id == activeSavedFilterId }
                .thenByDescending { it.updatedAt }
                .thenBy { it.name.lowercase() }
                .thenBy { it.id },
        )
        .take(limit)
        .map { candidate ->
            val isActive = candidate.id == activeSavedFilterId
            StashSavedFilterQuickApplyItem(
                id = candidate.id,
                label = if (isActive) stashString(R.string.auto_kr_0131, candidate.name) else candidate.name,
                summary = candidate.filterState.stashSavedFilterSummaryLabel(),
                isActive = isActive,
                contentDescription = stashString(R.string.auto_kr_0132, candidate.name),
            )
        }
}

fun recentFiltersForQuickAccess(
    recentFilters: List<StashVideoFilterState>,
    limit: Int = 5,
): List<StashVideoFilterState> {
    if (limit <= 0) return emptyList()
    return recentFilters
        .asSequence()
        .map { it.toRecentFilterSnapshot() }
        .filter { it.shouldSaveAsRecentFilter() }
        .distinctBy { it.serializeForStorage() }
        .take(limit)
        .toList()
}

private fun savedFilterBaseName(name: String): String = normalizeStashVideoFilterText(name).ifBlank { stashString(R.string.auto_kr_0017) }
