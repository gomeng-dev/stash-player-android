package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource

private const val DEBUG_ID_MAX_LENGTH = 96

internal fun playerPlaybackInfoLoadStartLogMessage(sceneId: String): String =
    "Playback info load started sceneId=${sceneId.debugId()}"

internal fun playerPlaybackInfoLoadSuccessLogMessage(
    sceneId: String,
    streamCandidateCount: Int,
    thumbnailAvailable: Boolean,
): String = "Playback info load succeeded sceneId=${sceneId.debugId()} " +
    "streamCandidateCount=${streamCandidateCount.coerceAtLeast(0)} " +
    "thumbnailAvailable=$thumbnailAvailable"

internal fun playerSimilarRecommendationsRequestStartLogMessage(
    sceneId: String,
    retryKey: Long,
): String = "Similar recommendations request started sceneId=${sceneId.debugId()} retryKey=$retryKey"

internal fun playerSimilarRecommendationsLoadedLogMessage(
    sceneId: String,
    source: SimilarVideosRecommendationSource,
    count: Int,
): String = "Similar recommendations loaded sceneId=${sceneId.debugId()} " +
    "source=$source count=${count.coerceAtLeast(0)}"

internal fun playerSimilarRecommendationClickLogMessage(
    currentSceneId: String,
    selectedSceneId: String,
    source: SimilarVideosRecommendationSource,
    recommendationFound: Boolean,
    thumbnailAvailable: Boolean,
    queueSizeBefore: Int,
    queueSizeAfter: Int,
    targetSceneId: String,
): String = "Similar recommendation clicked currentSceneId=${currentSceneId.debugId()} " +
    "selectedSceneId=${selectedSceneId.debugId()} " +
    "targetSceneId=${targetSceneId.debugId()} " +
    "source=$source recommendationFound=$recommendationFound " +
    "thumbnailAvailable=$thumbnailAvailable " +
    "queueSize=${queueSizeBefore.coerceAtLeast(0)}->${queueSizeAfter.coerceAtLeast(0)}"

internal fun playerSimilarRecommendationQueueLogMessage(
    currentSceneId: String,
    selectedSceneId: String,
    source: SimilarVideosRecommendationSource,
    recommendationFound: Boolean,
    thumbnailAvailable: Boolean,
    queueSizeBefore: Int,
    queueSizeAfter: Int,
): String = "Similar recommendation queued currentSceneId=${currentSceneId.debugId()} " +
    "selectedSceneId=${selectedSceneId.debugId()} " +
    "source=$source recommendationFound=$recommendationFound " +
    "thumbnailAvailable=$thumbnailAvailable " +
    "queueSize=${queueSizeBefore.coerceAtLeast(0)}->${queueSizeAfter.coerceAtLeast(0)}"

private fun String.debugId(): String =
    trim().ifBlank { "<blank>" }.take(DEBUG_ID_MAX_LENGTH)
