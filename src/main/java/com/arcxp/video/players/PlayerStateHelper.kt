package com.arcxp.video.players

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import com.arcxp.commons.util.BuildVersionProvider
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingVideoTypeData
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import java.util.Objects

/**
 * PlayerStateHelper is a class responsible for managing the state and behavior of the video player within the ArcXP platform.
 * It handles the initialization, configuration, and control of the video player, including managing captions, tracking events, and fullscreen mode.
 *
 * The class defines the following properties:
 * - playerState: An instance of PlayerState that holds the current state of the video player.
 * - trackingHelper: An instance of TrackingHelper for tracking video events.
 * - utils: An instance of Utils for utility functions.
 * - mListener: An instance of VideoListener for handling video-related events.
 * - captionsManager: An instance of CaptionsManager for managing video captions.
 * - buildVersionProvider: An instance of BuildVersionProvider for providing build version information.
 *
 * Usage:
 * - Create an instance of PlayerStateHelper with the necessary parameters.
 * - Use the provided methods to initialize and control the video player.
 *
 * Example:
 *
 * val playerStateHelper = PlayerStateHelper(playerState, trackingHelper, utils, mListener, captionsManager, buildVersionProvider)
 * playerStateHelper.initLocalPlayer()
 *
 * Note: Ensure that all required properties are properly set before using the PlayerStateHelper instance.
 *
 * @property playerState An instance of PlayerState that holds the current state of the video player.
 * @property trackingHelper An instance of TrackingHelper for tracking video events.
 * @property utils An instance of Utils for utility functions.
 * @property mListener An instance of VideoListener for handling video-related events.
 * @property captionsManager An instance of CaptionsManager for managing video captions.
 * @property buildVersionProvider An instance of BuildVersionProvider for providing build version information.
 * @method initLocalPlayer Initializes the local video player.
 * @method setUpPlayerControlListeners Sets up listeners for player control events.
 * @method toggleFullScreenDialog Toggles the fullscreen mode of the video player.
 * @method onVideoEvent Handles video events and triggers tracking.
 * @method onPipEnter Enters Picture-in-Picture mode.
 * @method onPipExit Exits Picture-in-Picture mode.
 * @method haveMoreVideosToPlay Checks if there are more videos to play.
 * @method createTrackingEvent Creates a tracking event for the video player.
 * @method addOverlayToFullScreen Adds overlays to the fullscreen video player.
 * @method isMinimalModeNow Checks if the player is in minimal mode.
 */
