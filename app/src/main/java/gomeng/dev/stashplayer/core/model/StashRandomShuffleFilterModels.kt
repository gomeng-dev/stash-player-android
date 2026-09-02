package gomeng.dev.stashplayer.core.model

import kotlin.random.Random

const val STASH_RANDOM_SORT_MAX_SEED = 99_999_999

fun nextStashRandomSortSeed(): Int = Random.Default.nextInt(STASH_RANDOM_SORT_MAX_SEED + 1)

fun normalizeStashRandomSortSeed(seed: Int): Int = Math.floorMod(seed, STASH_RANDOM_SORT_MAX_SEED + 1)

fun freshStashRandomServerSort(
    baseSort: String,
    previousSort: String? = null,
    seed: Int? = null,
): String {
    if (baseSort != "random") return baseSort
    val previousSeed = previousSort
        ?.takeIf { it.startsWith("random_") }
        ?.substringAfter("random_")
        ?.toIntOrNull()
    val candidate = normalizeStashRandomSortSeed(seed ?: nextStashRandomSortSeed())
    val freshSeed = if (candidate == previousSeed) normalizeStashRandomSortSeed(candidate + 1) else candidate
    return "random_$freshSeed"
}

fun StashVideoFilterState.withStashRandomShuffleSeed(seed: Int): StashVideoFilterState = copy(
    randomShuffle = true,
    randomShuffleSeed = normalizeStashRandomSortSeed(seed),
)

fun StashVideoFilterState.withGeneratedStashRandomShuffleSeedIfNeeded(): StashVideoFilterState = when {
    !randomShuffle && randomShuffleSeed != null -> withoutStashRandomShuffle()
    randomShuffle && randomShuffleSeed == null -> withStashRandomShuffleSeed(nextStashRandomSortSeed())
    else -> this
}

fun StashVideoFilterState.withoutStashRandomShuffle(): StashVideoFilterState = copy(
    randomShuffle = false,
    randomShuffleSeed = null,
)
