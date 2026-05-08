package gomeng.dev.stashplayer.app

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import gomeng.dev.stashplayer.app.navigation.StashNavHost
import gomeng.dev.stashplayer.app.navigation.isFoldLikeLayoutBySmallestWidthDp
import gomeng.dev.stashplayer.core.debug.StashDebugLogBuffer
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.ui.i18n.updateStashStringContext
import gomeng.dev.stashplayer.core.ui.i18n.withStashAppLanguage
import gomeng.dev.stashplayer.core.ui.theme.StashPlayerTheme
import gomeng.dev.stashplayer.core.ui.theme.resolveStashDarkTheme
import gomeng.dev.stashplayer.feature.security.BiometricAppLockGate

class StashPlayerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        updateStashStringContext(this)
        StashDebugLogBuffer.initialize(this)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            StashDebugLogBuffer.record("Uncaught", "Thread ${thread.name}", throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}

@Composable
fun StashPlayerAppRoot() {
    val context = LocalContext.current
    val deviceConfiguration = LocalConfiguration.current
    val repository = remember(context) { StashSettingsRepository(context) }
    val themeMode by repository.themeMode.collectAsState(
        initial = StashSettingsRepository.DEFAULT_THEME_MODE,
    )
    val appLanguage by repository.appLanguage.collectAsState(
        initial = StashSettingsRepository.DEFAULT_APP_LANGUAGE,
    )
    val accentColor by repository.accentColor.collectAsState(
        initial = StashSettingsRepository.DEFAULT_ACCENT_COLOR,
    )
    val uiScale by repository.uiScale.collectAsState(
        initial = StashSettingsRepository.DEFAULT_UI_SCALE,
    )
    val savedProfile by repository.serverProfile.collectAsState(initial = null)
    val biometricAppLockEnabledState = repository.biometricAppLockEnabled.collectAsState(initial = null as Boolean?)
    val biometricAppLockEnabled = biometricAppLockEnabledState.value
    var appSessionUnlocked by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(biometricAppLockEnabled) {
        if (biometricAppLockEnabled == false) {
            appSessionUnlocked = true
        }
    }
    val localizedContext = remember(context, appLanguage) {
        context.withStashAppLanguage(appLanguage)
    }
    val localizedConfiguration = remember(localizedContext) {
        localizedContext.resources.configuration
    }
    updateStashStringContext(localizedContext)
    val darkTheme = resolveStashDarkTheme(themeMode, isSystemInDarkTheme())
    val isFoldLikeLayout = isFoldLikeLayoutBySmallestWidthDp(deviceConfiguration.smallestScreenWidthDp)

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        StashPlayerTheme(
            darkTheme = darkTheme,
            accentColor = accentColor,
        ) {
            val background = MaterialTheme.colorScheme.background
            val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = if (darkTheme) 0.18f else 0.09f)
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(background)
                        drawCircle(
                            color = glowColor,
                            radius = size.width * 0.68f,
                            center = Offset(size.width / 2f, 0f),
                        )
                    },
                color = Color.Transparent,
            ) {
                biometricAppLockEnabled?.let { appLockEnabled ->
                    BiometricAppLockGate(
                        hasSavedProfile = savedProfile != null,
                        appLockEnabled = appLockEnabled,
                        appSessionUnlocked = appSessionUnlocked,
                        onUnlocked = { appSessionUnlocked = true },
                    ) {
                        StashNavHost(
                            isFoldLikeLayout = isFoldLikeLayout,
                            uiScale = uiScale,
                        )
                    }
                }
            }
        }
    }
}
