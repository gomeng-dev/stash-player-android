package gomeng.dev.stashplayer.core.security

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.FragmentActivity

fun resolveDeviceAuthenticationAvailability(context: Context): DeviceAuthenticationAvailability {
    val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate(BIOMETRIC_WEAK)
    val keyguardManager = context.getSystemService(KeyguardManager::class.java)
    val deviceCredentialAvailable = keyguardManager?.isDeviceSecure == true
    val deviceCredentialStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        biometricManager.canAuthenticate(DEVICE_CREDENTIAL)
    } else {
        null
    }
    return resolveDeviceAuthenticationAvailabilityFromSignals(
        biometricStatus = biometricStatus,
        deviceCredentialStatus = deviceCredentialStatus,
        keyguardDeviceSecure = deviceCredentialAvailable,
    )
}

fun resolveDeviceAuthenticationAvailabilityFromSignals(
    biometricStatus: Int,
    deviceCredentialStatus: Int?,
    keyguardDeviceSecure: Boolean,
): DeviceAuthenticationAvailability {
    if (
        biometricStatus == BiometricManager.BIOMETRIC_SUCCESS ||
        deviceCredentialStatus == BiometricManager.BIOMETRIC_SUCCESS ||
        keyguardDeviceSecure
    ) {
        return DeviceAuthenticationAvailability.Available
    }

    return when (biometricStatus) {
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> DeviceAuthenticationAvailability.NoDeviceCredential
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            if (deviceCredentialStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                DeviceAuthenticationAvailability.NoDeviceCredential
            } else {
                DeviceAuthenticationAvailability.NoHardware
            }
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> DeviceAuthenticationAvailability.HardwareUnavailable
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> DeviceAuthenticationAvailability.SecurityUpdateRequired
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> DeviceAuthenticationAvailability.Unsupported
        else -> DeviceAuthenticationAvailability.Unknown
    }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return current as? FragmentActivity
}
