package com.arcxp.commons.retrofit

import android.app.Application
import com.arcxp.commons.service.AnalyticsService
import com.arcxp.commons.util.MoshiController.moshi
import com.arcxp.sdk.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AnalyticsController {

    private fun okHttpClient(application: Application): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader("Content-Type", "application/json")
//                requestBuilder.addHeader("Authorization", "Splunk ${application.getString(R.string.splunk_auth_request)}")
                chain.proceed(requestBuilder.build())
            }
            .build()



//    fun getAnalyticsService(application: Application): AnalyticsService = Retrofit.Builder()
//        .baseUrl("https://hec.washpost.com:443")
//        .client(okHttpClient(application = application))
//        .addConverterFactory(MoshiConverterFactory.create(moshi))
//        .build().create(AnalyticsService::class.java)

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
