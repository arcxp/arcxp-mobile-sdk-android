package com.arcxp.content.retrofit

import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.commons.retrofit.NetworkController
import com.arcxp.commons.retrofit.NetworkController.moshiConverter
import retrofit2.Retrofit

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

    fun getNavigationService() : NavigationService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(moshiConverter)
            .client(NetworkController.client)
            .build()
            .create(NavigationService::class.java)
    }

}