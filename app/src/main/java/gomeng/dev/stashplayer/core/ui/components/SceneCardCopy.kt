package gomeng.dev.stashplayer.core.ui.components

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

fun watchLaterBadgeLabel(): String = stashString(R.string.auto_kr_0016)

fun favoriteBadgeLabel(): String = stashString(R.string.auto_kr_0238)

fun watchLaterToggleLabel(isInWatchLater: Boolean): String = if (isInWatchLater) {
    stashString(R.string.auto_kr_0231)
} else {
    stashString(R.string.auto_kr_0016)
}

data class SceneCardOverlayActionCopy(
    val contentDescription: String,
    val selected: Boolean,
)

fun shouldShowSceneCardInlineSubtitle(
    subtitle: String?,
    hasMetadataBadges: Boolean,
    hasTagChips: Boolean,
): Boolean = !subtitle.isNullOrBlank() && !hasMetadataBadges && !hasTagChips

fun favoriteSceneCardActionCopy(isFavorite: Boolean): SceneCardOverlayActionCopy = SceneCardOverlayActionCopy(
    contentDescription = if (isFavorite) stashString(R.string.auto_kr_0229) else stashString(R.string.auto_kr_0238),
    selected = isFavorite,
)

fun watchLaterSceneCardActionCopy(isInWatchLater: Boolean): SceneCardOverlayActionCopy = SceneCardOverlayActionCopy(
    contentDescription = watchLaterToggleLabel(isInWatchLater),
    selected = isInWatchLater,
)

fun queueSceneCardActionCopy(isInQueue: Boolean): SceneCardOverlayActionCopy = SceneCardOverlayActionCopy(
    contentDescription = if (isInQueue) {
        stashString(R.string.scene_card_queue_remove_content_description)
    } else {
        stashString(R.string.auto_kr_0010)
    },
    selected = isInQueue,
)
