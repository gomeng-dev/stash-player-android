package gomeng.dev.stashplayer.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import gomeng.dev.stashplayer.core.player.PlayerPlaylistOverflowMenuState
import gomeng.dev.stashplayer.core.player.PlayerPlaylistRowAction
import gomeng.dev.stashplayer.core.player.PlayerPlaylistSwipeRevealState
import gomeng.dev.stashplayer.core.player.PlayerPlaylistUiItem
import gomeng.dev.stashplayer.core.player.closePlayerPlaylistDeleteAction
import gomeng.dev.stashplayer.core.player.closePlayerPlaylistOverflowMenu
import gomeng.dev.stashplayer.core.player.openPlayerPlaylistOverflowMenu
import gomeng.dev.stashplayer.core.player.playerPlaylistDestructiveDeleteLabel
import gomeng.dev.stashplayer.core.player.playerPlaylistDragHandleContentDescription
import gomeng.dev.stashplayer.core.player.playerPlaylistCurrentItemScrollIndex
import gomeng.dev.stashplayer.core.player.playerPlaylistDrawerTitle
import gomeng.dev.stashplayer.core.player.playerPlaylistMoveDownAccessibilityLabel
import gomeng.dev.stashplayer.core.player.playerPlaylistMoveUpAccessibilityLabel
import gomeng.dev.stashplayer.core.player.playerPlaylistOverflowMenuContentDescription
import gomeng.dev.stashplayer.core.player.playerPlaylistRemoveFromPlaylistLabel
import gomeng.dev.stashplayer.core.player.playerPlaylistSheetItemSubtitle
import gomeng.dev.stashplayer.core.player.playerPlaylistSheetItemTitle
import gomeng.dev.stashplayer.core.player.playerPlaylistSheetItemTrailingLabel
import gomeng.dev.stashplayer.core.player.playerPlaylistDragTargetIndex
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistDeleteButtonTap
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistLiveReorderPreviewPolicy
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistOverflowRemoveTap
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistRowMotionPolicy
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistRowTap
import gomeng.dev.stashplayer.core.player.resolvePlayerPlaylistSwipeEnd
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha
import gomeng.dev.stashplayer.core.ui.designsystem.StashBottomSheetContainer
import gomeng.dev.stashplayer.core.ui.designsystem.StashRadii
import gomeng.dev.stashplayer.core.ui.designsystem.StashSheetHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSheetOptionRow
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.StashSurfaceRole
import gomeng.dev.stashplayer.core.ui.designsystem.stashSurfaceTreatment
import gomeng.dev.stashplayer.core.ui.designsystem.toStashSurfaceThemeColor
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerPlaylistDrawer(
    items: List<PlayerPlaylistUiItem>,
    shuffleEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectScene: (String) -> Unit,
    onReorderScene: (String, Int) -> Unit = { _, _ -> },
    onRemoveScene: (PlayerPlaylistUiItem) -> Unit = {},
    onRequestDeleteScene: (PlayerPlaylistUiItem) -> Unit = {},
) {
    val currentItemScrollIndex = playerPlaylistCurrentItemScrollIndex(items)
    val currentSceneId = items.getOrNull(currentItemScrollIndex)?.sceneId
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentItemScrollIndex)
    val rowHeightPx = with(LocalDensity.current) { 72.dp.toPx() }
    var swipeRevealState by remember { mutableStateOf(PlayerPlaylistSwipeRevealState()) }
    var overflowMenuState by remember { mutableStateOf(PlayerPlaylistOverflowMenuState()) }
    var reorderDraggingSceneId by remember { mutableStateOf<String?>(null) }
    var reorderDragPx by remember { mutableFloatStateOf(0f) }
    val liveReorderPreviewPolicy = resolvePlayerPlaylistLiveReorderPreviewPolicy(
        visibleItems = items,
        draggingSceneId = reorderDraggingSceneId,
        verticalDragPx = reorderDragPx,
        rowHeightPx = rowHeightPx,
    )

    LaunchedEffect(currentSceneId, items.size) {
        listState.scrollToItem(currentItemScrollIndex)
    }

    val overlayTreatment = stashSurfaceTreatment(StashSurfaceRole.ModalOverlay)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                overlayTreatment.containerRole.toStashSurfaceThemeColor().copy(alpha = overlayTreatment.containerAlpha),
            )
            .clickable(
                onClick = {
                    if (swipeRevealState.revealedSceneId != null) {
                        swipeRevealState = closePlayerPlaylistDeleteAction(swipeRevealState)
                    } else {
                        onDismiss()
                    }
                },
            ),
        contentAlignment = Alignment.CenterEnd,
    ) {
        StashBottomSheetContainer(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = 280.dp, max = 360.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(onClick = {}),
            contentPadding = PaddingValues(StashSpacing.CardPadding),
            fillMaxWidth = false,
        ) {
            StashSheetHeader(
                title = playerPlaylistDrawerTitle(items.size, shuffleEnabled),
                subtitle = stashString(R.string.auto_kr_0466),
                contentDescription = stashString(R.string.auto_kr_0467),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.sceneId }) { _, item ->
                    PlayerPlaylistDrawerItem(
                        modifier = Modifier.animateItem(),
                        item = item,
                        visibleItems = items,
                        shuffleEnabled = shuffleEnabled,
                        swipeRevealState = swipeRevealState,
                        rowHeightPx = rowHeightPx,
                        reorderPreviewOffsetPx = liveReorderPreviewPolicy.rowOffsetsBySceneId[item.sceneId] ?: 0f,
                        isReorderDragging = reorderDraggingSceneId == item.sceneId,
                        onSwipeRevealStateChange = { swipeRevealState = it },
                        onReorderPreviewChange = { sceneId, dragPx ->
                            reorderDraggingSceneId = sceneId
                            reorderDragPx = dragPx
                        },
                        onReorderPreviewEnd = {
                            reorderDraggingSceneId = null
                            reorderDragPx = 0f
                        },
                        onSelectScene = onSelectScene,
                        onReorderScene = onReorderScene,
                        overflowMenuState = overflowMenuState,
                        onOverflowMenuStateChange = { overflowMenuState = it },
                        onRemoveScene = onRemoveScene,
                        onRequestDeleteScene = onRequestDeleteScene,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerPlaylistDrawerItem(
    modifier: Modifier = Modifier,
    item: PlayerPlaylistUiItem,
    visibleItems: List<PlayerPlaylistUiItem>,
    shuffleEnabled: Boolean,
    swipeRevealState: PlayerPlaylistSwipeRevealState,
    rowHeightPx: Float,
    reorderPreviewOffsetPx: Float,
    isReorderDragging: Boolean,
    onSwipeRevealStateChange: (PlayerPlaylistSwipeRevealState) -> Unit,
    onReorderPreviewChange: (String, Float) -> Unit,
    onReorderPreviewEnd: () -> Unit,
    onSelectScene: (String) -> Unit,
    onReorderScene: (String, Int) -> Unit,
    overflowMenuState: PlayerPlaylistOverflowMenuState,
    onOverflowMenuStateChange: (PlayerPlaylistOverflowMenuState) -> Unit,
    onRemoveScene: (PlayerPlaylistUiItem) -> Unit,
    onRequestDeleteScene: (PlayerPlaylistUiItem) -> Unit,
) {
    val deleteRevealWidth = 88.dp
    val density = LocalDensity.current
    val deleteRevealWidthPx = with(density) { deleteRevealWidth.toPx() }
    val isDeleteRevealed = swipeRevealState.revealedSceneId == item.sceneId
    var horizontalDragPx by remember(item.sceneId) { mutableFloatStateOf(0f) }
    val motionPolicy = resolvePlayerPlaylistRowMotionPolicy(
        isDeleteRevealed = isDeleteRevealed,
        horizontalDragPx = horizontalDragPx,
        verticalDragPx = 0f,
        deleteRevealWidthPx = deleteRevealWidthPx,
        reorderDragPx = reorderPreviewOffsetPx,
        liftForReorderDrag = isReorderDragging && reorderPreviewOffsetPx != 0f,
    )
    val motionOffsetXDp = with(density) { motionPolicy.rowOffsetXPx.toDp() }
    val animatedOffsetXDp by animateDpAsState(
        targetValue = motionOffsetXDp,
        label = "playlistDeleteRevealOffset",
    )
    val rowOffsetXDp = if (motionPolicy.usesSettledOffsetAnimation) animatedOffsetXDp else motionOffsetXDp
    val motionOffsetYDp = with(density) { motionPolicy.rowOffsetYPx.toDp() }
    val animatedOffsetYDp by animateDpAsState(
        targetValue = motionOffsetYDp,
        label = "playlistLiveReorderGapOffset",
    )
    val rowOffsetYDp = if (isReorderDragging) motionOffsetYDp else animatedOffsetYDp
    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = motionPolicy.deleteActionVisible,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteRevealWidth),
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
        ) {
            val destructiveTreatment = stashSurfaceTreatment(StashSurfaceRole.Destructive)
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = destructiveTreatment.containerRole
                        .toStashSurfaceThemeColor()
                        .copy(alpha = destructiveTreatment.containerAlpha),
                    contentColor = destructiveTreatment.contentRole.toStashSurfaceThemeColor(),
                ),
                onClick = {
                    val result = resolvePlayerPlaylistDeleteButtonTap(swipeRevealState, item)
                    onSwipeRevealStateChange(result.state)
                    if (result.action is PlayerPlaylistRowAction.RequestDeleteConfirmation) {
                        onRequestDeleteScene(item)
                    }
                },
            ) {
                Text(playerPlaylistDestructiveDeleteLabel())
            }
        }
        StashSheetOptionRow(
            modifier = Modifier
                .zIndex(motionPolicy.zIndex)
                .offset(x = rowOffsetXDp, y = rowOffsetYDp)
                .pointerInput(item.sceneId, swipeRevealState.revealedSceneId) {
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDragPx = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            horizontalDragPx += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            val result = resolvePlayerPlaylistSwipeEnd(
                                state = swipeRevealState,
                                sceneId = item.sceneId,
                                horizontalDragPx = horizontalDragPx,
                                verticalDragPx = 0f,
                            )
                            onSwipeRevealStateChange(result.state)
                            horizontalDragPx = 0f
                        },
                        onDragCancel = { horizontalDragPx = 0f },
                    )
                },
            title = playerPlaylistSheetItemTitle(item),
            subtitle = playerPlaylistSheetItemSubtitle(item, shuffleEnabled),
            leadingLabel = "${item.index}/${item.total}",
            trailingLabel = playerPlaylistSheetItemTrailingLabel(item),
            current = item.isCurrent,
            onClick = {
                val result = resolvePlayerPlaylistRowTap(swipeRevealState, item)
                onSwipeRevealStateChange(result.state)
                if (result.action is PlayerPlaylistRowAction.SelectScene) {
                    onSelectScene(item.sceneId)
                }
            },
            leadingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerPlaylistDragHandle(
                        item = item,
                        visibleItems = visibleItems,
                        rowHeightPx = rowHeightPx,
                        onDragPreviewOffsetChange = { onReorderPreviewChange(item.sceneId, it) },
                        onDragPreviewEnd = onReorderPreviewEnd,
                        onReorderScene = onReorderScene,
                    )
                    PlayerPlaylistThumbnail(
                        thumbnailUrl = item.thumbnailUrl,
                        title = item.title,
                    )
                }
            },
            trailingContent = {
                PlayerPlaylistOverflowMenu(
                    item = item,
                    state = overflowMenuState,
                    onStateChange = onOverflowMenuStateChange,
                    onRemoveScene = onRemoveScene,
                )
            },
        )
    }
}

