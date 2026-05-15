package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SceneBulkDeleteResult
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.StashSortDirection
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val PLAYER_QUEUE_HASH_SEED: Long = 1125899906842597L
private val PLAYER_PLAYBACK_MENU_MAX_HEIGHT_DP = 560
private val PLAYER_PLAYBACK_MENU_SCROLL_THRESHOLD = 8

data class PlayerQueueItem(
    val sceneId: String,
    val title: String,
    val thumbnailUrl: String? = null,
)

sealed interface PlayerPlaybackQueueContinuation {
    val nextPage: Int
    val pageSize: Int
    val hasMore: Boolean

    data class Browse(
        val sort: String,
        val direction: StashSortDirection,
        val videoFilter: StashVideoFilterState,
        override val nextPage: Int,
        override val pageSize: Int,
        override val hasMore: Boolean,
    ) : PlayerPlaybackQueueContinuation

    data class Explore(
        val query: String,
        val sort: String,
        val direction: StashSortDirection,
        val videoFilter: StashVideoFilterState,
        override val nextPage: Int,
        override val pageSize: Int,
        override val hasMore: Boolean,
    ) : PlayerPlaybackQueueContinuation
}

fun PlayerPlaybackQueueContinuation.afterLoadedPage(
    loadedPage: Int,
    totalCount: Int,
): PlayerPlaybackQueueContinuation {
    val nextPage = loadedPage + 1
    val hasMore = loadedPage * pageSize < totalCount
    return when (this) {
        is PlayerPlaybackQueueContinuation.Browse -> copy(nextPage = nextPage, hasMore = hasMore)
        is PlayerPlaybackQueueContinuation.Explore -> copy(nextPage = nextPage, hasMore = hasMore)
    }
}

data class PlayerPlaylistUiItem(
    val sceneId: String,
    val title: String,
    val thumbnailUrl: String?,
    val isCurrent: Boolean,
    val index: Int,
    val total: Int,
)

data class PlayerPlaybackQueue(
    val items: List<PlayerQueueItem> = emptyList(),
    val currentSceneId: String? = null,
    val shuffleEnabled: Boolean = false,
    private val shuffleGeneration: Int = 0,
    private val detachedCurrentOrderIndex: Int? = null,
    private val detachedSequentialOrderIndex: Int? = null,
    internal val sequentialItems: List<PlayerQueueItem>? = null,
) {
    val hasQueue: Boolean get() = items.size > 1

    fun withCurrent(sceneId: String): PlayerPlaybackQueue = copy(
        currentSceneId = sceneId,
        detachedCurrentOrderIndex = null,
        detachedSequentialOrderIndex = null,
    )

    fun toggleShuffle(): PlayerPlaybackQueue = withShuffleEnabled(true)

    fun withShuffleEnabled(enabled: Boolean): PlayerPlaybackQueue {
        if (!enabled) {
            return copy(
                items = sequentialItems ?: items,
                shuffleEnabled = false,
                shuffleGeneration = 0,
                detachedCurrentOrderIndex = detachedSequentialOrderIndex ?: detachedCurrentOrderIndex,
                detachedSequentialOrderIndex = null,
                sequentialItems = null,
            )
        }
        val baseSequentialItems = if (shuffleEnabled) sequentialItems ?: items else items
        var nextGeneration = shuffleGeneration
        var shuffledItems: List<PlayerQueueItem>
        var attempts = 0
        do {
            nextGeneration += 1
            shuffledItems = materializeShuffledItems(
                items = baseSequentialItems,
                currentSceneId = currentSceneId,
                generation = nextGeneration,
            )
            attempts += 1
        } while (
            shuffleEnabled &&
                shuffledItems.map { it.sceneId } == items.map { it.sceneId } &&
                attempts <= baseSequentialItems.size
        )
        if (shuffleEnabled && shuffledItems.map { it.sceneId } == items.map { it.sceneId } && shuffledItems.size > 1) {
            val currentItem = shuffledItems.firstOrNull { it.sceneId == currentSceneId }
            val remainingItems = shuffledItems.filterNot { it.sceneId == currentItem?.sceneId }
            val rotation = nextGeneration.mod(remainingItems.size).takeIf { it > 0 } ?: 1
            val rotatedRemainingItems = remainingItems.drop(rotation) + remainingItems.take(rotation)
            shuffledItems = if (currentItem != null) listOf(currentItem) + rotatedRemainingItems else rotatedRemainingItems
        }
        return copy(
            items = shuffledItems,
            shuffleEnabled = true,
            shuffleGeneration = nextGeneration,
            sequentialItems = baseSequentialItems,
        )
    }

    fun playbackOrderSceneIds(): List<String> = itemSceneIds()

    fun nextSceneId(): String? {
        val order = playbackOrderSceneIds()
        val index = order.indexOf(currentSceneId).takeIf { it >= 0 }
        if (index != null) return order.getOrNull(index + 1)
        return detachedCurrentOrderIndex?.let { order.getOrNull(it) }
    }

    fun previousSceneId(): String? {
        val order = playbackOrderSceneIds()
        val index = order.indexOf(currentSceneId).takeIf { it >= 0 }
        if (index != null) return order.getOrNull(index - 1)
        return detachedCurrentOrderIndex?.let { order.getOrNull(it - 1) }
    }

    fun trailingPlaybackItemCount(sceneId: String): Int {
        val order = playbackOrderSceneIds()
        val index = order.indexOf(sceneId).takeIf { it >= 0 } ?: return 0
        return order.lastIndex - index
    }

    private fun itemSceneIds(): List<String> = items.map { it.sceneId }

    companion object {
        val Empty = PlayerPlaybackQueue()
    }
}