@SuppressLint("UnsafeOptInUsageError")
internal class PlayerStateHelper(
    private val playerState: PlayerState,
    private val trackingHelper: TrackingHelper,
    private val utils: Utils,
    private val mListener: VideoListener,
    private val captionsManager: CaptionsManager,
    private val buildVersionProvider: BuildVersionProvider
) {
    var playerListener: Player.Listener? = null
    var playVideoAtIndex: ((Int) -> Unit)? = null
    private fun setVideoCaptionsStartupDrawable() = playerState.ccButton?.let {
        val enabled = PrefManager.getBoolean(
            Objects.requireNonNull<Activity>(playerState.config.activity),
            PrefManager.IS_CAPTIONS_ENABLED,
            false
        ) || playerState.config.ccStartMode === ArcXPVideoConfig.CCStartMode.ON
        it.setImageDrawable(
            ContextCompat.getDrawable(
                Objects.requireNonNull<Activity>(
                    playerState.config.activity
                ), if (enabled) R.drawable.CcDrawableButton else R.drawable.CcOffDrawableButton
            )
        )
    }


    fun initLocalPlayer() {
        playerState.mTrackSelector = utils.createDefaultTrackSelector()
        val exoPlayer: ExoPlayer = utils.createExoPlayer(playerState.mTrackSelector)
        playerState.mediaSession = DependencyFactory.createMediaSession(
            playerState.config.activity!!.applicationContext,
            exoPlayer
        )
        playerState.mLocalPlayer = exoPlayer
        playerState.mLocalPlayer!!.addListener(playerListener!!)
        val playerView: PlayerView = utils.createPlayerView()
        playerView.layoutParams = utils.createLayoutParams()
        playerView.resizeMode = playerState.config.videoResizeMode.mode()
        playerView.id = R.id.wapo_player_view
        playerView.player = exoPlayer
        playerState.mLocalPlayerView = playerView
        playerState.title = playerView.findViewById(R.id.styled_controller_title_tv)
        if (playerState.mVideo!!.startMuted) {
            playerState.mCurrentVolume = exoPlayer.volume
            exoPlayer.volume = 0f
        }
        setUpPlayerControlListeners()
        setAudioAttributes(exoPlayer)
        if (!playerState.mIsFullScreen) {
            mListener.addVideoView(playerView)
        } else {
            addPlayerToFullScreen()
        }
        for (v in playerState.mFullscreenOverlays.values) {
            playerView.addView(v)
        }
        if (playerState.config.isDisableControls) {
            playerView.useController = false
        } else {
            playerState.ccButton = playerView.findViewById(R.id.exo_cc)
            setVideoCaptionsStartupDrawable()
        }
        playerView.setFullscreenButtonClickListener {
            toggleFullScreenDialog(
                playerState.mIsFullScreen
            )
        }
    }

    private fun showHideFullScreen() =
        if (playerState.config.showFullScreenButton) VISIBLE else GONE

    fun setUpPlayerControlListeners() {
        if (!playerState.config.isDisableControls) {
            try {
                if (playerState.mLocalPlayer == null || playerState.mLocalPlayerView == null) {
                    return
                }
                playerState.mLocalPlayerView!!.apply {
                    findViewById<ImageButton>(R.id.exo_fullscreen)?.apply {
                        visibility = showHideFullScreen()
                    }
                    findViewById<ImageButton>(R.id.exo_minimal_fullscreen)?.apply {
                        visibility = showHideFullScreen()
                    }
                }

                val shareButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_share)
                if (shareButton != null) {
                    shareButton.setOnClickListener {
                        val videoData: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                        videoData.arcVideo = playerState.mVideo
                        videoData.position = playerState.mLocalPlayer!!.currentPosition
                        onVideoEvent(TrackingType.ON_SHARE, videoData)
                        mListener.onShareVideo(playerState.mHeadline, playerState.mShareUrl)
                    }
                    if (TextUtils.isEmpty(playerState.mShareUrl)) {
                        shareButton.visibility = if (playerState.config.isKeepControlsSpaceOnHide)
                            INVISIBLE else GONE
                    } else {
                        shareButton.visibility = VISIBLE
                    }
                }
                val pipButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_pip)
                if (pipButton != null) {
                    if (buildVersionProvider.sdkInt() < Build.VERSION_CODES.O || !mListener.isPipEnabled) {
                        pipButton.visibility = GONE
                    }
                    pipButton.setOnClickListener { onPipEnter() }
                }
                val volumeButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_volume)
                if (volumeButton != null) {
                    if (playerState.config.showVolumeButton) {
                        volumeButton.visibility = VISIBLE
                        volumeButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                Objects.requireNonNull<Activity>(
                                    playerState.config.activity
                                ),
                                if (playerState.mLocalPlayer!!.volume != 0f) R.drawable.MuteOffDrawableButton else R.drawable.MuteDrawableButton
                            )
                        )
                        volumeButton.setOnClickListener {
                            val videoData: TrackingVideoTypeData =
                                utils.createTrackingVideoTypeData()
                            videoData.arcVideo = playerState.mVideo
                            videoData.position = playerState.mLocalPlayer!!.currentPosition
                            if (playerState.mLocalPlayer!!.volume != 0f) {
                                playerState.mCurrentVolume = playerState.mLocalPlayer!!.volume
                                playerState.mLocalPlayer!!.volume = 0f
                                volumeButton.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        Objects.requireNonNull<Activity>(playerState.config.activity),
                                        R.drawable.MuteDrawableButton
                                    )
                                )
                                mListener.onTrackingEvent(TrackingType.ON_MUTE, videoData)
                                trackingHelper.volumeChange(0f)
                            } else {
                                playerState.mLocalPlayer!!.volume = playerState.mCurrentVolume
                                volumeButton.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        Objects.requireNonNull<Activity>(playerState.config.activity),
                                        R.drawable.MuteOffDrawableButton
                                    )
                                )
                                mListener.onTrackingEvent(TrackingType.ON_UNMUTE, videoData)
                                trackingHelper.volumeChange(playerState.mCurrentVolume)
                            }
                        }
                    } else {
                        volumeButton.visibility = GONE
                    }
                }
                playerState.ccButton = playerState.mLocalPlayerView!!.findViewById(R.id.exo_cc)
                if (playerState.ccButton != null) {
                    playerState.ccButton!!.setOnClickListener {
                        if (playerState.config.isShowClosedCaptionTrackSelection) {
                            captionsManager.showCaptionsSelectionDialog()
                        } else {
                            captionsManager.toggleClosedCaption()
                        }
                    }
                    if (playerState.config.enableClosedCaption() && captionsManager.isClosedCaptionAvailable()) {
                        playerState.ccButton!!.visibility = VISIBLE
                    } else {
                        if (playerState.config.isKeepControlsSpaceOnHide) {
                            playerState.ccButton!!.visibility = INVISIBLE
                        } else {
                            playerState.ccButton!!.visibility = GONE
                        }
                    }
                }
                val nextButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_next_button)
                val previousButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_prev_button)
                if (playerState.config.showNextPreviousButtons) {
                    //separated these out in case somebody wants a next and not previous or vice versa
                    if (nextButton != null) {
                        nextButton.visibility = VISIBLE
                        nextButton.setOnClickListener { v: View? ->
                            createTrackingEvent(
                                TrackingType.NEXT_BUTTON_PRESSED
                            )
                        }
                        if (playerState.config.shouldDisableNextButton) {
                            nextButton.isEnabled = false
                            nextButton.alpha = 0.5f
                        }
                    }
                    if (previousButton != null) {
                        previousButton.visibility = VISIBLE
                        previousButton.setOnClickListener {
                            createTrackingEvent(
                                TrackingType.PREV_BUTTON_PRESSED
                            )
                        }
                        if (playerState.config.shouldDisablePreviousButton) {
                            previousButton.isEnabled = false
                            previousButton.alpha = 0.5f
                        }
                    }
                    //case of multiple videos being played, we enable next/prev functionality within sdk (and callbacks)
                    if (playingListOfVideos()) {
                        if (nextButton != null) {
                            if (haveMoreVideosToPlay()) {
                                nextButton.isEnabled = true
                                nextButton.setOnClickListener {
                                    playVideoAtIndex?.let { it(playerState.incrementVideoIndex(true)) }
                                    createTrackingEvent(TrackingType.NEXT_BUTTON_PRESSED)
                                }
                            } else {
                                nextButton.alpha = 0.5f
                                nextButton.isEnabled = false
                            }
                        }
                        previousButton?.setOnClickListener {
                            playVideoAtIndex?.let { it(playerState.incrementVideoIndex(false)) }
                            createTrackingEvent(TrackingType.PREV_BUTTON_PRESSED)
                        }
                    }
                } else {
                    if (nextButton != null) {
                        nextButton.visibility = GONE
                    }
                    if (previousButton != null) {
                        previousButton.visibility = GONE
                    }
                }


                //seek buttons

                playerState.mLocalPlayerView!!.setShowFastForwardButton(shouldShowSeekButtons())
                playerState.mLocalPlayerView!!.setShowRewindButton(shouldShowSeekButtons())

                //progress bar
                val exoDuration =
                    playerState.mLocalPlayerView!!.findViewById<View>(R.id.exo_duration)
                val exoProgress =
                    playerState.mLocalPlayerView!!.findViewById<DefaultTimeBar>(R.id.exo_progress)
                exoProgress?.apply {
                    setScrubberColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarScrubberColor
                        )
                    )
                    setPlayedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarPlayedColor
                        )
                    )
                    setUnplayedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarUnplayedColor
                        )
                    )
                    setBufferedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarBufferedColor
                        )
                    )
                    setAdMarkerColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.AdMarkerColor
                        )
                    )
                    setPlayedAdMarkerColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.AdPlayedMarkerColor
                        )
                    )
                }
                val exoTimeBarLayout =
                    playerState.mLocalPlayerView!!.findViewById<LinearLayout>(R.id.exo_time)

                if (playerState.mIsLive) {
                    exoTimeBarLayout?.visibility = GONE
                    exoProgress?.visibility = GONE
                } else {
                    if (!playerState.config.isShowProgressBar) {
                        exoTimeBarLayout?.visibility = GONE
                        exoProgress?.visibility = GONE
                    } else {
                        exoTimeBarLayout?.visibility = VISIBLE
                        exoProgress?.visibility = VISIBLE
                        exoDuration?.visibility =
                            if (playerState.config.isShowCountDown) VISIBLE else GONE
                    }
                }
                playerState.mLocalPlayerView!!.requestFocus() //TODO continue investigating this for fire tv// This doesn't seem to help anything, and I cannot tell this logic accomplishes anything
                if (playerState.config.controlsShowTimeoutMs != null) {
                    playerState.mLocalPlayerView!!.controllerShowTimeoutMs =
                        playerState.config.controlsShowTimeoutMs
                }
                playerState.mLocalPlayerView!!.controllerHideOnTouch =
                    playerState.config.isHideControlsWithTouch
                playerState.mLocalPlayerView!!.controllerAutoShow =
                    playerState.config.isAutoShowControls
                //playerState.mLocalPlayerView!!.setControllerHideDuringAds(playerState.config.isHideControlsDuringAds)
                if (playerState.title != null) {
                    if (playerState.config.showTitleOnController) {
                        if (playerState.mVideo != null) {
                            playerState.title!!.text = playerState.mVideo!!.headline
                            playerState.title!!.visibility = VISIBLE
                        }
                    } else {
                        playerState.title!!.visibility = INVISIBLE
                    }
                }
            } catch (e: Exception) {
                mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
            }
        }
    }

    private fun setAudioAttributes(exoPlayer: ExoPlayer) {
        val audioAttributes = utils.createAudioAttributeBuilder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        exoPlayer.setAudioAttributes(audioAttributes, true)
    }

    fun getCurrentTimelinePosition() = if (playerState.mLocalPlayer != null) {
        try {
            playerState.mLocalPlayer!!.currentPosition - playerState.mLocalPlayer!!.currentTimeline.getPeriod(
                playerState.mLocalPlayer!!.currentPeriodIndex, playerState.period
            ).positionInWindowMs
        } catch (e: Exception) {
            0
        }
    } else {
        0
    }

    fun addPlayerToFullScreen() =
        playerState.mFullScreenDialog?.let { dialog ->
            playerState.mLocalPlayerView?.let { localPlayerView ->
                dialog.addContentView(
                    localPlayerView,
                    utils.createLayoutParams()
                )
            }
        }


    fun onVideoEvent(trackingType: TrackingType, value: TrackingTypeData?) {
        if (trackingType == TrackingType.VIDEO_PERCENTAGE_WATCHED && playerState.mIsLive) {
            return
        }
        Log.e("ArcVideoSDK", "onVideoEvent $trackingType")
        mListener.onTrackingEvent(trackingType, value)
    }

    @Synchronized
    fun toggleFullScreenDialog(isFullScreen: Boolean) {
        if (!isFullScreen) {
            playerState.mFullScreenDialog =
                utils.createFullScreenDialog(playerState.config.activity!!)
            playerState.mFullScreenDialog?.setOnKeyListener { _: DialogInterface?, keyCode: Int, event: KeyEvent ->
                //we need to avoid intercepting volume controls
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    return@setOnKeyListener false
                }
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    return@setOnKeyListener false
                }
                if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                    return@setOnKeyListener false
                } //TODO improve this function
                //we do this so we don't get trigger for down and up
                if (event.action != KeyEvent.ACTION_UP) {
                    return@setOnKeyListener true
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mListener.isPipEnabled) {
                        onPipEnter()
                    } else {
                        playerState.mArcKeyListener?.onBackPressed()
                    }
                } else {
                    if (playerState.firstAdCompleted || !playerState.config.isEnableAds) {
                        if (playerState.mLocalPlayerView?.isControllerFullyVisible == false) {
                            playerState.mLocalPlayerView!!.showController()
                        }
                    }
                    playerState.mArcKeyListener?.onKey(keyCode, event)
                }
                false
            }
        }
        if (playerState.mFullScreenDialog != null && playerState.mLocalPlayerView != null) {
            if (!isFullScreen) {
                if (playerState.mLocalPlayerView!!.parent is ViewGroup) {
                    (playerState.mLocalPlayerView!!.parent as ViewGroup).removeView(playerState.mLocalPlayerView)
                }
                addPlayerToFullScreen()
                addOverlayToFullScreen()
                playerState.mFullScreenDialog!!.show()
                playerState.mIsFullScreen = true
                createTrackingEvent(TrackingType.ON_OPEN_FULL_SCREEN)
                trackingHelper.fullscreen()
            } else {
                if (playerState.mLocalPlayerView!!.parent is ViewGroup) {
                    (playerState.mLocalPlayerView!!.parent as ViewGroup).removeView(playerState.mLocalPlayerView)
                }
                mListener.playerFrame.addView(playerState.mLocalPlayerView)
                for (v in playerState.mFullscreenOverlays.values) {
                    (v.parent as ViewGroup).removeView(v)
                    mListener.playerFrame.addView(v)
                }
                if (mListener.isStickyPlayer) {
                    playerState.mLocalPlayerView!!.hideController()
                    playerState.mLocalPlayerView!!.requestLayout()
                }
                playerState.mIsFullScreen = false
                playerState.mFullScreenDialog!!.dismiss()
                val videoData = utils.createTrackingVideoTypeData()
                videoData.arcVideo = playerState.mVideo
                videoData.position =
                    if (playerState.mLocalPlayer != null) playerState.mLocalPlayer!!.currentPosition else 0L
                onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData)
                trackingHelper.normalScreen()
            }
        }
    }

    fun onPipEnter() {
        if (mListener.isPipEnabled) {
            if (!playerState.mIsFullScreen) {
                toggleFullScreenDialog(false)
            } else {
                playerState.wasInFullScreenBeforePip = true
            }
            playerState.mLocalPlayerView?.hideController()
            mListener.setSavedPosition(
                playerState.mVideoId,
                playerState.mLocalPlayer?.currentPosition ?: 0L
            )
            mListener.startPIP(playerState.mVideo)
        } else {
            openPIPSettings()
        }
    }

    fun onPipExit() {
        if (playerState.mLocalPlayerView != null) {
            if (!playerState.config.isDisableControls) {
                playerState.mLocalPlayerView!!.useController = true
            }
        }
        if (playerState.wasInFullScreenBeforePip) {
            playerState.wasInFullScreenBeforePip = false
        } else {
            toggleFullScreenDialog(true)
        }
    }

    private fun openPIPSettings() {
        try {
            playerState.config.activity?.let { activity ->
                utils.createAlertDialogBuilder(activity)
                    .setTitle("Picture-in-Picture functionality is disabled")//TODO remove hardcoded display strings
                    .setMessage("Would you like to enable Picture-in-Picture?")
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        val intent = utils.createIntent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            Objects.requireNonNull<Activity>(playerState.config.activity).packageName,
                            null
                        )
                        intent.data = uri
                        activity.startActivity(intent)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show()
            } ?: mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, "Activity Not Set", null)
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, playerState.mVideo)
        }
    }

    fun haveMoreVideosToPlay(): Boolean {
        return playerState.mVideos != null && playerState.currentVideoIndex < playerState.mVideos!!.size - 1
    }

    private fun playingListOfVideos(): Boolean {
        return playerState.mVideos != null && playerState.mVideos!!.size > 1
    }

    fun createTrackingEvent(trackingType: TrackingType) {
        val videoData = utils.createTrackingVideoTypeData()
        videoData.arcVideo = playerState.mVideo
        videoData.position =
            if (playerState.mLocalPlayer != null) playerState.mLocalPlayer!!.currentPosition else 0L
        mListener.onTrackingEvent(trackingType, videoData)
    }

    fun addOverlayToFullScreen() {
        if (playerState.mFullScreenDialog != null) {
            val matchParentLayoutParams = utils.createLayoutParams()
            for (v in playerState.mFullscreenOverlays.values) {
                (v.parent as ViewGroup).removeView(v)//TODO is this cast safe? should check old codebase
                playerState.mFullScreenDialog!!.addContentView(v, matchParentLayoutParams)
                v.bringToFront()
            }
        }
    }

    fun isMinimalModeNow() =
        playerState.mLocalPlayerView?.let {
            Utils.isMinimalMode(
                it.findViewById(R.id.exo_controller),
                it.findViewById(R.id.exo_center_controls),
                it.findViewById(R.id.exo_time),
                it.findViewById(R.id.exo_overflow_show),
                it.findViewById(R.id.exo_bottom_bar)
            )
        } ?: false


    private fun shouldShowSeekButtons() =
        playerState.config.isShowSeekButton && !playerState.mIsLive
}