package gomeng.dev.stashplayer.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private object StashThemeArgb {
    const val DarkBackground = 0xFF070A12
    const val DarkSurface = 0xFF0B1020
    const val DarkSurfaceElevated = 0xFF111827
    const val DarkSurfaceVariant = 0xFF1A2030
    const val DarkOnSurface = 0xFFF8FAFC
    const val DarkOnSurfaceVariant = 0xFFCBD5E1
    const val DarkOutline = 0xFF2A2F3C
    const val DarkPrimary = 0xFF7C3AED
    const val DarkOnPrimary = 0xFFFFFFFF
    const val DarkPrimaryContainer = 0xFF312E81
    const val DarkOnPrimaryContainer = 0xFFEDE9FE
    const val DarkSecondary = 0xFF22D3EE
    const val DarkOnSecondary = 0xFF062A33
    const val DarkSecondaryContainer = 0xFF164E63
    const val DarkOnSecondaryContainer = 0xFFCFFAFE
    const val DarkTertiary = 0xFFFBBF24
    const val DarkOnTertiary = 0xFF221A03
    const val DarkError = 0xFFF87171
    const val DarkOnError = 0xFF300304
    const val DarkErrorContainer = 0xFF7F1D1D
    const val DarkOnErrorContainer = 0xFFFEE2E2

    const val LightBackground = 0xFFF8FAFC
    const val LightSurface = 0xFFFFFFFF
    const val LightSurfaceElevated = 0xFFF1F5F9
    const val LightSurfaceVariant = 0xFFE2E8F0
    const val LightOnSurface = 0xFF0F172A
    const val LightOnSurfaceVariant = 0xFF334155
    const val LightOutline = 0xFFCBD5E1
    const val LightPrimary = 0xFF6D28D9
    const val LightOnPrimary = 0xFFFFFFFF
    const val LightPrimaryContainer = 0xFFEDE9FE
    const val LightOnPrimaryContainer = 0xFF312E81
    const val LightSecondary = 0xFF0E7490
    const val LightOnSecondary = 0xFFFFFFFF
    const val LightSecondaryContainer = 0xFFCFFAFE
    const val LightOnSecondaryContainer = 0xFF164E63
    const val LightTertiary = 0xFFB45309
    const val LightOnTertiary = 0xFFFFFFFF
    const val LightError = 0xFFDC2626
    const val LightOnError = 0xFFFFFFFF
    const val LightErrorContainer = 0xFFFEE2E2
    const val LightOnErrorContainer = 0xFF7F1D1D
}

private val MediaDarkColors = darkColorScheme(
    background = Color(StashThemeArgb.DarkBackground),
    onBackground = Color(StashThemeArgb.DarkOnSurface),
    surface = Color(StashThemeArgb.DarkSurface),
    onSurface = Color(StashThemeArgb.DarkOnSurface),
    surfaceContainer = Color(StashThemeArgb.DarkSurface),
    surfaceContainerHigh = Color(StashThemeArgb.DarkSurfaceElevated),
    surfaceVariant = Color(StashThemeArgb.DarkSurfaceVariant),
    onSurfaceVariant = Color(StashThemeArgb.DarkOnSurfaceVariant),
    primary = Color(StashThemeArgb.DarkPrimary),
    onPrimary = Color(StashThemeArgb.DarkOnPrimary),
    primaryContainer = Color(StashThemeArgb.DarkPrimaryContainer),
    onPrimaryContainer = Color(StashThemeArgb.DarkOnPrimaryContainer),
    secondary = Color(StashThemeArgb.DarkSecondary),
    onSecondary = Color(StashThemeArgb.DarkOnSecondary),
    secondaryContainer = Color(StashThemeArgb.DarkSecondaryContainer),
    onSecondaryContainer = Color(StashThemeArgb.DarkOnSecondaryContainer),
    tertiary = Color(StashThemeArgb.DarkTertiary),
    onTertiary = Color(StashThemeArgb.DarkOnTertiary),
    error = Color(StashThemeArgb.DarkError),
    onError = Color(StashThemeArgb.DarkOnError),
    errorContainer = Color(StashThemeArgb.DarkErrorContainer),
    onErrorContainer = Color(StashThemeArgb.DarkOnErrorContainer),
    outline = Color(StashThemeArgb.DarkOutline),
    outlineVariant = Color(StashThemeArgb.DarkSurfaceVariant),
)

