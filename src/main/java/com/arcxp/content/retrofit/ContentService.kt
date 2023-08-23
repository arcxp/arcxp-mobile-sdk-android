package com.arcxp.content.retrofit

import androidx.annotation.Keep
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.extendedModels.ArcXPContentElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * @suppress
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
    suspend fun searchCollection(
        @Path("searchTerms") searchTerms: String,
        @Query("size") size: Int,
        @Query("from") from: Int
    ): Response<List<ArcXPCollection>>

    @Keep
    @GET("/arc/outboundfeeds/searchVideo/{searchTerms}/")
    suspend fun searchVideos(
        @Path("searchTerms") searchTerms: String,
        @Query("size") size: Int,
        @Query("from") from: Int
    ): Response<List<ArcXPContentElement>>
}