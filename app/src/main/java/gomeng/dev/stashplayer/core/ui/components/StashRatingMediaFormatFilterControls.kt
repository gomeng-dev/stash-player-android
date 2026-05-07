package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.StashMediaFormatFilter
import gomeng.dev.stashplayer.core.model.StashRatingFilterSliderLayout
import gomeng.dev.stashplayer.core.model.StashRatingFilterSliderSelection
import gomeng.dev.stashplayer.core.model.StashVideoFileType
import gomeng.dev.stashplayer.core.model.StashVideoFilterState
import gomeng.dev.stashplayer.core.model.buildStashRatingRangeFromRatingStepSelection
import gomeng.dev.stashplayer.core.model.normalizeStashRatingFilterSliderSelection
import gomeng.dev.stashplayer.core.model.resolveStashRatingFilterSliderLayout
import gomeng.dev.stashplayer.core.model.stashResolutionFilterOptions
import gomeng.dev.stashplayer.core.model.toStashRatingStepFromRating100
import gomeng.dev.stashplayer.core.model.toStashRatingStepLabel
import gomeng.dev.stashplayer.core.model.toggleStashVideoFileType
import gomeng.dev.stashplayer.core.ui.designsystem.StashVisibilityFilterChip
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun StashRatingMediaFormatFilterButton(
    enabled: Boolean,
    videoFilter: StashVideoFilterState,
    onClick: () -> Unit,
) {
    val count = buildList {
        if (videoFilter.ratingRange?.isEmpty == false) add(Unit)
        if (!videoFilter.mediaFormat.isEmpty) add(Unit)
    }.size
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        val suffix = count.takeIf { it > 0 }?.let { " $it" }.orEmpty()
        Text(stashString(R.string.auto_kr_0330, suffix))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StashRatingMediaFormatFilterSheet(
    videoFilter: StashVideoFilterState,
    onApply: (StashVideoFilterState) -> Unit,
    onClearRatingRange: () -> Unit,
    onClearMediaFormat: () -> Unit,
    onDismiss: () -> Unit,
) {
    var ratingSelection by remember(videoFilter.ratingRange) {
        mutableStateOf(
            normalizeStashRatingFilterSliderSelection(
                minStep = videoFilter.ratingRange?.min?.toStashRatingStepFromRating100()?.takeIf { it > 0 },
                maxStep = videoFilter.ratingRange?.max?.toStashRatingStepFromRating100()?.takeIf { it > 0 },
            ),
        )
    }
    var selectedResolution by remember(videoFilter.mediaFormat.resolution) {
        mutableStateOf(videoFilter.mediaFormat.resolution)
    }
    var selectedFileTypes by remember(videoFilter.mediaFormat.fileTypes) {
        mutableStateOf(videoFilter.mediaFormat.fileTypes)
    }

    fun applyFilters() {
        onApply(
            videoFilter.copy(
                ratingRange = buildStashRatingRangeFromRatingStepSelection(
                    minStep = ratingSelection.minStep,
                    maxStep = ratingSelection.maxStep,
                ),
                mediaFormat = StashMediaFormatFilter(
                    resolution = selectedResolution,
                    fileTypes = selectedFileTypes,
                ),
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
            Text(stashString(R.string.auto_kr_0331), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stashString(R.string.auto_kr_0332),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stashString(R.string.auto_kr_0234), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        ratingSelection = StashRatingFilterSliderSelection()
                        onClearRatingRange()
                    }) { Text(stashString(R.string.auto_kr_0333)) }
                }
                Text(
                    text = stashString(R.string.auto_kr_0334),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RatingFilterSliderPair(
                    selection = ratingSelection,
                    onMinStepChange = { step ->
                        ratingSelection = normalizeStashRatingFilterSliderSelection(
                            minStep = step.takeIf { it > 0 },
                            maxStep = ratingSelection.maxStep,
                        )
                    },
                    onMaxStepChange = { step ->
                        ratingSelection = normalizeStashRatingFilterSliderSelection(
                            minStep = ratingSelection.minStep,
                            maxStep = step.takeIf { it > 0 },
                        )
                    },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stashString(R.string.auto_kr_0335), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = {
                        selectedResolution = null
                        selectedFileTypes = emptyList()
                        onClearMediaFormat()
                    }) { Text(stashString(R.string.auto_kr_0336)) }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    stashResolutionFilterOptions().forEach { option ->
                        StashVisibilityFilterChip(
                            selected = selectedResolution == option.resolution,
                            onClick = { selectedResolution = option.resolution },
                            label = { Text(option.label) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stashString(R.string.auto_kr_0337), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StashVideoFileType.entries.forEach { fileType ->
                        StashVisibilityFilterChip(
                            selected = selectedFileTypes.contains(fileType),
                            onClick = { selectedFileTypes = toggleStashVideoFileType(selectedFileTypes, fileType) },
                            label = { Text(fileType.label) },
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

@Composable
private fun RatingFilterSliderPair(
    selection: StashRatingFilterSliderSelection,
    onMinStepChange: (Int) -> Unit,
    onMaxStepChange: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        when (resolveStashRatingFilterSliderLayout(maxWidth.value)) {
            StashRatingFilterSliderLayout.OneRow -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RatingFilterSliderColumn(
                    title = stashString(R.string.auto_kr_0338),
                    ratingStep = selection.minStep ?: 0,
                    emptyLabel = stashString(R.string.auto_kr_0339),
                    valueSuffix = stashString(R.string.auto_kr_0340),
                    onSelectRatingStep = onMinStepChange,
                    modifier = Modifier.weight(1f),
                )
                RatingFilterSliderColumn(
                    title = stashString(R.string.auto_kr_0341),
                    ratingStep = selection.maxStep ?: 0,
                    emptyLabel = stashString(R.string.auto_kr_0339),
                    valueSuffix = stashString(R.string.auto_kr_0342),
                    onSelectRatingStep = onMaxStepChange,
                    modifier = Modifier.weight(1f),
                )
            }

            StashRatingFilterSliderLayout.TwoRows -> Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RatingFilterSliderColumn(
                    title = stashString(R.string.auto_kr_0338),
                    ratingStep = selection.minStep ?: 0,
                    emptyLabel = stashString(R.string.auto_kr_0339),
                    valueSuffix = stashString(R.string.auto_kr_0340),
                    onSelectRatingStep = onMinStepChange,
                )
                RatingFilterSliderColumn(
                    title = stashString(R.string.auto_kr_0341),
                    ratingStep = selection.maxStep ?: 0,
                    emptyLabel = stashString(R.string.auto_kr_0339),
                    valueSuffix = stashString(R.string.auto_kr_0342),
                    onSelectRatingStep = onMaxStepChange,
                )
            }
        }
    }
}

@Composable
private fun RatingFilterSliderColumn(
    title: String,
    ratingStep: Int,
    emptyLabel: String,
    valueSuffix: String,
    onSelectRatingStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(min = 240.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (ratingStep > 0) "${(ratingStep * 10).toStashRatingStepLabel()} $valueSuffix" else emptyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StashStarRatingSlider(
            ratingStep = ratingStep,
            contentDescriptionPrefix = stashString(R.string.auto_kr_0343, title),
            enabled = true,
            onSelectRatingStep = onSelectRatingStep,
        )
    }
}
