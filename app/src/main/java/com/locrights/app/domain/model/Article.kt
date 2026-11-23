package com.locrights.app.domain.model

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val contentHtml: String,
    val thumbnailUrl: String?,
    val publishedIso: String,
    val formattedDate: String,
    val authorName: String,
    val categories: List<String>,
    val url: String,
    val readingTimeMinutes: Int
)

data class BlogPage(
    val id: String,
    val title: String,
    val contentHtml: String,
    val url: String
)

data class CategoryInfo(
    val name: String,
    val count: Int
)

data class FeedPageResult(
    val articles: List<Article>,
    val categories: List<String>,
    val totalResults: Int,
    val fromCache: Boolean
)
