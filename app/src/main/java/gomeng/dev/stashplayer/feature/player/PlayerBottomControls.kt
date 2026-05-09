package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosLayoutContext
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.PlayerDebugInfoUiState
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashAction
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashActionRowItem
import gomeng.dev.stashplayer.core.player.PlayerExpandedStashActionVisualState
import gomeng.dev.stashplayer.core.player.PlayerFullscreenBottomChromeSection
import gomeng.dev.stashplayer.core.player.PlayerFullscreenSeekBarVisualPolicy
import gomeng.dev.stashplayer.core.player.PlayerOverlayAccessibilityPolicy
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickAction
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionState
import gomeng.dev.stashplayer.core.player.PlayerOverlayTransportUiState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerContentSection
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerContentState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerController
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerLayout
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerProgressiveRevealState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerSeekRowUiState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerState
import gomeng.dev.stashplayer.core.player.PlayerWatchPageController
import gomeng.dev.stashplayer.core.player.buildPlayerExpandedStashActionRowItems
import gomeng.dev.stashplayer.core.player.formatPlayerPosition
import gomeng.dev.stashplayer.core.player.playerAspectRatioToggleContentDescription
import gomeng.dev.stashplayer.core.player.playerLockButtonContentDescription
import gomeng.dev.stashplayer.core.ui.components.SimilarVideosSection
import gomeng.dev.stashplayer.core.ui.designsystem.StashAlpha
import gomeng.dev.stashplayer.core.ui.designsystem.StashColors
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadge
import gomeng.dev.stashplayer.core.ui.designsystem.StashMetadataBadgeModel
import gomeng.dev.stashplayer.core.ui.designsystem.StashPlayerYoutubeVisualTokens
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChip
import gomeng.dev.stashplayer.core.ui.designsystem.StashTagChipModel
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun PlayerBottomControls(
    title: String,
    displayedPositionMs: Long,
    durationMs: Long,
    sliderFraction: Float,
    transportState: PlayerOverlayTransportUiState,
    isPlaying: Boolean,
    aspectRatioMode: AspectRatioMode,
    canEnterPictureInPicture: Boolean,
    ratingStep: Int,
    ratingMessage: String?,
    ratingUpdating: Boolean,
    quickActions: List<PlayerOverlayQuickActionState>,
    sceneId: String,
    infoDrawerContentState: PlayerInfoDrawerContentState,
    debugInfoUiState: PlayerDebugInfoUiState,
    similarRecommendations: List<SimilarSceneRecommendation>,
    similarRecommendationsLoading: Boolean,
    similarRecommendationsError: String?,
    similarRecommendationsSource: SimilarVideosRecommendationSource,
    serverProfile: StashServerProfile?,
    onSliderFractionChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onPreviousTransport: () -> Unit,
    onPlayPause: () -> Unit,
    onNextTransport: () -> Unit,
    onToggleLock: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onSelectRatingStep: (Int) -> Unit,
    onAddCurrentSceneToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleWatchLater: () -> Unit,
    onPlaySimilarScene: (String) -> Unit,
    onAddSimilarSceneToQueue: (String) -> Unit,
    onRetrySimilarRecommendations: () -> Unit,
    modifier: Modifier = Modifier,
    infoDrawerState: PlayerInfoDrawerState = PlayerInfoDrawerState.Collapsed,
    infoDrawerLayout: PlayerInfoDrawerLayout,
    onToggleInfoDrawer: () -> Unit = {},
    onInfoDrawerDrag: (Float) -> Unit = {},
    onInfoDrawerDragEnd: () -> Unit = {},
) {
    val chrome = PlayerWatchPageController.buildFullscreenBottomChromeState(
        title = title,
        displayedPositionMs = displayedPositionMs,
        durationMs = durationMs,
        sliderFraction = sliderFraction,
        ratingStep = ratingStep,
        controlsVisible = true,
        legacyRevealFraction = infoDrawerLayout.revealFraction,
    )
    val seekRowUiState = PlayerInfoDrawerSeekRowUiState(
        visibleInCollapsedDrawer = chrome.visible,
        visibleInExpandedDrawer = chrome.visible,
        renderDuplicateInsideExpandedContent = false,
        sliderEnabled = chrome.seekEnabled,
        sliderFraction = chrome.sliderFraction,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = StashPlayerYoutubeVisualTokens.BottomSheetHorizontalInsetDp.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0f),
                        Color.Black.copy(alpha = 0.52f),
                        Color.Black.copy(alpha = 0.82f),
                    ),
                ),
            )
            .padding(
                start = StashPlayerYoutubeVisualTokens.BottomSheetContentInsetDp.dp,
                end = StashPlayerYoutubeVisualTokens.BottomSheetContentInsetDp.dp,
                bottom = StashPlayerYoutubeVisualTokens.BottomSheetContentInsetDp.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PlayerSeekRow(
            displayedPositionMs = displayedPositionMs,
            durationMs = durationMs,
            uiState = seekRowUiState,
            visualPolicy = chrome.seekBarVisualPolicy,
            onSliderFractionChange = onSliderFractionChange,
            onSliderChangeFinished = onSliderChangeFinished,
        )
        if (chrome.sectionOrder.contains(PlayerFullscreenBottomChromeSection.CompactTransport)) {
            PlayerFullscreenScreenshotStyleActionRow(
                state = transportState,
                isPlaying = isPlaying,
                onPreviousTransport = onPreviousTransport,
                onPlayPause = onPlayPause,
                onNextTransport = onNextTransport,
                onToggleLock = onToggleLock,
                aspectRatioMode = aspectRatioMode,
                onCycleAspectRatio = onCycleAspectRatio,
                canEnterPictureInPicture = canEnterPictureInPicture,
                onEnterPictureInPicture = onEnterPictureInPicture,
            )
        }
    }
}

