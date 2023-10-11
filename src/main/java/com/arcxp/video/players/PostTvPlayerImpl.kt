package com.arcxp.video.players

import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.Player
import java.util.Objects

/**
 * @hide
 */
internal class PostTvPlayerImpl(
    config: ArcXPVideoConfig,
    listener: VideoListener,
    helper: TrackingHelper,
    utils: Utils
) : PostTvContract {
    private val playerStateHelper: PlayerStateHelper
    @JvmField
    val videoPlayer: ArcVideoPlayer
    init {
        val arcCastManager = config.arcCastManager
        val playerState =
            utils.createPlayerState(Objects.requireNonNull(config.activity), listener, config)
        val captionsManager = utils.createCaptionsManager(playerState, config, listener)
        playerStateHelper =
            utils.createPlayerStateHelper(playerState, helper, listener, this, captionsManager)
        videoPlayer = utils.createArcVideoPlayer(
            playerState,
            playerStateHelper,
            listener,
            config,
            arcCastManager,
            helper,
            captionsManager
        )
        val arcAdEventListener =
            utils.createArcAdEventListener(playerState, playerStateHelper, config, videoPlayer)
        val playerListener: Player.Listener = utils.createPlayerListener(
            playerState,
            playerStateHelper,
            listener,
            config,
            arcCastManager,
            helper,
            captionsManager,
            arcAdEventListener,
            videoPlayer
        )
        videoPlayer.playerListener = playerListener
        videoPlayer.adEventListener = arcAdEventListener
        playerStateHelper.playerListener = playerListener
    }
    fun onPipExit() = playerStateHelper.onPipExit()
    fun onPipEnter() = playerStateHelper.onPipEnter()
    override fun playVideoAtIndex(index: Int) = videoPlayer.playVideoAtIndex(index) // called from helper
}