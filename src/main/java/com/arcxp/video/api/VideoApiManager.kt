package com.arc.arcvideo.api

import com.arc.arcvideo.ArcException
import com.arc.arcvideo.ArcVideoPlaylistCallback
import com.arc.arcvideo.ArcVideoStreamCallback
import com.arc.arcvideo.model.*
import com.arc.arcvideo.model.ArcVideoSDKErrorType.*
import com.arc.arcvideo.service.ArcMediaClientService
import com.arc.arcvideo.service.GeoRestrictionService
import com.arc.arcvideo.service.VirtualChannelService
import com.arc.arcvideo.util.Either
import com.arc.arcvideo.util.Failure
import com.arc.arcvideo.util.MoshiController.fromJson
import com.arc.arcvideo.util.RetrofitController.baseService
import com.arc.arcvideo.util.RetrofitController.geoRestrictedService
import com.arc.arcvideo.util.RetrofitController.virtualChannelService
import com.arc.arcvideo.util.Success
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
    private val geoRestrictedService: GeoRestrictionService = geoRestrictedService(
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
        checkGeoRestriction: Boolean = false,
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
                                SOURCE_ERROR,
                                "Error in call to findByUuidVirtual()",
                                t
                            )
                        }
                    })
            }
            checkGeoRestriction -> {
                geoRestrictedService.findByUuidGeo(uuid)
                    .enqueue(object : Callback<ResponseBody> {
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
                                        type = SOURCE_ERROR,
                                        message = "This Geo-restricted content is not allowed in region: ${arcVideoResponse.arcTypeResponse?.computedLocation?.country}",
                                        value = arcVideoResponse.arcTypeResponse
                                    )
                                } catch (e: Exception) {
                                    try {
                                        arcVideoResponse.arcVideoStreams = fromJson(
                                            result,
                                            Array<ArcVideoStream>::class.java
                                        )!!.toList()
                                        listener.onVideoResponse(arcVideoResponse = arcVideoResponse)
                                        listener.onVideoStream(videos = arcVideoResponse.arcVideoStreams)
                                    } catch (e: Exception) {
                                        listener.onError(
                                            type = SOURCE_ERROR,
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
                                SOURCE_ERROR,
                                "Error in geo restricted video call to findByUuid()",
                                t
                            )
                        }
                    })
            }
            else -> {
                baseService.findByUuid(uuid).enqueue(object : Callback<List<ArcVideoStream>> {
                    override fun onResponse(
                        call: Call<List<ArcVideoStream>>,
                        response: Response<List<ArcVideoStream>>
                    ) {
                        if (response.isSuccessful) {
                            listener.onVideoStream(videos = response.body())
                        } else {
                            handleError(response = response, listener = listener)
                        }
                    }

                    override fun onFailure(call: Call<List<ArcVideoStream>>, t: Throwable) {
                        listener.onError(
                            SOURCE_ERROR,
                            "Error in call to findByUuid()",
                            t
                        )
                    }
                })
            }
        }

    }

    /** makes call to base endpoint for uuid result list */
    fun findByUuidsApi(listener: ArcVideoStreamCallback, uuids: List<String>) {
        val call = baseService.findByUuids(uuids = uuids)
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
                    SOURCE_ERROR,
                    "Error in call to findByUuids()",
                    t
                )
            }
        })
    }

    /** makes call to base endpoint for playlist result */
    fun findByPlaylistApi(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        baseService.findByPlaylist(name = name, count = count)
            .enqueue(object : Callback<ArcVideoPlaylist> {
                override fun onResponse(
                    call: Call<ArcVideoPlaylist>,
                    response: Response<ArcVideoPlaylist>
                ) {
                    if (response.isSuccessful) {
                        listener.onVideoPlaylist(response.body())
                    } else {
                        listener.onError(
                            type = SERVER_ERROR,
                            message = formatErrorMessage(response = response),
                            value = response
                        )
                    }
                }

                override fun onFailure(call: Call<ArcVideoPlaylist>, t: Throwable) {
                    listener.onError(
                        SERVER_ERROR,
                        "Error in call to findByPlaylist()",
                        t
                    )
                }
            })
    }

    suspend fun findLiveSuspend(): Either<ArcException, List<VideoVO>> {
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
                    ArcException(
                        type = SERVER_ERROR,
                        message = "Find Live Failed"
                    )
                )
            }
        } catch (e: Exception) {
            return Failure(
                ArcException(
                    type = SERVER_ERROR,
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
                        type = SERVER_ERROR,
                        message = formatErrorMessage(response = response),
                        value = response
                    )
                }
            }

            override fun onFailure(call: Call<List<VideoVO>>, t: Throwable) {
                listener.onError(
                    SERVER_ERROR,
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
            type = SERVER_ERROR,
            message = formatErrorMessage(response = response),
            value = response
        )
    }
}