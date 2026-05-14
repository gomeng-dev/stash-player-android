package gomeng.dev.stashplayer.core.player

import kotlin.math.abs
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

fun playerPlaybackStatusTitle(status: PlayerPlaybackUiStatus): String = when (status) {
    PlayerPlaybackUiStatus.Loading -> stashString(R.string.auto_kr_0201)
    PlayerPlaybackUiStatus.Buffering -> stashString(R.string.auto_kr_0202)
    PlayerPlaybackUiStatus.Ended -> stashString(R.string.auto_kr_0203)
    PlayerPlaybackUiStatus.Error -> stashString(R.string.auto_kr_0204)
    PlayerPlaybackUiStatus.Ready -> ""
}

fun playerRatingSliderLabel(ratingStep: Int): String {
    val step = ratingStep.coerceIn(0, 10)
    return when {
        step == 0 -> stashString(R.string.auto_kr_0184)
        step % 2 == 0 -> "${step / 2}★"
        else -> "${step / 2}.5★"
    }
}

fun playerSeekPreviewDeltaLabel(deltaMs: Long): String {
    val sign = if (deltaMs >= 0L) "+" else "-"
    return "$sign${formatPlayerPosition(abs(deltaMs))}"
}

fun playerSeekPreviewPositionLabel(targetPositionMs: Long, durationMs: Long): String =
    formatPlayerPosition(targetPositionMs.coerceIn(0L, durationMs.coerceAtLeast(0L)))

fun playerSeekTargetBadgeContentDescription(targetPositionLabel: String): String =
    stashString(R.string.player_seek_target_badge_content_description, targetPositionLabel)

data class PlayerSeekPreviewTimelineUiState(
    val visible: Boolean,
    val targetFraction: Float,
    val targetLabel: String,
    val durationLabel: String,
    val deltaLabel: String,
    val contentDescription: String,
)

