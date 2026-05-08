package gomeng.dev.stashplayer.core.player

import gomeng.dev.stashplayer.core.model.SceneCardTagChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import kotlin.math.roundToInt

/**
 * Policy seam for the YouTube-like split between fullscreen player chrome and
 * non-drawer watch-page details.
 */
data class PlayerFullscreenOverlayInfoPolicy(
    val overlayInfoState: PlayerInfoDrawerState,
    val overlayDragDeltaPx: Float,
    val overlayRevealFraction: Float,
    val overlayLayout: PlayerInfoDrawerLayout,
    val drawerRevealGestureEnabled: Boolean,
    val watchPageDetailsEnabled: Boolean,
)

enum class PlayerFullscreenChromeSection {
    TopChrome,
    CenterTransport,
    BottomProgress,
    MinimalSceneAffordance,
}

enum class PlayerFullscreenBottomChromeSection {
    MinimalSceneLabel,
    CompactTransport,
    TimeLabels,
    SlimSeekBar,
}

enum class SceneWatchPageSection {
    SceneHeader,
    ActionRow,
    RatingControls,
    MetadataBadges,
    Tags,
    SimilarScenes,
}

data class PlayerFullscreenChromePolicy(
    val sectionOrder: List<PlayerFullscreenChromeSection>,
    val watchPageSectionsInOverlay: Set<SceneWatchPageSection>,
)

data class PlayerFullscreenBottomChromeState(
    val visible: Boolean,
    val title: String,
    val positionLabel: String,
    val durationLabel: String,
    val sliderFraction: Float,
    val seekEnabled: Boolean,
    val ratingLabel: String?,
    val sectionOrder: List<PlayerFullscreenBottomChromeSection>,
    val drawerHandleVisible: Boolean,
    val expandedMetadataVisible: Boolean,
    val expandedActionsVisible: Boolean,
    val similarRecommendationsVisible: Boolean,
    val appliedRevealFraction: Float,
    val seekBarVisualPolicy: PlayerFullscreenSeekBarVisualPolicy,
)

data class PlayerFullscreenSeekBarVisualPolicy(
    val touchTargetHeightDp: Int,
    val restingTrackHeightDp: Int,
    val activeTrackHeightDp: Int,
    val thumbDiameterDp: Int,
)

data class PlayerSeekBarAccessibilityState(
    val contentDescription: String,
    val stateDescription: String,
    val progressFraction: Float,
    val enabled: Boolean,
)

data class SceneWatchPageContentState(
    val title: String,
    val sectionOrder: List<SceneWatchPageSection>,
    val metadataBadges: List<String>,
    val tagLabels: List<String>,
    val hasSimilarScenes: Boolean,
)

data class SceneWatchPageMetadataBadgePolicy(
    val opensRatingDialog: Boolean,
    val minimumWidthDp: Float?,
    val minimumHeightDp: Float?,
)

data class SceneWatchPageRatingControlsPlacementPolicy(
    val section: SceneWatchPageSection,
    val visibleOutsideDetails: Boolean,
    val embeddedInDetailsDialog: Boolean,
)

object PlayerWatchPageController {
    fun resolveFullscreenChromePolicy(): PlayerFullscreenChromePolicy = PlayerFullscreenChromePolicy(
        sectionOrder = listOf(
            PlayerFullscreenChromeSection.TopChrome,
            PlayerFullscreenChromeSection.CenterTransport,
            PlayerFullscreenChromeSection.BottomProgress,
            PlayerFullscreenChromeSection.MinimalSceneAffordance,
        ),
        watchPageSectionsInOverlay = emptySet(),
    )

    fun buildFullscreenBottomChromeState(
        title: String,
        displayedPositionMs: Long,
        durationMs: Long,
        sliderFraction: Float,
        ratingStep: Int,
        controlsVisible: Boolean,
        legacyRevealFraction: Float,
    ): PlayerFullscreenBottomChromeState {
        val sections = if (controlsVisible) {
            listOf(
                PlayerFullscreenBottomChromeSection.MinimalSceneLabel,
                PlayerFullscreenBottomChromeSection.CompactTransport,
                PlayerFullscreenBottomChromeSection.TimeLabels,
                PlayerFullscreenBottomChromeSection.SlimSeekBar,
            )
        } else {
            emptyList()
        }
        return PlayerFullscreenBottomChromeState(
            visible = controlsVisible,
            title = title.trim().ifBlank { stashString(R.string.auto_kr_0035) },
            positionLabel = formatPlayerPosition(displayedPositionMs),
            durationLabel = formatPlayerPosition(durationMs),
            sliderFraction = sliderFraction.coerceIn(0f, 1f),
            seekEnabled = controlsVisible && durationMs > 0L,
            ratingLabel = playerFullscreenChromeRatingLabel(ratingStep),
            sectionOrder = sections,
            drawerHandleVisible = false,
            expandedMetadataVisible = false,
            expandedActionsVisible = false,
            similarRecommendationsVisible = false,
            appliedRevealFraction = 0f,
            seekBarVisualPolicy = PlayerFullscreenSeekBarVisualPolicy(
                touchTargetHeightDp = 48,
                restingTrackHeightDp = 2,
                activeTrackHeightDp = 4,
                thumbDiameterDp = 8,
            ),
        )
    }

