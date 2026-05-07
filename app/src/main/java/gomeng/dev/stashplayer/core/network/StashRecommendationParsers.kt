package gomeng.dev.stashplayer.core.network

import gomeng.dev.stashplayer.core.model.RecommendationBreakdown
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarSceneSummary
import gomeng.dev.stashplayer.core.model.filterSimilarRecommendationsForCurrentScene
import gomeng.dev.stashplayer.core.model.normalizeSimilarRecommendationReasons
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.net.URI
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val recommendationMoshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

fun parseSimilarSceneRecommendationsResponse(
    json: String,
    currentSceneId: String,
    stashServerProfile: StashServerProfile? = null,
): List<SimilarSceneRecommendation> {
    val envelope = recommendationMoshi.adapter(ApiSimilarRecommendationsEnvelope::class.java).fromJson(json)
        ?: error("Empty similar recommendations response")
    return filterSimilarRecommendationsForCurrentScene(
        currentSceneId = currentSceneId,
        recommendations = envelope.recommendations.mapNotNull { it.toDomainOrNull(stashServerProfile) },
    )
}

fun rewriteStashRecommendationMediaUrl(
    url: String?,
    stashServerProfile: StashServerProfile,
): String? {
    val trimmedUrl = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (trimmedUrl.startsWith("/scene/")) {
        return stashServerProfile.authenticatedUrl(trimmedUrl)
    }

    val parsedUrl = runCatching { URI(trimmedUrl) }.getOrNull() ?: return null
    val scheme = parsedUrl.scheme?.lowercase()
    if (scheme == null) {
        return if (trimmedUrl.startsWith("scene/")) {
            stashServerProfile.authenticatedUrl("/$trimmedUrl")
        } else {
            null
        }
    }
    if (scheme != "http" && scheme != "https") return null

    val host = parsedUrl.host?.lowercase() ?: return null
    val path = parsedUrl.path.orEmpty()
    val isLocalStashSceneAsset = (host == "127.0.0.1" || host == "localhost") && path.startsWith("/scene/")
    if (!isLocalStashSceneAsset) {
        return stashServerProfile.authenticatedUrl(trimmedUrl)
    }

    val rewrittenPath = buildString {
        append(path)
        parsedUrl.rawQuery?.takeIf { it.isNotBlank() }?.let { append("?").append(it) }
        parsedUrl.rawFragment?.takeIf { it.isNotBlank() }?.let { append("#").append(it) }
    }
    return stashServerProfile.authenticatedUrl(rewrittenPath)
}

private data class ApiSimilarRecommendationsEnvelope(
    val recommendations: List<ApiSimilarRecommendation> = emptyList(),
)

private data class ApiSimilarRecommendation(
    val sceneId: String? = null,
    val score: Double? = null,
    val reasons: List<String> = emptyList(),
    val breakdown: ApiRecommendationBreakdown? = null,
    val scene: ApiSimilarScene? = null,
) {
    fun toDomainOrNull(stashServerProfile: StashServerProfile?): SimilarSceneRecommendation? {
        val recommendationSceneId = sceneId?.trim()?.takeIf { it.isNotBlank() }
            ?: scene?.id?.trim()?.takeIf { it.isNotBlank() }
            ?: return null
        val sceneSummary = scene?.toDomain(recommendationSceneId, stashServerProfile)
            ?: SimilarSceneSummary(id = recommendationSceneId, title = stashString(R.string.auto_kr_0169, recommendationSceneId))
        return SimilarSceneRecommendation(
            sceneId = recommendationSceneId,
            scene = sceneSummary,
            score = score ?: 0.0,
            reasons = normalizeSimilarRecommendationReasons(reasons),
            breakdown = breakdown?.toDomain(),
        )
    }
}

private data class ApiRecommendationBreakdown(
    val tag: Double = 0.0,
    val filename: Double = 0.0,
    val visual: Double = 0.0,
    val duration: Double = 0.0,
    val resolution: Double = 0.0,
    val behavior: Double = 0.0,
) {
    fun toDomain(): RecommendationBreakdown {
        return RecommendationBreakdown(
            tag = tag,
            filename = filename,
            visual = visual,
            duration = duration,
            resolution = resolution,
            behavior = behavior,
        )
    }
}

private data class ApiSimilarScene(
    val id: String? = null,
    val title: String? = null,
    val fileName: String? = null,
    val durationSeconds: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    val rating100: Int? = null,
    val playCount: Int? = null,
    val thumbnailUrl: String? = null,
    val spriteImageUrl: String? = null,
    val paths: ApiSimilarScenePaths? = null,
) {
    fun toDomain(fallbackId: String, stashServerProfile: StashServerProfile?): SimilarSceneSummary {
        val resolvedId = id?.trim()?.takeIf { it.isNotBlank() } ?: fallbackId
        val rawThumbnailUrl = thumbnailUrl?.trim()?.takeIf { it.isNotBlank() } ?: paths?.screenshot?.trim()?.takeIf { it.isNotBlank() }
        val rawSpriteImageUrl = spriteImageUrl?.trim()?.takeIf { it.isNotBlank() } ?: paths?.sprite?.trim()?.takeIf { it.isNotBlank() }
        val rewrittenThumbnailUrl = if (stashServerProfile != null) {
            rewriteStashRecommendationMediaUrl(rawThumbnailUrl, stashServerProfile)
        } else {
            rawThumbnailUrl
        }
        val rewrittenSpriteImageUrl = if (stashServerProfile != null) {
            rewriteStashRecommendationMediaUrl(rawSpriteImageUrl, stashServerProfile)
        } else {
            rawSpriteImageUrl
        }
        return SimilarSceneSummary(
            id = resolvedId,
            title = title?.trim()?.takeIf { it.isNotBlank() } ?: stashString(R.string.auto_kr_0170, resolvedId),
            fileName = fileName?.trim()?.takeIf { it.isNotBlank() },
            thumbnailUrl = rewrittenThumbnailUrl ?: rewrittenSpriteImageUrl,
            spriteImageUrl = rewrittenSpriteImageUrl,
            durationSeconds = durationSeconds,
            width = width,
            height = height,
            rating100 = rating100,
            playCount = playCount,
        )
    }
}

private data class ApiSimilarScenePaths(
    val screenshot: String? = null,
    val sprite: String? = null,
)
