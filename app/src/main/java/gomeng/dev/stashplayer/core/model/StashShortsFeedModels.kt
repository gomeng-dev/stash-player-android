package gomeng.dev.stashplayer.core.model

import kotlin.math.ln

const val DEFAULT_STASH_SHORTS_PAGE_SIZE = 24
const val STASH_SHORTS_MIN_DURATION_SECONDS = 60
const val STASH_SHORTS_DEFAULT_MAX_DURATION_SECONDS = 60
const val STASH_SHORTS_MAX_CONFIGURABLE_DURATION_SECONDS = 180
const val STASH_SHORTS_LOAD_MORE_REMAINING_THRESHOLD = 5
const val STASH_SHORTS_DEFAULT_PREWARM_AHEAD_COUNT = 2
const val STASH_SHORTS_SPEED_HOLD_RATE = 1.5f
const val STASH_SHORTS_PLAYBACK_FEEDBACK_VISIBLE_MS = 1_000L

enum class ShortsFeedStatus {
    Idle,
    Loading,
    Ready,
    Empty,
    Error,
}

data class ShortsFeedItem(
    val scene: SceneCardModel,
    val explicitFeedback: ShortsExplicitFeedback = ShortsExplicitFeedback.None,
    val recommendationScore: Double = 0.0,
)

data class ShortsFeedState(
    val items: List<ShortsFeedItem> = emptyList(),
    val activeIndex: Int = 0,
    val nextPage: Int = 1,
    val hasMore: Boolean = true,
    val status: ShortsFeedStatus = ShortsFeedStatus.Idle,
    val errorMessage: String? = null,
) {
    fun loading(): ShortsFeedState = copy(status = ShortsFeedStatus.Loading, errorMessage = null)

    fun withFirstPage(
        scenes: List<SceneCardModel>,
        totalCount: Int,
        perPage: Int = DEFAULT_STASH_SHORTS_PAGE_SIZE,
    ): ShortsFeedState = copy(
        items = scenes.map(::ShortsFeedItem),
        activeIndex = 0,
        nextPage = 2,
        hasMore = perPage < totalCount,
        status = if (scenes.isEmpty()) ShortsFeedStatus.Empty else ShortsFeedStatus.Ready,
        errorMessage = null,
    )

    fun failed(message: String?): ShortsFeedState = copy(
        status = ShortsFeedStatus.Error,
        errorMessage = message,
    )
}

enum class ShortsExplicitFeedback(val storageValue: String) {
    None("none"),
    Liked("liked"),
    NotInterested("not_interested");

    companion object {
        fun fromStorageValue(value: String?): ShortsExplicitFeedback = entries.firstOrNull {
            it.storageValue == value
        } ?: None
    }
}

enum class ShortsGestureTarget {
    Surface,
    ActionRail,
    SeekSlider,
}

enum class ShortsActionRailButton {
    Like,
    NotInterested,
    Delete,
}

enum class ShortsCenterPlaybackFeedback {
    Play,
    Pause,
}

enum class ShortsInteractionOutcome {
    Impression,
    Completed,
    Skipped,
    Replay,
    PlaybackError,
}

data class ShortsSpeedHoldDecision(
    val playbackSpeed: Float,
    val restoreSpeed: Float?,
    val showHud: Boolean,
)

data class ShortsInteractionRecord(
    val sceneId: String,
    val explicitFeedback: ShortsExplicitFeedback,
    val impressionCount: Int,
    val completedCount: Int,
    val skipCount: Int,
    val replayCount: Int,
    val totalWatchMs: Long,
    val lastProgress: Float,
    val tagIdsSnapshot: List<String>,
    val studioSnapshot: String?,
    val updatedAt: Long,
)

data class ShortsRecommendationSignals(
    val interactions: List<ShortsInteractionRecord> = emptyList(),
    val favoriteSceneIds: Set<String> = emptySet(),
    val watchLaterSceneIds: Set<String> = emptySet(),
    val hybridScores: Map<String, Double> = emptyMap(),
    val likedAnchorHybridScores: Map<String, Double> = emptyMap(),
    val recentSeenSceneIds: List<String> = emptyList(),
)

