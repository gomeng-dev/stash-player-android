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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.local.LocalSavedGalleryFilter
import gomeng.dev.stashplayer.core.model.StashGalleryFilterState
import gomeng.dev.stashplayer.core.model.duplicateStashSavedFilterName
import gomeng.dev.stashplayer.core.model.filterStashSavedFilterNames
import gomeng.dev.stashplayer.core.model.galleryFilterSummaryLabel
import gomeng.dev.stashplayer.core.model.quickSavedGalleryFilterName
import gomeng.dev.stashplayer.core.model.toGallerySavedFilterPayload
import gomeng.dev.stashplayer.core.model.uniqueStashSavedFilterName
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StashGallerySavedFilterSheet(
    savedFilters: List<LocalSavedGalleryFilter>,
    recentFilters: List<StashGalleryFilterState>,
    currentFilter: StashGalleryFilterState,
    savedFilterName: String,
    onSavedFilterNameChange: (String) -> Unit,
    onApplyRecentFilter: (StashGalleryFilterState) -> Unit,
    onApplySavedFilter: (LocalSavedGalleryFilter) -> Unit,
    onSaveCurrentFilter: (String, StashGalleryFilterState) -> Unit,
    onQuickSaveCurrentFilter: (String, StashGalleryFilterState) -> Unit,
    onDeleteSavedFilter: (LocalSavedGalleryFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var savedFilterSearchQuery by remember { mutableStateOf("") }
    val existingNames = savedFilters.map { it.name }
    val duplicateName = duplicateStashSavedFilterName(savedFilterName, existingNames)
    val uniqueName = uniqueStashSavedFilterName(savedFilterName, existingNames)
    val filteredNames = filterStashSavedFilterNames(existingNames, savedFilterSearchQuery).toSet()
    val visibleSavedFilters = if (savedFilterSearchQuery.isBlank()) {
        savedFilters
    } else {
        savedFilters.filter { it.name in filteredNames }
    }
    val visibleRecentFilters = recentFilters.take(5)
    val canSaveCurrent = !currentFilter.toGallerySavedFilterPayload().isEmpty

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
                if (visibleRecentFilters.isNotEmpty()) {
                    item(key = "recent_gallery_filters") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stashString(R.string.auto_kr_0351), style = MaterialTheme.typography.titleMedium)
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
                                                text = filter.galleryFilterSummaryLabel(),
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

                item(key = "current_gallery_filter") {
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
                            ) {
                                Text(stashString(R.string.auto_kr_0357))
                            }
                            TextButton(
                                onClick = {
                                    val suffix = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date())
                                    onQuickSaveCurrentFilter(currentFilter.quickSavedGalleryFilterName(suffix), currentFilter)
                                },
                                enabled = canSaveCurrent,
                            ) {
                                Text(stashString(R.string.auto_kr_0538))
                            }
                        }
                        HorizontalDivider()
                    }
                }

                item(key = "saved_gallery_search") {
                    OutlinedTextField(
                        value = savedFilterSearchQuery,
                        onValueChange = { savedFilterSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stashString(R.string.auto_kr_0359)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    )
                }

                if (visibleSavedFilters.isEmpty()) {
                    item(key = "saved_gallery_empty") {
                        Text(
                            text = if (savedFilters.isEmpty()) stashString(R.string.auto_kr_0360) else stashString(R.string.auto_kr_0361),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(visibleSavedFilters, key = { it.id }) { savedFilter ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onApplySavedFilter(savedFilter)
                                    onDismiss()
                                },
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start,
                                ) {
                                    Text(savedFilter.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = savedFilter.filterState.galleryFilterSummaryLabel(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteSavedFilter(savedFilter) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = stashString(R.string.auto_kr_0362, savedFilter.name))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stashString(R.string.auto_kr_0348))
                }
            }
        }
    }
}
