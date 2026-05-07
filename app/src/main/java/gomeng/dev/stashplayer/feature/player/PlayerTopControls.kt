package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickAction
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionRailPolicy
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionState
import gomeng.dev.stashplayer.core.player.playerAspectRatioLabel
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.playerAspectRatioToggleContentDescription
import gomeng.dev.stashplayer.core.player.playerLockButtonContentDescription
import gomeng.dev.stashplayer.core.player.nextPlayerPlaybackModeShuffleEnabled
import gomeng.dev.stashplayer.core.player.playerPlaybackModeToggleContentDescription
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedLabel
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedOptionsContentDescription
import gomeng.dev.stashplayer.core.player.playerPlaylistButtonContentDescription
import gomeng.dev.stashplayer.core.player.playerStreamOptionsContentDescription
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashPlayerYoutubeVisualTokens
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerTopControls(
    playlistItemCount: Int,
    playbackSpeed: Float,
    aspectRatioMode: AspectRatioMode,
    shuffleEnabled: Boolean,
    canShuffleQueue: Boolean,
    quickActions: List<PlayerOverlayQuickActionState>,
    fullscreenPlayerActive: Boolean,
    canEnterPictureInPicture: Boolean,
    onToggleFullscreenPlayer: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onOpenStreamOptions: () -> Unit,
    onOpenSpeedOptions: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onTogglePlaybackMode: (Boolean) -> Unit,
    onOpenPlaylistDrawer: () -> Unit,
    onExitPlayer: () -> Unit,
    onToggleLock: () -> Unit,
    onAddCurrentSceneToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        PlayerGlassIconButton(
            onClick = onExitPlayer,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = PlayerWatchPageController.playerExitButtonContentDescription(fullscreenPlayerActive),
                tint = Color.White,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(StashPlayerYoutubeVisualTokens.TopChromeRowSpacingDp.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StashPlayerYoutubeVisualTokens.TopChromeControlSpacingDp.dp),
            ) {
                PlayerGlassIconButton(onClick = onToggleLock) {
                    Icon(Icons.Outlined.LockOpen, contentDescription = playerLockButtonContentDescription(locked = false), tint = Color.White)
                }
                PlayerGlassIconButton(onClick = onToggleFullscreenPlayer) {
                    Icon(
                        if (fullscreenPlayerActive) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                        contentDescription = PlayerWatchPageController.playerFullscreenToggleContentDescription(fullscreenPlayerActive),
                        tint = Color.White,
                    )
                }
                if (canEnterPictureInPicture) {
                    PlayerGlassIconButton(onClick = onEnterPictureInPicture) {
                        Icon(
                            Icons.Outlined.PictureInPictureAlt,
                            contentDescription = stashString(R.string.player_pip_button_content_description),
                            tint = Color.White,
                        )
                    }
                }
                PlayerTopPillButton(
                    label = stashString(R.string.auto_kr_0490),
                    contentDescription = playerStreamOptionsContentDescription(),
                    onClick = onOpenStreamOptions,
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(StashPlayerYoutubeVisualTokens.TopChromeIconSizeDp.dp))
                }
                PlayerGlassIconButton(
                    onClick = onOpenPlaylistDrawer,
                    enabled = playlistItemCount > 0,
                ) {
                    Icon(
                        Icons.Outlined.Menu,
                        contentDescription = playerPlaylistButtonContentDescription(playlistItemCount),
                        tint = Color.White.copy(alpha = if (playlistItemCount > 0) 1f else StashAlpha.DisabledContent),
                    )
                }
            }
            Row(
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
                PlayerTopPillButton(
                    label = playerAspectRatioLabel(aspectRatioMode),
                    contentDescription = playerAspectRatioToggleContentDescription(aspectRatioMode),
                    onClick = onCycleAspectRatio,
                ) {
                    Icon(Icons.Outlined.AspectRatio, contentDescription = null, modifier = Modifier.size(StashPlayerYoutubeVisualTokens.TopChromeIconSizeDp.dp))
                }
                val playbackModeToggleTarget = nextPlayerPlaybackModeShuffleEnabled(
                    shuffleEnabled = shuffleEnabled,
                    canShuffleQueue = canShuffleQueue,
                )
                PlayerTopPillButton(
                    label = if (shuffleEnabled) stashString(R.string.auto_kr_0491) else stashString(R.string.auto_kr_0492),
                    contentDescription = playerPlaybackModeToggleContentDescription(
                        shuffleEnabled = shuffleEnabled,
                        canShuffleQueue = canShuffleQueue,
                    ),
                    enabled = playbackModeToggleTarget != null,
                    onClick = { playbackModeToggleTarget?.let(onTogglePlaybackMode) },
                ) {
                    Icon(Icons.Outlined.Shuffle, contentDescription = null, modifier = Modifier.size(StashPlayerYoutubeVisualTokens.TopChromeIconSizeDp.dp))
                }
            }
            if (quickActions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = StashAlpha.PlayerQuickActionRail), RoundedCornerShape(percent = 50))
                        .border(1.dp, StashColors.PlayerChromeBorder, RoundedCornerShape(percent = 50))
                        .padding(PlayerOverlayQuickActionRailPolicy.ContainerPaddingDp.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PlayerOverlayQuickActionRailPolicy.ActionSpacingDp.dp),
                ) {
                    quickActions.forEach { action ->
                        PlayerOverlayQuickActionButton(
                            state = action,
                            onClick = when (action.action) {
                                PlayerOverlayQuickAction.Queue -> onAddCurrentSceneToQueue
                                PlayerOverlayQuickAction.Favorite -> onToggleFavorite
                                PlayerOverlayQuickAction.WatchLater -> onToggleWatchLater
                            },
                        )
                    }
                }
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
private fun PlayerOverlayQuickActionButton(
    state: PlayerOverlayQuickActionState,
    onClick: () -> Unit,
) {
    val activeTint = when (state.action) {
        PlayerOverlayQuickAction.Queue -> StashColors.QueueAction
        PlayerOverlayQuickAction.Favorite -> StashColors.FavoriteAction
        PlayerOverlayQuickAction.WatchLater -> StashColors.WatchLaterAction
    }
    val activeBackground = when (state.action) {
        PlayerOverlayQuickAction.Queue -> StashColors.QueueActionContainer.copy(alpha = StashAlpha.QueueActionSelected)
        PlayerOverlayQuickAction.Favorite -> StashColors.FavoriteActionContainer.copy(alpha = StashAlpha.FavoriteActionSelected)
        PlayerOverlayQuickAction.WatchLater -> StashColors.WatchLaterActionContainer.copy(alpha = StashAlpha.WatchLaterActionSelected)
    }
    IconButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = Modifier
            .size(PlayerOverlayQuickActionRailPolicy.ButtonTouchTargetDp.dp)
            .background(
                if (state.active) activeBackground else StashColors.PlayerChromeHighlight,
                CircleShape,
            )
            .border(1.dp, StashColors.PlayerChromeBorder, CircleShape),
    ) {
        Icon(
            imageVector = when (state.action) {
                PlayerOverlayQuickAction.Queue -> Icons.AutoMirrored.Outlined.PlaylistAdd
                PlayerOverlayQuickAction.Favorite -> if (state.active) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder
                PlayerOverlayQuickAction.WatchLater -> Icons.Outlined.Bookmarks
            },
            contentDescription = state.contentDescription,
            tint = if (state.active) activeTint else Color.White.copy(alpha = if (state.enabled) 1f else StashAlpha.DisabledContent),
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
