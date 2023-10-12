package com.arcxp.video.players

import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingVideoTypeData
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.util.Objects

internal class PlayerStateHelper(
    private val playerState: PlayerState,
    private val trackingHelper: TrackingHelper,
    private val utils: Utils,
    private val mListener: VideoListener,
    private val captionsManager: CaptionsManager,
) {
    var playerListener: Player.Listener? = null
    var playVideoAtIndex: ((Int) -> Unit)? = null
    private fun setVideoCaptionsStartupDrawable() {
        val enabled = PrefManager.getBoolean(
            Objects.requireNonNull<Activity>(playerState.config.activity),
            PrefManager.IS_CAPTIONS_ENABLED,
            false
        ) || playerState.config.ccStartMode === ArcXPVideoConfig.CCStartMode.ON
        playerState.ccButton?.setImageDrawable(
            ContextCompat.getDrawable(
                Objects.requireNonNull<Activity>(
                    playerState.config.activity
                ), if (enabled) R.drawable.CcDrawableButton else R.drawable.CcOffDrawableButton
            )
        )
    }

    fun initLocalPlayer() {
        val exoPlayer: ExoPlayer = utils.createExoPlayer()
        playerState.mLocalPlayer = exoPlayer
        playerState.mLocalPlayer!!.addListener(playerListener!!)
        val playerView: StyledPlayerView = utils.createPlayerView()
        playerState.mLocalPlayerView = playerView
        playerView.layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        playerView.resizeMode = playerState.config.videoResizeMode.mode()
        playerView.id = R.id.wapo_player_view
        playerView.player = exoPlayer
        playerView.controllerAutoShow = playerState.config.isAutoShowControls
        playerState.title = playerView.findViewById(R.id.styled_controller_title_tv)
        if (playerState.mVideo != null && playerState.mVideo!!.startMuted) {
            playerState.mCurrentVolume = exoPlayer.volume
            exoPlayer.volume = 0f
        }
        setUpPlayerControlListeners()
        setAudioAttributes()
        playerView.setOnTouchListener { v: View, event: MotionEvent ->
            v.performClick()
            if (event.action == MotionEvent.ACTION_UP) {
                trackingHelper.onTouch(event, getCurrentTimelinePosition())
            }
            false
        }
        if (!playerState.mIsFullScreen) {
            mListener.addVideoView(playerView)
        } else {
            addPlayerToFullScreen()
        }
        for (v in playerState.mFullscreenOverlays.values) {
            playerView.addView(v)
        }
        if (playerState.config.isDisableControlsFully) {
            playerView.useController = false
        } else {
            playerState.ccButton = playerView.findViewById(R.id.exo_cc)
            if (playerState.ccButton != null) {
                setVideoCaptionsStartupDrawable()
            }
        }
    }


    fun setUpPlayerControlListeners() {
        if (!playerState.config.isDisableControlsFully) {
            try {
                if (playerState.mLocalPlayer == null || playerState.mLocalPlayerView == null) {
                    return
                }
                val fullscreenButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                if (fullscreenButton != null) {
                    if (playerState.config.showFullScreenButton) {
                        fullscreenButton.setOnClickListener { v: View? ->
                            toggleFullScreenDialog(
                                playerState.mIsFullScreen
                            )
                        }
                        fullscreenButton.visibility = View.VISIBLE
                    } else {
                        fullscreenButton.visibility = View.GONE
                    }
                }
                val shareButton =
                    playerState.mLocalPlayerView?.findViewById<ImageButton>(R.id.exo_share)
                if (shareButton != null) {
                    shareButton.setOnClickListener { v: View? ->
                        val videoData: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                        videoData.arcVideo = playerState.mVideo
                        videoData.position = playerState.mLocalPlayer!!.currentPosition
                        onVideoEvent(TrackingType.ON_SHARE, videoData)
                        mListener.onShareVideo(playerState.mHeadline, playerState.mShareUrl)
                    }
                    if (TextUtils.isEmpty(playerState.mShareUrl)) {
                        if (playerState.config.isKeepControlsSpaceOnHide) {
                            shareButton.visibility = View.INVISIBLE
                        } else {
                            shareButton.visibility = View.GONE
                        }
                    } else {
                        shareButton.visibility = View.VISIBLE
                    }
                } else {
                    logNullErrorIfEnabled("shareButton", "setUpPlayerControlListeners")
                }
                val backButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_back)
                if (backButton != null) {
                    if (playerState.config.showBackButton) {
                        backButton.setOnClickListener { v: View? ->
                            val videoData: TrackingVideoTypeData =
                                utils.createTrackingVideoTypeData()
                            videoData.arcVideo = playerState.mVideo
                            videoData.position = playerState.mLocalPlayer!!.currentPosition
                            onVideoEvent(TrackingType.BACK_BUTTON_PRESSED, videoData)
                        }
                        backButton.visibility = View.VISIBLE
                    } else {
                        backButton.visibility = View.GONE
                    }
                }
                val pipButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_pip)
                if (pipButton != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !mListener.isPipEnabled()) {
                        pipButton.visibility = View.GONE
                    }
                    pipButton.setOnClickListener { v: View? -> onPipEnter() }
                } else {
                    logNullErrorIfEnabled("pipButton", "setUpPlayerControlListeners")
                }
                val volumeButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_volume)
                if (volumeButton != null) {
                    if (playerState.config.showVolumeButton) {
                        volumeButton.visibility = View.VISIBLE
                        volumeButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                Objects.requireNonNull<Activity>(
                                    playerState.config.activity
                                ),
                                if (playerState.mLocalPlayer!!.volume != 0f) R.drawable.MuteOffDrawableButton else R.drawable.MuteDrawableButton
                            )
                        )
                        volumeButton.setOnClickListener { v: View? ->
                            if (playerState.mLocalPlayer != null && playerState.mVideo != null) {
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
                        }
                    } else {
                        volumeButton.visibility = View.GONE
                    }
                } else {
                    logNullErrorIfEnabled("volumeButton", "setUpPlayerControlListeners")
                }
                playerState.ccButton = playerState.mLocalPlayerView!!.findViewById(R.id.exo_cc)
                if (playerState.ccButton != null) {
                    playerState.ccButton!!.setOnClickListener { v: View? ->
                        if (playerState.config.isShowClosedCaptionTrackSelection) {
                            captionsManager.showCaptionsSelectionDialog()
                        } else {
                            captionsManager.toggleClosedCaption()
                        }
                    }
                    if (playerState.config.enableClosedCaption() && captionsManager.isClosedCaptionAvailable()) {
                        playerState.ccButton!!.visibility = View.VISIBLE
                    } else {
                        if (playerState.config.isKeepControlsSpaceOnHide) {
                            playerState.ccButton!!.visibility = View.INVISIBLE
                        } else {
                            playerState.ccButton!!.visibility = View.GONE
                        }
                    }
                } else {
                    logNullErrorIfEnabled("ccButton", "setUpPlayerControlListeners")
                }
                val nextButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_next_button)
                val previousButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_prev_button)
                if (playerState.config.showNextPreviousButtons) {
                    //separated these out in case somebody wants a next and not previous or vice versa
                    if (nextButton != null) {
                        nextButton.visibility = View.VISIBLE
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
                        previousButton.visibility = View.VISIBLE
                        previousButton.setOnClickListener { v: View? ->
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
                                nextButton.setOnClickListener {
                                    playVideoAtIndex?.let { it(playerState.incrementVideoIndex(true)) }
                                    createTrackingEvent(TrackingType.NEXT_BUTTON_PRESSED)
                                }
                            } else {
                                nextButton.alpha = 0.5f
                            }
                        }
                        previousButton?.setOnClickListener {
                            playVideoAtIndex?.let { it(playerState.incrementVideoIndex(false)) }
                            createTrackingEvent(TrackingType.PREV_BUTTON_PRESSED)
                        }
                    }
                } else {
                    if (nextButton != null) {
                        nextButton.visibility = View.GONE
                    }
                    if (previousButton != null) {
                        previousButton.visibility = View.GONE
                    }
                }


                //seek buttons
                playerState.mLocalPlayerView!!.setShowFastForwardButton(playerState.config.isShowSeekButton && !playerState.mIsLive)
                playerState.mLocalPlayerView!!.setShowRewindButton(playerState.config.isShowSeekButton && !playerState.mIsLive)
                val exoPosition =
                    playerState.mLocalPlayerView!!.findViewById<View>(R.id.exo_position)
                val exoDuration =
                    playerState.mLocalPlayerView!!.findViewById<View>(R.id.exo_duration)
                val exoProgress =
                    playerState.mLocalPlayerView!!.findViewById<DefaultTimeBar>(R.id.exo_progress)
                val separator = playerState.mLocalPlayerView!!.findViewById<View>(R.id.separator)
                if (exoDuration != null && exoPosition != null && exoProgress != null) {
                    exoProgress.setScrubberColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarScrubberColor
                        )
                    )
                    exoProgress.setPlayedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarPlayedColor
                        )
                    )
                    exoProgress.setUnplayedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarUnplayedColor
                        )
                    )
                    exoProgress.setBufferedColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.TimeBarBufferedColor
                        )
                    )
                    exoProgress.setAdMarkerColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.AdMarkerColor
                        )
                    )
                    exoProgress.setPlayedAdMarkerColor(
                        Objects.requireNonNull<Activity>(playerState.config.activity).resources.getColor(
                            R.color.AdPlayedMarkerColor
                        )
                    )
                    val exoTimeBarLayout =
                        playerState.mLocalPlayerView!!.findViewById<LinearLayout>(R.id.time_bar_layout)
                    if (!playerState.config.isShowProgressBar && exoTimeBarLayout != null) {
                        exoTimeBarLayout.visibility = View.GONE
                    } else if (exoTimeBarLayout != null) {
                        exoTimeBarLayout.visibility = View.VISIBLE
                    }
                    if (playerState.mIsLive) {
                        exoPosition.visibility = View.GONE
                        exoDuration.visibility = View.GONE
                        exoProgress.visibility = View.GONE
                        separator.visibility = View.GONE
                    } else {
                        exoPosition.visibility = View.VISIBLE
                        exoDuration.visibility =
                            if (playerState.config.isShowCountDown) View.VISIBLE else View.GONE
                        exoProgress.visibility = View.VISIBLE
                        separator.visibility = View.VISIBLE
                    }
                } else {
                    logNullErrorIfEnabled(
                        "exo Duration, Position, or Progress",
                        "setUpPlayerControlListeners"
                    )
                }
                playerState.mLocalPlayerView!!.requestFocus() //TODO continue investigating this for fire tv// This doesn't seem to help anything, and I cannot tell this logic accomplishes anything
                if (playerState.config.controlsShowTimeoutMs != null) {
                    playerState.mLocalPlayerView!!.controllerShowTimeoutMs =
                        playerState.config.controlsShowTimeoutMs
                }
                if (playerState.config.isDisableControlsWithTouch) {
                    playerState.mLocalPlayerView!!.controllerHideOnTouch = true
                }
                if (playerState.title != null) {
                    if (playerState.config.showTitleOnController) {
                        if (playerState.mVideo != null) {
                            playerState.title!!.text = playerState.mVideo!!.headline
                            playerState.title!!.visibility = View.VISIBLE
                        }
                    } else {
                        playerState.title!!.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Exception) {
                mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
            }
        }
    }
    fun logNullErrorIfEnabled(nullMemberName: String, callingMethod: String) {
        if (playerState.config.isLoggingEnabled) {
            Log.d("ArcVideoSDK", "$nullMemberName is null, called from $callingMethod")
        }
    }

    private fun setAudioAttributes() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    fun getCurrentTimelinePosition() = if (playerState.mLocalPlayer != null) {
        try {
            playerState.mLocalPlayer!!.currentPosition - playerState.mLocalPlayer!!.currentTimeline.getPeriod(
                playerState.mLocalPlayer!!.currentPeriodIndex, playerState.period
            ).positionInWindowMs
        } catch (e: java.lang.Exception) {
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
                utils.createFullScreenDialog(Objects.requireNonNull<Activity>(playerState.config.activity))
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
                } //TODO improve this function and test
                //we do this so we don't get trigger for down and up
                if (event.action != KeyEvent.ACTION_UP) {
                    return@setOnKeyListener true
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mListener.isPipEnabled) {
                        onPipEnter()
                    } else {
                        if (playerState.mArcKeyListener != null) {
                            playerState.mArcKeyListener!!.onBackPressed()
                        }
                    }
                } else {
                    if (playerState.firstAdCompleted || !playerState.config.isEnableAds) {
                        if (playerState.mLocalPlayerView != null && !playerState.mLocalPlayerView!!.isControllerFullyVisible) {
                            playerState.mLocalPlayerView!!.showController()
                        }
                    }
                    if (playerState.mArcKeyListener != null) {
                        playerState.mArcKeyListener!!.onKey(keyCode, event)
                    }
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
                val fullScreenButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                if (fullScreenButton != null) {
                    fullScreenButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            Objects.requireNonNull<Activity>(
                                playerState.config.activity
                            ), R.drawable.FullScreenDrawableButtonCollapse
                        )
                    )
                } else {
                    logNullErrorIfEnabled("fullScreenButton", "toggleFullScreenDialog")
                }
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
                val fullScreenButton =
                    playerState.mLocalPlayerView!!.findViewById<ImageButton>(R.id.exo_fullscreen)
                fullScreenButton?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Objects.requireNonNull<Activity>(
                            playerState.config.activity
                        ), R.drawable.FullScreenDrawableButton
                    )
                )
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
            if (playerState.mLocalPlayerView != null) {
                playerState.mLocalPlayerView!!.hideController()
            } else {
                logNullErrorIfEnabled("playerState.getMLocalPlayerView()", "onPipEnter")
            }
            mListener.setSavedPosition(
                playerState.mVideoId,
                if (playerState.mLocalPlayer != null) playerState.mLocalPlayer!!.currentPosition else 0L
            )
            mListener.startPIP(playerState.mVideo)
        } else {
            openPIPSettings()
        }
    }

    fun onPipExit() {
        if (playerState.mLocalPlayerView != null) {
            if (!playerState.config.isDisableControlsFully) {
                playerState.mLocalPlayerView!!.useController = true
            }
        }
        if (playerState.wasInFullScreenBeforePip) {
            playerState.wasInFullScreenBeforePip = false
        } else {
            toggleFullScreenDialog(true)
        }
    }

    private fun openPIPSettings() =
        playerState.config.activity?.let { activity ->
            utils.createAlertDialogBuilder(activity)
                .setTitle("Picture-in-Picture functionality is disabled")
                .setMessage("Would you like to enable Picture-in-Picture?")
                .setPositiveButton(
                    android.R.string.yes
                ) { dialog, which ->
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


    fun getTextRendererIndex(mappedTrackInfo: MappingTrackSelector.MappedTrackInfo): Int {
        try {
            val count = mappedTrackInfo.rendererCount
            for (i in 0 until count) {
                if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                    return i
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return -1
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
            for (v in playerState.mFullscreenOverlays.values) {
                (v.parent as ViewGroup).removeView(v)
                playerState.mFullScreenDialog!!.addContentView(v, utils.createLayoutParams())
                v.bringToFront()
            }
        }
    }




}