package com.arcxp.video.util

import android.content.Context
import android.view.MotionEvent
import com.arcxp.commons.util.Utils
import com.arcxp.video.ArcVideoManager
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.listeners.VideoPlayer
import com.arcxp.video.model.*
import com.arcxp.video.service.AdUtils
import com.arcxp.video.service.AdUtils.Companion.callBeaconUrl
import com.arcxp.video.views.VideoFrameLayout
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TrackingHelperTest {

    private lateinit var testObject: TrackingHelper

    @RelaxedMockK
    lateinit var videoManager: ArcVideoManager
    @RelaxedMockK
    lateinit var config: ArcXPVideoConfig
    @RelaxedMockK
    lateinit var mContext: Context
    @RelaxedMockK
    lateinit var mLayout: VideoFrameLayout
    @RelaxedMockK
    lateinit var mListener: VideoListener
    @RelaxedMockK
    lateinit var avails: AvailList
    @RelaxedMockK
    lateinit var adInfo: AdInfo
    @RelaxedMockK
    lateinit var lastHandledEvent: TrackingType
    @RelaxedMockK
    lateinit var utils: com.arcxp.video.util.Utils
    @RelaxedMockK
    var omidHelper: OmidHelper? = null
    @RelaxedMockK
    lateinit var palHelper: PalHelper
    @RelaxedMockK
    lateinit var videoPlayer: VideoPlayer
    var adVerifications: List<AdVerification> = listOf(
        AdVerification(
            listOf(JavascriptResource("omid", "http://omid.com")),
            "vendor",
            "params"
        )
    )

    private val videoId = "expected Video ID"
    private val descriptionUrl = "description URL"
    private val beaconUrl = "beacon url"
    private val durationInSeconds = 234.34
    private val expectedLength = durationInSeconds.toFloat() * 1000.0f
    private val expectedVolume = 1.0f
    private val expectedTimeInMillis = 23874969L

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { config.isEnablePAL } returns true
        every { config.isEnableOmid } returns true
        every { config.isLoggingEnabled } returns true
        every { videoManager.videoPlayer} returns videoPlayer
        every { utils.createOmidHelper(mContext, config, mLayout, videoPlayer)} returns omidHelper
        every { utils.createPalHelper(mContext, config, mLayout, mListener)} returns palHelper

        mockkConstructor(ArcAd::class)
        every { anyConstructed<ArcAd>().companionAds = any() } just Runs
        mockkObject(AdUtils.Companion)
        every { callBeaconUrl(beaconUrl) } just Runs
        every { avails.avails } returns listOf(
            VideoAdAvail(
                listOf(adInfo),
                "availId",
                "duration",
                12.2,
                "startTime",
                13.4
            )
        )
        //every { lastHandledEvent } returns TrackingType.ON_PLAY_PAUSED
        every { adInfo.companionAd } returns listOf(mockk())
        every { adInfo.adId } returns "ad id"
        every { adInfo.adTitle } returns "ad title"
        every { adInfo.adVerifications } returns adVerifications
        every { adInfo.durationInSeconds } returns durationInSeconds
        every { adInfo.mediaFiles } returns MediaFiles("mez", listOf(mockk()))
        every { videoManager.isClosedCaptionTurnedOn } returns true
        every { videoManager.isLive } returns false
        mockkObject(Utils)
        every { Utils.currentTimeInMillis() } returns expectedTimeInMillis

        testObject = TrackingHelper(videoId, videoManager, config, mContext, mLayout, mListener, utils)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `checkTracking handles event of type Start`() {
        val position = 1500L
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slots = mutableListOf<TrackingTypeData>()
        clearAllMocks(answers = false)

        testObject.checkTracking(position)

        verifySequence {
            videoManager.isClosedCaptionTurnedOn
            videoManager.enableClosedCaption(false)
            videoManager.enableClosedCaption(true)
            omidHelper?.init(adVerifications)
            omidHelper?.mediaEventsStart(expectedLength, expectedVolume)
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_STARTED, capture(slots))

//            mListener.onTrackingEvent(TrackingType.AD_COMPANION_INFO, any())//TODO for some reason line 281 is F event.adInfo.companionAd is executing but not recording the value, run in debugger it will pass..
            mListener.onTrackingEvent(TrackingType.AD_MEDIA_FILES, capture(slots))
            callBeaconUrl(beaconUrl)
        }
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slots[0])
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slots[1])
    }

    @Test
    fun `checkTracking handles event of type firstQuartile`() {
        val position = 2500L
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "99.0", 99.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slot = slot<TrackingTypeData>()
        clearAllMocks(answers = false)

        testObject.checkTracking(position)

        verifySequence {
            omidHelper?.mediaEventsFirstQuartile()
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_25, capture(slot))
            callBeaconUrl(beaconUrl)
        }
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slot.captured)
    }

    @Test
    fun `checkTracking handles event of type midpoint`() {
        val position = 3500L
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "99.0", 99.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slot = slot<TrackingTypeData>()

        clearAllMocks(answers = false)

        testObject.checkTracking(position)

        verifySequence {
            omidHelper?.mediaEventsMidpoint()
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_50, capture(slot))
            callBeaconUrl(beaconUrl)
        }
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slot.captured)

    }

    @Test
    fun `checkTracking handles event of type thirdQuartile`() {
        val position = 4500L
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "99.0", 99.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slot = slot<TrackingTypeData>()
        clearAllMocks(answers = false)

        testObject.checkTracking(position)

        verifySequence {
            omidHelper?.mediaEventsThirdQuartile()
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_75, capture(slot))
            callBeaconUrl(beaconUrl)
        }
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slot.captured)
    }

    @Test
    fun `checkTracking handles event of type complete`() {
        val position = 5001L
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "99.0", 99.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slot = slot<TrackingTypeData>()
        clearAllMocks(answers = false)

        testObject.checkTracking(position)

        verifySequence {
            omidHelper?.mediaEventsComplete()
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_COMPLETED, capture(slot))
            callBeaconUrl(beaconUrl)
        }
        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slot.captured)
    }

