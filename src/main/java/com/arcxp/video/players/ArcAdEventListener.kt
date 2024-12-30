package com.arcxp.video.players

import androidx.media3.common.util.UnstableApi
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData.TrackingAdTypeData
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType

/**
 * ArcAdEventListener is a class that implements the AdEvent.AdEventListener interface to handle ad-related events within the ArcXP platform.
 * It manages the state of the video player during ad playback, triggers tracking events, and controls the video player based on ad events.
 *
 * The class defines the following properties:
 * - playerState: An instance of PlayerState that holds the current state of the video player.
 * - playerStateHelper: An instance of PlayerStateHelper for managing player state transitions and events.
 * - videoPlayer: An instance of ArcVideoPlayer for controlling video playback.
 * - mConfig: An instance of ArcXPVideoConfig containing configuration information for the video player.
 * - captionsManager: An instance of CaptionsManager for managing video captions.
 *
 * Usage:
 * - Create an instance of ArcAdEventListener with the necessary parameters.
 * - Use the onAdEvent method to handle various ad events and manage the video player state accordingly.
 *
 * Example:
 *
 * val adEventListener = ArcAdEventListener(playerState, playerStateHelper, videoPlayer, config, captionsManager)
 * adLoader.addAdEventListener(adEventListener)
 *
 * Note: Ensure that the Google IMA SDK is properly configured before using ArcAdEventListener.
 *
 * @property playerState An instance of PlayerState that holds the current state of the video player.
 * @property playerStateHelper An instance of PlayerStateHelper for managing player state transitions and events.
 * @property videoPlayer An instance of ArcVideoPlayer for controlling video playback.
 * @property mConfig An instance of ArcXPVideoConfig containing configuration information for the video player.
 * @property captionsManager An instance of CaptionsManager for managing video captions.
 * @method onAdEvent Handles various ad events and manages the video player state accordingly.
 * @method adEnded Resets the video player state after an ad has ended.
 */
@UnstableApi
internal class ArcAdEventListener(
    private val playerState: PlayerState,
    private val playerStateHelper: PlayerStateHelper,
    private val videoPlayer: ArcVideoPlayer,
    private val mConfig: ArcXPVideoConfig,
    private val captionsManager: CaptionsManager,
) : AdEvent.AdEventListener {

    override fun onAdEvent(adEvent: AdEvent) {
        val ad = ArcAd()
        if (adEvent.ad != null) {
            ad.adId = adEvent.ad.adId
            ad.adDuration = adEvent.ad.duration
            ad.adTitle = adEvent.ad.title
            ad.clickthroughUrl = adEvent.ad.surveyUrl
        }
        val value = TrackingAdTypeData()
        value.position = videoPlayer.currentTimelinePosition
        value.arcAd = ad
        when (adEvent.type) {
            AdEventType.AD_BREAK_READY -> videoPlayer.disableControls()
            AdEventType.COMPLETED -> playerStateHelper.onVideoEvent(
                TrackingType.AD_PLAY_COMPLETED,
                value
            )

            AdEventType.AD_BREAK_ENDED -> {
                playerState.firstAdCompleted = true
                playerStateHelper.onVideoEvent(TrackingType.AD_BREAK_ENDED, value)
            }

            AdEventType.CONTENT_RESUME_REQUESTED -> {
                captionsManager.initVideoCaptions()
            }

            AdEventType.ALL_ADS_COMPLETED -> {
                playerState.firstAdCompleted = true
                adEnded()
                playerStateHelper.onVideoEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, value)
            }

            AdEventType.FIRST_QUARTILE -> playerStateHelper.onVideoEvent(
                TrackingType.VIDEO_25_WATCHED,
                value
            )

            AdEventType.MIDPOINT -> playerStateHelper.onVideoEvent(
                TrackingType.VIDEO_50_WATCHED,
                value
            )

            AdEventType.PAUSED -> if (playerState.adPlaying && !playerState.adPaused) {
                playerState.currentPlayer?.pause()
                playerState.adPaused = true
                playerStateHelper.onVideoEvent(TrackingType.AD_PAUSE, value)
            } else {
                playerState.currentPlayer?.play()
                playerState.adPaused = false
                playerStateHelper.onVideoEvent(TrackingType.AD_RESUME, value)
            }

            AdEventType.THIRD_QUARTILE -> playerStateHelper.onVideoEvent(
                TrackingType.VIDEO_75_WATCHED,
                value
            )

            AdEventType.LOADED -> {
                videoPlayer.disableControls()
                playerStateHelper.onVideoEvent(TrackingType.AD_LOADED, value)
            }

            AdEventType.AD_BREAK_STARTED -> {
                videoPlayer.disableControls()
                playerStateHelper.onVideoEvent(TrackingType.AD_BREAK_STARTED, value)
            }

            AdEventType.SKIPPABLE_STATE_CHANGED -> playerStateHelper.onVideoEvent(
                TrackingType.AD_SKIP_SHOWN,
                value
            )

            AdEventType.SKIPPED -> {
                playerState.firstAdCompleted = true
                adEnded()
                playerStateHelper.onVideoEvent(TrackingType.AD_SKIPPED, value)
            }

            AdEventType.STARTED -> playerStateHelper.onVideoEvent(
                TrackingType.AD_PLAY_STARTED,
                value
            )

            AdEventType.CLICKED, AdEventType.TAPPED -> playerStateHelper.onVideoEvent(
                TrackingType.AD_CLICKED,
                value
            )

//            AdEventType.CUEPOINTS_CHANGED, AdEventType.LOG, AdEventType.ICON_TAPPED, AdEventType.AD_PROGRESS, AdEventType.AD_BUFFERING, AdEventType.RESUMED -> {}
            else -> {}
        }
    }

    private fun adEnded() {
        playerState.disabledControlsForAd = false
        playerState.adPlaying = false
        if (playerState.mLocalPlayerView != null) {
            if (!mConfig.isDisableControls) {
                playerState.mLocalPlayerView?.useController = true
            }
        }
    }
}