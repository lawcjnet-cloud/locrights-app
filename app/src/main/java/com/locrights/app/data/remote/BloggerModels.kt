package com.locrights.app.data.remote

import com.google.gson.annotations.SerializedName

data class BloggerFeedResponse(
    val feed: BloggerFeedDto?
)

data class BloggerFeedDto(
    val title: TextValueDto?,
    val subtitle: TextValueDto?,
    val category: List<CategoryTermDto>?,
    val entry: List<BloggerEntryDto>?,
    @SerializedName("openSearch\$totalResults")
    val totalResults: TextValueDto?
)

data class TextValueDto(
    @SerializedName("\$t")
    val value: String?
)

data class CategoryTermDto(
    val term: String?
)

data class BloggerAuthorDto(
    val name: TextValueDto?
)

data class BloggerLinkDto(
    val rel: String?,
    val type: String?,
    val href: String?,
    val title: String?
)

data class MediaThumbnailDto(
    val url: String?,
    val width: String?,
    val height: String?
)

data class BloggerEntryDto(
    val id: TextValueDto?,
    val published: TextValueDto?,
    val updated: TextValueDto?,
    val title: TextValueDto?,
    val content: TextValueDto?,
    val summary: TextValueDto?,
    val category: List<CategoryTermDto>?,
    val author: List<BloggerAuthorDto>?,
    val link: List<BloggerLinkDto>?,
    @SerializedName("media\$thumbnail")
    val mediaThumbnail: MediaThumbnailDto?
)
