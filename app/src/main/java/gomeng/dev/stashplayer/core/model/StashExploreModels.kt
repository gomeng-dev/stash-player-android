package gomeng.dev.stashplayer.core.model

fun defaultStashExplorePageSizeOptions(): List<Int> = defaultStashDiscoveryPageSizeOptions()

data class StashExploreSortOption(
    val id: String,
    val label: String,
    val sort: String,
    val defaultDirection: StashSortDirection,
)

fun defaultStashExploreSortOptions(): List<StashExploreSortOption> = defaultStashSceneSortOptionSpecs().map { spec ->
    StashExploreSortOption(
        id = spec.id,
        label = spec.label,
        sort = spec.sort,
        defaultDirection = spec.defaultDirection,
    )
}

data class StashExplorePageState(
    val sortOption: StashExploreSortOption,
    val query: String = "",
    val videoFilter: StashVideoFilterState = StashVideoFilterState(),
    val results: List<SceneCardModel> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
    val sortDirection: StashSortDirection = sortOption.defaultDirection,
) {
    val hasQuery: Boolean get() = query.isNotBlank()
    val hasExploreIntent: Boolean get() = true
    val activeFilterCount: Int get() = (if (hasQuery) 1 else 0) + videoFilter.activeFilterCount

    fun withQuery(query: String): StashExplorePageState = reset().copy(query = normalizeStashDiscoveryQuery(query))

    fun forSort(sortOption: StashExploreSortOption): StashExplorePageState = reset().copy(
        query = query,
        sortOption = sortOption,
        sortDirection = sortOption.defaultDirection,
        videoFilter = videoFilter,
    )

    fun withSortDirection(direction: StashSortDirection): StashExplorePageState = reset().copy(
        query = query,
        sortDirection = direction,
        videoFilter = videoFilter,
    )

    fun withPageSize(pageSize: Int): StashExplorePageState = reset().copy(
        query = query,
        pageSize = pageSize,
        videoFilter = videoFilter,
    )

    fun withVideoFilter(videoFilter: StashVideoFilterState): StashExplorePageState = reset().copy(
        query = query,
        videoFilter = videoFilter,
    )

    fun loading(): StashExplorePageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        results: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashExplorePageState = copy(
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
    ): StashExplorePageState = copy(
        results = this.results + results,
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashExplorePageState = copy(isLoading = false, error = message)

    private fun reset(): StashExplorePageState = initial(sortOption).copy(
        pageSize = pageSize,
        sortDirection = sortDirection,
        videoFilter = videoFilter,
    )

    companion object {
        fun initial(sortOption: StashExploreSortOption): StashExplorePageState =
            StashExplorePageState(sortOption = sortOption, sortDirection = sortOption.defaultDirection)
    }
}

fun shouldUseLocalFavoriteExploreResults(
    query: String,
    videoFilter: StashVideoFilterState,
): Boolean = shouldUseLocalFavoriteDiscoveryResults(query, videoFilter)

fun shouldLoadExploreResultsFromServer(
    query: String,
    videoFilter: StashVideoFilterState,
): Boolean = !shouldUseLocalFavoriteExploreResults(query, videoFilter)
