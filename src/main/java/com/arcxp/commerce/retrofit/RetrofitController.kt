package com.arcxp.commerce.retrofit

import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @suppress
 */
object RetrofitController {

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader("Content-Type", "application/json")
//                    .addHeader("Arc-Organization", Constants.SERVER_ORG)
//                    .addHeader("Arc-Site", Constants.SERVER_SITE)
                    AuthManager.getInstance().accessToken?.let {
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

    private fun okHttpClientApple(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader("Content-Type", "application/json")
                        .addHeader("Arc-Organization", "staging")
                        .addHeader("Arc-Site", "staging")
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()

    fun getIdentityService(): IdentityService = Retrofit.Builder().baseUrl(
        AuthManager.getInstance().identityBaseUrl
    )
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityService::class.java)

    fun makeTestCall(org: String, site: String, env: String): IdentityService = Retrofit.Builder().baseUrl(
        "https://api-${org}-${site}-${env}.cdn.arcpublishing.com/identity/public/v1/"
    )
        .client(testCall())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityService::class.java)

    fun getIdentityServiceForApple(): IdentityService = Retrofit.Builder().baseUrl(
            AuthManager.getInstance().identityBaseUrlApple
    )
            .client(okHttpClientApple())
            .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityService::class.java)

    fun getSalesService(): SalesService = Retrofit.Builder().baseUrl(
        AuthManager.getInstance().salesBaseUrl
    )
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build().create(SalesService::class.java)

    fun getRetailService(): RetailService = Retrofit.Builder().baseUrl(
        AuthManager.getInstance().retailBaseUrl
    )
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build().create(RetailService::class.java)
}
