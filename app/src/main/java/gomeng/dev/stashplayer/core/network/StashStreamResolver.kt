package gomeng.dev.stashplayer.core.network

import android.net.Uri
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteResult
import gomeng.dev.stashplayer.core.model.StashSceneDeleteOptions
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarSceneSummary
import gomeng.dev.stashplayer.core.model.StashDateRange
import gomeng.dev.stashplayer.core.model.StashDurationRange
import gomeng.dev.stashplayer.core.model.StashMainTabSection
import gomeng.dev.stashplayer.core.model.StashMainTabSectionSpec
import gomeng.dev.stashplayer.core.model.StashMediaFormatFilter
import gomeng.dev.stashplayer.core.model.StashPlaybackState
import gomeng.dev.stashplayer.core.model.StashRatingRange
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.StashVideoFileType
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.StashVideoResolution
import gomeng.dev.stashplayer.core.model.nextStashRandomSortSeed
import gomeng.dev.stashplayer.core.model.normalizeStashRandomSortSeed
import gomeng.dev.stashplayer.core.model.defaultStashMainTabSections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class StashStream(
    val sceneId: String,
    val title: String,
    val uri: Uri,
    val requestHeaders: Map<String, String> = emptyMap(),
    val startPositionMs: Long = 0L,
    val sourceCategory: StashStreamSourceCategory = StashStreamSourceCategory.Unknown,
    val sourceType: StashStreamSourceType = StashStreamSourceType.Unknown,
    val sourceLabel: String = sourceType.displayName,
    val sourceMimeType: String? = null,
    val sourceUrlExtensionHint: String = "stream",
    val sourceIsHlsManifest: Boolean = false,
    val streamCandidates: List<StashStreamCandidate> = emptyList(),
    val resolvedCandidates: List<ResolvedStashStreamCandidate> = emptyList(),
    val thumbnailUrl: String? = null,
    val spriteVttUrl: String? = null,
    val spriteImageUrl: String? = null,
    val spriteFrames: List<StashSpriteFrame> = emptyList(),
    val captionBaseUrl: String? = null,
    val captionTracks: List<StashCaptionTrack> = emptyList(),
    val durationSeconds: Double? = null,
    val studioName: String? = null,
    val playCount: Int? = null,
    val oCounter: Int? = null,
    val rating100: Int? = null,
    val fileName: String? = null,
    val path: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val tags: List<gomeng.dev.stashplayer.core.model.SceneCardTagChip> = emptyList(),
)

data class StashCaptionTrack(
    val languageCode: String,
    val captionType: String,
    val url: String,
)

data class ResolvedStashStreamCandidate(
    val uri: Uri,
    val sourceCategory: StashStreamSourceCategory = StashStreamSourceCategory.Unknown,
    val sourceType: StashStreamSourceType,
    val sourceLabel: String,
    val mimeType: String? = null,
    val urlExtensionHint: String = "stream",
    val isHlsManifest: Boolean = false,
    val requestHeaders: Map<String, String> = emptyMap(),
)

interface StashStreamResolver {
    suspend fun resolve(sceneId: String): StashStream
}

class DevStashStreamResolver : StashStreamResolver {
    override suspend fun resolve(sceneId: String): StashStream = StashStream(
        sceneId = sceneId,
        title = stashString(R.string.auto_kr_0173, sceneId),
        uri = Uri.parse(DEMO_STREAM_URL),
        sourceType = StashStreamSourceType.Mp4,
        sourceLabel = "Demo MP4",
    )

    private companion object {
        val DEMO_STREAM_URL = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }
}

class GraphQlStashStreamResolver(
    private val client: StashGraphQlClient,
    private val profile: StashServerProfile,
) : StashStreamResolver {
    override suspend fun resolve(sceneId: String): StashStream {
        val scene = client.findScene(sceneId) ?: error("Scene $sceneId not found")
        val spriteFrames = scene.spriteVttUrl
            ?.let { vttUrl -> runCatching { client.fetchSpriteFrames(vttUrl) }.getOrDefault(emptyList()) }
            .orEmpty()
        return buildStashStream(profile, scene).copy(spriteFrames = spriteFrames)
    }
}

