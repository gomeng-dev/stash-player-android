package gomeng.dev.stashplayer.core.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.app.StashPlayerApp
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import java.io.IOException
import kotlinx.coroutines.launch

internal const val DEVELOPER_GOOGLE_PLAY_URL =
    "https://play.google.com/store/apps/details?id=io.gomeng.easydownloader"

@Composable
internal fun DeveloperSupportCard(
    modifier: Modifier = Modifier,
    dismissible: Boolean = false,
) {
    val context = LocalContext.current
    val dismissFailed = stringResource(R.string.developer_promo_dismiss_failed)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.developer_promo_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (dismissible) {
                    IconButton(onClick = {
                        val application = context.applicationContext as StashPlayerApp
                        // Keep the write alive when the user immediately leaves Home.
                        application.applicationScope.launch {
                            try {
                                StashSettingsRepository(application).dismissHomeDeveloperPromo()
                            } catch (_: IOException) {
                                Toast.makeText(application, dismissFailed, Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.Close, stringResource(R.string.developer_promo_dismiss))
                    }
                }
            }
            Text(stringResource(R.string.developer_promo_slogan), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.developer_promo_disclosure), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {
                openSupportUrl(context, DEVELOPER_GOOGLE_PLAY_URL)?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }) {
                Text(stringResource(R.string.developer_promo_google_play))
            }
        }
    }
}

internal fun openSupportUrl(context: Context, url: String): String? {
    return runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        null
    }.getOrElse {
        context.getString(R.string.settings_support_link_open_failed)
    }
}
