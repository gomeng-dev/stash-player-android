package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StashEmptyState(
    state: StashEmptyStateModel,
    modifier: Modifier = Modifier,
    onPrimaryAction: (() -> Unit)? = null,
) {
    val primaryAction = if (state.hasPrimaryAction && onPrimaryAction != null) {
        StashStateAction(label = state.primaryActionLabel.orEmpty(), onClick = onPrimaryAction)
    } else {
        null
    }
    val panel = stashStatePanelModel(
        title = state.title,
        message = state.message,
        actionLabels = stashRenderedStateActionLabels(
            primaryActionLabel = state.primaryActionLabel,
            hasPrimaryAction = primaryAction != null,
            secondaryActionLabel = null,
            hasSecondaryAction = false,
        ),
        tone = StashStatePanelTone.Empty,
    )
    StashStatePanel(
        model = panel,
        modifier = modifier,
        primaryAction = primaryAction,
    )
}

@Composable
fun StashErrorState(
    state: StashErrorStateModel,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val primaryAction = if (state.hasPrimaryAction && onRetry != null) {
        StashStateAction(label = state.primaryActionLabel, onClick = onRetry)
    } else {
        null
    }
    val secondaryAction = if (state.hasSecondaryAction && onSecondaryAction != null) {
        StashStateAction(label = state.secondaryActionLabel.orEmpty(), onClick = onSecondaryAction)
    } else {
        null
    }
    val panel = stashStatePanelModel(
        title = state.title,
        message = state.message,
        actionLabels = stashRenderedStateActionLabels(
            primaryActionLabel = state.primaryActionLabel,
            hasPrimaryAction = primaryAction != null,
            secondaryActionLabel = state.secondaryActionLabel,
            hasSecondaryAction = secondaryAction != null,
        ),
        tone = StashStatePanelTone.Error,
    )
    StashStatePanel(
        model = panel,
        modifier = modifier,
        primaryAction = primaryAction,
        secondaryAction = secondaryAction,
    )
}

@Composable
fun StashSectionHeader(
    state: StashSectionHeaderModel,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
) {
    val presentation = stashSectionHeaderPresentation(state)
    StashSectionHeaderV2(
        title = presentation.title,
        modifier = modifier,
        subtitle = presentation.subtitle,
        countLabel = presentation.countBadgeLabel,
        actionLabel = presentation.actionLabel,
        contentDescription = presentation.accessibilityLabel,
        onActionClick = onActionClick,
    )
}

@Composable
fun StashScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

private data class StashStateAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun StashStatePanel(
    model: StashStatePanelModel,
    modifier: Modifier,
    primaryAction: StashStateAction? = null,
    secondaryAction: StashStateAction? = null,
) {
    val policy = stashStateComponentPolicy()
    StashGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = model.accessibilityLabel },
        cornerRadius = policy.panelCornerRadiusDp.dp,
        contentPadding = PaddingValues(policy.panelPaddingDp.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.titleLarge,
                color = model.titleColor(),
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = model.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (primaryAction != null || secondaryAction != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    if (primaryAction != null) {
                        when (model.tone) {
                            StashStatePanelTone.Empty -> StashPrimaryButton(
                                text = primaryAction.label,
                                onClick = primaryAction.onClick,
                            )
                            StashStatePanelTone.Error -> StashSecondaryButton(
                                text = primaryAction.label,
                                onClick = primaryAction.onClick,
                                destructive = true,
                            )
                        }
                    }
                    if (secondaryAction != null) {
                        StashSecondaryButton(
                            text = secondaryAction.label,
                            onClick = secondaryAction.onClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StashStatePanelModel.titleColor(): Color = when (tone) {
    StashStatePanelTone.Empty -> MaterialTheme.colorScheme.onSurface
    StashStatePanelTone.Error -> MaterialTheme.colorScheme.error
}
