package gomeng.dev.stashplayer.core.security

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.FragmentActivity

fun resolveDeviceAuthenticationAvailability(context: Context): DeviceAuthenticationAvailability {
    val biometricManager = BiometricManager.from(context)
    val strongBiometricStatus = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
    val keyguardManager = context.getSystemService(KeyguardManager::class.java)
    val deviceCredentialAvailable = keyguardManager?.isDeviceSecure == true
    if (strongBiometricStatus == BiometricManager.BIOMETRIC_SUCCESS || deviceCredentialAvailable) {
        return DeviceAuthenticationAvailability.Available
    }
    return when (strongBiometricStatus) {
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> DeviceAuthenticationAvailability.NoDeviceCredential
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> DeviceAuthenticationAvailability.Available
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> DeviceAuthenticationAvailability.NoDeviceCredential
                    else -> DeviceAuthenticationAvailability.NoHardware
                }
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