private val MediaLightColors = lightColorScheme(
    background = Color(StashThemeArgb.LightBackground),
    onBackground = Color(StashThemeArgb.LightOnSurface),
    surface = Color(StashThemeArgb.LightSurface),
    onSurface = Color(StashThemeArgb.LightOnSurface),
    surfaceContainer = Color(StashThemeArgb.LightSurfaceElevated),
    surfaceContainerHigh = Color(StashThemeArgb.LightSurfaceVariant),
    surfaceVariant = Color(StashThemeArgb.LightSurfaceVariant),
    onSurfaceVariant = Color(StashThemeArgb.LightOnSurfaceVariant),
    primary = Color(StashThemeArgb.LightPrimary),
    onPrimary = Color(StashThemeArgb.LightOnPrimary),
    primaryContainer = Color(StashThemeArgb.LightPrimaryContainer),
    onPrimaryContainer = Color(StashThemeArgb.LightOnPrimaryContainer),
    secondary = Color(StashThemeArgb.LightSecondary),
    onSecondary = Color(StashThemeArgb.LightOnSecondary),
    secondaryContainer = Color(StashThemeArgb.LightSecondaryContainer),
    onSecondaryContainer = Color(StashThemeArgb.LightOnSecondaryContainer),
    tertiary = Color(StashThemeArgb.LightTertiary),
    onTertiary = Color(StashThemeArgb.LightOnTertiary),
    error = Color(StashThemeArgb.LightError),
    onError = Color(StashThemeArgb.LightOnError),
    errorContainer = Color(StashThemeArgb.LightErrorContainer),
    onErrorContainer = Color(StashThemeArgb.LightOnErrorContainer),
    outline = Color(StashThemeArgb.LightOutline),
    outlineVariant = Color(StashThemeArgb.LightSurfaceVariant),
)

private data class StashAccentPalette(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
)

private fun stashAccentPalette(
    accentColor: StashAccentColor,
    darkTheme: Boolean,
): StashAccentPalette = when (accentColor) {
    StashAccentColor.Purple -> if (darkTheme) {
        StashAccentPalette(
            primary = StashThemeArgb.DarkPrimary.toArgbLong(),
            onPrimary = StashThemeArgb.DarkOnPrimary.toArgbLong(),
            primaryContainer = StashThemeArgb.DarkPrimaryContainer.toArgbLong(),
            onPrimaryContainer = StashThemeArgb.DarkOnPrimaryContainer.toArgbLong(),
            secondary = StashThemeArgb.DarkSecondary.toArgbLong(),
            onSecondary = StashThemeArgb.DarkOnSecondary.toArgbLong(),
            secondaryContainer = StashThemeArgb.DarkSecondaryContainer.toArgbLong(),
            onSecondaryContainer = StashThemeArgb.DarkOnSecondaryContainer.toArgbLong(),
        )
    } else {
        StashAccentPalette(
            primary = StashThemeArgb.LightPrimary.toArgbLong(),
            onPrimary = StashThemeArgb.LightOnPrimary.toArgbLong(),
            primaryContainer = StashThemeArgb.LightPrimaryContainer.toArgbLong(),
            onPrimaryContainer = StashThemeArgb.LightOnPrimaryContainer.toArgbLong(),
            secondary = StashThemeArgb.LightSecondary.toArgbLong(),
            onSecondary = StashThemeArgb.LightOnSecondary.toArgbLong(),
            secondaryContainer = StashThemeArgb.LightSecondaryContainer.toArgbLong(),
            onSecondaryContainer = StashThemeArgb.LightOnSecondaryContainer.toArgbLong(),
        )
    }

    StashAccentColor.Blue -> if (darkTheme) {
        StashAccentPalette(
            primary = 0xFF2563EBu.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFF1E3A8Au.toLong(),
            onPrimaryContainer = 0xFFDBEAFEu.toLong(),
            secondary = 0xFF38BDF8u.toLong(),
            onSecondary = 0xFF082F49u.toLong(),
            secondaryContainer = 0xFF075985u.toLong(),
            onSecondaryContainer = 0xFFE0F2FEu.toLong(),
        )
    } else {
        StashAccentPalette(
            primary = 0xFF2563EBu.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFFDBEAFEu.toLong(),
            onPrimaryContainer = 0xFF1E3A8Au.toLong(),
            secondary = 0xFF0369A1u.toLong(),
            onSecondary = 0xFFFFFFFFu.toLong(),
            secondaryContainer = 0xFFE0F2FEu.toLong(),
            onSecondaryContainer = 0xFF075985u.toLong(),
        )
    }

    StashAccentColor.Cyan -> if (darkTheme) {
        StashAccentPalette(
            primary = 0xFF22D3EEu.toLong(),
            onPrimary = 0xFF062A33u.toLong(),
            primaryContainer = 0xFF164E63u.toLong(),
            onPrimaryContainer = 0xFFCFFAFEu.toLong(),
            secondary = 0xFF06B6D4u.toLong(),
            onSecondary = 0xFF062A33u.toLong(),
            secondaryContainer = 0xFF155E75u.toLong(),
            onSecondaryContainer = 0xFFCFFAFEu.toLong(),
        )
    } else {
        StashAccentPalette(
            primary = 0xFF0E7490u.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFFCFFAFEu.toLong(),
            onPrimaryContainer = 0xFF164E63u.toLong(),
            secondary = 0xFF0F766Eu.toLong(),
            onSecondary = 0xFFFFFFFFu.toLong(),
            secondaryContainer = 0xFFE0F7FAu.toLong(),
            onSecondaryContainer = 0xFF155E75u.toLong(),
        )
    }

    StashAccentColor.Amber -> if (darkTheme) {
        StashAccentPalette(
            primary = 0xFFD97706u.toLong(),
            onPrimary = 0xFF221A03u.toLong(),
            primaryContainer = 0xFF78350Fu.toLong(),
            onPrimaryContainer = 0xFFFEF3C7u.toLong(),
            secondary = 0xFFF59E0Bu.toLong(),
            onSecondary = 0xFF221A03u.toLong(),
            secondaryContainer = 0xFF92400Eu.toLong(),
            onSecondaryContainer = 0xFFFEF3C7u.toLong(),
        )
    } else {
        StashAccentPalette(
            primary = 0xFFB45309u.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFFFEF3C7u.toLong(),
            onPrimaryContainer = 0xFF78350Fu.toLong(),
            secondary = 0xFFCA8A04u.toLong(),
            onSecondary = 0xFF221A03u.toLong(),
            secondaryContainer = 0xFFFEF9C3u.toLong(),
            onSecondaryContainer = 0xFF713F12u.toLong(),
        )
    }

    StashAccentColor.Rose -> if (darkTheme) {
        StashAccentPalette(
            primary = 0xFFE11D48u.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFF881337u.toLong(),
            onPrimaryContainer = 0xFFFFE4E6u.toLong(),
            secondary = 0xFFFB7185u.toLong(),
            onSecondary = 0xFF3F0713u.toLong(),
            secondaryContainer = 0xFF9F1239u.toLong(),
            onSecondaryContainer = 0xFFFFE4E6u.toLong(),
        )
    } else {
        StashAccentPalette(
            primary = 0xFFBE123Cu.toLong(),
            onPrimary = 0xFFFFFFFFu.toLong(),
            primaryContainer = 0xFFFFE4E6u.toLong(),
            onPrimaryContainer = 0xFF881337u.toLong(),
            secondary = 0xFFE11D48u.toLong(),
            onSecondary = 0xFFFFFFFFu.toLong(),
            secondaryContainer = 0xFFFFE4E6u.toLong(),
            onSecondaryContainer = 0xFF9F1239u.toLong(),
        )
    }
}

