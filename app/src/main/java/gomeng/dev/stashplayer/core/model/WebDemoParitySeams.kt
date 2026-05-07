package gomeng.dev.stashplayer.core.model

enum class WebDemoParitySeam {
    PlayerOverlayComponentBoundaries,
    PlayerDrawerStateHookPoints,
    PlayerRatingStateExtension,
    SceneCardMetadataBadgeTagChipApi,
    DiscoveryOpenSheetSealedState,
}

data class WebDemoParitySeamReadiness(
    val seam: WebDemoParitySeam,
    val sourceFiles: List<String>,
    val testFiles: List<String>,
    val introducesUserVisibleBehavior: Boolean = false,
    val notes: List<String> = emptyList(),
)

fun currentWebDemoParitySeamReadiness(): List<WebDemoParitySeamReadiness> = listOf(
    WebDemoParitySeamReadiness(
        seam = WebDemoParitySeam.PlayerOverlayComponentBoundaries,
        sourceFiles = listOf(
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerOverlay.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerTopControls.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerBottomControls.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerRatingControls.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerStatusOverlay.kt",
        ),
        testFiles = listOf(
            "app/src/test/java/gomeng/dev/stashplayer/core/player/PlayerOverlayComponentCopyTest.kt",
        ),
        notes = listOf("Overlay sections are split into named Compose components with pure copy helpers."),
    ),
    WebDemoParitySeamReadiness(
        seam = WebDemoParitySeam.PlayerDrawerStateHookPoints,
        sourceFiles = listOf(
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerPlaylistDrawer.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerPlaybackOptionsSheet.kt",
            "app/src/main/java/gomeng/dev/stashplayer/core/player/PlayerTransportController.kt",
        ),
        testFiles = listOf(
            "app/src/test/java/gomeng/dev/stashplayer/core/player/PlayerPlaybackMenuUxTest.kt",
            "app/src/test/java/gomeng/dev/stashplayer/core/player/PlayerTransportControllerTest.kt",
        ),
        notes = listOf("Playlist drawer and playback options remain separate surfaces with tested back handling."),
    ),
    WebDemoParitySeamReadiness(
        seam = WebDemoParitySeam.PlayerRatingStateExtension,
        sourceFiles = listOf(
            "app/src/main/java/gomeng/dev/stashplayer/core/player/PlayerRatingState.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/player/PlayerRatingControls.kt",
        ),
        testFiles = listOf(
            "app/src/test/java/gomeng/dev/stashplayer/core/player/PlayerRatingStateTest.kt",
        ),
        notes = listOf("Rating conversion, optimistic save, completion, and failure recovery are isolated in core state."),
    ),
    WebDemoParitySeamReadiness(
        seam = WebDemoParitySeam.SceneCardMetadataBadgeTagChipApi,
        sourceFiles = listOf(
            "app/src/main/java/gomeng/dev/stashplayer/core/model/SceneModels.kt",
            "app/src/main/java/gomeng/dev/stashplayer/core/ui/components/SceneCard.kt",
        ),
        testFiles = listOf(
            "app/src/test/java/gomeng/dev/stashplayer/core/model/WebDemoParitySeamReadinessTest.kt",
        ),
        notes = listOf("Scene cards can carry passive metadata badges and tag chips without rendering behavior changes."),
    ),
    WebDemoParitySeamReadiness(
        seam = WebDemoParitySeam.DiscoveryOpenSheetSealedState,
        sourceFiles = listOf(
            "app/src/main/java/gomeng/dev/stashplayer/core/discovery/StashDiscoveryOpenSheet.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/browse/BrowseRoute.kt",
            "app/src/main/java/gomeng/dev/stashplayer/feature/search/SearchRoute.kt",
        ),
        testFiles = listOf(
            "app/src/test/java/gomeng/dev/stashplayer/core/discovery/StashDiscoveryOpenSheetTest.kt",
        ),
        notes = listOf("Browse and Search share one sealed sheet state instead of parallel booleans."),
    ),
)
