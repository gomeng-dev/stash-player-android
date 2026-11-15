package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class SceneBulkDeleteConfirmationState(
    val isVisible: Boolean = false,
    val sceneIds: List<String> = emptyList(),
    val isDeleting: Boolean = false,
    val deleteOptions: StashSceneDeleteOptions = StashSceneDeleteOptions(),
) {
    val selectedCount: Int get() = sceneIds.size

    val canDelete: Boolean
        get() = isVisible && selectedCount > 0 && !isDeleting

    fun request(sceneIds: Collection<String>): SceneBulkDeleteConfirmationState =
        if (isVisible || isDeleting) this else open(sceneIds)

    fun withDeleteFile(deleteFile: Boolean): SceneBulkDeleteConfirmationState = copy(
        deleteOptions = deleteOptions.copy(deleteFile = deleteFile),
    )

    fun withDeleteGenerated(deleteGenerated: Boolean): SceneBulkDeleteConfirmationState = copy(
        deleteOptions = deleteOptions.copy(deleteGenerated = deleteGenerated),
    )

    fun deleting(): SceneBulkDeleteConfirmationState = copy(isDeleting = true)

    fun dismiss(): SceneBulkDeleteConfirmationState = Hidden

    companion object {
        val Hidden = SceneBulkDeleteConfirmationState()

        fun open(sceneIds: Collection<String>): SceneBulkDeleteConfirmationState {
            val ids = sceneIds.filter { it.isNotBlank() }.distinct()
            return SceneBulkDeleteConfirmationState(isVisible = ids.isNotEmpty(), sceneIds = ids)
        }
    }
}

data class StashSceneDeleteOptions(
    val deleteFile: Boolean = true,
    val deleteGenerated: Boolean = true,
)

data class SceneBulkDeleteResult(
    val requestedSceneIds: List<String>,
    val deletedSceneIds: Set<String>,
    val failedSceneIds: Map<String, String> = emptyMap(),
) {
    val hasFailures: Boolean get() = failedSceneIds.isNotEmpty()

    val koreanSummary: String
        get() = buildList {
            if (deletedSceneIds.isNotEmpty()) add(stashString(R.string.auto_kr_0019, deletedSceneIds.size))
            if (failedSceneIds.isNotEmpty()) add(stashString(R.string.auto_kr_0020, failedSceneIds.size))
            if (isEmpty()) add(stashString(R.string.auto_kr_0021))
        }.joinToString(" · ")
}

fun SceneSelectionState.afterBulkDelete(result: SceneBulkDeleteResult): SceneSelectionState = copy(
    selectedSceneIds = selectedSceneIds - result.deletedSceneIds,
)

fun List<SceneCardModel>.withoutBulkDeletedScenes(result: SceneBulkDeleteResult): List<SceneCardModel> =
    filterNot { it.id in result.deletedSceneIds }

fun List<String>.withoutBulkDeletedSceneIds(result: SceneBulkDeleteResult): List<String> =
    filterNot { it in result.deletedSceneIds }
