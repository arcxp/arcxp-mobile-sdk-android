package com.arcxp.video.service

import androidx.annotation.Keep
import com.arcxp.video.model.ArcVideoStreamVirtualChannel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Retrofit interface class that defines the API for the ArcMediaClient virtual channel calls
 */
interface VirtualChannelService {

    @Keep
    @GET("/v1/virtual-channels/{uuid}")
    fun findByUuidVirtual(@Path("uuid")uuid: String) : Call<ArcVideoStreamVirtualChannel>
    @Keep
    @GET("/v1/virtual-channels/{uuid}")
    fun findByUuidVirtualAsJson(@Path("uuid")uuid: String) : Call<ResponseBody>
}