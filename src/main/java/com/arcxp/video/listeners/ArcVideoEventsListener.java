package com.arcxp.video.listeners;

import androidx.annotation.Keep;

import com.arcxp.video.listeners.ArcVideoTrackingEvents;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;

/**
 *
 */
@Keep
public interface ArcVideoEventsListener extends ArcVideoTrackingEvents {
    void onVideoTrackingEvent(TrackingType type, TrackingTypeData.TrackingVideoTypeData videoData);
    void onAdTrackingEvent(TrackingType type, TrackingTypeData.TrackingAdTypeData adData);
    void onSourceTrackingEvent(TrackingType type, TrackingTypeData.TrackingSourceTypeData source);
    void onError(TrackingType type, TrackingTypeData.TrackingErrorTypeData video);
}