class StashGraphQlClient(
    private val profile: StashServerProfile,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) : StashPluginOperationsClient {
    suspend fun testConnection(): String {
        val json = execute(VERSION_QUERY)
        return parseVersionResponse(json)
    }

    suspend fun findScenes(
        perPage: Int = 25,
        page: Int = 1,
        query: String? = null,
        sort: String = "updated_at",
        direction: StashSortDirection = StashSortDirection.Desc,
        videoFilter: StashVideoFilterState = StashVideoFilterState(),
    ): List<SceneCardModel> = findSceneCardsPage(
        perPage = perPage,
        page = page,
        query = query,
        sort = sort,
        direction = direction,
        videoFilter = videoFilter,
    ).scenes

    suspend fun findSceneCardsPage(
        perPage: Int = 25,
        page: Int = 1,
        query: String? = null,
        sort: String = "updated_at",
        direction: StashSortDirection = StashSortDirection.Desc,
        videoFilter: StashVideoFilterState = StashVideoFilterState(),
    ): StashSceneCardPage {
        val variables = buildFindScenesVariables(
            perPage = perPage,
            page = page,
            query = query,
            sort = serverSortForStashVideoFilters(sort, videoFilter),
            direction = direction,
            videoFilter = videoFilter,
        )
        val parsedPage = parseFindScenesPageResponse(execute(FIND_SCENES_QUERY, variables))
        return parsedPage.copy(
            scenes = parsedPage.scenes.map { scene ->
                scene.copy(thumbnailUrl = scene.thumbnailUrl?.let(profile::authenticatedUrl))
            },
        )
    }

    suspend fun findMainTabSections(
        specs: List<StashMainTabSectionSpec> = defaultStashMainTabSections(),
    ): List<StashMainTabSection> {
        return specs.map { spec ->
            val requestedPerPage = if (spec.onlyResumable) spec.perPage.coerceAtLeast(30) else spec.perPage
            val scenes = findScenes(
                perPage = requestedPerPage,
                sort = spec.sort,
                direction = spec.direction,
            ).let { rows ->
                if (spec.onlyResumable) rows.filter { it.progress > 0f }.take(spec.perPage) else rows
            }
            StashMainTabSection(spec = spec, scenes = scenes)
        }.filter { it.shouldRender }
    }

    suspend fun findSimilarSceneRecommendations(sceneId: String, limit: Int = 10): List<SimilarSceneRecommendation> {
        val normalizedSceneId = sceneId.trim()
        if (normalizedSceneId.isBlank()) return emptyList()

        val currentScene = findScene(normalizedSceneId) ?: return emptyList()
        val safeLimit = limit.coerceIn(1, 50)
        val candidateLimit = (safeLimit * 4).coerceAtLeast(20).coerceAtMost(50)
        val currentTagIds = currentScene.tags.map { it.id }.filter { it.isNotBlank() }.distinct()
        val taggedCandidates = if (currentTagIds.isNotEmpty()) {
            val page = parseFindScenesPageResponse(
                execute(
                    FIND_SCENES_QUERY,
                    buildSimilarSceneCandidateVariables(
                        tagIds = currentTagIds,
                        perPage = candidateLimit,
                    ),
                ),
            )
            page.scenes.map { scene ->
                scene.copy(thumbnailUrl = scene.thumbnailUrl?.let(profile::authenticatedUrl))
            }
        } else {
            emptyList()
        }
        val fallbackCandidates = if (taggedCandidates.none { it.id != currentScene.id }) {
            findSceneCardsPage(perPage = (safeLimit + 1).coerceAtMost(50), sort = "updated_at").scenes
        } else {
            emptyList()
        }

        return buildSimilarRecommendationsFromSceneCards(
            currentScene = currentScene,
            candidateScenes = taggedCandidates + fallbackCandidates,
            limit = safeLimit,
        )
    }

    override suspend fun listPlugins(): List<StashPluginInfo> {
        return parseStashPluginsResponse(execute(PLUGINS_QUERY))
    }

    override suspend fun runPluginOperation(pluginId: String, args: Map<String, Any?>): String {
        return parseRunPluginOperationResponse(
            execute(
                RUN_PLUGIN_OPERATION_MUTATION,
                mapOf(
                    "pluginId" to pluginId,
                    "args" to args,
                ),
            ),
        )
    }

    suspend fun findScene(sceneId: String): StashScene? {
        return parseFindSceneResponse(execute(FIND_SCENE_QUERY, mapOf("id" to sceneId)))
    }

    suspend fun findTags(
        perPage: Int = 50,
        page: Int = 1,
        query: String? = null,
    ): List<StashSelectedTag> {
        return parseFindTagsResponse(execute(FIND_TAGS_QUERY, buildFindTagsVariables(perPage, page, query)))
    }

    suspend fun fetchSpriteFrames(vttUrl: String): List<StashSpriteFrame> = withContext(Dispatchers.IO) {
        val authenticatedVttUrl = profile.authenticatedUrl(vttUrl)
        val requestBuilder = Request.Builder().url(authenticatedVttUrl)
        if (profile.authHeadersFor(vttUrl).isNotEmpty()) {
            requestBuilder.header("ApiKey", profile.apiKey)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw IOException("Stash VTT HTTP ${response.code}: ${responseBody.take(240)}")
        }
        parseStashSpriteVtt(authenticatedVttUrl, responseBody)
    }

    suspend fun saveActivity(sceneId: String, resumeTimeSeconds: Double, playDurationSeconds: Double? = null) {
        val variables = buildMap<String, Any?> {
            put("id", sceneId)
            put("resume_time", resumeTimeSeconds)
            if (playDurationSeconds != null) put("playDuration", playDurationSeconds)
        }
        execute(SAVE_ACTIVITY_MUTATION, variables)
    }

    suspend fun addPlay(sceneId: String) {
        execute(ADD_PLAY_MUTATION, mapOf("id" to sceneId, "times" to null))
    }

    suspend fun addO(sceneId: String): Int {
        return parseSceneAddOResponse(execute(ADD_O_MUTATION, mapOf("id" to sceneId)))
    }

    suspend fun updateSceneRating(sceneId: String, rating100: Int?): Boolean {
        return parseSceneRatingUpdateResponse(execute(SCENE_UPDATE_MUTATION, buildSceneRatingUpdateVariables(sceneId, rating100)))
    }

    suspend fun deleteScenes(
        sceneIds: List<String>,
        deleteOptions: StashSceneDeleteOptions = StashSceneDeleteOptions(),
    ): SceneBulkDeleteResult {
        val requestedIds = sceneIds.distinct()
        if (requestedIds.isEmpty()) {
            return SceneBulkDeleteResult(
                requestedSceneIds = emptyList(),
                deletedSceneIds = emptySet(),
            )
        }

        val deleted = runCatching {
            parseScenesDestroyResponse(execute(SCENES_DESTROY_MUTATION, buildScenesDestroyVariables(requestedIds, deleteOptions)))
        }
        if (deleted.getOrDefault(false)) {
            return SceneBulkDeleteResult(
                requestedSceneIds = requestedIds,
                deletedSceneIds = requestedIds.toSet(),
            )
        }

        val failureMessage = deleted.exceptionOrNull()?.message
            ?.let(::redactStashCredentialText)
            ?: stashString(R.string.auto_kr_0174)
        return SceneBulkDeleteResult(
            requestedSceneIds = requestedIds,
            deletedSceneIds = emptySet(),
            failedSceneIds = requestedIds.associateWith { failureMessage },
        )
    }

    private suspend fun execute(query: String, variables: Map<String, Any?> = emptyMap()): String = withContext(Dispatchers.IO) {
        require(profile.isConfigured()) { "Stash server is not configured" }
        val bodyJson = JSONObject()
            .put("query", query)
            .put("variables", JSONObject(variables))
            .toString()
        val requestBuilder = Request.Builder()
            .url(profile.graphQlUrl())
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
            .header("Content-Type", "application/json")
        profile.authHeadersFor(profile.graphQlUrl()).forEach { (name, value) ->
            requestBuilder.header(name, value)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw IOException(redactStashCredentialText("Stash GraphQL HTTP ${response.code}: ${responseBody.take(240)}"))
        }
        responseBody
    }

    internal companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val VERSION_QUERY = """
            query Version { version { version } }
        """

        val FIND_SCENES_QUERY = """
            query FindScenes(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!, ${'$'}sceneFilter: SceneFilterType) {
              findScenes(filter: { per_page: ${'$'}perPage, page: ${'$'}page, sort: ${'$'}sort, direction: ${'$'}direction, q: ${'$'}q }, scene_filter: ${'$'}sceneFilter) {
                count
                scenes {
                  id
                  title
                  resume_time
                  play_count
                  studio { name }
                  files { duration width height path basename }
                  paths { screenshot preview webp sprite }
                  tags { id name }
                }
              }
            }
        """

        val FIND_SCENE_QUERY = """
            query FindScene(${'$'}id: ID!) {
              findScene(id: ${'$'}id) {
                id
                title
                rating100
                resume_time
                play_count
                o_counter
                studio { name }
                files { duration width height path basename }
                paths { stream screenshot vtt sprite caption }
                captions { language_code caption_type }
                tags { id name }
                sceneStreams { url mime_type label }
              }
            }
        """

        val FIND_TAGS_QUERY = """
            query FindTags(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!) {
              findTags(filter: { per_page: ${'$'}perPage, page: ${'$'}page, q: ${'$'}q, sort: ${'$'}sort, direction: ${'$'}direction }) {
                count
                tags {
                  id
                  name
                }
              }
            }
        """

        val PLUGINS_QUERY = """
            query Plugins {
              plugins {
                id
                name
                enabled
              }
            }
        """

        val RUN_PLUGIN_OPERATION_MUTATION = """
            mutation RunPluginOperation(${'$'}pluginId: ID!, ${'$'}args: Map!) {
              runPluginOperation(plugin_id: ${'$'}pluginId, args: ${'$'}args)
            }
        """

        val SAVE_ACTIVITY_MUTATION = """
            mutation SceneSaveActivity(${'$'}id: ID!, ${'$'}resume_time: Float, ${'$'}playDuration: Float) {
              sceneSaveActivity(id: ${'$'}id, resume_time: ${'$'}resume_time, playDuration: ${'$'}playDuration)
            }
        """

        val ADD_PLAY_MUTATION = """
            mutation SceneAddPlay(${'$'}id: ID!, ${'$'}times: [Timestamp!]) {
              sceneAddPlay(id: ${'$'}id, times: ${'$'}times) { count history }
            }
        """

        val ADD_O_MUTATION = """
            mutation SceneAddO(${'$'}id: ID!) {
              sceneAddO(id: ${'$'}id) { count history }
            }
        """

        val SCENE_UPDATE_MUTATION = """
            mutation SceneUpdate(${'$'}input: SceneUpdateInput!) {
              sceneUpdate(input: ${'$'}input) { id rating100 o_counter }
            }
        """

        val SCENES_DESTROY_MUTATION = """
            mutation ScenesDestroy(${'$'}input: ScenesDestroyInput!) {
              scenesDestroy(input: ${'$'}input)
            }
        """
    }
}

