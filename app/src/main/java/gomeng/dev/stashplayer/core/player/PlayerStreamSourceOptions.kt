package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class PlayerStreamSourceCandidateLabel(
    val sourceLabel: String?,
    val sourceTypeLabel: String,
    val sourceCategoryLabel: String = sourceTypeLabel,
    val mimeType: String? = null,
    val urlExtensionHint: String? = null,
    val isHlsManifest: Boolean = false,
)

data class PlayerStreamSourceOption(
    val index: Int,
    val title: String,
    val subtitle: String,
    val selected: Boolean,
    val sourceCategoryLabel: String,
    val mimeType: String?,
    val urlExtensionHint: String?,
    val isHlsManifest: Boolean,
)

data class PlayerStreamPreferenceOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val selected: Boolean,
    val enabled: Boolean,
)

fun canChoosePlayerStreamSource(candidateCount: Int): Boolean = candidateCount > 1

fun buildPlayerStreamSourceOptions(
    candidates: List<PlayerStreamSourceCandidateLabel>,
    selectedIndex: Int,
): List<PlayerStreamSourceOption> {
    if (candidates.isEmpty()) return emptyList()
    val coercedSelectedIndex = selectedIndex.coerceIn(0, candidates.lastIndex)
    return candidates.mapIndexed { index, candidate ->
        val categoryLabel = sanitizePlayerStreamSourceLabel(
            label = candidate.sourceCategoryLabel,
            fallback = "Stream",
        )
        val typeLabel = sanitizePlayerStreamSourceLabel(
            label = candidate.sourceTypeLabel,
            fallback = categoryLabel,
        )
        val title = sanitizePlayerStreamSourceLabel(
            label = candidate.sourceLabel,
            fallback = categoryLabel,
        )
        val mimeLabel = sanitizePlayerStreamSourceLabel(
            label = candidate.mimeType,
            fallback = "mime unknown",
        )
        val extensionLabel = sanitizePlayerStreamSourceLabel(
            label = candidate.urlExtensionHint,
            fallback = "stream",
        )
        val details = buildList {
            add(categoryLabel)
            if (typeLabel != categoryLabel) add(typeLabel)
            add(mimeLabel)
            add("ext: $extensionLabel")
            add(if (candidate.isHlsManifest) ".m3u8: yes" else ".m3u8: no")
        }.joinToString(" · ")
        PlayerStreamSourceOption(
            index = index,
            title = title,
            subtitle = details,
            selected = index == coercedSelectedIndex,
            sourceCategoryLabel = categoryLabel,
            mimeType = candidate.mimeType?.trim()?.takeIf { it.isNotBlank() },
            urlExtensionHint = candidate.urlExtensionHint?.trim()?.takeIf { it.isNotBlank() },
            isHlsManifest = candidate.isHlsManifest,
        )
    }
}

fun buildPlayerStreamPreferenceOptions(
    selectedPreferenceId: String,
    canChooseDirect: Boolean,
    canChooseHls: Boolean,
): List<PlayerStreamPreferenceOption> = listOf(
    PlayerStreamPreferenceOption(
        id = "auto",
        title = stashString(R.string.auto_kr_0171),
        subtitle = stashString(R.string.auto_kr_0262),
        selected = selectedPreferenceId == "auto",
        enabled = true,
    ),
    PlayerStreamPreferenceOption(
        id = "direct",
        title = stashString(R.string.auto_kr_0172),
        subtitle = stashString(R.string.auto_kr_0263),
        selected = selectedPreferenceId == "direct",
        enabled = canChooseDirect,
    ),
    PlayerStreamPreferenceOption(
        id = "hls",
        title = "HLS",
        subtitle = stashString(R.string.auto_kr_0264),
        selected = selectedPreferenceId == "hls",
        enabled = canChooseHls,
    ),
)

fun playerCurrentStreamInfoText(option: PlayerStreamSourceOption?): String {
    if (option == null) return stashString(R.string.auto_kr_0265)
    val label = sanitizePlayerStreamSourceLabel(option.title, option.sourceCategoryLabel)
    val mime = sanitizePlayerStreamSourceLabel(option.mimeType, "unknown")
    val hls = if (option.isHlsManifest) "yes" else "no"
    return stashString(R.string.auto_kr_0266, option.sourceCategoryLabel, label, mime, hls)
}

fun playerDebugOverlayText(enabled: Boolean, option: PlayerStreamSourceOption?): String? =
    if (enabled) playerCurrentStreamInfoText(option) else null

fun sanitizePlayerStreamSourceLabel(
    label: String?,
    fallback: String = "Stream",
): String {
    val trimmed = label.orEmpty().trim()
    val safeFallback = fallback.trim().takeIf { it.isNotBlank() } ?: "Stream"
    if (trimmed.isBlank()) return safeFallback
    val lower = trimmed.lowercase()
    val looksLikeUrl = lower.startsWith("http://") || lower.startsWith("https://") || lower.contains("://")
    val looksCredentialBearing = credentialPatterns.any { it.containsMatchIn(trimmed) }
    return if (looksLikeUrl || looksCredentialBearing) safeFallback else trimmed
}

private val credentialPatterns = listOf(
    Regex("(?i)\\bapi[_-]?key\\b\\s*[:=]"),
    Regex("(?i)\\bapikey\\s*="),
    Regex("(?i)\\b(token|secret|password|passwd)\\b\\s*[:=]"),
)
