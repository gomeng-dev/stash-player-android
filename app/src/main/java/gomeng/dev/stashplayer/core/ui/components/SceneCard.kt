package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.model.sceneCardOverlayActionHorizontalPaddingDp
import gomeng.dev.stashplayer.core.ui.designsystem.StashGradientScrim
import gomeng.dev.stashplayer.core.ui.designsystem.StashIconActionButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashIconActionSemantics
import gomeng.dev.stashplayer.core.ui.designsystem.StashMediaCard
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadge
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadgeModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashSurfaceRole
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChip
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChipModel
import gomeng.dev.stashplayer.core.ui.designsystem.buildStashTagChipOverflowState
import gomeng.dev.stashplayer.core.ui.designsystem.stashSurfaceTreatment
import gomeng.dev.stashplayer.core.ui.designsystem.toStashSurfaceThemeColor
import gomeng.dev.stashplayer.core.ui.designsystem.stashThumbnailClip
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SceneCard(
    scene: SceneCardModel,
    modifier: Modifier = Modifier,
    thumbnailHeight: Dp = 160.dp,
    thumbnailModel: Any? = scene.thumbnailUrl,
    showProgress: Boolean = true,
    isLocalFavorite: Boolean = false,
    isInWatchLater: Boolean = scene.isInWatchLater,
    isInQueue: Boolean = false,
    isSelected: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleWatchLater: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    showQuickActions: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val sceneCardLongClick = onLongClick?.let { longClick ->
        {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            longClick()
        }
    }

    StashMediaCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = sceneCardLongClick,
            ),
        selected = isSelected,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbnailHeight)
                    .stashThumbnailClip()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (thumbnailModel != null) {
                    AsyncImage(
                        model = thumbnailModel,
                        contentDescription = scene.title,
                        modifier = Modifier.matchParentSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                }
                StashGradientScrim(modifier = Modifier.matchParentSize())
                if (isSelected) {
                    Badge(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                        Text(stashString(R.string.auto_kr_0298))
                    }
                }
                if (scene.metadataBadges.isNotEmpty() || isLocalFavorite || isInWatchLater) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(
                                start = 8.dp,
                                end = 52.dp,
                                bottom = if (showProgress && scene.progress > 0f) 10.dp else 8.dp,
                            ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        if (isLocalFavorite || isInWatchLater) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (isLocalFavorite) {
                                    Badge {
                                        Text(favoriteBadgeLabel())
                                    }
                                }
                                if (isInWatchLater) {
                                    Badge {
                                        Text(watchLaterBadgeLabel())
                                    }
                                }
                            }
                        }
                        if (scene.metadataBadges.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                scene.metadataBadges.take(3).forEach { badge ->
                                    StashMetadataBadge(
                                        badge = StashMetadataBadgeModel(
                                            label = badge.label,
                                            contentDescription = badge.label,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
                if (showQuickActions) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        onToggleFavorite?.let { toggle ->
                            val copy = favoriteSceneCardActionCopy(isLocalFavorite)
                            SceneCardOverlayButton(
                                icon = if (isLocalFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                copy = copy,
                                onClick = toggle,
                            )
                        }
                        onToggleWatchLater?.let { toggle ->
                            val copy = watchLaterSceneCardActionCopy(isInWatchLater)
                            SceneCardOverlayButton(
                                icon = if (isInWatchLater) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks,
                                copy = copy,
                                onClick = toggle,
                            )
                        }
                        onAddToQueue?.let { add ->
                            val copy = queueSceneCardActionCopy(isInQueue)
                            SceneCardOverlayButton(
                                icon = if (isInQueue) Icons.Outlined.Close else Icons.AutoMirrored.Outlined.PlaylistPlay,
                                copy = copy,
                                onClick = add,
                            )
                        }
                    }
                }
                if (showProgress && scene.progress > 0f) {
                    LinearProgressIndicator(
                        progress = { scene.progress },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(4.dp),
                        trackColor = Color.Transparent,
                    )
                }
                val playerChrome = stashSurfaceTreatment(StashSurfaceRole.PlayerFullscreenChrome)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = playerChrome.containerRole
                        .toStashSurfaceThemeColor()
                        .copy(alpha = playerChrome.containerAlpha),
                    contentColor = playerChrome.contentRole.toStashSurfaceThemeColor(),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = scene.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (shouldShowSceneCardInlineSubtitle(
                        subtitle = scene.subtitle,
                        hasMetadataBadges = scene.metadataBadges.isNotEmpty(),
                        hasTagChips = scene.tagChips.isNotEmpty(),
                    )
                ) {
                    Text(
                        text = scene.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (scene.tagChips.isNotEmpty()) {
                    val tagState = buildStashTagChipOverflowState(
                        tags = scene.tagChips.map { it.label },
                        maxVisible = 3,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tagState.visibleTags.forEach { tag ->
                            StashTagChip(tag = StashTagChipModel(label = tag.displayLabel))
                        }
                        if (tagState.hasOverflow) {
                            StashTagChip(tag = StashTagChipModel(label = tagState.overflowLabel))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SceneCardOverlayButton(
    icon: ImageVector,
    copy: SceneCardOverlayActionCopy,
    onClick: () -> Unit,
) {
    StashIconActionButton(
        semantics = StashIconActionSemantics(
            label = copy.contentDescription,
            selected = copy.selected,
        ),
        onClick = onClick,
        containerAlpha = 0.34f,
        selectedContainerAlpha = 0.46f,
        contentPadding = PaddingValues(
            horizontal = sceneCardOverlayActionHorizontalPaddingDp().dp,
            vertical = 0.dp,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}
