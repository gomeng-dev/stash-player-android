package gomeng.dev.stashplayer.core.network

import android.net.Uri
import gomeng.dev.stashplayer.core.model.SceneCardMetadataBadge
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.SceneCardTagChip
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.normalizeStashVideoFilterText
import gomeng.dev.stashplayer.core.player.formatPlayerPosition
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.net.URLEncoder

private val stashMoshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

data class StashScene(
    val id: String,
    val title: String,
    val streamCandidates: List<StashStreamCandidate>,
    val screenshotUrl: String? = null,
    val spriteVttUrl: String? = null,
    val spriteImageUrl: String? = null,
    val studioName: String? = null,
    val durationSeconds: Double? = null,
    val resumeTimeSeconds: Double? = null,
    val playCount: Int? = null,
    val oCounter: Int? = null,
    val rating100: Int? = null,
    val fileName: String? = null,
    val path: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val tags: List<SceneCardTagChip> = emptyList(),
    val captionBaseUrl: String? = null,
    val captions: List<StashSceneCaption> = emptyList(),
) {
    val preferredStream: StashStreamCandidate?
        get() = streamCandidates.firstOrNull()

    val streamUrl: String
        get() = preferredStream?.url.orEmpty()

    val streamMimeType: String?
        get() = preferredStream?.mimeType

    val streamLabel: String?
        get() = preferredStream?.displayLabel

    val streamSourceType: StashStreamSourceType?
        get() = preferredStream?.sourceType

    val streamSourceCategory: StashStreamSourceCategory?
        get() = preferredStream?.sourceCategory
}

data class StashSceneCaption(
    val languageCode: String,
    val captionType: String,
)

fun parseFindSceneResponse(json: String): StashScene? {
    val envelope = parseJson<FindSceneEnvelope>(json)
    envelope.throwIfErrors()
    return envelope.data?.findScene?.toDomain()
}

data class StashSceneCardPage(
    val scenes: List<SceneCardModel>,
    val totalCount: Int,
)

fun parseFindScenesResponse(json: String): List<SceneCardModel> {
    return parseFindScenesPageResponse(json).scenes
}

fun parseFindScenesPageResponse(json: String): StashSceneCardPage {
    val envelope = parseJson<FindScenesEnvelope>(json)
    envelope.throwIfErrors()
    val result = envelope.data?.findScenes
    return StashSceneCardPage(
        scenes = result?.scenes.orEmpty().map { it.toCard() },
        totalCount = result?.count ?: result?.scenes.orEmpty().size,
    )
}

fun parseFindTagsResponse(json: String): List<StashSelectedTag> {
    val envelope = parseJson<FindTagsEnvelope>(json)
    envelope.throwIfErrors()
    return envelope.data?.findTags?.tags.orEmpty().mapNotNull { tag -> tag.toSelectedTagOrNull() }
}

fun parseVersionResponse(json: String): String {
    val envelope = parseJson<VersionEnvelope>(json)
    envelope.throwIfErrors()
    return envelope.data?.version?.version ?: "unknown"
}

fun buildStashStream(profile: StashServerProfile, scene: StashScene): StashStream {
    val selectedStream = scene.preferredStream ?: error("Scene ${scene.id} has no playable stream URL")
    val resolvedCandidates = scene.streamCandidates.map { candidate ->
        ResolvedStashStreamCandidate(
            uri = Uri.parse(profile.authenticatedUrl(candidate.url)),
            sourceCategory = candidate.sourceCategory,
            sourceType = candidate.sourceType,
            sourceLabel = candidate.displayLabel,
            mimeType = candidate.mimeType,
            urlExtensionHint = candidate.urlExtensionHint,
            isHlsManifest = candidate.isHlsManifest,
            requestHeaders = profile.authHeadersFor(candidate.url),
        )
    }
    return StashStream(
        sceneId = scene.id,
        title = scene.title,
        uri = Uri.parse(profile.authenticatedUrl(selectedStream.url)),
        requestHeaders = profile.authHeadersFor(selectedStream.url),
        startPositionMs = ((scene.resumeTimeSeconds ?: 0.0) * 1000.0).toLong().coerceAtLeast(0L),
        sourceCategory = selectedStream.sourceCategory,
        sourceType = selectedStream.sourceType,
        sourceLabel = selectedStream.displayLabel,
        sourceMimeType = selectedStream.mimeType,
        sourceUrlExtensionHint = selectedStream.urlExtensionHint,
        sourceIsHlsManifest = selectedStream.isHlsManifest,
        streamCandidates = scene.streamCandidates,
        resolvedCandidates = resolvedCandidates,
        thumbnailUrl = scene.screenshotUrl?.let(profile::authenticatedUrl),
        spriteVttUrl = scene.spriteVttUrl?.let(profile::authenticatedUrl),
        spriteImageUrl = scene.spriteImageUrl?.let(profile::authenticatedUrl),
        captionBaseUrl = scene.captionBaseUrl?.let(profile::absoluteUrl),
        captionTracks = buildStashCaptionTracks(profile, scene.captionBaseUrl, scene.captions),
        durationSeconds = scene.durationSeconds,
        studioName = scene.studioName,
        playCount = scene.playCount,
        oCounter = scene.oCounter,
        rating100 = scene.rating100,
        fileName = scene.fileName,
        path = scene.path,
        width = scene.width,
        height = scene.height,
        tags = scene.tags,
    )
}

