package gomeng.dev.stashplayer.core.local

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueue
import gomeng.dev.stashplayer.core.player.PlayerQueueItem
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val PLAYER_QUEUE_SNAPSHOT_VERSION = "1"
private const val VALUE_PREFIX = "v"
private const val NULL_VALUE = "n"

internal const val SAVED_PLAYBACK_QUEUE_LIMIT = 10

internal fun MutablePreferences.saveQueueCopy(prefix: String, key: Preferences.Key<String>, snapshot: String) {
    if (asMap().keys.count { it.name.startsWith(prefix) } < SAVED_PLAYBACK_QUEUE_LIMIT) this[key] = snapshot
}

internal fun serializeSavedPlaybackQueue(queue: PlayerPlaybackQueue, positionMs: Long): String {
    val first = queue.items.firstOrNull() ?: return ""
    val hasCurrent = queue.items.any { it.sceneId == queue.currentSceneId }
    val snapshot = serializePlayerPlaybackQueue(if (hasCurrent) queue else queue.withCurrent(first.sceneId))
    return "${if (hasCurrent) positionMs.coerceAtLeast(0) else 0}\n$snapshot"
}

internal fun deserializeSavedPlaybackQueue(serialized: String): Pair<PlayerPlaybackQueue, Long>? {
    val positionMs = serialized.substringBefore('\n').toLongOrNull()?.takeIf { it >= 0 } ?: return null
    val queue = deserializePlayerPlaybackQueue(serialized.substringAfter('\n', "")) ?: return null
    return queue to positionMs
}

fun serializePlayerPlaybackQueue(queue: PlayerPlaybackQueue): String {
    val items = queue.items.distinctBy { it.sceneId }
    val currentSceneId = queue.currentSceneId?.takeIf { current ->
        current.isNotBlank() && items.any { it.sceneId == current }
    } ?: return ""
    val sequentialItems = if (queue.shuffleEnabled) {
        (queue.sequentialItems ?: items).distinctBy { it.sceneId }
    } else {
        emptyList()
    }

    return buildList {
        add("$PLAYER_QUEUE_SNAPSHOT_VERSION\t${encodeValue(currentSceneId)}\t${if (queue.shuffleEnabled) 1 else 0}")
        items.forEach { add(it.toSnapshotLine("p")) }
        sequentialItems.forEach { add(it.toSnapshotLine("s")) }
    }.joinToString("\n")
}

fun deserializePlayerPlaybackQueue(serialized: String): PlayerPlaybackQueue? = runCatching {
    val lines = serialized.lineSequence().filter { it.isNotBlank() }.toList()
    val header = lines.firstOrNull()?.split('\t') ?: return null
    if (header.size != 3 || header[0] != PLAYER_QUEUE_SNAPSHOT_VERSION) return null
    val currentSceneId = decodeValue(header[1])?.takeIf { it.isNotBlank() } ?: return null
    val shuffleEnabled = when (header[2]) {
        "0" -> false
        "1" -> true
        else -> return null
    }
    val parsedLines = lines.drop(1).map { line ->
        val fields = line.split('\t')
        if (fields.size != 4 || fields[0] !in setOf("p", "s")) return null
        fields[0] to PlayerQueueItem(
            sceneId = decodeValue(fields[1])?.takeIf { it.isNotBlank() } ?: return null,
            title = decodeValue(fields[2]) ?: return null,
            thumbnailUrl = decodeNullableValue(fields[3]),
        )
    }
    val items = parsedLines.filter { it.first == "p" }.map { it.second }
    if (items.isEmpty() || items.distinctBy { it.sceneId }.size != items.size) return null
    if (items.none { it.sceneId == currentSceneId }) return null

    val sequentialItems = parsedLines.filter { it.first == "s" }.map { it.second }
    if (shuffleEnabled) {
        if (
            sequentialItems.size != items.size ||
            sequentialItems.map { it.sceneId }.toSet() != items.map { it.sceneId }.toSet()
        ) {
            return null
        }
    } else if (sequentialItems.isNotEmpty()) {
        return null
    }

    PlayerPlaybackQueue(
        items = items,
        currentSceneId = currentSceneId,
        shuffleEnabled = shuffleEnabled,
        sequentialItems = sequentialItems.takeIf { shuffleEnabled },
    )
}.getOrNull()

private fun PlayerQueueItem.toSnapshotLine(type: String): String = listOf(
    type,
    encodeValue(sceneId),
    encodeValue(title),
    encodeNullableValue(thumbnailUrl.scrubLocalCredentialQueryParameters()),
).joinToString("\t")

private fun encodeValue(value: String): String = VALUE_PREFIX + URLEncoder.encode(
    value,
    StandardCharsets.UTF_8.name(),
)

private fun decodeValue(value: String): String? = value
    .takeIf { it.startsWith(VALUE_PREFIX) }
    ?.removePrefix(VALUE_PREFIX)
    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }

private fun encodeNullableValue(value: String?): String = value?.let(::encodeValue) ?: NULL_VALUE

private fun decodeNullableValue(value: String): String? = when (value) {
    NULL_VALUE -> null
    else -> decodeValue(value)
}
