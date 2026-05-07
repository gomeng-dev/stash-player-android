package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashBrowseSortOption(
    val id: String,
    val label: String,
    val sort: String,
    val direction: StashSortDirection,
)

data class StashBrowseScenePageState(
    val sortOption: StashBrowseSortOption,
    val videoFilter: StashVideoFilterState = StashVideoFilterState(),
    val scenes: List<SceneCardModel> = emptyList(),
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int? = null,
    val pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
    val sortDirection: StashSortDirection = sortOption.direction,
) {
    fun forSort(sortOption: StashBrowseSortOption): StashBrowseScenePageState = reset().copy(
        sortOption = sortOption,
        sortDirection = sortOption.direction,
        videoFilter = videoFilter,
    )

    fun withSortDirection(direction: StashSortDirection): StashBrowseScenePageState = reset().copy(
        sortDirection = direction,
        videoFilter = videoFilter,
    )

    fun withPageSize(pageSize: Int): StashBrowseScenePageState = reset().copy(
        pageSize = pageSize,
        videoFilter = videoFilter,
    )

    fun withVideoFilter(videoFilter: StashVideoFilterState): StashBrowseScenePageState = reset().copy(
        videoFilter = videoFilter,
    )

    fun loading(): StashBrowseScenePageState = copy(isLoading = true, error = null)

    fun withFirstPage(
        scenes: List<SceneCardModel>,
        hasMore: Boolean,
    ): StashBrowseScenePageState = copy(
        scenes = scenes,
        nextPage = 2,
        hasMore = hasMore,
        isLoading = false,
        error = null,
    )

    fun withFirstPage(
        scenes: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashBrowseScenePageState = copy(
        scenes = scenes,
        nextPage = 2,
        hasMore = perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun withNextPage(
        scenes: List<SceneCardModel>,
        hasMore: Boolean,
    ): StashBrowseScenePageState = copy(
        scenes = this.scenes + scenes,
        nextPage = nextPage + 1,
        hasMore = hasMore,
        isLoading = false,
        error = null,
    )

    fun withNextPage(
        scenes: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int,
    ): StashBrowseScenePageState = copy(
        scenes = this.scenes + scenes,
        nextPage = nextPage + 1,
        hasMore = nextPage * perPage < totalCount,
        isLoading = false,
        error = null,
        totalCount = totalCount,
    )

    fun failed(message: String): StashBrowseScenePageState = copy(isLoading = false, error = message)

    private fun reset(): StashBrowseScenePageState = initial(
        sortOption = sortOption,
        videoFilter = videoFilter,
        sortDirection = sortDirection,
        pageSize = pageSize,
    )

    companion object {
        fun initial(
            sortOption: StashBrowseSortOption,
            videoFilter: StashVideoFilterState = StashVideoFilterState(),
            sortDirection: StashSortDirection = sortOption.direction,
            pageSize: Int = DEFAULT_STASH_DISCOVERY_PAGE_SIZE,
        ): StashBrowseScenePageState = StashBrowseScenePageState(
            sortOption = sortOption,
            videoFilter = videoFilter,
            sortDirection = sortDirection,
            pageSize = pageSize,
        )
    }
}

fun defaultStashBrowseSortOptions(): List<StashBrowseSortOption> = listOf(
    StashBrowseSortOption(
        id = "updated",
        label = stashString(R.string.auto_kr_0048),
        sort = "updated_at",
        direction = StashSortDirection.Desc,
    ),
    StashBrowseSortOption(
        id = "released",
        label = stashString(R.string.auto_kr_0049),
        sort = "date",
        direction = StashSortDirection.Desc,
    ),
    StashBrowseSortOption(
        id = "added",
        label = stashString(R.string.auto_kr_0050),
        sort = "created_at",
        direction = StashSortDirection.Desc,
    ),
    StashBrowseSortOption(
        id = "plays",
        label = stashString(R.string.auto_kr_0051),
        sort = "play_count",
        direction = StashSortDirection.Desc,
    ),
    StashBrowseSortOption(
        id = "duration",
        label = stashString(R.string.auto_kr_0052),
        sort = "duration",
        direction = StashSortDirection.Desc,
    ),
)