fun buildStashCaptionTracks(
    profile: StashServerProfile,
    captionBaseUrl: String?,
    captions: List<StashSceneCaption>,
): List<StashCaptionTrack> {
    val baseUrl = captionBaseUrl?.trim()?.takeIf { it.isNotBlank() } ?: return emptyList()
    return captions.mapNotNull { caption ->
        val languageCode = caption.languageCode.trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val captionType = caption.captionType.trim().lowercase().takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val trackUrl = appendStashCaptionQuery(baseUrl, languageCode, captionType)
        StashCaptionTrack(
            languageCode = languageCode,
            captionType = captionType,
            url = profile.authenticatedUrl(trackUrl),
        )
    }
}

private fun appendStashCaptionQuery(baseUrl: String, languageCode: String, captionType: String): String {
    val separator = if (baseUrl.contains("?")) "&" else "?"
    return baseUrl +
        separator +
        "lang=${languageCode.urlEncodeForQuery()}" +
        "&type=${captionType.urlEncodeForQuery()}"
}

private fun String.urlEncodeForQuery(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

private inline fun <reified T> parseJson(json: String): T {
    return stashMoshi.adapter(T::class.java).fromJson(json)
        ?: error("Empty GraphQL response")
}

private fun GraphQlEnvelope.throwIfErrors() {
    val message = errors?.firstOrNull()?.message
    if (!message.isNullOrBlank()) error("GraphQL error: $message")
}

private interface GraphQlEnvelope {
    val errors: List<GraphQlError>?
}

private data class GraphQlError(val message: String)

private data class VersionEnvelope(
    val data: VersionData? = null,
    override val errors: List<GraphQlError>? = null,
) : GraphQlEnvelope

private data class VersionData(val version: VersionPayload? = null)
private data class VersionPayload(val version: String? = null)

private data class FindSceneEnvelope(
    val data: FindSceneData? = null,
    override val errors: List<GraphQlError>? = null,
) : GraphQlEnvelope

private data class FindSceneData(val findScene: ApiScene? = null)

private data class FindScenesEnvelope(
    val data: FindScenesData? = null,
    override val errors: List<GraphQlError>? = null,
) : GraphQlEnvelope

private data class FindScenesData(val findScenes: ApiFindScenes? = null)
private data class ApiFindScenes(
    val count: Int? = null,
    val scenes: List<ApiScene> = emptyList(),
)

private data class FindTagsEnvelope(
    val data: FindTagsData? = null,
    override val errors: List<GraphQlError>? = null,
) : GraphQlEnvelope

private data class FindTagsData(val findTags: ApiFindTags? = null)
private data class ApiFindTags(
    val count: Int? = null,
    val tags: List<ApiTag> = emptyList(),
)

private data class ApiTag(
    val id: String,
    val name: String? = null,
) {
    fun toSelectedTagOrNull(): StashSelectedTag? {
        val normalizedName = normalizeStashVideoFilterText(name.orEmpty()).takeIf { it.isNotBlank() } ?: return null
        return StashSelectedTag(id = id, name = normalizedName)
    }
}

private data class ApiScene(
    val id: String,
    val title: String? = null,
    val rating100: Int? = null,
    @Json(name = "resume_time") val resumeTime: Double? = null,
    @Json(name = "play_count") val playCount: Int? = null,
    @Json(name = "o_counter") val oCounter: Int? = null,
    val studio: ApiStudio? = null,
    val files: List<ApiVideoFile> = emptyList(),
    val paths: ApiScenePaths? = null,
    val sceneStreams: List<ApiSceneStream> = emptyList(),
    val tags: List<ApiTag> = emptyList(),
    val captions: List<ApiVideoCaption>? = null,
) {
    fun toDomain(): StashScene {
        val file = files.firstOrNull()
        val candidates = rankStashStreamCandidates(
            sceneStreams.mapNotNull { it.toCandidate() } +
                listOfNotNull(
                    paths?.stream
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            StashStreamCandidate(
                                url = it,
                                label = "Direct stream",
                                origin = StashStreamOrigin.PathStream,
                            )
                        },
                ),
        )
        require(candidates.isNotEmpty()) { "Scene $id has no playable stream URL" }
        return StashScene(
            id = id,
            title = title?.ifBlank { null } ?: stashString(R.string.auto_kr_0168, id),
            streamCandidates = candidates,
            screenshotUrl = paths?.screenshot,
            spriteVttUrl = paths?.vtt?.trim()?.takeIf { it.isNotBlank() },
            spriteImageUrl = paths?.sprite?.trim()?.takeIf { it.isNotBlank() },
            studioName = studio?.name,
            durationSeconds = file?.duration,
            resumeTimeSeconds = resumeTime,
            playCount = playCount,
            oCounter = oCounter,
            rating100 = rating100,
            fileName = file?.path?.toStashFileNameOrNull(),
            path = file?.path?.trim()?.takeIf { it.isNotBlank() },
            width = file?.width,
            height = file?.height,
            tags = tags.mapNotNull { it.toSceneCardTagChipOrNull() },
            captionBaseUrl = paths?.caption?.trim()?.takeIf { it.isNotBlank() },
            captions = captions.orEmpty().mapNotNull { it.toDomainOrNull() },
        )
    }

    fun toCard(): SceneCardModel {
        val file = files.firstOrNull()
        val duration = file?.duration ?: 0.0
        val resume = resumeTime ?: 0.0
        val progress = if (duration > 0.0) (resume / duration).toFloat().coerceIn(0f, 1f) else 0f
        val durationText = formatPlayerPosition((duration * 1000.0).toLong())
        return SceneCardModel(
            id = id,
            title = title?.ifBlank { null } ?: stashString(R.string.auto_kr_0168, id),
            durationText = durationText,
            studio = studio?.name ?: "Stash",
            progress = progress,
            isInWatchLater = false,
            thumbnailUrl = paths.bestThumbnailUrl(),
            playCount = playCount,
            metadataBadges = buildSceneCardMetadataBadges(
                width = file?.width,
                height = file?.height,
                durationText = durationText,
            ),
            tagChips = tags.mapNotNull { it.toSceneCardTagChipOrNull() },
        )
    }
}

