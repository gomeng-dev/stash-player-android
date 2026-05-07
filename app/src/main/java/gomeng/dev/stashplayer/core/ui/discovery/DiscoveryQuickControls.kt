package gomeng.dev.stashplayer.core.ui.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.stashDiscoveryPrimaryQuickControlLabels
import gomeng.dev.stashplayer.core.model.stashScenesToolbarRandomActionLabel
import gomeng.dev.stashplayer.core.ui.components.StashUnifiedFilterButton
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> DiscoveryQuickControls(
    horizontalPadding: Dp,
    isConfigured: Boolean,
    sortValue: String,
    sortOptions: List<T>,
    sortOptionLabel: (T) -> String,
    onSelectSort: (T) -> Unit,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onOpenFilter: () -> Unit,
    onToggleRandomShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    sortDirectionLabel: String? = null,
    onToggleSortDirection: (() -> Unit)? = null,
    pageSizeValue: String? = null,
    pageSizeOptions: List<Int> = emptyList(),
    onSelectPageSize: ((Int) -> Unit)? = null,
) {
    val primaryLabels = stashDiscoveryPrimaryQuickControlLabels()
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DiscoveryToolbarDropdown(
            label = primaryLabels[0],
            value = sortValue,
            enabled = isConfigured,
            options = sortOptions,
            optionLabel = sortOptionLabel,
            onSelect = onSelectSort,
        )
        if (sortDirectionLabel != null && onToggleSortDirection != null) {
            OutlinedButton(
                onClick = onToggleSortDirection,
                enabled = isConfigured,
            ) {
                Text(sortDirectionLabel)
            }
        }
        StashUnifiedFilterButton(
            enabled = isConfigured,
            videoFilter = videoFilter,
            savedFilterCount = savedFilterCount,
            onClick = onOpenFilter,
        )
        OutlinedButton(
            onClick = onToggleRandomShuffle,
            enabled = isConfigured,
        ) {
            Text(stashScenesToolbarRandomActionLabel(videoFilter.randomShuffle))
        }
        if (pageSizeValue != null && onSelectPageSize != null && pageSizeOptions.isNotEmpty()) {
            DiscoveryToolbarDropdown(
                label = stashString(R.string.auto_kr_0074),
                value = pageSizeValue,
                enabled = isConfigured,
                options = pageSizeOptions,
                optionLabel = { stashString(R.string.auto_kr_0398, it) },
                onSelect = onSelectPageSize,
            )
        }
    }
}

@Composable
private fun <T> DiscoveryToolbarDropdown(
    label: String,
    value: String,
    enabled: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
        ) {
            Text("$label: $value")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
