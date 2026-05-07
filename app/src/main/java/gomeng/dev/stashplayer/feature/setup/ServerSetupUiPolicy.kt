package gomeng.dev.stashplayer.feature.setup

internal enum class SetupAuthMode {
    ApiKey,
    Password,
}

internal fun shouldUseScrollableSetupContent(authMode: SetupAuthMode): Boolean = when (authMode) {
    SetupAuthMode.ApiKey,
    SetupAuthMode.Password,
    -> true
}

internal fun shouldAllowUntestedSetupSave(authMode: SetupAuthMode): Boolean = authMode == SetupAuthMode.ApiKey
