package gomeng.dev.stashplayer.app.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import gomeng.dev.stashplayer.R
import gomeng.dev.stashplayer.core.local.StashLocalLibraryRepository
import gomeng.dev.stashplayer.core.local.applyLocalFavoriteFilter
import gomeng.dev.stashplayer.core.model.GalleryImageModel
import gomeng.dev.stashplayer.core.model.SceneCardModel
import gomeng.dev.stashplayer.core.debug.StashDebugLogBuffer
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueue
import gomeng.dev.stashplayer.core.player.PlayerPlaybackQueueContinuation
import gomeng.dev.stashplayer.core.player.PlayerPresentationMode
import gomeng.dev.stashplayer.core.player.appendLoadedResultPlaybackQueue
import gomeng.dev.stashplayer.core.player.afterLoadedPage
import gomeng.dev.stashplayer.core.player.handOffLoadedResultPlaybackQueue
import gomeng.dev.stashplayer.core.player.shouldLoadMorePlayerPlaylistItems
import gomeng.dev.stashplayer.core.network.StashGraphQlClient
import gomeng.dev.stashplayer.core.network.AppUpdateChecker
import gomeng.dev.stashplayer.core.network.AppUpdateNotice
import gomeng.dev.stashplayer.core.network.StashLoginClient
import gomeng.dev.stashplayer.core.network.StashSettingsRepository
import gomeng.dev.stashplayer.core.network.passwordSessionStartupRefreshKey
import gomeng.dev.stashplayer.core.network.shouldRefreshPasswordSessionOnStartup
import gomeng.dev.stashplayer.core.player.PlaybackOrientationMode
import gomeng.dev.stashplayer.core.ui.theme.StashUiScale
import gomeng.dev.stashplayer.core.ui.theme.StashUiScaleProvider
import gomeng.dev.stashplayer.feature.gallery.GalleryDetailRoute
import gomeng.dev.stashplayer.feature.gallery.GalleryPhotoViewerOverlay
import gomeng.dev.stashplayer.feature.gallery.GalleryRoute
import gomeng.dev.stashplayer.feature.home.HomeRoute
import gomeng.dev.stashplayer.feature.player.PlayerRoute
import gomeng.dev.stashplayer.feature.queue.QueueRoute
import gomeng.dev.stashplayer.feature.explore.ExploreRoute
import gomeng.dev.stashplayer.feature.shorts.ShortsRoute
import gomeng.dev.stashplayer.feature.settings.SettingsDestinations
import gomeng.dev.stashplayer.feature.settings.SettingsDetailRoute
import gomeng.dev.stashplayer.feature.settings.SettingsRoute
import gomeng.dev.stashplayer.feature.settings.SettingsSection
import gomeng.dev.stashplayer.feature.setup.ServerSetupRoute

private enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", topLevelDestinationLabelResource("home"), Icons.Outlined.Home),
    Explore("browse", topLevelDestinationLabelResource("browse"), Icons.Outlined.VideoLibrary),
    Shorts("shorts", topLevelDestinationLabelResource("shorts"), Icons.Outlined.PlayCircle),
    Gallery("gallery", topLevelDestinationLabelResource("gallery"), Icons.Outlined.PhotoLibrary),
    Queue("queue", topLevelDestinationLabelResource("queue"), Icons.AutoMirrored.Outlined.PlaylistPlay),
    Settings("settings", topLevelDestinationLabelResource("settings"), Icons.Outlined.Settings),
}

private data class ActiveGalleryPhotoViewer(
    val initialIndex: Int,
    val images: List<GalleryImageModel>,
)

@Composable
private fun TopLevelDestinationIcon(
    destination: TopLevelDestination,
    label: String,
    showBadge: Boolean,
) {
    if (showBadge) {
        BadgedBox(badge = { Badge() }) {
            androidx.compose.material3.Icon(destination.icon, contentDescription = label)
        }
    } else {
        androidx.compose.material3.Icon(destination.icon, contentDescription = label)
    }
}

