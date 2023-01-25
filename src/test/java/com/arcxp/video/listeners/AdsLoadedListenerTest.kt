package com.arc.arcvideo.listeners

import com.arc.arcvideo.model.ArcVideo
import com.arc.arcvideo.model.TrackingType
import com.arc.arcvideo.model.TrackingTypeData
import com.google.ads.interactivemedia.v3.api.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AdsLoadedListenerTest {

    private lateinit var testObject: AdsLoadedListener

    @RelaxedMockK private lateinit var mListener: VideoListener
    @RelaxedMockK private lateinit var player: VideoPlayer
    @RelaxedMockK private lateinit var adsManagerLoadedEvent: AdsManagerLoadedEvent
    @RelaxedMockK private lateinit var adsManager: AdsManager
    @RelaxedMockK private lateinit var factory: ImaSdkFactory
    @RelaxedMockK private lateinit var adsRenderingSettings: AdsRenderingSettings
    @RelaxedMockK private lateinit var adEvent: AdEvent

    private val expectedSessionId = "sessionId"
    private val expectedId = "id"
    private val expectedDuration = 23.234
    private val expectedTimeLinePosition = 342L
    private val expectedTitle = "Ad title"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        val configInfo = ArcVideo.Builder().setFocusSkipButton(true).build()
        every { adsManagerLoadedEvent.adsManager } returns adsManager
        mockkStatic(ImaSdkFactory::class)
        every { ImaSdkFactory.getInstance() } returns factory
        every { factory.createAdsRenderingSettings() } returns adsRenderingSettings
        every { adEvent.ad } returns mockk {
            every { adId } returns expectedId
            every { duration } returns expectedDuration
            every { title } returns expectedTitle
        }
        every { player.currentTimelinePosition } returns expectedTimeLinePosition

        testObject = AdsLoadedListener(mListener, configInfo, player, expectedSessionId)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `onAdsManagerLoaded sets focus skip button when config setting is true, initializes adsManager, and sets a Listener`() {

        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)

        verifySequence {
            adsRenderingSettings.focusSkipButtonWhenAvailable = true
            adsManager.init(adsRenderingSettings)
            adsManager.addAdEventListener(any())
        }
    }

    @Test
    fun `onAdsManagerLoaded listener given started and adType zero`() {
        //given
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.STARTED
        every { mListener.adType } returns 0
        clearAllMocks(answers = false)

        //when
        listenerSlot.captured.onAdEvent(adEvent)

        //then
        verifySequence {
            mListener.adType
            mListener.onTrackingEvent(TrackingType.PREROLL_AD_STARTED, capture(adDataSlot))
        }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given started and adType greater than zero`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.STARTED
        every { mListener.adType } returns 1
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence {
            mListener.adType
            mListener.onTrackingEvent(TrackingType.MIDROLL_AD_STARTED, capture(adDataSlot))
        }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given started and adType less than zero`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.STARTED
        every { mListener.adType } returns -1
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence {
            mListener.adType
            mListener.onTrackingEvent(TrackingType.POSTROLL_AD_STARTED, capture(adDataSlot))
        }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given completed and adType zero`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.COMPLETED
        every { mListener.adType } returns 0
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence { mListener.onTrackingEvent(TrackingType.PREROLL_AD_COMPLETED, capture(adDataSlot)) }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given completed after receiving start and adType greater than zero`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.STARTED
        every { mListener.adType } returns 1
        listenerSlot.captured.onAdEvent(adEvent)
        every { adEvent.type } returns AdEvent.AdEventType.COMPLETED
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence { mListener.onTrackingEvent(TrackingType.MIDROLL_AD_COMPLETED, capture(adDataSlot)) }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given completed and adType less than zero`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.STARTED
        every { mListener.adType } returns -1
        listenerSlot.captured.onAdEvent(adEvent)
        every { adEvent.type } returns AdEvent.AdEventType.COMPLETED
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence { mListener.onTrackingEvent(TrackingType.POSTROLL_AD_COMPLETED, capture(adDataSlot)) }
        assertAdDataValues(adDataSlot.captured)
    }

    @Test
    fun `onAdsManagerLoaded listener given loaded`() {
        val listenerSlot = slot<AdEvent.AdEventListener>()
        val adDataSlot = slot<TrackingTypeData.TrackingAdTypeData>()
        testObject.onAdsManagerLoaded(adsManagerLoadedEvent)
        verify { adsManager.addAdEventListener(capture(listenerSlot)) }
        every { adEvent.type } returns AdEvent.AdEventType.LOADED
        clearAllMocks(answers = false)

        listenerSlot.captured.onAdEvent(adEvent)

        verifySequence { mListener.onTrackingEvent(TrackingType.AD_LOADED, capture(adDataSlot)) }
        assertAdDataValues(adDataSlot.captured)
    }

    private fun assertAdDataValues(adData: TrackingTypeData.TrackingAdTypeData) {
        assertEquals(expectedTimeLinePosition, adData.position)
        assertEquals(expectedId, adData.arcAd!!.adId)
        assertEquals(expectedDuration, adData.arcAd!!.adDuration)
        assertEquals(expectedTitle, adData.arcAd!!.adTitle)
        assertEquals(expectedSessionId, adData.sessionId)
    }
}