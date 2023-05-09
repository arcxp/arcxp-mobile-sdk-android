package com.arcxp.content.retrofit

import com.arcxp.ArcXPMobileSDK.baseUrl
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

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            AuthManager.accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
        .build()

    private fun testCall(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
        .build()


    fun getContentService(): ContentService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(moshiConverter)
        .build()
        .create(ContentService::class.java)

    fun navigationService() : NavigationService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(moshiConverter)
            .build()
            .create(NavigationService::class.java)
    }

}