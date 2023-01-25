package com.arcxp.video.service

import androidx.annotation.Keep
import com.arc.arcvideo.model.ArcVideoPlaylist
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.model.VideoVO
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
    @GET("/api/v1/ansvideos/findByUuid")
    fun findByUuid(@Query("uuid") uuid: String): Call<List<ArcVideoStream>>

    @Keep
    @GET("/api/v1/ansvideos/findByUuids")
    fun findByUuids(@Query("uuids") uuids: List<String>): Call<List<ArcVideoStream>>

    @Keep
    @GET("/api/v1/ans/playlists/findByPlaylist")
    fun findByPlaylist(
        @Query("name") name: String,
        @Query("count") count: Int
    ): Call<ArcVideoPlaylist>

    @Keep
    @GET("/api/v1/generic/findLive")
    fun findLive(): Call<List<VideoVO>>

    @Keep
    @GET("/api/v1/generic/findLive")
    suspend fun findLiveSuspend(): Response<ResponseBody>

}