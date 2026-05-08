package gomeng.dev.stashplayer.core.network

import java.net.InetAddress
import java.net.URI

enum class StashCredentialTransportDecision {
    Secure,
    InsecureLocalAllowed,
    InsecureNeedsExplicitLocalConfirmation,
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
    if (!uri.scheme.equals("http", ignoreCase = true)) return StashCredentialTransportDecision.Blocked
    if (!uri.isLocalHttpHost()) return StashCredentialTransportDecision.Blocked
    return if (allowInsecureLocalApiKey) {
        StashCredentialTransportDecision.InsecureLocalAllowed
    } else {
        StashCredentialTransportDecision.InsecureNeedsExplicitLocalConfirmation
    }
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
    StashCredentialTransportDecision.InsecureNeedsExplicitLocalConfirmation,
    StashCredentialTransportDecision.Blocked -> false
}

private fun parseStashAuthUri(baseUrl: String): URI? {
    val normalized = StashServerProfile(baseUrl = baseUrl).normalizedBaseUrl()
    return runCatching { URI(normalized) }.getOrNull()?.takeIf { it.host != null }
}

private fun URI.isLocalHttpHost(): Boolean {
    val normalizedHost = host?.trim()?.lowercase().orEmpty()
    if (normalizedHost == "localhost") return true
    val address = runCatching { InetAddress.getByName(normalizedHost) }.getOrNull() ?: return false
    val bytes = address.address
    if (address.isLoopbackAddress || address.isSiteLocalAddress) return true
    if (bytes.size != 4) return false
    val first = bytes[0].toInt() and 0xff
    val second = bytes[1].toInt() and 0xff
    return first == 10 ||
        first == 127 ||
        (first == 172 && second in 16..31) ||
        (first == 192 && second == 168)
}
