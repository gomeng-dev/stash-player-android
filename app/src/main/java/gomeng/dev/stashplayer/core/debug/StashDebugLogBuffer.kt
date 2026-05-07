package gomeng.dev.stashplayer.core.debug

import android.content.Context
import java.io.File
import java.util.Base64
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val LOG_FILE_NAME = "stash-debug-log.tsv"
private const val LOG_FILE_VERSION = "v1"

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
    private var persistenceFile: File? = null

    @Synchronized
    fun initialize(context: Context) {
        val file = File(context.applicationContext.filesDir, LOG_FILE_NAME)
        persistenceFile = file
        mutableEntries.value = parsePersistedDebugLogEntries(
            runCatching { file.takeIf { it.isFile }?.readText().orEmpty() }.getOrDefault(""),
        )
    }

    @Synchronized
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
        persist()
    }

    @Synchronized
    fun clear() {
        mutableEntries.value = emptyList()
        runCatching { persistenceFile?.delete() }
    }

    fun copyText(): String = mutableEntries.value.joinToString(separator = "\n") { it.formatForCopy() }

    private fun persist() {
        runCatching { persistenceFile?.writeText(serializeDebugLogEntries(mutableEntries.value)) }
    }
}

internal fun serializeDebugLogEntries(entries: List<StashDebugLogEntry>): String =
    entries.take(StashDebugLogBuffer.MAX_ENTRIES).joinToString(separator = "\n") { entry ->
        listOf(
            LOG_FILE_VERSION,
            entry.timestampMs.toString(),
            entry.tag.base64ForDebugLog(),
            entry.message.base64ForDebugLog(),
        ).joinToString(separator = "\t")
    }

internal fun parsePersistedDebugLogEntries(text: String): List<StashDebugLogEntry> =
    text.lineSequence()
        .mapNotNull { line -> line.toDebugLogEntryOrNull() }
        .take(StashDebugLogBuffer.MAX_ENTRIES)
        .toList()

private fun String.toDebugLogEntryOrNull(): StashDebugLogEntry? {
    val parts = split('\t')
    if (parts.size != 4 || parts[0] != LOG_FILE_VERSION) return null
    return StashDebugLogEntry(
        timestampMs = parts[1].toLongOrNull() ?: return null,
        tag = parts[2].fromBase64ForDebugLog()?.take(40) ?: return null,
        message = parts[3].fromBase64ForDebugLog()?.take(1_200) ?: return null,
    )
}

private fun String.base64ForDebugLog(): String =
    Base64.getEncoder().encodeToString(toByteArray(Charsets.UTF_8))

private fun String.fromBase64ForDebugLog(): String? =
    runCatching { String(Base64.getDecoder().decode(this), Charsets.UTF_8) }.getOrNull()

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
