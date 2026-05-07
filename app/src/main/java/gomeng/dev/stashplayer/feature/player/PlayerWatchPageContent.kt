package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosLayoutContext
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.PlayerDebugInfoUiState
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashAction
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashActionRowItem
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashActionVisualState
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragRelease
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragSession
import gomeng.dev.stashplayer.core.player.PlayerPresentationDragUpdate
import gomeng.dev.stashplayer.core.player.PlayerPresentationGestureMode
import gomeng.dev.stashplayer.core.player.PlayerPresentationGestureStartArea
import gomeng.dev.stashplayer.core.player.PlayerPresentationMode
import gomeng.dev.stashplayer.core.player.SceneWatchPageContentState
import gomeng.dev.stashplayer.core.player.SceneWatchPageSection
import gomeng.dev.stashplayer.core.player.buildPlayerPresentationDragUpdate
import gomeng.dev.stashplayer.core.player.dragBy
import gomeng.dev.stashplayer.core.player.playerLockedTouchHint
import gomeng.dev.stashplayer.core.player.release
import gomeng.dev.stashplayer.core.player.startPlayerPresentationDragSession
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha
import gomeng.dev.stashplayer.core.ui.components.SimilarVideosSection
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadge
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadgeModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.StashSurfaceRole
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChip
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChipModel
import gomeng.dev.stashplayer.core.ui.designsystem.stashSurfaceTreatment
import gomeng.dev.stashplayer.core.ui.designsystem.toStashSurfaceThemeColor
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerWatchPageContent(
    state: SceneWatchPageContentState,
    actionItems: List<PlayerExpandedStashActionRowItem>,
    debugEntry: PlayerExpandedStashActionRowItem,
    debugInfoUiState: PlayerDebugInfoUiState,
    ratingStep: Int,
    ratingMessage: String?,
    ratingUpdating: Boolean,
    sceneId: String,
    similarRecommendations: List<SimilarSceneRecommendation>,
    similarRecommendationsLoading: Boolean,
    similarRecommendationsError: String?,
    similarRecommendationsSource: SimilarVideosRecommendationSource,
    queuedSceneIds: Set<String>,
    onSelectRatingStep: (Int) -> Unit,
    onAddCurrentSceneToQueue: () -> Unit,
    onIncrementOCounter: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onPlaySimilarScene: (String) -> Unit,
    onAddSimilarSceneToQueue: (String) -> Unit,
    onRetrySimilarRecommendations: () -> Unit,
    onPresentationDragUpdate: (PlayerPresentationDragUpdate?) -> Unit,
    onPresentationDragRelease: (PlayerPresentationDragRelease) -> Unit,
    onGestureHudText: (String?) -> Unit,
    presentationGestureLocked: Boolean,
    modifier: Modifier = Modifier,
) {
    var detailsDialogOpen by remember { mutableStateOf(false) }
    var ratingControlsInteracting by remember { mutableStateOf(false) }
    var contentContainerHeightPx by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberScrollState()
    val latestContentScrolledToTop by rememberUpdatedState(scrollState.value == 0)
    val latestRatingControlsInteracting by rememberUpdatedState(ratingControlsInteracting)
    val latestContentContainerHeightPx by rememberUpdatedState(contentContainerHeightPx)
    val latestOnPresentationDragUpdate by rememberUpdatedState(onPresentationDragUpdate)
    val latestOnPresentationDragRelease by rememberUpdatedState(onPresentationDragRelease)
    val latestOnGestureHudText by rememberUpdatedState(onGestureHudText)
    val latestPresentationGestureLocked by rememberUpdatedState(presentationGestureLocked)
    val surfaceTreatment = stashSurfaceTreatment(StashSurfaceRole.ElevatedCard)
    Surface(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .onSizeChanged { size -> contentContainerHeightPx = size.height.toFloat() }
            .navigationBarsPadding(),
        color = surfaceTreatment.containerRole.toStashSurfaceThemeColor().copy(alpha = surfaceTreatment.containerAlpha),
        contentColor = surfaceTreatment.contentRole.toStashSurfaceThemeColor(),
        tonalElevation = surfaceTreatment.tonalElevationDp.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    state = scrollState,
                    enabled = !ratingControlsInteracting,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.sectionOrder.forEach { section ->
                when (section) {
                    SceneWatchPageSection.SceneHeader -> PlayerWatchPageHeader(
                        title = state.title,
                        modifier = Modifier.watchPageContentPullToFullscreenGesture(
                            contentScrolledToTop = { latestContentScrolledToTop },
                            locked = { latestPresentationGestureLocked },
                            blocked = { latestRatingControlsInteracting },
                            containerHeightPx = { latestContentContainerHeightPx },
                            onPresentationDragUpdate = latestOnPresentationDragUpdate,
                            onPresentationDragRelease = latestOnPresentationDragRelease,
                            onHudText = latestOnGestureHudText,
                        ),
                    )
                    SceneWatchPageSection.ActionRow -> {
                        PlayerWatchPageActionRow(
                            items = actionItems + debugEntry,
                            onAddCurrentSceneToQueue = onAddCurrentSceneToQueue,
                            onIncrementOCounter = onIncrementOCounter,
                            onToggleFavorite = onToggleFavorite,
                            onToggleWatchLater = onToggleWatchLater,
                            onOpenDetails = { detailsDialogOpen = true },
                        )
                    }
                    SceneWatchPageSection.RatingControls -> PlayerWatchPageRatingControlsSurface(
                        ratingStep = ratingStep,
                        ratingMessage = ratingMessage,
                        ratingUpdating = ratingUpdating,
                        onSelectRatingStep = onSelectRatingStep,
                        onRatingInteractionStart = { ratingControlsInteracting = true },
                        onRatingInteractionEnd = { ratingControlsInteracting = false },
                    )
                    SceneWatchPageSection.MetadataBadges -> PlayerWatchPageMetadataBadges(labels = state.metadataBadges)
                    SceneWatchPageSection.Tags -> PlayerWatchPageTagChips(labels = state.tagLabels)
                    SceneWatchPageSection.SimilarScenes -> {
                        SimilarVideosSection(
                            currentSceneId = sceneId,
                            recommendations = similarRecommendations,
                            isLoading = similarRecommendationsLoading,
                            errorMessage = similarRecommendationsError,
                            onPlayScene = onPlaySimilarScene,
                            onAddToQueue = onAddSimilarSceneToQueue,
                            onRetry = onRetrySimilarRecommendations,
                            recommendationSource = similarRecommendationsSource,
                            layoutContext = SimilarVideosLayoutContext.WatchPage,
                            queuedSceneIds = queuedSceneIds,
                        )
                    }
                }
            }
        }
    }
    if (detailsDialogOpen) {
        PlayerWatchPageDebugInfoDialog(
            state = debugInfoUiState,
            onDismiss = { detailsDialogOpen = false },
        )
    }
}

