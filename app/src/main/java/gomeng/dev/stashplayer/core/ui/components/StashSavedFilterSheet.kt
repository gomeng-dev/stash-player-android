package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.local.LocalSavedVideoFilter
import gomeng.dev.stashplayer.core.model.StashSavedFilterQuickApplyCandidate
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.duplicateStashSavedFilterName
import gomeng.dev.stashplayer.core.model.filterStashSavedFilterNames
import gomeng.dev.stashplayer.core.model.quickSavedVideoFilterName
import gomeng.dev.stashplayer.core.model.recentFilterSummaryLabel
import gomeng.dev.stashplayer.core.model.recentFiltersForQuickAccess
import gomeng.dev.stashplayer.core.model.savedFiltersForQuickApply
import gomeng.dev.stashplayer.core.model.stashSavedFilterSummaryLabel
import gomeng.dev.stashplayer.core.model.toSavedFilterPayload
import gomeng.dev.stashplayer.core.model.uniqueStashSavedFilterName
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StashSavedFilterSheet(
    savedFilters: List<LocalSavedVideoFilter>,
    recentFilters: List<StashVideoFilterState>,
    currentFilter: StashVideoFilterState,
    savedFilterName: String,
    onSavedFilterNameChange: (String) -> Unit,
    onApplyRecentFilter: (StashVideoFilterState) -> Unit,
    onApplySavedFilter: (LocalSavedVideoFilter) -> Unit,
    onSaveCurrentFilter: (String, StashVideoFilterState) -> Unit,
    onQuickSaveCurrentFilter: (String, StashVideoFilterState) -> Unit,
    onDeleteSavedFilter: (LocalSavedVideoFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var savedFilterSearchQuery by remember { mutableStateOf("") }
    val existingNames = savedFilters.map { it.name }
    val duplicateName = duplicateStashSavedFilterName(savedFilterName, existingNames)
    val uniqueName = uniqueStashSavedFilterName(savedFilterName, existingNames)
    val filteredNames = filterStashSavedFilterNames(existingNames, savedFilterSearchQuery).toSet()
    val visibleQuickSavedFilters = savedFiltersForQuickApply(
        savedFilters = savedFilters.map { filter ->
            StashSavedFilterQuickApplyCandidate(
                id = filter.id,
                name = filter.name,
                filterState = filter.filterState,
                updatedAt = filter.updatedAt,
            )
        },
        currentFilter = currentFilter,
    )
    val visibleRecentFilters = recentFiltersForQuickAccess(recentFilters)
    val visibleSavedFilters = if (savedFilterSearchQuery.isBlank()) {
        savedFilters
    } else {
        savedFilters.filter { it.name in filteredNames }
    }
    val canSaveCurrent = !currentFilter.toSavedFilterPayload().isEmpty

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(stashString(R.string.auto_kr_0344), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = buildString {
                            append(if (savedFilters.isEmpty()) stashString(R.string.auto_kr_0345) else stashString(R.string.auto_kr_0346, savedFilters.size))
                            if (visibleRecentFilters.isNotEmpty()) append(stashString(R.string.auto_kr_0347, visibleRecentFilters.size))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = stashString(R.string.auto_kr_0348))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (visibleQuickSavedFilters.isNotEmpty()) {
                    item(key = "quick_saved_filters") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stashString(R.string.auto_kr_0349), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stashString(R.string.auto_kr_0350),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                visibleQuickSavedFilters.forEach { quickFilter ->
                                    val savedFilter = savedFilters.firstOrNull { it.id == quickFilter.id }
                                    StashVisibilityFilterChip(
                                        selected = quickFilter.isActive,
                                        enabled = savedFilter != null,
                                        onClick = {
                                            if (savedFilter != null) {
                                                onApplySavedFilter(savedFilter)
                                                onDismiss()
                                            }
                                        },
                                        modifier = Modifier.semantics {
                                            contentDescription = quickFilter.contentDescription
                                        },
                                        label = {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = quickFilter.label,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Text(
                                                    text = quickFilter.summary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }

                if (visibleRecentFilters.isNotEmpty()) {
                    item(key = "recent_filters") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stashString(R.string.auto_kr_0351), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stashString(R.string.auto_kr_0352),
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

                item(key = "current_filter") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(stashString(R.string.auto_kr_0353), style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val chips = currentFilter.activeFilterChips()
                            if (chips.isEmpty()) {
                                StashVisibilityFilterChip(selected = false, onClick = {}, label = { Text(stashString(R.string.auto_kr_0354)) })
                            } else {
                                chips.forEach { chip ->
                                    StashVisibilityFilterChip(selected = true, onClick = {}, label = { Text(chip.label, maxLines = 1) })
                                }
                            }
                        }
                        OutlinedTextField(
                            value = savedFilterName,
                            onValueChange = onSavedFilterNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(stashString(R.string.auto_kr_0324)) },
                            placeholder = { Text(stashString(R.string.auto_kr_0325)) },
                            supportingText = {
                                when {
                                    duplicateName != null -> Text(stashString(R.string.auto_kr_0355, duplicateName, uniqueName))
                                    savedFilterName.isBlank() -> Text(stashString(R.string.auto_kr_0356))
                                }
                            },
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = { onSaveCurrentFilter(uniqueName, currentFilter) },
                                enabled = canSaveCurrent,
                            ) { Text(stashString(R.string.auto_kr_0065)) }
                            TextButton(
                                onClick = {
                                    val timestamp = SimpleDateFormat("MM-dd HH:mm", Locale.KOREA).format(Date())
                                    val quickName = uniqueStashSavedFilterName(
                                        currentFilter.quickSavedVideoFilterName(timestamp),
                                        existingNames,
                                    )
                                    onQuickSaveCurrentFilter(quickName, currentFilter)
                                },
                                enabled = canSaveCurrent,
                            ) { Text(stashString(R.string.auto_kr_0357)) }
                        }
                    }
                }

                item(key = "saved_filter_search") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider()
                        OutlinedTextField(
                            value = savedFilterSearchQuery,
                            onValueChange = { savedFilterSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(stashString(R.string.auto_kr_0358)) },
                            placeholder = { Text(stashString(R.string.auto_kr_0359)) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        )
                    }
                }

                when {
                    savedFilters.isEmpty() -> item(key = "empty_saved_filters") {
                        Text(
                            text = stashString(R.string.auto_kr_0360),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    visibleSavedFilters.isEmpty() -> item(key = "no_matching_saved_filters") {
                        Text(
                            text = stashString(R.string.auto_kr_0361),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> items(visibleSavedFilters, key = { it.id }) { filter ->
                        StashSavedFilterSheetRow(
                            filter = filter,
                            isActive = currentFilter.savedFilter?.id == filter.id,
                            onApply = {
                                onApplySavedFilter(filter)
                                onDismiss()
                            },
                            onDelete = { onDeleteSavedFilter(filter) },
                        )
                    }
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
private fun StashSavedFilterSheetRow(
    filter: LocalSavedVideoFilter,
    isActive: Boolean,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (isActive) stashString(R.string.auto_kr_0329, filter.name) else filter.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = filter.filterState.stashSavedFilterSummaryLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onApply) { Text(stashString(R.string.auto_kr_0316)) }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stashString(R.string.auto_kr_0362, filter.name),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        HorizontalDivider()
    }
}
