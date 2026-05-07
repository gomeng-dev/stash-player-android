package gomeng.dev.stashplayer.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

enum class StashUiScale(
    val persistedValue: String,
    val multiplier: Float,
) {
    Compact("compact", 0.90f),
    Default("default", 1.00f),
    Comfortable("comfortable", 1.10f),
    Large("large", 1.20f);

    companion object {
        val default: StashUiScale = Default

        fun fromPersistedValue(value: String?): StashUiScale =
            entries.firstOrNull { it.persistedValue == value?.trim()?.lowercase() } ?: default
    }
}

val LocalStashUiScale = staticCompositionLocalOf { StashUiScale.default }

@Composable
fun StashUiScaleProvider(
    uiScale: StashUiScale,
    content: @Composable () -> Unit,
) {
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, uiScale) {
        Density(
            density = baseDensity.density * uiScale.multiplier,
            fontScale = baseDensity.fontScale,
        )
    }
    CompositionLocalProvider(
        LocalStashUiScale provides uiScale,
        LocalDensity provides scaledDensity,
        content = content,
    )
}

/** Use for isolated UI that is not wrapped by StashUiScaleProvider. */
@Composable
@ReadOnlyComposable
fun scaledDp(value: Dp): Dp = value * LocalStashUiScale.current.multiplier

/** Use for isolated UI that is not wrapped by StashUiScaleProvider. */
@Composable
@ReadOnlyComposable
fun scaledSp(value: TextUnit): TextUnit =
    value * LocalStashUiScale.current.multiplier
