package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
fun normalizeStashSearchQuery(query: String): String = query.trim()

data class StashSearchSortOption(
    val id: String,
    val label: String,
    val sort: String,
    val defaultDirection: StashSortDirection,
)

val DEFAULT_STASH_DISCOVERY_PAGE_SIZE = 40
val DEFAULT_STASH_SEARCH_PAGE_SIZE = DEFAULT_STASH_DISCOVERY_PAGE_SIZE

fun defaultStashDiscoveryPageSizeOptions(): List<Int> = listOf(20, 40, 60, 120, 250, 500, 1000)

fun defaultStashSearchPageSizeOptions(): List<Int> = defaultStashDiscoveryPageSizeOptions()

fun defaultStashSearchSortOptions(): List<StashSearchSortOption> = listOf(
    StashSearchSortOption(
        id = "updated",
        label = stashString(R.string.auto_kr_0048),
        sort = "updated_at",
        defaultDirection = StashSortDirection.Desc,
    ),
    StashSearchSortOption(
        id = "released",
        label = stashString(R.string.auto_kr_0049),
        sort = "date",
        defaultDirection = StashSortDirection.Desc,
    ),
    StashSearchSortOption(
        id = "added",
        label = stashString(R.string.auto_kr_0050),
        sort = "created_at",
        defaultDirection = StashSortDirection.Desc,
    ),
    StashSearchSortOption(
        id = "title",
        label = stashString(R.string.auto_kr_0135),
        sort = "title",
        defaultDirection = StashSortDirection.Asc,
    ),
)

data class StashSearchPageState(
    val sortOption: StashSearchSortOption,
    val query: String = "",
    val videoFilter: StashVideoFilterState = StashVideoFilterState(),
    val results: List<SceneCardModel> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_SEARCH_PAGE_SIZE,
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
) {
    val hasQuery: Boolean get() = query.isNotBlank()
    val hasSearchIntent: Boolean get() = hasQuery || !videoFilter.isEmpty
    val activeFilterCount: Int get() = (if (hasQuery) 1 else 0) + videoFilter.activeFilterCount

    fun withQuery(query: String): StashSearchPageState = reset().copy(query = normalizeStashSearchQuery(query))

    fun forSort(sortOption: StashSearchSortOption): StashSearchPageState = reset().copy(
        query = query,
        sortOption = sortOption,
        sortDirection = sortOption.defaultDirection,
        videoFilter = videoFilter,
    )

    fun withSortDirection(direction: StashSortDirection): StashSearchPageState = reset().copy(
        query = query,
        sortDirection = direction,
        videoFilter = videoFilter,
    )

    fun withPageSize(pageSize: Int): StashSearchPageState = reset().copy(
        query = query,
        pageSize = pageSize,
        videoFilter = videoFilter,
    )

    fun withVideoFilter(videoFilter: StashVideoFilterState): StashSearchPageState = reset().copy(
        query = query,
        videoFilter = videoFilter,
    )

    fun loading(): StashSearchPageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        results: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashSearchPageState = copy(
        results = results,
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextPage(
        results: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashSearchPageState = copy(
        results = this.results + results,
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashSearchPageState = copy(isLoading = false, error = message)

    private fun reset(): StashSearchPageState = initial(sortOption).copy(
        pageSize = pageSize,
        sortDirection = sortDirection,
        videoFilter = videoFilter,
    )

    companion object {
        fun initial(sortOption: StashSearchSortOption): StashSearchPageState =
            StashSearchPageState(sortOption = sortOption, sortDirection = sortOption.defaultDirection)
    }
}
