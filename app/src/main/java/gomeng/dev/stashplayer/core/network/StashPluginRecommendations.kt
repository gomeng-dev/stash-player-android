package gomeng.dev.stashplayer.core.network

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private const val HYBRID_RECOMMENDATIONS_PLUGIN_ID_PREFIX = "StashHybridRecommendationsEngine"

private val pluginMoshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val anyMapAdapter = pluginMoshi.adapter<Map<String, Any?>>(
    Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
)

data class StashPluginInfo(
    val id: String,
    val name: String = "",
    val enabled: Boolean = false,
)

data class StashPluginRecommendationStatus(
    val ok: Boolean,
    val message: String? = null,
)

data class StashPluginSimilarSceneRecommendationsResponse(
    val ok: Boolean,
    val recommendations: List<SimilarSceneRecommendation>,
    val message: String? = null,
)

data class StashPluginRecommendationStatusCopy(
    val message: String,
    val isSuccess: Boolean,
)

sealed class StashPluginRecommendationStatusResult {
    data class Available(val pluginId: String) : StashPluginRecommendationStatusResult()
    data class Fallback(val reason: String? = null) : StashPluginRecommendationStatusResult()
}

interface StashPluginOperationsClient {
    suspend fun listPlugins(): List<StashPluginInfo>

    suspend fun runPluginOperation(pluginId: String, args: Map<String, Any?>): String
}

class StashPluginRecommendationStatusClient(
    private val operationsClient: StashPluginOperationsClient,
) {
    suspend fun check(): StashPluginRecommendationStatusResult {
        val pluginId = detectStashHybridRecommendationsPluginId(operationsClient.listPlugins())
            ?: return StashPluginRecommendationStatusResult.Fallback("Plugin missing or disabled")
        val status = runCatching {
            parseStashPluginRecommendationStatusResponse(
                operationsClient.runPluginOperation(
                    pluginId = pluginId,
                    args = buildStashPluginStatusArgs(),
                ),
            )
        }.getOrElse { throwable ->
            return StashPluginRecommendationStatusResult.Fallback(throwable.message)
        }
        return if (status.ok) {
            StashPluginRecommendationStatusResult.Available(pluginId)
        } else {
            StashPluginRecommendationStatusResult.Fallback(status.message)
        }
    }
}

fun detectStashHybridRecommendationsPluginId(plugins: List<StashPluginInfo>): String? {
    return plugins
        .asSequence()
        .filter { plugin -> plugin.enabled }
        .map { plugin -> plugin.id.trim() }
        .firstOrNull { pluginId -> pluginId.startsWith(HYBRID_RECOMMENDATIONS_PLUGIN_ID_PREFIX) }
}

fun buildStashPluginRecommendArgs(sceneId: String, limit: Int): Map<String, Any?> = linkedMapOf(
    "mode" to "recommend",
    "sceneId" to sceneId.trim(),
    "limit" to limit.coerceIn(1, 50).toString(),
)

fun buildStashPluginStatusArgs(): Map<String, Any?> = linkedMapOf(
    "mode" to "status",
)

fun parseStashPluginsResponse(json: String): List<StashPluginInfo> {
    val envelope = pluginMoshi.adapter(PluginsEnvelope::class.java).fromJson(json)
        ?: error("Empty plugins response")
    envelope.graphQlErrorMessageOrNull()?.let { error("GraphQL error: $it") }
    return envelope.data?.plugins.orEmpty().mapNotNull { plugin ->
        val id = plugin.id?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        StashPluginInfo(
            id = id,
            name = plugin.name?.trim().orEmpty(),
            enabled = plugin.enabled == true,
        )
    }
}

