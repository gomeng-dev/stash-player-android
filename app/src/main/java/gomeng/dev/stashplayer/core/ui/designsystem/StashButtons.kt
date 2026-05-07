package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Visual policy for Stash CTA buttons and compact action pills.
 *
 * Exposed as a pure value so visual expectations can be covered by local JVM tests
 * without relying on Compose screenshot infrastructure.
 */
data class StashButtonPolicy(
    val cornerRadiusDp: Int,
    val primaryMinHeightDp: Int,
    val secondaryMinHeightDp: Int,
    val actionPillMinHeightDp: Int,
    val iconSpacingDp: Int,
)

data class StashDiscoveryVisualPolicy(
    val compactHorizontalPaddingDp: Int,
    val expandedHorizontalPaddingDp: Int,
    val gridItemGapDp: Int,
    val gridBottomPaddingDp: Int,
    val toolbarPillMinHeightDp: Int,
    val activeFilterChipsUseActionPills: Boolean,
)

fun stashButtonPolicy(): StashButtonPolicy = StashButtonPolicy(
    cornerRadiusDp = StashRadii.Pill.value.toInt(),
    primaryMinHeightDp = 44,
    secondaryMinHeightDp = 42,
    actionPillMinHeightDp = 38,
    iconSpacingDp = 6,
)

fun stashDiscoveryVisualPolicy(): StashDiscoveryVisualPolicy = StashDiscoveryVisualPolicy(
    compactHorizontalPaddingDp = StashSpacing.ScreenHorizontalCompact.value.toInt(),
    expandedHorizontalPaddingDp = StashSpacing.ScreenHorizontalExpanded.value.toInt(),
    gridItemGapDp = StashSpacing.CardGap.value.toInt(),
    gridBottomPaddingDp = StashSpacing.BottomContentPadding.value.toInt(),
    toolbarPillMinHeightDp = stashButtonPolicy().actionPillMinHeightDp,
    activeFilterChipsUseActionPills = true,
)

enum class StashActionPillTone {
    Neutral,
    Selected,
    Destructive,
    Disabled,
}

enum class StashActionPillColorRole {
    SurfaceContainer,
    SurfaceVariant,
    OnSurface,
    OnSurfaceVariant,
    Primary,
    PrimaryContainer,
    OnPrimaryContainer,
    Error,
    ErrorContainer,
    Outline,
}

data class StashActionPillVisualPolicy(
    val containerRole: StashActionPillColorRole,
    val contentRole: StashActionPillColorRole,
    val borderRole: StashActionPillColorRole,
    val containerAlpha: Float,
    val contentAlpha: Float,
    val borderAlpha: Float,
    val minTouchTargetDp: Int,
)

fun stashActionPillVisualPolicy(tone: StashActionPillTone): StashActionPillVisualPolicy = when (tone) {
    StashActionPillTone.Neutral -> StashActionPillVisualPolicy(
        containerRole = StashActionPillColorRole.SurfaceContainer,
        contentRole = StashActionPillColorRole.OnSurface,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = StashAlpha.SurfaceOverlay,
        contentAlpha = 0.92f,
        borderAlpha = 0.30f,
        minTouchTargetDp = StashTouch.MinTarget.value.toInt(),
    )

    StashActionPillTone.Selected -> StashActionPillVisualPolicy(
        containerRole = StashActionPillColorRole.PrimaryContainer,
        contentRole = StashActionPillColorRole.OnPrimaryContainer,
        borderRole = StashActionPillColorRole.Primary,
        containerAlpha = 1f,
        contentAlpha = 1f,
        borderAlpha = 0.90f,
        minTouchTargetDp = StashTouch.MinTarget.value.toInt(),
    )

    StashActionPillTone.Destructive -> StashActionPillVisualPolicy(
        containerRole = StashActionPillColorRole.ErrorContainer,
        contentRole = StashActionPillColorRole.Error,
        borderRole = StashActionPillColorRole.Error,
        containerAlpha = 0.46f,
        contentAlpha = 0.96f,
        borderAlpha = 0.70f,
        minTouchTargetDp = StashTouch.MinTarget.value.toInt(),
    )

    StashActionPillTone.Disabled -> StashActionPillVisualPolicy(
        containerRole = StashActionPillColorRole.SurfaceVariant,
        contentRole = StashActionPillColorRole.OnSurfaceVariant,
        borderRole = StashActionPillColorRole.Outline,
        containerAlpha = 0.58f,
        contentAlpha = 0.62f,
        borderAlpha = 0.22f,
        minTouchTargetDp = StashTouch.MinTarget.value.toInt(),
    )
}

data class StashActionPillModel(
    val label: String,
    val accessibilityLabel: String,
    val selected: Boolean,
    val enabled: Boolean,
    val destructive: Boolean,
    val tone: StashActionPillTone,
) {
    val isInteractive: Boolean = enabled
}

