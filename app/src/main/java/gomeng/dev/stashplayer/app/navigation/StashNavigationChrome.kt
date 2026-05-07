package gomeng.dev.stashplayer.app.navigation

import androidx.annotation.StringRes
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.ui.designsystem.StashTouch

private val PlayerRoutePattern = "player/{sceneId}"

internal enum class StashNavigationColorRole {
    Surface,
    OnSurfaceVariant,
    Primary,
    PrimaryContainer,
    Outline,
}

internal data class StashNavigationChromeVisualPolicy(
    val containerRole: StashNavigationColorRole,
    val selectedIconRole: StashNavigationColorRole,
    val selectedTextRole: StashNavigationColorRole,
    val selectedIndicatorRole: StashNavigationColorRole,
    val unselectedIconRole: StashNavigationColorRole,
    val unselectedTextRole: StashNavigationColorRole,
    val dividerRole: StashNavigationColorRole,
    val containerAlpha: Float,
    val selectedContentAlpha: Float,
    val unselectedContentAlpha: Float,
    val indicatorAlpha: Float,
    val dividerAlpha: Float,
    val disabledContentAlpha: Float,
    val minItemTouchTargetDp: Int,
)

internal fun stashNavigationChromeVisualPolicy(): StashNavigationChromeVisualPolicy = StashNavigationChromeVisualPolicy(
    containerRole = StashNavigationColorRole.Surface,
    selectedIconRole = StashNavigationColorRole.Primary,
    selectedTextRole = StashNavigationColorRole.Primary,
    selectedIndicatorRole = StashNavigationColorRole.PrimaryContainer,
    unselectedIconRole = StashNavigationColorRole.OnSurfaceVariant,
    unselectedTextRole = StashNavigationColorRole.OnSurfaceVariant,
    dividerRole = StashNavigationColorRole.Outline,
    containerAlpha = 0.96f,
    selectedContentAlpha = 1f,
    unselectedContentAlpha = 0.78f,
    indicatorAlpha = 1f,
    dividerAlpha = 0.18f,
    disabledContentAlpha = 0.42f,
    minItemTouchTargetDp = StashTouch.MinTarget.value.toInt(),
)

internal fun isPlayerRoute(route: String?): Boolean = route == PlayerRoutePattern || route?.startsWith("player/") == true

internal fun shouldShowBottomNavigation(
    route: String?,
    isFoldLikeLayout: Boolean,
): Boolean = !isFoldLikeLayout && !isPlayerRoute(route)

internal fun shouldShowNavigationRail(
    route: String?,
    isFoldLikeLayout: Boolean,
): Boolean = isFoldLikeLayout && !isPlayerRoute(route)

internal fun shouldApplyScaffoldChromePadding(route: String?): Boolean = !isPlayerRoute(route)

internal const val SetupRoute = "setup"
internal const val SetupResetRoute = "setup/reset"

internal fun resolveStashStartDestination(hasSavedProfile: Boolean): String = if (hasSavedProfile) "home" else SetupRoute

internal fun shouldRedirectSetupWithSavedProfile(hasSavedProfile: Boolean, route: String?): Boolean =
    hasSavedProfile && route == SetupRoute

private val TopLevelDestinationLabelResourcesByRoute = mapOf(
    "home" to R.string.navigation_home_label,
    "browse" to R.string.navigation_browse_label,
    "search" to R.string.navigation_search_label,
    "queue" to R.string.navigation_queue_label,
    "settings" to R.string.navigation_settings_label,
)

internal fun topLevelDestinationLabelResourcesByRoute(): Map<String, Int> = TopLevelDestinationLabelResourcesByRoute

@StringRes
internal fun topLevelDestinationLabelResource(route: String): Int = TopLevelDestinationLabelResourcesByRoute.getValue(route)
