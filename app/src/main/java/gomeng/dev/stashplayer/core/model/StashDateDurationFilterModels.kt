package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
fun toggleStashPlaybackState(
    current: StashPlaybackState?,
    selected: StashPlaybackState,
): StashPlaybackState? = if (current == selected) null else selected

fun buildStashDateRangeFromInputs(
    start: String,
    end: String,
): StashDateRange? = StashDateRange(
    start = start.toStashDateInputOrNull(),
    end = end.toStashDateInputOrNull(),
).takeUnless { it.isEmpty }

fun buildStashDurationRangeFromMinuteInputs(
    minMinutes: String,
    maxMinutes: String,
): StashDurationRange? = StashDurationRange(
    minSeconds = minMinutes.toPositiveMinutesOrNull()?.times(60),
    maxSeconds = maxMinutes.toPositiveMinutesOrNull()?.times(60),
).takeUnless { it.isEmpty }

enum class StashDurationPreset(
    val label: String,
    val minMinutes: Int?,
    val maxMinutes: Int?,
) {
    Short(label = stashString(R.string.auto_kr_0053), minMinutes = null, maxMinutes = 10),
    Normal(label = stashString(R.string.auto_kr_0054), minMinutes = 10, maxMinutes = 30),
    Long(label = stashString(R.string.auto_kr_0055), minMinutes = 30, maxMinutes = 60),
    VeryLong(label = stashString(R.string.auto_kr_0056), minMinutes = 60, maxMinutes = null),
}

fun stashDurationPresetOptions(): List<StashDurationPreset> = StashDurationPreset.entries.toList()

fun StashDurationPreset.toDurationRange(): StashDurationRange = StashDurationRange(
    minSeconds = minMinutes?.times(60),
    maxSeconds = maxMinutes?.times(60),
)

fun findMatchingStashDurationPreset(range: StashDurationRange?): StashDurationPreset? = range
    ?.takeUnless { it.isEmpty }
    ?.let { normalizedRange ->
        StashDurationPreset.entries.firstOrNull { preset -> preset.toDurationRange() == normalizedRange }
    }

private val stashDateInputRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

private fun String.toStashDateInputOrNull(): String? = normalizeStashVideoFilterText(this)
    .takeIf { it.matches(stashDateInputRegex) }

private val MAX_SAFE_DURATION_MINUTES = 35_791_394

private fun String.toPositiveMinutesOrNull(): Int? = trim()
    .toIntOrNull()
    ?.takeIf { it in 1..MAX_SAFE_DURATION_MINUTES }

data class StashDateRange(
    val start: String? = null,
    val end: String? = null,
) {
    val isEmpty: Boolean get() = start.isNullOrBlank() && end.isNullOrBlank()
}

data class StashDurationRange(
    val minSeconds: Int? = null,
    val maxSeconds: Int? = null,
) {
    val isEmpty: Boolean get() = minSeconds == null && maxSeconds == null
}

enum class StashPlaybackState(val id: String, val label: String) {
    Watched("watched", stashString(R.string.auto_kr_0057)),
    Unwatched("unwatched", stashString(R.string.auto_kr_0058)),
    Resumable("resumable", stashString(R.string.auto_kr_0059)),
}

internal fun StashDateRange.dateLabel(): String = when {
    !start.isNullOrBlank() && !end.isNullOrBlank() -> "${start}~${end}"
    !start.isNullOrBlank() -> stashString(R.string.auto_kr_0060, start)
    !end.isNullOrBlank() -> stashString(R.string.auto_kr_0061, end)
    else -> ""
}

internal fun StashDurationRange.durationLabel(): String = when {
    minSeconds != null && maxSeconds != null -> stashString(R.string.auto_kr_0062, minSeconds / 60, maxSeconds / 60)
    minSeconds != null -> stashString(R.string.auto_kr_0063, minSeconds / 60)
    maxSeconds != null -> stashString(R.string.auto_kr_0064, maxSeconds / 60)
    else -> ""
}
