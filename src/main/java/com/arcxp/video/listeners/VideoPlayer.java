package com.arcxp.video.listeners;

import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcxp.video.model.ArcVideo;
import androidx.media3.ui.PlayerView;

import java.util.List;

/**
 * VideoPlayer is an interface that defines the contract for video playback functionality within the ArcXP platform.
 * It includes methods for controlling video playback, managing video states, handling fullscreen mode, and interacting with video overlays.
 *
 * The interface defines the following methods:
 * - release: Frees any resources before the object is destroyed.
 * - getId: Returns the unique identifier of the video player.
 * - onStickyPlayerStateChanged: Handles changes in the sticky player state.
 * - getVideo: Retrieves the current video being played.
 * - onActivityResume: Handles actions when the activity resumes.
 * - playVideo: Plays a single video.
 * - playVideos: Plays a list of videos.
 * - addVideo: Adds a video to the playlist.
 * - pausePlay: Pauses or resumes playback based on the provided flag.
 * - start: Starts video playback.
 * - pause: Pauses video playback.
 * - resume: Resumes video playback.
 * - stop: Stops video playback.
 * - seekTo: Seeks to a specific position in the video.
 * - setVolume: Sets the volume for video playback.
 * - setFullscreen: Sets the fullscreen mode.
 * - setFullscreenUi: Sets the UI for fullscreen mode.
 * - setFullscreenListener: Sets the listener for fullscreen events.
 * - setPlayerKeyListener: Sets the listener for key events.
 * - getPlayWhenReadyState: Returns whether the player is ready to play.
 * - onKeyEvent: Handles key events.
 * - getCurrentPosition: Returns the current playback position.
 * - getCurrentTimelinePosition: Returns the current timeline position.
 * - getCurrentVideoDuration: Returns the duration of the current video.
 * - toggleCaptions: Toggles the captions on or off.
 * - isPlaying: Returns whether the video is currently playing.
 * - isFullScreen: Returns whether the player is in fullscreen mode.
 * - getPlayControls: Returns the player controls view.
 * - showControls: Shows or hides the player controls.
 * - getAdType: Returns the type of advertisement being played.
 * - getPlaybackState: Returns the current playback state.
 * - isVideoCaptionEnabled: Returns whether video captions are enabled.
 * - isClosedCaptionTurnedOn: Returns whether closed captions are turned on.
 * - isClosedCaptionAvailable: Returns whether closed captions are available.
 * - enableClosedCaption: Enables or disables closed captions.
 * - setCcButtonDrawable: Sets the drawable for the closed caption button.
 * - getOverlay: Returns the overlay view for the given tag.
 * - removeOverlay: Removes the overlay view for the given tag.
 * - isMinimalControlsNow: Returns whether minimal controls are currently shown.
 *
 * Usage:
 * - Implement this interface to provide custom video playback functionality.
 * - Use the provided methods to control and manage video playback.
 *
 * Example:
 *
 * public class CustomVideoPlayer implements VideoPlayer {
 *     // Implement all methods defined in the VideoPlayer interface
 * }
 *
 * Note: Ensure that all required methods are properly implemented to avoid runtime errors.
 *
 * @method release Frees any resources before the object is destroyed.
 * @method getId Returns the unique identifier of the video player.
 * @method onStickyPlayerStateChanged Handles changes in the sticky player state.
 * @method getVideo Retrieves the current video being played.
 * @method onActivityResume Handles actions when the activity resumes.
 * @method playVideo Plays a single video.
 * @method playVideos Plays a list of videos.
 * @method addVideo Adds a video to the playlist.
 * @method pausePlay Pauses or resumes playback based on the provided flag.
 * @method start Starts video playback.
 * @method pause Pauses video playback.
 * @method resume Resumes video playback.
 * @method stop Stops video playback.
 * @method seekTo Seeks to a specific position in the video.
 * @method setVolume Sets the volume for video playback.
 * @method setFullscreen Sets the fullscreen mode.
 * @method setFullscreenUi Sets the UI for fullscreen mode.
 * @method setFullscreenListener Sets the listener for fullscreen events.
 * @method setPlayerKeyListener Sets the listener for key events.
 * @method getPlayWhenReadyState Returns whether the player is ready to play.
 * @method onKeyEvent Handles key events.
 * @method getCurrentPosition Returns the current playback position.
 * @method getCurrentTimelinePosition Returns the current timeline position.
 * @method getCurrentVideoDuration Returns the duration of the current video.
 * @method toggleCaptions Toggles the captions on or off.
 * @method isPlaying Returns whether the video is currently playing.
 * @method isFullScreen Returns whether the player is in fullscreen mode.
 * @method getPlayControls Returns the player controls view.
 * @method showControls Shows or hides the player controls.
 * @method getAdType Returns the type of advertisement being played.
 * @method getPlaybackState Returns the current playback state.
 * @method isVideoCaptionEnabled Returns whether video captions are enabled.
 * @method isClosedCaptionTurnedOn Returns whether closed captions are turned on.
 * @method isClosedCaptionAvailable Returns whether closed captions are available.
 * @method enableClosedCaption Enables or disables closed captions.
 * @method setCcButtonDrawable Sets the drawable for the closed caption button.
 * @method getOverlay Returns the overlay view for the given tag.
 * @method removeOverlay Removes the overlay view for the given tag.
 * @method isMinimalControlsNow Returns whether minimal controls are currently shown.
 */
public interface VideoPlayer {
    /**
     * This method is responsible for freeing any resources before the object is destroyed,
     * it should stop any video that is playing in addition to removing any created views.
     */
    void release();

    @Nullable
    String getId();

    void onStickyPlayerStateChanged(boolean isSticky);

    @Nullable
    ArcVideo getVideo();

    void onActivityResume();

    void playVideo(@NonNull final ArcVideo video);
    void playVideos(@NonNull final List<ArcVideo> videos);
    void addVideo(@NonNull final ArcVideo video);

    void pausePlay(boolean shouldPlay);

    void start();
    void pause();
    void resume();
    void stop();
    void seekTo(int ms);
    void setVolume(float volume);
    void setFullscreen(boolean full);
    void setFullscreenUi(boolean full);
    void setFullscreenListener(ArcKeyListener listener);
    void setPlayerKeyListener(ArcKeyListener listener);
    boolean getPlayWhenReadyState();

    boolean onKeyEvent(KeyEvent event);

    long getCurrentPosition();

    long getCurrentTimelinePosition();

    long getCurrentVideoDuration();

    void toggleCaptions();

    boolean isPlaying();

    boolean isFullScreen();

    PlayerView getPlayControls();

    void showControls(boolean show);

    long getAdType();

    int getPlaybackState();

    boolean isVideoCaptionEnabled();

    boolean isClosedCaptionTurnedOn();

    boolean isClosedCaptionAvailable();

    boolean enableClosedCaption(boolean enable);

    boolean setCcButtonDrawable(@DrawableRes int ccButtonDrawable);

    View getOverlay(String tag);

    void removeOverlay(String tag);

    Boolean isMinimalControlsNow();
}