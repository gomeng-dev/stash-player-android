package gomeng.dev.stashplayer.core.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class StashPlaybackSessionService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        StashPlaybackSessionRegistry.session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val session = StashPlaybackSessionRegistry.session
        if (session?.player?.playWhenReady != true) {
            stopSelf()
        }
    }
}

object StashPlaybackSessionRegistry {
    var session: MediaSession? = null
        private set

    fun register(session: MediaSession) {
        this.session = session
    }

    fun unregister(session: MediaSession) {
        if (this.session === session) {
            this.session = null
        }
    }
}
