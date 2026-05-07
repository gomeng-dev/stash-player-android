package gomeng.dev.stashplayer.core.player

import java.util.Locale

fun shouldPausePlayerForLifecycleStop(backgroundPlaybackEnabled: Boolean): Boolean =
    !backgroundPlaybackEnabled

fun shouldExposePictureInPictureButton(
    pictureInPictureEnabled: Boolean,
    pictureInPictureSupported: Boolean,
): Boolean = pictureInPictureEnabled && pictureInPictureSupported

fun shouldEnterPictureInPictureOnUserLeave(
    pictureInPictureEnabled: Boolean,
    pictureInPictureSupported: Boolean,
    playerRouteActive: Boolean,
    playbackReady: Boolean,
    locked: Boolean,
    modalSurfaceOpen: Boolean,
): Boolean =
    pictureInPictureEnabled &&
        pictureInPictureSupported &&
        playerRouteActive &&
        playbackReady &&
        !locked &&
        !modalSurfaceOpen

enum class PictureInPictureActionType {
    Previous,
    PlayPause,
    Next,
}

fun buildPictureInPictureActionTypes(
    canPlayPrevious: Boolean,
    canPlayNext: Boolean,
): List<PictureInPictureActionType> = buildList {
    if (canPlayPrevious) add(PictureInPictureActionType.Previous)
    add(PictureInPictureActionType.PlayPause)
    if (canPlayNext) add(PictureInPictureActionType.Next)
}

fun subtitleTrackLanguageCode(
    preference: SubtitleLanguagePreference,
    locale: Locale = Locale.getDefault(),
): String? = when (preference) {
    SubtitleLanguagePreference.Auto -> locale.language.takeIf { it.isNotBlank() }
    SubtitleLanguagePreference.Off -> null
    else -> preference.mediaLanguageCode
}

fun shouldDisableSubtitleTrack(preference: SubtitleLanguagePreference): Boolean =
    preference == SubtitleLanguagePreference.Off
