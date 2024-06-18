package com.arcxp.video.players

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.accessibility.CaptioningManager
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import com.arcxp.commons.util.Utils.createTimeStamp
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.VideoTracker.Companion.getInstance
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.cast.ArcCastManager.Companion.createMediaItem
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData.TrackingErrorTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingVideoTypeData
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import java.util.Objects

@SuppressLint("UnsafeOptInUsageError")
internal class ArcVideoPlayer(
    val playerState: PlayerState,
    private val playerStateHelper: PlayerStateHelper,
    private val mListener: VideoListener,
    private val mConfig: ArcXPVideoConfig,
    private val arcCastManager: ArcCastManager?,
    private val utils: Utils,
    private val trackingHelper: TrackingHelper,
    private val captionsManager: CaptionsManager,
) : VideoPlayer, SessionAvailabilityListener {

    var adEventListener: AdEventListener? = null
    var playerListener: Player.Listener? = null

    override fun release() {
        if (playerState.mIsFullScreen) {
            try {
                playerStateHelper.toggleFullScreenDialog(true)
            } catch (_: Exception) {
            }
        }
        if (playerState.mLocalPlayerView != null) {
            try {
                if (playerState.mLocalPlayerView!!.parent is ViewGroup) {
                    (playerState.mLocalPlayerView!!.parent as ViewGroup).removeView(playerState.mLocalPlayerView)
                }
                playerState.mLocalPlayerView = null
            } catch (_: Exception) {
            }
        }
        if (playerState.videoTrackingSub != null) {
            try {
                playerState.videoTrackingSub!!.unsubscribe()
                playerState.videoTrackingSub = null
            } catch (_: Exception) {
            }
        }
        if (playerState.mediaSession != null) {
            try {
                playerState.mediaSession!!.release()
                playerState.mediaSession = null
            } catch (_: Exception) {
            }
        }
        if (playerState.mLocalPlayer != null) {
            try {
                playerState.mLocalPlayer!!.stop()
                playerState.mLocalPlayer!!.release()
                playerState.mLocalPlayer = null
            } catch (_: Exception) {
            }
        }
        if (playerState.mTrackSelector != null) {
            playerState.mTrackSelector = null
        }
        if (playerState.mAdsLoader != null) {
            try {
                playerState.mAdsLoader!!.setPlayer(null)
                playerState.mAdsLoader!!.release()
                playerState.mAdsLoader = null
            } catch (_: Exception) {
            }
        }
        if (!playerState.mIsFullScreen) {
            try {
                mListener.removePlayerFrame()
            } catch (_: Exception) {
            }
        }
        if (playerState.mCastPlayer != null) {
            try {
                playerState.mCastPlayer!!.setSessionAvailabilityListener(null)
                playerState.mCastPlayer!!.release()
            } catch (_: Exception) {
            }
        }
        if (playerState.mCastControlView != null) {
            try {
                playerState.mCastControlView!!.player = null
                if (playerState.mCastControlView!!.parent is ViewGroup) {
                    (playerState.mCastControlView!!.parent as ViewGroup).removeView(playerState.mCastControlView)
                }
                playerState.mCastControlView = null
            } catch (_: Exception) {
            }
        }
    }

    override fun getId() = playerState.mVideoId

    override fun onStickyPlayerStateChanged(isSticky: Boolean) {
        if (playerState.mLocalPlayerView != null) {
            if (isSticky && !playerState.mIsFullScreen) {
                playerState.mLocalPlayerView!!.hideController()
                playerState.mLocalPlayerView!!.requestLayout()
                playerState.mLocalPlayerView!!.setControllerVisibilityListener(
                    ControllerVisibilityListener { visibilityState: Int ->
                        if (playerState.mLocalPlayerView != null
                            && visibilityState == VISIBLE
                        ) {
                            playerState.mLocalPlayerView!!.hideController()
                            playerState.mLocalPlayerView!!.requestLayout()
                        }
                    })
            } else {
                playerState.mLocalPlayerView!!.setControllerVisibilityListener(null as ControllerVisibilityListener?)
            }
        }
    }

    override fun getVideo() = playerState.mVideo

    override fun onActivityResume() {
        if (playerState.mIsFullScreen && playerState.mVideo != null && playerState.mLocalPlayerView == null) {
            playVideo(playerState.mVideo!!)
        }
    }

    override fun playVideo(video: ArcVideo) {
        try {
            if (video.id == null) { //TODO so this section.. sets mVideoId, then it is overwritten with video.id in playVideo() (even if it is null), probably can scrap or update to do something
                if (video.fallbackUrl != null) {
                    playerState.mVideoId = video.fallbackUrl
                } else {
                    playerState.mVideoId = ""
                }
            } else {
                playerState.mVideoId = video.id
            }
            playerState.mVideos?.add(video)
            playerState.mVideo = video
            playVideo()
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun playVideos(videos: MutableList<ArcVideo>) {
        if (videos.size == 0) {
            playerStateHelper.onVideoEvent(TrackingType.ERROR_PLAYLIST_EMPTY, null)
            return
        }
        playerState.mVideos = videos
        playerState.mVideo = videos[0]

        playVideo()
    }

    override fun addVideo(video: ArcVideo) {
        if (playerState.mVideos != null) {
            playerState.mVideos!!.add(video)
        }
    }

    override fun pausePlay(shouldPlay: Boolean) {
        try {
            if (playerState.mLocalPlayer != null && playerState.mLocalPlayerView != null) {
                playerState.mLocalPlayer!!.playWhenReady = shouldPlay
                playerState.mLocalPlayerView!!.hideController()
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun start() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = true
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun pause() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = false
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun resume() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = true
                playerStateHelper.createTrackingEvent(TrackingType.ON_PLAY_RESUMED)
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun stop() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = false
                playerState.mLocalPlayer!!.stop()
                playerState.mLocalPlayer!!.seekTo(0)
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun seekTo(ms: Int) {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.seekTo(ms.toLong())
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun setVolume(volume: Float) {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.volume = volume
                val volumeButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_volume)
                if (volume > 0.0f) {
                    volumeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            mConfig.activity!!, R.drawable.MuteOffDrawableButton
                        )
                    )
                } else {
                    volumeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            mConfig.activity!!, R.drawable.MuteDrawableButton
                        )
                    )
                }
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun setFullscreen(full: Boolean) {
        playerStateHelper.toggleFullScreenDialog(!full)

        if (!mConfig.isUseFullScreenDialog) {
            mListener.setFullscreen(full)
        }
    }

    override fun setFullscreenUi(full: Boolean) {
        if (full) {
            trackingHelper.fullscreen()
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
        } else {
            trackingHelper.normalScreen()
            playerState.mLocalPlayerView?.apply {
                if (mListener.isStickyPlayer) {
                    hideController()
                    requestLayout()
                }
            }
            playerState.mIsFullScreen = false
            val videoData = utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            if (playerState.mLocalPlayer != null) {
                videoData.position = playerState.mLocalPlayer!!.currentPosition
            }
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
        }
    }

    override fun setFullscreenListener(listener: ArcKeyListener?) {
        playerState.mArcKeyListener = listener
    }

    override fun setPlayerKeyListener(listener: ArcKeyListener?) {
        try {
            if (playerState.mLocalPlayerView != null) {
                playerState.mLocalPlayerView!!.setOnKeyListener { _: View?, keyCode: Int, event: KeyEvent ->
                    if (listener != null && event.action == KeyEvent.ACTION_UP) {
                        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                            listener.onBackPressed()
                        } else {
                            listener.onKey(keyCode, event)
                        }
                    }
                    false
                }
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
        if (playerState.mCastControlView != null) {
            playerState.mCastControlView!!.setOnKeyListener { _, keyCode, event ->
                listener?.onKey(keyCode, event)
                false
            }
        }
    }

    override fun getPlayWhenReadyState() = if (playerState.currentPlayer != null) {
        playerState.currentPlayer!!.playWhenReady
    } else false


    override fun onKeyEvent(event: KeyEvent) =
        playerState.mLocalPlayerView?.dispatchKeyEvent(event) == true

    override fun getCurrentPosition() = playerState.currentPlayer?.currentPosition ?: 0L


    override fun getCurrentTimelinePosition() = playerStateHelper.getCurrentTimelinePosition()

    override fun getCurrentVideoDuration() = playerState.currentPlayer?.duration ?: 0L

    override fun toggleCaptions() = captionsManager.showCaptionsSelectionDialog()


    override fun isPlaying() = playerState.currentPlayer?.let {
        it.playbackState == Player.STATE_READY && it.playWhenReady
    } ?: false

    override fun isFullScreen() = playerState.mIsFullScreen

    override fun getPlayControls() = playerState.mLocalPlayerView


    override fun showControls(show: Boolean) {
        if (playerState.mLocalPlayerView != null) {
            if (show && playerState.disabledControlsForAd) {
                Log.d("ArcVideoSDK", "Called showControls() but controls are disabled")
            }
            if (show && !playerState.disabledControlsForAd) {
                Log.d("ArcVideoSDK", "Calling showControls()")
                playerState.mLocalPlayerView!!.showController()
                return
            }
            if (!show) {
                Log.d("ArcVideoSDK", "Calling hideControls()")
                playerState.mLocalPlayerView!!.hideController()
            }
        }
    }

    fun toggleAutoShow(show: Boolean) {
        playerState.mLocalPlayerView!!.controllerAutoShow = if (show) {
            mConfig.isAutoShowControls
        } else false
    }

    override fun getAdType(): Long {
        val adPlaying = playerState.mLocalPlayer?.isPlayingAd == true
        var adGroupTime: Long = 0

        if (adPlaying) {
            val timeline = playerState.mLocalPlayer!!.currentTimeline
            val period =
                timeline.getPeriod(playerState.mLocalPlayer!!.currentPeriodIndex, Timeline.Period())
            adGroupTime = period.getAdGroupTimeUs(playerState.mLocalPlayer!!.currentAdGroupIndex)
        }

        return adGroupTime
    }

    override fun getPlaybackState() = playerState.currentPlayer?.playbackState ?: 0

    override fun isVideoCaptionEnabled(): Boolean {
        return try {
            playerState.mVideo?.let {
                if (it.ccStartMode == ArcXPVideoConfig.CCStartMode.DEFAULT) {
                    var defaultValue = false
                    val service = mConfig.activity!!
                        .getSystemService(Context.CAPTIONING_SERVICE)
                    if (service is CaptioningManager) {
                        defaultValue = service.isEnabled
                    }
                    PrefManager.getBoolean(
                        Objects.requireNonNull(mConfig.activity),
                        PrefManager.IS_CAPTIONS_ENABLED,
                        defaultValue
                    )
                } else
                    it.ccStartMode == ArcXPVideoConfig.CCStartMode.ON
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun isClosedCaptionTurnedOn() =
        PrefManager.getBoolean(
            mConfig.activity,
            PrefManager.IS_CAPTIONS_ENABLED,
            false
        )


    override fun isClosedCaptionAvailable() = captionsManager.isClosedCaptionAvailable()


    override fun enableClosedCaption(enable: Boolean) = captionsManager.enableClosedCaption(enable)


    override fun setCcButtonDrawable(ccButtonDrawable: Int): Boolean {
        if (playerState.ccButton != null) {
            playerState.ccButton!!.setImageDrawable(
                ContextCompat.getDrawable(
                    mConfig.activity!!, ccButtonDrawable
                )
            )
            return true
        }
        return false
    }

    override fun getOverlay(tag: String?): View {
        return playerState.mFullscreenOverlays[tag]!!
    }
    override fun isMinimalControlsNow() = playerStateHelper.isMinimalModeNow()

    override fun removeOverlay(tag: String?) {
        val v = playerState.mFullscreenOverlays[tag]
        playerState.mFullscreenOverlays.remove(tag)
        (v!!.parent as ViewGroup).removeView(v)
    }

    private fun initCastPlayer() {
        if (arcCastManager != null) {
            val mCastPlayer: CastPlayer = utils.createCastPlayer(arcCastManager.getCastContext())
            playerState.mCastPlayer = mCastPlayer
            mCastPlayer.addListener(playerListener!!)
            mCastPlayer.setSessionAvailabilityListener(this)
            val mCastControlView: PlayerControlView = utils.createPlayerControlView()
            playerState.mCastControlView = mCastControlView
            mCastControlView.id = R.id.wapo_cast_control_view
            mCastControlView.player = mCastPlayer
            mCastControlView.showTimeoutMs = -1
            val fullScreen = mCastControlView.findViewById<ImageButton>(R.id.exo_fullscreen)
            val pipButton = mCastControlView.findViewById<ImageButton>(R.id.exo_pip)
            val shareButton = mCastControlView.findViewById<ImageButton>(R.id.exo_share)
            val volumeButton = mCastControlView.findViewById<ImageButton>(R.id.exo_volume)
            val ccButton = mCastControlView.findViewById<ImageButton>(R.id.exo_cc)
            if (fullScreen != null) {
                fullScreen.visibility = VISIBLE
                fullScreen.setOnClickListener { toggleFullScreenCast() }
            }
            if (pipButton != null) {
                pipButton.visibility = GONE
            }
            if (volumeButton != null) {
                volumeButton.visibility = VISIBLE
                volumeButton.setOnClickListener {
                    //toggle local state
                    val newValue = !playerState.castMuteOn
                    playerState.castMuteOn = newValue
                    arcCastManager.setMute(newValue)
                    volumeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            mConfig.activity!!.applicationContext,
                            if (playerState.castMuteOn) R.drawable.MuteDrawableButton else R.drawable.MuteOffDrawableButton
                        )
                    )
                }
                volumeButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!.applicationContext,
                        R.drawable.MuteOffDrawableButton
                    )
                )
            }
            if (ccButton != null) {
                if (playerState.mVideo!!.subtitleUrl != null || playerState.mVideo!!.isLive) {
                    ccButton.visibility = VISIBLE
                    ccButton.setOnClickListener {
                        playerState.castSubtitlesOn = !playerState.castSubtitlesOn
                        arcCastManager.showSubtitles(playerState.castSubtitlesOn)
                        ccButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                mConfig.activity!!.applicationContext,
                                if (playerState.castSubtitlesOn) R.drawable.CcDrawableButton else R.drawable.CcOffDrawableButton
                            )
                        )
                    }
                    ccButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            mConfig.activity!!.applicationContext,
                            R.drawable.CcOffDrawableButton
                        )
                    )
                } else {
                    ccButton.visibility = GONE
                }
            }
            if (shareButton != null) {
                shareButton.setOnClickListener {
                    val videoData: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                    videoData.arcVideo = playerState.mVideo
                    videoData.position = mCastPlayer.currentPosition
                    playerStateHelper.onVideoEvent(TrackingType.ON_SHARE, videoData)
                    mListener.onShareVideo(playerState.mHeadline, playerState.mShareUrl)
                }
                shareButton.visibility =
                    if (TextUtils.isEmpty(playerState.mShareUrl)) GONE else VISIBLE
            }
            mListener.addVideoView(mCastControlView)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun playVideo() {
        playerState.mIsLive = playerState.mVideo!!.isLive
        playerState.mHeadline = playerState.mVideo!!.headline
        playerState.mShareUrl = playerState.mVideo!!.shareUrl
        playerState.mVideoId = playerState.mVideo!!.id
        trackingHelper.initVideo(playerState.mVideo!!.id.orEmpty())
        playerStateHelper.initLocalPlayer()
        initCastPlayer()
        val castPlayer = playerState.mCastPlayer
        val exoPlayer = playerState.mLocalPlayer
        setCurrentPlayer(if (castPlayer != null && castPlayer.isCastSessionAvailable) castPlayer else exoPlayer!!)
        playerState.mLocalPlayerView!!.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                trackingHelper.onTouch(event, currentTimelinePosition)
            }
            return@setOnTouchListener false
        }
    }

    private fun setCurrentPlayer(currentPlayer: Player) {
        if (playerState.currentPlayer === currentPlayer) {
            return
        }

        // View management.
        val mLocalPlayer = playerState.mLocalPlayer
        val mCastControlView = playerState.mCastControlView
        if (currentPlayer === mLocalPlayer) {
            playerState.mLocalPlayerView!!.apply {
                visibility = VISIBLE
                playerState.currentPlayView = this
            }
            playerState.mCastControlView?.apply {
                hide()
                keepScreenOn = false
            }
        } else  /* currentPlayer == castPlayer */ {
            playerState.mLocalPlayerView!!.visibility = GONE
            mCastControlView!!.apply {
                show()
                keepScreenOn = true
                playerState.currentPlayView = this
            }
        }
        val previousPlayer = playerState.currentPlayer
        if (previousPlayer != null) {
            if (previousPlayer.playbackState != Player.STATE_ENDED) {
                mListener.setSavedPosition(playerState.mVideoId, previousPlayer.currentPosition)
            }
            if (playerState.mVideo!!.shouldPlayAds && !TextUtils.isEmpty(playerState.mVideo!!.adTagUrl)) {
                try {
                    playerState.mAdsLoader =
                        ImaAdsLoader.Builder(mConfig.activity!!.applicationContext)
                            .setAdEventListener(adEventListener!!)
                            .build()
                    playerState.mAdsLoader!!.adsLoader!!.addAdsLoadedListener(
                        utils.createAdsLoadedListener(
                            mListener,
                            playerState.mVideo,
                            this
                        )
                    )
                    playerState.mAdsLoader!!.setPlayer(mLocalPlayer) //TODO test ads here!!
                } catch (e: Exception) {
                    if (mConfig.isLoggingEnabled) {
                        Log.e(
                            "ArcVideoSDK",
                            "Error preparing ad for video " + playerState.mVideoId,
                            e
                        )
                    }
                }
            }
            previousPlayer.stop()
            previousPlayer.clearMediaItems()
        }
        playerState.currentPlayer = currentPlayer
        playerState.mVideoTracker = getInstance(
            mListener, currentPlayer, trackingHelper,
            playerState.mIsLive, Objects.requireNonNull<Activity?>(mConfig.activity)
        )
        startVideoOnCurrentPlayer()
    }

    private fun startVideoOnCurrentPlayer() {
        playerState.currentPlayer!!.playWhenReady = playerState.mVideo!!.autoStartPlay

        if (playerState.currentPlayer == playerState.mLocalPlayer) {
            playOnLocal()
        } else /* if (playerState.currentPlayer == playerState.mCastPlayer) */ {
            playOnCast()
        }
        playerState.currentPlayer!!.seekTo(mListener.getSavedPosition(playerState.mVideoId))
        trackingHelper.onPlaybackStart()

    }

    fun playOnLocal() {
        val contentMediaSource: MediaSource = captionsManager.createMediaSourceWithCaptions()!!
        var adsMediaSource: MediaSource? = null
        if (playerState.mVideo!!.shouldPlayAds && !TextUtils.isEmpty(playerState.mVideo!!.adTagUrl)) {
            try {
                playerState.mAdsLoader =
                    ImaAdsLoader.Builder(mConfig.activity!!.applicationContext)
                        .setAdEventListener(adEventListener!!)
                        .build()
                val mediaSourceFactory: MediaSource.Factory =
                    DefaultMediaSourceFactory(playerState.mMediaDataSourceFactory)
                        .setLocalAdInsertionComponents(
                            { playerState.mAdsLoader },
                            playerState.mLocalPlayerView!!
                        )
                playerState.mAdsLoader!!.setPlayer(playerState.mLocalPlayer)
                val adUri = Uri.parse(
                    playerState.mVideo!!.adTagUrl!!.replace(
                        "\\[(?i)timestamp]".toRegex(), createTimeStamp()
                    )
                )
                val dataSpec = utils.createDataSpec(adUri)
                val pair = Pair("", adUri.toString())
                adsMediaSource = utils.createAdsMediaSource(
                    contentMediaSource,
                    dataSpec,
                    pair,
                    mediaSourceFactory,
                    playerState.mAdsLoader,
                    playerState.mLocalPlayerView
                )
            } catch (e: Exception) {
                mListener.onError(ArcVideoSDKErrorType.INIT_ERROR, e.message, e)
            }
        }
        captionsManager.initVideoCaptions()
        if (adsMediaSource != null) {
            playerState.mLocalPlayer!!.setMediaSource(adsMediaSource)
            playerState.mLocalPlayer!!.prepare()
        } else {
            playerState.mLocalPlayer!!.setMediaSource(contentMediaSource)
            playerState.mLocalPlayer!!.prepare()
            playerStateHelper.setUpPlayerControlListeners()
        }
    }

    private fun playOnCast() {
        try {
            arcCastManager?.doCastSession(
                playerState.mVideo!!,
                mListener.getSavedPosition(playerState.mVideoId),
                mConfig.artworkUrl
            )
        } catch (e: Exception) {
            mListener.onTrackingEvent(
                TrackingType.ON_ERROR_OCCURRED,
                TrackingErrorTypeData(playerState.mVideo, mListener.sessionId, null)
            )
        }
    }

    fun addToCast() {
        try {
            playerState.mCastPlayer!!.addMediaItem(createMediaItem(playerState.mVideo!!))
        } catch (e: Exception) {
            mListener.onTrackingEvent(
                TrackingType.ON_ERROR_OCCURRED,
                TrackingErrorTypeData(playerState.mVideo, mListener.sessionId, null)
            )
        }
    }

    private fun toggleFullScreenCast() {
        if (playerState.castFullScreenOn) {
            playerState.castFullScreenOn = false
            (playerState.mCastControlView!!.parent as ViewGroup).removeView(playerState.mCastControlView)
            mListener.playerFrame.addView(playerState.mCastControlView)
            if (playerState.mFullScreenDialog != null) {
                playerState.mFullScreenDialog!!.setOnDismissListener(null)
                playerState.mFullScreenDialog!!.dismiss()
            }
            val videoData = utils.createTrackingVideoTypeData()
            videoData.arcVideo = playerState.mVideo
            videoData.position = playerState.mCastPlayer!!.currentPosition
            playerStateHelper.onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
            trackingHelper.normalScreen()
            for (v in playerState.mFullscreenOverlays.values) {
                (v.parent as ViewGroup).removeView(v)
                mListener.playerFrame.addView(v)
            }
            playerState.mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                ?.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!.applicationContext, R.drawable.exo_icon_fullscreen_enter
                    )
                )
        } else {
            playerState.castFullScreenOn = true
            if (playerState.mCastControlView!!.parent != null) {
                (playerState.mCastControlView!!.parent as ViewGroup).removeView(playerState.mCastControlView)
            }
            playerState.mFullScreenDialog =
                utils.createFullScreenDialog(Objects.requireNonNull(mConfig.activity))
            playerState.mFullScreenDialog!!.addContentView(
                playerState.mCastControlView!!,
                utils.createLayoutParams()
            )
            playerState.mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                ?.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!.applicationContext,
                        R.drawable.exo_controls_fullscreen_exit
                    )
                )
            playerStateHelper.addOverlayToFullScreen()
            playerState.mFullScreenDialog!!.show()
            playerState.mFullScreenDialog!!.setOnDismissListener { toggleFullScreenCast() }
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
            trackingHelper.fullscreen()
        }
    }

    fun disableControls() {
        playerState.disabledControlsForAd = true
        playerState.adPlaying = true
        if (!mConfig.isDisableControls) {
            playerState.mLocalPlayerView?.useController = false
        }

    }

    override fun onCastSessionAvailable() = setCurrentPlayer(playerState.mCastPlayer!!)
    override fun onCastSessionUnavailable() = setCurrentPlayer(playerState.mLocalPlayer!!)
    fun isCasting() = playerState.currentPlayer == playerState.mCastPlayer
}