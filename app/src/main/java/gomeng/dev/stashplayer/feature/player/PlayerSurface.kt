package gomeng.dev.stashplayer.feature.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.StashPlayerController

@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    controller: StashPlayerController,
    aspectRatioMode: AspectRatioMode,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                player = controller.player
                useController = false
                keepScreenOn = true
                subtitleView?.visibility = View.GONE
                resizeMode = aspectRatioMode.toResizeMode()
            }
        },
        update = { view ->
            view.player = controller.player
            view.resizeMode = aspectRatioMode.toResizeMode()
        },
    )
}

@OptIn(UnstableApi::class)
private fun AspectRatioMode.toResizeMode(): Int = when (this) {
    AspectRatioMode.Fit -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    AspectRatioMode.Crop -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    AspectRatioMode.Stretch -> AspectRatioFrameLayout.RESIZE_MODE_FILL
}
