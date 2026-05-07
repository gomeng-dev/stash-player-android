package gomeng.dev.stashplayer.core.player

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import gomeng.dev.stashplayer.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StashPictureInPictureRequest(
    val enabled: Boolean,
    val playbackReady: Boolean,
    val isPlaying: Boolean,
    val locked: Boolean,
    val modalSurfaceOpen: Boolean,
    val canPlayPrevious: Boolean = false,
    val canPlayNext: Boolean = false,
    val aspectRatio: Rational = Rational(16, 9),
)

interface StashPictureInPictureActionHandler {
    fun onPlayPause()
    fun onPrevious()
    fun onNext()
}

object StashPictureInPictureController {
    private var request: StashPictureInPictureRequest? = null
    private var actionHandler: StashPictureInPictureActionHandler? = null
    private val mutableActive = MutableStateFlow(false)

    val active: StateFlow<Boolean> = mutableActive.asStateFlow()

    fun register(request: StashPictureInPictureRequest, activity: Activity?) {
        this.request = request
        updateParams(activity)
    }

    fun unregister(request: StashPictureInPictureRequest) {
        if (this.request == request) {
            this.request = null
        }
    }

    fun registerActionHandler(handler: StashPictureInPictureActionHandler) {
        actionHandler = handler
    }

    fun unregisterActionHandler(handler: StashPictureInPictureActionHandler) {
        if (actionHandler == handler) {
            actionHandler = null
        }
    }

    fun setActive(active: Boolean) {
        mutableActive.value = active
    }

    fun isSupported(activity: Activity?): Boolean =
        activity != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    fun updateParams(activity: Activity?) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val currentRequest = request ?: return
        runCatching {
            activity.setPictureInPictureParams(buildParams(activity, currentRequest))
        }
    }

    fun enterIfEligible(activity: Activity?): Boolean {
        val currentRequest = request ?: return false
        val supported = isSupported(activity)
        if (
            activity == null ||
            !shouldEnterPictureInPictureOnUserLeave(
                pictureInPictureEnabled = currentRequest.enabled,
                pictureInPictureSupported = supported,
                playerRouteActive = true,
                playbackReady = currentRequest.playbackReady,
                locked = currentRequest.locked,
                modalSurfaceOpen = currentRequest.modalSurfaceOpen,
            )
        ) {
            return false
        }
        return runCatching {
            activity.enterPictureInPictureMode(buildParams(activity, currentRequest))
        }.getOrDefault(false)
    }

    internal fun dispatch(action: String?) {
        val handler = actionHandler ?: return
        when (action) {
            ACTION_PLAY_PAUSE -> handler.onPlayPause()
            ACTION_PREVIOUS -> handler.onPrevious()
            ACTION_NEXT -> handler.onNext()
        }
    }

    private fun buildParams(context: Context, request: StashPictureInPictureRequest): PictureInPictureParams {
        val actions = buildPictureInPictureActions(context, request)
        return PictureInPictureParams.Builder()
            .setAspectRatio(request.aspectRatio)
            .setActions(actions)
            .build()
    }

    internal fun buildPictureInPictureActions(
        context: Context,
        request: StashPictureInPictureRequest,
    ): List<RemoteAction> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()
        return buildPictureInPictureActionTypes(
            canPlayPrevious = request.canPlayPrevious,
            canPlayNext = request.canPlayNext,
        ).map { actionType ->
            when (actionType) {
                PictureInPictureActionType.Previous -> remoteAction(
                    context = context,
                    action = ACTION_PREVIOUS,
                    title = context.getString(R.string.player_pip_action_previous),
                    iconRes = android.R.drawable.ic_media_previous,
                )
                PictureInPictureActionType.PlayPause -> remoteAction(
                    context = context,
                    action = ACTION_PLAY_PAUSE,
                    title = context.getString(
                        if (request.isPlaying) R.string.player_pip_action_pause else R.string.player_pip_action_play,
                    ),
                    iconRes = if (request.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                )
                PictureInPictureActionType.Next -> remoteAction(
                    context = context,
                    action = ACTION_NEXT,
                    title = context.getString(R.string.player_pip_action_next),
                    iconRes = android.R.drawable.ic_media_next,
                )
            }
        }
    }

    private fun remoteAction(
        context: Context,
        action: String,
        title: String,
        iconRes: Int,
    ): RemoteAction {
        val intent = Intent(context, StashPictureInPictureActionReceiver::class.java).setAction(action)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return RemoteAction(
            Icon.createWithResource(context, iconRes),
            title,
            title,
            pendingIntent,
        )
    }

    private const val ACTION_PLAY_PAUSE = "gomeng.dev.stashplayer.action.PIP_PLAY_PAUSE"
    private const val ACTION_PREVIOUS = "gomeng.dev.stashplayer.action.PIP_PREVIOUS"
    private const val ACTION_NEXT = "gomeng.dev.stashplayer.action.PIP_NEXT"
}

class StashPictureInPictureActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        StashPictureInPictureController.dispatch(intent.action)
    }
}