@Composable
private fun PlayerPlaylistOverflowMenu(
    item: PlayerPlaylistUiItem,
    state: PlayerPlaylistOverflowMenuState,
    onStateChange: (PlayerPlaylistOverflowMenuState) -> Unit,
    onRemoveScene: (PlayerPlaylistUiItem) -> Unit,
) {
    Box {
        IconButton(
            onClick = {
                onStateChange(openPlayerPlaylistOverflowMenu(state, item.sceneId))
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = playerPlaylistOverflowMenuContentDescription(item),
            )
        }
        DropdownMenu(
            expanded = state.openSceneId == item.sceneId,
            onDismissRequest = { onStateChange(closePlayerPlaylistOverflowMenu(state)) },
        ) {
            DropdownMenuItem(
                text = { Text(playerPlaylistRemoveFromPlaylistLabel()) },
                onClick = {
                    val result = resolvePlayerPlaylistOverflowRemoveTap(state, item)
                    onStateChange(result.state)
                    if (result.action is PlayerPlaylistRowAction.RemoveFromPlaylist) {
                        onRemoveScene(item)
                    }
                },
            )
        }
    }
}

@Composable
private fun PlayerPlaylistDragHandle(
    item: PlayerPlaylistUiItem,
    visibleItems: List<PlayerPlaylistUiItem>,
    rowHeightPx: Float,
    onDragPreviewOffsetChange: (Float) -> Unit,
    onDragPreviewEnd: () -> Unit,
    onReorderScene: (String, Int) -> Unit,
) {
    val canMoveUp = item.index > 1
    val canMoveDown = item.index < item.total
    val customActions = buildList {
        if (canMoveUp) {
            add(
                CustomAccessibilityAction(playerPlaylistMoveUpAccessibilityLabel(item)) {
                    onReorderScene(item.sceneId, item.index - 2)
                    true
                },
            )
        }
        if (canMoveDown) {
            add(
                CustomAccessibilityAction(playerPlaylistMoveDownAccessibilityLabel(item)) {
                    onReorderScene(item.sceneId, item.index)
                    true
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 48.dp)
            .semantics {
                contentDescription = playerPlaylistDragHandleContentDescription(item)
                this.customActions = customActions
            }
            .pointerInput(item.sceneId, item.index, item.total, rowHeightPx) {
                var accumulatedVerticalDragPx = 0f
                detectVerticalDragGestures(
                    onDragStart = {
                        accumulatedVerticalDragPx = 0f
                        onDragPreviewOffsetChange(0f)
                    },
                    onVerticalDrag = { change, dragAmount ->
                        accumulatedVerticalDragPx += dragAmount
                        onDragPreviewOffsetChange(accumulatedVerticalDragPx)
                        change.consume()
                    },
                    onDragEnd = {
                        playerPlaylistDragTargetIndex(
                            visibleItems = visibleItems,
                            sceneId = item.sceneId,
                            verticalDragPx = accumulatedVerticalDragPx,
                            rowHeightPx = rowHeightPx,
                        )?.let { toIndex -> onReorderScene(item.sceneId, toIndex) }
                        accumulatedVerticalDragPx = 0f
                        onDragPreviewEnd()
                    },
                    onDragCancel = {
                        accumulatedVerticalDragPx = 0f
                        onDragPreviewEnd()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "≡",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayerPlaylistThumbnail(
    thumbnailUrl: String?,
    title: String,
) {
    Box(
        modifier = Modifier
            .size(width = 86.dp, height = 48.dp)
            .clip(RoundedCornerShape(StashRadii.Thumbnail))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.GlassStrong)),
        contentAlignment = Alignment.Center,
    ) {
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = stashString(R.string.auto_kr_0468),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
