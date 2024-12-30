package com.arcxp.video.api

import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.MoshiController.fromJsonList
import com.arcxp.commons.util.Success
import com.arcxp.sdk.R
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

/**
 * VideoApiManager is a class that manages API calls related to video content within the ArcXP platform.
 * It handles operations for fetching video streams, playlists, and live video content using Retrofit services.
 *
 * The class defines the following properties:
 * - orgName: The organization name used in the API base URL.
 * - environmentName: The environment name used in the API base URL.
 * - baseUrl: The base URL for the API endpoints.
 * - baseService: The Retrofit service for base video API calls.
 * - akamaiService: The Retrofit service for Akamai video API calls.
 * - virtualChannelService: The Retrofit service for virtual channel video API calls.
 *
 * Usage:
 * - Create an instance of VideoApiManager with the necessary parameters.
 * - Use the provided methods to make API calls for video content.
 *
 * Example:
 *
 * val videoApiManager = VideoApiManager(orgName = "exampleOrg", environmentName = "prod")
 * videoApiManager.findByUuidApi("videoUuid", videoStreamCallback)
 * videoApiManager.findByPlaylistApi("playlistName", 10, videoPlaylistCallback)
 *
 * Note: Ensure that the Retrofit services are properly configured before using VideoApiManager.
 *
 * @property orgName The organization name used in the API base URL.
 * @property environmentName The environment name used in the API base URL.
 * @property baseUrl The base URL for the API endpoints.
 * @property baseService The Retrofit service for base video API calls.
 * @property akamaiService The Retrofit service for Akamai video API calls.
 * @property virtualChannelService The Retrofit service for virtual channel video API calls.
 * @method findByUuidApi Fetches a single video stream by its UUID.
 * @method findByUuidApiAsJson Fetches a single video stream by its UUID and returns the result as JSON.
 * @method findByPlaylistApi Fetches a video playlist by its name.
 * @method findByPlaylistApiAsJson Fetches a video playlist by its name and returns the result as JSON.
 * @method findLiveSuspend Fetches live video content using a suspend function.
 * @method findLiveSuspendAsJson Fetches live video content using a suspend function and returns the result as JSON.
 * @method findLive Fetches live video content.
 * @method findLiveAsJson Fetches live video content and returns the result as JSON.
 */
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
            shouldUseVirtualChannel -> virtualChannelService.findByUuidVirtual(uuid)
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
                            application().getString(R.string.error_in_call_to_findbyuuidvirtual),
                            t
                        )
                    }
                })

            else -> akamaiService.findByUuid(uuid)
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
                                    type = ArcXPSDKErrorType.SOURCE_ERROR,
                                    message = application().getString(
                                        R.string.this_geo_restricted_content_is_not_allowed_in_region,
                                        arcVideoResponse.arcTypeResponse!!.computedLocation.country ?: application().getString(R.string.unknown_country)
                                    ),
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
                                        message = application().getString(R.string.bad_result_from_geo_restricted_video_call_to_findbyuuid),
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
                            application().getString(R.string.error_in_geo_restricted_video_call_to_findbyuuid),
                            t
                        )
                    }
                })
        }
    }

    /** makes call to endpoint(either normal or geo or virtual channel) for single uuid result */
    fun findByUuidApiAsJson(
        uuid: String,
        listener: ArcVideoStreamCallback,
        shouldUseVirtualChannel: Boolean = false
    ) {
        when {
            shouldUseVirtualChannel -> virtualChannelService.findByUuidVirtualAsJson(uuid)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            listener.onJsonResult(json = response.body()!!.string())
                        } else {
                            handleError(response = response, listener = listener)
                        }
                    }

                    override fun onFailure(
                        call: Call<ResponseBody>,
                        t: Throwable
                    ) {
                        listener.onError(
                            ArcXPSDKErrorType.SOURCE_ERROR,
                            application().getString(R.string.error_in_call_to_findbyuuidvirtual),
                            t
                        )
                    }
                })


            else -> akamaiService.findByUuidAsJson(uuid)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {

                            listener.onJsonResult(json = response.body()!!.string())
                        } else {
                            handleError(response = response, listener = listener)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        listener.onError(
                            ArcXPSDKErrorType.SOURCE_ERROR,
                            application().getString(R.string.error_in_geo_restricted_video_call_to_findbyuuid),
                            value = t
                        )
                    }
                })

        }

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
                        application().getString(R.string.error_in_call_to_findbyplaylist),
                        t
                    )
                }
            })
    }

    /** makes call to base endpoint for playlist result */
    fun findByPlaylistApiAsJson(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        akamaiService.findByPlaylistAsJson(name = name, count = count)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        listener.onJsonResult(json = response.body()!!.string())
                    } else {
                        listener.onError(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = formatErrorMessage(response = response),
                            value = response
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    listener.onError(
                        ArcXPSDKErrorType.SERVER_ERROR,
                        application().getString(R.string.error_in_call_to_findbyplaylist),
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
                        message = application().getString(R.string.find_live_failed)
                    )
                )
            }
        } catch (e: Exception) {
            return Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = application().getString(R.string.find_live_exception)
                )
            )
        }

    }

    suspend fun findLiveSuspendAsJson(): Either<ArcXPException, String> {
        val response = baseService.findLiveSuspend()
        return if (response.isSuccessful) {
            Success(success = response.body()!!.string())
        } else {
            Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = application().getString(R.string.find_live_failed)
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
                    application().getString(R.string.find_live_failed),
                    t
                )
            }
        })

    fun findLiveAsJson(listener: ArcVideoStreamCallback) =
        baseService.findLiveAsJson().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    listener.onJsonResult(json = response.body()!!.string())
                } else {
                    listener.onError(
                        type = ArcXPSDKErrorType.SERVER_ERROR,
                        message = formatErrorMessage(response = response),
                        value = response
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onError(
                    ArcXPSDKErrorType.SERVER_ERROR,
                    application().getString(R.string.find_live_failed),
                    t
                )
            }
        })

    private fun <T : Any> formatErrorMessage(response: Response<T>) =
        "${response.code()}: ${
            when (response.code()) {
                401 -> application().getString(R.string.unauthorized)
                403 -> application().getString(R.string.forbidden)
                404 -> application().getString(R.string.not_found)
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