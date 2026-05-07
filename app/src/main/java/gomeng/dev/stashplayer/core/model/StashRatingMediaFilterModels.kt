package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
fun buildStashRatingRangeFromStarSelection(
    minStars: Int?,
    maxStars: Int?,
): StashRatingRange? = buildStashRatingRangeFromRatingStepSelection(
    minStep = minStars?.toRatingStepFromStarsOrNull(),
    maxStep = maxStars?.toRatingStepFromStarsOrNull(),
)

fun buildStashRatingRangeFromRatingStepSelection(
    minStep: Int?,
    maxStep: Int?,
): StashRatingRange? {
    val selection = normalizeStashRatingFilterSliderSelection(minStep, maxStep)
    return StashRatingRange(
        min = selection.minStep?.toRating100FromRatingStepOrNull(),
        max = selection.maxStep?.toRating100FromRatingStepOrNull(),
    ).takeUnless { it.isEmpty }
}

data class StashRatingFilterSliderSelection(
    val minStep: Int? = null,
    val maxStep: Int? = null,
)

fun normalizeStashRatingFilterSliderSelection(
    minStep: Int?,
    maxStep: Int?,
): StashRatingFilterSliderSelection {
    val normalizedMin = minStep?.toValidRatingStepOrNull()
    val normalizedMax = maxStep?.toValidRatingStepOrNull()
    return StashRatingFilterSliderSelection(
        minStep = normalizedMin,
        maxStep = if (normalizedMin != null && normalizedMax != null && normalizedMax < normalizedMin) {
            normalizedMin
        } else {
            normalizedMax
        },
    )
}

enum class StashRatingFilterSliderLayout {
    OneRow,
    TwoRows,
}

val StashRatingFilterOneRowMinWidthDp: Float = 520f

fun resolveStashRatingFilterSliderLayout(availableWidthDp: Float): StashRatingFilterSliderLayout =
    if (availableWidthDp >= StashRatingFilterOneRowMinWidthDp) {
        StashRatingFilterSliderLayout.OneRow
    } else {
        StashRatingFilterSliderLayout.TwoRows
    }

fun toggleStashVideoFileType(
    selectedFileTypes: List<StashVideoFileType>,
    fileType: StashVideoFileType,
): List<StashVideoFileType> = if (selectedFileTypes.contains(fileType)) {
    selectedFileTypes.filterNot { it == fileType }
} else {
    selectedFileTypes + fileType
}

private fun Int.toRatingStepFromStarsOrNull(): Int? = takeIf { it in 1..5 }?.times(2)

private fun Int.toRating100FromRatingStepOrNull(): Int? = toValidRatingStepOrNull()?.times(10)

private fun Int.toValidRatingStepOrNull(): Int? = takeIf { it in 1..10 }

fun Int.toStashRatingStepFromRating100(): Int = when {
    this <= 0 -> 0
    this >= 100 -> 10
    else -> ((this + 5) / 10).coerceIn(1, 10)
}

fun Int.toStashStarsFromRating100(): Int = when {
    this <= 0 -> 0
    this >= 100 -> 5
    else -> ((this + 10) / 20).coerceIn(1, 5)
}

data class StashRatingRange(
    val min: Int? = null,
    val max: Int? = null,
) {
    val isEmpty: Boolean get() = min == null && max == null
}

enum class StashVideoResolution(val id: String, val label: String) {
    P480("480p", "480p"),
    P720("720p", stashString(R.string.auto_kr_0122)),
    P1080("1080p", stashString(R.string.auto_kr_0123)),
    P1440("1440p", stashString(R.string.auto_kr_0124)),
    P4K("4k", stashString(R.string.auto_kr_0125)),
}

data class StashResolutionFilterOption(
    val resolution: StashVideoResolution?,
    val label: String,
)

fun stashResolutionFilterOptions(): List<StashResolutionFilterOption> = listOf(
    StashResolutionFilterOption(resolution = null, label = stashString(R.string.auto_kr_0126)),
    StashResolutionFilterOption(resolution = StashVideoResolution.P720, label = StashVideoResolution.P720.label),
    StashResolutionFilterOption(resolution = StashVideoResolution.P1080, label = StashVideoResolution.P1080.label),
    StashResolutionFilterOption(resolution = StashVideoResolution.P1440, label = StashVideoResolution.P1440.label),
    StashResolutionFilterOption(resolution = StashVideoResolution.P4K, label = StashVideoResolution.P4K.label),
)

enum class StashVideoFileType(val id: String, val label: String) {
    Mp4("mp4", "MP4"),
    Mkv("mkv", "MKV"),
    Webm("webm", "WEBM"),
    Mov("mov", "MOV"),
    Avi("avi", "AVI"),
}

data class StashMediaFormatFilter(
    val resolution: StashVideoResolution? = null,
    val fileTypes: List<StashVideoFileType> = emptyList(),
) {
    val isEmpty: Boolean get() = resolution == null && fileTypes.isEmpty()
}

fun StashRatingRange.displayLabel(): String = when {
    min != null && max != null -> "${min.toStashRatingStepLabel()}~${max.toStashRatingStepLabel()}"
    min != null -> stashString(R.string.auto_kr_0127, min.toStashRatingStepLabel())
    max != null -> stashString(R.string.auto_kr_0128, max.toStashRatingStepLabel())
    else -> ""
}

fun Int.toStashRatingStepLabel(): String {
    val step = toStashRatingStepFromRating100()
    return when {
        step <= 0 -> "0★"
        step % 2 == 0 -> "${step / 2}★"
        else -> "${step / 2}.5★"
    }
}

internal fun StashMediaFormatFilter.mediaLabel(): String = buildList {
    resolution?.let { add(it.label) }
    if (fileTypes.isNotEmpty()) add(fileTypes.joinToString(", ") { it.label })
}.joinToString(" · ")
