package com.arcxp.video.players

import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData.TrackingAdTypeData
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType

internal class ArcAdEventListener(
    private val playerState: PlayerState,
    private val playerStateHelper: PlayerStateHelper,
    private val videoPlayer: ArcVideoPlayer,
    private val mConfig: ArcXPVideoConfig,
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