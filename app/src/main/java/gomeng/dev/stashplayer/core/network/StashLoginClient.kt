package gomeng.dev.stashplayer.core.network

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class StashLoginClient(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) {
    suspend fun loginWithPassword(
        baseUrl: String,
        username: String,
        password: String,
    ): StashPasswordLoginResult = withContext(Dispatchers.IO) {
        val profile = StashServerProfile(baseUrl = baseUrl, authMode = StashServerAuthMode.SessionCookie)
        val request = buildStashPasswordLoginRequest(profile, username, password)
        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw IOException(redactStashCredentialText("Stash login HTTP ${response.code}: ${body.take(240)}"))
        }
        val cookie = extractStashSessionCookie(response.headers.values("Set-Cookie"))
        if (cookie.isBlank()) {
            throw IOException("Stash login did not return a session cookie")
        }
        StashPasswordLoginResult(sessionCookie = cookie)
    }
}

data class StashPasswordLoginResult(
    val sessionCookie: String,
)

internal fun buildStashPasswordLoginRequest(
    profile: StashServerProfile,
    username: String,
    password: String,
): Request {
    val body = FormBody.Builder()
        .add("username", username)
        .add("password", password)
        .build()
    return Request.Builder()
        .url(profile.absoluteUrl("/login"))
        .post(body)
        .build()
}

internal fun extractStashSessionCookie(setCookieHeaders: List<String>): String {
    return setCookieHeaders
        .mapNotNull { header -> header.substringBefore(';').trim().takeIf { it.contains('=') } }
        .joinToString("; ")
}
