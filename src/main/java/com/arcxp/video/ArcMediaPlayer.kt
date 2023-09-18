package com.arcxp.video

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.arcxp.video.ArcMediaPlayerConfig.CCStartMode
import com.arcxp.video.ArcMediaPlayerConfig.PreferredStreamType
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.listeners.ArcVideoSDKErrorListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.ArcVideoStreamVirtualChannel
import com.arcxp.video.views.ArcVideoFrame

/**
 * This class is used to interact with the video capabilities of the SDK.
 *
 * ### How does it relate to other important Video SDK classes?
 *  ArcMediaPlayerConfig -
 *  ArcVideoManager -
 *  ArcVideoStream
 *
 * ### What are the core components that make it up?
 *  ArcMediaPlayerConfig -
 *  ArcVideoManager -
 *  ArcVideoStream
 *
 * ### How is this class used?
 *
 * ```
 * var mediaPlayer = ArcMediaPlayer.instantiate(this)
 *
 * mediaPlayer.trackMediaEvents(object: ArcVideoEventsListener() {
 *
 *      override fun onVideoTrackingEvent(type: TrackingType?, videoData: TrackingVideoTypeData?) {
 *          when(type) {
 *              TrackingType.ON_PLAY_STARTED -> {
 *                  //do something for playback started
 *              }
 *              TrackingType.ON_PLAY_COMPLETED -> {
 *                  //do something for playback ended
 *              }
 *          }
 *      }
 *
 *      override fun onSourceTrackingEvent(type: TrackingType?, source: TrackingSourceTypeData?) {
 *
 *      }
 *
 *      override fun onError(type: TrackingType?, video: TrackingErrorTypeData?) {
 *
 *      }
 *
 *      override fun onAdTrackingEvent(type: TrackingType?, adData: TrackingAdTypeData?) {
 *          val arcAd = adData.arcAd ?: return
 *          when (type) {
 *              TrackingType.AD_CLICKTHROUGH -> {
 *              }
 *              TrackingType.MIDROLL_AD_STARTED -> {
 *              }
 *              TrackingType.MIDROLL_AD_COMPLETED -> {
 *              }
 *          }
 *      })
 *
 *      mediaPlayer.trackErrors(object: ArcVideoSDKErrorListener {
 *          override fun onError(errorType: ArcVideoSDKErrorType, message: String, value: Any?) {
 *
 *          }
 *      })
 *  }
 *
 *  //Using an ArcMediaPlayerConfig object
 *  val config = ArcMediaPlayerConfig.Builder()
 *          .setActivity(activity)
 *          .setVideoFrame(mVideoContainer)
 *          .setServerSideAds(true)
 *          .setClientSideAds(true)
 *          ......
 *          .showProgressBar(true)
 *          .showClosedCaption(true)
 *          .setShowClosedCaptionTrackSelection(false)
 *          .addAdParam("sz", "400x300")
 *          ......
 *          .build()
 *  mediaPlayer.configureMediaPlayer(config)
 *
 *  //Configuring media player directly
 *  mediaPlayer.setActivity(activity)
 *  mediaPlayer.setVideoFrame(mVideoContainer)
 *
 *
 *
 *
 *  mediaPlayer.initMedia(stream)
 *  mediaPlayer.displayVideo()
 *  mediaPlayer.setServerSideAds(true)
 *  mediaPlayer.setClientSideAds(true)
 *  ....
 *
 *  mediaPlayer.initMedia(stream)
 *  mediaPlayer.displayVideo()
 * ```
 *
 */
@Keep
class ArcMediaPlayer private constructor(private val mContext: Context) {
    private var arcVideoManager: ArcVideoManager = VideoPackageUtils.createArcVideoManager(mContext)
    private val mConfigBuilder = VideoPackageUtils.createArcMediaPlayerConfigBuilder()
    private var mConfig: ArcMediaPlayerConfig? = null

    @Deprecated("Use configureMediaPlayer()")
    fun initMediaPlayer(config: ArcMediaPlayerConfig?): ArcMediaPlayer {
        mConfig = config
        return this
    }

