package com.arcxp.video.cast

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Menu
import androidx.annotation.VisibleForTesting
import androidx.mediarouter.app.MediaRouteButton
import com.arcxp.video.model.ArcVideo
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata.KEY_TITLE
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.MediaTrack
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage
import java.net.URI
import java.util.Collections

/**
 * ArcCastManager is a class that manages Google Cast sessions for video content within the ArcXP platform.
 * It handles operations for initializing Cast sessions, managing session listeners, and controlling media playback.
 *
 * The class defines the following properties:
 * - mActivityContext: The application context used to initialize the CastContext.
 * - mCastContext: The CastContext instance for managing Cast sessions.
 * - mCastStateListener: Listener for changes in the Cast state.
 * - mSessionManagerListener: Listener for session management events.
 * - arcSessionManagerListener: Custom listener for ArcXP-specific session events.
 * - mCastSession: The current CastSession instance.
 * - mMainHandler: Handler for running tasks on the main thread.
 *
 * Usage:
 * - Create an instance of ArcCastManager with the application context.
 * - Use the provided methods to manage Cast sessions and control media playback.
 *
 * Example:
 *
 * val arcCastManager = ArcCastManager(application)
 * arcCastManager.addSessionManager()
 * arcCastManager.doCastSession(video, position)
 *
 * Note: Ensure that the Google Cast SDK is properly configured before using ArcCastManager.
 *
 * @property mActivityContext The application context used to initialize the CastContext.
 * @property mCastContext The CastContext instance for managing Cast sessions.
 * @property mCastStateListener Listener for changes in the Cast state.
 * @property mSessionManagerListener Listener for session management events.
 * @property arcSessionManagerListener Custom listener for ArcXP-specific session events.
 * @property mCastSession The current CastSession instance.
 * @property mMainHandler Handler for running tasks on the main thread.
 * @method getCastContext Returns the CastContext instance.
 * @method setSessionManagerListener Sets the custom session manager listener.
 * @method removeSessionManagerListener Removes the custom session manager listener.
 * @method hasCastSession Checks if there is an active Cast session.
 * @method doCastSession Starts a Cast session with the given video and position.
 * @method reloadVideo Reloads the given video in the current Cast session.
 * @method loadRemoteMedia Loads media into the current Cast session.
 * @method getEndedPosition Returns the position where the media ended.
 * @method isIdleReasonEnd Checks if the media ended due to being idle.
 * @method addMenuCastButton Adds a Cast button to the menu.
 * @method addCastButton Adds a Cast button to the MediaRouteButton.
 * @method addSessionManager Adds the session manager listeners.
 * @method removeSessionManager Removes the session manager listeners.
 * @method initSessionManagerListener Initializes the session manager listener.
 * @method createMediaQueueItems Creates an array of MediaInfo items from a list of ArcVideo objects.
 * @method createMediaQueueItem Creates a MediaInfo item from an ArcVideo object.
 * @method createMediaItem Creates a MediaItem from an ArcVideo object.
 * @method onPause Pauses the Cast session manager.
 * @method onDestroy Destroys the Cast session manager.
 * @method onResume Resumes the Cast session manager.
 * @method getArcSessionManagerListener Returns the custom session manager listener.
 * @method getMSessionManagerListener Returns the session manager listener.
 * @method getMCastSession Returns the current Cast session.
 * @method showSubtitles Shows or hides subtitles in the current Cast session.
 * @method setMute Mutes or unmutes the current Cast session.
 * @method runOnUiThread Runs a task on the main thread.
 */
@SuppressLint("UnsafeOptInUsageError")
class ArcCastManager(private val mActivityContext: Application) {

    private val mCastContext: CastContext = CastContext.getSharedInstance(mActivityContext)
    private val mCastStateListener: CastStateListener
    private val mSessionManagerListener: SessionManagerListener<CastSession>

    private var arcSessionManagerListener: ArcCastSessionManagerListener? = null
    private var mCastSession: CastSession? = null
    private var mMainHandler: Handler = Handler(Looper.getMainLooper())

    init {
        mSessionManagerListener = initSessionManagerListener()
        mCastStateListener = CastStateListener { newState ->
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                //TODO:
            }
        }
    }

    fun getCastContext(): CastContext {
        return mCastContext;
    }

    fun setSessionManagerListener(listener: ArcCastSessionManagerListener) {
        arcSessionManagerListener = listener
    }

    fun removeSessionManagerListener() {
        arcSessionManagerListener = null
    }

    fun hasCastSession(): Boolean {
        return mCastSession?.isConnected ?: false
    }

    fun doCastSession(video: ArcVideo, position: Long) {
        val mediaInfo = createMediaQueueItem(video)
        loadRemoteMedia(position, true, mediaInfo)
    }

    fun doCastSession(video: ArcVideo, position: Long, artWorkUrl: String?) {
        val mediaInfo = createMediaQueueItem(video, artWorkUrl)
        loadRemoteMedia(position, true, mediaInfo)
    }

    fun reloadVideo(video: ArcVideo) {
        loadRemoteMedia(0L, false, createMediaQueueItem(video))
    }

    private fun loadRemoteMedia(position: Long, autoPlay: Boolean, mediaInfo: MediaInfo) =
        mCastSession?.remoteMediaClient?.load(
            mediaInfo,
            MediaLoadOptions.Builder().setAutoplay(autoPlay).setPlayPosition(position).build()
        )

    fun getEndedPosition() = mCastSession?.remoteMediaClient?.approximateStreamPosition

    fun isIdleReasonEnd() =
        mCastSession?.remoteMediaClient?.idleReason == MediaStatus.IDLE_REASON_FINISHED

    fun addMenuCastButton(menu: Menu, id: Int) {
        CastButtonFactory.setUpMediaRouteButton(mActivityContext, menu, id)
    }

    fun addCastButton(routeButton: MediaRouteButton) {
        CastButtonFactory.setUpMediaRouteButton(mActivityContext, routeButton)
    }

    fun addSessionManager() {
        mCastContext.addCastStateListener(mCastStateListener)
        mCastContext.sessionManager.addSessionManagerListener(
                mSessionManagerListener, CastSession::class.java)
        if (mCastSession == null) {
            mCastSession = CastContext.getSharedInstance(mActivityContext).sessionManager
                .currentCastSession
        }
    }

    fun removeSessionManager() {
        mCastContext.removeCastStateListener(mCastStateListener)
        mCastContext.sessionManager.removeSessionManagerListener(
                mSessionManagerListener, CastSession::class.java)
    }

    private fun initSessionManagerListener(): SessionManagerListener<CastSession> {
        return object : SessionManagerListener<CastSession> {
            override fun onSessionEnded(session: CastSession, error: Int) {
                if (session === mCastSession) {
                    mCastSession = null
                }
                arcSessionManagerListener?.onSessionEnded(error)
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                mCastSession = session
                arcSessionManagerListener?.onSessionResumed(wasSuspended)
            }

            override fun onSessionStarted(session: CastSession, sessionId: String) {
                mCastSession = session
                arcSessionManagerListener?.onSessionStarted(sessionId)
            }

            override fun onSessionStarting(session: CastSession) {
                mCastSession = session
                arcSessionManagerListener?.onSessionStarting()
            }

            override fun onSessionStartFailed(session: CastSession, error: Int) {
                mCastSession = session
                arcSessionManagerListener?.onSessionStartFailed(error)
            }

            override fun onSessionEnding(session: CastSession) {
                mCastSession = session
                arcSessionManagerListener?.onSessionEnding()
            }

            override fun onSessionResuming(session: CastSession, sessionId: String) {
                mCastSession = session
                arcSessionManagerListener?.onSessionResuming(sessionId)
            }

            override fun onSessionResumeFailed(session: CastSession, error: Int) {
                mCastSession = session
                arcSessionManagerListener?.onSessionResumeFailed(error)
            }

            override fun onSessionSuspended(session: CastSession, reason: Int) {
                mCastSession = session
                arcSessionManagerListener?.onSessionSuspended(reason)
            }
        }
    }

    companion object {

        const val SUBTITLE_TRACK_INDEX = 2L
        private val supportMimeTypes = Collections.unmodifiableMap(
            mapOf(
                C.CONTENT_TYPE_DASH to MimeTypes.APPLICATION_MPD,
                C.CONTENT_TYPE_SS to MimeTypes.APPLICATION_SS,
                C.CONTENT_TYPE_HLS to MimeTypes.APPLICATION_M3U8
            )
        )

        @JvmStatic
        fun createMediaQueueItems(arcVideos: List<ArcVideo>) =
            Array(arcVideos.size) { createMediaQueueItem(arcVideos[it]) }

        @JvmStatic
        fun createMediaQueueItem(arcVideo: ArcVideo, artWorkUrl: String? = null): MediaInfo {
            val uri = URI(arcVideo.id).path
            @C.ContentType val type: Int = Util.inferContentType(uri)
            if (!supportMimeTypes.containsKey(type)) {
                throw UnsupportedOperationException()
            }

            val metadata =
                com.google.android.gms.cast.MediaMetadata(com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MOVIE)
            metadata.putString(KEY_TITLE, arcVideo.headline.orEmpty())
            artWorkUrl?.let { metadata.addImage(WebImage(Uri.parse(it))) }

            val builder = MediaInfo.Builder(arcVideo.id.orEmpty())
                .setStreamType(if (arcVideo.isLive) MediaInfo.STREAM_TYPE_LIVE else MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(supportMimeTypes[type]!!)
                .setMetadata(metadata)
            arcVideo.subtitleUrl?.let {
                val englishSubtitle = MediaTrack.Builder(SUBTITLE_TRACK_INDEX, MediaTrack.TYPE_TEXT)
                    .setName("English Subtitle")
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(it) /* language is required for subtitle type but optional otherwise */
                    .setLanguage("en-US")
                    .build()
                builder.setMediaTracks(mutableListOf(englishSubtitle))
            }
            return builder.build()
        }

        @JvmStatic
        fun createMediaItem(arcVideo: ArcVideo): MediaItem {
            val uri = URI(arcVideo.id).path

            @C.ContentType val type: Int = Util.inferContentType(uri)
            if (!supportMimeTypes.containsKey(type)) {
                throw UnsupportedOperationException()
            }
            val metadata = MediaMetadata.Builder().setTitle(arcVideo.headline).build()

            return MediaItem.Builder()
                .setUri(arcVideo.id)
                .setMediaId(arcVideo.id.orEmpty())
                .setMediaMetadata(metadata)
                .setMimeType(supportMimeTypes[type])
                .build()
        }
    }


    public fun onPause() {
        removeSessionManager()
    }

    public fun onDestroy() {
        removeSessionManagerListener()
    }

    public fun onResume() {
        addSessionManager()
    }

    @VisibleForTesting
    fun getArcSessionManagerListener() = arcSessionManagerListener

    @VisibleForTesting
    fun getMSessionManagerListener() = mSessionManagerListener

    @VisibleForTesting
    fun getMCastSession() = mCastSession

    fun showSubtitles(shouldShow: Boolean) {
        runOnUiThread {
            mCastSession?.remoteMediaClient?.setActiveMediaTracks(
                if (shouldShow) longArrayOf(SUBTITLE_TRACK_INDEX) else longArrayOf()
            )

        }
    }

    fun setMute(shouldMute: Boolean) =
        runOnUiThread { mCastSession?.remoteMediaClient?.setStreamMute(shouldMute) }

    private fun runOnUiThread(runnable: Runnable) = mMainHandler.post(runnable)
}
