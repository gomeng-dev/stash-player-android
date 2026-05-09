package gomeng.dev.stashplayer.core.security

enum class RecentsPrivacyProtection {
    None,
    RecentsScreenshotDisabled,
    SecureWindowFlag,
}

fun resolveRecentsPrivacyProtection(
    hideRecentAppsPreview: Boolean,
    sdkInt: Int,
): RecentsPrivacyProtection = when {
    !hideRecentAppsPreview -> RecentsPrivacyProtection.None
    sdkInt >= 33 -> RecentsPrivacyProtection.RecentsScreenshotDisabled
    else -> RecentsPrivacyProtection.SecureWindowFlag
}
