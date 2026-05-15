package gomeng.dev.stashplayer.core.model

fun normalizeStashDiscoveryQuery(query: String): String = query.trim()

val DEFAULT_STASH_DISCOVERY_PAGE_SIZE = 40

fun defaultStashDiscoveryPageSizeOptions(): List<Int> = listOf(20, 40, 60, 120, 250, 500, 1000)
