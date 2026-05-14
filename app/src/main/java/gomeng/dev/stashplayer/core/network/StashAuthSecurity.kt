package gomeng.dev.stashplayer.core.network

import java.net.URI

enum class StashCredentialTransportDecision {
    Secure,
    InsecureLocalAllowed,
    Blocked,
}

fun resolveStashCredentialTransportDecision(
    baseUrl: String,
    authMode: StashServerAuthMode,
    allowInsecureLocalApiKey: Boolean,
): StashCredentialTransportDecision {
    val uri = parseStashAuthUri(baseUrl) ?: return StashCredentialTransportDecision.Blocked
    if (authMode == StashServerAuthMode.None) {
        return if (
            uri.scheme.equals("https", ignoreCase = true) ||
            uri.scheme.equals("http", ignoreCase = true)
        ) {
            StashCredentialTransportDecision.Secure
        } else {
            StashCredentialTransportDecision.Blocked
        }
    }
    if (uri.scheme.equals("https", ignoreCase = true)) return StashCredentialTransportDecision.Secure
    if (uri.scheme.equals("http", ignoreCase = true)) return StashCredentialTransportDecision.InsecureLocalAllowed
    return StashCredentialTransportDecision.Blocked
}

fun canAttemptStashCredentialTransport(
    baseUrl: String,
    authMode: StashServerAuthMode,
    allowInsecureLocalApiKey: Boolean,
): Boolean = when (
    resolveStashCredentialTransportDecision(
        baseUrl = baseUrl,
        authMode = authMode,
        allowInsecureLocalApiKey = allowInsecureLocalApiKey,
    )
) {
    StashCredentialTransportDecision.Secure,
    StashCredentialTransportDecision.InsecureLocalAllowed -> true
    StashCredentialTransportDecision.Blocked -> false
}

private fun parseStashAuthUri(baseUrl: String): URI? {
    val normalized = StashServerProfile(baseUrl = baseUrl).normalizedBaseUrl()
    return runCatching { URI(normalized) }.getOrNull()?.takeIf { it.host != null }
}
