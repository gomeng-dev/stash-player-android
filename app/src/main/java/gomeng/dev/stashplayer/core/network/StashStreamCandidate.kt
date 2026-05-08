package gomeng.dev.stashplayer.core.network

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
enum class StashStreamOrigin {
    SceneStreams,
    PathStream,
}

enum class StashStreamSourceCategory(val displayName: String) {
    Direct("Direct"),
    Hls("HLS"),
    Transcode("Transcode"),
    Unknown("Unknown"),
}

enum class StashStreamPreference(val id: String, val displayName: String) {
    Auto("auto", stashString(R.string.auto_kr_0171)),
    DirectFirst("direct", stashString(R.string.auto_kr_0172)),
    HlsFirst("hls", "HLS"),
}

enum class StashStreamSourceType(val displayName: String) {
    Hls("HLS"),
    Dash("DASH"),
    Mp4("MP4"),
    Webm("WEBM"),
    Mkv("MKV"),
    Direct("Direct stream"),
    Unknown("Stream"),
}

data class StashStreamCandidate(
    val url: String,
    val mimeType: String? = null,
    val label: String? = null,
    val origin: StashStreamOrigin = StashStreamOrigin.SceneStreams,
    val sourceCategory: StashStreamSourceCategory = classifyStashStreamSourceCategory(url, mimeType, label, origin),
    val sourceType: StashStreamSourceType = inferStashStreamSourceType(url, mimeType, label, origin, sourceCategory),
) {
    val displayLabel: String
        get() = label?.trim()?.takeIf { it.isNotBlank() } ?: sourceType.displayName

    val urlExtensionHint: String
        get() = stashStreamUrlExtensionHint(url)

    val isHlsManifest: Boolean
        get() = sourceCategory == StashStreamSourceCategory.Hls || hasHlsManifestHint(url, mimeType, label)
}

fun selectPreferredStashStream(
    candidates: List<StashStreamCandidate>,
    preference: StashStreamPreference = StashStreamPreference.Auto,
): StashStreamCandidate? = rankStashStreamCandidates(candidates, preference).firstOrNull()

fun rankStashStreamCandidates(
    candidates: List<StashStreamCandidate>,
    preference: StashStreamPreference = StashStreamPreference.Auto,
): List<StashStreamCandidate> = rankStashStreamCandidateIndexes(candidates, preference)
    .mapNotNull(candidates::getOrNull)

fun rankStashStreamCandidateIndexes(
    candidates: List<StashStreamCandidate>,
    preference: StashStreamPreference = StashStreamPreference.Auto,
): List<Int> = candidates
    .mapIndexedNotNull { index, candidate ->
        candidate.takeIf { it.url.isNotBlank() }?.let { IndexedStreamCandidate(index, it) }
    }
    .sortedWith(
        compareBy<IndexedStreamCandidate> { streamCandidateRank(it.candidate, preference) }
            .thenBy { streamCandidateOriginRank(it.candidate, preference) }
            .thenBy { it.index },
    )
    .map { it.index }

fun classifyStashStreamSourceCategory(
    url: String,
    mimeType: String?,
    label: String?,
    origin: StashStreamOrigin = StashStreamOrigin.SceneStreams,
): StashStreamSourceCategory {
    if (origin == StashStreamOrigin.PathStream) return StashStreamSourceCategory.Direct

    val urlPath = normalizedStreamUrlPath(url)
    val mime = mimeType.orEmpty().lowercase()
    val labelText = label.orEmpty().lowercase()

    return when {
        hasHlsManifestHint(url, mimeType, label) -> StashStreamSourceCategory.Hls
        isDirectStream(urlPath, labelText) -> StashStreamSourceCategory.Direct
        hasTranscodeHint(urlPath, mime, labelText) -> StashStreamSourceCategory.Transcode
        else -> StashStreamSourceCategory.Unknown
    }
}

