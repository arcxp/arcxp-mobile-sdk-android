package com.arc.arcvideo.listeners;

import androidx.annotation.Keep;

import com.arc.arcvideo.model.TrackingType;
import com.arc.arcvideo.model.TrackingTypeData;

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
