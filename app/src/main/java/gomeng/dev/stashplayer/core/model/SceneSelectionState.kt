package gomeng.dev.stashplayer.core.model

/**
 * Pure reducer state for scene multi-selection in media grids.
 *
 * Long press enters selection mode, then normal card taps toggle selection until
 * the last selected item is cleared. While inactive, a tap should keep opening
 * the player instead of mutating selection state.
 */
data class SceneSelectionState(
    val selectedSceneIds: Set<String> = emptySet(),
) {
    val isActive: Boolean
        get() = selectedSceneIds.isNotEmpty()

    val selectedCount: Int
        get() = selectedSceneIds.size

    fun selectFromLongPress(sceneId: String): SceneSelectionState = copy(
        selectedSceneIds = selectedSceneIds + sceneId,
    )

    fun handleCardTap(sceneId: String): SceneSelectionTapResult {
        if (!isActive) {
            return SceneSelectionTapResult(state = this, shouldOpenScene = true)
        }
        val updatedSelection = if (sceneId in selectedSceneIds) {
            selectedSceneIds - sceneId
        } else {
            selectedSceneIds + sceneId
        }
        return SceneSelectionTapResult(
            state = copy(selectedSceneIds = updatedSelection),
            shouldOpenScene = false,
        )
    }

    fun clear(): SceneSelectionState = copy(selectedSceneIds = emptySet())

    fun selectVisibleScenes(visibleSceneIds: Iterable<String>): SceneSelectionState = copy(
        selectedSceneIds = visibleSceneIds.toSet(),
    )

    fun invertVisibleSelection(visibleSceneIds: Iterable<String>): SceneSelectionState {
        val visibleIds = visibleSceneIds.toSet()
        val hiddenSelection = selectedSceneIds - visibleIds
        val invertedVisibleSelection = visibleIds - selectedSceneIds
        return copy(selectedSceneIds = hiddenSelection + invertedVisibleSelection)
    }

    fun selectedPlaybackRequest(visibleScenes: List<SceneCardModel>): SelectedScenePlaybackRequest? {
        val selectedScenes = visibleScenes.filter { it.id in selectedSceneIds }
        val startSceneId = selectedScenes.firstOrNull()?.id ?: return null
        return SelectedScenePlaybackRequest(
            startSceneId = startSceneId,
            scenes = selectedScenes,
            nextSelectionState = clear(),
        )
    }

    fun toolbarPlaybackRequest(visibleScenes: List<SceneCardModel>): SelectedScenePlaybackRequest? {
        if (isActive) return selectedPlaybackRequest(visibleScenes)
        val startSceneId = visibleScenes.firstOrNull()?.id ?: return null
        return SelectedScenePlaybackRequest(
            startSceneId = startSceneId,
            scenes = visibleScenes,
            nextSelectionState = clear(),
        )
    }

    fun clearIfResultIdentityChanged(
        previousSceneIds: List<String>,
        currentSceneIds: List<String>,
    ): SceneSelectionState = if (previousSceneIds == currentSceneIds) this else clear()
}

data class SelectedScenePlaybackRequest(
    val startSceneId: String,
    val scenes: List<SceneCardModel>,
    val nextSelectionState: SceneSelectionState,
)

data class SceneSelectionTapResult(
    val state: SceneSelectionState,
    val shouldOpenScene: Boolean,
)
