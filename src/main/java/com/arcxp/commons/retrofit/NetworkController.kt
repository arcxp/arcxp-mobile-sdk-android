package com.arcxp.commons.retrofit

import com.arcxp.commons.util.MoshiController.moshi
import okhttp3.OkHttpClient
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkController {

    val client : OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .header("User-Agent", "ArcXP-Mobile Android")
                    .build()
            )
        }
        .build()
    val moshiConverter: MoshiConverterFactory = MoshiConverterFactory.create(moshi)
}