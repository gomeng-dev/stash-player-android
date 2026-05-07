package gomeng.dev.stashplayer.core.debug

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StashDebugLogEntry(
    val timestampMs: Long,
    val tag: String,
    val message: String,
) {
    fun formatForCopy(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date(timestampMs))
        return "$timestamp [$tag] $message"
    }
}

object StashDebugLogBuffer {
    const val MAX_ENTRIES = 300

    private val mutableEntries = MutableStateFlow<List<StashDebugLogEntry>>(emptyList())
    val entries: StateFlow<List<StashDebugLogEntry>> = mutableEntries.asStateFlow()

    fun record(tag: String, message: String, throwable: Throwable? = null) {
        val text = buildString {
            append(redactDebugLogText(message))
            throwable?.message?.takeIf { it.isNotBlank() }?.let {
                append(": ")
                append(redactDebugLogText(it))
            }
        }
        val entry = StashDebugLogEntry(
            timestampMs = System.currentTimeMillis(),
            tag = tag.take(40),
            message = text.take(1_200),
        )
        mutableEntries.value = (listOf(entry) + mutableEntries.value).take(MAX_ENTRIES)
    }

    fun clear() {
        mutableEntries.value = emptyList()
    }

    fun copyText(): String = mutableEntries.value.joinToString(separator = "\n") { it.formatForCopy() }
}

fun redactDebugLogText(value: String): String {
    var result = value
    val patterns = listOf(
        Regex("(?i)(api[_-]?key\\s*[=:]\\s*)([^\\s&;,]+)"),
        Regex("(?i)(token\\s*[=:]\\s*)([^\\s&;,]+)"),
        Regex("(?i)(password\\s*[=:]\\s*)([^\\s&;,]+)"),
        Regex("(?i)(authorization\\s*:?\\s*)(bearer\\s+)?([^\\s;,]+)"),
        Regex("(?i)(cookie\\s*:?\\s*)([^\\n;]+)"),
        Regex("(?i)(apikey=)([^&#\\s]+)"),
        Regex("(?i)(session=)([^;&#\\s]+)"),
    )
    patterns.forEach { pattern ->
        result = pattern.replace(result) { match ->
            match.groupValues.getOrNull(1).orEmpty() + "[REDACTED]"
        }
    }
    result = result.replace(Regex("(?i)https?://[^\\s/@:]+:[^\\s/@]+@"), "https://[REDACTED]@")
    return result
}
