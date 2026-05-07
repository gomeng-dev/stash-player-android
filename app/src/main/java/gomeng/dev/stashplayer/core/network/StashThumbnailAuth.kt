package gomeng.dev.stashplayer.core.network

fun buildStashThumbnailModel(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): String? {
    val normalized = thumbnailUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return serverProfile?.authenticatedUrl(normalized) ?: normalized
}
