package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
fun shouldShowSceneCardQuickActionsInMediaGrid(): Boolean = true

fun sceneCardOverlayActionMinSizeDp(): Int = 34

fun sceneCardOverlayActionHorizontalPaddingDp(): Int = 6

fun shouldWrapUnifiedFilterTagChips(): Boolean = true

fun shouldWrapActiveFilterChips(): Boolean = true

fun activeFilterChipEditContentDescription(label: String): String = stashString(R.string.auto_kr_0118, label)

fun activeFilterChipClearContentDescription(label: String): String = stashString(R.string.auto_kr_0119, label)

fun unifiedFilterPanelTagOptionLimit(): Int = 36

fun queueThumbnailWidthDp(isFoldLikeLayout: Boolean): Int = if (isFoldLikeLayout) 112 else 96

fun queueThumbnailHeightDp(isFoldLikeLayout: Boolean): Int = if (isFoldLikeLayout) 72 else 64

fun favoriteToggleFeedbackText(willFavorite: Boolean): String =
    if (willFavorite) stashString(R.string.auto_kr_0120) else stashString(R.string.auto_kr_0121)

fun shouldUseLocalFavoriteDiscoveryResults(
    query: String,
    videoFilter: StashVideoFilterState,
): Boolean = query.isBlank() &&
    videoFilter.localFavoriteOnly &&
    videoFilter.copy(
        localFavoriteOnly = false,
        randomShuffle = false,
        randomShuffleSeed = null,
        savedFilter = null,
    ).isEmpty

fun shouldLoadDiscoveryResultsFromServer(
    query: String,
    videoFilter: StashVideoFilterState,
): Boolean = !shouldUseLocalFavoriteDiscoveryResults(query, videoFilter)
