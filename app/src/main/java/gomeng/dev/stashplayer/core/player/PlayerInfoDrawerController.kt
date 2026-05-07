package gomeng.dev.stashplayer.core.player

import kotlin.math.abs
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

private val DEFAULT_VIDEO_SCALE_REDUCTION = 0.18f
private val DEFAULT_VIDEO_TRANSLATE_REDUCTION_PX = 48f
private val DEFAULT_MINIMUM_DRAG_DISTANCE_PX = 48f
private val DEFAULT_FLING_VELOCITY_THRESHOLD_PX_PER_SECOND = 900f
private val DEFAULT_BROAD_HEADER_DRAG_TARGET_HEIGHT_DP = 96f

enum class PlayerInfoDrawerState {
    Collapsed,
    Expanded,
}

data class PlayerInfoDrawerLayout(
    val drawerOffsetPx: Float,
    val revealFraction: Float,
    val videoScale: Float,
    val videoTranslateYPx: Float,
)

data class PlayerInfoDrawerSheetMotionState(
    val translationYPx: Float,
    val revealFraction: Float,
    val headerDragTargetHeightDp: Float,
    val handleTouchTargetHeightDp: Float,
)

data class PlayerInfoDrawerProgressiveRevealState(
    val expandedContentVisible: Boolean,
    val ratingControlsVisible: Boolean,
    val metadataVisible: Boolean,
    val similarVideosVisible: Boolean,
    val expandedContentAlpha: Float,
    val expandedContentTranslationYPx: Float,
)

data class PlayerInfoDrawerSeekRowUiState(
    val visibleInCollapsedDrawer: Boolean,
    val visibleInExpandedDrawer: Boolean,
    val renderDuplicateInsideExpandedContent: Boolean,
    val sliderEnabled: Boolean,
    val sliderFraction: Float,
)

data class PlayerInfoDrawerCollapsedPeekPolicy(
    val showTitle: Boolean,
    val showRatingChip: Boolean,
    val showSeekRow: Boolean,
    val showExpandedRatingControls: Boolean,
)

enum class PlayerInfoDrawerHeaderTapTarget {
    Handle,
    TitleRow,
    ExpandedContent,
}

data class PlayerInfoDrawerExpandedContentLayout(
    val showDebugActionInMetadataRow: Boolean,
    val showStandaloneDebugActionRow: Boolean,
)

data class PlayerInfoDrawerMotionPolicy(
    val collapsedAnchorFraction: Float,
    val expandedAnchorFraction: Float,
    val minimumDragDistancePx: Float,
    val flingVelocityThresholdPxPerSecond: Float,
    val broadHeaderDragTargetHeightDp: Float,
) {
    companion object {
        val Default = PlayerInfoDrawerMotionPolicy(
            collapsedAnchorFraction = 1f,
            expandedAnchorFraction = 0f,
            minimumDragDistancePx = DEFAULT_MINIMUM_DRAG_DISTANCE_PX,
            flingVelocityThresholdPxPerSecond = DEFAULT_FLING_VELOCITY_THRESHOLD_PX_PER_SECOND,
            broadHeaderDragTargetHeightDp = DEFAULT_BROAD_HEADER_DRAG_TARGET_HEIGHT_DP,
        )
    }
}

object PlayerInfoDrawerController {
    val DefaultCollapsedOffsetPx: Float = 240f
    val CollapsedTitleSeekMinimumPeekHeightDp: Float = 168f

    fun resolveCollapsedOffsetPx(
        measuredSheetHeightPx: Float,
        minimumVisiblePeekHeightPx: Float = CollapsedTitleSeekMinimumPeekHeightDp,
        fallbackCollapsedOffsetPx: Float = DefaultCollapsedOffsetPx,
    ): Float {
        if (measuredSheetHeightPx <= 0f) return fallbackCollapsedOffsetPx.coerceAtLeast(0f)
        val safeMinimumPeekHeightPx = minimumVisiblePeekHeightPx.coerceAtLeast(1f)
        return (measuredSheetHeightPx - safeMinimumPeekHeightPx).coerceAtLeast(0f)
    }

