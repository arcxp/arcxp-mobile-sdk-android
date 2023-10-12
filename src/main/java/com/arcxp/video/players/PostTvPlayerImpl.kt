package com.arcxp.video.players

import com.google.ads.interactivemedia.v3.api.AdEvent

/**
 * @hide
 */
internal class PostTvPlayerImpl(
    private val playerStateHelper: PlayerStateHelper,
    @JvmField val videoPlayer: ArcVideoPlayer,
    adEventListener: AdEvent.AdEventListener,
    playerListener: PlayerListener,
) {
    init {
        playerStateHelper.playVideoAtIndex = videoPlayer::playVideoAtIndex
        videoPlayer.playerListener = playerListener
        videoPlayer.adEventListener = adEventListener
        playerStateHelper.playerListener = playerListener
    }
    fun onPipExit() = playerStateHelper.onPipExit()
    fun onPipEnter() = playerStateHelper.onPipEnter()
}