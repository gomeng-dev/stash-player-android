package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

enum class StashOCounterComparator(
    val storageId: String,
    val symbol: String,
) {
    Equals("eq", "="),
    GreaterThan("gt", ">"),
    GreaterThanOrEqual("gte", "≥"),
    LessThan("lt", "<"),
    LessThanOrEqual("lte", "≤"),
}

data class StashOCounterFilter(
    val comparator: StashOCounterComparator,
    val value: Int,
) {
    init {
        require(value >= 0) { "O-Count filter value must be non-negative" }
    }

    val isNoOp: Boolean
        get() = comparator == StashOCounterComparator.GreaterThanOrEqual && value == 0

    val chipLabel: String
        get() = stashString(R.string.scene_filter_o_count_chip, comparator.symbol, value)

    fun toGraphQlCriterionOrNull(): Map<String, Any?>? = when (comparator) {
        StashOCounterComparator.Equals -> mapOf("value" to value, "modifier" to "EQUALS")
        StashOCounterComparator.GreaterThan -> mapOf("value" to value, "modifier" to "GREATER_THAN")
        StashOCounterComparator.GreaterThanOrEqual -> if (value <= 0) {
            null
        } else {
            mapOf("value" to value - 1, "modifier" to "GREATER_THAN")
        }
        StashOCounterComparator.LessThan -> mapOf("value" to value, "modifier" to "LESS_THAN")
        StashOCounterComparator.LessThanOrEqual -> if (value == Int.MAX_VALUE) {
            null
        } else {
            mapOf("value" to value + 1, "modifier" to "LESS_THAN")
        }
    }
}

fun stashOCounterComparatorOptions(): List<StashOCounterComparator> = StashOCounterComparator.entries

fun buildStashOCounterFilterFromInput(
    comparator: StashOCounterComparator,
    input: String,
): StashOCounterFilter? {
    val value = input.trim().toIntOrNull() ?: return null
    val filter = value.takeIf { it >= 0 }?.let { StashOCounterFilter(comparator, it) } ?: return null
    return filter.takeUnless { it.isNoOp }
}

fun stashOCounterComparatorFromStorageId(storageId: String?): StashOCounterComparator? =
    StashOCounterComparator.entries.firstOrNull { it.storageId == storageId }
