package gomeng.dev.stashplayer.feature.home

import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.ShortsExplicitFeedback
import gomeng.dev.stashplayer.core.model.ShortsInteractionRecord
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.util.Locale

enum class HomeHubSectionId {
    ServerStatus,
    Recommended,
    Queue,
    WatchLater,
    Favorites,
}

enum class HomeHubAction {
    OpenSettings,
    RetryServerSections,
    OpenQueue,
    OpenFavorites,
    OpenExplore,
    OpenShorts,
    OpenBrowse,
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
    val subtitle: String?,
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
    val subtitle: String?,
)

enum class HomeQuickActionKey {
    PlayQueue,
    ShuffleQueue,
    OpenQueue,
    OpenWatchLater,
    OpenFavorites,
    OpenExplore,
    OpenShorts,
    OpenSettings,
}

data class HomeQuickActionModel(
    val key: HomeQuickActionKey,
    val label: String,
    val action: HomeHubAction,
    val enabled: Boolean,
)

data class HomeHeroSelection(
    val scene: SceneCardModel,
    val playbackScenes: List<SceneCardModel>,
    val resumesPlaybackQueue: Boolean,
)

data class HomeRecommendationAnchor(
    val sceneId: String,
    val tagIds: Set<String> = emptySet(),
    val studio: String? = null,
)

fun selectHomeHeroScene(
    playbackHistoryScenes: List<SceneCardModel>,
    queueScenes: List<SceneCardModel>,
    watchLaterScenes: List<SceneCardModel>,
    serverScenes: List<SceneCardModel>,
    favoriteScenes: List<SceneCardModel>,
): HomeHeroSelection? {
    val orderedBuckets = listOf(
        playbackHistoryScenes to true,
        queueScenes to false,
        watchLaterScenes to false,
        serverScenes to false,
        favoriteScenes to false,
    )
    val (scenes, resumesPlaybackQueue) = orderedBuckets.firstOrNull { it.first.isNotEmpty() } ?: return null
    return HomeHeroSelection(
        scene = scenes.first(),
        playbackScenes = scenes,
        resumesPlaybackQueue = resumesPlaybackQueue,
    )
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
        subtitle = null,
    ),
    HomeDashboardStat(
        sectionId = HomeHubSectionId.WatchLater,
        label = stashString(R.string.auto_kr_0016),
        value = watchLaterCount.coerceAtLeast(0).toString(),
        subtitle = null,
    ),
    HomeDashboardStat(
        sectionId = HomeHubSectionId.Favorites,
        label = stashString(R.string.auto_kr_0238),
        value = favoriteCount.coerceAtLeast(0).toString(),
        subtitle = null,
    ),
)

fun buildHomeQuickActions(
    canPlayQueue: Boolean,
    canShuffleQueue: Boolean,
): List<HomeQuickActionModel> = listOf(
    HomeQuickActionModel(
        key = HomeQuickActionKey.PlayQueue,
        label = stashString(R.string.auto_kr_0428),
        action = HomeHubAction.PlayQueue,
        enabled = canPlayQueue,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.ShuffleQueue,
        label = stashString(R.string.auto_kr_0429),
        action = HomeHubAction.ShuffleQueue,
        enabled = canShuffleQueue,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenQueue,
        label = stashString(R.string.auto_kr_0004),
        action = HomeHubAction.OpenQueue,
        enabled = true,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenWatchLater,
        label = stashString(R.string.auto_kr_0016),
        action = HomeHubAction.OpenQueue,
        enabled = true,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenFavorites,
        label = stashString(R.string.auto_kr_0238),
        action = HomeHubAction.OpenFavorites,
        enabled = true,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenExplore,
        label = stashString(R.string.navigation_explore_label),
        action = HomeHubAction.OpenExplore,
        enabled = true,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenShorts,
        label = stashString(R.string.navigation_shorts_label),
        action = HomeHubAction.OpenShorts,
        enabled = true,
    ),
    HomeQuickActionModel(
        key = HomeQuickActionKey.OpenSettings,
        label = stashString(R.string.auto_kr_0005),
        action = HomeHubAction.OpenSettings,
        enabled = true,
    ),
)

fun homePreviewSectionTitle(sectionId: HomeHubSectionId): String = when (sectionId) {
    HomeHubSectionId.Recommended -> stashString(R.string.home_recommended_section_title)
    HomeHubSectionId.Queue -> stashString(R.string.auto_kr_0004)
    HomeHubSectionId.WatchLater -> stashString(R.string.auto_kr_0016)
    HomeHubSectionId.Favorites -> stashString(R.string.auto_kr_0238)
    HomeHubSectionId.ServerStatus -> stashString(R.string.auto_kr_0431)
}

