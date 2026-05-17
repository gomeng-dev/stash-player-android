package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.network.StashStream

const val PLAYER_RESUME_PROMPT_MIN_POSITION_MS = 10_000L

enum class PlayerResumePromptPlacement {
    BottomStart,
}

data class ResumePlaybackPromptState(
    val resumePositionMs: Long,
    val placement: PlayerResumePromptPlacement,
    val restartLabel: String,
    val showResumeButton: Boolean,
    val defaultActionResumesPlayback: Boolean,
)

data class PlayerPictureInPictureAspectRatio(
    val width: Int,
    val height: Int,
)

fun resolvePlayerResumeStartPositionMs(
    startPositionMs: Long,
    durationSeconds: Double?,
): Long {
    val durationMs = ((durationSeconds ?: 0.0) * 1000.0).toLong().coerceAtLeast(0L)
    return if (isPlayerWatchedAtPosition(positionMs = startPositionMs, durationMs = durationMs)) {
        0L
    } else {
        startPositionMs.coerceAtLeast(0L)
    }
}

fun shouldPromptForPlayerResumePosition(
    startPositionMs: Long,
    durationSeconds: Double? = null,
): Boolean =
    startPositionMs >= PLAYER_RESUME_PROMPT_MIN_POSITION_MS &&
        resolvePlayerResumeStartPositionMs(startPositionMs, durationSeconds) > 0L

fun buildResumePlaybackPromptState(
    resumePositionMs: Long,
    restartLabel: String = "처음부터 재생",
): ResumePlaybackPromptState = ResumePlaybackPromptState(
    resumePositionMs = resumePositionMs,
    placement = PlayerResumePromptPlacement.BottomStart,
    restartLabel = restartLabel,
    showResumeButton = false,
    defaultActionResumesPlayback = true,
)

fun resolvePlayerPictureInPictureAspectRatio(
    width: Int?,
    height: Int?,
): PlayerPictureInPictureAspectRatio = PlayerPictureInPictureAspectRatio(
    width = (width?.takeIf { it > 0 } ?: 16).coerceIn(1, 239),
    height = (height?.takeIf { it > 0 } ?: 9).coerceIn(1, 239),
)

fun buildPlayerOverlayTitle(
    streamTitle: String,
    sourceLabel: String,
    isFoldLikeLayout: Boolean,
): String = buildList {
    add(streamTitle)
    add(sourceLabel)
    if (isFoldLikeLayout) add("Fold layout")
}.joinToString(" · ")

fun StashStream.toPlayerSceneCardModel(
    currentPositionMs: Long,
    durationMs: Long,
    isInWatchLater: Boolean,
): SceneCardModel = buildPlayerSceneCardModel(
    sceneId = sceneId,
    title = title,
    durationSeconds = durationSeconds,
    currentPositionMs = currentPositionMs,
    durationMs = durationMs,
    isInWatchLater = isInWatchLater,
    thumbnailUrl = thumbnailUrl,
    spriteImageUrl = spriteImageUrl,
    playCount = playCount,
)

fun buildPlayerSceneCardModel(
    sceneId: String,
    title: String,
    durationSeconds: Double?,
    currentPositionMs: Long,
    durationMs: Long,
    isInWatchLater: Boolean,
    thumbnailUrl: String?,
    spriteImageUrl: String?,
    playCount: Int?,
): SceneCardModel {
    val effectiveDurationMs = durationMs.takeIf { it > 0L }
        ?: ((durationSeconds ?: 0.0) * 1000.0).toLong().coerceAtLeast(0L)
    val progress = if (effectiveDurationMs > 0L) {
        (currentPositionMs.toFloat() / effectiveDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    return SceneCardModel(
        id = sceneId,
        title = title,
        durationText = formatPlayerPosition(effectiveDurationMs),
        studio = "Stash",
        progress = progress,
        isInWatchLater = isInWatchLater,
        thumbnailUrl = thumbnailUrl ?: spriteImageUrl,
        playCount = playCount,
    )
}
