package gomeng.dev.stashplayer.core.player

data class ReusablePlayerSeekRowState(
    val currentLabel: String,
    val remainingLabel: String,
    val sliderFraction: Float,
    val sliderEnabled: Boolean,
)

fun defaultPlayerFullscreenSeekBarVisualPolicy(): PlayerFullscreenSeekBarVisualPolicy =
    PlayerFullscreenSeekBarVisualPolicy(
        touchTargetHeightDp = 48,
        restingTrackHeightDp = 2,
        activeTrackHeightDp = 4,
        thumbDiameterDp = 8,
    )

fun buildReusablePlayerSeekRowState(
    displayedPositionMs: Long,
    durationMs: Long,
    sliderFraction: Float,
    sliderEnabled: Boolean,
): ReusablePlayerSeekRowState {
    val safeDurationMs = durationMs.coerceAtLeast(0L)
    val safePositionMs = displayedPositionMs.coerceIn(0L, safeDurationMs)
    val remainingMs = (safeDurationMs - safePositionMs).coerceAtLeast(0L)
    return ReusablePlayerSeekRowState(
        currentLabel = formatPlayerPosition(safePositionMs),
        remainingLabel = "-${formatPlayerPosition(remainingMs)}",
        sliderFraction = sliderFraction.coerceIn(0f, 1f),
        sliderEnabled = sliderEnabled && safeDurationMs > 0L,
    )
}