internal fun buildFindScenesVariables(
    perPage: Int,
    page: Int,
    query: String?,
    sort: String,
    direction: StashSortDirection,
    videoFilter: StashVideoFilterState = StashVideoFilterState(),
): Map<String, Any?> = buildMap {
    put("perPage", perPage)
    put("page", page.coerceAtLeast(1))
    put("q", query?.trim()?.takeIf { it.isNotBlank() })
    put("sort", sort)
    put("direction", direction.graphQlValue)
    put("sceneFilter", videoFilter.toGraphQlSceneFilterOrNull())
}

internal fun buildSimilarSceneCandidateVariables(
    tagIds: List<String>,
    perPage: Int,
): Map<String, Any?> = buildMap {
    val normalizedTagIds = tagIds.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    put("perPage", perPage.coerceIn(1, 50))
    put("page", 1)
    put("q", null)
    put("sort", "updated_at")
    put("direction", StashSortDirection.Desc.graphQlValue)
    put(
        "sceneFilter",
        normalizedTagIds.takeIf { it.isNotEmpty() }?.let { ids ->
            mapOf(
                "tags" to mapOf(
                    "value" to ids,
                    "modifier" to "INCLUDES",
                ),
            )
        },
    )
}

internal fun buildSimilarRecommendationsFromSceneCards(
    currentScene: StashScene,
    candidateScenes: List<SceneCardModel>,
    limit: Int,
): List<SimilarSceneRecommendation> {
    val safeLimit = limit.coerceIn(1, 50)
    val currentTagIds = currentScene.tags.map { it.id }.filter { it.isNotBlank() }.toSet()
    val currentTagLabelsById = currentScene.tags.associate { it.id to it.label }
    val currentStudio = currentScene.studioName?.trim()?.takeIf { it.isNotBlank() }

    return candidateScenes.asSequence()
        .filter { candidate -> candidate.id != currentScene.id }
        .distinctBy { it.id }
        .map { candidate ->
            val sharedTags = candidate.tagChips.filter { tag -> tag.id in currentTagIds }
            val sharedTagScore = if (currentTagIds.isNotEmpty()) {
                sharedTags.size.toDouble() / currentTagIds.size.toDouble()
            } else {
                0.0
            }
            val sameStudio = currentStudio != null && candidate.studio == currentStudio
            val score = when {
                sharedTags.isNotEmpty() -> 0.55 + sharedTagScore * 0.35 + if (sameStudio) 0.05 else 0.0
                sameStudio -> 0.45
                else -> 0.30
            }.coerceIn(0.0, 0.98)
            SimilarSceneRecommendation(
                sceneId = candidate.id,
                scene = SimilarSceneSummary(
                    id = candidate.id,
                    title = candidate.title,
                    thumbnailUrl = candidate.thumbnailUrl,
                    playCount = candidate.playCount,
                ),
                score = score,
                reasons = buildSimilarRecommendationReasons(
                    sharedTags = sharedTags.map { tag -> currentTagLabelsById[tag.id] ?: tag.label },
                    sameStudio = sameStudio,
                ),
            )
        }
        .sortedWith(compareByDescending<SimilarSceneRecommendation> { it.score }.thenBy { it.scene.title })
        .take(safeLimit)
        .toList()
}

