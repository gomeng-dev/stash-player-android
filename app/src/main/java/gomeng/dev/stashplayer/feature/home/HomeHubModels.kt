package gomeng.dev.stashplayer.feature.home

import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

enum class HomeHubSectionId {
    ServerStatus,
    Queue,
    WatchLater,
    Favorites,
    Discovery,
}

enum class HomeHubAction {
    OpenSettings,
    RetryServerSections,
    OpenQueue,
    OpenFavorites,
    OpenBrowse,
    OpenSearch,
    PlayQueue,
    ShuffleQueue,
}

data class HomeHubServerState(
    val statusLabel: String,
    val detail: String,
)

data class HomeHubSection(
    val id: HomeHubSectionId,
    val title: String,
    val subtitle: String,
    val count: Int? = null,
)

data class HomeHubState(
    val server: HomeHubServerState,
    val sections: List<HomeHubSection>,
    val actions: Set<HomeHubAction>,
    val requiresSetup: Boolean,
)

data class HomeDashboardStat(
    val sectionId: HomeHubSectionId,
    val label: String,
    val value: String,
    val subtitle: String,
)

data class HomeQuickActionModel(
    val label: String,
    val action: HomeHubAction,
    val enabled: Boolean,
)

data class HomeDiscoveryEntryModel(
    val title: String,
    val subtitle: String,
    val action: HomeHubAction,
)

data class HomeHeroSelection(
    val scene: SceneCardModel,
    val playbackScenes: List<SceneCardModel>,
)

fun selectHomeHeroScene(
    playbackHistoryScenes: List<SceneCardModel>,
    queueScenes: List<SceneCardModel>,
    watchLaterScenes: List<SceneCardModel>,
    serverScenes: List<SceneCardModel>,
    favoriteScenes: List<SceneCardModel>,
): HomeHeroSelection? {
    val orderedBuckets = listOf(
        playbackHistoryScenes,
        queueScenes,
        watchLaterScenes,
        serverScenes,
        favoriteScenes,
    )
    val scenes = orderedBuckets.firstOrNull { it.isNotEmpty() } ?: return null
    return HomeHeroSelection(scene = scenes.first(), playbackScenes = scenes)
}

fun buildHomeDashboardStats(
    queueCount: Int,
    watchLaterCount: Int,
    favoriteCount: Int,
): List<HomeDashboardStat> = listOf(
    HomeDashboardStat(
        sectionId = HomeHubSectionId.Queue,
        label = stashString(R.string.auto_kr_0004),
        value = queueCount.coerceAtLeast(0).toString(),
        subtitle = if (queueCount > 0) stashString(R.string.auto_kr_0424) else stashString(R.string.auto_kr_0425),
    ),
    HomeDashboardStat(
        sectionId = HomeHubSectionId.WatchLater,
        label = stashString(R.string.auto_kr_0016),
        value = watchLaterCount.coerceAtLeast(0).toString(),
        subtitle = if (watchLaterCount > 0) stashString(R.string.auto_kr_0426) else stashString(R.string.auto_kr_0425),
    ),
    HomeDashboardStat(
        sectionId = HomeHubSectionId.Favorites,
        label = stashString(R.string.auto_kr_0238),
        value = favoriteCount.coerceAtLeast(0).toString(),
        subtitle = if (favoriteCount > 0) stashString(R.string.auto_kr_0427) else stashString(R.string.auto_kr_0425),
    ),
)

fun buildHomeQuickActions(
    canPlayQueue: Boolean,
    canShuffleQueue: Boolean,
): List<HomeQuickActionModel> = listOf(
    HomeQuickActionModel(stashString(R.string.auto_kr_0428), HomeHubAction.PlayQueue, canPlayQueue),
    HomeQuickActionModel(stashString(R.string.auto_kr_0429), HomeHubAction.ShuffleQueue, canShuffleQueue),
    HomeQuickActionModel(stashString(R.string.auto_kr_0004), HomeHubAction.OpenQueue, true),
    HomeQuickActionModel(stashString(R.string.auto_kr_0016), HomeHubAction.OpenQueue, true),
    HomeQuickActionModel(stashString(R.string.auto_kr_0238), HomeHubAction.OpenFavorites, true),
    HomeQuickActionModel(stashString(R.string.auto_kr_0002), HomeHubAction.OpenBrowse, true),
    HomeQuickActionModel(stashString(R.string.auto_kr_0003), HomeHubAction.OpenSearch, true),
    HomeQuickActionModel(stashString(R.string.auto_kr_0005), HomeHubAction.OpenSettings, true),
)

