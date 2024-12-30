package com.arcxp.video.listeners;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.ArcVideoSDKErrorType;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;

/**
 * VideoListener is an interface that defines the contract for handling video-related events and actions within the ArcXP platform.
 * It includes methods for managing video views, handling playback states, tracking events, and managing Picture-in-Picture (PIP) mode.
 *
 * The interface defines the following methods:
 * - getPlayerFrame: Returns the RelativeLayout that contains the video player.
 * - removePlayerFrame: Removes the video player frame.
 * - addVideoView: Adds a video view to the layout.
 * - addVideoFragment: Adds a video fragment to the layout.
 * - removeVideoFragment: Removes a video fragment from the layout.
 * - setIsLoading: Sets the loading state of the video player.
 * - release: Frees any resources before the object is destroyed.
 * - onError: Handles errors that occur during video playback.
 * - logError: Logs an error message.
 * - isStickyPlayer: Returns whether the player is in sticky mode.
 * - onTrackingEvent: Handles tracking events for video playback.
 * - getSavedPosition: Returns the saved playback position for a given video ID.
 * - setSavedPosition: Sets the saved playback position for a given video ID.
 * - setNoPosition: Clears the saved playback position for a given video ID.
 * - onActivityResume: Handles actions when the activity resumes.
 * - isInPIP: Returns whether the player is in Picture-in-Picture (PIP) mode.
 * - isPipEnabled: Returns whether PIP mode is enabled.
 * - pausePIP: Pauses the PIP mode.
 * - startPIP: Starts the PIP mode with the given video.
 * - onShareVideo: Handles the sharing of a video with the given headline and URL.
 * - onMute: Mutes or unmutes the video player.
 * - getAdType: Returns the type of advertisement being played.
 * - setFullscreen: Sets the fullscreen mode for the video player.
 * - getSessionId: Returns the session ID for the current video session.
 *
 * Usage:
 * - Implement this interface to handle video-related events and actions.
 * - Use the provided methods to manage video views, playback states, and tracking events.
 *
 * Example:
 *
 * public class CustomVideoListener implements VideoListener {
 *     // Implement all methods defined in the VideoListener interface
 * }
 *
 * Note: Ensure that all required methods are properly implemented to avoid runtime errors.
 *
 * @method getPlayerFrame Returns the RelativeLayout that contains the video player.
 * @method removePlayerFrame Removes the video player frame.
 * @method addVideoView Adds a video view to the layout.
 * @method addVideoFragment Adds a video fragment to the layout.
 * @method removeVideoFragment Removes a video fragment from the layout.
 * @method setIsLoading Sets the loading state of the video player.
 * @method release Frees any resources before the object is destroyed.
 * @method onError Handles errors that occur during video playback.
 * @method logError Logs an error message.
 * @method isStickyPlayer Returns whether the player is in sticky mode.
 * @method onTrackingEvent Handles tracking events for video playback.
 * @method getSavedPosition Returns the saved playback position for a given video ID.
 * @method setSavedPosition Sets the saved playback position for a given video ID.
 * @method setNoPosition Clears the saved playback position for a given video ID.
 * @method onActivityResume Handles actions when the activity resumes.
 * @method isInPIP Returns whether the player is in Picture-in-Picture (PIP) mode.
 * @method isPipEnabled Returns whether PIP mode is enabled.
 * @method pausePIP Pauses the PIP mode.
 * @method startPIP Starts the PIP mode with the given video.
 * @method onShareVideo Handles the sharing of a video with the given headline and URL.
 * @method onMute Mutes or unmutes the video player.
 * @method getAdType Returns the type of advertisement being played.
 * @method setFullscreen Sets the fullscreen mode for the video player.
 * @method getSessionId Returns the session ID for the current video session.
 */
public interface VideoListener {
    @NonNull
    RelativeLayout getPlayerFrame();

    void removePlayerFrame();

    void addVideoView(View view);

    void addVideoFragment(Fragment fragment);

    void removeVideoFragment(Fragment fragment);

    void setIsLoading(boolean isLoading);

    void release();

    void onError(ArcVideoSDKErrorType type, String message, Object value);

    void logError(String log);

    boolean isStickyPlayer();

    void onTrackingEvent(@NonNull TrackingType type, @Nullable TrackingTypeData trackingData);

    long getSavedPosition(String id);

    void setSavedPosition(String id, long value);

    void setNoPosition(String id);

    void onActivityResume();

    boolean isInPIP();

    boolean isPipEnabled();

    void pausePIP();

    void startPIP(ArcVideo mVideo);

    void onShareVideo(String headline, String url);

    void onMute(boolean isMute);

    long getAdType();

    void setFullscreen(boolean full);

    String getSessionId();
}