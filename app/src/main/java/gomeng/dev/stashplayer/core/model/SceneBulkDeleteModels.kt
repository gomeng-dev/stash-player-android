package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class SceneBulkDeleteConfirmationState(
    val isVisible: Boolean = false,
    val selectedCount: Int = 0,
    val isConfirmed: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteOptions: StashSceneDeleteOptions = StashSceneDeleteOptions(),
) {
    val canDelete: Boolean
        get() = isVisible && selectedCount > 0 && isConfirmed && !isDeleting

    fun withConfirmation(isConfirmed: Boolean): SceneBulkDeleteConfirmationState = copy(isConfirmed = isConfirmed)

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

        fun open(selectedCount: Int): SceneBulkDeleteConfirmationState = SceneBulkDeleteConfirmationState(
            isVisible = selectedCount > 0,
            selectedCount = selectedCount.coerceAtLeast(0),
            isConfirmed = false,
            isDeleting = false,
            deleteOptions = StashSceneDeleteOptions(),
        )
    }
}

data class StashSceneDeleteOptions(
    val deleteFile: Boolean = false,
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
