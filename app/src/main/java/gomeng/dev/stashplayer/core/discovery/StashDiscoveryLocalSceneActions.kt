package gomeng.dev.stashplayer.core.discovery

import gomeng.dev.stashplayer.core.local.buildLocalFavoriteToggleDecision
import gomeng.dev.stashplayer.core.local.buildLocalSceneQueueAddDecision
import gomeng.dev.stashplayer.core.local.buildLocalWatchLaterToggleDecision

data class StashDiscoveryLocalToggleDecision(
    val shouldEnable: Boolean,
    val feedbackText: String?,
)

data class StashDiscoveryQueueAddDecision(
    val shouldAdd: Boolean,
    val feedbackText: String,
)

fun buildStashDiscoveryFavoriteToggleDecision(
    sceneId: String,
    favoriteSceneIds: Set<String>,
): StashDiscoveryLocalToggleDecision = buildLocalFavoriteToggleDecision(sceneId, favoriteSceneIds).let { decision ->
    StashDiscoveryLocalToggleDecision(
        shouldEnable = decision.shouldEnable,
        feedbackText = decision.feedbackText,
    )
}

fun buildStashDiscoveryWatchLaterToggleDecision(
    sceneId: String,
    watchLaterSceneIds: Set<String>,
): StashDiscoveryLocalToggleDecision = buildLocalWatchLaterToggleDecision(sceneId, watchLaterSceneIds).let { decision ->
    StashDiscoveryLocalToggleDecision(
        shouldEnable = decision.shouldEnable,
        feedbackText = decision.feedbackText,
    )
}

fun buildStashDiscoveryQueueAddDecision(
    sceneId: String,
    queuedSceneIds: Set<String>,
): StashDiscoveryQueueAddDecision = buildLocalSceneQueueAddDecision(sceneId, queuedSceneIds).let { decision ->
    StashDiscoveryQueueAddDecision(
        shouldAdd = decision.shouldAdd,
        feedbackText = decision.feedback.message,
    )
}
