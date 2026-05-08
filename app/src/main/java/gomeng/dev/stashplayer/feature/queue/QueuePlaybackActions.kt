package gomeng.dev.stashplayer.feature.queue

import gomeng.dev.stashplayer.core.local.buildLocalSceneQueueAddDecision
import gomeng.dev.stashplayer.core.local.buildLocalWatchLaterQueueActionState
import gomeng.dev.stashplayer.core.local.filterWatchLaterQueueAddCandidateIds
import gomeng.dev.stashplayer.core.local.shouldRestoreClearedLocalQueue
import gomeng.dev.stashplayer.core.local.shouldRestoreRemovedLocalWatchLater
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPillTone
import gomeng.dev.stashplayer.core.ui.designsystem.StashCompactActionModel
import gomeng.dev.stashplayer.core.ui.designsystem.stashCompactActionModel
import kotlin.random.Random
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class QueuePlaybackRequest(
    val selectedSceneId: String,
    val scenes: List<SceneCardModel>,
    val randomShuffle: Boolean,
)

enum class QueueMoveDirection { Up, Down }

data class WatchLaterItemQueueActionState(
    val label: String,
    val enabled: Boolean,
)

data class QueueRowContext(
    val positionLabel: String,
    val statusLabel: String?,
    val isCurrent: Boolean,
)

data class QueueAddDecision(
    val shouldAdd: Boolean,
    val feedbackText: String,
)

data class PlaybackHistoryOverviewModel(
    val title: String,
    val itemCount: Int,
    val subtitle: String,
    val actionLabel: String,
    val actionContentDescription: String,
    val actionEnabled: Boolean,
    val showInlineRows: Boolean,
)

enum class QueueVisualActionStyle {
    CompactPill,
    Ghost,
}

data class QueueVisualActionState(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val destructive: Boolean,
    val style: QueueVisualActionStyle,
    val accessibilityLabel: String,
    val pillTone: StashActionPillTone,
)

data class QueueSceneVisualModel(
    val title: String,
    val subtitle: String?,
    val thumbnailUrl: String?,
    val progress: Float?,
    val currentLabel: String?,
    val current: Boolean,
    val accessibilityLabel: String,
    val actions: List<QueueVisualActionState>,
)

fun buildPlaybackHistoryOverviewModel(
    historyCount: Int,
    displayLimit: Int,
    expanded: Boolean = false,
): PlaybackHistoryOverviewModel = PlaybackHistoryOverviewModel(
    title = stashString(R.string.queue_playback_history_title),
    itemCount = historyCount,
    subtitle = stashString(R.string.queue_playback_history_subtitle, displayLimit),
    actionLabel = if (expanded) {
        stashString(R.string.queue_playback_history_collapse_label)
    } else {
        stashString(R.string.queue_playback_history_action_label)
    },
    actionContentDescription = if (expanded) {
        stashString(R.string.queue_playback_history_collapse_content_description)
    } else {
        stashString(R.string.queue_playback_history_action_content_description)
    },
    actionEnabled = historyCount > 0,
    showInlineRows = expanded && historyCount > 0,
)

fun buildQueuePlaybackRequest(
    scenes: List<SceneCardModel>,
    shuffle: Boolean,
    seed: Long = System.currentTimeMillis(),
    selectedSceneId: String? = null,
): QueuePlaybackRequest? {
    if (scenes.isEmpty()) return null
    val orderedScenes = if (shuffle && scenes.size > 1) {
        scenes.shuffled(Random(seed))
    } else {
        scenes
    }
    val selectedId = selectedSceneId
        ?.takeIf { candidate -> orderedScenes.any { it.id == candidate } }
        ?: orderedScenes.first().id
    return QueuePlaybackRequest(
        selectedSceneId = selectedId,
        scenes = orderedScenes,
        randomShuffle = shuffle && orderedScenes.size > 1,
    )
}

fun moveQueueScene(
    scenes: List<SceneCardModel>,
    sceneId: String,
    direction: QueueMoveDirection,
): List<SceneCardModel> {
    val currentIndex = scenes.indexOfFirst { it.id == sceneId }
    if (currentIndex < 0) return scenes
    val targetIndex = when (direction) {
        QueueMoveDirection.Up -> currentIndex - 1
        QueueMoveDirection.Down -> currentIndex + 1
    }
    if (targetIndex !in scenes.indices) return scenes
    return scenes.toMutableList().also { mutableScenes ->
        val selected = mutableScenes[currentIndex]
        mutableScenes[currentIndex] = mutableScenes[targetIndex]
        mutableScenes[targetIndex] = selected
    }
}

fun buildWatchLaterQueueAddCandidates(
    watchLaterScenes: List<SceneCardModel>,
    queueScenes: List<SceneCardModel>,
): List<SceneCardModel> {
    val candidateIds = filterWatchLaterQueueAddCandidateIds(
        watchLaterSceneIds = watchLaterScenes.map { it.id },
        queuedSceneIds = queueScenes.mapTo(mutableSetOf()) { it.id },
    ).toSet()
    val emittedIds = mutableSetOf<String>()
    return watchLaterScenes.filter { scene -> scene.id in candidateIds && emittedIds.add(scene.id) }
}

fun buildWatchLaterItemQueueActionState(
    sceneId: String,
    queueScenes: List<SceneCardModel>,
): WatchLaterItemQueueActionState = buildLocalWatchLaterQueueActionState(
    sceneId = sceneId,
    queuedSceneIds = queueScenes.mapTo(mutableSetOf()) { it.id },
).let { state ->
    WatchLaterItemQueueActionState(label = state.label, enabled = state.enabled)
}

fun shouldRestoreClearedQueue(currentQueueSize: Int): Boolean = shouldRestoreClearedLocalQueue(currentQueueSize)

