package com.arcxp.video

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.arcxp.commons.util.Constants.OMID_VERSION
import com.arcxp.commons.util.Constants.PAL_VERSION
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.model.AdConfig
import com.arcxp.video.views.ArcVideoFrame
import androidx.media3.ui.AspectRatioFrameLayout

/**
 * This class is used to define a configuration to be used by the ArcMediaPlayer.
 *
 * ### How does it relate to other important Video SDK classes?
 * Once an object is created it can be passed into the media player using the configureMediaPlayer() method.
 * This object is also created in the media player and populated using the setter methods that mirror the methods
 * in the configuration object
 *
 * ### What are the core components that make it up?
 * [ArcXPVideoConfig.Builder]
 *
 * ### How is this class used?
 * Use the [ArcXPVideoConfig.Builder] to create an ArcMediaPlayerConfig object.
 *
 * ```
 * val mediaPlayer = MediaPlayer.createPlayer(this)
 * val config = ArcMediaConfig.Builder()
 *     .setActivity(this)
 *     .setVideoFrame(arcVideoFrame)
 *     .enablePip(true)
 *     .setViewsToHide(view1, view2, view3 ….)
 *     ...
 *     .build()
 *
 * mediaPlayer.configureMediaPlayer(config)
 * ```
 *
 * #### SetPreferredStreamType and setMaxBitRate
 * An ArcVideoStreamObject can return multiple streams that each have a different video type and bit rate.
 * The stream to play will be chosen based on a preferred stream type and a maximum bit rate that is
 * desired.  The preferred streams have a hierarchy that is HLS, TS, MP4, GIF, GIF-MP4.  The algorithm
 * to choose the correct stream will loop through all available streams of the preferred type.  Of
 * those it will find the one that does not exceed the given max bit rate.  If a stream of the preferred
 * type does not exist then it will go to the next preferred stream type and repeat the search,
 * working its way down until it finds a stream that does not exceed the bit rate.  For example, if
 * the preferred type is set to TS and there are no TS streams in the object then it will repeat the
 * search using MP4, then GIF, then GIF-MP4.
 *
 * #### Adding Overlays
 * One or more views can be added on top of the video view using the configuration option addOverlay().
 * This method takes a tag name for the view as well as the view.  The client app will still have a
 * pointer to the overlays so they can then control the visibility or other parameters of the views
 * from within the client code.  This method can be called multiple times in order to add multiple
 * overlays.
 * The views can be retrieved and removed through the media player as follows:
 * ```
 *      mediaPlayer.getOverlay(tag)
 *      mediaPlayer.removeOverlay(tag)
 * ```
 *
 * #### Adding Ad Params
 * A single ad parameter can be added using the method addAdParam(key, value).  Multiple calls to this method can be made.
 * This method takes a key and a value and will be added to the call as a JSON entry of the format
 * ```
 *    { “adParams” : {
 *        “key1”: “value1”,
 *        “key2”: “value2”
 *       }
 *    }
 * ```
 *
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
class ArcXPVideoConfig private constructor(
    /**
     * The parent activity for the player.  This must be set.
     */
    val activity: Activity? = null,
    /**
     * The ArcVideoFrame object for the player.  This must be set.
     */
    val videoFrame: ArcVideoFrame? = null,
    /**
     * Enable picture-in-picture(PIP).  If it is on then the video controls will have a pip menu button.
     * PIP will occur when the button is pressed or the user presses the back button while a video
     * is playing.  Default value = false.
     */
    val isEnablePip: Boolean = false,
    /**
     * Show or hide the closed caption button on the player control bar.  Default is false.
     */
    val mShowClosedCaption: Boolean = false,
    /**
     * Show or hide the countdown text on the video progress bar on the player control bar.
     * Default is true.
     */
    val isShowCountDown: Boolean = true,
    /**
     * Show or hide the player video progress bar including the countdown text on the player control bar.
     * Default is true.
     */
    val isShowProgressBar: Boolean = true,
    /**
     * Show or hide the rewind and fast forward buttons on the player control bar.
     * Default is true.
     */
    val isShowSeekButton: Boolean,

    /**
     * set this to true when you do not want any playback controls to appear at any time
     * this disables all the button listeners and should not display controls ever
     * default is false
     */
    val isDisableControlsFully: Boolean = false,

    /**
     * This is a variable size list of views that will be hidden when PIP occurs.
     * All views that are visible on the screen with the exception of the ArcVideoFrame should be listed here.
     * The views in this list will be set to GONE when PIP occurs so that only the video frame is showing.
     */
    var viewsToHide: List<View>?,
    /**
     * This flag indicates if ads should not be shown as part of the video content.
     * This is for Google IMA ads only.  Default value = false.
     */
    val isEnableAds: Boolean = false,
    val adConfigUrl: String?,
    val adConfig: AdConfig?,
    /**
     * Preferred stream type to play when there are multiple types returned in an ArcVideoStream object.
     */
    private val preferredStreamType: PreferredStreamType?,
    /**
     * The maximum bit rate of the video to play when there are multiple streams to choose from.
     * This is used in conjunction with the setPreferredStreamType() method.
     */
    val maxBitRate: Int,
    /**
     * Make the server call to enable server side ads.
     * Will only work with ArcVideoStream objects that have ads enabled.
     * Default is false.
     */
    val isEnableServerSideAds: Boolean = false,
    /**
     * Enable client side ad reporting.
     */
    val isEnableClientSideAds: Boolean,
    /**
     * Automatically start playing the video after it becomes ready.  Default value is true.
     */
    val isAutoStartPlay: Boolean = true,
    /**
     * Start the video muted.  Default is false.
     */
    val isStartMuted: Boolean = false,
    /**
     * Place focus on the skip button when it is shown during skippable ads.
     * This is for Google IMA ads only.  Default is true.
     */
    val isFocusSkipButton: Boolean = true,
    /**
     * CCStartMode has the following values:
     *     ON - Closed Captioning is on by default but can be turned off.
     *     OFF - Closed Captioning is off by default but can be turned on.
     *     DEFAULT - Closed Captioning follows the Captioning Service setting of the device.  Works for Android 19 and above only.
     */
    val ccStartMode: CCStartMode,
    /**
     * Determines if the player controls show automatically when playback ends.
     * Default is true.
     */
    val isAutoShowControls: Boolean = true,
    /**
     * If true then the track selection dialog will be shown that allows the user to select the closed
     * captioning track.  If false then the CC button will toggle between off and the default CC track.
     * Default is true.
     */
    val isShowClosedCaptionTrackSelection: Boolean = true,
    /**
     * Add a single ad parameter to be sent to MediaTailor.
     * Multiple calls to this method can be made.  This method takes a key and a value
     * and will be added to the call as a JSON entry of the format
     * { “adParams” : {
     *       “key1”: “value1”,
     *       “key2”: “value2”
     *     }
     * }
     *
     */
    val adParams: HashMap<String, String>,
    /**
     * One or more views can be added on top of the video view.
     * This method takes a tag name for the view as well as the view.  The client app will still have a
     * pointer to the overlays so they can then control the visibility or other parameters of the views
     * from within the client code.
     */
    val overlays: HashMap<String, View>,

    val isEnablePAL: Boolean = false,
    val palPartnerName: String = "washpost",
    val palPpid: String = "wapo",
    val palVersionName: String = PAL_VERSION,
    val exoplayerVersion: String,

    val isEnableOmid: Boolean = false,
    val omidPartnerName: String = "washpost",
    val omidPpid: String = "wapo",
    val omidVersionName: String = OMID_VERSION,

    /**
     * Sets the cast manager for enabling Chromecast.
     */
    var arcCastManager: ArcCastManager?,

    /**
     * Set the amount of time the player controls are shown before disappearing.
     */
    val controlsShowTimeoutMs: Int?,
    /**
     * Turn on enhanced logging that will show up in the logcat log.
     * Default value is false.
     */
    val isLoggingEnabled: Boolean = false,
    /**
     * Fullscreen can be displayed in two different ways, as a dialog or by expanding the layout_height
     * and layout_width parameters of the ArcVideoFrame to match_parent.  This method determines if
     * the dialog should be used.  Default setting is false.  If your layout has the ArcVideoFrame
     * element a direct child of the root view then setting this to false will work because the frame
     * will expand to the full size of the screen.  If your layout has the ArcVideoFrame as a child of
     * a view that is not the root view and that parent is not the full size of the screen then you
     * should use a dialog because expanding the height and width of the video frame will not take
     * up the whole screen.  Default value is false.
     */
    val isUseFullScreenDialog: Boolean = false,
    val isKeepControlsSpaceOnHide: Boolean = true,
    /**
     * Disable showing the player controls when the user touches the screen.
     * This will require the user to handle video player touches.
     * Default is false.
     */
    val isDisableControlsWithTouch: Boolean = false,

    /**
     * The string that is used as the User-Agent key that can be attached to the header of the call to enable
     * server side ads.
     */
    val userAgent: String? = null,

    /**
     * The artwork picture address that will be used by Chromecast
     */
    val artworkUrl: String? = null,
    /**
     * if enabled will show next/previous buttons and call back clicks
     * if playing single video, the call backs must perform any desired functionality
     * if playing playlist via findbyUUids (and mVideos.size > 1), will show regardless
     * of this boolean and have functionality with that (sdk-managed) playlist.
     */
    val showNextPreviousButtons: Boolean = false,
    /**
     *  if either of these are enabled, the corresponding button will be disabled.
     *  main use case here is disabling next button at end of (app-managed) playlist
     *  when there is no next video.
     *  Default behavior for previous button on first entry in list is to replay that entry,
     *  but perhaps you would rather disable the previous button.
     */
    val shouldDisableNextButton: Boolean = false,
    val shouldDisablePreviousButton: Boolean = false,
    /**
     * if enabled will show back button and call back clicks
     */
    val showBackButton: Boolean,
    /**
     * if enabled will show fullscreen button to toggle full screen mode
     */
    val showFullScreenButton: Boolean,

    /**
     * if enabled will show video title when controls are displayed
     */
    val showTitleOnController: Boolean,

    /**
     * if enabled will show volume button to toggle mute
     */
    val showVolumeButton: Boolean,

    /**
     * Resize mode for the video.
     */
    val videoResizeMode: VideoResizeMode = VideoResizeMode.FIT,
    /**
     * Disable Error overlaying video, if you don't want this feature and want to do your own error display
     */
    val disableErrorOverlay: Boolean = false
) {

    /**
     * These are the types of streams that can be sent to the player.
     */
    enum class PreferredStreamType(private val streamType: String) {
        HLS("hls"), TS("ts"), MP4("mp4"), GIF("gif"), GIFMP4("gif-mp4");

        operator fun next(): PreferredStreamType {
            return values[(ordinal + 1) % values.size]
        }

        fun getPreferredStreamType(): String {
            return streamType
        }

        companion object {
            private val values = values()
        }
    }

    enum class CCStartMode {
        DEFAULT, ON, OFF
    }

    enum class VideoResizeMode(private val resizeMode: Int) {
        FILL(AspectRatioFrameLayout.RESIZE_MODE_FILL), FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        fun mode(): Int {
            return resizeMode
        }

        companion object {
            private val values = PreferredStreamType.values()
        }
    }

    fun enableClosedCaption(): Boolean {
        return mShowClosedCaption
    }

    fun getPreferredStreamType(): PreferredStreamType {
        return preferredStreamType ?: PreferredStreamType.HLS
    }

    /**
     * This is the builder class for ArcMediaPlayerConfig objects.
     */
    class Builder {
        private var mActivity: Activity? = null
        private var mVideoFrame: ArcVideoFrame? = null
        private var mEnablePip = false
        private var useLegacyPlayer = false
        private var viewsToHide: MutableList<View>? = null
        private var enableAds = false
        private var adConfigUrl: String? = null
        private var adConfig: AdConfig? = null
        private var preferredStreamType: PreferredStreamType? = null
        private var maxBitRate = 0
        private var mShowHideCc = false
        private var mShowCountDown = true
        private var mShowProgressBar = true
        private var mEnableServerSideAds = true
        private var mEnableClientSideAds = true
        private var mAutoStartPlay = true
        private var mShowSeekButton = false
        private var mStartMuted = true
        private var mFocusSkipButton = true
        private var mCcStartMode = CCStartMode.DEFAULT
        private var mAutoShowControls = true
        private var mShowClosedCaptionTrackSelection = true
        private val mAdParams = HashMap<String, String>()
        private val mOverlays = HashMap<String, View>()

        private var arcCastManager: ArcCastManager? = null
        //private String descriptionUrl = "";
        private var mControlsShowTimeoutMs: Int? = null
        private var isLoggingEnabled = false
        private var useFullScreenDialog = false

        private var enableOmid = false
        private var omidPartnerName: String = "washpost"
        private var omidPpid: String = "wapo"
        private var omidVersionName = OMID_VERSION

        private var enablePAL = false
        private var palPartnerName: String = "washpost"
        private var palPpid: String = "wapo"

        private var palVersionName: String = PAL_VERSION
        private var exoplayerVersion: String = "2.13.3"

        private var keepControlsSpaceOnHide = false
        private var disableControlsWithTouch = false
        private var userAgent: String? = null
        private var artWorkUrl: String? = null
        private var showNextPreviousButtons = false
        private var shouldDisableNextButton = false
        private var shouldDisablePreviousButton = false
        private var showBackButton = false
        private var showFullScreenButton = false
        private var showTitleOnController = true
        private var showVolumeButton = true
        private var disableControlsFully = false

        private var videoResizeMode = VideoResizeMode.FIT
        private var disableErrorOverlay = false
        /**
         * Sets the parent activity for the player.  This method must be called.
         */
        fun setActivity(activity: AppCompatActivity?): Builder {
            mActivity = activity
            return this
        }

        /**
         * Sets the parent activity for the player.  This method must be called.
         */
        fun setActivity(activity: Activity?): Builder {
            mActivity = activity
            return this
        }

        /**
         * Sets the ArcVideoFrame object for the player.  This method must be called.
         */
        fun setVideoFrame(videoFrame: ArcVideoFrame?): Builder {
            mVideoFrame = videoFrame
            return this
        }

        /**
         * Enable picture-in-picture(PIP).  If it is on then the video controls will have a pip menu button.
         * PIP will occur when the button is pressed or the user presses the back button while a video
         * is playing.
         */
        fun enablePip(enable: Boolean): Builder {
            mEnablePip = enable
            return this
        }

        /**
         * This is a variable size list of views that will be hidden when PIP occurs.
         * All views that are visible on the screen with the exception of the ArcVideoFrame should be listed here.
         * The views in this list will be set to GONE when PIP occurs so that only the video frame is showing.
         */
        fun setViewsToHide(vararg views: View?): Builder {
            viewsToHide = ArrayList<View>(views.size)
            for (v in views) {
                (viewsToHide as ArrayList<View>).add(v!!)
            }
            return this
        }

        /**
         * This is a variable size list of views that will be hidden when PIP occurs.
         * All views that are visible on the screen with the exception of the ArcVideoFrame should be listed here.
         * The views in this list will be set to GONE when PIP occurs so that only the video frame is showing.
         */
        fun addViewToHide(view: View?): Builder {
            if (viewsToHide == null) {
                viewsToHide = ArrayList<View>()
            }
            viewsToHide?.add(view!!)
            return this
        }

        /**
         * This flag indicates if ads should not be shown as part of the video content.
         * This is for Google IMA ads only.  Default value = false.
         */
        @Deprecated("Use {@link #setAdsEnabled(Boolean)}")
        fun setEnableAds(enable: Boolean): Builder {
            enableAds = enable
            return this
        }

        /**
         * This flag indicates if ads should not be shown as part of the video content.
         * This is for Google IMA ads only.  Default value = false.
         */
        fun setAdsEnabled(enable: Boolean): Builder {
            enableAds = enable
            return this
        }

        @Deprecated("Use setAdUrl()")
        fun setAdConfigUrl(url: String?): Builder {
            adConfigUrl = url
            return this
        }

        fun setAdUrl(url: String?): Builder {
            adConfigUrl = url
            return this
        }

        @Deprecated("Recommended to not use the AdConfig object")
        fun setAdConfig(adconfig: AdConfig?): Builder {
            adConfig = adconfig
            return this
        }

        /**
         * Sets the preferred stream type to play when there are multiple types returned in an ArcVideoStream object.
         */
        fun setPreferredStreamType(type: PreferredStreamType?): Builder {
            preferredStreamType = type
            return this
        }

        /**
         * The maximum bit rate of the video to play when there are multiple streams to choose from.
         * This is used in conjunction with the setPreferredStreamType() method.
         */
        fun setMaxBitRate(rate: Int): Builder {
            maxBitRate = rate
            return this
        }

        /**
         * Show or hide the closed caption button on the player control bar.  Default is false.
         */
        fun showClosedCaption(enable: Boolean): Builder {
            mShowHideCc = enable
            return this
        }

        /**
         * Show or hide the countdown text on the video progress bar on the player control bar.
         */
        fun showCountdown(show: Boolean): Builder {
            mShowCountDown = show
            return this
        }

        /**
         * Show or hide the player video progress bar including the countdown text on the player control bar.
         */
        fun showProgressBar(show: Boolean): Builder {
            mShowProgressBar = show
            return this
        }

        /**
         * Make the server call to enable server side ads.
         * Will only work with ArcVideoStream objects that have ads enabled.
         */
        @Deprecated("Use {@link #setServerSideAdsEnabled(boolean)}")
        fun setServerSideAds(set: Boolean): Builder {
            mEnableServerSideAds = set
            return this
        }

        /**
         * Enable client side ad reporting.
         */
        @Deprecated("Use {@link #setClientSideAdsEnabled(boolean)}")
        fun setClientSideAds(set: Boolean): Builder {
            mEnableClientSideAds = set
            return this
        }

        /**
         * Make the server call to enable server side ads.
         * Will only work with ArcVideoStream objects that have ads enabled.
         */
        fun setServerSideAdsEnabled(set: Boolean): Builder {
            mEnableServerSideAds = set
            return this
        }

        /**
         * Enable client side ad reporting.
         */
        fun setClientSideAdsEnabled(set: Boolean): Builder {
            mEnableClientSideAds = set
            return this
        }

        /**
         * Automatically start playing the video after it becomes ready.  Default value is true.
         */
        fun setAutoStartPlay(play: Boolean): Builder {
            mAutoStartPlay = play
            return this
        }

        /**
         * Show or hide the rewind and fast forward buttons on the player control bar.
         */
        fun showSeekButton(show: Boolean): Builder {
            mShowSeekButton = show
            return this
        }

        /**
         * Start the video muted.  Default is false.
         */
        fun setStartMuted(muted: Boolean): Builder {
            mStartMuted = muted
            return this
        }

        /**
         * /**
         * Place focus on the skip button when it is shown during skippable ads.
         * This is for Google IMA ads only.
        */
         */
        fun setFocusSkipButton(focus: Boolean): Builder {
            mFocusSkipButton = focus
            return this
        }

        /**
         * CCStartMode has the following values:
         *   ON - Closed Captioning is on by default but can be turned off.
         *   OFF - Closed Captioning is off by default but can be turned on.
         *   DEFAULT - Closed Captioning follows the Captioning Service setting of the device.  Works for Android 19 and above only.
         */
        fun setCcStartMode(mode: CCStartMode): Builder {
            mCcStartMode = mode
            return this
        }

        /**
         * Determines if the player controls show automatically when playback ends.
         */
        fun setAutoShowControls(show: Boolean): Builder {
            mAutoShowControls = show
            return this
        }

        /**
         * Sets the cast manager for enabling Chromecast.
         */
        fun setCastManager(arcCastManager: ArcCastManager?): Builder {
            this.arcCastManager = arcCastManager
            return this
        }

        /**
         * If true then the track selection dialog will be shown that allows the user to select the closed
         * captioning track.  If false then the CC button will toggle between off and the default CC track.
         */
        fun setShowClosedCaptionTrackSelection(show: Boolean): Builder {
            mShowClosedCaptionTrackSelection = show
            return this
        }

        /**
         * This method takes a tag name for the view as well as the view.  The client app will still
         * have a pointer to the overlays so they can then control the visibility or other parameters
         * of the views from within the client code.  This method can be called multiple times
         * in order to add multiple overlays.
         */
        fun addOverlay(tag: String, overlay: View): Builder {
            mOverlays[tag] = overlay
            return this
        }

        /**
         * Add a single ad parameter to be sent to MediaTailor.
         * Multiple calls to this method can be made.  This method takes a key and a value
         * and will be added to the call as a JSON entry of the format
         * { “adParams” : {
         *       “key1”: “value1”,
         *       “key2”: “value2”
         *     }
         * }
         */
        fun addAdParam(key: String, `val`: String): Builder {
            mAdParams[key] = `val`
            return this
        }

        fun enablePAL(enable: Boolean): Builder {
            enablePAL = enable
            return this
        }

        fun setPalPartnerName(name: String): Builder {
            palPartnerName = name
            return this
        }

        fun setPalPpid(ppid: String): Builder {
            palPpid = ppid
            return this
        }

        fun setPalVersionName(name: String): Builder {
            palVersionName = name
            return this
        }

        fun enableOpenMeasurement(enable: Boolean): Builder {
            enableOmid = enable
            return this
        }

        fun setOmidPartnerName(name: String): Builder {
            omidPartnerName = name
            return this
        }

        fun setOmidPpid(ppid: String): Builder {
            omidPpid = ppid
            return this
        }

        fun setOmidVersionName(name: String): Builder {
            omidVersionName = name
            return this
        }

        /**
         * Set the amount of time the player controls are shown before disappearing.
         * Setting this to 0 or lower will cause the controls to always be displayed.
         */
        fun setControlsShowTimeoutMs(ms: Int): Builder {
            mControlsShowTimeoutMs = ms
            return this
        }

        /**
         * Turn on enhanced logging that will show up in the logcat log.
         */
        fun enableLogging(): Builder {
            isLoggingEnabled = true
            return this
        }

        /**
         * Fullscreen can be displayed in two different ways, as a dialog or by expanding the layout_height
         * and layout_width parameters of the ArcVideoFrame to match_parent.  This method determines if
         * the dialog should be used.  Default setting is false.  If your layout has the ArcVideoFrame
         * element a direct child of the root view then setting this to false will work because the frame
         * will expand to the full size of the screen.  If your layout has the ArcVideoFrame as a child of
         * a view that is not the root view and that parent is not the full size of the screen then you
         * should use a dialog because expanding the height and width of the video frame will not take
         * up the whole screen.
         */
        fun useDialogForFullscreen(use: Boolean): Builder {
            useFullScreenDialog = use
            return this
        }

        fun setKeepControlsSpaceOnHide(keep: Boolean): Builder {
            keepControlsSpaceOnHide = keep
            return this
        }

        /**
         * Disable showing the player controls when the user touches the screen.
         * This will require the user to handle video player touches.
         */
        fun setDisableControlsToggleWithTouch(disable: Boolean): Builder {
            disableControlsWithTouch = disable
            return this
        }

        /**
         * The string that is used as the User-Agent key that can be attached to the header of the call to enable
         * server side ads.
         */
        fun setUserAgent(agent: String): Builder {
            userAgent = agent
            return this
        }

        fun setArtWorkUrl(url: String): Builder {
            artWorkUrl = url
            return this
        }

        fun setShowNextPreviousButtons(value: Boolean): Builder {
            showNextPreviousButtons = value
            return this
        }

        fun setShouldDisableNextButton(value: Boolean): Builder {
            shouldDisableNextButton = value
            return this
        }

        fun setShouldDisablePreviousButton(value: Boolean): Builder {
            shouldDisablePreviousButton = value
            return this
        }

        fun setShouldShowBackButton(shouldShowBackButton: Boolean): Builder {
            showBackButton = shouldShowBackButton
            return this
        }

        fun setShouldShowFullScreenButton(shouldShowFullScreenButton: Boolean): Builder {
            showFullScreenButton = shouldShowFullScreenButton
            return this
        }

        fun setShouldShowTitleOnControls(shouldShowTitleOnControls: Boolean): Builder {
            showTitleOnController = shouldShowTitleOnControls
            return this
        }

        fun setShouldShowVolumeButton(showVolumeButton: Boolean): Builder {
            this.showVolumeButton = showVolumeButton
            return this
        }

        fun setVideoResizeMode(mode: VideoResizeMode): Builder {
            this.videoResizeMode = mode
            return this
        }
        /**
         * set this to true when you do not want any playback controls to appear at any time
         * this disables all the button listeners and should not display controls ever
         * default is false
         */
        fun setDisableControlsFully(disable: Boolean): Builder {
            this.disableControlsFully = disable
            return this
        }

        /**
         * set this to true when you do not want any error message overlay to appear on video
         * default is false
         */
        fun setDisableErrorOverlay(disable: Boolean): Builder {
            this.disableErrorOverlay = disable
            return this
        }

        fun build(): ArcXPVideoConfig {
            return ArcXPVideoConfig(
                activity = mActivity,
                videoFrame = mVideoFrame,
                isEnablePip = mEnablePip,
                mShowClosedCaption = mShowHideCc,
                isShowCountDown = mShowCountDown,
                isShowProgressBar = mShowProgressBar,
                isShowSeekButton = mShowSeekButton,
                viewsToHide = viewsToHide,
                isEnableAds = enableAds,
                adConfigUrl = adConfigUrl,
                adConfig = adConfig,
                preferredStreamType = preferredStreamType,
                maxBitRate = maxBitRate,
                isEnableServerSideAds = mEnableServerSideAds,
                isEnableClientSideAds = mEnableClientSideAds,
                isAutoStartPlay = mAutoStartPlay,
                isStartMuted = mStartMuted,
                isFocusSkipButton = mFocusSkipButton,
                ccStartMode = mCcStartMode,
                isAutoShowControls = mAutoShowControls,
                isShowClosedCaptionTrackSelection = mShowClosedCaptionTrackSelection,
                adParams = mAdParams,
                overlays = mOverlays,
                isEnablePAL = enablePAL,
                palPartnerName = palPartnerName,
                palPpid = palPpid,
                palVersionName = palVersionName,
                exoplayerVersion = exoplayerVersion,
                isEnableOmid = enableOmid,
                omidPartnerName = omidPartnerName,
                omidPpid = omidPpid,
                omidVersionName = omidVersionName,
                controlsShowTimeoutMs = mControlsShowTimeoutMs,
                isLoggingEnabled = isLoggingEnabled,
                isUseFullScreenDialog = useFullScreenDialog,
                arcCastManager = arcCastManager,
                isKeepControlsSpaceOnHide = keepControlsSpaceOnHide,
                isDisableControlsWithTouch = disableControlsWithTouch,
                userAgent = userAgent,
                artworkUrl = artWorkUrl,
                showNextPreviousButtons = showNextPreviousButtons,
                shouldDisableNextButton = shouldDisableNextButton,
                shouldDisablePreviousButton = shouldDisablePreviousButton,
                showBackButton = showBackButton,
                showFullScreenButton = showFullScreenButton,
                showTitleOnController = showTitleOnController,
                showVolumeButton = showVolumeButton,
                videoResizeMode = videoResizeMode,
                isDisableControlsFully = disableControlsFully,
                disableErrorOverlay = disableErrorOverlay
            )
        }
    }
}