package com.arcxp.content.retrofit

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.contentConfig
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @suppress
 */
interface NavigationService {

    @Keep
    @GET("/arc/outboundfeeds/navigation/{endpoint}/")
    suspend fun getSectionList(@Path("endpoint") endpoint: String = contentConfig().navigationEndpoint): Response<ResponseBody>

}