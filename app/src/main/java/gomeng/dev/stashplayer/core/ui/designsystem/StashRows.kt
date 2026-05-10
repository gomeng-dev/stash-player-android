package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

/**
 * Visual policy for playlist-style media rows and section headers.
 *
 * These values intentionally mirror the Stash media/card tokens so Queue,
 * Watch Later, Favorites, and future player sheets can share one compact row
 * language without re-copying raw Material sizes in feature code.
 */
data class StashRowPolicy(
    val rowCornerRadiusDp: Int,
    val thumbnailCornerRadiusDp: Int,
    val thumbnailSizeDp: Int,
    val minRowHeightDp: Int,
    val rowPaddingDp: Int,
    val contentGapDp: Int,
    val trailingActionGapDp: Int,
    val accentWidthDp: Int,
)

fun stashRowPolicy(): StashRowPolicy = StashRowPolicy(
    rowCornerRadiusDp = StashRadii.Card.value.toInt(),
    thumbnailCornerRadiusDp = StashRadii.Thumbnail.value.toInt(),
    thumbnailSizeDp = 72,
    minRowHeightDp = 88,
    rowPaddingDp = 12,
    contentGapDp = StashSpacing.CardGap.value.toInt(),
    trailingActionGapDp = StashSpacing.ChipGap.value.toInt(),
    accentWidthDp = 4,
)

enum class StashListMediaRowTone {
    Normal,
    Selected,
    Current,
    Disabled,
}

data class StashListMediaRowModel(
    val title: String,
    val subtitle: String?,
    val progress: Float?,
    val currentLabel: String?,
    val selected: Boolean,
    val current: Boolean,
    val enabled: Boolean,
    val tone: StashListMediaRowTone,
    val accessibilityLabel: String,
) {
    val isInteractive: Boolean = enabled
}

data class StashListMediaRowSemanticsModel(
    val accessibilityLabel: String,
    val hasButtonRole: Boolean,
    val disabled: Boolean,
    val stateDescription: String?,
)

fun stashListMediaRowSemanticsModel(
    model: StashListMediaRowModel,
    hasClickAction: Boolean,
): StashListMediaRowSemanticsModel = StashListMediaRowSemanticsModel(
    accessibilityLabel = model.accessibilityLabel,
    hasButtonRole = hasClickAction,
    disabled = hasClickAction && !model.enabled,
    stateDescription = when (model.tone) {
        StashListMediaRowTone.Current -> stashString(R.string.auto_kr_0390)
        StashListMediaRowTone.Selected -> stashString(R.string.auto_kr_0298)
        StashListMediaRowTone.Normal,
        StashListMediaRowTone.Disabled,
        -> null
    },
)

fun stashListMediaRowModel(
    title: String,
    subtitle: String? = null,
    progress: Float? = null,
    currentLabel: String? = null,
    selected: Boolean = false,
    current: Boolean = false,
    enabled: Boolean = true,
    contentDescription: String? = null,
): StashListMediaRowModel {
    val normalizedTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0391) }
    val normalizedSubtitle = subtitle.normalizedOrNull()
    val normalizedCurrentLabel = currentLabel.normalizedOrNull()
    val clampedProgress = progress?.coerceIn(0f, 1f)
    val tone = when {
        !enabled -> StashListMediaRowTone.Disabled
        current -> StashListMediaRowTone.Current
        selected -> StashListMediaRowTone.Selected
        else -> StashListMediaRowTone.Normal
    }
    val fallbackAccessibility = listOf(normalizedTitle, normalizedSubtitle, normalizedCurrentLabel)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
    return StashListMediaRowModel(
        title = normalizedTitle,
        subtitle = normalizedSubtitle,
        progress = clampedProgress,
        currentLabel = normalizedCurrentLabel,
        selected = selected,
        current = current,
        enabled = enabled,
        tone = tone,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
    )
}

data class StashCompactActionModel(
    val id: String,
    val label: String,
    val accessibilityLabel: String,
    val selected: Boolean,
    val enabled: Boolean,
    val destructive: Boolean,
    val tone: StashActionPillTone,
) {
    val isInteractive: Boolean = enabled
}

fun stashCompactActionModel(
    id: String,
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    destructive: Boolean = false,
    contentDescription: String? = null,
): StashCompactActionModel {
    val pill = stashActionPillModel(
        label = label.trim().ifBlank { id },
        selected = selected,
        enabled = enabled,
        destructive = destructive,
        contentDescription = contentDescription,
    )
    return StashCompactActionModel(
        id = id,
        label = pill.label,
        accessibilityLabel = pill.accessibilityLabel,
        selected = pill.selected,
        enabled = pill.enabled,
        destructive = pill.destructive,
        tone = pill.tone,
    )
}

data class StashSectionHeaderV2Model(
    val title: String,
    val subtitle: String?,
    val countLabel: String?,
    val actionLabel: String?,
    val accessibilityLabel: String,
)

fun stashSectionHeaderV2Model(
    title: String,
    subtitle: String? = null,
    countLabel: String? = null,
    actionLabel: String? = null,
    contentDescription: String? = null,
): StashSectionHeaderV2Model {
    val normalizedTitle = title.trim().ifBlank { stashString(R.string.auto_kr_0392) }
    val normalizedSubtitle = null
    val normalizedCount = countLabel.normalizedOrNull()
    val normalizedAction = actionLabel.normalizedOrNull()
    val fallbackAccessibility = listOf(normalizedTitle, normalizedSubtitle, normalizedCount)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
    return StashSectionHeaderV2Model(
        title = normalizedTitle,
        subtitle = normalizedSubtitle,
        countLabel = normalizedCount,
        actionLabel = normalizedAction,
        accessibilityLabel = contentDescription?.trim().orEmpty().ifBlank { fallbackAccessibility },
    )
}

@Composable
fun StashListMediaRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    thumbnailModel: Any? = null,
    thumbnailContentDescription: String? = title,
    progress: Float? = null,
    currentLabel: String? = null,
    selected: Boolean = false,
    current: Boolean = false,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
    trailingActions: @Composable RowScope.() -> Unit = {},
) {
    val model = stashListMediaRowModel(
        title = title,
        subtitle = subtitle,
        progress = progress,
        currentLabel = currentLabel,
        selected = selected,
        current = current,
        enabled = enabled,
        contentDescription = contentDescription,
    )
    val policy = stashRowPolicy()
    val colors = stashListMediaRowColors(model.tone)
    val clickModifier = if (onClick != null && model.isInteractive) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = policy.minRowHeightDp.dp)
            .stashListMediaRowSemantics(model, hasClickAction = onClick != null)
            .then(clickModifier),
        shape = RoundedCornerShape(policy.rowCornerRadiusDp.dp),
        color = colors.container,
        contentColor = colors.content,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(policy.rowPaddingDp.dp),
            horizontalArrangement = Arrangement.spacedBy(policy.contentGapDp.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.current || model.selected) {
                Box(
                    modifier = Modifier
                        .width(policy.accentWidthDp.dp)
                        .height(policy.thumbnailSizeDp.dp)
                        .clip(RoundedCornerShape(StashRadii.Pill))
                        .background(colors.accent),
                )
            }
            StashRowThumbnail(
                thumbnailModel = thumbnailModel,
                thumbnailContentDescription = thumbnailContentDescription,
                currentLabel = model.currentLabel,
                policy = policy,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.content,
                    fontWeight = if (model.current || model.selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 2,
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
                if (model.progress != null) {
                    LinearProgressIndicator(
                        progress = { model.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(StashRadii.Pill)),
                        color = colors.accent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.GlassStrong),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(policy.trailingActionGapDp.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = trailingActions,
            )
        }
    }
}

@Composable
fun StashCompactActionRow(
    actions: List<StashCompactActionModel>,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(stashRowPolicy().trailingActionGapDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            StashActionPill(
                label = action.label,
                onClick = { onActionClick(action.id) },
                selected = action.selected,
                enabled = action.enabled,
                destructive = action.destructive,
                contentDescription = action.accessibilityLabel,
            )
        }
    }
}

