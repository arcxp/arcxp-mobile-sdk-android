package com.arcxp.video.players

import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.ArcVideo

interface PlayerContract {
    fun getId(): String?
    fun getAdType(): Long
    fun showControls(show: Boolean)
    fun isControlsVisible(): Boolean
    fun getCurrentVideoDuration(): Long
    fun isClosedCaptionVisibleAndOn(): Boolean
    fun isClosedCaptionAvailable(): Boolean
    fun enableClosedCaption(enable: Boolean): Boolean
    fun setCcButtonDrawable(@DrawableRes ccButtonDrawable: Int): Boolean
    fun isFullScreen(): Boolean
    fun isControllerFullyVisible(): Boolean
    fun setFullscreen(full: Boolean)
    fun setFullscreenUi(full: Boolean)
    fun setFullscreenListener(listener: ArcKeyListener)
    fun setPlayerKeyListener(listener: ArcKeyListener)
    fun onPipExit()
    fun onPipEnter()
    fun release()
    fun playVideo(video: ArcVideo)
    fun     playVideos(videos: MutableList<ArcVideo>)
    fun addVideo(video: ArcVideo)
    fun isPlaying(): Boolean
    fun isCasting(): Boolean
    fun getPlayWhenReadyState(): Boolean
    fun onStickyPlayerStateChanged(isSticky: Boolean)
    fun pausePlay(shouldPlay: Boolean)
    fun toggleCaptions()
    fun getVideo(): ArcVideo?
    fun onActivityResume()
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun seekTo(ms: Int)
    fun setVolume(volume: Float)
    fun getPlaybackState(): Int
    fun getCurrentPosition(): Long
    fun getCurrentTimelinePosition(): Long
    fun onKeyEvent(event: KeyEvent): Boolean
    fun getOverlay(tag: String): View

    fun getVideoPlayer(): VideoPlayer
}