private fun Modifier.watchPageContentPullToFullscreenGesture(
    contentScrolledToTop: () -> Boolean,
    locked: () -> Boolean,
    blocked: () -> Boolean,
    containerHeightPx: () -> Float,
    onPresentationDragUpdate: (PlayerPresentationDragUpdate?) -> Unit,
    onPresentationDragRelease: (PlayerPresentationDragRelease) -> Unit,
    onHudText: (String?) -> Unit,
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val firstDown = awaitFirstDown(requireUnconsumed = false)
        if (blocked()) return@awaitEachGesture
        awaitWatchPageContentPullGesture(
            down = firstDown,
            contentScrolledToTop = contentScrolledToTop,
            locked = locked,
            blocked = blocked,
            containerHeight = containerHeightPx().takeIf { it > 0f } ?: size.height.toFloat(),
            onPresentationDragUpdate = onPresentationDragUpdate,
            onPresentationDragRelease = onPresentationDragRelease,
            onHudText = onHudText,
            width = size.width.toFloat(),
        )
    }
}

private suspend fun AwaitPointerEventScope.awaitWatchPageContentPullGesture(
    down: PointerInputChange,
    contentScrolledToTop: () -> Boolean,
    locked: () -> Boolean,
    blocked: () -> Boolean,
    containerHeight: Float,
    onPresentationDragUpdate: (PlayerPresentationDragUpdate?) -> Unit,
    onPresentationDragRelease: (PlayerPresentationDragRelease) -> Unit,
    onHudText: (String?) -> Unit,
    width: Float,
) {
    var presentationSession: PlayerPresentationDragSession? = startPlayerPresentationDragSession(
        currentMode = PlayerPresentationMode.WatchPage,
        startArea = PlayerPresentationGestureStartArea.WatchPageContent,
        startXPx = down.position.x,
        widthPx = width,
        containerHeightPx = containerHeight,
        uptimeMs = down.uptimeMillis,
    )
    var latestPosition = down.position
    try {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
                ?: event.changes.firstOrNull { it.pressed }
                ?: return
            if (blocked()) {
                return
            }
            if (!change.pressed) {
                val activeSession = presentationSession?.takeIf { it.isPresentationActive }
                if (activeSession != null) {
                    onPresentationDragRelease(activeSession.release())
                    presentationSession = null
                }
                return
            }
            val dragAmount = change.position - latestPosition
            latestPosition = change.position
            if (dragAmount == Offset.Zero) continue
            presentationSession = presentationSession?.dragBy(
                deltaX = dragAmount.x,
                deltaY = dragAmount.y,
                uptimeMs = change.uptimeMillis,
                locked = locked(),
                contentScrolledToTop = contentScrolledToTop(),
            )
            when (val gestureMode = presentationSession?.gestureMode ?: PlayerPresentationGestureMode.None) {
                PlayerPresentationGestureMode.EnterFullscreen -> {
                    presentationSession
                        ?.let(::buildPlayerPresentationDragUpdate)
                        ?.let(onPresentationDragUpdate)
                    change.consume()
                }
                PlayerPresentationGestureMode.Locked -> {
                    onHudText(playerLockedTouchHint())
                    change.consume()
                }
                PlayerPresentationGestureMode.None,
                PlayerPresentationGestureMode.ExitFullscreen,
                PlayerPresentationGestureMode.ReservedPlaybackGesture -> Unit
            }
        }
    } finally {
        presentationSession?.takeIf { it.isPresentationActive }?.let { session ->
            onPresentationDragRelease(session.release())
        }
    }
}

