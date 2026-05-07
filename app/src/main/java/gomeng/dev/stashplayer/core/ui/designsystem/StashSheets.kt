package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

/**
 * Visual policy for dark/glass bottom sheet surfaces and option rows.
 *
 * The policy is intentionally pure so future sheet migrations can test the
 * shared selected/current/divider contract without rendering Compose.
 */
data class StashSheetPolicy(
    val cornerRadiusDp: Int,
    val contentPaddingDp: Int,
    val headerGapDp: Int,
    val optionContentGapDp: Int,
    val optionMinHeightDp: Int,
    val optionCornerRadiusDp: Int,
    val currentAccentWidthDp: Int,
    val dividerAlpha: Float,
    val borderAlpha: Float,
    val selectedBorderAlpha: Float,
)

fun stashSheetPolicy(): StashSheetPolicy = StashSheetPolicy(
    cornerRadiusDp = StashRadii.Sheet.value.toInt(),
    contentPaddingDp = StashSpacing.CardPadding.value.toInt(),
    headerGapDp = StashSpacing.CardGap.value.toInt(),
    optionContentGapDp = StashSpacing.ChipGap.value.toInt(),
    optionMinHeightDp = 58,
    optionCornerRadiusDp = StashRadii.Card.value.toInt(),
    currentAccentWidthDp = 4,
    dividerAlpha = StashAlpha.BorderSubtle,
    borderAlpha = StashAlpha.BorderSubtle,
    selectedBorderAlpha = StashAlpha.BorderStrong,
)

data class StashSheetHeaderModel(
    val title: String,
    val subtitle: String?,
    val actionLabel: String?,
    val accessibilityLabel: String,
)

fun stashSheetHeaderModel(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    contentDescription: String? = null,
): StashSheetHeaderModel {
    val normalizedTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0393) }
    val normalizedSubtitle = subtitle.normalizedOrNull()
    val normalizedAction = actionLabel.normalizedOrNull()
    val fallbackAccessibility = listOf(normalizedTitle, normalizedSubtitle)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
    return StashSheetHeaderModel(
        title = normalizedTitle,
        subtitle = normalizedSubtitle,
        actionLabel = normalizedAction,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
    )
}

enum class StashSheetOptionTone {
    Normal,
    Selected,
    Current,
    Disabled,
}

data class StashSheetOptionRowModel(
    val title: String,
    val subtitle: String?,
    val leadingLabel: String?,
    val trailingLabel: String?,
    val selected: Boolean,
    val current: Boolean,
    val enabled: Boolean,
    val tone: StashSheetOptionTone,
    val accessibilityLabel: String,
    val stateDescription: String?,
) {
    val isInteractive: Boolean = enabled
}

fun stashSheetOptionRowModel(
    title: String,
    subtitle: String? = null,
    leadingLabel: String? = null,
    trailingLabel: String? = null,
    selected: Boolean = false,
    current: Boolean = false,
    enabled: Boolean = true,
    contentDescription: String? = null,
): StashSheetOptionRowModel {
    val normalizedTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0393) }
    val normalizedSubtitle = subtitle.normalizedOrNull()
    val normalizedLeading = leadingLabel.normalizedOrNull()
    val normalizedTrailing = trailingLabel.normalizedOrNull()
    val tone = when {
        !enabled -> StashSheetOptionTone.Disabled
        current -> StashSheetOptionTone.Current
        selected -> StashSheetOptionTone.Selected
        else -> StashSheetOptionTone.Normal
    }
    val fallbackAccessibility = listOf(
        normalizedLeading,
        normalizedTitle,
        normalizedSubtitle,
        normalizedTrailing,
    ).filter { !it.isNullOrBlank() }.joinToString(" ")
    val stateDescription = when (tone) {
        StashSheetOptionTone.Current -> stashString(R.string.auto_kr_0394)
        StashSheetOptionTone.Selected -> stashString(R.string.auto_kr_0298)
        StashSheetOptionTone.Normal,
        StashSheetOptionTone.Disabled,
        -> null
    }
    return StashSheetOptionRowModel(
        title = normalizedTitle,
        subtitle = normalizedSubtitle,
        leadingLabel = normalizedLeading,
        trailingLabel = normalizedTrailing,
        selected = selected,
        current = current,
        enabled = enabled,
        tone = tone,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
        stateDescription = stateDescription,
    )
}

@Composable
fun StashBottomSheetContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(StashSpacing.CardPadding),
    fillMaxWidth: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val policy = stashSheetPolicy()
    val treatment = stashSurfaceTreatment(StashSurfaceRole.Sheet)
    val surfaceModifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
    Surface(
        modifier = surfaceModifier,
        shape = RoundedCornerShape(policy.cornerRadiusDp.dp),
        color = treatment.containerRole.toStashSurfaceThemeColor().copy(alpha = treatment.containerAlpha),
        contentColor = treatment.contentRole.toStashSurfaceThemeColor(),
        tonalElevation = treatment.tonalElevationDp.dp,
        border = BorderStroke(
            width = 1.dp,
            color = treatment.borderRole.toStashSurfaceThemeColor().copy(alpha = treatment.borderAlpha),
        ),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(policy.headerGapDp.dp),
            content = content,
        )
    }
}

