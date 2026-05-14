package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Image-specific server filter state for Stash ImageFilterType.
 *
 * Keep this separate from gallery filters so saved/recent Gallery filters cannot
 * be restored into Image queries with incompatible field names.
 */
data class StashImageFilterTextState(
    val title: String = "",
    val details: String = "",
    val code: String = "",
    val photographer: String = "",
    val path: String = "",
    val url: String = "",
    val checksum: String = "",
) {
    val isEmpty: Boolean get() = listOf(title, details, code, photographer, path, url, checksum).all { it.isBlank() }
}

enum class StashImageOrientation(val id: String, val serverValue: String, val label: String) {
    Landscape("landscape", "LANDSCAPE", stashString(R.string.image_filter_orientation_landscape)),
    Portrait("portrait", "PORTRAIT", stashString(R.string.image_filter_orientation_portrait)),
    Square("square", "SQUARE", stashString(R.string.image_filter_orientation_square)),
}

enum class StashImageFileType(val id: String, val label: String) {
    Jpeg("jpeg", "JPEG"),
    Jpg("jpg", "JPG"),
    Png("png", "PNG"),
    Gif("gif", "GIF"),
    Webp("webp", "WEBP"),
}

enum class StashImageFilterCategory(val id: String) {
    Title("title"),
    Details("details"),
    Code("code"),
    Photographer("photographer"),
    Path("path"),
    Url("url"),
    Checksum("checksum"),
    DateRange("date_range"),
    CreatedAtRange("created_at_range"),
    UpdatedAtRange("updated_at_range"),
    Rating("rating"),
    BooleanFlags("boolean_flags"),
    OCounter("o_counter"),
    Resolution("resolution"),
    Orientation("orientation"),
    FileType("file_type"),
    Counts("counts"),
    PerformerAge("performer_age"),
    PerformerFavorite("performer_favorite"),
    Tags("tags"),
    PerformerTags("performer_tags"),
    Studios("studios"),
    Performers("performers"),
    Galleries("galleries"),
    SavedFilter("saved_filter"),
}

enum class StashImageFilterEditTarget {
    Text,
    Dates,
    RatingMedia,
    Counts,
    People,
    Relationships,
    SavedFilter,
}

data class StashActiveImageFilterChip(
    val category: StashImageFilterCategory,
    val label: String,
)

data class StashImageFilterState(
    val text: StashImageFilterTextState = StashImageFilterTextState(),
    val dateRange: StashDateRange? = null,
    val createdAtRange: StashDateRange? = null,
    val updatedAtRange: StashDateRange? = null,
    val ratingRange: StashRatingRange? = null,
    val organized: Boolean? = null,
    val oCounterRange: StashGalleryNumberRange? = null,
    val resolution: StashVideoResolution? = null,
    val orientations: List<StashImageOrientation> = emptyList(),
    val fileTypes: List<StashImageFileType> = emptyList(),
    val fileCountRange: StashGalleryNumberRange? = null,
    val tagCountRange: StashGalleryNumberRange? = null,
    val performerCountRange: StashGalleryNumberRange? = null,
    val performerAgeRange: StashGalleryNumberRange? = null,
    val performerFavorite: Boolean? = null,
    val tags: List<StashSelectedEntity> = emptyList(),
    val performerTags: List<StashSelectedEntity> = emptyList(),
    val studios: List<StashSelectedEntity> = emptyList(),
    val performers: List<StashSelectedEntity> = emptyList(),
    val galleries: List<StashSelectedEntity> = emptyList(),
    val savedFilter: StashSavedFilterRef? = null,
) {
    val isEmpty: Boolean get() = activeFilterChips().isEmpty()
    val activeFilterCount: Int get() = activeFilterChips().size
    val identityKey: String get() = serializeForStorage()

    fun activeFilterChips(): List<StashActiveImageFilterChip> = buildList {
        text.title.normalizedImageFilterTextOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Title, stashString(R.string.gallery_filter_chip_title, value)))
        }
        text.details.normalizedImageFilterTextOrNull()?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.Details, stashString(R.string.gallery_filter_chip_details)))
        }
        text.code.normalizedImageFilterTextOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Code, stashString(R.string.gallery_filter_chip_code, value)))
        }
        text.photographer.normalizedImageFilterTextOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Photographer, stashString(R.string.gallery_filter_chip_photographer, value)))
        }
        text.path.normalizedImageFilterTextOrNull()?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.Path, stashString(R.string.gallery_filter_chip_path)))
        }
        text.url.normalizedImageFilterTextOrNull()?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.Url, stashString(R.string.gallery_filter_chip_url)))
        }
        text.checksum.normalizedImageFilterTextOrNull()?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.Checksum, stashString(R.string.gallery_filter_chip_checksum)))
        }
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.DateRange, stashString(R.string.auto_kr_0156, range.dateLabel())))
        }
        createdAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.CreatedAtRange, stashString(R.string.gallery_filter_chip_created_at, range.dateLabel())))
        }
        updatedAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.UpdatedAtRange, stashString(R.string.gallery_filter_chip_updated_at, range.dateLabel())))
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Rating, stashString(R.string.auto_kr_0158, range.displayLabel())))
        }
        organized?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.BooleanFlags, stashString(R.string.gallery_filter_chip_boolean_flags)))
        }
        oCounterRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.OCounter, stashString(R.string.image_filter_chip_o_counter, range.countLabel())))
        }
        resolution?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Resolution, stashString(R.string.gallery_filter_chip_average_resolution, value.label)))
        }
        orientations.takeIf { it.isNotEmpty() }?.let { values ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Orientation, stashString(R.string.image_filter_chip_orientation, values.joinToString(", ") { it.label })))
        }
        fileTypes.takeIf { it.isNotEmpty() }?.let { values ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.FileType, stashString(R.string.image_filter_chip_file_type, values.joinToString(", ") { it.label })))
        }
        if (
            fileCountRange?.takeUnless { it.isEmpty } != null ||
            tagCountRange?.takeUnless { it.isEmpty } != null ||
            performerCountRange?.takeUnless { it.isEmpty } != null
        ) {
            add(StashActiveImageFilterChip(StashImageFilterCategory.Counts, stashString(R.string.gallery_filter_chip_counts)))
        }
        performerAgeRange?.takeUnless { it.isEmpty }?.let { range ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.PerformerAge, stashString(R.string.image_filter_chip_performer_age, range.countLabel())))
        }
        performerFavorite?.let {
            add(StashActiveImageFilterChip(StashImageFilterCategory.PerformerFavorite, stashString(R.string.image_filter_chip_performer_favorite)))
        }
        tags.firstImageEntityNameOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Tags, stashString(R.string.gallery_filter_chip_tags, value)))
        }
        performerTags.firstImageEntityNameOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.PerformerTags, stashString(R.string.image_filter_chip_performer_tags, value)))
        }
        studios.firstImageEntityNameOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Studios, stashString(R.string.gallery_filter_chip_studios, value)))
        }
        performers.firstImageEntityNameOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Performers, stashString(R.string.gallery_filter_chip_performers, value)))
        }
        galleries.firstImageEntityNameOrNull()?.let { value ->
            add(StashActiveImageFilterChip(StashImageFilterCategory.Galleries, stashString(R.string.image_filter_chip_galleries, value)))
        }
        savedFilter?.let { filter ->
            val name = normalizeStashVideoFilterText(filter.name).ifBlank { filter.id }
            add(StashActiveImageFilterChip(StashImageFilterCategory.SavedFilter, stashString(R.string.auto_kr_0160, name)))
        }
    }

    fun serializeForStorage(): String = buildList {
        addImageFilterStorageField("title", text.title)
        addImageFilterStorageField("details", text.details)
        addImageFilterStorageField("code", text.code)
        addImageFilterStorageField("photographer", text.photographer)
        addImageFilterStorageField("path", text.path)
        addImageFilterStorageField("url", text.url)
        addImageFilterStorageField("checksum", text.checksum)
        dateRange?.takeUnless { it.isEmpty }?.let { range ->
            add("dateStart=${encodeImageFilterField(range.start.orEmpty())}")
            add("dateEnd=${encodeImageFilterField(range.end.orEmpty())}")
        }
        createdAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add("createdStart=${encodeImageFilterField(range.start.orEmpty())}")
            add("createdEnd=${encodeImageFilterField(range.end.orEmpty())}")
        }
        updatedAtRange?.takeUnless { it.isEmpty }?.let { range ->
            add("updatedStart=${encodeImageFilterField(range.start.orEmpty())}")
            add("updatedEnd=${encodeImageFilterField(range.end.orEmpty())}")
        }
        ratingRange?.takeUnless { it.isEmpty }?.let { range ->
            add("ratingMin=${range.min ?: ""}")
            add("ratingMax=${range.max ?: ""}")
        }
        organized?.let { add("organized=$it") }
        oCounterRange?.takeUnless { it.isEmpty }?.let { addImageNumberRangeStorageFields("oCounter") }
        resolution?.let { add("resolution=${it.id}") }
        orientations.normalizedImageOrientations().takeIf { it.isNotEmpty() }?.let { values ->
            add("orientation=${values.joinToString(",") { it.id }}")
        }
        fileTypes.normalizedImageFileTypes().takeIf { it.isNotEmpty() }?.let { values ->
            add("fileTypes=${values.joinToString(",") { it.id }}")
        }
        fileCountRange?.takeUnless { it.isEmpty }?.let { addImageNumberRangeStorageFields("fileCount") }
        tagCountRange?.takeUnless { it.isEmpty }?.let { addImageNumberRangeStorageFields("tagCount") }
        performerCountRange?.takeUnless { it.isEmpty }?.let { addImageNumberRangeStorageFields("performerCount") }
        performerAgeRange?.takeUnless { it.isEmpty }?.let { addImageNumberRangeStorageFields("performerAge") }
        performerFavorite?.let { add("performerFavorite=$it") }
        addImageEntityStorageField("tags", tags)
        addImageEntityStorageField("performerTags", performerTags)
        addImageEntityStorageField("studios", studios)
        addImageEntityStorageField("performers", performers)
        addImageEntityStorageField("galleries", galleries)
        savedFilter?.let { filter ->
            add("saved=${encodeImageFilterField(filter.id)}:${encodeImageFilterField(filter.name)}")
        }
    }.joinToString(";")

    private fun MutableList<String>.addImageFilterStorageField(name: String, value: String) {
        val normalized = value.normalizedImageFilterTextOrNull() ?: return
        add("$name=${encodeImageFilterField(normalized)}")
    }

    private fun MutableList<String>.addImageNumberRangeStorageFields(prefix: String) {
        val range = when (prefix) {
            "oCounter" -> oCounterRange
            "fileCount" -> fileCountRange
            "tagCount" -> tagCountRange
            "performerCount" -> performerCountRange
            "performerAge" -> performerAgeRange
            else -> null
        } ?: return
        add("${prefix}Min=${range.min ?: ""}")
        add("${prefix}Max=${range.max ?: ""}")
    }

    private fun MutableList<String>.addImageEntityStorageField(name: String, entities: List<StashSelectedEntity>) {
        val serializedEntities = entities.normalizedGalleryEntities()
            .joinToString(",") { entity ->
                encodeImageFilterField("${entity.id}|${entity.name}")
            }
        if (serializedEntities.isNotBlank()) add("$name=$serializedEntities")
    }
}