private fun buildSimilarRecommendationReasons(
    sharedTags: List<String>,
    sameStudio: Boolean,
): List<String> = buildList {
    sharedTags.take(2).forEach { tag -> add(stashString(R.string.auto_kr_0175, tag)) }
    if (sameStudio) add(stashString(R.string.auto_kr_0176))
    if (isEmpty()) add(stashString(R.string.auto_kr_0048))
}

internal fun buildFindTagsVariables(
    perPage: Int,
    page: Int,
    query: String?,
): Map<String, Any?> = mapOf(
    "perPage" to perPage,
    "page" to page.coerceAtLeast(1),
    "q" to query?.trim()?.takeIf { it.isNotBlank() },
    "sort" to "name",
    "direction" to StashSortDirection.Asc.graphQlValue,
)

internal fun serverSortForStashVideoFilters(
    baseSort: String,
    videoFilter: StashVideoFilterState,
    randomSeed: Int? = null,
    supportsRandomSort: Boolean = true,
): String {
    val isRandomRequested = videoFilter.randomShuffle || baseSort == "random"
    if (!supportsRandomSort || !isRandomRequested) return baseSort
    val seed = normalizeStashRandomSortSeed(
        randomSeed ?: videoFilter.randomShuffleSeed ?: if (baseSort == "random") 0 else nextStashRandomSortSeed(),
    )
    return "random_$seed"
}

