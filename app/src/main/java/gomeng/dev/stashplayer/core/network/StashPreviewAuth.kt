package gomeng.dev.stashplayer.core.network

fun StashServerProfile.spritePreviewHeadersFor(frame: StashSpriteFrame): Map<String, String> {
    return authHeadersFor(frame.url)
}
