package com.arcxp.content.retrofit

import com.arcxp.commons.retrofit.NetworkController
import com.arcxp.commons.retrofit.NetworkController.moshiConverter
import retrofit2.Retrofit

/**
 * RetrofitController is a utility object that provides methods to create instances of Retrofit services
 * for interacting with the ArcXP content and navigation APIs.
 * It uses a common Retrofit configuration with a base URL, a Moshi converter, and a shared OkHttp client.
 *
 * The object defines the following methods:
 * - getContentService: Creates an instance of ContentService for content-related API calls.
 * - getNavigationService: Creates an instance of NavigationService for navigation-related API calls.
 *
 * Usage:
 * - Use the provided methods to obtain Retrofit service instances for making network requests.
 *
 * Example:
 *
 * val contentService = RetrofitController.getContentService("https://api.example.com/")
 * val navigationService = RetrofitController.getNavigationService("https://api.example.com/")
 *
 * Note: Ensure that the base URL is correctly configured before using the service methods.
 *
 * @method getContentService Creates an instance of ContentService for content-related API calls.
 * @method getNavigationService Creates an instance of NavigationService for navigation-related API calls.
 */
object RetrofitController {

    fun getContentService(baseUrl: String): ContentService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(moshiConverter)
        .client(NetworkController.client)
        .build()
        .create(ContentService::class.java)

    fun getNavigationService(baseUrl: String) : NavigationService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(moshiConverter)
            .client(NetworkController.client)
            .build()
            .create(NavigationService::class.java)
    }

}