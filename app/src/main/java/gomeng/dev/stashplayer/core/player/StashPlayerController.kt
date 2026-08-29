package gomeng.dev.stashplayer.core.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import gomeng.dev.stashplayer.core.network.StashCaptionTrack
import java.io.File

@OptIn(UnstableApi::class)
class StashPlayerController(
    context: Context,
    requestHeaders: Map<String, String> = emptyMap(),
) {
    private val preloadPolicy = stashPlayerPreloadPolicy()
    private val appContext = context.applicationContext
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(requestHeaders)
    private val cacheDataSourceFactory = CacheDataSource.Factory()
        .setCache(StashPlayerMediaCache.get(appContext, preloadPolicy))
        .setUpstreamDataSourceFactory(httpDataSourceFactory)
        .setCacheKeyFactory { dataSpec -> sanitizedStashMediaCacheKey(dataSpec.uri.toString()) }
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            preloadPolicy.minBufferMs,
            preloadPolicy.maxBufferMs,
            preloadPolicy.bufferForPlaybackMs,
            preloadPolicy.bufferForPlaybackAfterRebufferMs,
        )
        .setTargetBufferBytes(preloadPolicy.targetBufferBytes)
        .setPrioritizeTimeOverSizeThresholds(preloadPolicy.prioritizeTimeOverSizeThresholds)
        .build()

    val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setLoadControl(loadControl)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        .build()

    var lastError: PlaybackException? = null
        private set

    private var resumePlaybackWhenReady = false

    private val listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            lastError = error
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            resumeDeferredSeekPlaybackIfReady(playbackState)
        }
    }

    init {
        player.setSeekParameters(SeekParameters.CLOSEST_SYNC)
        player.addListener(listener)
    }

    fun prepare(
        uri: Uri,
        title: String,
        startPositionMs: Long = C.TIME_UNSET,
        playWhenReady: Boolean = true,
        requestHeaders: Map<String, String> = emptyMap(),
        captionTracks: List<StashCaptionTrack> = emptyList(),
    ) {
        lastError = null
        resumePlaybackWhenReady = false
        httpDataSourceFactory.setDefaultRequestProperties(requestHeaders)
        val item = MediaItem.Builder()
            .setUri(uri)
            .setSubtitleConfigurations(captionTracks.mapNotNull { it.toSubtitleConfigurationOrNull() })
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .build(),
            )
            .build()
        player.setMediaItem(item)
        if (startPositionMs != C.TIME_UNSET && startPositionMs > 0L) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    fun playPause() {
        if (player.isPlaying) {
            pause()
        } else {
            resumePlaybackWhenReady = false
            player.play()
        }
    }

    fun pause() {
        resumePlaybackWhenReady = false
        player.pause()
        player.playWhenReady = false
    }

    fun seekBy(deltaMs: Long) {
        seekTo(player.currentPosition + deltaMs)
    }

    fun seekTo(
        positionMs: Long,
        resumePlayback: Boolean = shouldResumePlaybackAfterSeek(
            wasPlaying = player.isPlaying,
            playWhenReady = player.playWhenReady,
        ),
    ) {
        val coercedPositionMs = coerceSeekRequestPosition(positionMs, player.duration)
        if (resumePlayback) {
            resumePlaybackWhenReady = true
            player.pause()
            player.playWhenReady = false
        } else {
            resumePlaybackWhenReady = false
        }
        player.seekTo(coercedPositionMs)
        resumeDeferredSeekPlaybackIfReady(player.playbackState)
    }

    fun resumePlaybackIfDesired(resumePlayback: Boolean) {
        if (!resumePlayback) return
        resumePlaybackWhenReady = true
        if (player.playbackState != Player.STATE_READY) {
            player.pause()
            player.playWhenReady = false
        }
        resumeDeferredSeekPlaybackIfReady(player.playbackState)
    }

    private fun resumeDeferredSeekPlaybackIfReady(playbackState: Int) {
        if (!shouldResumeDeferredSeekPlayback(resumePlaybackWhenReady, playbackState == Player.STATE_READY)) return
        resumePlaybackWhenReady = false
        player.playWhenReady = true
        player.play()
    }

    fun holdPlaybackForSeekPreview() {
        pause()
    }

    fun clearLastError() {
        lastError = null
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(coercePlayerPlaybackSpeed(speed))
    }

    private fun StashCaptionTrack.toSubtitleConfigurationOrNull(): MediaItem.SubtitleConfiguration? {
        val mimeType = stashCaptionMimeType(captionType) ?: return null
        val language = languageCode.trim().takeIf { it.isNotBlank() }
        return MediaItem.SubtitleConfiguration.Builder(Uri.parse(url))
            .setMimeType(mimeType)
            .setLanguage(language)
            .build()
    }

    fun applySubtitleLanguagePreference(
        preference: SubtitleLanguagePreference,
        languageCode: String?,
    ) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, shouldDisableSubtitleTrack(preference))
            .setPreferredTextLanguage(languageCode)
            .build()
    }

    fun release() {
        player.removeListener(listener)
        player.release()
    }
}

fun coercePlayerPlaybackSpeed(speed: Float): Float = speed.coerceIn(0.25f, 4f)

fun stashCaptionMimeType(captionType: String?): String? = when (captionType?.trim()?.lowercase()) {
    "vtt" -> MimeTypes.TEXT_VTT
    "srt" -> MimeTypes.APPLICATION_SUBRIP
    else -> null
}

@OptIn(UnstableApi::class)
private object StashPlayerMediaCache {
    private var sharedCache: SimpleCache? = null

    fun get(context: Context, policy: StashPlayerPreloadPolicy): SimpleCache = synchronized(this) {
        sharedCache ?: SimpleCache(
            File(context.cacheDir, policy.cacheDirectoryName),
            LeastRecentlyUsedCacheEvictor(policy.cacheSizeBytes),
            StandaloneDatabaseProvider(context),
        ).also { sharedCache = it }
    }
}
