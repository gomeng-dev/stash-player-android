package gomeng.dev.stashplayer.feature.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal enum class PlayerSideControlKind {
    Brightness,
    Volume,
}

internal data class PlayerSideControlOverlayState(
    val kind: PlayerSideControlKind,
    val fraction: Float,
    val updatedAtMs: Long,
)

@Composable
internal fun PlayerSideControlSliderOverlay(
    state: PlayerSideControlOverlayState,
    visible: Boolean,
    onFractionChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    fadeDurationMs: Int = 180,
) {
    val alignment = when (state.kind) {
        PlayerSideControlKind.Brightness -> Alignment.CenterEnd
        PlayerSideControlKind.Volume -> Alignment.CenterStart
    }
    val overlayAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = fadeDurationMs),
        label = "PlayerSideControlSliderOverlayAlpha",
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Surface(
            modifier = Modifier.graphicsLayer { alpha = overlayAlpha },
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(28.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${(state.fraction.coerceIn(0f, 1f) * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                )
                PlayerVerticalSideControlSlider(
                    fraction = state.fraction,
                    onFractionChange = onFractionChange,
                    onChangeFinished = onChangeFinished,
                )
            }
        }
    }
}

@Composable
private fun PlayerVerticalSideControlSlider(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .width(40.dp)
            .height(188.dp)
            .playerVerticalSideControlSliderInput(
                onFractionChange = onFractionChange,
                onChangeFinished = onChangeFinished,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val thumbSize = 28.dp
        val boundedFraction = fraction.coerceIn(0f, 1f)
        val thumbOffsetY = (maxHeight - thumbSize) * (1f - boundedFraction)
        Box(
            modifier = Modifier
                .width(7.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(7.dp)
                .fillMaxHeight(boundedFraction)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.primary),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = thumbOffsetY)
                .size(thumbSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun Modifier.playerVerticalSideControlSliderInput(
    onFractionChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
): Modifier {
    val currentOnFractionChange by rememberUpdatedState(onFractionChange)
    val currentOnChangeFinished by rememberUpdatedState(onChangeFinished)
    return pointerInput(Unit) {
        fun fractionFor(y: Float): Float {
            val height = size.height.toFloat().takeIf { it > 0f } ?: 1f
            return (1f - (y / height)).coerceIn(0f, 1f)
        }

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            currentOnFractionChange(fractionFor(down.position.y))
            down.consume()
            var activePointerId = down.id
            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == activePointerId }
                        ?: event.changes.firstOrNull { it.pressed }?.also { activePointerId = it.id }
                        ?: break
                    if (!change.pressed) {
                        break
                    }
                    currentOnFractionChange(fractionFor(change.position.y))
                    if (change.positionChange() != Offset.Zero) {
                        change.consume()
                    }
                }
            } finally {
                currentOnChangeFinished()
            }
        }
    }
}
