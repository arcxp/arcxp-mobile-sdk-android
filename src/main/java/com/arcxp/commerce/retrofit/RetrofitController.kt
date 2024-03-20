package com.arcxp.commerce.retrofit

import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.models.ArcXPAuthRequest
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commons.util.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @suppress
 */
object RetrofitController {

    private fun okHttpClientNoAuth(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("User-Agent", "ArcXP-Mobile Android")
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
        .build()

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("User-Agent", "ArcXP-Mobile Android")
            AuthManager.getInstance().accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }
        .authenticator(AuthAuthenticator())
        .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
        .build()

    private fun okHttpClientSales(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            AuthManager.getInstance().accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
        .authenticator(AuthAuthenticator())
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
                    .addHeader("User-Agent", "ArcXP-Mobile Android")
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()

    fun getIdentityService(): IdentityService = Retrofit.Builder()
        .baseUrl(AuthManager.getInstance().identityBaseUrl)
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityService::class.java)

    fun getIdentityServiceNoAuth(): IdentityServiceNoAuth = Retrofit.Builder()
        .baseUrl(AuthManager.getInstance().identityBaseUrl)
        .client(okHttpClientNoAuth())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityServiceNoAuth::class.java)

    fun makeTestCall(org: String, site: String, env: String): IdentityService = Retrofit.Builder()
        .baseUrl("https://api-${org}-${site}-${env}.cdn.arcpublishing.com/identity/public/v1/")
        .client(testCall())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityService::class.java)

    fun getIdentityServiceForApple(): IdentityServiceNoAuth = Retrofit.Builder()
        .baseUrl(AuthManager.getInstance().identityBaseUrlApple)
        .client(okHttpClientApple())
        .addConverterFactory(GsonConverterFactory.create()).build().create(IdentityServiceNoAuth::class.java)

    fun getSalesService(): SalesService = Retrofit.Builder()
        .baseUrl(AuthManager.getInstance().salesBaseUrl)
        .client(okHttpClientSales())
        .addConverterFactory(GsonConverterFactory.create()).build()
        .create(SalesService::class.java)

    fun getRetailService(): RetailService = Retrofit.Builder()
        .baseUrl(AuthManager.getInstance().retailBaseUrl)
        .client(okHttpClient())
        .addConverterFactory(GsonConverterFactory.create()).build().create(RetailService::class.java)

    class AuthAuthenticator (): Authenticator {
        private val maxRetries = 1
        private var responseCount = 0
        override fun authenticate(route: Route?, response: Response): Request? {

            if (responseCount >= maxRetries) {
                return null // Maximum retries reached, give up.
            } else {
                responseCount++
            }

            val token = AuthManager.getInstance().refreshToken
            return runBlocking {
                val newToken = getNewToken(token)
                if (!newToken.isSuccessful || newToken.body() == null) { //Couldn't refresh the token, so restart the login process
                    AuthManager.getInstance().accessToken = ""
                    AuthManager.getInstance().refreshToken = ""
                    return@runBlocking null // Or handle failure accordingly
                }
                newToken.body()?.let {
                    AuthManager.getInstance().accessToken = it.accessToken
                    AuthManager.getInstance().refreshToken = it.refreshToken
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer ${it.accessToken}")
                        .build()
                }
                null
            }
        }

        private suspend fun getNewToken(refreshToken: String?): retrofit2.Response<ArcXPAuth> {
            return getIdentityServiceNoAuth().refreshToken(
                ArcXPAuthRequest(
                    token = refreshToken,
                    grantType = ArcXPAuthRequest.Companion.GrantType.REFRESH_TOKEN.value
                )
            )
        }
    }
}
