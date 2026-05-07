package gomeng.dev.stashplayer.core.player

import kotlin.math.absoluteValue
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val PLAYER_PLAYLIST_REVEAL_THRESHOLD_PX = 56f

data class PlayerPlaylistSwipeRevealState(
    val revealedSceneId: String? = null,
)

data class PlayerPlaylistOverflowMenuState(
    val openSceneId: String? = null,
)

data class PlayerPlaylistRowMotionPolicy(
    val rowOffsetXPx: Float,
    val rowOffsetYPx: Float,
    val deleteActionVisible: Boolean,
    val usesSettledOffsetAnimation: Boolean,
    val zIndex: Float,
)

data class PlayerPlaylistLiveReorderPreviewPolicy(
    val fromIndex: Int?,
    val targetIndex: Int?,
    val rowOffsetsBySceneId: Map<String, Float>,
    val hasGapPreview: Boolean,
)

sealed interface PlayerPlaylistRowAction {
    data object None : PlayerPlaylistRowAction

    data class SelectScene(
        val sceneId: String,
    ) : PlayerPlaylistRowAction

    data class RequestDeleteConfirmation(
        val sceneId: String,
        val title: String,
    ) : PlayerPlaylistRowAction

    data class RemoveFromPlaylist(
        val sceneId: String,
        val title: String,
    ) : PlayerPlaylistRowAction
}

data class PlayerPlaylistRowActionResult(
    val state: PlayerPlaylistSwipeRevealState,
    val action: PlayerPlaylistRowAction,
)

data class PlayerPlaylistOverflowActionResult(
    val state: PlayerPlaylistOverflowMenuState,
    val action: PlayerPlaylistRowAction,
)

fun playerPlaylistRemoveFromPlaylistLabel(): String = stashString(R.string.auto_kr_0250)

fun playerPlaylistDestructiveDeleteLabel(): String = stashString(R.string.auto_kr_0251)

fun playerPlaylistOverflowMenuContentDescription(item: PlayerPlaylistUiItem): String =
    stashString(R.string.auto_kr_0252, item.title)

fun revealPlayerPlaylistDeleteAction(
    state: PlayerPlaylistSwipeRevealState,
    sceneId: String,
): PlayerPlaylistSwipeRevealState = state.copy(revealedSceneId = sceneId)

fun closePlayerPlaylistDeleteAction(
    state: PlayerPlaylistSwipeRevealState,
): PlayerPlaylistSwipeRevealState = if (state.revealedSceneId == null) state else state.copy(revealedSceneId = null)

fun openPlayerPlaylistOverflowMenu(
    state: PlayerPlaylistOverflowMenuState,
    sceneId: String,
): PlayerPlaylistOverflowMenuState = state.copy(openSceneId = sceneId)

fun closePlayerPlaylistOverflowMenu(
    state: PlayerPlaylistOverflowMenuState,
): PlayerPlaylistOverflowMenuState = if (state.openSceneId == null) state else state.copy(openSceneId = null)

fun playerPlaylistRowPlacementAnimationEnabled(): Boolean = true

fun resolvePlayerPlaylistLiveReorderPreviewPolicy(
    visibleItems: List<PlayerPlaylistUiItem>,
    draggingSceneId: String?,
    verticalDragPx: Float,
    rowHeightPx: Float,
): PlayerPlaylistLiveReorderPreviewPolicy {
    val baseOffsets = visibleItems.associate { it.sceneId to 0f }.toMutableMap()
    if (draggingSceneId.isNullOrBlank() || rowHeightPx <= 0f) {
        return PlayerPlaylistLiveReorderPreviewPolicy(
            fromIndex = null,
            targetIndex = null,
            rowOffsetsBySceneId = baseOffsets,
            hasGapPreview = false,
        )
    }
    val fromIndex = visibleItems.indexOfFirst { it.sceneId == draggingSceneId }.takeIf { it >= 0 }
        ?: return PlayerPlaylistLiveReorderPreviewPolicy(
            fromIndex = null,
            targetIndex = null,
            rowOffsetsBySceneId = baseOffsets,
            hasGapPreview = false,
        )

    baseOffsets[draggingSceneId] = verticalDragPx
    val targetIndex = playerPlaylistDragTargetIndex(
        visibleItems = visibleItems,
        sceneId = draggingSceneId,
        verticalDragPx = verticalDragPx,
        rowHeightPx = rowHeightPx,
    )

    if (targetIndex != null) {
        if (targetIndex > fromIndex) {
            for (index in (fromIndex + 1)..targetIndex) {
                baseOffsets[visibleItems[index].sceneId] = -rowHeightPx
            }
        } else if (targetIndex < fromIndex) {
            for (index in targetIndex until fromIndex) {
                baseOffsets[visibleItems[index].sceneId] = rowHeightPx
            }
        }
    }

    return PlayerPlaylistLiveReorderPreviewPolicy(
        fromIndex = fromIndex,
        targetIndex = targetIndex,
        rowOffsetsBySceneId = baseOffsets,
        hasGapPreview = targetIndex != null,
    )
}

