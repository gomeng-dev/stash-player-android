package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StashGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = stashMediaPrimitivePolicy().cardCornerRadiusDp.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val treatment = stashSurfaceTreatment(StashSurfaceRole.ElevatedCard)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = treatment.containerRole.toStashSurfaceThemeColor().copy(alpha = treatment.containerAlpha),
        contentColor = treatment.contentRole.toStashSurfaceThemeColor(),
        tonalElevation = treatment.tonalElevationDp.dp,
        border = BorderStroke(
            width = 1.dp,
            color = treatment.borderRole.toStashSurfaceThemeColor().copy(alpha = treatment.borderAlpha),
        ),
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun StashGradientScrim(
    modifier: Modifier = Modifier,
    topColor: Color = Color.Transparent,
    bottomColor: Color = Color.Black.copy(alpha = StashAlpha.ScrimBottom),
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(topColor, bottomColor),
            ),
        ),
    )
}

@Composable
fun StashIconActionButton(
    semantics: StashIconActionSemantics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = semantics.selected,
    containerAlpha: Float = StashAlpha.IconAction,
    selectedContainerAlpha: Float = StashAlpha.IconActionSelected,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    icon: @Composable () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = selectedContainerAlpha)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = containerAlpha)
    }
    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(
                minWidth = stashMediaPrimitivePolicy().iconActionMinTouchTargetDp.dp,
                minHeight = stashMediaPrimitivePolicy().iconActionMinTouchTargetDp.dp,
            )
            .background(containerColor, RoundedCornerShape(StashRadii.Pill))
            .semantics { contentDescription = semantics.contentDescription },
        contentPadding = contentPadding,
    ) {
        icon()
    }
}

@Composable
fun StashMetadataBadge(
    badge: StashMetadataBadgeModel,
    modifier: Modifier = Modifier,
) {
    if (!badge.isVisible) return
    val visibility = stashCompactChipVisibilityPolicy(StashCompactChipKind.MetadataBadge)
    Surface(
        modifier = modifier.semantics { contentDescription = badge.accessibilityLabel },
        shape = RoundedCornerShape(StashRadii.Pill),
        color = visibility.containerRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.containerAlpha),
        contentColor = visibility.contentRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.contentAlpha),
        border = BorderStroke(1.dp, visibility.borderRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.borderAlpha)),
    ) {
        Text(
            text = badge.displayLabel,
            modifier = Modifier.padding(horizontal = StashSpacing.ChipGap, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = stashMediaPrimitivePolicy().metadataBadgeMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StashTagChip(
    tag: StashTagChipModel,
    modifier: Modifier = Modifier,
) {
    if (!tag.isVisible) return
    val visibility = stashCompactChipVisibilityPolicy(StashCompactChipKind.Tag)
    Surface(
        modifier = modifier.semantics { contentDescription = tag.accessibilityLabel },
        shape = RoundedCornerShape(StashRadii.Pill),
        color = visibility.containerRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.containerAlpha),
        contentColor = visibility.contentRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.contentAlpha),
        border = BorderStroke(1.dp, visibility.borderRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.borderAlpha)),
    ) {
        Text(
            text = tag.displayLabel,
            modifier = Modifier.padding(horizontal = StashSpacing.ChipGap, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = stashMediaPrimitivePolicy().tagChipMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun StashVisibilityFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val tone = when {
        !enabled -> StashFilterChipTone.Disabled
        selected -> StashFilterChipTone.Selected
        else -> StashFilterChipTone.Unselected
    }
    val visibility = stashFilterChipVisibilityPolicy(tone)
    val containerColor = visibility.containerRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.containerAlpha)
    val contentColor = visibility.contentRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.contentAlpha)
    val borderColor = visibility.borderRole.toMediaPrimitiveThemeColor().copy(alpha = visibility.borderAlpha)

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            disabledContainerColor = containerColor,
            disabledLabelColor = contentColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = borderColor,
            selectedBorderColor = borderColor,
            disabledBorderColor = borderColor,
            disabledSelectedBorderColor = borderColor,
        ),
    )
}

@Composable
fun StashMediaCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    cornerRadius: Dp = stashMediaPrimitivePolicy().cardCornerRadiusDp.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val treatment = stashSurfaceTreatment(StashSurfaceRole.BaseCard)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                treatment.borderRole.toStashSurfaceThemeColor().copy(alpha = treatment.borderAlpha)
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = StashAlpha.SelectedContainer)
            } else {
                treatment.containerRole.toStashSurfaceThemeColor().copy(alpha = treatment.containerAlpha)
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                treatment.contentRole.toStashSurfaceThemeColor()
            },
        ),
    ) {
        Box(content = content)
    }
}

fun Modifier.stashThumbnailClip(): Modifier = clip(
    RoundedCornerShape(
        topStart = stashMediaPrimitivePolicy().thumbnailCornerRadiusDp.dp,
        topEnd = stashMediaPrimitivePolicy().thumbnailCornerRadiusDp.dp,
    ),
)

@Composable
private fun StashActionPillColorRole.toMediaPrimitiveThemeColor(): Color = when (this) {
    StashActionPillColorRole.SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
    StashActionPillColorRole.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
    StashActionPillColorRole.OnSurface -> MaterialTheme.colorScheme.onSurface
    StashActionPillColorRole.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    StashActionPillColorRole.Primary -> MaterialTheme.colorScheme.primary
    StashActionPillColorRole.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    StashActionPillColorRole.OnPrimaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
    StashActionPillColorRole.Error -> MaterialTheme.colorScheme.error
    StashActionPillColorRole.ErrorContainer -> MaterialTheme.colorScheme.errorContainer
    StashActionPillColorRole.Outline -> MaterialTheme.colorScheme.outline
}
