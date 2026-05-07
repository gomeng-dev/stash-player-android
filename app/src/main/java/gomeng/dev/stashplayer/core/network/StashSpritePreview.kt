package gomeng.dev.stashplayer.core.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URL

private val VTT_CUE_TIME_REGEX = Regex("""^\s*([0-9:.]+)\s+-->\s+([0-9:.]+).*$""")
private val SPRITE_CUE_REGEX = Regex("""^([^#]+)#xywh=(\d+),(\d+),(\d+),(\d+)\s*$""", RegexOption.IGNORE_CASE)

data class StashSpriteFrame(
    val url: String,
    val startSeconds: Double,
    val endSeconds: Double,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

fun parseStashSpriteVtt(vttUrl: String, vtt: String): List<StashSpriteFrame> {
    val lines = vtt.lineSequence().map { it.trim() }.toList()
    val frames = mutableListOf<StashSpriteFrame>()
    var index = 0
    while (index < lines.size) {
        val timeMatch = VTT_CUE_TIME_REGEX.matchEntire(lines[index])
        if (timeMatch == null) {
            index += 1
            continue
        }

        val startSeconds = parseVttTimestampSeconds(timeMatch.groupValues[1])
        val endSeconds = parseVttTimestampSeconds(timeMatch.groupValues[2])
        if (startSeconds == null || endSeconds == null) {
            index += 1
            continue
        }

        val text = lines.drop(index + 1).firstOrNull { it.isNotBlank() }.orEmpty()
        val spriteMatch = SPRITE_CUE_REGEX.matchEntire(text)
        if (spriteMatch != null) {
            frames += StashSpriteFrame(
                url = resolveSpriteUrl(vttUrl, spriteMatch.groupValues[1]),
                startSeconds = startSeconds,
                endSeconds = endSeconds,
                x = spriteMatch.groupValues[2].toInt(),
                y = spriteMatch.groupValues[3].toInt(),
                width = spriteMatch.groupValues[4].toInt(),
                height = spriteMatch.groupValues[5].toInt(),
            )
        }
        index += 1
    }
    return frames.sortedBy { it.startSeconds }
}

fun findStashSpriteAtTime(frames: List<StashSpriteFrame>?, seconds: Double): StashSpriteFrame? {
    if (frames.isNullOrEmpty() || !seconds.isFinite()) return null
    var previous: StashSpriteFrame? = null
    for (frame in frames) {
        if (seconds < frame.startSeconds) return previous ?: frame
        if (seconds >= frame.startSeconds && seconds < frame.endSeconds) return frame
        previous = frame
    }
    return previous
}

private fun parseVttTimestampSeconds(value: String): Double? {
    val parts = value.split(':')
    if (parts.size !in 2..3) return null
    return runCatching {
        val seconds = parts.last().toDouble()
        val minutes = parts[parts.lastIndex - 1].toDouble()
        val hours = if (parts.size == 3) parts[0].toDouble() else 0.0
        hours * 3600.0 + minutes * 60.0 + seconds
    }.getOrNull()
}

private fun resolveSpriteUrl(vttUrl: String, spritePath: String): String {
    val resolved = runCatching { URL(URL(vttUrl), spritePath).toString() }.getOrElse { spritePath }
    val vttApiKey = vttUrl.toHttpUrlOrNull()?.queryParameter("apikey") ?: return resolved
    val vttHttpUrl = vttUrl.toHttpUrlOrNull() ?: return resolved
    val resolvedHttpUrl = resolved.toHttpUrlOrNull() ?: return resolved
    if (!vttHttpUrl.isSameOrigin(resolvedHttpUrl)) return resolved
    if (resolvedHttpUrl.queryParameter("apikey") != null) return resolved
    return resolvedHttpUrl.newBuilder()
        .addQueryParameter("apikey", vttApiKey)
        .build()
        .toString()
}

private fun okhttp3.HttpUrl.isSameOrigin(other: okhttp3.HttpUrl): Boolean {
    return scheme == other.scheme && host == other.host && port == other.port
}
