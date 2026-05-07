package gomeng.dev.stashplayer.feature.queue

import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.buildStashThumbnailModel

fun buildQueueThumbnailModel(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): String? = buildStashThumbnailModel(thumbnailUrl, serverProfile)
