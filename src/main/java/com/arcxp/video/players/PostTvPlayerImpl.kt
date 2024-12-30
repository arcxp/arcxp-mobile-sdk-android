package com.arcxp.video.players

import android.annotation.SuppressLint
import android.view.KeyEvent
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.ArcVideo
import com.google.ads.interactivemedia.v3.api.AdEvent

/**
 * PostTvPlayerImpl is an internal class that implements the PlayerContract interface to provide video playback functionality within the ArcXP platform.
 * It integrates with various components such as PlayerStateHelper, ArcVideoPlayer, AdEventListener, and PlayerListener to manage video playback, ad events, and player state.
 *
 * The class defines the following properties:
 * - playerStateHelper: An instance of PlayerStateHelper for managing player state transitions and events.
 * - videoPlayer: An instance of ArcVideoPlayer for controlling video playback.
 *
 * Usage:
 * - Create an instance of PostTvPlayerImpl with the necessary parameters.
 * - Use the provided methods to control and manage video playback.
 *
 * Example:
 *
 * val postTvPlayer = PostTvPlayerImpl(playerStateHelper, videoPlayer, adEventListener, playerListener)
 * postTvPlayer.playVideo(video)
 *
 * Note: Ensure that all required properties are properly set before using the PostTvPlayerImpl instance.
 *
 * @property playerStateHelper An instance of PlayerStateHelper for managing player state transitions and events.
 * @property videoPlayer An instance of ArcVideoPlayer for controlling video playback.
 * @method getId Returns the unique identifier of the video player.
 * @method getAdType Returns the type of advertisement being played.
 * @method showControls Shows or hides the player controls.
 * @method toggleAutoShow Toggles the auto-show feature of the player controls.
 * @method isControlsVisible Returns whether the player controls are visible.
 * @method getCurrentVideoDuration Returns the duration of the current video.
 * @method isClosedCaptionVisibleAndOn Returns whether closed captions are visible and enabled.
 * @method isClosedCaptionAvailable Returns whether closed captions are available.
 * @method enableClosedCaption Enables or disables closed captions.
 * @method setCcButtonDrawable Sets the drawable for the closed caption button.
 * @method isFullScreen Returns whether the player is in fullscreen mode.
 * @method isControllerFullyVisible Returns whether the player controls are fully visible.
 * @method setFullscreen Sets the fullscreen mode.
 * @method setFullscreenUi Sets the UI for fullscreen mode.
 * @method setFullscreenListener Sets the listener for fullscreen events.
 * @method setPlayerKeyListener Sets the listener for key events.
 * @method onPipExit Exits Picture-in-Picture mode.
 * @method onPipEnter Enters Picture-in-Picture mode.
 * @method release Frees any resources before the object is destroyed.
 * @method playVideo Plays a single video.
 * @method playVideos Plays a list of videos.
 * @method addVideo Adds a video to the playlist.
 * @method isPlaying Returns whether the video is currently playing.
 * @method isCasting Returns whether the video is being cast.
 * @method getPlayWhenReadyState Returns whether the player is ready to play.
 * @method onStickyPlayerStateChanged Handles changes in the sticky player state.
 * @method pausePlay Pauses or resumes playback based on the provided flag.
 * @method toggleCaptions Toggles the captions on or off.
 * @method getVideo Retrieves the current video being played.
 * @method onActivityResume Handles actions when the activity resumes.
 * @method start Starts video playback.
 * @method stop Stops video playback.
 * @method pause Pauses video playback.
 * @method resume Resumes video playback.
 * @method seekTo Seeks to a specific position in the video.
 * @method setVolume Sets the volume for video playback.
 * @method getPlaybackState Returns the current playback state.
 * @method getCurrentPosition Returns the current playback position.
 * @method getCurrentTimelinePosition Returns the current timeline position.
 * @method onKeyEvent Handles key events.
 * @method getOverlay Returns the overlay view for the given tag.
 * @method getVideoPlayer Returns the video player instance.
 * @method isMinimalControlsNow Returns whether minimal controls are currently shown.
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

    override fun isMinimalControlsNow() = videoPlayer.isMinimalControlsNow()
}