@Composable
fun StashSectionHeaderV2(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    countLabel: String? = null,
    actionLabel: String? = null,
    contentDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val model = stashSectionHeaderV2Model(
        title = title,
        subtitle = subtitle,
        countLabel = countLabel,
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
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (model.countLabel != null) {
                    StashMetadataBadge(StashMetadataBadgeModel(label = model.countLabel))
                }
            }
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
            StashActionPill(
                label = model.actionLabel,
                onClick = onActionClick,
                contentDescription = model.actionLabel,
            )
        }
    }
}

@Composable
private fun StashRowThumbnail(
    thumbnailModel: Any?,
    thumbnailContentDescription: String?,
    currentLabel: String?,
    policy: StashRowPolicy,
) {
    Box(
        modifier = Modifier
            .size(policy.thumbnailSizeDp.dp)
            .clip(RoundedCornerShape(policy.thumbnailCornerRadiusDp.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.GlassStrong)),
    ) {
        if (thumbnailModel != null) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = thumbnailContentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            StashGradientScrim(
                modifier = Modifier.fillMaxSize(),
                topColor = Color.Transparent,
                bottomColor = Color.Black.copy(alpha = StashAlpha.ScrimBottom),
            )
        }
        if (currentLabel != null) {
            StashMetadataBadge(
                badge = StashMetadataBadgeModel(label = currentLabel),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp),
            )
        }
    }
}

private data class StashListMediaRowColors(
    val container: Color,
    val content: Color,
    val secondaryContent: Color,
    val border: Color,
    val accent: Color,
)

@Composable
private fun stashListMediaRowColors(tone: StashListMediaRowTone): StashListMediaRowColors = when (tone) {
    StashListMediaRowTone.Normal -> StashListMediaRowColors(
        container = MaterialTheme.colorScheme.surface.copy(alpha = StashAlpha.GlassStrong),
        content = MaterialTheme.colorScheme.onSurface,
        secondaryContent = MaterialTheme.colorScheme.onSurfaceVariant,
        border = MaterialTheme.colorScheme.outline.copy(alpha = StashAlpha.BorderSubtle),
        accent = MaterialTheme.colorScheme.primary,
    )

    StashListMediaRowTone.Selected -> StashListMediaRowColors(
        container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = StashAlpha.SelectedContainer),
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        secondaryContent = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f),
        border = MaterialTheme.colorScheme.primary.copy(alpha = StashAlpha.BorderStrong),
        accent = MaterialTheme.colorScheme.primary,
    )

    StashListMediaRowTone.Current -> StashListMediaRowColors(
        container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = StashAlpha.SelectedContainer),
        content = MaterialTheme.colorScheme.onSecondaryContainer,
        secondaryContent = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.76f),
        border = MaterialTheme.colorScheme.secondary.copy(alpha = StashAlpha.BorderStrong),
        accent = MaterialTheme.colorScheme.secondary,
    )

    StashListMediaRowTone.Disabled -> StashListMediaRowColors(
        container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.Glass),
        content = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        secondaryContent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
        border = MaterialTheme.colorScheme.outline.copy(alpha = StashAlpha.BorderSubtle),
        accent = MaterialTheme.colorScheme.outline.copy(alpha = StashAlpha.BorderStrong),
    )
}

private fun Modifier.stashListMediaRowSemantics(
    model: StashListMediaRowModel,
    hasClickAction: Boolean,
): Modifier = semantics(mergeDescendants = true) {
    val semanticsModel = stashListMediaRowSemanticsModel(model, hasClickAction)
    contentDescription = semanticsModel.accessibilityLabel
    if (semanticsModel.hasButtonRole) {
        role = Role.Button
    }
    if (semanticsModel.disabled) {
        disabled()
    }
    if (semanticsModel.stateDescription != null) {
        stateDescription = semanticsModel.stateDescription
    }
}

private fun String?.normalizedOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }
