package com.arcxp.video.players

import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.PlayerState
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingType.AD_CLICKED
import com.arcxp.video.model.TrackingType.AD_LOADED
import com.arcxp.video.model.TrackingType.AD_PAUSE
import com.arcxp.video.model.TrackingType.AD_PLAY_COMPLETED
import com.arcxp.video.model.TrackingType.AD_PLAY_STARTED
import com.arcxp.video.model.TrackingType.AD_RESUME
import com.arcxp.video.model.TrackingType.AD_SKIPPED
import com.arcxp.video.model.TrackingType.AD_SKIP_SHOWN
import com.arcxp.video.model.TrackingType.ALL_MIDROLL_AD_COMPLETE
import com.arcxp.video.model.TrackingType.VIDEO_25_WATCHED
import com.arcxp.video.model.TrackingType.VIDEO_50_WATCHED
import com.arcxp.video.model.TrackingType.VIDEO_75_WATCHED
import com.arcxp.video.model.TrackingTypeData
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.AD_BREAK_READY
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.ALL_ADS_COMPLETED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.CLICKED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.COMPLETED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.CUEPOINTS_CHANGED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.FIRST_QUARTILE
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.MIDPOINT
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.PAUSED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.SKIPPABLE_STATE_CHANGED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.SKIPPED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.STARTED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.TAPPED
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.THIRD_QUARTILE
import com.google.android.exoplayer2.ui.StyledPlayerView
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ArcAdEventListenerTest {

    @RelaxedMockK
    private lateinit var playerState: PlayerState

    @RelaxedMockK
    private lateinit var playerStateHelper: PlayerStateHelper

    @RelaxedMockK
    private lateinit var videoPlayer: ArcVideoPlayer

    @RelaxedMockK
    private lateinit var mLocalPlayerView: StyledPlayerView

    @MockK
    private lateinit var mConfig: ArcXPVideoConfig

    @MockK
    private lateinit var inputAd: Ad

    private val expectedAdId = "ad id"
    private val expectedDuration = 1234.0
    private val expectedTitle = "title"
    private val expectedClickThroughUrl = "url"


    private lateinit var testObject: ArcAdEventListener


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { playerState.mLocalPlayerView } returns mLocalPlayerView
        every { inputAd.adId } returns expectedAdId
        every { inputAd.duration } returns expectedDuration
        every { inputAd.title } returns expectedTitle
        every { inputAd.surveyUrl } returns expectedClickThroughUrl
        every { mConfig.isDisableControlsFully } returns false
        testObject = ArcAdEventListener(playerState, playerStateHelper, videoPlayer, mConfig)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }


    private fun verifyOnAdEvent(
        adEventType: AdEvent.AdEventType,
        getAdResult: Ad,
        trackingType: TrackingType,
    ) {

        val inputAdEvent = mockk<AdEvent>()
        val slot = slot<TrackingTypeData.TrackingAdTypeData>()

        inputAdEvent.apply {
            every { type } returns adEventType
            every { ad } returns getAdResult

        }
        clearAllMocks(answers = false)
        testObject.onAdEvent(inputAdEvent)


        inputAdEvent.apply {
            verify {
                type
                ad
                getAdResult?.let {
                    ad
                    ad
                    ad
                    ad
                }
            }
        }

        verify(exactly = 1) {
            playerStateHelper.onVideoEvent(trackingType, capture(slot))
        }
        if (getAdResult != null) {
            assertEquals(expectedAdId, slot.captured.arcAd!!.adId)

            assertEquals(expectedDuration, slot.captured.arcAd!!.adDuration)
            assertEquals(expectedTitle, slot.captured.arcAd!!.adTitle)
            assertEquals(expectedClickThroughUrl, slot.captured.arcAd!!.clickthroughUrl)
        }
        assertEquals(0L, slot.captured.position)
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is not null but mPlayer is null`() {
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, inputAd, AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with COMPLETED getAd is not null`() {
        verifyOnAdEvent(COMPLETED, inputAd, AD_PLAY_COMPLETED)
    }

    @Test
    fun `onAdEvent with FIRST_QUARTILE getAd is not null`() {
        verifyOnAdEvent(FIRST_QUARTILE, inputAd, VIDEO_25_WATCHED)
    }

    @Test
    fun `onAdEvent with MIDPOINT getAd is not null`() {
        verifyOnAdEvent(MIDPOINT, inputAd, VIDEO_50_WATCHED)
    }

    @Test
    fun `onAdEvent with THIRD_QUARTILE getAd is not null`() {
        verifyOnAdEvent(THIRD_QUARTILE, inputAd, VIDEO_75_WATCHED)
    }

    @Test
    fun `onAdEvent with AD_LOADED getAd is not null`() {
        every { videoPlayer.currentPosition } returns 0L
        verifyOnAdEvent(AdEvent.AdEventType.LOADED, inputAd, AD_LOADED)

        verify(exactly = 1) {
            videoPlayer.disableControls()
        }
    }

    @Test
    fun `onAdEvent with AD_BREAK_STARTED getAd is not null`() {
        every { videoPlayer.currentPosition } returns 0L
        verifyOnAdEvent(
            AdEvent.AdEventType.AD_BREAK_STARTED,
            inputAd,
            TrackingType.AD_BREAK_STARTED
        )
        verify(exactly = 1) {
            videoPlayer.disableControls()
        }
    }

    @Test
    fun `onAdEvent with AD_BREAK_READY calls disable controls`() {
        testObject.onAdEvent(mockk(relaxed = true) {
            every { type } returns AD_BREAK_READY
        })

        verify(exactly = 1) {
            videoPlayer.disableControls()
        }
        verify {
            playerStateHelper wasNot called
        }
    }

    @Test
    fun `onAdEvent with SKIPPABLE_STATE_CHANGED getAd is not null and mPlayer is not null`() {
        every { videoPlayer.currentPosition } returns 0L
        verifyOnAdEvent(SKIPPABLE_STATE_CHANGED, inputAd, AD_SKIP_SHOWN)
    }

    @Test
    fun `onAdEvent with PAUSED and ad is not paused`() {
        every { videoPlayer.currentPosition } returns 0L
        every { playerState.adPlaying } returns true
        every { playerState.adPaused } returns false

        verifyOnAdEvent(PAUSED, inputAd, AD_PAUSE)

        verify(exactly = 1) {
            playerState.currentPlayer!!.pause()
            playerState.adPaused = true
        }
    }

    @Test
    fun `onAdEvent with PAUSED and ad is paused`() {
        every { videoPlayer.currentPosition } returns 0L
        every { playerState.adPlaying } returns true
        every { playerState.adPaused } returns true

        verifyOnAdEvent(PAUSED, inputAd, AD_RESUME)

        verify(exactly = 1) {
            playerState.currentPlayer!!.play()
            playerState.adPaused = false
        }
    }

    @Test
    fun `onAdEvent with SKIPPED getAd is not null, not disable controls fully`() {
        every { mConfig.isDisableControlsFully } returns false
        verifyOnAdEvent(SKIPPED, inputAd, AD_SKIPPED)
        verify(exactly = 1) {
            playerState.firstAdCompleted = true
            playerState.disabledControlsForAd = false
            playerState.adPlaying = false
            mLocalPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with SKIPPED when controls fully disabled does not re enable`() {
        every { mConfig.isDisableControlsFully } returns true
        verifyOnAdEvent(SKIPPED, inputAd, AD_SKIPPED)
        verify(exactly = 1) {
            playerState.firstAdCompleted = true
            playerState.disabledControlsForAd = false
            playerState.adPlaying = false
        }
        verify(exactly = 0) {
            mLocalPlayerView.useController = any()
        }
    }

    @Test
    fun `onAdEvent with ALL_ADS_COMPLETED getAd is not null, not disable controls fully`() {
        every { mConfig.isDisableControlsFully } returns false
        verifyOnAdEvent(ALL_ADS_COMPLETED, inputAd, ALL_MIDROLL_AD_COMPLETE)
        verify(exactly = 1) {
            playerState.firstAdCompleted = true
            playerState.disabledControlsForAd = false
            playerState.adPlaying = false
            mLocalPlayerView.useController = true
        }
    }

    @Test
    fun `onAdEvent with ALL_ADS_COMPLETED when controls fully disabled does not re enable`() {
        every { mConfig.isDisableControlsFully } returns true
        verifyOnAdEvent(ALL_ADS_COMPLETED, inputAd, ALL_MIDROLL_AD_COMPLETE)
        verify(exactly = 1) {
            playerState.firstAdCompleted = true
            playerState.disabledControlsForAd = false
            playerState.adPlaying = false
        }
        verify(exactly = 0) {
            mLocalPlayerView.useController = any()
        }
    }

    @Test
    fun `onAdEvent with STARTED getAd is not null`() {
        verifyOnAdEvent(STARTED, inputAd, AD_PLAY_STARTED)
    }

    @Test
    fun `onAdEvent with CLICKED getAd is not null`() {
        verifyOnAdEvent(CLICKED, inputAd, AD_CLICKED)
    }

    @Test
    fun `onAdEvent with TAPPED getAd is not null`() {
        verifyOnAdEvent(TAPPED, inputAd, AD_CLICKED)
    }

    @Test
    fun `onAdEvent with other`() {
        val inputAdEvent = mockk<AdEvent>()
        inputAdEvent.apply {
            every { type } returns CUEPOINTS_CHANGED
            every { ad } returns inputAd

        }
        clearAllMocks(answers = false)
        testObject.onAdEvent(inputAdEvent)


        inputAdEvent.apply {
            verify {
                type
                ad
                inputAdEvent.ad
                inputAdEvent.ad
                inputAdEvent.ad
                inputAdEvent.ad
            }
        }
        verify { playerStateHelper wasNot called }
    }
}