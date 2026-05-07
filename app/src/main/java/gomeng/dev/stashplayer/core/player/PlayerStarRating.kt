package gomeng.dev.stashplayer.core.player

import kotlin.math.ceil
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

enum class PlayerStarFillState {
    Empty,
    Half,
    Full,
}

fun starFillStatesForRatingStep(ratingStep: Int): List<PlayerStarFillState> {
    val step = ratingStep.coerceIn(0, 10)
    return List(PlayerStarRatingStarCount) { index ->
        val starStep = (index + 1) * 2
        when {
            step >= starStep -> PlayerStarFillState.Full
            step == starStep - 1 -> PlayerStarFillState.Half
            else -> PlayerStarFillState.Empty
        }
    }
}

fun ratingStepFromTouchFraction(fraction: Float): Int = when {
    fraction <= 0f -> 0
    fraction >= 1f -> 10
    else -> ceil(fraction * 10f).toInt().coerceIn(1, 10)
}

fun ratingStepFromTouchPosition(xPx: Float, widthPx: Float): Int {
    if (widthPx <= 0f) return 0
    return ratingStepFromTouchFraction(xPx / widthPx)
}

data class PlayerStarRatingDragPolicy(
    val trackPressesAcrossRow: Boolean,
    val trackMovesAcrossRow: Boolean,
    val consumeMoveEvents: Boolean,
)

fun resolvePlayerStarRatingDragPolicy(ratingUpdating: Boolean): PlayerStarRatingDragPolicy = PlayerStarRatingDragPolicy(
    trackPressesAcrossRow = true,
    trackMovesAcrossRow = true,
    consumeMoveEvents = true,
)

fun playerStarRatingDragTrackingEnabled(ratingUpdating: Boolean): Boolean =
    resolvePlayerStarRatingDragPolicy(ratingUpdating).trackPressesAcrossRow

fun playerStarRatingContentDescription(ratingStep: Int): String {
    val step = ratingStep.coerceIn(0, 10)
    return when {
        step == 0 -> stashString(R.string.auto_kr_0256)
        step % 2 == 0 -> stashString(R.string.auto_kr_0257, step / 2)
        else -> stashString(R.string.auto_kr_0258, step / 2)
    }
}

val PlayerStarRatingStarCount: Int = 5