//    @Test
//    fun `checkTracking handles event of type impression`() {
//        val position = 6666L
//        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "99.0", 99.0)
//        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
//        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
//        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
//        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
//        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "6.0", 6.0)
//        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
//        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7)
//        every { adInfo.trackingEvents } returns trackingEvents
//        testObject.initVideo(descriptionUrl)
//        testObject.addEvents(avails, 12)
//        val slot = slot<TrackingTypeData>()
//        clearAllMocks(answers = false)
//
//        testObject.checkTracking(position)
//
//        verifySequence {
////            omidHelper.adEventsImpressionOccurred()
//            mListener.onTrackingEvent(TrackingType.AD_IMPRESSION, capture(slot))
//            callBeaconUrl(beaconUrl)
//        }
//        assertTrackingTypeDataMatchesMockedData(position, trackingTypeData = slot.captured)
//
//    }//think we don't need this but leaving it commented while we have the relevant code commented

    @Test
    fun `checkTracking handles event of type clickThrough`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        val slot = mutableListOf<TrackingTypeData>()
        clearAllMocks(answers = false)

        testObject.checkTracking(7777)

        verify(exactly = 1) {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
        }
        verify(exactly = 3) {
            callBeaconUrl(beaconUrl)
        }
    }

    @Test
    fun `checkTracking on last event`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        clearAllMocks(answers = false)

        testObject.checkTracking(7777)

        verify(exactly = 1) {
            mListener.onTrackingEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, any())
            omidHelper?.clear()
        }
    }

    @Test
    fun `checkTracking throws exception`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        clearAllMocks(answers = false)

        testObject.checkTracking(7777)

        verify(exactly = 1) {
            mListener.onTrackingEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, any())
            omidHelper?.clear()
        }
    }

    @Test
    fun `addEvents creates event given ad count has changed`() {

        val trackingEvent = mockk<TrackingEvent>()
        val trackingEvents = listOf(
            trackingEvent,
            trackingEvent,
            trackingEvent,
            trackingEvent,
            trackingEvent,
            trackingEvent
        )
        every { adInfo.trackingEvents } returns trackingEvents
        every { trackingEvent.startTimeInSeconds } returns 3.3
        every { trackingEvent.eventType } returns "start"

        testObject.addEvents(avails, 12)

        val actual = testObject.getEventList()[0]

        assertEquals("availId", actual.availId)
        assertEquals((3.3 * 1000).toLong(), actual.timestamp)
        assertEquals(adInfo, actual.adInfo)
        assertFalse(actual.isLast)
        assertFalse(actual.handled)
        assertEquals(0, actual.count)
        assertEquals(1, actual.total)
        assertEquals(trackingEvent, actual.trackingEvent)
    }