fun buildPlayerPlaybackQueue(
    scenes: List<SceneCardModel>,
    selectedSceneId: String,
): PlayerPlaybackQueue = buildLoadedResultPlaybackQueue(
    scenes = scenes,
    selectedSceneId = selectedSceneId,
    randomShuffle = false,
)

fun buildLoadedResultPlaybackQueue(
    scenes: List<SceneCardModel>,
    selectedSceneId: String,
    randomShuffle: Boolean,
): PlayerPlaybackQueue {
    val loadedItems = scenes
        .distinctBy { it.id }
        .map { scene -> PlayerQueueItem(sceneId = scene.id, title = scene.title, thumbnailUrl = scene.thumbnailUrl) }
    val items = when {
        loadedItems.isEmpty() -> listOf(PlayerQueueItem(sceneId = selectedSceneId, title = selectedSceneId))
        loadedItems.any { it.sceneId == selectedSceneId } -> loadedItems
        else -> listOf(PlayerQueueItem(sceneId = selectedSceneId, title = selectedSceneId)) + loadedItems
    }
    val queue = PlayerPlaybackQueue(
        items = items,
        currentSceneId = selectedSceneId,
    )
    if (!randomShuffle || !queue.hasQueue) return queue
    return queue.withShuffleEnabled(true)
}

fun handOffLoadedResultPlaybackQueue(
    currentQueue: PlayerPlaybackQueue,
    scenes: List<SceneCardModel>,
    selectedSceneId: String,
    randomShuffle: Boolean,
): PlayerPlaybackQueue {
    val nextQueue = buildLoadedResultPlaybackQueue(
        scenes = scenes,
        selectedSceneId = selectedSceneId,
        randomShuffle = randomShuffle,
    )
    return if (
        currentQueue.items == nextQueue.items &&
        currentQueue.shuffleEnabled == nextQueue.shuffleEnabled &&
        currentQueue.playbackOrderSceneIds() == nextQueue.playbackOrderSceneIds()
    ) {
        currentQueue.withCurrent(selectedSceneId)
    } else {
        nextQueue
    }
}

fun appendLoadedResultPlaybackQueue(
    queue: PlayerPlaybackQueue,
    scenes: List<SceneCardModel>,
): PlayerPlaybackQueue {
    if (scenes.isEmpty()) return queue
    val existingIds = queue.items.mapTo(mutableSetOf()) { it.sceneId }
    val appendedItems = scenes
        .filter { scene -> existingIds.add(scene.id) }
        .map { scene -> PlayerQueueItem(sceneId = scene.id, title = scene.title, thumbnailUrl = scene.thumbnailUrl) }
    if (appendedItems.isEmpty()) return queue
    return queue.copy(
        items = queue.items + appendedItems,
        sequentialItems = queue.sequentialItems?.plus(appendedItems),
    )
}

fun handOffSingleScenePlaybackQueue(
    currentQueue: PlayerPlaybackQueue,
    sceneId: String,
): PlayerPlaybackQueue = if (
    currentQueue.items.size == 1 &&
    currentQueue.items.firstOrNull()?.sceneId == sceneId &&
    !currentQueue.shuffleEnabled
) {
    currentQueue.withCurrent(sceneId)
} else {
    buildSingleScenePlaybackQueue(sceneId)
}

fun canShowPlayerPlaylistAction(queue: PlayerPlaybackQueue): Boolean = shouldShowPlayerPlaylistAction(queue.items.size)

fun shouldShowPlayerPlaylistAction(itemCount: Int): Boolean = itemCount > 1

fun shouldLoadMorePlayerPlaylistItems(
    queue: PlayerPlaybackQueue,
    currentSceneId: String,
    minimumTrailingCount: Int,
    hasMore: Boolean,
): Boolean =
    hasMore &&
        minimumTrailingCount > 0 &&
        queue.trailingPlaybackItemCount(currentSceneId) < minimumTrailingCount

fun playerPlaybackMenuMaxHeightDp(): Int = PLAYER_PLAYBACK_MENU_MAX_HEIGHT_DP

fun playerPlaylistDrawerTitle(total: Int, shuffleEnabled: Boolean): String = buildString {
    append(stashString(R.string.auto_kr_0243))
    append(total)
    append(stashString(R.string.auto_kr_0244))
    if (shuffleEnabled) append(stashString(R.string.auto_kr_0245))
}

fun playerPlaylistDrawerItemTitle(item: PlayerPlaylistUiItem): String =
    if (item.isCurrent) "▶ ${item.title}" else item.title

fun playerPlaylistDrawerItemSubtitle(item: PlayerPlaylistUiItem, shuffleEnabled: Boolean): String = buildString {
    append(item.index)
    append("/")
    append(item.total)
    if (shuffleEnabled) append(stashString(R.string.auto_kr_0245))
}

fun playerPlaylistSheetItemTitle(item: PlayerPlaylistUiItem): String = item.title

fun playerPlaylistSheetItemSubtitle(item: PlayerPlaylistUiItem, shuffleEnabled: Boolean): String =
    playerPlaylistDrawerItemSubtitle(item, shuffleEnabled)

fun playerPlaylistSheetItemTrailingLabel(item: PlayerPlaylistUiItem): String? =
    if (item.isCurrent) stashString(R.string.auto_kr_0246) else null

fun playerPlaylistCurrentItemScrollIndex(items: List<PlayerPlaylistUiItem>): Int =
    items.indexOfFirst { it.isCurrent }.coerceAtLeast(0)

fun shouldUseScrollablePlaybackOptionsMenu(streamSourceCount: Int): Boolean =
    streamSourceCount >= PLAYER_PLAYBACK_MENU_SCROLL_THRESHOLD

