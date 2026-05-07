package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.player.PlayerErrorRecoveryAction
import gomeng.dev.stashplayer.core.player.PlayerPlaybackUiStatus
import gomeng.dev.stashplayer.core.player.PlayerStatusOverlayContent
import gomeng.dev.stashplayer.core.player.buildPlayerErrorActionUiModels
import gomeng.dev.stashplayer.core.player.playerPlaybackStatusTitle
import gomeng.dev.stashplayer.core.player.resolvePlayerStatusOverlayContent
import gomeng.dev.stashplayer.core.player.sanitizePlaybackErrorText
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashRadii

@Composable
fun PlayerStatusOverlay(
    hudText: String?,
    seekPreview: PlayerSeekPreview?,
    playbackStatus: PlayerPlaybackUiStatus,
    playbackErrorText: String?,
    canTryAlternateSource: Boolean,
    canOpenSettings: Boolean,
    canOpenNextScene: Boolean,
    previewRequestHeadersFor: (StashSpriteFrame) -> Map<String, String>,
    onRetryPlayback: () -> Unit,
    onTryAlternateSource: () -> Unit,
    onOpenNextScene: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (
            resolvePlayerStatusOverlayContent(
                status = playbackStatus,
                hasSeekPreview = seekPreview != null,
                hasHudText = hudText != null,
            )
        ) {
            PlayerStatusOverlayContent.SeekPreview -> seekPreview?.let { preview ->
                PlayerSeekPreviewOverlay(
                    preview = preview,
                    requestHeadersFor = previewRequestHeadersFor,
                )
            }
            PlayerStatusOverlayContent.PlaybackStatus -> {
                PlaybackStatusCard(
                    status = playbackStatus,
                    errorText = playbackErrorText,
                    canTryAlternateSource = canTryAlternateSource,
                    canOpenSettings = canOpenSettings,
                    canOpenNextScene = canOpenNextScene,
                    onRetryPlayback = onRetryPlayback,
                    onTryAlternateSource = onTryAlternateSource,
                    onOpenNextScene = onOpenNextScene,
                    onOpenSettings = onOpenSettings,
                )
            }
            PlayerStatusOverlayContent.Hud -> hudText?.let { text ->
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = StashAlpha.PlayerHudSurface), MaterialTheme.shapes.medium)
                        .border(1.dp, StashColors.PlayerChromeBorder, MaterialTheme.shapes.medium)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
            PlayerStatusOverlayContent.Hidden -> Unit
        }
    }
}

@Composable
private fun PlaybackStatusCard(
    status: PlayerPlaybackUiStatus,
    errorText: String?,
    canTryAlternateSource: Boolean,
    canOpenSettings: Boolean,
    canOpenNextScene: Boolean,
    onRetryPlayback: () -> Unit,
    onTryAlternateSource: () -> Unit,
    onOpenNextScene: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val title = playerPlaybackStatusTitle(status)
    val safeErrorText = sanitizePlaybackErrorText(errorText)
    val statusShape = RoundedCornerShape(StashRadii.Thumbnail)
    Column(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .background(StashColors.ScrimStrong, statusShape)
            .border(1.dp, Color.White.copy(alpha = StashAlpha.PlayerStatusCardBorder), statusShape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (status == PlayerPlaybackUiStatus.Loading || status == PlayerPlaybackUiStatus.Buffering) {
            CircularProgressIndicator(color = StashColors.PlayerStatusAccent)
        }
        Text(
            text = title,
            color = StashColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (status == PlayerPlaybackUiStatus.Error && !safeErrorText.isNullOrBlank()) {
            Text(
                text = safeErrorText,
                color = StashColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val actions = buildPlayerErrorActionUiModels(
            status = status,
            canTryAlternateSource = canTryAlternateSource,
            canOpenSettings = canOpenSettings,
            canOpenNextScene = canOpenNextScene,
        )
        if (actions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions.forEach { actionModel ->
                    val onClick = when (actionModel.action) {
                        PlayerErrorRecoveryAction.RetryPlayback -> onRetryPlayback
                        PlayerErrorRecoveryAction.TryAlternateSource -> onTryAlternateSource
                        PlayerErrorRecoveryAction.OpenSettings -> onOpenSettings
                        PlayerErrorRecoveryAction.OpenNextScene -> onOpenNextScene
                    }
                    if (actionModel.primary) {
                        Button(
                            onClick = onClick,
                            modifier = Modifier
                                .sizeIn(minWidth = actionModel.minimumTouchTargetDp.dp, minHeight = actionModel.minimumTouchTargetDp.dp)
                                .semantics { contentDescription = actionModel.contentDescription },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StashColors.PlayerStatusAccent,
                                contentColor = StashColors.Background,
                            ),
                        ) {
                            Text(actionModel.label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClick,
                            modifier = Modifier
                                .sizeIn(minWidth = actionModel.minimumTouchTargetDp.dp, minHeight = actionModel.minimumTouchTargetDp.dp)
                                .semantics { contentDescription = actionModel.contentDescription },
                            border = BorderStroke(1.dp, StashColors.PlayerChromeBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = StashColors.TextPrimary,
                            ),
                        ) {
                            Text(actionModel.label)
                        }
                    }
                }
            }
        }
    }
}
