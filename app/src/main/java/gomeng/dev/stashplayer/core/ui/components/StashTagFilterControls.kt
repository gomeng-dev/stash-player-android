package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashSelectedEntity
import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.core.model.StashVideoFilterCategory
import gomeng.dev.stashplayer.core.model.StashVideoFilterEditTarget
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.filterSurfaceDescriptor
import gomeng.dev.stashplayer.core.model.orderStashTagOptionsForDraft
import gomeng.dev.stashplayer.core.model.stashTagFilterApplyContentDescription
import gomeng.dev.stashplayer.core.model.stashTagFilterSheetLayoutPolicy
import gomeng.dev.stashplayer.core.model.stashTagFilterStickyActionSummary
import gomeng.dev.stashplayer.core.model.stashTagFilterTitleRowActions
import gomeng.dev.stashplayer.core.model.stashTagDraftSelectedCountLabel
import gomeng.dev.stashplayer.core.model.toTagFilterDraft
import gomeng.dev.stashplayer.core.ui.designsystem.StashActionPill
import gomeng.dev.stashplayer.core.ui.designsystem.StashActiveFilterChipAction
import gomeng.dev.stashplayer.core.ui.designsystem.StashSpacing
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.core.ui.designsystem.stashActiveFilterChipActionModel
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashTagFilterButton(
    enabled: Boolean,
    videoFilter: StashVideoFilterState,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        val suffix = videoFilter.tags.takeIf { it.isNotEmpty() }?.let { " ${it.size}" }.orEmpty()
        Text(stashString(R.string.auto_kr_0364, suffix))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StashActiveVideoFilterChipsRow(
    videoFilter: StashVideoFilterState,
    horizontalPadding: Dp,
    onTagClick: () -> Unit,
    onClearTags: () -> Unit,
    onDateDurationPlaybackClick: () -> Unit = {},
    onClearDateRange: () -> Unit = {},
    onClearDurationRange: () -> Unit = {},
    onClearPlaybackState: () -> Unit = {},
    onRatingMediaFormatClick: () -> Unit = {},
    onClearRatingRange: () -> Unit = {},
    onClearMediaFormat: () -> Unit = {},
    onLocalLibraryClick: () -> Unit = {},
    onClearLocalFavoriteOnly: () -> Unit = {},
    onClearSavedFilter: () -> Unit = {},
    onRandomShuffleClick: () -> Unit = {},
    onClearRandomShuffle: () -> Unit = {},
) {
    if (videoFilter.isEmpty) return

    fun onEditTargetClick(target: StashVideoFilterEditTarget) {
        when (target) {
            StashVideoFilterEditTarget.Tags -> onTagClick()
            StashVideoFilterEditTarget.DateDurationPlayback -> onDateDurationPlaybackClick()
            StashVideoFilterEditTarget.RatingMedia -> onRatingMediaFormatClick()
            StashVideoFilterEditTarget.LocalLibrary -> onLocalLibraryClick()
            StashVideoFilterEditTarget.RandomShuffle -> onRandomShuffleClick()
        }
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
        verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
    ) {
        videoFilter.activeFilterChips().forEach { chip ->
            when (chip.category) {
                StashVideoFilterCategory.Tag -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0365),
                        onClear = onClearTags,
                    )
                }
                StashVideoFilterCategory.DateRange -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0305),
                        onClear = onClearDateRange,
                    )
                }
                StashVideoFilterCategory.DurationRange -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0309),
                        onClear = onClearDurationRange,
                    )
                }
                StashVideoFilterCategory.PlaybackState -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0315),
                        onClear = onClearPlaybackState,
                    )
                }
                StashVideoFilterCategory.Rating -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0333),
                        onClear = onClearRatingRange,
                    )
                }
                StashVideoFilterCategory.MediaFormat -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0336),
                        onClear = onClearMediaFormat,
                    )
                }
                StashVideoFilterCategory.LocalFavorite -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0229),
                        onClear = onClearLocalFavoriteOnly,
                    )
                }
                StashVideoFilterCategory.SavedFilter -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0366),
                        onClear = onClearSavedFilter,
                    )
                }
                StashVideoFilterCategory.RandomShuffle -> {
                    ActiveFilterChipWithClear(
                        label = chip.label,
                        onClick = { chip.category.filterSurfaceDescriptor().editTarget?.let(::onEditTargetClick) },
                        clearLabel = stashString(R.string.auto_kr_0367),
                        onClear = onClearRandomShuffle,
                    )
                }
                else -> {
                    StashVisibilityFilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(chip.label) },
                        enabled = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterChipWithClear(
    label: String,
    onClick: () -> Unit,
    clearLabel: String,
    onClear: () -> Unit,
) {
    val editModel = stashActiveFilterChipActionModel(label, StashActiveFilterChipAction.Edit)
    val clearModel = stashActiveFilterChipActionModel(label, StashActiveFilterChipAction.Clear).copy(label = clearLabel)

    StashActionPill(
        label = editModel.label,
        onClick = onClick,
        selected = true,
        contentDescription = editModel.contentDescription,
    )
    StashActionPill(
        label = clearModel.label,
        onClick = onClear,
        contentDescription = clearModel.contentDescription,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StashTagFilterSheet(
    videoFilter: StashVideoFilterState,
    tagQuery: String,
    tagOptions: List<StashSelectedTag>,
    isLoading: Boolean,
    error: String?,
    onTagQueryChange: (String) -> Unit,
    onApplyTags: (List<StashSelectedTag>) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    var tagDraft by remember(videoFilter.tags) { mutableStateOf(videoFilter.toTagFilterDraft()) }
    val selectedIds = tagDraft.tags.map { it.id }.toSet()
    val orderedTagOptions = remember(tagDraft.tags, tagOptions) {
        orderStashTagOptionsForDraft(
            selectedTags = tagDraft.tags,
            tagOptions = tagOptions,
        )
    }
    val layoutPolicy = stashTagFilterSheetLayoutPolicy()
    val titleRowActions = stashTagFilterTitleRowActions()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = layoutPolicy.contentBottomPaddingDp.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stashString(R.string.auto_kr_0368), style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = stashTagDraftSelectedCountLabel(tagDraft.tags.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = { tagDraft = tagDraft.reset() },
                                enabled = tagDraft.tags.isNotEmpty(),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription = titleRowActions.clearContentDescription
                                    },
                            ) { Text(titleRowActions.clearLabel) }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.semantics {
                                    contentDescription = titleRowActions.closeContentDescription
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                    Text(
                        text = stashString(R.string.auto_kr_0369),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = tagQuery,
                        onValueChange = onTagQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stashString(R.string.auto_kr_0370)) },
                        placeholder = { Text(stashString(R.string.auto_kr_0371)) },
                    )
                    if (tagDraft.tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            tagDraft.tags.forEach { tag ->
                                StashVisibilityFilterChip(
                                    selected = true,
                                    onClick = { tagDraft = tagDraft.toggle(tag) },
                                    label = {
                                        Text(
                                            text = "${tag.name} ×",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                when {
                    isLoading -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                    error != null -> Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stashString(R.string.auto_kr_0372), color = MaterialTheme.colorScheme.error)
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = onRetry) { Text(stashString(R.string.auto_kr_0031)) }
                    }
                    orderedTagOptions.isEmpty() -> Text(
                        text = stashString(R.string.auto_kr_0373),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 116.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 380.dp)
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(orderedTagOptions, key = { it.id }) { tag ->
                            StashVisibilityFilterChip(
                                selected = selectedIds.contains(tag.id),
                                onClick = { tagDraft = tagDraft.toggle(tag) },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        text = tag.name,
                                        maxLines = layoutPolicy.optionChipMaxLines,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
            }

            StashTagFilterStickyActionBar(
                selectedCount = tagDraft.tags.size,
                canReset = tagDraft.tags.isNotEmpty(),
                onReset = { tagDraft = tagDraft.reset() },
                onDismiss = onDismiss,
                onApply = {
                    onApplyTags(tagDraft.tags)
                    onDismiss()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StashEntityFilterSheet(
    title: String,
    searchLabel: String,
    selectedEntities: List<StashSelectedEntity>,
    availableEntities: List<StashSelectedEntity>,
    searchQuery: String,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleEntity: (StashSelectedEntity) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    allowTypedOption: Boolean = false,
) {
    val selectedIds = selectedEntities.map { it.id }.toSet()
    val typedOption = searchQuery.trim().takeIf { allowTypedOption && it.isNotBlank() }?.let { value ->
        StashSelectedEntity(id = value, name = value)
    }
    val options = buildList {
        if (typedOption != null && availableEntities.none { it.id == typedOption.id }) add(typedOption)
        addAll(availableEntities)
    }.distinctBy { it.id }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = StashSpacing.CardPadding, vertical = StashSpacing.CardGap),
            verticalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = stashString(R.string.auto_kr_0077))
                }
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(searchLabel) },
            )
            if (selectedEntities.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                ) {
                    selectedEntities.forEach { entity ->
                        StashVisibilityFilterChip(
                            selected = true,
                            onClick = { onToggleEntity(entity) },
                            label = { Text("${entity.name} ×", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    contentPadding = PaddingValues(bottom = StashSpacing.SectionGap),
                    horizontalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                    verticalArrangement = Arrangement.spacedBy(StashSpacing.ChipGap),
                ) {
                    items(options, key = { it.id }) { entity ->
                        StashVisibilityFilterChip(
                            selected = entity.id in selectedIds,
                            onClick = { onToggleEntity(entity) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(entity.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(StashSpacing.CardGap),
            ) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text(stashString(R.string.auto_kr_0078))
                }
                Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                    Text(stashString(R.string.auto_kr_0079))
                }
            }
        }
    }
}

@Composable
private fun StashTagFilterStickyActionBar(
    selectedCount: Int,
    canReset: Boolean,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stashTagFilterStickyActionSummary(selectedCount),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onReset,
                    enabled = canReset,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stashString(R.string.auto_kr_0374)) }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stashString(R.string.auto_kr_0296)) }
                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .semantics { contentDescription = stashTagFilterApplyContentDescription() },
                ) { Text(stashString(R.string.auto_kr_0316)) }
            }
        }
    }
}
