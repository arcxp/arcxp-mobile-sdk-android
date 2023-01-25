package com.arcxp.content.retrofit

import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.util.AuthManager
import com.arcxp.content.sdk.util.Constants
import com.arcxp.content.sdk.util.MoshiController
import com.arcxp.content.sdk.util.MoshiController.moshi
import com.squareup.moshi.Moshi
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
        .baseUrl(ArcXPContentSDK.arcxpContentConfig().baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ContentService::class.java)

    fun navigationService() : NavigationService {
        return Retrofit.Builder()
            .baseUrl(ArcXPContentSDK.arcxpContentConfig().baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NavigationService::class.java)
    }

    fun getAnalyticsService(): AnalyticsService = Retrofit.Builder()
        .baseUrl("https://hec.washpost.com:443")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AnalyticsService::class.java)

}