private fun StashVideoFilterState.toGraphQlSceneFilterOrNull(): Map<String, Any?>? = buildMap {
    tags.takeIf { it.isNotEmpty() }?.let { selectedTags ->
        put(
            "tags",
            mapOf(
                "value" to selectedTags.map { it.id },
                "modifier" to "INCLUDES_ALL",
            ),
        )
    }
    dateRange?.toDateCriterionOrNull()?.let { put("date", it) }
    durationRange?.toIntCriterionOrNull()?.let { put("duration", it) }
    ratingRange?.toRatingCriterionOrNull()?.let { put("rating100", it) }
    playbackState?.let { state ->
        when (state) {
            StashPlaybackState.Watched -> put("play_count", mapOf("value" to 0, "modifier" to "GREATER_THAN"))
            StashPlaybackState.Unwatched -> put("play_count", mapOf("value" to 0, "modifier" to "EQUALS"))
            StashPlaybackState.Resumable -> put("resume_time", mapOf("value" to 0, "modifier" to "GREATER_THAN"))
        }
    }
    mediaFormat.toGraphQlCriteria().forEach { (key, value) -> put(key, value) }
}.takeIf { it.isNotEmpty() }

private fun StashDateRange.toDateCriterionOrNull(): Map<String, Any?>? = rangeCriterion(
    value = start?.takeIf { it.isNotBlank() },
    value2 = end?.takeIf { it.isNotBlank() },
)

