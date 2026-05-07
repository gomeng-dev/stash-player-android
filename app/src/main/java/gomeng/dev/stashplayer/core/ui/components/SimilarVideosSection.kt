package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.SimilarSceneCardUiModel
import gomeng.dev.stashplayer.core.model.SimilarSceneActionUiModel
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosLayout
import gomeng.dev.stashplayer.core.model.SimilarVideosLayoutContext
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.model.SimilarVideosSectionUiState
import gomeng.dev.stashplayer.core.model.buildSimilarScenePrimaryClickAction
import gomeng.dev.stashplayer.core.model.buildSimilarVideosSectionUiState
import gomeng.dev.stashplayer.core.model.shouldUseScrollableSimilarVideosList
import gomeng.dev.stashplayer.core.model.similarVideosCompactRecommendationCardVisualPolicy
import gomeng.dev.stashplayer.core.model.similarVideosCompactVerticalMaxHeightDp
import gomeng.dev.stashplayer.core.ui.designsystem.StashGradientScrim
import gomeng.dev.stashplayer.core.ui.designsystem.StashIconActionButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashIconActionSemantics
import gomeng.dev.stashplayer.core.ui.designsystem.StashMediaCard
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadge
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadgeModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChip
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChipModel
import coil.compose.AsyncImage
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun SimilarVideosSection(
    currentSceneId: String,
    recommendations: List<SimilarSceneRecommendation>,
    isLoading: Boolean,
    errorMessage: String?,
    onPlayScene: (String) -> Unit,
    onAddToQueue: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    recommendationSource: SimilarVideosRecommendationSource = SimilarVideosRecommendationSource.HybridBackend,
    layoutContext: SimilarVideosLayoutContext = SimilarVideosLayoutContext.General,
    queuedSceneIds: Set<String> = emptySet(),
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val state = buildSimilarVideosSectionUiState(
            currentSceneId = currentSceneId,
            recommendations = recommendations,
            isLoading = isLoading,
            errorMessage = errorMessage,
            availableWidthDp = maxWidth.value.toInt(),
            recommendationSource = recommendationSource,
            layoutContext = layoutContext,
            queuedSceneIds = queuedSceneIds,
        )
        SimilarVideosSection(
            state = state,
            onPlayScene = onPlayScene,
            onAddToQueue = onAddToQueue,
            onRetry = onRetry,
        )
    }
}

@Composable
fun SimilarVideosSection(
    state: SimilarVideosSectionUiState,
    onPlayScene: (String) -> Unit,
    onAddToQueue: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        SimilarVideosSectionUiState.Loading -> SimilarVideosLoading(modifier)
        is SimilarVideosSectionUiState.Empty -> SimilarVideosEmpty(
            state = state,
            onRetry = onRetry,
            modifier = modifier,
        )
        is SimilarVideosSectionUiState.Error -> SimilarVideosError(
            state = state,
            onRetry = onRetry,
            modifier = modifier,
        )
        is SimilarVideosSectionUiState.Success -> SimilarVideosSuccess(
            state = state,
            onPlayScene = onPlayScene,
            onAddToQueue = onAddToQueue,
            modifier = modifier,
        )
    }
}

@Composable
private fun SimilarVideosLoading(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.width(24.dp))
        Text(
            text = stashString(R.string.auto_kr_0299),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SimilarVideosMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        modifier = modifier.padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SimilarVideosEmpty(
    state: SimilarVideosSectionUiState.Empty,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.semantics {
                contentDescription = state.retryContentDescription
            },
        ) {
            Text(state.retryLabel)
        }
    }
}

@Composable
private fun SimilarVideosError(
    state: SimilarVideosSectionUiState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.semantics {
                contentDescription = state.retryContentDescription
            },
        ) {
            Text(state.retryLabel)
        }
    }
}

