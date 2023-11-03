package com.arcxp.video.players

import android.net.Uri
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.MediaItem
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class CaptionsManagerTest {

    @MockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var mConfig: ArcXPVideoConfig

    @RelaxedMockK
    private lateinit var mListener: VideoListener


    private val expectedId = "123"
    private val expectedVolume = 0.67f
    private val subtitleUrl = "mock subtitle url"
    private val expectedSavedPosition = 12345L
    private val expectedPosition = 987453L
    private val expectedStartPosition = 123L
    private val renderError = "An error occurred during playback."
    private val sourceError = "An error occurred during playback."
    private val unknownError = "An unknown error occurred while trying to play the video."
    private val mHeadline = "headline"
    private val mShareUrl = "shareUrl"
    private val mArtWorkUrl = "artworkUrl"
    private val mockPackageName = "packageName"
    private val expectedIncrement = 10000
    private val expectedSessionId = "sessionId"
    private val expectedUrl = "expectedUrl"
    private val mediaItem = MediaItem.fromUri(expectedUrl)

    private val expectedTimeScrubColor = 342
    private val expectedTimePlayColor = 3421
    private val expectedTimeUnPlayedColor = 3422
    private val expectedBufferedColor = 3423
    private val expectedAdPlayedColor = 342367
    private val expectedAdMarkerColor = 342378
    private val expectedCurrentPosition = 83746L
    private val expectedPeriodPosition = 83744L
    private val expectedAdjustedPosition = 2L
    private val expectedPeriodIndex = 7


    private  lateinit var testObject: CaptionsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testObject = CaptionsManager(playerState, utils, mConfig, mListener)
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }



    @Test
    fun `createMediaSourceWithCaptions throws exception, and is handled`() {
        val message = "createMediaSourceWithCaptions exception"
        val exception = Exception(message)
        val newId = "382764"
        val arcVideo = ArcVideo(
            newId,
            "uuid",
            expectedStartPosition,
            false,
            false,
            100,
            "shareUrl",
            "headline",
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            subtitleUrl,
            "source",
            mockk(),
            false,
            false,
            false,
            ArcXPVideoConfig.CCStartMode.DEFAULT
        )
        mockkStatic(Uri::class)
        every { Uri.parse(newId) } throws exception

        every { playerState.mVideo } returns arcVideo

        testObject.createMediaSourceWithCaptions()

        verifySequence {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, message, exception)
        }
    }

    //TODO test rest of this class
}