fun buildShortsVideoFilter(seed: Int): StashVideoFilterState = StashVideoFilterState(
    durationRange = StashDurationRange(maxSeconds = STASH_SHORTS_DEFAULT_MAX_DURATION_SECONDS),
).withStashRandomShuffleSeed(seed)

fun buildShortsVideoFilter(
    seed: Int,
    maxDurationSeconds: Int,
): StashVideoFilterState = StashVideoFilterState(
    durationRange = StashDurationRange(maxSeconds = coerceShortsMaxDurationSeconds(maxDurationSeconds)),
).withStashRandomShuffleSeed(seed)

fun coerceShortsMaxDurationSeconds(value: Int): Int =
    value.coerceIn(STASH_SHORTS_MIN_DURATION_SECONDS, STASH_SHORTS_MAX_CONFIGURABLE_DURATION_SECONDS)

fun shortsPrewarmSceneIds(
    items: List<ShortsFeedItem>,
    activeIndex: Int,
    aheadCount: Int = STASH_SHORTS_DEFAULT_PREWARM_AHEAD_COUNT,
): List<String> {
    if (items.isEmpty() || aheadCount <= 0) return emptyList()
    val start = activeIndex.coerceAtLeast(0) + 1
    if (start >= items.size) return emptyList()
    val endExclusive = (start + aheadCount).coerceAtMost(items.size)
    return items.subList(start, endExclusive).map { it.scene.id }
}

fun shortsControllerWindowSceneIds(
    items: List<ShortsFeedItem>,
    activeIndex: Int,
    aheadCount: Int = STASH_SHORTS_DEFAULT_PREWARM_AHEAD_COUNT,
): List<String> {
    if (items.isEmpty()) return emptyList()
    val active = activeIndex.coerceIn(0, items.lastIndex)
    return listOf(items[active].scene.id) + shortsPrewarmSceneIds(
        items = items,
        activeIndex = active,
        aheadCount = aheadCount,
    )
}

fun shouldLoadMoreShorts(
    activeIndex: Int,
    itemCount: Int,
    hasMore: Boolean,
    remainingThreshold: Int = STASH_SHORTS_LOAD_MORE_REMAINING_THRESHOLD,
): Boolean {
    if (!hasMore || itemCount <= 0) return false
    val safeIndex = activeIndex.coerceAtLeast(0)
    val remaining = itemCount - safeIndex - 1
    return remaining <= remainingThreshold.coerceAtLeast(0)
}

fun appendDistinctShortsPage(
    existing: List<ShortsFeedItem>,
    incoming: List<SceneCardModel>,
): List<ShortsFeedItem> {
    val seenIds = existing.map { it.scene.id }.toMutableSet()
    val distinctIncoming = incoming
        .filter { scene -> seenIds.add(scene.id) }
        .map(::ShortsFeedItem)
    return existing + distinctIncoming
}

fun ShortsFeedState.withoutDeletedShortsScenes(
    result: SceneBulkDeleteResult,
): ShortsFeedState {
    if (result.deletedSceneIds.isEmpty()) return this
    val remaining = items.filterNot { it.scene.id in result.deletedSceneIds }
    return copy(
        items = remaining,
        activeIndex = if (remaining.isEmpty()) 0 else activeIndex.coerceIn(0, remaining.lastIndex),
        status = if (remaining.isEmpty()) ShortsFeedStatus.Empty else status,
    )
}

fun toggleShortsExplicitFeedback(
    current: ShortsExplicitFeedback,
    requested: ShortsExplicitFeedback,
): ShortsExplicitFeedback = if (current == requested) {
    ShortsExplicitFeedback.None
} else {
    requested
}

fun shouldHandleShortsSurfaceGesture(
    active: Boolean,
    target: ShortsGestureTarget,
): Boolean = active && target == ShortsGestureTarget.Surface

