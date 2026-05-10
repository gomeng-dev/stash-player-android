package gomeng.dev.stashplayer.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

const val STASH_ANDROID_RELEASES_URL = "https://github.com/gomeng-dev/stash-player-android/releases"
const val STASH_ANDROID_LATEST_RELEASE_API_URL =
    "https://api.github.com/repos/gomeng-dev/stash-player-android/releases/latest"

data class AppUpdateApkAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long? = null,
)

data class AppUpdateRelease(
    val versionName: String,
    val releaseUrl: String,
    val changelog: String? = null,
    val apkAsset: AppUpdateApkAsset? = null,
)

data class AppUpdateNotice(
    val currentVersionName: String,
    val latestVersionName: String,
    val releaseUrl: String,
    val changelog: String? = null,
    val apkAsset: AppUpdateApkAsset? = null,
)

class AppUpdateChecker(
    private val client: OkHttpClient = OkHttpClient(),
    private val latestReleaseApiUrl: String = STASH_ANDROID_LATEST_RELEASE_API_URL,
) {
    suspend fun fetchLatestRelease(): AppUpdateRelease = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(latestReleaseApiUrl)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Stash-Android-Player")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Latest release request failed: HTTP ${response.code}")
            }
            parseGitHubLatestRelease(response.body?.string().orEmpty())
                ?: throw IllegalStateException("Latest release response is missing version data")
        }
    }

    suspend fun checkForUpdate(currentVersionName: String): AppUpdateNotice? {
        return resolveAppUpdateNotice(
            currentVersionName = currentVersionName,
            latestRelease = fetchLatestRelease(),
        )
    }
}

fun parseGitHubLatestRelease(json: String): AppUpdateRelease? {
    val release = appUpdateMoshi.adapter(GitHubLatestReleaseResponse::class.java).fromJson(json)
        ?: return null
    val versionName = normalizeAppVersionName(release.tagName ?: release.name ?: return null)
        ?: return null
    val releaseUrl = release.htmlUrl?.takeIf { it.isNotBlank() } ?: STASH_ANDROID_RELEASES_URL
    return AppUpdateRelease(
        versionName = versionName,
        releaseUrl = releaseUrl,
        changelog = release.body?.trim()?.takeIf { it.isNotBlank() },
        apkAsset = release.assets.orEmpty().firstNotNullOfOrNull { asset -> asset.toApkAssetOrNull() },
    )
}

fun resolveAppUpdateNotice(
    currentVersionName: String,
    latestRelease: AppUpdateRelease,
): AppUpdateNotice? {
    if (!isAppUpdateAvailable(currentVersionName, latestRelease.versionName)) return null
    return AppUpdateNotice(
        currentVersionName = normalizeAppVersionName(currentVersionName) ?: currentVersionName,
        latestVersionName = latestRelease.versionName,
        releaseUrl = latestRelease.releaseUrl,
        changelog = latestRelease.changelog,
        apkAsset = latestRelease.apkAsset,
    )
}

fun isAppUpdateAvailable(
    currentVersionName: String,
    latestVersionName: String,
): Boolean {
    val current = normalizeAppVersionParts(currentVersionName) ?: return false
    val latest = normalizeAppVersionParts(latestVersionName) ?: return false
    val maxSize = maxOf(current.size, latest.size)
    for (index in 0 until maxSize) {
        val currentPart = current.getOrElse(index) { 0 }
        val latestPart = latest.getOrElse(index) { 0 }
        if (latestPart != currentPart) return latestPart > currentPart
    }
    return false
}

fun normalizeAppVersionParts(versionName: String): List<Int>? {
    val core = versionName
        .trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')
    val parts = core.split('.')
        .filter { it.isNotBlank() }
        .map { part -> part.toIntOrNull() ?: return null }
    return parts.takeIf { it.isNotEmpty() }
}

private fun normalizeAppVersionName(versionName: String): String? =
    normalizeAppVersionParts(versionName)?.joinToString(".")

private val appUpdateMoshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private data class GitHubLatestReleaseResponse(
    @Json(name = "tag_name") val tagName: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "body") val body: String? = null,
    @Json(name = "assets") val assets: List<GitHubReleaseAsset>? = null,
)

private data class GitHubReleaseAsset(
    @Json(name = "name") val name: String? = null,
    @Json(name = "browser_download_url") val browserDownloadUrl: String? = null,
    @Json(name = "content_type") val contentType: String? = null,
    @Json(name = "size") val size: Long? = null,
)

private fun GitHubReleaseAsset.toApkAssetOrNull(): AppUpdateApkAsset? {
    val assetName = name?.takeIf { it.isNotBlank() } ?: return null
    val downloadUrl = browserDownloadUrl?.takeIf { it.isNotBlank() } ?: return null
    val isApk = assetName.endsWith(".apk", ignoreCase = true) ||
        contentType.equals("application/vnd.android.package-archive", ignoreCase = true)
    if (!isApk) return null
    return AppUpdateApkAsset(
        name = assetName,
        downloadUrl = downloadUrl,
        sizeBytes = size?.takeIf { it > 0L },
    )
}
