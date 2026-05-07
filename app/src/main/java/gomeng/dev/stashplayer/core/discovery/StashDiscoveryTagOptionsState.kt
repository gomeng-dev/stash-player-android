package gomeng.dev.stashplayer.core.discovery

import gomeng.dev.stashplayer.core.model.StashSelectedTag
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

data class StashDiscoveryTagOptionsState(
    val query: String = "",
    val options: List<StashSelectedTag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val requestSerial: Long = 0L,
)

data class StashDiscoveryTagOptionsRequest(
    val serial: Long,
    val profileRevision: Int,
)

data class StashDiscoveryTagOptionsRequestStart(
    val state: StashDiscoveryTagOptionsState,
    val request: StashDiscoveryTagOptionsRequest,
)

fun StashDiscoveryTagOptionsState.withTagOptionsQuery(query: String): StashDiscoveryTagOptionsState = copy(
    query = query,
)

fun StashDiscoveryTagOptionsState.resetTagOptionsQuery(): StashDiscoveryTagOptionsState = copy(
    query = "",
    options = emptyList(),
    isLoading = false,
    error = null,
)

fun StashDiscoveryTagOptionsState.startTagOptionsRequest(
    query: String,
    profileRevision: Int,
): StashDiscoveryTagOptionsRequestStart {
    val nextSerial = requestSerial + 1L
    return StashDiscoveryTagOptionsRequestStart(
        state = copy(
            query = query,
            isLoading = true,
            error = null,
            requestSerial = nextSerial,
        ),
        request = StashDiscoveryTagOptionsRequest(
            serial = nextSerial,
            profileRevision = profileRevision,
        ),
    )
}

fun StashDiscoveryTagOptionsState.applyTagOptionsSuccess(
    request: StashDiscoveryTagOptionsRequest,
    currentProfileRevision: Int,
    options: List<StashSelectedTag>,
): StashDiscoveryTagOptionsState = if (acceptsTagOptionsResponse(request, currentProfileRevision)) {
    copy(
        options = options,
        isLoading = false,
        error = null,
    )
} else {
    this
}

fun StashDiscoveryTagOptionsState.applyTagOptionsFailure(
    request: StashDiscoveryTagOptionsRequest,
    currentProfileRevision: Int,
    errorMessage: String?,
): StashDiscoveryTagOptionsState = if (acceptsTagOptionsResponse(request, currentProfileRevision)) {
    copy(
        isLoading = false,
        error = errorMessage ?: stashString(R.string.auto_kr_0006),
    )
} else {
    this
}

private fun StashDiscoveryTagOptionsState.acceptsTagOptionsResponse(
    request: StashDiscoveryTagOptionsRequest,
    currentProfileRevision: Int,
): Boolean = requestSerial == request.serial && request.profileRevision == currentProfileRevision
