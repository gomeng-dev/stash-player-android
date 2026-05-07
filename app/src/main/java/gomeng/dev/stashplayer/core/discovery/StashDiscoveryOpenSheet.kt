package gomeng.dev.stashplayer.core.discovery

sealed interface StashDiscoveryOpenSheet {
    data object None : StashDiscoveryOpenSheet
    data object Tags : StashDiscoveryOpenSheet
    data object DateDurationPlayback : StashDiscoveryOpenSheet
    data object RatingMedia : StashDiscoveryOpenSheet
    data object LocalLibrary : StashDiscoveryOpenSheet
    data object UnifiedFilterPanel : StashDiscoveryOpenSheet
    data object SavedFilters : StashDiscoveryOpenSheet
}

fun StashDiscoveryOpenSheet.open(target: StashDiscoveryOpenSheet): StashDiscoveryOpenSheet = target

fun StashDiscoveryOpenSheet.dismiss(): StashDiscoveryOpenSheet = StashDiscoveryOpenSheet.None

data class StashDiscoveryFilterSheetVisibility(
    val isUnifiedFilterPanelOpen: Boolean,
    val isSavedFilterSheetOpen: Boolean,
    val isTagFilterOpen: Boolean,
    val isDateDurationPlaybackFilterOpen: Boolean,
    val isRatingMediaFormatFilterOpen: Boolean,
    val isLocalLibraryFilterOpen: Boolean,
)

fun StashDiscoveryOpenSheet.toFilterSheetVisibility(): StashDiscoveryFilterSheetVisibility =
    StashDiscoveryFilterSheetVisibility(
        isUnifiedFilterPanelOpen = this == StashDiscoveryOpenSheet.UnifiedFilterPanel,
        isSavedFilterSheetOpen = this == StashDiscoveryOpenSheet.SavedFilters,
        isTagFilterOpen = this == StashDiscoveryOpenSheet.Tags,
        isDateDurationPlaybackFilterOpen = this == StashDiscoveryOpenSheet.DateDurationPlayback,
        isRatingMediaFormatFilterOpen = this == StashDiscoveryOpenSheet.RatingMedia,
        isLocalLibraryFilterOpen = this == StashDiscoveryOpenSheet.LocalLibrary,
    )