fun buildPlayerSeekPreviewTimelineUiState(
    deltaMs: Long,
    targetPositionMs: Long,
    durationMs: Long,
): PlayerSeekPreviewTimelineUiState {
    val safeDurationMs = durationMs.coerceAtLeast(0L)
    val targetLabel = playerSeekPreviewPositionLabel(targetPositionMs, safeDurationMs)
    val durationLabel = formatPlayerPosition(safeDurationMs)
    val deltaLabel = playerSeekPreviewDeltaLabel(deltaMs)
    return PlayerSeekPreviewTimelineUiState(
        visible = safeDurationMs > 0L,
        targetFraction = if (safeDurationMs > 0L) {
            targetPositionMs.toFloat().div(safeDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        },
        targetLabel = targetLabel,
        durationLabel = durationLabel,
        deltaLabel = deltaLabel,
        contentDescription = stashString(
            R.string.player_seek_preview_timeline_content_description,
            targetLabel,
            durationLabel,
            deltaLabel,
        ),
    )
}

enum class PlayerOverlayQuickAction {
    Queue,
    Favorite,
    WatchLater,
}

data class PlayerOverlayQuickActionState(
    val action: PlayerOverlayQuickAction,
    val contentDescription: String,
    val active: Boolean,
    val enabled: Boolean = true,
)

enum class PlayerExpandedStashAction {
    Rating,
    Favorite,
    WatchLater,
    Queue,
    OCounter,
    MoreDetails,
}

enum class PlayerExpandedStashActionVisualState {
    Inactive,
    Active,
    Disabled,
    Loading,
    Error,
}

data class PlayerExpandedStashActionRowItem(
    val action: PlayerExpandedStashAction,
    val label: String,
    val contentDescription: String,
    val visualState: PlayerExpandedStashActionVisualState,
    val enabled: Boolean = true,
    val valueLabel: String? = null,
)

enum class PlayerOverlayTransportAction {
    Previous,
    PlayPause,
    Next,
}

data class PlayerOverlayTransportUiState(
    val visible: Boolean,
    val previousContentDescription: String,
    val playPauseContentDescription: String,
    val nextContentDescription: String,
    val previousEnabled: Boolean = true,
    val playPauseEnabled: Boolean = true,
    val nextEnabled: Boolean,
    val actions: List<PlayerOverlayTransportAction> = listOf(
        PlayerOverlayTransportAction.Previous,
        PlayerOverlayTransportAction.PlayPause,
        PlayerOverlayTransportAction.Next,
    ),
)

data class PlayerOverlayTransportButtonVisualStyle(
    val action: PlayerOverlayTransportAction,
    val sizeDp: Int,
    val iconSizeDp: Int,
    val containerAlpha: Float,
    val contentAlpha: Float,
    val primary: Boolean = false,
)

object PlayerOverlayCenterTransportVisualPolicy {
    val ContainerAlpha: Float = 0.24f
    val ButtonSpacingDp: Int = 26
    val SecondaryButtonSizeDp: Int = 56
    val PrimaryButtonSizeDp: Int = 76
    val SecondaryIconSizeDp: Int = 30
    val PrimaryIconSizeDp: Int = 40
    val SecondaryContainerAlpha: Float = 0.48f
    val DisabledContainerAlpha: Float = 0.22f
    val DisabledContentAlpha: Float = 0.38f
    val EnabledContentAlpha: Float = 1f
    val PrimaryContainerAlpha: Float = 0.94f
}

object ImageViewerTransportVisualPolicy {
    val ContainerAlpha: Float = 0.30f
    val ButtonSpacingDp: Int = 10
    val SecondaryButtonSizeDp: Int = 48
    val PrimaryButtonSizeDp: Int = 56
    val SecondaryIconSizeDp: Int = 24
    val PrimaryIconSizeDp: Int = 30
    val SecondaryContainerAlpha: Float = 0.42f
    val DisabledContainerAlpha: Float = 0.18f
    val DisabledContentAlpha: Float = 0.34f
    val EnabledContentAlpha: Float = 1f
    val PrimaryContainerAlpha: Float = 0.92f
}

fun buildImageViewerTransportButtonVisualStyles(
    previousEnabled: Boolean = true,
    playPauseEnabled: Boolean = true,
    nextEnabled: Boolean,
): List<PlayerOverlayTransportButtonVisualStyle> = listOf(
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.Previous,
        sizeDp = playerOverlayTouchTargetSizeDp(ImageViewerTransportVisualPolicy.SecondaryButtonSizeDp),
        iconSizeDp = ImageViewerTransportVisualPolicy.SecondaryIconSizeDp,
        containerAlpha = if (previousEnabled) {
            ImageViewerTransportVisualPolicy.SecondaryContainerAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (previousEnabled) {
            ImageViewerTransportVisualPolicy.EnabledContentAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContentAlpha
        },
    ),
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.PlayPause,
        sizeDp = playerOverlayTouchTargetSizeDp(ImageViewerTransportVisualPolicy.PrimaryButtonSizeDp),
        iconSizeDp = ImageViewerTransportVisualPolicy.PrimaryIconSizeDp,
        containerAlpha = if (playPauseEnabled) {
            ImageViewerTransportVisualPolicy.PrimaryContainerAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (playPauseEnabled) {
            ImageViewerTransportVisualPolicy.EnabledContentAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContentAlpha
        },
        primary = true,
    ),
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.Next,
        sizeDp = playerOverlayTouchTargetSizeDp(ImageViewerTransportVisualPolicy.SecondaryButtonSizeDp),
        iconSizeDp = ImageViewerTransportVisualPolicy.SecondaryIconSizeDp,
        containerAlpha = if (nextEnabled) {
            ImageViewerTransportVisualPolicy.SecondaryContainerAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (nextEnabled) {
            ImageViewerTransportVisualPolicy.EnabledContentAlpha
        } else {
            ImageViewerTransportVisualPolicy.DisabledContentAlpha
        },
    ),
)

fun buildPlayerOverlayTransportButtonVisualStyles(
    previousEnabled: Boolean = true,
    playPauseEnabled: Boolean = true,
    nextEnabled: Boolean,
): List<PlayerOverlayTransportButtonVisualStyle> = listOf(
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.Previous,
        sizeDp = playerOverlayTouchTargetSizeDp(PlayerOverlayCenterTransportVisualPolicy.SecondaryButtonSizeDp),
        iconSizeDp = PlayerOverlayCenterTransportVisualPolicy.SecondaryIconSizeDp,
        containerAlpha = if (previousEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.SecondaryContainerAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (previousEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.EnabledContentAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContentAlpha
        },
    ),
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.PlayPause,
        sizeDp = playerOverlayTouchTargetSizeDp(PlayerOverlayCenterTransportVisualPolicy.PrimaryButtonSizeDp),
        iconSizeDp = PlayerOverlayCenterTransportVisualPolicy.PrimaryIconSizeDp,
        containerAlpha = if (playPauseEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.PrimaryContainerAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (playPauseEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.EnabledContentAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContentAlpha
        },
        primary = true,
    ),
    PlayerOverlayTransportButtonVisualStyle(
        action = PlayerOverlayTransportAction.Next,
        sizeDp = playerOverlayTouchTargetSizeDp(PlayerOverlayCenterTransportVisualPolicy.SecondaryButtonSizeDp),
        iconSizeDp = PlayerOverlayCenterTransportVisualPolicy.SecondaryIconSizeDp,
        containerAlpha = if (nextEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.SecondaryContainerAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContainerAlpha
        },
        contentAlpha = if (nextEnabled) {
            PlayerOverlayCenterTransportVisualPolicy.EnabledContentAlpha
        } else {
            PlayerOverlayCenterTransportVisualPolicy.DisabledContentAlpha
        },
    ),
)

data class PlayerOverlayVisibilityPolicy(
    val showTopControls: Boolean,
    val showQuickActions: Boolean,
    val showBottomControls: Boolean,
    val showUnlockOnly: Boolean,
)

enum class PlayerTopChromeAction {
    Close,
    Stream,
    Speed,
    Orientation,
    Fullscreen,
    More,
}

fun defaultPlayerTopChromeActions(): List<PlayerTopChromeAction> = listOf(
    PlayerTopChromeAction.Close,
    PlayerTopChromeAction.Stream,
    PlayerTopChromeAction.Speed,
    PlayerTopChromeAction.Orientation,
    PlayerTopChromeAction.Fullscreen,
    PlayerTopChromeAction.More,
)

enum class PlayerBottomChromeAction {
    Lock,
    Previous,
    PlayPause,
    Next,
    AspectRatio,
    PictureInPicture,
}

fun defaultPlayerBottomChromeActions(): List<PlayerBottomChromeAction> = listOf(
    PlayerBottomChromeAction.Lock,
    PlayerBottomChromeAction.Previous,
    PlayerBottomChromeAction.PlayPause,
    PlayerBottomChromeAction.Next,
    PlayerBottomChromeAction.AspectRatio,
    PlayerBottomChromeAction.PictureInPicture,
)

enum class PlayerOverflowMenuAction {
    Playlist,
    Queue,
    Favorite,
    WatchLater,
    PlaybackMode,
}

fun defaultPlayerOverflowMenuActions(): List<PlayerOverflowMenuAction> = listOf(
    PlayerOverflowMenuAction.Playlist,
    PlayerOverflowMenuAction.Queue,
    PlayerOverflowMenuAction.Favorite,
    PlayerOverflowMenuAction.WatchLater,
    PlayerOverflowMenuAction.PlaybackMode,
)

object PlayerOverlayAccessibilityPolicy {
    val MinimumTouchTargetDp: Int = 48
    val InfoDrawerHandleTouchTargetDp: Int = MinimumTouchTargetDp
    val DefaultMotionDurationMs: Int = 220

    fun isRecommendedMotionDuration(durationMs: Int): Boolean = durationMs in 150..300
}

object PlayerOverlayQuickActionRailPolicy {
    val ButtonTouchTargetDp: Int = PlayerOverlayAccessibilityPolicy.MinimumTouchTargetDp
    val ContainerPaddingDp: Int = 8
    val ActionSpacingDp: Int = 10
    val TopInsetDp: Int = 16
    val RightInsetDp: Int = 16
    private val MaximumComfortableActions: Int = 4

    fun doesNotCoverExcessiveVideoContent(actionCount: Int): Boolean = actionCount <= MaximumComfortableActions
}

fun playerOverlayTouchTargetSizeDp(preferredDp: Int): Int =
    preferredDp.coerceAtLeast(PlayerOverlayAccessibilityPolicy.MinimumTouchTargetDp)

fun playerLockButtonContentDescription(locked: Boolean): String =
    if (locked) stashString(R.string.auto_kr_0205) else stashString(R.string.auto_kr_0206)

enum class PlayerSeparatedPlaybackOptionSheet {
    Stream,
    Speed,
    AspectRatio,
    PlaybackMode,
}

fun playerSeparatedPlaybackOptionSheetTitle(sheet: PlayerSeparatedPlaybackOptionSheet): String = when (sheet) {
    PlayerSeparatedPlaybackOptionSheet.Stream -> stashString(R.string.auto_kr_0207)
    PlayerSeparatedPlaybackOptionSheet.Speed -> stashString(R.string.auto_kr_0208)
    PlayerSeparatedPlaybackOptionSheet.AspectRatio -> stashString(R.string.auto_kr_0209)
    PlayerSeparatedPlaybackOptionSheet.PlaybackMode -> stashString(R.string.auto_kr_0210)
}

fun playerStreamOptionsContentDescription(): String = stashString(R.string.auto_kr_0211)

fun playerPlaybackSpeedOptionsContentDescription(playbackSpeed: Float): String =
    stashString(R.string.auto_kr_0212, playerPlaybackSpeedLabel(playbackSpeed))

fun playerAspectRatioOptionsContentDescription(aspectRatioMode: AspectRatioMode): String =
    stashString(R.string.auto_kr_0213, playerAspectRatioLabel(aspectRatioMode))

fun playerAspectRatioToggleContentDescription(aspectRatioMode: AspectRatioMode): String =
    stashString(R.string.auto_kr_0214, playerAspectRatioLabel(aspectRatioMode), playerAspectRatioLabel(aspectRatioMode.next()))

fun playerPlaybackModeLabel(shuffleEnabled: Boolean): String =
    if (shuffleEnabled) stashString(R.string.auto_kr_0215) else stashString(R.string.auto_kr_0216)

fun nextPlayerPlaybackModeShuffleEnabled(
    shuffleEnabled: Boolean,
    canShuffleQueue: Boolean,
): Boolean? = when {
    shuffleEnabled -> false
    canShuffleQueue -> true
    else -> null
}

fun playerPlaybackModeToggleContentDescription(
    shuffleEnabled: Boolean,
    canShuffleQueue: Boolean,
): String = when {
    shuffleEnabled -> stashString(R.string.auto_kr_0217)
    canShuffleQueue -> stashString(R.string.auto_kr_0218)
    else -> stashString(R.string.auto_kr_0219)
}

fun playerPlaybackModeOptionsContentDescription(shuffleEnabled: Boolean): String =
    stashString(R.string.auto_kr_0220, playerPlaybackModeLabel(shuffleEnabled))

fun playerPlaylistButtonContentDescription(playlistItemCount: Int): String =
    if (playlistItemCount > 0) stashString(R.string.auto_kr_0221, playlistItemCount) else stashString(R.string.auto_kr_0222)

fun nextPlaybackOrientationMode(current: PlaybackOrientationMode): PlaybackOrientationMode = when (current) {
    PlaybackOrientationMode.Off -> PlaybackOrientationMode.Sensor
    PlaybackOrientationMode.Sensor -> PlaybackOrientationMode.Off
}

fun playerPlaybackOrientationContentDescription(mode: PlaybackOrientationMode): String = when (mode) {
    PlaybackOrientationMode.Off -> stashString(R.string.player_orientation_toggle_sensor_content_description)
    PlaybackOrientationMode.Sensor -> stashString(R.string.player_orientation_toggle_off_content_description)
}

fun playerPlaybackOrientationHudText(mode: PlaybackOrientationMode): String = when (mode) {
    PlaybackOrientationMode.Off -> stashString(R.string.player_orientation_hud_off)
    PlaybackOrientationMode.Sensor -> stashString(R.string.player_orientation_hud_sensor)
}

fun resolvePlayerOverlayTransportUiState(
    controlsVisible: Boolean,
    locked: Boolean,
    isPlaying: Boolean,
    canOpenNextScene: Boolean,
    seekPreviewActive: Boolean = false,
    playbackStatusVisible: Boolean = false,
): PlayerOverlayTransportUiState = PlayerOverlayTransportUiState(
    visible = false,
    previousContentDescription = stashString(R.string.auto_kr_0223),
    playPauseContentDescription = if (isPlaying) stashString(R.string.auto_kr_0224) else stashString(R.string.auto_kr_0039),
    nextContentDescription = if (canOpenNextScene) stashString(R.string.auto_kr_0225) else stashString(R.string.auto_kr_0226),
    previousEnabled = true,
    playPauseEnabled = true,
    nextEnabled = canOpenNextScene,
)

fun buildPlayerOverlayQuickActionStates(
    isQueued: Boolean,
    isFavorite: Boolean,
    isInWatchLater: Boolean,
): List<PlayerOverlayQuickActionState> = listOf(
    PlayerOverlayQuickActionState(
        action = PlayerOverlayQuickAction.Queue,
        contentDescription = if (isQueued) stashString(R.string.auto_kr_0227) else stashString(R.string.auto_kr_0228),
        active = isQueued,
        enabled = !isQueued,
    ),
    PlayerOverlayQuickActionState(
        action = PlayerOverlayQuickAction.Favorite,
        contentDescription = if (isFavorite) stashString(R.string.auto_kr_0229) else stashString(R.string.auto_kr_0230),
        active = isFavorite,
    ),
    PlayerOverlayQuickActionState(
        action = PlayerOverlayQuickAction.WatchLater,
        contentDescription = if (isInWatchLater) stashString(R.string.auto_kr_0231) else stashString(R.string.auto_kr_0232),
        active = isInWatchLater,
    ),
)

fun buildPlayerExpandedStashActionRowItems(
    ratingStep: Int,
    ratingUpdating: Boolean,
    isQueued: Boolean,
    isFavorite: Boolean,
    isInWatchLater: Boolean,
    ratingMessage: String? = null,
): List<PlayerExpandedStashActionRowItem> {
    val ratingLabel = playerRatingSliderLabel(ratingStep)
    val ratingError = ratingMessage?.startsWith(stashString(R.string.auto_kr_0233)) == true
    return listOf(
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.Rating,
            label = stashString(R.string.auto_kr_0234),
            valueLabel = ratingLabel,
            contentDescription = if (ratingUpdating) {
                stashString(R.string.auto_kr_0235, ratingLabel)
            } else {
                stashString(R.string.auto_kr_0236, ratingLabel)
            },
            visualState = when {
                ratingError -> PlayerExpandedStashActionVisualState.Error
                ratingUpdating -> PlayerExpandedStashActionVisualState.Loading
                ratingStep.coerceIn(0, 10) > 0 -> PlayerExpandedStashActionVisualState.Active
                else -> PlayerExpandedStashActionVisualState.Inactive
            },
            enabled = !ratingUpdating,
        ),
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.Favorite,
            label = if (isFavorite) stashString(R.string.auto_kr_0237) else stashString(R.string.auto_kr_0238),
            contentDescription = if (isFavorite) stashString(R.string.auto_kr_0229) else stashString(R.string.auto_kr_0230),
            visualState = if (isFavorite) {
                PlayerExpandedStashActionVisualState.Active
            } else {
                PlayerExpandedStashActionVisualState.Inactive
            },
        ),
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.WatchLater,
            label = if (isInWatchLater) stashString(R.string.auto_kr_0239) else stashString(R.string.auto_kr_0016),
            contentDescription = if (isInWatchLater) stashString(R.string.auto_kr_0231) else stashString(R.string.auto_kr_0232),
            visualState = if (isInWatchLater) {
                PlayerExpandedStashActionVisualState.Active
            } else {
                PlayerExpandedStashActionVisualState.Inactive
            },
        ),
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.Queue,
            label = if (isQueued) stashString(R.string.auto_kr_0009) else stashString(R.string.auto_kr_0040),
            contentDescription = if (isQueued) {
                stashString(R.string.auto_kr_0240)
            } else {
                stashString(R.string.auto_kr_0228)
            },
            visualState = if (isQueued) {
                PlayerExpandedStashActionVisualState.Disabled
            } else {
                PlayerExpandedStashActionVisualState.Inactive
            },
            enabled = !isQueued,
        ),
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.MoreDetails,
            label = stashString(R.string.auto_kr_0241),
            contentDescription = stashString(R.string.auto_kr_0242),
            visualState = PlayerExpandedStashActionVisualState.Inactive,
        ),
    )
}

