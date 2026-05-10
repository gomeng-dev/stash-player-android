package gomeng.dev.stashplayer.core.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class AppUpdateInstaller(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun downloadApk(
        context: Context,
        asset: AppUpdateApkAsset,
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(asset.downloadUrl)
            .header("User-Agent", "Stash-Android-Player")
            .build()
        val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updateDir, asset.name.sanitizeApkFileName())
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("APK download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("APK download returned an empty body")
            apkFile.outputStream().use { output ->
                body.byteStream().use { input -> input.copyTo(output) }
            }
        }
        apkFile
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openUnknownAppSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    fun launchInstaller(context: Context, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}

private fun String.sanitizeApkFileName(): String {
    val sanitized = replace(Regex("[^A-Za-z0-9._-]"), "_")
        .takeIf { it.isNotBlank() }
        ?: "stash-player-update.apk"
    return if (sanitized.endsWith(".apk", ignoreCase = true)) {
        sanitized
    } else {
        "$sanitized.apk"
    }
}
