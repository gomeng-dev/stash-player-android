package gomeng.dev.stashplayer.core.local

import gomeng.dev.stashplayer.core.model.favoriteToggleFeedbackText
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class LocalSceneToggleDecision(
    val shouldEnable: Boolean,
    val feedbackText: String?,
)

data class LocalSceneSnackbarFeedback(
    val message: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = true,
)

data class LocalSceneQueueAddDecision(
    val shouldAdd: Boolean,
    val feedback: LocalSceneSnackbarFeedback,
)

data class LocalWatchLaterQueueActionState(
    val label: String,
    val enabled: Boolean,
)

fun buildLocalFavoriteToggleDecision(
    sceneId: String,
    favoriteSceneIds: Set<String>,
): LocalSceneToggleDecision {
    val shouldFavorite = sceneId !in favoriteSceneIds
    return LocalSceneToggleDecision(
        shouldEnable = shouldFavorite,
        feedbackText = favoriteToggleFeedbackText(shouldFavorite),
    )
}

fun buildLocalWatchLaterToggleDecision(
    sceneId: String,
    watchLaterSceneIds: Set<String>,
): LocalSceneToggleDecision = LocalSceneToggleDecision(
    shouldEnable = sceneId !in watchLaterSceneIds,
    feedbackText = null,
)

fun buildLocalSceneQueueAddDecision(
    sceneId: String,
    queuedSceneIds: Set<String>,
): LocalSceneQueueAddDecision = if (sceneId in queuedSceneIds) {
    LocalSceneQueueAddDecision(
        shouldAdd = false,
        feedback = LocalSceneSnackbarFeedback(message = stashString(R.string.auto_kr_0007), withDismissAction = false),
    )
} else {
    LocalSceneQueueAddDecision(
        shouldAdd = true,
        feedback = LocalSceneSnackbarFeedback(message = stashString(R.string.auto_kr_0008), withDismissAction = false),
    )
}

fun filterWatchLaterQueueAddCandidateIds(
    watchLaterSceneIds: List<String>,
    queuedSceneIds: Set<String>,
): List<String> {
    val seenSceneIds = queuedSceneIds.toMutableSet()
    return watchLaterSceneIds.filter { sceneId -> seenSceneIds.add(sceneId) }
}

fun buildLocalWatchLaterQueueActionState(
    sceneId: String,
    queuedSceneIds: Set<String>,
): LocalWatchLaterQueueActionState = if (sceneId in queuedSceneIds) {
    LocalWatchLaterQueueActionState(label = stashString(R.string.auto_kr_0009), enabled = false)
} else {
    LocalWatchLaterQueueActionState(label = stashString(R.string.auto_kr_0010), enabled = true)
}

fun queueClearUndoFeedback(): LocalSceneSnackbarFeedback = LocalSceneSnackbarFeedback(
    message = stashString(R.string.auto_kr_0011),
    actionLabel = stashString(R.string.auto_kr_0012),
    withDismissAction = true,
)

fun watchLaterRemoveUndoFeedback(): LocalSceneSnackbarFeedback = LocalSceneSnackbarFeedback(
    message = stashString(R.string.auto_kr_0013),
    actionLabel = stashString(R.string.auto_kr_0012),
    withDismissAction = true,
)

fun watchLaterAddedToQueueFeedback(count: Int): LocalSceneSnackbarFeedback = LocalSceneSnackbarFeedback(
    message = if (count <= 1) {
        stashString(R.string.auto_kr_0014)
    } else {
        stashString(R.string.auto_kr_0015, count)
    },
    withDismissAction = true,
)

fun shouldRestoreClearedLocalQueue(currentQueueSize: Int): Boolean = currentQueueSize == 0

fun shouldRestoreRemovedLocalWatchLater(
    currentWatchLaterSceneIds: Set<String>,
    removedSceneId: String,
): Boolean = removedSceneId !in currentWatchLaterSceneIds
