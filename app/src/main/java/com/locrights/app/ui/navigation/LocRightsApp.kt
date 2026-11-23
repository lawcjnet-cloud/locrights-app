package com.locrights.app.ui.navigation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.locrights.app.R
import com.locrights.app.notifications.NotificationHelper
import com.locrights.app.ui.components.LocRightsBrandMark
import com.locrights.app.ui.screens.AboutScreen
import com.locrights.app.ui.screens.ArticleDetailScreen
import com.locrights.app.ui.screens.BookmarksScreen
import com.locrights.app.ui.screens.CategoriesScreen
import com.locrights.app.ui.screens.HomeScreen
import com.locrights.app.ui.screens.SearchScreen
import com.locrights.app.ui.screens.SplashScreen
import com.locrights.app.ui.screens.StaticBlogPageScreen
import com.locrights.app.ui.viewmodel.MainViewModel
import com.locrights.app.util.ShareUtils
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val BOOKMARKS = "bookmarks"
    const val ABOUT = "about"
    const val SEARCH = "search"
    const val PRIVACY = "privacy"
    const val CONTACT = "contact"
    const val ARTICLE_DETAIL = "article/{articleId}"

    fun articleDetail(id: String) = "article/${Uri.encode(id)}"
}

@Composable
fun LocRightsApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val aboutState by viewModel.aboutState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val bookmarkIds by viewModel.bookmarkIds.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    var showNotificationDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Routes.HOME,
        Routes.CATEGORIES,
        Routes.BOOKMARKS,
        Routes.ABOUT
    )

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    LocRightsBrandMark(showSubtitle = true)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = currentRoute == Routes.HOME,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.selectCategory(null)
                        navigateTopLevel(Routes.HOME)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Newspaper, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_latest_articles)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.selectCategory(null)
                        viewModel.loadFeed(reset = true)
                        navigateTopLevel(Routes.HOME)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Category, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_categories)) },
                    selected = currentRoute == Routes.CATEGORIES,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navigateTopLevel(Routes.CATEGORIES)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_saved_articles)) },
                    selected = currentRoute == Routes.BOOKMARKS,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navigateTopLevel(Routes.BOOKMARKS)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_about_loc_rights)) },
                    selected = currentRoute == Routes.ABOUT,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navigateTopLevel(Routes.ABOUT)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Policy, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_privacy_policy)) },
                    selected = currentRoute == Routes.PRIVACY,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.PRIVACY)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.ContactMail, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_contact_us)) },
                    selected = currentRoute == Routes.CONTACT,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.CONTACT)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_share_app)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        ShareUtils.shareApp(context)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.StarRate, contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_rate_app)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        ShareUtils.openPlayStoreListing(context)
                    }
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            selected = currentRoute == Routes.HOME,
                            onClick = { navigateTopLevel(Routes.HOME) },
                            icon = {
                                Icon(
                                    if (currentRoute == Routes.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_home)) }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.CATEGORIES,
                            onClick = { navigateTopLevel(Routes.CATEGORIES) },
                            icon = {
                                Icon(
                                    if (currentRoute == Routes.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_categories)) }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.BOOKMARKS,
                            onClick = { navigateTopLevel(Routes.BOOKMARKS) },
                            icon = {
                                Icon(
                                    if (currentRoute == Routes.BOOKMARKS) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_bookmarks)) }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.ABOUT,
                            onClick = { navigateTopLevel(Routes.ABOUT) },
                            icon = {
                                Icon(
                                    if (currentRoute == Routes.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_about)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onFinished = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        uiState = homeState,
                        bookmarkIds = bookmarkIds,
                        isOnline = isOnline,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenNotifications = { showNotificationDialog = true },
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onRefresh = { viewModel.loadFeed(reset = true, isPullToRefresh = true) },
                        onLoadMore = { viewModel.loadFeed(reset = false) },
                        onOpenArticle = { article ->
                            viewModel.openArticle(article.id)
                            navController.navigate(Routes.articleDetail(article.id))
                        },
                        onToggleBookmark = { viewModel.toggleBookmark(it) }
                    )
                }

                composable(Routes.CATEGORIES) {
                    CategoriesScreen(
                        categories = homeState.categories,
                        selectedCategory = homeState.selectedCategory,
                        onSelectCategory = { label ->
                            viewModel.selectCategory(label)
                            navigateTopLevel(Routes.HOME)
                        }
                    )
                }

                composable(Routes.BOOKMARKS) {
                    BookmarksScreen(
                        bookmarks = bookmarks,
                        onOpenArticle = { article ->
                            viewModel.openArticle(article.id)
                            navController.navigate(Routes.articleDetail(article.id))
                        },
                        onRemoveBookmark = { viewModel.removeBookmark(it.id) }
                    )
                }

                composable(Routes.ABOUT) {
                    AboutScreen(
                        uiState = aboutState,
                        themeMode = settings.themeMode,
                        fontScale = settings.readerFontScale,
                        onChangeThemeMode = { viewModel.setThemeMode(it) },
                        onRetryPages = { viewModel.loadBlogPages() },
                        onOpenPrivacy = { navController.navigate(Routes.PRIVACY) }
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        uiState = searchState,
                        suggestions = homeState.categories,
                        bookmarkIds = bookmarkIds,
                        onBack = { navController.popBackStack() },
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onSearchSubmit = { viewModel.executeSearch(it) },
                        onOpenArticle = { article ->
                            viewModel.openArticle(article.id)
                            navController.navigate(Routes.articleDetail(article.id))
                        },
                        onToggleBookmark = { viewModel.toggleBookmark(it) }
                    )
                }

                composable(
                    route = Routes.ARTICLE_DETAIL,
                    arguments = listOf(navArgument("articleId") { type = NavType.StringType })
                ) {
                    ArticleDetailScreen(
                        uiState = detailState,
                        bookmarkIds = bookmarkIds,
                        fontScale = settings.readerFontScale,
                        onBack = { navController.popBackStack() },
                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                        onChangeFontScale = { viewModel.setFontScale(it) },
                        onOpenRelatedArticle = { related ->
                            viewModel.openArticle(related.id)
                            navController.navigate(Routes.articleDetail(related.id))
                        }
                    )
                }

                composable(Routes.PRIVACY) {
                    StaticBlogPageScreen(
                        title = stringResource(R.string.menu_privacy_policy),
                        pageTitleMatcher = "Privacy",
                        uiState = aboutState,
                        fontScale = settings.readerFontScale,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.CONTACT) {
                    StaticBlogPageScreen(
                        title = stringResource(R.string.menu_contact_us),
                        pageTitleMatcher = "Contact",
                        uiState = aboutState,
                        fontScale = settings.readerFontScale,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showNotificationDialog) {
        val fcmConfigured = NotificationHelper.isPushBackendConfigured()
        val hasPermission = NotificationHelper.hasNotificationPermission(context)

        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Article Update Notifications") },
            text = {
                Text(
                    if (fcmConfigured) {
                        "Push notifications are active for new Law Order and Civil Rights articles."
                    } else {
                        "Notification channels are configured on your device, but live push delivery requires linking Firebase Cloud Messaging (google-services.json) in this build. No simulated alerts are sent."
                    }
                )
            },
            confirmButton = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                    TextButton(
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            showNotificationDialog = false
                        }
                    ) {
                        Text("Allow Permission")
                    }
                } else {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("Dismiss")
                    }
                }
            }
        )
    }
}
