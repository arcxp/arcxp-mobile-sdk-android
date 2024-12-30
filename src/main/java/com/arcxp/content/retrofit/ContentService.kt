package com.arcxp.content.retrofit

import androidx.annotation.Keep
import com.arcxp.content.extendedModels.ArcXPContentElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ContentService is an interface that defines the API endpoints for interacting with the ArcXP content services.
 * It provides methods to fetch content, collections, and perform searches using Retrofit.
 *
 * The interface defines the following operations:
 * - Fetch a specific content item by its ID
 * - Fetch a collection of content items by its ID
 * - Fetch a full collection of content items by its ID
 * - Search for content items based on search terms
 * - Search for video content items based on search terms
 *
 * Usage:
 * - Implement this interface using Retrofit to make network calls to the ArcXP content services.
 *
 * Example:
 *
 * val contentService = retrofit.create(ContentService::class.java)
 * val contentResponse = contentService.getContent("contentId")
 * val collectionResponse = contentService.getCollection("collectionId", 10, 0)
 * val searchResponse = contentService.search("searchTerms", 10, 0)
 *
 * Note: Ensure that the Retrofit instance is properly configured before using ContentService.
 *
 * @method getContent Fetch a specific content item by its ID.
 * @method getCollection Fetch a collection of content items by its ID.
 * @method getCollectionFull Fetch a full collection of content items by its ID.
 * @method search Search for content items based on search terms.
 * @method searchAsJson Search for content items and return the result as a JSON string.
 * @method searchVideos Search for video content items based on search terms.
 */
interface ContentService {

    @Keep
    @GET("/arc/outboundfeeds/article")
    suspend fun getContent(@Query("_id") id: String): Response<ResponseBody>

    /**
     * returns no content-elements
     */
    @Keep
    @GET("/arc/outboundfeeds/collection/{id}")
    suspend fun getCollection(
        @Path("id") id: String,
        @Query("size") size: Int,
        @Query("from") from: Int
        ): Response<ResponseBody>

    /**
     * returns content-elements
     */
    @Keep
    @GET("/arc/outboundfeeds/collection-full/{id}")
    suspend fun getCollectionFull(
        @Path("id") id: String,
        @Query("size") size: Int,
        @Query("from") from: Int
        ): Response<ResponseBody>

    @Keep
    @GET("/arc/outboundfeeds/search/{searchTerms}/")
    suspend fun search(
        @Path("searchTerms") searchTerms: String,
        @Query("size") size: Int,
        @Query("from") from: Int
    ): Response<List<ArcXPContentElement>>
    @Keep
    @GET("/arc/outboundfeeds/search/{searchTerms}/")
    suspend fun searchAsJson(
        @Path("searchTerms") searchTerms: String,
        @Query("size") size: Int,
        @Query("from") from: Int
    ): Response<ResponseBody>

    @Keep
    @GET("/arc/outboundfeeds/searchVideo/{searchTerms}/")
    suspend fun searchVideos(
        @Path("searchTerms") searchTerms: String,
        @Query("size") size: Int,
        @Query("from") from: Int
    ): Response<List<ArcXPContentElement>>
}