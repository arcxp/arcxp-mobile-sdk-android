package com.arcxp.video.service

import androidx.annotation.Keep
import com.arcxp.video.model.ArcVideoPlaylist
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.VideoVO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface class that defines the API for the ArcMediaClient
 * @suppress
 */
interface ArcMediaClientService {

    @Keep
    @GET("/api/v1/generic/findLive")
    fun findLive(): Call<List<VideoVO>>

    @Keep
    @GET("/api/v1/generic/findLive")
    suspend fun findLiveSuspend(): Response<ResponseBody>

}