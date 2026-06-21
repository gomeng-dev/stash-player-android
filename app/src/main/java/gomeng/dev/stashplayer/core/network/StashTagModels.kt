package gomeng.dev.stashplayer.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.net.URI
import java.util.Locale

const val STASH_TAG_API_PORT = 7861
const val STASH_TAG_API_PATH = "/api/predict"
const val STASH_TAG_API_QUERY_THRESHOLD = 0.2f
const val STASH_TAG_DEFAULT_REVIEW_THRESHOLD = 0.4f
const val STASH_TAG_DEFAULT_FLOOR_THRESHOLD = 0.2f

private val STASH_TAG_API_TAG_NAME_ORDER = listOf(
    "Anal",
    "Vaginal Penetration",
    "Blow Job",
    "Doggy Style",
    "Cowgirl",
    "Reverse Cowgirl",
    "Side Fuck",
    "Seashell",
    "Gape",
    "Face Fuck",
    "Fingering",
    "Kneeling",
    "Butter Churner",
    "Table Top",
    "Double Penetration",
    "Missionary",
    "Scissoring",
    "Flatiron",
    "Pussy Licking",
    "Ass Licking",
    "Ball Licking",
    "Face Sitting",
    "Hand Job",
    "Tit Job",
    "69",
    "Kissing",
    "Dildo",
    "Cumshot",
)

data class StashTagPrediction(
    val name: String,
    val probability: Float,
    val frame: Float? = null,
    val timeSeconds: Float? = null,
)

data class StashTagSuggestionResult(
    val spriteUrl: String,
    val tagApiUrl: String,
    val predictions: List<StashTagPrediction>,
)

data class StashTagApplyResult(
    val addedTagNames: List<String>,
    val createdTagNames: List<String>,
)

data class StashKnownTag(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
)

class StashTagMissingGeneratedResourceException(
    message: String = "Stash Tag generated resources are missing or stale",
    cause: Throwable? = null,
) : IOException(message, cause)

enum class StashTagFailureGuidance {
    MissingGeneratedResources,
    Generic,
}

fun classifyStashTagFailureForGuidance(throwable: Throwable): StashTagFailureGuidance =
    if (throwable.isMissingGeneratedResourceFailure()) {
        StashTagFailureGuidance.MissingGeneratedResources
    } else {
        StashTagFailureGuidance.Generic
    }

private fun Throwable.isMissingGeneratedResourceFailure(): Boolean {
    if (this is StashTagMissingGeneratedResourceException) return true
    val text = buildString {
        append(message.orEmpty())
        cause?.message?.takeIf { it.isNotBlank() }?.let { causeMessage ->
            append('\n')
            append(causeMessage)
        }
    }.lowercase(Locale.ROOT)
    return "stash asset http 404" in text ||
        "no sprite found" in text ||
        "generate stash sprites first" in text
}

fun defaultStashTagApiUrl(profile: StashServerProfile): String {
    val base = runCatching { URI(profile.normalizedBaseUrl()) }.getOrNull()
    val scheme = base?.scheme?.takeIf { it.equals("https", ignoreCase = true) } ?: "http"
    val host = base?.host?.takeIf { it.isNotBlank() } ?: "127.0.0.1"
    return "$scheme://$host:$STASH_TAG_API_PORT$STASH_TAG_API_PATH"
}

fun deriveStashTagThumbsVttUrl(spriteUrl: String): String {
    val normalized = spriteUrl.trim()
    val replacements = listOf(
        "_sprite.jpg" to "_thumbs.vtt",
        "_sprite.jpeg" to "_thumbs.vtt",
        "_sprite.png" to "_thumbs.vtt",
    )
    val replacement = replacements.firstOrNull { (suffix, _) -> normalized.contains(suffix) }
        ?: error("Scene sprite URL does not look like a Stash sprite sheet")
    return normalized.replace(replacement.first, replacement.second)
}

private val stashTagMoshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private data class StashTagPredictEnvelope(
    val data: List<Map<String, StashTagPredictMeta>> = emptyList(),
)

private data class StashTagPredictMeta(
    val prob: Float? = null,
    val frame: Float? = null,
    val time: Float? = null,
)

fun buildStashTagPredictPayload(
    imageDataUrl: String,
    vttDataUrl: String,
    queryThreshold: Float = STASH_TAG_API_QUERY_THRESHOLD,
): String = "{\"data\":[${imageDataUrl.toJsonString()},${vttDataUrl.toJsonString()},${queryThreshold}]}"

fun parseStashTagPredictResponse(json: String): List<StashTagPrediction> {
    val envelope = stashTagMoshi.adapter(StashTagPredictEnvelope::class.java).fromJson(json)
    val matches = envelope?.data?.firstOrNull().orEmpty()
    return matches.mapNotNull { (name, meta) ->
        val normalizedName = name.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val probability = meta.prob ?: return@mapNotNull null
        StashTagPrediction(
            name = normalizedName,
            probability = probability.coerceIn(0f, 1f),
            frame = meta.frame,
            timeSeconds = meta.time,
        )
    }.sortedWith(compareBy<StashTagPrediction> { prediction ->
        STASH_TAG_API_TAG_NAME_ORDER.indexOf(prediction.name).takeIf { it >= 0 } ?: Int.MAX_VALUE
    }.thenBy { it.frame ?: Float.MAX_VALUE }.thenBy { it.name.lowercase(Locale.ROOT) }).toList()
}

fun filterStashTagPredictions(
    predictions: List<StashTagPrediction>,
    threshold: Float,
    floor: Float = STASH_TAG_DEFAULT_FLOOR_THRESHOLD,
): List<StashTagPrediction> {
    val normalizedFloor = floor.coerceIn(0f, 1f)
    val normalizedThreshold = threshold.coerceIn(normalizedFloor, 1f)
    val usable = predictions.filter { it.probability >= normalizedFloor }
    val strong = usable.filter { it.probability >= normalizedThreshold }
    if (strong.isNotEmpty()) return strong
    val maxProbability = usable.maxOfOrNull { it.probability } ?: return emptyList()
    return usable.filter { it.probability >= maxProbability - 0.000001f }
}

fun selectStashTagPredictionsForReview(
    predictions: List<StashTagPrediction>,
    threshold: Float,
    excludedTagNames: Set<String>,
): List<StashTagPrediction> {
    val excluded = excludedTagNames.map { it.trim().lowercase(Locale.ROOT) }.toSet()
    return filterStashTagPredictions(predictions, threshold)
        .filterNot { it.name.trim().lowercase(Locale.ROOT) in excluded }
}

fun excludeStashTagPrediction(
    excludedTagNames: Set<String>,
    tagName: String,
): Set<String> = tagName.trim()
    .takeIf { it.isNotBlank() }
    ?.let { excludedTagNames + it.lowercase(Locale.ROOT) }
    ?: excludedTagNames

private fun String.toJsonString(): String = buildString {
    append('"')
    for (char in this@toJsonString) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}
