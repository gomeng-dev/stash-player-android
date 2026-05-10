package gomeng.dev.stashplayer.feature.security

import android.os.Build
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.security.DeviceAuthenticationAvailability
import gomeng.dev.stashplayer.core.security.findFragmentActivity
import gomeng.dev.stashplayer.core.security.resolveDeviceAuthenticationAvailability

@Composable
fun rememberDeviceAuthenticationAvailability(): DeviceAuthenticationAvailability {
    val context = LocalContext.current
    return remember(context) { resolveDeviceAuthenticationAvailability(context) }
}

@Composable
fun rememberDeviceAuthenticationLauncher(
    onAuthenticated: () -> Unit,
    onAuthenticationError: (CharSequence) -> Unit,
): (() -> Unit)? {
    val context = LocalContext.current
    val viewContext = LocalView.current.context
    val contextActivity = remember(context) { context.findFragmentActivity() }
    val viewActivity = remember(viewContext) { viewContext.findFragmentActivity() }
    val activitySource = remember(contextActivity, viewActivity) {
        resolveDeviceAuthenticationActivitySource(
            localContextHasFragmentActivity = contextActivity != null,
            viewContextHasFragmentActivity = viewActivity != null,
        )
    }
    val activity = when (activitySource) {
        DeviceAuthenticationActivitySource.LocalContext -> contextActivity
        DeviceAuthenticationActivitySource.ViewContext -> viewActivity
        DeviceAuthenticationActivitySource.Missing -> null
    }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val onAuthenticatedState by rememberUpdatedState(onAuthenticated)
    val onAuthenticationErrorState by rememberUpdatedState(onAuthenticationError)
    val promptInfo = remember(context) { buildDeviceAuthenticationPromptInfo(context.getString(R.string.biometric_prompt_title)) }
    val prompt = remember(activity, executor) {
        activity?.let {
            BiometricPrompt(
                it,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onAuthenticatedState()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onAuthenticationErrorState(errString)
                    }

                    override fun onAuthenticationFailed() {
                        onAuthenticationErrorState(context.getString(R.string.biometric_prompt_failed))
                    }
                },
            )
        }
    }
    return remember(prompt, promptInfo) {
        prompt?.let {
            { it.authenticate(promptInfo) }
        }
    }
}

@StringRes
fun deviceAuthenticationAvailabilityDescriptionRes(
    availability: DeviceAuthenticationAvailability,
): Int = when (availability) {
    DeviceAuthenticationAvailability.Available -> R.string.biometric_availability_available
    DeviceAuthenticationAvailability.NoneEnrolled -> R.string.biometric_availability_none_enrolled
    DeviceAuthenticationAvailability.NoDeviceCredential -> R.string.biometric_availability_no_device_credential
    DeviceAuthenticationAvailability.NoHardware -> R.string.biometric_availability_no_hardware
    DeviceAuthenticationAvailability.HardwareUnavailable -> R.string.biometric_availability_hardware_unavailable
    DeviceAuthenticationAvailability.SecurityUpdateRequired -> R.string.biometric_availability_security_update_required
    DeviceAuthenticationAvailability.Unsupported -> R.string.biometric_availability_unsupported
    DeviceAuthenticationAvailability.Unknown -> R.string.biometric_availability_unknown
}

@Suppress("DEPRECATION")
private fun buildDeviceAuthenticationPromptInfo(title: String): BiometricPrompt.PromptInfo {
    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        builder.setAllowedAuthenticators(deviceAuthenticationPromptAuthenticators())
    } else {
        builder.setDeviceCredentialAllowed(true)
    }
    return builder.build()
}

fun deviceAuthenticationPromptAuthenticators(): Int = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

internal enum class DeviceAuthenticationActivitySource {
    LocalContext,
    ViewContext,
    Missing,
}

internal fun resolveDeviceAuthenticationActivitySource(
    localContextHasFragmentActivity: Boolean,
    viewContextHasFragmentActivity: Boolean,
): DeviceAuthenticationActivitySource = when {
    localContextHasFragmentActivity -> DeviceAuthenticationActivitySource.LocalContext
    viewContextHasFragmentActivity -> DeviceAuthenticationActivitySource.ViewContext
    else -> DeviceAuthenticationActivitySource.Missing
}
