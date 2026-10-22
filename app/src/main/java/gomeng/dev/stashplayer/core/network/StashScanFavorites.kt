package gomeng.dev.stashplayer.core.network

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import okio.ByteString.Companion.encodeUtf8

internal fun isValidStashScanPath(path: String): Boolean {
    val normalized = path.replace('\\', '/')
    // Stash roots may be server-relative (data/...), not Android filesystem paths.
    return path.isNotBlank() && path.none { it.isISOControl() } &&
        normalized.split('/').none { it == "." || it == ".." }
}

internal object StashScanFavorites {
    fun canSubmit(running: Boolean, pendingJob: String?, queueLoading: Boolean, queueError: String?, jobs: List<StashJob>): Boolean =
        !running && pendingJob == null && !queueLoading && queueError == null &&
            jobs.none { it.status in listOf(StashJobStatus.Ready, StashJobStatus.Running, StashJobStatus.Stopping) }

    // Use the saved primary profile, not the currently reachable fallback endpoint.
    fun key(profile: StashServerProfile): Preferences.Key<Set<String>> {
        val identity = when (profile.authMode) {
            StashServerAuthMode.None -> ""
            StashServerAuthMode.ApiKey -> profile.apiKey
            StashServerAuthMode.SessionCookie -> profile.username.ifBlank { profile.sessionCookie }
        }
        val scope = listOf(profile.normalizedBaseUrl(), profile.authMode.persistedValue, identity)
            .joinToString { "${it.length}:$it" }.encodeUtf8().sha256().hex()
        return stringSetPreferencesKey("scan_favorites_$scope")
    }

    fun read(prefs: Preferences, profile: StashServerProfile): List<String> =
        prefs[key(profile)].orEmpty().filter(::isValidStashScanPath).sorted()

    fun set(prefs: MutablePreferences, profile: StashServerProfile, path: String, favorite: Boolean) {
        require(isValidStashScanPath(path)) { "Invalid scan path" }
        val key = key(profile)
        val current = prefs[key].orEmpty()
        prefs[key] = if (favorite) current + path else current - path
    }
}
