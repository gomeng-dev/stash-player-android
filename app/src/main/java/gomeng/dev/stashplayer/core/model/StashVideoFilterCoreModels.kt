package gomeng.dev.stashplayer.core.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

fun normalizeStashVideoFilterText(value: String): String = value
    .trim()
    .replace(Regex("\\s+"), " ")

enum class StashVideoFilterCategory(val id: String) {
    Tag("tag"),
    DateRange("date_range"),
    DurationRange("duration_range"),
    OCounter("o_counter"),
    Rating("rating"),
    PlaybackState("playback_state"),
    LocalFavorite("local_favorite"),
    MediaFormat("media_format"),
    RandomShuffle("random_shuffle"),
    SavedFilter("saved_filter"),
}

enum class StashVideoFilterEditTarget {
    Tags,
    DateDurationPlayback,
    RatingMedia,
    LocalLibrary,
    RandomShuffle,
}

fun StashVideoFilterCategory.editTarget(): StashVideoFilterEditTarget = when (this) {
    StashVideoFilterCategory.Tag -> StashVideoFilterEditTarget.Tags
    StashVideoFilterCategory.DateRange,
    StashVideoFilterCategory.DurationRange,
    StashVideoFilterCategory.OCounter,
    StashVideoFilterCategory.PlaybackState -> StashVideoFilterEditTarget.DateDurationPlayback
    StashVideoFilterCategory.Rating,
    StashVideoFilterCategory.MediaFormat -> StashVideoFilterEditTarget.RatingMedia
    StashVideoFilterCategory.LocalFavorite,
    StashVideoFilterCategory.SavedFilter -> StashVideoFilterEditTarget.LocalLibrary
    StashVideoFilterCategory.RandomShuffle -> StashVideoFilterEditTarget.RandomShuffle
}

data class StashActiveFilterChip(
    val category: StashVideoFilterCategory,
    val label: String,
)

data class StashVideoFilterState(
    val tags: List<StashSelectedTag> = emptyList(),
    val dateRange: StashDateRange? = null,
    val durationRange: StashDurationRange? = null,
    val oCounterFilter: StashOCounterFilter? = null,
    val ratingRange: StashRatingRange? = null,
    val playbackState: StashPlaybackState? = null,
    val localFavoriteOnly: Boolean = false,
    val mediaFormat: StashMediaFormatFilter = StashMediaFormatFilter(),
    val randomShuffle: Boolean = false,
    val randomShuffleSeed: Int? = null,
    val savedFilter: StashSavedFilterRef? = null,
) {
    val isEmpty: Boolean get() = activeFilterChips().isEmpty()
    val activeFilterCount: Int get() = activeFilterChips().size

    fun activeFilterChips(): List<StashActiveFilterChip> = buildList {
        if (tags.isNotEmpty()) {
            add(StashActiveFilterChip(StashVideoFilterCategory.Tag, stashString(R.string.auto_kr_0155, tags.size)))
        }
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveFilterChip(StashVideoFilterCategory.DateRange, stashString(R.string.auto_kr_0156, range.dateLabel())))
        }
        durationRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveFilterChip(StashVideoFilterCategory.DurationRange, stashString(R.string.auto_kr_0157, range.durationLabel())))
        }
        oCounterFilter?.takeUnless { it.isNoOp }?.let { filter ->
            add(StashActiveFilterChip(StashVideoFilterCategory.OCounter, filter.chipLabel))
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveFilterChip(StashVideoFilterCategory.Rating, stashString(R.string.auto_kr_0158, range.displayLabel())))
        }
        playbackState?.let { state ->
            add(StashActiveFilterChip(StashVideoFilterCategory.PlaybackState, state.label))
        }
        if (localFavoriteOnly) {
            add(StashActiveFilterChip(StashVideoFilterCategory.LocalFavorite, stashString(R.string.auto_kr_0159)))
        }
        mediaFormat.takeUnless { it.isEmpty }?.let { format ->
            add(StashActiveFilterChip(StashVideoFilterCategory.MediaFormat, format.mediaLabel()))
        }
        if (randomShuffle) {
            add(StashActiveFilterChip(StashVideoFilterCategory.RandomShuffle, stashString(R.string.auto_kr_0073)))
        }
        savedFilter?.let { filter ->
            val name = normalizeStashVideoFilterText(filter.name).ifBlank { filter.id }
            add(StashActiveFilterChip(StashVideoFilterCategory.SavedFilter, stashString(R.string.auto_kr_0160, name)))
        }
    }

    fun serializeForStorage(): String = buildList {
        if (tags.isNotEmpty()) {
            add("tags=${tags.joinToString(",") { "${encodeStashVideoFilterField(it.id)}:${encodeStashVideoFilterField(it.name)}" }}")
        }
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add("dateStart=${encodeStashVideoFilterField(range.start.orEmpty())}")
            add("dateEnd=${encodeStashVideoFilterField(range.end.orEmpty())}")
        }
        durationRange?.takeUnless { it.isEmpty }?.let { range ->
            add("durationMin=${range.minSeconds ?: ""}")
            add("durationMax=${range.maxSeconds ?: ""}")
        }
        oCounterFilter?.takeUnless { it.isNoOp }?.let { filter ->
            add("oCounterOp=${filter.comparator.storageId}")
            add("oCounterValue=${filter.value}")
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add("ratingMin=${range.min ?: ""}")
            add("ratingMax=${range.max ?: ""}")
        }
        playbackState?.let { add("playback=${it.id}") }
        if (localFavoriteOnly) add("favorite=true")
        mediaFormat.resolution?.let { add("resolution=${it.id}") }
        if (mediaFormat.fileTypes.isNotEmpty()) {
            add("fileTypes=${mediaFormat.fileTypes.joinToString(",") { it.id }}")
        }
        if (randomShuffle) {
            add("random=true")
            randomShuffleSeed?.let { add("randomSeed=${normalizeStashRandomSortSeed(it)}") }
        }
        savedFilter?.let { filter ->
            add("saved=${encodeStashVideoFilterField(filter.id)}:${encodeStashVideoFilterField(filter.name)}")
        }
    }.joinToString(";")
}

private fun encodeStashVideoFilterField(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
