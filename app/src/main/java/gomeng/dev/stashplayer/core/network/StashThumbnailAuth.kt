package gomeng.dev.stashplayer.core.network

data class StashThumbnailRequestSpec(
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
)

fun buildStashThumbnailModel(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): String? = buildStashThumbnailRequestSpec(thumbnailUrl, serverProfile)?.url

fun buildStashThumbnailRequestSpec(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): StashThumbnailRequestSpec? {
    val normalized = thumbnailUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val resolvedUrl = serverProfile?.authenticatedUrl(normalized) ?: normalized
    val requestHeaders = when (serverProfile?.authMode) {
        StashServerAuthMode.SessionCookie -> serverProfile.authHeadersFor(resolvedUrl)
        StashServerAuthMode.ApiKey,
        null,
        -> emptyMap()
    }
    return StashThumbnailRequestSpec(
        url = resolvedUrl,
        requestHeaders = requestHeaders,
    )
}