fun parseRunPluginOperationResponse(json: String): String {
    val envelope = pluginMoshi.adapter(RunPluginOperationEnvelope::class.java).fromJson(json)
        ?: error("Empty runPluginOperation response")
    envelope.graphQlErrorMessageOrNull()?.let { error("GraphQL error: $it") }
    val operation = envelope.data?.runPluginOperation
        ?: error("Missing runPluginOperation response")
    return when (operation) {
        is String -> operation
        is Map<*, *> -> anyMapAdapter.toJson(operation.stringKeyMap())
        is List<*> -> pluginMoshi.adapter(Any::class.java).toJson(operation)
        else -> operation.toString()
    }
}

fun parseStashPluginRecommendationStatusResponse(json: String): StashPluginRecommendationStatus {
    val payload = json.toPluginPayloadMap()
    return StashPluginRecommendationStatus(
        ok = payload.booleanValue("ok") ?: payload.booleanValue("success") ?: false,
        message = payload.stringValue("message"),
    )
}

fun parseStashPluginSimilarSceneRecommendationsResponse(
    json: String,
    currentSceneId: String,
    stashServerProfile: StashServerProfile,
): StashPluginSimilarSceneRecommendationsResponse {
    val payload = json.toPluginPayloadMap()
    val ok = payload.booleanValue("ok") ?: payload.booleanValue("success") ?: false
    val message = payload.stringValue("message")
    val recommendationsPayload = payload["recommendations"]
    val recommendationsJson = when (recommendationsPayload) {
        null -> anyMapAdapter.toJson(mapOf("recommendations" to emptyList<Any>()))
        is List<*> -> anyMapAdapter.toJson(mapOf("recommendations" to recommendationsPayload))
        else -> error("Invalid plugin recommendations payload")
    }
    return StashPluginSimilarSceneRecommendationsResponse(
        ok = ok,
        recommendations = parseSimilarSceneRecommendationsResponse(
            json = recommendationsJson,
            currentSceneId = currentSceneId,
            stashServerProfile = stashServerProfile,
        ),
        message = message,
    )
}

fun StashPluginRecommendationStatusResult.toSettingsStatusCopy(): StashPluginRecommendationStatusCopy = when (this) {
    is StashPluginRecommendationStatusResult.Available -> StashPluginRecommendationStatusCopy(
        message = stashString(R.string.auto_kr_0161),
        isSuccess = true,
    )
    is StashPluginRecommendationStatusResult.Fallback -> StashPluginRecommendationStatusCopy(
        message = stashString(R.string.auto_kr_0162),
        isSuccess = false,
    )
}

private fun String.toPluginPayloadMap(): Map<String, Any?> {
    return anyMapAdapter.fromJson(trim()) ?: error("Empty plugin response")
}

private fun Map<*, *>.stringKeyMap(): Map<String, Any?> = entries.associate { (key, value) ->
    key.toString() to value
}

private fun Map<String, Any?>.booleanValue(key: String): Boolean? {
    return when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull()
        else -> null
    }
}

private fun Map<String, Any?>.stringValue(key: String): String? {
    return (this[key] as? String)?.trim()?.takeIf { it.isNotBlank() }
}

private interface PluginGraphQlEnvelope {
    val errors: List<PluginGraphQlError>?
}

private fun PluginGraphQlEnvelope.graphQlErrorMessageOrNull(): String? {
    return errors?.firstOrNull()?.message?.trim()?.takeIf { it.isNotBlank() }
}

private data class PluginGraphQlError(val message: String? = null)

private data class PluginsEnvelope(
    val data: PluginsData? = null,
    override val errors: List<PluginGraphQlError>? = null,
) : PluginGraphQlEnvelope

private data class PluginsData(
    val plugins: List<PluginPayload> = emptyList(),
)

private data class PluginPayload(
    val id: String? = null,
    val name: String? = null,
    val enabled: Boolean? = null,
)

private data class RunPluginOperationEnvelope(
    val data: RunPluginOperationData? = null,
    override val errors: List<PluginGraphQlError>? = null,
) : PluginGraphQlEnvelope

private data class RunPluginOperationData(
    val runPluginOperation: Any? = null,
)