fun inferStashStreamSourceType(
    url: String,
    mimeType: String?,
    label: String?,
    origin: StashStreamOrigin = StashStreamOrigin.SceneStreams,
    sourceCategory: StashStreamSourceCategory = classifyStashStreamSourceCategory(url, mimeType, label, origin),
): StashStreamSourceType {
    val urlPath = normalizedStreamUrlPath(url)
    val mime = mimeType.orEmpty().lowercase()
    val labelText = label.orEmpty().lowercase()

    return when {
        origin == StashStreamOrigin.PathStream || sourceCategory == StashStreamSourceCategory.Direct -> StashStreamSourceType.Direct

        urlPath.endsWith(".m3u8") ||
            mime.contains("mpegurl") ||
            labelText.contains("hls") -> StashStreamSourceType.Hls

        urlPath.endsWith(".mpd") ||
            mime.contains("dash") ||
            labelText.contains("dash") -> StashStreamSourceType.Dash

        urlPath.endsWith(".webm") ||
            mime.contains("webm") ||
            labelText.contains("webm") -> StashStreamSourceType.Webm

        urlPath.endsWith(".mkv") ||
            mime.contains("matroska") ||
            labelText.contains("mkv") -> StashStreamSourceType.Mkv

        urlPath.endsWith(".mp4") ||
            labelText.contains("mp4") ||
            mime == "video/mp4" -> StashStreamSourceType.Mp4

        else -> StashStreamSourceType.Unknown
    }
}

fun stashStreamPreferenceFromId(id: String?): StashStreamPreference =
    StashStreamPreference.entries.firstOrNull { it.id == id } ?: StashStreamPreference.Auto

fun preferredStashStreamCandidateIndex(
    candidates: List<StashStreamCandidate>,
    preference: StashStreamPreference,
): Int {
    if (candidates.isEmpty()) return -1
    val preferred = selectPreferredStashStream(candidates, preference) ?: return 0
    return candidates.indexOfFirst { it.url == preferred.url && it.origin == preferred.origin }.takeIf { it >= 0 } ?: 0
}

fun stashStreamUrlExtensionHint(url: String): String {
    val path = normalizedStreamUrlPath(url)
    val fileName = path.substringAfterLast('/').takeIf { it.isNotBlank() } ?: return "stream"
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
    return extension.takeIf { it.isNotBlank() } ?: fileName
}

internal fun hasHlsManifestHint(url: String, mimeType: String?, label: String?): Boolean {
    val urlPath = normalizedStreamUrlPath(url)
    val mime = mimeType.orEmpty().lowercase()
    val labelText = label.orEmpty().lowercase()
    return urlPath.endsWith(".m3u8") ||
        mime.contains("mpegurl") ||
        mime == "application/x-mpegurl" ||
        mime == "application/vnd.apple.mpegurl" ||
        labelText.contains("hls")
}

private data class IndexedStreamCandidate(
    val index: Int,
    val candidate: StashStreamCandidate,
)

private fun streamCandidateRank(candidate: StashStreamCandidate, preference: StashStreamPreference): Int = when (preference) {
    StashStreamPreference.Auto,
    StashStreamPreference.DirectFirst -> when (candidate.sourceCategory) {
        StashStreamSourceCategory.Direct -> 0
        StashStreamSourceCategory.Hls -> 1
        StashStreamSourceCategory.Transcode -> 2
        StashStreamSourceCategory.Unknown -> 3
    }
    StashStreamPreference.HlsFirst -> when (candidate.sourceCategory) {
        StashStreamSourceCategory.Hls -> 0
        StashStreamSourceCategory.Transcode -> 1
        StashStreamSourceCategory.Direct -> 2
        StashStreamSourceCategory.Unknown -> 3
    }
}

private fun streamCandidateOriginRank(candidate: StashStreamCandidate, preference: StashStreamPreference): Int = when {
    candidate.sourceCategory == StashStreamSourceCategory.Direct && candidate.origin == StashStreamOrigin.PathStream -> 0
    candidate.sourceCategory == StashStreamSourceCategory.Direct -> 1
    preference == StashStreamPreference.HlsFirst && candidate.sourceCategory == StashStreamSourceCategory.Direct -> 1
    else -> 0
}

private fun normalizedStreamUrlPath(url: String): String = url.trim()
    .substringBefore('#')
    .substringBefore('?')
    .lowercase()

private fun hasTranscodeHint(urlPath: String, mime: String, labelText: String): Boolean =
    labelText.contains("transcode") ||
        urlPath.contains("transcode") ||
        urlPath.endsWith(".mpd") ||
        mime.contains("dash") ||
        labelText.contains("dash") ||
        urlPath.endsWith(".mp4") ||
        labelText.contains("mp4") ||
        mime == "video/mp4" ||
        urlPath.endsWith(".webm") ||
        mime.contains("webm") ||
        labelText.contains("webm") ||
        urlPath.endsWith(".mkv") ||
        mime.contains("matroska") ||
        labelText.contains("mkv")

private fun isDirectStream(urlPath: String, labelText: String): Boolean =
    labelText.contains("direct stream") ||
        labelText == "direct" ||
        urlPath.endsWith("/stream") ||
        urlPath == "stream"
