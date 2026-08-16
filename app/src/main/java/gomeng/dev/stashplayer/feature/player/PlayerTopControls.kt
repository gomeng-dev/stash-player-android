package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickAction
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionState
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.playerLockButtonContentDescription
import gomeng.dev.stashplayer.core.player.nextPlayerPlaybackModeShuffleEnabled
import gomeng.dev.stashplayer.core.player.playerPlaybackOrientationContentDescription
import gomeng.dev.stashplayer.core.player.playerPlaybackModeLabel
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedLabel
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedOptionsContentDescription
import gomeng.dev.stashplayer.core.player.playerStreamOptionsContentDescription
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashPlayerYoutubeVisualTokens
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerTopControls(
    title: String,
    playlistItemCount: Int,
    playbackSpeed: Float,
    playbackOrientationMode: PlaybackOrientationMode,
    shuffleEnabled: Boolean,
    canShuffleQueue: Boolean,
    quickActions: List<PlayerOverlayQuickActionState>,
    fullscreenPlayerActive: Boolean,
    onToggleFullscreenPlayer: () -> Unit,
    onOpenStreamOptions: () -> Unit,
    onOpenSpeedOptions: () -> Unit,
    onTogglePlaybackOrientationMode: () -> Unit,
    onTogglePlaybackMode: (Boolean) -> Unit,
    onOpenPlaylistDrawer: () -> Unit,
    onExitPlayer: () -> Unit,
    onAddCurrentSceneToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    val playbackModeToggleTarget = nextPlayerPlaybackModeShuffleEnabled(
        shuffleEnabled = shuffleEnabled,
        canShuffleQueue = canShuffleQueue,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = StashPlayerYoutubeVisualTokens.TopChromeHorizontalInsetDp.dp,
                top = StashPlayerYoutubeVisualTokens.TopChromeTopInsetDp.dp,
                end = StashPlayerYoutubeVisualTokens.TopChromeHorizontalInsetDp.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(StashPlayerYoutubeVisualTokens.TopChromeRowSpacingDp.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(StashPlayerYoutubeVisualTokens.TopChromeControlSpacingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerGlassIconButton(onClick = onExitPlayer) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = PlayerWatchPageController.playerExitButtonContentDescription(fullscreenPlayerActive),
                    tint = Color.White,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            PlayerGlassIconButton(onClick = onOpenStreamOptions) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = playerStreamOptionsContentDescription(),
                    tint = Color.White,
                )
            }
            PlayerGlassIconButton(onClick = onToggleFullscreenPlayer) {
                Icon(
                    if (fullscreenPlayerActive) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                    contentDescription = PlayerWatchPageController.playerFullscreenToggleContentDescription(fullscreenPlayerActive),
                    tint = Color.White,
                )
            }
            Box {
                PlayerGlassIconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stashString(R.string.player_more_options_content_description),
                        tint = Color.White,
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stashString(R.string.auto_kr_0004)) },
                        enabled = playlistItemCount > 0,
                        leadingIcon = { Icon(Icons.Outlined.Menu, contentDescription = null) },
                        onClick = {
                            overflowExpanded = false
                            onOpenPlaylistDrawer()
                        },
                    )
                    quickActions.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action.contentDescription) },
                            enabled = action.enabled,
                            leadingIcon = {
                                Icon(
                                    imageVector = when (action.action) {
                                        PlayerOverlayQuickAction.Queue -> Icons.AutoMirrored.Outlined.PlaylistAdd
                                        PlayerOverlayQuickAction.Favorite -> if (action.active) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder
                                        PlayerOverlayQuickAction.WatchLater -> Icons.Outlined.Bookmarks
                                    },
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                overflowExpanded = false
                                when (action.action) {
                                    PlayerOverlayQuickAction.Queue -> onAddCurrentSceneToQueue()
                                    PlayerOverlayQuickAction.Favorite -> onToggleFavorite()
                                    PlayerOverlayQuickAction.WatchLater -> onToggleWatchLater()
                                }
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(playerPlaybackModeLabel(shuffleEnabled = true)) },
                        enabled = playbackModeToggleTarget != null,
                        leadingIcon = { Icon(Icons.Outlined.Shuffle, contentDescription = null) },
                        trailingIcon = {
                            Switch(
                                checked = shuffleEnabled,
                                onCheckedChange = null,
                                enabled = playbackModeToggleTarget != null,
                            )
                        },
                        onClick = {
                            overflowExpanded = false
                            playbackModeToggleTarget?.let(onTogglePlaybackMode)
                        },
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StashPlayerYoutubeVisualTokens.TopChromeControlSpacingDp.dp),
        ) {
            PlayerTopPillButton(
                label = playerPlaybackSpeedLabel(playbackSpeed),
                contentDescription = playerPlaybackSpeedOptionsContentDescription(playbackSpeed),
                onClick = onOpenSpeedOptions,
            ) {
                Icon(Icons.Outlined.Speed, contentDescription = null, modifier = Modifier.size(StashPlayerYoutubeVisualTokens.TopChromeIconSizeDp.dp))
            }
            PlayerGlassIconButton(onClick = onTogglePlaybackOrientationMode) {
                Icon(
                    Icons.Outlined.ScreenRotation,
                    contentDescription = playerPlaybackOrientationContentDescription(playbackOrientationMode),
                    tint = if (playbackOrientationMode == PlaybackOrientationMode.Sensor) StashColors.Primary else Color.White,
                )
            }
        }
    }
}

@Composable
private fun PlayerGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(StashPlayerYoutubeVisualTokens.TopChromeBubbleSizeDp.dp)
            .background(Color.Black.copy(alpha = StashPlayerYoutubeVisualTokens.TopChromeSurfaceAlpha), CircleShape)
            .border(1.dp, StashColors.PlayerChromeBorder, CircleShape),
        content = content,
    )
}

@Composable
private fun PlayerTopPillButton(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .widthIn(min = StashPlayerYoutubeVisualTokens.TopChromePillMinWidthDp.dp)
            .heightIn(min = StashPlayerYoutubeVisualTokens.TopChromePillMinHeightDp.dp)
            .semantics {
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = StashPlayerYoutubeVisualTokens.TopChromeSurfaceAlpha),
            contentColor = Color.White,
        ),
        border = BorderStroke(1.dp, StashColors.PlayerChromeBorder),
        contentPadding = PaddingValues(horizontal = StashPlayerYoutubeVisualTokens.TopChromePillHorizontalPaddingDp.dp, vertical = 0.dp),
    ) {
        icon()
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun PlayerLockedTopControls(
    onToggleLock: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = StashPlayerYoutubeVisualTokens.TopChromeHorizontalInsetDp.dp,
                top = StashPlayerYoutubeVisualTokens.TopChromeTopInsetDp.dp,
                end = StashPlayerYoutubeVisualTokens.TopChromeHorizontalInsetDp.dp,
            ),
        horizontalArrangement = Arrangement.End,
    ) {
        PlayerGlassIconButton(onClick = onToggleLock) {
            Icon(Icons.Outlined.Lock, contentDescription = playerLockButtonContentDescription(locked = true), tint = Color.White)
        }
    }
}
