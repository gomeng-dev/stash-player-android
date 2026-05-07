package gomeng.dev.stashplayer.core.network

import gomeng.dev.stashplayer.core.model.StashSceneDeleteOptions

internal fun buildScenesDestroyVariables(
    sceneIds: List<String>,
    deleteOptions: StashSceneDeleteOptions = StashSceneDeleteOptions(),
): Map<String, Any?> = mapOf(
    "input" to mapOf(
        "ids" to sceneIds.distinct(),
        "delete_file" to deleteOptions.deleteFile,
        "delete_generated" to deleteOptions.deleteGenerated,
    ),
)

internal fun parseScenesDestroyResponse(json: String): Boolean {
    val errorMessage = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1)
    if (!errorMessage.isNullOrBlank()) {
        error(redactStashCredentialText(errorMessage))
    }
    return Regex("\"scenesDestroy\"\\s*:\\s*true", RegexOption.IGNORE_CASE).containsMatchIn(json)
}

fun redactStashCredentialText(message: String?): String = message
    ?.replace(Regex("(?i)(apikey=)([^&\\s]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(\\bApiKey\\s*:\\s*)([^\\s,;]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(\\bAuthorization\\s*:?\\s*)(bearer\\s+)?([^\\s,;]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)(\\bCookie\\s*:?\\s*)([^\\n;]+)")) { match ->
        "${match.groupValues[1]}[REDACTED]"
    }
    ?.replace(Regex("(?i)\\b(token|password|passwd|secret)=([^&\\s]+)")) { match ->
        "${match.groupValues[1]}=[REDACTED]"
    }
    ?: ""
