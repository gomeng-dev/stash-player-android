package gomeng.dev.stashplayer.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.network.StashSceneTagCandidate
import gomeng.dev.stashplayer.core.network.StashSceneTaggerSource
import gomeng.dev.stashplayer.core.ui.designsystem.StashBottomSheetContainer
import gomeng.dev.stashplayer.core.ui.designsystem.StashPrimaryButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashSecondaryButton
import gomeng.dev.stashplayer.core.ui.designsystem.StashSheetHeader
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSceneTagScanSheet(
    sources: List<StashSceneTaggerSource>,
    selectedSourceId: String?,
    candidates: List<StashSceneTagCandidate>,
    selectedCandidateKeys: Set<String>,
    loadingSources: Boolean,
    scanning: Boolean,
    applying: Boolean,
    statusText: String?,
    errorText: String?,
    onSelectSource: (String) -> Unit,
    onRunScan: () -> Unit,
    onToggleCandidate: (String) -> Unit,
    onApplySelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    val busy = loadingSources || scanning || applying
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        StashBottomSheetContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .heightIn(max = 560.dp)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StashSheetHeader(
                    title = stashString(R.string.player_tag_scan_sheet_title),
                    subtitle = stashString(R.string.player_tag_scan_sheet_subtitle),
                    contentDescription = stashString(R.string.player_tag_scan_sheet_title),
                )
                Text(
                    text = stashString(R.string.player_tag_scan_sheet_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TagScanSourceSection(
                    sources = sources,
                    selectedSourceId = selectedSourceId,
                    busy = busy,
                    loadingSources = loadingSources,
                    onSelectSource = onSelectSource,
                )
                statusText?.takeIf { it.isNotBlank() }?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                errorText?.takeIf { it.isNotBlank() }?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (candidates.isNotEmpty()) {
                    TagScanCandidateList(
                        candidates = candidates,
                        selectedCandidateKeys = selectedCandidateKeys,
                        enabled = !busy,
                        onToggleCandidate = onToggleCandidate,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StashSecondaryButton(
                        text = stashString(R.string.player_tag_scan_close),
                        onClick = onDismiss,
                    )
                    StashSecondaryButton(
                        text = stashString(R.string.player_tag_scan_run),
                        onClick = onRunScan,
                        enabled = !busy && selectedSourceId != null,
                    )
                    StashPrimaryButton(
                        text = stashString(R.string.player_tag_scan_apply),
                        onClick = onApplySelected,
                        enabled = !busy && selectedCandidateKeys.isNotEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TagScanSourceSection(
    sources: List<StashSceneTaggerSource>,
    selectedSourceId: String?,
    busy: Boolean,
    loadingSources: Boolean,
    onSelectSource: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stashString(R.string.player_tag_scan_source_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        when {
            loadingSources -> Text(
                text = stashString(R.string.player_tag_scan_loading_sources),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sources.isEmpty() -> Text(
                text = stashString(R.string.player_tag_scan_no_sources),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            else -> sources.forEach { source ->
                StashSecondaryButton(
                    text = source.displayName,
                    onClick = { onSelectSource(source.id) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                    selected = source.id == selectedSourceId,
                )
            }
        }
    }
}

@Composable
private fun TagScanCandidateList(
    candidates: List<StashSceneTagCandidate>,
    selectedCandidateKeys: Set<String>,
    enabled: Boolean,
    onToggleCandidate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        candidates.forEach { candidate ->
            val checked = candidate.key in selectedCandidateKeys
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onToggleCandidate(candidate.key) }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = { onToggleCandidate(candidate.key) },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidate.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val detail = if (candidate.createsLocalTag) {
                        stashString(R.string.player_tag_scan_new_tag)
                    } else {
                        candidate.sourceTitle.orEmpty()
                    }
                    detail.takeIf { it.isNotBlank() }?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