fun buildPlayerPlaylistUiItems(queue: PlayerPlaybackQueue): List<PlayerPlaylistUiItem> {
    val itemsBySceneId = queue.items.associateBy { it.sceneId }
    val playbackOrder = queue.playbackOrderSceneIds()
    val total = playbackOrder.size
    return playbackOrder.mapIndexedNotNull { index, sceneId ->
        val item = itemsBySceneId[sceneId] ?: return@mapIndexedNotNull null
        PlayerPlaylistUiItem(
            sceneId = sceneId,
            title = item.title,
            thumbnailUrl = item.thumbnailUrl,
            isCurrent = sceneId == queue.currentSceneId,
            index = index + 1,
            total = total,
        )
    }
}

fun selectPlayerPlaylistItem(
    queue: PlayerPlaybackQueue,
    sceneId: String,
): PlayerPlaybackQueue = if (sceneId in queue.playbackOrderSceneIds()) {
    queue.withCurrent(sceneId)
} else {
    queue
}

fun removePlayerPlaylistItem(
    queue: PlayerPlaybackQueue,
    sceneId: String,
): PlayerPlaybackQueue {
    val currentOrder = queue.playbackOrderSceneIds()
    val removeIndex = currentOrder.indexOf(sceneId).takeIf { it >= 0 } ?: return queue
    val sequentialRemoveIndex = queue.sequentialItems
        ?.indexOfFirst { it.sceneId == sceneId }
        ?.takeIf { it >= 0 }
    val nextItems = queue.items.filterNot { it.sceneId == sceneId }
    val detachedIndex = if (sceneId == queue.currentSceneId) removeIndex else null
    val detachedSequentialIndex = if (sceneId == queue.currentSceneId) sequentialRemoveIndex else null
    return queue.copy(
        items = nextItems,
        detachedCurrentOrderIndex = detachedIndex,
        detachedSequentialOrderIndex = detachedSequentialIndex,
        sequentialItems = queue.sequentialItems?.filterNot { it.sceneId == sceneId },
    )
}

data class PlayerPlaylistDeleteCleanupResult(
    val queue: PlayerPlaybackQueue,
    val sceneToOpen: String?,
    val shouldExitPlayer: Boolean,
)

data class PlayerRecommendedQueueAppendResult(
    val queue: PlayerPlaybackQueue,
    val appendedSceneId: String,
)

fun applyPlayerPlaylistDeleteResult(
    queue: PlayerPlaybackQueue,
    currentSceneId: String,
    result: SceneBulkDeleteResult,
): PlayerPlaylistDeleteCleanupResult {
    if (result.deletedSceneIds.isEmpty()) {
        return PlayerPlaylistDeleteCleanupResult(
            queue = queue.withCurrent(currentSceneId),
            sceneToOpen = null,
            shouldExitPlayer = false,
        )
    }
    val currentQueue = queue.withCurrent(currentSceneId)
    val originalOrder = currentQueue.playbackOrderSceneIds()
    val currentIndex = originalOrder.indexOf(currentSceneId)
    val remainingItems = currentQueue.items.filterNot { it.sceneId in result.deletedSceneIds }
    val remainingOrder = originalOrder.filterNot { it in result.deletedSceneIds }
    val currentWasDeleted = currentSceneId in result.deletedSceneIds

    if (!currentWasDeleted) {
        return PlayerPlaylistDeleteCleanupResult(
            queue = currentQueue.copy(
                items = remainingItems,
                sequentialItems = currentQueue.sequentialItems?.filterNot { it.sceneId in result.deletedSceneIds },
            ),
            sceneToOpen = null,
            shouldExitPlayer = false,
        )
    }

    val replacementSceneId = remainingOrder.getOrNull(currentIndex)
        ?: remainingOrder.getOrNull(currentIndex - 1)
    if (replacementSceneId == null) {
        return PlayerPlaylistDeleteCleanupResult(
            queue = PlayerPlaybackQueue.Empty,
            sceneToOpen = null,
            shouldExitPlayer = true,
        )
    }
    return PlayerPlaylistDeleteCleanupResult(
        queue = currentQueue.copy(
            items = remainingItems,
            currentSceneId = replacementSceneId,
            detachedCurrentOrderIndex = null,
            sequentialItems = currentQueue.sequentialItems?.filterNot { it.sceneId in result.deletedSceneIds },
        ),
        sceneToOpen = replacementSceneId,
        shouldExitPlayer = false,
    )
}

