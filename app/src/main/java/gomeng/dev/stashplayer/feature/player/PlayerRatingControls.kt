package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.ui.components.StashStarRatingSlider
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.player.playerStarRatingDragTrackingEnabled
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerRatingControls(
    ratingStep: Int,
    ratingMessage: String?,
    ratingUpdating: Boolean,
    onSelectRatingStep: (Int) -> Unit,
    onRatingInteractionStart: () -> Unit = {},
    onRatingInteractionEnd: () -> Unit = {},
) {
    val errorMessage = ratingMessage?.takeIf { it.startsWith(stashString(R.string.auto_kr_0233)) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StashStarRatingSlider(
            ratingStep = ratingStep,
            contentDescriptionPrefix = stashString(R.string.auto_kr_0469),
            enabled = playerStarRatingDragTrackingEnabled(ratingUpdating),
            onSelectRatingStep = onSelectRatingStep,
            onInteractionStart = onRatingInteractionStart,
            onInteractionEnd = onRatingInteractionEnd,
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