private data class ApiStudio(val name: String? = null)
private data class ApiVideoFile(
    val duration: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    val path: String? = null,
)
private data class ApiScenePaths(
    val stream: String? = null,
    val screenshot: String? = null,
    val preview: String? = null,
    val webp: String? = null,
    val vtt: String? = null,
    val sprite: String? = null,
    val caption: String? = null,
)

private fun ApiScenePaths?.bestThumbnailUrl(): String? = listOf(
    this?.screenshot,
    this?.preview,
    this?.webp,
    this?.sprite,
).firstNotBlankOrNull()

private fun List<String?>.firstNotBlankOrNull(): String? = firstNotNullOfOrNull { value ->
    value?.trim()?.takeIf { it.isNotBlank() }
}

private data class ApiVideoCaption(
    @Json(name = "language_code") val languageCode: String? = null,
    @Json(name = "caption_type") val captionType: String? = null,
) {
    fun toDomainOrNull(): StashSceneCaption? {
        val normalizedLanguage = languageCode?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val normalizedType = captionType?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return StashSceneCaption(languageCode = normalizedLanguage, captionType = normalizedType)
    }
}

private fun ApiTag.toSceneCardTagChipOrNull(): SceneCardTagChip? {
    val normalizedName = normalizeStashVideoFilterText(name.orEmpty()).takeIf { it.isNotBlank() } ?: return null
    return SceneCardTagChip(id = id, label = normalizedName)
}

private fun String.toStashFileNameOrNull(): String? {
    val normalizedPath = trim().substringBefore('?').substringBefore('#')
    return normalizedPath.substringAfterLast('/').substringAfterLast('\\').takeIf { it.isNotBlank() }
}

private fun buildSceneCardMetadataBadges(
    width: Int?,
    height: Int?,
    durationText: String,
): List<SceneCardMetadataBadge> = buildList {
    formatResolutionBadge(width = width, height = height)?.let { label ->
        add(SceneCardMetadataBadge(id = "resolution", label = label))
    }
    durationText.takeIf { it.isNotBlank() && it != "00:00" }?.let { label ->
        add(SceneCardMetadataBadge(id = "duration", label = label))
    }
}

private fun formatResolutionBadge(width: Int?, height: Int?): String? {
    val normalizedHeight = height?.takeIf { it > 0 } ?: return null
    return when {
        normalizedHeight >= 2160 -> "4K"
        normalizedHeight >= 1440 -> "1440p"
        normalizedHeight >= 1080 -> "1080p"
        normalizedHeight >= 720 -> "720p"
        width != null && width > 0 -> "${normalizedHeight}p"
        else -> null
    }
}

private data class ApiSceneStream(
    val url: String? = null,
    @Json(name = "mime_type") val mimeType: String? = null,
    val label: String? = null,
) {
    fun toCandidate(): StashStreamCandidate? {
        val streamUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return StashStreamCandidate(
            url = streamUrl,
            mimeType = mimeType,
            label = label,
            origin = StashStreamOrigin.SceneStreams,
        )
    }
}
