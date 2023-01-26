package com.arcxp.video.listeners;

import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcxp.video.listeners.ArcKeyListener;
import com.arcxp.video.model.ArcVideo;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.util.List;

/**
 * @hide
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

    StyledPlayerView getPlayControls();

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
}