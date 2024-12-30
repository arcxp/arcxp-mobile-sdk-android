package com.arcxp.content.retrofit

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * NavigationService is an interface that defines the API endpoints for interacting with the ArcXP navigation services.
 * It provides methods to fetch navigation-related data, such as section lists, using Retrofit.
 *
 * The interface defines the following operations:
 * - Fetch a list of sections for a given site hierarchy
 *
 * Usage:
 * - Implement this interface using Retrofit to make network calls to the ArcXP navigation services.
 *
 * Example:
 *
 * val navigationService = retrofit.create(NavigationService::class.java)
 * val sectionListResponse = navigationService.getSectionList("siteHierarchy")
 *
 * Note: Ensure that the Retrofit instance is properly configured before using NavigationService.
 *
 * @method getSectionList Fetch a list of sections for a given site hierarchy.
 */
interface NavigationService {

    @Keep
    @GET("/arc/outboundfeeds/navigation/{siteHierarchy}/")
    suspend fun getSectionList(@Path("siteHierarchy") siteHierarchy: String): Response<ResponseBody>

}