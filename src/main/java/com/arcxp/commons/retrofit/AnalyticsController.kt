package com.arcxp.commons.retrofit

import com.arcxp.commons.service.AnalyticsService
import com.arcxp.commons.util.MoshiController.moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * @suppress
 */
object AnalyticsController {

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader("Content-Type", "application/json")
//                    .addHeader("Arc-Organization", Constants.SERVER_ORG)
//                    .addHeader("Arc-Site", Constants.SERVER_SITE)
                    requestBuilder.addHeader("Authorization", "Splunk 2044ec87-885f-4495-8d94-4e74ae7a9db2")

                chain.proceed(requestBuilder.build())
            }
            //.connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()



    fun getAnalyticsService(): AnalyticsService = Retrofit.Builder()
        .baseUrl("https://hec.washpost.com:443")
        .client(okHttpClient())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build().create(AnalyticsService::class.java)

    fun makeTestCall(org: String, site: String, env: String): AnalyticsService = Retrofit.Builder().baseUrl("https://hec.washpost.com:443")
        .client(testCall()).build().create(AnalyticsService::class.java)

    private fun testCall(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }
        //.connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
        .build()
}
