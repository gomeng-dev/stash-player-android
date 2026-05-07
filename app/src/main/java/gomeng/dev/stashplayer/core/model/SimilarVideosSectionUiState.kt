package gomeng.dev.stashplayer.core.model

import kotlin.math.roundToInt
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

enum class SimilarVideosLayout {
    HorizontalRail,
    CompactVertical,
}

enum class SimilarVideosLayoutContext {
    General,
    PlayerDrawer,
    WatchPage,
}

private val SIMILAR_VIDEOS_COMPACT_VERTICAL_CARD_HEIGHT_DP = 130
private val SIMILAR_VIDEOS_COMPACT_VERTICAL_ITEM_SPACING_DP = 16
private val SIMILAR_VIDEOS_COMPACT_VERTICAL_MAX_HEIGHT_DP = 360

enum class SimilarVideosRecommendationSource {
    HybridBackend,
    GraphQlFallback,
}

object SimilarVideosSectionCopy {
    val title = stashString(R.string.auto_kr_0028)
    val subtitle = ""
    val emptyMessage = stashString(R.string.auto_kr_0029)
    val errorMessage = stashString(R.string.auto_kr_0030)
    val retryLabel = stashString(R.string.auto_kr_0031)
    val retryContentDescription = stashString(R.string.auto_kr_0032)

    fun titleFor(source: SimilarVideosRecommendationSource): String = title

    fun subtitleFor(source: SimilarVideosRecommendationSource): String = when (source) {
        SimilarVideosRecommendationSource.HybridBackend -> ""
        SimilarVideosRecommendationSource.GraphQlFallback -> ""
    }

    fun sourceBadgeFor(source: SimilarVideosRecommendationSource): String = when (source) {
        SimilarVideosRecommendationSource.HybridBackend -> stashString(R.string.auto_kr_0034)
        SimilarVideosRecommendationSource.GraphQlFallback -> stashString(R.string.auto_kr_0033)
    }
}

data class SimilarVideosCompactRecommendationCardVisualPolicy(
    val horizontalSpacingDp: Int,
    val cardPaddingDp: Int,
    val thumbnailRadiusDp: Int,
    val verticalItemSpacingDp: Int,
    val metadataBadgeSpacingDp: Int,
    val horizontalRailCardWidthDp: Int,
    val thumbnailWidthDp: Int,
    val actionButtonSizeDp: Int,
    val actionIconSizeDp: Int,
    val actionButtonHorizontalPaddingDp: Int,
    val actionButtonSpacingDp: Int,
    val thumbnailAspectRatio: Float,
    val titleMaxLines: Int,
    val metadataMaxLines: Int,
)

fun similarVideosCompactRecommendationCardVisualPolicy(): SimilarVideosCompactRecommendationCardVisualPolicy =
    SimilarVideosCompactRecommendationCardVisualPolicy(
        horizontalSpacingDp = 16,
        cardPaddingDp = 12,
        thumbnailRadiusDp = 12,
        verticalItemSpacingDp = 16,
        metadataBadgeSpacingDp = 16,
        horizontalRailCardWidthDp = 320,
        thumbnailWidthDp = 144,
        actionButtonSizeDp = 60,
        actionIconSizeDp = 38,
        actionButtonHorizontalPaddingDp = 0,
        actionButtonSpacingDp = 10,
        thumbnailAspectRatio = 16f / 9f,
        titleMaxLines = 2,
        metadataMaxLines = 1,
    )

sealed interface SimilarVideosSectionUiState {
    data object Loading : SimilarVideosSectionUiState

    data class Success(
        val title: String,
        val subtitle: String,
        val sourceBadgeLabel: String,
        val layout: SimilarVideosLayout,
        val items: List<SimilarSceneCardUiModel>,
    ) : SimilarVideosSectionUiState

    data class Empty(
        val message: String,
        val retryLabel: String,
        val retryContentDescription: String,
    ) : SimilarVideosSectionUiState

    data class Error(
        val message: String,
        val retryLabel: String,
        val retryContentDescription: String,
    ) : SimilarVideosSectionUiState
}

data class SimilarSceneCardUiModel(
    val sceneId: String,
    val title: String,
    val imageUrl: String?,
    val metadataBadges: List<String>,
    val scoreLabel: String,
    val reasonChips: List<String>,
    val openContentDescription: String,
    val playContentDescription: String,
    val queueContentDescription: String,
    val playAction: SimilarSceneActionUiModel,
    val queueAction: SimilarSceneActionUiModel,
)

data class SimilarSceneActionUiModel(
    val label: String,
    val contentDescription: String,
    val enabled: Boolean = true,
    val selected: Boolean = false,
)

data class SimilarScenePrimaryClickAction(
    val sceneId: String,
    val contentDescription: String,
)

fun buildSimilarScenePrimaryClickAction(
    item: SimilarSceneCardUiModel,
): SimilarScenePrimaryClickAction = SimilarScenePrimaryClickAction(
    sceneId = item.sceneId,
    contentDescription = item.openContentDescription,
)

fun buildSimilarVideosSectionUiState(
    currentSceneId: String,
    recommendations: List<SimilarSceneRecommendation>,
    isLoading: Boolean,
    errorMessage: String?,
    availableWidthDp: Int,
    recommendationSource: SimilarVideosRecommendationSource = SimilarVideosRecommendationSource.HybridBackend,
    layoutContext: SimilarVideosLayoutContext = SimilarVideosLayoutContext.General,
    queuedSceneIds: Set<String> = emptySet(),
): SimilarVideosSectionUiState {
    if (isLoading) return SimilarVideosSectionUiState.Loading
    if (!errorMessage.isNullOrBlank()) {
        return SimilarVideosSectionUiState.Error(
            message = SimilarVideosSectionCopy.errorMessage,
            retryLabel = SimilarVideosSectionCopy.retryLabel,
            retryContentDescription = SimilarVideosSectionCopy.retryContentDescription,
        )
    }

    val items = filterSimilarRecommendationsForCurrentScene(currentSceneId, recommendations)
        .map { recommendation ->
            buildSimilarSceneCardUiModel(
                recommendation = recommendation,
                queuedSceneIds = queuedSceneIds,
            )
        }
    if (items.isEmpty()) {
        return SimilarVideosSectionUiState.Empty(
            message = SimilarVideosSectionCopy.emptyMessage,
            retryLabel = SimilarVideosSectionCopy.retryLabel,
            retryContentDescription = SimilarVideosSectionCopy.retryContentDescription,
        )
    }

    return SimilarVideosSectionUiState.Success(
        title = SimilarVideosSectionCopy.titleFor(recommendationSource),
        subtitle = SimilarVideosSectionCopy.subtitleFor(recommendationSource),
        sourceBadgeLabel = SimilarVideosSectionCopy.sourceBadgeFor(recommendationSource),
        layout = chooseSimilarVideosLayout(
            availableWidthDp = availableWidthDp,
            layoutContext = layoutContext,
        ),
        items = items,
    )
}

fun chooseSimilarVideosLayout(
    availableWidthDp: Int,
    layoutContext: SimilarVideosLayoutContext = SimilarVideosLayoutContext.General,
): SimilarVideosLayout {
    val horizontalThresholdDp = when (layoutContext) {
        SimilarVideosLayoutContext.General -> 480
        SimilarVideosLayoutContext.PlayerDrawer -> 600
        SimilarVideosLayoutContext.WatchPage -> 600
    }
    return if (availableWidthDp >= horizontalThresholdDp) {
        SimilarVideosLayout.HorizontalRail
    } else {
        SimilarVideosLayout.CompactVertical
    }
}

fun shouldUseScrollableSimilarVideosList(
    layout: SimilarVideosLayout,
    itemCount: Int,
): Boolean = layout == SimilarVideosLayout.CompactVertical && itemCount > 2

fun similarVideosCompactVerticalMaxHeightDp(
    itemCount: Int,
    cardHeightDp: Int = SIMILAR_VIDEOS_COMPACT_VERTICAL_CARD_HEIGHT_DP,
    itemSpacingDp: Int = SIMILAR_VIDEOS_COMPACT_VERTICAL_ITEM_SPACING_DP,
    maxHeightDp: Int = SIMILAR_VIDEOS_COMPACT_VERTICAL_MAX_HEIGHT_DP,
): Int {
    val safeItemCount = itemCount.coerceAtLeast(0)
    val safeCardHeight = cardHeightDp.coerceAtLeast(1)
    val safeSpacing = itemSpacingDp.coerceAtLeast(0)
    val safeMaxHeight = maxHeightDp.coerceAtLeast(safeCardHeight)
    if (safeItemCount == 0) return 0

    val fullHeight = safeItemCount * safeCardHeight + (safeItemCount - 1) * safeSpacing
    return fullHeight.coerceAtMost(safeMaxHeight)
}

fun buildSimilarSceneCardUiModel(
    recommendation: SimilarSceneRecommendation,
    queuedSceneIds: Set<String> = emptySet(),
): SimilarSceneCardUiModel {
    val scene = recommendation.scene
    val sceneId = recommendation.sceneId.ifBlank { scene.id }
    val title = scene.title.trim()
        .ifBlank { scene.fileName?.trim().orEmpty() }
        .ifBlank { stashString(R.string.auto_kr_0035) }
    val isQueued = sceneId in queuedSceneIds
    return SimilarSceneCardUiModel(
        sceneId = sceneId,
        title = title,
        imageUrl = scene.thumbnailUrl?.trim().takeUnless { it.isNullOrEmpty() }
            ?: scene.spriteImageUrl?.trim().takeUnless { it.isNullOrEmpty() },
        metadataBadges = buildSimilarSceneMetadataBadges(scene),
        scoreLabel = formatSimilarSceneScore(recommendation.score),
        reasonChips = emptyList(),
        openContentDescription = stashString(R.string.auto_kr_0036, title),
        playContentDescription = stashString(R.string.auto_kr_0037, title),
        queueContentDescription = stashString(R.string.auto_kr_0038, title),
        playAction = SimilarSceneActionUiModel(
            label = stashString(R.string.auto_kr_0039),
            contentDescription = stashString(R.string.auto_kr_0037, title),
        ),
        queueAction = SimilarSceneActionUiModel(
            label = if (isQueued) stashString(R.string.auto_kr_0009) else stashString(R.string.auto_kr_0040),
            contentDescription = if (isQueued) {
                stashString(R.string.auto_kr_0041, title)
            } else {
                stashString(R.string.auto_kr_0038, title)
            },
            enabled = !isQueued,
            selected = isQueued,
        ),
    )
}

fun buildSimilarSceneMetadataBadges(scene: SimilarSceneSummary): List<String> {
    return listOfNotNull(
        formatSimilarSceneResolution(scene.width, scene.height),
        formatSimilarSceneDuration(scene.durationSeconds),
    )
}

fun formatSimilarSceneScore(score: Double): String {
    return when {
        score >= 0.75 -> stashString(R.string.auto_kr_0042)
        score >= 0.45 -> stashString(R.string.auto_kr_0043)
        else -> stashString(R.string.auto_kr_0044)
    }
}

fun formatSimilarSceneResolution(width: Int?, height: Int?): String? {
    val normalizedHeight = height?.takeIf { it > 0 } ?: return null
    return when {
        normalizedHeight >= 2160 -> "4K"
        normalizedHeight >= 1440 -> "1440p"
        normalizedHeight >= 1080 -> "1080p"
        else -> "${normalizedHeight}p"
    }
}

fun formatSimilarSceneDuration(durationSeconds: Double?): String? {
    val totalSeconds = durationSeconds?.roundToInt()?.takeIf { it > 0 } ?: return null
    val totalMinutes = (totalSeconds + 30) / 60
    return if (totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        if (minutes == 0) stashString(R.string.auto_kr_0045, hours) else stashString(R.string.auto_kr_0046, hours, minutes)
    } else {
        stashString(R.string.auto_kr_0047, totalMinutes.coerceAtLeast(1))
    }
}