fun shouldToggleShortsLikeOnDoubleTap(
    active: Boolean,
    target: ShortsGestureTarget,
): Boolean = shouldHandleShortsSurfaceGesture(active, target)

fun shouldToggleShortsPlaybackOnTap(
    active: Boolean,
    target: ShortsGestureTarget,
): Boolean = shouldHandleShortsSurfaceGesture(active, target)

fun shortsActionRailButtons(): List<ShortsActionRailButton> = listOf(
    ShortsActionRailButton.Like,
    ShortsActionRailButton.NotInterested,
    ShortsActionRailButton.Delete,
)

fun resolveShortsCenterPlaybackFeedback(
    active: Boolean,
    controllerReady: Boolean,
    hasError: Boolean,
    feedbackRequested: Boolean,
    playing: Boolean,
): ShortsCenterPlaybackFeedback? {
    if (!active || !controllerReady || hasError || !feedbackRequested) return null
    return if (playing) ShortsCenterPlaybackFeedback.Pause else ShortsCenterPlaybackFeedback.Play
}

fun resolveShortsSeekTarget(
    sliderFraction: Float,
    durationMs: Long,
): Long {
    if (durationMs <= 0L) return 0L
    return (durationMs * sliderFraction.coerceIn(0f, 1f)).toLong().coerceIn(0L, durationMs)
}

fun resolveShortsLongPressSpeed(
    previousSpeed: Float,
    pressed: Boolean,
): ShortsSpeedHoldDecision = if (pressed) {
    ShortsSpeedHoldDecision(
        playbackSpeed = STASH_SHORTS_SPEED_HOLD_RATE,
        restoreSpeed = previousSpeed,
        showHud = true,
    )
} else {
    ShortsSpeedHoldDecision(
        playbackSpeed = previousSpeed,
        restoreSpeed = null,
        showHud = false,
    )
}

fun resolveShortsInteractionOutcome(
    positionMs: Long,
    durationMs: Long,
    watchMs: Long,
    replayed: Boolean,
    errored: Boolean,
): ShortsInteractionOutcome {
    if (errored) return ShortsInteractionOutcome.PlaybackError
    if (replayed) return ShortsInteractionOutcome.Replay
    val progress = if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f
    if (progress >= 0.85f || (durationMs > 0L && durationMs - positionMs <= 2_000L)) {
        return ShortsInteractionOutcome.Completed
    }
    if (watchMs < 5_000L || progress < 0.25f) return ShortsInteractionOutcome.Skipped
    return ShortsInteractionOutcome.Impression
}

fun rankShortsCandidates(
    candidates: List<SceneCardModel>,
    signals: ShortsRecommendationSignals,
    seed: Int,
): List<ShortsFeedItem> {
    val interactionByScene = signals.interactions.associateBy { it.sceneId }
    val likedSignals = signals.interactions.filter { it.explicitFeedback == ShortsExplicitFeedback.Liked }
    val rejectedSignals = signals.interactions.filter { it.explicitFeedback == ShortsExplicitFeedback.NotInterested }
    val recentSeen = signals.recentSeenSceneIds.take(24).toSet()

    return candidates
        .map { scene ->
            val score = shortsCandidateScore(
                scene = scene,
                interaction = interactionByScene[scene.id],
                likedSignals = likedSignals,
                rejectedSignals = rejectedSignals,
                signals = signals,
                recentSeen = recentSeen,
                seed = seed,
            )
            ShortsFeedItem(
                scene = scene,
                explicitFeedback = interactionByScene[scene.id]?.explicitFeedback ?: ShortsExplicitFeedback.None,
                recommendationScore = score,
            )
        }
        .sortedWith(
            compareByDescending<ShortsFeedItem> { it.recommendationScore }
                .thenBy { it.scene.id },
        )
}