fun reorderPlayerPlaylistItem(
    queue: PlayerPlaybackQueue,
    sceneId: String,
    toIndex: Int,
): PlayerPlaybackQueue {
    val fromIndex = queue.items.indexOfFirst { it.sceneId == sceneId }.takeIf { it >= 0 } ?: return queue
    if (toIndex !in queue.items.indices || fromIndex == toIndex) return queue
    val reorderedItems = queue.items.toMutableList().apply {
        val moved = removeAt(fromIndex)
        add(toIndex, moved)
    }
    return queue.copy(items = reorderedItems)
}

fun playerPlaylistDragTargetIndex(
    visibleItems: List<PlayerPlaylistUiItem>,
    sceneId: String,
    verticalDragPx: Float,
    rowHeightPx: Float,
): Int? {
    if (rowHeightPx <= 0f) return null
    val fromIndex = visibleItems.indexOfFirst { it.sceneId == sceneId }.takeIf { it >= 0 } ?: return null
    val rowsMoved = (verticalDragPx / rowHeightPx).roundToInt()
    if (rowsMoved == 0) return null
    val toIndex = (fromIndex + rowsMoved).coerceIn(visibleItems.indices)
    return toIndex.takeUnless { it == fromIndex }
}

fun reorderPlayerPlaylistItemByDrag(
    queue: PlayerPlaybackQueue,
    visibleItems: List<PlayerPlaylistUiItem>,
    sceneId: String,
    verticalDragPx: Float,
    rowHeightPx: Float,
): PlayerPlaybackQueue {
    val toIndex = playerPlaylistDragTargetIndex(
        visibleItems = visibleItems,
        sceneId = sceneId,
        verticalDragPx = verticalDragPx,
        rowHeightPx = rowHeightPx,
    ) ?: return queue
    return reorderPlayerPlaylistItem(queue, sceneId = sceneId, toIndex = toIndex)
}

fun playerPlaylistDragHandleContentDescription(item: PlayerPlaylistUiItem): String =
    stashString(R.string.auto_kr_0247, item.title, item.index, item.total)

fun playerPlaylistMoveUpAccessibilityLabel(item: PlayerPlaylistUiItem): String = stashString(R.string.auto_kr_0248)

fun playerPlaylistMoveDownAccessibilityLabel(item: PlayerPlaylistUiItem): String = stashString(R.string.auto_kr_0249)

fun updatePlaybackQueueForTransportNavigation(
    queue: PlayerPlaybackQueue,
    sceneId: String,
): PlayerPlaybackQueue = PlayerTransportController.updateCurrentScene(queue, sceneId)

fun appendSimilarSceneToPlaybackQueue(
    queue: PlayerPlaybackQueue,
    currentSceneId: String,
    sceneId: String,
    title: String,
    thumbnailUrl: String?,
): PlayerPlaybackQueue {
    val normalizedSceneId = sceneId.trim()
    if (normalizedSceneId.isBlank()) return queue.withCurrent(currentSceneId)
    val currentQueue = queue.withCurrent(currentSceneId)
    if (currentQueue.items.any { it.sceneId == normalizedSceneId }) return currentQueue
    val itemTitle = title.trim().ifBlank { normalizedSceneId }
    val newItem = PlayerQueueItem(
        sceneId = normalizedSceneId,
        title = itemTitle,
        thumbnailUrl = thumbnailUrl,
    )
    return currentQueue.copy(
        items = currentQueue.items + newItem,
        sequentialItems = currentQueue.sequentialItems?.plus(newItem),
    )
}