private fun StashDurationRange.toIntCriterionOrNull(): Map<String, Any?>? = rangeCriterion(
    value = minSeconds,
    value2 = maxSeconds,
)

private fun StashRatingRange.toRatingCriterionOrNull(): Map<String, Any?>? = when {
    min != null && max != null -> mapOf("value" to min, "value2" to max, "modifier" to "BETWEEN")
    min != null -> mapOf("value" to (min - 1).coerceAtLeast(0), "modifier" to "GREATER_THAN")
    max != null -> mapOf("value" to (max + 1).coerceAtMost(101), "modifier" to "LESS_THAN")
    else -> null
}

private fun <T : Any> rangeCriterion(value: T?, value2: T?): Map<String, Any?>? = when {
    value != null && value2 != null -> mapOf("value" to value, "value2" to value2, "modifier" to "BETWEEN")
    value != null -> mapOf("value" to value, "modifier" to "GREATER_THAN")
    value2 != null -> mapOf("value" to value2, "modifier" to "LESS_THAN")
    else -> null
}

private fun StashMediaFormatFilter.toGraphQlCriteria(): Map<String, Any?> = buildMap {
    resolution?.toGraphQlAtLeastResolutionCriterion()?.let { criterion ->
        put("resolution", criterion)
    }
    fileTypes.takeIf { it.isNotEmpty() }?.let { types ->
        put("path", mapOf("value" to types.toFileExtensionRegex(), "modifier" to "MATCHES_REGEX"))
    }
}

private fun StashVideoResolution.toGraphQlAtLeastResolutionCriterion(): Map<String, Any?> = when (this) {
    StashVideoResolution.P480 -> mapOf("value" to "STANDARD", "modifier" to "EQUALS")
    StashVideoResolution.P720 -> mapOf("value" to "STANDARD", "modifier" to "GREATER_THAN")
    StashVideoResolution.P1080 -> mapOf("value" to "STANDARD_HD", "modifier" to "GREATER_THAN")
    StashVideoResolution.P1440 -> mapOf("value" to "FULL_HD", "modifier" to "GREATER_THAN")
    StashVideoResolution.P4K -> mapOf("value" to "QUAD_HD", "modifier" to "GREATER_THAN")
}

private fun List<StashVideoFileType>.toFileExtensionRegex(): String = joinToString(
    separator = "|",
    prefix = """(?i)\.(""",
    postfix = ")$",
) { it.id.lowercase() }

internal fun findSceneQueryForTesting(): String = StashGraphQlClient.FIND_SCENE_QUERY

internal fun findScenesQueryForTesting(): String = StashGraphQlClient.FIND_SCENES_QUERY

internal fun findTagsQueryForTesting(): String = StashGraphQlClient.FIND_TAGS_QUERY
