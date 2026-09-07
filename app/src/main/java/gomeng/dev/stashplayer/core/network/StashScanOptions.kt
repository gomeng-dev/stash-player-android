package gomeng.dev.stashplayer.core.network

data class StashScanOptions(
    val scanGenerateCovers: Boolean = true,
    val scanGeneratePreviews: Boolean = false,
    val scanGenerateImagePreviews: Boolean = false,
    val scanGenerateSprites: Boolean = false,
    val scanGeneratePhashes: Boolean = false,
    val scanGenerateThumbnails: Boolean = false,
    val scanGenerateImagePhashes: Boolean = false,
    val scanGenerateClipPreviews: Boolean = false,
    val rescan: Boolean = false,
)

data class StashServerDirectory(
    val path: String,
    val parent: String?,
    val directories: List<String>,
)
