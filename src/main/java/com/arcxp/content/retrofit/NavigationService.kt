package com.arcxp.content.retrofit

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @suppress
 */
interface NavigationService {

    @Keep
    @GET("/arc/outboundfeeds/navigation/{siteServiceHierarchy}/")
    suspend fun getSectionList(@Path("siteServiceHierarchy") siteServiceHierarchy: String): Response<ResponseBody>

}