@Composable
private fun SimilarVideosSuccess(
    state: SimilarVideosSectionUiState.Success,
    onPlayScene: (String) -> Unit,
    onAddToQueue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardPolicy = similarVideosCompactRecommendationCardVisualPolicy()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (state.subtitle.isNotBlank()) {
                Text(
                    text = state.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (state.layout == SimilarVideosLayout.HorizontalRail) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(cardPolicy.horizontalSpacingDp.dp)) {
                items(state.items, key = { it.sceneId }) { item ->
                    SimilarSceneCard(
                        item = item,
                        onPlayScene = onPlayScene,
                        onAddToQueue = onAddToQueue,
                        modifier = Modifier.width(cardPolicy.horizontalRailCardWidthDp.dp),
                    )
                }
            }
        } else {
            val compactListModifier = Modifier.heightIn(
                max = similarVideosCompactVerticalMaxHeightDp(state.items.size).dp,
            )
            LazyColumn(
                modifier = compactListModifier,
                verticalArrangement = Arrangement.spacedBy(cardPolicy.verticalItemSpacingDp.dp),
                userScrollEnabled = shouldUseScrollableSimilarVideosList(state.layout, state.items.size),
            ) {
                items(state.items, key = { it.sceneId }) { item ->
                    SimilarSceneCard(
                        item = item,
                        onPlayScene = onPlayScene,
                        onAddToQueue = onAddToQueue,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun SimilarSceneCard(
    item: SimilarSceneCardUiModel,
    onPlayScene: (String) -> Unit,
    onAddToQueue: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryClickAction = buildSimilarScenePrimaryClickAction(item)
    val cardPolicy = similarVideosCompactRecommendationCardVisualPolicy()
    StashMediaCard(
        modifier = modifier.clickable(
            onClickLabel = primaryClickAction.contentDescription,
            onClick = { onPlayScene(primaryClickAction.sceneId) },
        ),
    ) {
        Row(
            modifier = Modifier.padding(cardPolicy.cardPaddingDp.dp),
            horizontalArrangement = Arrangement.spacedBy(cardPolicy.horizontalSpacingDp.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(cardPolicy.thumbnailWidthDp.dp)
                    .aspectRatio(cardPolicy.thumbnailAspectRatio)
                    .clip(RoundedCornerShape(cardPolicy.thumbnailRadiusDp.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                item.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                StashGradientScrim(modifier = Modifier.fillMaxSize())
                StashMetadataBadge(
                    badge = StashMetadataBadgeModel(
                        label = item.scoreLabel,
                        contentDescription = stashString(R.string.auto_kr_0300, item.scoreLabel),
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = cardPolicy.titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(cardPolicy.metadataBadgeSpacingDp.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StashMetadataBadge(StashMetadataBadgeModel(label = item.provenanceLabel))
                    item.metadataBadges.forEach { badge ->
                        StashMetadataBadge(StashMetadataBadgeModel(label = badge))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item.reasonChips.forEach { reason ->
                        StashTagChip(tag = StashTagChipModel(label = reason))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(cardPolicy.actionButtonSpacingDp.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimilarSceneActionButton(
                        icon = Icons.Outlined.PlayArrow,
                        action = item.playAction,
                        onClick = { if (item.playAction.enabled) onPlayScene(item.sceneId) },
                    )
                    SimilarSceneActionButton(
                        icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                        action = item.queueAction,
                        onClick = { if (item.queueAction.enabled) onAddToQueue(item.sceneId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarSceneActionButton(
    icon: ImageVector,
    action: SimilarSceneActionUiModel,
    onClick: () -> Unit,
) {
    val cardPolicy = similarVideosCompactRecommendationCardVisualPolicy()
    StashIconActionButton(
        semantics = StashIconActionSemantics(
            label = action.contentDescription,
            selected = action.selected,
        ),
        onClick = onClick,
        modifier = Modifier.size(cardPolicy.actionButtonSizeDp.dp),
        selected = action.selected,
        containerAlpha = if (action.enabled) 0.72f else 0.30f,
        contentPadding = PaddingValues(
            horizontal = cardPolicy.actionButtonHorizontalPaddingDp.dp,
            vertical = 0.dp,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(cardPolicy.actionIconSizeDp.dp),
        )
    }
}
