package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashUnifiedFilterPanelSection
import gomeng.dev.stashplayer.core.model.StashVideoFilterEditTarget
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.hasResettableUnifiedFilterPanelSection
import gomeng.dev.stashplayer.core.model.recentFilterSummaryLabel
import gomeng.dev.stashplayer.core.model.recentFiltersForQuickAccess
import gomeng.dev.stashplayer.core.model.resetUnifiedFilterPanelSection
import gomeng.dev.stashplayer.core.model.unifiedFilterPanelButtonLabel
import gomeng.dev.stashplayer.core.model.unifiedFilterPanelSections
import gomeng.dev.stashplayer.core.model.editTarget
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashUnifiedFilterButton(
    enabled: Boolean,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        BadgedBox(
            badge = {
                if (videoFilter.activeFilterCount > 0) {
                    Badge { Text(videoFilter.activeFilterCount.toString()) }
                }
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(videoFilter.unifiedFilterPanelButtonLabel(savedFilterCount))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StashUnifiedVideoFilterSheet(
    videoFilter: StashVideoFilterState,
    recentFilters: List<StashVideoFilterState>,
    savedFilterCount: Int,
    onApplyRecentFilter: (StashVideoFilterState) -> Unit,
    onApplyVideoFilter: (StashVideoFilterState) -> Unit,
    onOpenTagFilter: () -> Unit,
    onOpenDateDurationPlaybackFilter: () -> Unit,
    onOpenRatingMediaFormatFilter: () -> Unit,
    onOpenLocalLibraryFilter: () -> Unit,
    onToggleRandomShuffle: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sections = videoFilter.unifiedFilterPanelSections(savedFilterCount)
    val visibleRecentFilters = recentFiltersForQuickAccess(recentFilters)

    fun openSection(target: StashVideoFilterEditTarget) {
        onDismiss()
        when (target) {
            StashVideoFilterEditTarget.Tags -> onOpenTagFilter()
            StashVideoFilterEditTarget.DateDurationPlayback -> onOpenDateDurationPlaybackFilter()
            StashVideoFilterEditTarget.RatingMedia -> onOpenRatingMediaFormatFilter()
            StashVideoFilterEditTarget.LocalLibrary -> onOpenLocalLibraryFilter()
            StashVideoFilterEditTarget.RandomShuffle -> onToggleRandomShuffle()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            ) {
                item(key = "header") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                Text(stashString(R.string.auto_kr_0375), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = if (videoFilter.isEmpty) {
                                        savedFilterCount.takeIf { it > 0 }?.let { stashString(R.string.auto_kr_0376, it) } ?: stashString(R.string.auto_kr_0354)
                                    } else {
                                        stashString(R.string.auto_kr_0377, videoFilter.activeFilterCount)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(
                                onClick = { onApplyVideoFilter(StashVideoFilterState()) },
                                enabled = !videoFilter.isEmpty,
                            ) {
                                Text(stashString(R.string.auto_kr_0378))
                            }
                        }

                        val activeChips = videoFilter.activeFilterChips()
                        if (activeChips.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                activeChips.forEach { chip ->
                                    StashVisibilityFilterChip(
                                        selected = true,
                                        onClick = { openSection(chip.category.editTarget()) },
                                        label = {
                                            Text(
                                                text = chip.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                if (visibleRecentFilters.isNotEmpty()) {
                    item(key = "recent_filters") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stashString(R.string.auto_kr_0351), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stashString(R.string.auto_kr_0379),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                visibleRecentFilters.forEach { filter ->
                                    StashVisibilityFilterChip(
                                        selected = false,
                                        onClick = {
                                            onApplyRecentFilter(filter)
                                            onDismiss()
                                        },
                                        label = {
                                            Text(
                                                text = filter.recentFilterSummaryLabel(),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                items(sections, key = { it.title }) { section ->
                    val resetAction = if (videoFilter.hasResettableUnifiedFilterPanelSection(section.editTarget)) {
                        { onApplyVideoFilter(videoFilter.resetUnifiedFilterPanelSection(section.editTarget)) }
                    } else {
                        null
                    }
                    UnifiedFilterHubSectionCard(
                        section = section,
                        onOpen = { openSection(section.editTarget) },
                        onReset = resetAction,
                    )
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text(stashString(R.string.auto_kr_0192)) }
            }
        }
    }
}

@Composable
private fun UnifiedFilterHubSectionCard(
    section: StashUnifiedFilterPanelSection,
    onOpen: () -> Unit,
    onReset: (() -> Unit)?,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(section.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (section.isActive) section.summary else stashString(R.string.auto_kr_0380, section.summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (section.isActive) {
                    Badge { Text("ON") }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onReset != null) {
                    TextButton(onClick = onReset) { Text(stashString(R.string.auto_kr_0374)) }
                }
                Button(onClick = onOpen) {
                    Text(if (section.editTarget == StashVideoFilterEditTarget.RandomShuffle) stashString(R.string.auto_kr_0381) else stashString(R.string.auto_kr_0382))
                }
            }
        }
    }
}