    /**
     * Configures the media player
     * @param config Configuration object
     * @return Media player instance
     */
    fun configureMediaPlayer(config: ArcMediaPlayerConfig?): ArcMediaPlayer {
        mConfig = config
        arcVideoManager.initMediaPlayer(config!!)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Set the parent activity of the media player
     * @param activity Activity
     * @return Media player object
     */
    fun setActivity(activity: AppCompatActivity?): ArcMediaPlayer {
        mConfigBuilder.setActivity(activity)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Set the parent activity of the media player
     * @param activity Activity
     * @return Media player object
     */
    fun setActivity(activity: Activity?): ArcMediaPlayer {
        mConfigBuilder.setActivity(activity)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets the video frame object that will display the player
     * @param videoFrame ArcVideoFrame object
     * @return Media player object
     */
    fun setVideoFrame(videoFrame: ArcVideoFrame?): ArcMediaPlayer {
        mConfigBuilder.setVideoFrame(videoFrame)
        return this
    }

    /**
     * Initialize the player with a media object
     * @param video ArcVideo object
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(video: ArcVideo?): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(video!!)
        return this
    }

    /**
     * Initialize the player with a media object
     * @param video ArcVideoStream object
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(video: ArcVideoStream?): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(video!!)
        return this
    }

    /**
     * Initialize the player with a media object
     * @param video ArcVideoStream object
     * @param shareURL Share url, when populated will show share button
     * @return Media player instance
     * @throws ArcException
     */
    fun initMediaWithShareURL(video: ArcVideoStream?, shareURL: String): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMediaWithShareUrl(video!!, shareURL)
        return this
    }//TODO perhaps find a better solution for this.  Reasoning for this was we didn't seem to have a shareUrl in our network object, and had no way to initialize it externally

    /**
     * Initialize the player with a virtual channel url
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(arcVideoStreamVirtualChannel: ArcVideoStreamVirtualChannel): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(arcVideoStreamVirtualChannel)
        return this
    }

    /**
     * Initialize the player with a media object and an ad URL.  This will play
     * the video with the ad.
     * @param video ArcVideoStream object
     * @param adUrl ad url string
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(video: ArcVideoStream?, adUrl: String?): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(video!!, adUrl)
        return this
    }

    /**
     * Initialize the player with a list of media objects.  The objects will be played consecutively.
     * @param videos List of ArcVideoStream objects
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(videos: List<ArcVideoStream?>?): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(videos!!)
        return this
    }

    /**
     * Initialize the player with a list of media objects and list of corresponding ad urls.  Each ad URL will be
     * associated with its corresponding video based upon order in the list.  videos[0] will associate with adUrls[0],
     * videos[1] will be associated with adUrls[1], etc.
     * @param videos List of ArcVideoStream objects
     * @param adUrls List of ad url strings
     * @return Media player instance
     * @throws ArcException
     */
    fun initMedia(videos: List<ArcVideoStream?>?, adUrls: List<String?>?): ArcMediaPlayer {
        arcVideoManager.release()
        if (mConfig == null) {
            arcVideoManager.initMediaPlayer(mConfigBuilder.build())
        } else {
            arcVideoManager.initMediaPlayer(mConfig!!)
        }
        arcVideoManager.initMedia(videos!!, adUrls)
        return this
    }

    /**
     * Add a video to the player that will be played after the last video is played.  This effectively
     * creates a playlist or adds a video to the end of an existing playlist.
     * @param video Video to be added
     * @throws ArcException
     */
    fun addVideo(video: ArcVideoStream?) {
        arcVideoManager.addVideo(video)
    }

    /**
     * Add a video and a corresponding ad to be played after the last video is played.  Works the
     * same as [.addVideo] with the addition of an ad url.
     * @param video Video to be added.
     * @param adUrl Ad to be played with the video
     * @throws ArcException
     */
    fun addVideo(video: ArcVideoStream?, adUrl: String?) {
        arcVideoManager.addVideo(video, adUrl)
    }

    /**
     * @param trackEvents
     * @return Media player instance
     */
    @Deprecated(
        """Use {@link #trackMediaEvents(ArcVideoEventsListener)}
      """
    )
    fun initMediaEvents(trackEvents: ArcVideoEventsListener?): ArcMediaPlayer {
        arcVideoManager.initEvents(trackEvents)
        return this
    }

    /**
     * Used to track the various media events being returned.
     * @param listener Listener for the events.
     * @return Media player instance
     */
    fun trackMediaEvents(listener: ArcVideoEventsListener): ArcMediaPlayer {
        arcVideoManager.initEvents(listener)
        return this
    }

    /**
     * @param listener
     * @return
     */
    @Deprecated(
        """use {@link #trackErrors(ArcVideoSDKErrorListener)}
      """
    )
    fun setErrorListener(listener: ArcVideoSDKErrorListener?): ArcMediaPlayer {
        arcVideoManager.setErrorListener(listener)
        return this
    }

    /**
     * Used to track errors being returned.
     * @param listener Listener for the events
     * @return Media player instance.
     */
    fun trackErrors(listener: ArcVideoSDKErrorListener?): ArcMediaPlayer {
        arcVideoManager.setErrorListener(listener)
        return this
    }

    /**
     * Start playing the video
     */
    fun playVideo() {
        arcVideoManager.displayVideo()
    }

    fun displayVideo() {
        arcVideoManager.displayVideo()
    }

    /**
     * Call to clean up video resources.
     */
    fun finish() {
        arcVideoManager.release()
    }

    /**
     * Call when onBackPressed is called on the parent activity.
     * @return
     */
    fun onBackPressed(): Boolean {
        return arcVideoManager.onBackPressed()
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets whether picture-in-picture is enabled for the video.
     * @param enable True=enabled, False=disabled
     * @return Media player object
     */
    fun enablePip(enable: Boolean): ArcMediaPlayer {
        mConfigBuilder.enablePip(enable)
        return this
    }

    /**
     * Programmatically exits picture-in-picture if it is playing
     * This is for pressing x to close app from picture-in-picture  mode
     */
    fun exitAppFromPip() {
        arcVideoManager.setmIsInPIP(false)
        arcVideoManager.release()
        val arcVideoActivity = arcVideoManager.currentActivity
        arcVideoActivity?.finish()
    }

    /**
     * Programmatically stops picture-in-picture if it is playing
     * This is for returning to previous mode from picture-in-picture mode
     */
    fun returnToNormalFromPip() {
        arcVideoManager.stopPIP()
    }

    /**
     * Programmatically show the control bar on the media player
     */
    fun showControls() {
        arcVideoManager.showControls()
    }

    /**
     * Programmatically hides the control bar on the media player
     */
    fun hideControls() {
        arcVideoManager.hideControls()
    }

    /**
     * Returns if the control bar is currently showing on the media player.
     * @return true=showing, false=not showing
     */
    val isControlsVisible: Boolean
        get() = arcVideoManager.isControlsVisible

    /**
     * Returns if closed caption is available on the currently playing video.
     * @return true=available, false=unavailable
     */
    val isClosedCaptionAvailable: Boolean
        get() = arcVideoManager.isClosedCaptionAvailable

    /**
     * Returns if the video is currently playing in full screen
     * @return true=fullscreen, false=not fullscreen
     */
    val isFullScreen: Boolean
        get() = arcVideoManager.isFullScreen

    /**
     * Programmatically turn fullscreen on or off
     * @param full true=on, false=off
     */
    fun setFullscreen(full: Boolean) {
        arcVideoManager?.setFullscreen(full)
    }

    /**
     * For FireTV implementations, sets the listener for remote control keypresses when in fullscreen
     * @param listener [ArcKeyListener]
     */
    fun setFullscreenKeyListener(listener: ArcKeyListener?) {
        arcVideoManager.setFullscreenListener(listener)
    }

    /**
     * For FireTV implementations, sets the listener for remote control keypresses
     * @param listener [ArcKeyListener]
     */
    fun setPlayerKeyListener(listener: ArcKeyListener?) {
        arcVideoManager.setPlayerKeyListener(listener)
    }

    /**
     * Programmatically stop the player
     */
    fun stop() {
        arcVideoManager.stopPlay()
    }

    /**
     * Programmatically start the player
     */
    fun start() {
        arcVideoManager.startPlay()
    }

    /**
     * Programmatically pause the player
     */
    fun pause() {
        arcVideoManager.pausePlay()
    }

    /**
     * Programmatically resume the player.
     */
    fun resume() {
        arcVideoManager.resumePlay()
    }

    /**
     * Seek to a point in the video
     * @param ms Point to jump to in milliseconds
     */
    fun seekTo(ms: Int) {
        arcVideoManager.seekTo(ms)
    }

    /**
     * Programmatically set the volume of the player
     * @param volume
     */
    fun setVolume(volume: Float) {
        arcVideoManager.setVolume(volume)
    }

    /**
     * Returns the current playback state
     * @return One of STATE_IDLE, STATE_BUFFERING, STATE_READY, STATE_ENDED
     */
    val playbackState: Int
        get() = arcVideoManager.playbackState

    /**
     * Returns the play when ready state of the player
     * @return true=automatically start playback when ready, false=wait for play button to be pressed
     */
    val playWhenReadyState: Boolean
        get() = arcVideoManager.playWhenReadyState

    /**
     * Returns the current position in milliseconds of the video.  Used for VOD videos
     * @return Millisecond position of video
     */
    val playerPosition: Long
        get() = arcVideoManager.playheadPosition

    /**
     * Returns the current position within the timeline for live video feeds.
     * @return Millisecond position of video
     */
    val currentTimelinePosition: Long
        get() = arcVideoManager.currentTimelinePosition

    /**
     * Returns the duration of the video
     * @return Duration in milliseconds
     */
    val currentVideoDuration: Long
        get() = arcVideoManager.currentVideoDuration

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * One or more views can be added on top of the video view using the configuration option addOverlay().  This method takes a tag name for the view as
     * well as the view.  The client app will still have a pointer to the overlays so they can then control the visibility or other parameters of the views
     * from within the client code.  This method can can be called multiple times in order to add multiple overlays.
     * @param tag Name of the overlay.  This name is used to retrieve the overlay using [.getOverlay]
     * @param overlay Overlay view
     * @return Media player object
     */
    fun addOverlay(tag: String, overlay: View): ArcMediaPlayer {
        mConfigBuilder.addOverlay(tag, overlay)
        return this
    }

    /**
     * Retrieve the overlay view associated with a tag
     * @param tag Name of the overlay used in [.addOverlay]
     * @return Overlay view
     */
    fun getOverlay(tag: String?): View {
        return arcVideoManager.getOverlay(tag)
    }

    /**
     * Returns the current SDK version
     * @return Version
     */
    val sdkVersion: String
        get() = "1.5.0" //mContext.getString(R.string.sdk_version)

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Adds a list of views that will be hidden when PIP is activated.  This is used to hide any views in the Activity or Fragment such as action bar,
     * buttons, etc that are not the actual video view.
     * @param views List of views
     * @return Media player object
     */
    fun setViewsToHide(vararg views: View?): ArcMediaPlayer {
        mConfigBuilder.setViewsToHide(*views)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Enable ads.  If set to false then ads will not be shown.
     * @param enable true=enabled, false=disabled
     * @return Media player object
     */
    fun setEnableAds(enable: Boolean): ArcMediaPlayer {
        mConfigBuilder.setEnableAds(enable)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * This can be used to set the same ad url for all videos.  It is better to use one of the initMedia() methods that take an ad string.
     * @param url String of the URL for the ads
     * @return Media player object
     */
    fun setAdConfigUrl(url: String?): ArcMediaPlayer {
        mConfigBuilder.setAdConfigUrl(url)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * An ArcVideoStreamObject can return multiple streams that each have a different video type and bit rate.  The stream to play will be chosen based
     * on a preferred stream type and a maximum bit rate that is desired.  The preferred streams have a hierarchy that is HLS, TS, MP4, GIF, GIF-MP4.
     * The algorithm to choose the correct stream will loop through all available streams of the preferred type.  Of those it will find the one that
     * does not exceed the given max bit rate.  If a stream of the preferred type does not exist then it will go to the next preferred stream type and
     * repeat the search, working its way down until it finds a stream that does not exceed the bit rate.  For example, if the preferred type is set to
     * TS and there are no TS streams in the object then it will repeat the search using MP4, then GIF, then GIF-MP4.
     * @param type One of HLS, TS, MP4, GIF, GIFMP4
     * @return Media player object
     */
    fun setPreferredStreamType(type: PreferredStreamType?): ArcMediaPlayer {
        mConfigBuilder.setPreferredStreamType(type)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * An ArcVideoStreamObject can return multiple streams that each have a different video type and bit rate.  The stream to play will be chosen based
     * on a preferred stream type and a maximum bit rate that is desired.  The preferred streams have a hierarchy that is HLS, TS, MP4, GIF, GIF-MP4.
     * The algorithm to choose the correct stream will loop through all available streams of the preferred type.  Of those it will find the one that
     * does not exceed the given max bit rate.  If a stream of the preferred type does not exist then it will go to the next preferred stream type and
     * repeat the search, working its way down until it finds a stream that does not exceed the bit rate.  For example, if the preferred type is set to
     * TS and there are no TS streams in the object then it will repeat the search using MP4, then GIF, then GIF-MP4.
     * @param rate Maximum rate
     * @return Media player object
     */
    fun setMaxBitRate(rate: Int): ArcMediaPlayer {
        mConfigBuilder.setMaxBitRate(rate)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Show the closed caption button on the control bar
     * @param enable true=show the button, false=do not show the button
     * @return Media player object
     */
    fun showClosedCaption(enable: Boolean): ArcMediaPlayer {
        mConfigBuilder.showClosedCaption(enable)
        return this
    }

    /**
     * Programmatically toggle the closed captioning on or off
     * @param show true=on, false=off
     * @return Success or failure
     */
    fun toggleClosedCaption(show: Boolean): Boolean {
        return arcVideoManager.enableClosedCaption(show)
    }

    /**
     * Change the drawable used for the closed caption button on the control bar
     * @param ccButtonDrawable Drawable resource
     * @return Success/failure
     */
    fun setCcButtonDrawable(@DrawableRes ccButtonDrawable: Int): Boolean {
        return arcVideoManager.setCcButtonDrawable(ccButtonDrawable)
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Show or hide the video duration countdown at the right side of the progress bar
     * @param show true=show, false=hide
     * @return Media player object
     */
    fun showCountdown(show: Boolean): ArcMediaPlayer {
        mConfigBuilder.showCountdown(show)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Show or hide the progress bar.
     * @param show true=show, false = hide
     * @return Media player object
     */
    fun showProgressBar(show: Boolean): ArcMediaPlayer {
        mConfigBuilder.showProgressBar(show)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Turn server side ads on/off.  This only works with live feeds.
     * @param set true=on, false=off
     * @return
     */
    fun setServerSideAds(set: Boolean): ArcMediaPlayer {
        mConfigBuilder.setServerSideAds(set)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Turn client side ads on/off.  This only works with live feeds.
     * @param set true=on, false=off
     * @return Media player object
     */
    fun setClientSideAds(set: Boolean): ArcMediaPlayer {
        mConfigBuilder.setClientSideAds(set)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets the flag to automatically start playback when the video is ready.
     * @param play true=auto start, false=do not auto start
     * @return Media play object
     */
    fun setAutoStartPlay(play: Boolean): ArcMediaPlayer {
        mConfigBuilder.setAutoStartPlay(play)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Show or hide the seek buttons on the control bar.
     * @param show true=show, false=hide
     * @return Media player object
     */
    fun showSeekButton(show: Boolean): ArcMediaPlayer {
        mConfigBuilder.showSeekButton(show)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets whether the video start with the audio muted.
     * @param muted true=muted, false=volume on
     * @return Media player object
     */
    fun setStartMuted(muted: Boolean): ArcMediaPlayer {
        mConfigBuilder.setStartMuted(muted)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets whether the skip button on skippable ads is focused when it appears.  This is for FireTv implementations.
     * @param focus true=focus, false=do not focus
     * @return Media player object
     */
    fun setFocusSkipButton(focus: Boolean): ArcMediaPlayer {
        mConfigBuilder.setFocusSkipButton(focus)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets if closed caption starts on, off or the device default.  Set to on and CC will display when playback starts, set to off and CC will not show
     * when playback starts, set to default and the start mode will follow the accessibility settings of the device.
     * @param mode DEFAULT, ON, OFF
     * @return Media player object
     */
    fun setCcStartMode(mode: CCStartMode): ArcMediaPlayer {
        mConfigBuilder.setCcStartMode(mode)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets whether the playback controls are automatically shown when playback starts, pauses, ends,
     * or fails
     * @param show true=show, false=do not show
     * @return Media player object
     */
    fun setAutoShowControls(show: Boolean): ArcMediaPlayer {
        mConfigBuilder.setAutoShowControls(show)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Determines if the closed caption track selection dialog is displayed when CC button is pressed or if CC mode toggles on/off.
     * @param show true=show the dialog, false=toggle CC on/off
     * @return Media player object
     */
    fun setShowClosedCaptionTrackSelection(show: Boolean): ArcMediaPlayer {
        mConfigBuilder.setShowClosedCaptionTrackSelection(show)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Add an adParam key/value pair to be passed to MediaTailor when enabling server side ads.
     * Multiple calls can be made to this method and each parameter will be added to the call.
     * This method takes a key and a value and will be added to the call as a JSON entry of the format
     * { “adParams” : {
     * “key1”: “value1”,
     * “key2”: “value2”
     * }
     * }
     * @param key Key
     * @param value Associated value
     * @return Media player object
     */
    fun addAdParam(key: String, value: String): ArcMediaPlayer {
        mConfigBuilder.addAdParam(key, value)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Setting the ArcCastManager effectively activates Chromecast for the app.
     * @param manager
     * @return
     */
    fun setCastManager(manager: ArcCastManager): ArcMediaPlayer {
        mConfigBuilder.setCastManager(manager)
        return this
    }

    /**
     * Lifecycle method called from parent activity
     */
    fun onPause() {
        finish()
        arcVideoManager.onPause()
    }

    /**
     * Lifecycle method called from parent activity
     */
    fun onStop() {
        arcVideoManager.onStop()
    }

    /**
     * Call this from onDestroy() in the activity if Chromecast is enabled.
     */
    fun onDestroy() {
        arcVideoManager.onDestroy()
    }

    /**
     * Lifecycle method called from parent activity
     */
    fun onResume() {
        arcVideoManager.onResume()
    }

    /**
     * Call this from the override method in the parent activity
     * @param isInPictureInPictureMode True if the transition is into PIP
     * @param newConfig Configuration object for this change
     */
    fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (arcVideoManager.isPipStopRequest) {
            exitAppFromPip()
        }
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Sets the number of seconds to show the controls before hiding them.
     * @param ms Milliseconds to show
     * @return Media player object
     */
    fun setControlsShowTimeoutMs(ms: Int): ArcMediaPlayer {
        mConfigBuilder.setControlsShowTimeoutMs(ms)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Enable debug logging
     * @return Media player object
     */
    fun enableLogging(): ArcMediaPlayer {
        mConfigBuilder.enableLogging()
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * Fullscreen can be displayed either as a dialog that takes up the full device screen or by setting the layout parameters of the
     * [ArcVideoFrame] to match_parent for both width and height.  The preference depends on how the layout is built.  If the video frame is a
     * direct child of the root layout then a dialog is not necessary.  If the video frame is not a direct child, i.e. it is the child of another view
     * inside of the root view, setting the layout parameters will only expand it to the size of its parent view and will not make it full screen but rather
     * the full size of the parent view.  In this case a dialog should be used.  This is provided to allow flexibility based upon how the layout is built.
     * @param use true=use dialog, false=expand view
     * @return Media player object
     */
    fun useDialogForFullscreen(use: Boolean): ArcMediaPlayer {
        mConfigBuilder.useDialogForFullscreen(use)
        return this
    }

    /**
     * Configuration setting:  This can also be set using [.configureMediaPlayer] and the [ArcMediaPlayerConfig] object.
     * When controls are hidden in the control bar the visibility is set to GONE so the space is reclaimed during the next layout pass.  Depending on how the custom
     * control bar is set up this can cause controls to shift.  Setting this to true will cause the control visibility to be set to INVISIBLE so the space will
     * not be reclaimed during the next layout pass.
     * @param keep true=INVISIBLE, false=GONE
     * @return Media player object
     */
    fun keepControlsSpaceOnHide(keep: Boolean): ArcMediaPlayer {
        mConfigBuilder.setKeepControlsSpaceOnHide(keep)
        return this
    }

    fun isPipEnabled() = arcVideoManager.isPipEnabled

    fun dispatchKeyEvent(event: KeyEvent) : Boolean {
        return arcVideoManager.onKeyEvent(event)
    }

    companion object {
        /**
         * @deprecated("Use createPlayer instead")
         * Creates an instance of the ArcMediaPlayer class.  Use this method if you need multiple instances of a player in a single activity.
         * @param context Activity context
         * @return Media player instance
         */
        @JvmStatic
        fun instantiate(context: Context): ArcMediaPlayer {
            return ArcMediaPlayer(context)
        }

        /**
         * Creates an instance of the ArcMediaPlayer class.  Use this method if you need multiple instances of a player in a single activity.
         * @param context Activity context
         * @return Media player instance
         */
        @JvmStatic
        fun createPlayer(context: Context): ArcMediaPlayer {
            return ArcMediaPlayer(context)
        }
    }
}