fun stashButtonAccessibilityLabel(
    label: String,
    contentDescription: String?,
): String = contentDescription?.trim().orEmpty().ifBlank { label }

fun stashActionPillModel(
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    destructive: Boolean = false,
    contentDescription: String? = null,
): StashActionPillModel {
    val tone = when {
        !enabled -> StashActionPillTone.Disabled
        selected -> StashActionPillTone.Selected
        destructive -> StashActionPillTone.Destructive
        else -> StashActionPillTone.Neutral
    }
    return StashActionPillModel(
        label = label,
        accessibilityLabel = stashButtonAccessibilityLabel(label, contentDescription),
        selected = selected,
        enabled = enabled,
        destructive = destructive,
        tone = tone,
    )
}

@Composable
fun StashPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    contentDescription: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val policy = stashButtonPolicy()
    val shape = RoundedCornerShape(policy.cornerRadiusDp.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = policy.primaryMinHeightDp.dp)
            .stashButtonSemantics(text, contentDescription, selected, enabled),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.GlassStrong),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    ) {
        StashButtonContent(text = text, icon = icon, bold = true)
    }
}

@Composable
fun StashSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    destructive: Boolean = false,
    contentDescription: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val policy = stashButtonPolicy()
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = StashAlpha.BorderSubtle)
        destructive -> MaterialTheme.colorScheme.error.copy(alpha = StashAlpha.BorderRaised)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = StashAlpha.BorderRaised)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = StashAlpha.BorderStrong)
    }
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = StashAlpha.SelectedContainer)
        destructive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = StashAlpha.GlassStrong)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = StashAlpha.GlassStrong)
    }
    val contentColor = when {
        destructive -> MaterialTheme.colorScheme.error
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = policy.secondaryMinHeightDp.dp)
            .stashButtonSemantics(text, contentDescription, selected, enabled),
        enabled = enabled,
        shape = RoundedCornerShape(policy.cornerRadiusDp.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = StashAlpha.Glass),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
    ) {
        StashButtonContent(text = text, icon = icon, bold = selected)
    }
}

@Composable
fun StashGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    destructive: Boolean = false,
    contentDescription: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val policy = stashButtonPolicy()
    val contentColor = when {
        destructive -> MaterialTheme.colorScheme.error
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = policy.secondaryMinHeightDp.dp)
            .stashButtonSemantics(text, contentDescription, selected, enabled),
        enabled = enabled,
        shape = RoundedCornerShape(policy.cornerRadiusDp.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    ) {
        StashButtonContent(text = text, icon = icon, bold = selected)
    }
}

@Composable
fun StashDangerGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    contentDescription: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    StashGhostButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        destructive = true,
        contentDescription = contentDescription,
        icon = icon,
    )
}

@Composable
fun StashActionPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    destructive: Boolean = false,
    contentDescription: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val model = stashActionPillModel(
        label = label,
        selected = selected,
        enabled = enabled,
        destructive = destructive,
        contentDescription = contentDescription,
    )
    val policy = stashButtonPolicy()
    val colors = stashActionPillColors(model.tone)
    val shape = RoundedCornerShape(policy.cornerRadiusDp.dp)

    Surface(
        modifier = modifier
            .heightIn(min = policy.actionPillMinHeightDp.dp)
            .stashButtonSemantics(model.label, model.accessibilityLabel, model.selected, model.enabled)
            .clickable(
                enabled = model.isInteractive,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = shape,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(policy.iconSpacingDp.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = model.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (model.selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class StashActionPillColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun stashActionPillColors(tone: StashActionPillTone): StashActionPillColors =
    stashActionPillVisualPolicy(tone).toActionPillColors()

@Composable
private fun StashActionPillVisualPolicy.toActionPillColors(): StashActionPillColors = StashActionPillColors(
    container = containerRole.toThemeColor().copy(alpha = containerAlpha),
    content = contentRole.toThemeColor().copy(alpha = contentAlpha),
    border = borderRole.toThemeColor().copy(alpha = borderAlpha),
)

@Composable
private fun StashActionPillColorRole.toThemeColor(): Color = when (this) {
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

@Composable
private fun StashButtonContent(
    text: String,
    icon: (@Composable () -> Unit)?,
    bold: Boolean,
) {
    val policy = stashButtonPolicy()
    Row(
        horizontalArrangement = Arrangement.spacedBy(policy.iconSpacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            icon()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Modifier.stashButtonSemantics(
    label: String,
    contentDescription: String?,
    selected: Boolean,
    enabled: Boolean,
): Modifier = semantics(mergeDescendants = true) {
    this.contentDescription = stashButtonAccessibilityLabel(label, contentDescription)
    role = Role.Button
    if (selected) {
        stateDescription = stashString(R.string.auto_kr_0298)
    }
    if (!enabled) {
        disabled()
    }
}
