package gomeng.dev.stashplayer.core.network

import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

interface SimilarScenesRepository {
    suspend fun getSimilarScenes(sceneId: String, limit: Int = 10): Result<List<SimilarSceneRecommendation>>

    suspend fun getSimilarScenesWithSource(sceneId: String, limit: Int = 10): Result<SimilarScenesRepositoryResult> {
        return getSimilarScenes(sceneId, limit).map { recommendations ->
            SimilarScenesRepositoryResult(
                recommendations = recommendations,
                source = SimilarVideosRecommendationSource.HybridBackend,
            )
        }
    }
}

data class SimilarScenesRepositoryResult(
    val recommendations: List<SimilarSceneRecommendation>,
    val source: SimilarVideosRecommendationSource,
)

interface SimilarScenesClient {
    suspend fun fetchSimilarScenes(sceneId: String, limit: Int = 10): List<SimilarSceneRecommendation>
}

sealed class SimilarScenesLoadState {
    data object Loading : SimilarScenesLoadState()
    data object Empty : SimilarScenesLoadState()
    data class Success(val recommendations: List<SimilarSceneRecommendation>) : SimilarScenesLoadState()
    data class Error(val message: String) : SimilarScenesLoadState()
}

fun similarScenesLoadStateFrom(
    result: Result<List<SimilarSceneRecommendation>>,
): SimilarScenesLoadState {
    return result.fold(
        onSuccess = { recommendations ->
            if (recommendations.isEmpty()) {
                SimilarScenesLoadState.Empty
            } else {
                SimilarScenesLoadState.Success(recommendations)
            }
        },
        onFailure = { SimilarScenesLoadState.Error(stashString(R.string.auto_kr_0167)) },
    )
}

class SessionCachedSimilarScenesRepository(
    private val client: SimilarScenesClient,
    private val cacheKeyScope: () -> String = { "" },
    private val recommendationSource: SimilarVideosRecommendationSource = SimilarVideosRecommendationSource.HybridBackend,
) : SimilarScenesRepository {
    private val cache = LinkedHashMap<CacheKey, List<SimilarSceneRecommendation>>()

    override suspend fun getSimilarScenes(sceneId: String, limit: Int): Result<List<SimilarSceneRecommendation>> {
        return getSimilarScenesWithSource(sceneId, limit).map { it.recommendations }
    }

    override suspend fun getSimilarScenesWithSource(sceneId: String, limit: Int): Result<SimilarScenesRepositoryResult> {
        val normalizedSceneId = sceneId.trim()
        if (normalizedSceneId.isBlank()) {
            return Result.success(
                SimilarScenesRepositoryResult(
                    recommendations = emptyList(),
                    source = recommendationSource,
                ),
            )
        }
        val safeLimit = limit.coerceIn(1, 50)
        val key = CacheKey(cacheKeyScope(), normalizedSceneId, safeLimit)
        cache[key]?.let { cached ->
            return Result.success(
                SimilarScenesRepositoryResult(
                    recommendations = cached,
                    source = recommendationSource,
                ),
            )
        }

        return runCatching { client.fetchSimilarScenes(normalizedSceneId, safeLimit) }
            .onSuccess { recommendations -> cache[key] = recommendations }
            .map { recommendations ->
                SimilarScenesRepositoryResult(
                    recommendations = recommendations,
                    source = recommendationSource,
                )
            }
    }

    private data class CacheKey(
        val scope: String,
        val sceneId: String,
        val limit: Int,
    )
}

class FallbackSimilarScenesRepository(
    private val primary: SimilarScenesRepository,
    private val fallback: SimilarScenesRepository?,
) : SimilarScenesRepository {
    override suspend fun getSimilarScenes(sceneId: String, limit: Int): Result<List<SimilarSceneRecommendation>> {
        return getSimilarScenesWithSource(sceneId, limit).map { it.recommendations }
    }

    override suspend fun getSimilarScenesWithSource(sceneId: String, limit: Int): Result<SimilarScenesRepositoryResult> {
        val primaryResult = primary.getSimilarScenesWithSource(sceneId, limit)
        val primaryRecommendations = primaryResult.getOrNull()?.recommendations
        if (primaryRecommendations != null && primaryRecommendations.isNotEmpty()) {
            return primaryResult
        }
        return fallback?.getSimilarScenesWithSource(sceneId, limit) ?: primaryResult
    }
}

fun buildSimilarScenesRepository(
    graphQlClient: StashGraphQlClient,
    stashServerProfile: StashServerProfile,
): SimilarScenesRepository {
    val graphQlRepository = SessionCachedSimilarScenesRepository(
        client = GraphQlSimilarScenesClient(graphQlClient),
        cacheKeyScope = { GRAPHQL_SIMILAR_SCENES_CACHE_KEY },
        recommendationSource = SimilarVideosRecommendationSource.GraphQlFallback,
    )
    val pluginRepository = SessionCachedSimilarScenesRepository(
        client = StashPluginSimilarScenesClient(
            operationsClient = graphQlClient,
            stashServerProfile = stashServerProfile,
        ),
        cacheKeyScope = { PLUGIN_SIMILAR_SCENES_CACHE_KEY },
        recommendationSource = SimilarVideosRecommendationSource.HybridBackend,
    )
    return FallbackSimilarScenesRepository(
        primary = pluginRepository,
        fallback = graphQlRepository,
    )
}

private val GRAPHQL_SIMILAR_SCENES_CACHE_KEY = "graphql-similar-scenes"
private val PLUGIN_SIMILAR_SCENES_CACHE_KEY = "stash-plugin-similar-scenes"

class GraphQlSimilarScenesClient(
    private val client: StashGraphQlClient,
) : SimilarScenesClient {
    override suspend fun fetchSimilarScenes(sceneId: String, limit: Int): List<SimilarSceneRecommendation> {
        return client.findSimilarSceneRecommendations(sceneId = sceneId, limit = limit)
    }
}

class StashPluginSimilarScenesClient(
    private val operationsClient: StashPluginOperationsClient,
    private val stashServerProfile: StashServerProfile,
) : SimilarScenesClient {
    override suspend fun fetchSimilarScenes(sceneId: String, limit: Int): List<SimilarSceneRecommendation> {
        val pluginId = detectStashHybridRecommendationsPluginId(operationsClient.listPlugins())
            ?: error("Stash Hybrid Recommendations Engine plugin is not enabled")
        val statusPayload = operationsClient.runPluginOperation(
            pluginId = pluginId,
            args = buildStashPluginStatusArgs(),
        )
        val status = parseStashPluginRecommendationStatusResponse(statusPayload)
        check(status.ok) { status.message ?: "Stash Hybrid Recommendations Engine status is not ok" }

        val responsePayload = operationsClient.runPluginOperation(
            pluginId = pluginId,
            args = buildStashPluginRecommendArgs(sceneId = sceneId, limit = limit),
        )
        val response = parseStashPluginSimilarSceneRecommendationsResponse(
            json = responsePayload,
            currentSceneId = sceneId,
            stashServerProfile = stashServerProfile,
        )
        check(response.ok) { response.message ?: "Stash Hybrid Recommendations Engine recommend failed" }
        if (response.recommendations.isEmpty()) {
            error("Stash Hybrid Recommendations Engine returned no recommendations")
        }
        return response.recommendations
    }
}

/**
 * Clearly named fallback adapter for sessions where the recommendation backend is not configured yet.
 * It uses the same repository contract as the real client and intentionally returns an empty state.
 */
class TemporaryEmptySimilarScenesRepository : SimilarScenesRepository {
    override suspend fun getSimilarScenes(sceneId: String, limit: Int): Result<List<SimilarSceneRecommendation>> {
        return Result.success(emptyList())
    }

    override suspend fun getSimilarScenesWithSource(sceneId: String, limit: Int): Result<SimilarScenesRepositoryResult> {
        return Result.success(
            SimilarScenesRepositoryResult(
                recommendations = emptyList(),
                source = SimilarVideosRecommendationSource.GraphQlFallback,
            ),
        )
    }
}
