package com.arcxp.video.service

import androidx.annotation.Keep
import com.arcxp.video.model.VideoVO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

/**
 * Retrofit interface class that defines the API for the ArcMediaClient
 */
interface ArcMediaClientService {

    @Keep
    @GET("/api/v1/generic/findLive")
    fun findLive(): Call<List<VideoVO>>

    @Keep
    @GET("/api/v1/generic/findLive")
    suspend fun findLiveSuspend(): Response<ResponseBody>

    @Keep
    @GET("/api/v1/generic/findLive")
    fun findLiveAsJson(): Call<ResponseBody>

}