fun shouldRestoreRemovedWatchLater(
    currentWatchLaterSceneIds: Set<String>,
    removedSceneId: String,
): Boolean = shouldRestoreRemovedLocalWatchLater(currentWatchLaterSceneIds, removedSceneId)

fun buildQueueRowContext(
    scenes: List<SceneCardModel>,
    sceneId: String,
    currentSceneId: String?,
): QueueRowContext {
    val index = scenes.indexOfFirst { it.id == sceneId }
    val positionLabel = if (index >= 0 && scenes.isNotEmpty()) {
        "${index + 1}/${scenes.size}"
    } else {
        "-/${scenes.size}"
    }
    val isCurrent = currentSceneId != null && currentSceneId == sceneId
    return QueueRowContext(
        positionLabel = positionLabel,
        statusLabel = if (isCurrent) stashString(R.string.auto_kr_0494) else null,
        isCurrent = isCurrent,
    )
}

fun buildQueueSceneVisualModel(
    scene: SceneCardModel,
    rowContext: QueueRowContext? = null,
    actionLabel: String,
    actionEnabled: Boolean = true,
    secondaryActionLabel: String? = null,
    moveUpLabel: String? = null,
    moveUpEnabled: Boolean = false,
    moveDownLabel: String? = null,
    moveDownEnabled: Boolean = false,
): QueueSceneVisualModel {
    val positionPrefix = rowContext?.positionLabel?.takeIf { it.isNotBlank() }
    val subtitle = listOfNotNull(positionPrefix, scene.subtitle.takeIf { it.isNotBlank() })
        .joinToString(" · ")
        .ifBlank { null }
    val currentLabel = rowContext?.statusLabel ?: positionPrefix
    val actions = buildList {
        if (moveUpLabel != null) {
            add(
                queueVisualActionState(
                    id = "move_up",
                    label = moveUpLabel,
                    enabled = moveUpEnabled,
                    style = QueueVisualActionStyle.CompactPill,
                    contentDescription = stashString(R.string.auto_kr_0495, scene.title),
                ),
            )
        }
        if (moveDownLabel != null) {
            add(
                queueVisualActionState(
                    id = "move_down",
                    label = moveDownLabel,
                    enabled = moveDownEnabled,
                    style = QueueVisualActionStyle.CompactPill,
                    contentDescription = stashString(R.string.auto_kr_0496, scene.title),
                ),
            )
        }
        add(
            queueVisualActionState(
                id = "primary",
                label = actionLabel,
                enabled = actionEnabled,
                style = queueActionStyleForLabel(actionLabel),
                contentDescription = "${scene.title} $actionLabel",
            ),
        )
        if (secondaryActionLabel != null) {
            add(
                queueVisualActionState(
                    id = "secondary",
                    label = secondaryActionLabel,
                    enabled = true,
                    style = queueActionStyleForLabel(secondaryActionLabel),
                    contentDescription = "${scene.title} $secondaryActionLabel",
                ),
            )
        }
    }
    return QueueSceneVisualModel(
        title = scene.title,
        subtitle = subtitle,
        thumbnailUrl = scene.thumbnailUrl,
        progress = scene.progress.takeIf { it > 0f }?.coerceIn(0f, 1f),
        currentLabel = currentLabel,
        current = rowContext?.isCurrent == true,
        accessibilityLabel = listOf(scene.title, subtitle, currentLabel)
            .filter { !it.isNullOrBlank() }
            .joinToString(" "),
        actions = actions,
    )
}

fun buildQueueBulkActionModels(): List<StashCompactActionModel> = listOf(
    stashCompactActionModel(
        id = "play_all",
        label = stashString(R.string.auto_kr_0497),
        contentDescription = stashString(R.string.auto_kr_0498),
    ),
    stashCompactActionModel(
        id = "shuffle_all",
        label = stashString(R.string.auto_kr_0429),
        contentDescription = stashString(R.string.auto_kr_0499),
    ),
    stashCompactActionModel(
        id = "clear_queue",
        label = stashString(R.string.auto_kr_0500),
        destructive = true,
        contentDescription = stashString(R.string.auto_kr_0501),
    ),
)

fun buildWatchLaterBulkActionModels(addAllEnabled: Boolean): List<StashCompactActionModel> = listOf(
    stashCompactActionModel(
        id = "add_all_to_queue",
        label = stashString(R.string.auto_kr_0502),
        enabled = addAllEnabled,
        contentDescription = stashString(R.string.auto_kr_0503),
    ),
)

private fun queueVisualActionState(
    id: String,
    label: String,
    enabled: Boolean,
    style: QueueVisualActionStyle,
    contentDescription: String,
): QueueVisualActionState {
    val pill = stashCompactActionModel(
        id = id,
        label = label,
        enabled = enabled,
        destructive = false,
        contentDescription = contentDescription,
    )
    return QueueVisualActionState(
        id = id,
        label = pill.label,
        enabled = pill.enabled,
        destructive = pill.destructive,
        style = style,
        accessibilityLabel = pill.accessibilityLabel,
        pillTone = pill.tone,
    )
}

private fun queueActionStyleForLabel(label: String): QueueVisualActionStyle = when (label) {
    stashString(R.string.auto_kr_0504), stashString(R.string.auto_kr_0144) -> QueueVisualActionStyle.Ghost
    else -> QueueVisualActionStyle.CompactPill
}

fun buildQueueAddDecision(
    sceneId: String,
    queuedSceneIds: Set<String>,
): QueueAddDecision = buildLocalSceneQueueAddDecision(sceneId, queuedSceneIds).let { decision ->
    QueueAddDecision(
        shouldAdd = decision.shouldAdd,
        feedbackText = decision.feedback.message,
    )
}
