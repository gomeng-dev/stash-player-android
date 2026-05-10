package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.PlayerFullscreenSeekBarVisualPolicy
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.buildReusablePlayerSeekRowState
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashPlayerYoutubeVisualTokens

@Composable
fun ReusablePlayerSeekRow(
    displayedPositionMs: Long,
    durationMs: Long,
    sliderFraction: Float,
    sliderEnabled: Boolean,
    visualPolicy: PlayerFullscreenSeekBarVisualPolicy,
    onSliderFractionChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowState = buildReusablePlayerSeekRowState(
        displayedPositionMs = displayedPositionMs,
        durationMs = durationMs,
        sliderFraction = sliderFraction,
        sliderEnabled = sliderEnabled,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rowState.currentLabel,
            color = StashColors.TextPrimary.copy(
                alpha = StashPlayerYoutubeVisualTokens.BottomSheetTimePrimaryAlpha,
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(48.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            ReusableThinPlayerSeekBar(
                fraction = rowState.sliderFraction,
                enabled = rowState.sliderEnabled,
                visualPolicy = visualPolicy,
                onFractionChange = onSliderFractionChange,
                onChangeFinished = onSliderChangeFinished,
            )
        }
        Text(
            text = rowState.remainingLabel,
            color = StashColors.TextSecondary.copy(
                alpha = StashPlayerYoutubeVisualTokens.BottomSheetTimeSecondaryAlpha,
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(52.dp),
        )
    }
}

@Composable
fun ReusableThinPlayerSeekBar(
    fraction: Float,
    enabled: Boolean,
    visualPolicy: PlayerFullscreenSeekBarVisualPolicy,
    onFractionChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coercedFraction = fraction.coerceIn(0f, 1f)
    val accessibilityState = PlayerWatchPageController.buildPlayerSeekBarAccessibilityState(
        fraction = coercedFraction,
        enabled = enabled,
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(visualPolicy.touchTargetHeightDp.dp)
            .semantics {
                contentDescription = accessibilityState.contentDescription
                stateDescription = accessibilityState.stateDescription
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = accessibilityState.progressFraction,
                    range = 0f..1f,
                    steps = 0,
                )
                if (!accessibilityState.enabled) {
                    disabled()
                }
                if (accessibilityState.enabled) {
                    setProgress { targetFraction ->
                        onFractionChange(targetFraction.coerceIn(0f, 1f))
                        onChangeFinished()
                        true
                    }
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun updateFraction(x: Float) {
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        onFractionChange((x / widthPx).coerceIn(0f, 1f))
                    }
                    updateFraction(down.position.x)
                    drag(down.id) { change ->
                        updateFraction(change.position.x)
                        change.consume()
                    }
                    onChangeFinished()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(visualPolicy.restingTrackHeightDp.dp)
                .clip(CircleShape)
                .background(
                    StashColors.TextSecondary.copy(
                        alpha = StashPlayerYoutubeVisualTokens.BottomSheetSeekInactiveTrackAlpha,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(maxWidth * coercedFraction)
                .height(visualPolicy.activeTrackHeightDp.dp)
                .clip(CircleShape)
                .background(StashColors.Primary),
        )
        if (enabled) {
            val thumbSize = visualPolicy.thumbDiameterDp.dp
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = maxWidth * coercedFraction - (thumbSize / 2))
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(StashColors.Primary),
            )
        }
    }
}
