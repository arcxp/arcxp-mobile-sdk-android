package com.arcxp.video.service

import androidx.annotation.Keep
import com.arcxp.video.model.ArcVideoPlaylist
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Retrofit interface class that defines the API call for akamai calls
 */
interface AkamaiService {

    @Keep
    @GET("/video/v1/ansvideos/findByUuid")
    fun findByUuid(@Query("uuid") uuid: String): Call<ResponseBody>

    @Keep
    @GET("/video/v1/ans/playlists/findByPlaylist")
    fun findByPlaylist(
        @Query("name") name: String,
        @Query("count") count: Int
    ): Call<ArcVideoPlaylist>

    @Keep
    @GET("/video/v1/ansvideos/findByUuid")
    fun findByUuidAsJson(@Query("uuid") uuid: String): Call<ResponseBody>

    @Keep
    @GET("/video/v1/ans/playlists/findByPlaylist")
    fun findByPlaylistAsJson(
        @Query("name") name: String,
        @Query("count") count: Int
    ): Call<ResponseBody>
}