fun rerankShortsTail(
    feedState: ShortsFeedState,
    signals: ShortsRecommendationSignals,
    lockedAheadCount: Int = STASH_SHORTS_DEFAULT_PREWARM_AHEAD_COUNT,
    seed: Int = 0,
): ShortsFeedState {
    if (feedState.items.isEmpty()) return feedState
    val lockEndExclusive = (feedState.activeIndex.coerceAtLeast(0) + lockedAheadCount + 1)
        .coerceAtMost(feedState.items.size)
    val locked = feedState.items.take(lockEndExclusive)
    val rankedTail = rankShortsCandidates(
        candidates = feedState.items.drop(lockEndExclusive).map { it.scene },
        signals = signals,
        seed = seed,
    )
    return feedState.copy(items = locked + rankedTail)
}

fun applyShortsFeedbackToItems(
    items: List<ShortsFeedItem>,
    sceneId: String,
    feedback: ShortsExplicitFeedback,
): List<ShortsFeedItem> = items.map { item ->
    if (item.scene.id == sceneId) item.copy(explicitFeedback = feedback) else item
}

fun mergeLikedAnchorHybridScores(
    likedSceneIds: Set<String>,
    recommendationsByLikedScene: List<List<SimilarSceneRecommendation>>,
): Map<String, Double> {
    if (recommendationsByLikedScene.isEmpty()) return emptyMap()
    val scores = linkedMapOf<String, Double>()
    recommendationsByLikedScene.flatten().forEach { recommendation ->
        val sceneId = recommendation.sceneId.trim().takeIf { it.isNotBlank() }
            ?: recommendation.scene.id.trim().takeIf { it.isNotBlank() }
            ?: return@forEach
        if (sceneId in likedSceneIds) return@forEach
        val score = recommendation.score.coerceIn(0.0, 1.0)
        scores[sceneId] = maxOf(scores[sceneId] ?: 0.0, score)
    }
    return scores
}

private fun shortsCandidateScore(
    scene: SceneCardModel,
    interaction: ShortsInteractionRecord?,
    likedSignals: List<ShortsInteractionRecord>,
    rejectedSignals: List<ShortsInteractionRecord>,
    signals: ShortsRecommendationSignals,
    recentSeen: Set<String>,
    seed: Int,
): Double {
    var score = deterministicShortsJitter(scene.id, seed)
    val tagIds = scene.tagChips.map { it.id }.toSet()
    val studio = scene.studio.trim().takeIf { it.isNotBlank() }

    score += when (interaction?.explicitFeedback) {
        ShortsExplicitFeedback.Liked -> 100.0
        ShortsExplicitFeedback.NotInterested -> -120.0
        ShortsExplicitFeedback.None,
        null -> 0.0
    }
    interaction?.let {
        score += it.completedCount * 6.0
        score += it.replayCount * 9.0
        score -= it.skipCount * 8.0
        if (it.totalWatchMs > 0L) score += ln((it.totalWatchMs / 1_000.0) + 1.0)
    }
    likedSignals.forEach { signal ->
        score += tagIds.intersect(signal.tagIdsSnapshot.toSet()).size * 12.0
        if (studio != null && studio == signal.studioSnapshot) score += 10.0
    }
    rejectedSignals.forEach { signal ->
        score -= tagIds.intersect(signal.tagIdsSnapshot.toSet()).size * 14.0
        if (studio != null && studio == signal.studioSnapshot) score -= 12.0
    }
    if (scene.id in signals.favoriteSceneIds) score += 8.0
    if (scene.id in signals.watchLaterSceneIds) score += 4.0
    if (scene.id in recentSeen) score -= 18.0
    score += (signals.hybridScores[scene.id] ?: 0.0).coerceIn(0.0, 1.0) * 30.0
    score += (signals.likedAnchorHybridScores[scene.id] ?: 0.0).coerceIn(0.0, 1.0) * 50.0
    scene.playCount?.takeIf { it > 0 }?.let { score += ln(it.toDouble() + 1.0) }
    return score
}

private fun deterministicShortsJitter(sceneId: String, seed: Int): Double {
    val value = Math.floorMod(sceneId.hashCode() xor seed, 10_000)
    return value / 10_000.0
}