@Composable
fun StashNavHost(
    isFoldLikeLayout: Boolean,
    uiScale: StashUiScale = StashUiScale.default,
) {
    val context = LocalContext.current
    val settingsRepository = remember(context) { StashSettingsRepository(context) }
    val localRepository = remember(context) { StashLocalLibraryRepository(context) }
    val savedProfile by settingsRepository.serverProfile.collectAsState(initial = null)
    val playbackOrientationMode by settingsRepository.playbackOrientationMode.collectAsState(
        initial = StashSettingsRepository.DEFAULT_PLAYBACK_ORIENTATION_MODE,
    )
    val favoriteSceneIds by localRepository.favoriteSceneIds.collectAsState(initial = emptySet())
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val updateChecker = remember { AppUpdateChecker() }
    val topLevelDestinations = TopLevelDestination.entries
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    var playbackQueue by remember { mutableStateOf(PlayerPlaybackQueue.Empty) }
    var playbackQueueContinuation by remember { mutableStateOf<PlayerPlaybackQueueContinuation?>(null) }
    var playerPresentationMode by remember { mutableStateOf(PlayerPresentationMode.WatchPage) }
    var refreshedPasswordSessionKey by remember { mutableStateOf<String?>(null) }
    var availableUpdateNotice by remember { mutableStateOf<AppUpdateNotice?>(null) }
    var updateChangelogNotice by remember { mutableStateOf<AppUpdateNotice?>(null) }
    var activeGalleryPhotoViewer by remember { mutableStateOf<ActiveGalleryPhotoViewer?>(null) }
    val navigationChromePolicy = stashNavigationChromeVisualPolicy()
    val activeUiScale = if (isPlayerRoute(currentRoute)) StashUiScale.Default else uiScale
    val activity = remember(context) { context.findActivity() }
    AppOrientationEffect(
        activity = activity,
        request = resolveAppOrientationRequest(
            isFoldLikeLayout = isFoldLikeLayout,
            route = currentRoute,
            playerPresentationMode = playerPresentationMode,
            playbackOrientationMode = playbackOrientationMode,
        ),
    )

    LaunchedEffect(savedProfile, currentRoute) {
        if (shouldRedirectSetupWithSavedProfile(savedProfile != null, currentRoute)) {
            navController.navigate(TopLevelDestination.Home.route) {
                popUpTo(SetupRoute) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(savedProfile) {
        val profile = savedProfile ?: return@LaunchedEffect
        if (!shouldRefreshPasswordSessionOnStartup(profile)) return@LaunchedEffect
        val refreshKey = passwordSessionStartupRefreshKey(profile)
        if (refreshedPasswordSessionKey == refreshKey) return@LaunchedEffect
        refreshedPasswordSessionKey = refreshKey
        runCatching {
            StashLoginClient().loginWithPassword(
                baseUrl = profile.baseUrl,
                username = profile.username,
                password = profile.password,
            )
        }.onSuccess { login ->
            settingsRepository.saveServerProfile(profile.copy(sessionCookie = login.sessionCookie))
        }.onFailure { throwable ->
            StashDebugLogBuffer.record("Navigation", "Password session refresh failed", throwable)
        }
    }

    LaunchedEffect(Unit) {
        runCatching { updateChecker.checkForUpdate(currentAppVersionName(context)) }
            .onSuccess { notice ->
                if (notice != null) {
                    availableUpdateNotice = notice
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.settings_support_update_available_snackbar,
                            notice.latestVersionName,
                        ),
                        actionLabel = context.getString(R.string.settings_support_update_changelog_action),
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        updateChangelogNotice = notice
                    }
                }
            }
            .onFailure { throwable ->
                StashDebugLogBuffer.record("Navigation", "App update check failed", throwable)
            }
    }

    fun openPlaybackQueueScene(
        sceneId: String,
        scenes: List<SceneCardModel>,
        randomShuffle: Boolean = false,
        continuation: PlayerPlaybackQueueContinuation? = null,
    ) {
        playbackQueue = handOffLoadedResultPlaybackQueue(
            currentQueue = playbackQueue,
            scenes = scenes,
            selectedSceneId = sceneId,
            randomShuffle = randomShuffle,
        )
        playerPresentationMode = resolvePlayerPresentationModeForOpenedScene(
            openedFromActivePlayer = isPlayerRoute(currentRoute),
            currentMode = playerPresentationMode,
        )
        playbackQueueContinuation = continuation
        navController.navigate(playerRouteForScene(sceneId))
    }

    suspend fun ensurePlaylistTrailingItems(sceneId: String, minimumTrailingCount: Int) {
        val activeProfile = savedProfile ?: return
        var continuation = playbackQueueContinuation ?: return
        var queue = playbackQueue.withCurrent(sceneId)
        if (
            !shouldLoadMorePlayerPlaylistItems(
                queue = queue,
                currentSceneId = sceneId,
                minimumTrailingCount = minimumTrailingCount,
                hasMore = continuation.hasMore,
            )
        ) {
            return
        }

        val client = StashGraphQlClient(activeProfile)
        while (
            shouldLoadMorePlayerPlaylistItems(
                queue = queue,
                currentSceneId = sceneId,
                minimumTrailingCount = minimumTrailingCount,
                hasMore = continuation.hasMore,
            )
        ) {
            val pageToLoad = continuation.nextPage
            val result = runCatching {
                when (val activeContinuation = continuation) {
                    is PlayerPlaybackQueueContinuation.Browse -> client.findSceneCardsPage(
                        perPage = activeContinuation.pageSize,
                        page = pageToLoad,
                        sort = activeContinuation.sort,
                        direction = activeContinuation.direction,
                        videoFilter = activeContinuation.videoFilter,
                    )
                    is PlayerPlaybackQueueContinuation.Explore -> client.findSceneCardsPage(
                        perPage = activeContinuation.pageSize,
                        page = pageToLoad,
                        query = activeContinuation.query,
                        sort = activeContinuation.sort,
                        direction = activeContinuation.direction,
                        videoFilter = activeContinuation.videoFilter,
                    )
                }
            }.getOrNull() ?: break
            val scenes = result.scenes.applyLocalFavoriteFilter(
                continuation.videoFilterState().localFavoriteOnly,
                favoriteSceneIds,
            )
            continuation = continuation.afterLoadedPage(pageToLoad, result.totalCount)
            queue = appendLoadedResultPlaybackQueue(queue, scenes)
            playbackQueue = queue
            playbackQueueContinuation = continuation
        }
    }

    fun navigateTopLevel(destination: TopLevelDestination) {
        val statePolicy = resolveTopLevelNavigationStatePolicy(destination.route)
        navController.navigate(destination.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = statePolicy.saveState
            }
            launchSingleTop = true
            restoreState = statePolicy.restoreState
        }
    }

    fun replaceCurrentPlayerScene(sceneId: String) {
        playerPresentationMode = resolvePlayerPresentationModeForOpenedScene(
            openedFromActivePlayer = true,
            currentMode = playerPresentationMode,
        )
        navController.navigate(playerRouteForScene(sceneId)) {
            popUpTo("player/{sceneId}") { inclusive = true }
            launchSingleTop = true
        }
    }

    fun exitPlayer() {
        if (!navController.popBackStack()) {
            navController.navigate(TopLevelDestination.Home.route) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    StashUiScaleProvider(uiScale = activeUiScale) {
        updateChangelogNotice?.let { notice ->
            AppUpdateChangelogDialog(
                notice = notice,
                onOpenRelease = {
                    updateChangelogNotice = null
                    openExternalUrl(context, notice.releaseUrl)
                },
                onDismiss = { updateChangelogNotice = null },
            )
        }
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (shouldShowBottomNavigation(currentRoute, isFoldLikeLayout)) {
                        val navigationDividerColor = navigationChromePolicy.dividerRole.toNavigationChromeColor()
                            .copy(alpha = navigationChromePolicy.dividerAlpha)
                        val navigationContainerColor = navigationChromePolicy.containerRole.toNavigationChromeColor()
                            .copy(alpha = navigationChromePolicy.containerAlpha)
                        NavigationBar(
                            modifier = Modifier
                                .drawBehind {
                                    drawLine(
                                        color = navigationDividerColor,
                                        start = Offset.Zero,
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 1.dp.toPx(),
                                    )
                                },
                            containerColor = navigationContainerColor,
                            tonalElevation = 0.dp,
                            windowInsets = NavigationBarDefaults.windowInsets,
                        ) {
                            topLevelDestinations.forEach { destination ->
                                val destinationLabel = stringResource(destination.labelRes)
                                NavigationBarItem(
                                    selected = isTopLevelDestinationSelected(currentRoute, currentDestination?.hierarchy?.map { it.route }, destination),
                                    onClick = {
                                        navigateTopLevel(destination)
                                    },
                                    icon = {
                                        TopLevelDestinationIcon(
                                            destination = destination,
                                            label = destinationLabel,
                                            showBadge = destination == TopLevelDestination.Settings &&
                                                availableUpdateNotice != null,
                                        )
                                    },
                                    colors = navigationChromePolicy.navigationBarItemColors(),
                                    label = {
                                        Text(
                                            destinationLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        )
                                    },
                                )
                            }
                        }
                    }
                },
            ) { paddingValues ->
            androidx.compose.foundation.layout.Row {
                val navHostModifier = if (shouldApplyScaffoldChromePadding(currentRoute)) {
                    Modifier.weight(1f).padding(paddingValues)
                } else {
                    Modifier.weight(1f)
                }
                if (shouldShowNavigationRail(currentRoute, isFoldLikeLayout)) {
                    NavigationRail(
                        containerColor = navigationChromePolicy.containerRole.toNavigationChromeColor()
                            .copy(alpha = navigationChromePolicy.containerAlpha),
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val destinationLabel = stringResource(destination.labelRes)
                            NavigationRailItem(
                                selected = isTopLevelDestinationSelected(currentRoute, currentDestination?.hierarchy?.map { it.route }, destination),
                                onClick = {
                                    navigateTopLevel(destination)
                                },
                                icon = {
                                    TopLevelDestinationIcon(
                                        destination = destination,
                                        label = destinationLabel,
                                        showBadge = destination == TopLevelDestination.Settings &&
                                            availableUpdateNotice != null,
                                    )
                                },
                                colors = navigationChromePolicy.navigationRailItemColors(),
                                label = { Text(destinationLabel) },
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = resolveStashStartDestination(savedProfile != null),
                    modifier = navHostModifier,
                ) {
                    composable(SetupRoute) {
                        ServerSetupRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            onContinue = {
                                navController.navigate(TopLevelDestination.Home.route) {
                                    popUpTo(SetupRoute) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(SetupResetRoute) {
                        ServerSetupRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            onContinue = {
                                navController.navigate(TopLevelDestination.Home.route) {
                                    popUpTo(SetupResetRoute) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(TopLevelDestination.Home.route) {
                        HomeRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            onOpenScene = { sceneId, scenes, randomShuffle ->
                                openPlaybackQueueScene(
                                    sceneId = sceneId,
                                    scenes = scenes,
                                    randomShuffle = randomShuffle,
                                )
                            },
                            onOpenSettings = { navigateTopLevel(TopLevelDestination.Settings) },
                            onOpenQueue = { navigateTopLevel(TopLevelDestination.Queue) },
                            onOpenFavorites = { navigateTopLevel(TopLevelDestination.Queue) },
                            onOpenExplore = { navigateTopLevel(TopLevelDestination.Explore) },
                            onOpenShorts = { navigateTopLevel(TopLevelDestination.Shorts) },
                        )
                    }
                    composable(TopLevelDestination.Explore.route) {
                        ExploreRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            onOpenScene = { sceneId, scenes, randomShuffle, continuation ->
                                openPlaybackQueueScene(
                                    sceneId = sceneId,
                                    scenes = scenes,
                                    randomShuffle = randomShuffle,
                                    continuation = continuation,
                                )
                            },
                            onOpenSettings = { navigateTopLevel(TopLevelDestination.Settings) },
                        )
                    }
                    composable(TopLevelDestination.Shorts.route) {
                        ShortsRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                        )
                    }
                    composable(TopLevelDestination.Gallery.route) {
                        GalleryRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            onOpenGallery = { galleryId ->
                                navController.navigate(galleryDetailRouteForGallery(galleryId))
                            },
                            onOpenPhoto = { index, images ->
                                activeGalleryPhotoViewer = ActiveGalleryPhotoViewer(
                                    initialIndex = index,
                                    images = images,
                                )
                            },
                            onOpenSettings = { navigateTopLevel(TopLevelDestination.Settings) },
                        )
                    }
                    composable("gallery/{galleryId}") { backStackEntry ->
                        GalleryDetailRoute(
                            galleryId = backStackEntry.arguments?.getString("galleryId") ?: "",
                            isFoldLikeLayout = isFoldLikeLayout,
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(TopLevelDestination.Gallery.route) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onOpenPhoto = { _, index, images ->
                                activeGalleryPhotoViewer = ActiveGalleryPhotoViewer(
                                    initialIndex = index,
                                    images = images,
                                )
                            },
                            onOpenScene = { sceneId -> navController.navigate(playerRouteForScene(sceneId)) },
                            onOpenGallery = { linkedGalleryId -> navController.navigate(galleryDetailRouteForGallery(linkedGalleryId)) },
                            onOpenSettings = { navigateTopLevel(TopLevelDestination.Settings) },
                        )
                    }
                    composable(TopLevelDestination.Queue.route) {
                        QueueRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            currentSceneId = playbackQueue.currentSceneId,
                            onOpenScene = { sceneId, scenes, randomShuffle ->
                                openPlaybackQueueScene(
                                    sceneId = sceneId,
                                    scenes = scenes,
                                    randomShuffle = randomShuffle,
                                )
                            },
                            onOpenBrowse = { navigateTopLevel(TopLevelDestination.Explore) },
                            onOpenExplore = { navigateTopLevel(TopLevelDestination.Explore) },
                        )
                    }
                    composable(TopLevelDestination.Settings.route) {
                        SettingsRoute(
                            isFoldLikeLayout = isFoldLikeLayout,
                            hasAvailableUpdate = availableUpdateNotice != null,
                            onOpenSection = { route -> navController.navigate(route) },
                        )
                    }
                    SettingsSection.entries.forEach { section ->
                        composable(section.route) {
                            SettingsDetailRoute(
                                section = section,
                                isFoldLikeLayout = isFoldLikeLayout,
                                availableUpdateNotice = availableUpdateNotice,
                                onUpdateNoticeChange = { availableUpdateNotice = it },
                                onNavigateBack = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate(SettingsDestinations.Root) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onOpenOnboarding = { navController.navigate(SetupResetRoute) },
                            )
                        }
                    }
                    composable("player/{sceneId}") { backStackEntry ->
                        PlayerRoute(
                            sceneId = backStackEntry.arguments?.getString("sceneId") ?: "demo",
                            isFoldLikeLayout = isFoldLikeLayout,
                            playbackQueue = playbackQueue,
                            initialPresentationMode = playerPresentationMode,
                            onPlaybackQueueChange = { playbackQueue = it },
                            onPresentationModeChange = { playerPresentationMode = it },
                            onOpenScene = { sceneId -> replaceCurrentPlayerScene(sceneId) },
                            onOpenSettings = { navController.navigate(TopLevelDestination.Settings.route) },
                            onExitPlayer = ::exitPlayer,
                            onPlaylistDrawerOpen = ::ensurePlaylistTrailingItems,
                        )
                    }
                }
            }
        }
            activeGalleryPhotoViewer?.let { viewer ->
                GalleryPhotoViewerOverlay(
                    images = viewer.images,
                    initialIndex = viewer.initialIndex,
                    serverProfile = savedProfile,
                    onDismiss = { activeGalleryPhotoViewer = null },
                    modifier = Modifier.fillMaxSize(),
                    onOpenLinkedGallery = { galleryId ->
                        activeGalleryPhotoViewer = null
                        navController.navigate(galleryDetailRouteForGallery(galleryId))
                    },
                )
            }
        }
    }
}

@Composable
private fun AppUpdateChangelogDialog(
    notice: AppUpdateNotice,
    onOpenRelease: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    R.string.settings_support_update_changelog_title,
                    notice.latestVersionName,
                ),
            )
        },
        text = {
            Text(
                text = notice.changelog?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.settings_support_update_changelog_empty),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenRelease) {
                Text(stringResource(R.string.settings_support_update_open_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_support_update_changelog_close))
            }
        },
    )
}

