package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
private val REDACTED_DEBUG_VALUE = "[REDACTED]"

private val sensitiveDebugQueryNames = setOf(
    "apikey",
    "api_key",
    "key",
    "token",
    "auth",
    "authorization",
    "secret",
    "password",
    "passwd",
    "credential",
    "credentials",
    "session",
)

data class PlayerDebugInfoRow(
    val label: String,
    val value: String,
)

data class PlayerDebugInfoUiState(
    val buttonLabel: String,
    val buttonContentDescription: String,
    val title: String,
    val dismissLabel: String,
    val rows: List<PlayerDebugInfoRow>,
)

fun buildPlayerDebugInfoUiState(
    pathOrUrl: String?,
    streamSourceLabel: String?,
    streamSourceTypeLabel: String?,
    streamSourceCategoryLabel: String?,
    thumbnailUrl: String?,
    spriteVttUrl: String?,
    spriteImageUrl: String?,
    spriteFrameCount: Int,
    activeCandidateIndex: Int,
    resolvedCandidateCount: Int,
    rawCandidateCount: Int,
    recommendationSourceLabel: String?,
): PlayerDebugInfoUiState {
    val safeResolvedCandidateCount = resolvedCandidateCount.coerceAtLeast(0)
    val safeActivePosition = if (safeResolvedCandidateCount > 0) {
        (activeCandidateIndex.coerceIn(0, safeResolvedCandidateCount - 1) + 1).toString()
    } else {
        "0"
    }
    val streamSummary = listOfNotNull(
        streamSourceLabel.normalizedDebugInfoValue()?.let(::redactPlayerDebugInfoText),
        streamSourceTypeLabel.normalizedDebugInfoValue()?.let(::redactPlayerDebugInfoText),
        streamSourceCategoryLabel.normalizedDebugInfoValue()?.let(::redactPlayerDebugInfoText),
    ).distinct().joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0178) }
    val rows = listOf(
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0179),
            value = pathOrUrl.normalizedDebugInfoValue()?.let(::redactPlayerDebugInfoText) ?: stashString(R.string.auto_kr_0178),
        ),
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0180),
            value = streamSummary,
        ),
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0181),
            value = thumbnailUrl.normalizedDebugInfoValue()?.let { stashString(R.string.auto_kr_0182, redactPlayerDebugInfoText(it)) } ?: stashString(R.string.auto_kr_0178),
        ),
        PlayerDebugInfoRow(
            label = "VTT",
            value = spriteVttUrl.normalizedDebugInfoValue()?.let { stashString(R.string.auto_kr_0183, redactPlayerDebugInfoText(it)) } ?: stashString(R.string.auto_kr_0184),
        ),
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0185),
            value = buildSpriteDebugInfoText(
                spriteImageUrl = spriteImageUrl,
                spriteFrameCount = spriteFrameCount,
            ),
        ),
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0186),
            value = stashString(R.string.auto_kr_0187, safeActivePosition, safeResolvedCandidateCount, rawCandidateCount.coerceAtLeast(0)),
        ),
        PlayerDebugInfoRow(
            label = stashString(R.string.auto_kr_0188),
            value = recommendationSourceLabel.normalizedDebugInfoValue()?.let(::redactPlayerDebugInfoText) ?: stashString(R.string.auto_kr_0178),
        ),
    )
    return PlayerDebugInfoUiState(
        buttonLabel = stashString(R.string.auto_kr_0189),
        buttonContentDescription = stashString(R.string.auto_kr_0190),
        title = stashString(R.string.auto_kr_0191),
        dismissLabel = stashString(R.string.auto_kr_0192),
        rows = rows,
    )
}

fun redactPlayerDebugInfoText(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return trimmed
    return redactSensitiveHeaderText(redactSensitiveQueryValues(redactSensitiveUrlUserInfo(trimmed)))
}

private fun buildSpriteDebugInfoText(
    spriteImageUrl: String?,
    spriteFrameCount: Int,
): String {
    val image = spriteImageUrl.normalizedDebugInfoValue()
    val frameText = if (spriteFrameCount > 0) stashString(R.string.auto_kr_0193, spriteFrameCount) else null
    val imageText = image?.let { stashString(R.string.auto_kr_0194, redactPlayerDebugInfoText(it)) }
    return listOfNotNull(frameText, imageText).joinToString(" · ").ifBlank { stashString(R.string.auto_kr_0184) }
}

private fun String?.normalizedDebugInfoValue(): String? = this
    ?.trim()
    ?.takeIf { it.isNotBlank() }

private fun redactSensitiveQueryValues(value: String): String {
    val fragmentStart = value.indexOf('#')
    val valueBeforeFragment = if (fragmentStart >= 0) value.substring(0, fragmentStart) else value
    val fragment = if (fragmentStart >= 0) value.substring(fragmentStart + 1) else null
    val queryStart = valueBeforeFragment.indexOf('?')
    val valueWithRedactedQuery = if (queryStart >= 0) {
        val prefix = valueBeforeFragment.substring(0, queryStart + 1)
        val query = valueBeforeFragment.substring(queryStart + 1)
        prefix + redactSensitiveParameterValues(query)
    } else {
        valueBeforeFragment
    }
    return if (fragment != null) {
        valueWithRedactedQuery + "#" + redactSensitiveParameterValues(fragment)
    } else {
        valueWithRedactedQuery
    }
}

private fun redactSensitiveParameterValues(parameters: String): String {
    if (parameters.isBlank()) return parameters
    return parameters.split('&').joinToString("&") { parameter ->
        val name = parameter.substringBefore('=', missingDelimiterValue = parameter)
        val separatorAndValue = parameter.substringAfter(name, missingDelimiterValue = "")
        if (name.isSensitiveDebugQueryName() && separatorAndValue.startsWith("=")) {
            "$name=$REDACTED_DEBUG_VALUE"
        } else {
            parameter
        }
    }
}

private fun redactSensitiveUrlUserInfo(value: String): String = value.replace(
    Regex("(?i)\\b(https?://)([^\\s/?#@]+@)"),
    "$1$REDACTED_DEBUG_VALUE@",
)

private fun String.isSensitiveDebugQueryName(): Boolean {
    val normalized = trim().lowercase().replace("-", "_")
    return normalized in sensitiveDebugQueryNames ||
        sensitiveDebugQueryNames.any { sensitiveName -> normalized.contains(sensitiveName) }
}

private fun redactSensitiveHeaderText(value: String): String = value
    .replace(
        Regex("(?i)(^|[\\s,;])(authorization\\s*[:=]\\s*)(bearer|basic)\\s+[^\\s,;]+"),
        "$1$2$3 $REDACTED_DEBUG_VALUE",
    )
    .replace(
        Regex("(?i)(^|[\\s,;])(api[-_ ]?key\\s*[:=]\\s*)[^\\s,;]+"),
        "$1$2$REDACTED_DEBUG_VALUE",
    )
    .replace(
        Regex("(?i)(^|[\\s,;])((?:auth|key|token|password|passwd|secret|credential|credentials|session)\\s*[:=]\\s*)[^\\s,;]+"),
        "$1$2$REDACTED_DEBUG_VALUE",
    )
