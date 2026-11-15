package gomeng.dev.stashplayer.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.model.SceneBulkDeleteConfirmationState
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString
import java.io.IOException
import kotlinx.coroutines.flow.first

@Composable
fun SceneBulkDeleteConfirmationDialog(
    state: SceneBulkDeleteConfirmationState,
    onDeleteFileChange: (Boolean) -> Unit,
    onDeleteGeneratedChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    if (!state.isVisible) return

    val context = LocalContext.current.applicationContext
    val repository = remember(context) { StashSettingsRepository(context) }
    // Snapshot the opt-in once per delete request; later setting changes cannot confirm an open dialog.
    val skipConfirmation by produceState<Boolean?>(initialValue = null, repository) {
        value = try {
            repository.skipSceneDeleteConfirmation.first()
        } catch (_: IOException) {
            false
        }
    }
    if (skipConfirmation == null) return
    if (skipConfirmation == true) {
        val latestOnDelete by rememberUpdatedState(onDelete)
        LaunchedEffect(Unit) {
            if (state.canDelete) latestOnDelete()
        }
        return
    }

    AlertDialog(
        onDismissRequest = { if (!state.isDeleting) onCancel() },
        title = { Text(stashString(R.string.auto_kr_0289)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stashString(R.string.auto_kr_0290, state.selectedCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stashString(R.string.auto_kr_0291),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stashString(R.string.auto_kr_0292),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DeleteOptionRow(
                    checked = state.deleteOptions.deleteFile,
                    enabled = !state.isDeleting,
                    text = stashString(R.string.auto_kr_0293),
                    onCheckedChange = onDeleteFileChange,
                )
                DeleteOptionRow(
                    checked = state.deleteOptions.deleteGenerated,
                    enabled = !state.isDeleting,
                    text = stashString(R.string.auto_kr_0294),
                    onCheckedChange = onDeleteGeneratedChange,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !state.isDeleting) {
                Text(stashString(R.string.auto_kr_0296))
            }
        },
        confirmButton = {
            Button(onClick = onDelete, enabled = state.canDelete) {
                Text(if (state.isDeleting) stashString(R.string.auto_kr_0297) else stashString(R.string.auto_kr_0081))
            }
        },
    )
}

@Composable
private fun DeleteOptionRow(
    checked: Boolean,
    enabled: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Text(text)
    }
}
