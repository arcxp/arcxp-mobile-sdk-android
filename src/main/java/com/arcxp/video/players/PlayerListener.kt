package com.arcxp.video.players

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.arcxp.commons.util.Utils.createTimeStamp
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.VideoTracker.Companion.getInstance
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData.TrackingErrorTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingSourceTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingVideoTypeData
import com.arcxp.video.util.TAG
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.PositionInfo
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import java.util.Objects

internal class PlayerListener(
    private val trackingHelper: TrackingHelper,
    private val playerState: PlayerState,
    private val playerStateHelper: PlayerStateHelper,
    private val mListener: VideoListener,
    private val captionsManager: CaptionsManager,
    private val mConfig: ArcXPVideoConfig,
    private val arcCastManager: ArcCastManager?,
    private val utils: Utils,
    private val adEventListener: AdEventListener,
    private val videoPlayer: ArcVideoPlayer,
) : Player.Listener {

    override fun onVolumeChanged(volume: Float) {
        trackingHelper.volumeChange(volume)
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        mListener.setIsLoading(isLoading)
    }

    private fun showCaptions(): Boolean {
        return mConfig.isShowClosedCaptionTrackSelection && captionsManager.isClosedCaptionAvailable()
    }


    override fun onTimelineChanged(timeline: Timeline, reason: Int) = setCaptionVisibility()

    private fun setCaptionVisibility() {
        playerState.ccButton?.visibility =
            if (showCaptions()) {
                VISIBLE
            } else if (mConfig.isKeepControlsSpaceOnHide) INVISIBLE else GONE
    }

    override fun onTracksChanged(tracks: Tracks) {
        var language = "none"
        for (group in tracks.groups) {
            for (index in 0 until group.length) {
                val f = group.getTrackFormat(index)
                if (f.id?.startsWith("CC:") == true) {
                    language = f.id!!.substring(f.id!!.lastIndexOf("CC:") + 3)
                }
            }
        }
        val source = TrackingSourceTypeData()
        source.source = language
        playerStateHelper.onVideoEvent(TrackingType.SUBTITLE_SELECTION, source)
    }

    fun isCasting() = playerState.currentPlayer is CastPlayer


    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playerStateCode: Int) {
        if (playerStateCode == Player.STATE_IDLE && isCasting()) {
            playerState.mVideo?.let { arcCastManager?.reloadVideo(it) }
        } else {
            try {
                if (playerState.mLocalPlayer == null || playerState.mVideoTracker == null || playerState.mLocalPlayerView == null) {
                    return
                }
                setCaptionVisibility()
                val videoData = utils.createTrackingVideoTypeData()
                videoData.position = playerState.mLocalPlayer!!.currentPosition
                if (playerStateCode == Player.STATE_BUFFERING) {
                    mListener.setIsLoading(true)
                } else {
                    if (playWhenReady && playerStateCode == Player.STATE_READY) {
                        //Player state code must be ready
                        playerState.mLocalPlayerView!!.keepScreenOn = true
                        if (playerState.videoTrackingSub == null) {
                            subscribe()
                        } else if (playerState.videoTrackingSub!!.isUnsubscribed) {
                            subscribe()
                        }
                        if (playerState.mIsLive || playerState.mLocalPlayer!!.currentPosition > 50) {
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_RESUMED, videoData)
                            trackingHelper.resumePlay()
                        }

                        if (!playerState.mLocalPlayer!!.isPlayingAd) {
                            captionsManager.initVideoCaptions()
                        }
                    } else if (playerState.mVideoId != null) {
                        playerState.mLocalPlayerView!!.keepScreenOn = false
                        if (mListener.isInPIP) {
                            mListener.pausePIP()
                        }
                        if (playerStateCode == Player.STATE_ENDED) {
                            videoData.percentage = 100
                            videoData.arcVideo = playerState.mVideo
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
                            if (playerState.videoTrackingSub != null) {
                                playerState.videoTrackingSub!!.unsubscribe()
                                playerState.videoTrackingSub = null
                            }
                            playerState.mVideoTracker!!.reset()
                            mListener.setNoPosition(playerState.mVideoId)
                            if (playerStateHelper.haveMoreVideosToPlay()) {
                                playVideoAtIndex(playerState.incrementVideoIndex(true))
                            }
                            trackingHelper.onPlaybackEnd()
                        } else if (playerStateCode == Player.STATE_READY) {
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_PAUSED, videoData)
                            trackingHelper.pausePlay()
                        }
                        if (playerState.videoTrackingSub != null) {
                            playerState.videoTrackingSub!!.unsubscribe()
                            playerState.videoTrackingSub = null
                        }
                    }
                    mListener.setIsLoading(false)
                }
            } catch (e: Exception) {
                if (mConfig.isLoggingEnabled) {
                    Log.e("ArcMobileSDK", "Exoplayer Exception - " + e.message, e)
                }
            }
        }
    }

    private fun subscribe() {
        playerState.videoTrackingSub =
            playerState.mVideoTracker!!.getObs().subscribe()
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {}

    override fun onIsPlayingChanged(isPlaying: Boolean) {}

    override fun onRepeatModeChanged(repeatMode: Int) {}

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

    override fun onPlayerError(e: PlaybackException) {
        if (e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
            if (playerState.mLocalPlayerView != null) {
                for (v in playerState.mFullscreenOverlays.values) {
                    playerState.mLocalPlayerView!!.removeView(v)
                }
            }
            playerState.mLocalPlayer?.seekToDefaultPosition()
            playerState.mLocalPlayer?.prepare()

            playerStateHelper.onVideoEvent(
                TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT,
                TrackingErrorTypeData(playerState.mVideo, mListener.sessionId, null)
            )
            return
        }
        if (e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
            mListener.onError(
                ArcVideoSDKErrorType.SOURCE_ERROR,
                Objects.requireNonNull<Activity>(mConfig.activity).getString(R.string.source_error),
                e.cause
            )
            if (e.cause is FileDataSourceException) {
                // no url passed from backend
                mListener.logError(
                    """
                Exoplayer Source Error: No url passed from backend. Caused by:
                ${e.cause}
                """.trimIndent()
                )
            }
        } else {
            mListener.onError(
                ArcVideoSDKErrorType.EXOPLAYER_ERROR,
                Objects.requireNonNull<Activity>(mConfig.activity)
                    .getString(R.string.unknown_error),
                e
            )
        }
        if (mConfig.isLoggingEnabled) {
            Log.e(TAG, "ExoPlayer Error", e)
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: PositionInfo,
        newPosition: PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
            if (playerState.currentPlayer != null) {
                val latestWindowIndex: Int = playerState.currentPlayer!!.getCurrentWindowIndex()
                try { //TODO this block seems to get trigger a lot, but seems to require a playlist to work/test
                    val videoData: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                    videoData.percentage = 100
                    videoData.arcVideo = playerState.mVideos?.get(latestWindowIndex - 1)
                    playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_COMPLETED, videoData)
                    val videoData2: TrackingVideoTypeData = utils.createTrackingVideoTypeData()
                    videoData2.percentage = 0
                    videoData2.position = 0L
                    videoData2.arcVideo = playerState.mVideos?.get(latestWindowIndex)
                    playerState.mVideo = playerState.mVideos?.get(latestWindowIndex)
                    playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_STARTED, videoData2)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}

    override fun onSeekProcessed() {}


    private fun playVideoAtIndex(indexInput: Int) {
        var modifiedIndex = indexInput
        try {
            if (playerState.mVideos?.isNotEmpty() == true) {
                modifiedIndex = modifiedIndex.coerceIn(0, playerState.mVideos!!.size - 1)
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
                playerState.mVideo = playerState.mVideos!![modifiedIndex]
                if (isCasting()) {
                    videoPlayer.addToCast()
                } else {
                    videoPlayer.playOnLocal()
                }

                for (v in playerState.mFullscreenOverlays.values) {
                    if (v.parent is ViewGroup) {
                        (v.parent as ViewGroup).removeView(v)
                    }
                    playerState.mLocalPlayerView!!.addView(v)
                }
                val contentMediaSource = captionsManager.createMediaSourceWithCaptions()
                var adsMediaSource: MediaSource? = null
                if (playerState.mVideo!!.shouldPlayAds && !TextUtils.isEmpty(playerState.mVideo!!.adTagUrl)) {
                    try {
                        playerState.mAdsLoader =
                            ImaAdsLoader.Builder(mConfig.activity)
                                .setAdEventListener(adEventListener)
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
                                "\\[(?i)timestamp]".toRegex(),
                                    createTimeStamp()

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
                        ) //TODO test ads here too!!
                    } catch (e: Exception) {
                        if (mConfig.isLoggingEnabled) {
                            Log.d(
                                "ArcMobileSDK",
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
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }
}