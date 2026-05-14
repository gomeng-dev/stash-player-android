package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.PlayerOverlayCenterTransportVisualPolicy
import gomeng.dev.stashplayer.core.player.PlayerOverlayTransportAction
import gomeng.dev.stashplayer.core.player.PlayerOverlayTransportButtonVisualStyle
import gomeng.dev.stashplayer.core.player.PlayerOverlayTransportUiState
import gomeng.dev.stashplayer.core.player.buildPlayerOverlayTransportButtonVisualStyles

@Composable
fun PlayerOverlayTransportControls(
    state: PlayerOverlayTransportUiState,
    isPlaying: Boolean,
    onPreviousTransport: () -> Unit,
    onPlayPause: () -> Unit,
    onNextTransport: () -> Unit,
    modifier: Modifier = Modifier,
    visualStyles: List<PlayerOverlayTransportButtonVisualStyle> = buildPlayerOverlayTransportButtonVisualStyles(
        previousEnabled = state.previousEnabled,
        playPauseEnabled = state.playPauseEnabled,
        nextEnabled = state.nextEnabled,
    ),
    containerAlpha: Float = PlayerOverlayCenterTransportVisualPolicy.ContainerAlpha,
    buttonSpacingDp: Int = PlayerOverlayCenterTransportVisualPolicy.ButtonSpacingDp,
) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = containerAlpha), CircleShape),
        horizontalArrangement = Arrangement.spacedBy(buttonSpacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerOverlayTransportButton(
            onClick = onPreviousTransport,
            contentDescription = state.previousContentDescription,
            enabled = state.previousEnabled,
            visualStyle = visualStyles.styleFor(PlayerOverlayTransportAction.Previous),
        ) {
            val style = visualStyles.styleFor(PlayerOverlayTransportAction.Previous)
            Icon(
                Icons.Outlined.SkipPrevious,
                contentDescription = null,
                tint = Color.White.copy(alpha = style.contentAlpha),
                modifier = Modifier.size(style.iconSizeDp.dp),
            )
        }
        PlayerOverlayTransportButton(
            onClick = onPlayPause,
            contentDescription = state.playPauseContentDescription,
            enabled = state.playPauseEnabled,
            visualStyle = visualStyles.styleFor(PlayerOverlayTransportAction.PlayPause),
        ) {
            val style = visualStyles.styleFor(PlayerOverlayTransportAction.PlayPause)
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF05070D).copy(alpha = style.contentAlpha),
                modifier = Modifier.size(style.iconSizeDp.dp),
            )
        }
        PlayerOverlayTransportButton(
            onClick = onNextTransport,
            contentDescription = state.nextContentDescription,
            enabled = state.nextEnabled,
            visualStyle = visualStyles.styleFor(PlayerOverlayTransportAction.Next),
        ) {
            val style = visualStyles.styleFor(PlayerOverlayTransportAction.Next)
            Icon(
                Icons.Outlined.SkipNext,
                contentDescription = null,
                tint = Color.White.copy(alpha = style.contentAlpha),
                modifier = Modifier.size(style.iconSizeDp.dp),
            )
        }
    }
}

private fun List<PlayerOverlayTransportButtonVisualStyle>.styleFor(
    action: PlayerOverlayTransportAction,
): PlayerOverlayTransportButtonVisualStyle = first { it.action == action }

@Composable
private fun PlayerOverlayTransportButton(
    onClick: () -> Unit,
    contentDescription: String,
    visualStyle: PlayerOverlayTransportButtonVisualStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val containerColor = if (visualStyle.primary) {
        Color.White.copy(alpha = visualStyle.containerAlpha)
    } else {
        Color.Black.copy(alpha = visualStyle.containerAlpha)
    }
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(visualStyle.sizeDp.dp)
            .semantics { this.contentDescription = contentDescription }
            .background(containerColor, CircleShape),
        content = content,
    )
}
