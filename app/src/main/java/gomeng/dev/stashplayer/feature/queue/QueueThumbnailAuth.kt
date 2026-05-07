package gomeng.dev.stashplayer.feature.queue

import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.buildStashThumbnailModel
import gomeng.dev.stashplayer.core.network.buildStashThumbnailRequestSpec

fun buildQueueThumbnailModel(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
): String? = buildStashThumbnailModel(thumbnailUrl, serverProfile)

fun buildQueueThumbnailRequestSpec(
    thumbnailUrl: String?,
    serverProfile: StashServerProfile?,
) = buildStashThumbnailRequestSpec(thumbnailUrl, serverProfile)