private fun ColorScheme.withStashAccent(
    accentColor: StashAccentColor,
    darkTheme: Boolean,
): ColorScheme {
    val palette = stashAccentPalette(accentColor, darkTheme)
    return copy(
        primary = Color(palette.primary),
        onPrimary = Color(palette.onPrimary),
        primaryContainer = Color(palette.primaryContainer),
        onPrimaryContainer = Color(palette.onPrimaryContainer),
        secondary = Color(palette.secondary),
        onSecondary = Color(palette.onSecondary),
        secondaryContainer = Color(palette.secondaryContainer),
        onSecondaryContainer = Color(palette.onSecondaryContainer),
    )
}

data class StashThemeColorSnapshot(
    val modeName: String,
    val backgroundArgb: Long,
    val surfaceArgb: Long,
    val surfaceVariantArgb: Long,
    val onSurfaceArgb: Long,
    val onSurfaceVariantArgb: Long,
    val outlineArgb: Long,
    val primaryArgb: Long,
    val onPrimaryArgb: Long,
    val primaryContainerArgb: Long,
    val onPrimaryContainerArgb: Long,
    val secondaryArgb: Long,
    val onSecondaryArgb: Long,
    val secondaryContainerArgb: Long,
    val onSecondaryContainerArgb: Long,
    val tertiaryArgb: Long,
    val onTertiaryArgb: Long,
    val errorArgb: Long,
    val onErrorArgb: Long,
    val errorContainerArgb: Long,
    val onErrorContainerArgb: Long,
)