    fun resolveDragState(
        currentState: PlayerInfoDrawerState,
        dragDeltaPx: Float,
        thresholdPx: Float,
    ): PlayerInfoDrawerState {
        if (abs(dragDeltaPx) < thresholdPx) return currentState
        return if (dragDeltaPx < 0f) {
            PlayerInfoDrawerState.Expanded
        } else {
            PlayerInfoDrawerState.Collapsed
        }
    }

    fun resolveSettleState(
        currentState: PlayerInfoDrawerState,
        dragDeltaPx: Float,
        collapsedOffsetPx: Float,
        velocityYPxPerSecond: Float,
        policy: PlayerInfoDrawerMotionPolicy = PlayerInfoDrawerMotionPolicy.Default,
    ): PlayerInfoDrawerState {
        val flingThreshold = abs(policy.flingVelocityThresholdPxPerSecond).coerceAtLeast(1f)
        if (velocityYPxPerSecond <= -flingThreshold) return PlayerInfoDrawerState.Expanded
        if (velocityYPxPerSecond >= flingThreshold) return PlayerInfoDrawerState.Collapsed

        val minimumDragDistancePx = policy.minimumDragDistancePx.coerceAtLeast(0f)
        if (abs(dragDeltaPx) < minimumDragDistancePx) return currentState

        val safeCollapsedOffsetPx = collapsedOffsetPx.coerceAtLeast(1f)
        val layout = resolveLayout(
            state = currentState,
            dragDeltaPx = dragDeltaPx,
            collapsedOffsetPx = safeCollapsedOffsetPx,
        )
        val expandedAnchor = policy.expandedAnchorFraction.coerceIn(0f, 1f)
        val collapsedAnchor = policy.collapsedAnchorFraction.coerceIn(0f, 1f)
        val midpoint = (expandedAnchor + collapsedAnchor) / 2f
        val normalizedOffset = (layout.drawerOffsetPx / safeCollapsedOffsetPx).coerceIn(0f, 1f)
        return if (normalizedOffset <= midpoint) {
            PlayerInfoDrawerState.Expanded
        } else {
            PlayerInfoDrawerState.Collapsed
        }
    }

    fun resolveControlsVisibilityState(
        currentState: PlayerInfoDrawerState,
        controlsVisible: Boolean,
        explicitlyOpened: Boolean = false,
    ): PlayerInfoDrawerState = if (controlsVisible || (explicitlyOpened && currentState == PlayerInfoDrawerState.Expanded)) {
        currentState
    } else {
        PlayerInfoDrawerState.Collapsed
    }

    fun handleContentDescription(state: PlayerInfoDrawerState): String = when (state) {
        PlayerInfoDrawerState.Collapsed -> stashString(R.string.auto_kr_0199)
        PlayerInfoDrawerState.Expanded -> stashString(R.string.auto_kr_0200)
    }

    fun shouldToggleFromHeaderTap(target: PlayerInfoDrawerHeaderTapTarget): Boolean =
        target == PlayerInfoDrawerHeaderTapTarget.Handle

    fun resolveExpandedContentLayout(
        metadataVisible: Boolean,
    ): PlayerInfoDrawerExpandedContentLayout = PlayerInfoDrawerExpandedContentLayout(
        showDebugActionInMetadataRow = metadataVisible,
        showStandaloneDebugActionRow = false,
    )

    fun resolveSheetMotionState(
        layout: PlayerInfoDrawerLayout,
        policy: PlayerInfoDrawerMotionPolicy = PlayerInfoDrawerMotionPolicy.Default,
        handleTouchTargetHeightDp: Float,
    ): PlayerInfoDrawerSheetMotionState {
        val handleHeight = handleTouchTargetHeightDp.coerceAtLeast(1f)
        val headerHeight = policy.broadHeaderDragTargetHeightDp.coerceAtLeast(handleHeight)
        return PlayerInfoDrawerSheetMotionState(
            translationYPx = layout.drawerOffsetPx,
            revealFraction = layout.revealFraction.coerceIn(0f, 1f),
            headerDragTargetHeightDp = headerHeight,
            handleTouchTargetHeightDp = handleHeight,
        )
    }

