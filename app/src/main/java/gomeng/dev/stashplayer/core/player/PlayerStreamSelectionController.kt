package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.network.StashStreamCandidate
import gomeng.dev.stashplayer.core.network.StashStreamPreference
import gomeng.dev.stashplayer.core.network.preferredStashStreamCandidateIndex
import gomeng.dev.stashplayer.core.network.stashStreamPreferenceFromId
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class PlayerStreamSourceSelectionDecision(
    val selectedIndex: Int,
    val shouldReprepare: Boolean,
    val reprepareStartPositionMs: Long?,
    val shouldClearPendingSeek: Boolean,
    val hudText: String,
)

data class PlayerStreamPreferenceSelectionDecision(
    val preference: StashStreamPreference,
    val selectedIndex: Int,
    val shouldReprepare: Boolean,
    val reprepareStartPositionMs: Long?,
    val shouldClearPendingSeek: Boolean,
    val hudText: String,
)

sealed class PlayerStreamFallbackDecision {
    data class TryNext(
        val nextIndex: Int,
        val startPositionMs: Long,
        val hudText: String = stashString(R.string.auto_kr_0259),
    ) : PlayerStreamFallbackDecision()

    data class RetryCurrent(
        val startPositionMs: Long,
    ) : PlayerStreamFallbackDecision()
}

object PlayerStreamSelectionController {
    fun coerceCandidateIndex(index: Int, candidateCount: Int): Int =
        if (candidateCount <= 0) 0 else index.coerceIn(0, candidateCount - 1)

    fun preferredCandidateIndex(
        candidates: List<StashStreamCandidate>,
        preference: StashStreamPreference,
        candidateCount: Int,
    ): Int {
        if (candidateCount <= 0) return 0
        return preferredStashStreamCandidateIndex(candidates, preference)
            .takeIf { it >= 0 }
            ?.coerceIn(0, candidateCount - 1)
            ?: 0
    }

    fun selectSource(
        selectedIndex: Int,
        activeCandidateIndex: Int,
        candidateCount: Int,
        currentPositionMs: Long,
        title: String,
    ): PlayerStreamSourceSelectionDecision {
        val coercedIndex = coerceCandidateIndex(selectedIndex, candidateCount)
        val shouldReprepare = coercedIndex != activeCandidateIndex
        return PlayerStreamSourceSelectionDecision(
            selectedIndex = coercedIndex,
            shouldReprepare = shouldReprepare,
            reprepareStartPositionMs = if (shouldReprepare) currentPositionMs.coerceAtLeast(0L) else null,
            shouldClearPendingSeek = shouldReprepare,
            hudText = stashString(R.string.auto_kr_0260, sanitizePlayerStreamSourceLabel(title, "Stream")),
        )
    }

    fun selectPreference(
        preferenceId: String,
        candidates: List<StashStreamCandidate>,
        candidateCount: Int,
        activeCandidateIndex: Int,
        currentPositionMs: Long,
    ): PlayerStreamPreferenceSelectionDecision {
        val preference = stashStreamPreferenceFromId(preferenceId)
        val preferredIndex = preferredCandidateIndex(
            candidates = candidates,
            preference = preference,
            candidateCount = candidateCount,
        )
        val shouldReprepare = preferredIndex != activeCandidateIndex
        return PlayerStreamPreferenceSelectionDecision(
            preference = preference,
            selectedIndex = preferredIndex,
            shouldReprepare = shouldReprepare,
            reprepareStartPositionMs = if (shouldReprepare) currentPositionMs.coerceAtLeast(0L) else null,
            shouldClearPendingSeek = shouldReprepare,
            hudText = stashString(R.string.auto_kr_0261, preference.displayName),
        )
    }

    fun resolveFallback(
        activeCandidateIndex: Int,
        candidateCount: Int,
        startPositionMs: Long,
    ): PlayerStreamFallbackDecision {
        val safeStartPositionMs = startPositionMs.coerceAtLeast(0L)
        val nextIndex = activeCandidateIndex + 1
        return if (candidateCount > 0 && nextIndex < candidateCount) {
            PlayerStreamFallbackDecision.TryNext(
                nextIndex = nextIndex,
                startPositionMs = safeStartPositionMs,
            )
        } else {
            PlayerStreamFallbackDecision.RetryCurrent(startPositionMs = safeStartPositionMs)
        }
    }

    fun activeSourceHudText(sourceLabel: String?, sourceTypeLabel: String): String =
        sanitizePlayerStreamSourceLabel(sourceLabel, sourceTypeLabel)
}
