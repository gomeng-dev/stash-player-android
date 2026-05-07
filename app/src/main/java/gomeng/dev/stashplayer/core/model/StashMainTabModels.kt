package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
enum class StashSortDirection(val graphQlValue: String) {
    Asc("ASC"),
    Desc("DESC"),
}

data class StashMainTabSectionSpec(
    val id: String,
    val title: String,
    val sort: String,
    val direction: StashSortDirection,
    val onlyResumable: Boolean = false,
    val perPage: Int = 18,
)

data class StashMainTabSection(
    val spec: StashMainTabSectionSpec,
    val scenes: List<SceneCardModel>,
) {
    val shouldRender: Boolean
        get() = scenes.isNotEmpty()
}

fun defaultStashMainTabSections(): List<StashMainTabSectionSpec> = listOf(
    StashMainTabSectionSpec(
        id = "continue-watching",
        title = stashString(R.string.auto_kr_0059),
        sort = "resume_time",
        direction = StashSortDirection.Desc,
        onlyResumable = true,
        perPage = 18,
    ),
    StashMainTabSectionSpec(
        id = "recently-released-scenes",
        title = stashString(R.string.auto_kr_0115),
        sort = "date",
        direction = StashSortDirection.Desc,
    ),
    StashMainTabSectionSpec(
        id = "recently-added-scenes",
        title = stashString(R.string.auto_kr_0116),
        sort = "created_at",
        direction = StashSortDirection.Desc,
    ),
    StashMainTabSectionSpec(
        id = "recently-updated-scenes",
        title = stashString(R.string.auto_kr_0117),
        sort = "updated_at",
        direction = StashSortDirection.Desc,
    ),
)
