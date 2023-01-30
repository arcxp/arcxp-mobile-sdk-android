package com.arcxp.video.util

import android.content.Context
import android.view.MotionEvent
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.views.VideoFrameLayout
import com.google.ads.interactivemedia.pal.ConsentSettings
import com.google.ads.interactivemedia.pal.NonceLoader
import com.google.ads.interactivemedia.pal.NonceManager
import com.google.ads.interactivemedia.pal.NonceRequest
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PalHelperTest {

    @RelaxedMockK lateinit var context: Context
    @RelaxedMockK lateinit var config: ArcMediaPlayerConfig
    @RelaxedMockK lateinit var layout: VideoFrameLayout
    @RelaxedMockK lateinit var utils: Utils
    @RelaxedMockK lateinit var nonceLoader: NonceLoader
    @RelaxedMockK lateinit var nonceManager: NonceManager
    @RelaxedMockK lateinit var mListener: VideoListener
    @RelaxedMockK lateinit var nonceData: TrackingTypeData.TrackingAdTypeData

    private val descriptionUrl = "url"
    private val palPartnerName = "palPartnerName"
    private val palPPid = "palPPid"
    private val layoutHeight = 234
    private val layoutWidth = 265
    private val expectedAdAutoPlay = true
    private val expectedAdPlayMuted = false
    private val expectedPlayerType = "Exoplayer"
    private val expectedPlayerVersion = "2.11.6"

    private lateinit var testObject: PalHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { utils.createNonceLoader(context) } returns nonceLoader
        every { layout.width } returns layoutWidth
        every { layout.height } returns layoutHeight
        every { utils.createNonceData() } returns nonceData

        testObject = PalHelper(context, config, layout, utils, mListener)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `init given pal enabled and config items non blank creates nonceLoader`() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid

        testObject.initVideo("url")

        assertEquals(nonceLoader, testObject.getNonceLoader())
    }

    @Test
    fun `initVideo builds request and sets nonceManager from listener result `() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid
        every { config.exoplayerVersion } returns expectedPlayerVersion
        testObject.initVideo("url")
        val nonceRequestSlot = slot<NonceRequest>()
        val expectedRequest = NonceRequest.builder()
            .descriptionURL(descriptionUrl)
            .omidVersion(config.palVersionName)
            .omidPartnerName(config.palPartnerName)
            .playerType(expectedPlayerType)
            .playerVersion(expectedPlayerVersion)
            .ppid(config.palPpid)
            .videoPlayerHeight(layoutWidth)
            .videoPlayerWidth(layoutHeight)
            .willAdAutoPlay(expectedAdAutoPlay)
            .willAdPlayMuted(expectedAdPlayMuted)
            .build()
        every { utils.createNonceRequest(config, descriptionUrl, layout)} returns expectedRequest
        val task = mockk<Task<NonceManager>>(relaxed = true)
        val loadNonceManagerOnSuccessListener = slot<OnSuccessListener<NonceManager>>()
        every { nonceLoader.loadNonceManager(any()) } returns task
        every { task.addOnSuccessListener(any()) } returns task

        testObject.initVideo(descriptionUrl)
        verifySequence {
            nonceLoader.loadNonceManager(capture(nonceRequestSlot))
            nonceLoader.loadNonceManager(capture(nonceRequestSlot))
            task.addOnSuccessListener(capture(loadNonceManagerOnSuccessListener))
            task.addOnFailureListener(any())
        }
        loadNonceManagerOnSuccessListener.captured.onSuccess(nonceManager)

        assertTrue(nonceRequestSlot.captured == expectedRequest)
        assertEquals(nonceManager, testObject.getNonceManager())
    }

    @Test
    fun `onTouch sends touch event and current ad to manager`() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid
        initVideo()
        val event = mockk<MotionEvent>()
        val currentAd = mockk<ArcAd>()

        testObject.onTouch(event, currentAd)

        verifySequence {
            nonceManager.nonce
            nonceManager.sendAdTouch(event)
            nonceManager.sendAdClick()
        }

    }

    @Test
    fun `sendAdImpression triggers nonceManger method `() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid
        initVideo()

        testObject.sendAdImpression()

        verifySequence {
            nonceManager.nonce
            nonceManager.sendAdImpression() }
    }

    @Test
    fun `clear nulls nonceManager`() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid
        initVideo()
        assertNotNull(testObject.getNonceManager())

        testObject.clear()

        assertNull(testObject.getNonceManager())
    }

    @Test
    fun `init nulls nonceManager`() {
        every { config.isEnablePAL } returns true
        every { config.palPartnerName } returns palPartnerName
        every { config.palPpid } returns palPPid
        initVideo()
        assertNotNull(testObject.getNonceManager())

        testObject.initVideo("url")

        assertNull(testObject.getNonceManager())
    }

    @Test
    fun `sendPlaybackStart triggers nonceManger method `() {
        initVideo()
        clearAllMocks(answers = false)

        testObject.sendPlaybackStart()

        verifySequence {
            nonceManager.sendPlaybackStart()
            mListener.onTrackingEvent(TrackingType.PAL_VIDEO_START, nonceData)}
    }

    @Test
    fun `sendPlaybackEnd triggers nonceManger method `() {
        initVideo()
        clearAllMocks(answers = false)

        testObject.sendPlaybackEnd()
        verifySequence {
            nonceManager.sendPlaybackEnd()
            mListener.onTrackingEvent(TrackingType.PAL_VIDEO_END, nonceData)
        }
    }

    private fun initVideo() {
        testObject.initVideo("url")
        val task = mockk<Task<NonceManager>>(relaxed = true)
        val loadNonceManagerOnSuccessListener = slot<OnSuccessListener<NonceManager>>()
        every { nonceLoader.loadNonceManager(any())} returns task
        every { task.addOnSuccessListener(any()) } returns task
        testObject.initVideo((descriptionUrl))
        verify {
            task.addOnSuccessListener(capture(loadNonceManagerOnSuccessListener))
        }
        loadNonceManagerOnSuccessListener.captured.onSuccess(nonceManager)
    }
}