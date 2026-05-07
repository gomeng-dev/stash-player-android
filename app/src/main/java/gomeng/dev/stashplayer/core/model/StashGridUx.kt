package gomeng.dev.stashplayer.core.model

fun stashMediaGridColumnCount(isFoldLikeLayout: Boolean): Int = 2

fun stashMediaGridThumbnailHeightDp(isFoldLikeLayout: Boolean): Int = if (isFoldLikeLayout) 180 else 156
