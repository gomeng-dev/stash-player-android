package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.network.redactStashCredentialText
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

fun stashRating100ForStars(stars: Int): Int? = when (stars.coerceIn(0, 5)) {
    0 -> null
    else -> stars.coerceIn(1, 5) * 20
}

fun starsForStashRating100(rating100: Int?): Int = when {
    rating100 == null || rating100 <= 0 -> 0
    else -> ((rating100.coerceIn(1, 100) + 10) / 20).coerceIn(1, 5)
}

fun stashRating100ForRatingStep(ratingStep: Int): Int? = when (val step = ratingStep.coerceIn(0, 10)) {
    0 -> null
    else -> step * 10
}

fun ratingStepForStashRating100(rating100: Int?): Int = when {
    rating100 == null || rating100 <= 0 -> 0
    else -> ((rating100.coerceIn(1, 100) + 5) / 10).coerceIn(1, 10)
}

data class PlayerRatingState(
    val rating100: Int?,
    val isUpdating: Boolean = false,
    val previousRating100: Int? = null,
    val message: String? = null,
) {
    val ratingStep: Int
        get() = ratingStepForStashRating100(rating100)

    val stars: Float
        get() = ratingStep / 2f

    fun optimisticallySelectStars(stars: Int): PlayerRatingState =
        optimisticallySelectRatingStep(stars.coerceIn(0, 5) * 2)

    fun optimisticallySelectRatingStep(ratingStep: Int): PlayerRatingState = copy(
        rating100 = stashRating100ForRatingStep(ratingStep),
        isUpdating = true,
        previousRating100 = rating100,
        message = stashString(R.string.auto_kr_0253),
    )

    fun completeUpdate(): PlayerRatingState = copy(
        isUpdating = false,
        previousRating100 = null,
        message = stashString(R.string.auto_kr_0254, playerRatingSliderLabel(ratingStep)),
    )

    fun failUpdate(errorMessage: String?): PlayerRatingState = copy(
        rating100 = previousRating100,
        isUpdating = false,
        previousRating100 = null,
        message = stashString(
            R.string.auto_kr_0255,
            redactStashCredentialText(errorMessage).ifBlank { stashString(R.string.auto_kr_0447) },
        ),
    )
}
