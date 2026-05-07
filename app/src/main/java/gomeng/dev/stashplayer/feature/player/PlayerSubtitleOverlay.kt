package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gomeng.dev.stashplayer.core.player.SubtitlePosition
import gomeng.dev.stashplayer.core.player.SubtitleTextAlignment
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha

@Composable
fun BoxScope.PlayerSubtitleOverlay(
    cueText: String?,
    fontScale: Float,
    position: SubtitlePosition,
    alignment: SubtitleTextAlignment,
    modifier: Modifier = Modifier,
) {
    val normalizedCueText = cueText?.trim()?.takeIf { it.isNotBlank() } ?: return
    Text(
        text = normalizedCueText,
        modifier = modifier
            .align(position.toBoxAlignment())
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .background(Color.Black.copy(alpha = StashAlpha.PlayerHudSurface), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.White,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = (18f * fontScale).sp),
        textAlign = alignment.toTextAlign(),
    )
}

private fun SubtitlePosition.toBoxAlignment(): Alignment = when (this) {
    SubtitlePosition.Bottom -> Alignment.BottomCenter
    SubtitlePosition.Middle -> Alignment.Center
    SubtitlePosition.Top -> Alignment.TopCenter
}

private fun SubtitleTextAlignment.toTextAlign(): TextAlign = when (this) {
    SubtitleTextAlignment.Start -> TextAlign.Start
    SubtitleTextAlignment.Center -> TextAlign.Center
    SubtitleTextAlignment.End -> TextAlign.End
}