    fun shouldShowSceneWatchPageSimilarSection(
        recommendationCount: Int,
        isLoading: Boolean,
        errorMessage: String?,
    ): Boolean = isLoading || !errorMessage.isNullOrBlank() || recommendationCount > 0

    fun buildPlayerSeekBarAccessibilityState(
        fraction: Float,
        enabled: Boolean,
    ): PlayerSeekBarAccessibilityState {
        val progressFraction = fraction.coerceIn(0f, 1f)
        val progressPercent = (progressFraction * 100f).roundToInt().coerceIn(0, 100)
        return PlayerSeekBarAccessibilityState(
            contentDescription = stashString(R.string.player_seek_bar_content_description),
            stateDescription = stashString(R.string.player_seek_bar_state_description, progressPercent),
            progressFraction = progressFraction,
            enabled = enabled,
        )
    }

    fun sceneWatchPageHeaderSubtitle(): String? = null

    fun resolveWatchPageRatingControlsPlacementPolicy(): SceneWatchPageRatingControlsPlacementPolicy =
        SceneWatchPageRatingControlsPlacementPolicy(
            section = SceneWatchPageSection.RatingControls,
            visibleOutsideDetails = true,
            embeddedInDetailsDialog = false,
        )

    fun resolveSceneWatchPageMetadataBadgePolicy(isRatingBadge: Boolean): SceneWatchPageMetadataBadgePolicy =
        if (isRatingBadge) {
            SceneWatchPageMetadataBadgePolicy(
                opensRatingDialog = false,
                minimumWidthDp = 44f,
                minimumHeightDp = null,
            )
        } else {
            SceneWatchPageMetadataBadgePolicy(
                opensRatingDialog = false,
                minimumWidthDp = null,
                minimumHeightDp = null,
            )
        }

    fun shouldRenderSceneWatchPageContent(fullscreenPlayerActive: Boolean): Boolean = !fullscreenPlayerActive

    fun playerSurfacePresentationGestureMode(fullscreenPlayerActive: Boolean): PlayerPresentationMode = if (fullscreenPlayerActive) {
        PlayerPresentationMode.Fullscreen
    } else {
        PlayerPresentationMode.WatchPage
    }

    fun playerFullscreenToggleContentDescription(fullscreenPlayerActive: Boolean): String = if (fullscreenPlayerActive) {
        stashString(R.string.auto_kr_0282)
    } else {
        stashString(R.string.auto_kr_0283)
    }

    fun playerExitButtonContentDescription(fullscreenPlayerActive: Boolean): String = if (fullscreenPlayerActive) {
        stashString(R.string.auto_kr_0284)
    } else {
        stashString(R.string.auto_kr_0285)
    }

    fun buildSceneWatchPageContentState(
        title: String,
        tagChips: List<SceneCardTagChip>,
        studioName: String? = null,
        playCount: Int?,
        width: Int?,
        height: Int?,
        durationMs: Long,
        rating100: Int?,
        hasSimilarScenes: Boolean,
    ): SceneWatchPageContentState {
        val metadataBadges = buildSceneWatchPageMetadataBadges(
            studioName = studioName,
            playCount = playCount,
            width = width,
            height = height,
            durationMs = durationMs,
            rating100 = rating100,
        )
        val tagLabels = tagChips
            .mapNotNull { chip -> chip.label.trim().takeIf { it.isNotBlank() } }
            .distinct()
        val sections = buildList {
            add(SceneWatchPageSection.SceneHeader)
            add(SceneWatchPageSection.ActionRow)
            add(resolveWatchPageRatingControlsPlacementPolicy().section)
            if (metadataBadges.isNotEmpty()) add(SceneWatchPageSection.MetadataBadges)
            if (tagLabels.isNotEmpty()) add(SceneWatchPageSection.Tags)
            if (hasSimilarScenes) add(SceneWatchPageSection.SimilarScenes)
        }
        return SceneWatchPageContentState(
            title = title.trim().ifBlank { stashString(R.string.auto_kr_0035) },
            sectionOrder = sections,
            metadataBadges = metadataBadges,
            tagLabels = tagLabels,
            hasSimilarScenes = hasSimilarScenes,
        )
    }