@Composable
private fun AppOrientationEffect(
    activity: Activity?,
    request: AppOrientationRequest,
) {
    DisposableEffect(activity, request) {
        val previousOrientation = activity?.requestedOrientation
        if (activity != null) {
            activity.requestedOrientation = request.toActivityInfoOrientation()
        }
        onDispose {
            if (activity != null && previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }
}

private fun AppOrientationRequest.toActivityInfoOrientation(): Int = when (this) {
    AppOrientationRequest.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    AppOrientationRequest.Sensor -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
    AppOrientationRequest.Unspecified -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure { throwable ->
        StashDebugLogBuffer.record("Navigation", "External URL open failed", throwable)
    }
}

private fun currentAppVersionName(context: Context): String {
    return runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }.getOrDefault("")
}

private fun isTopLevelDestinationSelected(
    currentRoute: String?,
    hierarchyRoutes: Sequence<String?>?,
    destination: TopLevelDestination,
): Boolean = when (destination) {
    TopLevelDestination.Settings -> SettingsDestinations.isSettingsRoute(currentRoute)
    else -> hierarchyRoutes?.any { it == destination.route } == true
}

private fun PlayerPlaybackQueueContinuation.videoFilterState() = when (this) {
    is PlayerPlaybackQueueContinuation.Browse -> videoFilter
    is PlayerPlaybackQueueContinuation.Explore -> videoFilter
}

@Composable
private fun StashNavigationColorRole.toNavigationChromeColor(): Color = when (this) {
    StashNavigationColorRole.Surface -> MaterialTheme.colorScheme.surface
    StashNavigationColorRole.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    StashNavigationColorRole.Primary -> MaterialTheme.colorScheme.primary
    StashNavigationColorRole.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    StashNavigationColorRole.Outline -> MaterialTheme.colorScheme.outline
}

@Composable
private fun StashNavigationChromeVisualPolicy.navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = selectedIconRole.toNavigationChromeColor().copy(alpha = selectedContentAlpha),
    selectedTextColor = selectedTextRole.toNavigationChromeColor().copy(alpha = selectedContentAlpha),
    indicatorColor = selectedIndicatorRole.toNavigationChromeColor().copy(alpha = indicatorAlpha),
    unselectedIconColor = unselectedIconRole.toNavigationChromeColor().copy(alpha = unselectedContentAlpha),
    unselectedTextColor = unselectedTextRole.toNavigationChromeColor().copy(alpha = unselectedContentAlpha),
    disabledIconColor = unselectedIconRole.toNavigationChromeColor().copy(alpha = disabledContentAlpha),
    disabledTextColor = unselectedTextRole.toNavigationChromeColor().copy(alpha = disabledContentAlpha),
)

@Composable
private fun StashNavigationChromeVisualPolicy.navigationRailItemColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = selectedIconRole.toNavigationChromeColor().copy(alpha = selectedContentAlpha),
    selectedTextColor = selectedTextRole.toNavigationChromeColor().copy(alpha = selectedContentAlpha),
    indicatorColor = selectedIndicatorRole.toNavigationChromeColor().copy(alpha = indicatorAlpha),
    unselectedIconColor = unselectedIconRole.toNavigationChromeColor().copy(alpha = unselectedContentAlpha),
    unselectedTextColor = unselectedTextRole.toNavigationChromeColor().copy(alpha = unselectedContentAlpha),
    disabledIconColor = unselectedIconRole.toNavigationChromeColor().copy(alpha = disabledContentAlpha),
    disabledTextColor = unselectedTextRole.toNavigationChromeColor().copy(alpha = disabledContentAlpha),
)
