package gomeng.dev.stashplayer.feature.queue

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.local.localPlaybackHistoryDisplayLimit
import gomeng.dev.stashplayer.core.local.queueClearUndoFeedback
import gomeng.dev.stashplayer.core.local.watchLaterAddedToQueueFeedback
import gomeng.dev.stashplayer.core.local.watchLaterRemoveUndoFeedback
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPill
import gomeng.dev.stashplayer.core.ui.designsystem.StashCompactActionRow
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyState
import gomeng.dev.stashplayer.core.ui.designsystem.StashEmptyStateModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashGhostButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashListMediaRow
import gomeng.dev.stashplayer.core.ui.designsystem.StashScreenHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSectionHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSectionHeaderModel
import gomeng.dev.stashplayer.core.ui.components.rememberStashThumbnailModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun QueueRoute(
    isFoldLikeLayout: Boolean,
    currentSceneId: String?,
    onOpenScene: (String, List<SceneCardModel>, Boolean) -> Unit,
    onOpenBrowse: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember(context) { StashLocalLibraryRepository(context) }
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val queueScenes by repository.queueScenes.collectAsState(initial = emptyList())
    val watchLaterScenes by repository.watchLaterScenes.collectAsState(initial = emptyList())
    val favoriteScenes by repository.favoriteScenes.collectAsState(initial = emptyList())
    val playbackHistoryScenes by repository.playbackHistoryScenes.collectAsState(initial = emptyList())
    val serverProfile by settingsRepository.serverProfile.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var playbackHistoryDetailOpen by remember { mutableStateOf(false) }
    val playbackHistoryOverview = buildPlaybackHistoryOverviewModel(
        historyCount = playbackHistoryScenes.size,
        displayLimit = localPlaybackHistoryDisplayLimit(),
    )

    BackHandler(enabled = playbackHistoryDetailOpen) {
        playbackHistoryDetailOpen = false
    }

    fun openQueue(shuffle: Boolean, selectedSceneId: String? = null) {
        val request = buildQueuePlaybackRequest(
            scenes = queueScenes,
            shuffle = shuffle,
            selectedSceneId = selectedSceneId,
        ) ?: return
        onOpenScene(request.selectedSceneId, request.scenes, request.randomShuffle)
    }

    fun moveQueuedScene(sceneId: String, direction: QueueMoveDirection) {
        val movedScenes = moveQueueScene(queueScenes, sceneId, direction)
        if (movedScenes == queueScenes) return
        scope.launch { repository.restoreQueue(movedScenes) }
    }

    fun addWatchLaterToQueue(scene: SceneCardModel) {
        if (!buildWatchLaterItemQueueActionState(scene.id, queueScenes).enabled) return
        scope.launch {
            repository.addToQueue(scene)
            val feedback = watchLaterAddedToQueueFeedback(count = 1)
            snackbarHostState.showSnackbar(
                message = feedback.message,
                withDismissAction = feedback.withDismissAction,
            )
        }
    }

    fun addHistoryToQueue(scene: SceneCardModel) {
        if (!buildWatchLaterItemQueueActionState(scene.id, queueScenes).enabled) return
        scope.launch {
            repository.addToQueue(scene)
            val feedback = watchLaterAddedToQueueFeedback(count = 1)
            snackbarHostState.showSnackbar(
                message = feedback.message,
                withDismissAction = feedback.withDismissAction,
            )
        }
    }

    fun addAllWatchLaterToQueue() {
        val candidates = buildWatchLaterQueueAddCandidates(
            watchLaterScenes = watchLaterScenes,
            queueScenes = queueScenes,
        )
        if (candidates.isEmpty()) return
        scope.launch {
            repository.addAllToQueue(candidates)
            val feedback = watchLaterAddedToQueueFeedback(count = candidates.size)
            snackbarHostState.showSnackbar(
                message = feedback.message,
                withDismissAction = feedback.withDismissAction,
            )
        }
    }

    fun clearQueueWithUndo() {
        val snapshot = queueScenes
        if (snapshot.isEmpty()) return
        scope.launch {
            repository.clearQueue()
            val feedback = queueClearUndoFeedback()
            val result = snackbarHostState.showSnackbar(
                message = feedback.message,
                actionLabel = feedback.actionLabel,
                withDismissAction = feedback.withDismissAction,
            )
            if (
                result == SnackbarResult.ActionPerformed &&
                shouldRestoreClearedQueue(repository.queueSceneCount())
            ) {
                repository.restoreQueue(snapshot)
            }
        }
    }

    fun removeWatchLaterWithUndo(scene: SceneCardModel) {
        scope.launch {
            repository.removeFromWatchLater(scene.id)
            val feedback = watchLaterRemoveUndoFeedback()
            val result = snackbarHostState.showSnackbar(
                message = feedback.message,
                actionLabel = feedback.actionLabel,
                withDismissAction = feedback.withDismissAction,
            )
            if (
                result == SnackbarResult.ActionPerformed &&
                shouldRestoreRemovedWatchLater(repository.watchLaterSceneIds.first(), scene.id)
            ) {
                repository.setWatchLater(scene, true)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(if (isFoldLikeLayout) 24.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        item {
            StashScreenHeader(
                title = stashString(R.string.auto_kr_0004),
                subtitle = stashString(R.string.auto_kr_0505),
            )
        }

        item {
            StashSectionHeader(
                state = StashSectionHeaderModel(
                    title = playbackHistoryOverview.title,
                    itemCount = playbackHistoryOverview.itemCount,
                    subtitle = playbackHistoryOverview.subtitle,
                    actionLabel = if (playbackHistoryOverview.actionEnabled) playbackHistoryOverview.actionLabel else null,
                ),
                onActionClick = if (playbackHistoryOverview.actionEnabled) {
                    { playbackHistoryDetailOpen = true }
                } else {
                    null
                },
            )
        }
        if (playbackHistoryDetailOpen) {
            item {
                StashGhostButton(
                    text = "← 재생 대기열로",
                    onClick = { playbackHistoryDetailOpen = false },
                    contentDescription = "재생 대기열로 돌아가기",
                )
            }
            if (playbackHistoryScenes.isEmpty()) {
                item {
                    StashEmptyState(
                        state = StashEmptyStateModel(
                            title = "아직 재생 기록이 없어요",
                            message = "영상을 재생하면 가장 최근 항목부터 여기에 쌓입니다.",
                            primaryActionLabel = stashString(R.string.auto_kr_0509),
                        ),
                        onPrimaryAction = onOpenBrowse,
                    )
                }
            } else {
                items(playbackHistoryScenes, key = { "history-${it.id}" }) { scene ->
                    val queueAction = buildWatchLaterItemQueueActionState(scene.id, queueScenes)
                    QueueSceneRow(
                        scene = scene,
                        onOpenScene = { sceneId -> onOpenScene(sceneId, playbackHistoryScenes, false) },
                        serverProfile = serverProfile,
                        actionLabel = queueAction.label,
                        actionEnabled = queueAction.enabled,
                        onAction = { addHistoryToQueue(scene) },
                    )
                }
            }
        } else if (playbackHistoryScenes.isEmpty()) {
            item {
                StashEmptyState(
                    state = StashEmptyStateModel(
                        title = "아직 재생 기록이 없어요",
                        message = "영상을 재생하면 가장 최근 항목부터 여기에 쌓입니다.",
                        primaryActionLabel = stashString(R.string.auto_kr_0509),
                    ),
                    onPrimaryAction = onOpenBrowse,
                )
            }
        }

        if (!playbackHistoryDetailOpen) {
        item {
            StashSectionHeader(
                state = StashSectionHeaderModel(
                    title = stashString(R.string.auto_kr_0506),
                    itemCount = queueScenes.size,
                ),
            )
        }
        if (queueScenes.isNotEmpty()) {
            item {
                QueueBulkActionRow(
                    onPlayAll = { openQueue(shuffle = false) },
                    onShuffleAll = { openQueue(shuffle = true) },
                    onClearQueue = ::clearQueueWithUndo,
                )
            }
        }
        if (queueScenes.isEmpty()) {
            item {
                StashEmptyState(
                    state = StashEmptyStateModel(
                        title = stashString(R.string.auto_kr_0507),
                        message = stashString(R.string.auto_kr_0508),
                        primaryActionLabel = stashString(R.string.auto_kr_0509),
                    ),
                    onPrimaryAction = onOpenBrowse,
                )
            }
        } else {
            items(queueScenes, key = { "queue-${it.id}" }) { scene ->
                val index = queueScenes.indexOfFirst { it.id == scene.id }
                val rowContext = buildQueueRowContext(
                    scenes = queueScenes,
                    sceneId = scene.id,
                    currentSceneId = currentSceneId,
                )
                QueueSceneRow(
                    scene = scene,
                    rowContext = rowContext,
                    onOpenScene = { sceneId -> openQueue(shuffle = false, selectedSceneId = sceneId) },
                    serverProfile = serverProfile,
                    actionLabel = stashString(R.string.auto_kr_0504),
                    onAction = { scope.launch { repository.removeFromQueue(scene.id) } },
                    moveUpLabel = "↑",
                    onMoveUp = { moveQueuedScene(scene.id, QueueMoveDirection.Up) },
                    moveUpEnabled = index > 0,
                    moveDownLabel = "↓",
                    onMoveDown = { moveQueuedScene(scene.id, QueueMoveDirection.Down) },
                    moveDownEnabled = index >= 0 && index < queueScenes.lastIndex,
                )
            }
        }

        item {
            StashSectionHeader(
                state = StashSectionHeaderModel(
                    title = stashString(R.string.auto_kr_0016),
                    itemCount = watchLaterScenes.size,
                    subtitle = stashString(R.string.auto_kr_0510),
                ),
            )
        }
        if (watchLaterScenes.isNotEmpty()) {
            item {
                WatchLaterBulkActionRow(
                    onAddAllToQueue = ::addAllWatchLaterToQueue,
                    addAllEnabled = buildWatchLaterQueueAddCandidates(watchLaterScenes, queueScenes).isNotEmpty(),
                )
            }
        }
        if (watchLaterScenes.isEmpty()) {
            item {
                StashEmptyState(
                    state = StashEmptyStateModel(
                        title = stashString(R.string.auto_kr_0511),
                        message = stashString(R.string.auto_kr_0512),
                        primaryActionLabel = stashString(R.string.auto_kr_0513),
                    ),
                    onPrimaryAction = onOpenSearch,
                )
            }
        } else {
            items(watchLaterScenes, key = { "later-${it.id}" }) { scene ->
                val queueAction = buildWatchLaterItemQueueActionState(scene.id, queueScenes)
                QueueSceneRow(
                    scene = scene,
                    onOpenScene = { sceneId -> onOpenScene(sceneId, watchLaterScenes, false) },
                    serverProfile = serverProfile,
                    actionLabel = queueAction.label,
                    actionEnabled = queueAction.enabled,
                    onAction = { addWatchLaterToQueue(scene) },
                    secondaryActionLabel = stashString(R.string.auto_kr_0144),
                    onSecondaryAction = { removeWatchLaterWithUndo(scene) },
                )
            }
        }

        item {
            StashSectionHeader(
                state = StashSectionHeaderModel(
                    title = stashString(R.string.auto_kr_0238),
                    itemCount = favoriteScenes.size,
                ),
            )
        }
        if (favoriteScenes.isEmpty()) {
            item {
                StashEmptyState(
                    state = StashEmptyStateModel(
                        title = stashString(R.string.auto_kr_0514),
                        message = stashString(R.string.auto_kr_0515),
                        primaryActionLabel = stashString(R.string.auto_kr_0509),
                    ),
                    onPrimaryAction = onOpenBrowse,
                )
            }
        } else {
            items(favoriteScenes, key = { "favorite-${it.id}" }) { scene ->
                QueueSceneRow(
                    scene = scene,
                    onOpenScene = { sceneId -> onOpenScene(sceneId, favoriteScenes, false) },
                    serverProfile = serverProfile,
                    actionLabel = stashString(R.string.auto_kr_0144),
                    onAction = { scope.launch { repository.setFavorite(scene, false) } },
                )
            }
        }
    }
}

}

}

@Composable
private fun QueueBulkActionRow(
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onClearQueue: () -> Unit,
) {
    StashCompactActionRow(
        actions = buildQueueBulkActionModels(),
        onActionClick = { actionId ->
            when (actionId) {
                "play_all" -> onPlayAll()
                "shuffle_all" -> onShuffleAll()
                "clear_queue" -> onClearQueue()
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun WatchLaterBulkActionRow(
    onAddAllToQueue: () -> Unit,
    addAllEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StashCompactActionRow(
            actions = buildWatchLaterBulkActionModels(addAllEnabled),
            onActionClick = { actionId ->
                if (actionId == "add_all_to_queue") {
                    onAddAllToQueue()
                }
            },
        )
        Text(
            text = if (addAllEnabled) {
                stashString(R.string.auto_kr_0516)
            } else {
                stashString(R.string.auto_kr_0517)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QueueSceneRow(
    scene: SceneCardModel,
    rowContext: QueueRowContext? = null,
    onOpenScene: (String) -> Unit,
    serverProfile: StashServerProfile?,
    actionLabel: String,
    actionEnabled: Boolean = true,
    onAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    moveUpLabel: String? = null,
    onMoveUp: (() -> Unit)? = null,
    moveUpEnabled: Boolean = false,
    moveDownLabel: String? = null,
    onMoveDown: (() -> Unit)? = null,
    moveDownEnabled: Boolean = false,
) {
    val visualModel = buildQueueSceneVisualModel(
        scene = scene,
        rowContext = rowContext,
        actionLabel = actionLabel,
        actionEnabled = actionEnabled,
        secondaryActionLabel = secondaryActionLabel,
        moveUpLabel = moveUpLabel,
        moveUpEnabled = moveUpEnabled,
        moveDownLabel = moveDownLabel,
        moveDownEnabled = moveDownEnabled,
    )
    val thumbnailModel = rememberStashThumbnailModel(visualModel.thumbnailUrl, serverProfile)
    StashListMediaRow(
        title = visualModel.title,
        subtitle = visualModel.subtitle,
        thumbnailModel = thumbnailModel,
        thumbnailContentDescription = visualModel.title,
        progress = visualModel.progress,
        currentLabel = visualModel.currentLabel,
        current = visualModel.current,
        contentDescription = visualModel.accessibilityLabel,
        onClick = { onOpenScene(scene.id) },
        trailingActions = {
            visualModel.actions.forEach { action ->
                when (action.style) {
                    QueueVisualActionStyle.CompactPill -> StashActionPill(
                        label = action.label,
                        onClick = { handleQueueRowAction(action.id, onAction, onSecondaryAction, onMoveUp, onMoveDown) },
                        enabled = action.enabled,
                        destructive = action.destructive,
                        contentDescription = action.accessibilityLabel,
                    )

                    QueueVisualActionStyle.Ghost -> StashGhostButton(
                        text = action.label,
                        onClick = { handleQueueRowAction(action.id, onAction, onSecondaryAction, onMoveUp, onMoveDown) },
                        enabled = action.enabled,
                        contentDescription = action.accessibilityLabel,
                    )
                }
            }
        },
    )
}

private fun handleQueueRowAction(
    actionId: String,
    onAction: () -> Unit,
    onSecondaryAction: (() -> Unit)?,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
) {
    when (actionId) {
        "move_up" -> onMoveUp?.invoke()
        "move_down" -> onMoveDown?.invoke()
        "primary" -> onAction()
        "secondary" -> onSecondaryAction?.invoke()
    }
}
