package com.locrights.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.locrights.app.R
import com.locrights.app.domain.model.Article
import com.locrights.app.ui.components.ArticleCard
import com.locrights.app.ui.components.ArticleSkeletonList
import com.locrights.app.ui.components.CategoryChipsBar
import com.locrights.app.ui.components.ConnectivityBanner
import com.locrights.app.ui.components.ErrorFallbackView
import com.locrights.app.ui.components.FeaturedHeroCard
import com.locrights.app.ui.components.LocRightsBrandMark
import com.locrights.app.ui.viewmodel.HomeUiState
import com.locrights.app.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    bookmarkIds: Set<String>,
    isOnline: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSelectCategory: (String?) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenArticle: (Article) -> Unit,
    onToggleBookmark: (Article) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasMore, uiState.isLoadingMore) {
        if (shouldLoadMore && uiState.hasMore && !uiState.isLoadingMore) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                    }
                },
                title = {
                    LocRightsBrandMark(showSubtitle = true)
                },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConnectivityBanner(isOnline = isOnline, showingCache = uiState.fromCache)

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.articles.isEmpty() -> {
                        ArticleSkeletonList()
                    }

                    uiState.errorMessage != null && uiState.articles.isEmpty() -> {
                        ErrorFallbackView(
                            message = uiState.errorMessage,
                            onRetry = onRefresh,
                            onOpenWebsite = { ShareUtils.openExternalUrl(context, ShareUtils.WEBSITE_URL) }
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item(key = "categories_bar") {
                                CategoryChipsBar(
                                    categories = uiState.categories,
                                    selectedCategory = uiState.selectedCategory,
                                    onSelectCategory = onSelectCategory
                                )
                            }

                            val featured = uiState.articles.firstOrNull()
                            if (featured != null) {
                                item(key = "featured_${featured.id}") {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = uiState.selectedCategory ?: stringResource(R.string.featured_story),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FeaturedHeroCard(
                                            article = featured,
                                            isBookmarked = featured.id in bookmarkIds,
                                            onClick = { onOpenArticle(featured) },
                                            onToggleBookmark = { onToggleBookmark(featured) }
                                        )
                                    }
                                }
                            }

                            val remaining = if (uiState.articles.size > 1) uiState.articles.drop(1) else emptyList()
                            if (remaining.isNotEmpty()) {
                                item(key = "latest_header") {
                                    Text(
                                        text = stringResource(R.string.latest_articles),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            itemsIndexed(
                                items = remaining,
                                key = { _, item -> item.id }
                            ) { _, article ->
                                ArticleCard(
                                    article = article,
                                    isBookmarked = article.id in bookmarkIds,
                                    onClick = { onOpenArticle(article) },
                                    onToggleBookmark = { onToggleBookmark(article) }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item(key = "loading_more") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    }
                                }
                            } else if (uiState.hasMore) {
                                item(key = "load_more_btn") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        OutlinedButton(onClick = onLoadMore) {
                                            Text(stringResource(R.string.load_more))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
