package gomeng.dev.stashplayer.core.network

internal fun buildImageRatingUpdateVariables(imageId: String, rating100: Int?): Map<String, Any?> = mapOf(
    "input" to mapOf(
        "id" to imageId.trim(),
        "rating100" to rating100?.coerceIn(0, 100),
    ),
)

internal fun parseImageRatingUpdateResponse(json: String): Boolean {
    val errorMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1)
    if (!errorMessage.isNullOrBlank()) {
        error(redactStashCredentialText(errorMessage))
    }
    return Regex("\"imageUpdate\"\\s*:\\s*\\{", RegexOption.IGNORE_CASE).containsMatchIn(json)
}

internal fun parseImageOCounterMutationResponse(json: String, mutationName: String): Int {
    val errorMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1)
    if (!errorMessage.isNullOrBlank()) {
        error(redactStashCredentialText(errorMessage))
    }
    val escapedMutationName = Regex.escape(mutationName)
    val countText = Regex(
        "\"$escapedMutationName\"\\s*:\\s*(-?\\d+)",
        RegexOption.IGNORE_CASE,
    ).find(json)?.groupValues?.getOrNull(1)
    return countText?.toIntOrNull()?.coerceAtLeast(0)
        ?: error("Stash did not return image O-counter update.")
}