    fun resolveProgressiveRevealState(
        revealFraction: Float,
        expandedThreshold: Float = 0.18f,
        metadataThreshold: Float = 0.32f,
        similarVideosThreshold: Float = 0.62f,
        collapsedTranslationYPx: Float = 24f,
    ): PlayerInfoDrawerProgressiveRevealState {
        val reveal = revealFraction.coerceIn(0f, 1f)
        val expandedAt = expandedThreshold.coerceIn(0f, 1f)
        val metadataAt = metadataThreshold.coerceIn(expandedAt, 1f)
        val similarAt = similarVideosThreshold.coerceIn(metadataAt, 1f)
        val alpha = ((reveal - expandedAt) / (1f - expandedAt).coerceAtLeast(0.001f)).coerceIn(0f, 1f)
        val visible = reveal >= expandedAt
        return PlayerInfoDrawerProgressiveRevealState(
            expandedContentVisible = visible,
            ratingControlsVisible = visible,
            metadataVisible = reveal >= metadataAt,
            similarVideosVisible = reveal >= similarAt,
            expandedContentAlpha = alpha,
            expandedContentTranslationYPx = collapsedTranslationYPx.coerceAtLeast(0f) * (1f - alpha),
        )
    }

    fun resolveSeekRowUiState(
        sliderFraction: Float,
        durationMs: Long,
    ): PlayerInfoDrawerSeekRowUiState = PlayerInfoDrawerSeekRowUiState(
        visibleInCollapsedDrawer = true,
        visibleInExpandedDrawer = true,
        renderDuplicateInsideExpandedContent = false,
        sliderEnabled = durationMs > 0L,
        sliderFraction = sliderFraction.coerceIn(0f, 1f),
    )

    fun resolveCollapsedPeekPolicy(
        ratingStep: Int,
        seekRowUiState: PlayerInfoDrawerSeekRowUiState,
        progressiveRevealState: PlayerInfoDrawerProgressiveRevealState,
    ): PlayerInfoDrawerCollapsedPeekPolicy = PlayerInfoDrawerCollapsedPeekPolicy(
        showTitle = true,
        showRatingChip = ratingStep > 0,
        showSeekRow = seekRowUiState.visibleInCollapsedDrawer,
        showExpandedRatingControls = progressiveRevealState.ratingControlsVisible,
    )

    fun resolveLayout(
        state: PlayerInfoDrawerState,
        dragDeltaPx: Float,
        collapsedOffsetPx: Float,
        videoScaleReduction: Float = DEFAULT_VIDEO_SCALE_REDUCTION,
        videoTranslateReductionPx: Float = DEFAULT_VIDEO_TRANSLATE_REDUCTION_PX,
    ): PlayerInfoDrawerLayout {
        val safeCollapsedOffsetPx = collapsedOffsetPx.coerceAtLeast(1f)
        val baseOffsetPx = when (state) {
            PlayerInfoDrawerState.Collapsed -> safeCollapsedOffsetPx
            PlayerInfoDrawerState.Expanded -> 0f
        }
        val drawerOffsetPx = (baseOffsetPx + dragDeltaPx).coerceIn(0f, safeCollapsedOffsetPx)
        val revealFraction = (1f - drawerOffsetPx / safeCollapsedOffsetPx).coerceIn(0f, 1f)
        return PlayerInfoDrawerLayout(
            drawerOffsetPx = drawerOffsetPx,
            revealFraction = revealFraction,
            videoScale = 1f - revealFraction * videoScaleReduction,
            videoTranslateYPx = -revealFraction * videoTranslateReductionPx,
        )
    }
}
