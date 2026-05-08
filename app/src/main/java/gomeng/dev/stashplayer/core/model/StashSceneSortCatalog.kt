package gomeng.dev.stashplayer.core.model

import androidx.annotation.StringRes
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class StashSceneSortOptionSpec(
    val id: String,
    val label: String,
    val sort: String,
    val defaultDirection: StashSortDirection,
)

fun defaultStashSceneSortOptionSpecs(): List<StashSceneSortOptionSpec> = STASH_SCENE_SORT_OPTION_RESOURCES.map { resource ->
    StashSceneSortOptionSpec(
        id = resource.id,
        label = stashString(resource.labelResId),
        sort = resource.sort,
        defaultDirection = resource.defaultDirection,
    )
}

private data class StashSceneSortOptionResource(
    val id: String,
    @StringRes val labelResId: Int,
    val sort: String,
    val defaultDirection: StashSortDirection,
)

private val STASH_SCENE_SORT_OPTION_RESOURCES = listOf(
    StashSceneSortOptionResource("updated", R.string.auto_kr_0048, "updated_at", StashSortDirection.Desc),
    StashSceneSortOptionResource("released", R.string.auto_kr_0049, "date", StashSortDirection.Desc),
    StashSceneSortOptionResource("added", R.string.auto_kr_0050, "created_at", StashSortDirection.Desc),
    StashSceneSortOptionResource("plays", R.string.auto_kr_0051, "play_count", StashSortDirection.Desc),
    StashSceneSortOptionResource("duration", R.string.auto_kr_0052, "duration", StashSortDirection.Desc),
    StashSceneSortOptionResource("title", R.string.auto_kr_0135, "title", StashSortDirection.Asc),
    StashSceneSortOptionResource("bitrate", R.string.stash_sort_bitrate_label, "bitrate", StashSortDirection.Desc),
    StashSceneSortOptionResource("code", R.string.stash_sort_code_label, "code", StashSortDirection.Asc),
    StashSceneSortOptionResource("file_count", R.string.stash_sort_file_count_label, "file_count", StashSortDirection.Desc),
    StashSceneSortOptionResource("filesize", R.string.stash_sort_filesize_label, "filesize", StashSortDirection.Desc),
    StashSceneSortOptionResource("file_mod_time", R.string.stash_sort_file_mod_time_label, "file_mod_time", StashSortDirection.Desc),
    StashSceneSortOptionResource("framerate", R.string.stash_sort_framerate_label, "framerate", StashSortDirection.Desc),
    StashSceneSortOptionResource("group_scene_number", R.string.stash_sort_group_scene_number_label, "group_scene_number", StashSortDirection.Asc),
    StashSceneSortOptionResource("id", R.string.stash_sort_id_label, "id", StashSortDirection.Asc),
    StashSceneSortOptionResource("interactive", R.string.stash_sort_interactive_label, "interactive", StashSortDirection.Desc),
    StashSceneSortOptionResource("interactive_speed", R.string.stash_sort_interactive_speed_label, "interactive_speed", StashSortDirection.Desc),
    StashSceneSortOptionResource("last_o_at", R.string.stash_sort_last_o_at_label, "last_o_at", StashSortDirection.Desc),
    StashSceneSortOptionResource("last_played_at", R.string.stash_sort_last_played_at_label, "last_played_at", StashSortDirection.Desc),
    StashSceneSortOptionResource("movie_scene_number", R.string.stash_sort_movie_scene_number_label, "movie_scene_number", StashSortDirection.Asc),
    StashSceneSortOptionResource("o_counter", R.string.stash_sort_o_counter_label, "o_counter", StashSortDirection.Desc),
    StashSceneSortOptionResource("organized", R.string.stash_sort_organized_label, "organized", StashSortDirection.Desc),
    StashSceneSortOptionResource("performer_count", R.string.stash_sort_performer_count_label, "performer_count", StashSortDirection.Desc),
    StashSceneSortOptionResource("play_duration", R.string.stash_sort_play_duration_label, "play_duration", StashSortDirection.Desc),
    StashSceneSortOptionResource("resume_time", R.string.stash_sort_resume_time_label, "resume_time", StashSortDirection.Desc),
    StashSceneSortOptionResource("path", R.string.stash_sort_path_label, "path", StashSortDirection.Asc),
    StashSceneSortOptionResource("perceptual_similarity", R.string.stash_sort_perceptual_similarity_label, "perceptual_similarity", StashSortDirection.Desc),
    StashSceneSortOptionResource("random", R.string.stash_sort_random_label, "random", StashSortDirection.Desc),
    StashSceneSortOptionResource("rating", R.string.stash_sort_rating_label, "rating", StashSortDirection.Desc),
    StashSceneSortOptionResource("resolution", R.string.stash_sort_resolution_label, "resolution", StashSortDirection.Desc),
    StashSceneSortOptionResource("studio", R.string.stash_sort_studio_label, "studio", StashSortDirection.Asc),
    StashSceneSortOptionResource("tag_count", R.string.stash_sort_tag_count_label, "tag_count", StashSortDirection.Desc),
    StashSceneSortOptionResource("performer_age", R.string.stash_sort_performer_age_label, "performer_age", StashSortDirection.Desc),
)
