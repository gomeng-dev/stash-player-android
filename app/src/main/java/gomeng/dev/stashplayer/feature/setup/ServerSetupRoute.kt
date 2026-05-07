package gomeng.dev.stashplayer.feature.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import gomeng.dev.stashplayer.core.debug.StashDebugLogBuffer
import gomeng.dev.stashplayer.core.network.StashCredentialTransportDecision
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.StashLoginClient
import gomeng.dev.stashplayer.core.network.StashServerAuthMode
import gomeng.dev.stashplayer.core.network.StashServerProfile
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.canAttemptStashCredentialTransport
import gomeng.dev.stashplayer.core.network.resolveStashCredentialTransportDecision
import gomeng.dev.stashplayer.core.ui.StashUxCopy
import kotlinx.coroutines.launch
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.i18n.stashString

@Composable
fun ServerSetupRoute(
    isFoldLikeLayout: Boolean,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val savedProfile by repository.serverProfile.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var serverName by remember { mutableStateOf("Home") }
    var serverUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(SetupAuthMode.ApiKey) }
    var allowInsecureLocalApiKey by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(savedProfile) {
        savedProfile?.let {
            serverName = it.name
            serverUrl = it.baseUrl
            apiKey = it.apiKey
            allowInsecureLocalApiKey = it.allowInsecureLocalApiKey
            authMode = if (it.authMode == StashServerAuthMode.SessionCookie) SetupAuthMode.Password else SetupAuthMode.ApiKey
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (shouldUseScrollableSetupContent(authMode)) Modifier.verticalScroll(scrollState) else Modifier)
            .imePadding()
            .padding(if (isFoldLikeLayout) 32.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Stash Player", style = MaterialTheme.typography.headlineMedium)
        Text(StashUxCopy.setupIntro)
        Text(stashString(R.string.auto_kr_0529))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text(stashString(R.string.auto_kr_0530)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text(stashString(R.string.auto_kr_0531)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                SetupAuthModeRow(
                    selected = authMode == SetupAuthMode.ApiKey,
                    label = R.string.setup_auth_mode_api_key_label,
                    description = R.string.setup_auth_mode_api_key_description,
                    onClick = { authMode = SetupAuthMode.ApiKey },
                )
                SetupAuthModeRow(
                    selected = authMode == SetupAuthMode.Password,
                    label = R.string.setup_auth_mode_password_label,
                    description = R.string.setup_auth_mode_password_description,
                    onClick = { authMode = SetupAuthMode.Password },
                )
                if (authMode == SetupAuthMode.ApiKey) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text(stashString(R.string.settings_api_key_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    val decision = resolveStashCredentialTransportDecision(
                        baseUrl = serverUrl,
                        authMode = StashServerAuthMode.ApiKey,
                        allowInsecureLocalApiKey = allowInsecureLocalApiKey,
                    )
                    if (decision == StashCredentialTransportDecision.InsecureNeedsExplicitLocalConfirmation ||
                        decision == StashCredentialTransportDecision.InsecureLocalAllowed
                    ) {
                        SetupInsecureLocalHttpAcknowledgement(
                            checked = allowInsecureLocalApiKey,
                            onCheckedChange = { allowInsecureLocalApiKey = it },
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stashString(R.string.setup_username_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stashString(R.string.setup_password_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Text(
                        stashString(R.string.setup_password_https_only_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val decision = resolveStashCredentialTransportDecision(
                        baseUrl = serverUrl,
                        authMode = StashServerAuthMode.SessionCookie,
                        allowInsecureLocalApiKey = allowInsecureLocalApiKey,
                    )
                    if (decision == StashCredentialTransportDecision.InsecureNeedsExplicitLocalConfirmation ||
                        decision == StashCredentialTransportDecision.InsecureLocalAllowed
                    ) {
                        SetupInsecureLocalHttpAcknowledgement(
                            checked = allowInsecureLocalApiKey,
                            onCheckedChange = { allowInsecureLocalApiKey = it },
                        )
                    }
                }
                if (statusText != null) {
                    Text(statusText!!, color = MaterialTheme.colorScheme.primary)
                }
                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val profile = buildSetupProfile(
                                serverName = serverName,
                                serverUrl = serverUrl,
                                apiKey = apiKey,
                                allowInsecureLocalApiKey = allowInsecureLocalApiKey,
                            )
                            if (!profile.isConfigured()) {
                                errorText = stashString(R.string.auto_kr_0532)
                                return@launch
                            }
                            val requestedAuthMode = if (authMode == SetupAuthMode.Password) {
                                StashServerAuthMode.SessionCookie
                            } else {
                                StashServerAuthMode.ApiKey
                            }
                            if (!canAttemptStashCredentialTransport(profile.baseUrl, requestedAuthMode, profile.allowInsecureLocalApiKey)) {
                                errorText = stashString(R.string.settings_insecure_auth_blocked)
                                return@launch
                            }
                            isSaving = true
                            errorText = null
                            statusText = stashString(R.string.auto_kr_0533)
                            val connectResult = if (authMode == SetupAuthMode.Password) {
                                if (username.isBlank() || password.isBlank()) {
                                    Result.failure(IllegalArgumentException(stashString(R.string.setup_username_password_required)))
                                } else {
                                    runCatching {
                                        val login = StashLoginClient().loginWithPassword(serverUrl, username, password)
                                        profile.copy(
                                            apiKey = "",
                                            authMode = StashServerAuthMode.SessionCookie,
                                            sessionCookie = login.sessionCookie,
                                            allowInsecureLocalApiKey = profile.allowInsecureLocalApiKey,
                                        )
                                    }
                                }
                            } else {
                                Result.success(profile)
                            }
                            connectResult
                                .mapCatching { authenticatedProfile ->
                                    val version = StashGraphQlClient(authenticatedProfile).testConnection()
                                    repository.saveServerProfile(authenticatedProfile)
                                    version
                                }
                                .onSuccess { version ->
                                    statusText = stashString(R.string.auto_kr_0534, version)
                                    onContinue()
                                }
                                .onFailure {
                                    StashDebugLogBuffer.record("Onboarding", "Connection failed", it)
                                    errorText = it.message ?: stashString(R.string.auto_kr_0535)
                                    statusText = null
                                }
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                ) {
                    if (isSaving) CircularProgressIndicator() else Text(stashString(R.string.auto_kr_0536))
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val profile = buildSetupProfile(serverName, serverUrl, apiKey, allowInsecureLocalApiKey)
                            if (!canAttemptStashCredentialTransport(profile.baseUrl, profile.authMode, profile.allowInsecureLocalApiKey)) {
                                errorText = stashString(R.string.settings_insecure_auth_blocked)
                                return@launch
                            }
                            repository.saveServerProfile(profile)
                            onContinue()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && serverUrl.isNotBlank() && shouldAllowUntestedSetupSave(authMode),
                ) {
                    Text(stashString(R.string.auto_kr_0537))
                }
            }
        }
    }
}

private fun buildSetupProfile(
    serverName: String,
    serverUrl: String,
    apiKey: String,
    allowInsecureLocalApiKey: Boolean,
): StashServerProfile = StashServerProfile(
    name = serverName,
    baseUrl = serverUrl,
    apiKey = apiKey,
    authMode = StashServerAuthMode.ApiKey,
    allowInsecureLocalApiKey = allowInsecureLocalApiKey,
)

@Composable
private fun SetupAuthModeRow(
    selected: Boolean,
    label: Int,
    description: Int,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stashString(label), style = MaterialTheme.typography.bodyLarge)
            Text(
                stashString(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SetupInsecureLocalHttpAcknowledgement(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            stashString(R.string.settings_insecure_local_http_api_key_acknowledgement),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
