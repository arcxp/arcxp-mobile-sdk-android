package com.arc.arcvideo.service

import androidx.annotation.Keep
import com.arc.arcvideo.model.ArcVideoStreamVirtualChannel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Retrofit interface class that defines the API for the ArcMediaClient virtual channel calls
 * @suppress
 */
interface VirtualChannelService {

    @Keep
    @GET("/v1/virtual-channels/{uuid}")
    fun findByUuidVirtual(@Path("uuid")uuid: String) : Call<ArcVideoStreamVirtualChannel>
}