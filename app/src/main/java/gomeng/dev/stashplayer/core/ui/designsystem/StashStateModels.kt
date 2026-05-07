package gomeng.dev.stashplayer.core.ui.designsystem

import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
data class StashEmptyStateModel(
    val title: String,
    val message: String,
    val primaryActionLabel: String? = null,
) {
    val hasPrimaryAction: Boolean
        get() = !primaryActionLabel.isNullOrBlank()
}

data class StashErrorStateModel(
    val title: String,
    val message: String,
    val primaryActionLabel: String = stashString(R.string.auto_kr_0031),
    val secondaryActionLabel: String? = null,
) {
    val hasPrimaryAction: Boolean
        get() = primaryActionLabel.isNotBlank()

    val hasSecondaryAction: Boolean
        get() = !secondaryActionLabel.isNullOrBlank()
}

data class StashSectionHeaderModel(
    val title: String,
    val subtitle: String? = null,
    val itemCount: Int? = null,
    val actionLabel: String? = null,
) {
    val displayTitle: String
        get() = if (itemCount != null) stashString(R.string.auto_kr_0395, title, itemCount) else title

    val hasAction: Boolean
        get() = !actionLabel.isNullOrBlank()
}

enum class StashStatePanelTone {
    Empty,
    Error,
}

data class StashStateComponentPolicy(
    val panelCornerRadiusDp: Int,
    val panelPaddingDp: Int,
    val panelSurface: String,
    val emptyPrimaryButton: String,
    val errorPrimaryButton: String,
    val titleFontWeight: Int,
)

fun stashStateComponentPolicy(): StashStateComponentPolicy = StashStateComponentPolicy(
    panelCornerRadiusDp = StashRadii.Card.value.toInt(),
    panelPaddingDp = StashSpacing.CardPadding.value.toInt(),
    panelSurface = "StashGlassSurface",
    emptyPrimaryButton = "StashPrimaryButton",
    errorPrimaryButton = "StashSecondaryButton",
    titleFontWeight = 700,
)

data class StashStatePanelModel(
    val title: String,
    val message: String,
    val actionLabels: List<String>,
    val tone: StashStatePanelTone,
    val accessibilityLabel: String,
)

fun stashStatePanelModel(
    title: String,
    message: String,
    actionLabels: List<String> = emptyList(),
    tone: StashStatePanelTone,
    contentDescription: String? = null,
): StashStatePanelModel {
    val normalizedTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0396) }
    val normalizedMessage = message.trim()
    val normalizedActions = actionLabels
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val fallbackAccessibility = listOf(normalizedTitle, normalizedMessage)
        .plus(normalizedActions)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    return StashStatePanelModel(
        title = normalizedTitle,
        message = normalizedMessage,
        actionLabels = normalizedActions,
        tone = tone,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
    )
}

fun stashRenderedStateActionLabels(
    primaryActionLabel: String?,
    hasPrimaryAction: Boolean,
    secondaryActionLabel: String?,
    hasSecondaryAction: Boolean,
): List<String> = buildList {
    if (hasPrimaryAction) {
        primaryActionLabel.normalizedOrNull()?.let(::add)
    }
    if (hasSecondaryAction) {
        secondaryActionLabel.normalizedOrNull()?.let(::add)
    }
}

data class StashSectionHeaderPresentation(
    val title: String,
    val subtitle: String?,
    val countBadgeLabel: String?,
    val actionLabel: String?,
    val accessibilityLabel: String,
)

fun stashSectionHeaderPresentation(
    state: StashSectionHeaderModel,
    contentDescription: String? = null,
): StashSectionHeaderPresentation {
    val normalizedTitle = state.title.trim().ifBlank { stashString(R.string.auto_kr_0392) }
    val normalizedSubtitle = state.subtitle.normalizedOrNull()
    val countBadgeLabel = state.itemCount?.let { count -> stashString(R.string.auto_kr_0397, count.coerceAtLeast(0)) }
    val normalizedAction = state.actionLabel.normalizedOrNull()
    val fallbackAccessibility = listOf(normalizedTitle, normalizedSubtitle, countBadgeLabel)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
    return StashSectionHeaderPresentation(
        title = normalizedTitle,
        subtitle = normalizedSubtitle,
        countBadgeLabel = countBadgeLabel,
        actionLabel = normalizedAction,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
    )
}

private fun String?.normalizedOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }
