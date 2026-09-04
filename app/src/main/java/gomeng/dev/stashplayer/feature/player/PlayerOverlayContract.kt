package gomeng.dev.stashplayer.feature.player

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import gomeng.dev.stashplayer.core.model.SimilarSceneRecommendation
import gomeng.dev.stashplayer.core.model.SimilarVideosRecommendationSource
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSpriteFrame
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.player.PlayerDebugInfoUiState
import gomeng.dev.stashplayer.core.player.PlayerGestureExclusionBounds
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerContentState
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerLayout
import gomeng.dev.stashplayer.core.player.PlayerInfoDrawerState
import gomeng.dev.stashplayer.core.player.PlayerOverlayQuickActionState
import gomeng.dev.stashplayer.core.player.PlayerPlaybackUiStatus
import gomeng.dev.stashplayer.core.player.PlayerPlaylistUiItem
import gomeng.dev.stashplayer.core.player.PlayerStreamPreferenceOption
import gomeng.dev.stashplayer.core.player.PlayerStreamSourceOption

@Immutable
data class PlayerOverlayState(
    val title: String,
    val controlsVisible: Boolean,
    val locked: Boolean,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val playbackSpeed: Float,
    val playbackOrientationMode: PlaybackOrientationMode,
    val aspectRatioMode: AspectRatioMode,
    val hudText: String?,
    val seekPreview: PlayerSeekPreview?,
    val playbackStatus: PlayerPlaybackUiStatus,
    val playbackErrorText: String?,
    val canTryAlternateSource: Boolean,
    val canOpenSettings: Boolean,
    val canOpenNextScene: Boolean,
    val canEnterPictureInPicture: Boolean,
    val canShuffleQueue: Boolean,
    val shuffleEnabled: Boolean,
    val ratingStep: Int,
    val ratingMessage: String?,
    val ratingUpdating: Boolean,
    val currentStreamInfoText: String?,
    val quickActions: List<PlayerOverlayQuickActionState>,
    val fullscreenPlayerActive: Boolean,
    val showFullscreenToggle: Boolean,
    val sceneId: String,
    val infoDrawerContentState: PlayerInfoDrawerContentState,
    val debugInfoUiState: PlayerDebugInfoUiState,
    val similarRecommendations: List<SimilarSceneRecommendation>,
    val similarRecommendationsLoading: Boolean,
    val similarRecommendationsError: String?,
    val similarRecommendationsSource: SimilarVideosRecommendationSource,
    val serverProfile: StashServerProfile?,
    val streamPreferenceOptions: List<PlayerStreamPreferenceOption>,
    val streamSourceOptions: List<PlayerStreamSourceOption>,
    val playlistItems: List<PlayerPlaylistUiItem>,
    val infoDrawerState: PlayerInfoDrawerState,
    val infoDrawerLayout: PlayerInfoDrawerLayout,
    val previewFrameFor: (Long) -> StashSpriteFrame?,
)

@Immutable
data class PlayerOverlayCallbacks(
    val onSeekPreview: (PlayerSeekPreview?) -> Unit,
    val onExitPlayer: () -> Unit,
    val onPlayPause: () -> Unit,
    val onSeekTo: (Long) -> Unit,
    val onPreviousTransport: () -> Unit,
    val onNextTransport: () -> Unit,
    val onToggleLock: () -> Unit,
    val onToggleFullscreenPlayer: () -> Unit,
    val onEnterPictureInPicture: () -> Unit,
    val onCycleSpeed: () -> Unit,
    val onTogglePlaybackOrientationMode: () -> Unit,
    val onCycleAspectRatio: () -> Unit,
    val onSelectPlaybackSpeed: (Float) -> Unit,
    val onSelectAspectRatioMode: (AspectRatioMode) -> Unit,
    val onSelectShuffleEnabled: (Boolean) -> Unit,
    val onSelectRatingStep: (Int) -> Unit,
    val onAddCurrentSceneToQueue: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onToggleWatchLater: () -> Unit,
    val onPlaySimilarScene: (String) -> Unit,
    val onAddSimilarSceneToQueue: (String) -> Unit,
    val onRetrySimilarRecommendations: () -> Unit,
    val onSelectStreamPreference: (String) -> Unit,
    val onSelectStreamSource: (Int) -> Unit,
    val onToggleInfoDrawer: () -> Unit,
    val onInfoDrawerDrag: (Float) -> Unit,
    val onInfoDrawerDragEnd: () -> Unit,
    val onOpenPlaylistDrawer: () -> Unit,
    val onRetryPlayback: () -> Unit,
    val onTryAlternateSource: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onBottomControlsGestureBoundsChanged: (PlayerGestureExclusionBounds?) -> Unit = {},
    val onBottomControlsHeightChanged: (Float) -> Unit = {},
    val onPlayerGestureSuspendedByModalSurfaceChanged: (Boolean) -> Unit = {},
)