fun buildPlayerOCounterActionRowItem(
    oCounter: Int,
    updating: Boolean,
): PlayerExpandedStashActionRowItem {
    val count = oCounter.coerceAtLeast(0)
    val valueLabel = stashString(R.string.player_o_counter_value_label, count)
    return PlayerExpandedStashActionRowItem(
        action = PlayerExpandedStashAction.OCounter,
        label = stashString(R.string.player_o_counter_label),
        valueLabel = valueLabel,
        contentDescription = if (updating) {
            stashString(R.string.player_o_counter_saving_content_description, valueLabel)
        } else {
            stashString(R.string.player_o_counter_add_content_description, valueLabel)
        },
        visualState = when {
            updating -> PlayerExpandedStashActionVisualState.Loading
            count > 0 -> PlayerExpandedStashActionVisualState.Active
            else -> PlayerExpandedStashActionVisualState.Inactive
        },
        enabled = !updating,
    )
}

fun resolvePlayerOverlayVisibilityPolicy(
    controlsVisible: Boolean,
    locked: Boolean,
    infoDrawerExpanded: Boolean = false,
): PlayerOverlayVisibilityPolicy = when {
    locked -> PlayerOverlayVisibilityPolicy(
        showTopControls = false,
        showQuickActions = false,
        showBottomControls = false,
        showUnlockOnly = true,
    )
    controlsVisible -> PlayerOverlayVisibilityPolicy(
        showTopControls = true,
        showQuickActions = true,
        showBottomControls = true,
        showUnlockOnly = false,
    )
    else -> PlayerOverlayVisibilityPolicy(
        showTopControls = false,
        showQuickActions = false,
        showBottomControls = false,
        showUnlockOnly = false,
    )
}
