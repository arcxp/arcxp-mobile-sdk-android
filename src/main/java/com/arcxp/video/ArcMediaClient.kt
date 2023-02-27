package com.arcxp.video

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory.createArcXPError
import com.arcxp.commons.util.DependencyFactory.createVideoApiManager
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
class ArcMediaClient private constructor() {

    private var baseUrl: String = ""
    private var orgName: String = ""
    private var environmentName: String = ""

    private lateinit var videoApiManager: VideoApiManager

    /**
     * Create the service to connect to the Arc server
     *
     * @param baseUrl Organization name - Server environment. ie wp-prod
     * @return The service object
     */
    private fun create(baseUrl: String) {

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
     * Create the service to connect to the Arc server
     *
     * @param orgName Organization name. Provided by Arc
     * @param environmentName Server environment. Can be production, sandbox or empty (for older orgs that do not use env)
     */
    private fun create(orgName: String, environmentName: String) {

        if (orgName.isBlank()) {
            throw createArcXPError(
                type = ArcXPSDKErrorType.INIT_ERROR,
                message = application().getString(R.string.org_failure)
            )
        }
        this.orgName = orgName
        this.environmentName = environmentName
        videoApiManager =
            createVideoApiManager(orgName = orgName, environmentName = environmentName)
    }

    /**
     * Returns an array containing a single ArcVideoStream object
     *
     * @param uuid String uuid for the video
     * @param listener [ArcVideoStreamCallback] object
     * @param shouldUseVirtualChannel Boolean indicator to use virtual channel endpoint
     * @return List of ArcVideoStream objects
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
     * Returns an array containing the ArcVideoStream objects for the given UUIDs
     *
     * @param uuids Array of strings with the UUIDs of the videos to retrieve. Use this method
     * if the UUID list is a fixed size array.
     * @param listener [ArcVideoStreamCallback] object
     * @return List of ArcVideoStream objects
     */
    fun findByUuids(vararg uuids: String, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApi(listener, uuids.asList())
    }

    fun findByUuids(listener: ArcVideoStreamCallback, vararg uuids: String) {
        videoApiManager.findByUuidsApi(listener, uuids.asList())
    }

    fun findByUuids(uuids: List<String>, listener: ArcVideoStreamCallback) {
        videoApiManager.findByUuidsApi(listener, uuids)
    }

    /**
     * Returns the playlist with the given name containing the first count number of objects. A playlist is a list of ArcVideoStream objects.
     *
     * @param name Name of the playlist
     * @param count Number of entries to return
     * @param listener [ArcVideoStreamCallback] object
     * @return ArcVideoPlaylist object
     */
    fun findByPlaylist(name: String, count: Int, listener: ArcVideoPlaylistCallback) {
        videoApiManager.findByPlaylistApi(name, count, listener)
    }

    /**
     * Returns the version of the SDK
     *
     */
    @Deprecated(
        message = "Use ArcXPVideoSDK.getVersion(context: Context)",
        ReplaceWith(expression = "com.arcxp.video.ArcXPVideoSDK.getVersion(context)")
    )

    fun findLive(listener: ArcVideoStreamCallback) {
        videoApiManager.findLive(listener = listener)
    }

    suspend fun findLiveSuspend() = videoApiManager.findLiveSuspend()

    companion object {
        @Volatile
        private var INSTANCE: ArcMediaClient? = null

        /**
         * @deprecated Use instantiate(baseUrl)
         * Creates a singleton instance of the media client initialized with a base URL
         *
         * @param serverEnvironment Organization name - Server environment. ie wp-prod
         * @return ArcMediaClient instance
         */
        @JvmStatic
        fun initialize(serverEnvironment: String): ArcMediaClient {
            var client = ArcMediaClient()
            client.create(serverEnvironment)
            INSTANCE = client
            return client
        }

        /**
         * Creates a singleton instance of the media client initialized with a base URL
         *
         * @param serverEnvironment Organization name - Server environment. ie wp-prod
         * @return ArcMediaClient instance
         */
        @JvmStatic
        fun instantiate(serverEnvironment: String): ArcMediaClient {
            var client = ArcMediaClient()
            client.create(serverEnvironment)
            INSTANCE = client
            return client
        }

        /**
         * Create a unique instance of the media client
         *
         * @param serverEnvironment Organization name - Server environment. ie wp-prod
         * @return ArcMediaClient instance
         */
        @JvmStatic
        fun createClient(serverEnvironment: String): ArcMediaClient {
            var client = ArcMediaClient()
            client.create(serverEnvironment)

            INSTANCE = client

            return client
        }

        /**
         * Create a unique instance of the media client
         *
         * @param orgName Organization name. Provided by Arc
         * @param serverEnvironment Server environment. Production or sandbox or empty (for older orgs that do not use env)
         * @return ArcMediaClient instance
         */
        @JvmStatic
        fun createClient(
            orgName: String,
            serverEnvironment: String
        ): ArcMediaClient {
            val client = ArcMediaClient()
            client.create(orgName = orgName, environmentName = serverEnvironment)
            INSTANCE = client
            return client
        }
    }
}