fun stashThemeColorSnapshot(
    darkTheme: Boolean,
    accentColor: StashAccentColor = StashAccentColor.default,
): StashThemeColorSnapshot {
    val accentPalette = stashAccentPalette(accentColor, darkTheme)
    return if (darkTheme) {
        StashThemeColorSnapshot(
            modeName = "dark:${accentColor.persistedValue}",
            backgroundArgb = StashThemeArgb.DarkBackground.toArgbLong(),
            surfaceArgb = StashThemeArgb.DarkSurface.toArgbLong(),
            surfaceVariantArgb = StashThemeArgb.DarkSurfaceVariant.toArgbLong(),
            onSurfaceArgb = StashThemeArgb.DarkOnSurface.toArgbLong(),
            onSurfaceVariantArgb = StashThemeArgb.DarkOnSurfaceVariant.toArgbLong(),
            outlineArgb = StashThemeArgb.DarkOutline.toArgbLong(),
            primaryArgb = accentPalette.primary,
            onPrimaryArgb = accentPalette.onPrimary,
            primaryContainerArgb = accentPalette.primaryContainer,
            onPrimaryContainerArgb = accentPalette.onPrimaryContainer,
            secondaryArgb = accentPalette.secondary,
            onSecondaryArgb = accentPalette.onSecondary,
            secondaryContainerArgb = accentPalette.secondaryContainer,
            onSecondaryContainerArgb = accentPalette.onSecondaryContainer,
            tertiaryArgb = StashThemeArgb.DarkTertiary.toArgbLong(),
            onTertiaryArgb = StashThemeArgb.DarkOnTertiary.toArgbLong(),
            errorArgb = StashThemeArgb.DarkError.toArgbLong(),
            onErrorArgb = StashThemeArgb.DarkOnError.toArgbLong(),
            errorContainerArgb = StashThemeArgb.DarkErrorContainer.toArgbLong(),
            onErrorContainerArgb = StashThemeArgb.DarkOnErrorContainer.toArgbLong(),
        )
    } else {
        StashThemeColorSnapshot(
            modeName = "light:${accentColor.persistedValue}",
            backgroundArgb = StashThemeArgb.LightBackground.toArgbLong(),
            surfaceArgb = StashThemeArgb.LightSurface.toArgbLong(),
            surfaceVariantArgb = StashThemeArgb.LightSurfaceVariant.toArgbLong(),
            onSurfaceArgb = StashThemeArgb.LightOnSurface.toArgbLong(),
            onSurfaceVariantArgb = StashThemeArgb.LightOnSurfaceVariant.toArgbLong(),
            outlineArgb = StashThemeArgb.LightOutline.toArgbLong(),
            primaryArgb = accentPalette.primary,
            onPrimaryArgb = accentPalette.onPrimary,
            primaryContainerArgb = accentPalette.primaryContainer,
            onPrimaryContainerArgb = accentPalette.onPrimaryContainer,
            secondaryArgb = accentPalette.secondary,
            onSecondaryArgb = accentPalette.onSecondary,
            secondaryContainerArgb = accentPalette.secondaryContainer,
            onSecondaryContainerArgb = accentPalette.onSecondaryContainer,
            tertiaryArgb = StashThemeArgb.LightTertiary.toArgbLong(),
            onTertiaryArgb = StashThemeArgb.LightOnTertiary.toArgbLong(),
            errorArgb = StashThemeArgb.LightError.toArgbLong(),
            onErrorArgb = StashThemeArgb.LightOnError.toArgbLong(),
            errorContainerArgb = StashThemeArgb.LightErrorContainer.toArgbLong(),
            onErrorContainerArgb = StashThemeArgb.LightOnErrorContainer.toArgbLong(),
        )
    }
}

fun stashThemeContrastRatio(foregroundArgb: Long, backgroundArgb: Long): Double {
    val foregroundLuminance = relativeLuminance(foregroundArgb)
    val backgroundLuminance = relativeLuminance(backgroundArgb)
    val lighter = maxOf(foregroundLuminance, backgroundLuminance)
    val darker = minOf(foregroundLuminance, backgroundLuminance)
    return (lighter + 0.05) / (darker + 0.05)
}

private fun Long.toArgbLong(): Long = this and 0xFFFFFFFFL

private fun relativeLuminance(argb: Long): Double {
    val red = ((argb shr 16) and 0xFF) / 255.0
    val green = ((argb shr 8) and 0xFF) / 255.0
    val blue = (argb and 0xFF) / 255.0
    return 0.2126 * red.toLinearChannel() +
        0.7152 * green.toLinearChannel() +
        0.0722 * blue.toLinearChannel()
}

private fun Double.toLinearChannel(): Double = if (this <= 0.04045) {
    this / 12.92
} else {
    Math.pow((this + 0.055) / 1.055, 2.4)
}

private val StashMediaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun StashPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: StashAccentColor = StashAccentColor.default,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = (if (darkTheme) MediaDarkColors else MediaLightColors).withStashAccent(
            accentColor = accentColor,
            darkTheme = darkTheme,
        ),
        shapes = StashMediaShapes,
        content = content,
    )
}
