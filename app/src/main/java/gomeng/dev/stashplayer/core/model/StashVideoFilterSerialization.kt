package gomeng.dev.stashplayer.core.model

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun deserializeStashVideoFilterState(serialized: String): StashVideoFilterState {
    if (serialized.isBlank()) return StashVideoFilterState()
    val fields = serialized
        .split(';')
        .mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
        }
        .toMap()

    val fileTypes = fields["fileTypes"]
        ?.split(',')
        ?.filter { it.isNotBlank() }
        ?.mapNotNull { id -> StashVideoFileType.entries.firstOrNull { it.id == id } }
        .orEmpty()

    return StashVideoFilterState(
        tags = fields["tags"].parseTags(),
        dateRange = rangeOrNull(
            StashDateRange(
                start = fields["dateStart"]?.let(::decodeOrNull)?.takeIf { it.isNotBlank() },
                end = fields["dateEnd"]?.let(::decodeOrNull)?.takeIf { it.isNotBlank() },
            ),
        ),
        durationRange = rangeOrNull(
            StashDurationRange(
                minSeconds = fields["durationMin"]?.toIntOrNull(),
                maxSeconds = fields["durationMax"]?.toIntOrNull(),
            ),
        ),
        ratingRange = rangeOrNull(
            StashRatingRange(
                min = fields["ratingMin"]?.toIntOrNull(),
                max = fields["ratingMax"]?.toIntOrNull(),
            ),
        ),
        playbackState = fields["playback"]?.let { id -> StashPlaybackState.entries.firstOrNull { it.id == id } },
        localFavoriteOnly = fields["favorite"] == "true",
        mediaFormat = StashMediaFormatFilter(
            resolution = fields["resolution"]?.let { id -> StashVideoResolution.entries.firstOrNull { it.id == id } },
            fileTypes = fileTypes,
        ),
        randomShuffle = fields["random"] == "true",
        randomShuffleSeed = fields["randomSeed"]?.toIntOrNull()?.let(::normalizeStashRandomSortSeed),
        savedFilter = fields["saved"].parseSavedFilter(),
    )
}

private fun String?.parseTags(): List<StashSelectedTag> = this
    ?.takeIf { it.isNotBlank() }
    ?.split(',')
    ?.mapNotNull { token ->
        val index = token.indexOf(':')
        if (index <= 0) {
            null
        } else {
            val id = decodeOrNull(token.substring(0, index)) ?: return@mapNotNull null
            val name = decodeOrNull(token.substring(index + 1)) ?: return@mapNotNull null
            StashSelectedTag(id = id, name = name)
        }
    }
    .orEmpty()

private fun String?.parseSavedFilter(): StashSavedFilterRef? {
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    val index = value.indexOf(':')
    if (index <= 0) return null
    val id = decodeOrNull(value.substring(0, index)) ?: return null
    val name = decodeOrNull(value.substring(index + 1)) ?: return null
    return StashSavedFilterRef(id = id, name = name)
}

private fun rangeOrNull(range: StashDateRange): StashDateRange? = range.takeUnless { it.isEmpty }

private fun rangeOrNull(range: StashDurationRange): StashDurationRange? = range.takeUnless { it.isEmpty }

private fun rangeOrNull(range: StashRatingRange): StashRatingRange? = range.takeUnless { it.isEmpty }

private fun decodeOrNull(value: String): String? = runCatching {
    URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}.getOrNull()
