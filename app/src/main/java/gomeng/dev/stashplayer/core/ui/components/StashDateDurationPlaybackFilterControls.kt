package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashPlaybackState
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.buildStashDateRangeFromInputs
import gomeng.dev.stashplayer.core.model.buildStashDurationRangeFromMinuteInputs
import gomeng.dev.stashplayer.core.model.findMatchingStashDurationPreset
import gomeng.dev.stashplayer.core.model.stashDurationPresetOptions
import gomeng.dev.stashplayer.core.model.toggleStashPlaybackState
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashDateDurationPlaybackFilterButton(
    enabled: Boolean,
    videoFilter: StashVideoFilterState,
    onClick: () -> Unit,
) {
    val count = buildList {
        if (videoFilter.dateRange?.isEmpty == false) add(Unit)
        if (videoFilter.durationRange?.isEmpty == false) add(Unit)
        if (videoFilter.playbackState != null) add(Unit)
    }.size
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        val suffix = count.takeIf { it > 0 }?.let { " $it" }.orEmpty()
        Text(stashString(R.string.auto_kr_0301, suffix))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StashDateDurationPlaybackFilterSheet(
    videoFilter: StashVideoFilterState,
    onApply: (StashVideoFilterState) -> Unit,
    onClearDateRange: () -> Unit,
    onClearDurationRange: () -> Unit,
    onClearPlaybackState: () -> Unit,
    onDismiss: () -> Unit,
) {
    var startDate by remember(videoFilter.dateRange) { mutableStateOf(videoFilter.dateRange?.start.orEmpty()) }
    var endDate by remember(videoFilter.dateRange) { mutableStateOf(videoFilter.dateRange?.end.orEmpty()) }
    var minMinutes by remember(videoFilter.durationRange) {
        mutableStateOf(videoFilter.durationRange?.minSeconds?.let { (it / 60).toString() }.orEmpty())
    }
    var maxMinutes by remember(videoFilter.durationRange) {
        mutableStateOf(videoFilter.durationRange?.maxSeconds?.let { (it / 60).toString() }.orEmpty())
    }
    var playbackState by remember(videoFilter.playbackState) { mutableStateOf(videoFilter.playbackState) }
    val draftDurationRange = buildStashDurationRangeFromMinuteInputs(minMinutes, maxMinutes)
    val selectedDurationPreset = findMatchingStashDurationPreset(draftDurationRange)

    fun applyFilters() {
        onApply(
            videoFilter.copy(
                dateRange = buildStashDateRangeFromInputs(startDate, endDate),
                durationRange = buildStashDurationRangeFromMinuteInputs(minMinutes, maxMinutes),
                playbackState = playbackState,
            ),
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stashString(R.string.auto_kr_0302), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stashString(R.string.auto_kr_0303),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stashString(R.string.auto_kr_0304), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        startDate = ""
                        endDate = ""
                        onClearDateRange()
                    }) { Text(stashString(R.string.auto_kr_0305)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(stashString(R.string.auto_kr_0306)) },
                        placeholder = { Text("YYYY-MM-DD") },
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(stashString(R.string.auto_kr_0307)) },
                        placeholder = { Text("YYYY-MM-DD") },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stashString(R.string.auto_kr_0308), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        minMinutes = ""
                        maxMinutes = ""
                        onClearDurationRange()
                    }) { Text(stashString(R.string.auto_kr_0309)) }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    stashDurationPresetOptions().forEach { preset ->
                        StashVisibilityFilterChip(
                            selected = selectedDurationPreset == preset,
                            onClick = {
                                minMinutes = preset.minMinutes?.toString().orEmpty()
                                maxMinutes = preset.maxMinutes?.toString().orEmpty()
                            },
                            label = { Text(preset.label) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minMinutes,
                        onValueChange = { minMinutes = it.filter(Char::isDigit) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stashString(R.string.auto_kr_0310)) },
                        placeholder = { Text(stashString(R.string.auto_kr_0311)) },
                    )
                    OutlinedTextField(
                        value = maxMinutes,
                        onValueChange = { maxMinutes = it.filter(Char::isDigit) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stashString(R.string.auto_kr_0312)) },
                        placeholder = { Text(stashString(R.string.auto_kr_0313)) },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stashString(R.string.auto_kr_0314), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        playbackState = null
                        onClearPlaybackState()
                    }) { Text(stashString(R.string.auto_kr_0315)) }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StashPlaybackState.entries.forEach { state ->
                        StashVisibilityFilterChip(
                            selected = playbackState == state,
                            onClick = { playbackState = toggleStashPlaybackState(playbackState, state) },
                            label = { Text(state.label) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text(stashString(R.string.auto_kr_0192)) }
                Button(onClick = {
                    applyFilters()
                    onDismiss()
                }) { Text(stashString(R.string.auto_kr_0316)) }
            }
            Spacer(modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}
