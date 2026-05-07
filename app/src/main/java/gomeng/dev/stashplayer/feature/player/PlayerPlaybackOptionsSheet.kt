package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.player.AspectRatioMode
import gomeng.dev.stashplayer.core.player.PlayerSeparatedPlaybackOptionSheet
import gomeng.dev.stashplayer.core.player.PlayerStreamPreferenceOption
import gomeng.dev.stashplayer.core.player.PlayerStreamSourceOption
import gomeng.dev.stashplayer.core.player.buildPlayerAspectRatioQuickOptions
import gomeng.dev.stashplayer.core.player.buildPlayerPlaybackSpeedQuickOptions
import gomeng.dev.stashplayer.core.player.canChoosePlayerStreamSource
import gomeng.dev.stashplayer.core.player.playerAspectRatioLabel
import gomeng.dev.stashplayer.core.player.playerPlaybackMenuMaxHeightDp
import gomeng.dev.stashplayer.core.player.playerPlaybackModeLabel
import gomeng.dev.stashplayer.core.player.playerPlaybackSpeedLabel
import gomeng.dev.stashplayer.core.player.playerSeparatedPlaybackOptionSheetTitle
import gomeng.dev.stashplayer.core.player.shouldUseScrollablePlaybackOptionsMenu
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPill
import gomeng.dev.stashplayer.core.ui.designsystem.StashBottomSheetContainer
import gomeng.dev.stashplayer.core.ui.designsystem.StashSheetHeader
import gomeng.dev.stashplayer.core.ui.designsystem.StashSheetOptionRow
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerPlaybackOptionsSheet(
    sheet: PlayerSeparatedPlaybackOptionSheet,
    currentStreamInfoText: String?,
    streamPreferenceOptions: List<PlayerStreamPreferenceOption>,
    streamSourceOptions: List<PlayerStreamSourceOption>,
    playbackSpeed: Float,
    aspectRatioMode: AspectRatioMode,
    canShuffleQueue: Boolean,
    shuffleEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectStreamSource: (Int) -> Unit,
    onSelectStreamPreference: (String) -> Unit,
    onSelectPlaybackSpeed: (Float) -> Unit,
    onSelectAspectRatioMode: (AspectRatioMode) -> Unit,
    onSelectShuffleEnabled: (Boolean) -> Unit,
) {
    val optionsScrollState = rememberScrollState()
    val optionsShouldScroll = sheet == PlayerSeparatedPlaybackOptionSheet.Stream &&
        shouldUseScrollablePlaybackOptionsMenu(streamSourceOptions.size)
    val sheetSubtitle = when (sheet) {
        PlayerSeparatedPlaybackOptionSheet.Stream -> currentStreamInfoText
        PlayerSeparatedPlaybackOptionSheet.Speed -> stashString(R.string.auto_kr_0459, playerPlaybackSpeedLabel(playbackSpeed))
        PlayerSeparatedPlaybackOptionSheet.AspectRatio -> stashString(R.string.auto_kr_0460, playerAspectRatioLabel(aspectRatioMode))
        PlayerSeparatedPlaybackOptionSheet.PlaybackMode -> stashString(R.string.auto_kr_0461, playerPlaybackModeLabel(shuffleEnabled))
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        StashBottomSheetContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .heightIn(max = playerPlaybackMenuMaxHeightDp().dp)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .then(if (optionsShouldScroll) Modifier.verticalScroll(optionsScrollState) else Modifier),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StashSheetHeader(
                    title = playerSeparatedPlaybackOptionSheetTitle(sheet),
                    subtitle = sheetSubtitle,
                    contentDescription = stashString(R.string.auto_kr_0462, playerSeparatedPlaybackOptionSheetTitle(sheet)),
                )
                when (sheet) {
                    PlayerSeparatedPlaybackOptionSheet.Stream -> {
                        streamPreferenceOptions.forEach { option ->
                            StashSheetOptionRow(
                                title = option.title,
                                subtitle = option.subtitle,
                                selected = option.selected,
                                enabled = option.enabled,
                                onClick = { onSelectStreamPreference(option.id) },
                            )
                        }
                        if (canChoosePlayerStreamSource(streamSourceOptions.size)) {
                            Text(stashString(R.string.auto_kr_0186), style = MaterialTheme.typography.titleMedium)
                            streamSourceOptions.forEach { option ->
                                StashSheetOptionRow(
                                    title = option.title,
                                    subtitle = option.subtitle,
                                    leadingLabel = "#${option.index + 1}",
                                    selected = option.selected,
                                    onClick = { onSelectStreamSource(option.index) },
                                )
                            }
                        }
                    }
                    PlayerSeparatedPlaybackOptionSheet.Speed -> {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            buildPlayerPlaybackSpeedQuickOptions(playbackSpeed).forEach { option ->
                                StashActionPill(
                                    label = option.label,
                                    selected = option.selected,
                                    onClick = { onSelectPlaybackSpeed(option.speed) },
                                )
                            }
                        }
                    }
                    PlayerSeparatedPlaybackOptionSheet.AspectRatio -> {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            buildPlayerAspectRatioQuickOptions(aspectRatioMode).forEach { option ->
                                StashActionPill(
                                    label = option.label,
                                    selected = option.selected,
                                    onClick = { onSelectAspectRatioMode(option.mode) },
                                )
                            }
                        }
                    }
                    PlayerSeparatedPlaybackOptionSheet.PlaybackMode -> {
                        StashSheetOptionRow(
                            title = stashString(R.string.auto_kr_0216),
                            subtitle = stashString(R.string.auto_kr_0463),
                            selected = !shuffleEnabled,
                            enabled = true,
                            onClick = { onSelectShuffleEnabled(false) },
                        )
                        StashSheetOptionRow(
                            title = stashString(R.string.auto_kr_0215),
                            subtitle = if (canShuffleQueue) {
                                stashString(R.string.auto_kr_0464)
                            } else {
                                stashString(R.string.auto_kr_0465)
                            },
                            selected = shuffleEnabled,
                            enabled = canShuffleQueue,
                            onClick = { onSelectShuffleEnabled(true) },
                        )
                    }
                }
            }
        }
    }
}
