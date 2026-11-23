package com.locrights.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.locrights.app.R
import com.locrights.app.domain.model.Article
import com.locrights.app.ui.components.ArticleCard
import com.locrights.app.ui.components.ArticleThumbnail
import com.locrights.app.ui.components.NativeHtmlRenderer
import com.locrights.app.ui.viewmodel.ArticleDetailUiState
import com.locrights.app.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    uiState: ArticleDetailUiState,
    bookmarkIds: Set<String>,
    fontScale: Float,
    onBack: () -> Unit,
    onToggleBookmark: (Article) -> Unit,
    onChangeFontScale: (Float) -> Unit,
    onOpenRelatedArticle: (Article) -> Unit
) {
    val context = LocalContext.current
    val article = uiState.article
    val isBookmarked = article != null && article.id in bookmarkIds
    var showFontMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = article?.categories?.firstOrNull() ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                actions = {
                    if (article != null) {
                        Box {
                            IconButton(onClick = { showFontMenu = true }) {
                                Icon(Icons.Outlined.FormatSize, contentDescription = "Text size")
                            }
                            DropdownMenu(
                                expanded = showFontMenu,
                                onDismissRequest = { showFontMenu = false }
                            ) {
                                listOf(
                                    0.9f to "Compact (90%)",
                                    1.0f to "Standard (100%)",
                                    1.15f to "Comfortable (115%)",
                                    1.3f to "Large (130%)"
                                ).forEach { (scale, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            onChangeFontScale(scale)
                                            showFontMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onToggleBookmark(article) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { ShareUtils.shareArticle(context, article.title, article.url) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }

                        IconButton(onClick = { ShareUtils.openExternalUrl(context, article.url) }) {
                            Icon(Icons.Outlined.OpenInBrowser, contentDescription = "Open in browser")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            article == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Article unavailable.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!article.thumbnailUrl.isNullOrBlank()) {
                        item(key = "hero_image") {
                            ArticleThumbnail(
                                imageUrl = article.thumbnailUrl,
                                title = article.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }
                    }

                    item(key = "header") {
                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                if (article.categories.isNotEmpty()) {
                                    Text(
                                        text = article.categories.take(3).joinToString(" · "),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = listOfNotNull(
                                            article.authorName.takeIf { it.isNotBlank() },
                                            article.formattedDate.takeIf { it.isNotBlank() },
                                            "${article.readingTimeMinutes} min read"
                                        ).joinToString(" · "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }

                    item(key = "content_body") {
                        NativeHtmlRenderer(
                            html = article.contentHtml,
                            fontScale = fontScale,
                            onLinkClick = { url -> ShareUtils.openExternalUrl(context, url) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }

                    if (uiState.relatedArticles.isNotEmpty()) {
                        item(key = "related_header") {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.related_articles),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        items(uiState.relatedArticles, key = { "rel_${it.id}" }) { related ->
                            ArticleCard(
                                article = related,
                                isBookmarked = related.id in bookmarkIds,
                                onClick = { onOpenRelatedArticle(related) },
                                onToggleBookmark = { onToggleBookmark(related) }
                            )
                        }
                    }
                }
            }
        }
    }
}
