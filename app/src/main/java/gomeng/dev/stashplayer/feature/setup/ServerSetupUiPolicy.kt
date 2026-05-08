package gomeng.dev.stashplayer.feature.setup

internal enum class SetupAuthMode {
    LinkOnly,
    ApiKey,
    Password,
}

internal fun shouldUseScrollableSetupContent(authMode: SetupAuthMode): Boolean = when (authMode) {
    SetupAuthMode.LinkOnly,
    SetupAuthMode.ApiKey,
    SetupAuthMode.Password,
    -> true
}

internal fun shouldAllowUntestedSetupSave(authMode: SetupAuthMode): Boolean = when (authMode) {
    SetupAuthMode.LinkOnly,
    SetupAuthMode.ApiKey -> true
    SetupAuthMode.Password -> false
}
