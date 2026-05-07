package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
/**
 * Android-side contract for server/Mac precomputed similar-scene recommendations.
 * The app consumes already-computed scores and never performs heavy visual similarity locally.
 */
data class SimilarSceneRecommendation(
    val sceneId: String,
    val scene: SimilarSceneSummary,
    val score: Double,
    val reasons: List<String>,
    val breakdown: RecommendationBreakdown? = null,
)

data class RecommendationBreakdown(
    val tag: Double = 0.0,
    val filename: Double = 0.0,
    val visual: Double = 0.0,
    val duration: Double = 0.0,
    val resolution: Double = 0.0,
    val behavior: Double = 0.0,
)

data class SimilarSceneSummary(
    val id: String,
    val title: String,
    val fileName: String? = null,
    val thumbnailUrl: String? = null,
    val spriteImageUrl: String? = null,
    val durationSeconds: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    val rating100: Int? = null,
    val playCount: Int? = null,
)

object SimilarRecommendationReasonLabels {
    val TAG = stashString(R.string.auto_kr_0022)
    val FILENAME = stashString(R.string.auto_kr_0023)
    val VISUAL = stashString(R.string.auto_kr_0024)
    val DURATION = stashString(R.string.auto_kr_0025)
    val RESOLUTION = stashString(R.string.auto_kr_0026)
    val BEHAVIOR = stashString(R.string.auto_kr_0027)

    val allowed: Set<String> = linkedSetOf(
        TAG,
        FILENAME,
        VISUAL,
        DURATION,
        RESOLUTION,
        BEHAVIOR,
    )
}

fun normalizeSimilarRecommendationReasons(rawReasons: Iterable<String>): List<String> {
    return rawReasons
        .mapNotNull { rawReason -> rawReason.toSafeSimilarRecommendationReasonOrNull() }
        .distinct()
}

fun filterSimilarRecommendationsForCurrentScene(
    currentSceneId: String,
    recommendations: List<SimilarSceneRecommendation>,
): List<SimilarSceneRecommendation> {
    val normalizedCurrentSceneId = currentSceneId.trim()
    if (normalizedCurrentSceneId.isBlank()) return recommendations
    return recommendations.filterNot { recommendation ->
        recommendation.sceneId == normalizedCurrentSceneId || recommendation.scene.id == normalizedCurrentSceneId
    }
}

private fun String.toSafeSimilarRecommendationReasonOrNull(): String? {
    val trimmed = trim()
    if (trimmed in SimilarRecommendationReasonLabels.allowed) return trimmed

    return when (trimmed.lowercase().replace('-', '_').replace(' ', '_')) {
        "tag", "tags", "tag_similarity", "tag_similar", "shared_tag", "shared_tags" ->
            SimilarRecommendationReasonLabels.TAG
        "filename", "file_name", "filename_similarity", "file_name_similarity", "filename_similar" ->
            SimilarRecommendationReasonLabels.FILENAME
        "visual", "image", "sprite", "visual_similarity", "image_similarity", "sprite_similarity" ->
            SimilarRecommendationReasonLabels.VISUAL
        "duration", "length", "duration_similarity", "length_similarity" ->
            SimilarRecommendationReasonLabels.DURATION
        "resolution", "quality", "resolution_match", "quality_match" ->
            SimilarRecommendationReasonLabels.RESOLUTION
        "behavior", "taste", "playback", "behavior_signal", "taste_signal" ->
            SimilarRecommendationReasonLabels.BEHAVIOR
        else -> null
    }
}
