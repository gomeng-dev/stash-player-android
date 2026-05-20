package gomeng.dev.stashplayer.core.player

import java.net.URI

private const val MIB_BYTES = 1024L * 1024L

/**
 * Forward-buffer and disk-cache policy for the active video player.
 *
 * The player already performs warm seeks while dragging. This policy makes the currently watched stream
 * keep a larger forward buffer and persist fetched media chunks in the app cache so repeat/nearby seeks
 * do not immediately go back to the network.
 */
data class StashPlayerPreloadPolicy(
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val bufferForPlaybackMs: Int,
    val bufferForPlaybackAfterRebufferMs: Int,
    val targetBufferBytes: Int,
    val cacheSizeBytes: Long,
    val cacheDirectoryName: String,
    val prioritizeTimeOverSizeThresholds: Boolean,
)

fun stashPlayerPreloadPolicy(): StashPlayerPreloadPolicy = StashPlayerPreloadPolicy(
    minBufferMs = 60_000,
    maxBufferMs = 180_000,
    bufferForPlaybackMs = 750,
    bufferForPlaybackAfterRebufferMs = 1_000,
    targetBufferBytes = (192L * MIB_BYTES).toInt(),
    cacheSizeBytes = 1024L * MIB_BYTES,
    cacheDirectoryName = "stash-player-media-cache",
    prioritizeTimeOverSizeThresholds = true,
)

fun sanitizedStashMediaCacheKey(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return trimmed
    return runCatching { sanitizeParsedCacheKey(URI(trimmed)) }
        .getOrElse { sanitizeMalformedCacheKey(trimmed) }
}

private fun sanitizeParsedCacheKey(uri: URI): String {
    val sanitizedQuery = sanitizeRawQuery(uri.rawQuery)
    return buildString {
        uri.scheme?.let { append(it).append(':') }
        uri.rawAuthority?.let { append("//").append(it) }
        append(uri.rawPath.orEmpty())
        if (!sanitizedQuery.isNullOrBlank()) {
            append('?').append(sanitizedQuery)
        }
    }.ifBlank { sanitizeMalformedCacheKey(uri.toString()) }
}

private fun sanitizeMalformedCacheKey(rawUrl: String): String {
    val withoutFragment = rawUrl.substringBefore('#')
    val base = withoutFragment.substringBefore('?')
    val rawQuery = withoutFragment.substringAfter('?', missingDelimiterValue = "")
    val sanitizedQuery = sanitizeRawQuery(rawQuery)
    return buildString {
        append(base)
        if (!sanitizedQuery.isNullOrBlank()) {
            append('?').append(sanitizedQuery)
        }
    }
}

private fun sanitizeRawQuery(rawQuery: String?): String? {
    if (rawQuery.isNullOrBlank()) return null
    return rawQuery
        .split('&')
        .filter { pair ->
            val key = pair.substringBefore('=').lowercase()
            key.isNotBlank() && !key.isSensitiveMediaCacheKey()
        }
        .joinToString("&")
        .takeIf { it.isNotBlank() }
}

private fun String.isSensitiveMediaCacheKey(): Boolean = when (this) {
    "apikey",
    "api_key",
    "key",
    "token",
    "access_token",
    "auth",
    "authorization",
    "signature",
    "sig",
    "expires",
    -> true
    else -> false
}
