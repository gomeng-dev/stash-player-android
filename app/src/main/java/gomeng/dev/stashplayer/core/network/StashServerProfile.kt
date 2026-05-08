package gomeng.dev.stashplayer.core.network

import java.net.URI
import java.net.URLEncoder

private const val DEFAULT_SCHEME = "http://"

enum class StashServerAuthMode(val persistedValue: String) {
    None("none"),
    ApiKey("api_key"),
    SessionCookie("session_cookie");

    companion object {
        val default: StashServerAuthMode = ApiKey

        fun fromPersistedValue(value: String?): StashServerAuthMode =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

data class StashServerProfile(
    val name: String = "Home",
    val baseUrl: String = "",
    val apiKey: String = "",
    val authMode: StashServerAuthMode = StashServerAuthMode.ApiKey,
    val sessionCookie: String = "",
    val allowInsecureLocalApiKey: Boolean = false,
) {
    fun isConfigured(): Boolean = baseUrl.isNotBlank()

    fun normalizedBaseUrl(): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        if (trimmed.isBlank()) return ""
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            DEFAULT_SCHEME + trimmed
        }
    }

    fun graphQlUrl(): String = normalizedBaseUrl() + "/graphql"

    fun absoluteUrl(url: String): String {
        val value = url.trim()
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        val base = normalizedBaseUrl()
        return if (value.startsWith("/")) base + value else "$base/$value"
    }

    fun isSameOrigin(url: String): Boolean {
        val target = parseHttpUri(absoluteUrl(url)) ?: return false
        val base = parseHttpUri(normalizedBaseUrl()) ?: return false
        return target.scheme.equals(base.scheme, ignoreCase = true) &&
            target.host.equals(base.host, ignoreCase = true) &&
            target.effectivePort() == base.effectivePort()
    }

    fun authHeadersFor(url: String): Map<String, String> {
        if (!isSameOrigin(url)) return emptyMap()
        return when (authMode) {
            StashServerAuthMode.None -> emptyMap()
            StashServerAuthMode.ApiKey -> if (apiKey.isNotBlank()) mapOf("ApiKey" to apiKey) else emptyMap()
            StashServerAuthMode.SessionCookie -> if (sessionCookie.isNotBlank()) mapOf("Cookie" to sessionCookie) else emptyMap()
        }
    }

    fun authenticatedUrl(url: String): String {
        val absolute = absoluteUrl(url)
        if (authMode != StashServerAuthMode.ApiKey || apiKey.isBlank() || !isSameOrigin(absolute) || absolute.contains("apikey=")) return absolute
        val encoded = URLEncoder.encode(apiKey, Charsets.UTF_8.name())
        val fragmentStart = absolute.indexOf('#')
        val main = if (fragmentStart >= 0) absolute.substring(0, fragmentStart) else absolute
        val fragment = if (fragmentStart >= 0) absolute.substring(fragmentStart) else ""
        val separator = if (main.contains('?')) "&" else "?"
        return "$main${separator}apikey=$encoded$fragment"
    }

    private fun parseHttpUri(value: String): URI? = runCatching { URI(value) }
        .getOrNull()
        ?.takeIf { uri ->
            uri.host != null && (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true))
        }

    private fun URI.effectivePort(): Int = when {
        port >= 0 -> port
        scheme.equals("https", ignoreCase = true) -> 443
        else -> 80
    }
}

fun persistStashServerAuthMode(mode: StashServerAuthMode): String = mode.persistedValue

fun stashServerAuthModeFromPersistedValue(value: String?): StashServerAuthMode =
    StashServerAuthMode.fromPersistedValue(value)
