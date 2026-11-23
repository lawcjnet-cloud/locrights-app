package com.locrights.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BloggerApiService {

    @GET("feeds/posts/default")
    suspend fun getPosts(
        @Query("alt") alt: String = "json",
        @Query("start-index") startIndex: Int = 1,
        @Query("max-results") maxResults: Int = 15,
        @Query("q") query: String? = null
    ): BloggerFeedResponse

    @GET("feeds/posts/default/-/{label}")
    suspend fun getPostsByCategory(
        @Path(value = "label", encoded = false) label: String,
        @Query("alt") alt: String = "json",
        @Query("start-index") startIndex: Int = 1,
        @Query("max-results") maxResults: Int = 15
    ): BloggerFeedResponse

    @GET("feeds/pages/default")
    suspend fun getPages(
        @Query("alt") alt: String = "json",
        @Query("max-results") maxResults: Int = 20
    ): BloggerFeedResponse
}
