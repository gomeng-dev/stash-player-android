package gomeng.dev.stashplayer.core.player

import android.content.Context
import android.net.Uri
import gomeng.dev.stashplayer.core.network.StashCaptionTrack
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@OptIn(UnstableApi::class)
class StashPlayerController(
    context: Context,
    requestHeaders: Map<String, String> = emptyMap(),
) {
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(requestHeaders)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
        .build()

    var lastError: PlaybackException? = null
        private set

    private val listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            lastError = error
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
        if (player.isPlaying) player.pause() else player.play()
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
            player.playWhenReady = true
        }
        player.seekTo(coercedPositionMs)
        resumePlaybackIfDesired(resumePlayback)
    }

    fun resumePlaybackIfDesired(resumePlayback: Boolean) {
        if (resumePlayback) {
            player.playWhenReady = true
            player.play()
        }
    }

    fun holdPlaybackForSeekPreview() {
        player.pause()
        player.playWhenReady = false
    }

    fun clearLastError() {
        lastError = null
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed.coerceIn(0.25f, 3f))
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

fun stashCaptionMimeType(captionType: String?): String? = when (captionType?.trim()?.lowercase()) {
    "vtt" -> MimeTypes.TEXT_VTT
    "srt" -> MimeTypes.APPLICATION_SUBRIP
    else -> null
}
