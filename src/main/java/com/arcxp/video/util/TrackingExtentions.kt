package com.arcxp.video.util

import com.arc.arcvideo.listeners.ArcVideoEventsListener
import com.arc.arcvideo.listeners.VideoPlayer
import com.arc.arcvideo.model.*

internal fun eventTracking(trackingType: TrackingType,
                           trackingData: TrackingTypeData?,
                           videoPlayer: VideoPlayer?,
                           sessionId: String?,
                           eventTracker: ArcVideoEventsListener) = trackingData?.let { data ->
    when (data) {
        is TrackingTypeData.TrackingVideoTypeData -> videoPlayer?.video?.let { video ->
            data.arcVideo = video
            data.sessionId = sessionId
            eventTracker.onVideoTrackingEvent(trackingType, data)
        }
        is TrackingTypeData.TrackingAdTypeData -> {
            data.sessionId = sessionId
            eventTracker.onAdTrackingEvent(trackingType, data)
        }
        is TrackingTypeData.TrackingSourceTypeData -> {
            data.sessionId = sessionId
            eventTracker.onSourceTrackingEvent(trackingType, data)
        }
        is TrackingTypeData.TrackingErrorTypeData -> {
            data.sessionId = sessionId
            eventTracker.onError(trackingType, data)
        }
    }
}
