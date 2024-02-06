package com.arcxp.video.api

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.Success
import com.arcxp.video.ArcVideoPlaylistCallback
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.model.*
import com.arcxp.video.service.AkamaiService
import com.arcxp.video.service.ArcMediaClientService
import com.arcxp.video.service.VirtualChannelService
import com.arcxp.video.util.RetrofitController.akamaiService
import com.arcxp.video.util.RetrofitController.baseService
import com.arcxp.video.util.RetrofitController.virtualChannelService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoApiManager(
    private val orgName: String = "",
    private val environmentName: String = "",
    private val baseUrl: String = "$orgName${if (environmentName.isNotBlank()) "-$environmentName" else ""}",
    private val baseService: ArcMediaClientService = baseService(
        orgName = orgName,
        environmentName = environmentName,
        baseUrl = baseUrl
    ),
    private val akamaiService: AkamaiService = akamaiService(
        orgName = orgName,
        environmentName = environmentName,
        baseUrl = baseUrl
    ),
    private val virtualChannelService: VirtualChannelService = virtualChannelService(
        orgName = orgName,
        environmentName = environmentName,
        baseUrl = baseUrl
    )
) {
    /** makes call to endpoint(either normal or geo or virtual channel) for single uuid result */
    fun findByUuidApi(
        uuid: String,
        listener: ArcVideoStreamCallback,
        shouldUseVirtualChannel: Boolean = false
    ) {
        when {
            shouldUseVirtualChannel -> {
                virtualChannelService.findByUuidVirtual(uuid)
                    .enqueue(object : Callback<ArcVideoStreamVirtualChannel> {
                        override fun onResponse(
                            call: Call<ArcVideoStreamVirtualChannel>,
                            response: Response<ArcVideoStreamVirtualChannel>
                        ) {
                            if (response.isSuccessful) {
                                listener.onVideoStreamVirtual(arcVideoStreamVirtualChannel = response.body())
                            } else {
                                handleError(response = response, listener = listener)
                            }
                        }

                        override fun onFailure(
                            call: Call<ArcVideoStreamVirtualChannel>,
                            t: Throwable
                        ) {
                            listener.onError(
                                ArcXPSDKErrorType.SOURCE_ERROR,
                                "Error in call to findByUuidVirtual()",
                                t
                            )
                        }
                    })
            }

            else -> {
                akamaiService.findByUuid(uuid)
                    .enqueue(object : Callback<ResponseBody> {
                        @OptIn(UnstableApi::class)
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val arcVideoResponse = ArcVideoResponse(null, null)

                                //response.body().string() can only be accessed once, so we cache
                                val result = response.body()!!.string()
                                try {
                                    arcVideoResponse.arcTypeResponse =
                                        fromJson(result, ArcTypeResponse::class.java)!!
                                    listener.onError(
                                        type = ArcXPSDKErrorType.SOURCE_ERROR,
                                        message = "This Geo-restricted content is not allowed in region: ${arcVideoResponse.arcTypeResponse?.computedLocation?.country}",
                                        value = arcVideoResponse.arcTypeResponse
                                    )
                                } catch (e: Exception) {
                                    try {
                                        arcVideoResponse.arcVideoStreams = fromJsonList(
                                            result,
                                            ArcVideoStream::class.java
                                        )!!
                                        listener.onVideoResponse(arcVideoResponse = arcVideoResponse)
                                        listener.onVideoStream(videos = arcVideoResponse.arcVideoStreams)
                                    } catch (e: Exception) {
                                        listener.onError(
                                            type = ArcXPSDKErrorType.SOURCE_ERROR,
                                            message = "Bad result from geo restricted video call to findByUuid()",
                                            value = e
                                        )
                                    }
                                }
                            } else {
                                handleError(response = response, listener = listener)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            listener.onError(
                                ArcXPSDKErrorType.SOURCE_ERROR,
                                "Error in geo restricted video call to findByUuid()",
                                t
                            )
                        }
                    })
            }
        }

    }

    /** makes call to base endpoint for uuid result list */
    fun findByUuidsApi(listener: ArcVideoStreamCallback, uuids: List<String>) {
        val call = akamaiService.findByUuids(uuids = uuids)
        call.enqueue(object : Callback<List<ArcVideoStream>> {
            override fun onResponse(
                call: Call<List<ArcVideoStream>>,
                response: Response<List<ArcVideoStream>>
            ) {
                if (response.isSuccessful) {
                    listener.onVideoStream(response.body())
                } else {
                    handleError(response = response, listener = listener)
                }
            }

            override fun onFailure(call: Call<List<ArcVideoStream>>, t: Throwable) {
                listener.onError(
                    ArcXPSDKErrorType.SOURCE_ERROR,
                    "Error in call to findByUuids()",
                    t
                )
            }
        })
    }

    /** makes call to base endpoint for playlist result */
    fun findByPlaylistApi(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        akamaiService.findByPlaylist(name = name, count = count)
            .enqueue(object : Callback<ArcVideoPlaylist> {
                override fun onResponse(
                    call: Call<ArcVideoPlaylist>,
                    response: Response<ArcVideoPlaylist>
                ) {
                    if (response.isSuccessful) {
                        listener.onVideoPlaylist(response.body())
                    } else {
                        listener.onError(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = formatErrorMessage(response = response),
                            value = response
                        )
                    }
                }

                override fun onFailure(call: Call<ArcVideoPlaylist>, t: Throwable) {
                    listener.onError(
                        ArcXPSDKErrorType.SERVER_ERROR,
                        "Error in call to findByPlaylist()",
                        t
                    )
                }
            })
    }

    suspend fun findLiveSuspend(): Either<ArcXPException, List<VideoVO>> {
        try {
            val response = baseService.findLiveSuspend()
            return if (response.isSuccessful) {

                val json = response.body()!!.string()
                val result = fromJson(
                    json,
                    Array<VideoVO>::class.java
                )!!.toList()
                Success(result)

            } else {
                Failure(
                    ArcXPException(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = "Find Live Failed"
                    )
                )
            }
        } catch (e: Exception) {
            return Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "Find Live Exception"
                )
            )
        }

    }

    fun findLive(listener: ArcVideoStreamCallback) =
        baseService.findLive().enqueue(object : Callback<List<VideoVO>> {
            override fun onResponse(
                call: Call<List<VideoVO>>,
                response: Response<List<VideoVO>>
            ) {
                if (response.isSuccessful) {
                    listener.onLiveVideos(response.body())
                } else {
                    listener.onError(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = formatErrorMessage(response = response),
                        value = response
                    )
                }
            }

            override fun onFailure(call: Call<List<VideoVO>>, t: Throwable) {
                listener.onError(
                    ArcXPSDKErrorType.SERVER_ERROR,
                    "Error in call to findLive()",
                    t
                )
            }
        })

    private fun <T : Any> formatErrorMessage(response: Response<T>) =
        "${response.code()}: ${
            when (response.code()) {
                401 -> "Unauthorized"
                403 -> "Forbidden"
                404 -> "Not Found"
                else -> response.message()
            }
        }"

    private fun <T : Any> handleError(response: Response<T>, listener: ArcVideoStreamCallback) {
        listener.onError(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = formatErrorMessage(response = response),
            value = response
        )
    }
}