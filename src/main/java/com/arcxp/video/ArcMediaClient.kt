package com.arcxp.video

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory.createArcXPError
import com.arcxp.commons.util.DependencyFactory.createVideoApiManager
import com.arcxp.commons.util.Either
import com.arcxp.sdk.R
import com.arcxp.video.api.VideoApiManager

/**
 * This class is used to interface with the Arc server.
 * The API allows for the retrieval of video objects and playlist objects.
 * The client uses the methods in this class to return ArcVideoStream objects.
 * These objects can then be used to play videos using the ArcMediaPlayer.
 *
 * ### How does it relate to other important Video SDK classes?
 * This class does not interact with other classes.
 *
 * ### What are the core components that make it up?
 * var arcMediaClientService: ArcMediaClientService - This is the retrofit interface that defines the server calls.
 *
 * ### How is the class used?
 *
 * #### Instantiating the media client
 * ```
 * class MyFragment : Fragment() {
 *
 * override fun onCreate(savedInstanceState: Bundle?) {
 * super.onCreate(savedInstanceState)
 * ArcMediaClient.instantiate("http://baseurl.com")
 * }
 *
 * fun doSomething() {
 * var mediaClient = ArcMediaClient.getInstance()
 * mediaClient.findByUuid(.....)
 * }
 *
 * }
 * ```
 * ```
 * class MyFragment: Fragment() {
 * var client1 : ArcMediaClient? = null
 * var client2: ArcMediaClient? = null
 *
 * fun connect() {
 * client1 = ArcMediaClient.createClient("http://client1base")
 * client2 = ArcMediaClient.createClient("http://client2base")
 * }
 *
 * fun useClient1() {
 * client1.findByUuid(.....)
 * }
 *
 * fun useClient2() {
 * client2.findByUuid(....)
 * }
 * }
 * }
 * ```
 *
 * ```
 * var mediaClient = ArcMediaClient.instantiate("http://baseurl.com")
 *
 * mediaClient.findByUuid("xxxx-xxx-xxxx-xxxx",
 * object : ArcVideoStreamCallback {
 * override fun onVideoStream(videos: List<ArcVideoStream>?) {
 * //play the video stream
 * }
 * }
 *
 * override fun onError(type: ArcXPSDKErrorType, message: String, value: Any?) {
 * //process error
 * }
 * })
 * ```
 *
 */
@Keep
class ArcMediaClient {

    private var baseUrl: String = ""
    private var orgName: String = ""
    private var environmentName: String = ""
    private val videoApiManager: VideoApiManager

    /**
     * @param baseUrl full base url to use
     */
    constructor(baseUrl: String) {
        if (baseUrl.isBlank()) {
            throw createArcXPError(
                type = ArcXPSDKErrorType.INIT_ERROR,
                    message = application().getString(R.string.blank_baseurl_failure)
            )
        }
        this.baseUrl = baseUrl
        videoApiManager = createVideoApiManager(baseUrl = this.baseUrl)
    }

    /**
     * @param orgName Organization name. Provided by Arc
     * @param serverEnvironment Server environment. Production or sandbox or empty (for older orgs that do not use env)
     */
    constructor(orgName: String, serverEnvironment: String) {
        if (orgName.isBlank()) {
            throw createArcXPError(
                type = ArcXPSDKErrorType.INIT_ERROR,
                message = application().getString(R.string.org_failure)
            )
        }
        this.orgName = orgName
        this.environmentName = serverEnvironment
        this.videoApiManager =
            createVideoApiManager(orgName = orgName, environmentName = serverEnvironment)
    }

    /**
     * Returns an array containing a single ArcVideoStream object
     *
     * @param uuid String uuid for the video
     * @param listener [ArcVideoStreamCallback]
     * use [ArcVideoStreamCallback.onVideoStream] or
     * [ArcVideoStreamCallback.onVideoStreamVirtual] for successful results
     * @param shouldUseVirtualChannel Boolean indicator to use virtual channel endpoint
     */
    fun findByUuid(
        uuid: String,
        listener: ArcVideoStreamCallback,
        shouldUseVirtualChannel: Boolean = false
    ) {
        videoApiManager.findByUuidApi(
            uuid,
            listener,
            shouldUseVirtualChannel = shouldUseVirtualChannel
        )
    }

