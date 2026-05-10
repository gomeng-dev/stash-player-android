package gomeng.dev.stashplayer.feature.shorts

import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.network.StashStream
import gomeng.dev.stashplayer.core.network.StashStreamResolver
import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.core.player.StashPlayerController

internal data class ShortsPreparedPlayback(
    val sceneId: String,
    val stream: StashStream,
    val controller: StashPlayerController,
)

internal class ShortsPlaybackControllerPool(
    private val context: Context,
    private val resolver: StashStreamResolver,
    private val onPlaybackEnded: (String) -> Unit,
    private val onPlaybackError: (String, String?) -> Unit,
) {
    private val prepared = linkedMapOf<String, ShortsPreparedPlayback>()
    private val errors = linkedMapOf<String, String?>()
    private val listeners = linkedMapOf<String, Player.Listener>()

    fun controllerFor(sceneId: String): StashPlayerController? = prepared[sceneId]?.controller

    fun streamFor(sceneId: String): StashStream? = prepared[sceneId]?.stream

    fun errorFor(sceneId: String): String? = errors[sceneId]

    fun clear(sceneId: String) {
        val playback = prepared.remove(sceneId)
        val listener = listeners.remove(sceneId)
        if (playback != null && listener != null) {
            playback.controller.player.removeListener(listener)
        }
        playback?.controller?.release()
        errors.remove(sceneId)
    }

    fun releaseOutside(sceneIds: Set<String>) {
        val staleIds = prepared.keys.filterNot { it in sceneIds }
        staleIds.forEach(::clear)
        errors.keys.filterNot { it in sceneIds }.forEach(errors::remove)
    }

    suspend fun ensurePrepared(
        scene: SceneCardModel,
        active: Boolean,
    ): ShortsPreparedPlayback? {
        prepared[scene.id]?.let { existing ->
            applyActiveState(existing.controller, active)
            return existing
        }

        return runCatching {
            val stream = resolver.resolve(scene.id)
            val controller = StashPlayerController(
                context = context,
                requestHeaders = stream.requestHeaders,
            )
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackEnded(scene.id)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    errors[scene.id] = redactStashCredentialText(error.message ?: error.errorCodeName)
                    onPlaybackError(scene.id, errors[scene.id])
                }
            }
            controller.player.addListener(listener)
            listeners[scene.id] = listener
            controller.player.volume = if (active) 1f else 0f
            controller.prepare(
                uri = stream.uri,
                title = stream.title,
                startPositionMs = 0L,
                playWhenReady = active,
                requestHeaders = stream.requestHeaders,
                captionTracks = stream.captionTracks,
            )
            if (!active) {
                controller.player.pause()
                controller.player.playWhenReady = false
            }
            val playback = ShortsPreparedPlayback(
                sceneId = scene.id,
                stream = stream,
                controller = controller,
            )
            prepared[scene.id] = playback
            errors.remove(scene.id)
            playback
        }.onFailure { throwable ->
            errors[scene.id] = redactStashCredentialText(throwable.message ?: throwable::class.simpleName.orEmpty())
        }.getOrNull()
    }

    fun activate(sceneId: String) {
        prepared.forEach { (id, playback) ->
            applyActiveState(playback.controller, id == sceneId)
        }
    }

    fun releaseAll() {
        prepared.keys.toList().forEach(::clear)
        errors.clear()
    }

    private fun applyActiveState(
        controller: StashPlayerController,
        active: Boolean,
    ) {
        controller.player.volume = if (active) 1f else 0f
        if (active) {
            controller.player.playWhenReady = true
            controller.player.play()
        } else {
            controller.player.pause()
            controller.player.playWhenReady = false
        }
    }
}
