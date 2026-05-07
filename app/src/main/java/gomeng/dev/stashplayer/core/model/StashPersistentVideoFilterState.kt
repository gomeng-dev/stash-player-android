package gomeng.dev.stashplayer.core.model

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class StashPersistedBrowseFilterState(
    val sortOptionId: String = "",
    val sortDirection: StashSortDirection? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
    val videoFilter: StashVideoFilterState = StashVideoFilterState(),
) {
    fun serializeForStorage(): String = buildList {
        val normalizedSort = normalizeStashVideoFilterText(sortOptionId)
        if (normalizedSort.isNotBlank()) add("sort=${encodePersistedValue(normalizedSort)}")
        sortDirection?.let { add("direction=${it.name}") }
        pageSize.takeIf { it > 0 }?.let { add("pageSize=$it") }
        val filterPayload = videoFilter.serializeForStorage()
        if (filterPayload.isNotBlank()) add("filter=${encodePersistedValue(filterPayload)}")
    }.joinToString(";")
}

data class StashPersistedSearchFilterState(
    val query: String = "",
    val sortOptionId: String = "",
    val sortDirection: StashSortDirection? = null,
    val pageSize: Int = DEFAULT_STASH_SEARCH_PAGE_SIZE,
    val videoFilter: StashVideoFilterState = StashVideoFilterState(),
) {
    fun serializeForStorage(): String = buildList {
        val normalizedQuery = normalizeStashSearchQuery(query)
        if (normalizedQuery.isNotBlank()) add("query=${encodePersistedValue(normalizedQuery)}")
        val normalizedSort = normalizeStashVideoFilterText(sortOptionId)
        if (normalizedSort.isNotBlank()) add("sort=${encodePersistedValue(normalizedSort)}")
        sortDirection?.let { add("direction=${it.name}") }
        pageSize.takeIf { it > 0 }?.let { add("pageSize=$it") }
        val filterPayload = videoFilter.serializeForStorage()
        if (filterPayload.isNotBlank()) add("filter=${encodePersistedValue(filterPayload)}")
    }.joinToString(";")
}

fun deserializeStashPersistedBrowseFilterState(serialized: String): StashPersistedBrowseFilterState {
    val fields = parsePersistedFields(serialized)
    return StashPersistedBrowseFilterState(
        sortOptionId = fields["sort"].orEmpty(),
        sortDirection = fields["direction"]?.let { direction ->
            StashSortDirection.entries.firstOrNull { it.name == direction }
        },
        pageSize = fields["pageSize"]?.toIntOrNull()?.takeIf { it > 0 } ?: DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
        videoFilter = fields["filter"]?.let(::deserializeStashVideoFilterState) ?: StashVideoFilterState(),
    )
}

fun deserializeStashPersistedSearchFilterState(serialized: String): StashPersistedSearchFilterState {
    val fields = parsePersistedFields(serialized)
    return StashPersistedSearchFilterState(
        query = fields["query"]?.let(::normalizeStashSearchQuery).orEmpty(),
        sortOptionId = fields["sort"].orEmpty(),
        sortDirection = fields["direction"]?.let { direction ->
            StashSortDirection.entries.firstOrNull { it.name == direction }
        },
        pageSize = fields["pageSize"]?.toIntOrNull()?.takeIf { it > 0 } ?: DEFAULT_STASH_SEARCH_PAGE_SIZE,
        videoFilter = fields["filter"]?.let(::deserializeStashVideoFilterState) ?: StashVideoFilterState(),
    )
}

fun StashBrowseScenePageState.Companion.initialFromPersisted(
    sortOptions: List<StashBrowseSortOption>,
    persisted: StashPersistedBrowseFilterState,
    pageSizeOptions: List<Int> = defaultStashDiscoveryPageSizeOptions(),
): StashBrowseScenePageState {
    val defaultSort = sortOptions.first()
    val sortOption = sortOptions.firstOrNull { it.id == persisted.sortOptionId } ?: defaultSort
    val pageSize = persisted.pageSize.takeIf { it in pageSizeOptions }
        ?: pageSizeOptions.firstOrNull { it == DEFAULT_STASH_DISCOVERY_PAGE_SIZE }
        ?: pageSizeOptions.first()
    return initial(
        sortOption = sortOption,
        videoFilter = persisted.videoFilter.withGeneratedStashRandomShuffleSeedIfNeeded(),
        sortDirection = persisted.sortDirection ?: sortOption.direction,
        pageSize = pageSize,
    )
}

fun StashSearchPageState.Companion.initialFromPersisted(
    sortOptions: List<StashSearchSortOption>,
    pageSizeOptions: List<Int>,
    persisted: StashPersistedSearchFilterState,
): StashSearchPageState {
    val defaultSort = sortOptions.first()
    val sortOption = sortOptions.firstOrNull { it.id == persisted.sortOptionId } ?: defaultSort
    val pageSize = persisted.pageSize.takeIf { it in pageSizeOptions }
        ?: pageSizeOptions.firstOrNull { it == DEFAULT_STASH_SEARCH_PAGE_SIZE }
        ?: pageSizeOptions.first()
    return initial(sortOption).copy(
        query = normalizeStashSearchQuery(persisted.query),
        sortDirection = persisted.sortDirection ?: sortOption.defaultDirection,
        pageSize = pageSize,
        videoFilter = persisted.videoFilter.withGeneratedStashRandomShuffleSeedIfNeeded(),
    )
}

fun StashBrowseScenePageState.toPersistedFilterState(): StashPersistedBrowseFilterState = StashPersistedBrowseFilterState(
    sortOptionId = sortOption.id,
    sortDirection = sortDirection,
    pageSize = pageSize,
    videoFilter = videoFilter,
)

fun StashSearchPageState.toPersistedFilterState(): StashPersistedSearchFilterState = StashPersistedSearchFilterState(
    query = query,
    sortOptionId = sortOption.id,
    sortDirection = sortDirection,
    pageSize = pageSize,
    videoFilter = videoFilter,
)

private fun parsePersistedFields(serialized: String): Map<String, String> {
    if (serialized.isBlank()) return emptyMap()
    return serialized
        .split(';')
        .mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) {
                null
            } else {
                val key = entry.substring(0, index)
                val value = decodePersistedValueOrNull(entry.substring(index + 1)) ?: return@mapNotNull null
                key to value
            }
        }
        .toMap()
}

private fun encodePersistedValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

private fun decodePersistedValueOrNull(value: String): String? = runCatching {
    URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}.getOrNull()