@Composable
private fun PlayerFullscreenScreenshotStyleActionRow(
    state: PlayerOverlayTransportUiState,
    isPlaying: Boolean,
    onPreviousTransport: () -> Unit,
    onPlayPause: () -> Unit,
    onNextTransport: () -> Unit,
    onToggleLock: () -> Unit,
    aspectRatioMode: AspectRatioMode,
    onCycleAspectRatio: () -> Unit,
    canEnterPictureInPicture: Boolean,
    onEnterPictureInPicture: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerFullscreenCompactTransportButton(
            onClick = onToggleLock,
            contentDescription = playerLockButtonContentDescription(locked = false),
        ) {
            Icon(
                Icons.Outlined.LockOpen,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(22.dp),
            )
        }
        PlayerFullscreenCompactTransportButton(
            onClick = onPreviousTransport,
            contentDescription = state.previousContentDescription,
        ) {
            Icon(
                Icons.Outlined.SkipPrevious,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(22.dp),
            )
        }
        PlayerFullscreenCompactTransportButton(
            onClick = onPlayPause,
            contentDescription = state.playPauseContentDescription,
            emphasized = true,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
        PlayerFullscreenCompactTransportButton(
            onClick = onNextTransport,
            contentDescription = state.nextContentDescription,
            enabled = state.nextEnabled,
        ) {
            Icon(
                Icons.Outlined.SkipNext,
                contentDescription = null,
                tint = Color.White.copy(alpha = if (state.nextEnabled) 0.92f else 0.38f),
                modifier = Modifier.size(22.dp),
            )
        }
        PlayerFullscreenCompactTransportButton(
            onClick = onCycleAspectRatio,
            contentDescription = playerAspectRatioToggleContentDescription(aspectRatioMode),
        ) {
            Icon(
                Icons.Outlined.AspectRatio,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(22.dp),
            )
        }
        if (canEnterPictureInPicture) {
            PlayerFullscreenCompactTransportButton(
                onClick = onEnterPictureInPicture,
                contentDescription = stashString(R.string.player_pip_button_content_description),
            ) {
                Icon(
                    Icons.Outlined.PictureInPictureAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerFullscreenCompactTransportButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(PlayerOverlayAccessibilityPolicy.MinimumTouchTargetDp.dp)
            .semantics { this.contentDescription = contentDescription }
            .background(
                color = Color.Black.copy(alpha = if (emphasized) 0.34f else 0.20f),
                shape = CircleShape,
            ),
        content = content,
    )
}

@Composable
private fun PlayerFullscreenChromeTitleRow(
    title: String,
    ratingLabel: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ratingLabel?.let { label ->
            Text(
                text = "★ $label",
                color = StashColors.TextPrimary.copy(alpha = 0.86f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PlayerSeekRow(
    displayedPositionMs: Long,
    durationMs: Long,
    uiState: PlayerInfoDrawerSeekRowUiState,
    visualPolicy: PlayerFullscreenSeekBarVisualPolicy,
    onSliderFractionChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
) {
    val remainingMs = (durationMs - displayedPositionMs).coerceAtLeast(0L)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatPlayerPosition(displayedPositionMs),
            color = StashColors.TextPrimary.copy(
                alpha = StashPlayerYoutubeVisualTokens.BottomSheetTimePrimaryAlpha,
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(48.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            ThinPlayerSeekBar(
                fraction = uiState.sliderFraction,
                enabled = uiState.sliderEnabled,
                visualPolicy = visualPolicy,
                onFractionChange = onSliderFractionChange,
                onChangeFinished = onSliderChangeFinished,
            )
        }
        Text(
            text = "-${formatPlayerPosition(remainingMs)}",
            color = StashColors.TextSecondary.copy(
                alpha = StashPlayerYoutubeVisualTokens.BottomSheetTimeSecondaryAlpha,
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(52.dp),
        )
    }
}

@Composable
private fun ThinPlayerSeekBar(
    fraction: Float,
    enabled: Boolean,
    visualPolicy: PlayerFullscreenSeekBarVisualPolicy,
    onFractionChange: (Float) -> Unit,
    onChangeFinished: () -> Unit,
) {
    val coercedFraction = fraction.coerceIn(0f, 1f)
    val accessibilityState = PlayerWatchPageController.buildPlayerSeekBarAccessibilityState(
        fraction = coercedFraction,
        enabled = enabled,
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(visualPolicy.touchTargetHeightDp.dp)
            .semantics {
                contentDescription = accessibilityState.contentDescription
                stateDescription = accessibilityState.stateDescription
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = accessibilityState.progressFraction,
                    range = 0f..1f,
                    steps = 0,
                )
                if (!accessibilityState.enabled) {
                    disabled()
                }
                if (accessibilityState.enabled) {
                    setProgress { targetFraction ->
                        onFractionChange(targetFraction.coerceIn(0f, 1f))
                        onChangeFinished()
                        true
                    }
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun updateFraction(x: Float) {
                        val widthPx = size.width.toFloat().coerceAtLeast(1f)
                        onFractionChange((x / widthPx).coerceIn(0f, 1f))
                    }
                    updateFraction(down.position.x)
                    drag(down.id) { change ->
                        updateFraction(change.position.x)
                        change.consume()
                    }
                    onChangeFinished()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(visualPolicy.restingTrackHeightDp.dp)
                .clip(CircleShape)
                .background(
                    StashColors.TextSecondary.copy(
                        alpha = StashPlayerYoutubeVisualTokens.BottomSheetSeekInactiveTrackAlpha,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(maxWidth * coercedFraction)
                .height(visualPolicy.activeTrackHeightDp.dp)
                .clip(CircleShape)
                .background(StashColors.Primary),
        )
        if (enabled) {
            val thumbSize = visualPolicy.thumbDiameterDp.dp
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = maxWidth * coercedFraction - (thumbSize / 2))
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(StashColors.Primary),
            )
        }
    }
}

@Composable
private fun PlayerRatingChip(ratingStep: Int) {
    val label = playerCompactRatingLabel(ratingStep)
    Row(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(0xFF22D3EE))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "★",
            color = Color(0xFF05070D),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = label,
            color = Color(0xFF05070D),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun playerCompactRatingLabel(ratingStep: Int): String {
    val step = ratingStep.coerceIn(0, 10)
    return when {
        step == 0 -> "—"
        step % 2 == 0 -> "${step / 2}.0"
        else -> "${step / 2}.5"
    }
}

@Composable
private fun PlayerExpandedStashActionRow(
    items: List<PlayerExpandedStashActionRowItem>,
    onAddCurrentSceneToQueue: () -> Unit,
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
            if (item.action != PlayerExpandedStashAction.Rating) {
                PlayerExpandedStashActionChip(
                    item = item,
                    onClick = when (item.action) {
                        PlayerExpandedStashAction.Favorite -> onToggleFavorite
                        PlayerExpandedStashAction.WatchLater -> onToggleWatchLater
                        PlayerExpandedStashAction.Queue -> onAddCurrentSceneToQueue
                        PlayerExpandedStashAction.OCounter -> ({})
                        PlayerExpandedStashAction.MoreDetails -> onOpenDetails
                        PlayerExpandedStashAction.Rating -> ({})
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerExpandedStashActionChip(
    item: PlayerExpandedStashActionRowItem,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(percent = 50)
    val accentColor = when (item.action) {
        PlayerExpandedStashAction.Queue -> StashColors.QueueAction
        PlayerExpandedStashAction.Favorite -> StashColors.FavoriteAction
        PlayerExpandedStashAction.WatchLater -> StashColors.WatchLaterAction
        PlayerExpandedStashAction.OCounter -> StashColors.Warning
        PlayerExpandedStashAction.MoreDetails -> StashColors.TextPrimary
        PlayerExpandedStashAction.Rating -> StashColors.Warning
    }
    val containerColor = when (item.visualState) {
        PlayerExpandedStashActionVisualState.Active -> accentColor.copy(alpha = 0.24f)
        PlayerExpandedStashActionVisualState.Disabled -> Color.White.copy(alpha = 0.07f)
        PlayerExpandedStashActionVisualState.Loading -> StashColors.Primary.copy(alpha = 0.18f)
        PlayerExpandedStashActionVisualState.Error -> StashColors.Error.copy(alpha = 0.20f)
        PlayerExpandedStashActionVisualState.Inactive -> Color.White.copy(alpha = 0.09f)
    }
    val contentAlpha = if (item.enabled) 1f else StashAlpha.DisabledContent
    Row(
        modifier = Modifier
            .heightIn(min = PlayerOverlayAccessibilityPolicy.MinimumTouchTargetDp.dp)
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (item.visualState == PlayerExpandedStashActionVisualState.Active) {
                    accentColor.copy(alpha = 0.74f)
                } else {
                    Color.White.copy(alpha = 0.13f)
                },
                shape = shape,
            )
            .clickable(enabled = item.enabled, onClick = onClick)
            .semantics { contentDescription = item.contentDescription }
            .padding(horizontal = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = when (item.action) {
                PlayerExpandedStashAction.Queue -> Icons.AutoMirrored.Outlined.PlaylistAdd
                PlayerExpandedStashAction.Favorite -> if (item.visualState == PlayerExpandedStashActionVisualState.Active) {
                    Icons.Outlined.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                }
                PlayerExpandedStashAction.WatchLater -> Icons.Outlined.Bookmarks
                PlayerExpandedStashAction.OCounter -> Icons.Outlined.Star
                PlayerExpandedStashAction.MoreDetails -> Icons.Outlined.MoreHoriz
                PlayerExpandedStashAction.Rating -> Icons.Outlined.Star
            },
            contentDescription = null,
            tint = accentColor.copy(alpha = contentAlpha),
        )
        Text(
            text = item.label,
            color = StashColors.TextPrimary.copy(alpha = contentAlpha),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        item.valueLabel?.let { value ->
            Text(
                text = value,
                color = accentColor.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PlayerExpandedInfoDrawerContent(
    sceneId: String,
    state: PlayerInfoDrawerContentState,
    debugInfoUiState: PlayerDebugInfoUiState,
    revealState: PlayerInfoDrawerProgressiveRevealState,
    similarRecommendations: List<SimilarSceneRecommendation>,
    similarRecommendationsLoading: Boolean,
    similarRecommendationsError: String?,
    similarRecommendationsSource: SimilarVideosRecommendationSource,
    serverProfile: StashServerProfile?,
    onPlaySimilarScene: (String) -> Unit,
    onAddSimilarSceneToQueue: (String) -> Unit,
    onRetrySimilarRecommendations: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val contentLayout = PlayerInfoDrawerController.resolveExpandedContentLayout(
        metadataVisible = revealState.metadataVisible,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.sectionOrder.forEach { section ->
            when (section) {
                PlayerInfoDrawerContentSection.CompactMetadata -> {
                    if (revealState.metadataVisible &&
                        (state.metadataBadges.isNotEmpty() || contentLayout.showDebugActionInMetadataRow)
                    ) {
                        PlayerMetadataBadgesRow(
                            labels = state.metadataBadges,
                            trailingContent = if (contentLayout.showDebugActionInMetadataRow) {
                                {
                                    PlayerDebugInfoButton(
                                        state = debugInfoUiState,
                                        onClick = onOpenDetails,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
                PlayerInfoDrawerContentSection.FilePath -> {
                    // Filename/path details are intentionally hidden from the normal drawer.
                }
                PlayerInfoDrawerContentSection.Tags -> {
                    if (revealState.metadataVisible) {
                        PlayerTagChipsRow(labels = state.tagLabels)
                    }
                }
                PlayerInfoDrawerContentSection.SimilarVideos -> {
                    if (revealState.similarVideosVisible) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ) {
                            SimilarVideosSection(
                                currentSceneId = sceneId,
                                recommendations = similarRecommendations,
                                isLoading = similarRecommendationsLoading,
                                errorMessage = similarRecommendationsError,
                                onPlayScene = onPlaySimilarScene,
                                onAddToQueue = onAddSimilarSceneToQueue,
                                onRetry = onRetrySimilarRecommendations,
                                modifier = Modifier.padding(12.dp),
                                recommendationSource = similarRecommendationsSource,
                                layoutContext = SimilarVideosLayoutContext.PlayerDrawer,
                                serverProfile = serverProfile,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerDebugInfoButton(
    state: PlayerDebugInfoUiState,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = state.buttonContentDescription
        },
    ) {
        Text(text = state.buttonLabel)
    }
}

@Composable
private fun PlayerDebugInfoDialog(
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

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlayerMetadataBadgesRow(
    labels: List<String>,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
            verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        ) {
            labels.forEach { label ->
                StashMetadataBadge(badge = StashMetadataBadgeModel(label = label))
            }
        }
        trailingContent?.invoke()
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PlayerTagChipsRow(labels: List<String>) {
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
private fun PlayerInfoDrawerHeader(
    title: String,
    ratingStep: Int,
    showTitle: Boolean,
    showRatingChip: Boolean,
    state: PlayerInfoDrawerState,
    dragTargetHeightDp: Float,
    handleHeightDp: Float,
    onClick: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = PlayerWatchPageController.detailsSurfaceContentDescription(expanded = false)
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (showTitle) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (showRatingChip) {
                PlayerRatingChip(ratingStep = ratingStep)
            }
        }
    }
}
