package com.arcxp.video.players

import android.annotation.SuppressLint
import android.view.KeyEvent
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.ArcVideo
import com.google.ads.interactivemedia.v3.api.AdEvent

/**
 * @hide
 */
@SuppressLint("UnsafeOptInUsageError")
internal class PostTvPlayerImpl(
    private val playerStateHelper: PlayerStateHelper,
    private val videoPlayer: ArcVideoPlayer,
    adEventListener: AdEvent.AdEventListener,
    playerListener: PlayerListener,
) : PlayerContract {
    init {
        playerStateHelper.playVideoAtIndex = playerListener::playVideoAtIndex
        videoPlayer.playerListener = playerListener
        videoPlayer.adEventListener = adEventListener
        playerStateHelper.playerListener = playerListener
    }

    override fun getId() = videoPlayer.id
    override fun getAdType() = videoPlayer.adType
    override fun showControls(show: Boolean) = videoPlayer.showControls(show = show)
    override fun toggleAutoShow(show: Boolean) = videoPlayer.toggleAutoShow(show = show)
    override fun isControlsVisible() = videoPlayer.playControls?.isControllerFullyVisible == true
    override fun getCurrentVideoDuration() = videoPlayer.currentVideoDuration
    override fun isClosedCaptionVisibleAndOn() = videoPlayer.isVideoCaptionEnabled
    override fun isClosedCaptionAvailable() = videoPlayer.isClosedCaptionAvailable
    override fun enableClosedCaption(enable: Boolean) =
        videoPlayer.enableClosedCaption(enable = enable)
    override fun setCcButtonDrawable(ccButtonDrawable: Int) =
        videoPlayer.setCcButtonDrawable(ccButtonDrawable)
    override fun isFullScreen() = videoPlayer.isFullScreen
    override fun isControllerFullyVisible() =
        videoPlayer.playControls?.isControllerFullyVisible == true
    override fun setFullscreen(full: Boolean) = videoPlayer.setFullscreen(full = full)
    override fun setFullscreenUi(full: Boolean) = videoPlayer.setFullscreenUi(full = full)
    override fun setFullscreenListener(listener: ArcKeyListener) =
        videoPlayer.setFullscreenListener(listener = listener)
    override fun setPlayerKeyListener(listener: ArcKeyListener) =
        videoPlayer.setPlayerKeyListener(listener = listener)
    override fun onPipExit() = playerStateHelper.onPipExit()
    override fun onPipEnter() = playerStateHelper.onPipEnter()
    override fun release() = videoPlayer.release()
    override fun playVideo(video: ArcVideo) = videoPlayer.playVideo(video = video)
    override fun playVideos(videos: MutableList<ArcVideo>) = videoPlayer.playVideos(videos = videos)
    override fun addVideo(video: ArcVideo) = videoPlayer.addVideo(video = video)
    override fun isPlaying() = videoPlayer.isPlaying
    override fun isCasting() = videoPlayer.isCasting()
    override fun getPlayWhenReadyState() = videoPlayer.playWhenReadyState
    override fun onStickyPlayerStateChanged(isSticky: Boolean) =
        videoPlayer.onStickyPlayerStateChanged(isSticky = isSticky)
    override fun pausePlay(shouldPlay: Boolean) = videoPlayer.pausePlay(shouldPlay = shouldPlay)
    override fun toggleCaptions() = videoPlayer.toggleCaptions()
    override fun getVideo() = videoPlayer.video
    override fun onActivityResume() = videoPlayer.onActivityResume()
    override fun start() = videoPlayer.start()
    override fun stop() = videoPlayer.stop()
    override fun pause() = videoPlayer.pause()
    override fun resume() = videoPlayer.resume()
    override fun seekTo(ms: Int) = videoPlayer.seekTo(ms = ms)
    override fun setVolume(volume: Float) = videoPlayer.setVolume(volume = volume)
    override fun getPlaybackState() = videoPlayer.playbackState
    override fun getCurrentPosition() = videoPlayer.currentPosition
    override fun getCurrentTimelinePosition() = videoPlayer.currentTimelinePosition
    override fun onKeyEvent(event: KeyEvent) = videoPlayer.onKeyEvent(event = event)

    override fun getOverlay(tag: String) = videoPlayer.getOverlay(tag = tag)
    override fun getVideoPlayer(): VideoPlayer = videoPlayer


}