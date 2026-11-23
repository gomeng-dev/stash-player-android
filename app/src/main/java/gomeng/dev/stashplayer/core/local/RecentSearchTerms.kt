package gomeng.dev.stashplayer.core.local

import gomeng.dev.stashplayer.core.model.normalizeStashDiscoveryQuery
import java.net.URLDecoder
import java.net.URLEncoder

data class RecentSearchTerm(val query: String, val favorite: Boolean = false)

internal fun List<RecentSearchTerm>.recordSearchTerm(query: String, favorite: Boolean? = null): List<RecentSearchTerm> {
    val term = normalizeStashDiscoveryQuery(query)
    if (term.isBlank()) return this
    val isFavorite = favorite ?: firstOrNull { it.query == term }?.favorite ?: false
    val (favorites, recents) = (listOf(RecentSearchTerm(term, isFavorite)) + filterNot { it.query == term })
        .partition { it.favorite }
    return favorites + recents.take(50)
}

internal fun List<RecentSearchTerm>.encodeRecentSearchTerms(): String = joinToString("\n") {
    (if (it.favorite) "1" else "0") + URLEncoder.encode(it.query, Charsets.UTF_8.name())
}

internal fun decodeRecentSearchTerms(value: String): List<RecentSearchTerm> = value.lineSequence()
    .filter { it.firstOrNull() == '0' || it.firstOrNull() == '1' }
    .mapNotNull { line ->
        val query = runCatching { URLDecoder.decode(line.drop(1), Charsets.UTF_8.name()) }.getOrNull()
        query?.takeIf(String::isNotBlank)?.let { RecentSearchTerm(it, favorite = line.first() == '1') }
    }.distinctBy { it.query }.toList()
