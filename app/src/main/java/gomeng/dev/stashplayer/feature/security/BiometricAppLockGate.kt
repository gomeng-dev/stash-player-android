package gomeng.dev.stashplayer.feature.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.security.DeviceAuthenticationAvailability
import gomeng.dev.stashplayer.core.security.shouldRequireBiometricAppUnlock

@Composable
fun BiometricAppLockGate(
    hasSavedProfile: Boolean,
    appLockEnabled: Boolean,
    appSessionUnlocked: Boolean,
    onUnlocked: () -> Unit,
    content: @Composable () -> Unit,
) {
    val requiresUnlock = shouldRequireBiometricAppUnlock(
        hasSavedProfile = hasSavedProfile,
        appLockEnabled = appLockEnabled,
        appSessionUnlocked = appSessionUnlocked,
    )
    if (!requiresUnlock) {
        content()
        return
    }

    val availability = rememberDeviceAuthenticationAvailability()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var retryNonce by remember { mutableIntStateOf(0) }
    val authenticate = rememberDeviceAuthenticationLauncher(
        onAuthenticated = {
            statusMessage = null
            onUnlocked()
        },
        onAuthenticationError = { message ->
            statusMessage = message.toString()
        },
    )

    LaunchedEffect(availability, retryNonce, authenticate) {
        if (availability == DeviceAuthenticationAvailability.Available) {
            authenticate?.invoke()
        }
    }

    BiometricLockScreen(
        availability = availability,
        statusMessage = statusMessage,
        onUnlock = {
            if (availability == DeviceAuthenticationAvailability.Available) {
                retryNonce += 1
            }
        },
    )
}

@Composable
private fun BiometricLockScreen(
    availability: DeviceAuthenticationAvailability,
    statusMessage: String?,
    onUnlock: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.biometric_lock_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(R.string.biometric_lock_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    statusMessage ?: stringResource(deviceAuthenticationAvailabilityDescriptionRes(availability)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (availability == DeviceAuthenticationAvailability.Available) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Button(
                    onClick = onUnlock,
                    enabled = availability == DeviceAuthenticationAvailability.Available,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.biometric_unlock_button))
                }
            }
        }
    }
}
