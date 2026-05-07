package gomeng.dev.stashplayer.core.player

data class PlayerSimilarRecommendationsRequestKey(
    val sceneId: String,
    val retryKey: Long,
)

data class PlayerSimilarRecommendationsRequestState(
    val inFlightKey: PlayerSimilarRecommendationsRequestKey? = null,
    val completedKey: PlayerSimilarRecommendationsRequestKey? = null,
)

private const val DEFAULT_SIMILAR_RECOMMENDATIONS_REVEAL_THRESHOLD = 0.62f

fun isSimilarRecommendationsLoadEligible(
    infoDrawerState: PlayerInfoDrawerState,
    revealFraction: Float,
    revealThreshold: Float = DEFAULT_SIMILAR_RECOMMENDATIONS_REVEAL_THRESHOLD,
): Boolean {
    if (infoDrawerState == PlayerInfoDrawerState.Expanded) return true
    return revealFraction.coerceIn(0f, 1f) >= revealThreshold.coerceIn(0f, 1f)
}

fun shouldRequestSimilarRecommendations(
    sceneId: String,
    drawerExpanded: Boolean,
    hasRequested: Boolean,
    retryKey: Long,
): Boolean {
    return drawerExpanded && sceneId.trim().isNotBlank() && !hasRequested
}

fun shouldRequestSimilarRecommendations(
    sceneId: String,
    drawerExpanded: Boolean,
    requestedKey: PlayerSimilarRecommendationsRequestKey?,
    retryKey: Long,
): Boolean {
    val normalizedSceneId = sceneId.trim()
    if (!drawerExpanded || normalizedSceneId.isBlank()) return false
    return requestedKey != PlayerSimilarRecommendationsRequestKey(
        sceneId = normalizedSceneId,
        retryKey = retryKey,
    )
}

fun shouldRequestSimilarRecommendations(
    sceneId: String,
    drawerExpanded: Boolean,
    requestState: PlayerSimilarRecommendationsRequestState,
    retryKey: Long,
): Boolean {
    val normalizedSceneId = sceneId.trim()
    if (!drawerExpanded || normalizedSceneId.isBlank()) return false
    val key = PlayerSimilarRecommendationsRequestKey(
        sceneId = normalizedSceneId,
        retryKey = retryKey,
    )
    return requestState.inFlightKey != key && requestState.completedKey != key
}

fun shouldRequestSimilarRecommendationsForWatchPage(
    sceneId: String,
    watchPageVisible: Boolean,
    requestState: PlayerSimilarRecommendationsRequestState,
    retryKey: Long,
): Boolean = shouldRequestSimilarRecommendations(
    sceneId = sceneId,
    drawerExpanded = watchPageVisible,
    requestState = requestState,
    retryKey = retryKey,
)

fun shouldRequestSimilarRecommendations(
    sceneId: String,
    infoDrawerState: PlayerInfoDrawerState,
    revealFraction: Float,
    requestState: PlayerSimilarRecommendationsRequestState,
    retryKey: Long,
): Boolean = shouldRequestSimilarRecommendations(
    sceneId = sceneId,
    drawerExpanded = isSimilarRecommendationsLoadEligible(
        infoDrawerState = infoDrawerState,
        revealFraction = revealFraction,
    ),
    requestState = requestState,
    retryKey = retryKey,
)

fun PlayerSimilarRecommendationsRequestState.markSimilarRecommendationsRequestStarted(
    key: PlayerSimilarRecommendationsRequestKey,
): PlayerSimilarRecommendationsRequestState {
    return copy(inFlightKey = key)
}

fun PlayerSimilarRecommendationsRequestState.markSimilarRecommendationsRequestCompleted(
    key: PlayerSimilarRecommendationsRequestKey,
): PlayerSimilarRecommendationsRequestState {
    return if (inFlightKey == key) {
        copy(inFlightKey = null, completedKey = key)
    } else {
        this
    }
}

fun PlayerSimilarRecommendationsRequestState.markSimilarRecommendationsRequestCancelled(
    key: PlayerSimilarRecommendationsRequestKey,
): PlayerSimilarRecommendationsRequestState {
    return if (inFlightKey == key) {
        copy(inFlightKey = null)
    } else {
        this
    }
}
