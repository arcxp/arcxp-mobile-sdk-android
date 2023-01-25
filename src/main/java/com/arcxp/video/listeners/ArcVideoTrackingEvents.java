package com.arc.arcvideo.listeners;

import com.arc.arcvideo.model.ArcVideo;
import com.arc.arcvideo.model.TrackingType;
import com.arc.arcvideo.model.TrackingTypeData;

/**
 * @deprecated Use {@link ArcVideoEventsListener}
 */
public interface ArcVideoTrackingEvents {

    void onVideoTrackingEvent(TrackingType type, TrackingTypeData.TrackingVideoTypeData videoData);
    void onAdTrackingEvent(TrackingType type, TrackingTypeData.TrackingAdTypeData adData);
    void onSourceTrackingEvent(TrackingType type, TrackingTypeData.TrackingSourceTypeData source);
    void onError(TrackingType type, TrackingTypeData.TrackingErrorTypeData video);
}
