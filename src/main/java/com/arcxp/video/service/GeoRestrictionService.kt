package com.arcxp.video.service

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Retrofit interface class that defines the API call for georestricted calls
 * @suppress
 */
interface GeoRestrictionService {
    @Keep
    @GET("/video/v1/ansvideos/findByUuid")
    fun findByUuidGeo(@Query("uuid") uuid: String): Call<ResponseBody>
}