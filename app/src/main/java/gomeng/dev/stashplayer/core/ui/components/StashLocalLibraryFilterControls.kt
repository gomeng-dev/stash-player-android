package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.local.LocalSavedVideoFilter
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.toSavedFilterPayload
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashLocalLibraryFilterButton(
    enabled: Boolean,
    videoFilter: StashVideoFilterState,
    savedFilterCount: Int,
    onClick: () -> Unit,
) {
    val suffix = buildString {
        if (videoFilter.localFavoriteOnly) append(" ★")
        if (videoFilter.savedFilter != null) append(stashString(R.string.auto_kr_0317))
        if (savedFilterCount > 0) append(" $savedFilterCount")
    }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(stashString(R.string.auto_kr_0318, suffix))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StashLocalLibraryFilterSheet(
    videoFilter: StashVideoFilterState,
    savedFilters: List<LocalSavedVideoFilter>,
    savedFilterName: String,
    onSavedFilterNameChange: (String) -> Unit,
    onToggleLocalFavoriteOnly: () -> Unit,
    onSaveCurrentFilter: () -> Unit,
    onApplySavedFilter: (LocalSavedVideoFilter) -> Unit,
    onDeleteSavedFilter: (LocalSavedVideoFilter) -> Unit,
    onClearSavedFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    val payload = videoFilter.toSavedFilterPayload()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stashString(R.string.auto_kr_0319), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stashString(R.string.auto_kr_0320),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stashString(R.string.auto_kr_0321), style = MaterialTheme.typography.titleMedium)
                StashVisibilityFilterChip(
                    selected = videoFilter.localFavoriteOnly,
                    onClick = onToggleLocalFavoriteOnly,
                    label = { Text(stashString(R.string.auto_kr_0322)) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stashString(R.string.auto_kr_0323), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = savedFilterName,
                    onValueChange = onSavedFilterNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stashString(R.string.auto_kr_0324)) },
                    placeholder = { Text(stashString(R.string.auto_kr_0325)) },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onSaveCurrentFilter,
                        enabled = savedFilterName.isNotBlank() && !payload.isEmpty,
                    ) {
                        Text(stashString(R.string.auto_kr_0065))
                    }
                    if (videoFilter.savedFilter != null) {
                        TextButton(onClick = onClearSavedFilter) { Text(stashString(R.string.auto_kr_0326)) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stashString(R.string.auto_kr_0327), style = MaterialTheme.typography.titleMedium)
                if (savedFilters.isEmpty()) {
                    Text(
                        text = stashString(R.string.auto_kr_0328),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(savedFilters, key = { it.id }) { filter ->
                            SavedFilterRow(
                                filter = filter,
                                isActive = videoFilter.savedFilter?.id == filter.id,
                                onApply = { onApplySavedFilter(filter) },
                                onDelete = { onDeleteSavedFilter(filter) },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun SavedFilterRow(
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
                    text = filter.filterState.activeFilterChips().joinToString(" · ") { it.label }.ifBlank { stashString(R.string.auto_kr_0130) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onApply) { Text(stashString(R.string.auto_kr_0316)) }
                TextButton(onClick = onDelete) { Text(stashString(R.string.auto_kr_0081)) }
            }
        }
    }
}