fun StashImageFilterCategory.editTarget(): StashImageFilterEditTarget = when (this) {
    StashImageFilterCategory.Title,
    StashImageFilterCategory.Details,
    StashImageFilterCategory.Code,
    StashImageFilterCategory.Photographer,
    StashImageFilterCategory.Path,
    StashImageFilterCategory.Url,
    StashImageFilterCategory.Checksum -> StashImageFilterEditTarget.Text
    StashImageFilterCategory.DateRange,
    StashImageFilterCategory.CreatedAtRange,
    StashImageFilterCategory.UpdatedAtRange -> StashImageFilterEditTarget.Dates
    StashImageFilterCategory.Rating,
    StashImageFilterCategory.BooleanFlags,
    StashImageFilterCategory.OCounter,
    StashImageFilterCategory.Resolution,
    StashImageFilterCategory.Orientation,
    StashImageFilterCategory.FileType -> StashImageFilterEditTarget.RatingMedia
    StashImageFilterCategory.Counts -> StashImageFilterEditTarget.Counts
    StashImageFilterCategory.PerformerAge,
    StashImageFilterCategory.PerformerFavorite -> StashImageFilterEditTarget.People
    StashImageFilterCategory.Tags,
    StashImageFilterCategory.PerformerTags,
    StashImageFilterCategory.Studios,
    StashImageFilterCategory.Performers,
    StashImageFilterCategory.Galleries -> StashImageFilterEditTarget.Relationships
    StashImageFilterCategory.SavedFilter -> StashImageFilterEditTarget.SavedFilter
}

