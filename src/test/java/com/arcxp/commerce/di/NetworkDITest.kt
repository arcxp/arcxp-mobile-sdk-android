package com.arcxp.commerce.di

import com.arcxp.commerce.retrofit.IdentityService
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.SalesService
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Network module test configuration with mock server url.
 */
fun configureNetworkModuleForTest(baseApi : String)
        = module {
    single {
        Retrofit.Builder()
            .baseUrl(baseApi)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    factory { get<Retrofit>().create(IdentityService::class.java) }
    factory { get<Retrofit>().create(SalesService::class.java) }
    factory { get<Retrofit>().create(RetailService::class.java)}
}
