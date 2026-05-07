package gomeng.dev.stashplayer.core.network

internal fun buildSceneRatingUpdateVariables(sceneId: String, rating100: Int?): Map<String, Any?> = mapOf(
    "input" to mapOf(
        "id" to sceneId,
        "rating100" to rating100?.coerceIn(0, 100),
    ),
)

internal fun parseSceneRatingUpdateResponse(json: String): Boolean {
    val errorMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1)
    if (!errorMessage.isNullOrBlank()) {
        error(redactStashCredentialText(errorMessage))
    }
    return Regex("\"sceneUpdate\"\\s*:\\s*\\{", RegexOption.IGNORE_CASE).containsMatchIn(json)
}

internal fun parseSceneAddOResponse(json: String): Int {
    val errorMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1)
    if (!errorMessage.isNullOrBlank()) {
        error(redactStashCredentialText(errorMessage))
    }
    val sceneAddOBody = Regex(
        "\"sceneAddO\"\\s*:\\s*\\{([^}]*)\\}",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    ).find(json)?.groupValues?.getOrNull(1)
    val countText = sceneAddOBody
        ?.let { body -> Regex("\"count\"\\s*:\\s*(-?\\d+)").find(body)?.groupValues?.getOrNull(1) }
    return countText?.toIntOrNull()?.coerceAtLeast(0)
        ?: error("Stash did not return O-counter update.")
}