fun homePreviewSectionTitle(sectionId: HomeHubSectionId): String = when (sectionId) {
    HomeHubSectionId.Queue -> stashString(R.string.auto_kr_0004)
    HomeHubSectionId.WatchLater -> stashString(R.string.auto_kr_0016)
    HomeHubSectionId.Favorites -> stashString(R.string.auto_kr_0238)
    HomeHubSectionId.Discovery -> stashString(R.string.auto_kr_0430)
    HomeHubSectionId.ServerStatus -> stashString(R.string.auto_kr_0431)
}

fun homeDiscoveryEntryModels(): List<HomeDiscoveryEntryModel> = listOf(
    HomeDiscoveryEntryModel(
        title = stashString(R.string.auto_kr_0002),
        subtitle = stashString(R.string.auto_kr_0432),
        action = HomeHubAction.OpenBrowse,
    ),
    HomeDiscoveryEntryModel(
        title = stashString(R.string.auto_kr_0003),
        subtitle = stashString(R.string.auto_kr_0433),
        action = HomeHubAction.OpenSearch,
    ),
)

fun homeHubErrorText(message: String?): String? = message
    ?.let(::redactStashCredentialText)
    ?.ifBlank { null }

fun buildHomeHubState(
    hasProfile: Boolean,
    serverLabel: String,
    queueCount: Int,
    watchLaterCount: Int,
    favoriteCount: Int,
    hasServerError: Boolean,
): HomeHubState {
    val server = when {
        !hasProfile -> HomeHubServerState(
            statusLabel = stashString(R.string.auto_kr_0434),
            detail = stashString(R.string.auto_kr_0435),
        )
        hasServerError -> HomeHubServerState(
            statusLabel = stashString(R.string.auto_kr_0436),
            detail = stashString(R.string.auto_kr_0437),
        )
        else -> HomeHubServerState(
            statusLabel = stashString(R.string.auto_kr_0438),
            detail = serverLabel.ifBlank { stashString(R.string.auto_kr_0439) },
        )
    }

    val actions = buildSet {
        if (!hasProfile || hasServerError) add(HomeHubAction.OpenSettings)
        if (hasProfile && hasServerError) add(HomeHubAction.RetryServerSections)
        if (hasProfile) {
            add(HomeHubAction.OpenQueue)
            add(HomeHubAction.OpenFavorites)
            add(HomeHubAction.OpenBrowse)
            add(HomeHubAction.OpenSearch)
        }
        if (queueCount > 0) {
            add(HomeHubAction.PlayQueue)
            add(HomeHubAction.ShuffleQueue)
        }
    }

    val sections = buildList {
        add(
            HomeHubSection(
                id = HomeHubSectionId.ServerStatus,
                title = stashString(R.string.auto_kr_0431),
                subtitle = server.statusLabel,
            ),
        )
        if (hasProfile) {
            add(
                HomeHubSection(
                    id = HomeHubSectionId.Queue,
                    title = stashString(R.string.auto_kr_0004),
                    subtitle = if (queueCount > 0) stashString(R.string.auto_kr_0440, queueCount) else stashString(R.string.auto_kr_0441),
                    count = queueCount,
                ),
            )
            add(
                HomeHubSection(
                    id = HomeHubSectionId.WatchLater,
                    title = stashString(R.string.auto_kr_0016),
                    subtitle = if (watchLaterCount > 0) stashString(R.string.auto_kr_0442, watchLaterCount) else stashString(R.string.auto_kr_0443),
                    count = watchLaterCount,
                ),
            )
            add(
                HomeHubSection(
                    id = HomeHubSectionId.Favorites,
                    title = stashString(R.string.auto_kr_0238),
                    subtitle = if (favoriteCount > 0) stashString(R.string.auto_kr_0444, favoriteCount) else stashString(R.string.auto_kr_0445),
                    count = favoriteCount,
                ),
            )
            add(
                HomeHubSection(
                    id = HomeHubSectionId.Discovery,
                    title = stashString(R.string.auto_kr_0430),
                    subtitle = stashString(R.string.auto_kr_0446),
                ),
            )
        }
    }

    return HomeHubState(
        server = server,
        sections = sections,
        actions = actions,
        requiresSetup = !hasProfile,
    )
}