    fun buildSceneWatchPageActionRowItems(
        ratingStep: Int,
        ratingUpdating: Boolean,
        isQueued: Boolean,
        isFavorite: Boolean,
        isInWatchLater: Boolean,
        oCounter: Int? = null,
        oCounterUpdating: Boolean = false,
        ratingMessage: String? = null,
    ): List<PlayerExpandedStashActionRowItem> {
        val localItems = buildPlayerExpandedStashActionRowItems(
            ratingStep = ratingStep,
            ratingUpdating = ratingUpdating,
            isQueued = isQueued,
            isFavorite = isFavorite,
            isInWatchLater = isInWatchLater,
            ratingMessage = ratingMessage,
        ).filterNot { item ->
            item.action == PlayerExpandedStashAction.Rating || item.action == PlayerExpandedStashAction.MoreDetails
        }
        return localItems + listOfNotNull(
            oCounter?.let { count ->
                buildPlayerOCounterActionRowItem(
                    oCounter = count,
                    updating = oCounterUpdating,
                )
            },
        )
    }

    fun buildSceneWatchPageDebugEntry(enabled: Boolean): PlayerExpandedStashActionRowItem =
        PlayerExpandedStashActionRowItem(
            action = PlayerExpandedStashAction.MoreDetails,
            label = stashString(R.string.auto_kr_0241),
            contentDescription = stashString(R.string.auto_kr_0242),
            visualState = if (enabled) {
                PlayerExpandedStashActionVisualState.Inactive
            } else {
                PlayerExpandedStashActionVisualState.Disabled
            },
            enabled = enabled,
        )

    fun watchPageBackActionPriority(): List<PlayerBackAction> = listOf(
        PlayerBackAction.DismissPlaybackOptions,
        PlayerBackAction.DismissPlaylistDrawer,
        PlayerBackAction.DismissDebugSurface,
        PlayerBackAction.HideControls,
        PlayerBackAction.ExitPlayer,
    )

    fun resolveFullscreenOverlayInfoPolicy(
        legacyInfoDrawerState: PlayerInfoDrawerState,
        legacyDragDeltaPx: Float,
        watchPageDetailsVisible: Boolean,
    ): PlayerFullscreenOverlayInfoPolicy {
        return PlayerFullscreenOverlayInfoPolicy(
            overlayInfoState = PlayerInfoDrawerState.Collapsed,
            overlayDragDeltaPx = 0f,
            overlayRevealFraction = 0f,
            overlayLayout = PlayerInfoDrawerLayout(
                drawerOffsetPx = 0f,
                revealFraction = 0f,
                videoScale = 1f,
                videoTranslateYPx = 0f,
            ),
            drawerRevealGestureEnabled = false,
            watchPageDetailsEnabled = watchPageDetailsVisible,
        )
    }

    fun detailsSurfaceContentDescription(expanded: Boolean): String = if (expanded) {
        stashString(R.string.auto_kr_0286)
    } else {
        stashString(R.string.auto_kr_0287)
    }

    private fun playerFullscreenChromeRatingLabel(ratingStep: Int): String? {
        val step = ratingStep.coerceIn(0, 10)
        return when {
            step == 0 -> null
            step % 2 == 0 -> "${step / 2}.0"
            else -> "${step / 2}.5"
        }
    }

    private fun buildSceneWatchPageMetadataBadges(
        studioName: String?,
        playCount: Int?,
        width: Int?,
        height: Int?,
        durationMs: Long,
        rating100: Int?,
    ): List<String> = buildList {
        studioName?.toSceneWatchPageMetadataValue()?.let(::add)
        formatPlayerInfoDrawerResolution(width = width, height = height)?.let(::add)
        durationMs.takeIf { it > 0L }?.let { add(formatPlayerPosition(it)) }
        rating100?.takeIf { it > 0 }?.let { add("★ ${formatPlayerInfoDrawerRating(it)}") }
        playCount?.takeIf { it > 0 }?.let { add(stashString(R.string.auto_kr_0197, it)) }
    }

    private fun String.toSceneWatchPageMetadataValue(): String? {
        val normalized = trim().takeIf { it.isNotBlank() } ?: return null
        return normalized.takeUnless { it in sceneWatchPageMetadataPlaceholders }
    }

    private val sceneWatchPageMetadataPlaceholders = setOf(stashString(R.string.auto_kr_0184), stashString(R.string.auto_kr_0198), "-")
}