fun appendTopRecommendedSceneToPlaybackQueue(
    queue: PlayerPlaybackQueue,
    currentSceneId: String,
    recommendations: List<SimilarSceneRecommendation>,
): PlayerRecommendedQueueAppendResult? {
    val currentQueue = queue.withCurrent(currentSceneId)
    val existingIds = currentQueue.items.mapTo(mutableSetOf()) { it.sceneId }
    val recommendation = recommendations.firstOrNull { candidate ->
        val candidateSceneIds = candidate.recommendedQueueSceneIds()
        candidateSceneIds.isNotEmpty() && candidateSceneIds.none { it in existingIds }
    } ?: return null
    val sceneId = recommendation.recommendedQueueSceneId().takeIf { it.isNotBlank() } ?: return null
    val title = recommendation.scene.title
        .takeIf { it.isNotBlank() }
        ?: recommendation.scene.fileName
        ?: sceneId
    val thumbnailUrl = recommendation.scene.thumbnailUrl ?: recommendation.scene.spriteImageUrl
    val updatedQueue = appendSimilarSceneToPlaybackQueue(
        queue = currentQueue,
        currentSceneId = currentSceneId,
        sceneId = sceneId,
        title = title,
        thumbnailUrl = thumbnailUrl,
    )
    if (updatedQueue.items.size == currentQueue.items.size) return null
    return PlayerRecommendedQueueAppendResult(
        queue = updatedQueue,
        appendedSceneId = sceneId,
    )
}

private fun SimilarSceneRecommendation.recommendedQueueSceneId(): String =
    sceneId.trim().ifBlank { scene.id.trim() }

private fun SimilarSceneRecommendation.recommendedQueueSceneIds(): Set<String> = buildSet {
    sceneId.trim().takeIf { it.isNotBlank() }?.let(::add)
    scene.id.trim().takeIf { it.isNotBlank() }?.let(::add)
}

fun selectSimilarSceneForPlayback(
    queue: PlayerPlaybackQueue,
    currentSceneId: String,
    sceneId: String,
    title: String,
    thumbnailUrl: String?,
): PlayerPlaybackQueue {
    val normalizedSceneId = sceneId.trim()
    if (normalizedSceneId.isBlank()) return queue.withCurrent(currentSceneId)
    return appendSimilarSceneToPlaybackQueue(
        queue = queue,
        currentSceneId = currentSceneId,
        sceneId = normalizedSceneId,
        title = title,
        thumbnailUrl = thumbnailUrl,
    ).withCurrent(normalizedSceneId)
}

fun buildSingleScenePlaybackQueue(sceneId: String): PlayerPlaybackQueue = PlayerPlaybackQueue(
    items = listOf(PlayerQueueItem(sceneId = sceneId, title = sceneId)),
    currentSceneId = sceneId,
)

private fun materializeShuffledItems(
    items: List<PlayerQueueItem>,
    currentSceneId: String?,
    generation: Int,
): List<PlayerQueueItem> {
    if (items.size <= 1) return items
    val currentItem = items.firstOrNull { it.sceneId == currentSceneId }
    val remainingItems = items
        .filterNot { it.sceneId == currentItem?.sceneId }
        .sortedWith(compareBy({ stableShuffleKey(it.sceneId, generation) }, { it.sceneId }))
    val orderedItems = if (currentItem != null) listOf(currentItem) + remainingItems else remainingItems
    val currentOrder = items.map { it.sceneId }
    val materializedOrder = orderedItems.map { it.sceneId }
    if (materializedOrder != currentOrder || remainingItems.size <= 1) return orderedItems

    val rotation = generation.mod(remainingItems.size).takeIf { it > 0 } ?: 1
    val rotatedRemainingItems = remainingItems.drop(rotation) + remainingItems.take(rotation)
    return if (currentItem != null) listOf(currentItem) + rotatedRemainingItems else rotatedRemainingItems
}

private fun stableShuffleKey(sceneId: String, generation: Int): Long = sceneId.fold(
    PLAYER_QUEUE_HASH_SEED + generation * 1_000_003L,
) { acc, char ->
    acc * 31 + char.code
}.absoluteValue