fun clearStashImageFilterCategory(
    state: StashImageFilterState,
    category: StashImageFilterCategory,
): StashImageFilterState = when (category) {
    StashImageFilterCategory.Title -> state.copy(text = state.text.copy(title = ""))
    StashImageFilterCategory.Details -> state.copy(text = state.text.copy(details = ""))
    StashImageFilterCategory.Code -> state.copy(text = state.text.copy(code = ""))
    StashImageFilterCategory.Photographer -> state.copy(text = state.text.copy(photographer = ""))
    StashImageFilterCategory.Path -> state.copy(text = state.text.copy(path = ""))
    StashImageFilterCategory.Url -> state.copy(text = state.text.copy(url = ""))
    StashImageFilterCategory.Checksum -> state.copy(text = state.text.copy(checksum = ""))
    StashImageFilterCategory.DateRange -> state.copy(dateRange = null)
    StashImageFilterCategory.CreatedAtRange -> state.copy(createdAtRange = null)
    StashImageFilterCategory.UpdatedAtRange -> state.copy(updatedAtRange = null)
    StashImageFilterCategory.Rating -> state.copy(ratingRange = null)
    StashImageFilterCategory.BooleanFlags -> state.copy(organized = null)
    StashImageFilterCategory.OCounter -> state.copy(oCounterRange = null)
    StashImageFilterCategory.Resolution -> state.copy(resolution = null)
    StashImageFilterCategory.Orientation -> state.copy(orientations = emptyList())
    StashImageFilterCategory.FileType -> state.copy(fileTypes = emptyList())
    StashImageFilterCategory.Counts -> state.copy(fileCountRange = null, tagCountRange = null, performerCountRange = null)
    StashImageFilterCategory.PerformerAge -> state.copy(performerAgeRange = null)
    StashImageFilterCategory.PerformerFavorite -> state.copy(performerFavorite = null)
    StashImageFilterCategory.Tags -> state.copy(tags = emptyList())
    StashImageFilterCategory.PerformerTags -> state.copy(performerTags = emptyList())
    StashImageFilterCategory.Studios -> state.copy(studios = emptyList())
    StashImageFilterCategory.Performers -> state.copy(performers = emptyList())
    StashImageFilterCategory.Galleries -> state.copy(galleries = emptyList())
    StashImageFilterCategory.SavedFilter -> state.copy(savedFilter = null)
}

