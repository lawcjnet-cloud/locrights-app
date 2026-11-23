package com.locrights.app.data.repository

import com.locrights.app.data.local.BookmarkDao
import com.locrights.app.data.local.BookmarkEntity
import com.locrights.app.data.local.CachedArticleDao
import com.locrights.app.data.local.CachedArticleEntity
import com.locrights.app.data.remote.BloggerApiService
import com.locrights.app.data.remote.BloggerEntryDto
import com.locrights.app.domain.model.Article
import com.locrights.app.domain.model.BlogPage
import com.locrights.app.domain.model.FeedPageResult
import com.locrights.app.util.HtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BlogRepository(
    private val api: BloggerApiService,
    private val bookmarkDao: BookmarkDao,
    private val cachedArticleDao: CachedArticleDao
) {
    private val inMemoryCache = LinkedHashMap<String, Article>()

    val bookmarkedArticles: Flow<List<Article>> = bookmarkDao.observeAllBookmarks()
        .map { entities -> entities.map { it.toDomain() } }

    val bookmarkIds: Flow<Set<String>> = bookmarkDao.observeBookmarkIds()
        .map { it.toSet() }

    fun observeIsBookmarked(articleId: String): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(articleId)

    suspend fun fetchArticles(
        startIndex: Int = 1,
        maxResults: Int = 15,
        category: String? = null
    ): Result<FeedPageResult> = withContext(Dispatchers.IO) {
        try {
            val response = if (category.isNullOrBlank()) {
                api.getPosts(startIndex = startIndex, maxResults = maxResults)
            } else {
                api.getPostsByCategory(label = category, startIndex = startIndex, maxResults = maxResults)
            }

            val feed = response.feed ?: throw IllegalStateException("Empty Blogger feed response")
            val articles = (feed.entry ?: emptyList()).mapNotNull { it.toDomainArticle() }
            val categories = (feed.category ?: emptyList())
                .mapNotNull { it.term?.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            val total = feed.totalResults?.value?.toIntOrNull() ?: articles.size

            articles.forEach { inMemoryCache[it.id] = it }

            if (category.isNullOrBlank() && startIndex == 1 && articles.isNotEmpty()) {
                cachedArticleDao.upsertAll(articles.map { it.toCachedEntity() })
                cachedArticleDao.pruneOldCache()
            }

            Result.success(
                FeedPageResult(
                    articles = articles,
                    categories = categories,
                    totalResults = total,
                    fromCache = false
                )
            )
        } catch (e: Exception) {
            if (category.isNullOrBlank() && startIndex == 1) {
                val cached = cachedArticleDao.getAllCached().map { it.toDomain() }
                if (cached.isNotEmpty()) {
                    cached.forEach { inMemoryCache[it.id] = it }
                    val cachedCategories = cached.flatMap { it.categories }.distinct()
                    return@withContext Result.success(
                        FeedPageResult(
                            articles = cached,
                            categories = cachedCategories,
                            totalResults = cached.size,
                            fromCache = true
                        )
                    )
                }
            }
            Result.failure(e)
        }
    }

    suspend fun searchArticles(query: String): Result<List<Article>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext Result.success(emptyList())
        try {
            val response = api.getPosts(startIndex = 1, maxResults = 25, query = trimmed)
            val articles = (response.feed?.entry ?: emptyList()).mapNotNull { it.toDomainArticle() }
            articles.forEach { inMemoryCache[it.id] = it }
            Result.success(articles)
        } catch (e: Exception) {
            val cachedMatches = cachedArticleDao.getAllCached()
                .map { it.toDomain() }
                .filter {
                    it.title.contains(trimmed, ignoreCase = true) ||
                        it.summary.contains(trimmed, ignoreCase = true) ||
                        it.categories.any { c -> c.contains(trimmed, ignoreCase = true) }
                }
            if (cachedMatches.isNotEmpty()) {
                Result.success(cachedMatches)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getArticleById(id: String): Article? = withContext(Dispatchers.IO) {
        inMemoryCache[id]?.let { return@withContext it }
        bookmarkDao.getBookmarkById(id)?.toDomain()?.let {
            inMemoryCache[it.id] = it
            return@withContext it
        }
        cachedArticleDao.getCachedById(id)?.toDomain()?.let {
            inMemoryCache[it.id] = it
            return@withContext it
        }
        null
    }

    suspend fun getRelatedArticles(article: Article, limit: Int = 4): List<Article> =
        withContext(Dispatchers.IO) {
            val pool = (inMemoryCache.values + cachedArticleDao.getAllCached().map { it.toDomain() })
                .distinctBy { it.id }
                .filter { it.id != article.id }
            val primaryCats = article.categories.toSet()
            pool.sortedByDescending { candidate ->
                candidate.categories.count { it in primaryCats }
            }.filter { candidate ->
                primaryCats.isEmpty() || candidate.categories.any { it in primaryCats }
            }.take(limit)
        }

    suspend fun fetchBlogPages(): Result<List<BlogPage>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPages()
            val pages = (response.feed?.entry ?: emptyList()).mapNotNull { entry ->
                val title = entry.title?.value?.trim() ?: return@mapNotNull null
                val id = entry.id?.value ?: title
                val content = entry.content?.value ?: entry.summary?.value ?: ""
                val url = entry.link?.firstOrNull { it.rel == "alternate" }?.href
                    ?: "https://locrights-23.blogspot.com/"
                BlogPage(id = id, title = title, contentHtml = content, url = url)
            }
            Result.success(pages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(article: Article) = withContext(Dispatchers.IO) {
        val existing = bookmarkDao.getBookmarkById(article.id)
        if (existing != null) {
            bookmarkDao.deleteBookmarkById(article.id)
        } else {
            bookmarkDao.upsertBookmark(article.toBookmarkEntity())
        }
    }

    suspend fun removeBookmark(articleId: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(articleId)
    }

    private fun BloggerEntryDto.toDomainArticle(): Article? {
        val rawId = id?.value ?: return null
        val cleanId = rawId.substringAfterLast("post-").ifBlank { rawId }
        val title = title?.value?.trim().orEmpty().ifBlank { "Untitled Article" }
        val rawHtml = content?.value ?: summary?.value ?: ""
        val plain = HtmlParser.stripToPlainText(rawHtml)
        val excerpt = if (plain.length > 180) plain.take(177).trimEnd() + "…" else plain
        val thumbFromMedia = mediaThumbnail?.url?.let { HtmlParser.upscaleBloggerImage(it) }
        val thumb = thumbFromMedia ?: HtmlParser.extractFirstImageUrl(rawHtml)
        val isoDate = published?.value ?: updated?.value ?: ""
        val formattedDate = formatBloggerDate(isoDate)
        val author = author?.firstOrNull()?.name?.value?.trim().orEmpty()
            .ifBlank { "Law Order and Civil Rights" }
        val cats = (category ?: emptyList())
            .mapNotNull { it.term?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val alternateUrl = link?.firstOrNull { it.rel == "alternate" }?.href
            ?: "https://locrights-23.blogspot.com/"

        return Article(
            id = cleanId,
            title = title,
            summary = excerpt,
            contentHtml = rawHtml,
            thumbnailUrl = thumb,
            publishedIso = isoDate,
            formattedDate = formattedDate,
            authorName = author,
            categories = cats,
            url = alternateUrl,
            readingTimeMinutes = HtmlParser.estimateReadingTimeMinutes(plain)
        )
    }

    private fun formatBloggerDate(iso: String): String {
        if (iso.isBlank()) return ""
        return runCatching {
            val zdt = ZonedDateTime.parse(iso)
            zdt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
        }.getOrElse {
            iso.take(10)
        }
    }
}

private fun Article.toBookmarkEntity() = BookmarkEntity(
    id = id,
    title = title,
    summary = summary,
    contentHtml = contentHtml,
    thumbnailUrl = thumbnailUrl,
    publishedIso = publishedIso,
    formattedDate = formattedDate,
    authorName = authorName,
    categoriesCsv = categories.joinToString("\u001F"),
    url = url,
    readingTimeMinutes = readingTimeMinutes,
    savedAtEpochMs = System.currentTimeMillis()
)

private fun BookmarkEntity.toDomain() = Article(
    id = id,
    title = title,
    summary = summary,
    contentHtml = contentHtml,
    thumbnailUrl = thumbnailUrl,
    publishedIso = publishedIso,
    formattedDate = formattedDate,
    authorName = authorName,
    categories = categoriesCsv.split("\u001F").filter { it.isNotBlank() },
    url = url,
    readingTimeMinutes = readingTimeMinutes
)

private fun Article.toCachedEntity() = CachedArticleEntity(
    id = id,
    title = title,
    summary = summary,
    contentHtml = contentHtml,
    thumbnailUrl = thumbnailUrl,
    publishedIso = publishedIso,
    formattedDate = formattedDate,
    authorName = authorName,
    categoriesCsv = categories.joinToString("\u001F"),
    url = url,
    readingTimeMinutes = readingTimeMinutes,
    cachedAtEpochMs = System.currentTimeMillis()
)

private fun CachedArticleEntity.toDomain() = Article(
    id = id,
    title = title,
    summary = summary,
    contentHtml = contentHtml,
    thumbnailUrl = thumbnailUrl,
    publishedIso = publishedIso,
    formattedDate = formattedDate,
    authorName = authorName,
    categories = categoriesCsv.split("\u001F").filter { it.isNotBlank() },
    url = url,
    readingTimeMinutes = readingTimeMinutes
)
