package gomeng.dev.stashplayer.core.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StashSurfaceColorRole.toStashSurfaceThemeColor(): Color = when (this) {
    StashSurfaceColorRole.Background -> MaterialTheme.colorScheme.background
    StashSurfaceColorRole.Surface -> MaterialTheme.colorScheme.surface
    StashSurfaceColorRole.SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
    StashSurfaceColorRole.SurfaceContainerHigh -> MaterialTheme.colorScheme.surfaceContainerHigh
    StashSurfaceColorRole.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
    StashSurfaceColorRole.OnSurface -> MaterialTheme.colorScheme.onSurface
    StashSurfaceColorRole.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    StashSurfaceColorRole.Outline -> MaterialTheme.colorScheme.outline
    StashSurfaceColorRole.Error -> MaterialTheme.colorScheme.error
    StashSurfaceColorRole.ErrorContainer -> MaterialTheme.colorScheme.errorContainer
    StashSurfaceColorRole.OnError -> MaterialTheme.colorScheme.onError
    StashSurfaceColorRole.OnErrorContainer -> MaterialTheme.colorScheme.onErrorContainer
    StashSurfaceColorRole.PlayerChromeBlack -> Color.Black
    StashSurfaceColorRole.PlayerChromeOnBlack -> Color.White
}