fun deserializeStashImageFilterState(serialized: String): StashImageFilterState {
    if (serialized.isBlank()) return StashImageFilterState()
    val fields = serialized
        .split(';')
        .mapNotNull { entry ->
            val index = entry.indexOf('=')
            if (index <= 0) null else entry.substring(0, index) to entry.substring(index + 1)
        }
        .toMap()

    return StashImageFilterState(
        text = StashImageFilterTextState(
            title = fields["title"].decodeImageFilterFieldOrNull().orEmpty(),
            details = fields["details"].decodeImageFilterFieldOrNull().orEmpty(),
            code = fields["code"].decodeImageFilterFieldOrNull().orEmpty(),
            photographer = fields["photographer"].decodeImageFilterFieldOrNull().orEmpty(),
            path = fields["path"].decodeImageFilterFieldOrNull().orEmpty(),
            url = fields["url"].decodeImageFilterFieldOrNull().orEmpty(),
            checksum = fields["checksum"].decodeImageFilterFieldOrNull().orEmpty(),
        ),
        dateRange = StashDateRange(
            start = fields["dateStart"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["dateEnd"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        createdAtRange = StashDateRange(
            start = fields["createdStart"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["createdEnd"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        updatedAtRange = StashDateRange(
            start = fields["updatedStart"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
            end = fields["updatedEnd"].decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() },
        ).takeUnless { it.isEmpty },
        ratingRange = StashRatingRange(
            min = fields["ratingMin"]?.toIntOrNull(),
            max = fields["ratingMax"]?.toIntOrNull(),
        ).takeUnless { it.isEmpty },
        organized = fields["organized"]?.toBooleanStrictOrNull(),
        oCounterRange = imageNumberRangeOrNull(fields, "oCounter"),
        resolution = fields["resolution"]?.let { id -> StashVideoResolution.entries.firstOrNull { it.id == id } },
        orientations = fields["orientation"].parseImageOrientations(),
        fileTypes = fields["fileTypes"].parseImageFileTypes(),
        fileCountRange = imageNumberRangeOrNull(fields, "fileCount"),
        tagCountRange = imageNumberRangeOrNull(fields, "tagCount"),
        performerCountRange = imageNumberRangeOrNull(fields, "performerCount"),
        performerAgeRange = imageNumberRangeOrNull(fields, "performerAge"),
        performerFavorite = fields["performerFavorite"]?.toBooleanStrictOrNull(),
        tags = fields["tags"].parseImageEntities(),
        performerTags = fields["performerTags"].parseImageEntities(),
        studios = fields["studios"].parseImageEntities(),
        performers = fields["performers"].parseImageEntities(),
        galleries = fields["galleries"].parseImageEntities(),
        savedFilter = fields["saved"].parseImageSavedFilterRef(),
    )
}

private fun StashGalleryNumberRange.countLabel(): String = when {
    min != null && max != null -> "$min~$max"
    min != null -> stashString(R.string.image_filter_count_min_label, min)
    max != null -> stashString(R.string.image_filter_count_max_label, max)
    else -> ""
}

private fun List<StashSelectedEntity>.firstImageEntityNameOrNull(): String? = normalizedGalleryEntities()
    .firstOrNull()
    ?.name

private fun List<StashImageOrientation>.normalizedImageOrientations(): List<StashImageOrientation> {
    val seen = mutableSetOf<StashImageOrientation>()
    return filter { seen.add(it) }
}

private fun List<StashImageFileType>.normalizedImageFileTypes(): List<StashImageFileType> {
    val seen = mutableSetOf<StashImageFileType>()
    return filter { seen.add(it) }
}

private fun imageNumberRangeOrNull(fields: Map<String, String>, prefix: String): StashGalleryNumberRange? = StashGalleryNumberRange(
    min = fields["${prefix}Min"]?.toIntOrNull(),
    max = fields["${prefix}Max"]?.toIntOrNull(),
).takeUnless { it.isEmpty }

private fun String?.parseImageEntities(): List<StashSelectedEntity> = this
    ?.takeIf { it.isNotBlank() }
    ?.split(',')
    ?.mapNotNull { value ->
        val decoded = value.decodeImageFilterFieldOrNull() ?: return@mapNotNull null
        val index = decoded.indexOf('|')
        if (index <= 0) return@mapNotNull null
        val id = decoded.substring(0, index)
        val name = decoded.substring(index + 1)
        StashSelectedEntity(id = id, name = name)
    }
    .orEmpty()
    .normalizedGalleryEntities()

private fun String?.parseImageOrientations(): List<StashImageOrientation> = this
    ?.split(',')
    ?.mapNotNull { id -> StashImageOrientation.entries.firstOrNull { it.id == id.trim() } }
    .orEmpty()
    .normalizedImageOrientations()

private fun String?.parseImageFileTypes(): List<StashImageFileType> = this
    ?.split(',')
    ?.mapNotNull { id -> StashImageFileType.entries.firstOrNull { it.id == id.trim() } }
    .orEmpty()
    .normalizedImageFileTypes()

private fun String?.parseImageSavedFilterRef(): StashSavedFilterRef? {
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    val index = value.indexOf(':')
    if (index <= 0) return null
    val id = value.substring(0, index).decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() } ?: return null
    val name = value.substring(index + 1).decodeImageFilterFieldOrNull()?.takeIf { it.isNotBlank() } ?: return null
    return StashSavedFilterRef(id = id, name = name)
}

private fun String.normalizedImageFilterTextOrNull(): String? = normalizeStashVideoFilterText(this).takeIf { it.isNotBlank() }

fun StashImageFilterState.toImageSavedFilterPayload(): StashImageFilterState = copy(savedFilter = null)

fun StashImageFilterState.toRecentImageFilterSnapshot(): StashImageFilterState = copy(savedFilter = null)

fun StashImageFilterState.shouldSaveAsRecentImageFilter(): Boolean = toRecentImageFilterSnapshot()
    .serializeForStorage()
    .isNotBlank()

fun shouldPromoteRecentImageFilterAfterChange(
    previous: StashImageFilterState,
    updated: StashImageFilterState,
): Boolean {
    val previousKey = previous.toRecentImageFilterSnapshot().serializeForStorage()
    val updatedKey = updated.toRecentImageFilterSnapshot().serializeForStorage()
    return previousKey != updatedKey && updatedKey.isNotBlank()
}

fun StashImageFilterState.imageFilterSummaryLabel(maxVisibleChips: Int = 3): String {
    val labels = toImageSavedFilterPayload().activeFilterChips().map { it.label }
    if (labels.isEmpty()) return stashString(R.string.auto_kr_0130)
    val visibleLabels = labels.take(maxVisibleChips.coerceAtLeast(1))
    val hiddenCount = labels.size - visibleLabels.size
    return buildString {
        append(visibleLabels.joinToString(" · "))
        if (hiddenCount > 0) append(" +$hiddenCount")
    }
}

fun StashImageFilterState.quickSavedImageFilterName(suffix: String? = null): String {
    val baseName = imageFilterSummaryLabel(maxVisibleChips = Int.MAX_VALUE)
        .takeUnless { it == stashString(R.string.auto_kr_0130) }
        ?: stashString(R.string.auto_kr_0134)
    val normalizedSuffix = suffix?.let(::normalizeStashVideoFilterText).orEmpty()
    return if (normalizedSuffix.isBlank()) baseName else "$baseName · $normalizedSuffix"
}

fun promoteStashRecentImageFilter(
    existing: List<StashImageFilterState>,
    candidate: StashImageFilterState,
    limit: Int,
): List<StashImageFilterState> {
    val normalizedCandidate = candidate.toRecentImageFilterSnapshot()
    val candidateKey = normalizedCandidate.serializeForStorage()
    if (candidateKey.isBlank()) return existing.take(limit.coerceAtLeast(0))
    val maxSize = limit.coerceAtLeast(1)
    return (listOf(normalizedCandidate) + existing.filterNot { it.toRecentImageFilterSnapshot().serializeForStorage() == candidateKey })
        .take(maxSize)
}

fun StashImageFilterState.withSavedFilterReference(ref: StashSavedFilterRef): StashImageFilterState = copy(savedFilter = ref)

private fun encodeImageFilterField(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

private fun String?.decodeImageFilterFieldOrNull(): String? = this?.let { value ->
    runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.name()) }.getOrNull()
}
