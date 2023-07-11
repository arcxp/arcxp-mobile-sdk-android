package com.arcxp.content.retrofit

import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.commons.retrofit.NetworkController
import com.arcxp.commons.retrofit.NetworkController.moshiConverter
import com.arcxp.content.util.AuthManager
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.MoshiController.moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @suppress
 */
object RetrofitController {

    fun getContentService(): ContentService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(moshiConverter)
        .client(NetworkController.client)
        .build()
        .create(ContentService::class.java)

    fun navigationService() : NavigationService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(moshiConverter)
            .client(NetworkController.client)
            .build()
            .create(NavigationService::class.java)
    }

}