@Composable
fun StashSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    contentDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val model = stashSheetHeaderModel(
        title = title,
        subtitle = subtitle,
        actionLabel = actionLabel,
        contentDescription = contentDescription,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = model.accessibilityLabel },
        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (model.subtitle != null) {
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (model.actionLabel != null && onActionClick != null) {
            StashGhostButton(
                text = model.actionLabel,
                onClick = onActionClick,
                contentDescription = model.actionLabel,
            )
        }
    }
}

@Composable
fun StashSheetOptionRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingLabel: String? = null,
    trailingLabel: String? = null,
    selected: Boolean = false,
    current: Boolean = false,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val model = stashSheetOptionRowModel(
        title = title,
        subtitle = subtitle,
        leadingLabel = leadingLabel,
        trailingLabel = trailingLabel,
        selected = selected,
        current = current,
        enabled = enabled,
        contentDescription = contentDescription,
    )
    val policy = stashSheetPolicy()
    val colors = stashSheetOptionColors(model.tone)
    val clickModifier = if (onClick != null && model.isInteractive) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = policy.optionMinHeightDp.dp)
                .stashSheetOptionSemantics(model, hasClickAction = onClick != null)
                .then(clickModifier),
            shape = RoundedCornerShape(policy.optionCornerRadiusDp.dp),
            color = colors.container,
            contentColor = colors.content,
            border = BorderStroke(1.dp, colors.border),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(policy.optionContentGapDp.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (model.current || model.selected) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(policy.currentAccentWidthDp.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(StashRadii.Pill))
                            .background(colors.accent),
                    )
                }
                if (leadingContent != null) {
                    leadingContent()
                } else if (model.leadingLabel != null) {
                    StashMetadataBadge(StashMetadataBadgeModel(label = model.leadingLabel))
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.content,
                        fontWeight = if (model.current || model.selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (model.subtitle != null) {
                        Text(
                            text = model.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryContent,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (trailingContent != null) {
                    trailingContent()
                } else if (model.trailingLabel != null) {
                    StashMetadataBadge(StashMetadataBadgeModel(label = model.trailingLabel))
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = policy.contentPaddingDp.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = policy.dividerAlpha),
            )
        }
    }
}

private data class StashSheetOptionColors(
    val container: Color,
    val content: Color,
    val secondaryContent: Color,
    val border: Color,
    val accent: Color,
)

@Composable
private fun stashSheetOptionColors(tone: StashSheetOptionTone): StashSheetOptionColors = when (tone) {
    StashSheetOptionTone.Normal -> StashSheetOptionColors(
        container = MaterialTheme.colorScheme.surface.copy(alpha = StashAlpha.Glass),
        content = MaterialTheme.colorScheme.onSurface,
        secondaryContent = MaterialTheme.colorScheme.onSurfaceVariant,
        border = MaterialTheme.colorScheme.outline.copy(alpha = stashSheetPolicy().borderAlpha),
        accent = MaterialTheme.colorScheme.primary,
    )

    StashSheetOptionTone.Selected -> StashSheetOptionColors(
        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = StashAlpha.SelectedContainer),
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        secondaryContent = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
        border = MaterialTheme.colorScheme.primary.copy(alpha = stashSheetPolicy().selectedBorderAlpha),
        accent = MaterialTheme.colorScheme.primary,
    )

    StashSheetOptionTone.Current -> StashSheetOptionColors(
        container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = StashAlpha.SelectedContainer),
        content = MaterialTheme.colorScheme.onSecondaryContainer,
        secondaryContent = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.76f),
        border = MaterialTheme.colorScheme.secondary.copy(alpha = stashSheetPolicy().selectedBorderAlpha),
        accent = MaterialTheme.colorScheme.secondary,
    )

    StashSheetOptionTone.Disabled -> StashSheetOptionColors(
        container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.Glass),
        content = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        secondaryContent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
        border = MaterialTheme.colorScheme.outline.copy(alpha = stashSheetPolicy().borderAlpha),
        accent = MaterialTheme.colorScheme.outline.copy(alpha = stashSheetPolicy().selectedBorderAlpha),
    )
}

private fun Modifier.stashSheetOptionSemantics(
    model: StashSheetOptionRowModel,
    hasClickAction: Boolean,
): Modifier = semantics(mergeDescendants = true) {
    contentDescription = model.accessibilityLabel
    if (hasClickAction) {
        role = Role.Button
    }
    if (hasClickAction && !model.enabled) {
        disabled()
    }
    if (model.stateDescription != null) {
        stateDescription = model.stateDescription
    }
}

private fun String?.normalizedOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }
