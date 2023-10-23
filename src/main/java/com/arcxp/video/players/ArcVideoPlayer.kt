package com.arcxp.video.players

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.CaptioningManager
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData.TrackingErrorTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingVideoTypeData
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView.ControllerVisibilityListener
import com.google.android.exoplayer2.upstream.DataSpec
import java.util.Date
import java.util.Objects

internal class ArcVideoPlayer(
    private val playerState: PlayerState,
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
            } catch (e: Exception) {
            }
        }
        if (playerState.mLocalPlayerView != null) {
            try {
                if (playerState.mLocalPlayerView!!.parent is ViewGroup) {
                    (playerState.mLocalPlayerView!!.parent as ViewGroup).removeView(playerState.mLocalPlayerView)
                }
                playerState.mLocalPlayerView = null
            } catch (e: Exception) {
            }
        }
        if (playerState.videoTrackingSub != null) {
            try {
                playerState.videoTrackingSub!!.unsubscribe()
                playerState.videoTrackingSub = null
            } catch (e: Exception) {
            }
        }
        if (playerState.mLocalPlayer != null) {
            try {
                playerState.mLocalPlayer!!.stop()
                playerState.mLocalPlayer!!.release()
                playerState.mLocalPlayer = null
            } catch (e: Exception) {
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
            } catch (e: Exception) {
            }
        }
        if (!playerState.mIsFullScreen) {
            try {
                mListener.removePlayerFrame()
            } catch (e: Exception) {
            }
        }
        if (playerState.mCastPlayer != null) {
            try {
                playerState.mCastPlayer!!.setSessionAvailabilityListener(null)
                playerState.mCastPlayer!!.release()
            } catch (e: Exception) {
            }
        }
        if (playerState.mCastControlView != null) {
            try {
                playerState.mCastControlView!!.player = null
                if (playerState.mCastControlView!!.parent is ViewGroup) {
                    (playerState.mCastControlView!!.parent as ViewGroup).removeView(playerState.mCastControlView)
                }
                playerState.mCastControlView = null
            } catch (e: Exception) {
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
                            && visibilityState == View.VISIBLE
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
            if (video.url == null) { //TODO so this section.. sets mVideoId, then it is overwritten with video.id in playVideo() (even if it is null), probably can scrap or update to do something
                if (video.fallbackUrl != null) {
                    playerState.mVideoId = video.fallbackUrl
                } else {
                    playerState.mVideoId = ""
                }
            } else {
                playerState.mVideoId = video.url
            }
            playerState.mVideos?.add(video)
            playerState.mVideo = video
            playVideo()
        } catch (e: java.lang.Exception) {
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
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun start() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = true
            }
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun pause() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = false
            }
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun resume() {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.playWhenReady = true
                playerStateHelper.createTrackingEvent(TrackingType.ON_PLAY_RESUMED)
            }
        } catch (e: java.lang.Exception) {
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
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    override fun seekTo(ms: Int) {
        try {
            if (playerState.mLocalPlayer != null) {
                playerState.mLocalPlayer!!.seekTo(ms.toLong())
            }
        } catch (e: java.lang.Exception) {
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
        } catch (e: java.lang.Exception) {
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
            if (trackingHelper != null) {
                trackingHelper.fullscreen()
            }
            val fullScreenButton =
                playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!, R.drawable.FullScreenDrawableButtonCollapse
                    )
                )
            } else {
                playerStateHelper.logNullErrorIfEnabled("fullScreenButton", "setFullscreenUi")
            }
            playerState.mIsFullScreen = true
            playerStateHelper.createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
        } else {
            if (trackingHelper != null) {
                trackingHelper.normalScreen()
            }
            if (playerState.mLocalPlayerView != null) {
                val fullScreenButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!, R.drawable.FullScreenDrawableButton
                    )
                )
                if (mListener.isStickyPlayer) {
                    playerState.mLocalPlayerView!!.hideController()
                    playerState.mLocalPlayerView!!.requestLayout()
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
                playerState.mLocalPlayerView!!.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
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
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
        if (playerState.mCastControlView != null) {
            playerState.mCastControlView!!.setOnKeyListener { v, keyCode, event ->
                listener?.onKey(keyCode, event)
                false
            }
        }
    }

    override fun getPlayWhenReadyState() = if (playerState.currentPlayer != null) {
        playerState.currentPlayer!!.playWhenReady
    } else false


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return playerState.mLocalPlayerView!!.dispatchKeyEvent(event!!)
    }

    override fun getCurrentPosition(): Long {
        return if (playerState.currentPlayer != null) {
            playerState.currentPlayer!!.currentPosition
        } else {
            0
        }
    }

    override fun getCurrentTimelinePosition(): Long {
        return playerStateHelper.getCurrentTimelinePosition()
    }

    override fun getCurrentVideoDuration(): Long {
        return if (playerState.currentPlayer != null) {
            playerState.currentPlayer!!.duration
        } else 0
    }

    override fun toggleCaptions() {
        captionsManager.showCaptionsSelectionDialog()
    }

    override fun isPlaying() =
        playerState.currentPlayer?.playbackState == Player.STATE_READY && playerState.currentPlayer?.playWhenReady == true

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

    override fun getAdType(): Long {
        val adPlaying = playerState.mLocalPlayer != null && playerState.mLocalPlayer!!.isPlayingAd
        var adGroupTime: Long = 0

        if (adPlaying) {
            val timeline = playerState.mLocalPlayer!!.currentTimeline
            val period =
                timeline.getPeriod(playerState.mLocalPlayer!!.currentPeriodIndex, Timeline.Period())
            adGroupTime = period.getAdGroupTimeUs(playerState.mLocalPlayer!!.currentAdGroupIndex)
        }

        return adGroupTime
    }

    override fun getPlaybackState(): Int {
        return if (playerState.currentPlayer != null) {
            playerState.currentPlayer!!.playbackState
        } else 0
    }

    override fun isVideoCaptionEnabled(): Boolean {
        return try {
            if (playerState.mVideo != null && playerState.mVideo!!.ccStartMode === ArcXPVideoConfig.CCStartMode.DEFAULT) {
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
            } else playerState.mVideo != null && playerState.mVideo!!.ccStartMode === ArcXPVideoConfig.CCStartMode.ON
        } catch (e: java.lang.Exception) {
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


    override fun enableClosedCaption(enable: Boolean): Boolean {
        return captionsManager.enableClosedCaption(enable)
    }

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
            val artwork = mCastControlView.findViewById<ImageView>(R.id.exo_artwork)
            if (artwork != null) {
                artwork.visibility = View.VISIBLE
                if (playerState.mVideo != null && mConfig.artworkUrl != null) {
                    utils.loadImageIntoView(mConfig.artworkUrl, artwork)
                }
            }
            if (fullScreen != null) {
                fullScreen.visibility = View.VISIBLE
                fullScreen.setOnClickListener { v: View? -> toggleFullScreenCast() }
            }
            if (pipButton != null) {
                pipButton.visibility = View.GONE
            }
            if (volumeButton != null) {
                if (playerState.mVideo != null) {
                    volumeButton.visibility = View.VISIBLE
                    volumeButton.setOnClickListener { v: View? ->
                        //toggle local state
                        playerState.castMuteOn = !playerState.castMuteOn
                        arcCastManager.setMute(playerState.castMuteOn)
                        volumeButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                Objects.requireNonNull<Activity>(mConfig.activity),
                                if (playerState.castMuteOn) R.drawable.MuteDrawableButton else R.drawable.MuteOffDrawableButton
                            )
                        )
                    }
                    volumeButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            Objects.requireNonNull<Activity>(
                                mConfig.activity
                            ), R.drawable.MuteOffDrawableButton
                        )
                    )
                } else {
                    volumeButton.visibility = View.GONE
                }
            }
            if (ccButton != null) {
                if (playerState.mVideo != null && (playerState.mVideo!!.subtitleUrl != null || playerState.mVideo!!.isLive)) {
                    ccButton.visibility = View.VISIBLE
                    ccButton.setOnClickListener { v: View? ->
                        playerState.castSubtitlesOn = !playerState.castSubtitlesOn
                        arcCastManager.showSubtitles(playerState.castSubtitlesOn)
                        ccButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                Objects.requireNonNull<Activity>(mConfig.activity),
                                if (playerState.castSubtitlesOn) R.drawable.CcDrawableButton else R.drawable.CcOffDrawableButton
                            )
                        )
                    }
                    ccButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            Objects.requireNonNull<Activity>(
                                mConfig.activity
                            ), R.drawable.CcOffDrawableButton
                        )
                    )
                } else {
                    ccButton.visibility = View.GONE
                }
            }
            if (shareButton != null) {
                shareButton.setOnClickListener { v: View? ->
                    val videoData: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                    videoData.arcVideo = playerState.mVideo
                    videoData.position = mCastPlayer.currentPosition
                    playerStateHelper.onVideoEvent(TrackingType.ON_SHARE, videoData)
                    mListener.onShareVideo(playerState.mHeadline, playerState.mShareUrl)
                }
                shareButton.visibility =
                    if (TextUtils.isEmpty(playerState.mShareUrl)) View.GONE else View.VISIBLE
            }
            mListener.addVideoView(mCastControlView)
        }
    }

    private fun playVideo() {
        playerState.mIsLive = playerState.mVideo!!.isLive
        playerState.mHeadline = playerState.mVideo!!.headline
        playerState.mShareUrl = playerState.mVideo!!.shareUrl
        playerState.mVideoId = playerState.mVideo!!.url
        trackingHelper.initVideo(playerState.mVideo!!.url.orEmpty())
        playerStateHelper.initLocalPlayer()
        initCastPlayer()
        val castPlayer = playerState.mCastPlayer
        val exoPlayer = playerState.mLocalPlayer
        setCurrentPlayer(if (castPlayer != null && castPlayer.isCastSessionAvailable) castPlayer else exoPlayer!!)
        playerState.mLocalPlayerView!!.setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                trackingHelper.onTouch(event, currentTimelinePosition)
            }
            if (!mConfig.isDisableControlsWithTouch) {
                v.performClick()
                return@setOnTouchListener false
            }
            true
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
            if (playerState.mLocalPlayerView != null) {
                playerState.mLocalPlayerView!!.visibility = View.VISIBLE
                playerState.currentPlayView = playerState.mLocalPlayerView
            }
            if (playerState.mCastControlView != null) {
                playerState.mCastControlView!!.hide()
                playerState.mCastControlView!!.keepScreenOn = false
            }
        } else  /* currentPlayer == castPlayer */ {
            if (playerState.mLocalPlayerView != null) playerState.mLocalPlayerView!!.visibility =
                View.GONE
            if (mCastControlView != null) {
                mCastControlView.show()
                mCastControlView.keepScreenOn = true
                playerState.currentPlayView = mCastControlView
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
                } catch (e: java.lang.Exception) {
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
        if (playerState.currentPlayer != null && playerState.mVideo != null) {
            playerState.currentPlayer!!.playWhenReady = playerState.mVideo!!.autoStartPlay
            if (playerState.currentPlayer === playerState.mLocalPlayer && playerState.mLocalPlayer != null) {
                playOnLocal()
            } else if (playerState.currentPlayer === playerState.mCastPlayer && playerState.mCastPlayer != null) {
                playOnCast()
            }
            playerState.currentPlayer!!.seekTo(mListener.getSavedPosition(playerState.mVideoId))
            trackingHelper.onPlaybackStart()
        }
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
                            { unusedAdTagUri: AdsConfiguration? -> playerState.mAdsLoader },
                            playerState.mLocalPlayerView!!
                        )
                playerState.mAdsLoader!!.setPlayer(playerState.mLocalPlayer)
                val adUri = Uri.parse(
                    playerState.mVideo!!.adTagUrl!!.replace(
                        "\\[(?i)timestamp]".toRegex(), java.lang.Long.toString(
                            Date().time
                        )
                    )
                )
                val dataSpec = DataSpec(adUri)
                val pair = Pair("", adUri.toString())
                adsMediaSource = utils.createAdsMediaSource(
                    contentMediaSource,
                    dataSpec,
                    pair,
                    mediaSourceFactory,
                    playerState.mAdsLoader,
                    playerState.mLocalPlayerView
                )
            } catch (e: java.lang.Exception) {
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
            if (playerState.mVideo != null) {
                arcCastManager?.doCastSession(
                    playerState.mVideo!!,
                    mListener.getSavedPosition(playerState.mVideoId),
                    mConfig.artworkUrl
                )
            }
        } catch (e: UnsupportedOperationException) {
            mListener.onTrackingEvent(
                TrackingType.ON_ERROR_OCCURRED,
                TrackingErrorTypeData(playerState.mVideo, mListener.sessionId, null)
            )
        }
    }

    fun addToCast() {
        try {
            playerState.mCastPlayer!!.addMediaItem(createMediaItem(playerState.mVideo!!))
        } catch (e: UnsupportedOperationException) {
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
            val fullScreenButton =
                playerState.mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!.applicationContext, R.drawable.FullScreenDrawableButton
                    )
                )
            } else {
                playerStateHelper.logNullErrorIfEnabled(
                    "fullScreenButton",
                    "toggleFullScreenDialog"
                )
            }
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
            val fullScreenButton =
                playerState.mCastControlView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        mConfig.activity!!.applicationContext,
                        R.drawable.FullScreenDrawableButtonCollapse
                    )
                )
            } else {
                playerStateHelper.logNullErrorIfEnabled(
                    "fullScreenButton",
                    "toggleFullScreenDialog"
                )
            }
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
        if (playerState.mLocalPlayerView != null) {
            if (!mConfig.isDisableControlsFully) {
                playerState.mLocalPlayerView!!.useController = false
            }
        }
    }

    override fun onCastSessionAvailable() {
        setCurrentPlayer(playerState.mCastPlayer!!)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(playerState.mLocalPlayer!!)
    }

    fun isCasting(): Boolean {
        return playerState.currentPlayer === playerState.mCastPlayer
    }

    fun playVideoAtIndex(index: Int) {
        var index = index
        try {
            if (playerState.mVideos != null && !playerState.mVideos!!.isEmpty()) {
                if (index < 0) {
                    index = 0
                }
                if (index >= playerState.mVideos!!.size) {
                    index = playerState.mVideos!!.size - 1
                }
                if (!playerState.mIsFullScreen) {
                    mListener.addVideoView(playerState.mLocalPlayerView)
                } else {
                    playerStateHelper.addPlayerToFullScreen()
                }
                playerState.mVideoTracker = getInstance(
                    mListener,
                    playerState.mLocalPlayer!!,
                    trackingHelper,
                    playerState.mIsLive,
                    mConfig.activity!!
                )
                playerState.mVideo = playerState.mVideos!![index]
                if (playerState.currentPlayer === playerState.mLocalPlayer && playerState.mLocalPlayer != null) {
                    playOnLocal()
                } else if (playerState.currentPlayer === playerState.mCastPlayer && playerState.mCastPlayer != null) {
                    addToCast()
                }
                for (v in playerState.mFullscreenOverlays.values) {
                    if (v.parent != null && v.parent is ViewGroup) {
                        (v.parent as ViewGroup).removeView(v)
                    }
                    playerState.mLocalPlayerView!!.addView(v)
                }
                val contentMediaSource = captionsManager.createMediaSourceWithCaptions()
                var adsMediaSource: MediaSource? = null
                if (playerState.mVideo!!.shouldPlayAds && !TextUtils.isEmpty(playerState.mVideo!!.adTagUrl)) {
                    try {
                        playerState.mAdsLoader =
                            ImaAdsLoader.Builder(Objects.requireNonNull(mConfig.activity))
                                .setAdEventListener(adEventListener!!)
                                .build()
                        val mediaSourceFactory: MediaSource.Factory =
                            DefaultMediaSourceFactory(playerState.mMediaDataSourceFactory)
                                .setLocalAdInsertionComponents(
                                    { unusedAdTagUri: AdsConfiguration? -> playerState.mAdsLoader },
                                    playerState.mLocalPlayerView!!
                                )
                        playerState.mAdsLoader!!.setPlayer(playerState.mLocalPlayer)
                        val adUri = Uri.parse(
                            playerState.mVideo!!.adTagUrl!!.replace(
                                "\\[(?i)timestamp]".toRegex(), java.lang.Long.toString(
                                    Date().time
                                )
                            )
                        )
                        val dataSpec = DataSpec(adUri)
                        val pair = Pair("", adUri.toString())
                        adsMediaSource = utils.createAdsMediaSource(
                            contentMediaSource,
                            dataSpec,
                            pair,
                            mediaSourceFactory,
                            playerState.mAdsLoader,
                            playerState.mLocalPlayerView
                        ) //TODO test ads here too!!
                    } catch (e: java.lang.Exception) {
                        if (mConfig.isLoggingEnabled) {
                            Log.d(
                                "ArcVideoSDK",
                                "Error preparing ad for video " + playerState.mVideoId,
                                e
                            )
                        }
                    }
                }
                if (adsMediaSource != null) {
                    playerState.mLocalPlayer!!.setMediaSource(adsMediaSource)
                } else {
                    playerState.mLocalPlayer!!.setMediaSource(contentMediaSource!!)
                }
                playerStateHelper.setUpPlayerControlListeners()
            }
        } catch (e: java.lang.Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }
}