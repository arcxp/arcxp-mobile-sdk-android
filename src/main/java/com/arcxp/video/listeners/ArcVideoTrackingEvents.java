package com.arcxp.video.listeners;

import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;

/**
 * @deprecated Use {@link ArcVideoEventsListener}
 */
public interface ArcVideoTrackingEvents {

    void onVideoTrackingEvent(TrackingType type, TrackingTypeData.TrackingVideoTypeData videoData);
    void onAdTrackingEvent(TrackingType type, TrackingTypeData.TrackingAdTypeData adData);
    void onSourceTrackingEvent(TrackingType type, TrackingTypeData.TrackingSourceTypeData source);
    void onError(TrackingType type, TrackingTypeData.TrackingErrorTypeData video);
}
