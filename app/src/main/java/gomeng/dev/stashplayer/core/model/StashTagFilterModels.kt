package gomeng.dev.stashplayer.core.model

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashSelectedTag(
    val id: String,
    val name: String,
)

data class StashSelectedEntity(
    val id: String,
    val name: String,
)

enum class StashRelationshipFilterKind(val koreanLabel: String) {
    Tag(stashString(R.string.auto_kr_0066)),
    Performer(stashString(R.string.auto_kr_0136)),
    Studio(stashString(R.string.auto_kr_0137)),
}

data class StashRelationshipFilterReadiness(
    val kind: StashRelationshipFilterKind,
    val canExposeFilterUi: Boolean,
    val blockerReason: String,
)

data class StashRelationshipFilterDraft(
    val entities: List<StashSelectedEntity> = emptyList(),
) {
    fun toggle(entity: StashSelectedEntity): StashRelationshipFilterDraft = copy(
        entities = toggleStashSelectedEntity(entities, entity),
    )

    fun reset(): StashRelationshipFilterDraft = copy(entities = emptyList())
}

fun currentStashRelationshipFilterReadiness(): List<StashRelationshipFilterReadiness> = listOf(
    StashRelationshipFilterReadiness(
        kind = StashRelationshipFilterKind.Tag,
        canExposeFilterUi = true,
        blockerReason = stashString(R.string.auto_kr_0138),
    ),
    StashRelationshipFilterReadiness(
        kind = StashRelationshipFilterKind.Performer,
        canExposeFilterUi = false,
        blockerReason = stashString(R.string.auto_kr_0139),
    ),
    StashRelationshipFilterReadiness(
        kind = StashRelationshipFilterKind.Studio,
        canExposeFilterUi = false,
        blockerReason = stashString(R.string.auto_kr_0140),
    ),
)

fun toggleStashSelectedEntity(
    selectedEntities: List<StashSelectedEntity>,
    entity: StashSelectedEntity,
): List<StashSelectedEntity> = if (selectedEntities.any { it.id == entity.id }) {
    selectedEntities.filterNot { it.id == entity.id }
} else {
    selectedEntities + entity
}

fun orderStashRelationshipEntityOptionsForDraft(
    selectedEntities: List<StashSelectedEntity>,
    entityOptions: List<StashSelectedEntity>,
): List<StashSelectedEntity> {
    val seenIds = mutableSetOf<String>()
    return buildList {
        selectedEntities.forEach { entity ->
            if (seenIds.add(entity.id)) add(entity)
        }
        entityOptions.forEach { entity ->
            if (seenIds.add(entity.id)) add(entity)
        }
    }
}

private fun StashSelectedTag.toRelationshipEntity(): StashSelectedEntity = StashSelectedEntity(id = id, name = name)

private fun StashSelectedEntity.toSelectedTag(): StashSelectedTag = StashSelectedTag(id = id, name = name)

fun toggleStashSelectedTag(
    selectedTags: List<StashSelectedTag>,
    tag: StashSelectedTag,
): List<StashSelectedTag> = toggleStashSelectedEntity(
    selectedEntities = selectedTags.map { it.toRelationshipEntity() },
    entity = tag.toRelationshipEntity(),
).map { it.toSelectedTag() }

data class StashTagFilterDraft(
    val tags: List<StashSelectedTag> = emptyList(),
) {
    fun toggle(tag: StashSelectedTag): StashTagFilterDraft = copy(tags = toggleStashSelectedTag(tags, tag))

    fun reset(): StashTagFilterDraft = copy(tags = emptyList())

    fun applyTo(filter: StashVideoFilterState): StashVideoFilterState = filter.copy(tags = tags)
}

fun StashVideoFilterState.toTagFilterDraft(): StashTagFilterDraft = StashTagFilterDraft(tags = tags)

fun orderStashTagOptionsForDraft(
    selectedTags: List<StashSelectedTag>,
    tagOptions: List<StashSelectedTag>,
): List<StashSelectedTag> = orderStashRelationshipEntityOptionsForDraft(
    selectedEntities = selectedTags.map { it.toRelationshipEntity() },
    entityOptions = tagOptions.map { it.toRelationshipEntity() },
).map { it.toSelectedTag() }

fun stashTagDraftSelectedCountLabel(selectedCount: Int): String = selectedCount
    .takeIf { it > 0 }
    ?.let { stashString(R.string.auto_kr_0141, it) }
    ?: stashString(R.string.auto_kr_0142)

enum class StashTagFilterApplyActionPlacement {
    StickyBottomBar,
}

data class StashTagFilterSheetLayoutPolicy(
    val usesWrappingOptionChips: Boolean = true,
    val usesWrappingSelectedSummaryChips: Boolean = true,
    val prefersLazyGridForLargeOptionSets: Boolean = true,
    val optionChipMaxLines: Int = 1,
    val ellipsizesLongTagNames: Boolean = true,
    val applyActionPlacement: StashTagFilterApplyActionPlacement = StashTagFilterApplyActionPlacement.StickyBottomBar,
    val applyActionAlwaysVisible: Boolean = true,
    val usesNavigationBarsPadding: Boolean = true,
    val contentBottomPaddingDp: Int = 96,
)

fun stashTagFilterSheetLayoutPolicy(): StashTagFilterSheetLayoutPolicy = StashTagFilterSheetLayoutPolicy()

fun stashTagFilterApplyContentDescription(): String = stashString(R.string.auto_kr_0143)

data class StashTagFilterTitleRowActions(
    val clearLabel: String = stashString(R.string.auto_kr_0144),
    val clearContentDescription: String = stashString(R.string.auto_kr_0145),
    val closeContentDescription: String = stashString(R.string.auto_kr_0146),
)

fun stashTagFilterTitleRowActions(): StashTagFilterTitleRowActions = StashTagFilterTitleRowActions()

fun stashTagFilterStickyActionSummary(selectedCount: Int): String = selectedCount
    .takeIf { it > 0 }
    ?.let { stashString(R.string.auto_kr_0147, it) }
    ?: stashString(R.string.auto_kr_0142)
