package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.PlayerStarFillState
import gomeng.dev.stashplayer.core.player.playerStarRatingContentDescription
import gomeng.dev.stashplayer.core.player.ratingStepFromTouchPosition
import gomeng.dev.stashplayer.core.player.resolvePlayerStarRatingDragPolicy
import gomeng.dev.stashplayer.core.player.starFillStatesForRatingStep
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashStarRatingSlider(
    ratingStep: Int,
    contentDescriptionPrefix: String,
    enabled: Boolean,
    onSelectRatingStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {},
    onInteractionEnd: () -> Unit = {},
) {
    val fillStates = starFillStatesForRatingStep(ratingStep)
    val latestOnSelectRatingStep by rememberUpdatedState(onSelectRatingStep)
    val latestOnInteractionStart by rememberUpdatedState(onInteractionStart)
    val latestOnInteractionEnd by rememberUpdatedState(onInteractionEnd)
    val dragPolicy = resolvePlayerStarRatingDragPolicy(ratingUpdating = !enabled)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$contentDescriptionPrefix ${playerStarRatingContentDescription(ratingStep)}"
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.pointerInput(dragPolicy, enabled) {
                if (enabled && dragPolicy.trackPressesAcrossRow) {
                    awaitEachGesture {
                        val down = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial,
                        )
                        val rowWidth = size.width.toFloat()
                        var lastSelectedStep: Int? = null
                        fun selectStepAt(xPx: Float) {
                            val nextStep = ratingStepFromTouchPosition(xPx, rowWidth)
                            if (nextStep != lastSelectedStep) {
                                lastSelectedStep = nextStep
                                latestOnSelectRatingStep(nextStep)
                            }
                        }
                        latestOnInteractionStart()
                        try {
                            selectStepAt(down.position.x)
                            down.consume()
                            while (dragPolicy.trackMovesAcrossRow) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull { it.pressed }
                                    ?: break
                                if (!change.pressed) break
                                selectStepAt(change.position.x)
                                if (dragPolicy.consumeMoveEvents) {
                                    change.consume()
                                }
                            }
                        } finally {
                            latestOnInteractionEnd()
                        }
                    }
                }
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            fillStates.forEachIndexed { index, fillState ->
                StashStarRatingSliderButton(
                    fillState = fillState,
                    starIndex = index,
                    enabled = enabled,
                    onSelectRatingStep = onSelectRatingStep,
                )
            }
        }
    }
}

@Composable
private fun StashStarRatingSliderButton(
    fillState: PlayerStarFillState,
    starIndex: Int,
    enabled: Boolean,
    onSelectRatingStep: (Int) -> Unit,
) {
    val fullStep = (starIndex + 1) * 2
    val selectedContentDescription = playerStarRatingContentDescription(fullStep) + stashString(R.string.auto_kr_0363)
    val shape = CircleShape
    val buttonContainer = when (fillState) {
        PlayerStarFillState.Empty -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
        PlayerStarFillState.Half,
        PlayerStarFillState.Full -> Color(0xFFFACC15).copy(alpha = 0.18f)
    }
    val borderColor = when (fillState) {
        PlayerStarFillState.Empty -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
        PlayerStarFillState.Half,
        PlayerStarFillState.Full -> Color(0xFFFACC15).copy(alpha = 0.48f)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(buttonContainer)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .alpha(if (enabled) 1f else 0.56f)
            .semantics {
                contentDescription = selectedContentDescription
                if (enabled) {
                    onClick(selectedContentDescription) {
                        onSelectRatingStep(fullStep)
                        true
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = fillState.icon(),
            contentDescription = null,
            tint = if (fillState == PlayerStarFillState.Empty) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            } else {
                Color(0xFFFACC15)
            },
            modifier = Modifier.size(34.dp),
        )
    }
}

private fun PlayerStarFillState.icon(): ImageVector = when (this) {
    PlayerStarFillState.Empty -> Icons.Outlined.StarBorder
    PlayerStarFillState.Half -> Icons.AutoMirrored.Outlined.StarHalf
    PlayerStarFillState.Full -> Icons.Outlined.Star
}
