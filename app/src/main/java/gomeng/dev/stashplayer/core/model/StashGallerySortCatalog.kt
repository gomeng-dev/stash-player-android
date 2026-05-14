package gomeng.dev.stashplayer.core.model

import androidx.annotation.StringRes
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class StashGallerySortOption(
    val id: String,
    @StringRes val labelRes: Int,
    val serverValue: String,
    val defaultDirection: StashSortDirection,
) {
    val label: String get() = stashString(labelRes)
}

fun defaultStashGallerySortOption(): StashGallerySortOption = stashGallerySortOptions().first()

fun defaultStashImageSortOption(): StashGallerySortOption = stashImageSortOptions().first()

fun stashGallerySortOptions(): List<StashGallerySortOption> = listOf(
    StashGallerySortOption("path", R.string.stash_sort_path_label, "path", StashSortDirection.Asc),
    StashGallerySortOption("date", R.string.gallery_sort_date_label, "date", StashSortDirection.Desc),
    StashGallerySortOption("title", R.string.auto_kr_0135, "title", StashSortDirection.Asc),
    StashGallerySortOption("rating", R.string.stash_sort_rating_label, "rating", StashSortDirection.Desc),
    StashGallerySortOption("file_mod_time", R.string.stash_sort_file_mod_time_label, "file_mod_time", StashSortDirection.Desc),
    StashGallerySortOption("tag_count", R.string.stash_sort_tag_count_label, "tag_count", StashSortDirection.Desc),
    StashGallerySortOption("performer_count", R.string.stash_sort_performer_count_label, "performer_count", StashSortDirection.Desc),
    StashGallerySortOption("random", R.string.stash_sort_random_label, "random", StashSortDirection.Desc),
    StashGallerySortOption("images_count", R.string.gallery_sort_images_count_label, "images_count", StashSortDirection.Desc),
    StashGallerySortOption("file_count", R.string.stash_sort_file_count_label, "file_count", StashSortDirection.Desc),
    StashGallerySortOption("created_at", R.string.gallery_sort_created_at_label, "created_at", StashSortDirection.Desc),
    StashGallerySortOption("updated_at", R.string.gallery_sort_updated_at_label, "updated_at", StashSortDirection.Desc),
)

fun stashImageSortOptions(): List<StashGallerySortOption> = listOf(
    StashGallerySortOption("path", R.string.stash_sort_path_label, "path", StashSortDirection.Asc),
    StashGallerySortOption("title", R.string.auto_kr_0135, "title", StashSortDirection.Asc),
    StashGallerySortOption("rating", R.string.stash_sort_rating_label, "rating", StashSortDirection.Desc),
    StashGallerySortOption("file_mod_time", R.string.stash_sort_file_mod_time_label, "file_mod_time", StashSortDirection.Desc),
    StashGallerySortOption("tag_count", R.string.stash_sort_tag_count_label, "tag_count", StashSortDirection.Desc),
    StashGallerySortOption("performer_count", R.string.stash_sort_performer_count_label, "performer_count", StashSortDirection.Desc),
    StashGallerySortOption("random", R.string.stash_sort_random_label, "random", StashSortDirection.Desc),
    StashGallerySortOption("filesize", R.string.stash_sort_filesize_label, "filesize", StashSortDirection.Desc),
    StashGallerySortOption("file_count", R.string.stash_sort_file_count_label, "file_count", StashSortDirection.Desc),
    StashGallerySortOption("date", R.string.gallery_sort_date_label, "date", StashSortDirection.Desc),
    StashGallerySortOption("resolution", R.string.stash_sort_resolution_label, "resolution", StashSortDirection.Desc),
    StashGallerySortOption("o_counter", R.string.stash_sort_o_counter_label, "o_counter", StashSortDirection.Desc),
    StashGallerySortOption("created_at", R.string.gallery_sort_created_at_label, "created_at", StashSortDirection.Desc),
    StashGallerySortOption("updated_at", R.string.gallery_sort_updated_at_label, "updated_at", StashSortDirection.Desc),
)

fun galleryServerSortValue(
    sortOption: StashGallerySortOption,
    randomSeed: Int?,
): String {
    if (sortOption.serverValue != "random") return sortOption.serverValue
    val seed = normalizeStashRandomSortSeed(randomSeed ?: nextStashRandomSortSeed())
    return "random_$seed"
}

fun imageServerSortValue(
    sortOption: StashGallerySortOption,
    randomSeed: Int?,
): String = galleryServerSortValue(sortOption, randomSeed)

fun StashGalleryDisplayMode.label(): String = when (this) {
    StashGalleryDisplayMode.Grid -> stashString(R.string.auto_kr_0083)
    StashGalleryDisplayMode.List -> stashString(R.string.auto_kr_0084)
    StashGalleryDisplayMode.Wall -> stashString(R.string.gallery_display_mode_wall_label)
    StashGalleryDisplayMode.Folders -> stashString(R.string.gallery_display_mode_folders_label)
}

fun StashGalleryDisplayMode.stashGalleryDisplayModeContentDescription(): String =
    stashScenesToolbarImmediateActionContentDescription(stashString(R.string.auto_kr_0108), label())

fun StashGallerySortOption.isRandomSort(): Boolean = serverValue == "random"
