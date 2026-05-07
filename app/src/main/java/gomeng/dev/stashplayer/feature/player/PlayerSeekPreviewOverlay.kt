package gomeng.dev.stashplayer.feature.player

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.player.playerSeekPreviewDeltaLabel
import gomeng.dev.stashplayer.core.player.playerSeekPreviewPositionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.roundToInt

@Composable
fun PlayerSeekPreviewOverlay(
    preview: PlayerSeekPreview,
    requestHeadersFor: (StashSpriteFrame) -> Map<String, String>,
) {
    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.72f), MaterialTheme.shapes.medium)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (preview.frame != null) {
            SpriteFrameImage(
                frame = preview.frame,
                requestHeaders = requestHeadersFor(preview.frame),
                modifier = Modifier
                    .width(220.dp)
                    .aspectRatio(preview.frame.width.toFloat() / preview.frame.height.toFloat()),
            )
        }
        Text(
            text = playerSeekPreviewDeltaLabel(preview.deltaMs),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = playerSeekPreviewPositionLabel(preview.targetPositionMs, preview.durationMs),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SpriteFrameImage(
    frame: StashSpriteFrame,
    requestHeaders: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val okHttpClient = remember { OkHttpClient() }
    var bitmap by remember(frame.url) { mutableStateOf<ImageBitmap?>(null) }
    var failed by remember(frame.url) { mutableStateOf(false) }

    LaunchedEffect(frame.url, requestHeaders) {
        bitmap = null
        failed = false
        val loadedBitmap = withContext(Dispatchers.IO) {
            runCatching {
                val requestBuilder = Request.Builder().url(frame.url)
                requestHeaders.forEach { (name, value) -> requestBuilder.header(name, value) }
                okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    val decoded = BitmapFactory.decodeStream(response.body?.byteStream())
                    requireNotNull(decoded) { "Unable to decode preview sprite" }
                    decoded.asImageBitmap()
                }
            }.getOrNull()
        }
        if (loadedBitmap == null) {
            failed = true
        } else {
            bitmap = loadedBitmap
        }
    }

    Box(
        modifier = modifier.background(Color.DarkGray, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        when {
            image != null -> Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = image,
                    srcOffset = IntOffset(frame.x, frame.y),
                    srcSize = IntSize(frame.width, frame.height),
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                )
            }
            failed -> Text(
                text = "preview unavailable",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