fun buildHomeRecommendationAnchors(
    favoriteScenes: List<SceneCardModel>,
    shortsInteractions: List<ShortsInteractionRecord>,
    maxAnchors: Int = 5,
): List<HomeRecommendationAnchor> {
    val shortsAnchors = shortsInteractions
        .asSequence()
        .filter { it.explicitFeedback == ShortsExplicitFeedback.Liked }
        .sortedByDescending { it.updatedAt }
        .mapNotNull { interaction ->
            interaction.sceneId.trim().takeIf { it.isNotBlank() }?.let { sceneId ->
                HomeRecommendationAnchor(
                    sceneId = sceneId,
                    tagIds = interaction.tagIdsSnapshot.mapNotNull { it.normalizedTokenOrNull() }.toSet(),
                    studio = interaction.studioSnapshot?.normalizedTokenOrNull(),
                )
            }
        }

    val favoriteAnchors = favoriteScenes.asSequence().mapNotNull { scene ->
        scene.id.trim().takeIf { it.isNotBlank() }?.let { sceneId ->
            HomeRecommendationAnchor(
                sceneId = sceneId,
                tagIds = scene.tagChips.mapNotNull { it.id.normalizedTokenOrNull() }.toSet(),
                studio = scene.studio.normalizedTokenOrNull(),
            )
        }
    }

    return (shortsAnchors + favoriteAnchors)
        .distinctBy { it.sceneId }
        .take(maxAnchors.coerceAtLeast(0))
        .toList()
}

fun rankHomeRecommendedScenes(
    candidates: List<SceneCardModel>,
    anchors: List<HomeRecommendationAnchor>,
    hybridRecommendations: List<SimilarSceneRecommendation>,
    limit: Int = 12,
): List<SceneCardModel> {
    if (anchors.isEmpty()) return emptyList()
    val anchorIds = anchors.map { it.sceneId }.toSet()
    val candidateById = linkedMapOf<String, SceneCardModel>()
    candidates.forEach { scene ->
        if (scene.id !in anchorIds) candidateById.putIfAbsent(scene.id, scene)
    }
    hybridRecommendations.forEach { recommendation ->
        if (recommendation.sceneId !in anchorIds && recommendation.scene.id !in anchorIds) {
            candidateById.putIfAbsent(recommendation.sceneId, recommendation.toSceneCardModel())
        }
    }

    val hybridScores = hybridRecommendations
        .filterNot { it.sceneId in anchorIds || it.scene.id in anchorIds }
        .groupBy { it.sceneId }
        .mapValues { (_, rows) -> rows.maxOf { it.score.coerceAtLeast(0.0) } }

    return candidateById.values
        .mapNotNull { scene ->
            val score = homeRecommendationScore(
                scene = scene,
                anchors = anchors,
                hybridScore = hybridScores[scene.id] ?: 0.0,
            )
            if (score > 0.0) scene to score else null
        }
        .sortedWith(
            compareByDescending<Pair<SceneCardModel, Double>> { it.second }
                .thenBy { it.first.title.lowercase() }
                .thenBy { it.first.id },
        )
        .take(limit.coerceAtLeast(0))
        .map { it.first }
}

fun homeRecommendationCandidatePool(
    libraryCandidates: List<SceneCardModel>,
    sectionCandidates: List<SceneCardModel>,
): List<SceneCardModel> = (libraryCandidates + sectionCandidates)
    .distinctBy { it.id }

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
            add(HomeHubAction.OpenExplore)
            add(HomeHubAction.OpenShorts)
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
        }
    }

    return HomeHubState(
        server = server,
        sections = sections,
        actions = actions,
        requiresSetup = !hasProfile,
    )
}

private fun homeRecommendationScore(
    scene: SceneCardModel,
    anchors: List<HomeRecommendationAnchor>,
    hybridScore: Double,
): Double {
    val sceneTagIds = scene.tagChips.mapNotNull { it.id.normalizedTokenOrNull() }.toSet()
    val sceneStudio = scene.studio.normalizedTokenOrNull()?.lowercase()
    val sharedTagScore = anchors.sumOf { anchor ->
        sceneTagIds.intersect(anchor.tagIds).size
    } * 10.0
    val studioScore = if (sceneStudio != null && anchors.any { it.studio?.lowercase() == sceneStudio }) 6.0 else 0.0
    return hybridScore * 100.0 + sharedTagScore + studioScore
}

private fun SimilarSceneRecommendation.toSceneCardModel(): SceneCardModel = SceneCardModel(
    id = sceneId.ifBlank { scene.id },
    title = scene.title.ifBlank { scene.fileName.orEmpty() }.ifBlank { stashString(R.string.auto_kr_0168, sceneId) },
    durationText = scene.durationSeconds?.let { formatHomeRecommendationDuration(it) }.orEmpty(),
    studio = "",
    progress = 0f,
    thumbnailUrl = scene.thumbnailUrl ?: scene.spriteImageUrl,
    playCount = scene.playCount,
)

private fun formatHomeRecommendationDuration(durationSeconds: Double): String {
    val totalSeconds = durationSeconds.toLong().coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

private fun String.normalizedTokenOrNull(): String? = trim()
    .takeIf { it.isNotBlank() }
