package gomeng.dev.stashplayer.core.security

enum class DeviceAuthenticationAvailability {
    Available,
    NoneEnrolled,
    NoDeviceCredential,
    NoHardware,
    HardwareUnavailable,
    SecurityUpdateRequired,
    Unsupported,
    Unknown,
}

fun shouldRequireBiometricAppUnlock(
    hasSavedProfile: Boolean,
    appLockEnabled: Boolean,
    appSessionUnlocked: Boolean,
): Boolean = hasSavedProfile && appLockEnabled && !appSessionUnlocked

fun shouldShowBiometricOnboardingPrompt(
    connectionSaved: Boolean,
    appLockEnabled: Boolean,
    deviceAuthenticationAvailability: DeviceAuthenticationAvailability,
): Boolean = connectionSaved && !appLockEnabled && deviceAuthenticationAvailability == DeviceAuthenticationAvailability.Available
