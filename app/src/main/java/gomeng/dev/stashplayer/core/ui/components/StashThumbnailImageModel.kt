package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.buildStashThumbnailRequestSpec

@Composable
fun rememberStashThumbnailModel(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): Any? {
    val context = LocalContext.current
    val requestSpec = remember(thumbnailUrl, serverProfile) {
        buildStashThumbnailRequestSpec(thumbnailUrl, serverProfile)
    }
    return remember(context, requestSpec) {
        requestSpec?.let { spec ->
            if (spec.requestHeaders.isEmpty()) {
                spec.url
            } else {
                ImageRequest.Builder(context)
                    .data(spec.url)
                    .apply {
                        spec.requestHeaders.forEach { (name, value) ->
                            setHeader(name, value)
                        }
                    }
                    .build()
            }
        }
    }
}
