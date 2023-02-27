package com.arcxp.video.listeners;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arcxp.commons.throwables.ArcXPSDKErrorType;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;

/**
 * @hide
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

    void onError(ArcXPSDKErrorType type, String message, Object value);

    void logError(String log);

    boolean isStickyPlayer();

    void onTrackingEvent(@NonNull TrackingType type, @Nullable TrackingTypeData trackingData);

    long getSavedPosition(String id);

    void setSavedPosition(String id, long value);

    void onActivityResume();

    boolean isInPIP();

    void onShareVideo(String headline, String url);

    void onMute(boolean isMute);

    long getAdType();

    void setFullscreen(boolean full);
}