fun resolvePlayerPlaylistRowMotionPolicy(
    isDeleteRevealed: Boolean,
    horizontalDragPx: Float,
    verticalDragPx: Float,
    deleteRevealWidthPx: Float,
    reorderDragPx: Float,
    liftForReorderDrag: Boolean = reorderDragPx != 0f,
): PlayerPlaylistRowMotionPolicy {
    val settledOffsetPx = if (isDeleteRevealed) -deleteRevealWidthPx else 0f
    val horizontalDominant = horizontalDragPx.absoluteValue > verticalDragPx.absoluteValue
    val draggingHorizontally = horizontalDominant && horizontalDragPx != 0f
    val rowOffsetXPx = if (draggingHorizontally) {
        (settledOffsetPx + horizontalDragPx).coerceIn(-deleteRevealWidthPx, 0f)
    } else {
        settledOffsetPx
    }
    return PlayerPlaylistRowMotionPolicy(
        rowOffsetXPx = rowOffsetXPx,
        rowOffsetYPx = reorderDragPx,
        deleteActionVisible = isDeleteRevealed || rowOffsetXPx < 0f,
        usesSettledOffsetAnimation = !draggingHorizontally,
        zIndex = if (liftForReorderDrag) 1f else 0f,
    )
}

fun resolvePlayerPlaylistRowTap(
    state: PlayerPlaylistSwipeRevealState,
    item: PlayerPlaylistUiItem,
): PlayerPlaylistRowActionResult = if (state.revealedSceneId != null) {
    PlayerPlaylistRowActionResult(
        state = closePlayerPlaylistDeleteAction(state),
        action = PlayerPlaylistRowAction.None,
    )
} else {
    PlayerPlaylistRowActionResult(
        state = state,
        action = PlayerPlaylistRowAction.SelectScene(item.sceneId),
    )
}

fun resolvePlayerPlaylistDeleteButtonTap(
    state: PlayerPlaylistSwipeRevealState,
    item: PlayerPlaylistUiItem,
): PlayerPlaylistRowActionResult = if (state.revealedSceneId == item.sceneId) {
    PlayerPlaylistRowActionResult(
        state = closePlayerPlaylistDeleteAction(state),
        action = PlayerPlaylistRowAction.RequestDeleteConfirmation(
            sceneId = item.sceneId,
            title = item.title,
        ),
    )
} else {
    PlayerPlaylistRowActionResult(
        state = state,
        action = PlayerPlaylistRowAction.None,
    )
}

fun resolvePlayerPlaylistOverflowRemoveTap(
    state: PlayerPlaylistOverflowMenuState,
    item: PlayerPlaylistUiItem,
): PlayerPlaylistOverflowActionResult = if (state.openSceneId == item.sceneId) {
    PlayerPlaylistOverflowActionResult(
        state = closePlayerPlaylistOverflowMenu(state),
        action = PlayerPlaylistRowAction.RemoveFromPlaylist(
            sceneId = item.sceneId,
            title = item.title,
        ),
    )
} else {
    PlayerPlaylistOverflowActionResult(
        state = state,
        action = PlayerPlaylistRowAction.None,
    )
}

fun resolvePlayerPlaylistSwipeEnd(
    state: PlayerPlaylistSwipeRevealState,
    sceneId: String,
    horizontalDragPx: Float,
    verticalDragPx: Float,
    revealThresholdPx: Float = PLAYER_PLAYLIST_REVEAL_THRESHOLD_PX,
): PlayerPlaylistRowActionResult {
    val horizontalDominant = horizontalDragPx.absoluteValue > verticalDragPx.absoluteValue
    val nextState = when {
        !horizontalDominant -> state
        horizontalDragPx <= -revealThresholdPx -> revealPlayerPlaylistDeleteAction(state, sceneId)
        horizontalDragPx >= revealThresholdPx -> closePlayerPlaylistDeleteAction(state)
        else -> state
    }
    return PlayerPlaylistRowActionResult(
        state = nextState,
        action = PlayerPlaylistRowAction.None,
    )
}
