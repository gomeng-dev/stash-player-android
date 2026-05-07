package gomeng.dev.stashplayer.core.ui.designsystem

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashMediaPrimitivePolicy(
    val iconActionMinTouchTargetDp: Int = StashTouch.MinTarget.value.toInt(),
    val cardCornerRadiusDp: Int = StashRadii.Card.value.toInt(),
    val thumbnailCornerRadiusDp: Int = StashRadii.Thumbnail.value.toInt(),
    val drawerTopCornerRadiusDp: Int = StashRadii.Sheet.value.toInt(),
    val tagChipMaxLines: Int = 1,
    val metadataBadgeMaxLines: Int = 1,
)

fun stashMediaPrimitivePolicy(): StashMediaPrimitivePolicy = StashMediaPrimitivePolicy()

enum class StashFilterChipTone {
    Selected,
    Unselected,
    Disabled,
}

data class StashFilterChipVisibilityPolicy(
    val containerRole: StashActionPillColorRole,
    val contentRole: StashActionPillColorRole,
    val borderRole: StashActionPillColorRole,
    val containerAlpha: Float,
    val contentAlpha: Float,
    val borderAlpha: Float,
    val usesSelectionAccent: Boolean,
)

fun stashFilterChipVisibilityPolicy(tone: StashFilterChipTone): StashFilterChipVisibilityPolicy = when (tone) {
    StashFilterChipTone.Selected -> StashFilterChipVisibilityPolicy(
        containerRole = StashActionPillColorRole.PrimaryContainer,
        contentRole = StashActionPillColorRole.OnPrimaryContainer,
        borderRole = StashActionPillColorRole.Primary,
        containerAlpha = 1f,
        contentAlpha = 1f,
        borderAlpha = 0.88f,
        usesSelectionAccent = true,
    )

    StashFilterChipTone.Unselected -> StashFilterChipVisibilityPolicy(
        containerRole = StashActionPillColorRole.SurfaceContainer,
        contentRole = StashActionPillColorRole.OnSurface,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = 0.78f,
        contentAlpha = 0.92f,
        borderAlpha = 0.34f,
        usesSelectionAccent = false,
    )

    StashFilterChipTone.Disabled -> StashFilterChipVisibilityPolicy(
        containerRole = StashActionPillColorRole.SurfaceVariant,
        contentRole = StashActionPillColorRole.OnSurfaceVariant,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = 0.56f,
        contentAlpha = 0.58f,
        borderAlpha = 0.22f,
        usesSelectionAccent = false,
    )
}

enum class StashActiveFilterChipAction {
    Edit,
    Clear,
}

data class StashActiveFilterChipActionModel(
    val label: String,
    val contentDescription: String,
    val tone: StashActionPillTone,
)

fun stashActiveFilterChipActionModel(
    label: String,
    action: StashActiveFilterChipAction,
): StashActiveFilterChipActionModel {
    val normalized = label.trim()
    return when (action) {
        StashActiveFilterChipAction.Edit -> StashActiveFilterChipActionModel(
            label = normalized,
            contentDescription = stashString(R.string.auto_kr_0386, normalized),
            tone = StashActionPillTone.Selected,
        )

        StashActiveFilterChipAction.Clear -> StashActiveFilterChipActionModel(
            label = stashString(R.string.auto_kr_0387, normalized),
            contentDescription = stashString(R.string.auto_kr_0388, normalized),
            tone = StashActionPillTone.Neutral,
        )
    }
}

enum class StashCompactChipKind {
    MetadataBadge,
    Tag,
}

data class StashCompactChipVisibilityPolicy(
    val containerRole: StashActionPillColorRole,
    val contentRole: StashActionPillColorRole,
    val borderRole: StashActionPillColorRole,
    val containerAlpha: Float,
    val contentAlpha: Float,
    val borderAlpha: Float,
)

fun stashCompactChipVisibilityPolicy(kind: StashCompactChipKind): StashCompactChipVisibilityPolicy = when (kind) {
    StashCompactChipKind.MetadataBadge -> StashCompactChipVisibilityPolicy(
        containerRole = StashActionPillColorRole.SurfaceContainer,
        contentRole = StashActionPillColorRole.OnSurface,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = 0.84f,
        contentAlpha = 0.96f,
        borderAlpha = 0.34f,
    )

    StashCompactChipKind.Tag -> StashCompactChipVisibilityPolicy(
        containerRole = StashActionPillColorRole.SurfaceVariant,
        contentRole = StashActionPillColorRole.OnSurfaceVariant,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = 0.80f,
        contentAlpha = 0.94f,
        borderAlpha = 0.28f,
    )
}

data class StashMetadataBadgeModel(
    val label: String,
    val contentDescription: String? = null,
) {
    val displayLabel: String = label.trim()
    val accessibilityLabel: String = contentDescription?.trim().takeUnless { it.isNullOrEmpty() } ?: displayLabel
    val isVisible: Boolean = displayLabel.isNotBlank()
}

data class StashTagChipModel(
    val label: String,
    val contentDescription: String? = null,
) {
    val displayLabel: String = label.trim()
    val accessibilityLabel: String = contentDescription?.trim().takeUnless { it.isNullOrEmpty() } ?: displayLabel
    val isVisible: Boolean = displayLabel.isNotBlank()
}

data class StashTagChipOverflowState(
    val visibleTags: List<StashTagChipModel>,
    val overflowCount: Int,
) {
    val hasOverflow: Boolean = overflowCount > 0
    val overflowLabel: String = if (hasOverflow) "+$overflowCount" else ""
}

fun buildStashTagChipOverflowState(
    tags: List<String>,
    maxVisible: Int,
): StashTagChipOverflowState {
    val normalized = tags
        .map(::StashTagChipModel)
        .filter { it.isVisible }
    val visibleCount = maxVisible.coerceAtLeast(0)
    return StashTagChipOverflowState(
        visibleTags = normalized.take(visibleCount),
        overflowCount = (normalized.size - visibleCount).coerceAtLeast(0),
    )
}

data class StashIconActionSemantics(
    val label: String,
    val selected: Boolean = false,
) {
    val contentDescription: String = buildString {
        append(label.trim())
        if (selected) append(stashString(R.string.auto_kr_0389))
    }.trim()
    val hasContentDescription: Boolean = contentDescription.isNotBlank()
}
