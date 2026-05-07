package gomeng.dev.stashplayer.core.model

import kotlin.random.Random

const val STASH_RANDOM_SORT_MAX_SEED = 99_999_999

fun nextStashRandomSortSeed(): Int = Random.Default.nextInt(STASH_RANDOM_SORT_MAX_SEED + 1)

fun normalizeStashRandomSortSeed(seed: Int): Int = Math.floorMod(seed, STASH_RANDOM_SORT_MAX_SEED + 1)

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
