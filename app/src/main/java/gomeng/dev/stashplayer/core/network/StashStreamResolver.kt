package gomeng.dev.stashplayer.core.network

import android.net.Uri
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteResult
import gomeng.dev.stashplayer.core.model.StashGalleryPage
import gomeng.dev.stashplayer.core.model.StashImagePage
import gomeng.dev.stashplayer.core.model.StashSceneDeleteOptions
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarSceneSummary
import gomeng.dev.stashplayer.core.model.StashDateRange
import gomeng.dev.stashplayer.core.model.StashDurationRange
import gomeng.dev.stashplayer.core.model.StashGalleryFilterState
import gomeng.dev.stashplayer.core.model.StashGalleryDetailModel
import gomeng.dev.stashplayer.core.model.StashGalleryNumberRange
import gomeng.dev.stashplayer.core.model.StashImageFileType
import gomeng.dev.stashplayer.core.model.StashImageFilterState
import gomeng.dev.stashplayer.core.model.StashMainTabSection
import gomeng.dev.stashplayer.core.model.StashMainTabSectionSpec
import gomeng.dev.stashplayer.core.model.StashMediaFormatFilter
import gomeng.dev.stashplayer.core.model.StashPlaybackState
import gomeng.dev.stashplayer.core.model.StashRatingRange
import gomeng.dev.stashplayer.core.model.StashSelectedEntity
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.normalizedGalleryEntities
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

    suspend fun findGalleryCardsPage(
        perPage: Int = 25,
        page: Int = 1,
        query: String? = null,
        sort: String = "updated_at",
        direction: StashSortDirection = StashSortDirection.Desc,
        galleryFilter: StashGalleryFilterState = StashGalleryFilterState(),
    ): StashGalleryPage {
        val parsedPage = parseFindGalleriesPageResponse(
            execute(
                FIND_GALLERIES_QUERY,
                buildFindGalleriesVariables(
                    perPage = perPage,
                    page = page,
                    query = query,
                    sort = sort,
                    direction = direction,
                    galleryFilter = galleryFilter,
                ),
            ),
        )
        return parsedPage.copy(
            galleries = parsedPage.galleries.map { gallery ->
                gallery.copy(
                    coverUrl = gallery.coverUrl?.let(profile::authenticatedUrl),
                    previewUrl = gallery.previewUrl?.let(profile::authenticatedUrl),
                )
            },
        )
    }

    suspend fun findGalleryImagesPage(
        galleryId: String,
        perPage: Int = 50,
        page: Int = 1,
        sort: String = "title",
        direction: StashSortDirection = StashSortDirection.Asc,
    ): StashImagePage {
        val parsedPage = parseFindImagesPageResponse(
            execute(
                FIND_GALLERY_IMAGES_QUERY,
                buildFindGalleryImagesVariables(
                    galleryId = galleryId,
                    perPage = perPage,
                    page = page,
                    sort = sort,
                    direction = direction,
                ),
            ),
        )
        return parsedPage.copy(
            images = parsedPage.images.map { image ->
                image.copy(
                    thumbnailUrl = image.thumbnailUrl?.let(profile::authenticatedUrl),
                    previewUrl = image.previewUrl?.let(profile::authenticatedUrl),
                    imageUrl = image.imageUrl?.let(profile::authenticatedUrl),
                )
            },
        )
    }

    suspend fun findImagesPage(
        perPage: Int = 50,
        page: Int = 1,
        query: String? = null,
        sort: String = "title",
        direction: StashSortDirection = StashSortDirection.Asc,
        imageFilter: StashImageFilterState = StashImageFilterState(),
    ): StashImagePage {
        val parsedPage = parseFindImagesPageResponse(
            execute(
                FIND_GALLERY_IMAGES_QUERY,
                buildFindImagesVariables(
                    perPage = perPage,
                    page = page,
                    query = query,
                    sort = sort,
                    direction = direction,
                    imageFilter = imageFilter,
                ),
            ),
        )
        return parsedPage.copy(
            images = parsedPage.images.map { image ->
                image.copy(
                    thumbnailUrl = image.thumbnailUrl?.let(profile::authenticatedUrl),
                    previewUrl = image.previewUrl?.let(profile::authenticatedUrl),
                    imageUrl = image.imageUrl?.let(profile::authenticatedUrl),
                )
            },
        )
    }

    suspend fun findGalleryDetail(galleryId: String): StashGalleryDetailModel? {
        val detail = parseFindGalleryDetailResponse(
            execute(
                FIND_GALLERY_DETAIL_QUERY,
                buildFindGalleryDetailVariables(galleryId),
            ),
        ) ?: return null
        return detail.copy(
            gallery = detail.gallery.copy(
                coverUrl = detail.gallery.coverUrl?.let(profile::authenticatedUrl),
                previewUrl = detail.gallery.previewUrl?.let(profile::authenticatedUrl),
            ),
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

    suspend fun findStudios(
        perPage: Int = 50,
        page: Int = 1,
        query: String? = null,
    ): List<StashSelectedEntity> {
        return parseFindStudiosResponse(execute(FIND_STUDIOS_QUERY, buildFindTagsVariables(perPage, page, query)))
    }

    suspend fun findPerformers(
        perPage: Int = 50,
        page: Int = 1,
        query: String? = null,
    ): List<StashSelectedEntity> {
        return parseFindPerformersResponse(execute(FIND_PERFORMERS_QUERY, buildFindTagsVariables(perPage, page, query)))
    }

    suspend fun findSceneEntities(
        perPage: Int = 50,
        page: Int = 1,
        query: String? = null,
    ): List<StashSelectedEntity> {
        return parseFindSceneEntitiesResponse(
            execute(
                FIND_SCENES_QUERY,
                buildFindScenesVariables(
                    perPage = perPage,
                    page = page,
                    query = query,
                    sort = "title",
                    direction = StashSortDirection.Asc,
                ),
            ),
        )
    }

    suspend fun ensureShortsTagOnScene(scene: SceneCardModel): Boolean {
        val existingShortsTag = scene.tagChips.firstOrNull { it.label.equals(SHORTS_TAG_NAME, ignoreCase = true) }
            ?: findTags(perPage = 10, query = SHORTS_TAG_NAME).firstOrNull { it.name.equals(SHORTS_TAG_NAME, ignoreCase = true) }
                ?.let { tag -> gomeng.dev.stashplayer.core.model.SceneCardTagChip(id = tag.id, label = tag.name) }
            ?: parseTagCreateResponse(execute(TAG_CREATE_MUTATION, buildTagCreateVariables(SHORTS_TAG_NAME)))
        if (existingShortsTag.id.isBlank()) return false
        if (scene.tagChips.any { it.id == existingShortsTag.id }) return true
        return parseSceneTagsUpdateResponse(
            execute(
                SCENE_TAGS_UPDATE_MUTATION,
                buildSceneTagUpdateVariables(
                    sceneId = scene.id,
                    existingTagIds = scene.tagChips.map { it.id },
                    shortsTagId = existingShortsTag.id,
                ),
            ),
        )
    }

    suspend fun fetchSpriteFrames(vttUrl: String): List<StashSpriteFrame> = withContext(Dispatchers.IO) {
        val authenticatedVttUrl = profile.authenticatedUrl(vttUrl)
        val requestBuilder = Request.Builder().url(authenticatedVttUrl)
        profile.authHeadersFor(authenticatedVttUrl).forEach { (name, value) ->
            requestBuilder.header(name, value)
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

    suspend fun updateImageRating(imageId: String, rating100: Int?): Boolean {
        return parseImageRatingUpdateResponse(execute(IMAGE_UPDATE_MUTATION, buildImageRatingUpdateVariables(imageId, rating100)))
    }

    suspend fun incrementImageO(imageId: String): Int {
        return parseImageOCounterMutationResponse(
            execute(IMAGE_INCREMENT_O_MUTATION, mapOf("id" to imageId.trim())),
            "imageIncrementO",
        )
    }

    suspend fun decrementImageO(imageId: String): Int {
        return parseImageOCounterMutationResponse(
            execute(IMAGE_DECREMENT_O_MUTATION, mapOf("id" to imageId.trim())),
            "imageDecrementO",
        )
    }

    suspend fun resetImageO(imageId: String): Int {
        return parseImageOCounterMutationResponse(
            execute(IMAGE_RESET_O_MUTATION, mapOf("id" to imageId.trim())),
            "imageResetO",
        )
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
                  files {
                    ... on VideoFile {
                      duration
                      width
                      height
                      path
                      basename
                    }
                  }
                  paths { screenshot preview webp sprite }
                  tags { id name }
                }
              }
            }
        """

        val FIND_GALLERIES_QUERY = """
            query FindGalleries(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!, ${'$'}galleryFilter: GalleryFilterType) {
              findGalleries(filter: { per_page: ${'$'}perPage, page: ${'$'}page, q: ${'$'}q, sort: ${'$'}sort, direction: ${'$'}direction }, gallery_filter: ${'$'}galleryFilter) {
                count
                galleries {
                  id
                  title
                  date
                  rating100
                  image_count
                  details
                  organized
                  studio { name }
                  tags { id name }
                  performers { id name }
                  scenes { id title }
                  files {
                    path
                    basename
                    size
                  }
                  paths { cover preview }
                }
              }
            }
        """

        val FIND_GALLERY_IMAGES_QUERY = """
            query FindGalleryImages(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!, ${'$'}imageFilter: ImageFilterType) {
              findImages(filter: { per_page: ${'$'}perPage, page: ${'$'}page, q: ${'$'}q, sort: ${'$'}sort, direction: ${'$'}direction }, image_filter: ${'$'}imageFilter) {
                count
                images {
                  id
                  title
                  date
                  rating100
                  details
                  photographer
                  organized
                  o_counter
                  paths { thumbnail preview image }
                  visual_files {
                    ... on ImageFile {
                      path
                      basename
                      width
                      height
                      size
                    }
                  }
                  studio { name }
                  tags { id name }
                  performers { id name }
                  galleries { id title }
                }
              }
            }
        """

        val FIND_GALLERY_DETAIL_QUERY = """
            query FindGalleryDetail(${'$'}id: ID!) {
              findGallery(id: ${'$'}id) {
                id
                title
                date
                rating100
                image_count
                details
                code
                photographer
                organized
                studio { name }
                tags { id name }
                performers { id name }
                scenes { id title }
                files {
                  path
                  basename
                  size
                }
                chapters {
                  id
                  title
                  image_index
                }
                paths { cover preview }
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
                files {
                  ... on VideoFile {
                    duration
                    width
                    height
                    path
                    basename
                  }
                }
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

        val FIND_STUDIOS_QUERY = """
            query FindStudios(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!) {
              findStudios(filter: { per_page: ${'$'}perPage, page: ${'$'}page, q: ${'$'}q, sort: ${'$'}sort, direction: ${'$'}direction }) {
                count
                studios {
                  id
                  name
                }
              }
            }
        """

        val FIND_PERFORMERS_QUERY = """
            query FindPerformers(${'$'}perPage: Int!, ${'$'}page: Int!, ${'$'}q: String, ${'$'}sort: String!, ${'$'}direction: SortDirectionEnum!) {
              findPerformers(filter: { per_page: ${'$'}perPage, page: ${'$'}page, q: ${'$'}q, sort: ${'$'}sort, direction: ${'$'}direction }) {
                count
                performers {
                  id
                  name
                }
              }
            }
        """

        val TAG_CREATE_MUTATION = """
            mutation TagCreate(${'$'}input: TagCreateInput!) {
              tagCreate(input: ${'$'}input) { id name }
            }
        """

        val SCENE_TAGS_UPDATE_MUTATION = """
            mutation SceneTagsUpdate(${'$'}input: SceneUpdateInput!) {
              sceneUpdate(input: ${'$'}input) { id tags { id name } }
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

        val IMAGE_UPDATE_MUTATION = """
            mutation ImageUpdate(${'$'}input: ImageUpdateInput!) {
              imageUpdate(input: ${'$'}input) { id rating100 o_counter }
            }
        """

        val IMAGE_INCREMENT_O_MUTATION = """
            mutation ImageIncrementO(${'$'}id: ID!) {
              imageIncrementO(id: ${'$'}id)
            }
        """

        val IMAGE_DECREMENT_O_MUTATION = """
            mutation ImageDecrementO(${'$'}id: ID!) {
              imageDecrementO(id: ${'$'}id)
            }
        """

        val IMAGE_RESET_O_MUTATION = """
            mutation ImageResetO(${'$'}id: ID!) {
              imageResetO(id: ${'$'}id)
            }
        """

        val SCENES_DESTROY_MUTATION = """
            mutation ScenesDestroy(${'$'}input: ScenesDestroyInput!) {
              scenesDestroy(input: ${'$'}input)
            }
        """

        const val SHORTS_TAG_NAME = "shorts"
    }
}

internal fun buildTagCreateVariables(name: String): Map<String, Any?> = mapOf(
    "input" to mapOf("name" to name.trim().ifBlank { StashGraphQlClient.SHORTS_TAG_NAME }),
)

internal fun buildSceneTagUpdateVariables(
    sceneId: String,
    existingTagIds: List<String>,
    shortsTagId: String,
): Map<String, Any?> = mapOf(
    "input" to mapOf(
        "id" to sceneId,
        "tag_ids" to (existingTagIds + shortsTagId)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct(),
    ),
)

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

internal fun buildFindGalleriesVariables(
    perPage: Int,
    page: Int,
    query: String?,
    sort: String,
    direction: StashSortDirection,
    galleryFilter: StashGalleryFilterState = StashGalleryFilterState(),
): Map<String, Any?> = buildMap {
    put("perPage", perPage)
    put("page", page.coerceAtLeast(1))
    put("q", query?.trim()?.takeIf { it.isNotBlank() })
    put("sort", sort)
    put("direction", direction.graphQlValue)
    put("galleryFilter", galleryFilter.toGraphQlGalleryFilterOrNull())
}

internal fun buildFindGalleryImagesVariables(
    galleryId: String,
    perPage: Int,
    page: Int,
    sort: String,
    direction: StashSortDirection,
): Map<String, Any?> = buildFindImagesVariables(
    perPage = perPage,
    page = page,
    query = null,
    sort = sort,
    direction = direction,
    imageFilter = StashImageFilterState(
        galleries = listOf(StashSelectedEntity(id = galleryId.trim(), name = galleryId.trim())),
    ),
)

internal fun buildFindImagesVariables(
    perPage: Int,
    page: Int,
    query: String?,
    sort: String,
    direction: StashSortDirection,
    imageFilter: StashImageFilterState = StashImageFilterState(),
): Map<String, Any?> = mapOf(
    "perPage" to perPage,
    "page" to page.coerceAtLeast(1),
    "q" to query?.trim()?.takeIf { it.isNotBlank() },
    "sort" to sort,
    "direction" to direction.graphQlValue,
    "imageFilter" to imageFilter.toGraphQlImageFilterOrNull(),
)

internal fun buildFindGalleryDetailVariables(galleryId: String): Map<String, Any?> = mapOf(
    "id" to galleryId.trim(),
)

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

private fun StashGalleryFilterState.toGraphQlGalleryFilterOrNull(): Map<String, Any?>? = buildMap {
    text.title.toTextCriterionOrNull()?.let { put("title", it) }
    text.details.toTextCriterionOrNull()?.let { put("details", it) }
    text.code.toTextCriterionOrNull()?.let { put("code", it) }
    text.photographer.toTextCriterionOrNull()?.let { put("photographer", it) }
    text.path.toTextCriterionOrNull()?.let { put("path", it) }
    text.url.toTextCriterionOrNull()?.let { put("url", it) }
    text.checksum.toTextCriterionOrNull()?.let { put("checksum", it) }
    dateRange?.toDateCriterionOrNull()?.let { put("date", it) }
    createdAtRange?.toDateCriterionOrNull()?.let { put("created_at", it) }
    updatedAtRange?.toDateCriterionOrNull()?.let { put("updated_at", it) }
    ratingRange?.toRatingCriterionOrNull()?.let { put("rating100", it) }
    organized?.let { put("organized", it) }
    isZip?.let { put("is_zip", it) }
    hasChapters?.let { put("has_chapters", it) }
    imageCountRange?.toIntCriterionOrNull()?.let { put("image_count", it) }
    fileCountRange?.toIntCriterionOrNull()?.let { put("file_count", it) }
    tagCountRange?.toIntCriterionOrNull()?.let { put("tag_count", it) }
    averageResolution?.toGraphQlAtLeastResolutionCriterion()?.let { put("average_resolution", it) }
    tags.toGalleryEntityCriterionOrNull()?.let { put("tags", it) }
    studios.toGalleryEntityCriterionOrNull()?.let { put("studios", it) }
    performers.toGalleryEntityCriterionOrNull()?.let { put("performers", it) }
    scenes.toGalleryEntityCriterionOrNull()?.let { put("scenes", it) }
    parentFolders.toGalleryEntityCriterionOrNull()?.let { put("parent_folder", it) }
}.takeIf { it.isNotEmpty() }

private fun StashImageFilterState.toGraphQlImageFilterOrNull(): Map<String, Any?>? = buildMap {
    text.title.toTextCriterionOrNull()?.let { put("title", it) }
    text.details.toTextCriterionOrNull()?.let { put("details", it) }
    text.code.toTextCriterionOrNull()?.let { put("code", it) }
    text.photographer.toTextCriterionOrNull()?.let { put("photographer", it) }
    text.path.toTextCriterionOrNull()?.let { put("path", it) }
    text.url.toTextCriterionOrNull()?.let { put("url", it) }
    text.checksum.toTextCriterionOrNull()?.let { put("checksum", it) }
    dateRange?.toDateCriterionOrNull()?.let { put("date", it) }
    createdAtRange?.toDateCriterionOrNull()?.let { put("created_at", it) }
    updatedAtRange?.toDateCriterionOrNull()?.let { put("updated_at", it) }
    ratingRange?.toRatingCriterionOrNull()?.let { put("rating100", it) }
    organized?.let { put("organized", it) }
    oCounterRange?.toIntCriterionOrNull()?.let { put("o_counter", it) }
    resolution?.toGraphQlAtLeastResolutionCriterion()?.let { put("resolution", it) }
    orientations.takeIf { it.isNotEmpty() }?.let { values ->
        put("orientation", mapOf("value" to values.distinct().map { it.serverValue }))
    }
    val fileTypePathCriterion = fileTypes.takeIf { it.isNotEmpty() }?.let { types ->
        mapOf("value" to types.distinct().toImageFileExtensionRegex(), "modifier" to "MATCHES_REGEX")
    }
    if (fileTypePathCriterion != null) {
        if (containsKey("path")) {
            put("AND", mapOf("path" to fileTypePathCriterion))
        } else {
            put("path", fileTypePathCriterion)
        }
    }
    fileCountRange?.toIntCriterionOrNull()?.let { put("file_count", it) }
    tagCountRange?.toIntCriterionOrNull()?.let { put("tag_count", it) }
    performerCountRange?.toIntCriterionOrNull()?.let { put("performer_count", it) }
    performerAgeRange?.toIntCriterionOrNull()?.let { put("performer_age", it) }
    performerFavorite?.let { put("performer_favorite", it) }
    tags.toGalleryEntityCriterionOrNull()?.let { put("tags", it) }
    performerTags.toGalleryEntityCriterionOrNull()?.let { put("performer_tags", it) }
    studios.toGalleryEntityCriterionOrNull()?.let { put("studios", it) }
    performers.toGalleryEntityCriterionOrNull()?.let { put("performers", it) }
    galleries.toGalleryEntityCriterionOrNull()?.let { put("galleries", it) }
}.takeIf { it.isNotEmpty() }

private fun List<StashSelectedEntity>.toGalleryEntityCriterionOrNull(): Map<String, Any?>? {
    val ids = normalizedGalleryEntities().map { it.id }
    return ids.takeIf { it.isNotEmpty() }?.let { value ->
        mapOf("value" to value, "modifier" to "INCLUDES")
    }
}

private fun String.toTextCriterionOrNull(): Map<String, Any?>? = trim()
    .takeIf { it.isNotBlank() }
    ?.let { value -> mapOf("value" to value, "modifier" to "INCLUDES") }

private fun StashGalleryNumberRange.toIntCriterionOrNull(): Map<String, Any?>? = rangeCriterion(
    value = min,
    value2 = max,
)

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

private fun List<StashImageFileType>.toImageFileExtensionRegex(): String = joinToString(
    separator = "|",
    prefix = """(?i)\.(""",
    postfix = ")$",
) { it.id.lowercase() }

internal fun findSceneQueryForTesting(): String = StashGraphQlClient.FIND_SCENE_QUERY

internal fun findScenesQueryForTesting(): String = StashGraphQlClient.FIND_SCENES_QUERY

internal fun findGalleriesQueryForTesting(): String = StashGraphQlClient.FIND_GALLERIES_QUERY

internal fun findGalleryImagesQueryForTesting(): String = StashGraphQlClient.FIND_GALLERY_IMAGES_QUERY

internal fun findGalleryDetailQueryForTesting(): String = StashGraphQlClient.FIND_GALLERY_DETAIL_QUERY

internal fun imageUpdateMutationForTesting(): String = StashGraphQlClient.IMAGE_UPDATE_MUTATION

internal fun imageIncrementOMutationForTesting(): String = StashGraphQlClient.IMAGE_INCREMENT_O_MUTATION

internal fun imageDecrementOMutationForTesting(): String = StashGraphQlClient.IMAGE_DECREMENT_O_MUTATION

internal fun imageResetOMutationForTesting(): String = StashGraphQlClient.IMAGE_RESET_O_MUTATION

internal fun findTagsQueryForTesting(): String = StashGraphQlClient.FIND_TAGS_QUERY
