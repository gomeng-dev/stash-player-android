package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SceneCardTagChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val MAX_PLAYER_DRAWER_TAGS = 8

enum class PlayerInfoDrawerContentSection {
    CompactMetadata,
    FilePath,
    Tags,
    SimilarVideos,
}

data class PlayerInfoDrawerContentState(
    val sectionOrder: List<PlayerInfoDrawerContentSection>,
    val metadataBadges: List<String>,
    val fileName: String,
    val pathLine: String,
    val tagLabels: List<String>,
    val hasSimilarVideosSurface: Boolean,
)

fun buildPlayerInfoDrawerContentState(
    title: String,
    fileName: String?,
    path: String?,
    tagChips: List<SceneCardTagChip>,
    studioName: String? = null,
    playCount: Int?,
    width: Int?,
    height: Int?,
    durationMs: Long,
    rating100: Int?,
    hasSimilarVideosSurface: Boolean,
): PlayerInfoDrawerContentState {
    val safeTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0035) }
    val safeFileName = fileName?.trim()?.takeIf { it.isNotBlank() } ?: safeTitle
    val safePath = path?.toPlayerDrawerPathLine()?.takeIf { it.isNotBlank() } ?: safeFileName
    val tags = tagChips
        .mapNotNull { chip -> chip.label.trim().takeIf { it.isNotBlank() } }
        .distinct()
        .take(MAX_PLAYER_DRAWER_TAGS)
    val sections = buildList {
        add(PlayerInfoDrawerContentSection.CompactMetadata)
        if (tags.isNotEmpty()) add(PlayerInfoDrawerContentSection.Tags)
        if (hasSimilarVideosSurface) add(PlayerInfoDrawerContentSection.SimilarVideos)
    }
    return PlayerInfoDrawerContentState(
        sectionOrder = sections,
        metadataBadges = buildPlayerInfoDrawerMetadataBadges(
            studioName = studioName,
            playCount = playCount,
            width = width,
            height = height,
            durationMs = durationMs,
            rating100 = rating100,
        ),
        fileName = safeFileName,
        pathLine = safePath,
        tagLabels = tags,
        hasSimilarVideosSurface = hasSimilarVideosSurface,
    )
}

fun buildPlayerInfoDrawerMetadataBadges(
    studioName: String? = null,
    playCount: Int?,
    width: Int?,
    height: Int?,
    durationMs: Long,
    rating100: Int?,
): List<String> = buildList {
    studioName?.toPlayerInfoDrawerMetadataValue()?.let(::add)
    formatPlayerInfoDrawerResolution(width = width, height = height)?.let(::add)
    durationMs.takeIf { it > 0L }?.let { add(formatPlayerPosition(it)) }
    rating100?.takeIf { it > 0 }?.let { add(stashString(R.string.auto_kr_0196, formatPlayerInfoDrawerRating(it))) }
    playCount?.takeIf { it > 0 }?.let { add(stashString(R.string.auto_kr_0197, it)) }
}

fun formatPlayerInfoDrawerResolution(width: Int?, height: Int?): String? {
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

fun formatPlayerInfoDrawerRating(rating100: Int): String {
    return "%.1f".format((rating100.coerceIn(0, 100) / 20.0))
}

private fun String.toPlayerInfoDrawerMetadataValue(): String? {
    val normalized = trim().takeIf { it.isNotBlank() } ?: return null
    return normalized.takeUnless { it in PLAYER_INFO_DRAWER_METADATA_PLACEHOLDERS }
}

private val PLAYER_INFO_DRAWER_METADATA_PLACEHOLDERS = setOf(stashString(R.string.auto_kr_0184), stashString(R.string.auto_kr_0198), "-")

private fun String.toPlayerDrawerPathLine(): String = trim()
    .substringBefore('?')
    .substringBefore('#')
    .trim()