//    @Test
//    fun `addEvents reprocesses avail`() {
//        val handledAvails = mockk<HashMap<String, Int>>()
//
//        val trackingEvent = mockk<TrackingEvent>()
//        val trackingEvents = listOf(
//            trackingEvent,
//            trackingEvent,
//            trackingEvent,
//            trackingEvent,
//            trackingEvent,
//            trackingEvent
//        )
//        every { adInfo.trackingEvents } returns trackingEvents
//        every { trackingEvent.startTimeInSeconds } returns 3.3
//        every { trackingEvent.eventType } returns "start"
//
//        every { handledAvails.get("availId") } returns 100
//
//        testObject.addEvents(avails, 12)
//
//        verify (exactly = 1) {
//            testObject.adjustEvents(avails.avails?.get(1)!!, 12)
//        }
//    }

    @Test
    fun `addEvents does not create event if should have already happened`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "6.0", 6.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)

        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7)
        every { adInfo.trackingEvents } returns trackingEvents

        testObject.addEvents(avails, 9001)
        assertTrue(testObject.getEventList().isEmpty())
    }

    @Test
    fun `addEvents does not create ad event given ad count less than six`() {
        val trackingEvent = mockk<TrackingEvent>()
        val trackingEvents =
            listOf(trackingEvent, trackingEvent, trackingEvent, trackingEvent, trackingEvent)
        every { adInfo.trackingEvents } returns trackingEvents
        every { trackingEvent.startTimeInSeconds } returns 3.3
        every { trackingEvent.eventType } returns "start"

        testObject.addEvents(avails, 12)

        assertTrue(testObject.getEventList().isEmpty())
    }

    @Test
    fun `addEvents adjust removes matching events from event list`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "6.0", 6.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)

        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        val avails2 = mockk<AvailList>()
        every { avails2.avails } returns listOf(
            VideoAdAvail(
                emptyList(),
                "availId",
                "duration",
                12.2,
                "startTime",
                13.4
            )
        )

        testObject.addEvents(avails, 12)
        testObject.addEvents(avails2, 16)

        assertTrue(testObject.getEventList().isEmpty())
    }

    @Test
    fun `addEvents adjust adds new events to event list`() {
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "2.0", 2.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "3.0", 3.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "4.0", 4.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "5.0", 5.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "6.0", 6.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)

        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7)
        every { adInfo.trackingEvents } returns trackingEvents
        val avails2 = mockk<AvailList>()
        every { avails2.avails } returns listOf(
            VideoAdAvail(
                listOf(adInfo),
                "availId2",
                "duration",
                12.2,
                "startTime",
                13.4
            )
        )

        testObject.addEvents(avails, 12)
        testObject.addEvents(avails2, 16)

        assertEquals(14, testObject.getEventList().size)
    }

    @Test
    fun `initVideo creates oMid Helper if enabled`() {
        assertNull(testObject.getOMidHelper())

        testObject.initVideo(descriptionUrl)

        assertNotNull(testObject.getOMidHelper())
    }

    @Test
    fun `initVideo does not create oMid Helper`() {
        every { config.isEnableOmid } returns false

        testObject.initVideo(descriptionUrl)

        assertNull(testObject.getOMidHelper())
    }

    @Test
    fun `initVideo creates PAL Helper and inits if enabled`() {
        assertNull(testObject.getPalHelper())

        testObject.initVideo(descriptionUrl)

        verifySequence {
            palHelper.initVideo(descriptionUrl)
        }
        assertNotNull(testObject.getPalHelper())
    }

    @Test
    fun `initVideo does not create PAL Helper`() {
        every { config.isEnablePAL } returns false

        testObject.initVideo(descriptionUrl)

        verify(exactly = 0) {
            palHelper.initVideo(descriptionUrl)
        }
    }

    @Test
    fun `initAdTracking calls omidHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.initAdTracking(adVerifications)

        verifySequence {
            omidHelper?.init(adVerifications)
        }
    }

    @Test
    fun `initAdTracking does not call omidHelper`() {
        every { config.isEnableOmid } returns false

        testObject.initVideo(descriptionUrl)

        testObject.initAdTracking(adVerifications)

        verify(exactly = 0) {
            omidHelper?.init(adVerifications)
        }
    }

    @Test
    fun `onDestroy calls omidHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.onDestroy()

        verifySequence {
            omidHelper?.onDestroy()
        }
    }

    @Test
    fun `onDestroy does not call omidHelper`() {
        every { config.isEnableOmid } returns false
        testObject.initVideo(descriptionUrl)

        testObject.onDestroy()

        verify(exactly = 0) {
            omidHelper?.onDestroy()
        }
    }

    @Test
    fun `pausePlay calls omidHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.pausePlay()

        verifySequence {
            omidHelper?.mediaEventsPause()
        }
    }

    @Test
    fun `pausePlay does not call omidHelper`() {
        every { config.isEnableOmid } returns false

        testObject.initVideo(descriptionUrl)

        testObject.pausePlay()

        verify(exactly = 0) {
            omidHelper?.mediaEventsPause()
        }
    }

    @Test
    fun `resumePlay calls omidHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.resumePlay()

        verifySequence {
            omidHelper?.mediaEventsResume()
        }
    }

    @Test
    fun `resumePlay does not call omidHelper`() {
        every { config.isEnableOmid } returns false
        testObject.initVideo(descriptionUrl)

        testObject.resumePlay()

        verify(exactly = 0) {
            omidHelper?.mediaEventsResume()
        }
    }

    @Test
    fun `fullscreen calls omidHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.fullscreen()

        verifySequence {
            omidHelper?.mediaEventsFullscreen()
        }
    }

    @Test
    fun `fullscreen does not call omidHelper`() {
        every { config.isEnableOmid } returns false
        testObject.initVideo(descriptionUrl)

        testObject.fullscreen()

        verify(exactly = 0) {
            omidHelper?.mediaEventsFullscreen()
        }
    }

    @Test
    fun `normalScreen calls omidHelper`() {

        testObject.initVideo(descriptionUrl)
        testObject.normalScreen()

        verifySequence {
            omidHelper?.mediaEventsNormalScreen()
        }
    }

    @Test
    fun `normalScreen does not call omidHelper`() {
        every { config.isEnableOmid } returns false
        testObject.initVideo(descriptionUrl)
        testObject.normalScreen()

        verify(exactly = 0) {
            omidHelper?.mediaEventsNormalScreen()
        }
    }

    @Test
    fun `volumeChange calls omidHelper`() {
        val volume = 0.234f
        testObject.initVideo(descriptionUrl)

        testObject.volumeChange(volume)

        verifySequence {
            omidHelper?.mediaEventsVolumeChange(volume)
        }
    }

    @Test
    fun `volumeChange does not call omidHelper`() {
        every { config.isEnableOmid } returns false
        val volume = 0.234f
        testObject.initVideo(descriptionUrl)

        testObject.volumeChange(volume)

        verify(exactly = 0) {
            omidHelper?.mediaEventsVolumeChange(volume)
        }
    }

    @Test
    fun `onPlaybackStart calls trackingHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.onPlaybackStart()

        verify(exactly = 1) {
            palHelper.sendPlaybackStart()
        }
    }

    @Test
    fun `onPlaybackStart does not call trackingHelper`() {
        every { config.isEnablePAL } returns false
        testObject.initVideo(descriptionUrl)

        testObject.onPlaybackStart()

        verify(exactly = 0) {
            palHelper.sendPlaybackStart()
        }
    }

    @Test
    fun `onPlaybackEnd calls trackingHelper`() {
        testObject.initVideo(descriptionUrl)

        testObject.onPlaybackEnd()

        verify(exactly = 1) {
            palHelper.sendPlaybackEnd()
        }
    }

    @Test
    fun `onPlaybackEnd does not call trackingHelper`() {
        every { config.isEnablePAL } returns false
        testObject.initVideo(descriptionUrl)

        testObject.onPlaybackEnd()

        verify(exactly = 0) {
            palHelper.sendPlaybackEnd()
        }
    }

    @Test
    fun `onTouch when mCurrentAd is null`() {
        val builder = mockk<ArcVideo.Builder>(relaxed = true)
        val video = mockk<ArcVideo>(relaxed = true)
        val slot = slot<TrackingTypeData>()
        mockkConstructor(ArcVideo.Builder::class)
        every { anyConstructed<ArcVideo.Builder>().setUuid(any()) } returns builder
        every { builder.build() } returns video

        testObject.onTouch(mockk(), 123L)

        verifySequence {
            anyConstructed<ArcVideo.Builder>().setUuid(videoId)
            builder.build()
            mListener.onTrackingEvent(TrackingType.ON_PLAYER_TOUCHED, capture(slot))
        }
        assertTrue(slot.captured is TrackingTypeData.TrackingVideoTypeData)
        assertTrue((slot.captured as TrackingTypeData.TrackingVideoTypeData).position == 123L)
        assertTrue((slot.captured as TrackingTypeData.TrackingVideoTypeData).percentage == 0)
        assertTrue((slot.captured as TrackingTypeData.TrackingVideoTypeData).arcVideo == video)
    }

    @Test
    fun `onTouch when videoManager is live`() {
        every { videoManager.isLive } returns true
        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        val builder = mockk<ArcVideo.Builder>(relaxed = true)
        val video = mockk<ArcVideo>(relaxed = true)
        val trackingDataSlot = slot<TrackingTypeData>()
        mockkConstructor(ArcVideo.Builder::class)
        every { anyConstructed<ArcVideo.Builder>().setUuid(any()) } returns builder
        every { builder.build() } returns video

        testObject.onTouch(event, 123L)

        verifySequence {
            anyConstructed<ArcVideo.Builder>().setUuid(videoId)
            builder.build()
            mListener.onTrackingEvent(TrackingType.ON_PLAYER_TOUCHED, capture(trackingDataSlot))
        }
        assertTrue(trackingDataSlot.captured is TrackingTypeData.TrackingVideoTypeData)
        assertTrue((trackingDataSlot.captured as TrackingTypeData.TrackingVideoTypeData).position == 123L)
        assertTrue((trackingDataSlot.captured as TrackingTypeData.TrackingVideoTypeData).percentage == 0)
        assertTrue((trackingDataSlot.captured as TrackingTypeData.TrackingVideoTypeData).arcVideo == video)
    }


    @Test
    fun `onTouch when mCurrentAd is non null `() {
        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        testObject.onTouch(event, 123L)

        verifySequence {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
            omidHelper?.mediaEventsOnTouch()
            palHelper.sendAdImpression()
            omidHelper?.adEventsImpressionOccurred()
            mListener.onTrackingEvent(TrackingType.AD_CLICKED, capture(slot))
            palHelper.onTouch(event, testObject.getMCurrentAd())
        }

        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[0]
        )
        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[1]
        )
    }

    @Test
    fun `onTouch when mCurrentAd is non null logging enabled`() {
        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        every { config.isLoggingEnabled } returns false

        testObject.onTouch(event, 123L)

        verifySequence {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
            omidHelper?.mediaEventsOnTouch()
            palHelper.sendAdImpression()
            omidHelper?.adEventsImpressionOccurred()
            mListener.onTrackingEvent(TrackingType.AD_CLICKED, capture(slot))
            palHelper.onTouch(event, testObject.getMCurrentAd())
        }

        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[0]
        )
        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[1]
        )
    }

    @Test
    fun `onTouch when mCurrentAd is non null mCurrent ad is null`() {
        val mCurrentAd = mockk<ArcAd>()

        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        every { mCurrentAd?.adId } returns null

        testObject.onTouch(event, 123L)

        verifySequence {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
            omidHelper?.mediaEventsOnTouch()
            palHelper.sendAdImpression()
            omidHelper?.adEventsImpressionOccurred()
            mListener.onTrackingEvent(TrackingType.AD_CLICKED, capture(slot))
            palHelper.onTouch(event, testObject.getMCurrentAd())
        }

        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[0]
        )
        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[1]
        )
    }

    @Test
    fun `onTouch when mCurrentAd is non null omid and pal disabled`() {
        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        every { config.isEnableOmid } returns false
        every { config.isEnablePAL } returns false
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        testObject.onTouch(event, 123L)

        verifySequence {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
            mListener.onTrackingEvent(TrackingType.AD_CLICKED, capture(slot))
        }

        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[0]
        )
        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[1]
        )
    }

    @Test
    fun `onTouch when mCurrentAd is non null omid and pal disabled logging disabled`() {
        val position = 7777L
        val slot = mutableListOf<TrackingTypeData>()
        val event1 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "start", "1.0", 1.0)
        val event2 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "firstQuartile", "99.0", 99.0)
        val event3 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "midpoint", "99.0", 99.0)
        val event4 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "thirdQuartile", "99.0", 99.0)
        val event5 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "complete", "99.0", 99.0)
        val event6 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "impression", "99.0", 99.0)
        val event7 = TrackingEvent(listOf(beaconUrl), "4", 4.0, "id", "clickThrough", "7.0", 7.0)
        val trackingEvents = listOf(event1, event2, event3, event4, event5, event6, event7, event1)
        every { adInfo.trackingEvents } returns trackingEvents
        every { config.isEnableOmid } returns false
        every { config.isEnablePAL } returns false
        every { config.isLoggingEnabled } returns false
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)
        testObject.checkTracking(position)
        clearAllMocks(answers = false)
        slot.clear()
        unmockkConstructor(ArcAd::class)
        val event = mockk<MotionEvent>()

        testObject.onTouch(event, 123L)

        verifySequence {
            mListener.onTrackingEvent(TrackingType.AD_CLICKTHROUGH, capture(slot))
            mListener.onTrackingEvent(TrackingType.AD_CLICKED, capture(slot))
        }

        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[0]
        )
        assertTrackingTypeDataMatchesMockedData(
            position,
            trackingTypeData = slot[1]
        )
    }

    @Test
    fun `onTouch sets lastTouchTime `() {
        assertEquals(0L, testObject.getLastTouchTime())

        testObject.onTouch(mockk(), 123L)

        assertEquals(expectedTimeInMillis, testObject.getLastTouchTime())
    }

    private fun assertTrackingTypeDataMatchesMockedData(
        position: Long,
        trackingTypeData: TrackingTypeData
    ) {
        assertTrue(trackingTypeData is TrackingTypeData.TrackingAdTypeData)
        assertTrue((trackingTypeData as TrackingTypeData.TrackingAdTypeData).position == position)
        assertTrue(trackingTypeData.arcAd!!.adId == "ad id")
        assertTrue(trackingTypeData.arcAd!!.adDuration == 234.34)
        assertTrue(trackingTypeData.arcAd!!.adTitle == "ad title")
        assertTrue(trackingTypeData.arcAd!!.clickthroughUrl == beaconUrl)
    }

    @Test
    fun `handleMessage no logging`() {
        every { config.isLoggingEnabled } returns false

    }

    @Test
    fun `addEvents bad avail count`() {
        every { avails.avails } returns null
        testObject.initVideo(descriptionUrl)
        testObject.addEvents(avails, 12)

        verify(exactly = 0) {
            config.isLoggingEnabled
        }
    }
}