@Composable
private fun PlayerWatchPageHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        PlayerWatchPageController.sceneWatchPageHeaderSubtitle()?.let { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlayerWatchPageActionRow(
    items: List<PlayerExpandedStashActionRowItem>,
    onAddCurrentSceneToQueue: () -> Unit,
    onIncrementOCounter: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            PlayerWatchPageActionChip(
                item = item,
                onClick = when (item.action) {
                    PlayerExpandedStashAction.Favorite -> onToggleFavorite
                    PlayerExpandedStashAction.WatchLater -> onToggleWatchLater
                    PlayerExpandedStashAction.Queue -> onAddCurrentSceneToQueue
                    PlayerExpandedStashAction.OCounter -> onIncrementOCounter
                    PlayerExpandedStashAction.MoreDetails -> onOpenDetails
                    PlayerExpandedStashAction.Rating -> ({})
                },
            )
        }
    }
}

@Composable
private fun PlayerWatchPageActionChip(
    item: PlayerExpandedStashActionRowItem,
    onClick: () -> Unit,
) {
    val accentColor = when (item.action) {
        PlayerExpandedStashAction.Queue -> StashColors.QueueAction
        PlayerExpandedStashAction.Favorite -> StashColors.FavoriteAction
        PlayerExpandedStashAction.WatchLater -> StashColors.WatchLaterAction
        PlayerExpandedStashAction.OCounter -> StashColors.Warning
        PlayerExpandedStashAction.MoreDetails -> StashColors.TextSecondary
        PlayerExpandedStashAction.Rating -> StashColors.Warning
    }
    val shape = RoundedCornerShape(percent = 50)
    val containerColor = when (item.visualState) {
        PlayerExpandedStashActionVisualState.Active -> accentColor.copy(alpha = 0.20f)
        PlayerExpandedStashActionVisualState.Disabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
        PlayerExpandedStashActionVisualState.Loading -> StashColors.Primary.copy(alpha = 0.18f)
        PlayerExpandedStashActionVisualState.Error -> StashColors.Error.copy(alpha = 0.18f)
        PlayerExpandedStashActionVisualState.Inactive -> when (item.action) {
            PlayerExpandedStashAction.MoreDetails -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.74f)
            PlayerExpandedStashAction.OCounter -> StashColors.Warning.copy(alpha = 0.16f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
        }
    }
    val borderColor = when (item.visualState) {
        PlayerExpandedStashActionVisualState.Active -> accentColor.copy(alpha = 0.56f)
        PlayerExpandedStashActionVisualState.Loading -> StashColors.Primary.copy(alpha = 0.44f)
        PlayerExpandedStashActionVisualState.Error -> StashColors.Error.copy(alpha = 0.48f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
    }
    val contentAlpha = if (item.enabled) 1f else StashAlpha.DisabledContent
    Row(
        modifier = Modifier
            .clip(shape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(enabled = item.enabled, onClick = onClick)
            .semantics { contentDescription = item.contentDescription }
            .padding(horizontal = 13.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.action.watchPageIcon(item.visualState),
            contentDescription = null,
            tint = accentColor.copy(alpha = contentAlpha),
        )
        Text(
            text = item.label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        item.valueLabel?.let { value ->
            Text(
                text = value,
                color = accentColor.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private fun PlayerExpandedStashAction.watchPageIcon(
    visualState: PlayerExpandedStashActionVisualState,
): ImageVector = when (this) {
    PlayerExpandedStashAction.Queue -> Icons.AutoMirrored.Outlined.PlaylistAdd
    PlayerExpandedStashAction.Favorite -> if (visualState == PlayerExpandedStashActionVisualState.Active) {
        Icons.Outlined.Favorite
    } else {
        Icons.Outlined.FavoriteBorder
    }
    PlayerExpandedStashAction.WatchLater -> Icons.Outlined.Bookmarks
    PlayerExpandedStashAction.OCounter -> Icons.Outlined.Whatshot
    PlayerExpandedStashAction.MoreDetails -> Icons.Outlined.Info
    PlayerExpandedStashAction.Rating -> Icons.Outlined.Star
}

@Composable
private fun PlayerWatchPageRatingControlsSurface(
    ratingStep: Int,
    ratingMessage: String?,
    ratingUpdating: Boolean,
    onSelectRatingStep: (Int) -> Unit,
    onRatingInteractionStart: () -> Unit,
    onRatingInteractionEnd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stashString(R.string.auto_kr_0234),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        PlayerRatingControls(
            ratingStep = ratingStep,
            ratingMessage = ratingMessage,
            ratingUpdating = ratingUpdating,
            onSelectRatingStep = onSelectRatingStep,
            onRatingInteractionStart = onRatingInteractionStart,
            onRatingInteractionEnd = onRatingInteractionEnd,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlayerWatchPageMetadataBadges(
    labels: List<String>,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
    ) {
        labels.forEach { label ->
            val ratingLabel = label.isWatchPageRatingMetadataLabel()
            val badgePolicy = PlayerWatchPageController.resolveSceneWatchPageMetadataBadgePolicy(
                isRatingBadge = ratingLabel,
            )
            var badgeModifier: Modifier = Modifier
            badgePolicy.minimumWidthDp?.let { minimumWidthDp ->
                badgeModifier = badgeModifier.defaultMinSize(minWidth = minimumWidthDp.dp)
            }
            badgePolicy.minimumHeightDp?.let { minimumHeightDp ->
                badgeModifier = badgeModifier.defaultMinSize(minHeight = minimumHeightDp.dp)
            }
            StashMetadataBadge(
                badge = StashMetadataBadgeModel(
                    label = label,
                    contentDescription = if (ratingLabel) {
                        stashString(R.string.auto_kr_0493, label.removePrefix("★").trim())
                    } else {
                        null
                    },
                ),
                modifier = badgeModifier,
            )
        }
    }
}

private fun String.isWatchPageRatingMetadataLabel(): Boolean = trim().startsWith("★")

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlayerWatchPageTagChips(labels: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
    ) {
        labels.forEach { label ->
            StashTagChip(tag = StashTagChipModel(label = label))
        }
    }
}

@Composable
private fun PlayerWatchPageDebugInfoDialog(
    state: PlayerDebugInfoUiState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.rows.forEach { row ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = row.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = row.value,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = state.dismissLabel)
            }
        },
    )
}
