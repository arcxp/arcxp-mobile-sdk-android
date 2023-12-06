package com.arcxp.video.util

import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData

import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test

class TrackingExtensionsTest {


    @RelaxedMockK lateinit var trackingType: TrackingType
    @RelaxedMockK lateinit var video: ArcVideo
    @RelaxedMockK lateinit var eventTracker: ArcVideoEventsListener
    private val sessionId = "sessionId"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `eventTracking when TrackingTypeData TrackingVideoTypeData`() {
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)

        eventTracking(trackingType, trackingData, video, sessionId, eventTracker)

        verifySequence {
            trackingData.arcVideo = video
            trackingData.sessionId = sessionId
            eventTracker.onVideoTrackingEvent(trackingType, trackingData)
        }
    }

    @Test
    fun `eventTracking when TrackingTypeData null`() {
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val video = mockk<ArcVideo>()

        eventTracking(trackingType, null, video, sessionId, eventTracker)

        verify(exactly = 0) {
            eventTracker.onVideoTrackingEvent(trackingType, trackingData)
        }
    }


    @Test
    fun `eventTracking when TrackingTypeData TrackingVideoTypeData video is null`() {
        val trackingData = mockk<TrackingTypeData.TrackingVideoTypeData>(relaxed = true)
        val video = mockk<ArcVideo>()

        eventTracking(trackingType, trackingData, null, sessionId, eventTracker)

        verify(exactly = 0) {
            eventTracker.onVideoTrackingEvent(trackingType, trackingData)
        }
    }
    @Test
    fun `eventTracking when TrackingTypeData TrackingAdTypeData`() {
        val trackingData = mockk<TrackingTypeData.TrackingAdTypeData>(relaxed = true)

        eventTracking(trackingType, trackingData, video, sessionId, eventTracker)

        verifySequence {
            trackingData.sessionId = sessionId
            eventTracker.onAdTrackingEvent(trackingType, trackingData)
        }
    }

    @Test
    fun `eventTracking when TrackingTypeData TrackingSourceTypeData`() {
        val trackingData = mockk<TrackingTypeData.TrackingSourceTypeData>(relaxed = true)

        eventTracking(trackingType, trackingData, video, sessionId, eventTracker)

        verifySequence {
            trackingData.sessionId = sessionId
            eventTracker.onSourceTrackingEvent(trackingType, trackingData)
        }
    }

    @Test
    fun `eventTracking when TrackingTypeData TrackingErrorTypeData`() {
        val trackingData = mockk<TrackingTypeData.TrackingErrorTypeData>(relaxed = true)

        eventTracking(trackingType, trackingData, video, sessionId, eventTracker)

        verifySequence {
            trackingData.sessionId = sessionId
            eventTracker.onError(trackingType, trackingData)
        }
    }
}