    /**
     * Returns an json representation of array containing a single ArcVideoStream object
     *
     * @param uuid String uuid for the video
     * @param listener [ArcVideoStreamCallback] use [ArcVideoStreamCallback.onJsonResult] for successful results
     * @param shouldUseVirtualChannel Boolean indicator to use virtual channel endpoint
     */
    fun findByUuidAsJson(
        uuid: String,
        listener: ArcVideoStreamCallback,
        shouldUseVirtualChannel: Boolean = false
    ) {
        videoApiManager.findByUuidApiAsJson(
            uuid,
            listener,
            shouldUseVirtualChannel = shouldUseVirtualChannel
        )
    }

    /**
     * Returns an array containing the ArcVideoStream objects for the given UUIDs
     *
     * @param uuids Array of strings with the UUIDs of the videos to retrieve. Use this method
     * if the UUID list is a fixed size array.
     * @param listener [ArcVideoStreamCallback]
     * use [ArcVideoStreamCallback.onVideoStream]
     * or [ArcVideoStreamCallback.onVideoStreamVirtual] for successful results
     */
    fun findByUuids(vararg uuids: String, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApi(listener, uuids.asList())
    }

    fun findByUuids(listener: ArcVideoStreamCallback, vararg uuids: String) {
        videoApiManager.findByUuidsApi(listener, uuids.asList())
    }

    fun findByUuids(uuids: List<String>, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApi(listener = listener, uuids = uuids)
    }

    /**
     * Returns a json representation of an array containing the ArcVideoStream objects for the given UUIDs
     *
     * @param uuids Array of strings with the UUIDs of the videos to retrieve. Use this method
     * if the UUID list is a fixed size array.
     * @param listener [ArcVideoStreamCallback] use [ArcVideoStreamCallback.onJsonResult] for successful results
     * @return List of ArcVideoStream objects
     */
    fun findByUuidsAsJson(vararg uuids: String, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApiAsJson(listener, uuids.asList())
    }

    fun findByUuidsAsJson(listener: ArcVideoStreamCallback, vararg uuids: String) {
        videoApiManager.findByUuidsApiAsJson(listener, uuids.asList())
    }

    fun findByUuidsAsJson(uuids: List<String>, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApiAsJson(listener, uuids)
    }

    /**
     * Returns the playlist with the given name containing the first count number of objects. A playlist is a list of ArcVideoStream objects.
     *
     * @param name Name of the playlist
     * @param count Number of entries to return
     * @param listener [ArcVideoPlaylistCallback] use [ArcVideoPlaylistCallback.onVideoPlaylist] for successful results
     */
    fun findByPlaylist(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        videoApiManager.findByPlaylistApi(name, count, listener)
    }

    /**
     * Returns the playlist as json with the given name containing the first count number of objects. A playlist is a list of ArcVideoStream objects.
     *
     * @param name Name of the playlist
     * @param count Number of entries to return
     * @param listener [ArcVideoPlaylistCallback] use [ArcVideoPlaylistCallback.onJsonResult] for successful results
     */
    fun findByPlaylistAsJson(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        videoApiManager.findByPlaylistApiAsJson(name, count, listener)
    }

    /**
     * Returns list of current live videos
     *
     * @param listener [ArcVideoStreamCallback] use [ArcVideoStreamCallback.onLiveVideos] for successful results
     */
    fun findLive(listener: ArcVideoStreamCallback) {
        videoApiManager.findLive(listener = listener)
    }

    /**
     * Returns list of current live videos (suspend)
     *
     * @return [Either] Success: [List]<[com.arcxp.video.model.VideoVO]> or [ArcXPException]
     */
    suspend fun findLiveSuspend() = videoApiManager.findLiveSuspend()

    /**
     * Returns list of current live videos as json [String]
     *
     * @param listener [ArcVideoStreamCallback] use [ArcVideoStreamCallback.onJsonResult] for successful results
     */
    fun findLiveAsJson(listener: ArcVideoStreamCallback) {
        videoApiManager.findLiveAsJson(listener = listener)
    }

    /**
     * Returns list of current live videos (suspend) as json [String]
     *
     * @return [Either] Success: [String] or [ArcXPException]
     */
    suspend fun findLiveSuspendAsJson() = videoApiManager.findLiveSuspendAsJson()
}